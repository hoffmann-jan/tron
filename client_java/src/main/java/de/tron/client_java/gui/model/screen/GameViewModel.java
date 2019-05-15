package de.tron.client_java.gui.model.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.AbstractMap.SimpleEntry;

import de.tron.client_java.gui.model.data.Rectangle;
import de.tron.client_java.model.GameController;
import de.tron.client_java.model.data.Position;
import de.tron.client_java.network.message.Player;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

public class GameViewModel {

	private final GameController controller;
	
	private final BooleanProperty updatesExist = new SimpleBooleanProperty();
	
	private final DoubleProperty width = new SimpleDoubleProperty();
	private final DoubleProperty height = new SimpleDoubleProperty();
	
	private final ListProperty<Rectangle> playerRectangels = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final MapProperty<Integer, List<Rectangle>> playerTails =  new SimpleMapProperty<>(FXCollections.observableHashMap());
	
	public GameViewModel(GameController controller) {
		this.controller = controller;
	}
	
	/**
	 * Update the data of the game screen
	 * 
	 */
	public void refresh() {
		this.playerRectangels.clear();
		this.playerTails.clear();
		refreshPlayers();
		refreshTails();
		this.updatesExist.set(true);
	}


	private void refreshPlayers() {
		this.controller.getUpdatedPlayers()
			.stream()
			.map(this::playerToRectangle)
			.forEach(playerRectangels::add);
	}
	
	private void refreshTails() {
		this.controller.getPlayerModels()
		.entrySet()
		.stream()
		.map(e -> tailToRectangles(e.getKey(), e.getValue()))
		.forEach(e -> this.playerTails.put(e.getKey(), e.getValue()));
	}

	private Entry<Integer, List<Rectangle>> tailToRectangles(Player player, Queue<Position> tail) {
		List<Rectangle> rectangles = new ArrayList<>();
		for (Position position : tail) {
			Rectangle rectangle = new Rectangle();
			rectangle.setWidth(4 * (this.width.get() / GameController.FIELD_SIZE));
			rectangle.setX(position.getX() * (this.width.get() / GameController.FIELD_SIZE));
			rectangle.setY(position.getY() * (this.height.get() / GameController.FIELD_SIZE));
			rectangle.setFill(Color.web(String.format("0x%06X", player.getColor())));
			rectangles.add(rectangle);
		}
		return new SimpleEntry<>(player.getId(), rectangles);
	}

	private Rectangle playerToRectangle(Player player) {
		int playerSize = player.getPosition().isJumping() 
				? GameController.PLAYER_JUMPING_SIZE 
				: GameController.PLAYER_SIZE;
		
		double widthScale = this.width.get() / GameController.FIELD_SIZE;
		double heightScale = this.height.get() / GameController.FIELD_SIZE;
		
		Rectangle rectangle = new Rectangle();
		rectangle.setX(player.getPosition().getX() * widthScale);
		rectangle.setY(player.getPosition().getY() * heightScale);
		rectangle.setWidth(playerSize * widthScale);
		rectangle.setHeight(playerSize * heightScale);
		rectangle.setFill(getPlayerFill(player));
		return rectangle;
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
	
	public BooleanProperty updatesExistProperty() {
		return this.updatesExist;
	}
	
	public ListProperty<Rectangle> playerRectangelsProperty() {
		return this.playerRectangels;
	}

	public MapProperty<Integer, List<Rectangle>> playerTailsProperty() {
		return this.playerTails;
	}

	public DoubleProperty widthProperty() {
		return this.width;
	}
	
	public DoubleProperty heightProperty() {
		return this.height;
	}
	
}
