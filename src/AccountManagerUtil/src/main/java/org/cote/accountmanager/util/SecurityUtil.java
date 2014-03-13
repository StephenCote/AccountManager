package org.cote.accountmanager.util;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.SecurityType;
import org.w3c.dom.Document;

public class SecurityUtil {
	public static final Logger logger = Logger.getLogger(SecurityUtil.class.getName());
	private static MessageDigest hash_algorithm = null;
	private static String HASH_PROVIDER = "SHA-256";
	private static String HASH_SALT = "aostnh234stnh;qk234;2354!@#$%10";
	
	/*
	public static byte[] getPassphraseBytes(String passphrase){
		String passphrase_tmp = getSaltedDigest(passphrase);
		return Arrays.copyOf(getSaltedDigest(passphrase).getBytes(), 16);
	}
	*/
	public static String getSaltedDigest(String in_value)
	{
		if (in_value == null || in_value.length() == 0) return null;
		return getDigestAsString(in_value + HASH_SALT);
	}
	private static MessageDigest getMessageDigest(){
		return getMessageDigest(false);
	}
	private static MessageDigest getMessageDigest(boolean use_singleton){
		///Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		if(use_singleton && hash_algorithm != null) return hash_algorithm;
		MessageDigest digest = null;
		try{
			digest = MessageDigest.getInstance(HASH_PROVIDER);
		}
		catch(NoSuchAlgorithmException nsae){
			nsae.printStackTrace();
		}
		if(use_singleton && digest != null) hash_algorithm = digest;
		return digest;
	}

	public static byte[] getDigest(byte[] in_bytes){
		MessageDigest digest = getMessageDigest();
		

		digest.update(in_bytes,0,in_bytes.length);
		byte[] out_bytes = digest.digest();
		return out_bytes;
	}
	public static String getDigestAsString(byte[] in_bytes){
		return new String(BinaryUtil.toBase64(getDigest(in_bytes)));
	}
	public static String getDigestAsString(String in_str){
		byte[] digest = getDigest(in_str.getBytes());
		return new String(BinaryUtil.toBase64(digest));
	}
	public static SecurityBean getPasswordBean(String password, byte[] salt){
		SecurityBean bean = new SecurityBean();
		SecurityFactory.getSecurityFactory().setPassKey(bean, password, salt,false);
		return bean;
	}
	public static byte[] encipher(byte[] data, String password, byte[] salt){
		return encipher(getPasswordBean(password, salt),data);
	}
	public static byte[] decipher(byte[] data, String password, byte[] salt){
		return decipher(getPasswordBean(password, salt),data);
	}
	public static byte[] decipher(SecurityBean bean, byte[] data){
		byte[] ret = new byte[0];
		/// Cipher cipher = generateSecretCipherKey();
		Cipher cipher = SecurityFactory.getSecurityFactory().getDecryptCipherKey(bean);;
		SecretKey secret_key = bean.getSecretKey();
		if(cipher == null || secret_key == null ){
			logger.error("Secret key is null");
			return ret;
		}
		try {
			ret = cipher.doFinal(data);
		}
		catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		return ret;
	}
	public static byte[] encipher(SecurityBean bean, byte[] data){
		byte[] ret = new byte[0];
		Cipher cipher = SecurityFactory.getSecurityFactory().getEncryptCipherKey(bean);;
		if(cipher == null || bean.getSecretKey() == null ) return ret;
		try {
			ret = cipher.doFinal(data);
		}
		catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return ret;
	}
	public static byte[] encrypt(SecurityBean bean, byte[] data){
		PublicKey key = bean.getPublicKey();

		byte[] ret = new byte[0];
		if(key == null || data.length == 0){
			return ret;
		}
		try{
			Cipher cipher = Cipher.getInstance(bean.getAsymetricCipherKeySpec());
			if(cipher == null){
				logger.error("Null Cipher");
				return ret;
			}


    	    cipher.init(Cipher.ENCRYPT_MODE, key);
			ret = cipher.doFinal(data);
		}
		catch(Exception e){
			e.printStackTrace();
		}

		return ret;

	}

	public static byte[] decrypt(SecurityBean bean, byte[] data){
		PrivateKey key = bean.getPrivateKey();
		byte[] ret = new byte[0];
		if(key == null || data.length == 0){
			return ret;
		}
		try{
			Cipher cipher = Cipher.getInstance(bean.getAsymetricCipherKeySpec());
    	    cipher.init(Cipher.DECRYPT_MODE, key);
			ret = cipher.doFinal(data);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return ret;
	}
	public static String serializeToXml(SecurityBean bean, boolean include_private_key, boolean include_public_key, boolean include_cipher){
		StringBuilder buff = new StringBuilder();
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<SecurityManager>");
		if(include_public_key){
			buff.append("<public><key>" + BinaryUtil.toBase64Str(sf.serializePublicKeyToRSAXml(bean)) + "</key></public>");
		}
		if(include_private_key){
			buff.append("<private><key>" + BinaryUtil.toBase64Str(sf.serializePrivateKeyToRSAXml(bean)) + "</key></private>");
		}
		if(include_cipher){
			buff.append("<cipher>" + (new String(sf.serializeCipher(bean))) + "</cipher>");
		}
		buff.append("</SecurityManager>\r\n");
		return buff.toString();
		
		
		/*
		 StringBuilder buff = new StringBuilder();
		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<SecurityManager>\r\n");
		if(include_public_key && security_type.getPublicKeyBytes() != null && security_type.getPublicKeyBytes().length > 0){
			buff.append("<public><key><![CDATA[" + BinaryUtil.toBase64Str(security_type.getPublicKeyBytes()) + "]]></key></public>\r\n");
		}
		if(include_private_key && security_type.getPrivateKeyBytes() != null && security_type.getPrivateKeyBytes().length > 0){
			buff.append("<private><key><![CDATA[" + BinaryUtil.toBase64Str(security_type.getPrivateKeyBytes()) + "]]></key></private>");
		}
		byte[] civ = (security_type.isEncryptCipherKey() ? security_type.getEncryptedCipherIV() : security_type.getCipherIV());
		byte[] ckey = (security_type.isEncryptCipherKey() ? security_type.getEncryptedCipherKey() : security_type.getCipherKey());
		if(include_secret_key && civ != null && civ.length > 0 && ckey != null && ckey.length > 0){
			buff.append("<cipher>\r\n");
			
			buff.append("<key><![CDATA[" + BinaryUtil.toBase64Str(ckey) + "]]></key>");
			buff.append("<iv><![CDATA[" + BinaryUtil.toBase64Str(civ) + "]]></iv>");
			buff.append("</cipher>\r\n");
		}
		
		buff.append("</SecurityManager>\r\n");
		return buff.toString();
		*/
		
	}


}
