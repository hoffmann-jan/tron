package de.tron.client_java.model.data;

import de.tron.client_java.model.network.message.Coordinate;

public class Bike {

	private final int playerId;
	
	private boolean isAlive = true;
	
	private int x;
	private int y;
	
	public Bike(int playerId) {
		this.playerId = playerId;
	}

	public void updateCoordinates(Coordinate coordinate) {		
		if (getPlayerId() == coordinate.getPlayerId()) {
			setX(coordinate.getX());
			setY(coordinate.getY());
		} else {
			throw new IllegalArgumentException(
				String.format("The player id %d of the coordinate doesn't match the bikes player id %d",
				coordinate.getPlayerId(), this.playerId));
		}
	}

	public void die() {
		setAlive(false);		
	}
	
	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}
	
}
