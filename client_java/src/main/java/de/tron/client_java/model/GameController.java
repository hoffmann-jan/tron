package de.tron.client_java.model;

import java.io.IOException;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.SubmissionPublisher;

import de.tron.client_java.model.data.ConnectionData;
import de.tron.client_java.model.data.GameMessage;
import de.tron.client_java.model.data.Position;
import de.tron.client_java.network.NetworkController;
import de.tron.client_java.network.message.Action;
import de.tron.client_java.network.message.Message;
import de.tron.client_java.network.message.MessageType;
import de.tron.client_java.network.message.Player;

/**
 * Class which handles received messages and creates messages of the client
 * 
 * @author emaeu
 *
 */
public class GameController implements Processor<Message, GameMessage> {
	
	private static final Logger LOGGER = Logger.getLogger("root");
	
	public static final int FIELD_SIZE = 500;
	public static final int PLAYER_SIZE = 10;
	public static final int PLAYER_JUMPING_SIZE = 12;
	
	private final SubmissionPublisher<GameMessage> publisher = new SubmissionPublisher<>(ForkJoinPool.commonPool(), 4);
	private Subscription subscription;
	
	private final GameState state = new GameState();
	
	private final NetworkController network = new NetworkController();

	public GameController() {
		this.network.subscribe(this);
	}
	
	@Override
	public void onSubscribe(Subscription subscription) {
		this.subscription = subscription;
		this.subscription.request(1);
	}

	@Override
	public void onNext(Message message) {
		GameController.LOGGER.log(Level.INFO, "Received message of type {0} in game controller", message.getType());	
		GameMessage information = this.state.applyMessage(message);
		this.publisher.submit(information);
		this.subscription.request(1);
	}

	@Override
	public void onError(Throwable throwable) {
		this.publisher.closeExceptionally(throwable);
	}

	@Override
	public void onComplete() {
		this.publisher.close();		
	}

	@Override
	public void subscribe(Subscriber<? super GameMessage> subscriber) {
		this.publisher.subscribe(subscriber);		
	}
	
	public void connect(ConnectionData data) throws IOException {
		// create local player to recognize which player belongs to this clien
		Player localPlayer = new Player();
		localPlayer.setColor(data.getColor());
		localPlayer.setName(data.getName());
		
		Message connectMessage = createConnectMessage(data, localPlayer);
		Message connectResponse = this.network.connect(data.getIp(), data.getPort(), connectMessage);
		this.state.setLocalPlayer(mergeLocalPlayerInformation(connectResponse, localPlayer));
	}

	private Player mergeLocalPlayerInformation(Message connectResponse, Player localPlayer) {
		Player responsePlayer = connectResponse.getPlayers().get(0);
		if (responsePlayer != null) {
			localPlayer.setId(responsePlayer.getId());
			localPlayer.setPosition(responsePlayer.getPosition());
		}
		return localPlayer;
	}

	private Message createConnectMessage(ConnectionData data, Player localPlayer) {
		Message message = new Message();
		message.setType(MessageType.CONNECT);
		message.setLobbyId(data.getLobbyNumber());
		message.getPlayers().add(localPlayer);
		return message;
	}
	
	/**
	 * Disconnect from server
	 */
	public void disconnect() {
		Player localPlayer = this.state.getLocalPlayer();
		if (localPlayer != null && localPlayer.getId() != -1) {
			Player player = new Player();
			player.setId(localPlayer.getId());
			Message message = new Message();
			message.setType(MessageType.DISCONNECT);
			message.getPlayers().add(player);
			this.network.sendMessage(message);
		}
		this.network.close();
	}
	
	/**
	 * Send ready message to the server 
	 */
	public void readyToPlay() {
		Player player = new Player();
		player.setId(this.state.getLocalPlayer().getId());
		Message message = new Message();
		message.setType(MessageType.READY);
		message.getPlayers().add(player);
		this.network.sendMessage(message);
	}
	
	/**
	 * Send server action of this player
	 * 
	 * @param key keyboard input
	 */
	public void changeDirection(char key) {
		if (this.state.getLocalPlayer() == null) {
			return;
		}
		key = Character.toLowerCase(key);
		Action action = null;
		switch (key) {
			case 'w':
				action = Action.UP;
				break;
			case 'd':
				action = Action.RIGHT;
				break;
			case 's':
				action = Action.DOWN;
				break;
			case 'a':
				action = Action.LEFT;
				break;
			case ' ':
				action = Action.JUMP;
				break;
			default:
				return;	
		}
		this.network.sendMessage(createActionMessage(action));
	}

	private Message createActionMessage(Action action) {
		Player player = new Player();
		player.setId(this.state.getLocalPlayer().getId());
		Message message = new Message();
		message.setType(MessageType.ACTION);
		message.setAction(action);
		message.getPlayers().add(player);
		return message;
	}

	public Set<Player> getOriginalPlayers() {
		return this.state.getOriginalPlayers();
	}

	public Map<Player, Queue<Position>> getPlayerModels() {
		return this.state.getPlayerModels();
	}

	public Player getWinner() {
		return this.state.getWinner();
	}

	public Set<Player> getUpdatedPlayers() {
		return this.state.getUpdatedPlayers();
	}
}
