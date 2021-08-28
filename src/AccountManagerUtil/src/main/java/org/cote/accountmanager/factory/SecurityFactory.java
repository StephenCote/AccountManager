/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.factory;

import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.util.BinaryUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.accountmanager.util.XmlUtil;
import org.w3c.dom.Document;


public class SecurityFactory {
	public static final Logger logger = LogManager.getLogger(SecurityFactory.class);

	/*
	 * https://github.com/bcgit/bc-java/wiki/Support-for-ECDSA,-ECGOST-Curves
	 */
	
	/// 2017/06/22 - this fixed salt is largely deprecated since 2015/06, but there's still one reference needing removal
	///
	private final static byte[] defaultSalt = new byte[]{
			110,41,-1,-64,-107,14,1,68,-127,-93,-110,-23,-73,-113,-98,-62
	};
	private static SecurityFactory securityFactory = null;
	private static SecureRandom secureRandom = null;
	public static SecurityFactory getSecurityFactory(){
		if(securityFactory != null)
				return securityFactory;
		securityFactory = new SecurityFactory();
		return securityFactory;
	}

	public SecurityFactory(){
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		secureRandom = new SecureRandom();
	}

	public byte[] serializeCipher(SecurityBean bean){
		StringBuilder buff = new StringBuilder();
		byte[] key = bean.getCipherKey();
		byte[] iv = bean.getCipherIV();
		if(key != null && key.length > 0 && iv != null && iv.length > 0){
			if(bean.getEncryptCipherKey().booleanValue()){
				key = bean.getEncryptedCipherKey();
				iv = bean.getEncryptedCipherIV();
			}
			buff.append("<key>" + BinaryUtil.toBase64Str(key) + "</key>");
			buff.append("<iv>" + BinaryUtil.toBase64Str(iv) + "</iv>");
		}
		else {
			logger.error((key == null || key.length == 0 ? "Key" : "IV") + " was null or empty");
		}
		return buff.toString().getBytes(StandardCharsets.UTF_8);
	}
	public byte[] serializePrivateKeyToRSAXml(SecurityBean bean){
		StringBuilder buff = new StringBuilder();
		buff.append("<RSAKeyValue>");

		RSAPrivateKey keySpec = (RSAPrivateKey) bean.getPrivateKey();
		buff.append("<Modulus>" + BinaryUtil.toBase64Str(keySpec.getModulus().toByteArray()) + "</Modulus>");
		buff.append("<D>" + BinaryUtil.toBase64Str(keySpec.getPrivateExponent().toByteArray()) + "</D>");
			
		buff.append("</RSAKeyValue>\r\n");
		return buff.toString().getBytes(StandardCharsets.UTF_8);
	}
	public byte[] serializePublicKeyToRSAXml(SecurityBean bean){
		StringBuilder buff = new StringBuilder();
		buff.append("<RSAKeyValue>");
		KeyFactory keyFactory = null;
		try {
			keyFactory = KeyFactory.getInstance(bean.getAsymmetricCipherKeySpec());
			RSAPublicKeySpec keySpec = keyFactory.getKeySpec(bean.getPublicKey(), RSAPublicKeySpec.class);
			buff.append("<Modulus>" + BinaryUtil.toBase64Str(keySpec.getModulus().toByteArray()) + "</Modulus>");
			buff.append("<Exponent>" + BinaryUtil.toBase64Str(keySpec.getPublicExponent().toByteArray()) + "</Exponent>");
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			logger.error(e.getMessage());
			logger.error(e);
		}
		buff.append("</RSAKeyValue>\r\n");
		return buff.toString().getBytes(StandardCharsets.UTF_8);
	}

	/// TODO: 2015/06/23 - Need to refactor to use a CredentialType
	///
	public void setPassKey(SecurityBean bean, String passKey, boolean encryptedPassKey){

		logger.warn("Static default salt needs to be refactored");
		setPassKey(bean, passKey, defaultSalt, encryptedPassKey);
	}
	
