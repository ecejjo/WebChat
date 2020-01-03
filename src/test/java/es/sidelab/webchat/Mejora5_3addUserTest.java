package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class Mejora5_3addUserTest {

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
			
			chatManager.newUser(new TestUser(userName[i]) {
				public void newUserInChat(Chat chat, User user) {
					chatName[final_i] = chat.getName();
					userName[final_i] = user.getName();
				}
			});
			
			users[i] = (TestUser) chatManager.getUser(userName[i]);		
			chat.addUser(users[i]);
			
			// First user in chat is not notified
			if (i != 0) {
				// Only users already in chat are notified
				for (int j = 0; j < i; j++) {
					assertTrue("The method 'newUserInChat' should be invoked with 'Chat', but the value is "
							+ chatName[j], Objects.equals(chatName[j], "Chat"));
					
					assertTrue("The method 'newUserInChat' should be invoked with 'user" + i + "', but the value is "
							+ userName[j], Objects.equals(userName[j], "user" + i));
				}
			}
		}
	}
}
