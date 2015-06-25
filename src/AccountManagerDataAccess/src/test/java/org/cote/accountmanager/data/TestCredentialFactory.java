package org.cote.accountmanager.data;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.factory.ControlFactory;
import org.cote.accountmanager.data.factory.CredentialFactory;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.Test;


public class TestCredentialFactory extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestCredentialFactory.class.getName());
	
	@Test
	public void TestCredentialCRUD(){
		UserType qaUser = getUser("QA Vault User", "password");
		CredentialType cred = CredentialService.newHashedPasswordCredential(qaUser, qaUser, "password1", true);
		assertNotNull("New credential is null",cred);
		assertTrue("Credential was invalid",CredentialService.validateHashedPasswordCredential(cred, "password1"));
		assertFalse("Credential should be invalid",CredentialService.validateHashedPasswordCredential(cred, "BadPassword"));
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