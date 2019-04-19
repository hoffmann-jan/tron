package de.tron.message.converter;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;

import org.junit.Test;

import de.tron.message.Coordinate;
import de.tron.message.Message;
import de.tron.message.MessageType;
import de.tron.message.converter.JsonMessageConverter;

public class JsonMessageConverterTest {

	private static String openJsonFile(String path) {
		try (FileReader input = new FileReader(path)) {
			BufferedReader reader = new BufferedReader(input);
			StringBuilder builder = new StringBuilder();
			reader.lines()
				.map(String::trim)
				.forEach(builder::append);
			return builder.toString();
		} catch (Exception e) {
			fail(e.getMessage());
			return "";
		}
	}
	
	@Test
	public void deserializeExampleOneTest() {
		String json = openJsonFile("example_messages\\example1.json");
		JsonMessageConverter converter = new JsonMessageConverter();
		Message result = converter.deserialize(json);
		
		assertEquals(-1, result.getId());
		assertNull(result.getMove());
		assertEquals(MessageType.UPDATE, result.getType());
		assertEquals(1, result.getUpdatedCoordinates().size());

		Coordinate coordinate = result.getUpdatedCoordinates().iterator().next();
		assertEquals(1, coordinate.getPlayerId());
		assertEquals(42, coordinate.getX());
		assertEquals(350, coordinate.getY());
	}
	
	@Test
	public void serializeExampleOneTest() {
		String json = openJsonFile("example_messages\\example1.json");
		JsonMessageConverter converter = new JsonMessageConverter();
		
		Message message = new Message();
		Coordinate coordinate = new Coordinate();
		message.setType(MessageType.UPDATE);
		message.getUpdatedCoordinates().add(coordinate);
		coordinate.setPlayerId(1);
		coordinate.setX(42);
		coordinate.setY(350);
		
		String serializationResult = converter.serialize(message);
		
		assertEquals(json, serializationResult);
	}
	
	@Test
	public void deserializeExampleTwoTest() {
		String json = openJsonFile("example_messages\\example2.json");
		JsonMessageConverter converter = new JsonMessageConverter();
		Message result = converter.deserialize(json);
		
		assertEquals(-1, result.getId());
		assertNull(result.getMove());
		assertEquals(MessageType.UPDATE, result.getType());
		assertEquals(4, result.getUpdatedCoordinates().size());
	}
	
}
