package org.cote.accountmanager.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.beans.SecurityBean;
import org.junit.Before;
import org.junit.Test;

public class TestKeyStoreUtil {
	
	/*
	 * NOTE: Java Key Store passwords are for verification only
	 * Hence why it's necessary to password protect the private keys inside the stores
	 * 
	 * NOTE: The ssl scripts wrap openssl and keytool for some automation.
	 * The DN is presently hard coded into the generate.sh script, and needs to be pulled out.
	 * 
	 * Alternately, a stronger store could be used
	 */
	
	public static final Logger logger = Logger.getLogger(TestKeyStoreUtil.class.getName());
	private static String sslBasePath = "/Users/Steve/Projects/Source/ssl";
	private static String keyStorePath = sslBasePath + "/stores/key/apis.jks";
	private static String keyStorePass = "password";
	private static String trustStorePath = sslBasePath + "/stores/trust/apis.jks";
	private static String trustStorePass = "password";
	private static String signerAlias = "signer";
	private static String signerKeyPass = "password1";
	
	@Before
	public void setUp() throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}
	}
	
	
	
	private Certificate getCertificate(String path, char[] password, String alias){
		Certificate cert = null;
		KeyStore store = KeyStoreUtil.getKeyStore(path, password);
		if(store == null) return cert;
		return KeyStoreUtil.getCertificate(store, password,alias);
	}
	/*
	@Test
	public void TestGenSignCert(){
		String testUserName = "TestUser-" + UUID.randomUUID().toString();
		String testUserPassword = "password123";
		//String cmd = "./generate.sh " + testUserName + " " + testUserName + " " + testUserPassword + " signer password1";
		String[] cmd = new String[]{
			"./generate.sh",
			testUserName,
			testUserName,
			testUserPassword,
			signerAlias,
			signerKeyPass
		};
		boolean processCompleted = false;
		boolean certificateCreated = false;
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(new File(sslBasePath + "/"));
		try{
			final Process proc = pb.start();
	
		    BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		    String line;
		    while ((line = br.readLine()) != null) {
		      System.out.println(line);
		    }
		    br.close();
		    processCompleted = true;
		    File check = new File(sslBasePath + "/certificates/signed/" + testUserName + ".cert");
		    certificateCreated = check.exists();
		}
		catch(IOException e){
			logger.error(e.getMessage());
		}
		assertTrue("Process failed",processCompleted);
		assertTrue("Certificate was not created",certificateCreated);
	  }
*/
	

	
	@Test
	public void TestSerialization(){
		KeyStore store = KeyStoreUtil.getKeyStore(keyStorePath, keyStorePass.toCharArray());
		assertNotNull("Store is null",store);
		byte[] storeBytes = KeyStoreUtil.getKeyStoreBytes(store, keyStorePass.toCharArray());
		assertTrue("Bytes are empty",storeBytes.length > 0);
		store = KeyStoreUtil.getKeyStore(storeBytes, keyStorePass.toCharArray());
		assertNotNull("Restored store is null",store);
	}
	
	@Test
	public void TestLoadStore(){
		KeyStore store = KeyStoreUtil.getKeyStore(keyStorePath, keyStorePass.toCharArray());
		assertNotNull("Store is null",store);
		Certificate cert = getCertificate(keyStorePath, keyStorePass.toCharArray(),"bob");
		assertNotNull("Cert is null",cert);
		
		//PublicKey key = cert.getPublicKey();
		
	}
	
	@Test
	public void TestValidation(){
		logger.info("Testing that the provided client certificate is valid");
		Certificate cert = getCertificate(keyStorePath,keyStorePass.toCharArray(),"bob");
		assertNotNull("Certificate is null", cert);
		X509Certificate xcert = (X509Certificate)cert;
		String certSignerName = KeyStoreUtil.getRdnFromPrincipal(xcert.getIssuerX500Principal(), "cn");
		Certificate certSigner = getCertificate(trustStorePath,trustStorePass.toCharArray(),certSignerName);
		assertNotNull("Signer certificate is null", certSigner);
		boolean ver = false;
		try {
			cert.verify(certSigner.getPublicKey());
			ver = true;
		} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Certificate not verified",ver);
	}


	@Test
	public void TestLoadP12Store(){
		KeyStore store = KeyStoreUtil.getKeyStore(trustStorePath, trustStorePass.toCharArray());
		assertNotNull("Store is null",store);
		Certificate cert = KeyStoreUtil.getCertificate(store, trustStorePass.toCharArray(),"bob");
		assertNotNull("Cert is null",cert);
		Key key = KeyStoreUtil.getKey(store,trustStorePass.toCharArray(),"bob.key","password1".toCharArray());
		assertNotNull("Key is null",key);
		PrivateKey pkey = (PrivateKey)key;
		Certificate cert2 = getCertificate(keyStorePath,keyStorePass.toCharArray(),"bob");
		assertNotNull("Certificate is null", cert2);
		
		SecurityBean bean = new SecurityBean();
		KeyStoreUtil.setPublicKey(cert2.getPublicKey(), bean);
		KeyStoreUtil.setPrivateKey(pkey, bean);
		
		String test = "This is the text";
		String enc = BinaryUtil.toBase64Str(SecurityUtil.encrypt(bean, test.getBytes()));
		
		logger.info("Enc: " + enc);
		String dec = new String(SecurityUtil.decrypt(bean, BinaryUtil.fromBase64(enc.getBytes())));
		logger.info("Dec: " + dec);
		
	}
	
	
}
