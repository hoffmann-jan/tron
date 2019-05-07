package de.tron.client_java.network;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.SubmissionPublisher;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.tron.client_java.network.message.Message;
import de.tron.client_java.network.message.converter.JsonMessageConverter;

public class NetworkController implements Closeable {

	private static final Logger LOGGER = Logger.getLogger("root");
	
	private final SubmissionPublisher<Message> publisher = new SubmissionPublisher<>(ForkJoinPool.commonPool(), 4);
	
	private Socket connection;
	private Scanner input;
	private PrintWriter output;

	public void subscribe(Subscriber<? super Message> subscriber) {
		this.publisher.subscribe(subscriber);
	}
	
	public void configureConnection(String ip, int port) throws IOException {
		NetworkController.LOGGER.log(Level.INFO, "Trying to connect to {0}:{1}", new Object[] {ip, port});
		
		this.connection = new Socket(ip, port);
		this.input = new Scanner(connection.getInputStream());
		this.output = new PrintWriter(this.connection.getOutputStream(), true);
		
		NetworkController.LOGGER.log(Level.INFO, "Successfully connected to {0}:{1}", new Object[] {ip, port});
		
		Thread receiver = new Thread(this::receiveAndPublish);
		receiver.setDaemon(true);
		receiver.setUncaughtExceptionHandler((t,e) -> NetworkController.LOGGER.log(Level.WARNING, "An Error occured in the receiver thread", e));
		receiver.start();
	}	

	@Override
	public void close() {
		NetworkController.LOGGER.log(Level.INFO, "Disconnecting from server");
		
		this.input.close();
		this.output.close();
		try {
			this.connection.close();
		} catch (IOException e) {
			NetworkController.LOGGER.log(Level.INFO, "Failed to close connection", e);
		}
	}

	public void sendMessage(Message message) {
		
		JsonMessageConverter converter = new JsonMessageConverter();
		String messageString = converter.serialize(message);
		NetworkController.LOGGER.log(Level.INFO, "Sending message of type {0} with content {1}", 
				new Object[] {message.getType(), messageString});
		this.output.println(messageString);
	}

	private void receiveAndPublish() {
		try {
			while (this.input.hasNext()) {
				Message message = receiveMessage();
				this.publisher.submit(message);
			}
		} catch (Exception e) {
			this.publisher.closeExceptionally(e);
		}
		this.publisher.close();
	}
	
	private Message receiveMessage() {
		JsonMessageConverter converter = new JsonMessageConverter();
		String messageStr = this.input.next();
		Message message =  converter.deserialize(messageStr);
		NetworkController.LOGGER.log(Level.INFO, "Received message of type {0} with content {1}", 
				new Object[] {message.getType(), messageStr});
		return message;
	}
}

	