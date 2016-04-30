package org.cote.accountmanager.data;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.factory.ControlFactory;
import org.cote.accountmanager.data.factory.CredentialFactory;
import org.cote.accountmanager.data.security.ApiClientConfigurationBean;
import org.cote.accountmanager.data.security.ApiConnectionConfigurationService;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.objects.ApiClientConfigurationType;
import org.cote.accountmanager.objects.ApiServiceEnumType;
import org.cote.accountmanager.objects.AttributeType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.Test;


public class TestCredentialFactory extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestCredentialFactory.class.getName());
	
	@Test
	public void TestApiToken(){
		String apiTokenValue = UUID.randomUUID().toString();
		DirectoryGroupType dir = ApiConnectionConfigurationService.getApiDirectory(testUser);
		CredentialType cred = CredentialService.newTokenCredential(testUser, dir, apiTokenValue, true);
		/*
		CredentialType cred = null;
		try {
			cred = CredentialService.newCredential(CredentialEnumType.TOKEN, null, testUser, dir, apiTokenValue.getBytes("UTF-8"), true, false);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		assertNotNull("Credential is null",cred);
		CredentialType cred2 = CredentialService.getPrimaryCredential(dir,CredentialEnumType.TOKEN,true);
		assertNotNull("Find credential failed",cred);
		boolean valid = CredentialService.validateTokenCredential(dir, cred2, apiTokenValue);
		assertTrue("Token was not valid",valid);
	}
	
	@Test
	public void TestAPIConfig(){
		String apiConfigName = UUID.randomUUID().toString();
		ApiClientConfigurationBean apiConfig = ApiConnectionConfigurationService.getApiClientConfiguration(ApiServiceEnumType.REST, apiConfigName,Factories.getDevelopmentOrganization().getId());
		assertNull("Expected null config",apiConfig);
		List<AttributeType> attrs = new ArrayList<AttributeType>();
		attrs.add(Factories.getAttributeFactory().newAttribute(null, "example1", "value1"));
		attrs.add(Factories.getAttributeFactory().newAttribute(null, "example2", "value2"));
		attrs.add(Factories.getAttributeFactory().newAttribute(null, "example3", "value3"));
		
		apiConfig = ApiConnectionConfigurationService.addApiClientConfiguration(ApiServiceEnumType.REST, apiConfigName, "http://localhost", "steve".getBytes(), "password".getBytes(), attrs,Factories.getDevelopmentOrganization().getId());
		assertNotNull("Expected config",apiConfig);
		//logger.info(JSONUtil.exportObject(apiConfig));
		ApiClientConfigurationType chkConfig = JSONUtil.importObject(getApiConfigString(),ApiClientConfigurationType.class);
		assertNotNull("Import config is null",chkConfig);
		//logger.info("Imported config: " + chkConfig.getName() + " from " + getApiConfigString());
		apiConfig = ApiConnectionConfigurationService.getApiClientConfiguration(ApiServiceEnumType.REST, apiConfigName,Factories.getDevelopmentOrganization().getId());
		assertNotNull("Expected config",apiConfig);
		logger.info("Got apiConfig " + apiConfig.getName());
		
		String identity = null;
		String credential = null;
		try {
			identity = new String(ApiConnectionConfigurationService.getApiClientCredential(apiConfig, CredentialEnumType.ENCRYPTED_IDENTITY),"UTF-8");
			credential = new String(ApiConnectionConfigurationService.getApiClientCredential(apiConfig, CredentialEnumType.ENCRYPTED_PASSWORD),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull("Identity is null",identity);
		assertNotNull("Credential is null",credential);
		logger.info("Identity=" + identity);
		logger.info("Credential=" + credential);
	}
	
	@Test
	public void TestCredentialCRUD(){
		UserType qaUser = getUser("QA Vault User", "password");
		CredentialType cred = CredentialService.newHashedPasswordCredential(qaUser, qaUser, "password1", true);
		assertNotNull("New credential is null",cred);
		assertTrue("Credential was invalid",CredentialService.comparePasswordCredential(cred, "password1"));
		assertFalse("Credential should be invalid",CredentialService.comparePasswordCredential(cred, "BadPassword"));
		assertTrue("Auditable Credential was invalid",CredentialService.validatePasswordCredential(qaUser, cred, "password1"));
		
		/// Test legacy system
		///
		CredentialType legCred = CredentialService.newLegacyPasswordCredential(qaUser, "password",false);
		boolean legVal = CredentialService.validatePasswordCredential(qaUser, legCred, "password");
		//CredentialType cred2 = CredentialService.newHashedPasswordCredential(qaUser, qaUser, "password1", false);
		//assertNotNull("New credential is null",cred2);
		/*
		boolean del = false;
		try {
			del = Factories.getCredentialFactory().deleteCredential(cred2);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		//assertTrue("Failed to delete credential",del);
		

	}
	
	private String getApiConfigString(){
		return 
		"{"
		+"	  \"attributes\" : [ {\n"
		+"	    \"values\" : [ \"value1\" ],\n"
		+"	    \"name\" : \"example1\",\n"
		+"	    \"dataType\" : \"VARCHAR\"\n"
		+"	  }, {\n"
		+"	    \"values\" : [ \"value2\" ],\n"
		+"	    \"name\" : \"example2\",\n"
		+"	    \"dataType\" : \"VARCHAR\"\n"
		+"	  }, {\n"
		+"	    \"values\" : [ \"value3\" ],\n"
		+"	    \"name\" : \"example3\",\n"
		+"	    \"dataType\" : \"VARCHAR\"\n"
		+"	  } ],\n"
		+"	  \"serviceType\" : \"REST\",\n"
		+"	  \"serviceUrl\" : \"http://localhost\",\n"
		+"	  \"name\" : \"6c37c233-8eb3-48be-8581-c2c72f3cecb6\"\n"
		+"	}";
	}
	
	/*
	@Test
	public void TestKeyCRUD(){
		String testStr = "blah blah blah";
		UserType qaUser = getUser("QA Vault User", "password");
		SecurityBean userKey = null;
		SecurityBean orgKey = null;
		SecurityBean mUserKey = null;
		SecurityBean mOrgKey = null;
		SecurityBean userPKey = null;
		SecurityBean orgPKey = null;
		SecurityBean eUserKey = null;
		SecurityBean eOrgKey = null;
		try {
			userKey = KeyService.newPersonalSymmetricKey(qaUser, true);
			orgKey = KeyService.newOrganizationSymmetricKey(qaUser.getOrganization(), true);
			mUserKey = KeyService.getPrimarySymmetricKey(qaUser);
			mOrgKey = KeyService.getPrimarySymmetricKey(qaUser.getOrganization());

			userPKey = KeyService.newPersonalAsymmetricKey(qaUser, true);
			assertNotNull("User Asymm key is null on create",userPKey);
			orgPKey = KeyService.newOrganizationAsymmetricKey(qaUser.getOrganization(), true);

			userPKey = KeyService.getPrimaryAsymmetricKey(qaUser);
			assertNotNull("User Asymm key is null on read",userPKey);
			orgPKey = KeyService.getPrimaryAsymmetricKey(qaUser.getOrganization());
			
			eUserKey = KeyService.newPersonalSymmetricKey(userPKey, qaUser, false);
			byte[] enc = SecurityUtil.encipher(eUserKey, testStr.getBytes());
			assertTrue("Expected encrypted key",eUserKey.getEncryptCipherKey());
			eOrgKey = KeyService.newOrganizationSymmetricKey(userPKey, qaUser.getOrganization(), false);
			assertTrue("Expected encrypted key",eOrgKey.getEncryptCipherKey());
			eUserKey = KeyService.getSymmetricKeyByObjectId(eUserKey.getObjectId(),qaUser.getOrganization());
			byte[] dec = SecurityUtil.decipher(eUserKey, enc);
			logger.info("Decipher test: " + new String(dec));
			eOrgKey = KeyService.getSymmetricKeyByObjectId(eUserKey.getObjectId(), qaUser.getOrganization());
			
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		assertNotNull("New user security Key is null",userKey);
		assertNotNull("New org security Key is null",orgKey);
		assertNotNull("User Key is null",mUserKey);
		assertNotNull("Org Key is null",mOrgKey);
		assertNotNull("User Asymm Key is null",userPKey);
		assertNotNull("Org Asymm Key is null",orgPKey);
		assertNotNull("User Enc Key is null",eUserKey);
		assertNotNull("Org Enc Key is null",eOrgKey);
		CredentialFactory cf = Factories.getCredentialFactory();
		ControlFactory ctf = Factories.getControlFactory();

	}
	*/
}