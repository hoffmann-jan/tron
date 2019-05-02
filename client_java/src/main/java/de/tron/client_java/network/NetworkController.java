package de.tron.client_java.network;

import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.ForkJoinPool;

import de.tron.client_java.network.message.Message;
import de.tron.client_java.network.message.converter.JsonMessageConverter;

public class NetworkController implements Closeable {

	private static final long OFFER_TIMEOUT = 10; 
	
	private final SubmissionPublisher<Message> publisher = new SubmissionPublisher<>(ForkJoinPool.commonPool(), 4);
	
	private Socket connection;
	private Scanner input;
	private PrintWriter output;
	
	public void subscribe(Subscriber<? super Message> subscriber) {
		this.publisher.subscribe(subscriber);
	}
	
	public void configureConnection(String ip, int port) throws IOException {
		this.connection = new Socket(ip, port);
		this.input = new Scanner(connection.getInputStream());
		this.output = new PrintWriter(this.connection.getOutputStream(), true);
		
		Thread receiver = new Thread(this::receiveAndPublish);
		receiver.setDaemon(true);
		receiver.setUncaughtExceptionHandler((t,e) -> e.printStackTrace());
		receiver.start();
	}	

	@Override
	public void close() {
		this.input.close();
		this.output.close();
		try {
			this.connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendMessage(Message message) {
		JsonMessageConverter converter = new JsonMessageConverter();
		String messageString = converter.serialize(message);
//				+ "<EOF>";
		this.output.println(messageString);
	}
	
	private void receiveAndPublish() {
		try {
			while (this.input.hasNext()) {
				Message message = receiveMessage();
				publishMessage(message);
			}
		} catch (Exception e) {
			this.publisher.closeExceptionally(e);
		}
		this.publisher.close();
	}

	private Message receiveMessage() {
		JsonMessageConverter converter = new JsonMessageConverter();
		String messageStr = this.input.next();
		messageStr = messageStr.replaceAll("<EOF>", "");
		System.out.println(messageStr);
		return converter.deserialize(messageStr);
	}
	
	private void publishMessage(Message message) {
		this.publisher.offer(message, NetworkController.OFFER_TIMEOUT, TimeUnit.MILLISECONDS, null);		
	}
	
}
