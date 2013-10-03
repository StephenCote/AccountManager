package org.cote.accountmanager.console;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.util.SecurityUtil;

public class OrganizationCommand {
	public static final Logger logger = Logger.getLogger(OrganizationCommand.class.getName());
	
	public static boolean addOrganization(String parentPath, String name, String parentAdminPassword, String newPassword){
		boolean out_bool = false;
		try{
		OrganizationType org = Factories.getOrganizationFactory().findOrganization(parentPath);
		if(org != null){
			UserType adminUser = SessionSecurity.login("Admin", SecurityUtil.getSaltedDigest(parentAdminPassword), org);
			if(adminUser != null){
				OrganizationType newOrg = Factories.getOrganizationFactory().addOrganization(name,OrganizationEnumType.PUBLIC,org);
				if(newOrg != null && FactoryDefaults.setupOrganization(newOrg, SecurityUtil.getSaltedDigest(newPassword))){
					logger.info("Created organization " + name + " in " + org.getName());
					UserType adminUser2 = SessionSecurity.login("Admin", SecurityUtil.getSaltedDigest(newPassword), newOrg);
					if(adminUser2 != null){
						logger.info("Verified new administrator user");
						SessionSecurity.logout(adminUser2);
						out_bool = true;
					}
					else{
						logger.error("Unable to verify new administrator user");
					}
				}
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
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}

		return out_bool;
	}
	
}
