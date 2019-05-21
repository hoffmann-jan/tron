package de.tron.client_java.network.encryption;

import java.math.BigInteger;

public class RSAPublicKeyData {

	private byte[] exponent;
	private byte[] modulus;

	public BigInteger getExponent() {
		return new BigInteger(exponent);
	}

	public void setExponent(BigInteger bigInteger) {
		this.exponent = bigInteger.toByteArray();
	}

	public BigInteger getModulus() {
		return new BigInteger(modulus);
	}

	public void setModulus(BigInteger bigInteger) {
		this.modulus = bigInteger.toByteArray();
	}
	
}
