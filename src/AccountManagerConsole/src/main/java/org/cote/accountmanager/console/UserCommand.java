package org.cote.accountmanager.console;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.PersonService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.SecurityUtil;

public class UserCommand {
public static final Logger logger = Logger.getLogger(UserCommand.class.getName());
	
	public static boolean addUser(String orgPath, String name, String adminPassword, String newPassword, String email){
		boolean out_bool = false;
		try{
		OrganizationType org = Factories.getOrganizationFactory().findOrganization(orgPath);
		if(org != null){
			UserType adminUser = SessionSecurity.login("Admin", SecurityUtil.getSaltedDigest(adminPassword), org);
			if(adminUser != null){
				AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, UserCommand.class.getName(), AuditEnumType.USER, "Admin");
				AuditService.targetAudit(audit, AuditEnumType.USER, name);
				out_bool = PersonService.createUserAsPerson(audit, name, SecurityUtil.getSaltedDigest(newPassword), email, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, org);
				SessionSecurity.logout(adminUser);
			}
			else{
				logger.error("Failed to login as admin user.");
			}
		}
		else{
			logger.error("Organization was not found");
		}
		}
		catch(ArgumentException ae){
			logger.error(ae.getMessage());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} 

		return out_bool;
	}
	
}
