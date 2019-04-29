package de.tron.client_java.network.message;

public enum MessageType {

	CONNECT(0),
	DISCONNECT(1),
	ADD(2),
	UPDATE(3),
	ACTION(4),
	DEAD(5),
	LOBBY(6),
	READY(7),
	START(8),
	RESULT(9);
	
	private final int index;
	
	public static MessageType get(int index) {
		switch (index) {
			case 0:
				return MessageType.CONNECT;
			case 1:
				return MessageType.DISCONNECT;
			case 2:
				return MessageType.ADD;
			case 3:
				return MessageType.UPDATE;
			case 4:
				return MessageType.ACTION;
			case 5:
				return MessageType.DEAD;
			case 6:
				return MessageType.LOBBY;
			case 7:
				return MessageType.READY;
			case 8:
				return MessageType.START;
			case 9:
				return MessageType.RESULT;
			default:
				throw new IndexOutOfBoundsException("There is no message type for the index " + index);
		}
	}
	
	private MessageType(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return this.index;
	}
	
}
