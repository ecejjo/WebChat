package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class Mejora5_5sendMessage {

	@Test
	public void addUser4Users() throws InterruptedException, TimeoutException {
		
		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 1;

		final String[] chatName = new String[NUM_CONCURRENT_USERS];
		final String[] userName = new String[NUM_CONCURRENT_USERS];
		final String[] messages = new String[NUM_CONCURRENT_USERS];
		TestUser users[] = new TestUser[NUM_CONCURRENT_USERS];

		ChatManager chatManager = new ChatManager(MAX_CHATS);
		Chat chat = chatManager.newChat("Chat", 5, TimeUnit.SECONDS);
		
		// Users are added to chat
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {

			final int final_i = i;
			users[i] = new TestUser("user" + i) {
				@Override
				public void newMessage(Chat chat, User user, String message) {
					System.out.println("New message '" + message
										+ "' from user " + user.getName()
										+ " in chat " + chat.getName());
					chatName[final_i] = chat.getName();
					userName[final_i] = user.getName();
					messages[final_i] = message;
				}
			};
			chat.addUser(users[i]);
		}
		
		System.out.println("Users added!!");
		
		// Message is sent
		int userSendingMessage = 1;
		String messageSent = "Hello!!";
		
		System.out.println("Sending message: '" +  messageSent + "', from user" + userSendingMessage );
		chat.sendMessage(users[userSendingMessage], messageSent);
		
		System.out.println("Message sent!!");
		
		for (int userNotified = 0; userNotified < NUM_CONCURRENT_USERS; userNotified++) {

			// User sending message is not notified
			if (userNotified == userSendingMessage) {
				continue;
			}
			
			// Only users already in chat are notified
			assertTrue("The method 'newMessage' should be invoked with 'Chat', but the value is "
					+ chatName[userNotified], Objects.equals(chatName[userNotified], "Chat"));

			assertTrue("The method 'newMessage' should be invoked with 'user" + userSendingMessage + "', but the value is "
					+ userName[userNotified], Objects.equals(userName[userNotified], "user" + userSendingMessage));

			assertTrue("The method 'newMessage' should be invoked with message " + messageSent + "', but the value is "
					+ messages[userNotified], Objects.equals(messages[userNotified], messageSent));
		}
	}
}
