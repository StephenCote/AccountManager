package org.cote.accountmanager.beans;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;

import org.cote.accountmanager.objects.SecurityType;

public class SecurityBean extends SecurityType {
	private SecretKey secretKey = null;
	/// private Cipher cipher = null;
	private PublicKey publicKey = null;
	private PrivateKey privateKey = null;
	
	/*
	public Cipher getCipher() {
		return cipher;
	}

	public void setCipher(Cipher cipher) {
		this.cipher = cipher;
	}
*/
	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}

	public SecretKey getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(SecretKey secretKey) {
		this.secretKey = secretKey;
	}
	
}
