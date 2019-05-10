package de.tron.client_java.network.encription;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHPublicKeySpec;

public class AsymmetricEncryption {
	
	public static final String HASH_ALGORITHM = "SHA-256";
	public static final String ALGORITHM = "DH";
	
	public final KeyPair key;
	
	public AsymmetricEncryption() throws NoSuchAlgorithmException, InvalidKeyException {
		this.key = generateKeys();
	}
	
	private KeyPair generateKeys() throws NoSuchAlgorithmException {
		KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance(AsymmetricEncryption.ALGORITHM);
		keyGenerator.initialize(1024);
		return keyGenerator.generateKeyPair();		
	}
	
	public byte[] generateSecret(DHPublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
		KeyAgreement keyAgreement = KeyAgreement.getInstance(AsymmetricEncryption.ALGORITHM);
		keyAgreement.init(this.key.getPrivate());
		keyAgreement.doPhase(mapToPublicKey(publicKey), true);
		byte[] secret = keyAgreement.generateSecret();
		secret = MessageDigest.getInstance(AsymmetricEncryption.HASH_ALGORITHM).digest(secret);
		return secret;
	}


	private PublicKey mapToPublicKey(DHPublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
		KeyFactory factory = KeyFactory.getInstance(AsymmetricEncryption.ALGORITHM);
		DHPublicKeySpec spec = new DHPublicKeySpec(publicKey.getY(), publicKey.getP(), publicKey.getG());
		return factory.generatePublic(spec);
	}

	public javax.crypto.interfaces.DHPublicKey getPublicKey() {
		return (javax.crypto.interfaces.DHPublicKey) this.key.getPublic();
	}
	

}
