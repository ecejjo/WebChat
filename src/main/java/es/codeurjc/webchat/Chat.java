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

public class Chat {

	private String name;
	private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

	private ChatManager chatManager;
	
	private CountDownLatch sendMessageLatch = new CountDownLatch(0);
	
	public Chat(ChatManager chatManager, String name) {
		this.chatManager = chatManager;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addUser(User user) {
		// putIfAbsent() returns:
		// the previous value associated with the specified key,
		// or null if there was no mapping for the key
		if (users.putIfAbsent(user.getName(), user) == null) {
			for(User u : users.values()){
				if (u != user) {
					u.newUserInChat(this, user);
				}
			}			
		}
	}

	public void removeUser(User user) {
		// remove() returns:
		// the previous value associated with key,
		// or null if there was no mapping for key
		if (users.remove(user.getName()) != null) {
			for(User u : users.values()){
				u.userExitedFromChat(this, user);
			}
		}
	}

	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(users.values());
	}

	public User getUser(String name) {
		return users.get(name);
	}

	public void sendMessage(User user, String message) {
			
		// Using a copy of users in chat to avoid concurrency problems.
		ArrayList<User> usersInChat = new ArrayList<>(users.values());
		
		ExecutorService executorService = Executors.newFixedThreadPool(usersInChat.size());
		CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executorService);
		
		// CountDownLatch must be created before any submit to avoid race conditions
		sendMessageLatch = new CountDownLatch(usersInChat.size());
		
		for (int i = 0; i < usersInChat.size(); i++) {			
			final int userIndex = i;
			completionService.submit(() -> sendMessageThread(usersInChat.get(userIndex), user, message));
		}
		
		try {
			sendMessageLatch.await();
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
	
	private boolean sendMessageThread(User userTo, User userFrom, String message) {
		userTo.newMessage(this, userFrom, message);
		sendMessageLatch.countDown();
		return true;
	}
	
	public void close() {
		this.chatManager.closeChat(this);
	}
}
