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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.exceptions.FactoryException;

import com.google.common.io.Files;

public class OpenSSLUtil {
	public static final Logger logger = LogManager.getLogger(OpenSSLUtil.class);
	
	private String openSSL = null;
	private String sslPath = null;
	public static final String CERTIFICATES_BASE_PATH = "certificates";
	public static final String CERTIFICATE_REQUEST_PATH = CERTIFICATES_BASE_PATH + "/requests";
	public static final String CERTIFICATE_SIGNED_PATH = CERTIFICATES_BASE_PATH + "/signed";
	public static final String CERTIFICATE_ROOT_PATH = CERTIFICATES_BASE_PATH + "/root";
	public static final String CERTIFICATE_PRIVATE_PATH = CERTIFICATES_BASE_PATH + "/private";
	public static final String KEYS_BASE_PATH = "keys";
	public static final String KEY_PRIVATE_PATH = KEYS_BASE_PATH + "/private";
	public static final String KEY_PUBLIC_PATH = KEYS_BASE_PATH + "/public";
	public static final String OPENSSL_CONFIG = "openssl.conf";
	public static final String OPENSSL_DB_PATH = "openssl";
	public static final String OPENSSL_CERTINDEX = OPENSSL_DB_PATH + "/certindex.txt";
	public static final String STORES_BASE_PATH = "stores";
	public static final String STORE_KEY_PATH = STORES_BASE_PATH + "/key";
	public static final String STORE_TRUST_PATH = STORES_BASE_PATH + "/trust";

	/// ,"-salt" - I have it turned off at the moment while I figure out a good way to handle the salt location
	///
	private String[] keyCipherOptions = new String[]{"-aes256"};
	
	private String[] signOptions = new String[]{"-sha512"};
	
	public OpenSSLUtil(String openSSLBinary, String sslWorkPath){
		openSSL = openSSLBinary;

		sslPath = sslWorkPath + (sslWorkPath.endsWith("/") ? "" : "/");
	}
	
	
	public boolean configure(){
		boolean configured = false; 
		if(
				FileUtil.makePath(sslPath + CERTIFICATE_REQUEST_PATH)
				&& FileUtil.makePath(sslPath + CERTIFICATE_SIGNED_PATH)
				&& FileUtil.makePath(sslPath + CERTIFICATE_ROOT_PATH)
				&& FileUtil.makePath(sslPath + CERTIFICATE_PRIVATE_PATH)
				&& FileUtil.makePath(sslPath + KEY_PRIVATE_PATH)
				&& FileUtil.makePath(sslPath + KEY_PUBLIC_PATH)
				&& FileUtil.makePath(sslPath + STORE_KEY_PATH)
				&& FileUtil.makePath(sslPath + STORE_TRUST_PATH)
				&& FileUtil.makePath(sslPath + OPENSSL_DB_PATH)
			){
			File f = new File(sslPath + OPENSSL_CERTINDEX);
			if(f.exists() == false){
				FileUtil.emitFile(sslPath + OPENSSL_CERTINDEX, "");
			}
			configured = true;
		}
		return configured;
	}
	
	public boolean generateCertificate(String alias, String dn, char[] password, String signerAlias, char[] signerPassword ){
		boolean outBool = false;
		
		return outBool;
	}
	public boolean exportPrivateKey(String alias, char[] password){
		logger.debug("Exporting private key");
		String checkFilePath = sslPath + KEY_PRIVATE_PATH + "/" + alias + ".pem";
		File checkFile = new File(checkFilePath);
		if(checkFile.exists() == false){
			logger.error("Key " + checkFilePath + " does not exist");
			return false;
		}
		String[] commands = new String[]{
			openSSL,"rsa",
			"-passin","pass:" + String.valueOf(password),
			"-passout","pass:" + String.valueOf(password),
			"-in",sslPath + KEY_PRIVATE_PATH + "/" + alias + ".pem",
			"-out",sslPath + KEY_PRIVATE_PATH + "/" + alias + ".key"
		};
		
		ProcessUtil.runProcess(sslPath,commands);
		
		checkFilePath = sslPath + KEY_PRIVATE_PATH + "/" + alias + ".key";
		checkFile = new File(checkFilePath);
		return checkFile.exists();

	}
	public boolean exportPublicKey(String alias, char[] password){
		logger.debug("Exporting public key");
		String checkFilePath = sslPath + KEY_PRIVATE_PATH + "/" + alias + ".pem";
		File checkFile = new File(checkFilePath);
		if(checkFile.exists() == false){
			logger.error("Key " + checkFilePath + " does not exist");
			return false;
		}
		String[] commands = new String[]{
			openSSL,"rsa",
			"-passin","pass:" + String.valueOf(password),
			"-passout","pass:" + String.valueOf(password),
			"-pubout","-outform","DER",
			"-in",sslPath + KEY_PRIVATE_PATH + "/" + alias + ".pem",
			"-out",sslPath + KEY_PUBLIC_PATH + "/" + alias + ".der"
		};
		
		ProcessUtil.runProcess(sslPath,commands);
		checkFilePath = sslPath + KEY_PUBLIC_PATH + "/" + alias + ".der";
		checkFile = new File(checkFilePath);
		return checkFile.exists();
	}
	public String getDefaultDN(String alias){
		return "/C=US/ST=WA/L=Mukilteo/O=AM5.2/CN=" + alias;
	}

