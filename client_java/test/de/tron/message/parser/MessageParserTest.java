package de.tron.message.parser;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.FileReader;

import org.junit.Test;

import de.tron.message.Coordinates;
import de.tron.message.Message;
import de.tron.message.MessageType;

public class MessageParserTest {

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
	public void parseExampleOneTest() {
		String json = openJsonFile("example_messages\\example1.json");
		Message result = MessageParser.parse(json);
		
		assertEquals(-1, result.getId());
		assertNull(result.getDirection());
		assertEquals(MessageType.UPDATE, result.getType());
		assertEquals(1, result.getUpdatedCoordinates().size());
		
		Coordinates coordinates = result.getUpdatedCoordinates().iterator().next();
		assertEquals(1, coordinates.getPlayerId());
		assertEquals(42, coordinates.getX());
		assertEquals(350, coordinates.getY());
		
	}
	
}
