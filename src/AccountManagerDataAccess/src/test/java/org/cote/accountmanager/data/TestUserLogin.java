package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.services.DataMaintenance;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.Test;

public class TestUserLogin extends BaseDataAccessTest {
	public static final Logger logger = LogManager.getLogger(TestUserLogin.class);
	private static String testUserName1 = "TestSessionUser";
	private static String testUserName2 = "TestSessionUser2";
	private UserType sessionUser = null;
	private UserType sessionUser2 = null;
	private static String sessionId = null;
	private static String sessionId2 = null;
	
	public TestUserLogin(){
	
	}
	
	@Test
	public void TestInvalidLogin(){
		UserType user1 = null;
		UserSessionType session1 = null;
		try{
			session1 = SessionSecurity.getUserSession(UUID.randomUUID().toString(), Factories.getDevelopmentOrganization().getId());
			user1 = SessionSecurity.login(session1.getSessionId(),"Invalid", CredentialEnumType.HASHED_PASSWORD,"password1", Factories.getDevelopmentOrganization().getId());
		}
		catch(FactoryException fe){
			logger.info(fe.getMessage());
			logger.error("Error",fe);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertNull("User #1 is null", user1);

	}
	
	@Test
	public void TestLogin(){
		UserType session1 = null;
		UserType session1b = null;
		UserType session2 = null;
		try{
			session1 = SessionSecurity.login(testUserName1, CredentialEnumType.HASHED_PASSWORD,"password1", Factories.getDevelopmentOrganization().getId());
			session2 = SessionSecurity.login(testUserName2, CredentialEnumType.HASHED_PASSWORD,"password1", Factories.getDevelopmentOrganization().getId());
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(session1);
			logger.info("*** Check session populated: " + session1.getSession().getSessionId());
			session1b = SessionSecurity.getUserBySession(session1.getSession().getSessionId(), session1.getOrganizationId());
			logger.info("*** Check populated: " + session1b.getPopulated() + " should be " + session1.getPopulated());
			//assertTrue("Session was not populated",session1b.getPopulated());
		}
		catch(FactoryException fe){
			logger.info(fe.getMessage());
			logger.error("Error",fe);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertNotNull("User #1 is null", session1);
		assertNotNull("Session #1 is null", session1.getSession());
		assertNotNull("User #2 is null", session2);
		assertNotNull("Session #2 is null", session2.getSession());
		sessionId = session1.getSession().getSessionId();
		sessionId2 = session2.getSession().getSessionId();
	}
	
	@Test
	public void TestIntraSessionCleanup(){
		boolean cleanup = DataMaintenance.cleanupSessions();
		assertTrue("Sessions were not cleaned up", cleanup);
	}
	
	@Test
	public void TestRetrieveUser(){
		UserType user = null;
		try {
			
			user = SessionSecurity.getUserBySession(sessionId, Factories.getDevelopmentOrganization().getId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertNotNull("User is null", user);
		assertNotNull("User session is null", user.getSession());
		assertTrue("User session is not authenticated", SessionSecurity.isAuthenticated(user));
	}
	
	@Test
	public void TestSessionData(){
		UserType user1 = null;
		boolean updated = false;
		try{
			user1 = SessionSecurity.getUserBySession(sessionId, Factories.getDevelopmentOrganization().getId());
			Factories.getSessionDataFactory().setValue(user1.getSession(), "Example data","Example example example");
			updated = SessionSecurity.updateSession(user1.getSession());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertTrue(updated);
		
	}
	
	@Test
	public void TestLogout(){
		boolean logout = false;
		logger.info("Log out session '" + sessionId + "'");
		try {
			logout = SessionSecurity.logout(sessionId, Factories.getDevelopmentOrganization().getId());
			if(logout) logout = SessionSecurity.logout(sessionId2, Factories.getDevelopmentOrganization().getId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		assertTrue("User session '" + sessionId + "' was not logged out", logout);
	}

	@Test
	public void TestLoginExistingSession(){
		
		Factories.getSessionFactory().clearSessions();
		
		UserSessionType session1 = null;
		UserType user1 = null;
		try{
			session1 = SessionSecurity.getUserSession(sessionId, Factories.getDevelopmentOrganization().getId());
			user1 = SessionSecurity.login(sessionId, testUserName1, CredentialEnumType.HASHED_PASSWORD,"password1", Factories.getDevelopmentOrganization().getId());
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(user1);
		}
		catch(FactoryException fe){
			logger.info(fe.getMessage());
			logger.error("Error",fe);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertNotNull("User #1 is null", user1);
		assertNotNull("Session #1 is null", session1);
		
		boolean logout = false;

		try {
			logout = SessionSecurity.logout(sessionId, Factories.getDevelopmentOrganization().getId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		assertTrue("User session '" + sessionId + "' was not logged out", logout);
		Factories.getSessionFactory().clearSessions();
		
	}
	
	@Test
	public void TestDoubleLogin(){
		UserType session1 = null;
		boolean logout = false;
		try{
			session1 = SessionSecurity.login(testUserName1, CredentialEnumType.HASHED_PASSWORD,"password1", Factories.getDevelopmentOrganization().getId());
			session1 = SessionSecurity.login(testUserName1, CredentialEnumType.HASHED_PASSWORD,"password1", Factories.getDevelopmentOrganization().getId());
			logout = SessionSecurity.logout(session1.getSession().getSessionId(), Factories.getDevelopmentOrganization().getId());
		}
		catch(FactoryException fe){
			logger.info(fe.getMessage());
			logger.error("Error",fe);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertNotNull("User #1 is null", session1);
		assertNotNull("Session #1 is null", session1.getSession());
	}
}