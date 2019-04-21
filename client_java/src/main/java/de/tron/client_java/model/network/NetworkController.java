package de.tron.client_java.model.network;

import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.ForkJoinPool;

import de.tron.client_java.model.data.MessageProcessor;
import de.tron.client_java.model.network.message.Message;

public class NetworkController {

	private final SubmissionPublisher<Message> publisher = new SubmissionPublisher<>(ForkJoinPool.commonPool(), 4);

	public void registerSubscriber(Subscriber<Message> subscriber) {
		
	}
	
}
