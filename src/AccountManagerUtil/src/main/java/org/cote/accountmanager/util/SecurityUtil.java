/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;

import org.apache.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.factory.SecurityFactory;


public class SecurityUtil {

	public static final Logger logger = Logger.getLogger(SecurityUtil.class.getName());
	/// TODO: 2015/06/23 - Need to refactor salt references to use a CredentialType
	///
	private static int SALT_LENGTH = 16;
	private static SecureRandom random = null;
	static{
		try{
			long start = System.currentTimeMillis();
			random = SecureRandom.getInstance("SHA1PRNG");
			logger.debug("Secure Random: " + (System.currentTimeMillis() - start) + "ms");
		}
		catch(NoSuchAlgorithmException e){
			logger.error(e.getMessage());
		}
	}
			//new SecureRandom("NativePRNG");
	
	private static MessageDigest hash_algorithm = null;
	
	/// TODO: For CredentialType update, this will go away
	///
	private static String HASH_PROVIDER = "SHA-256";
	
	/// TODO: For CredentialType update, this will go away
	///
	private static String HASH_SALT = "aostnh234stnh;qk234;2354!@#$%10";
	
	public static byte[] getRandomSalt(){
		byte[] salt = new byte [SALT_LENGTH];
	    random.nextBytes (salt);
	    return salt;
	}
	

	/// TODO: For CredentialType update, this will go away
	///
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

	public static byte[] getDigest(byte[] in_bytes, byte[] salt){
		MessageDigest digest = getMessageDigest();
		/// 2015/06/23 - Changed for CredentialType updated
		///
		digest.reset();
		//if(salt.length > 0){
			digest.update(salt);
		//}
		digest.digest(in_bytes);
		digest.update(in_bytes,0,in_bytes.length);
		return digest.digest();
	}
	public static byte[] getDigestOLDOLD(byte[] in_bytes, byte[] salt){
		MessageDigest digest = getMessageDigest();
		logger.warn("Refactor this method - it uses the old salt method");
		digest.update(in_bytes,0,in_bytes.length);
		return digest.digest();

	}
	public static String getDigestAsString(byte[] in_bytes){
		return new String(BinaryUtil.toBase64(getDigestOLDOLD(in_bytes,new byte[0])));
	}
	public static String getDigestAsString(String in_str){
		byte[] digest = getDigestOLDOLD(in_str.getBytes(),new byte[0]);
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
		long start_enc = System.currentTimeMillis();
		byte[] ret = new byte[0];
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
			e.printStackTrace();
			logger.error(e.getMessage());
		} catch (BadPaddingException e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		logger.debug("Deciphered in " + (System.currentTimeMillis() - start_enc) + "ms");
		return ret;
	}
	public static byte[] encipher(SecurityBean bean, byte[] data){
		long start_enc = System.currentTimeMillis();
		byte[] ret = new byte[0];
		Cipher cipher = SecurityFactory.getSecurityFactory().getEncryptCipherKey(bean);;
		if(cipher == null || bean.getSecretKey() == null )
			return ret;
		try {
			ret = cipher.doFinal(data);
		}
		catch (IllegalBlockSizeException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (BadPaddingException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} 
		logger.debug("Enciphered in " + (System.currentTimeMillis() - start_enc) + "ms");
		return ret;
	}
	public static byte[] encrypt(SecurityBean bean, byte[] data){
		PublicKey key = bean.getPublicKey();

		byte[] ret = new byte[0];
		if(key == null || data.length == 0){
			logger.error("Invalid parameter - " + (key == null ? " Null key" : "Null data"));
			return ret;
		}
		try{
			Cipher cipher = Cipher.getInstance(bean.getAsymmetricCipherKeySpec());
			if(cipher == null){
				logger.error("Null Cipher");
				return ret;
			}


    	    cipher.init(Cipher.ENCRYPT_MODE, key);
			ret = cipher.doFinal(data);
		}
		catch(Exception e){
			logger.error(e.getMessage());
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
			Cipher cipher = Cipher.getInstance(bean.getAsymmetricCipherKeySpec());
    	    cipher.init(Cipher.DECRYPT_MODE, key);
			ret = cipher.doFinal(data);
		}
		catch(Exception e){
			logger.error(e.getMessage());
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
		
	}


}
