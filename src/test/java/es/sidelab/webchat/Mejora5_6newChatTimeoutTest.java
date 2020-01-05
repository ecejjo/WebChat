package es.sidelab.webchat;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;

public class Mejora5_6newChatTimeoutTest {

	@Test(expected = TimeoutException.class)
	public void newChatTimeout() throws InterruptedException, TimeoutException {
		
		final int MAX_CHATS = 3;

		ChatManager chatManager = new ChatManager(MAX_CHATS);
		chatManager.newChat("Chat1", 1, TimeUnit.SECONDS);
		chatManager.newChat("Chat2", 1, TimeUnit.SECONDS);
		chatManager.newChat("Chat3", 1, TimeUnit.SECONDS);
		chatManager.newChat("ChatToTimeout", 1, TimeUnit.SECONDS);
	}
	
	@Test
	public void newChatAvoidsTimeout() throws InterruptedException, TimeoutException {

		final int MAX_CHATS = 3;
		Chat chats[] = new Chat[MAX_CHATS + 1];

		ChatManager chatManager = new ChatManager(MAX_CHATS);
		
		chats[0] = chatManager.newChat("Chat1", 1, TimeUnit.SECONDS);
		chats[1] = chatManager.newChat("Chat2", 1, TimeUnit.SECONDS);
		chats[2] = chatManager.newChat("Chat3", 1, TimeUnit.SECONDS);

		Runnable runnable =
				() -> { try {
					chats[3] = chatManager.newChat("ChatToAvoidTimeout", 3, TimeUnit.SECONDS);
				} catch (InterruptedException | TimeoutException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} };

		Thread newChatThread = new Thread(runnable);

		newChatThread.start();
		Thread.sleep(1);

		chatManager.closeChat(chats[1]);
		
		Thread.sleep(2); // At this point, no TimeoutException should take place
		Thread.sleep(1); // Guard timining

		chatManager.closeChat(chats[0]);
		// chatManager.closeChat(chats[1]);
		chatManager.closeChat(chats[2]);
		chatManager.closeChat(chats[3]);		
	}
}
