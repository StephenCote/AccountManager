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
	public static UserType login(String user_name, String password_hash, OrganizationType organization) throws FactoryException, ArgumentException
	{
		return login(UUID.randomUUID().toString(), user_name, password_hash, organization);
	}
	public static UserType login(String session_id, String user_name, String password_hash, OrganizationType organization) throws FactoryException, ArgumentException
	{
		if (session_id == null) throw new FactoryException("Session is null");
		UserSessionType session = Factories.getSessionFactory().getCreateSession(session_id, organization);
				//SecureAccessLayer.GetCreateSession(session_id);
		return login(session, user_name, password_hash,organization);
	}
	public static UserType login(UserSessionType session, String user_name, String password_hash, OrganizationType organization) throws FactoryException, ArgumentException{
		UserType user = null;
		
		if (session == null)
		{
			throw new FactoryException("New session was not allocated for account '" + user_name + "'");
		}
		if(user_name == null || password_hash == null || organization == null){
			throw new ArgumentException("One or more arguments were null");
		}
		
		/*
		if (session.getSessionStatus().equals(SessionStatusEnumType.AUTHENTICATED)) user = Factories.getUserFactory().getUserBySession(session);
		
		
	 	//2014/03/17 - Removed - this was causing a problem when logging out /in as the same user in the same session
		logger.info("User: " + (user == null ? "Null User" : user.getName()));
		logger.info("Password: " + (user == null ? "Null User" : user.getPassword()));
		logger.info("Organization: " + (organization == null ? "Null Org" : organization.getName()));
		if(user != null){
			/// If a user logs in with an existing valid session
			/// Then double check the user id from the session with the provided password hash
			/// BUG: The original code tried to match password against the object, whose value is explicitly excluded in the Factory.
			/// NOTE: Password isn't returned from user factory, so it can't be checked in code - it must be matched at the data level.
			List<NameIdType> users = Factories.getUserFactory().getList(new QueryField[]{QueryFields.getFieldId(user), QueryFields.getFieldPassword(password_hash)}, organization);
			if (users.size() == 0){
				throw new FactoryException("Organization " + organization.getId() + " and user id " + user.getId() + " did not match for supplied password and session id " + session.getSessionId()+ ".");
			}
			user = (UserType)users.get(0);
			/ *
			else if (user.getPassword() == null || user.getPassword().equals(password_hash) == false || user.getName().equals(user_name) == false || user.getOrganization() == null || user.getOrganization().getId() != organization.getId())
			{
				throw new FactoryException("Invalid user, password, or organization.");
			}
			* /
		}
		/// user is null
		///
		else
		{
		*/
			/// Slightly different query
			///
		List<NameIdType> users = Factories.getUserFactory().getList(new QueryField[]{QueryFields.getFieldName(user_name), QueryFields.getFieldPassword(password_hash)}, organization);
		if (users.size() == 0){
			throw new FactoryException("Failed to retrieve user");
		}
		user = (UserType)users.get(0);
		//}
		
		session.setUserId(user.getId());
		session.setOrganizationId(user.getOrganization().getId());
		user.setSession(session);
		Factories.getUserFactory().populate(user);
		Factories.getUserFactory().updateUserToCache(user);
		/// }


		//account.IsLoggedIn = true;
		//account.Session.SetBool(KEY_LOGGED_IN,true);
		session.setSessionStatus(SessionStatusEnumType.AUTHENTICATED);
		StatisticsType stats = Factories.getStatisticsFactory().getStatistics(user);
		if(stats == null){
			stats = Factories.getStatisticsFactory().newStatistics(user);
			if(Factories.getStatisticsFactory().addStatistics(stats) == false) throw new FactoryException("Failed to add statistics to user #" + user.getId());
			stats = Factories.getStatisticsFactory().getStatistics(user);
		}
		stats.setAccessedDate(CalendarUtil.getXmlGregorianCalendar(Calendar.getInstance().getTime()));
		if(Factories.getStatisticsFactory().updateStatistics(stats) == false){
			throw new FactoryException("Error updating statistics");
		}
		if(Factories.getSessionFactory().update(session) == false){
			throw new FactoryException("Error updating session");
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
