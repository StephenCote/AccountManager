package org.cote.accountmanager.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.common.io.Files;

public class OpenSSLUtil {
	public static final Logger logger = Logger.getLogger(OpenSSLUtil.class.getName());
	
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
	
	public static final String STORES_BASE_PATH = "stores";
	public static final String STORE_KEY_PATH = STORES_BASE_PATH + "/key";
	public static final String STORE_TRUST_PATH = STORES_BASE_PATH + "/trust";

	/// ,"-salt" - I have it turned off at the moment while I figure out a good way to handle the salt location
	///
	private String[] keyCipherOptions = new String[]{"-aes256"};
	
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
			){
			configured = true;
		}
		return configured;
	}
	
	public boolean generateCertificate(String alias, String dn, char[] password, String signerAlias, char[] signerPassword ){
		boolean out_bool = false;
		
		return out_bool;
	}
	public boolean exportPrivateKey(String alias, char[] password){
		//openssl rsa -passin pass:$3 -passout pass:$3 -in keys/private/$1.pem -out keys/private/$1.key
		String checkFilePath = sslPath + KEY_PRIVATE_PATH + "/" + alias + ".pem";
		File checkFile = new File(checkFilePath);
		if(checkFile.exists() == false){
			logger.error("Key " + checkFilePath + " does not exist");
			return false;
		}
		//List<String> commands = new ArrayList<String>();
		String[] commands = new String[]{
			openSSL,"rsa",
			"-passin","pass:" + String.valueOf(password),
			"-passout","pass:" + String.valueOf(password),
			"-in",KEY_PRIVATE_PATH + "/" + alias + ".pem",
			"-out",KEY_PRIVATE_PATH + "/" + alias + ".key"
		};
		
		List<String> cmd = ProcessUtil.runProcess(sslPath,commands);
		
		checkFilePath = sslPath + KEY_PRIVATE_PATH + "/" + alias + ".key";
		checkFile = new File(checkFilePath);
		return checkFile.exists();

	}
	public boolean exportPublicKey(String alias, char[] password){
			//openssl rsa -passin pass:$3 -passout pass:$3 -in keys/private/$1.pem -pubout -outform DER -out keys/public/$1.der
		String checkFilePath = sslPath + KEY_PRIVATE_PATH + "/" + alias + ".pem";
		File checkFile = new File(checkFilePath);
		if(checkFile.exists() == false){
			logger.error("Key " + checkFilePath + " does not exist");
			return false;
		}
		//List<String> commands = new ArrayList<String>();
		String[] commands = new String[]{
			openSSL,"rsa",
			"-passin","pass:" + String.valueOf(password),
			"-passout","pass:" + String.valueOf(password),
			"-pubout","-outform","DER",
			"-in",KEY_PRIVATE_PATH + "/" + alias + ".pem",
			"-out",KEY_PUBLIC_PATH + "/" + alias + ".der"
		};
		
		List<String> cmd = ProcessUtil.runProcess(sslPath,commands);
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
		/// openssl pkcs12 -export -nokeys -in certificates/signed/$1.cert -out certificates/signed/$1.p12 -passin pass:$3 -passout pass:$3
		
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
			"-in",CERTIFICATE_SIGNED_PATH + "/" + alias + ".cert",
			"-out",CERTIFICATE_SIGNED_PATH + "/" + alias + ".p12",
			"-name",alias
		};
		
		List<String> cmd = ProcessUtil.runProcess(sslPath,commands);
		return (new File(sslPath + CERTIFICATE_PRIVATE_PATH + "/" + alias + ".p12")).exists();
	}
	public boolean exportPKCS12PrivateCertificate(String alias, char[] password, String signerAlias){
		/// openssl pkcs12 $CHAIN -clcerts -passin pass:$3 -passout pass:$3 -export -inkey keys/private/$1.key -in certificates/signed/$1.cert -out certificates/private/$1.p12 -descert
		// CHAIN="-chain -CAfile $CHAINCERT"
		
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
			chainFile = CERTIFICATE_SIGNED_PATH + "/" + signerAlias + ".chain.cert";
			File checkChain = new File(sslPath + chainFile);
			if(checkChain.exists() == false){
				chainFile = CERTIFICATE_SIGNED_PATH + "/" + signerAlias + ".cert";
				checkChain = new File(sslPath + chainFile);
				if(checkChain.exists() == false){
					logger.error("Certificate chain not found for " + signerAlias);
					return false;
				}
			}
		}

		
		List<String> commands = new ArrayList<String>();
		commands.add(openSSL);
		commands.add("pkcs12");
		if(signerAlias != null){
			commands.add("-chain");
			commands.add("-CAfile");
			commands.add(chainFile);
		}
		commands.add("-clcerts");
		commands.add("-export");
		commands.add("-passin");
		commands.add("pass:" + String.valueOf(password));
		commands.add("-passout");
		commands.add("pass:" + String.valueOf(password));
		commands.add("-inkey");
		commands.add(KEY_PRIVATE_PATH + "/" + alias + ".key");
		commands.add("-in");
		commands.add(CERTIFICATE_SIGNED_PATH + "/" + alias + ".cert");
		commands.add("-out");
		commands.add(CERTIFICATE_PRIVATE_PATH + "/" + alias + ".p12");
		commands.add("-descert");
		commands.add("-name");
		commands.add(alias);
		
		List<String> cmd = ProcessUtil.runProcess(sslPath,commands.toArray(new String[0]));
		return (new File(sslPath + CERTIFICATE_PRIVATE_PATH + "/" + alias + ".p12")).exists();
	}
	public boolean generateCertificateRequest(String alias, char[] password, String dn, int expiryDays){
		/*
		 *   
   
   echo Amending Chain: certificates/signed/$1.cert $CHAINCERT to $1.chain.cert

   cat certificates/signed/$1.cert $CHAINCERT > certificates/signed/$1.chain.cert
		 */
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
				"-key",KEY_PRIVATE_PATH + "/" + alias + ".key",
				"-days",Integer.toString(expiryDays),"-nodes",
				"-subj",dn,
				"-out",CERTIFICATE_REQUEST_PATH + "/" + alias + ".csr"
			};
			
			List<String> cmd = ProcessUtil.runProcess(sslPath,commands);
			return (new File(sslPath + CERTIFICATE_REQUEST_PATH + "/" + alias + ".csr")).exists();
	}
	
	public boolean signCertificate(String requestAlias, String signerAlias, int expiryDays){
		//openssl x509 -req -in certificates/requests/$1.csr -days 720 -CA certificates/signed/$4.cert -CAkey keys/private/$4.key -out certificates/signed/$1.cert -CAcreateserial
		


		String checkFilePath = sslPath + CERTIFICATE_REQUEST_PATH + "/" + requestAlias + ".csr";
		File checkFile = new File(checkFilePath);
		if(checkFile.exists() == false){
			logger.error("CSR " + checkFilePath + " does not exist");
			return false;
		}

		String[] commands = new String[]{
				openSSL,"x509","-req",
				"-in",CERTIFICATE_REQUEST_PATH + "/" + requestAlias + ".csr",
				"-CA",CERTIFICATE_SIGNED_PATH + "/" + signerAlias + ".cert",
				"-CAkey",KEY_PRIVATE_PATH + "/" + signerAlias + ".key",
				"-days",Integer.toString(expiryDays),
				"-out",CERTIFICATE_SIGNED_PATH + "/" + requestAlias + ".cert",
				"-CAcreateserial"
			};
			
			List<String> cmd = ProcessUtil.runProcess(sslPath,commands);
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
		///    openssl req -x509 -new -passin pass:$3 -key keys/private/$1.key -days 720 -nodes -subj "/C=US/ST=WA/L=Mukilteo/O=AM5.2/CN=$2" -out certificates/root/$1.cert
		
		//List<String> commands = new ArrayList<String>();
		String[] commands = new String[]{
			openSSL,"req","-x509","-new",
			"-passin","pass:" + String.valueOf(password),
			"-key",KEY_PRIVATE_PATH + "/" + alias + ".key",
			"-days",Integer.toString(expiryDays),"-nodes",
			"-subj",dn,
			"-out",CERTIFICATE_ROOT_PATH + "/" + alias + ".cert"
		};
		
		boolean out_bool = false;
		List<String> cmd = ProcessUtil.runProcess(sslPath,commands);
		checkFilePath = sslPath + CERTIFICATE_ROOT_PATH + "/" + alias + ".cert";
		checkFile = new File(checkFilePath);
		if(checkFile.exists()){
			try {
				Files.copy(checkFile, new File(sslPath + CERTIFICATE_SIGNED_PATH + "/" + alias + ".cert"));
				out_bool = true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		
		return out_bool;
	}
	public boolean generateKeyPair(String alias, char[] password){
		/// openssl genrsa -aes256 -passout pass:$3 -out keys/private/$1.pem 2048
		String checkFilePath = sslPath + KEY_PRIVATE_PATH + "/" + alias + ".pem";
		
		List<String> commands = new ArrayList<String>();
		commands.add(openSSL);
		commands.add("genrsa");
		commands.addAll(Arrays.asList(keyCipherOptions));
		commands.add("-passout");
		commands.add("pass:" + String.valueOf(password));
		commands.add("-out");
		commands.add(KEY_PRIVATE_PATH + "/" + alias + ".pem");
		List<String> cmd = ProcessUtil.runProcess(sslPath,commands.toArray(new String[0]));
		File checkFile = new File(checkFilePath);
		return checkFile.exists();
	}

	
}
