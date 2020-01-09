package es.codeurjc.webchat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChatManager {

	private ConcurrentHashMap<String, Chat> chats = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
	
	private int maxChats;	
	private final Semaphore maxChatsSemaphore;
	
	private CountDownLatch newChatLatch = new CountDownLatch(0);
	
	public ChatManager(int maxChats) {
		this.maxChats = maxChats;
		this.maxChatsSemaphore = new Semaphore(this.maxChats, true);
	}

	public void newUser(User user) {
		// putIfAbsent() returns:
		// the previous value associated with the specified key,
		// or null if there was no mapping for the key
		if (users.putIfAbsent(user.getName(), user) != null) {
			throw new IllegalArgumentException("There is already a user with name \'"
					+ user.getName() + "\'");				
		}
	}

	public Chat newChat(String name, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {

		try {
			if (this.maxChatsSemaphore.tryAcquire(timeout, unit) == true) {

				Chat newChat = new Chat(this, name);

				// putIfAbsent() returns:
				// the previous value associated with the specified key,
				// or null if there was no mapping for the key
				if (chats.putIfAbsent(name, newChat) == null) {
					notifyUsersNewChat(newChat);
					return newChat;
				}
				// Chat already existed
				else {
					this.maxChatsSemaphore.release();
					return chats.get(name);				
				}
			}
			else
			{
				throw new TimeoutException("There is no enought capacity to create a new chat");
			}
		}
		catch ( InterruptedException e )
		{
			throw new TimeoutException("InterruptedException.");
		}
	}
	
	private void notifyUsersNewChat(Chat newChat) throws InterruptedException, TimeoutException {
		
		// Using a copy of users in chat to avoid concurrency problems.
		ArrayList<User> usersInChat = new ArrayList<>(users.values());
		
		ExecutorService executorService = Executors.newFixedThreadPool(usersInChat.size());
		CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executorService);
		
		// CountDownLatch must be created before any submit to avoid race conditions
		newChatLatch = new CountDownLatch(usersInChat.size());
		
		for (int i = 0; i < usersInChat.size(); i++) {			
			final int userIndex = i;
			completionService.submit(() -> newChatThread(usersInChat.get(userIndex), newChat));
		}
		
		try {
			newChatLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Checks execution results (futures)
		for (int i = 0; i < usersInChat.size(); i++) {
			try {
				Future<Boolean> f = completionService.take();
				assert(f.get().equals(true));
			} catch (Exception e) {
				System.out.println("Thread execution throwed exception: " + e.getMessage());
			}
		}
	}
	
	private boolean newChatThread(User user, Chat newChat) {
		user.newChat(newChat);
		newChatLatch.countDown();
		return true;
	}

	public void closeChat(Chat chat) {
		Chat removedChat = chats.remove(chat.getName());
		if (removedChat == null) {
			throw new IllegalArgumentException("Trying to remove an unknown chat with name \'"
					+ chat.getName() + "\'");
		}
		
		this.maxChatsSemaphore.release();

		for(User user : users.values()){
			user.chatClosed(removedChat);
		}
	}

	public Collection<Chat> getChats() {
		return Collections.unmodifiableCollection(chats.values());
	}

	public Chat getChat(String chatName) {
		return chats.get(chatName);
	}

	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(users.values());
	}

	public User getUser(String userName) {
		return users.get(userName);
	}

	public void close() {}
}
