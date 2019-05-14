package de.tron.client_java.network.encryption;

import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.Scanner;

import com.google.gson.Gson;

public class SecurityHandler {
	
	private final RSAEncryption rsa;
	
	public SecurityHandler() throws GeneralSecurityException {
		this.rsa = new RSAEncryption();
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
			String publicKeyMessage = input.next();
			RSAPublicKeyData publicKey = converter.fromJson(publicKeyMessage, RSAPublicKeyData.class);
			this.rsa.setOtherPublicKey(publicKey);
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
		return this.rsa.encrypt(message);
	}
	
	/**
	 * Decodes the Base64 encoding and the RSA encoding with the private key of this client
	 * 
	 * @param Base64 and RSA encrypted message
	 * @return unencrypted message
	 */
	public String decrypt(String message) {
		return this.rsa.decrypt(message);
	}

}
