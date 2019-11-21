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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.junit.Before;
import org.junit.Test;

public class TestOpenSSLUtil {
	public static final Logger logger = LogManager.getLogger(TestOpenSSLUtil.class);
	
	@Before
	public void setUp() throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

	}
	
	private boolean generateCA(OpenSSLUtil util, String alias, char[] password){

		return (
				util.generateRootCertificate(alias, password, util.getDefaultDN(alias), 720)
				&& util.exportPKCS12PrivateCertificate(alias, password,null)
				&& util.exportPKCS12PublicCertificate(alias, password)
			);
	}
	
	private boolean generateSignedCert(OpenSSLUtil util, String alias, char[] password, String signerAlias, char[] signerPassword){
		return (
				util.generateCertificateRequest(alias, password, util.getDefaultDN(alias), 720)
				&& util.signCertificate(alias, signerAlias, 720)
				&& util.amendCertificateChain(alias, signerAlias)
				&& util.exportPKCS12PrivateCertificate(alias, password, signerAlias)
				&& util.exportPKCS12PublicCertificate(alias, password)

		);
	}
	
	/// Yes, it's one big massive test here - needs to be split up
	/// 
	@Test
	public void TestOpenSSL(){
		
		OpenSSLUtil util = new OpenSSLUtil("openssl","./target/ssl/");
		boolean conf = util.configure();
		assertTrue("Path not configured", conf);

		String rootKey = "root." + UUID.randomUUID().toString();
		char[] rootKeyPassword = "password".toCharArray();
		String serverKey = "server." + UUID.randomUUID().toString();
		char[] serverKeyPassword = "password".toCharArray();
		String userKey = "user." + UUID.randomUUID().toString();
		char[] userKeyPassword = "password".toCharArray();

		
		
		logger.info("Generating new root certificate: " + rootKey);
		/*
		boolean proc = util.generateKeyPair(rootKey, rootKeyPassword);
		assertTrue("Failed to run process", proc);
		boolean expPriv = util.exportPrivateKey(rootKey, rootKeyPassword);
		assertTrue("Failed to export private key", expPriv);
		boolean expPub = util.exportPublicKey(rootKey, rootKeyPassword);
		assertTrue("Failed to export public key", expPub);
		*/
		boolean genRootCert = util.generateRootCertificate(rootKey, rootKeyPassword, util.getDefaultDN(rootKey), 720);
		assertTrue("Failed to generate root cert",genRootCert);
		boolean exportP12Private = util.exportPKCS12PrivateCertificate(rootKey, rootKeyPassword,null);
		assertTrue("Failed to export P12",exportP12Private);
		boolean exportP12Public = util.exportPKCS12PublicCertificate(rootKey, rootKeyPassword);
		assertTrue("Failed to export P12",exportP12Public);
		boolean genCSR = util.generateCertificateRequest(serverKey, serverKeyPassword, util.getDefaultDN(serverKey), 720);
		assertTrue("Failed to generate csr",genCSR);
		boolean signed = util.signCertificate(serverKey, rootKey, 720);
		assertTrue("Failed to sign csr",signed);
		boolean amend = util.amendCertificateChain(serverKey, rootKey);
		assertTrue("Failed to amend chain",amend);
		exportP12Private = util.exportPKCS12PrivateCertificate(serverKey, serverKeyPassword, rootKey);
		assertTrue("Failed to export P12",exportP12Private);
		exportP12Public = util.exportPKCS12PublicCertificate(serverKey, serverKeyPassword);
		assertTrue("Failed to export P12",exportP12Public);
		
		String trustStoreName = "trust." + UUID.randomUUID().toString();
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
		boolean imported = KeyStoreUtil.importPKCS12(keyStore,  p12file, serverKey + ".key", serverKeyPassword);
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
		
		String test = "This is the text";
		String enc = BinaryUtil.toBase64Str(SecurityUtil.encrypt(bean, test.getBytes()));
		
		logger.info("Enc: " + enc);
		String dec = new String(SecurityUtil.decrypt(bean, BinaryUtil.fromBase64(enc.getBytes())));
		logger.info("Dec: " + dec);
	}
	
}
