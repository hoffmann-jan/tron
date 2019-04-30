package de.tron.client_java.gui.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import de.tron.client_java.model.GameController;
import de.tron.client_java.model.Position;
import de.tron.client_java.network.message.Player;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

public class GameViewModel {

	private final GameController controller;
	
	private final BooleanProperty updatesExist = new SimpleBooleanProperty();
	
	private final ListProperty<Rectangle> playerRectangels = new SimpleListProperty<>(FXCollections.observableArrayList());
	private final ListProperty<Rectangle> playerTails = new SimpleListProperty<>(FXCollections.observableArrayList());
	
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
			.map(e -> tailToRectangles(e.getKey().getColor(), e.getValue()))
			.flatMap(List::stream)
			.forEach(this.playerTails::add);
		this.updatesExist.set(true);
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
	
	public BooleanProperty updatesExistProperty() {
		return this.updatesExist;
	}
	
	public ListProperty<Rectangle> playerRectangelsProperty() {
		return this.playerRectangels;
	}

	public ListProperty<Rectangle> playerTailsProperty() {
		return this.playerTails;
	}

}
