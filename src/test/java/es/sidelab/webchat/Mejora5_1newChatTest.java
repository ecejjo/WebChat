package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;

public class Mejora5_1newChatTest {

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
}