	public boolean amendCertificateChain(String alias, String chainAlias){
		File cert = new File(sslPath + CERTIFICATE_SIGNED_PATH + "/" + alias + ".cert");
		File chainCert = new File(sslPath + CERTIFICATE_SIGNED_PATH + "/" + chainAlias + ".chain.cert");
		if(chainCert.exists() == false){
			chainCert = new File(sslPath + CERTIFICATE_SIGNED_PATH + "/" + chainAlias + ".cert");
		}
		if(cert.exists() == false || chainCert.exists() == false){
			logger.error("Certificates could not be found");
			return false;
		}
		String certStr = FileUtil.getFileAsString(cert);
		String chainStr = FileUtil.getFileAsString(chainCert);
		return FileUtil.emitFile(sslPath + CERTIFICATE_SIGNED_PATH + "/" + alias + ".chain.cert", chainStr + certStr);
	}
	public boolean exportPKCS12PublicCertificate(String alias, char[] password){
		String checkFilePath = sslPath + CERTIFICATE_SIGNED_PATH + "/" + alias + ".cert";
		File checkFile = new File(checkFilePath);
		if(checkFile.exists() == false){
			logger.error("Certificate " + checkFilePath + " does not exist");
			return false;
		}
		
		String[] commands = new String[]{
			openSSL,"pkcs12",
			"-nokeys","-export",
			"-passin","pass:" + String.valueOf(password),
			"-passout","pass:" + String.valueOf(password),
			"-in",sslPath + CERTIFICATE_SIGNED_PATH + "/" + alias + ".cert",
			"-out",sslPath + CERTIFICATE_SIGNED_PATH + "/" + alias + ".p12",
			"-name",alias
		};
		
		ProcessUtil.runProcess(sslPath,commands);
		return (new File(sslPath + CERTIFICATE_PRIVATE_PATH + "/" + alias + ".p12")).exists();
	}
	public boolean exportPKCS12PrivateCertificate(String alias, char[] password, String signerAlias){
		
		String checkFilePath = sslPath + KEY_PRIVATE_PATH + "/" + alias + ".key";
		File checkFile = new File(checkFilePath);
		if(checkFile.exists() == false){
			logger.error("Key " + checkFilePath + " does not exist");
			return false;
		}
		checkFilePath = sslPath + CERTIFICATE_SIGNED_PATH + "/" + alias + ".cert";
		checkFile = new File(checkFilePath);
		if(checkFile.exists() == false){
			logger.error("Certificate " + checkFilePath + " does not exist");
			return false;
		}
		
		String chainFile = null;
		if(signerAlias != null){
			chainFile = sslPath + CERTIFICATE_SIGNED_PATH + "/" + signerAlias + ".chain.cert";
			File checkChain = new File(chainFile);
			if(checkChain.exists() == false){
				chainFile = sslPath + CERTIFICATE_SIGNED_PATH + "/" + signerAlias + ".cert";
				checkChain = new File(chainFile);
				if(checkChain.exists() == false){
					logger.error("Certificate chain not found for " + signerAlias);
					return false;
				}
			}
		}

		
		List<String> commands = new ArrayList<>();
		commands.add(openSSL);
		commands.add("pkcs12");
		if(signerAlias != null){
			commands.add("-chain");
			commands.add("-CAfile");
			//commands.add("-certfile");
			commands.add(chainFile);
		}
		commands.add("-clcerts");
		commands.add("-export");
		commands.add("-passin");
		commands.add("pass:" + String.valueOf(password));
		commands.add("-passout");
		commands.add("pass:" + String.valueOf(password));
		commands.add("-inkey");
		commands.add(sslPath + KEY_PRIVATE_PATH + "/" + alias + ".key");
		commands.add("-in");
		commands.add(sslPath + CERTIFICATE_SIGNED_PATH + "/" + alias + ".cert");
		commands.add("-out");
		commands.add(sslPath + CERTIFICATE_PRIVATE_PATH + "/" + alias + ".p12");
		commands.add("-descert");
		commands.add("-name");
		commands.add(alias);
		
		ProcessUtil.runProcess(sslPath,commands.toArray(new String[0]));
		return (new File(sslPath + CERTIFICATE_PRIVATE_PATH + "/" + alias + ".p12")).exists();
	}
	public boolean generateCertificateRequest(String alias, char[] password, String dn, int expiryDays){
		if(!generateKeyPair(alias, password) || !exportPrivateKey(alias, password) || !exportPublicKey(alias, password)){
			logger.error("Failed to generate keys");
			return false;
		}

		String checkFilePath = sslPath + KEY_PRIVATE_PATH + "/" + alias + ".key";
		File checkFile = new File(checkFilePath);
		if(checkFile.exists() == false){
			logger.error("Key " + checkFilePath + " does not exist");
			return false;
		}

		String[] commands = new String[]{
				openSSL,"req","-new",
				"-config",OPENSSL_CONFIG,
				"-key",sslPath + KEY_PRIVATE_PATH + "/" + alias + ".key",
				/// "-days",Integer.toString(expiryDays),
				"-nodes",
				"-subj",dn,
				"-out",sslPath + CERTIFICATE_REQUEST_PATH + "/" + alias + ".csr"
			};
			logger.info("Generating '" + dn + "'");
			ProcessUtil.runProcess(sslPath,commands);
			return (new File(sslPath + CERTIFICATE_REQUEST_PATH + "/" + alias + ".csr")).exists();
	}
	
