package de.tron.client_java.gui.model.screen;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;

import de.tron.client_java.gui.model.ViewModel;
import de.tron.client_java.model.GameController;
import de.tron.client_java.model.data.ConnectionData;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.paint.Color;

public class ConnectionViewModel {
	
	private static final Logger LOGGER = Logger.getLogger("root");
	
	private static final String NORMAL_INPUT_ID = "input";
	private static final String ERROR_INPUT_ID = "input-error";
	
	private final GameController controller;
	
	private final BooleanProperty isConnecting = new SimpleBooleanProperty(false);
	
	private final StringProperty ipID = new SimpleStringProperty();
	private final StringProperty portID= new SimpleStringProperty();
	private final StringProperty lobbyID = new SimpleStringProperty();
	private final StringProperty nameID = new SimpleStringProperty();
	private final StringProperty colorID = new SimpleStringProperty();
	
	private final StringProperty ip = new SimpleStringProperty();
	private final StringProperty port = new SimpleStringProperty();
	private final StringProperty lobby = new SimpleStringProperty();
	private final StringProperty name = new SimpleStringProperty();
	
	private final ObjectProperty<Color> color = new SimpleObjectProperty<>();

	private final ViewModel viewModel;
	
	public ConnectionViewModel(GameController controller, ViewModel viewModel) {
		this.controller = controller;
		this.viewModel = viewModel;
		importConnectionData();
	}
	
	public void connect() {
		Service<String> service = new Service<>() {
			@Override
			protected Task<String> createTask() {
				return createConnectionTask();
			}
		};
		
		this.isConnecting.bind(service.runningProperty());
		service.valueProperty().addListener((p,o,n) -> this.viewModel.statusProperty().set(n));
		service.start();
		
	}
	
	private Task<String> createConnectionTask() {
		return new Task<String>() {
			@Override
			protected String call() throws Exception {
				ConnectionData data = createConnectionData();
				exportConnectionData(data);
				String status = null;
				try {
					controller.connect(data);
					status = "Successfully connected. Receiving lobby information";
				} catch (IOException e) {
					controller.disconnect();
					status = "Failed to connect";
					ipID.set(ERROR_INPUT_ID);
					portID.set(ERROR_INPUT_ID);
					String log = String.format("Failed to connect to %s:%d because of an exception", 
							data.getIp(), data.getPort());
					ConnectionViewModel.LOGGER.log(Level.WARNING, log, e);
				}
				return status;
			}
		};
	}
	
	private void importConnectionData() {
		Gson gson = new Gson();
		File dataFile = new File("connectionDetails.json");
		try (FileReader reader = new FileReader(dataFile)) {
			ConnectionData data = gson.fromJson(reader, ConnectionData.class);
			this.ip.set(data.getIp());
			this.port.set(Integer.toString(data.getPort()));
			this.lobby.set(Integer.toString(data.getLobbyNumber()));
			this.name.set(data.getName());
			this.color.set(Color.web(String.format("0x%06X", data.getColor())));
		}  catch (IOException e) {
			ConnectionViewModel.LOGGER.log(Level.INFO, 
				"Failed to load connection data from file \"connectionDetails.json\"");
		}
	}

	private void exportConnectionData(ConnectionData data) {
		Gson gson = new Gson();
		File dataFile = new File("connectionDetails.json");
		try (FileWriter writer = new FileWriter(dataFile)) {
			gson.toJson(data, writer);
		} catch (IOException e) {
			ConnectionViewModel.LOGGER.log(Level.INFO, 
				"Failed to save connection data in file \"connectionDetails.json\"");
		}
	}

	private ConnectionData createConnectionData() {
		resetIDs();
		
		ConnectionData data = new ConnectionData();
		data.setIp(checkEmpty(this.ip, this.ipID));
		data.setPort(parseToInt(this.port, this.portID));					
		data.setLobbyNumber(parseToInt(this.lobby, this.lobbyID));
		data.setName(checkEmpty(this.name, this.nameID));
		data.setColor(parseColor());
		return data;
	}

	private String checkEmpty(StringProperty property, StringProperty idProperty) {
		if (property.isEmpty().get()) {
			idProperty.set(ERROR_INPUT_ID);
			this.isConnecting.set(false);
			throw new IllegalArgumentException();
		} 
		return property.get();
	}

	private void resetIDs() {
		this.ipID.set(NORMAL_INPUT_ID);		
		this.portID.set(NORMAL_INPUT_ID);		
		this.lobbyID.set(NORMAL_INPUT_ID);	
		this.nameID.set(NORMAL_INPUT_ID);
		this.colorID.set(NORMAL_INPUT_ID);
	}

	private int parseToInt(StringProperty property, StringProperty idProperty) {
		if (property.isNotEmpty().get()) {
			try {
				return Integer.valueOf(property.get());
			} catch (NumberFormatException e) {
				idProperty.set(ERROR_INPUT_ID);
				this.isConnecting.set(false);
				this.viewModel.statusProperty().set(String.format("Value \"%s\" is invalid", property.get()));
				throw new IllegalArgumentException(e);
			}
		} else {
			return -1;
		}
	}

	private int parseColor() {
		Color value = this.color.get();
		if (value == null) {
			this.isConnecting.set(false);
			this.colorID.set(ERROR_INPUT_ID);
			throw new IllegalArgumentException();
		}
		String hexColor = String.format( "%02X%02X%02X",
				(int)( value.getRed() * 255 ),
				(int)( value.getGreen() * 255 ),
				(int)( value.getBlue() * 255 ) );
		return Integer.valueOf(hexColor, 16);
	}

	public StringProperty ipProperty() {
		return this.ip;
	}

	public StringProperty portProperty() {
		return this.port;
	}

	public StringProperty lobbyProperty() {
		return this.lobby;
	}

	public StringProperty nameProperty() {
		return this.name;
	}

	public ObjectProperty<Color> colorProperty() {
		return this.color;
	}

	public StringProperty ipIDProperty() {
		return this.ipID;
	}

	public StringProperty portIDProperty() {
		return this.portID;
	}

	public StringProperty lobbyIDProperty() {
		return this.lobbyID;
	}
	
	public StringProperty nameIDProperty() {
		return this.nameID;
	}
	
	public StringProperty colorIDProperty() {
		return this.colorID;
	}

	public BooleanProperty isConnectingProperty() {
		return this.isConnecting;
	}

}
