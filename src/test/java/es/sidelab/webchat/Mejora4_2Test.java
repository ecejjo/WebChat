package es.sidelab.webchat;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CompletionService;
import java.util.concurrent.Exchanger;
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

public class Mejora4_2Test {

	Exchanger<Boolean> exchanger = new Exchanger<Boolean>();
	boolean rightOrderFlag = true;

	@Test
	public void mejora4_2() throws Throwable {

		final int MAX_CHATS = 1;
		final int NUM_CONCURRENT_USERS = 2;

		ChatManager chatManager = new ChatManager(MAX_CHATS);
		Chat chat = chatManager.newChat("mejora4_2", 5, TimeUnit.SECONDS);
		
		TestUser user;
		int i = 0;
		do {
			user = new TestUser("user" + i) {
				
				int messageCounter = 0;
				
				@Override
				public void newMessage(Chat chat, User user, String message) {
					
					String traceHeader = "newMessage(), " + user.getName() + ": "; 
					System.out.println(traceHeader + "rightOrderFlag: " + rightOrderFlag);
					
					try {
						// Thread.sleep(500);
						Thread.sleep((long)(Math.random() * 500));
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.out.println(traceHeader + "Exception took place!!");
						e.printStackTrace();
					}
					
					System.out.println(traceHeader + " - New message '" + message + "' from user " + user.getName() + " in chat " + chat.getName());

					try {
						System.out.println(traceHeader + "messageCounter: " + messageCounter);
						if (message.equals(Integer.toString(messageCounter))) {
							System.out.println(traceHeader + "Mensaje recibido en orden correcto!!");
							rightOrderFlag = exchanger.exchange(rightOrderFlag);
						}
						else {
							System.out.println(traceHeader + "Mensaje recibido en orden INcorrecto!!");
							rightOrderFlag = exchanger.exchange(false);						
						}
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					messageCounter++;
				}
			};
			chat.addUser(user);
			i++;
		}
		while(i < NUM_CONCURRENT_USERS);
		
		ExecutorService executor = Executors.newFixedThreadPool(NUM_CONCURRENT_USERS);
		CompletionService<String> completionService = new ExecutorCompletionService<>(executor);
		
		final TestUser userFinal = user;
		completionService.submit(() -> mejora4_2Thread(chat, userFinal));
		
		try {
			Future<String> f = completionService.take();
			assertTrue("Thread execution did not return success", f.get().equals("Success"));
			assertTrue("Some message was received in wrong order", rightOrderFlag);
		} catch (ExecutionException e) {
			assertTrue("Thread execution throwed ExecutionException:" + e.getMessage(), false);
			throw e.getCause();
		} catch (Exception e) {
			assertTrue("Thread execution throwed exception: " + e.getMessage(), false);
		}		
	}
	
	public String mejora4_2Thread(Chat chat, TestUser user) throws InterruptedException, TimeoutException, RuntimeException {

		String traceHeader = "mejora4_2Thread(), " + user.getName() + ": "; 

		final int NUM_MESSAGES = 5;
		String result = "Success";

		for (int i = 0; i < NUM_MESSAGES; i++) {

			if (i == 1) { continue; } // Forces an error!!

			System.out.println(traceHeader + "Sending message ...");
			chat.sendMessage(user, Integer.toString(i));			
			chat.waitForMessageSent();			
		}
				
		System.out.println(traceHeader + "result is: " + result);
		return result;
	}

}
