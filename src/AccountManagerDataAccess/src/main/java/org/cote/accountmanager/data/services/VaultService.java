/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.data.services;

/*
 * Refactor TODO:
 * 1) (done) Replace original single-config implementation with variable config
 * 2) Add by-owner services to more easily find/associate vaults with users, to simplify implementation
 * 3) Refactor getVaultKey(VaultType ...) into an optional service call, such that one instance may defer to a second instance for the cipher operations
 *    i) this would allow an arrangement where the public and cipher keys are in the database, and the vault private key is only on designated nodes
 *    ii) A whole separate instance (including database) would be possible with a registration/key-exchange, where the same database level operations are performed in the registrant and registrar, with the private key held outside the database by the registrar
 *    iii) Which basically makes item (ii) into a simplified HCM 
 */


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.security.cert.Certificate;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.beans.VaultBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.VaultType;
import org.cote.accountmanager.objects.types.CompressionEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.KeyStoreUtil;
import org.cote.accountmanager.util.OpenSSLUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.accountmanager.util.ZipUtil;
	public class VaultService
	{
		
		
		
		public static final Logger logger = LogManager.getLogger(VaultService.class);
		

		/* 2017/06/22 - START REFACTOR */
		/// TODO: I already don't like including the ssl binary config here
		///
		private String sslBinary = null;
		private String sslPath = null;
		private OpenSSLUtil sslUtil = null;
		private boolean generateCertificates = false;
		
		/// export a version of the vault that does not include exposed (aka unencrypted) information that should be protected
		///
		public static String exportVault(VaultType vault){
			VaultType clone = new VaultType();
			clone.setNameType(NameEnumType.VAULT);
			clone.setInitialized(false);
			clone.setActiveKeyId(vault.getActiveKeyId());
			clone.setCreated(vault.getCreated());
			clone.setDn(vault.getDn());
			clone.setExpiryDays(vault.getExpiryDays());
			clone.setHaveVaultKey(vault.getHaveVaultKey());
			clone.setHaveCredential(vault.getHaveCredential());
			clone.setKeyExtension(vault.getKeyExtension());
			clone.setKeyPrefix(vault.getKeyPrefix());
			clone.setKeyProtectedPrefix(vault.getKeyProtectedPrefix());
			clone.setObjectId(vault.getObjectId());
			clone.setOrganizationPath(vault.getOrganizationPath());
			clone.setServiceUserUrn(vault.getServiceUserUrn());
			clone.setProtected(vault.getProtected());
			clone.setVaultAlias(vault.getVaultAlias());
			clone.setVaultGroupName(vault.getVaultGroupName());
			clone.setVaultGroupUrn(vault.getVaultGroupUrn());
			clone.setVaultKeyPath(vault.getVaultKeyPath());
			clone.setVaultName(vault.getVaultName());
			clone.setVaultNameHash(vault.getVaultNameHash());
			clone.setServiceUser(null);
			clone.setProtectedCredential(null);
			clone.setVaultPath(vault.getVaultPath());
			clone.setCredential(vault.getCredential());
			clone.setVaultKey(null);
			return JSONUtil.exportObject(clone);
		}
		public static byte[] getCertificate(VaultType vault, boolean isPrivate){
			String path = vault.getVaultPath() + "/certificates/" + (isPrivate ? "private" : "signed") + "/" + vault.getVaultAlias() + "." + (isPrivate ? "p12" : "cert");
			return FileUtil.getFile(path);
		}
		
		/// Only necessary if generating secondary certificate
		///
		public VaultService(String sslBin, String path){
			sslBinary = sslBin;
			sslPath = path + (path.endsWith("/") ? "" : "/");
			
			sslUtil = new OpenSSLUtil(sslBinary, sslPath);
			sslUtil.configure();
		}
		
		public VaultService(){
			
		}
		
		public CredentialType loadProtectedCredential(String filePath){
			String fileDat = FileUtil.getFileAsString(filePath);
			if(fileDat == null || fileDat.length() == 0){
				logger.warn("File not found: " + filePath);
				return null;
			}
			return JSONUtil.importObject(FileUtil.getFileAsString(filePath), CredentialType.class);
		}
		
		/// Create an encrypted credential used to protect the private vault key
		/// This credential is enciphered with a discrete secret key, stored in the database
		///
		public boolean createProtectedCredentialFile(UserType vaultOwner, String filePath, byte[] credential) throws ArgumentException, FactoryException{
			

			
			((NameIdFactory)Factories.getFactory(FactoryEnumType.USER)).populate(vaultOwner);
			
			File f = new File(filePath);
			if(f.exists()){
				logger.error("File '" + filePath + "' already exists");
				return false;
			}
			
			CredentialType cred = new CredentialType();
			cred.setNameType(NameEnumType.CREDENTIAL);
			cred.setCredentialType(CredentialEnumType.ENCRYPTED_PASSWORD);
			cred.setOrganizationPath(vaultOwner.getOrganizationPath());
			cred.setOrganizationId(vaultOwner.getOrganizationId());
			cred.setEnciphered(true);
			cred.setOrganizationId(vaultOwner.getOrganizationId());

			SecurityBean bean = KeyService.newPersonalSymmetricKey(vaultOwner, false);
			
			cred.setCredential(SecurityUtil.encipher(bean,credential));
			cred.setKeyId(bean.getObjectId());
			
			if(FileUtil.emitFile(filePath, JSONUtil.exportObject(cred))){
				logger.info("Created credential file '" + filePath + "'");
			}
			else{
				logger.error("Failed to create credential file at '" + filePath + "'");
				return false;
			}

			/// Test decipher the credential
			///
			CredentialType chkCred = JSONUtil.importObject(new String(FileUtil.getFile(filePath)), CredentialType.class);
			if(chkCred == null){
				logger.error("Failed check to read in credential file");
				return false;
			}
			SecurityBean chkBean = KeyService.getSymmetricKeyByObjectId(cred.getKeyId(), cred.getOrganizationId());
			if(chkBean == null){
				logger.error("Failed check to load the referenced symmetric key");
				return false;
			}
			byte[] chkCredBa = SecurityUtil.decipher(chkBean, chkCred.getCredential());
			String chkCredVal = new String(chkCredBa);
			if(chkCredVal == null || chkCredVal.length() == 0){
				logger.error("Failed to decipher credential");
				return false;
			}
			if(Arrays.equals(chkCredBa,credential) == false){
				logger.error("Restored credential does not match the submitted credential.");
				return false;
			}
			logger.info("Created credential at " + filePath);
			return true;
		}
		
		public VaultBean loadVault(String vaultBasePath, String vaultName, boolean isProtected){
			VaultType chkV = new VaultType();
			String path = vaultBasePath + File.separator + Hex.encodeHexString(SecurityUtil.getDigest(vaultName.getBytes(),new byte[0])) + "-" + chkV.getKeyPrefix() + (isProtected ? chkV.getKeyProtectedPrefix() : "") + chkV.getKeyExtension();
			File f = new File(path);
			if(!f.exists()){
				logger.error("Vault file is not accessible: '" + path + "'");
				return null;
			}
			String content = FileUtil.getFileAsString(f);
			chkV = JSONUtil.importObject(content, VaultType.class);
			
			return promote(chkV);
		}
		public CredentialType getSalt(VaultType vault){
			DirectoryGroupType dir=null;
			try {
				//dir = getVaultGroup(vault);
				dir = getVaultInstanceGroup(vault);
			} catch (FactoryException | ArgumentException e) {
				
				logger.error("Error",e);
			}
			if(dir == null) return null;
			return CredentialService.getPrimaryCredential(dir, CredentialEnumType.SALT, true);
		}
		
		public VaultBean promote(VaultType vault){
			VaultBean vaultb = new VaultBean();
			vaultb.setNameType(NameEnumType.VAULT);
			vaultb.setInitialized(vault.getInitialized());
			vaultb.setActiveKey(vault.getActiveKey());
			vaultb.setActiveKeyId(vault.getActiveKeyId());
			vaultb.setCreated(vault.getCreated());
			vaultb.setCredential(vault.getCredential());
			vaultb.setDn(vault.getDn());
			vaultb.setExpiryDays(vault.getExpiryDays());
			vaultb.setHaveCredential(vault.getHaveCredential());
			vaultb.setHaveVaultKey(vault.getHaveVaultKey());
			vaultb.setKeyExtension(vault.getKeyExtension());
			vaultb.setKeyPrefix(vault.getKeyPrefix());
			vaultb.setKeyProtectedPrefix(vault.getKeyProtectedPrefix());
			vaultb.setObjectId(vault.getObjectId());
			vaultb.setProtected(vault.getProtected());
			vaultb.setProtectedCredential(vault.getProtectedCredential());
			vaultb.setServiceUser(vault.getServiceUser());
			vaultb.setServiceUserUrn(vault.getServiceUserUrn());
			vaultb.setVaultAlias(vault.getVaultAlias());
			vaultb.setVaultGroupName(vault.getVaultGroupName());
			vaultb.setVaultGroupUrn(vault.getVaultGroupUrn());
			vaultb.setVaultKey(vault.getVaultKey());
			vaultb.setVaultKeyPath(vault.getVaultKeyPath());
			vaultb.setVaultName(vault.getVaultName());
			vaultb.setVaultNameHash(vault.getVaultNameHash());
			vaultb.setVaultPath(vault.getVaultPath());
			vaultb.setHaveVaultKey(vault.getHaveVaultKey());
			return vaultb;
		}
		
		public VaultBean newVault(UserType serviceUser, String vaultBasePath, String vaultName){
			VaultBean vault = new VaultBean();
			vault.setNameType(NameEnumType.VAULT);
			vault.setObjectId(UUID.randomUUID().toString());
			vault.setServiceUserUrn(serviceUser.getUrn());
			vault.setOrganizationPath(serviceUser.getOrganizationPath());
			vault.setVaultPath(vaultBasePath);
			vault.setVaultName(vaultName);
			vault.setServiceUser(serviceUser);
			vault.setExpiryDays(720);
			vault.setCreated(CalendarUtil.getXmlGregorianCalendar(new Date()));
			vault.setVaultAlias(vaultName.replaceAll("\\s", "").toLowerCase());
			if(sslUtil != null) vault.setDn(sslUtil.getDefaultDN(vault.getVaultAlias()));
			vault.setVaultNameHash(Hex.encodeHexString(SecurityUtil.getDigest(vaultName.getBytes(),new byte[0])));
			vault.setVaultKeyPath(vaultBasePath + File.separator + vault.getVaultNameHash() + "-" + vault.getKeyPrefix() + (vault.getProtected() ? vault.getKeyProtectedPrefix() : "") + vault.getKeyExtension());
	
			return vault;
		}
		
		private byte[] getProtectedCredentialValue(CredentialType credential){
			if(credential.getCredentialType() == CredentialEnumType.ENCRYPTED_PASSWORD){
				SecurityBean bean = KeyService.getSymmetricKeyByObjectId(credential.getObjectId(), credential.getOrganizationId());
				if(bean == null){
					return new byte[0];
				}
				return SecurityUtil.decipher(bean, credential.getCredential());
			}
			return credential.getCredential();
		}
		
		public boolean changeVaultPassword(VaultBean vault, CredentialType currentCred, CredentialType newCred) throws ArgumentException
		{

			if(vault.getProtected() && currentCred == null) throw new ArgumentException("Credential required to decipher vault key");
			
			SecurityBean org_sm = KeyService.getPrimarySymmetricKey(vault.getServiceUser().getOrganizationId());
			
			byte[] dec_config = SecurityUtil.decipher(org_sm,  vault.getCredential().getCredential());
			CredentialType credSalt = getSalt(vault);
			if(credSalt == null){
				logger.error("Salt is null");
				return false;
			}
			if(vault.getProtected()) dec_config = SecurityUtil.decipher(dec_config, new String(getProtectedCredentialValue(currentCred)),credSalt.getSalt());
			if (dec_config.length == 0) throw new ArgumentException("Failed to decipher config");

			if(newCred != null){
				dec_config = SecurityUtil.encipher(dec_config, new String(getProtectedCredentialValue(newCred)),credSalt.getSalt());
			}

			// Encipher with product key
			//
			byte[] enc_private_key = SecurityUtil.encipher(org_sm, dec_config);
			vault.getCredential().setCredential(enc_private_key);
			logger.info("Saving vault to '" + vault.getVaultKeyPath() + "'");
			FileUtil.emitFile(vault.getVaultKeyPath(), exportVault(vault));

			vault.setVaultKey(null);
			setProtected(vault, newCred);

			
			if (getVaultKey(vault) == null){
				logger.error("Failed to restore key with reset password");
				return false;
			}
			return true;
		}
		
		public boolean createVault(VaultBean vault, CredentialType credential){
			try{
			if (vault.getHaveVaultKey()== true){
				logger.info("Vault key already exists for " + vault.getVaultName());
				return true;
			}
			File vaultFile = new File(vault.getVaultKeyPath());
			if (vaultFile.exists() == true){
				logger.error("Vault Key Path already exists: " + vault.getVaultKeyPath());
				return false;
			}
			
			setProtected(vault, credential);
			vault.setVaultKeyPath(vault.getVaultPath() + File.separator + vault.getVaultNameHash() + "-" + vault.getKeyPrefix() + (vault.getProtected() ? vault.getKeyProtectedPrefix() : "") + vault.getKeyExtension());

			DirectoryGroupType imp_dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(vault.getServiceUser(), vault.getVaultGroupName(), vault.getServiceUser().getHomeDirectory(), vault.getServiceUser().getOrganizationId());

			DataType imp_data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(vault.getVaultName(), true, imp_dir);
			if (imp_data != null)
			{
				logger.error("Vault for '" + vault.getVaultName() + "' could not be made.  Existing vault must first be unimproved.");
				return false;
			}
			Factories.getNameIdFactory(FactoryEnumType.GROUP).populate(imp_dir);
			
			// Create a new group directory for storing DES keys that are vaulted for a specified vault
			// The name is the same as the vault data name
			//
			DirectoryGroupType local_imp_dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(vault.getServiceUser(), vault.getVaultName(), imp_dir, vault.getServiceUser().getOrganizationId());
			
			//imp_
			CredentialType credSalt = CredentialService.newCredential(CredentialEnumType.SALT, null, vault.getServiceUser(), local_imp_dir, new byte[0], true, false,false);
			if(credSalt == null || credSalt.getSalt().length == 0){
				logger.info("Failed to create salt");
				return false;
			}

			SecurityBean sm = new SecurityBean();
			sm.setEncryptCipherKey(true);
			sm.setReverseEncrypt(false);
			SecurityFactory.getSecurityFactory().generateKeyPair(sm);
			SecurityFactory.getSecurityFactory().generateSecretKey(sm);

			byte[] private_key_config = new byte[0];
			
			
			private_key_config = SecurityUtil.serializeToXml(sm, true, true, true).getBytes("UTF-8");
			
			String in_password = null;
			if(vault.getProtectedCredential() != null && (vault.getProtectedCredential().getCredentialType() == CredentialEnumType.ENCRYPTED_PASSWORD || vault.getProtectedCredential().getCredentialType() == CredentialEnumType.HASHED_PASSWORD)){
				//in_password = new String(CredentialService.decryptCredential(credential));
				in_password = new String(getProtectedCredentialValue(vault.getProtectedCredential()));
			}
			
			// If a password was specified, encrypt with password
			//
			if (vault.getProtected() && in_password != null && in_password.length() > 0)
			{
				logger.info("Private key config: " + private_key_config.length + " with salt " + new String(credSalt.getSalt()));
				private_key_config = SecurityUtil.encipher(private_key_config, in_password, credSalt.getSalt()); 
			}

			// Encipher with product key
			//
			SecurityBean org_sm = KeyService.getPrimarySymmetricKey(vault.getServiceUser().getOrganizationId()); 
			byte[] enc_private_key = SecurityUtil.encipher(org_sm,private_key_config);

			// No need to encrypt the public key beyond the auto-encrypt supplied by the AM org-level key (same key as used by default to encrypt private key)
			//
			byte[] public_key_config = SecurityUtil.serializeToXml(sm, false, true, false).getBytes("UTF-8");

			imp_data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(vault.getServiceUser(), imp_dir.getId());
			imp_data.setName(vault.getVaultName());
			imp_data.setDescription("Vault public key for node/cluster");
			imp_data.setMimeType("text/xml");
			DataUtil.setValue(imp_data, public_key_config);

			((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(imp_data);
			imp_data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(vault.getVaultName(), imp_dir);
			if(generateCertificates && in_password != null){
				if(createVaultCertificate(vault, null, in_password) == false){
					throw new ArgumentException("Failed to generate new certificates for vault");
				}
				byte[] pubCertificate = getCertificate(vault, false);
				CredentialType pubCred = CredentialService.newCredential(CredentialEnumType.CERTIFICATE, null, vault.getServiceUser(), imp_data, pubCertificate, true, false, false);
				if(pubCred == null){
					logger.error("Failed to create public certificate credential");
					return false;
				}
				
				byte[] privCertificate = getCertificate(vault, true);
				CredentialType cred = CredentialService.newCredential(CredentialEnumType.CERTIFICATE, null, vault.getServiceUser(), pubCred, privCertificate, true, false, false);
				if(cred == null){
					logger.error("Failed to create certificate credential");
					return false;
				}
			}
			
			vault.setHaveVaultKey(true);
			CredentialType vaultCred = new CredentialType();
			vaultCred.setNameType(NameEnumType.CREDENTIAL);
			vaultCred.setEnciphered(true);
			vaultCred.setCredentialType(CredentialEnumType.KEY);
			vaultCred.setCredential(enc_private_key);
			vaultCred.setCreatedDate(vault.getCreated());
			
			vault.setCredential(vaultCred);
			vault.setHaveCredential(true);
			vault.setVaultKeyBean(sm);
			FileUtil.emitFile(vault.getVaultKeyPath(), exportVault(vault));


			}
			catch(UnsupportedEncodingException | DataException | FactoryException | ArgumentException | NullPointerException e){
				logger.error("Error", e);
			}
			return true;
		}
		public boolean createVaultCertificate(VaultType vault, String dn, String password) throws ArgumentException, FactoryException{
			CredentialType cred = null;
			OrganizationType org = Factories.getNameIdFactory(FactoryEnumType.ORGANIZATION).find(vault.getOrganizationPath());
			if(org == null){
				throw new ArgumentException("Failed to find organization '" + vault.getOrganizationPath() + "'");
			}
			Factories.getAttributeFactory().populateAttributes(org);
			if(sslUtil.generateCertificateRequest(vault.getVaultAlias(), password.toCharArray(), vault.getDn(), vault.getExpiryDays())){
				logger.info("Generated request for " + vault.getVaultAlias());

				cred = CredentialService.getPrimaryCredential(org, CredentialEnumType.CERTIFICATE, true);
				if(cred == null){
					throw new ArgumentException("Organization '" + vault.getOrganizationPath() + " does not define a primary certificate credential");
				}
				Certificate cert = KeyStoreUtil.decodeCertificate(cred.getCredential());
				if(cert == null){
					throw new ArgumentException("Organization '" + vault.getOrganizationPath() + " does not define a valid certificate");
				}
				String alias = Factories.getAttributeFactory().getAttributeValueByName(org, "certificate.alias");
				if(alias == null || alias.length() == 0){
					throw new ArgumentException("Organization '" + vault.getOrganizationPath() + "' does not define an certificate.alias attribute");
				}
				logger.info("Signing " + vault.getVaultName() + " with signer " + alias);
				if (
						sslUtil.signCertificate(vault.getVaultAlias(), alias, vault.getExpiryDays())
						&&
						sslUtil.amendCertificateChain(vault.getVaultAlias(), alias)
					)
				{
					logger.info("Completed signing");
					boolean out_bool = false;
					logger.info("Exporting PKCS12 Certificate " + vault.getVaultAlias() + " ...");
					boolean exportP12Private = sslUtil.exportPKCS12PrivateCertificate(vault.getVaultAlias(), password.toCharArray(),alias);
					if(!exportP12Private){
						logger.error("Failed to export private key");
						return out_bool;
					}

					boolean exportP12Public = sslUtil.exportPKCS12PublicCertificate(alias, password.toCharArray());
					if(!exportP12Public){
						logger.error("Failed to export public key");
						return out_bool;
					}
					out_bool = true;
					logger.info("Exported PKCS12 Certificates");
					return out_bool;
					
					
				}
				else{
					logger.warn("Failed to issue a new vault certificate");
				}
			}
			else{
				logger.warn("Failed to create new vault certificate request");
			}
			return false;
		}
		
		private void setProtected(VaultType vault, CredentialType credential){
			boolean prot = (credential != null);
			vault.setProtected(prot);
			vault.setProtectedCredential(credential);
		}
		
		public void initialize(VaultType vault, CredentialType credential) throws ArgumentException, FactoryException{
			
			if(vault.getVaultPath() == null || vault.getVaultPath().length() == 0) throw new ArgumentException("Invalid base path");

			//symmetric_keys = new HashMap<String, SecurityBean>();
			logger.info("Initializing Vault '" + vault.getVaultName() + "' In " + vault.getVaultPath());
			if (FileUtil.makePath(vault.getVaultPath()) == false)
			{
				throw new ArgumentException("Unable to create path to " + vault.getVaultPath());
			}
			if(vault.getServiceUser() == null && vault.getServiceUserUrn() != null){
				vault.setServiceUser(Factories.getNameIdFactory(FactoryEnumType.USER).getByUrn(vault.getServiceUserUrn()));
			}
			if(vault.getServiceUser() == null){
				throw new ArgumentException("Unable to locate service user '" + vault.getServiceUserUrn() + "'");
			}
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(vault.getServiceUser());
			vault.setOrganizationId(vault.getServiceUser().getOrganizationId());
			// Check for non-password protected file
			//
			File vaultKeyFile = new File(vault.getVaultKeyPath());
			if (vaultKeyFile.exists())
			{
				vault.setHaveVaultKey(true);
				setProtected(vault, credential);
			}
			vault.setInitialized(true);
		}
		
		public DirectoryGroupType getVaultGroup(VaultType vault) throws FactoryException, ArgumentException{
			return ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(vault.getVaultGroupName(), vault.getServiceUser().getHomeDirectory(), vault.getServiceUser().getOrganizationId());
		}
		public DirectoryGroupType getVaultInstanceGroup(VaultType vault) throws FactoryException, ArgumentException{
			return ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(vault.getVaultName(),getVaultGroup(vault), vault.getServiceUser().getOrganizationId());
		}
		public boolean deleteVault(VaultType vault) throws ArgumentException, FactoryException
		{
			logger.info("Cleaning up vault instance");
			if (vault.getHaveVaultKey() == false){
				logger.warn("No key detected, so nothing is deleted");
			}
			if (vault.getVaultKeyPath() == null){
				logger.warn("Path is null");
			}
			else{
				File vaultKeyFile = new File(vault.getVaultKeyPath());
				if(vaultKeyFile.exists()){
					if(vaultKeyFile.delete() == false) logger.error("Unable to delete vault key file " + vault.getVaultKeyPath());
				}
				else{
					logger.warn("Vault file " + vault.getVaultKeyPath() + " does not exist");
				}
			}

			DirectoryGroupType local_imp_dir = getVaultInstanceGroup(vault);
			logger.info("Removing implementation group: " + (local_imp_dir == null ? "[null]" : local_imp_dir.getUrn()));
			if (local_imp_dir != null && !((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).deleteDirectoryGroup(local_imp_dir))
			{
				logger.warn("Unable to delete keys from vault directory");
			}
			DirectoryGroupType vaultGroup = getVaultGroup(vault);
			if(vaultGroup != null){
				DataType imp_data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(vault.getVaultName(), true,vaultGroup);
				logger.info("Removing implementation data: " + (imp_data == null ? "[null]" : imp_data.getUrn()));
				if(imp_data != null && !((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).delete(imp_data)){
					logger.warn("Unable to delete improvement key");
				}
				else if(imp_data == null){
					logger.warn("Implementation data '" + vault.getVaultName() + "' in group " + vaultGroup.getUrn() + " could not be removed");
				}
			}
			else{
				logger.warn("Vault group is null");
			}
			
			vault.setVaultKeyPath(null);
			vault.setHaveCredential(false);
			vault.setProtectedCredential(null);
			vault.setActiveKey(null);
			vault.setVaultKey(null);
			vault.setCredential(null);

			return true;
		}
		public boolean newActiveKey(VaultBean vault) throws FactoryException, ArgumentException, DataException, UnsupportedEncodingException{
			DataType imp_data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(vault.getVaultName(), getVaultGroup(vault));

			// Can't make active key
			//
			if (imp_data == null){
				logger.error("Vault implementation data is null");
				return false;
			}

			// Import the key, and specify that the DES key is encrypted
			//
			SecurityBean sm = SecurityFactory.getSecurityFactory().createSecurityBean(DataUtil.getValue(imp_data), true);
			if (sm == null){
				logger.error("SecurityBean for Vault implementation is null");
				return false;
			}
			sm.setEncryptCipherKey(true);
			SecurityFactory.getSecurityFactory().generateSecretKey(sm);

			// The des key is new because the import was only the public key, and the export is encrypted.
			// The only way to get the key from the export is with the private key to decrypt it, and the private key is enciphered on the improved computer with the org-level SM.
			//
			/// TODO: 2017/06/22 - Verify the key export is encrypted - it isn't clear it's being encrypted here (besides a really old code comment)
			///
			byte[] secret_key = SecurityUtil.serializeToXml(sm, false, false, true).getBytes("UTF-8");
			String id = UUID.randomUUID().toString();
			
			DirectoryGroupType loc_imp_dir = getVaultInstanceGroup(vault);
			DataType new_key = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(vault.getServiceUser(), loc_imp_dir.getId());
			new_key.setName(id);
			new_key.setMimeType("text/xml");
			new_key.setDescription("Improvement key for " + vault.getVaultName());
			DataUtil.setValue(new_key, secret_key);
			if(((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(new_key)){
				vault.setActiveKeyId(id);
				vault.setActiveKeyBean(sm);
				return true;
			}
			else{
				logger.error("Failed to add new vault key");
			}
			return false;
			
		}
		/*
		 * TODO: Change back to private - only public while testing the refactor
		 */
		public SecurityBean getVaultKey(VaultBean vault) throws ArgumentException
		{
			if(vault.getInitialized() == false){
				throw new ArgumentException("Vault was not initialized");
			}
			if (vault.getHaveVaultKey() == false || (vault.getProtected() && vault.getHaveCredential() == null)){
				if(vault.getProtected()) logger.error("Vault password was not specified");
				else if(vault.getHaveVaultKey() == false) logger.error("Vault configuration does not indicate a key is defined.");
				return null;
			}
				if (vault.getVaultKey() == null)
				{
					try
					{
						byte[] key_bytes = vault.getCredential().getCredential();
						if (key_bytes.length == 0){
							logger.error("Vault key credential is null");
							return null;
						}
						SecurityBean org_sm = KeyService.getPrimarySymmetricKey(vault.getServiceUser().getOrganizationId()); 
						byte[] dec_config = SecurityUtil.decipher(org_sm,key_bytes);
						CredentialType credSalt = getSalt(vault);
						if(credSalt == null){
							logger.info("Salt is null");
							return null;
						}
						if (vault.getProtected()){
							logger.info("Deciphering private key with salt " + new String(credSalt.getSalt()));
							
							dec_config = SecurityUtil.decipher(dec_config, new String(getProtectedCredentialValue(vault.getProtectedCredential())),credSalt.getSalt());
						}
						if (dec_config.length == 0) return null;

						vault.setVaultKeyBean(SecurityFactory.getSecurityFactory().createSecurityBean(dec_config, true));
					}
					catch (Exception e)
					{
						logger.error("Error",e);
					}
				}
				return vault.getVaultKeyBean();

		}
		
		
		
		/// NOTE:
		///		The Volatile Key includes the deciphered/exposed private key from the vaultKey
		///		The private key should be immediately null'd after decrypting the secret key
		///		
		private SecurityBean getVolatileVaultKey(VaultBean vault) throws ArgumentException
		{
			if (vault.getHaveVaultKey() == false || getVaultKey(vault) == null){
				logger.error("Vault is not initialized correctly.  The vault key is not present.");
				return null;
			}

			SecurityBean out_bean = new SecurityBean();
			
			SecurityBean in_bean = vault.getVaultKeyBean();
			
			
			out_bean.setEncryptCipherKey(in_bean.getEncryptCipherKey());
			out_bean.setCipherIV(in_bean.getCipherIV());
			out_bean.setCipherKey(in_bean.getCipherKey());
			out_bean.setPrivateKeyBytes(in_bean.getPrivateKeyBytes());
			out_bean.setPrivateKey(in_bean.getPrivateKey());
			out_bean.setPublicKeyBytes(in_bean.getPublicKeyBytes());
			out_bean.setPublicKey(in_bean.getPublicKey());
			out_bean.setSecretKey(in_bean.getSecretKey());

			return out_bean;
		
		}
		
		private SecurityBean getCipherFromData(VaultBean vault, DataType data) throws DataException, ArgumentException{
			// Get a mutable security manager to swap out the keys
			// The Volatile Key includes the exposed private key, so it's immediately wiped from the object after decrypting the cipher key
			//
			SecurityBean v_sm = getVolatileVaultKey(vault);
			if (v_sm == null){
				logger.error("Volatile key copy is null");
				return null;
			}
			byte[] dataBytes = DataUtil.getValue(data);
			if(dataBytes.length == 0){
				logger.error("Key data was empty");
				return null;
			}
			SecurityFactory.getSecurityFactory().importSecurityBean(v_sm, dataBytes, true);
			v_sm.setPrivateKey(null);
			v_sm.setPrivateKeyBytes(new byte[0]);
			return v_sm;
		}
		
		public SecurityBean getVaultCipher(VaultBean vault, String keyId) throws FactoryException, ArgumentException, DataException{
			if(vault.getSymmetricKeyMap().containsKey(keyId) == true){
				return vault.getSymmetricKeyMap().get(keyId);
			}

			// Get the encrypted DES keys for this data item.
			//
			DataType key = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(keyId, getVaultInstanceGroup(vault));
			if (key == null){
				logger.error("Vault key " + keyId + " does not exist");
				return null;
			}

			SecurityBean v_sm = getCipherFromData(vault, key);
			if(v_sm == null){
				logger.error("Failed to restore cipher from data");
				return null;
			}
			vault.getSymmetricKeyMap().put(keyId, v_sm);
			return v_sm;
		}
		
		
		public void setVaultBytes(VaultBean vault, DataType data, byte[] in_data) throws DataException, FactoryException, UnsupportedEncodingException, ArgumentException
		{
			if (vault.getActiveKey() == null || vault.getActiveKeyId() == null){
				if(newActiveKey(vault) == false){
					throw new FactoryException("Failed to establish active key");
				}
				if (vault.getActiveKey() == null)
				{
					throw new FactoryException("Active key is null");
				}
			}

			data.setCompressed(false);
			data.setDataHash(SecurityUtil.getDigestAsString(in_data,new byte[0]));

			if (in_data.length > 512 && DataUtil.tryCompress(data))
			{
				in_data = ZipUtil.gzipBytes(in_data);
				data.setCompressed(true);
				data.setCompressionType(CompressionEnumType.GZIP);
			}
			data.setVaulted(true);
			DataUtil.setValue(data,SecurityUtil.encipher(vault.getActiveKeyBean(), in_data));
			data.setKeyId(vault.getActiveKeyId());
			data.setVaultId(vault.getVaultName());

		}
		public byte[] extractVaultData(VaultBean vault, DataType in_data) throws FactoryException, ArgumentException, DataException
		{
			byte[] out_bytes = new byte[0];
			if (vault.getHaveVaultKey() == false || in_data.getVaulted() == false) return out_bytes;

			// If the data vault id isn't the same as this vault name, then it can't be decrypted.
			//
			if (vault.getVaultName().equals(in_data.getVaultId()) == false){
				logger.error("Data vault id '" + in_data.getVaultId() + "' does not match the specified vault name '" + vault.getVaultName() + "'.  This is a precautionary/secondary check, probably due to changing the persisted vault configuration name");
				return out_bytes;
			}

			return getVaultBytes(vault,in_data, getVaultCipher(vault,in_data.getKeyId()));

		}
		public static byte[] getVaultBytes(VaultBean vault, DataType data, SecurityBean bean) throws DataException
		{
			byte[] ret = SecurityUtil.decipher(bean,DataUtil.getValue(data));
			if (data.getCompressed() && ret.length > 0)
			{
				ret = ZipUtil.gunzipBytes(ret);
			}
			if (data.getPointer())
			{
				ret = FileUtil.getFile(new String(ret));
			}
			return ret;
		}
		public DataType newVaultData(VaultBean vault, UserType dataOwner, String name, DirectoryGroupType group, String mime_type, byte[] in_data, byte[] clientCipher) throws FactoryException, ArgumentException, DataException, UnsupportedEncodingException
		{
			boolean encipher = (clientCipher != null && clientCipher.length > 0 ? true : false);
			if (in_data == null || in_data.length == 0) return null;

			DataType out_data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(dataOwner, group.getId());
			out_data.setName(name);
			out_data.setMimeType(mime_type);

			if (encipher && clientCipher.length > 0)
			{
				out_data.setCipherKey(clientCipher);
				out_data.setEncipher(true);
			}
			setVaultBytes(vault, out_data, in_data);

			return out_data;
		}
		
		public List<String> listVaultsByOwner(UserType owner) throws FactoryException, ArgumentException{
			VaultType vault = new VaultType();
			((NameIdFactory)Factories.getFactory(FactoryEnumType.USER)).populate(owner);
			vault.setServiceUser(owner);
			/// Using the default group location ("~/.vault)
			///
			DirectoryGroupType dir = getVaultGroup(vault);
			logger.info("List in parent: " + dir.getId());
			List<BaseGroupType> groupList = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).listInParent("DATA", dir.getId(), 0L, 0, dir.getOrganizationId());
			List<String> vaultNames = new ArrayList<>();
			for(BaseGroupType group : groupList) vaultNames.add(group.getName());
			return vaultNames;
		}
		
		public List<SecurityBean> getCiphers(VaultBean vault){
			List<SecurityBean> beans = new ArrayList<SecurityBean>();
			
			try {
				List<DataType> dataList = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataListByGroup(getVaultInstanceGroup(vault), false, 0, 0, vault.getServiceUser().getOrganizationId());
				for(int i = 0; i < dataList.size();i++) beans.add(getCipherFromData(vault,dataList.get(i)));
			} catch (FactoryException | ArgumentException | DataException e) {
				logger.error("Error",e);
			}
			return beans;
		}
		
		
		public boolean updateImprovedData(VaultBean vault, DataType in_data, byte[] in_bytes) throws FactoryException, ArgumentException, UnsupportedEncodingException, DataException, DataAccessException
		{

			if (in_data == null) return false;

			DirectoryGroupType loc_imp_dir = getVaultInstanceGroup(vault);

			// if there is no localized improvement in the database, then don't continue
			//
			if (loc_imp_dir == null) return false;

			// If there is no active key, then load the public key for the improvement, which will cause a new SecretKey to be created
			// And then add the DES key export to the improvement for later reference
			//
			if (vault.getActiveKey() == null)
			{
				if(in_data.getKeyId() != null) vault.setActiveKeyId(in_data.getKeyId());
				else newActiveKey(vault);
			}

			if (vault.getActiveKey() == null || vault.getActiveKeyId() == null) return false;
			setVaultBytes(vault, in_data, in_bytes);

			return ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).update(in_data);
		}

	}

