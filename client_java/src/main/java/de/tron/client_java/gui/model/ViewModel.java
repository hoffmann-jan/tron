package de.tron.client_java.gui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.function.Consumer;

import de.tron.client_java.model.GameController;
import de.tron.client_java.model.GameMessage;
import de.tron.client_java.model.Position;
import de.tron.client_java.network.message.Player;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

public class ViewModel implements Subscriber<GameMessage> {

	private final GameController controller = new GameController();
	private Subscription subscription;
	
	private Runnable onShowLobby;
	private Runnable onUpdate;
	private Runnable onStart;
	private Consumer<String> onStatusChange;

	private final ListProperty<Rectangle> playerRectangels = new SimpleListProperty<>(
			FXCollections.observableArrayList());
	private final ListProperty<Rectangle> playerTails = new SimpleListProperty<>(FXCollections.observableArrayList());

	private final ConnectionViewModel connectionModel = new ConnectionViewModel(this.controller);
	private final LobbyViewModel lobbyModel = new LobbyViewModel(this.controller);

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
			Platform.runLater(() -> onStatusChange.accept(item.getMessage())); 
			break;
		case STATUS:
			Platform.runLater(() -> onStatusChange.accept(item.getMessage())); 
			break;
		case LOBBY:
			Platform.runLater(this.onShowLobby); 
			break;
		case RESULT:
			break;
		case START:
			Platform.runLater(this.onStart); 
			break;
		case UPDATE:
			updateContent();
			this.onUpdate.run();
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

	private void updateContent() {
		this.playerRectangels.clear();
		this.playerTails.clear();
		this.controller.getUpdatedPlayers()
			.stream()
			.map(this::playerToRectangel)
			.forEach(playerRectangels::add);
		this.controller.getPlayerModels()
			.entrySet()
			.stream()
			.map(e -> tailToRectangles(e.getKey().getColor(), e.getValue()))
			.flatMap(List::stream)
			.forEach(this.playerTails::add);
	}

	private List<Rectangle> tailToRectangles(int color, Queue<Position> tail) {
		List<Rectangle> rectangels = new ArrayList<>();
		int size = tail.size();
		int counter = size;
		for (Position position : tail) {
			double opacity = (1 - ((double) counter / size)) / 10;
			opacity = counter == size ? 1 : opacity;
			Rectangle rectangel = new Rectangle();
			rectangel.setX(position.getX());
			rectangel.setY(position.getY());
			rectangel.setWidth(GameController.PLAYER_SIZE);
			rectangel.setHeight(GameController.PLAYER_SIZE);
			rectangel.setFill(Color.web(String.format("0x%06X", color), opacity));
			counter--;
			rectangels.add(rectangel);
		}
		return rectangels;
	}

	private Rectangle playerToRectangel(Player player) {
		Rectangle rectangel = new Rectangle();
		rectangel.setX(player.getPosition().getX());
		rectangel.setY(player.getPosition().getY());
		rectangel.setWidth(GameController.PLAYER_SIZE);
		rectangel.setHeight(GameController.PLAYER_SIZE);
		rectangel.setFill(getPlayerFill(player));
		return rectangel;
	}

	private Color getPlayerFill(Player player) {
		return this.controller.getOriginalPlayers()
				.stream()
				.filter(p -> p.getId() == player.getId())
				.findFirst()
				.map(Player::getColor)
				.map(c -> Color.web(String.format("0x%06X", c)))
				.orElse(Color.WHITE);
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
	
	public void setOnUpdate(Runnable action) {
		this.onUpdate = action;
	}

	public void setOnShowLobby(Runnable action) {
		this.onShowLobby = action;
	}
	
	public void setOnStatusChange(Consumer<String> action) {
		this.onStatusChange = action;
	}

	public ListProperty<Rectangle> playerRectangelsProperty() {
		return this.playerRectangels;
	}

	public ListProperty<Rectangle> playerTailsProperty() {
		return this.playerTails;
	}

	public ConnectionViewModel getConnectionViewModel() {
		return this.connectionModel;
	}
	
	public LobbyViewModel getLobbyViewModel() {
		return this.lobbyModel;
	}

}
