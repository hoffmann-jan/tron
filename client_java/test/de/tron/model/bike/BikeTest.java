package de.tron.model.bike;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.Test;

import de.tron.model.bike.Bike;
import de.tron.model.bike.Coordinate;

public class BikeTest {

	@Test
	public void updateCoordinatesTestOne() {
		int playerId = 1;
		int x = 10;
		int y = 20;
		
		Bike bike = new Bike(playerId);
		Coordinate coordinate = new Coordinate();
		coordinate.setPlayerId(playerId);
		coordinate.setX(x);
		coordinate.setY(y);
		
		bike.updateCoordinates(coordinate);
		
		assertEquals(x, bike.getX());
		assertEquals(y, bike.getY());
	}
	
	@Test
	public void updateCoordinatesTestTwo() {
		int playerId = 2;
		int x = 10;
		int y = 20;
		
		Bike bike = new Bike(1);
		Coordinate coordinate = new Coordinate();
		coordinate.setPlayerId(playerId);
		coordinate.setX(x);
		coordinate.setY(y);
		
		assertThrows(IllegalArgumentException.class, () -> bike.updateCoordinates(coordinate));
		assertEquals(0, bike.getX());
		assertEquals(0, bike.getY());
	}
	
}
