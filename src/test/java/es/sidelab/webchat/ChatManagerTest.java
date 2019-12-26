package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Objects;
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
	public void mejora1() throws InterruptedException, TimeoutException {
		
		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 50;

		ChatManager chatManager = new ChatManager(MAX_CHATS);
		
		final Thread[] threadsList = new Thread[NUM_CONCURRENT_USERS];
		
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			threadsList[i] = new Thread(mejora1Thread(chatManager, i));
		}
		
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			threadsList[i].start();
		}

		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			threadsList[i].join();
		}
	}
	
	public Runnable mejora1Thread(ChatManager chatManager, int userIndex) throws InterruptedException, TimeoutException {
		
		Thread.sleep((long)(Math.random() * 1));

		TestUser user = new TestUser("user" + userIndex);
		chatManager.newUser(user);
				
		final int NUM_ITERATIONS = 4;
		
		for (int userIteration = 0; userIteration < NUM_ITERATIONS; userIteration++) {
			System.out.println("Running..." + " userIndex #" + userIndex + " userIteration #" + userIteration);

			Chat chat = chatManager.newChat("Chat" + userIteration, 5, TimeUnit.SECONDS);
			
			chat.addUser(user);
			
			Collection<User> usersInChat = chat.getUsers();
			System.out.println(usersInChat.toString());			
		}
		return null;
	}
}
