package de.tron.client_java.network.encryption;

import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.Scanner;

import com.google.gson.Gson;

import de.tron.client_java.network.encryption.asymmetric.RSAEncryption;
import de.tron.client_java.network.encryption.symmetric.AESEncryption;
import de.tron.client_java.network.encryption.symmetric.SymmetricHandshakeData;

public class SecurityHandler {
	
	private final RSAEncryption rsa;
	private final AESEncryption aes;
	
	public SecurityHandler() throws GeneralSecurityException {
		this.rsa = new RSAEncryption();
		this.aes = new AESEncryption();
	}
	/**
	 * Exchanges public keys
	 * 
	 * @param input Stream to receive public key
	 * @param output Stream to send public key
	 * @throws GeneralSecurityException
	 */
	public void doHandShake(Scanner input, PrintWriter output) throws GeneralSecurityException {
		Gson converter = new Gson();
		String out = converter.toJson(this.rsa.getPublicKey());
		output.println(out);
		// Waits until scanner has next or stream is closed
		if (input.hasNext()) {
			String initMessage = input.next();
			System.out.println(initMessage);
			initMessage = this.rsa.decrypt(initMessage);
			SymmetricHandshakeData data = converter.fromJson(initMessage, SymmetricHandshakeData.class);
			this.aes.initialize(data.getKey(), data.getIv());
		}
	}
	
	/**
	 * Encodes the message with the public key of the receiver and 
	 * additionally apply Base64 encoding
	 * 
	 * @param message unencrypted message
	 * @return Base64 and RSA encrypted message
	 */
	public String encrypt(String message) {
		return this.aes.encrypt(message);
	}
	
	/**
	 * Decodes the Base64 encoding and the RSA encoding with the private key of this client
	 * 
	 * @param Base64 and RSA encrypted message
	 * @return unencrypted message
	 */
	public String decrypt(String message) {
		return this.aes.decrypt(message);
	}

}
