package de.tron.client_java.network.encryption.symmetric;

public class SymmetricHandshakeData {

	private byte[] key;
	private byte[] iv;

	public byte[] getKey() {
		return key;
	}

	public void setKey(byte[] key) {
		this.key = key;
	}

	public byte[] getIv() {
		return iv;
	}

	public void setIv(byte[] iv) {
		this.iv = iv;
	}

}
