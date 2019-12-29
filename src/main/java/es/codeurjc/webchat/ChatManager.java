package es.codeurjc.webchat;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ChatManager {

	private ConcurrentHashMap<String, Chat> chats = new ConcurrentHashMap<>();
	private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();
	private int maxChats;

	public ChatManager(int maxChats) {
		this.maxChats = maxChats;
	}

	public void newUser(User user) {
		// putIfAbsent() returns:
		// the previous value associated with the specified key, or null if there was no mapping for the key
		if (users.putIfAbsent(user.getName(), user) != null) {
			throw new IllegalArgumentException("There is already a user with name \'"
					+ user.getName() + "\'");				
		}
	}

	public Chat newChat(String name, long timeout, TimeUnit unit) throws InterruptedException,
			TimeoutException {

		if (chats.size() == maxChats) {
			throw new TimeoutException("There is no enought capacity to create a new chat");
		}

		if(chats.containsKey(name)){
			return chats.get(name);
		} else {
			Chat newChat = new Chat(this, name);
			// putIfAbsent() returns:
			// the previous value associated with the specified key, or null if there was no mapping for the key
			if (chats.putIfAbsent(name, newChat) == null) {
				for(User user : users.values()){
					user.newChat(newChat);
				}
				return newChat;
			}
			else {
				return chats.get(name);				
			}
		}
	}

	public void closeChat(Chat chat) {
		Chat removedChat = chats.remove(chat.getName());
		if (removedChat == null) {
			throw new IllegalArgumentException("Trying to remove an unknown chat with name \'"
					+ chat.getName() + "\'");
		}

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
