package org.cote.accountmanager.factory;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;

import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.crypto.params.RSAPrivateCrtKeyParameters;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.util.BinaryUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.accountmanager.util.XmlUtil;
import org.w3c.dom.Document;

public class SecurityFactory {
	public static SecurityFactory securityFactory = null;
	public static SecurityFactory getSecurityFactory(){
		if(securityFactory != null) return securityFactory;
		securityFactory = new SecurityFactory();
		return securityFactory;
	}
	public SecurityFactory(){
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
	}
/*
	public byte[] serialize(SecurityBean bean, boolean serialize_public_key, boolean serialize_private_key, boolean serialize_cipher){
		StringBuilder buff = new StringBuilder();
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<SecurityManager>");
		if(serialize_public_key){
			buff.append("<public><key>" + BinaryUtil.toBase64Str(serializePublicKeyToRSAXml(bean)) + "</key></public>");
		}
		if(serialize_private_key){
			buff.append("<private><key>" + BinaryUtil.toBase64Str(serializePrivateKeyToRSAXml(bean)) + "</key></private>");
		}
		if(serialize_cipher){
			buff.append("<cipher>" + BinaryUtil.toBase64Str(serializeCipher(bean)) + "</cipher>");
		}
		buff.append("</SecurityManager>\r\n");
		return buff.toString().getBytes();
	}
*/
	public byte[] serializeCipher(SecurityBean bean){
		StringBuilder buff = new StringBuilder();
		byte[] key = bean.getCipherKey();
		byte[] iv = bean.getCipherIV();
		if(key != null && key.length > 0 && iv != null && iv.length > 0){
			if(bean.getEncryptCipherKey()){
				key = bean.getEncryptedCipherKey();
				iv = bean.getEncryptedCipherIV();
			}
			buff.append("<key>" + BinaryUtil.toBase64Str(key) + "</key>");
			buff.append("<iv>" + BinaryUtil.toBase64Str(iv) + "</iv>");
		}
		return buff.toString().getBytes();
	}
	public byte[] serializePrivateKeyToRSAXml(SecurityBean bean){
		StringBuilder buff = new StringBuilder();
		buff.append("<RSAKeyValue>");
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			///RSAKeyParameters privateKey = (RSAKeyParameters) bean.getPrivateKey();
			RSAPrivateKey keySpec = (RSAPrivateKey) bean.getPrivateKey();
			
			//keySpec.g
			//RSAPrivateKeySpec keySpec = (RSAPrivateKeySpec)bean.getPrivateKey();
			
			//RSAKeyGenParameterSpec keySpec = (RSAKeyGenParameterSpec)bean.getPrivateKey();
			//keySpec.
			//bean.getPrivateKey()
			///buff.append((new String(bean.getPrivateKey().getEncoded())));
			//PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(bean.getPrivateKey().getEncoded());
			//privKeySpec.
			
			//RSAPrivateCrtKeySpec keySpec = keyFactory.getKeySpec(bean.getPrivateKey(), RSAPrivateCrtKeySpec.class);
/*			
			buff.append("<Modules>" + BinaryUtil.toBase64Str(privateKey.getModulus().toByteArray()) + "</Modulus>");
			buff.append("<Exponent>" + BinaryUtil.toBase64Str(privateKey.getPublicExponent().toByteArray()) + "</Exponent>");
			buff.append("<P>" + BinaryUtil.toBase64Str(privateKey.getP().toString().getBytes()) + "</P>");
			buff.append("<Q>" + BinaryUtil.toBase64Str(privateKey.getQ().toString().getBytes()) + "</Q>");
			buff.append("<DP>" + BinaryUtil.toBase64Str(privateKey.getDP().toString().getBytes()) + "</DP>");
			buff.append("<DQ>" + BinaryUtil.toBase64Str(privateKey.getDQ().toString().getBytes()) + "</DQ>");
			buff.append("<InverseQ>" + BinaryUtil.toBase64Str(privateKey.getQInv().toString().getBytes()) + "</InverseQ>");
			buff.append("<D>" + BinaryUtil.toBase64Str(privateKey.getExponent().toString().getBytes()) + "</D>");
*/
			buff.append("<Modulus>" + BinaryUtil.toBase64Str(keySpec.getModulus().toByteArray()) + "</Modulus>");
			/*
			buff.append("<Exponent>" + BinaryUtil.toBase64Str(keySpec.getPublicExponent().toByteArray()) + "</Exponent>");
			buff.append("<P>" + BinaryUtil.toBase64Str(keySpec.getPrimeP().toString().getBytes()) + "</P>");
			buff.append("<Q>" + BinaryUtil.toBase64Str(keySpec.getPrimeQ().toString().getBytes()) + "</Q>");
			buff.append("<DP>" + BinaryUtil.toBase64Str(keySpec.getPrimeExponentP().toString().getBytes()) + "</DP>");
			buff.append("<DQ>" + BinaryUtil.toBase64Str(keySpec.getPrimeExponentQ().toString().getBytes()) + "</DQ>");
			buff.append("<InverseQ>" + BinaryUtil.toBase64Str(keySpec.getCrtCoefficient().toString().getBytes()) + "</InverseQ>");
			*/
			buff.append("<D>" + BinaryUtil.toBase64Str(keySpec.getPrivateExponent().toByteArray()) + "</D>");
			
		} catch (NoSuchAlgorithmException e) {
		
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		buff.append("</RSAKeyValue>\r\n");
		return buff.toString().getBytes();
	}
	public byte[] serializePublicKeyToRSAXml(SecurityBean bean){
		StringBuilder buff = new StringBuilder();
		buff.append("<RSAKeyValue>");
		KeyFactory keyFactory;
		try {
			keyFactory = KeyFactory.getInstance(bean.getAsymetricCipherKeySpec());
			RSAPublicKeySpec keySpec = keyFactory.getKeySpec(bean.getPublicKey(), RSAPublicKeySpec.class);
			//RSAPrivateCrtKeySpec keySpec = keyFactory.getKeySpec(bean.getPrivateKey(), RSAPrivateCrtKeySpec.class);
			buff.append("<Modulus>" + BinaryUtil.toBase64Str(keySpec.getModulus().toByteArray()) + "</Modulus>");
			buff.append("<Exponent>" + BinaryUtil.toBase64Str(keySpec.getPublicExponent().toByteArray()) + "</Exponent>");
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		buff.append("</RSAKeyValue>\r\n");
		return buff.toString().getBytes();
	}
	public void setPassKey(SecurityBean bean, byte[] passKey, boolean encrypted_pass_key){
		final byte[] iv = {
				64, 65, 66, 64, 65, 66, 78, 94,
				64, 65, 66, 64, 65, 66, 78, 94
		};
		setSecretKey(bean, passKey, iv, encrypted_pass_key);
		
	}
	public void setSecretKey(SecurityBean bean, byte[] key, byte iv[], boolean encrypted_cipher){
		byte[] dec_key = new byte[0];
		byte[] dec_iv = new byte[0];
		if(encrypted_cipher){
			bean.setEncryptCipherKey(true);
			bean.setEncryptedCipherIV(iv);
			bean.setEncryptedCipherKey(key);
			dec_key = SecurityUtil.decrypt(bean, key);
			dec_iv = SecurityUtil.decrypt(bean,  iv);	
		}
		else{
			dec_key = key;
			dec_iv = iv;
		}
		bean.setSecretKey(new SecretKeySpec(dec_key, bean.getSymetricCipherKeySpec()));
		bean.setCipherIV(dec_iv);
		bean.setCipherKey(dec_key);
	}
	
	public void setPublicKey(SecurityBean bean, byte[] publicKey){
		PublicKey pubKey = null;
		try {
			KeyFactory factory = KeyFactory.getInstance(bean.getAsymetricCipherKeySpec());
    	   	X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKey);
			pubKey = factory.generatePublic(x509KeySpec);
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bean.setPublicKey(pubKey);
		
	}
	
	public void setPrivateKey(SecurityBean bean, byte[] privateKey){
		PrivateKey privKey = null;
		try{
	        KeyFactory k_fact = KeyFactory.getInstance(bean.getAsymetricCipherKeySpec());
			PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(privateKey);
			privKey = k_fact.generatePrivate(privKeySpec);		
		}
		catch(Exception e){
			System.out.println("DSAKeyUtil:: decodeX509PrivateKey: " + e.toString());
			e.printStackTrace();
		}
		bean.setPrivateKey(privKey);
	}
	
	public void setPublicKey(SecurityBean bean, byte[] modBytes, byte[] expBytes){

		BigInteger modules = new BigInteger(1, modBytes);
		BigInteger exponent = new BigInteger(1, expBytes);

		PublicKey pubKey = null;
		try {
			KeyFactory factory = KeyFactory.getInstance(bean.getAsymetricCipherKeySpec());
			
			RSAPublicKeySpec pubSpec = new RSAPublicKeySpec(modules, exponent);
			pubKey = factory.generatePublic(pubSpec);
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bean.setPublicKey(pubKey);
	}
	public void setRSAXMLPublicKey(SecurityBean bean, byte[] rsaKey){
		bean.setPublicKeyBytes(rsaKey);
		
		Document xml = XmlUtil.GetDocumentFromBytes(rsaKey);
		
		byte[] modBytes = BinaryUtil.fromBase64(XmlUtil.GetElementText(xml.getDocumentElement(), "Modulus").getBytes());
		byte[] expBytes = BinaryUtil.fromBase64(XmlUtil.GetElementText(xml.getDocumentElement(), "Exponent").getBytes());

		setPublicKey(bean, modBytes, expBytes);

	}
	public void setRSAXMLPrivateKey(SecurityBean bean, byte[] rsaKey){
		bean.setPrivateKeyBytes(rsaKey);
		Document xml = XmlUtil.GetDocumentFromBytes(rsaKey);
		byte[] modBytes = BinaryUtil.fromBase64(XmlUtil.GetElementText(xml.getDocumentElement(), "Modulus").getBytes());
		byte[] dBytes = BinaryUtil.fromBase64(XmlUtil.GetElementText(xml.getDocumentElement(), "D").getBytes());
		setPrivateKey(bean, modBytes, dBytes);
	}
	public void setPrivateKey(SecurityBean bean, byte[] modBytes, byte[] dBytes){
		BigInteger modulus = new BigInteger(1, modBytes);
		BigInteger d = new BigInteger(1, dBytes);

		PrivateKey priKey = null;
		try {
			KeyFactory factory = KeyFactory.getInstance(bean.getAsymetricCipherKeySpec());
			///Cipher cipher = Cipher.getInstance(ASYMETRIC_CIPHER_KEY_SPEC);
			///String input = "test";

			RSAPrivateKeySpec privSpec = new RSAPrivateKeySpec(modulus, d);
			priKey = factory.generatePrivate(privSpec);
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bean.setPrivateKey(priKey);

	}
	public SecurityBean createSecurityBean(byte[] keys, boolean encrypted_cipher){

		Document d = XmlUtil.GetDocumentFromBytes(keys);
		if(d == null) return null;
		SecurityBean bean = new SecurityBean();
		String pubKey = XmlUtil.FindElementText(d.getDocumentElement(), "public", "key");
		if(pubKey != null){
			setRSAXMLPublicKey(bean, BinaryUtil.fromBase64(pubKey.getBytes()));
		}
		String priKey = XmlUtil.FindElementText(d.getDocumentElement(), "private", "key");
		if(priKey != null){
			setRSAXMLPrivateKey(bean,BinaryUtil.fromBase64(priKey.getBytes()));
		}
		String cipKey = XmlUtil.FindElementText(d.getDocumentElement(), "cipher", "key");
		String cipIv = XmlUtil.FindElementText(d.getDocumentElement(), "cipher", "iv");
		if(cipKey != null && cipIv != null){
			setSecretKey(bean, BinaryUtil.fromBase64(cipKey.getBytes()),BinaryUtil.fromBase64(cipIv.getBytes()), encrypted_cipher);
		}
		return bean;
	}
	public Cipher getEncryptCipherKey(SecurityBean bean){
		return getCipherKey(bean, false);
	}
	public Cipher getDecryptCipherKey(SecurityBean bean){
		return getCipherKey(bean, true);
	}
	private Cipher getCipherKey(SecurityBean bean,  boolean decrypt){
		if(bean.getSecretKey() == null){
			return null;
		}

		Cipher cipher_key = null;
       try {
		cipher_key = Cipher.getInstance(bean.getSymetricCipherKeySpec());

		int mode = Cipher.ENCRYPT_MODE;
		if(decrypt) mode = Cipher.DECRYPT_MODE;
		if(bean.getCipherIV() != null && bean.getCipherIV().length > 0){
			IvParameterSpec iv = new IvParameterSpec(bean.getCipherIV());
			cipher_key.init(mode, bean.getSecretKey(), iv);

		}
		else{
			cipher_key.init(mode,  bean.getSecretKey());
			bean.setCipherIV(cipher_key.getIV());
		}

       }
       catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       ///bean.setCipher(cipher_key);
       return cipher_key;
	}
	public boolean generateSecretKey(SecurityBean bean){
		boolean ret = false;
		if(bean.getEncryptCipherKey() && bean.getPrivateKey() == null){
			/// Cannot generate encrypted cipher if PKI is not initialized
			return false;
		}
		KeyGenerator kgen;
		SecretKey secret_key = null;
		try {
			/// was "AES"
			kgen = KeyGenerator.getInstance(bean.getCipherKeySpec());
			// TODO: Make key size configurable - 128 is only for dev/debug
			//
			kgen.init(128);

			secret_key = kgen.generateKey();
			bean.setSecretKey(secret_key);
			bean.setCipherKey(secret_key.getEncoded());
			Cipher cipher = getCipherKey(bean, false);
			bean.setCipherIV(cipher.getIV());
			if(bean.getEncryptCipherKey()){
				bean.setEncryptedCipherKey(SecurityUtil.encrypt(bean, bean.getCipherKey()));
				bean.setEncryptedCipherIV(SecurityUtil.encrypt(bean, bean.getCipherIV()));
			}
			/// generateSecretCipherKey(bean);
			///bean.getCipher().init(Cipher.ENCRYPT_MODE, secret_key);
			
			//byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();

			ret = true;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		/*
		 catch (InvalidKeyException e) {
		 
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		return ret;
	}
	public boolean generateKeyPair(SecurityBean bean){

		boolean ret = false;
		try{
	        KeyPairGenerator key_gen = KeyPairGenerator.getInstance(bean.getAsymetricCipherKeySpec());
	        key_gen.initialize(bean.getKeySize());
        	KeyPair key_pair = key_gen.generateKeyPair();
        	bean.setPublicKey(key_pair.getPublic());
        	bean.setPrivateKey(key_pair.getPrivate());
			/* the public key */
			bean.setPublicKeyBytes(key_pair.getPublic().getEncoded());
			bean.setPrivateKeyBytes(key_pair.getPrivate().getEncoded());

			ret = true;
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return ret;
	}

}
