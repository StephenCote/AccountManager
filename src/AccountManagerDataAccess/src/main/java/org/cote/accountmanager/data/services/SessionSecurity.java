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
package org.cote.accountmanager.data.services;

import java.security.Principal;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.StatisticsFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.security.ApiConnectionConfigurationService;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.UserPrincipal;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.StatisticsType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.SessionStatusEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.JSONUtil;
public class SessionSecurity {
	public static final Logger logger = LogManager.getLogger(SessionSecurity.class);
	private static Map<UserPrincipal, UserType> principalCache = Collections.synchronizedMap(new HashMap<>());
	
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
	public static void clearCache() {
		principalCache.clear();
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
	public static UserType getUserBySession(String session_id, long organizationId) throws FactoryException, ArgumentException{
		UserType user = null;
		UserSessionType session = getUserSession(session_id, organizationId);
		verifySessionAuthentication(session);
		if(session.getSessionStatus().equals(SessionStatusEnumType.AUTHENTICATED)){
			user = ((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).getUserBySession(session);
		}
		else {
			logger.info("Unauthenticated user with session id " + session_id);
		}
		if(user == null) {
			logger.info("No user associated with session id " + session_id);
			return null;
		}
		user.setSession(session);
		user.setSessionStatus(session.getSessionStatus());
		return user;
	}

	public static UserSessionType getUserSession(String session_id, long organizationId) throws FactoryException{
		UserSessionType session = Factories.getSessionFactory().getCreateSession(session_id, organizationId);
		if(verifySessionAuthentication(session) == false){
			throw new FactoryException("Failed to verify session authentication status");
		}
		
		return session;
	}
	protected static boolean verifySessionAuthentication(UserSessionType session) throws FactoryException{
		boolean outBool = false;
		if(session == null){
			logger.error("Null session object");
			return outBool;
		}
		if(session.getSessionStatus().equals(SessionStatusEnumType.AUTHENTICATED) && Factories.getSessionFactory().isValid(session) == false){
			session.setSessionStatus(SessionStatusEnumType.NOT_AUTHENTICATED);
			outBool = updateSession(session);
		}
		else{
			outBool = true;
		}
		return outBool;
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
		boolean outBool = false;
		updateSessionExpiry(session);
		outBool = Factories.getSessionFactory().update(session);
		if(session.getChangeSessionData().size() > 0){
			outBool = Factories.getSessionFactory().updateData(session);
		}
		return outBool;
	}
	
	public static boolean isUserAuthenticated(UserType user) throws FactoryException{
		boolean outBool = false;
		UserSessionType session = user.getSession();
		if(session == null){
			session = Factories.getSessionFactory().getCreateSession(null, user.getOrganizationId());
			if(session == null) throw new FactoryException("Session is null for user #" + user.getId());
			user.setSession(session);
		}
		if(session.getSessionStatus().equals(SessionStatusEnumType.AUTHENTICATED) && Factories.getSessionFactory().isValid(session)){
			outBool = true;
		}

		return outBool;
	}
	/*
	public static UserType legacyLogin(String user_name, String password_hash, long organizationId) throws FactoryException, ArgumentException
	{
		return legacyLogin(UUID.randomUUID().toString(), user_name, password_hash, organization);
	}

	public static UserType legacyLogin(String session_id, String user_name, String password_hash, long organizationId) throws FactoryException, ArgumentException{
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
	public static UserType login(String userName, CredentialEnumType credType, String suppliedCredential, long organizationId) throws FactoryException, ArgumentException{
		return authenticateSession(UUID.randomUUID().toString(), userName, credType, suppliedCredential, organizationId);
	}
	public static UserType login(String sessionId, String userName, CredentialEnumType credType, String suppliedCredential, long organizationId) throws FactoryException, ArgumentException{
		return authenticateSession(sessionId, userName, credType, suppliedCredential, organizationId);
	}
	
	public static UserType getPrincipalUser(HttpServletRequest request){
		Principal principal = request.getUserPrincipal();
		UserType outUser = null;
		if(principal != null && principal instanceof UserPrincipal){
			UserPrincipal userp = (UserPrincipal)principal;
			// logger.info("UserPrincipal: " + userp.toString());
			if(principalCache.containsKey(userp)) return principalCache.get(userp);
			
			try {
				OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(userp.getOrganizationPath());

				UserType user = Factories.getNameIdFactory(FactoryEnumType.USER).getById(userp.getId(), org.getId());
				//UserType user = getUserBySession(request.getSession().getId(), org.getId());
				if(user != null){
					authenticateUser(user, request.getSession().getId());
					outUser = user;
					if(BaseService.getEnableExtendedAttributes()){
						Factories.getAttributeFactory().populateAttributes(outUser);
					}
					principalCache.put(userp, user);
				}
				else {
					logger.warn("User is null for " + userp.getId() + " in " + org.getId());
				}
			} catch (FactoryException | ArgumentException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		else{
			logger.debug("Don't know what: " + (principal == null ? "Null" : "Uknown") + " principal");
		}
		return outUser;
	}
	
	/// 2015/06/24
	/// Rebranded API to reflect change in approach and actual intent of authenticating against a session using a user and a credential
	///
	private static UserType authenticateSession(String sessionId, String userName, CredentialEnumType credType, String suppliedCredential, long organizationId) throws FactoryException, ArgumentException{
		UserType user = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(userName, organizationId);
		if(user == null){
			throw new ArgumentException("User does not exist");
		}
		CredentialType cred = null;
		if(credType == CredentialEnumType.TOKEN){
			if(RoleService.getIsUserInEffectiveRole(RoleService.getApiUserUserRole(user.getOrganizationId()), user) == false){
				logger.error("User '" + user.getName() + "' is not an authorized API User.");
				return null;
			}
			DirectoryGroupType dir = ApiConnectionConfigurationService.getApiDirectory(user);
			cred = CredentialService.getPrimaryCredential(dir,credType,true);
		}
		else if(credType == CredentialEnumType.HASHED_PASSWORD){
			logger.debug("Looking up primary credential");
			cred = CredentialService.getPrimaryCredential(user,credType,true);
			logger.debug("Found primary credential: " + (cred != null));
		}
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
				logger.warn("LEGACY CREDENTIAL NOT SUPPORTED FOR " + user.getName() + " (#" + user.getId() +")");
			}
		}
		if(cred == null){
			throw new ArgumentException("AM5.1 credential does not exist");

		}
		return authenticateSession(sessionId, user, cred, suppliedCredential, organizationId);
	}
	/// 2015/06/24
	/// Rebranded API to reflect change in approach and actual intent of authenticating against a session using a user and a credential
	///
	private static UserType authenticateSession(String sessionId, UserType user, CredentialType credential, String suppliedCredential, long organizationId) throws FactoryException, ArgumentException{

		if(user == null || credential == null){
			throw new ArgumentException("User object or credential was null");
		}
		if(credential.getCredentialType() == CredentialEnumType.HASHED_PASSWORD || credential.getCredentialType() == CredentialEnumType.LEGACY_PASSWORD){
			if(credential.getCredentialType() == CredentialEnumType.LEGACY_PASSWORD && enableLegacyPasswordAuthentication == false){
				throw new ArgumentException("Legacy Password support is disabled, and a legacy password credential was supplied");
			}
			logger.debug("Validating credential");
			if(CredentialService.validatePasswordCredential(user, credential, suppliedCredential) == false){
				throw new FactoryException("Failed to validate user");
			}
		}
		else{
			throw new FactoryException("Unsupported authentication option");
		}
		authenticateUser(user, sessionId);
		return user;
	}

	public static UserType authenticatePrincipal(Principal principal, String sessionId){
		UserType user = null;
		if(principal != null && principal instanceof UserPrincipal){
			UserPrincipal userp = (UserPrincipal)principal;
			try {
				if(principalCache.containsKey(userp)) {
					user = principalCache.get(userp);
				}
				else {
					OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(userp.getOrganizationPath());
					user = Factories.getNameIdFactory(FactoryEnumType.USER).getById(userp.getId(), org.getId());
					if(user != null){
						principalCache.put(userp, user);
					}
				
				}
				if(user != null) {
					authenticateUser(user, sessionId);
				}
			} catch (FactoryException | ArgumentException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}

		}
		else {
			logger.warn("Null principal");
		}
		return user;
	}
	private static void authenticateUser(UserType user, String sessionId) throws FactoryException, ArgumentException{
		UserSessionType session = null;
		
		if(sessionId != null && sessionId.length() > 0){
			session = Factories.getSessionFactory().getCreateSession(sessionId, user.getOrganizationId());
			if (session == null){
				throw new FactoryException("New session '" + sessionId + "' was not allocated.");
			}
		}
		else{
			session = Factories.getSessionFactory().newUserSession();
		}
		session.setUserId(user.getId());
		session.setOrganizationId(user.getOrganizationId());
		user.setSession(session);
		Factories.getNameIdFactory(FactoryEnumType.USER).populate(user);
		Factories.getNameIdFactory(FactoryEnumType.USER).normalize(user);
		Factories.getNameIdFactory(FactoryEnumType.USER).updateToCache(user);

		session.setSessionStatus(SessionStatusEnumType.AUTHENTICATED);
		
		StatisticsType stats = ((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).getStatistics(user);
		if(stats == null){
			stats = ((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).newStatistics(user);
			if(((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).add(stats) == false) throw new FactoryException("Failed to add statistics to user #" + user.getId());
			stats = ((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).getStatistics(user);
		}
		stats.setAccessedDate(CalendarUtil.getXmlGregorianCalendar(Calendar.getInstance().getTime()));
		if(((StatisticsFactory)Factories.getFactory(FactoryEnumType.STATISTICS)).update(stats) == false){
			throw new FactoryException("Error updating statistics for " + session.getSessionId() + " in organization id " + session.getOrganizationId());
		}
		if(sessionId != null && Factories.getSessionFactory().update(session) == false){
			throw new FactoryException("Error updating session " + session.getSessionId() + " in organization id " + session.getOrganizationId());
		}

	}
	
	public static boolean logout(UserType user) throws FactoryException{
		if (user == null) return false;
		if (user.getSession() == null) return true;
		return logout(user.getSession());
	}
	public static boolean logout(String session_id, long organizationId) throws FactoryException {
		UserSessionType session = Factories.getSessionFactory().getSession(session_id, organizationId);
		if(session_id == null || organizationId <= 0L){
			logger.error("Invalid parameters");
			return false;
		}
		if(session == null){
			logger.info("Session " + session_id + " does not exist in " + organizationId + ".  Skipping logout procedure");
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
