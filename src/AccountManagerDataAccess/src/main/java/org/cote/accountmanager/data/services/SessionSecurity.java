/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.data.services;

import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.StatisticsType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.SessionStatusEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class SessionSecurity {
	public static final Logger logger = Logger.getLogger(SessionSecurity.class.getName());
	// default expiry in hours
	//
	private static int defaultSessionExpiry = 1;
	// max time in hours
	//
	private static int maxSessionTime = 6;
	
	// Enable legacy password lookup
	//
	private static boolean enableLegacyPasswordAuthentication = false;
	public static void setEnableLegacyPasswordAuthentication(boolean b){
		enableLegacyPasswordAuthentication = b;
	}
	public static boolean isAuthenticated(UserType user){
		if(user == null) return false;
		/// NOTE: Document Control is the user through with anonymous data requests may be proxied
		///
		if(user.getName().equals(Factories.getDocumentControlName())) return true;
		else if(user.getSession() == null) return false;
		return isAuthenticated(user.getSession());
	}
	public static boolean isAuthenticated(UserSessionType session){
		if(Factories.getSessionFactory().isValid(session) == false) return false;
		return session.getSessionStatus().equals(SessionStatusEnumType.AUTHENTICATED);
	}	
	public static UserType getUserBySession(String session_id, OrganizationType organization) throws FactoryException, ArgumentException{
		UserType user = null;
		UserSessionType session = getUserSession(session_id, organization);
		if(session == null) return null;
		verifySessionAuthentication(session);
		if(session.getSessionStatus().equals(SessionStatusEnumType.AUTHENTICATED)) user = Factories.getUserFactory().getUserBySession(session);
		if(user == null) return null;
		user.setSession(session);
		user.setSessionStatus(session.getSessionStatus());
		return user;
	}

	public static UserSessionType getUserSession(String session_id, OrganizationType organization) throws FactoryException{
		UserSessionType session = Factories.getSessionFactory().getCreateSession(session_id, organization);
		if(verifySessionAuthentication(session) == false){
			throw new FactoryException("Failed to verify session authentication status");
		}
		
		return session;
	}
	protected static boolean verifySessionAuthentication(UserSessionType session) throws FactoryException{
		boolean out_bool = false;
		if(session == null){
			logger.error("Null session object");
			return out_bool;
		}
		if(session.getSessionStatus().equals(SessionStatusEnumType.AUTHENTICATED) && Factories.getSessionFactory().isValid(session) == false){
			session.setSessionStatus(SessionStatusEnumType.NOT_AUTHENTICATED);
			out_bool = updateSession(session);
		}
		else{
			out_bool = true;
		}
		return out_bool;
	}
	protected static void updateSessionExpiry(UserSessionType session){
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, defaultSessionExpiry);
		if(
			/// If the current time is less than session expiry less the default expiry
			///
			//cal.getTimeInMillis() <= (session.getSessionExpires().toGregorianCalendar().getTimeInMillis() + (1000 * 60 * 60 * defaultSessionExpiry))
			Factories.getSessionFactory().isValid(session)
			&&
			/// If the current session less the session created date is less than the maximum session time
			///
			(session.getSessionExpires().toGregorianCalendar().getTimeInMillis() - session.getSessionCreated().toGregorianCalendar().getTimeInMillis()) <= (1000 * 60 * 60 * maxSessionTime)
		){
			/// Then extend the session expiration by the default expiration time.
			///
			session.setSessionExpires(CalendarUtil.getXmlGregorianCalendar(cal.getTime()));
		}
	}
	
	public static boolean updateSession(UserSessionType session) throws FactoryException{
		boolean out_bool = false;
		updateSessionExpiry(session);
		out_bool = Factories.getSessionFactory().update(session);
		if(session.getChangeSessionData().size() > 0){
			out_bool = Factories.getSessionFactory().updateData(session);
		}
		return out_bool;
	}
	
	public static boolean isUserAuthenticated(UserType user) throws FactoryException{
		boolean out_bool = false;
		UserSessionType session = user.getSession();
		if(session == null){
			session = Factories.getSessionFactory().getCreateSession(null, user.getOrganization());
			if(session == null) throw new FactoryException("Session is null for user #" + user.getId());
			user.setSession(session);
		}
		if(session.getSessionStatus().equals(SessionStatusEnumType.AUTHENTICATED) && Factories.getSessionFactory().isValid(session) == true){
			out_bool = true;
		}

		return out_bool;
	}
	/*
	public static UserType legacyLogin(String user_name, String password_hash, OrganizationType organization) throws FactoryException, ArgumentException
	{
		return legacyLogin(UUID.randomUUID().toString(), user_name, password_hash, organization);
	}

	public static UserType legacyLogin(String session_id, String user_name, String password_hash, OrganizationType organization) throws FactoryException, ArgumentException{
		/// 2015/06/24 - Legacy password handling
		/// The hashed password is put into a LegacyPassword CredentialType.
		/// The validation will repeat the following query to make sure the hash matches the persisted record
		///
		return authenticateSession(session_id, user_name, CredentialEnumType.LEGACY_PASSWORD, password_hash, organization);
	}
	*/
	/// 2015/06/24
	/// New 'login' API to reflect change in approach
	/// All existing login calls must be changed to use legacyLogin, or use the new method once the credentials are migrated
	///
	public static UserType login(String userName, CredentialEnumType credType, String suppliedCredential, OrganizationType organization) throws FactoryException, ArgumentException{
		return authenticateSession(UUID.randomUUID().toString(), userName, credType, suppliedCredential, organization);
	}
	public static UserType login(String sessionId, String userName, CredentialEnumType credType, String suppliedCredential, OrganizationType organization) throws FactoryException, ArgumentException{
		return authenticateSession(sessionId, userName, credType, suppliedCredential, organization);
	}
	/// 2015/06/24
	/// Rebranded API to reflect change in approach and actual intent of authenticating against a session using a user and a credential
	///
	private static UserType authenticateSession(String sessionId, String userName, CredentialEnumType credType, String suppliedCredential, OrganizationType organization) throws FactoryException, ArgumentException{
		UserType user = Factories.getUserFactory().getUserByName(userName, organization);
		if(user == null){
			throw new ArgumentException("User does not exits");
		}
		CredentialType cred = null;
		if(credType == CredentialEnumType.HASHED_PASSWORD) cred = CredentialService.getPrimaryCredential(user,credType,true);
		else if(credType == CredentialEnumType.LEGACY_PASSWORD){
			/// Legacy support means:
			/// 1) Password is hashed using the old method
			/// 2) Hashed password is supplied to this method as the value of suppliedCredential
			/// 3) A CredentialType of LEGACY_PASSWORD will be created for CredentialService using suppliedCredential
			/// 4) CredentialService validation will perform a database query for the user against the suppliedCredential value, replicating the current legacy process
			/// 5) Once all passwords and APIs stop using the legacy method, this will go away
			///
			/// NOTE: LEGACY_PASSWORD CredentialType uses the supplied credential, so it's NOT TRUSTWORTHY
			/// Only use the CredentialService validation method to verify it against the database record
			///
			if(enableLegacyPasswordAuthentication){
				logger.warn("LEGACY CREDENTIAL SUPPORT BEING APPLIED FOR " + user.getName() + " (#" + user.getId() +")");
				cred = CredentialService.newLegacyPasswordCredential(user, suppliedCredential,true);
			}
		}
		if(cred == null){
			throw new ArgumentException("AM5.1 credential does not exist");

		}
		return authenticateSession(sessionId, user, cred, suppliedCredential, organization);
	}
	/// 2015/06/24
	/// Rebranded API to reflect change in approach and actual intent of authenticating against a session using a user and a credential
	///
	private static UserType authenticateSession(String sessionId, UserType user, CredentialType credential, String suppliedCredential, OrganizationType organization) throws FactoryException, ArgumentException{
		UserSessionType session = Factories.getSessionFactory().getCreateSession(sessionId, organization);
		if (session == null){
			throw new FactoryException("New session was not allocated.");
		}
		if(user == null || credential == null){
			throw new ArgumentException("User object or credential was null");
		}
		if(credential.getCredentialType() == CredentialEnumType.HASHED_PASSWORD || credential.getCredentialType() == CredentialEnumType.LEGACY_PASSWORD){
			if(credential.getCredentialType() == CredentialEnumType.LEGACY_PASSWORD && enableLegacyPasswordAuthentication == false){
				throw new ArgumentException("Legacy Password support is disabled, and a legacy password credential was supplied");
			}
			if(CredentialService.validatePasswordCredential(user, credential, suppliedCredential) == false){
				throw new FactoryException("Failed to validate user");
			}
		}
		
		session.setUserId(user.getId());
		session.setOrganizationId(user.getOrganization().getId());
		user.setSession(session);
		Factories.getUserFactory().populate(user);
		Factories.getUserFactory().updateUserToCache(user);

		session.setSessionStatus(SessionStatusEnumType.AUTHENTICATED);
		StatisticsType stats = Factories.getStatisticsFactory().getStatistics(user);
		if(stats == null){
			stats = Factories.getStatisticsFactory().newStatistics(user);
			if(Factories.getStatisticsFactory().addStatistics(stats) == false) throw new FactoryException("Failed to add statistics to user #" + user.getId());
			stats = Factories.getStatisticsFactory().getStatistics(user);
		}
		stats.setAccessedDate(CalendarUtil.getXmlGregorianCalendar(Calendar.getInstance().getTime()));
		if(Factories.getStatisticsFactory().updateStatistics(stats) == false){
			throw new FactoryException("Error updating statistics for " + session.getSessionId() + " in organization id " + session.getOrganizationId());
		}
		if(Factories.getSessionFactory().update(session) == false){
			throw new FactoryException("Error updating session " + session.getSessionId() + " in organization id " + session.getOrganizationId());
		}
		
		return user;
	}
	
	public static boolean logout(UserType user) throws FactoryException{
		if (user == null) return false;
		if (user.getSession() == null) return true;
		return logout(user.getSession());
	}
	public static boolean logout(String session_id, OrganizationType organization) throws FactoryException {
		UserSessionType session = Factories.getSessionFactory().getSession(session_id, organization);
		if(session_id == null || organization == null){
			logger.error("Invalid parameters");
			return false;
		}
		if(session == null){
			logger.info("Session " + session_id + " does not exist in " + organization.getName() + ".  Skipping logout procedure");
			return false;
		}
		/*
		if(session.getSessionStatus().equals(SessionStatusEnumType.AUTHENTICATED) == false){
			Factories.getSessionFactory().updateSessionToCache(session);
			return true;
		}
		*/
		return logout(session);
	}
	public static boolean logout(UserSessionType session) throws FactoryException{

		session.setSessionStatus(SessionStatusEnumType.NOT_AUTHENTICATED);
		session.setUserId((long)0);

		if(Factories.getSessionFactory().clearSessionData(session.getSessionId()) == false){
			logger.warn("Failed to clear session data for " + session.getSessionId());
		}
		session.getChangeSessionData().clear();
		session.getSessionData().clear();
		session.setDataSize(0);
		Factories.getSessionFactory().updateSessionToCache(session);
		return Factories.getSessionFactory().update(session);
	}
}
