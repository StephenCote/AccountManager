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
package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.beans.VaultBean;
import org.cote.accountmanager.data.factory.AttributeFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.VaultService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AttributeType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.VaultType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.Test;
public class TestVaultService extends BaseDataAccessTest{
	public static final Logger logger = LogManager.getLogger(TestVaultService.class);

	private boolean resetVault = true;
	
	private VaultService vaultService = null;
	private String vaultName = null;
	private String vaultUserName = "QA Vault User";
	private String vaultUserName2 = "QA Vault User 2";
	private int keyRotations = 10;
	
	@Test
	public void TestVaultAttributes() {
		UserType vaultUser = getUser(vaultUserName, "password");
		VaultService vs = getVaultService();
		String testVaultName = getTestVaultName();
		String credPath = testProperties.getProperty("vault.credentialPath") + "/" + testVaultName + ".json";
		CredentialType cred = getProtectedCredential(vaultUser, credPath, "password");
		VaultBean vault = getCreateVault(vaultUser, testProperties.getProperty("vault.path"),testVaultName, cred,credPath);
		DirectoryGroupType dir = getGroup(vaultUser, "Data", GroupEnumType.DATA, vaultUser.getHomeDirectory());
		DataType data = this.newTextData(testVaultName + " Demo Data", "The demo data", vaultUser, dir);
		boolean error = false;
		try {
			SecurityBean cipher = KeyService.newPersonalSymmetricKey(vaultUser,false);
			AttributeFactory af = Factories.getAttributeFactory();
			data.getAttributes().add(af.newAttribute(data,"plainText","Plain text value"));
			data.getAttributes().add(af.newEncipheredAttribute(data,cipher,"cipherText","Cipher text value"));
			AttributeType vAttr = af.newAttribute(data,"vaultText","Vaulted text value");
			vs.setVaultAttributeValues(vault, vAttr);
			data.getAttributes().add(vAttr);
			
			logger.info("Updating mixed attributes: " + data.getAttributes().size());
			assertTrue("Failed to update attributes",af.updateAttributes(data));
			
			data = this.newTextData(testVaultName + " Demo Data", "The demo data", vaultUser, dir);
			af.populateAttributes(data);
			
			String testVal1 = af.getAttributeValueByName(data, "plainText");
			String testEncVal1 = af.getAttributeValueByName(data, "cipherText");
			
			String testVaultVal1 = af.getVaultAttributeValueByName(vault, data, "vaultText");
			
			assertNotNull("Plain text value is null", testVal1);
			assertNotNull("Enciphered text value is null",testEncVal1);
			assertNotNull("Vaulted text value is null",testVaultVal1);
			logger.info("Plain text val: " + testVal1);
			logger.info("Cipher text val: " + testEncVal1);
			logger.info("Vault text val: " + testVaultVal1);
			
			af.setEncipheredAttributeValues(data, "cipherText",new String[] {"New cipher text value"});

			vs.setVaultAttributeValues(vault, vAttr, new String[] {"New vault text value"});
			assertTrue("Failed to update attributes",af.updateAttributes(data));
			
			data = this.newTextData(testVaultName + " Demo Data", "The demo data", vaultUser, dir);
			af.populateAttributes(data);
			
			testEncVal1 = af.getAttributeValueByName(data, "cipherText");
			assertNotNull("Enciphered text value is null",testEncVal1);

			testVaultVal1 = af.getVaultAttributeValueByName(vault, data, "vaultText");

			assertNotNull("Vault attribute was null",testVaultVal1);
			
			logger.info("Cipher text val: " + testEncVal1);
			logger.info("Vault text val: " + testVaultVal1);
			
		} catch (ArgumentException | FactoryException | UnsupportedEncodingException | DataException | NullPointerException e) {
			logger.error(e);
			e.printStackTrace();
			error = true;
		}
		assertFalse("An error occurred",error);

	}
	
