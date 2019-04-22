package de.tron.client_java.gui;

import java.util.Map.Entry;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;

public class View implements Subscriber<GuiMessage> {

	private ViewModel viewModel;
	
	private Subscription subscription;
	
	@FXML 
	private Canvas field;
	
	@FXML 
	private void initialize() {
		this.viewModel = new ViewModel();
		this.viewModel.subscribe(this);
	}
	
	public void changeDirection(KeyEvent event) {	
		this.viewModel.changeDirection(event.getCode());
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
	}

	@Override
	public void onNext(GuiMessage item) {
		if (item.updatesExist()) {
			GraphicsContext context = this.field.getGraphicsContext2D();
			context.clearRect(0, 0, this.field.getWidth(), this.field.getHeight());
			for (Entry<Color, Integer[]> update : item.getPlayerUpdates().entrySet()) {
				context.setFill(update.getKey());
				double x = update.getValue()[0].doubleValue();
				double y = update.getValue()[1].doubleValue();
				context.fillRect(x, y, 10, 10);
			}
		}
		this.subscription.request(1);
		
	}

	@Override
	public void onError(Throwable throwable) {
		// TODO display error
		
	}

	@Override
	public void onComplete() {
		// TODO display end of the game
		
	}


}
