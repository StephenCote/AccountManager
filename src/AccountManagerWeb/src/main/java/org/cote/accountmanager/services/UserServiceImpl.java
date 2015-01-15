package org.cote.accountmanager.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.util.ServiceUtil;





public class UserServiceImpl  {
	
	public static final String defaultDirectory = "~/Users";

	public static boolean delete(UserType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.USER, bean, request);
	}
	
	public static boolean add(UserType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.USER, bean, request);
	}
	public static boolean update(UserType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.USER, bean, request);
	}
	public static UserType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.USER, name, request);
	}
	public static UserType readByOrganizationId(long orgId, String name,HttpServletRequest request){
		return BaseService.readByNameInOrganization(AuditEnumType.USER, orgId, name, request);
	}	
	public static UserType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.USER, id, request);
	}
	public static int count(long orgId, HttpServletRequest request){
		return BaseService.countByOrganization(AuditEnumType.USER, orgId, request);
	}
	
	public static List<UserType> getList(UserType user, OrganizationType organization, long startRecord, int recordCount){
		///return BaseService.getGroupList(AuditEnumType.USER, user, path, startRecord, recordCount);
		

		List<UserType> out_obj = new ArrayList<UserType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All users",AuditEnumType.USER,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.USER, "All users");
		
		if(SessionSecurity.isAuthenticated(user) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}
		if(organization == null){
			AuditService.denyResult(audit,  "Organization is null");
			return null;
		}
		try {
			//AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(
				AuthorizationService.isAccountAdministratorInOrganization(user, organization) == true
				||
				AuthorizationService.isAccountReaderInOrganization(user, organization) == true
			){
				AuditService.permitResult(audit, "Access authorized to list users");
				out_obj = getList(startRecord,recordCount,organization);
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list users.");
				return out_obj;
			}
			
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_obj;
		
	}
	private static List<UserType> getList(long startRecord, int recordCount, OrganizationType organization) throws ArgumentException, FactoryException {

		return Factories.getUserFactory().getUserList(startRecord, recordCount, organization);
		
	}
	
	
	
}
