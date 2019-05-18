package de.tron.client_java.model;

import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.tron.client_java.model.data.GameMessage;
import de.tron.client_java.model.data.Information;
import de.tron.client_java.model.data.Position;
import de.tron.client_java.network.message.Message;
import de.tron.client_java.network.message.Player;

/**
 * An object of this class contains every information that is necessary for the client 
 * to display the game. Applying messages will update the data structure
 * 
 * @author emaeu
 *
 */
public class GameState {

	private Player localPlayer;
	private Player winner;
	
	private final Set<Player> updatedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final Map<Player, Queue<Position>> playerModels = new ConcurrentHashMap<>();
	
	/**
	 * Adjust the state of the game to the received message and create GameMessage to inform what happened
	 * 
	 * @param message
	 * @return
	 */
	public GameMessage applyMessage(Message message) {
		GameMessage information = null;
		switch (message.getType()) {
			case UPDATE:
				information = getUpdateMessage();
				// addPlayers updates the players because the old ones are replaced in the set
				updatePlayers(message);
				updateTails(message);
				break;
			case DISCONNECT:
				information = getDisconnectedMessage(message);
				removeOriginalPlayer(message);
				break;
			case DEAD:
				information = getDeadMessage(message);
				break;
			case ADD:
				information = getAddMessage(message);
				addOriginalPlayers(message);
				break;
			case LOBBY:
				information = getLobbyMessage();
				addOriginalPlayers(message);
				break;
			case START:
				information = getStartMessage();
				break;
			case RESULT:
				information = getResultMessage(message);
				break;
			default:
				// Enum values READY and ACTION are not used because the client should never receive messages of this types
		}		
		return information;
	}
	
	private void updatePlayers(Message message) {
		this.updatedPlayers.clear();
		this.updatedPlayers.addAll(message.getPlayers());
	}
	
	private void addOriginalPlayers(Message message) {
		message.getPlayers().forEach(p -> this.playerModels.put(p, new ConcurrentLinkedQueue<>()));
	}
	
	private void removeOriginalPlayer(Message message) {
		message.getPlayers().forEach(this.playerModels::remove);
	}

	private void updateTails(Message message) {
		int lenght = message.getLength();
		for (Player player : message.getPlayers()) {
			int playerSize = player.getPosition().isJumping() 
					? GameController.PLAYER_JUMPING_SIZE 
					: GameController.PLAYER_SIZE;
			Queue<Position> playerTail = this.playerModels.get(player);
			Position position = new Position();
			position.setX(player.getPosition().getX() + playerSize / 2);
			position.setY(player.getPosition().getY() + playerSize / 2);
			playerTail.add(position);
			
			while (playerTail.size() > lenght) {
				playerTail.poll();
			}
		}		
	}

	private GameMessage getUpdateMessage() {
		return new GameMessage(null, Information.UPDATE);
	}
	
	private GameMessage getDisconnectedMessage(Message message) {
		Player disconnectedPlayer = message.getPlayers().get(0);
		String status = String.format("Player %s (%d) disconnected", 
				disconnectedPlayer.getName(), disconnectedPlayer.getId());
		return new GameMessage(status, Information.PLAYER_CHANGE);
	}

	private GameMessage getDeadMessage(Message message) {
		Player deadPlayer = message.getPlayers().get(0);
		String status;
		if (this.localPlayer.equals(deadPlayer)) {
			status = "You died";
		} else {
			status = String.format("Player %s (%d) died", 
					deadPlayer.getName(), deadPlayer.getId());			
		}
		return new GameMessage(status, Information.STATUS);
	}

	private GameMessage getAddMessage(Message message) {
		Player joinedPlayer = message.getPlayers().get(0);
		String status = String.format("Player %s (%d) joined the game", 
				joinedPlayer.getName(), joinedPlayer.getId());
		return new GameMessage(status, Information.PLAYER_CHANGE);
	}
	
	private GameMessage getLobbyMessage() {
		return new GameMessage(null, Information.LOBBY);
	}
	
	private GameMessage getStartMessage() {
		return new GameMessage("The game will start", Information.START);
	}
	
	private GameMessage getResultMessage(Message message) {
		Player messageWinner = message.getPlayers().get(0);
		this.winner = getOriginalPlayers()
				.stream()
				.filter(p -> p.getId() == messageWinner.getId())
				.findFirst()
				.orElse(messageWinner);
		return new GameMessage(null, Information.RESULT);
	}
	
	public Player getLocalPlayer() {
		return this.localPlayer;
	}
	
	public void setLocalPlayer(Player localPlayer) {
		this.localPlayer = localPlayer;
		this.updatedPlayers.add(localPlayer);
		this.playerModels.put(this.localPlayer, new ConcurrentLinkedQueue<>());
	}
	
	public Set<Player> getOriginalPlayers() {
		return this.playerModels.keySet();
	}
	
	public Set<Player> getUpdatedPlayers() {
		return this.updatedPlayers;
	}

	public Map<Player, Queue<Position>> getPlayerModels() {
		return this.playerModels;
	}

	public Player getWinner() {
		return this.winner;
	}
	
}
