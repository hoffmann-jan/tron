package de.tron.client_java.network;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.tron.client_java.network.encryption.SecurityHandler;
import de.tron.client_java.network.exception.NoConnectionMessageException;
import de.tron.client_java.network.message.Message;
import de.tron.client_java.network.message.MessageType;
import de.tron.client_java.network.message.converter.JsonMessageConverter;
/**
 * Class for everything that is related to network communication
 * <ul>
 * 	<li>Establish connection</li>
 * 	<li>Sending messages</li>
 * 	<li>Receiving messages</li>
 * 	<li>Closing connection</li>
 * </ul>
 * 
 * @author emaeu
 *
 */
public class NetworkController implements Closeable, Publisher<Message> {

	private static final Logger LOGGER = Logger.getLogger("root");
	
	private final SubmissionPublisher<Message> publisher = new SubmissionPublisher<>(ForkJoinPool.commonPool(), 4);
	
	private SecurityHandler securityHandler;
	
	private Socket connection;
	private Scanner input;
	private PrintWriter output;

	@Override
	public void subscribe(Subscriber<? super Message> subscriber) {
		this.publisher.subscribe(subscriber);
	}
	
	/**
	 * Connect to server, determine encoding between client and server and start thread 
	 * which will receive the server messages
	 * 
	 * @param address
	 * @param port
	 * @throws IOException
	 */
	public Message connect(String address, int port, Message connectionMessage) throws IOException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Message> connector = executor.submit(() -> handShake(address, port, connectionMessage));
		
	 	Message response;
		try {
			response = connector.get(10, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			connector.cancel(true);
			executor.shutdownNow();
			throw new IOException("Stopped connecting because of a time out", e);
		} catch (InterruptedException | ExecutionException e) {
			connector.cancel(true);
			throw new IOException("Failed to connect to the server", e);
		} finally {
			executor.shutdown();
		}
		
		startReceiverThread();
	 	return response;
	}

	private Message handShake(String address, int port, Message connectionMessage) throws IOException {
		connectSockets(address, port);
		if (Thread.currentThread().isInterrupted())
			return null;
		initializeEncryption();
		if (Thread.currentThread().isInterrupted())
			return null;
		return initializeConnection(connectionMessage);
	}

	private void connectSockets(String ip, int port) throws IOException {
		NetworkController.LOGGER.log(Level.INFO, "Trying to connect to {0}:{1}", new Object[] {ip, port});
		
		this.connection = new Socket(ip, port);
		this.connection.setTcpNoDelay(true);
		this.input = new Scanner(connection.getInputStream());
		this.output = new PrintWriter(this.connection.getOutputStream(), true);
		
		NetworkController.LOGGER.log(Level.INFO, "Successfully connected to {0}:{1}", new Object[] {ip, port});
	}

	private void initializeEncryption() throws IOException {
		try {
			NetworkController.LOGGER.log(Level.INFO, "Trying to exchange encryption keys");
			this.securityHandler = new SecurityHandler();
			this.securityHandler.doHandShake(this.input, this.output);
			NetworkController.LOGGER.log(Level.INFO, "Successfully exchanged encryption keys");
		} catch (GeneralSecurityException e) {
			NetworkController.LOGGER.log(Level.INFO, "Failed to exchange encryption keys");
			close();
			throw new IOException("Couldn't perform handshake for encryption with the server", e);
		}
	}	
	
	private Message initializeConnection(Message connectionMessage) throws IOException {
		sendMessage(connectionMessage);
		return waitForConnectResponse();
	}
	
	private Message waitForConnectResponse() throws IOException {
		NetworkController.LOGGER.log(Level.INFO, "Waiting for connection message");
		Message message = null;
		if (this.input.hasNext()) {
			message = receiveMessage();
			if (MessageType.CONNECT == message.getType()) {
				NetworkController.LOGGER.log(Level.INFO, "Received connection message");
				return message;
			} else {
				NetworkController.LOGGER.log(Level.WARNING, "Failed to establish connection because server "
						+ "has sent something else than a connection message");
				throw new NoConnectionMessageException();
			}
		}
		NetworkController.LOGGER.log(Level.WARNING, "Failed to establish connection because server "
				+ "has sent no connection message");
		throw new NoConnectionMessageException();
	}

	private void startReceiverThread() {
		Thread receiver = new Thread(this::receiveAndPublish);
		receiver.setDaemon(true);
		receiver.setUncaughtExceptionHandler((t,e) -> NetworkController.LOGGER.log(Level.WARNING, "An Error occured in the receiver thread", e));
		receiver.start();
	}
	
	@Override
	public void close() {
		if (this.connection != null && !this.connection.isClosed()) {
			NetworkController.LOGGER.log(Level.INFO, "Trying to disconnect from server");
			try {
				this.connection.close();
				NetworkController.LOGGER.log(Level.INFO, "Successfully disconnected from server");
			} catch (IOException e) {
				NetworkController.LOGGER.log(Level.WARNING, "Failed to close connection", e);
			}
		}
	}

	/**
	 * Converts message to JSON string. Afterwards this string will be encrypted and 
	 * send to the server
	 * 
	 * @param message
	 */
	public void sendMessage(Message message) {
		String messageString = JsonMessageConverter.serialize(message);
		NetworkController.LOGGER.log(Level.INFO, "Sending message of type {0} with content {1}", 
				new Object[] {message.getType(), messageString});
		messageString = this.securityHandler.encrypt(messageString);
		this.output.println(messageString);
	}

	private void receiveAndPublish() {
		try {
			// End of while is reached if input is closed
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
		String messageString = this.input.next();
		messageString = this.securityHandler.decrypt(messageString);
		Message message =  JsonMessageConverter.deserialize(messageString);
		NetworkController.LOGGER.log(Level.INFO, "Received message of type {0} with content {1}", 
				new Object[] {message.getType(), messageString});
		return message;
	}
}

	