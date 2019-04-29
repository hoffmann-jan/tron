package de.tron.client_java.model;

public class GameMessage {

	private final String status;
	
	private final Information information;
	
	public GameMessage(String status, Information information) {
		this.status = status;
		this.information = information;
	}
	
	public String getMessage() {
		return this.status;
	}

	public Information getInformation() {
		return this.information;
	}
	
}
