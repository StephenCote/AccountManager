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
package org.cote.accountmanager.data.services;

/*
 * Refactor TODO:
 * 1) (done) Replace original single-config implementation with variable config
 * 2) (in progress) Add by-owner services to more easily find/associate vaults with users, to simplify implementation
 *    i) note: The vault isn't the data, it's the key, so to find the vault doesn't imply accessing or discovering anything protected by the vault, but to see the vault itself - this allows one party to encrypt data that only the second party can read (not really a key exchange, just public key discovery)
 * 3) Refactor getVaultKey(VaultType ...) into an optional service call, such that one instance may defer to a second instance for the cipher operations
 *    i) this would allow an arrangement where the public and cipher keys are in the database, and the vault private key is only on designated nodes
 *    ii) A whole separate instance (including database) would be possible with a registration/key-exchange, where the same database level operations are performed in the registrant and registrar, with the private key held outside the database by the registrar
 *    iii) Which basically makes item (ii) into a simplified HCM 
 * 4) Add migration/move option that updates both the file- and db- persisted vault meta data.  This meta data is used more easily find the vault key.
 *    ii) Note: Optionally encipher the key path because at the moment it is in plaintext.
 * 5) Add audit
 * 6) Make vault access contingent on readability
 * 7) Need a good way to clean up orphaned keys.  The following query pulls up the orphaned keys used by DataType objects, but wouldn't factor in any other use of a vault key
 * 8) There's a memory leak due to caching that needs to be cleared up.  One of the caches is filling up and not being cleared.
 *    i) This may be in the authZ or audit services since these are now being exercised harder when using VaultService to encrypt the data values, vs. not using the vault service
 *       a) Not Audit - it was stacking up on bulk console operations because the maintenance thread wasn't running
 *       b) Not factory caches, though the typeMap cache in the factories was stacking up a bit without the factory cache running
 *       c) It looks like it may be the repeated invocation of newActiveKey on the same vaultCheck other factories for stray caches - caches
 *    ii) After running against a Visual VM, the culprit popped right out: Cloning via JAXB.  I swapped it out and all seems well again.
delete FROM data WHERE groupid in(
   SELECT id FROM groups WHERE parentid in (SELECT id FROM groups WHERE name = '.vault')
) AND name NOT IN (SELECT keyid FROM data WHERE NOT keyid IS NULL)

 *
 *
 */


