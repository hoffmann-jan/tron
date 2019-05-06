package de.tron.client_java.network.message.converter;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import de.tron.client_java.network.message.Position;
import de.tron.client_java.network.message.Message;
import de.tron.client_java.network.message.MessageType;
import de.tron.client_java.network.message.Action;
import de.tron.client_java.network.message.Player;

public class JsonMessageConverter implements JsonSerializer<Message>, JsonDeserializer<Message> {

	private static final String LENGTH = "Length";
	private static final String LOBBY_ID = "LobbyId";
	private static final String PACKAGE_NUMBER = "PackageNumber";
	private static final String TYPE = "Type";
	private static final String MOVE = "Action";
	private static final String PLAYERS = "Players";
	private static final String PLAYER_ID = "Id";
	private static final String PLAYER_POSITION = "Position";
	private static final String PLAYER_NAME = "Name";
	private static final String PLAYER_COLOR = "Color";	
	private static final String POSITION_X = "X";
	private static final String POSITION_Y = "Y";
	private static final String POSITION_JUMPING = "Jumping";
	
	
	public String serialize(Message message) {
		return serialize(message, null, null).toString();
	}
	
	@Override
	public JsonElement serialize(Message src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject root = new JsonObject();
		addNumberIfNotInit(src.getLength(), root, LENGTH);
		addNumberIfNotInit(src.getLobbyId(), root, LOBBY_ID);
		addNumberIfNotInit(src.getPackageNumber(), root, PACKAGE_NUMBER);
		addTypeIfNotNull(src.getType(), root);
		addMoveIfNotNull(src.getAction(), root);
		addAllPlayers(src, root);
		return root;
	}
	
	private void addNumberIfNotInit(long value, JsonObject json, String property) {
		if (value != -1) {
			json.addProperty(property, value);
		}
	}

	private void addTypeIfNotNull(MessageType value, JsonObject json) {
		if (value != null) {
			json.addProperty(TYPE, value.getIndex());
		}
	}
	
	private void addMoveIfNotNull(Action value, JsonObject json) {
		if (value != null) {
			json.addProperty(MOVE, value.getIndex());
		}
	}
	
	private void addObjectIfNotNull(String value, JsonObject json, String property) {
		if (value != null) {
			json.addProperty(property, value);
		}
	}
	
	private void addPositionIfNotNull(Position value, JsonObject json) {
		if (value != null) {
			JsonObject jsonPosition = new JsonObject();
			addNumberIfNotInit(value.getX(), jsonPosition, POSITION_X);
			addNumberIfNotInit(value.getY(), jsonPosition, POSITION_Y);
			jsonPosition.addProperty(POSITION_JUMPING, value.isJumping());
			json.add(PLAYER_POSITION, jsonPosition);
		}
	}
	
	private void addAllPlayers(Message src, JsonObject root) {		
		JsonArray players = new JsonArray();
		for (Player player : src.getPlayers()) {
			JsonObject jsonPlayer = new JsonObject();
			addNumberIfNotInit(player.getId(), jsonPlayer, PLAYER_ID);
			addNumberIfNotInit(player.getColor(), jsonPlayer, PLAYER_COLOR);
			addObjectIfNotNull(player.getName(), jsonPlayer, PLAYER_NAME);
			addPositionIfNotNull(player.getPosition(), jsonPlayer);
			players.add(jsonPlayer);
		}
		if (players.size() > 0) {
			root.add(PLAYERS, players);
		}
	}
	
	public Message deserialize(String jsonString) {
		jsonString = jsonString.trim();
		JsonElement element = new JsonParser().parse(jsonString);
		return deserialize(element, null, null);

	}

	@Override
	public Message deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
		Message message = new Message();
		if (json.isJsonObject()) {
			JsonObject jsonMessage = json.getAsJsonObject();

			int move = getAttributeAsInt(jsonMessage, MOVE);
			int type = getAttributeAsInt(jsonMessage, TYPE);
			
			message.setLobbyId(getAttributeAsInt(jsonMessage, LOBBY_ID));
			message.setLength(getAttributeAsInt(jsonMessage, LENGTH));
			message.setPackageNumber(getAttributeAsLong(jsonMessage, PACKAGE_NUMBER));
			message.setAction(mapMove(move));
			message.setType(mapType(type));
			message.getPlayers().addAll(getAttributeAsPlayerSet(jsonMessage, PLAYERS));
		}
		return message;
	}
	
	private Action mapMove(int index) {
		return index == -1
			? null
			: Action.get(index);
	}
	
	private MessageType mapType(int index) {
		return index == -1
			? null
			: MessageType.get(index);
	}
	
	private int getAttributeAsInt(JsonObject object, String property) {
		JsonElement attribute = object.get(property);
		if (attribute != null) {
			return attribute.getAsInt();
		} else {
			return -1;
		}
	}
	
	private long getAttributeAsLong(JsonObject object, String property) {
		JsonElement attribute = object.get(property);
		if (attribute != null) {
			return attribute.getAsLong();
		} else {
			return -1;
		}
	}
	
	private boolean getAttributeAsBoolean(JsonObject object, String property) {
		JsonElement attribute = object.get(property);
		if (attribute != null) {
			return attribute.getAsBoolean();
		} else {
			return false;
		}
	}
	
	private String getAttributeAsString(JsonObject object, String property) {
		JsonElement attribute = object.get(property);
		if (attribute != null) {
			return attribute.getAsString();
		} else {
			return null;
		}
	}
	
	private Set<Player> getAttributeAsPlayerSet(JsonObject object, String property) {
		JsonElement playersElement = object.get(property);
		if (playersElement != null && playersElement.isJsonArray()) {
			Set<Player> players = new HashSet<>();
			JsonArray playersArray = playersElement.getAsJsonArray();
			playersArray.forEach(e -> players.add(mapPlayer(e)));
			return players;
		} else {
			return Collections.emptySet();
		}
	}
	
	private Position getAttributeAsPosition(JsonObject object, String property) {
		JsonElement attribute = object.get(property);
		if (attribute != null && attribute.isJsonObject()) {
			JsonObject positionObject = attribute.getAsJsonObject();
			Position position = new Position();
			position.setX(getAttributeAsInt(positionObject, POSITION_X));
			position.setY(getAttributeAsInt(positionObject, POSITION_Y));
			position.setJumping(getAttributeAsBoolean(positionObject, POSITION_JUMPING));
			return position;
		} else {
			return null;
		}
	}

	private Player mapPlayer(JsonElement coordinateElement) {
		Player player = new Player();
		JsonObject playerObject = coordinateElement.getAsJsonObject();
		player.setId(getAttributeAsInt(playerObject, PLAYER_ID));
		player.setName(getAttributeAsString(playerObject, PLAYER_NAME));
		player.setColor(getAttributeAsInt(playerObject, PLAYER_COLOR));
		player.setPosition(getAttributeAsPosition(playerObject, PLAYER_POSITION));
		return player;
	}
}