	@Test
	public void TestVaultCredential(){
		UserType vaultUser = getUser(vaultUserName, "password");
		VaultService vs = getVaultService();
		String testVaultName = getTestVaultName();
		String credPath = testProperties.getProperty("vault.credentialPath") + "/" + testVaultName + ".json";
		CredentialType cred = getProtectedCredential(vaultUser, credPath, "password");

		VaultBean vault = getCreateVault(vaultUser, testProperties.getProperty("vault.path"),testVaultName, cred,credPath);
		DirectoryGroupType dir = getGroup(vaultUser, "Data", GroupEnumType.DATA, vaultUser.getHomeDirectory());
		DataType data = this.newTextData(testVaultName + " Demo Data", "The demo data", vaultUser, dir);
		
		CredentialType cred2 = CredentialService.newHashedPasswordCredential(vaultUser, data, "password", true, vault.getVaultDataUrn());
		//logger.info(JSONUtil.exportObject(cred2));
		logger.info("Vaulting credential " + cred2.getObjectId() + " against " + vault.getVaultDataUrn() + " attached to data " + data.getUrn());
		assertTrue("Failed to validate vaulted credential",CredentialService.validatePasswordCredential(data, cred2, "password"));
	}
	
	@Test
	public void TestVaultActiveKey(){
		UserType vaultUser = getUser(vaultUserName, "password");
		VaultService vs = getVaultService();
		String testVaultName = getTestVaultName();
		String credPath = testProperties.getProperty("vault.credentialPath") + "/" + testVaultName + ".json";
		CredentialType cred = getProtectedCredential(vaultUser, credPath, "password");

		try {
			
			VaultBean vault = getCreateVault(vaultUser, testProperties.getProperty("vault.path"),testVaultName, cred,credPath);
			
			assertTrue("Failed to create new active key",vs.newActiveKey(vault));
			
			String dataToEnc = "Blah blah blah";
			String activeKeyId = vault.getActiveKeyId();
			byte[] enc = SecurityUtil.encipher(vault.getActiveKeyBean(), dataToEnc.getBytes());
			logger.info("Encrypted " + dataToEnc + " with key " + activeKeyId);
			VaultService.clearCache();
			logger.info("Resetting Active Key to test cipher protection");
			vault.setActiveKeyId(activeKeyId);
			byte[] dec = SecurityUtil.decipher(vault.getActiveKeyBean(), enc);
			String dataWasEnc = new String(dec);
			logger.info("Was encrypted: " + dataWasEnc);
			
		} catch (ArgumentException | FactoryException | DataException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	
	@Test
	public void TestVault2Data(){
		VaultService service = getVaultService();
		
		UserType vaultUser = getUser(vaultUserName, "password");
		UserType vaultUser2 = getUser(vaultUserName2, "password");
		
		/// Note about names and vaults - if you run a unit test with a static name and then clean out that temporary directory
		/// then it will break parity with the database and the vault and everything it is meant to protect will be trashed
		/// 
		String testVaultName = getTestVaultName();
		String credPath = testProperties.getProperty("vault.credentialPath") + "/" + testVaultName + ".json";
		CredentialType cred = getProtectedCredential(vaultUser2, credPath, "password");
		VaultBean vault = getCreateVault(vaultUser, testProperties.getProperty("vault.path"),testVaultName, cred,credPath);

		try {
			DirectoryGroupType dir = service.getVaultGroup(vault);
			assertTrue("Failed to authorize Vault 2 user",AuthorizationService.authorize(vaultUser, vaultUser2, dir, AuthorizationService.getViewPermissionForMapType(NameEnumType.GROUP, testUser.getOrganizationId()), true));
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			assertTrue("Vault User 2 should be able to view Vault User 1's vault group",AuthorizationService.canView(vaultUser2, dir));
		} catch (FactoryException | DataAccessException | ArgumentException e1) {
			logger.error(e1);
		}
		
		String dataName = "Vault Data - " + UUID.randomUUID().toString();
		StringBuilder buff = new StringBuilder();
		for(int i = 0; i < 250; i++) buff.append("This is the test data.  This is some of the data.  This is more of the data.");
		
		DirectoryGroupType dataDir = null;
		try {
			dataDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(vaultUser2, "Data", vaultUser2.getHomeDirectory(), vaultUser2.getOrganizationId());
			BaseService.denormalize(dataDir);
			byte[] buffBytes = buff.toString().getBytes();
			
			DataType testData = service.newVaultData(vault, vaultUser2, dataName, dataDir, "text/plain", buffBytes, null);
			boolean added = BaseService.add(AuditEnumType.DATA, testData, vaultUser2);

			assertTrue("Failed to add data", added);
		
			DataType chkData = BaseService.readByName(AuditEnumType.DATA, dataDir, dataName, vaultUser2);
			assertNotNull("Data is null",chkData);
			
			byte[] data = DataUtil.getValue(chkData);
			assertTrue("Expected data with " + buffBytes.length + " and received " + (data == null ? "null data" : data.length + " bytes"), data != null && data.length == buffBytes.length);
			
			buff = new StringBuilder();
			for(int i = 0; i < 250; i++) buff.append("This is the new data.  This is some of the new data.  This is more of the new data.  ");
			service.setVaultBytes(vault, chkData, buff.toString().getBytes());
			
			boolean updated = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).update(chkData);
			assertTrue("Failed to update data", updated);
			
			logger.info("Checking data restoration");
			
			chkData = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, dataDir);
			assertNotNull("Data is null",chkData);
			data = service.extractVaultData(vault, chkData);
			assertTrue("Expected data", data != null && data.length > 0);
			
		} catch (FactoryException | ArgumentException | UnsupportedEncodingException | DataException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION, e);
		}
	}
	
	
	private String getTestVaultName() {
		if(vaultName == null) {
			vaultName = "QA Vault - " + UUID.randomUUID().toString();
		}
		return vaultName;
	}
	private VaultService getVaultService() {
		if(vaultService == null) {
			vaultService = new VaultService(testProperties.getProperty("ssl.binary"),testProperties.getProperty("ssl.ca.path"));
		}
		return vaultService;

	}

	private VaultBean getCreateVault(UserType owner, String vaultPath, String vaultName, CredentialType protectionCredential, String credPath){
		VaultService service = getVaultService();
		VaultBean vault =  service.loadVault(vaultPath, vaultName, (protectionCredential != null ? true : false));
		if(vault == null){
			vault = service.newVault(owner, vaultPath, vaultName);
			if(credPath != null) service.setProtectedCredentialPath(vault, credPath);
			if(service.createVault(vault, protectionCredential) == false){
				vault = null;
			}
		}
		if(vault != null){
			try {
				service.initialize(vault, protectionCredential);
				SecurityBean key = service.getVaultKey(vault);
				if(key == null){
					logger.error("Failed to restore key");
					vault = null;
				}
			} catch (ArgumentException | FactoryException e) {
				logger.error(e);
				vault = null;
			}
		}
		return vault;
	}
	
	private CredentialType getLooseCredential(String password){
		CredentialType cred = new CredentialType();
		cred.setCredentialType(CredentialEnumType.HASHED_PASSWORD);
		cred.setCredential(password.getBytes());
		return cred;
	}
	private CredentialType getProtectedCredential(UserType owner, String filePath,String password){
		VaultService service = getVaultService();
		CredentialType cred = service.loadProtectedCredential(filePath);
		try{
			if(cred == null && service.createProtectedCredentialFile(owner, filePath, password.getBytes())){
				 cred = service.loadProtectedCredential(filePath);
			}
		}
		catch(ArgumentException | FactoryException e){
			logger.error(e);
		}
		return cred;
	}
	
	
	/*
	 * There's a perf issue on large bulk operations where, quite suddenly, all operations cease and the CPU spikes like it's caught in a while loop
	 * ... let's go find it
	 */
	
	@Test
	public void TestVaultMemoryPerformance(){
		VaultService service = getVaultService();
		UserType vaultUser = getUser("QA Vault User 5", "password");
		String testVaultName = getTestVaultName();
		String credPath = testProperties.getProperty("vault.credentialPath") + "/" + testVaultName + ".json";
		CredentialType cred = getProtectedCredential(vaultUser, credPath, "password");
		VaultBean vault = getCreateVault(vaultUser, testProperties.getProperty("vault.path"),testVaultName, cred,credPath);

		assertNotNull("Vault is null", vault);
		
		///String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		
		/// Rotate through 10K ciphers
		///   Watching the cache size on the vault and vault service
		try{
			logger.info("Cycle through " + keyRotations + " keyRotations");
			for(int i = 0; i < keyRotations; i++){
				service.newActiveKey(vault);
			}
			logger.info("Export current vault object");
			VaultType expVault = (VaultType)vault;
			expVault.setProtectedCredential(null);
			if(expVault.getVaultKey() != null) {
				((SecurityBean)expVault.getVaultKey()).setPrivateKey(null);
				((SecurityBean)expVault.getVaultKey()).setPublicKey(null);
				((SecurityBean)expVault.getVaultKey()).setSecretKey(null);
			}
			if(expVault.getActiveKey() != null) {
				((SecurityBean)expVault.getActiveKey()).setPrivateKey(null);
				((SecurityBean)expVault.getActiveKey()).setPublicKey(null);
				((SecurityBean)expVault.getActiveKey()).setSecretKey(null);
			}
			FileUtil.emitFile(testProperties.getProperty("vault.path") + "/vault.test.txt", JSONUtil.exportObject(expVault));

		}
		catch(Exception  e){
			logger.error(e);
		}
		try{
			logger.info("Export current vault service cache");
			FileUtil.emitFile(testProperties.getProperty("vault.path") + "/cacheReport.txt",Factories.reportCaches());
		}
		catch(NullPointerException e){
			logger.error(e);
			logger.error(e.getStackTrace());
		}
		logger.info("End");
	}
	
	/*
	@Test
	public void TestProtectedCredential(){
		VaultService service = new VaultService(testProperties.getProperty("ssl.binary"),testProperties.getProperty("ssl.ca.path"));
		UserType vaultUser4 = getUser("QA Vault User 4", "password");
		String credPath = "c:\\projects\\vault\\credentials\\development.test4.credential.json";
		CredentialType cred = getProtectedCredential(vaultUser4, credPath, "12345");
		assertNotNull("Credential is null",cred);
		String testVaultName = "Vault QA Data Test 4.5";
		VaultBean vault1 = getCreateVault(vaultUser4, "c:\\projects\\vault",testVaultName, cred, credPath);
		assertNotNull("Vault is null", vault1);
		
		try {
			List<VaultType> pubVaults = service.listVaultsByOwner(vault1.getServiceUser());
			assertTrue("No vaults were found",pubVaults.size() > 0);
			for(VaultType pvault : pubVaults){
				logger.info("Pub Vault for " + pvault.getVaultName() + " (" + pvault.getVaultDataUrn() + ")");
			}
		} catch (FactoryException | ArgumentException | DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		boolean addKey = false;
		VaultBean chkVault = service.getVaultByUrn(vault1.getServiceUser(),vault1.getVaultDataUrn());
		try {
			addKey = service.newActiveKey(chkVault);
			
		} catch (UnsupportedEncodingException | FactoryException | ArgumentException | DataException e) {
			logger.error(e);
		}
		assertTrue("Failed to create new active key",addKey);
		
	
		
		boolean newKey = false;
		try {
			newKey = service.newActiveKey(vault1);
		} catch (UnsupportedEncodingException | FactoryException | ArgumentException | DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Failed to create new key", newKey);
		//SecurityBean cipher = null;
		logger.info("Active Key Id: '" + vault1.getActiveKeyId());


	}


	@Test
	public void TestVaultChangeCleanup(){

		VaultService service = new VaultService(testProperties.getProperty("ssl.binary"),testProperties.getProperty("ssl.ca.path"));
		UserType vaultUser3 = getUser("QA Vault User 3", "password");
		String testVaultName = "Vault QA Data Test 3 - " + UUID.randomUUID().toString();
		String credPath = "c:\\projects\\vault\\credentials\\development.test3.credential.json";

		VaultBean vault1 = getCreateVault(vaultUser3, "c:\\projects\\vault",testVaultName, getLooseCredential("password"),credPath);
		
		assertNotNull("Vault is null - this may happen in a test if the working directory including the vault key is cleared, and the same vault is attempted to be created without first cleaning up the corresponding database keys", vault1);
		boolean changed = false;
		try {
			changed = service.changeVaultPassword(vault1, getLooseCredential("password"), getLooseCredential("password1"));
		} catch (ArgumentException e) {
			logger.error(e);
		}
		assertTrue("Failed to change password", changed);
		VaultBean vault11 = getCreateVault(vaultUser3, "c:\\projects\\vault", testVaultName, getLooseCredential("password1"),credPath);
		//assertNotNull("Vault is null - this may happen in a test if the working directory including the vault key is cleared, and the same vault is attempted to be created without first cleaning up the corresponding database keys", vault11);
		boolean lookup = (vault11 != null);
		if(lookup == false){
			logger.error("Vault is null - this may happen in a test if the working directory including the vault key is cleared, and the same vault is attempted to be created without first cleaning up the corresponding database keys");
		}
		List<VaultType> vaults = new ArrayList<>();
		boolean cleanup = false;
		try {
			
			vaults = service.listVaultsByOwner(vaultUser3);
			logger.info("Vault Count: " + vaults.size());
			for(VaultType vault : vaults){
				logger.info("Cleaning up vault: " + vault.getVaultName());
				service.deleteVault(vault);
			}
			cleanup = true;
		} catch (FactoryException | ArgumentException | DataException e) {
			logger.error(e);
		}
		assertTrue("Failed to load changed vault", lookup);
		assertTrue("Failed to cleanup vaults",cleanup);

	}
*/
	/*
	 * Refactor. 2017/09/16 - once the authZ checks were added in, this test is failing because the test user is trying to read a vault it's not authorized to accesss
	 * Refactored 2018/05/18 to add authZ to test user 2 to read test user 1 vault, and to tweak the behavior on newVaultData method to work with BaseService.add (for authZ check)
	 */
	
	/*
	@Test
	public void TestVault2Setup(){
		VaultService service = new VaultService(testProperties.getProperty("ssl.binary"),testProperties.getProperty("ssl.ca.path"));
		UserType vaultUser = getUser("QA Vault User", "password");
		//UserType vaultUser2 = getUser("QA Vault User 2", "password");
		
		String testVaultName = "Vault - " + UUID.randomUUID().toString();
		VaultBean vault = service.newVault(vaultUser, "./target/VaultExp", testVaultName);

		
	
		CredentialType cred = new CredentialType();
		cred.setCredentialType(CredentialEnumType.HASHED_PASSWORD);
		cred.setCredential("password".getBytes());
		assertNotNull("Vault is null",vault);
		assertNotNull("Credential is null",cred);
		
		try {
			service.initialize(vault, cred);
		} catch (ArgumentException | FactoryException e1) {
			logger.error(FactoryException.LOGICAL_EXCEPTION, e1);
		}
		//logger.info(VaultService.exportVault(vault));
		
		boolean created = service.createVault(vault, cred);
		assertTrue("Failed to create vault", created);
		
		
		SecurityBean key = null;
		try {
			key = service.getVaultKey(vault);
		} catch (ArgumentException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		assertNotNull("Vault key was null", key);
		
		VaultBean chkVault = service.loadVault("./target/VaultExp", testVaultName, true);
		assertNotNull("Failed to load vault", chkVault);
		
		try {
			service.initialize(chkVault,cred);
			key = null;
			key = service.getVaultKey(chkVault);
		} catch (ArgumentException | FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		assertNotNull("Check vault key was null", key);
		
		boolean newKey = false;
		try {
			newKey = service.newActiveKey(vault);
		} catch (UnsupportedEncodingException | FactoryException | ArgumentException | DataException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Key is null", newKey);
		assertNotNull("Key is null", vault.getActiveKeyId());
		logger.info("Created active key : " + vault.getActiveKeyId());
		
		
		
		try {
			service.deleteVault(vault);
		} catch (ArgumentException | FactoryException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION, e);
		}
		
	}	

	*/
