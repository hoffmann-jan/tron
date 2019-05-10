package de.tron.client_java.network.encription;

import java.security.GeneralSecurityException;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class SymmetricEncryption {
	
	public static final String ALGORITHM = "AES";

	private final SecretKeySpec key;
	
	public SymmetricEncryption(byte[] key) {
		this.key = new SecretKeySpec(key, SymmetricEncryption.ALGORITHM);		
	}
	
	public String encrypt(String message) {
		byte[] text = message.getBytes();
		try {
			text = encryptBytes(text);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		Base64.Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(text);
	}
	
	public String decrypt(String message) {
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] text = decoder.decode(message);
		
		try {
			text = decryptBytes(text);
		} catch (GeneralSecurityException e) {
			e.printStackTrace();
		}
		return new String(text);
	}

	private byte[] encryptBytes(byte[] text) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(SymmetricEncryption.ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, this.key);
		return cipher.doFinal(text);
	}
	
	private byte[] decryptBytes(byte[] text) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(SymmetricEncryption.ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, this.key);
		return cipher.doFinal(text);
	}

}
