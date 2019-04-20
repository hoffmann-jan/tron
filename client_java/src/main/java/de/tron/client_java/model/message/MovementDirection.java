package de.tron.client_java.model.message;

public enum MovementDirection {

	UP(0),
	DOWN(1),
	LEFT(2),
	RIGHT(3),
	JUMP(4);
	
	private final int index;
	
	public static MovementDirection get(int index) {
		switch (index) {
			case 0:
				return MovementDirection.UP;
			case 1:
				return MovementDirection.DOWN;
			case 2:
				return MovementDirection.LEFT;
			case 3:
				return MovementDirection.RIGHT;
			case 4:
				return MovementDirection.JUMP;
			default:
				throw new IndexOutOfBoundsException("There is no movement direction for the index " + index);
		}
	}
	
	private MovementDirection(int index) {
		this.index = index;
	}

	public int getIndex() {
		return this.index;
	}
	
}
