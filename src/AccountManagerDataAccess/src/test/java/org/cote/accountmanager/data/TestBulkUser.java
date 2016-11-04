package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.junit.Test;

public class TestBulkUser extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestBulkUser.class.getName());
	
	@Test
	public void TestBulkUser(){
		boolean success = false;
		String guid = UUID.randomUUID().toString();
		try{
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			
			UserType new_user = Factories.getUserFactory().newUser("BulkUser-" + guid, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization().getId());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.USER, new_user);
			//SecurityType asymKey = KeyService.newPersonalAsymmetricKey(sessionId,null,new_user,false);
			//SecurityType symKey = KeyService.newPersonalSymmetricKey(sessionId,null,new_user,false);
			CredentialService.newCredential(CredentialEnumType.HASHED_PASSWORD,sessionId,new_user, new_user, "password1".getBytes("UTF-8"), true, true,false);

			logger.info("Retrieving Bulk User");
			UserType check = Factories.getUserFactory().getByName("BulkUser-" + guid, new_user.getOrganizationId());
			assertNotNull("Failed user cache check",check);
			
			logger.info("Retrieving User By Id");
			check = Factories.getUserFactory().getById(new_user.getId(), new_user.getOrganizationId());
			assertNotNull("Failed id cache check",check);
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			success = true;
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(fe.getStackTrace());
		}  catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(e.getStackTrace());
		} catch (DataAccessException e) {
			
			logger.error(e.getMessage());
			logger.error(e.getStackTrace());
		} catch (UnsupportedEncodingException e) {
			
			logger.error(e.getStackTrace());
		}
		assertTrue("Success bit is false",success);
		// Now try to authenticate as the new bulk loaded user
		UserType chkUser = null;
		try {
			 chkUser = SessionSecurity.login("BulkUser-" + guid, CredentialEnumType.HASHED_PASSWORD, "password1", Factories.getDevelopmentOrganization().getId());
			 assertNotNull("Unable to authenticate as new user",chkUser);
			 SessionSecurity.logout(chkUser);
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}
	}

}