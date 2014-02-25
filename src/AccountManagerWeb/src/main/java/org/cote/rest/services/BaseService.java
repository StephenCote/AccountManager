package org.cote.rest.services;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.util.ServiceUtil;

public class BaseService {
	public static final Logger logger = Logger.getLogger(BaseService.class.getName());
	public UserType getUserFromSession(AuditType audit, HttpServletRequest request){
		UserType user = getUserFromSession(request);
		if(SessionSecurity.isAuthenticated(user) == false){
			AuditService.denyResult(audit,  "Invalid user. " + (user == null ? "Null user" : "Status is " + user.getSession().getSessionStatus()));
			System.out.println("User is null or not authenticated");
			return null;
		}
		AuditService.sourceAudit(audit,AuditEnumType.USER,user.getName() + " (#" + user.getId() + ") in Org " + user.getOrganization().getName() + " (#" + user.getOrganization().getId() + ")");
		return user;
	}
	public UserType getUserFromSession(HttpServletRequest request){
		String sessionId = request.getSession(true).getId();
		//System.out.println("Session Id=" + sessionId);
		return getUserFromSession(request, sessionId);
	}
	public UserType getUserFromSession(HttpServletRequest request,String sessionId){

		UserType user = null;
		
		try {
			user = SessionSecurity.getUserBySession(sessionId, ServiceUtil.getOrganizationFromRequest(request));

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return user;
	}
}
