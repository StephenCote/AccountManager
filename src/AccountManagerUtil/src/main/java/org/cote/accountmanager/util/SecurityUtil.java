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
package org.cote.accountmanager.util;

import java.io.IOException;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.factory.SecurityFactory;


public class SecurityUtil {

	public static final Logger logger = LogManager.getLogger(SecurityUtil.class);

	private static int saltLength = 16;
	private static SecureRandom secureRandom = null;
	private static Random random = null;
	public static final boolean USESECURERANDOM = true;
	
	private static MessageDigest hashAlgorithm = null;
	
	/// Left in place for global static hash operations
	///
	private static String hashProvider = "SHA-512";
	
	private SecurityUtil() {
		
	}
	private static byte[] nextRandom(int length){
		byte[] outByte = new byte[length];
		if(USESECURERANDOM){
			if(secureRandom == null){
				try{
					secureRandom = SecureRandom.getInstance("SHA1PRNG");
				}
				catch(NoSuchAlgorithmException e){
					logger.error(e.getMessage());
				}
			}
			if(secureRandom == null) return new byte[0];
			else secureRandom.nextBytes(outByte);
		}
		else{
			if(random == null){
				random = new Random();
			}
			random.nextBytes(outByte);
		}
		return outByte;
	}
	public static byte[] getRandomSalt(){
		return nextRandom(saltLength);
	}

	

	public static int getSaltLength() {
		return saltLength;
	}
	public static void setSaltLength(int saltLength) {
		SecurityUtil.saltLength = saltLength;
	}
	
	
	public static MessageDigest getHashAlgorithm() {
		return hashAlgorithm;
	}
	public static void setHashAlgorithm(MessageDigest hashAlgorithm) {
		SecurityUtil.hashAlgorithm = hashAlgorithm;
	}
	private static MessageDigest getMessageDigest(){
		return getMessageDigest(false);
	}
	private static MessageDigest getMessageDigest(boolean useSingleton){
		if(useSingleton && hashAlgorithm != null) return hashAlgorithm;
		MessageDigest digest = null;
		try{
			digest = MessageDigest.getInstance(hashProvider);
		}
		catch(NoSuchAlgorithmException e){
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		if(useSingleton && digest != null) hashAlgorithm = digest;
		return digest;
	}
	public static String getDigestAsString(String inStr) {
		return getDigestAsString(inStr.getBytes(), new byte[0]);
	}
	public static String getDigestAsString(byte[] inBytes, byte[] salt){
		return new String(BinaryUtil.toBase64(getDigest(inBytes,salt)));
	}
	public static byte[] getDigest(byte[] inBytes, byte[] salt){
		MessageDigest digest = getMessageDigest();
		if(digest == null) {
			logger.error("Null digest");
			return new byte[0];
		}
		/// 2015/06/23 - Changed for CredentialType updated
		///
		digest.reset();
		digest.update(salt);
		digest.digest(inBytes);
		digest.update(inBytes,0,inBytes.length);
		return digest.digest();
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
		boolean bECD = bean.getCipherKeySpec().startsWith("EC");
		Cipher cipher = SecurityFactory.getSecurityFactory().getDecryptCipherKey(bean);

		if(cipher == null || ((!bECD && bean.getSecretKey() == null) && (bean.getPrivateKey() == null))) {
			logger.error("Expected keys not present");
			if(cipher == null) logger.error("Null cipher");
			return ret;
		}
		try {
			ret = cipher.doFinal(data);
		}
		catch (IllegalBlockSizeException | BadPaddingException e) {
			logger.error(FactoryException.TRACE_EXCEPTION,e);
			logger.error(e.getMessage());
		}

		return ret;
	}
	public static byte[] encipher(SecurityBean bean, byte[] data){
		byte[] ret = new byte[0];
		boolean bECD = bean.getCipherKeySpec().startsWith("EC");
		Cipher cipher = SecurityFactory.getSecurityFactory().getEncryptCipherKey(bean);
		if(cipher == null || ((!bECD && bean.getSecretKey() == null) && (bean.getPublicKey() == null))) {
			logger.error("Expected keys not present");
			if(cipher == null) logger.error("Null cipher");
			return ret;
		}
		try {
			ret = cipher.doFinal(data);
		}
		catch (IllegalBlockSizeException | BadPaddingException e) {
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
			e.printStackTrace();
		} 
		return ret;
	}
	public static byte[] encrypt(SecurityBean bean, byte[] data){
		PublicKey key = bean.getPublicKey();

		byte[] ret = new byte[0];
		if(key == null || data.length == 0){
			String reason = (key == null ? " Null key" : "Null data");
			logger.error(String.format("Invalid parameter: %s",reason));
			return ret;
		}
		try{
			boolean bECD = bean.getAsymmetricCipherKeySpec().startsWith("EC");
			Cipher cipher = Cipher.getInstance((bECD ? bean.getCipherKeySpec() : bean.getAsymmetricCipherKeySpec()));
			if(cipher == null){
				logger.error("Null Cipher");
				return ret;
			}


    	    cipher.init(Cipher.ENCRYPT_MODE, key);
			ret = cipher.doFinal(data);
		}
		catch(Exception e){
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
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
			boolean bECD = bean.getAsymmetricCipherKeySpec().startsWith("EC");
			Cipher cipher = Cipher.getInstance((bECD ? bean.getCipherKeySpec() : bean.getAsymmetricCipherKeySpec()));
    	    cipher.init(Cipher.DECRYPT_MODE, key);
			ret = cipher.doFinal(data);
		}
		catch(Exception e){
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		
		return ret;
	}
	public static String serializeToXml(SecurityBean bean, boolean includePrivateKey, boolean includePublicKey, boolean includeCipher){
		StringBuilder buff = new StringBuilder();
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		String spec = bean.getAsymmetricCipherKeySpec();

		buff.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\r\n<SecurityManager>");
		
		if(spec != null) {
			if(spec.matches("RSA")) {
				if(includePublicKey){
					buff.append("<public><key>" + BinaryUtil.toBase64Str(sf.serializePublicKeyToRSAXml(bean)) + "</key></public>");
				}
				if(includePrivateKey){
					buff.append("<private><key>" + BinaryUtil.toBase64Str(sf.serializePrivateKeyToRSAXml(bean)) + "</key></private>");
				}
			}
			else if(spec.startsWith("EC")) {
				if(includePublicKey){
					try {
						StringWriter writer = new StringWriter();
						PemWriter privateKeyWriter = new PemWriter(writer);
						privateKeyWriter.writeObject(new PemObject("PUBLIC KEY", bean.getPublicKeyBytes()));
						privateKeyWriter.close();
						writer.close();
						buff.append("<public><ec-key>" + BinaryUtil.toBase64Str(writer.toString()) + "</ec-key></public>");

					}
					catch(IOException e) {
						logger.error(e);
					}
				}
				if(includePrivateKey){
					try {
						StringWriter writer = new StringWriter();
						PemWriter privateKeyWriter = new PemWriter(writer);
						privateKeyWriter.writeObject(new PemObject("PRIVATE KEY", bean.getPrivateKeyBytes()));
						privateKeyWriter.close();
						writer.close();
						buff.append("<private><ec-key>" + BinaryUtil.toBase64Str(writer.toString()) + "</ec-key></private>");
					}
					catch(IOException e) {
						logger.error(e);
					}
				}
			}
		}
		if(includeCipher){
			buff.append("<cipher>" + (new String(sf.serializeCipher(bean))) + "</cipher>");
		}
		buff.append("</SecurityManager>\r\n");
		return buff.toString();
		
	}


}
