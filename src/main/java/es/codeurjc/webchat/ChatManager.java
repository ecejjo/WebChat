package es.codeurjc.webchat;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
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

	public ChatManager(int maxChats) {
		this.maxChats = maxChats;
		this.maxChatsSemaphore = new Semaphore(this.maxChats, true);
	}

	public void newUser(User user) {
		if (users.putIfAbsent(user.getName(), user) != null) {
			throw new IllegalArgumentException("There is already a user with name \'"
					+ user.getName() + "\'");				
		}
	}

	public Chat newChat(String name, long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {

		try {
			if (this.maxChatsSemaphore.tryAcquire(timeout, unit) == true) {

				Chat newChat = new Chat(this, name);

				if (chats.putIfAbsent(name, newChat) == null) {
					notifyUsersNewChat(newChat);
					return newChat;
				}
				else {
					System.out.println("chatManager::newChat(): chat already exists: " + name +  ".");
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

	private void notifyUsersNewChat(Chat newChat) throws InterruptedException {

		ExecutorService executorService = Executors.newFixedThreadPool(10);
		CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executorService);

		int existingUsers;
		synchronized(users) {
			existingUsers = users.size();		
			for(User u : users.values()) {
				completionService.submit(() -> newChatThread(u, newChat));
			}
		}

		// Checks execution results (futures)
		for (int i = 0; i < existingUsers; i++) {
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
		return true;
	}

	public void closeChat(Chat chat) {
		Chat removedChat = chats.remove(chat.getName());
		if (removedChat == null) {
			throw new IllegalArgumentException("Trying to remove an unknown chat with name \'"
					+ chat.getName() + "\'");
		}

		this.maxChatsSemaphore.release();

		ExecutorService executorService = Executors.newFixedThreadPool(10);
		CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executorService);

		int existingUsers;
		synchronized(users) {
			existingUsers = users.size();		

			for(User u : users.values()) {
				completionService.submit(() -> closeChatThread(u, removedChat));
			}
		}
		
		// Checks execution results (futures)
		for (int i = 0; i < existingUsers; i++) {
			try {
				Future<Boolean> f = completionService.take();
				assert(f.get().equals(true));
			} catch (Exception e) {
				System.out.println("Thread execution throwed exception: " + e.getMessage());
			}
		}
	}

	public boolean closeChatThread(User user, Chat removedChat) {
		user.chatClosed(removedChat);
		return true;
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
