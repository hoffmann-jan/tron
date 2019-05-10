package de.tron.client_java.network.encription;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import javax.crypto.Cipher;

public class RSAEncryption {

	public static final String ALGORITHM = "RSA";

	private final KeyPair key;
	private PublicKey otherPublicKey;
	
	public RSAEncryption() throws NoSuchAlgorithmException {
		this.key = generateKeys();
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

	private KeyPair generateKeys() throws NoSuchAlgorithmException {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(RSAEncryption.ALGORITHM);
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
	
	public void setOtherPublicKey(RSAPublicKeyData publicKey) throws GeneralSecurityException {
		RSAPublicKeySpec keySpec = new RSAPublicKeySpec(publicKey.getModulus(), publicKey.getExponent());
		KeyFactory keyFactory = KeyFactory.getInstance(RSAEncryption.ALGORITHM);
		this.otherPublicKey = keyFactory.generatePublic(keySpec);
	}

	public RSAPublicKeyData getPublicKey() {	
		RSAPublicKeyData data = new RSAPublicKeyData();
		data.setExponent(((RSAPublicKey) this.key.getPublic()).getPublicExponent());
		data.setModulus(((RSAPublicKey) this.key.getPublic()).getModulus());
		return data;
	}

}
