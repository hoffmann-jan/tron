package de.tron.client_java.network.encryption.symmetric;

import java.security.GeneralSecurityException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryption {

	private static final Logger LOGGER = Logger.getLogger("root");
	
	public static final String ALGORITHM = "AES/CBC/PKCS5PADDING";

	private SecretKeySpec key;
	private IvParameterSpec iv;

	/**
	 * Sets the secret key and initialize vector for encoding and decoding
	 * 
	 * @param key
	 * @param iv
	 */
	public void initialize(byte[] key, byte[] iv) {
		this.iv = new IvParameterSpec(iv);
		this.key = new SecretKeySpec(key, "AES");
	}
	
	/**
	 * Encodes the message with the secret key and the initialize vector and 
	 * applies an base64 encoding
	 * 
	 * @param unencrypted message
	 * @return encrypted message
	 */
	public String encrypt(String message) {
		byte[] text = message.getBytes();
		try {
			text = encryptBytes(text);
		} catch (GeneralSecurityException e) {
			AESEncryption.LOGGER.log(Level.WARNING, "Failed to encrypt message", e);
		}
		Base64.Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(text);
	}

	/**
	 * Decodes the Base64 encoding and the AES encoding with the secret key
	 * and the initialize vector
	 * 
	 * @param Base64 and RSA encrypted message
	 * @return unencrypted message
	 */
	public String decrypt(String message) {
		Base64.Decoder decoder = Base64.getDecoder();
		byte[] text = decoder.decode(message);

		try {
			text = decryptBytes(text);
		} catch (GeneralSecurityException e) {
			AESEncryption.LOGGER.log(Level.WARNING, "Failed to decrypt message", e);
		}
		return new String(text);
	}

	private byte[] encryptBytes(byte[] text) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(AESEncryption.ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, this.key, this.iv);
		return cipher.doFinal(text);
	}

	private byte[] decryptBytes(byte[] text) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(AESEncryption.ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, this.key, this.iv);
		return cipher.doFinal(text);
	}
}
