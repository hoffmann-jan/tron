package de.tron.client_java.gui.model.data;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.paint.Color;

public class GuiMessage {

	private boolean connectionAccepted = false;
	private boolean updatesExist = false;
	
	private final Map<Color, Integer[]> playerUpdates = new HashMap<>();

	public boolean isConnectionAccepted() {
		return connectionAccepted;
	}

	public void setConnectionAccepted(boolean connectionAccepted) {
		this.connectionAccepted = connectionAccepted;
	}

	public boolean updatesExist() {
		return updatesExist;
	}

	public void setUpdatesExist(boolean updatesExist) {
		this.updatesExist = updatesExist;
	}

	public Map<Color, Integer[]> getPlayerUpdates() {
		return playerUpdates;
	}
	
	public void putPlayerUpdate(Color color, Integer[] update) {
		this.playerUpdates.put(color, update);
	}
	
}
