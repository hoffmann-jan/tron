package de.tron.client_java.network.message;

import java.util.ArrayList;
import java.util.List;

public class Message {
	
	int lobbyId = -1;
	int length = -1;
	
	private MessageType type;
	private Action action;
	
	private final List<Player> players = new ArrayList<>();

	public int getLobbyId() {
		return lobbyId;
	}

	public void setLobbyId(int lobbyId) {
		this.lobbyId = lobbyId;
	}
	
	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	public List<Player> getPlayers() {
		return players;
	}

}
