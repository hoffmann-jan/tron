package de.tron.client_java.network.encription;

import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.util.Scanner;

import com.google.gson.Gson;

public class SecurityHandler {
	
	private final RSAEncryption rsa;
	
	public SecurityHandler() throws GeneralSecurityException {
		this.rsa = new RSAEncryption();
	}
	
	public void doHandShake(Scanner input, PrintWriter output) throws GeneralSecurityException {
		Gson converter = new Gson();
		String out = converter.toJson(this.rsa.getPublicKey());
		System.out.println(out);
		output.println(out);
		if (input.hasNext()) {
			String publicKeyMessage = input.next();
			System.out.println(publicKeyMessage);
			RSAPublicKeyData publicKey = converter.fromJson(publicKeyMessage, RSAPublicKeyData.class);
			this.rsa.setOtherPublicKey(publicKey);
		}
	}
	
	public String encrypt(String message) {
		return this.rsa.encrypt(message);
	}
	
	public String decrypt(String message) {
		return this.rsa.decrypt(message);
	}

}
