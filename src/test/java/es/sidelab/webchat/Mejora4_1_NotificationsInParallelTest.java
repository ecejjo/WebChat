package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class Mejora4_1_NotificationsInParallelTest {
	
	@Test
	public void mejora_4_1_1_ChatManager_newChat() throws Throwable {

		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 1;

		ChatManager chatManager = new ChatManager(MAX_CHATS);

		TestUser user;
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			user = new TestUser("user" + i) {
				@Override
				public void newChat(Chat chat) {
					System.out.println("newChat(): Starting ...");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("newChat '" + chat.getName()+ "' notification in user " + this.getName());
				}
			};
			chatManager.newUser(user);
		}
		
		long startTime = System.currentTimeMillis();
		chatManager.newChat("mejora4_1_ChatManager_newChat", 5, TimeUnit.SECONDS);
		long endTime = System.currentTimeMillis();
		
		System.out.println("DEBUG: newChat notification took " + (endTime - startTime) + " milliseconds to run.");
		assertTrue("newChat notification took more than 1.5 seconds to sent and receive.", (endTime - startTime) < 1500);
	}
	
	@Test
	public void mejora_4_1_2_ChatManager_closeChat() throws Throwable {

		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 1;

		ChatManager chatManager = new ChatManager(MAX_CHATS);

		TestUser user;
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			user = new TestUser("user" + i) {
				@Override
				public void chatClosed(Chat chat) {
					System.out.println("chatClosed(): Starting ...");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("chatClosed '" + chat.getName()+ "' notification in user " + this.getName());
				}
			};
			chatManager.newUser(user);
		}
		
		Chat chat = chatManager.newChat("mejora4_1_ChatManager_closeChat", 5, TimeUnit.SECONDS);
		
		long startTime = System.currentTimeMillis();
		chatManager.closeChat(chat);
		long endTime = System.currentTimeMillis();
		
		System.out.println("DEBUG: newChat notification took " + (endTime - startTime) + " milliseconds to run.");
		assertTrue("newChat notification took more than 1.5 seconds to sent and receive.", (endTime - startTime) < 1500);
	}
	
	@Test
	public void mejora_4_1_3_Chat_addUser() throws Throwable {

		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 1;

		ChatManager chatManager = new ChatManager(MAX_CHATS);
		Chat chat = chatManager.newChat("mejora4_1_Chat_addUser", 5, TimeUnit.SECONDS);

		TestUser user;
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			user = new TestUser("user" + i) {
				@Override
				public void newUserInChat(Chat chat, User user) {
					System.out.println("newUserInChat(): Starting ...");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("newUserInChat '" + user.getName() + " notification in chat " + chat.getName());
				}
			};
			chatManager.newUser(user);
			chat.addUser(user);
		}

		user = new TestUser("LastUserToAdd");

		long startTime = System.currentTimeMillis();
		chat.addUser(user);
		long endTime = System.currentTimeMillis();
		
		System.out.println("DEBUG: newUserInChat notification took " + (endTime - startTime) + " milliseconds to run.");
		assertTrue("newUserInChat notification took more than 1.5 seconds to sent and receive.", (endTime - startTime) < 1500);
	}

	@Test
	public void mejora_4_1_4_Chat_removeUser() throws Throwable {

		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 1;

		ChatManager chatManager = new ChatManager(MAX_CHATS);
		Chat chat = chatManager.newChat("mejora4_1_Chat_removeUser", 5, TimeUnit.SECONDS);

		TestUser user;
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			user = new TestUser("user" + i) {
				@Override
				public void userExitedFromChat(Chat chat, User user) {
					System.out.println("userExitedFromChat(): Starting ...");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("userExitedFromChat '" + user.getName() + " notification in chat " + chat.getName());
				}
			};
			chatManager.newUser(user);
			chat.addUser(user);
		}

		user = (TestUser) chat.getUser("user1");

		long startTime = System.currentTimeMillis();
		chat.removeUser(user);
		long endTime = System.currentTimeMillis();
		
		System.out.println("DEBUG: newUserInChat notification took " + (endTime - startTime) + " milliseconds to run.");
		assertTrue("newUserInChat notification took more than 1.5 seconds to sent and receive.", (endTime - startTime) < 1500);
	}

	@Test
	public void mejora_4_1_5_Chat_newMessage() throws Throwable {

		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 1;

		ChatManager chatManager = new ChatManager(MAX_CHATS);
		Chat chat = chatManager.newChat("mejora4_1newMessage", 5, TimeUnit.SECONDS);

		TestUser user;
		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
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
			chatManager.newUser(user);
			chat.addUser(user);
		}

		user = (TestUser) chat.getUser("user1");

		long startTime = System.currentTimeMillis();
		chat.sendMessage(user, "Hello!!");
		long endTime = System.currentTimeMillis();
		
		System.out.println("DEBUG: newMessage notification took " + (endTime - startTime) + " milliseconds to run.");
		assertTrue("newMessage notification took more than 1.5 seconds to sent and receive.", (endTime - startTime) < 1500);
	}
}
