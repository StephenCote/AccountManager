package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.log4j.Logger;

import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.SecurityUtil;

import org.junit.Test;

public class TestBulkUser extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestBulkUser.class.getName());
	
	@Test
	public void TestBulkUser(){
		boolean success = false;
		try{
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			String guid = UUID.randomUUID().toString();
			UserType new_user = Factories.getUserFactory().newUser("BulkUser-" + guid, SecurityUtil.getSaltedDigest("password1"), UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.USER, new_user);
			
			logger.info("Retrieving Bulk User");
			UserType check = Factories.getUserFactory().getUserByName("BulkUser-" + guid, new_user.getOrganization());
			assertNotNull("Failed user cache check",check);
			
			logger.info("Retrieving User By Id");
			check = Factories.getUserFactory().getById(new_user.getId(), new_user.getOrganization());
			assertNotNull("Failed id cache check",check);
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			success = true;
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		}  catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		assertTrue("Success bit is false",success);
	}

}