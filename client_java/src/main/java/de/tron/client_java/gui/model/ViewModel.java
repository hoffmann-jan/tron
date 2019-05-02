package de.tron.client_java.gui.model;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;

import de.tron.client_java.model.GameController;
import de.tron.client_java.model.GameMessage;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCode;

public class ViewModel implements Subscriber<GameMessage> {

	private final GameController controller = new GameController();
	private Subscription subscription;
	
	private Runnable onShowLobby;
	private Runnable onStart;
	private Runnable onShowResult;

	private final ConnectionViewModel connectionModel = new ConnectionViewModel(this.controller);
	private final LobbyViewModel lobbyModel = new LobbyViewModel(this.controller);
	private final GameViewModel gameModel = new GameViewModel(this.controller);

	private final StringProperty status = new SimpleStringProperty();
	
	public ViewModel() {
		this.controller.subscribe(this);
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
	}

	@Override
	public void onNext(GameMessage item) {
		switch (item.getInformation()) {
		case REFUSED:
			this.connectionModel.connectionWasRefused();
			break;
		case NEW_PLAYER:		
			Platform.runLater(this.lobbyModel::refresh); 
			Platform.runLater(() -> this.status.set(item.getMessage())); 
			break;
		case STATUS:
			Platform.runLater(() -> this.status.set(item.getMessage())); 
			break;
		case LOBBY:
			Platform.runLater(this.lobbyModel::refresh); 
			Platform.runLater(this.onShowLobby); 
			break;
		case RESULT:
			Platform.runLater(this.onShowResult);
			break;
		case START:
			Platform.runLater(this.onStart); 
			break;
		case UPDATE:
			Platform.runLater(this.gameModel::refresh);
			break;
		}
		this.subscription.request(1);
	}

	@Override
	public void onError(Throwable throwable) {
		// TODO Error
	}

	@Override
	public void onComplete() {
		// TODO
	}

	public void changeDirection(KeyCode code) {
		this.controller.changeDirection(code.getChar().charAt(0));
	}

	public void disconnect() {
		this.controller.disconnect();
		System.exit(0);
	}
	
	public void setOnStart(Runnable action) {
		this.onStart = action;
	}

	public void setOnShowLobby(Runnable action) {
		this.onShowLobby = action;
	}
	
	public void setOnResult(Runnable action) {
		this.onShowResult = action;		
	}

	public ConnectionViewModel getConnectionViewModel() {
		return this.connectionModel;
	}
	
	public LobbyViewModel getLobbyViewModel() {
		return this.lobbyModel;
	}

	public GameViewModel getGameViewModel() {
		return this.gameModel;
	}
	
	public StringProperty statusProperty() {
		return this.status;
	}

}
