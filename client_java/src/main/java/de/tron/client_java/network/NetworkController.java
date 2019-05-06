package de.tron.client_java.network;

import java.util.concurrent.SubmissionPublisher;
import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Flow.Subscriber;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.concurrent.ForkJoinPool;

import de.tron.client_java.network.message.Message;
import de.tron.client_java.network.message.converter.JsonMessageConverter;

public class NetworkController implements Closeable {
	
	private static final Logger LOGGER = Logger.getLogger("root");
	
	private final SubmissionPublisher<Message> publisher = new SubmissionPublisher<>(ForkJoinPool.commonPool(), 4);
	
	private SocketChannel channel;
	private Selector selector;
	
	private long lastTime = 0;
	
	public void subscribe(Subscriber<? super Message> subscriber) {
		this.publisher.subscribe(subscriber);
	}
	
	public void configureConnection(String ip, int port) throws IOException {
		NetworkController.LOGGER.log(Level.INFO, "Trying to connect to {0}:{1}", new Object[] {ip, port});
		
		this.channel = SocketChannel.open(new InetSocketAddress(ip, port));
		this.channel.configureBlocking(false);
		
		this.selector = Selector.open();
		this.channel.register(selector, SelectionKey.OP_READ);
		
		NetworkController.LOGGER.log(Level.INFO, "Successfully connected to {0}:{1}", new Object[] {ip, port});
		
		Thread receiver = new Thread(this::receiveAndPublish);
		receiver.setDaemon(true);
		receiver.setUncaughtExceptionHandler((t,e) -> 
			NetworkController.LOGGER.log(Level.WARNING, "An Error occured in the receiver thread", e));
		receiver.start();
	}	

	@Override
	public void close() {
		NetworkController.LOGGER.log(Level.INFO, "Disconnecting from server");
		
		try {
			this.channel.close();
		} catch (IOException e) {
			NetworkController.LOGGER.log(Level.INFO, "Failed to close connection");
		}
	}
	
	public void sendMessage(Message message) {
		JsonMessageConverter converter = new JsonMessageConverter();
		String messageString = converter.serialize(message);
		
		NetworkController.LOGGER.log(Level.INFO, "Sending message of type {0} with content {1}", 
				new Object[] {message.getType(), messageString});
		
		try {
			this.channel.write(ByteBuffer.wrap(messageString.getBytes()));
		} catch (IOException e) {
			NetworkController.LOGGER.log(Level.INFO, "Failed to send message", e);
		}
	}
	
	private void receiveAndPublish() {
		try {
			while (this.channel.isConnected()) {
				this.selector.select();
				Iterator<SelectionKey> keys = this.selector.selectedKeys().iterator();
				iterateKeys(keys);
			}
		} catch (Exception e) {
			this.publisher.closeExceptionally(e);
			throw new RuntimeException("An exception occured in the receiver thread", e);
		}
		this.publisher.close();
	}

	private void iterateKeys(Iterator<SelectionKey> keys) {
		while (keys.hasNext()) {
			SelectionKey selectedKey = keys.next();
			if (selectedKey.isReadable()) {
				getReceivedMessages().forEach(this.publisher::submit);
			}
			keys.remove();
		}
	}

	private List<Message> getReceivedMessages() {
		JsonMessageConverter converter = new JsonMessageConverter();
		List<Message> messages = new ArrayList<>();
		
		long thisTime = System.currentTimeMillis();
		NetworkController.LOGGER.log(Level.INFO, "Time passed since last message: {0}", thisTime - this.lastTime);
		this.lastTime = thisTime;
		
		for (String messageString : getNewMessageStrings()) {
			Message message = converter.deserialize(messageString);
			NetworkController.LOGGER.log(Level.INFO, "Received message of type {0} with content {1}", 
					new Object[] {message.getType(), messageString});
			messages.add(message);
		}
		
		return messages;
	}

	private List<String> getNewMessageStrings() {
		List<String> messages;
		ByteBuffer buffer = ByteBuffer.allocate(2048);
		if (readInBuffer(buffer)) {
			String complete = new String(buffer.array());
			messages = Arrays.stream(complete.split("\n"))
					.filter(m -> !"".equals(m.trim()))
					.collect(Collectors.toList());
		} else {
			messages = Collections.emptyList();			
		}

		buffer.clear();
		return messages;
	}

	private boolean readInBuffer(ByteBuffer buffer) {
		try {
			return this.channel.read(buffer) != -1;
		} catch (IOException e) {
			NetworkController.LOGGER.log(Level.INFO, "Failed to read received messages", e);
			return false;
		}
	}
	
}
