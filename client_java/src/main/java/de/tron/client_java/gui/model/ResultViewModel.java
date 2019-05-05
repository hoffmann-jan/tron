package de.tron.client_java.gui.model;

import de.tron.client_java.network.message.Player;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

public class ResultViewModel {

	private final ObjectProperty<Color> playerColor = new SimpleObjectProperty<>();
	private final StringProperty playerName = new SimpleStringProperty();
	
	public void refreshProperties(Player winner) {
		this.playerName.set(String.format("%s (%d)", winner.getName(), winner.getId()));
		this.playerColor.set(Color.web(String.format("0x%06X", winner.getColor())));
	}
	
	public ObjectProperty<Color> playerColorProperty() {
		return this.playerColor;
	}
	
	public StringProperty playerNameProperty() {
		return this.playerName;
	}
	
}
