package de.tron.client_java.network.encription;

import java.math.BigInteger;

import javax.crypto.spec.DHParameterSpec;

public class DHPublicKey {

	private BigInteger p;
	private BigInteger g;
	private BigInteger y;
	
	public DHPublicKey(BigInteger p, BigInteger g, BigInteger y) {
		setP(p);
		setG(g);
		setY(y);
	}

	public DHPublicKey(javax.crypto.interfaces.DHPublicKey publicKey) {
		DHParameterSpec parameters = publicKey.getParams();
		setP(parameters.getP());
		setG(parameters.getG());
		setY(publicKey.getY());
	}

	public BigInteger getP() {
		return p;
	}

	public void setP(BigInteger p) {
		this.p = p;
	}

	public BigInteger getG() {
		return g;
	}

	public void setG(BigInteger g) {
		this.g = g;
	}

	public BigInteger getY() {
		return y;
	}

	public void setY(BigInteger y) {
		this.y = y;
	}

}
