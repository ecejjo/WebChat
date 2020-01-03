package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;

public class Mejora5_2closeChatTest {

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
}
