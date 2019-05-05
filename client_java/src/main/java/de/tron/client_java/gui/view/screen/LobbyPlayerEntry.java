package de.tron.client_java.gui.view.screen;

import java.io.IOException;

import de.tron.client_java.App;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;

public class LobbyPlayerEntry extends HBox {

	@FXML private Rectangle playerColor;
	@FXML private Label playerName;
	
	public LobbyPlayerEntry() {
		FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("LobbyPlayer.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	public StringProperty playerNameProperty() {
		return this.playerName.textProperty();
	}
	
	public void setPlayerName(String name) {
		this.playerName.setText(name);
	}
	
	public ObjectProperty<Paint> playerColorProperty() {
		return this.playerColor.fillProperty();
	}
	
	public void setPlayerColor(Color color) {
		this.playerColor.setFill(color);
	}

}
