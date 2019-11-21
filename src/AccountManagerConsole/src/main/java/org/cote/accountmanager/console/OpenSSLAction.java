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
package org.cote.accountmanager.console;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.OpenSSLUtil;

public class OpenSSLAction {
	public static final Logger logger = LogManager.getLogger(OpenSSLAction.class);
	private String binary = null;
	private String caPath = null;
	private OpenSSLUtil sslUtil = null;
	public OpenSSLAction(String bin, String path){
		binary = bin;
		caPath = path + (path.endsWith("/") ? "" : "/");
		sslUtil = new OpenSSLUtil(binary, caPath);
		sslUtil.configure();
	}
	/*
	 * boolean genCSR = util.generateCertificateRequest(serverKey, serverKeyPassword, util.getDefaultDN(serverKey), 720);
		assertTrue("Failed to generate csr",genCSR);
		boolean signed = util.signCertificate(serverKey, rootKey, 720);
		assertTrue("Failed to sign csr",signed);
		boolean amend = util.amendCertificateChain(serverKey, rootKey);
		assertTrue("Failed to amend chain",amend);
		exportP12Private = util.exportPKCS12PrivateCertificate(serverKey, serverKeyPassword, rootKey);
		assertTrue("Failed to export P12",exportP12Private);
		exportP12Public = util.exportPKCS12PublicCertificate(serverKey, serverKeyPassword);
		assertTrue("Failed to export P12",exportP12Public);
	 */
	public byte[] getCertificate(String alias, boolean isPrivate){
		String path = caPath + "/certificates/" + (isPrivate ? "private" : "signed") + "/" + alias + "." + (isPrivate ? "p12" : "cert");
		return FileUtil.getFile(path);
	}
	public boolean signCertificate(String alias, String signerAlias, int expiryDays){
		logger.info("Signing " + alias + " with " + signerAlias + " ...");
		boolean outBool = (
				sslUtil.signCertificate(alias, signerAlias, expiryDays)
				&&
				sslUtil.amendCertificateChain(alias, signerAlias)
			);
		if(outBool) logger.info("Completed signing");
		return outBool;
		

	}
	public boolean exportPKCS12Certificate(String alias, char[] password, String signer){
		logger.info("Exporting PKCS12 Certificate " + alias + " ...");
		boolean outBool = false;
		boolean exportP12Private = sslUtil.exportPKCS12PrivateCertificate(alias, password,signer);
		if(!exportP12Private){
			logger.error("Failed to export private key");
			return outBool;
		}

		boolean exportP12Public = sslUtil.exportPKCS12PublicCertificate(alias, password);
		if(!exportP12Public){
			logger.error("Failed to export public key");
			return outBool;
		}
		outBool = true;
		logger.info("Exported PKCS12 Certificates");
		return outBool;
	}
	public boolean generateCertificateRequest(String alias, String dn, char[] password, int expiryDays){
		logger.info("Generating request " + alias + " ...");
		if(sslUtil.generateCertificateRequest(alias, password, (dn != null ? dn : sslUtil.getDefaultDN(alias)), expiryDays)){
			logger.info("Generated request for " + alias	 );
			return true;
		}
		return false;
		
	};
	public boolean generateRootCertificate(String alias, String dn, char[] password, int expiryDays){
		boolean outBool = false;
		logger.info("Generating " + alias + " ...");
		boolean genRootCert = sslUtil.generateRootCertificate(alias, password, (dn == null ? sslUtil.getDefaultDN(alias) : dn), expiryDays);
		if(!genRootCert){
			logger.error("Failed to generate root certificate");
			return outBool;
		}
		outBool = exportPKCS12Certificate(alias, password, null);
		/*
		 * logger.info("Exporting Keys " + alias + " ...");
		boolean exportP12Private = sslUtil.exportPKCS12PrivateCertificate(alias, password,null);
		if(!exportP12Private){
			logger.error("Failed to export private key");
			return outBool;
		}

		boolean exportP12Public = sslUtil.exportPKCS12PublicCertificate(alias, password);
		if(!exportP12Public){
			logger.error("Failed to export public key");
			return outBool;
		}
		*/
		outBool = true;
		logger.info("Finished generating certificate");
		return outBool;
	}
	
	
}
