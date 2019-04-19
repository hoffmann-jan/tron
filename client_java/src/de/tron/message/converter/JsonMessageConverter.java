package de.tron.message.converter;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.tron.message.Coordinate;
import de.tron.message.Message;
import de.tron.message.MessageType;
import de.tron.message.MovementDirection;

public class JsonMessageConverter implements JsonSerializer<Message>, JsonDeserializer<Message> {

	private static final String ID = "id";
	private static final String TYPE = "type";
	private static final String MOVE = "move";
	private static final String COORDINATES = "coordinates";
	private static final String COORDINATE_ID = "id";
	private static final String COORDINATE_X = "x";
	private static final String COORDINATE_Y = "y";
	
	public String serialize(Message message) {
		return serialize(message, null, null).toString();
	}
	
	@Override
	public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject root = new JsonObject();
		addIdIfNotInit(src.getId(), root);
		addTypeIfNotNull(src.getType(), root);
		addMoveIfNotNull(src.getMove(), root);
		addAllCoordinates(src, root);
		return root;
	}
	
	private void addIdIfNotInit(int id, JsonObject root) {
		if (id != -1) {
			root.addProperty(ID, id);
		}
	}

	private void addTypeIfNotNull(MessageType type, JsonObject root) {
		if (type != null) {
			root.addProperty(TYPE, type.getIndex());
		}
	}
	
	private void addMoveIfNotNull(MovementDirection move, JsonObject root) {
		if (move != null) {
			root.addProperty(TYPE, move.getIndex());
		}
	}
	
	private void addAllCoordinates(Message src, JsonObject root) {
		JsonArray coordinates = new JsonArray();
		for (Coordinate coordinate : src.getUpdatedCoordinates()) {
			JsonObject jsonCoordinate = new JsonObject();
			jsonCoordinate.addProperty(COORDINATE_ID, coordinate.getPlayerId());
			jsonCoordinate.addProperty(COORDINATE_X, coordinate.getX());
			jsonCoordinate.addProperty(COORDINATE_Y, coordinate.getY());
			coordinates.add(jsonCoordinate);
		}
		root.add(COORDINATES, coordinates);
	}
	
	public Message deserialize(String jsonString) {
		JsonElement element = new JsonParser().parse(jsonString);
		return deserialize(element, null, null);
	}

	@Override
	public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
		Message message = new Message();
		if (json.isJsonObject()) {
			JsonObject jsonMessage = json.getAsJsonObject();
			int id = getAttributeAsInt(jsonMessage, ID);
			int move = getAttributeAsInt(jsonMessage, MOVE);
			int type = getAttributeAsInt(jsonMessage, TYPE);
			
			message.setId(id);
			message.setMove(mapMove(move));
			message.setType(mapType(type));
			message.getUpdatedCoordinates().addAll(getCoordinateSet(jsonMessage));
		}
		return message;
	}
	
	private int getAttributeAsInt(JsonObject object, String name) {
		JsonElement attribute = object.get(name);
		if (attribute != null) {
			return attribute.getAsInt();
		} else {
			return -1;
		}
	}

	private MovementDirection mapMove(int index) {
		return index == -1
			? null
			: MovementDirection.get(index);
	}
	
	private MessageType mapType(int index) {
		return index == -1
			? null
			: MessageType.get(index);
	}
	
	private Set<Coordinate> getCoordinateSet(JsonObject jsonMessage) {
		Set<Coordinate> coordinates = new HashSet<>();
		JsonElement coordinatesElement = jsonMessage.get(COORDINATES);
		if (coordinatesElement.isJsonArray()) {
			JsonArray coordinatesArray = coordinatesElement.getAsJsonArray();
			coordinatesArray.forEach(e -> coordinates.add(mapCoordinate(e)));
			return coordinates;
		} else {
			return Collections.emptySet();
		}
	}

	private Coordinate mapCoordinate(JsonElement coordinateElement) {
		Coordinate coordinate = new Coordinate();
		JsonObject coordinateObject = coordinateElement.getAsJsonObject();
		coordinate.setPlayerId(getAttributeAsInt(coordinateObject, COORDINATE_ID));
		coordinate.setX(getAttributeAsInt(coordinateObject, COORDINATE_X));
		coordinate.setY(getAttributeAsInt(coordinateObject, COORDINATE_Y));
		return coordinate;
	}
}
