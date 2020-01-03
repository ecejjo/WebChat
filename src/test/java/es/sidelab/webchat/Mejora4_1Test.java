package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class Mejora4_1Test {
	
	@Test
	public void mejora4_1() throws Throwable {

		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 1;

		ChatManager chatManager = new ChatManager(MAX_CHATS);
		Chat chat = chatManager.newChat("mejora4_1", 5, TimeUnit.SECONDS);
		
		TestUser user;
		int i = 0;
		do {
			user = new TestUser("user" + i) {
				@Override
				public void newMessage(Chat chat, User user, String message) {
					System.out.println("newMessage(): Starting ...");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("New message '" + message + "' from user " + user.getName() + " in chat " + chat.getName());
				}
			};
			chat.addUser(user);
			i++;
		}
		while(i < NUM_CONCURRENT_USERS);
		
		long startTime = System.currentTimeMillis();
		chat.sendMessage(user, "Hello!!");
		chat.waitForMessageSent();
		long endTime = System.currentTimeMillis();
		System.out.println("DEBUG: Message took " + (endTime - startTime) + " milliseconds to run.");
		assertTrue("Message took more than 1.5 seconds to sent and receive.", (endTime - startTime) < 1500);
	}

}
