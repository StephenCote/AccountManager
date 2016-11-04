package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.DataMaintenance;
import org.cote.accountmanager.data.services.PersonService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestUserRegistration{
	public static final Logger logger = Logger.getLogger(TestUserRegistration.class.getName());
	private static String testUserName1 = "TestRegistrationUser";
	private static String testUserName2 = "TestRegistrationUser2";
	private UserType registrationUser = null;
	private UserType registrationUser2 = null;
	private static String registrationId = null;
	private static String registrationId2 = null;
	private static String sessionId = null;
	private static String sessionId2 = null;
	
	public TestUserRegistration(){
	
	}
	@Before
	public void setUp() throws Exception {
		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}
		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.SINGLE);
		cf.setDriverClassName("org.postgresql.Driver");
		cf.setUserName("devuser");
		cf.setUserPassword("password");
		cf.setUrl("jdbc:postgresql://127.0.0.1:5432/devdb");
		

		logger.info("Setup");
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void TestRegisterUser(){
		UserSessionType session1 = null;
		UserSessionType session2 = null;
		try{
			sessionId = UUID.randomUUID().toString();
			sessionId2 = UUID.randomUUID().toString();
			registrationId = UUID.randomUUID().toString();
			registrationId2 = UUID.randomUUID().toString();
			session1 = Factories.getSessionFactory().newUserSession(sessionId);
			session2 = Factories.getSessionFactory().newUserSession(sessionId2);
			session1.setOrganizationId(Factories.getDevelopmentOrganization().getId());
			session2.setOrganizationId(Factories.getDevelopmentOrganization().getId());
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 1);
			session1.setSessionExpires(CalendarUtil.getXmlGregorianCalendar(cal.getTime()));
			session2.setSessionExpires(CalendarUtil.getXmlGregorianCalendar(cal.getTime()));

			Factories.getSessionFactory().addSession(session1);
			Factories.getSessionFactory().addSession(session2);

			
			session1 = Factories.getSessionFactory().getSession(sessionId, Factories.getDevelopmentOrganization().getId());
			session2 = Factories.getSessionFactory().getSession(sessionId2, Factories.getDevelopmentOrganization().getId());
			
			Factories.getSessionDataFactory().setValue(session1, "registration-id", registrationId);
			Factories.getSessionDataFactory().setValue(session2, "registration-id", registrationId2);
			
			Factories.getSessionFactory().updateData(session1);
			Factories.getSessionFactory().updateData(session2);
			
			logger.info("Session 1 Size: " + session1.getSessionData().size());
			logger.info("Session 2 Size: " + session2.getSessionData().size());
		}
		catch(FactoryException fe){
			logger.info(fe.getMessage());
			logger.error(fe.getStackTrace());
		}
	}
	
	@Test
	public void TestIntraSessionCleanup(){
		boolean cleanup = DataMaintenance.cleanupSessions();
		assertTrue("Sessions were not cleaned up", cleanup);
	}
	
	@Test
	public void TestRegisterNewUser(){
		String userName = "TestUser-" + UUID.randomUUID().toString();
		String password = "password";
		String email = UUID.randomUUID().toString() + "@nowhere.no";
		AuditType audit = AuditService.beginAudit(ActionEnumType.REQUEST, "Register", AuditEnumType.USER, userName);
		AuditService.targetAudit(audit, AuditEnumType.USER, userName);
		boolean created = PersonService.createRegisteredUserAsPerson(audit, userName, password, email, Factories.getDevelopmentOrganization().getId());
		assertTrue("User was not created",created);
		
		UserType user = null;
		try {
			user = SessionSecurity.login(UUID.randomUUID().toString(), userName, CredentialEnumType.HASHED_PASSWORD,password, Factories.getDevelopmentOrganization().getId());
			assertNotNull("Failed to login with new user", user);
			
			PersonType person = Factories.getPersonFactory().getPersonByUser(user);
			assertNotNull("Person object is null", user);
			Factories.getPersonFactory().populate(person);
			ContactType userEmail = PersonService.getPreferredEmailContact(person);
			assertNotNull("Contact is null",userEmail);
			
			logger.info("User email: " + userEmail.getContactValue());
			
			boolean logout = SessionSecurity.logout(user);
			assertTrue("Failed to logout", logout);
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}
		
		
	}
	
}