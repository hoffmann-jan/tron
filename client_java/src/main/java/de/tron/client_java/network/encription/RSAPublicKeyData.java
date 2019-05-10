package de.tron.client_java.network.encription;

import java.math.BigInteger;

public class RSAPublicKeyData {

	private BigInteger exponent;
	private BigInteger modulus;

	public BigInteger getExponent() {
		return exponent;
	}

	public void setExponent(BigInteger bigInteger) {
		this.exponent = bigInteger;
	}

	public BigInteger getModulus() {
		return modulus;
	}

	public void setModulus(BigInteger bigInteger) {
		this.modulus = bigInteger;
	}
	
}