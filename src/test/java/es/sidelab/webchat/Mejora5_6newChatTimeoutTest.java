package es.sidelab.webchat;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.ChatManager;

public class Mejora5_6newChatTimeoutTest {

	@Test(expected = TimeoutException.class)
	public void newChatTimeout() throws InterruptedException, TimeoutException {
		
		final int MAX_CHATS = 1;

		ChatManager chatManager = new ChatManager(MAX_CHATS);
		chatManager.newChat("Chat", 1, TimeUnit.SECONDS);
		chatManager.newChat("ChatToTimeout", 1, TimeUnit.SECONDS);
	}
}
