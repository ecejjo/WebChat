package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.Objects;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class ChatManagerTest {

	@Test
	public void newChat() throws InterruptedException, TimeoutException {

		// Crear el chat Manager
		ChatManager chatManager = new ChatManager(5);

		// Crear un usuario que guarda en chatName el nombre del nuevo chat
		final String[] chatName = new String[1];

		chatManager.newUser(new TestUser("user") {
			public void newChat(Chat chat) {
				chatName[0] = chat.getName();
			}
		});

		// Crear un nuevo chat en el chatManager
		chatManager.newChat("Chat", 5, TimeUnit.SECONDS);

		// Comprobar que el chat recibido en el método 'newChat' se llama 'Chat'
		assertTrue("The method 'newChat' should be invoked with 'Chat', but the value is "
				+ chatName[0], Objects.equals(chatName[0], "Chat"));
	}

	@Test
	public void newUserInChat() throws InterruptedException, TimeoutException {

		ChatManager chatManager = new ChatManager(5);

		final String[] newUser = new String[1];

		TestUser user1 = new TestUser("user1") {
			@Override
			public void newUserInChat(Chat chat, User user) {
				newUser[0] = user.getName();
			}
		};

		TestUser user2 = new TestUser("user2");

		chatManager.newUser(user1);
		chatManager.newUser(user2);

		Chat chat = chatManager.newChat("Chat", 5, TimeUnit.SECONDS);

		chat.addUser(user1);
		chat.addUser(user2);

		assertTrue("Notified new user '" + newUser[0] + "' is not equal than user name 'user2'",
				"user2".equals(newUser[0]));

	}
	
	@Test
	public void mejora1() throws Throwable {

		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 50;

		ChatManager chatManager = new ChatManager(MAX_CHATS);

		ExecutorService executor = Executors.newFixedThreadPool(NUM_CONCURRENT_USERS);
		CompletionService<String> completionService = new ExecutorCompletionService<>(executor);

		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			final int userIndex = i;
			completionService.submit(() -> mejora1Thread(chatManager, userIndex));
		}

		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			try {
				Future<String> f = completionService.take();
				assertTrue("Thread execution did not return success", f.get().equals("Success"));
			} catch (ExecutionException e) {
				assertTrue("Thread execution throwed ExecutionException:" + e.getMessage(), false);
				throw e.getCause();
			} catch (Exception e) {
				assertTrue("Thread execution throwed exception: " + e.getMessage(), false);
			}
		}
	}
	
	public String mejora1Thread(ChatManager chatManager, int userIndex) throws InterruptedException, TimeoutException {
		
		TestUser user = new TestUser("user" + userIndex);
		chatManager.newUser(user);
				
		final int NUM_ITERATIONS = 5;
		
		for (int userIteration = 0; userIteration < NUM_ITERATIONS; userIteration++) {
			System.out.println("Running..." + " userIndex #" + userIndex + " userIteration #" + userIteration);

			Chat chat = chatManager.newChat("Chat" + userIteration, 5, TimeUnit.SECONDS);
			
			chat.addUser(user);
									
			for (User userInChat : chat.getUsers()) {
				System.out.println("User: " + userInChat.getName() + " is in chat " + chat.getName() +  ".");
			}
			
			chat.sendMessage(user, "In chat " + chat.getName() + ", user " + user.getName() + " says: Hello!!");
		}
		return "Success";
	}
	
	@Test
	public void mejora4() throws Throwable {

		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 1;

		ChatManager chatManager = new ChatManager(MAX_CHATS);
		Chat chat = chatManager.newChat("Chat", 5, TimeUnit.SECONDS);
		
		TestUser user;
		int i = 0;
		do {
			
			user = new TestUser("user" + i) {
				@Override
				public void newMessage(Chat chat, User user, String message) {
					System.out.println("New message '" + message + "' from user " + user.getName() + " in chat " + chat.getName());
					 try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			
			chat.addUser(user);
		}
		while(i < NUM_CONCURRENT_USERS);
		
		long startTime = System.currentTimeMillis();
		chat.sendMessage(user, "In chat " + chat.getName() + ", user " + user.getName() + " says: Hello!!");		
		chat.waitForMessageSent();
		long endTime = System.currentTimeMillis();
		System.out.println("DEBUG: Message took " + (endTime - startTime) + " milliseconds to run.");
		assertTrue("Message took more than 1.5 seconds to sent and receive.", (endTime - startTime) < 1500);
	}
}
