package es.codeurjc.webchat;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;

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

	public synchronized void addUser(User user) {
		// putIfAbsent() returns:
		// the previous value associated with the specified key,
		// or null if there was no mapping for the key

		if (users.putIfAbsent(user.getName(), user) == null) {
			int existingUsers = users.size() - 1;

			ExecutorService executorService = Executors.newFixedThreadPool(10);
			CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executorService);

			for(User u : users.values()){
				if (u != user) {
					completionService.submit(() -> addUserThread(u, user));
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
	}

	private boolean addUserThread(User user, User userNew) {
		user.newUserInChat(this, userNew);
		return true;
	}

	public synchronized void removeUser(User user) {
		// remove() returns:
		// the previous value associated with key,
		// or null if there was no mapping for key
		if (users.remove(user.getName()) != null) {

			int existingUsers = users.size();

			ExecutorService executorService = Executors.newFixedThreadPool(10);
			CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executorService);

			// CountDownLatch must be created before any submit to avoid race conditions

			for(User u : users.values()){
				completionService.submit(() -> removeUserThread(u, user));
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
	}

	private boolean removeUserThread(User user, User userExited) {
		user.userExitedFromChat(this, userExited);
		return true;
	}


	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(users.values());
	}

	public User getUser(String name) {
		return users.get(name);
	}

	public void sendMessage(User user, String message) {
		int existingUsers = users.size();

		ExecutorService executorService = Executors.newFixedThreadPool(10);
		CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executorService);
						
		for(User u : users.values()) {
			completionService.submit(() -> sendMessageThread(u, user, message));
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
	
	private boolean sendMessageThread(User userTo, User userFrom, String message) {
		userTo.newMessage(this, userFrom, message);
		return true;
	}
	
	public void close() throws TimeoutException {
		this.chatManager.closeChat(this);
	}
}