	/// TODO: Remove hard coded algorithm reference
	///
	public void setPassKey(SecurityBean bean, String passKey, byte[] salt, boolean encryptedPassKey){

		try{
			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");

			KeySpec spec = new javax.crypto.spec.PBEKeySpec(passKey.toCharArray(),salt, 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKey secret = new SecretKeySpec(tmp.getEncoded(), bean.getCipherKeySpec());

			Cipher cipher = Cipher.getInstance(bean.getSymmetricCipherKeySpec());
			cipher.init(Cipher.ENCRYPT_MODE, secret, new IvParameterSpec(salt, 0, 16));
			AlgorithmParameters params = cipher.getParameters();
			byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
			setSecretKey(bean, secret.getEncoded(), iv, encryptedPassKey);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | InvalidParameterSpecException e) {
			logger.error(e.getMessage());
			logger.error(e);
			e.printStackTrace();
		}  
	}
	public void setSecretKey(SecurityBean bean, byte[] key, byte[] iv, boolean encryptedCipher){
		byte[] decKey = key;
		byte[] decIv = iv;
		if(encryptedCipher){
			bean.setEncryptCipherKey(true);
			bean.setEncryptedCipherIV(iv);
			bean.setEncryptedCipherKey(key);
			decKey = SecurityUtil.decrypt(bean, key);
			decIv = SecurityUtil.decrypt(bean,  iv);	
		}
		bean.setSecretKey(new SecretKeySpec(decKey, bean.getSymmetricCipherKeySpec()));
		bean.setCipherIV(decIv);
		bean.setCipherKey(decKey);
	}
	
	public void setPublicKey(SecurityBean bean, byte[] publicKey){
		PublicKey pubKey = null;
		try {
			KeyFactory factory = KeyFactory.getInstance(bean.getAsymmetricCipherKeySpec());
    	   	X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
			pubKey = factory.generatePublic(x509KeySpec);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			logger.error(e.getMessage());
			logger.error(e);
		}
		bean.setPublicKey(pubKey);
		
	}
	
	public void setPrivateKey(SecurityBean bean, byte[] privateKey){
		PrivateKey privKey = null;
		try{
	        KeyFactory kFact = KeyFactory.getInstance(bean.getAsymmetricCipherKeySpec());
			PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(privateKey);
			privKey = kFact.generatePrivate(privKeySpec);		
		}
		catch(Exception e){
			logger.error(e.toString());
			logger.error(e);
		}
		bean.setPrivateKey(privKey);
	}
	
	public void setPublicKey(SecurityBean bean, byte[] modBytes, byte[] expBytes){

		BigInteger modules = new BigInteger(1, modBytes);
		BigInteger exponent = new BigInteger(1, expBytes);

		PublicKey pubKey = null;
		try {
			KeyFactory factory = KeyFactory.getInstance(bean.getAsymmetricCipherKeySpec());
			
			RSAPublicKeySpec pubSpec = new RSAPublicKeySpec(modules, exponent);
			pubKey = factory.generatePublic(pubSpec);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			logger.error(e.getMessage());
			logger.error(e);
		}
		bean.setPublicKey(pubKey);
	}
	public void setECDSAPublicKey(SecurityBean bean, byte[] ecdsaKey) {
		try {
			String key = new String(ecdsaKey,StandardCharsets.UTF_8);
			PemObject spki = new PemReader(new StringReader(key)).readPemObject();
			if(spki == null) {
				logger.error("Null Pem object");
				return;
			}
			/// KeyFactory keyGen = KeyFactory.getInstance(bean.getAsymmetricCipherKeySpec());
			KeyFactory keyGen = KeyFactory.getInstance(bean.getKeyAgreementSpec());
			PublicKey pubK = keyGen.generatePublic(new X509EncodedKeySpec(spki.getContent()));
        	bean.setPublicKey(pubK);
        	bean.setPublicKeyBytes(pubK.getEncoded());
		}
		catch(NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	public void setECDSAPrivateKey(SecurityBean bean, byte[] ecdsaKey) {
		
		try {
			String key = new String(ecdsaKey,StandardCharsets.UTF_8);
			PemObject spki = new PemReader(new StringReader(key)).readPemObject();
			if(spki == null) {
				logger.error("Null Pem object");
				return;
			}
			/// KeyFactory keyGen = KeyFactory.getInstance(bean.getAsymmetricCipherKeySpec());
			KeyFactory keyGen = KeyFactory.getInstance(bean.getKeyAgreementSpec());
			PrivateKey privK = keyGen.generatePrivate(new PKCS8EncodedKeySpec(spki.getContent()));
        	bean.setPrivateKey(privK);
        	bean.setPrivateKeyBytes(privK.getEncoded());

		}
		catch(NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	public void setRSAXMLPublicKey(SecurityBean bean, byte[] rsaKey){
		bean.setPublicKeyBytes(rsaKey);
		
		Document xml = XmlUtil.getDocumentFromBytes(rsaKey);
		
		byte[] modBytes = BinaryUtil.fromBase64(XmlUtil.GetElementText(xml.getDocumentElement(), "Modulus").getBytes(StandardCharsets.UTF_8));
		byte[] expBytes = BinaryUtil.fromBase64(XmlUtil.GetElementText(xml.getDocumentElement(), "Exponent").getBytes(StandardCharsets.UTF_8));

		setPublicKey(bean, modBytes, expBytes);

	}
	public void setRSAXMLPrivateKey(SecurityBean bean, byte[] rsaKey){
		bean.setPrivateKeyBytes(rsaKey);
		Document xml = XmlUtil.getDocumentFromBytes(rsaKey);
		byte[] modBytes = BinaryUtil.fromBase64(XmlUtil.GetElementText(xml.getDocumentElement(), "Modulus").getBytes(StandardCharsets.UTF_8));
		byte[] dBytes = BinaryUtil.fromBase64(XmlUtil.GetElementText(xml.getDocumentElement(), "D").getBytes(StandardCharsets.UTF_8));
		setPrivateKey(bean, modBytes, dBytes);
	}
	public void setPrivateKey(SecurityBean bean, byte[] modBytes, byte[] dBytes){
		BigInteger modulus = new BigInteger(1, modBytes);
		BigInteger d = new BigInteger(1, dBytes);

		PrivateKey priKey = null;
		try {
			KeyFactory factory = KeyFactory.getInstance(bean.getAsymmetricCipherKeySpec());
			RSAPrivateKeySpec privSpec = new RSAPrivateKeySpec(modulus, d);
			priKey = factory.generatePrivate(privSpec);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			logger.error(e.getMessage());
			logger.error(e);
		}
		bean.setPrivateKey(priKey);

	}
	public SecurityBean createSecurityBean(byte[] keys, boolean encryptedCipher){
		SecurityBean bean = new SecurityBean();
		importSecurityBean(bean, keys, encryptedCipher);
		return bean;
	}
	public void importSecurityBean(SecurityBean bean, byte[] keys, boolean encryptedCipher){
		Document d = XmlUtil.getDocumentFromBytes(keys);
		if(d == null)
			return;

		String pubKey = XmlUtil.FindElementText(d.getDocumentElement(), "public", "key");
		if(pubKey != null){
			setRSAXMLPublicKey(bean, BinaryUtil.fromBase64(pubKey.getBytes(StandardCharsets.UTF_8)));
		}
		pubKey = XmlUtil.FindElementText(d.getDocumentElement(), "public", "ec-key");
		if(pubKey != null){
			setECDSAPublicKey(bean, BinaryUtil.fromBase64(pubKey.getBytes(StandardCharsets.UTF_8)));
		}
		String priKey = XmlUtil.FindElementText(d.getDocumentElement(), "private", "key");
		if(priKey != null){
			setRSAXMLPrivateKey(bean,BinaryUtil.fromBase64(priKey.getBytes(StandardCharsets.UTF_8)));
		}
		priKey = XmlUtil.FindElementText(d.getDocumentElement(), "private", "ec-key");
		if(priKey != null){
			setECDSAPrivateKey(bean,BinaryUtil.fromBase64(priKey.getBytes(StandardCharsets.UTF_8)));
		}
		String cipKey = XmlUtil.FindElementText(d.getDocumentElement(), "cipher", "key");
		String cipIv = XmlUtil.FindElementText(d.getDocumentElement(), "cipher", "iv");
		if(cipKey != null && cipIv != null){
			setSecretKey(bean, BinaryUtil.fromBase64(cipKey.getBytes(StandardCharsets.UTF_8)),BinaryUtil.fromBase64(cipIv.getBytes(StandardCharsets.UTF_8)), encryptedCipher);
		}
	}
	public Cipher getEncryptCipherKey(SecurityBean bean){
		return getCipherKey(bean, false);
	}
	public Cipher getDecryptCipherKey(SecurityBean bean){
		return getCipherKey(bean, true);
	}
	private Cipher getCipherKey(SecurityBean bean,  boolean decrypt){
		boolean bECD = bean.getCipherKeySpec().startsWith("EC");
		if(
			bean.getSecretKey() == null
			&&
			(bECD
			&&
			( decrypt && bean.getPrivateKey() == null)
			|| ( bean.getPublicKey() == null )
			)
		){
			return null;
		}

		Cipher cipherKey = null;
       try {
		cipherKey = Cipher.getInstance((bECD ? bean.getCipherKeySpec() : bean.getSymmetricCipherKeySpec()));
		int mode = Cipher.ENCRYPT_MODE;
		if(decrypt) mode = Cipher.DECRYPT_MODE;

		if(bECD) {
			cipherKey.init(mode,  (decrypt ? bean.getPrivateKey() : bean.getPublicKey()));
		}
		else if(bean.getCipherIV() != null && bean.getCipherIV().length > 0){
			IvParameterSpec iv = new IvParameterSpec(bean.getCipherIV());
			cipherKey.init(mode, bean.getSecretKey(), iv);

		}
		else{
			cipherKey.init(mode,  bean.getSecretKey());
			bean.setCipherIV(cipherKey.getIV());
		}

       }
       catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			logger.error(e.getMessage());
			logger.error(e);
			e.printStackTrace();
		}
       return cipherKey;
	}
	public boolean generateSecretKey(SecurityBean bean){
		boolean ret = false;
		if(bean == null) {
			logger.error("SecurityBean is null");
			return ret;
		}
		if(bean.getEncryptCipherKey().booleanValue() && bean.getPublicKey() == null){
			logger.error("Cannot encrypt secret key with missing PKI data.  Verify PKI is initialized.");
			return false;
		}

		boolean bECD = (bean.getKeyAgreementSpec() != null);
		SecretKey secretKey = null;
		try {
			if(bECD) {
				KeyAgreement keyAgreement = KeyAgreement.getInstance(bean.getKeyAgreementSpec());
				keyAgreement.init(bean.getPrivateKey());
				keyAgreement.doPhase(bean.getPublicKey(), true);
			    /// secretKey = keyAgreement.generateSecret(bean.getAsymmetricCipherKeySpec());
				secretKey = keyAgreement.generateSecret(bean.getSymmetricCipherKeySpec());
			}
			else {
				KeyGenerator kgen = KeyGenerator.getInstance(bean.getCipherKeySpec());
				/// KeyGenerator kgen = KeyGenerator.getInstance(bean.getSymmetricCipherKeySpec());
				kgen.init(bean.getCipherKeySize());
				secretKey = kgen.generateKey();
			}
			bean.setSecretKey(secretKey);
			bean.setCipherKey(secretKey.getEncoded());
			Cipher cipher = getCipherKey(bean, false);
			if(cipher == null) {
				logger.error("Cipher is null");
				return ret;
			}
			bean.setCipherIV(cipher.getIV());
			if(bean.getEncryptCipherKey().booleanValue()){
				bean.setEncryptedCipherKey(SecurityUtil.encrypt(bean, bean.getCipherKey()));
				bean.setEncryptedCipherIV(SecurityUtil.encrypt(bean, bean.getCipherIV()));
			}
			ret = true;
		} catch (NoSuchAlgorithmException | InvalidKeyException e) {
			logger.error(e.getMessage());
			logger.error(e);
			e.printStackTrace();
			
		}
		return ret;
	}
	public boolean generateKeyPair(SecurityBean bean){

		boolean ret = false;
		try{
	        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(bean.getAsymmetricCipherKeySpec());
	        
	        boolean bECD = bean.getAsymmetricCipherKeySpec().startsWith("EC");
	        if(bECD) {

	        	ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec(bean.getCurveName());
	        	
	        	keyGen.initialize(ecSpec, secureRandom);
	        }
	        else keyGen.initialize(bean.getKeySize(), secureRandom);
        	KeyPair keyPair = keyGen.generateKeyPair();
        	bean.setPublicKey(keyPair.getPublic());
        	bean.setPrivateKey(keyPair.getPrivate());
			/* the public key */
			bean.setPublicKeyBytes(keyPair.getPublic().getEncoded());
			bean.setPrivateKeyBytes(keyPair.getPrivate().getEncoded());
	        
			ret = true;
		}
		catch(Exception e){
			logger.error(e);
			e.printStackTrace();
		}
		return ret;
	}

}
