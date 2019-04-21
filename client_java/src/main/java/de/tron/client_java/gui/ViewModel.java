package de.tron.client_java.gui;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import de.tron.client_java.model.data.Bike;

public class ViewModel implements Subscriber<Bike> {

	private Subscription subscription;
	
	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
	}

	@Override
	public void onNext(Bike item) {
		// TODO display bike
		
	}

	@Override
	public void onError(Throwable throwable) {
		// TODO unexpected end of the game
		
	}

	@Override
	public void onComplete() {
		// TODO normal end of the game
		
	}

}
