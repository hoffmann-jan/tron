package de.tron.client_java.network.message;

import java.util.ArrayList;
import java.util.List;

public class Message {

	private int id = -1;
	
	private MessageType type;
	private MovementDirection move;
	
	private final List<Coordinate> updatedCoordinates = new ArrayList<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public MessageType getType() {
		return type;
	}

	public void setType(MessageType type) {
		this.type = type;
	}

	public MovementDirection getMove() {
		return move;
	}

	public void setMove(MovementDirection move) {
		this.move = move;
	}

	public List<Coordinate> getUpdatedCoordinates() {
		return updatedCoordinates;
	}

}
