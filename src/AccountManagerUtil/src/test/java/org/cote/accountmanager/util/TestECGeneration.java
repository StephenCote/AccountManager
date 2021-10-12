package org.cote.accountmanager.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Base64Util;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.util.Arrays;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.SecurityType;
import org.junit.Before;
import org.junit.Test;

public class TestECGeneration {
	
	public static final Logger logger = LogManager.getLogger(TestSecurityFactory.class);
	@Before
	public void setUp() throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

	}
	
	@Test
	public void TestEC256() {
		SecurityBean bean = new SecurityBean();
		bean.setKeyAgreementSpec("ECDH");
		bean.setCipherKeySpec("AES/GCM/NoPadding");
		bean.setAsymmetricCipherKeySpec("ECIES");
		bean.setHashProvider("SHA256withECDSA");
		bean.setCurveName("secp256k1");
		bean.setEncryptCipherKey(true);
		bean.setReverseEncrypt(false);
		logger.info("Testing ES256 generation");
		SecurityFactory.getSecurityFactory().generateKeyPair(bean);

	}
	
	@Test
	public void TestGeneration() {
		logger.info("Testing generation");
		
		KeyPair keyPair = null;
		/// ECDSA = Signature
		/// ECDH = Key Exchange
		/// ECIES = Encryption
		try{
			/// asym cipher spec
	        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECIES");
	        /// curve name
        	ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1");
	        keyGen.initialize(ecSpec, new SecureRandom());
        	keyPair = keyGen.generateKeyPair();
		}
		catch(Exception e){
			logger.error(e);
			e.printStackTrace();
		}
		assertNotNull("KeyPair is null", keyPair);
		SecretKey secretKey = null;
		try {
				/// keyagree spec
				KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
				keyAgreement.init(keyPair.getPrivate());
				keyAgreement.doPhase(keyPair.getPublic(), true);
				/// asym cipher spec->symmetric cipher
			    secretKey = keyAgreement.generateSecret("ECIES");
		}
	    catch (NoSuchAlgorithmException | InvalidKeyException e) {
			logger.error(e.getMessage());
			logger.error(e);
			e.printStackTrace();
		}
		assertNotNull("Secret key is null", secretKey);

		Cipher cipherKey = null;
       try {
    	/// cipher spec
		cipherKey = Cipher.getInstance("AES/GCM/NoPadding");
		cipherKey.init(Cipher.ENCRYPT_MODE,  secretKey);

       }
       catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			logger.error(e.getMessage());
			logger.error(e);
			e.printStackTrace();
		}
       assertNotNull("Cipher is null", cipherKey);
       
       String dataPlain = "The test data";
       byte[] encData1 = new byte[0];
       try {
    	   encData1 = cipherKey.doFinal(dataPlain.getBytes());
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       assertTrue("Expected enciphered data", encData1.length > 0);
       
		byte[] encData2 = new byte[0];
		try {
			Cipher cipher = Cipher.getInstance("ECIES");
		    cipher.init(Cipher.ENCRYPT_MODE, keyPair.getPublic());
			encData2 = cipher.doFinal(dataPlain.getBytes());
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Expected encrypted data", encData2.length > 0);

	}
	
	@Test
	public void TestGenerationWithBean() {
		SecurityBean bean = new SecurityBean();
		bean.setKeyAgreementSpec("ECDH");
		bean.setCipherKeySpec("AES/GCM/NoPadding");
		bean.setAsymmetricCipherKeySpec("ECIES");
		bean.setHashProvider("SHA256withECDSA");
		bean.setCurveName("secp256r1");
		bean.setEncryptCipherKey(true);
		bean.setReverseEncrypt(false);
		logger.info("Testing modified bean initialization");
		SecurityFactory.getSecurityFactory().generateKeyPair(bean);
		SecurityFactory.getSecurityFactory().generateSecretKey(bean);
	}
	@Test
	public void TestCurveWithSymmetricCipher() {
		/*
		 * TODO: Need to refactor the security factory so that:
		 * 	Asym = ECDSA
		 *  KeyAgreement = ECDH
		 *  KeyGen = AES
		 *  Cipher = ECIES
		 *  Sym = AES/GCM/NoPadding
		 *  
		 *  Because the SecurityType and factories are overloaded, and there's a lot of legacy code in there, the whole setup likely needs to be refactored to better support this and simplify the de/serialization
		 *  
		 *  At the moment the current arrangement works with EC, but the factory fails on encrypting the symmetric key (secretkey) due to the arrangement of the cipher references using asymmetriccipherkeyspec, cipherkeyspec, and symmetriccipherkeyspec
		 */
		/*
		SecurityBean bean = new SecurityBean();
		bean.setCipherKeySpec("ECDH");
		bean.setSymmetricCipherKeySpec("AES/GCM/NoPadding");
		bean.setAsymmetricCipherKeySpec("ECDSA");
		bean.setHashProvider("SHA256withECDSA");
		bean.setCurveName("secp256r1");
		bean.setEncryptCipherKey(true);
		bean.setReverseEncrypt(false);
		SecurityFactory.getSecurityFactory().generateKeyPair(bean);
		SecurityFactory.getSecurityFactory().generateSecretKey(bean);

		
		assertNotNull("Key is null", bean.getSecretKey());
		*/
/*
 * KeyGenerator kgen = null;
		SecretKey key = null;
		try {
				KeyAgreement keyAgreement = KeyAgreement.getInstance(bean.getCipherKeySpec(), bean.getCipherProvider());
		      keyAgreement.init(bean.getPrivateKey());
		      keyAgreement.doPhase(bean.getPublicKey(), true);

		    key = keyAgreement.generateSecret("AES");
		} catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 */
		
	}

	@Test
	public void testECDSACipher() {
		logger.info("Testing ECIES as a Cipher");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = new SecurityBean();
		bean.setCipherKeySpec("ECIES");
		bean.setKeyAgreementSpec("ECDH");
		bean.setAsymmetricCipherKeySpec("ECIES");
		bean.setHashProvider("SHA256withECDSA");
		bean.setCurveName("secp256r1");
		boolean generated = sf.generateKeyPair(bean);
		assertTrue("Failed to generate secret", generated);
		StringBuilder buff = new StringBuilder();
		for(int i = 0; i < 1000; i++) {
			buff.append(UUID.randomUUID());
		}
		String test_data = buff.toString();
		byte[] enc = SecurityUtil.encipher(bean, test_data.getBytes());
		
		assertTrue("Expected an enciphered value", enc.length > 0);
		
		byte[] dec = SecurityUtil.decipher(bean, enc);
		
		String test_data2 = new String(dec);
		assertTrue("Strings don't match", test_data2.equals(test_data));
		///logger.info("ECIES " + test_data + "='" + enc_str + "'");
	}
	
	@Test
	public void testECDSAKeyPair() {
		logger.info("Testing EC Serialization");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean bean = new SecurityBean();
		bean.setAsymmetricCipherKeySpec("ECIES");
		//bean.setSymmetricCipherKeySpec("ECIES");
		bean.setKeyAgreementSpec("ECDH");
		///bean.setCipherKeySpec("AES/GCM/NoPadding");
		bean.setSymmetricCipherKeySpec("AES/CBC/PKCS5Padding");
		bean.setHashProvider("SHA256withECDSA");
		bean.setCurveName("secp256r1");
		/// bean.setKeySize(571);
		logger.info("Generating keypair");
		boolean generated = sf.generateKeyPair(bean);
		
		assertTrue("Failed to generate ECIES Key Pair",generated);
		logger.info("Generating secret");
		generated = sf.generateSecretKey(bean);
		assertTrue("Failed to generate AES Key",generated);
		assertNotNull("Expected a secret key", bean.getSecretKey());
		String test_data = "This is a test";
		logger.info("Testing encrypt");
		byte[] enc = SecurityUtil.encrypt(bean, test_data.getBytes());
		logger.info("Testing encipher");
		String enc_str = new String(BinaryUtil.toBase64(enc));
		byte[] enc3 = SecurityUtil.encipher(bean, test_data.getBytes());
		String enc3_str = new String(BinaryUtil.toBase64(enc3));
		logger.info("ECIES " + test_data + "='" + enc_str + "' / Enc Secret=" + bean.getEncryptCipherKey());
		
		/// TODO: Current issue is with the encoding around UTF_8
		
		/*
		String base64 = BinaryUtil.toBase64Str(bean.getCipherIV());
		byte[] check = BinaryUtil.fromBase64(base64.getBytes(StandardCharsets.UTF_8));
		assertTrue("Ivs don't match", Arrays.areEqual(bean.getCipherIV(),  check));
		*/
		String serialized = SecurityUtil.serializeToXml(bean, true, true, true);
		logger.info("Serialized: " + serialized);
		SecurityBean bean2 = new SecurityBean();
		bean2.setKeyAgreementSpec("ECDH");
		// bean2.setCipherKeySpec("AES/GCM/NoPadding");
		bean.setSymmetricCipherKeySpec("AES/CBC/PKCS5Padding");
		bean2.setAsymmetricCipherKeySpec("ECIES");
		bean2.setHashProvider("SHA256withECDSA");
		bean2.setCurveName("secp256r1");
		/// bean2.setKeySize(571);
		
		try {
			sf.importSecurityBean(bean2, serialized.getBytes(StandardCharsets.UTF_8), false);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		assertTrue("Expected keys to match", Arrays.areEqual(bean2.getPublicKeyBytes(), bean.getPublicKeyBytes()));
		assertNotNull("Expected a secret key", bean2.getSecretKey());
		/// assertTrue("Expected iv to match", Arrays.areEqual(bean2.getCipherIV(), bean.getCipherIV()));
		/// logger.info(BinaryUtil.toBase64Str(bean.getCipherIV()) + " == " + BinaryUtil.toBase64Str(bean2.getCipherIV()));
		String test_data_mark = "More test data";
		
		
		Cipher cipherKey = null;
		byte[] encX = new byte[0];
		try {
			cipherKey = Cipher.getInstance(bean2.getSymmetricCipherKeySpec());
			///IvParameterSpec iv = new IvParameterSpec(bean2.getCipherIV());
			///GCMParameterSpec iv = new GCMParameterSpec(bean2.getCipherIV().length, bean2.getCipherIV());
			cipherKey.init(Cipher.ENCRYPT_MODE,  bean2.getSecretKey());

			encX = cipherKey.doFinal(test_data_mark.getBytes());
			logger.info("Test enc: " + encX.length);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull("Cipher is null", cipherKey);
		
		logger.info("Bean 2: " + bean2.getCipherKeySpec());
		
		byte[] enc2 = SecurityUtil.encipher(bean2, test_data_mark.getBytes());
		byte[] dec = SecurityUtil.decrypt(bean2,enc);
		String test_data2 = new String(dec);
		assertTrue("Strings don't match",test_data2.equals(test_data));
		logger.info("ECDSA " + test_data2 + " = '" + enc_str + "'");
		byte[] dec2 = SecurityUtil.decipher(bean2, enc2);
		String test_data_mark2 = new String(dec2);
		assertTrue("Strings 2 don't match", test_data_mark.equals(test_data_mark2));
	}
}
