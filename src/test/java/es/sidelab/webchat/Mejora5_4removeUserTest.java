package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class Mejora5_4removeUserTest {
		
	@Test
	public void removeUser4Users() throws InterruptedException, TimeoutException {
		
		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 1;

		final String[] chatName = new String[NUM_CONCURRENT_USERS];
		final String[] userName = new String[NUM_CONCURRENT_USERS];
		TestUser users[] = new TestUser[NUM_CONCURRENT_USERS];

		ChatManager chatManager = new ChatManager(MAX_CHATS);
		Chat chat = chatManager.newChat("Chat", 5, TimeUnit.SECONDS);
		
		// Users are added to chat
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {

			final int final_i = i;
			users[i] = new TestUser("user" + i) {
				@Override
				public void userExitedFromChat(Chat chat, User user) {
					System.out.println("User " + user.getName() + " exited from chat " + chat.getName());
					chatName[final_i] = chat.getName();
					userName[final_i] = user.getName();
				}
			};			
			chat.addUser(users[i]);
		}
		
		// Users are removed from chat
		for (int userRemoved = 0; userRemoved < NUM_CONCURRENT_USERS; userRemoved++) {
			
			System.out.println("userRemoved is:" + userRemoved);

			chat.removeUser(users[userRemoved]);

			// Only remaining users in chat are notified
			for (int userNotified = userRemoved + 1; userNotified < NUM_CONCURRENT_USERS; userNotified++) {
				
				System.out.println("userNotified is:" + userNotified);

				assertTrue("The method 'userExitedFromChat' should be invoked with 'Chat', but the value is "
						+ chatName[userNotified], Objects.equals(chatName[userNotified], "Chat"));

				assertTrue("The method 'userExitedFromChat' should be invoked with 'user" + userRemoved + 
							"', but the value is " + userName[userNotified], 
							Objects.equals(userName[userNotified], "user" + userRemoved));
			}
		}
	}
}
