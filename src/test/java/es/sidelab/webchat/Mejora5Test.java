package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class Mejora5Test {
	
	@Test
	public void newChat4Users() throws InterruptedException, TimeoutException {
		
		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 1;

		// Crear el chat Manager
		ChatManager chatManager = new ChatManager(MAX_CHATS);

		// Crear un usuario que guarda en chatName el nombre del nuevo chat
		final String[] chatName = new String[NUM_CONCURRENT_USERS];
		
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			chatName[i] = new String();
			final int final_i = i;
			
			chatManager.newUser(new TestUser("user" + i) {
				public void newChat(Chat chat) {
					chatName[final_i] = chat.getName();
				}
			});
		}

		// Crear un nuevo chat en el chatManager
		chatManager.newChat("Chat", 5, TimeUnit.SECONDS);
		
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			// Comprobar que el chat recibido en el método 'newChat' se llama 'Chat'
			assertTrue("The method 'newChat' should be invoked with 'Chat', but the value is "
					+ chatName[i], Objects.equals(chatName[0], "Chat"));
		}
	}
	
	@Test
	public void closeChat4Users() throws InterruptedException, TimeoutException {
		
		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 1;

		ChatManager chatManager = new ChatManager(MAX_CHATS);

		final String[] chatToCloseName = new String[NUM_CONCURRENT_USERS];
		
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			chatToCloseName[i] = new String();
			final int final_i = i;
			
			chatManager.newUser(new TestUser("user" + i) {
				public void chatClosed(Chat chat) {
					chatToCloseName[final_i] = chat.getName();
				}
			});
		}

		Chat chat = chatManager.newChat("ChatToClose", 5, TimeUnit.SECONDS);		
		chatManager.closeChat(chat);
		
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			// Comprobar que el chat recibido en el método 'newChat' se llama 'Chat'
			assertTrue("The method 'newChat' should be invoked with 'Chat', but the value is "
					+ chatToCloseName[i], Objects.equals(chatToCloseName[0], "ChatToClose"));
		}
	}
	
	@Test
	public void addUser4Users() throws InterruptedException, TimeoutException {
		
		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 1;

		ChatManager chatManager = new ChatManager(MAX_CHATS);
		TestUser users[] = new TestUser[NUM_CONCURRENT_USERS];

		final String[] chatName = new String[NUM_CONCURRENT_USERS];
		final String[] userName = new String[NUM_CONCURRENT_USERS];
		
		Chat chat = chatManager.newChat("Chat", 5, TimeUnit.SECONDS);
		
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {

			chatName[i] = new String();
			userName[i] = "user" + i;

			final int final_i = i;
			System.out.println("value of i is: " + i);
			System.out.println("value of final_i is: " + final_i);
			
			chatManager.newUser(new TestUser(userName[i]) {
				public void newUserInChat(Chat chat, User user) {
					chatName[final_i] = chat.getName();
					userName[final_i] = user.getName();
				}
			});
			
			users[i] = (TestUser) chatManager.getUser(userName[i]);		
			chat.addUser(users[i]);
		}

						
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			assertTrue("The method 'newUserInChat' should be invoked with 'Chat', but the value is "
					+ chatName[i], Objects.equals(chatName[i], "Chat"));
			
			assertTrue("The method 'newUserInChat' should be invoked with 'user" + i + "', but the value is "
					+ userName[i], Objects.equals(userName[i], "user" + i));
		}
	}

}
