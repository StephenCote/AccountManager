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

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.binary.Hex;
import org.apache.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.data.security.OrganizationSecurity;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.CompressionEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.accountmanager.util.ZipUtil;

	public class VaultService
	{
		
		public static final Logger logger = Logger.getLogger(VaultService.class.getName());
		
		private static String VAULT_GROUP_NAME = ".vault";

		private static String KEY_EXT = ".mprv";
		private static String VAULT_KEY_PREF = "Vault";
		private static String VAULT_KEY_PWD = "Prot";

		//private String keyPath = null;

		private String vaultPath = null;
		private String vaultKeyPath = null;
		private boolean haveVaultKey = false;
		private String vaultName = null;
		private String vaultNameHash = null;

		private SecurityBean activeKey = null;
		private SecurityBean vaultKey = null;
		
		private UserType vaultServiceUser = null;

		private String activeKeyId = null;
		private Map<String, SecurityBean> symmetric_keys = null;
		private boolean passwordProtected = false;
		private boolean havePassword = false;
		private String password = null;
		private OrganizationType organization = null;
		
		private byte[] dataCipher = new byte[0];
		
		/// TODO: For CredentialType update, this will go away
		///
		/*
		private static byte[] defaultSalt = new byte[]{
			-124,-25,48,114,-70,-7,-26,31,18,10,40,44,64,-97,27,-39
		};
		*/
		//private static byte[] salt = new byte[0];
		
		//private CredentialType credSalt = null;
		
		//private SecurityFactory securityFactory = null;
		public VaultService(UserType serviceUser,String vaultBasePath, String vaultName)
		{
			vaultServiceUser = serviceUser;
			try {
				this.organization = Factories.getOrganizationFactory().getOrganizationById(serviceUser.getOrganizationId());
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.vaultName = vaultName;
			vaultNameHash = Hex.encodeHexString(vaultName.getBytes());//SecurityUtil.getDigestAsString(vaultName);
			
			vaultPath = vaultBasePath;
			//securityFactory = SecurityFactory.getSecurityFactory();
		}
		public void clearCache(){
			unsetActiveKey();
			symmetric_keys.clear();
		}
		public String getVaultPath() {
			return vaultPath;
		}
		public String getVaultName() {
			return vaultName;
		}
		public SecurityBean getActiveKey() {
			return activeKey;
		}
		public void unsetActiveKey(){
			this.activeKey = null;
			this.activeKeyId = null;
		}
		public void setActiveKey(String keyName) throws FactoryException, ArgumentException, DataException {
			SecurityBean bean = this.getVaultCipher(keyName);
			if(bean != null){
				this.activeKey = bean;
				this.activeKeyId = keyName;
			}
		}

		public String getActiveKeyId() {
			return activeKeyId;
		}
		
		public String getVaultKeyPath() {
			return vaultKeyPath;
		}

		public String getVaultNameHash() {
			return vaultNameHash;
		}

		public void setDataCipher(byte[] dataCipher) {
			this.dataCipher = dataCipher;
		}
		public CredentialType getSalt(){
			DirectoryGroupType dir=null;
			try {
				dir = getVaultGroup();
			} catch (FactoryException | ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(dir == null) return null;
			return CredentialService.getPrimaryCredential(dir, CredentialEnumType.SALT, true);
		}
/*
		public byte[] getSalt(){
			if(salt.length == 0) return defaultSalt;
			return salt;
		}
		public void setSalt(byte[] salt){
			this.salt = salt;
		}
*/
		public void initialize() throws ArgumentException, FactoryException{
			
			if(vaultPath == null || vaultPath.length() == 0) throw new ArgumentException("Invalid base path");

			symmetric_keys = new HashMap<String, SecurityBean>();
			logger.info("Initializing Vault Service In " + vaultPath);
			if (FileUtil.makePath(vaultPath) == false)
			{
				throw new ArgumentException("Unable to create path to " + vaultPath);
			}
			
			Factories.getUserFactory().populate(vaultServiceUser);
	
			// Check for non-password protected file
			//
			File vaultKeyFile = new File(vaultPath + File.separator + vaultNameHash + "-" + VAULT_KEY_PREF + KEY_EXT);
			if (vaultKeyFile.exists())
			{
				vaultKeyPath = vaultPath + File.separator + vaultNameHash + "-" + VAULT_KEY_PREF + KEY_EXT;
				haveVaultKey = true;
			}
			else
			{
				vaultKeyFile = new File(vaultPath + File.separator + vaultNameHash + "-" + VAULT_KEY_PREF + VAULT_KEY_PWD + KEY_EXT);
				if (vaultKeyFile.exists())
				{
					vaultKeyPath = vaultPath + File.separator + vaultNameHash + "-" + VAULT_KEY_PREF + VAULT_KEY_PWD + KEY_EXT;
					haveVaultKey = true;
					passwordProtected = true;
				}
			}
		}
		
		public void setPassword(String value)
		{
				password = value;
				if (password != null && password.length() > 0)
				{
					this.havePassword = true;
					// If the password can't open the vault key, then it's invalid
					//
					if (getVaultKey() == null)
					{
						havePassword = false;
						password = null;
					}
				}
				else
				{
					havePassword = false;
					vaultKey = null;
				}
		}
		
		/// NOTE: At the moment, this method quietly/wrongfully always assumes the vault was password protected to begin with
		/// and doesn't catch the null password case, and/or allow for setting the password on an unprotected vault
		///
		public boolean changeVaultPassword(String new_password) throws ArgumentException
		{
			if(new_password == null || new_password.length() == 0 || haveVaultKey == false || (passwordProtected && password == null)){
				throw new ArgumentException("Invalid argument or configuration");
			}


			byte[] config_bytes = FileUtil.getFile(vaultKeyPath);
			if (config_bytes.length == 0) return false;

			SecurityBean org_sm = KeyService.getPrimarySymmetricKey(organization.getId());
					//OrganizationSecurity.getSecurityBean(organization.getId());
			
			byte[] dec_config = SecurityUtil.decipher(org_sm,  config_bytes);
			CredentialType credSalt = getSalt();
			if(credSalt == null){
				logger.info("Salt is null");
				return false;
			}
			dec_config = SecurityUtil.decipher(dec_config, password,credSalt.getSalt());
			if (dec_config.length == 0) throw new ArgumentException("Failed to decipher config");

			dec_config = SecurityUtil.encipher(dec_config, new_password,credSalt.getSalt());

			// Encipher with product key
			//
			byte[] enc_private_key = SecurityUtil.encipher(org_sm, dec_config);
			FileUtil.emitFile(vaultKeyPath, enc_private_key);

			this.password = new_password;
			this.vaultKey = null;
			if (getVaultKey() == null) return false;
			return true;
		}
		
		public SecurityBean getVaultKey()
		{
				if (this.haveVaultKey == false || (this.passwordProtected && this.password == null)){
					if(this.passwordProtected) logger.error("Vault password was not specified");
					return null;
				}
				if (this.vaultKey == null)
				{
					try
					{
						byte[] config_bytes = FileUtil.getFile(this.vaultKeyPath);
						if (config_bytes.length == 0) return null;
						//Core.Tools.Security.SecurityManager org_sm = (Core.Tools.Security.SecurityManager)product.SecurityManager;
						SecurityBean org_sm = KeyService.getPrimarySymmetricKey(organization.getId()); 
								///OrganizationSecurity.getSecurityBean(organization.getId());
						byte[] dec_config = SecurityUtil.decipher(org_sm,config_bytes);
						CredentialType credSalt = getSalt();
						if(credSalt == null){
							logger.info("Salt is null");
							return null;
						}
						if (passwordProtected) dec_config = SecurityUtil.decipher(dec_config, password,credSalt.getSalt());
						if (dec_config.length == 0) return null;

						vaultKey = SecurityFactory.getSecurityFactory().createSecurityBean(dec_config, true); 
					}
					catch (Exception e)
					{
						logger.error(e.getMessage());
					}
				}
				return vaultKey;

		}
		
		

		public boolean getIsImproved()
		{
			return haveVaultKey;
		}
		public DirectoryGroupType getVaultGroup() throws FactoryException, ArgumentException{
			return Factories.getGroupFactory().getDirectoryByName(VAULT_GROUP_NAME, vaultServiceUser.getHomeDirectory(), organization.getId());
		}
		public DirectoryGroupType getVaultInstanceGroup() throws FactoryException, ArgumentException{
			return Factories.getGroupFactory().getDirectoryByName(vaultName,getVaultGroup(), organization.getId());
		}
		public boolean deleteVault() throws ArgumentException, FactoryException
		{
			logger.info("Cleaning up vault instance");
			if (this.haveVaultKey == false){
				logger.warn("No key detected, so nothing is deleted");
				//return true;
			}
			if (this.vaultKeyPath == null){
				logger.warn("Path is null");
				//return false;
			}
			else{
				File vaultKeyFile = new File(vaultKeyPath);
				if(vaultKeyFile.delete() == false) throw new ArgumentException("Unable to delete vault key file " + vaultKeyFile);
			}

			// Flood 1MB of data over the file.
			//
			/*
			byte[] random = new byte[1048576];
			Random rand = new Random();
			rand.NextBytes(random);
			Core.Util.IO.FileUtil.EmitTextFile(vaultKeyPath, random, true);

			File.Delete(this.vaultKeyPath);
			*/
			
			DirectoryGroupType local_imp_dir = getVaultInstanceGroup();
			
			if (local_imp_dir != null && !Factories.getGroupFactory().deleteDirectoryGroup(local_imp_dir))
			{
				logger.warn("Unable to delete keys from vault directory");
			}
			DirectoryGroupType vaultGroup = getVaultGroup();
			if(vaultGroup != null){
				DataType imp_data = Factories.getDataFactory().getDataByName(vaultName, true,vaultGroup);
				if(imp_data != null && !Factories.getDataFactory().deleteData(imp_data)){
					logger.warn("Unable to delete improvement key");
				}
			}
			vaultKeyPath = null;
			haveVaultKey = false;

			return true;
		}
		
		public boolean createVault() throws FactoryException, ArgumentException, UnsupportedEncodingException, DataException
		{
			return createVault(null);
		}
		public boolean createVault(String in_password) throws FactoryException, ArgumentException, UnsupportedEncodingException, DataException
		{
			if (this.haveVaultKey == true){
				logger.info("Vault key already exists for " + vaultName);
				return true;
			}
			
			else if (this.vaultKeyPath != null){
				logger.error("Vault Key Path already exists: " + vaultKeyPath);
				return false;
			}
			
			DirectoryGroupType imp_dir = Factories.getGroupFactory().getCreateDirectory(vaultServiceUser, VAULT_GROUP_NAME, vaultServiceUser.getHomeDirectory(), organization.getId());

			DataType imp_data = Factories.getDataFactory().getDataByName(vaultName, true, imp_dir);
			if (imp_data != null)
			{
				logger.error("Vault for '" + vaultName + "' could not be made.  Machine must first be unimproved.");
				return false;
			}
			
			CredentialType credSalt = CredentialService.newCredential(CredentialEnumType.SALT, null, vaultServiceUser, imp_dir, new byte[0], true, false,false);
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
			

			// If a password was specified, encrypt with password
			//
			if (in_password != null && in_password.length() > 0)
			{
				this.passwordProtected = true;
				vaultKeyPath = vaultPath + File.separator + vaultNameHash + "-" + VAULT_KEY_PREF + VAULT_KEY_PWD + KEY_EXT;
				this.password = in_password;
				logger.info("Private key config: " + private_key_config.length);
				private_key_config = SecurityUtil.encipher(private_key_config, password, credSalt.getSalt()); 
			}
			else{
				this.passwordProtected = false;
				vaultKeyPath = vaultPath + File.separator + vaultNameHash + "-" + VAULT_KEY_PREF + KEY_EXT;
			}

			// Encipher with product key
			//
			SecurityBean org_sm = KeyService.getPrimarySymmetricKey(organization.getId()); 
					//OrganizationSecurity.getSecurityBean(organization.getId());
			byte[] enc_private_key = SecurityUtil.encipher(org_sm,private_key_config);
			FileUtil.emitFile(vaultKeyPath, enc_private_key);

			// Create a new group directory for storing DES keys that are vaulted for a specified vault
			// The name is the same as the vault data name
			//
			DirectoryGroupType local_imp_dir = Factories.getGroupFactory().getCreateDirectory(vaultServiceUser, vaultName, imp_dir, organization.getId());

			// No need to encrypt the public key beyond the auto-encrypt supplied by the AM org-level key (same key as used by default to encrypt private key)
			//
			byte[] public_key_config = SecurityUtil.serializeToXml(sm, false, true, false).getBytes("UTF-8");

			imp_data = Factories.getDataFactory().newData(vaultServiceUser, imp_dir.getId());
			imp_data.setName(vaultName);
			imp_data.setDescription("Vault public key for node/cluster");
			imp_data.setMimeType("text/xml");
			DataUtil.setValue(imp_data, public_key_config);

			Factories.getDataFactory().addData(imp_data);
			
			haveVaultKey = true;

			vaultKey = sm;

			return true;
		}
		public List<SecurityBean> getCiphers(){
			List<SecurityBean> beans = new ArrayList<SecurityBean>();
			
			try {
				List<DataType> dataList = Factories.getDataFactory().getDataListByGroup(getVaultInstanceGroup(), false, 0, 0, organization.getId());
				for(int i = 0; i < dataList.size();i++) beans.add(getCipherFromData(dataList.get(i)));
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			} catch (DataException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
			
			
			return beans;
		}
		
		/// NOTE:
		///		The Volatile Key includes the deciphered/exposed private key from the vaultKey
		///		The private key should be immediately null'd after decrypting the secret key
		///		
		private SecurityBean getVolatileVaultKey()
		{
			if (this.haveVaultKey == false || getVaultKey() == null){
				logger.error("Vault is not initialized correctly.  The vault key is not present.");
				return null;
			}

			SecurityBean out_bean = new SecurityBean();
			
			out_bean.setEncryptCipherKey(vaultKey.getEncryptCipherKey());
			out_bean.setCipherIV(vaultKey.getCipherIV());
			out_bean.setCipherKey(vaultKey.getCipherKey());
			out_bean.setPrivateKeyBytes(vaultKey.getPrivateKeyBytes());
			out_bean.setPrivateKey(vaultKey.getPrivateKey());
			out_bean.setPublicKeyBytes(vaultKey.getPublicKeyBytes());
			out_bean.setPublicKey(vaultKey.getPublicKey());
			out_bean.setSecretKey(vaultKey.getSecretKey());

			return out_bean;
		
		}
		
		private SecurityBean getCipherFromData(DataType data) throws DataException{
			// Get a mutable security manager to swap out the DES keys on
			// The Volatile Key includes the exposed private key, so it's immediately wiped from the object after decrypting the cipher key
			//
			SecurityBean v_sm = getVolatileVaultKey();
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
			//SecurityBean keyBean = SecurityFactory.getSecurityFactory().createSecurityBean(dataBytes, false);
			//SecurityFactory.getSecurityFactory().setSecretKey(v_sm, keyBean.getCipherKey(), keyBean.getCipherIV(), true);
			return v_sm;
		}
		public SecurityBean getVaultCipher(String name) throws FactoryException, ArgumentException, DataException{
			if (this.symmetric_keys.containsKey(name) == false)
			{
				// Get the encrypted DES keys for this data item.
				//
				DataType key = Factories.getDataFactory().getDataByName(name, getVaultInstanceGroup());
				if (key == null){
					logger.error("Vault key " + name + " does not exist");
					return null;
				}

				SecurityBean v_sm = getCipherFromData(key);
				if(v_sm == null){
					logger.error("Failed to restore cipher from data");
					return null;
				}
				this.symmetric_keys.put(name, v_sm);
			}
			return this.symmetric_keys.get(name);
		}
		public byte[] extractVaultData(DataType in_data) throws FactoryException, ArgumentException, DataException
		{
			byte[] out_bytes = new byte[0];
			if (this.haveVaultKey == false || in_data.getVaulted() == false) return out_bytes;

			// If the data vault id isn't the same as this vault name, then it can't be decrypted.
			//
			if (vaultName.equals(in_data.getVaultId()) == false){
				logger.error("Data vault id '" + in_data.getVaultId() + "' does not match the specified vault name '" + vaultName + "'");
				return out_bytes;
			}

			return getVaultBytes(in_data, getVaultCipher(in_data.getKeyId()));

		}
		public byte[] encipher(byte[] in_data) throws FactoryException{
			if(activeKey == null) throw new FactoryException("Active key is null");
			return SecurityUtil.encipher(getActiveKey(), in_data);
		}
		public byte[] decipher(byte[] in_data) throws FactoryException{
			if(activeKey == null) throw new FactoryException("Active key is null");
			return SecurityUtil.decipher(getActiveKey(), in_data);
		}
		public void setVaultBytes(DataType data, byte[] in_data) throws DataException, FactoryException, UnsupportedEncodingException, ArgumentException
		{
			if (activeKey == null || activeKeyId == null){
				newActiveKey();
				if (activeKey == null)
				{
					throw new FactoryException("Active key is null");
				}
			}

			data.setCompressed(false);
			data.setDataHash(SecurityUtil.getDigestAsString(in_data));

			if (in_data.length > 512 && DataUtil.tryCompress(data))
			{
				in_data = ZipUtil.gzipBytes(in_data);
				data.setCompressed(true);
				data.setCompressionType(CompressionEnumType.GZIP);
			}
			data.setVaulted(true);
			DataUtil.setValue(data,SecurityUtil.encipher(getActiveKey(), in_data));
			//data.setDataBytesStore(in_data);
			//data.Value = vault.ActiveKey.Encipher(in_data);
			data.setKeyId(getActiveKeyId());
			data.setVaultId(getVaultName());

		}
		public static byte[] getVaultBytes(DataType data, SecurityBean bean) throws DataException
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
		public DataType newVaultData(String name, DirectoryGroupType group, String mime_type, byte[] in_data, boolean encipher) throws FactoryException, ArgumentException, DataException, UnsupportedEncodingException
		{
			if (in_data == null || in_data.length == 0) return null;

			/*
			DirectoryGroupType loc_imp_dir = getVaultInstanceGroup();

			if (loc_imp_dir == null) return null;
			*/
			// If there is no active key, then load the public key for the improvement, which will cause a new SecretKey to be created
			// And then add the DES key export to the improvement for later reference
			//


			DataType out_data = Factories.getDataFactory().newData(vaultServiceUser, group.getId());
			out_data.setName(name);
			out_data.setMimeType(mime_type);

			if (encipher && dataCipher.length > 0)
			{
				out_data.setCipherKey(dataCipher);
				out_data.setEncipher(true);
			}
			setVaultBytes(out_data, in_data);

			return out_data;
		}
		public boolean newActiveKey() throws FactoryException, ArgumentException, DataException, UnsupportedEncodingException{
			DataType imp_data = Factories.getDataFactory().getDataByName(vaultName, getVaultGroup());

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

			activeKey = sm;

			// The des key is new because the import was only the public key, and the export is encrypted.
			// The only way to get the key from the export is with the private key to decrypt it, and the private key is enciphered on the improved computer with the org-level SM.
			//
			byte[] secret_key = SecurityUtil.serializeToXml(sm, false, false, true).getBytes("UTF-8");
			String id = UUID.randomUUID().toString();
			
			DirectoryGroupType loc_imp_dir = getVaultInstanceGroup();
			DataType new_key = Factories.getDataFactory().newData(vaultServiceUser, loc_imp_dir.getId());
			new_key.setName(id);
			new_key.setMimeType("text/xml");
			new_key.setDescription("Improvement key for " + vaultName);
			DataUtil.setValue(new_key, secret_key);
			if(Factories.getDataFactory().addData(new_key)){
				activeKeyId = id;
				return true;
			}
			else{
				logger.error("Failed to add new vault key");
			}
			return false;
			
		}
		public boolean updateImprovedData(DataType in_data, byte[] in_bytes) throws FactoryException, ArgumentException, UnsupportedEncodingException, DataException, DataAccessException
		{

			if (in_data == null) return false;

			DirectoryGroupType loc_imp_dir = getVaultInstanceGroup();

			// if there is no localized improvement in the database, then don't continue
			//
			if (loc_imp_dir == null) return false;

			// If there is no active key, then load the public key for the improvement, which will cause a new SecretKey to be created
			// And then add the DES key export to the improvement for later reference
			//
			if (activeKey == null)
			{
				if(in_data.getKeyId() != null) this.setActiveKey(in_data.getKeyId());
				else newActiveKey();
			}

			if (activeKey == null || activeKeyId == null) return false;
			setVaultBytes(in_data, in_bytes);

			return Factories.getDataFactory().updateData(in_data);
		}

	}

