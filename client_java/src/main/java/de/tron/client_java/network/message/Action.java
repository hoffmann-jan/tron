package de.tron.client_java.network.message;

public enum Action {

	UP(0),
	DOWN(1),
	LEFT(2),
	RIGHT(3),
	JUMP(4);
	
	private final int index;
	
	public static Action get(int index) {
		switch (index) {
			case 0:
				return Action.UP;
			case 1:
				return Action.DOWN;
			case 2:
				return Action.LEFT;
			case 3:
				return Action.RIGHT;
			case 4:
				return Action.JUMP;
			default:
				throw new IndexOutOfBoundsException("There is no movement direction for the index " + index);
		}
	}
	
	private Action(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
	}
	
}
