package de.tron.client_java.gui;

import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import de.tron.client_java.network.NetworkController;
import de.tron.client_java.network.message.Coordinate;
import de.tron.client_java.network.message.Message;
import de.tron.client_java.network.message.MessageType;
import javafx.scene.paint.Color;

public class ViewModel implements Processor<Message, GuiMessage> {
	
	private static final long OFFER_TIMEOUT = 10; 
	
	private final SubmissionPublisher<GuiMessage> publisher = new SubmissionPublisher<>();
	private Subscription subscription;
	
	private NetworkController controller;
	
	private final Map<Integer, Color> playerColors = new HashMap<>();
	
	public ViewModel() {
		this.controller = new NetworkController();
		this.controller.subscribe(this);
	}
	
	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
	}

	@Override
	public void onNext(Message item) {
		switch (item.getType()) {
		case UPDATE:
		case CONNECT:
			GuiMessage message = convertToGuiMessage(item); 
			publishMessage(message);
			break;
		case ADD:
			addPlayer(item.getId());
			break;
		default:
			break;
		}
		this.subscription.request(1);
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
	public void subscribe(Subscriber<? super GuiMessage> subscriber) {
		this.publisher.subscribe(subscriber);
	}
	
	private GuiMessage convertToGuiMessage(Message message) {
		GuiMessage guiMessage = new GuiMessage();
		if (message.getType() == MessageType.UPDATE) {
			guiMessage.setUpdatesExist(true);
			converUpdates(message, guiMessage);
		} else if (message.getType() == MessageType.CONNECT) {
			guiMessage.setConnectionAccepted(true);
		}
		return guiMessage;
	}

	private void converUpdates(Message message, GuiMessage guiMessage) {
		message.getUpdatedCoordinates()
			.forEach(c -> convertUpdate(guiMessage, c));
	}

	private void convertUpdate(GuiMessage guiMessage, Coordinate coordinate) {
		Color color = this.playerColors.get(coordinate.getPlayerId());
		Integer[] update = new Integer[2];
		update[0] = coordinate.getX();
		update[1] = coordinate.getY();
		guiMessage.putPlayerUpdate(color, update);
	}
	
	private void addPlayer(int id) {
		switch (id) {
			case 1:
				this.playerColors.put(id, Color.RED);
				break;
			case 2:
				this.playerColors.put(id, Color.BLUE);						
				break;
			case 3:
				this.playerColors.put(id, Color.GREEN);										
				break;
			case 4:
				this.playerColors.put(id, Color.YELLOW);
				break;
			default:
				break;
		}		
	}
	
	private void publishMessage(GuiMessage message) {
		this.publisher.offer(message, ViewModel.OFFER_TIMEOUT, TimeUnit.MILLISECONDS, null);		
	}

}
