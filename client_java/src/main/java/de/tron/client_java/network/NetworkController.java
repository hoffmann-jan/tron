package de.tron.client_java.network;

import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.ForkJoinPool;

import de.tron.client_java.network.message.Message;
import de.tron.client_java.network.message.converter.JsonMessageConverter;

public class NetworkController implements Closeable {

	private static final long OFFER_TIMEOUT = 10; 
	
	private final SubmissionPublisher<Message> publisher = new SubmissionPublisher<>(ForkJoinPool.commonPool(), 4);
	
	private DatagramSocket connection;
	
	private int port;
	private InetAddress address;
	
	public void subscribe(Subscriber<? super Message> subscriber) {
		this.publisher.subscribe(subscriber);
	}
	
	public void configureConnection(String address, int port) throws IOException {
		this.address = InetAddress.getByName(address);
		this.port = port;
		System.out.println("coonect");
		this.connection = new DatagramSocket(5000);
		
		Thread receive = new Thread(this::receiveAndPublish);
		receive.setDaemon(true);
		receive.start();
	}	

	@Override
	public void close() {
		this.connection.close();
		this.publisher.close();
	}
	
	public void sendMessage(Message message) {
		JsonMessageConverter converter = new JsonMessageConverter();
		String messageString = converter.serialize(message);
		byte[] packetData = messageString.getBytes();
		try {
			this.connection.send(new DatagramPacket(packetData, packetData.length, this.address, this.port));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void receiveAndPublish() {
		DatagramPacket packet;
		try {
			while (true) {
				packet = new DatagramPacket(new byte[1024], 1024);
				this.connection.receive(packet);
				Message message = receiveMessage(packet);
				publishMessage(message);
			}
		} catch (Exception e) {
			this.publisher.closeExceptionally(e);
		}
	}

	private Message receiveMessage(DatagramPacket packet) {
		JsonMessageConverter converter = new JsonMessageConverter();
		String messageStr = new String(packet.getData());
		System.out.println(messageStr);
		return converter.deserialize(messageStr);
	}
	
	private void publishMessage(Message message) {
		this.publisher.offer(message, NetworkController.OFFER_TIMEOUT, TimeUnit.MILLISECONDS, null);		
	}
	
}
