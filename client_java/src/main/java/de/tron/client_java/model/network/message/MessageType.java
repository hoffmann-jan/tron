package de.tron.client_java.model.network.message;

public enum MessageType {

	CONNECT(0),
	DISCONNECT(1),
	ADD(2),
	MOVE(3),
	UPDATE(4),
	DEAD(5);
	
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
				return MessageType.MOVE;
			case 4:
				return MessageType.UPDATE;
			case 5:
				return MessageType.DEAD;
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
