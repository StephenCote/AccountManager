package org.cote.accountmanager.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Enumeration;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.security.auth.x500.X500Principal;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;

public class KeyStoreUtil {
	
	private KeyStoreUtil(){
		
	}
	
	public static final Logger logger = LogManager.getLogger(KeyStoreUtil.class);

	public static final String PREFERRED_KEYSTORE_PROVIDER = "JKS";


	public static byte[] getKeyStoreBytes(KeyStore store, char[] password) {
	   byte[] outByte = new byte[0];
	   ByteArrayOutputStream baos = new ByteArrayOutputStream();
	   try {
		store.store(baos,password);
		outByte = baos.toByteArray();
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			logger.error(e.getMessage());
			logger.error(e);
		}

	   return outByte;
	}


	public static KeyStore getKeyStore(byte[] keystoreBytes, char[] password) {
		return getKeyStore(keystoreBytes, password, PREFERRED_KEYSTORE_PROVIDER);
	}
	public static KeyStore getKeyStore(byte[] keystoreBytes, char[] password, String provider) {

	      KeyStore store = null;
	      try {
	    	store =  KeyStore.getInstance(PREFERRED_KEYSTORE_PROVIDER);
			store.load(new ByteArrayInputStream(keystoreBytes), password);
		} catch (NoSuchAlgorithmException | CertificateException | IOException | KeyStoreException e) {
			logger.error(e.getMessage());
			logger.error(e);
			store = null;
		} 
	      return store;
  
	}
	public static Certificate decodeCertificate(byte[] certificate){
		Certificate cert = null;
		try{
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			ByteArrayInputStream bais = new ByteArrayInputStream(certificate);
			cert =  cf.generateCertificate(bais);
		}
		catch(CertificateException e){
			logger.error(e.getMessage());
			logger.error(e);
		}
		return cert;
	}
	public static boolean importCertificate(KeyStore store, byte[] certificate, String alias){
		boolean outBool = false;
		logger.info("Import certificate");
		try{
			Certificate cert =  decodeCertificate(certificate);
			store.setCertificateEntry(alias, cert);
			outBool = true;
		}
		catch(KeyStoreException e){
			logger.error(e.getMessage());
			logger.error(e);
		}
		return outBool;
	}
	public static boolean importPKCS12(KeyStore store,  byte[] p12cert, String alias, char[] password){
		KeyStore pkstore = getKeyStore(p12cert,password);
		logger.info("Import PKCS12 Store");
		if(pkstore == null){
			logger.error("PKCS12 store is null");
			return false;
		}
		boolean outBool = false;
		boolean useAlias = false;
		try{
			Enumeration aliases = pkstore.aliases();
		    int n = 0;
		    while (aliases.hasMoreElements()) {
		      String storeAlias = (String)aliases.nextElement();
		         if (pkstore.isKeyEntry(storeAlias)) {

		            Key key = pkstore.getKey(storeAlias, password);
	
		            Certificate[] chain = pkstore.getCertificateChain(storeAlias);
		            if(useAlias == false){
		            	useAlias = true;
		            	storeAlias = alias;
		            }
		            logger.debug("Adding key for alias " + storeAlias);
		            store.setKeyEntry(storeAlias, key, password, chain);

		            outBool = true;
		         }
		    }
		}
		catch(KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e){
			logger.error(e.getMessage());
			logger.error(e);
		}
	    return outBool;

	}
	/*
	 * 
	 * 
	 */
	public static KeyStore getCreateKeyStore(String path, char[] password){
		File check = new File(path);
		if(check.exists() == false){
			KeyStore newStore=null;
			try {
				newStore = KeyStore.getInstance(PREFERRED_KEYSTORE_PROVIDER);
				newStore.load(null, password);
				FileOutputStream fos = new FileOutputStream(path);
				newStore.store(fos, password);
				fos.close();
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
				logger.error(e.getMessage());
				logger.error(e);
			}
		}
		return getKeyStore(path,password);
	}
	
	public static boolean saveKeyStore(KeyStore store, String path, char[] password){
		boolean saved = false;
		try{
			FileOutputStream fos = new FileOutputStream(path);
			store.store(fos, password);
			fos.close();
			saved = true;
		}
		 catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {
			 logger.error(e.getMessage());
			logger.error(e);
			}
		return saved;
	}
	
	public static KeyStore getKeyStore(String path, char[] password){
		File check = new File(path);
		if(check.exists() == false){
			logger.error("Key store does not exist at " + path);
			return null;
		}
		KeyStore keystore = null;
		FileInputStream is = null;
		try{
			
		  is = new FileInputStream(path);
		  keystore = KeyStore.getInstance(PREFERRED_KEYSTORE_PROVIDER);
		  keystore.load(is, password);
		}
		catch(KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e){
			logger.info(e.getMessage());
			logger.error(e);
		}
		finally{
			try {
				if(is != null) is.close();
			}
			catch (IOException e) {
				logger.error(e);
			}
		}
		return keystore;
	}
	public static Certificate getCertificate(String path, char[] password, String keyAlias){
			KeyStore store = getKeyStore(path,password);
			return getCertificate(store, password, keyAlias);
	}

	public static Certificate getCertificate(KeyStore store, char[] password, String keyAlias){
			Certificate cert = null;

		    try {
				cert = store.getCertificate(keyAlias);
			} catch (KeyStoreException e) {
				logger.error(e.getMessage());
				logger.error(e);
			}

		    return cert;
	}
	public static Key getKey(String path, char[] password, String keyAlias, char[] keyPassword){
		KeyStore store = getKeyStore(path,password);
		return getKey(store, password, keyAlias, keyPassword);
	}

	public static Key getKey(KeyStore store, char[] password, String keyAlias, char[] keyPassword){
		Key key = null;

	    
			try {
				key = store.getKey(keyAlias, (keyPassword == null ? password : keyPassword));

				if(key != null){
					Certificate[] certs = store.getCertificateChain(keyAlias);
					logger.info("Alias " + keyAlias + " chain: " + certs.length);
				}
			} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
				logger.error(e.getMessage());
				logger.error(e);
			}
		    return key;
	}
	public static void setPublicKey(PublicKey key, SecurityBean bean){
		if(key == null || bean == null){
			logger.error("Null arguments");
			return;
		}
		bean.setPublicKey(key);
		bean.setPublicKeyBytes(key.getEncoded());
		bean.setAsymmetricCipherKeySpec(key.getAlgorithm());
	}
	public static void setPrivateKey(PrivateKey key, SecurityBean bean){
		if(key == null || bean == null){
			logger.error("Null arguments");
			return;
		}
		bean.setPrivateKey(key);
		bean.setPrivateKeyBytes(key.getEncoded());
		bean.setAsymmetricCipherKeySpec(key.getAlgorithm());
	}
	
	public static String getRdnFromPrincipal(X500Principal principal, String attrName){
		String outVal = null;
		LdapName ldapDN = null;
		try {
			ldapDN = new LdapName(principal.getName(X500Principal.CANONICAL));
		} catch (InvalidNameException e) {
			logger.error(e.getMessage());
			logger.error(e);
		}
		if(ldapDN == null){
			logger.error("Unable to parse DN from '" + principal.getName() + "'");
			return outVal;
		}
		for(Rdn rdn: ldapDN.getRdns()) {
			if(rdn.getType().equals(attrName)){
				outVal = (String)rdn.getValue();
				break;
			}
		}
		return outVal;
	}
}
