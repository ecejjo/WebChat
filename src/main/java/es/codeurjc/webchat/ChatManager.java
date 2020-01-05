package es.codeurjc.webchat;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
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
					for(User user : users.values()){
						user.newChat(newChat);
					}
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
