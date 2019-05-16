package de.tron.client_java.gui.model;

import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.tron.client_java.gui.model.screen.ConnectionViewModel;
import de.tron.client_java.gui.model.screen.GameViewModel;
import de.tron.client_java.gui.model.screen.LobbyViewModel;
import de.tron.client_java.gui.model.screen.ResultViewModel;
import de.tron.client_java.model.GameController;
import de.tron.client_java.model.data.GameMessage;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.KeyCode;

/**
 * Main class of the view model. Contains the complete data of the view and acts as 
 * interface between the model and the view 
 * 
 * @author emaeu
 *
 */
public class ViewModel implements Subscriber<GameMessage> {

	private static final Logger LOGGER = Logger.getLogger("root");
	
	private final GameController controller = new GameController();
	private Subscription subscription;
	
	private Runnable onShowLobby;
	private Runnable onStart;
	private Runnable onShowResult;
	private Runnable onReturnToStart;

	private final ConnectionViewModel connectionModel = new ConnectionViewModel(this.controller, this);
	private final LobbyViewModel lobbyModel = new LobbyViewModel(this.controller);
	private final GameViewModel gameModel = new GameViewModel(this.controller);
	private final ResultViewModel resultModel = new ResultViewModel();

	private final StringProperty status = new SimpleStringProperty();
	
	public ViewModel() {
		this.controller.subscribe(this);
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
	}
	
	/**
	 * Determines what the view will do with the received message
	 * 
	 */
	@Override
	public void onNext(GameMessage item) {
		ViewModel.LOGGER.log(Level.INFO, "Receiving information in view model");
		switch (item.getInformation()) {
		case CONNECTED:
			this.connectionModel.isConnectingProperty().set(false);
			Platform.runLater(() -> this.status.set(item.getMessage())); 
			break;
		case REFUSED:
			this.connectionModel.connectionWasRefused();
			break;
		case PLAYER_CHANGE:		
			Platform.runLater(this.lobbyModel::refresh); 
			// Fall through is wanted
		case STATUS:
			Platform.runLater(() -> this.status.set(item.getMessage())); 
			break;
		case LOBBY:
			Platform.runLater(this.lobbyModel::refresh); 
			Platform.runLater(this.onShowLobby); 
			break;
		case RESULT:
			Platform.runLater(() -> this.resultModel.refreshProperties(this.controller.getWinner()));
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
		ViewModel.LOGGER.log(Level.WARNING, "Flow was closed because of an exception", throwable);
		this.controller.disconnect();
		Platform.runLater(this.onReturnToStart);
	}

	@Override
	public void onComplete() {
		ViewModel.LOGGER.log(Level.INFO, "Flow was closed properly");
		this.controller.disconnect();
		Platform.runLater(this.onReturnToStart);
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
	
	public void setOnReturnToStart(Runnable action) {
		this.onReturnToStart = action;		
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
	
	public ResultViewModel getResultViewModel() {
		return this.resultModel;
	}
	
	public StringProperty statusProperty() {
		return this.status;
	}

}
