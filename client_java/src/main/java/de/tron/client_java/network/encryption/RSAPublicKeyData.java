package de.tron.client_java.network.encryption;

import java.math.BigInteger;

public class RSAPublicKeyData {

	private String exponent;
	private String modulus;

	public BigInteger getExponent() {
		return new BigInteger(exponent);
	}

	public void setExponent(BigInteger bigInteger) {
		this.exponent = bigInteger.toString();
	}

	public BigInteger getModulus() {
		return new BigInteger(modulus);
	}

	public void setModulus(BigInteger bigInteger) {
		this.modulus = bigInteger.toString();
	}
	
}
