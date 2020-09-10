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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Calendar;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.DataMaintenance;
import org.cote.accountmanager.data.services.PersonService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestUserRegistration{
	public static final Logger logger = LogManager.getLogger(TestUserRegistration.class);
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
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
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
			
			PersonType person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getSystemPersonByUser(user);
			assertNotNull("Person object is null", user);
			((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).populate(person);
			ContactType userEmail = PersonService.getPreferredEmailContact(person);
			assertNotNull("Contact is null",userEmail);
			
			logger.info("User email: " + userEmail.getContactValue());
			
			boolean logout = SessionSecurity.logout(user);
			assertTrue("Failed to logout", logout);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		
	}
	
}