/*
	@Test
	public void TestVaultData(){
		UserType qaUser = getUser("QA Vault User", "password");
		String vaultName = "QA Vault Keys";
		VaultService vs = new VaultService(qaUser,"./target/VaultExp",vaultName);

		String dataName = "QA Vault Data - " + UUID.randomUUID().toString();
		SecurityBean cipherBean = new SecurityBean();
		SecurityFactory.getSecurityFactory().generateSecretKey(cipherBean);
		try {

			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(qaUser, "VaultExamples", qaUser.getHomeDirectory(), qaUser.getOrganizationId());

			vs.initialize();
			if(resetVault){
				/// Cleanup anything already present
				vs.deleteVault();
				/// run initialize a second time after deleting the remnants
				///
				vs.initialize();
			}
			if(vs.getIsImproved() == false){
				boolean created = vs.createVault("password");
				logger.info("Created = " + created);
				assertTrue("Failed to create vault",created);
			}
			else vs.setPassword("password");
			String dataStr = "This is the vaulted data";
			DataType data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(qaUser, dir.getId());
			data.setName(dataName);
			data.setMimeType("text/plain");
			DataUtil.setCipher(data, cipherBean);
			DataUtil.setPassword(data, "data password");
			vs.setVaultBytes(data, dataStr.getBytes());
					//vs.newVaultData(dataName,dir,"text/plain",dataStr.getBytes(), false);
			
			
			assertNotNull("Vault data was null",data);
			assertTrue("Failed to add data",((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(data));
			
			vs.clearCache();
			
			DataType dataCheck = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, dir);
			assertNotNull("Data was null",dataCheck);

			DataUtil.setPassword(dataCheck, "data password");
			DataUtil.setCipher(dataCheck, cipherBean);
			logger.info("Vaulted data value: " + BinaryUtil.toBase64Str(DataUtil.getValue(dataCheck)));
			byte[] decBytes = vs.extractVaultData(dataCheck);
			logger.info("Extracted data value: " + new String(decBytes));
			
			byte[] newData = "Updated vault data".getBytes();
			DataUtil.setPassword(dataCheck, "data password");
			DataUtil.setCipher(dataCheck, cipherBean);
			dataCheck.setDescription("Updated description");
			vs.updateImprovedData(dataCheck, newData);
			//((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).updateData(dataCheck);
			logger.info("Making sure multi-enciphered data survives an update");
			DataType dataCheck2 = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, dir);
			assertNotNull("Data was null",dataCheck2);

			DataUtil.setPassword(dataCheck2, "data password");
			DataUtil.setCipher(dataCheck2, cipherBean);
			logger.info("Vaulted data value: " + BinaryUtil.toBase64Str(DataUtil.getValue(dataCheck2)));
			byte[] decBytes2 = vs.extractVaultData(dataCheck2);
			logger.info("Extracted data value: " + new String(decBytes2));
			
			logger.info("Making sure a details-only data survives an update");
			DataType dataCheck3 = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, true, dir);
			dataCheck3.setDescription("Updated description #2");
			((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).update(dataCheck3);

			logger.info("Making sure an unread detailed data survives an update");
			
			dataCheck3 = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, false, dir);
			dataCheck3.setDescription("Updated description #3");
			((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).update(dataCheck3);
			
			dataCheck3 = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, false, dir);
			DataUtil.setPassword(dataCheck3, "data password");
			DataUtil.setCipher(dataCheck3, cipherBean);
			logger.info("Vaulted data value: " + BinaryUtil.toBase64Str(DataUtil.getValue(dataCheck3)));
			byte[] decBytes3 = vs.extractVaultData(dataCheck3);
			logger.info("Extracted data value: " + new String(decBytes3));

			logger.info("Make sure vaulted data cannot be read outside of using the vault");
			DataType dataCheck4 = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(dataName, false, dir);
			dataCheck4.setVaulted(false);
			DataUtil.setPassword(dataCheck4, "data password");
			DataUtil.setCipher(dataCheck4, cipherBean);
			
			String data4Str = new String(DataUtil.getValue(dataCheck4));
			assertFalse("Attempt to read data without the vault should have failed",data4Str.equals("Updated vault data"));
			//logger.info("Invalid data: '" + data4Str + "'");
			
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (UnsupportedEncodingException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	*/
	
}
	