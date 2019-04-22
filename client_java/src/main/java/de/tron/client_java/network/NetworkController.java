package de.tron.client_java.network;

import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.ForkJoinPool;

import de.tron.client_java.network.message.Message;
import de.tron.client_java.network.message.MessageType;
import de.tron.client_java.network.message.converter.JsonMessageConverter;

public class NetworkController implements AutoCloseable {

	private static final long OFFER_TIMEOUT = 10; 
	
	private final SubmissionPublisher<Message> publisher = new SubmissionPublisher<>(ForkJoinPool.commonPool(), 4);
	
	private Socket connection;
	private Scanner input;
	private PrintWriter output;
	
	public NetworkController() {
		try {
			connect("localhost", 4321);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void subscribe(Subscriber<? super Message> subscriber) {
		this.publisher.subscribe(subscriber);
	}
	
	public void connect(String ip, int port) throws IOException {
		this.connection = new Socket(ip, port);
		this.input = new Scanner(connection.getInputStream());
		this.output = new PrintWriter(this.connection.getOutputStream());
		
		Thread receiver = new Thread(this::receiveAndPublish);
		receiver.setDaemon(true);
		receiver.start();
	}	

	@Override
	public void close() throws Exception {
		this.input.close();
		this.output.close();
		this.connection.close();
	}
	
	public void sendMessage(Message message) {
		JsonMessageConverter converter = new JsonMessageConverter();
		String messageString = converter.serialize(message);
		this.output.println(messageString);
	}
	
	private void receiveAndPublish() {
		while (this.input.hasNext()) {
			Message message = receiveMessage();
			if (message.getType() == MessageType.DISCONNECT) {
				break;
			} else {
				publishMessage(message);
			}
		}
	}

	private Message receiveMessage() {
		JsonMessageConverter converter = new JsonMessageConverter();
		String messageStr = this.input.next();
		System.out.println(messageStr);
		return converter.deserialize(messageStr);
	}
	
	private void publishMessage(Message message) {
		this.publisher.offer(message, NetworkController.OFFER_TIMEOUT, TimeUnit.MILLISECONDS, null);		
	}
	
}