import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.beans.VaultBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.data.util.UrnUtil;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.VaultType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.CompressionEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.service.rest.BaseService;
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
	
	private static Map<String,VaultBean> cacheByUrn = Collections.synchronizedMap(new HashMap<>());
	
	/// export a version of the vault that does not include exposed (aka unencrypted) information that should be protected
	///
	public String exportVault(VaultType vault){
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
		clone.setVaultDataUrn(vault.getVaultDataUrn());
		clone.setVaultKeyPath(vault.getVaultKeyPath());
		clone.setVaultName(vault.getVaultName());
		clone.setVaultNameHash(vault.getVaultNameHash());
		clone.setServiceUser(null);
		clone.setProtectedCredential(null);
		clone.setVaultPath(vault.getVaultPath());
		clone.setCredential(vault.getCredential());
		clone.setVaultKey(null);
		clone.setProtectedCredentialPath(vault.getProtectedCredentialPath());
		return JSONUtil.exportObject(clone);
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
		CredentialType outCred = JSONUtil.importObject(FileUtil.getFileAsString(filePath), CredentialType.class);
		if(outCred == null || outCred.getCredential().length == 0){
			logger.error("Credential was not successfully restored");
		}
		return outCred;
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
		
		/// Note: CredentialService is intentionally NOT USED here because this credential SHOULD NOT be stored in the database
		///
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
			logger.warn("Vault file is not accessible: '" + path + "'");
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
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
		vaultb.setVaultDataUrn(vault.getVaultDataUrn());
		vaultb.setVaultKey(vault.getVaultKey());
		vaultb.setVaultKeyPath(vault.getVaultKeyPath());
		vaultb.setVaultName(vault.getVaultName());
		vaultb.setVaultNameHash(vault.getVaultNameHash());
		vaultb.setVaultPath(vault.getVaultPath());
		vaultb.setHaveVaultKey(vault.getHaveVaultKey());
		vaultb.setProtectedCredentialPath(vault.getProtectedCredentialPath());
		return vaultb;
	}
	
	public VaultBean newVault(UserType serviceUser, String vaultBasePath, String vaultName){
		VaultBean vault = new VaultBean();
		vault.setNameType(NameEnumType.VAULT);
		vault.setObjectId(UUID.randomUUID().toString());
		vault.setServiceUser(serviceUser);
		vault.setServiceUserUrn(serviceUser.getUrn());
		vault.setOrganizationPath(serviceUser.getOrganizationPath());
		setVaultPath(vault,vaultBasePath);
		vault.setVaultName(vaultName);		vault.setServiceUser(serviceUser);
		vault.setExpiryDays(720);
		vault.setCreated(CalendarUtil.getXmlGregorianCalendar(new Date()));
		vault.setVaultAlias(vaultName.replaceAll("\\s", "").toLowerCase());
		if(sslUtil != null) vault.setDn(sslUtil.getDefaultDN(vault.getVaultAlias()));
		vault.setVaultNameHash(Hex.encodeHexString(SecurityUtil.getDigest(vaultName.getBytes(),new byte[0])));
		setVaultKeyPath(vault);
		return vault;
	}
	
	private byte[] getProtectedCredentialValue(CredentialType credential){
		if(credential.getCredentialType() == CredentialEnumType.ENCRYPTED_PASSWORD){
			SecurityBean bean = KeyService.getSymmetricKeyByObjectId(credential.getKeyId(), credential.getOrganizationId());
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
		FileUtil.emitFile(new String(SecurityUtil.decipher(org_sm,vault.getVaultKeyPath())), exportVault(vault));

		vault.setVaultKey(null);
		setProtected(vault, newCred);

		
		if (getVaultKey(vault) == null){
			logger.error("Failed to restore key with reset password");
			return false;
		}
		return true;
	}
	
	private void setVaultPath(VaultType vault, String path){
		SecurityBean org_sm = KeyService.getPrimarySymmetricKey(vault.getServiceUser().getOrganizationId());
		vault.setVaultPath(SecurityUtil.encipher(org_sm, path.getBytes()));
	}
	
	private String getVaultPath(VaultType vault){
		SecurityBean org_sm = KeyService.getPrimarySymmetricKey(vault.getServiceUser().getOrganizationId());
		return new String(SecurityUtil.decipher(org_sm, vault.getVaultPath()));
	}
	
	private void setVaultKeyPath(VaultType vault){
		SecurityBean org_sm = KeyService.getPrimarySymmetricKey(vault.getServiceUser().getOrganizationId());
		String path = getVaultPath(vault) + File.separator + vault.getVaultNameHash() + "-" + vault.getKeyPrefix() + (vault.getProtected() ? vault.getKeyProtectedPrefix() : "") + vault.getKeyExtension();
		vault.setVaultKeyPath(SecurityUtil.encipher(org_sm, path.getBytes()));
	}
	
	private String getVaultKeyPath(VaultType vault){
		if(vault.getServiceUser() == null){
			logger.error("Vault is not properly initialized");
			return null;
		}
		SecurityBean org_sm = KeyService.getPrimarySymmetricKey(vault.getServiceUser().getOrganizationId());
		return new String(SecurityUtil.decipher(org_sm, vault.getVaultKeyPath()));

	}
	
	public void setProtectedCredentialPath(VaultType vault, String path){
		SecurityBean org_sm = KeyService.getPrimarySymmetricKey(vault.getServiceUser().getOrganizationId());
		vault.setProtectedCredentialPath(SecurityUtil.encipher(org_sm, path.getBytes()));
	}
	
	private String getProtectedCredentialPath(VaultType vault){
		SecurityBean org_sm = KeyService.getPrimarySymmetricKey(vault.getServiceUser().getOrganizationId());
		return new String(SecurityUtil.decipher(org_sm, vault.getProtectedCredentialPath()));

	}
	
	public boolean createVault(VaultBean vault, CredentialType credential){
		try{
		if (vault.getHaveVaultKey()== true){
			logger.info("Vault key already exists for " + vault.getVaultName());
			return true;
		}


		
		setProtected(vault, credential);
		setVaultKeyPath(vault);
		String vaultKeyPath = getVaultKeyPath(vault);
		File vaultFile = new File(vaultKeyPath);
		if (vaultFile.exists()){
			logger.error("Vault Key Path already exists: " + vault.getVaultKeyPath());
			return false;
		}
		//vault.setVaultKeyPath(vault.getVaultPath() + File.separator + vault.getVaultNameHash() + "-" + vault.getKeyPrefix() + (vault.getProtected() ? vault.getKeyProtectedPrefix() : "") + vault.getKeyExtension());

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

		byte[] private_key_config = SecurityUtil.serializeToXml(sm, true, true, true).getBytes("UTF-8");
		
		String in_password = null;
		if(vault.getProtectedCredential() != null && (vault.getProtectedCredential().getCredentialType() == CredentialEnumType.ENCRYPTED_PASSWORD || vault.getProtectedCredential().getCredentialType() == CredentialEnumType.HASHED_PASSWORD)){
			in_password = new String(getProtectedCredentialValue(vault.getProtectedCredential()));
		}
		
		// If a password was specified, encrypt with password
		//
		if (vault.getProtected() && in_password != null && in_password.length() > 0)
		{
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
		
		/// Use promote to clone the vault. The private key is set on the vault below, and this public copy will contain the public key
		/// This also leaves a copy of relevent meta data in the database so configuration to find the other configuration isn't needed
		/// The main challenge being solved is, given a vault id, make it straightforward to find the corresponding offline configuration for that id
		/// By using the vault object vs. the key itself, it makes the credentialtype variable, and therefore easier to swap out in the future,
		/// Such as for certificates, or any other desired cryptograpic configuration that is desired
		///
		
		VaultBean pubVault = newVault(vault.getServiceUser(), getVaultPath(vault),vault.getVaultName());
		
		pubVault.setHaveVaultKey(true);
		pubVault.setProtectedCredentialPath(vault.getProtectedCredentialPath());
		CredentialType pubVaultCred = new CredentialType();
		pubVaultCred.setNameType(NameEnumType.CREDENTIAL);
		pubVaultCred.setEnciphered(true);
		pubVaultCred.setCredentialType(CredentialEnumType.KEY);
		pubVaultCred.setCredential(public_key_config);
		pubVaultCred.setCreatedDate(pubVault.getCreated());
		
		pubVault.setCredential(pubVaultCred);
		pubVault.setProtected(vault.getProtected());
		pubVault.setHaveCredential(true);
		pubVault.setVaultDataUrn(UrnUtil.getUrn(imp_data));
		vault.setVaultDataUrn(pubVault.getVaultDataUrn());
		DataUtil.setValue(imp_data, exportVault(pubVault).getBytes());
		
		//DataUtil.setValue(imp_data, public_key_config);

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
		FileUtil.emitFile(vaultKeyPath, exportVault(vault));


		}
		catch(UnsupportedEncodingException | DataException | FactoryException | ArgumentException | NullPointerException e){
			logger.error(FactoryException.LOGICAL_EXCEPTION, e);
		}
		return true;
	}
	
	public byte[] getCertificate(VaultType vault, boolean isPrivate){
		String path = getVaultPath(vault) + "/certificates/" + (isPrivate ? "private" : "signed") + "/" + vault.getVaultAlias() + "." + (isPrivate ? "p12" : "cert");
		return FileUtil.getFile(path);
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
				boolean outBool = false;
				logger.info("Exporting PKCS12 Certificate " + vault.getVaultAlias() + " ...");
				boolean exportP12Private = sslUtil.exportPKCS12PrivateCertificate(vault.getVaultAlias(), password.toCharArray(),alias);
				if(!exportP12Private){
					logger.error("Failed to export private key");
					return outBool;
				}

				boolean exportP12Public = sslUtil.exportPKCS12PublicCertificate(alias, password.toCharArray());
				if(!exportP12Public){
					logger.error("Failed to export public key");
					return outBool;
				}
				outBool = true;
				logger.info("Exported PKCS12 Certificates");
				return outBool;
				
				
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
	
	/// When loading a vault via urn, this method will be invoked twice: Once for the meta data reference in the database which is used to find the vault, and Two for the vault itself
	/// Therefore, log statements are dialed down to debug, otherwise it looks like the call to initialize twice is in error
	///
	public void initialize(VaultType vault, CredentialType credential) throws ArgumentException, FactoryException{
		
		if(vault.getServiceUser() == null && vault.getServiceUserUrn() != null){
			vault.setServiceUser(Factories.getNameIdFactory(FactoryEnumType.USER).getByUrn(vault.getServiceUserUrn()));
		}
		if(vault.getServiceUser() == null){
			throw new ArgumentException("Unable to locate service user '" + vault.getServiceUserUrn() + "'");
		}
		if(vault.getVaultPath().length == 0) throw new ArgumentException("Invalid base path");

		String vaultPath = getVaultPath(vault);
		logger.debug("Initializing Vault '" + vault.getVaultName() + "' In " + vaultPath);
		if (FileUtil.makePath(vaultPath) == false)
		{
			throw new ArgumentException("Unable to create path to " + vaultPath);
		}

		Factories.getNameIdFactory(FactoryEnumType.USER).populate(vault.getServiceUser());
		vault.setOrganizationId(vault.getServiceUser().getOrganizationId());
		// Check for non-password protected file
		//
		File vaultKeyFile = new File(getVaultKeyPath(vault));
		if (vaultKeyFile.exists())
		{
			vault.setHaveVaultKey(true);
			setProtected(vault, credential);
		}
		vault.setInitialized(true);
	}

	public DirectoryGroupType getVaultGroup(VaultType vault) throws FactoryException, ArgumentException{
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "getVaultGroup", AuditEnumType.USER, vault.getServiceUserUrn());
		AuditService.targetAudit(audit, AuditEnumType.GROUP, ".vault");
		return BaseService.readByNameInParent(audit, AuditEnumType.GROUP, vault.getServiceUser(), vault.getServiceUser().getHomeDirectory(), vault.getVaultGroupName(), "DATA");
	}
	public DirectoryGroupType getVaultInstanceGroup(VaultType vault) throws FactoryException, ArgumentException{
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "getVaultInstanceGroup", AuditEnumType.USER, vault.getServiceUserUrn());
		AuditService.targetAudit(audit, AuditEnumType.GROUP, vault.getVaultName());
		return BaseService.readByNameInParent(audit, AuditEnumType.GROUP, vault.getServiceUser(), getVaultGroup(vault), vault.getVaultName(), "DATA");
	}
	
	
	private AuditType beginAudit(VaultType vault,  ActionEnumType action, String actionName, boolean requireInit){
		AuditType audit = AuditService.beginAudit(action, actionName, AuditEnumType.USER, "Anonymous");
		if(requireInit && (vault.getInitialized() == false || vault.getServiceUser() == null)){
			AuditService.denyResult(audit, "Vault is not properly initialized");
			return null;
		}
		audit.setAuditSourceData(vault.getServiceUserUrn());
		audit.setAuditTargetType(AuditEnumType.VAULT);
		audit.setAuditTargetData(vault.getVaultDataUrn());
		return audit;
	}
	
	
	public boolean deleteVault(VaultType vault) throws ArgumentException, FactoryException
	{
		AuditType audit = beginAudit(vault,ActionEnumType.DELETE, "Delete vault",true);

		logger.info("Cleaning up vault instance");
		if (vault.getHaveVaultKey() == false){
			logger.warn("No key detected, so nothing is deleted");
		}
		if (vault.getVaultKeyPath() == null){
			logger.warn("Path is null");
		}
		else{
			File vaultKeyFile = new File(getVaultKeyPath(vault));
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
		cacheByUrn.remove(vault.getVaultDataUrn());
		vault.setVaultDataUrn(null);
		vault.setVaultKeyPath(null);
		vault.setHaveCredential(false);
		vault.setProtectedCredential(null);
		vault.setActiveKey(null);
		vault.setVaultKey(null);
		vault.setCredential(null);
		
		AuditService.permitResult(audit, "TODO: Add authZ check");
		return true;
	}
	private DataType getVaultMetaData(VaultBean vault) throws FactoryException, ArgumentException{
		AuditType audit = beginAudit(vault,ActionEnumType.READ, "getVaultMetaData",true);
		return BaseService.readByName(audit, AuditEnumType.DATA, vault.getServiceUser(),  getVaultGroup(vault), vault.getVaultName());
	}
	
	/// Creates a new symmetric key within the vault group/data structure 
	/// This is currently using the older key storage style than the symmetrickeys table because (a) the keys don't hold any relationship reference other than vault ids, and (b) that makes it harder to determine if the key is still used without scanning every other table that includes a cipherkey reference
	/// By keeping it in the vault meta data (where it's effectively the same level of protection (or lack of), the keys can more easily be cleaned up by simply deleting the vault
	/// This could be refactored by defining groups of symmetric keys vs. groups of data items containing the keys and associating that group relative to the vault group
	///
	public boolean newActiveKey(VaultBean vault) throws FactoryException, ArgumentException, DataException, UnsupportedEncodingException{
		
		
		AuditType audit = beginAudit(vault,ActionEnumType.ADD, "Vault key",true);
		DataType imp_data = getVaultMetaData(vault);

		// Can't make active key
		//
		if (imp_data == null){
			AuditService.denyResult(audit, "Vault implementation data is null");
			return false;
		}
		AuditService.targetAudit(audit, AuditEnumType.DATA, imp_data.getUrn());
		VaultType pubVault = JSONUtil.importObject(new String(DataUtil.getValue(imp_data)), VaultType.class);
		if(pubVault == null){
			AuditService.denyResult(audit, "Cannot restore public vault");
			return false;
		}

		// Import the key, and specify that the DES key is encrypted
		//
		SecurityBean sm = SecurityFactory.getSecurityFactory().createSecurityBean(pubVault.getCredential().getCredential(), true);
		if (sm == null){
			AuditService.denyResult(audit, "SecurityBean for Vault implementation is null");
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
			AuditService.permitResult(audit, "Created new vault key");
			return true;
		}
		else{
			AuditService.denyResult(audit, "Failed to add new vault key");
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
						//logger.info("Deciphering private key with salt " + new String(credSalt.getSalt()));
						//logger.warn("Check Pwd: " + new String(getProtectedCredentialValue(vault.getProtectedCredential())) + " / Salt = " + credSalt.getSalt());
						dec_config = SecurityUtil.decipher(dec_config, new String(getProtectedCredentialValue(vault.getProtectedCredential())),credSalt.getSalt());
					}
					if (dec_config.length == 0) return null;

					vault.setVaultKeyBean(SecurityFactory.getSecurityFactory().createSecurityBean(dec_config, true));
				}
				catch (Exception e)
				{
					logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
	
	private SecurityBean getVaultCipher(VaultBean vault, String keyId) throws FactoryException, ArgumentException, DataException{
		if(vault.getSymmetricKeyMap().containsKey(keyId)){
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
	
	
	public void setVaultBytes(VaultBean vault, DataType data, byte[] inData) throws DataException, FactoryException, UnsupportedEncodingException, ArgumentException
	{
		if (vault.getActiveKey() == null || vault.getActiveKeyId() == null){
			if(!newActiveKey(vault)){
				throw new FactoryException("Failed to establish active key");
			}
			if (vault.getActiveKey() == null)
			{
				throw new FactoryException("Active key is null");
			}
		}

		data.setCompressed(false);
		data.setDataHash(SecurityUtil.getDigestAsString(inData,new byte[0]));

		if (inData.length > 512 && DataUtil.tryCompress(data))
		{
			inData = ZipUtil.gzipBytes(inData);
			data.setCompressed(true);
			data.setCompressionType(CompressionEnumType.GZIP);
		}
		data.setVaulted(true);
		DataUtil.setValue(data,SecurityUtil.encipher(vault.getActiveKeyBean(), inData));
		data.setKeyId(vault.getActiveKeyId());
		data.setVaultId(vault.getVaultDataUrn());

	}
	public byte[] extractVaultData(VaultBean vault, DataType inData) throws FactoryException, ArgumentException, DataException
	{
		byte[] outBytes = new byte[0];
		if(vault == null){
			logger.error("Vault reference is null");
			return outBytes;
		}
		if (vault.getHaveVaultKey() == false || inData.getVaulted() == false){
			logger.warn("Data is not vaulted or the vault key is not specified");
			return outBytes;
		}

		// If the data vault id isn't the same as this vault name, then it can't be decrypted.
		//
		if (vault.getVaultDataUrn().equals(inData.getVaultId()) == false){
			logger.error("Data vault id '" + inData.getVaultId() + "' does not match the specified vault name '" + vault.getVaultDataUrn() + "'.  This is a precautionary/secondary check, probably due to changing the persisted vault configuration name");
			return outBytes;
		}

		return getVaultBytes(vault,inData, getVaultCipher(vault,inData.getKeyId()));

	}
	public static byte[] getVaultBytes(VaultBean vault, DataType data, SecurityBean bean) throws DataException
	{
		
		if(bean == null){
			logger.error("Vault cipher for " + data.getKeyId() + " is null");
			return new byte[0];
		}
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
	/// Use this method with BaseService.add
	/// The TypeSanitizer will take care of setVaultBytes
	/// Otherwise, for direct factory adds (such as with bulk inserts), use setVaultBytes and invoke the add method directly on the factory
	public DataType newVaultData(VaultBean vault, UserType dataOwner, String name, DirectoryGroupType group, String mimeType, byte[] inData, byte[] clientCipher) throws FactoryException, ArgumentException, DataException, UnsupportedEncodingException
	{
		
		boolean encipher = (clientCipher != null && clientCipher.length > 0 ? true : false);
		if (inData == null || inData.length == 0) return null;

		DataType outData = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(dataOwner, group.getId());
		outData.setName(name);
		outData.setMimeType(mimeType);
		outData.setGroupPath(group.getPath());
		if (encipher && clientCipher.length > 0)
		{
			outData.setCipherKey(clientCipher);
			outData.setEncipher(true);
		}
		
		//setVaultBytes(vault, outData, inData);
		DataUtil.setValue(outData, inData);
		outData.setVaulted(true);
		outData.setVaultId(vault.getVaultDataUrn());
		return outData;
	}
	
	/// return a list of the PUBLIC vault configurations
	/// These have the same data as the PRIVATE configuration, with the exception of which key is held
	/// 

	
	public List<VaultType> listVaultsByOwner(UserType owner) throws FactoryException, ArgumentException, DataException{
		VaultType vault = new VaultType();
		((NameIdFactory)Factories.getFactory(FactoryEnumType.USER)).populate(owner);
		vault.setServiceUser(owner);
		vault.setServiceUserUrn(owner.getUrn());

		/// Using the default group location ("~/.vault)
		///
		DirectoryGroupType dir = getVaultGroup(vault);
		if(dir == null){
			return new ArrayList<VaultType>();
		}

		List<DataType> dataList = BaseService.listByGroup(AuditEnumType.DATA, "DATA", dir.getObjectId(), 0L, 0, owner);
		
		List<VaultType> vaults = new ArrayList<>();
		for(DataType data : dataList){
			VaultBean vaultb = getVaultByUrn(owner, data.getUrn());
			if(vaultb != null) vaults.add(vaultb);
		}

		return vaults;
	}
	
	/// User provided for context authorization
	///
	public VaultBean getVaultByUrn(UserType user, String urn){
		if(cacheByUrn.containsKey(urn)) return cacheByUrn.get(urn);
		
		DataType data = BaseService.readByUrn(AuditEnumType.DATA, urn, user);
		if(data == null){
			logger.error("Data is null for urn '" + urn + "'");
			return null;
		}
		return getVault(user, data);
	}
	/// User provided for context authorization
	///
	public VaultBean getVaultByObjectId(UserType user, String objectId){
		if(cacheByUrn.containsKey(objectId)) return cacheByUrn.get(objectId);
		
		DataType data = BaseService.readByObjectId(AuditEnumType.DATA, objectId, user);
		if(data == null){
			logger.error("Data is null for object id '" + objectId + "'");
			return null;
		}
		return getVault(user, data);
	}
	
	private VaultBean getVault(UserType user, DataType data){
		VaultBean vault = null;
		VaultType pubVault = null;


		try {
			pubVault = JSONUtil.importObject(new String(DataUtil.getValue(data)), VaultType.class);
			initialize(pubVault, null);
		} catch ( DataException | ArgumentException | FactoryException e) {
			logger.error(e);
		}

		
		if(pubVault == null){
			logger.error("Vault reference could not be restored");
			return null;
		}
		
		String vaultPath = getVaultPath(pubVault);
		String credPath = getProtectedCredentialPath(pubVault);

		CredentialType cred = loadProtectedCredential(credPath);

		vault = loadVault(vaultPath, pubVault.getVaultName(), pubVault.getProtected());
		if(vault == null){
			logger.error("Failed to restore vault");
			return null;
		}
		try {
			initialize(vault, cred);
		} catch (ArgumentException | FactoryException e) {
			logger.error(e);
			vault = null;
		}
		cacheByUrn.put(data.getUrn(), vault);
		return vault;
	}
	
	/// TODO: Deprecate?
	private List<SecurityBean> getCiphers(VaultBean vault){
		List<SecurityBean> beans = new ArrayList<SecurityBean>();
		
		try {
			List<DataType> dataList = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataListByGroup(getVaultInstanceGroup(vault), false, 0, 0, vault.getServiceUser().getOrganizationId());
			for(int i = 0; i < dataList.size();i++) beans.add(getCipherFromData(vault,dataList.get(i)));
		} catch (FactoryException | ArgumentException | DataException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return beans;
	}
	
	
	/// TODO: Deprecate?
	private boolean updateImprovedData(VaultBean vault, DataType inData, byte[] in_bytes) throws FactoryException, ArgumentException, UnsupportedEncodingException, DataException, DataAccessException
	{

		if (inData == null) return false;

		DirectoryGroupType loc_imp_dir = getVaultInstanceGroup(vault);

		// if there is no localized improvement in the database, then don't continue
		//
		if (loc_imp_dir == null) return false;

		// If there is no active key, then load the public key for the improvement, which will cause a new SecretKey to be created
		// And then add the DES key export to the improvement for later reference
		//
		if (vault.getActiveKey() == null)
		{
			if(inData.getKeyId() != null) vault.setActiveKeyId(inData.getKeyId());
			else newActiveKey(vault);
		}

		if (vault.getActiveKey() == null || vault.getActiveKeyId() == null) return false;
		setVaultBytes(vault, inData, in_bytes);

		return ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).update(inData);
	}
	public static String reportCacheSize(){
		return "VaultService Cache Report\ncacheByUrn\t" + cacheByUrn.keySet().size() + "\n";
	}

}

