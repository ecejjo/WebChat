package es.codeurjc.webchat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Chat {

	private String name;
	private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

	private ChatManager chatManager;
	
	private ArrayList<User> usersInChat;
	private ArrayList<ExecutorService> executors;
	private ArrayList<CompletionService<String>> completionServices;
	
	public Chat(ChatManager chatManager, String name) {
		this.chatManager = chatManager;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addUser(User user) {
		// putIfAbsent() returns:
		// the previous value associated with the specified key, or null if there was no mapping for the key
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
		// the previous value associated with key, or null if there was no mapping for key
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
		
		usersInChat = new ArrayList<>(users.values());
		executors = new ArrayList<ExecutorService>(usersInChat.size());
		completionServices = new ArrayList<CompletionService<String>>(usersInChat.size());
		
		for (int i = 0; i < usersInChat.size(); i++) {			
			executors.add(i, Executors.newFixedThreadPool(1));			
			completionServices.add(i, new ExecutorCompletionService<>(executors.get(i)));
			
			final int userIndex = i;
			completionServices.get(i).submit(() -> sendMessageThread(usersInChat.get(userIndex), message));
		}
	}
	
	public void waitForMessageSent() {
		for (int i = 0; i < usersInChat.size(); i++) {
			try {
				Future<String> f = completionServices.get(i).take();
				assert(f.get().equals("Sent!"));
			} catch (Exception e) {
				System.out.println("Thread execution throwed exception: " + e.getMessage());
			}
		}
	}
	
	private String sendMessageThread(User user, String message) {
		user.newMessage(this, user, message);
		return "Sent!";
	}

	public void close() {
		this.chatManager.closeChat(this);
	}
}
