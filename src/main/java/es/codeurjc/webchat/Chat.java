package es.codeurjc.webchat;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

public class Chat {

	private String name;
	private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

	private ChatManager chatManager;

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
		for(User u : users.values()){
			u.newMessage(this, user, message);
		}
	}

	public void close() {
		this.chatManager.closeChat(this);
	}
}
