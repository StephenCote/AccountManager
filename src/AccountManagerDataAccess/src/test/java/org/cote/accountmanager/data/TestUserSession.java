package org.cote.accountmanager.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.FactoryService;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.SessionStatusEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestUserSession extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestSecuritySpool.class.getName());
	
	private static String sessionId = UUID.randomUUID().toString();
	private static String sessionId2 = UUID.randomUUID().toString();
	
	@Test
	public void TestClearSessions(){
		Factories.getSessionFactory().clearSessions();
	}
	@Test
	public void TestCreateSession(){
		assertNotNull(testUser);
		UserSessionType session = Factories.getSessionFactory().newUserSession(testUser, sessionId);
		UserSessionType session2 = Factories.getSessionFactory().newUserSession(testUser2, sessionId2);
		logger.info("New session will expire in: " + CalendarUtil.getTimeSpanFromNow(session.getSessionExpires()));
		boolean add_session = false;
		try {
			add_session = Factories.getSessionFactory().addSession(session);
			if(add_session) add_session = Factories.getSessionFactory().addSession(session2);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		assertTrue(add_session);
		logger.info("Session id: " + sessionId);
	}
	@Test
	public void TestGetSession(){
		assertNotNull(testUser);
		UserSessionType session = null;
		try {
			session = Factories.getSessionFactory().getSession(sessionId, testUser.getOrganizationId());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull(session);
	}
	
	@Test
	public void TestUpdateSession(){
		assertNotNull(testUser);
		UserSessionType session = null;
		boolean updated = false;
		try {
			session = Factories.getSessionFactory().getSession(sessionId, testUser.getOrganizationId());
			session.setSessionStatus(SessionStatusEnumType.NOT_AUTHENTICATED);
			updated = Factories.getSessionFactory().update(session);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue(updated);
	}
	
	@Test
	public void TestAddSessionData(){
		UserSessionType session = null;
		UserSessionType session2 = null;
		try {
			session = Factories.getSessionFactory().getSession(sessionId, testUser.getOrganizationId());
			session2 = Factories.getSessionFactory().getSession(sessionId2, testUser2.getOrganizationId());
			String testName1 = "testdata";
			String testName2 = "testdata2";
			String testName3 = "testdata3";
			String testVal1 = "123";
			String testVal2 = "456";
			String testVal3 = "789";
			Factories.getSessionDataFactory().setValue(session,testName1,testVal1);
			Factories.getSessionDataFactory().setValue(session,testName2,testVal1);
			Factories.getSessionDataFactory().setValue(session,testName3,testVal1);
			
			Factories.getSessionFactory().updateData(session);
			Factories.getSessionDataFactory().setValue(session2,testName1,testVal1);
			Factories.getSessionFactory().update(session2);
			Factories.getSessionFactory().updateData(session2);
			session = Factories.getSessionFactory().getSession(sessionId, testUser.getOrganizationId());
			assertTrue("Session data for '" + sessionId + "' should be 3",session.getSessionData().size() == 3);
			for(int i = 0; i < session.getSessionData().size();i++){
				logger.info(session.getSessionData().get(i) + " = " + session.getSessionData().get(i).getValue());
			}
			assertNotNull("Session data was null for id " + session.getSessionId() + " - a value was expected",Factories.getSessionDataFactory().getValue(session,  testName1));
			
			Factories.getSessionDataFactory().setValue(session, testName2, testVal2);
			Factories.getSessionDataFactory().setValue(session, testName3, testVal3);
			Factories.getSessionFactory().updateData(session);
			Factories.getSessionDataFactory().setValue(session, testName3, null);
			Factories.getSessionDataFactory().setValue(session, testName1, testVal3);
			Factories.getSessionFactory().updateData(session);
			
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void TestClearSessionData(){
		UserSessionType session = null;
		try{
			Factories.getSessionFactory().clearSession(sessionId);
			session = Factories.getSessionFactory().getSession(sessionId, testUser.getOrganizationId());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNull(session);
	}
	
}