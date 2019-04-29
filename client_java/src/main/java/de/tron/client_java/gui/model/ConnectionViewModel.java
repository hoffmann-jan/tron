package de.tron.client_java.gui.model;

import java.io.IOException;

import de.tron.client_java.model.ConnectionData;
import de.tron.client_java.model.GameController;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

public class ConnectionViewModel {

	private final GameController controller;
	
	private final BooleanProperty isConnecting = new SimpleBooleanProperty(false);
	
	private final StringProperty ip = new SimpleStringProperty();
	private final StringProperty port = new SimpleStringProperty();
	private final StringProperty room = new SimpleStringProperty();
	private final StringProperty name = new SimpleStringProperty();
	
	private final ObjectProperty<Color> color = new SimpleObjectProperty<>();

	public ConnectionViewModel(GameController controller) {
		this.controller = controller;
	}
	
	public void connect() {
		this.isConnecting.set(true);
		ConnectionData data = createConnectionData();
		try {
			this.controller.connect(data);
		} catch (IOException e) {
			e.printStackTrace();
			this.isConnecting.set(false);
			// TODO set style of ip and port to error
		}
	}
	
	public void connectionWasRefused() {
		this.isConnecting.set(false);
		// TODO set style of lobby number to error
	}

	private ConnectionData createConnectionData() {
		ConnectionData data = new ConnectionData();
		data.setIp(this.ip.get());
		data.setPort(parseToInt(this.port.get()));
		data.setLobbyNumber(parseToInt(this.room.get()));
		data.setName(this.name.get());
		data.setColor(parseColor());
		return data;
	}

	private int parseToInt(String value) {
		if (value != null) {
			return Integer.valueOf(value);
		} else {
			return -1;
		}
	}

	private int parseColor() {
		Color value = this.color.get();
		if (value != null) {
			String hexColor = String.format( "%02X%02X%02X",
		            (int)( value.getRed() * 255 ),
		            (int)( value.getGreen() * 255 ),
		            (int)( value.getBlue() * 255 ) );
			return Integer.valueOf(hexColor, 16);
		}
		return 0;
	}

	public StringProperty ipProperty() {
		return this.ip;
	}

	public StringProperty portProperty() {
		return this.port;
	}

	public StringProperty roomProperty() {
		return this.room;
	}

	public StringProperty nameProperty() {
		return this.name;
	}

	public ObjectProperty<Color> colorProperty() {
		return this.color;
	}

	public BooleanProperty isConnectingProperty() {
		return this.isConnecting;
	}

}
