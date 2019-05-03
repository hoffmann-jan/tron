package de.tron.client_java.gui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.AbstractMap.SimpleEntry;

import de.tron.client_java.model.GameController;
import de.tron.client_java.model.Position;
import de.tron.client_java.network.message.Player;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

public class GameViewModel {

	private final GameController controller;
	
	private final BooleanProperty updatesExist = new SimpleBooleanProperty();
	
	private final ListProperty<Rectangle> playerRectangels = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final MapProperty<Integer, List<Rectangle>> playerTails =  new SimpleMapProperty<>(FXCollections.observableHashMap());
	
	public GameViewModel(GameController controller) {
		this.controller = controller;
	}
	
	public void refresh() {
		this.playerRectangels.clear();
		this.playerTails.clear();
		this.controller.getUpdatedPlayers()
			.stream()
			.map(this::playerToRectangel)
			.forEach(playerRectangels::add);
		this.controller.getPlayerModels()
			.entrySet()
			.stream()
			.map(e -> tailToRectangles(e.getKey(), e.getValue()))
			.forEach(e -> this.playerTails.put(e.getKey(), e.getValue()));
		this.updatesExist.set(true);
	}

	private Entry<Integer, List<Rectangle>> tailToRectangles(Player player, Queue<Position> tail) {
		List<Rectangle> rectangels = new ArrayList<>();
		for (Position position : tail) {
			Rectangle rectangel = new Rectangle();
			rectangel.setX(position.getX());
			rectangel.setY(position.getY());
			rectangel.setFill(Color.web(String.format("0x%06X", player.getColor())));
			rectangels.add(rectangel);
		}
		return new SimpleEntry<>(player.getId(), rectangels);
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
	
	public BooleanProperty updatesExistProperty() {
		return this.updatesExist;
	}
	
	public ListProperty<Rectangle> playerRectangelsProperty() {
		return this.playerRectangels;
	}

	public MapProperty<Integer, List<Rectangle>> playerTailsProperty() {
		return this.playerTails;
	}

}
