package org.cote.accountmanager.data.services;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;

public class ServiceUtil {
	public static final Logger logger = Logger.getLogger(FactoryService.class.getName());
	public static boolean isFactorySetup(){
		boolean out_bool = false;
		AccountType rootAcct = null;
		AccountType adminAcct = null;
		UserType root = null;
		UserType admin = null;
		UserType doc = null;
		UserRoleType adminRole = null;
		UserRoleType dataRole = null;
		try{
			dataRole = RoleService.getDataAdministratorUserRole(Factories.getPublicOrganization());
			adminRole = RoleService.getAccountAdministratorUserRole(Factories.getPublicOrganization());
			if(dataRole == null){
				logger.error("Data role in public org is null");
				return out_bool;
			}
			if(adminRole == null){
				logger.error("Admin role in public org is null");
				return out_bool;
			}
			rootAcct = Factories.getAccountFactory().getAccountByName("Root", Factories.getGroupFactory().getDirectoryByName("Root", Factories.getSystemOrganization()));
			root = Factories.getUserFactory().getUserByName("Root", Factories.getSystemOrganization());
			if(rootAcct == null || root == null){
				logger.error("Root account or root user is null");
				return out_bool;
			}
			
			Factories.getUserFactory().populate(root);
			
			admin = Factories.getUserFactory().getUserByName("Admin", Factories.getPublicOrganization());
			doc = Factories.getUserFactory().getUserByName("Document Control", Factories.getPublicOrganization());
			if(admin == null || doc == null){
				logger.error("Admin or Document Control user in public org was null");
				return out_bool;
			}
			Factories.getUserFactory().populate(admin);
			boolean adminHasRole = RoleService.getIsUserInRole(adminRole, admin);
			boolean rootHasRole = RoleService.getIsUserInRole(adminRole, root);
			boolean docHasRole = RoleService.getIsUserInRole(dataRole, doc);
			/* 2014/07/14 - see below comment || docHasRole == false */
			if(adminHasRole == false || rootHasRole == false ){
				logger.warn("Required organization users not found in administration roles.");
				logger.info("Admin " + admin.getId() + " Has Role " + adminRole.getId() + " = " + adminHasRole);
				logger.info("Root " + root.getId() + " Has Role " + adminRole.getId() + " = " + rootHasRole);
				logger.info("Doc " + doc.getId() + " Has Role " + dataRole.getId() + " = " + docHasRole);
				logger.warn("Attempting to rebuild role cache.");
				EffectiveAuthorizationService.rebuildUserRoleCache(Factories.getPublicOrganization());
				EffectiveAuthorizationService.rebuildUserRoleCache(Factories.getSystemOrganization());
				
				/// 2014/07/14 - Removed check for doc control in data admin role per prior authorization changes where doc control is delegated permission instead of receiving carte blanche data admin rigths
				///  || RoleService.getIsUserInRole(dataRole, doc) == false
				if(RoleService.getIsUserInRole(adminRole, admin) == false || RoleService.getIsUserInRole(adminRole, root) == false){
					logger.error("Root, Admin, or Document Control were not in the admin role in the public org.");
					return out_bool;
				}
			}
			out_bool = true;
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return out_bool;
	}
}
