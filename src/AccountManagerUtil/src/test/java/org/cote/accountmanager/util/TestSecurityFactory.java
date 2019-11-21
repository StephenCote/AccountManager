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

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class TestSecurityFactory {
	public static final Logger logger = LogManager.getLogger(TestSecurityFactory.class);
	@Before
	public void setUp() throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testEncipherDecipher(){
		String phrase = "password";
		String data = "This is the example data";
		
		byte[] salt = SecurityUtil.getRandomSalt();
		long start = System.currentTimeMillis();
		logger.info("Original: " + data);
		String enc = BinaryUtil.toBase64Str(SecurityUtil.encipher(data.getBytes(), phrase, salt));
		logger.info("Encrypted: " + enc);

		String dec = new String(SecurityUtil.decipher(BinaryUtil.fromBase64(enc.getBytes()), phrase, salt));
		
		logger.info("Decrypted: " + dec);
		logger.debug("Completed: " + (System.currentTimeMillis() - start) + "ms");
	}
	@Test
	public void testCipherCompat(){
		SecurityBean bean = new SecurityBean();
		boolean pass = false;
		try {
			
			KeyGenerator kgen = KeyGenerator.getInstance(bean.getCipherKeySpec());
			kgen.init(bean.getCipherKeySize());

			SecretKey secret_key = kgen.generateKey();
			logger.info(bean.getSymmetricCipherKeySpec());
			Cipher cipher_key = Cipher.getInstance(bean.getSymmetricCipherKeySpec());
			byte[] iv = {
					64, 65, 66, 64, 65, 66, 78, 94,
					64, 65, 66, 64, 65, 66, 78, 94
			};
			if(cipher_key != null){
				int mode = Cipher.ENCRYPT_MODE;
				
				IvParameterSpec iv_spec = new IvParameterSpec(iv);
				cipher_key.init(Cipher.ENCRYPT_MODE, secret_key, iv_spec);
				logger.info("IV=" + (cipher_key.getIV() == null ? " null " : cipher_key.getIV().length));
				pass = true;
			}
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		assertTrue("Didn't pass", pass);
	}
	
	@Test
	public void testGenerateCipher() {
		logger.info("Generating cipher ...");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = new SecurityBean();
		sf.generateSecretKey(bean);
		String test_data = "This is a test";
		byte[] enc = SecurityUtil.encipher(bean, test_data.getBytes());
		logger.info("AES Secret Key: " + BinaryUtil.toBase64(bean.getCipherKey()) + " / IV: " + BinaryUtil.toBase64Str(bean.getCipherIV()));
	}
	
	@Test
	public void testGenerateCipherWithIV() {
		logger.info("Generating cipher ...");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = new SecurityBean();
		byte[] iv = {
				64, 65, 66, 64, 65, 66, 78, 94,
				64, 65, 66, 64, 65, 66, 78, 94
		};
		bean.setCipherIV(iv);
		sf.generateSecretKey(bean);
		String test_data = "This is a test";
		byte[] enc = SecurityUtil.encipher(bean, test_data.getBytes());
	}
	
	@Test
	public void testDecipherString(){
		logger.info("Generating cipher ...");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = new SecurityBean();
		sf.generateSecretKey(bean);
		String test_data = "This is a test";
		byte[] enc = SecurityUtil.encipher(bean, test_data.getBytes());
		String enc_str = new String(BinaryUtil.toBase64(enc));
		logger.info(test_data + "='" + enc_str + "'");
		byte[] dec = SecurityUtil.decipher(bean,enc);
		logger.info((new String(dec)) + " = '" + enc_str + "'");
	}
	
	@Test
	public void testGenerateKeyPair(){
		logger.info("Generating keypair ...");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = new SecurityBean();
		sf.generateKeyPair(bean);
		assertTrue("Public key is null", bean.getPublicKey() != null);
		assertTrue("Private key is null", bean.getPrivateKey() != null);
	}
	@Test
	public void testDecrypt(){
		logger.info("Generating keypair ...");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = new SecurityBean();
		sf.generateKeyPair(bean);
		byte[] data = "Text to encrypt".getBytes();
		byte[] enc = SecurityUtil.encrypt(bean, data);
		///logger.info((new String(BinaryUtil.toBase64(enc))));
		byte[] dec = SecurityUtil.decrypt(bean, enc);
		logger.info((new String(BinaryUtil.toBase64(dec))));
	}
	
	@Test
	public void testSerialize(){
		logger.info("Generating keypair ...");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = new SecurityBean();
		sf.generateKeyPair(bean);
		sf.generateSecretKey(bean);
		///logger.info("Public Key = " + (bean.getPublicKeyBytes() == null ? "0" : bean.getPublicKeyBytes().length));
		///logger.info("Private Key = " + (bean.getPrivateKeyBytes() == null ? "0" : bean.getPrivateKeyBytes().length));
		assertTrue("Public key is null or empty", (bean.getPublicKeyBytes() != null && bean.getPublicKeyBytes().length > 0));
		assertTrue("Private key is null or empty", (bean.getPrivateKeyBytes() != null && bean.getPrivateKeyBytes().length  > 0));
		assertTrue("Cipher key is null or empty", (bean.getCipherKey() != null && bean.getCipherKey().length > 0));
		assertTrue("Cipher IV is null or empty", (bean.getCipherIV() != null && bean.getCipherIV().length > 0));
		
		String serial = SecurityUtil.serializeToXml(bean, true, true, true);
		///logger.info("Java Serial = " + serial);
		String serial2 = SecurityUtil.serializeToXml(bean, false, true, false);
		///logger.info("Java Serial 2 = " + serial2);
	}
	@Test
	public void testSerializeElements(){
		logger.info("Generating keypair ...");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = new SecurityBean();
		sf.generateKeyPair(bean);
		sf.generateSecretKey(bean);
		String serial = SecurityUtil.serializeToXml(bean, true, true, true);
		///logger.info("Java Serial = " + serial);
		Document d = XmlUtil.GetDocumentFromBytes(serial.getBytes());
		assertTrue("Serial document is null", d != null);
		String pubKey = XmlUtil.FindElementText(d.getDocumentElement(), "public", "key");
		assertTrue("Public key not found", pubKey != null);
		//logger.info("Public key = " + BinaryUtil.fromBase64Str(pubKey.getBytes()));
		String priKey = XmlUtil.FindElementText(d.getDocumentElement(), "private", "key");
		assertTrue("Private key not found", priKey != null);
		//logger.info("Private key = " + BinaryUtil.fromBase64Str(priKey.getBytes()));
		String cipKey = XmlUtil.FindElementText(d.getDocumentElement(), "cipher", "key");
		String cipIv = XmlUtil.FindElementText(d.getDocumentElement(), "cipher", "iv");
		assertTrue("Cipher IV not found", cipIv != null);
		assertTrue("Cipher Key not found", cipKey != null);

	}
	@Test
	public void testDeserialConstruct(){
		
		byte[] srcBytes = "Example Text".getBytes();
		
		String key_source = "<?xml version=\"1.0\"?><SecurityManager><public><key><![CDATA[PFJTQUtleVZhbHVlPjxNb2R1bHVzPm5kM2xxMUszVWk1SHc5bnZ0YVd3MVp1eU1sUkJYZHkvVGNWZ0VQaUxzZThwMmRoN3VPUDg4VGVyUjc5NWFwTXJEYmFOQTJleW5ndk9seDUrYWtpWmdMTGtJYTVZVXlvMFZmVnZ3UnU4dm5lb2NSaUluUUIyTUY3THhhUjdRckkxclhIaGhiaHFtaW1wZnYxaE1vbmV3cmZ2WkJIZGt4N3BSLzNCK0FuVGRZaz08L01vZHVsdXM+PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjwvUlNBS2V5VmFsdWU+]]></key></public><private><key><![CDATA[PFJTQUtleVZhbHVlPjxNb2R1bHVzPm5kM2xxMUszVWk1SHc5bnZ0YVd3MVp1eU1sUkJYZHkvVGNWZ0VQaUxzZThwMmRoN3VPUDg4VGVyUjc5NWFwTXJEYmFOQTJleW5ndk9seDUrYWtpWmdMTGtJYTVZVXlvMFZmVnZ3UnU4dm5lb2NSaUluUUIyTUY3THhhUjdRckkxclhIaGhiaHFtaW1wZnYxaE1vbmV3cmZ2WkJIZGt4N3BSLzNCK0FuVGRZaz08L01vZHVsdXM+PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjxQPnlTaE1mRWd2Z21tRHgrYkxoSHdXUUtqQXRTQVdlWnJrU2xESUN0OU1hdTlBWmlJOXJ1Z2ZzcG5ueDZaNnRMWEFTWjNKc2xmUXVtNmZoWmFqOTRBbzhRPT08L1A+PFE+eU9nbGFIZjI2amN2dE9RVXlwQ0E2c3JXSVYzbUVEd3lwcjF0Uk5OUU83cGxxTEUvVmdGMnpVWnlxSm1GQnhYMnh2MHZiaTlVS0ZyM3BmOXlyWGJXR1E9PTwvUT48RFA+R0U4MmJ3NktMMGh4RklkZnNQTU4vV0puWjN3cE95anN6YzVWWG5yOTBTNTRxZDhaZFRtNEd1MWVoVklwSWcyVTMxQ2lQMXM5YmtwUUhPVEhpL0dCQVE9PTwvRFA+PERRPmRxeHFMR053ZnJsS2ZOZWRVR283UEhYRU5zRjRmRzZTbk51WUIrZXFwUjFkbjEvVHdjSHJveVhSNUxXS1ZyMHFvREErTEIvWTNsMmRtM2hoRFFYOVFRPT08L0RRPjxJbnZlcnNlUT51d1l1UkJ2bGlrK3h0dG1TL3V0WTZKOHV0Qnh6cWZpRGRvdXlpelBzSStuWCtMbDNiNUQ0VE13Z2VQV2I5UXVKMXVWUmhITXFCSk9FdXpmWElqdE5NQT09PC9JbnZlcnNlUT48RD5kUWNWQmU4NHRQUllBUWtqV1U0dUMvdnltcnE1Qm1McGNqYTZJM3FNM0dnR1oxYkRTT25DRGZPTnhvOWI2N1NUZXdQei95MDFUVkpGWU9PYkpTRVNvUnB0c3VmSUpvcVcwaCtMWXBaNmszYzd5dnlnTWhvZGtWVTM3TEhkV0xKQXFRMDJUeDc2cWdPSWQ4OGphQzA2RXk2Rk53NFhXM3E0cDNUMlVJMWJuWUU9PC9EPjwvUlNBS2V5VmFsdWU+]]></key></private><cipher><key><![CDATA[vbTSKZ7ss0AQkC6BFYR/vg==]]></key><iv><![CDATA[BvuxGsOWgFnRGBlbfhDbUQ==]]></iv></cipher></SecurityManager>";
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = SecurityFactory.getSecurityFactory().createSecurityBean(key_source.getBytes(), false);
		assertTrue("Bean is null", bean != null);
	
		byte[] encBytes = SecurityUtil.encrypt(bean, srcBytes);
		byte[] decBytes = SecurityUtil.decrypt(bean, encBytes);
		
		String dec = BinaryUtil.fromBase64Str("PFJTQUtleVZhbHVlPjxNb2R1bHVzPm5kM2xxMUszVWk1SHc5bnZ0YVd3MVp1eU1sUkJYZHkvVGNWZ0VQaUxzZThwMmRoN3VPUDg4VGVyUjc5NWFwTXJEYmFOQTJleW5ndk9seDUrYWtpWmdMTGtJYTVZVXlvMFZmVnZ3UnU4dm5lb2NSaUluUUIyTUY3THhhUjdRckkxclhIaGhiaHFtaW1wZnYxaE1vbmV3cmZ2WkJIZGt4N3BSLzNCK0FuVGRZaz08L01vZHVsdXM+PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjwvUlNBS2V5VmFsdWU+".getBytes());
		///logger.info("C# Public Key = " + dec);
		
		String dec_pri = BinaryUtil.fromBase64Str("PFJTQUtleVZhbHVlPjxNb2R1bHVzPm5kM2xxMUszVWk1SHc5bnZ0YVd3MVp1eU1sUkJYZHkvVGNWZ0VQaUxzZThwMmRoN3VPUDg4VGVyUjc5NWFwTXJEYmFOQTJleW5ndk9seDUrYWtpWmdMTGtJYTVZVXlvMFZmVnZ3UnU4dm5lb2NSaUluUUIyTUY3THhhUjdRckkxclhIaGhiaHFtaW1wZnYxaE1vbmV3cmZ2WkJIZGt4N3BSLzNCK0FuVGRZaz08L01vZHVsdXM+PEV4cG9uZW50PkFRQUI8L0V4cG9uZW50PjxQPnlTaE1mRWd2Z21tRHgrYkxoSHdXUUtqQXRTQVdlWnJrU2xESUN0OU1hdTlBWmlJOXJ1Z2ZzcG5ueDZaNnRMWEFTWjNKc2xmUXVtNmZoWmFqOTRBbzhRPT08L1A+PFE+eU9nbGFIZjI2amN2dE9RVXlwQ0E2c3JXSVYzbUVEd3lwcjF0Uk5OUU83cGxxTEUvVmdGMnpVWnlxSm1GQnhYMnh2MHZiaTlVS0ZyM3BmOXlyWGJXR1E9PTwvUT48RFA+R0U4MmJ3NktMMGh4RklkZnNQTU4vV0puWjN3cE95anN6YzVWWG5yOTBTNTRxZDhaZFRtNEd1MWVoVklwSWcyVTMxQ2lQMXM5YmtwUUhPVEhpL0dCQVE9PTwvRFA+PERRPmRxeHFMR053ZnJsS2ZOZWRVR283UEhYRU5zRjRmRzZTbk51WUIrZXFwUjFkbjEvVHdjSHJveVhSNUxXS1ZyMHFvREErTEIvWTNsMmRtM2hoRFFYOVFRPT08L0RRPjxJbnZlcnNlUT51d1l1UkJ2bGlrK3h0dG1TL3V0WTZKOHV0Qnh6cWZpRGRvdXlpelBzSStuWCtMbDNiNUQ0VE13Z2VQV2I5UXVKMXVWUmhITXFCSk9FdXpmWElqdE5NQT09PC9JbnZlcnNlUT48RD5kUWNWQmU4NHRQUllBUWtqV1U0dUMvdnltcnE1Qm1McGNqYTZJM3FNM0dnR1oxYkRTT25DRGZPTnhvOWI2N1NUZXdQei95MDFUVkpGWU9PYkpTRVNvUnB0c3VmSUpvcVcwaCtMWXBaNmszYzd5dnlnTWhvZGtWVTM3TEhkV0xKQXFRMDJUeDc2cWdPSWQ4OGphQzA2RXk2Rk53NFhXM3E0cDNUMlVJMWJuWUU9PC9EPjwvUlNBS2V5VmFsdWU+".getBytes());
		///logger.info("C# Private Key = " + dec_pri);
		
		Document xml = XmlUtil.GetDocumentFromBytes(dec_pri.getBytes());
		byte[] modBytes = BinaryUtil.fromBase64(XmlUtil.GetElementText(xml.getDocumentElement(), "Modulus").getBytes());
		byte[] dBytes = BinaryUtil.fromBase64(XmlUtil.GetElementText(xml.getDocumentElement(), "D").getBytes());
		BigInteger modulus = new BigInteger(1, modBytes);
		BigInteger d = new BigInteger(1, dBytes);
		
		///logger.info("C# modulus = " + modulus);
		///logger.info("C# D = " + d);
		
		String re_enc_pub = SecurityUtil.serializeToXml(bean,true,true,true);
		String pri_key = new String(sf.serializePrivateKeyToRSAXml(bean));
		///logger.info("Java Private Key = " + pri_key);
		
		
		SecurityBean bean2 = SecurityFactory.getSecurityFactory().createSecurityBean(re_enc_pub.getBytes(), false);
		decBytes = SecurityUtil.decrypt(bean2, encBytes);
		
		String decStr = new String(decBytes);
		assertTrue("Source text doesn't match","Example Text".equals(decStr));
		
		/*
		String pri_key2 = new String(sf.serializePrivateKeyToRSAXml(bean2));
		logger.info("Java Private Key 2 = " + pri_key2);
		
		sf.setSecretKey(bean2,  bean2.getCipherKey(), bean2.getCipherKey(), true);
		logger.info("Serialize with encrypted cipher");
		String enc3 = new String(sf.serialize(bean2, true, true, true));
		logger.info("Deserialize with encrypted cipher");
		SecurityBean bean3 = sf.createSecurityBean(enc3.getBytes(), true);
		*/

	}
	@Test
	public void testEncryptedCipher(){
		logger.info("Generating keypair ...");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = new SecurityBean();
		bean.setEncryptCipherKey(true);
		sf.generateKeyPair(bean);
		sf.generateSecretKey(bean);
		String sourceText = "Example text";
		byte[] encBytes = SecurityUtil.encipher(bean, sourceText.getBytes());
		String serial = SecurityUtil.serializeToXml(bean, true, true, true);
		//logger.info(serial);
		SecurityBean bean2 = SecurityFactory.getSecurityFactory().createSecurityBean(serial.getBytes(), true);
		String decText = new String(SecurityUtil.decipher(bean2, encBytes));
		assertTrue("Source text doesn't match", sourceText.equals(decText));
		logger.info("Deciphered test string used deserialized encrypted cipher");
	}
	@Test
	public void testPassKey(){
		SecurityBean bean = new SecurityBean();
		//byte[] passphrase = SecurityUtil.getPassphraseBytes("the password");
		byte[] salt = SecurityUtil.getRandomSalt();
		SecurityFactory.getSecurityFactory().setPassKey(bean, "The password", false);
		String sourceText = "Example text";
		logger.info("Enc Key = " + BinaryUtil.toBase64Str(bean.getCipherKey()));
		logger.info("Enc IV = " + BinaryUtil.toBase64Str(bean.getCipherIV()));

		byte[] encBytes = SecurityUtil.encipher(bean, sourceText.getBytes());
		assertTrue("Enciphered value is empty",encBytes.length > 0);
		
		StringBuffer buff = new StringBuffer();
		for(int i = 0; i < salt.length;i++){
			if(i > 0) buff.append(",");
			buff.append(salt[i]);
		}
		logger.info(buff.toString());
		
		SecurityBean dec_bean = new SecurityBean();
		SecurityFactory.getSecurityFactory().setPassKey(dec_bean, "The password", false);
		logger.info("Dec Key = " + BinaryUtil.toBase64Str(dec_bean.getCipherKey()));
		logger.info("Dec IV = " + BinaryUtil.toBase64Str(dec_bean.getCipherIV()));
		byte[] decBytes = SecurityUtil.decipher(dec_bean, encBytes);
		String outText = new String(decBytes);
		assertTrue("The decrypted text does not match the input text", outText.equals(sourceText));
		logger.info("Deciphered: " + outText);
	}

	/*
	@Test
	public void testSharedPassKey(){
		SecurityBean bean = new SecurityBean();
		byte[] passphrase = BinaryUtil.fromBase64("dGRwVmxOMnZaVTBVNHBoWQ==".getBytes());
		//SecurityUtil.getPassphraseBytes("the password");
		SecurityFactory.getSecurityFactory().setPassKey(bean, passphrase, false);
		
		logger.info("Key=" + BinaryUtil.toBase64Str(bean.getCipherKey()) + " / " + BinaryUtil.toBase64Str(bean.getCipherIV()));
		//for(int i = 0; i < bean.getCipherKey().length; i++) logger.info(bean.getCipherKey()[i]);
		
		String sourceText = "Example text";
		byte[] encBytes = SecurityUtil.encipher(bean, sourceText.getBytes());
		logger.info("Enc=" + BinaryUtil.toBase64Str(encBytes) + " from " + encBytes.length + " bytes");
		logger.info("Base 64 test: " + BinaryUtil.toBase64Str(sourceText));
		//for(int i = 0; i < encBytes.length;i++) logger.info("Byte: " + (char)encBytes[i]);
		logger.info(bean.getSecretKey().getAlgorithm());
		logger.info(bean.getSecretKey().getFormat());
		
		SecurityBean dec_bean = new SecurityBean();
		SecurityFactory.getSecurityFactory().setPassKey(dec_bean, passphrase, false);
	
		SecurityBean decBean = new SecurityBean();
		byte[] key = BinaryUtil.fromBase64("+nurnv86BqUnHM+5tthyjQ==".getBytes());
		byte[] iv = BinaryUtil.fromBase64("DHozTXSLAwce799T6nXDdA==".getBytes());
		byte[] enc = BinaryUtil.fromBase64("QFt5k19VAzqtd5TzZdqsB3f9Qp+hVi+xQE/GJKNkgQ0=".getBytes());
		SecurityFactory.getSecurityFactory().setSecretKey(decBean, key, iv, false);
		String inText = new String(SecurityUtil.decipher(decBean, enc));
		logger.info(inText);

	}
	*/
	@Test
	public void testSharedPassKey2(){
		SecurityBean bean = new SecurityBean();
		//byte[] passphrase = BinaryUtil.fromBase64("dGRwVmxOMnZaVTBVNHBoWQ==".getBytes());
		///byte[] passphrase = SecurityUtil.getPassphraseBytes("the password");
		String passphrase = "password password";
		byte[] salt = SecurityUtil.getRandomSalt();
		logger.info("PASSPHRASE = " + new String(passphrase));
		
		SecurityFactory.getSecurityFactory().setPassKey(bean, passphrase, salt,false);
		
		logger.info("Key=" + BinaryUtil.toBase64Str(bean.getCipherKey()) + " / " + BinaryUtil.toBase64Str(bean.getCipherIV()));
		//for(int i = 0; i < bean.getCipherKey().length; i++) logger.info(bean.getCipherKey()[i]);
		
		String sourceText = "Example text";
		byte[] encBytes = SecurityUtil.encipher(bean, sourceText.getBytes());
		logger.info("Enc=" + BinaryUtil.toBase64Str(encBytes) + " from " + encBytes.length + " bytes");
		logger.info("Base 64 test: " + BinaryUtil.toBase64Str(sourceText));
		//for(int i = 0; i < encBytes.length;i++) logger.info("Byte: " + (char)encBytes[i]);
		logger.info(bean.getSecretKey().getAlgorithm());
		logger.info(bean.getSecretKey().getFormat());
		
		SecurityBean dec_bean = new SecurityBean();
		SecurityFactory.getSecurityFactory().setPassKey(dec_bean, passphrase, salt,false);
		
		//logger.info("Example enc: " + BinaryUtil.toBase64Str(SecurityUtil.encipher(dec_bean,  "example".getBytes())));
		//5/u/g9Qdn3EVj9Y6g85Z+A==
		//w6fDu8K/woPDlB3Cn3EVwo/DljrCg8OOWcO4
		
		
		SecurityBean decBean = new SecurityBean();
		byte[] key = BinaryUtil.fromBase64("+nurnv86BqUnHM+5tthyjQ==".getBytes());
		byte[] iv = BinaryUtil.fromBase64("DHozTXSLAwce799T6nXDdA==".getBytes());
		byte[] enc = BinaryUtil.fromBase64("QFt5k19VAzqtd5TzZdqsB3f9Qp+hVi+xQE/GJKNkgQ0=".getBytes());
		SecurityFactory.getSecurityFactory().setSecretKey(decBean, key, iv, false);
		String inText = new String(SecurityUtil.decipher(decBean, enc));
		logger.info(inText);

	}
	
}
