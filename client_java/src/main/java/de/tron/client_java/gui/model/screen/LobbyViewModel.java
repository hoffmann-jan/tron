package de.tron.client_java.gui.model.screen;

import de.tron.client_java.model.GameController;
import de.tron.client_java.network.message.Player;
import javafx.beans.property.MapProperty;
import javafx.beans.property.SimpleMapProperty;
import javafx.collections.FXCollections;
import javafx.scene.paint.Color;

public class LobbyViewModel {
	
	private final GameController controller;
	
	private final MapProperty<String, Color> players = new SimpleMapProperty<>(FXCollections.observableHashMap());
	
	public LobbyViewModel(GameController controller) {
		this.controller = controller;
		refresh();
	}
	
	public void readyToPlay() {
		this.controller.readyToPlay();		
	}
	
	/**
	 * Refresh all players in the lobby 
	 */
	public void refresh() {
		this.players.clear();
		for (Player player : this.controller.getOriginalPlayers()) {
			String name = String.format("%s (%d)", player.getName(), player.getId());
			if (!players.containsKey(name)) {
				Color color = Color.web(String.format("0x%06X", player.getColor()));
				this.players.put(name, color);
			}
		}
	}

	public MapProperty<String, Color> playersProperty() {
		return this.players;
	}

}
