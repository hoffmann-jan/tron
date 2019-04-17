package de.tron.message;

import java.util.Set;

public class Message {

	private int id = -1;
	
	private MessageType type;
	private MovementDirection direction;
	
	private Set<Coordinates> updatedCoordinates;

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

	public MovementDirection getDirection() {
		return direction;
	}

	public void setDirection(MovementDirection direction) {
		this.direction = direction;
	}

	public Set<Coordinates> getUpdatedCoordinates() {
		return updatedCoordinates;
	}

	public void setUpdatedCoordinates(Set<Coordinates> updatedCoordinates) {
		this.updatedCoordinates = updatedCoordinates;
	}
	
	
}
