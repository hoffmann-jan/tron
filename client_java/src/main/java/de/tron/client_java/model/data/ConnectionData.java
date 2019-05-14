package de.tron.client_java.model.data;

public class ConnectionData {

	private int port;
	private int lobbyNumber;
	private int color;

	private String ip;
	private String name;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getLobbyNumber() {
		return lobbyNumber;
	}

	public void setLobbyNumber(int lobbyNumber) {
		this.lobbyNumber = lobbyNumber;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
