package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;

import es.codeurjc.webchat.Chat;
import es.codeurjc.webchat.ChatManager;
import es.codeurjc.webchat.User;

public class Mejora1Test {
	
	@Test
	public void mejora1() throws Throwable {

		final int NUM_CONCURRENT_USERS = 4;
		final int MAX_CHATS = 50;

		ChatManager chatManager = new ChatManager(MAX_CHATS);

		ExecutorService executor = Executors.newFixedThreadPool(NUM_CONCURRENT_USERS);
		CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executor);

		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			final int userIndex = i;
			completionService.submit(() -> mejora1Thread(chatManager, userIndex));
		}

		for (int i = 0; i < NUM_CONCURRENT_USERS; i++) {
			try {
				Future<Boolean> f = completionService.take();
				assertTrue("Thread execution did not return success", f.get().equals(true));
			} catch (ExecutionException e) {
				assertTrue("Thread execution throwed ExecutionException:" + e.getMessage(), false);
				throw e.getCause();
			} catch (Exception e) {
				assertTrue("Thread execution throwed exception: " + e.getMessage(), false);
			}
		}
	}
	
	public boolean mejora1Thread(ChatManager chatManager, int userIndex) throws InterruptedException, TimeoutException {
		
		TestUser user = new TestUser("user" + userIndex);
		chatManager.newUser(user);
				
		final int NUM_ITERATIONS = 5;
		
		for (int userIteration = 0; userIteration < NUM_ITERATIONS; userIteration++) {
			System.out.println("Running..." + " userIndex #" + userIndex + " userIteration #" + userIteration);

			Chat chat = chatManager.newChat("Chat" + userIteration, 5, TimeUnit.SECONDS);
			
			chat.addUser(user);
									
			for (User userInChat : chat.getUsers()) {
				System.out.println("User: " + userInChat.getName() + " is in chat " + chat.getName() +  ".");
			}
		}
		return true;
	}

}
