package de.tron.client_java.network.encryption;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.crypto.Cipher;

public class RSAEncryption {
	
	private static final Logger LOGGER = Logger.getLogger("root");

	public static final String ALGORITHM = "RSA/ECB/PKCS1PADDING";

	private final KeyPair key;
	private PublicKey otherPublicKey;
	
	/**
	 * Automatically generates private and public key for RSA encoding of this client
	 * 
	 * @throws NoSuchAlgorithmException
	 */
	public RSAEncryption() throws NoSuchAlgorithmException {
		this.key = generateKeys();
	}

	/**
	 * Encodes the message with the public key of the receiver and 
	 * additionally apply Base64 encoding
	 * 
	 * @param message unencrypted message
	 * @return Base64 and RSA encrypted message
	 */
	public String encrypt(String message) {
		byte[] text = message.getBytes();
		try {
			text = encryptBytes(text);
		} catch (GeneralSecurityException e) {
			RSAEncryption.LOGGER.log(Level.WARNING, "Failed to encrypt message", e);
		}
		Base64.Encoder encoder = Base64.getEncoder();
		return encoder.encodeToString(text);
	}

	/**
	 * Decodes the Base64 encoding and the RSA encoding with the private key of this client
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
			RSAEncryption.LOGGER.log(Level.WARNING, "Failed to decrypt message", e);
		}
		return new String(text);
	}

	private KeyPair generateKeys() throws NoSuchAlgorithmException {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
		keyGenerator.initialize(2048);
		return keyGenerator.generateKeyPair();
	}

	private byte[] decryptBytes(byte[] message) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(RSAEncryption.ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, this.key.getPrivate());
		return cipher.doFinal(message);
	}

	private byte[] encryptBytes(byte[] message) throws GeneralSecurityException {
		Cipher cipher = Cipher.getInstance(RSAEncryption.ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, this.otherPublicKey);
		return cipher.doFinal(message);
	}
	
	/**
	 * Create an usable public key from the given data
	 * 
	 * @param publicKey
	 * @throws GeneralSecurityException
	 */
	public void setOtherPublicKey(RSAPublicKeyData publicKey) throws GeneralSecurityException {
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(publicKey.getModulus(), publicKey.getExponent());
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		this.otherPublicKey = keyFactory.generatePublic(keySpec);
	}

	/**
	 * @return Data of the public key of this client 
	 */
	public RSAPublicKeyData getPublicKey() {	
		RSAPublicKeyData data = new RSAPublicKeyData();
		data.setExponent(((RSAPublicKey) this.key.getPublic()).getPublicExponent());
		data.setModulus(((RSAPublicKey) this.key.getPublic()).getModulus());
		return data;
	}

}