	public boolean signCertificate(String requestAlias, String signerAlias, int expiryDays){
		String checkFilePath = sslPath + CERTIFICATE_REQUEST_PATH + "/" + requestAlias + ".csr";
		File checkFile = new File(checkFilePath);
		if(checkFile.exists() == false){
			logger.error("CSR " + checkFilePath + " does not exist");
			return false;
		}
		

		String[] commands = new String[]{
				openSSL,"x509","-req",
				"-extfile",OPENSSL_CONFIG,
				"-extensions","v3_ca",
				String.join(",", signOptions),
				"-in",sslPath + CERTIFICATE_REQUEST_PATH + "/" + requestAlias + ".csr",
				"-CA",sslPath + CERTIFICATE_SIGNED_PATH + "/" + signerAlias + ".cert",
				"-CAkey",sslPath + KEY_PRIVATE_PATH + "/" + signerAlias + ".key",
				"-days",Integer.toString(expiryDays),
				"-out",sslPath + CERTIFICATE_SIGNED_PATH + "/" + requestAlias + ".cert",
				"-CAcreateserial"
			};
			
			ProcessUtil.runProcess(sslPath,commands);
			return (new File(sslPath + CERTIFICATE_SIGNED_PATH + "/" + requestAlias + ".cert")).exists();
		
	}
	
	public boolean generateRootCertificate(String alias, char[] password, String dn, int expiryDays){
		
		
		if(!generateKeyPair(alias, password) || !exportPrivateKey(alias, password) || !exportPublicKey(alias, password)){
			logger.error("Failed to generate keys");
			return false;
		}

		String checkFilePath = sslPath + KEY_PRIVATE_PATH + "/" + alias + ".key";
		File checkFile = new File(checkFilePath);
		if(checkFile.exists() == false){
			logger.error("Key " + checkFilePath + " does not exist");
			return false;
		}

		String[] commands = new String[]{
			openSSL,"req","-x509","-new",
			"-config",OPENSSL_CONFIG,
			"-extensions","v3_ca",
			"-passin","pass:" + String.valueOf(password),
			"-key",sslPath + KEY_PRIVATE_PATH + "/" + alias + ".key",
			"-days",Integer.toString(expiryDays),"-nodes",
			"-subj",dn,
			"-out",sslPath + CERTIFICATE_ROOT_PATH + "/" + alias + ".cert"
		};
		
		boolean outBool = false;
		ProcessUtil.runProcess(sslPath,commands);
		checkFilePath = sslPath + CERTIFICATE_ROOT_PATH + "/" + alias + ".cert";
		checkFile = new File(checkFilePath);
		if(checkFile.exists()){
			try {
				Files.copy(checkFile, new File(sslPath + CERTIFICATE_SIGNED_PATH + "/" + alias + ".cert"));
				outBool = true;
			} catch (IOException e) {
				logger.error(e.getMessage());
				logger.error(FactoryException.TRACE_EXCEPTION,e);
			}
		}

		
		return outBool;
	}
	public boolean generateKeyPair(String alias, char[] password){

		logger.debug("Generating key pair");
		
		String checkFilePath = sslPath + KEY_PRIVATE_PATH + "/" + alias + ".pem";
		
		List<String> commands = new ArrayList<String>();
		commands.add(openSSL);
		commands.add("genrsa");
		commands.addAll(Arrays.asList(keyCipherOptions));
		commands.add("-passout");
		commands.add("pass:" + String.valueOf(password));
		commands.add("-out");
		commands.add(sslPath + KEY_PRIVATE_PATH + "/" + alias + ".pem");
		ProcessUtil.runProcess(sslPath,commands.toArray(new String[0]));
		File checkFile = new File(checkFilePath);
		return checkFile.exists();
	}

	
}
