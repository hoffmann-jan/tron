package de.tron.client_java.model;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Flow.Processor;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.TimeUnit;

import de.tron.client_java.network.NetworkController;
import de.tron.client_java.network.message.Action;
import de.tron.client_java.network.message.Message;
import de.tron.client_java.network.message.MessageType;
import de.tron.client_java.network.message.Player;

public class GameController implements Processor<Message, GameMessage> {

	public static final int FIELD_SIZE = 500;
	public static final int PLAYER_SIZE = 10;
	private static final long OFFER_TIMEOUT = 10;
	
	private final SubmissionPublisher<GameMessage> publisher = new SubmissionPublisher<>(ForkJoinPool.commonPool(), 4);
	private Subscription subscription;
	
	private Player localPlayer;
	
	private final Set<Player> updatedPlayers = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private final Map<Player, Queue<Position>> playerModels = new ConcurrentHashMap<>();
	
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
		GameMessage information = parseMessage(message);
		publishMessage(information);
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
	
	private void publishMessage(GameMessage message) {
		this.publisher.offer(message, GameController.OFFER_TIMEOUT, TimeUnit.MILLISECONDS, null);		
	}
	
	public void connect(ConnectionData data) throws IOException {
		this.network.configureConnection(data.getIp(), data.getPort());
		this.localPlayer = new Player();
		this.localPlayer.setColor(data.getColor());
		this.localPlayer.setName(data.getName());
		Message message = new Message();
		message.setType(MessageType.CONNECT);
		message.setLobbyId(data.getLobbyNumber());
		message.getPlayers().add(this.localPlayer);
		this.network.sendMessage(message);
	}
	
	public void disconnect() {
		if (localPlayer != null) {
			Player player = new Player();
			player.setId(this.localPlayer.getId());
			Message message = new Message();
			message.setType(MessageType.DISCONNECT);
			message.getPlayers().add(player);
			this.network.sendMessage(message);
		}
	}
	
	public void readyToPlay() {
		Player player = new Player();
		player.setId(this.localPlayer.getId());
		Message message = new Message();
		message.setType(MessageType.READY);
		message.getPlayers().add(player);
		this.network.sendMessage(message);
	}
	
	public void changeDirection(char key) {
		if (localPlayer == null) {
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
		Player player = new Player();
		player.setId(this.localPlayer.getId());
		Message message = new Message();
		message.setType(MessageType.ACTION);
		message.setAction(action);
		message.getPlayers().add(player);
		this.network.sendMessage(message);
	}
	
	private GameMessage parseMessage(Message message) {
		GameMessage information = null;
		switch (message.getType()) {
			case CONNECT:
				System.out.println("connect");
				information = getConnectMessage();
				getInformationOfLocalPlayer(message);
				break;	
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
				information = getResultMessage();
				break;
			default:
				// Enum values READY and ACTION are not used because the client should never receive messages of this types
		}		
		return information;
	}

	private void getInformationOfLocalPlayer(Message message) {
		Player messagPlayer = message.getPlayers().get(0);
		if (messagPlayer != null) {
			this.localPlayer.setId(messagPlayer.getId());
		}
		this.updatedPlayers.add(localPlayer);
		this.playerModels.put(this.localPlayer, new ConcurrentLinkedQueue<>());
	}
	
	private void updatePlayers(Message message) {
		this.updatedPlayers.clear();
		this.updatedPlayers.addAll(message.getPlayers());
	}
	
	private void addOriginalPlayers(Message message) {
		message.getPlayers().forEach(p -> this.playerModels.put(p, new ConcurrentLinkedQueue<>()));
	}
	
	private void removeOriginalPlayer(Message message) {
		System.out.println(this.playerModels.size());
		message.getPlayers().forEach(this.playerModels::remove);		
		System.out.println(this.playerModels.size());
	}

	private void updateTails(Message message) {
		int lenght = message.getLength();
		for (Player player : message.getPlayers()) {
			Queue<Position> playerTail = this.playerModels.get(player);
			Position position = new Position();
			position.setX(player.getPosition().getX() + PLAYER_SIZE / 2);
			position.setY(player.getPosition().getY() + PLAYER_SIZE / 2);
			playerTail.add(position);
			
			while (playerTail.size() > lenght) {
				playerTail.poll();
			}
		}		
	}
	
	private GameMessage getConnectMessage() {
		return new GameMessage("Successfully connected. Receiving lobby information", Information.STATUS);
	}

	private GameMessage getUpdateMessage() {
		return new GameMessage(null, Information.UPDATE);
	}
	
	private GameMessage getDisconnectedMessage(Message message) {
		Player disconnectedPlayer = message.getPlayers().get(0);
		if (disconnectedPlayer.getId() != this.localPlayer.getId()) {
			String status = String.format("Player %s (%d) disconnected", 
					disconnectedPlayer.getName(), disconnectedPlayer.getId());
			return new GameMessage(status, Information.PLAYER_CHANGE);
		} else {
			return new GameMessage("Couldn't join lobby", Information.REFUSED);
		}
	}

	private GameMessage getDeadMessage(Message message) {
		Player disconnectedPlayer = message.getPlayers().get(0);
		String status;
		if (this.localPlayer.equals(disconnectedPlayer)) {
			status = String.format("Player %s (%d) died", 
				disconnectedPlayer.getName(), disconnectedPlayer.getId());			
		} else {
			status = "You died";
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
	
	private GameMessage getResultMessage() {
		return new GameMessage(null, Information.RESULT);
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
}
