/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.console;

import java.security.KeyStore;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.KeyStoreUtil;


public class KeyStoreAction {
	public static final Logger logger = LogManager.getLogger(KeyStoreAction.class);
	
	private String binary = null;
	private String storePath = null;
	private String basePath = null;
	public KeyStoreAction(String bin, String path){
		binary = bin;
		basePath = path + (path.endsWith("/") ? "" : "/");
		storePath = basePath + "stores/";
	}
	
	public KeyStore getCreateStore(String name, char[] password, boolean isTrust){
		KeyStore store = null;
		String path = storePath + (isTrust ? "trust" : "key") + "/" + name + ".jks";
		/*
		File check = new File(path);
		if(check.exists()){
			logger.warn("Store " + path + " already exists");
			return null;
		}
		*/
		return KeyStoreUtil.getCreateKeyStore(path, password);
	}

	public boolean importCertificate(String storeName, char[] storePassword, boolean isTrust, String alias){
		boolean outBool = false;
		KeyStore store = getCreateStore(storeName, storePassword, isTrust);
		String certPath = basePath + "certificates/signed/" + alias + ".cert";
		String useStorePath = storePath + (isTrust ? "trust" : "key") + "/" + storeName + ".jks";
		byte[] certFile = FileUtil.getFile(certPath);
		if(certFile.length == 0){
			logger.error("Failed to open file: " + certPath);
			return false;
		}
		outBool = KeyStoreUtil.importCertificate(store, certFile, alias);
		if(outBool && KeyStoreUtil.saveKeyStore(store, useStorePath, storePassword)){
			logger.info("Imported certificate " + alias);
		}
 
		return outBool;
	}
	
	public boolean importPKCS12(String storeName, char[] storePassword, boolean isTrust, String alias, char[] password, boolean isPrivate){
		boolean outBool = false;
		KeyStore store = getCreateStore(storeName, storePassword, isTrust);
		String certPath = basePath + "certificates/" + (isPrivate ? "private" : "signed") + "/" + alias + ".p12";
		String useStorePath = storePath + (isTrust ? "trust" : "key") + "/" + storeName + ".jks";
		byte[] p12file = FileUtil.getFile(certPath);
		if(p12file.length == 0){
			logger.error("Failed to open file: " + certPath);
			return false;
		}
		outBool = KeyStoreUtil.importPKCS12(store, p12file, alias, password);
		if(outBool && KeyStoreUtil.saveKeyStore(store, useStorePath, storePassword)){
			logger.info("Imported P12 " + alias);
		}
 
		return outBool;
	}
	
	
	/*
	 * String trustStoreName = "trust." + UUID.randomUUID().toString();
		String trustStorePath = "target/ssl/stores/trust/" + trustStoreName + ".jks";
		char[] trustStorePass = "password".toCharArray();
		String keyStoreName = "key." + UUID.randomUUID().toString();
		String keyStorePath= "target/ssl/stores/key/" + keyStoreName + ".jks";
		char[] keyStorePass = "password".toCharArray();
		KeyStore keyStore = KeyStoreUtil.getCreateKeyStore(keyStorePath,keyStorePass);
		assertNotNull("Keystore is null",keyStore);
		
		byte[] p12file = FileUtil.getFile("target/ssl/certificates/private/" + serverKey + ".p12");
		byte[] certfile = FileUtil.getFile("target/ssl/certificates/signed/" + serverKey + ".cert");
		assertTrue("File could not be found", p12file.length > 0);
		boolean imported = KeyStoreUtil.importPKCS12(keyStore, keyStorePass, p12file, serverKey + ".key", serverKeyPassword);
		assertTrue("Failed to import p12",imported);
		imported = KeyStoreUtil.importCertificate(keyStore, certfile, serverKey);
		assertTrue("Failed to import cert",imported);
		boolean saved = KeyStoreUtil.saveKeyStore(keyStore, keyStorePath, keyStorePass);
		assertTrue("Failed to save key store",saved);
		
		certfile = FileUtil.getFile("target/ssl/certificates/signed/" + serverKey + ".cert");
		KeyStore trustStore = KeyStoreUtil.getCreateKeyStore(trustStorePath,trustStorePass);
		imported = KeyStoreUtil.importCertificate(trustStore, certfile, serverKey);
		KeyStoreUtil.saveKeyStore(trustStore, trustStorePath, trustStorePass);
		Certificate cert = KeyStoreUtil.getCertificate(trustStore, trustStorePass,serverKey);
		assertNotNull("Cert is null",cert);
		Key key = KeyStoreUtil.getKey(keyStore,keyStorePass,serverKey + ".key",serverKeyPassword);
		assertNotNull("Key is null",key);
		PrivateKey pkey = (PrivateKey)key;
		
		SecurityBean bean = new SecurityBean();
		KeyStoreUtil.setPublicKey(cert.getPublicKey(), bean);
		KeyStoreUtil.setPrivateKey(pkey, bean);
	 */
	
	
}
