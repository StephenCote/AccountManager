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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.types.SessionStatusEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.junit.Test;

public class TestUserSession extends BaseDataAccessTest{
	public static final Logger logger = LogManager.getLogger(TestSecuritySpool.class);
	
	private static String sessionId = UUID.randomUUID().toString();
	private static String sessionId2 = UUID.randomUUID().toString();
	
	@Test
	public void TestClearSessions(){
		logger.info("Clear all sessions");
		Factories.getSessionFactory().clearSessions();
	}
	@Test
	public void TestCreateSession(){
		logger.info("Create new session");
		assertNotNull(testUser);
		UserSessionType session = Factories.getSessionFactory().newUserSession(testUser, sessionId);
		UserSessionType session2 = Factories.getSessionFactory().newUserSession(testUser2, sessionId2);
		logger.info("New session will expire in: " + CalendarUtil.getTimeSpanFromNow(session.getSessionExpires()));
		boolean add_session = false;
		try {
			add_session = Factories.getSessionFactory().addSession(session);
			if(add_session) add_session = Factories.getSessionFactory().addSession(session2);
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue(add_session);
		logger.info("Session id: " + sessionId);
	}
	@Test
	public void TestGetSession(){
		logger.info("Get session");
		assertNotNull(testUser);
		UserSessionType session = null;
		try {
			session = Factories.getSessionFactory().getSession(sessionId, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertNotNull("Session object is null",session);
	}
	
	@Test
	public void TestUpdateSession(){
		logger.info("Update session");
		assertNotNull(testUser);
		UserSessionType session = null;
		boolean updated = false;
		try {
			session = Factories.getSessionFactory().getSession(sessionId, testUser.getOrganizationId());
			session.setSessionStatus(SessionStatusEnumType.NOT_AUTHENTICATED);
			updated = Factories.getSessionFactory().update(session);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue(updated);
	}
	
	@Test
	public void TestAddSessionData(){
		logger.info("Add session data");
		UserSessionType session = null;
		UserSessionType session2 = null;
		try {
			session = Factories.getSessionFactory().getSession(sessionId, testUser.getOrganizationId());
			session2 = Factories.getSessionFactory().getSession(sessionId2, testUser2.getOrganizationId());
			assertNotNull("Session object is null", session);
			assertNotNull("Session object is null",session2);
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
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	
	@Test
	public void TestClearSessionData(){
		logger.info("Clear session data");
		UserSessionType session = null;
		try{
			Factories.getSessionFactory().clearSession(sessionId);
			session = Factories.getSessionFactory().getSession(sessionId, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertNull(session);
	}
	
}