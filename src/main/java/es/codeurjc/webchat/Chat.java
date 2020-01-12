package es.codeurjc.webchat;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Chat {

	private String name;
	private ConcurrentHashMap<String, User> users = new ConcurrentHashMap<>();

	private ChatManager chatManager;
	
	private final int AWAIT_TIMEOUT = 2;
	private CountDownLatch addUserLatch = new CountDownLatch(0);
	private CountDownLatch removeUserLatch = new CountDownLatch(0);
	private CountDownLatch sendMessageLatch = new CountDownLatch(0);
	
	public Chat(ChatManager chatManager, String name) {
		this.chatManager = chatManager;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public synchronized void addUser(User user) throws TimeoutException {
		
		String traceHead = new String();
		traceHead = "Chat::addUser(): chat: " + this.name + " user: " + user.getName() + ": ";
				
		// putIfAbsent() returns:
		// the previous value associated with the specified key,
		// or null if there was no mapping for the key
		if (users.putIfAbsent(user.getName(), user) == null) {

			// Using a copy of users in chat to avoid concurrency problems.
			ConcurrentHashMap<String, User> usersInChat = new ConcurrentHashMap<>(users);;

			ExecutorService executorService = Executors.newFixedThreadPool(usersInChat.size());
			CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executorService);

			// CountDownLatch must be created before any submit to avoid race conditions
			addUserLatch = new CountDownLatch(usersInChat.size() - 1);
			System.out.println(traceHead + "addUserLatch set with: " + addUserLatch.getCount());

			for(User u : usersInChat.values()){
				if (u != user) {
					completionService.submit(() -> addUserThread(u, user));
				}
			}

			try {
				if (addUserLatch.await(AWAIT_TIMEOUT, TimeUnit.SECONDS) == false) {
					System.out.println(traceHead + "timeout in addUserLatch.await()");
					throw new TimeoutException ("timeout in addUserLatch.await()");
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// Checks execution results (futures)
			for (int i = 0; i < usersInChat.size() - 1; i++) {
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
		addUserLatch.countDown();
		System.out.println("addUserThread(): addUserLatch.getcount(): " + addUserLatch.getCount());
		return true;
	}

	public void removeUser(User user) throws TimeoutException {
		// remove() returns:
		// the previous value associated with key,
		// or null if there was no mapping for key
		if (users.remove(user.getName()) != null) {
			
			// Using a copy of users in chat to avoid concurrency problems.
			ConcurrentHashMap<String, User> usersInChat = new ConcurrentHashMap<>(users);
			
			if (usersInChat.size() != 0) {
				
				ExecutorService executorService = Executors.newFixedThreadPool(usersInChat.size());
				CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executorService);

				// CountDownLatch must be created before any submit to avoid race conditions
				removeUserLatch = new CountDownLatch(usersInChat.size());

				for(User u : usersInChat.values()){
					completionService.submit(() -> removeUserThread(u, user));
				}

				try {
					if (removeUserLatch.await(AWAIT_TIMEOUT, TimeUnit.SECONDS) == false) {
						System.out.println("removeUser(): timeout in removeUserLatch.await()");
						throw new TimeoutException ("timeout in removeUserLatch.await()");
					}
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
		}
	}
	
	private boolean removeUserThread(User user, User userExited) {
		user.userExitedFromChat(this, userExited);
		removeUserLatch.countDown();
		return true;
	}


	public Collection<User> getUsers() {
		return Collections.unmodifiableCollection(users.values());
	}

	public User getUser(String name) {
		return users.get(name);
	}

	public void sendMessage(User user, String message) {
			
		// Using a copy of users in chat to avoid concurrency problems.
		ConcurrentHashMap<String, User> usersInChat = new ConcurrentHashMap<>(users);
		
		ExecutorService executorService = Executors.newFixedThreadPool(usersInChat.size());
		CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executorService);
		
		// CountDownLatch must be created before any submit to avoid race conditions
		sendMessageLatch = new CountDownLatch(usersInChat.size());
		
		for(User u : usersInChat.values()) {
			completionService.submit(() -> sendMessageThread(u, user, message));
		}
		
		try {
			if (sendMessageLatch.await(AWAIT_TIMEOUT, TimeUnit.SECONDS) == false) {
				System.out.println("sendMessage(): timeout in sendMessageLatch.await()");
			}
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
	
	public void close() throws TimeoutException {
		this.chatManager.closeChat(this);
	}
}
