package de.tron.client_java.model.data;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import de.tron.client_java.model.network.message.Message;

public class MessageProcessor implements Processor<Message, Bike> {

	private final SubmissionPublisher<Bike> publisher = new SubmissionPublisher<>(ForkJoinPool.commonPool(), 4);

	private Subscription subscription;
	
	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
	}

	@Override
	public void onNext(Message item) {
		// TODO convert message to bike and publish bike
		
	}

	@Override
	public void onError(Throwable throwable) {
		this.publisher.closeExceptionally(throwable);		
	}

	@Override
	public void onComplete() {
		this.publisher.close();		
	}

	@Override
	public void subscribe(Subscriber<? super Bike> subscriber) {
		this.publisher.subscribe(subscriber);		
	}

}
