package org.cote.accountmanager.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
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
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.util.ServiceUtil;





public class RoleServiceImpl  {
	
	public static final String defaultDirectory = "~/Users";
	public static final Logger logger = Logger.getLogger(RoleServiceImpl.class.getName());
	
	public static boolean authorizeUser(long orgId, long userId, long roleId, boolean view, boolean edit, boolean delete, boolean create, HttpServletRequest request){
		boolean out_bool = false;

		NameIdType role = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(org != null) role = Factories.getRoleFactory().getRoleById(roleId, org);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(role != null) out_bool = BaseService.authorizeUser(AuditEnumType.ROLE, org, userId, role, view, edit, delete, create, request);
		return out_bool;
	}
	
	public static boolean delete(BaseRoleType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.ROLE, bean, request);
	}
	
	public static boolean add(BaseRoleType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.ROLE, bean, request);
	}
	public static boolean update(BaseRoleType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.ROLE, bean, request);
	}
	public static BaseRoleType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.ROLE, name, request);
	}
	public static BaseRoleType readByParent(long orgId, long parentId, String name,HttpServletRequest request){
		OrganizationType org = null;
		BaseRoleType parent = null;
		try{
			org = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(org != null) parent = Factories.getRoleFactory().getById(parentId, org);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
		if(parent == null) return null;
		return BaseService.readByNameInParent(AuditEnumType.ROLE, parent, name, request);
	}
	public static BaseRoleType readByParent(BaseRoleType parent, String name,HttpServletRequest request){
		return BaseService.readByNameInParent(AuditEnumType.ROLE, parent, name, request);
	}
	public static BaseRoleType readByOrganizationId(long orgId, String name,HttpServletRequest request){
		return BaseService.readByNameInOrganization(AuditEnumType.ROLE, orgId, name, request);
	}	
	public static BaseRoleType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.ROLE, id, request);
	}
	public static int count(long orgId, HttpServletRequest request){
		return BaseService.countByOrganization(AuditEnumType.ROLE, orgId, request);
	}
	public static int countInParent(long orgId, long parentId, HttpServletRequest request){
		OrganizationType org = null;
		BaseRoleType role = null;
		try{
			org = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(org != null) role = Factories.getRoleFactory().getById(parentId, org);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(role == null){
			System.out.println("Invalid parentId reference: " + parentId);
			return 0;
		}
		return BaseService.countInParent(AuditEnumType.ROLE, role, request);
	}
	public static List<UserType> getListOfUsers(UserType user, UserRoleType targRole){
		List<UserType> out_obj = new ArrayList<UserType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All roles",AuditEnumType.ROLE,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "All roles");
		
		if(user==null || SessionSecurity.isAuthenticated(user.getSession()) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}
		if(targRole == null){
			AuditService.denyResult(audit, "Target role is null");
			return null;
		}

		try {
			///AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(
				AuthorizationService.isMapOwner(user, targRole)
				||
				AuthorizationService.isAccountAdministratorInOrganization(user,targRole.getOrganization())
				||
				AuthorizationService.isRoleReaderInOrganization(user, targRole.getOrganization())
			){
				AuditService.permitResult(audit, "Access authorized to list roles");
				out_obj = Factories.getRoleParticipationFactory().getUsersInRole(targRole);
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list roles.");
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
	public static List<UserRoleType> getListForUser(UserType user, UserType targUser){
		List<UserRoleType> out_obj = new ArrayList<UserRoleType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All roles",AuditEnumType.ROLE,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "All roles");
		
		if(user==null || SessionSecurity.isAuthenticated(user.getSession()) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}
		if(targUser == null){
			AuditService.denyResult(audit, "Target user is null");
			return null;
		}

		try {
			///AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(
				user.getId() == targUser.getId()
				||
				AuthorizationService.isAccountAdministratorInOrganization(user,targUser.getOrganization())
				||
				AuthorizationService.isRoleReaderInOrganization(user, targUser.getOrganization())
			){
				AuditService.permitResult(audit, "Access authorized to list roles");
				out_obj = (List<UserRoleType>)Factories.getRoleParticipationFactory().getUserRoles(targUser);
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list roles.");
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
	public static List<BaseRoleType> getListInOrganization(UserType user, OrganizationType org, int startRecord, int recordCount){
		///return BaseService.getGroupList(AuditEnumType.ROLE, user, path, startRecord, recordCount);
		

		List<BaseRoleType> out_obj = new ArrayList<BaseRoleType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All roles",AuditEnumType.ROLE,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "All roles");
		
		if(user==null || SessionSecurity.isAuthenticated(user.getSession()) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}

		try {
			///AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(
				AuthorizationService.isAccountAdministratorInOrganization(user,org)
				||
				AuthorizationService.isRoleReaderInOrganization(user, org)
			){
				AuditService.permitResult(audit, "Access authorized to list roles");
				out_obj = getList(null,startRecord,recordCount,org);
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list roles.");
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
	public static List<BaseRoleType> getListInParent(UserType user, BaseRoleType parentRole, int startRecord, int recordCount){
		///return BaseService.getGroupList(AuditEnumType.ROLE, user, path, startRecord, recordCount);
		

		List<BaseRoleType> out_obj = new ArrayList<BaseRoleType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All roles",AuditEnumType.ROLE,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "All roles");
		
		if(user==null || SessionSecurity.isAuthenticated(user.getSession()) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}

		try {
			///AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(
				AuthorizationService.canViewRole(user, parentRole)
				||
				AuthorizationService.isRoleReaderInMapOrganization(user, parentRole)
			){
				AuditService.permitResult(audit, "Access authorized to list roles");
				out_obj = getList(parentRole,startRecord,recordCount,parentRole.getOrganization() );
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list roles.");
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
	private static List<BaseRoleType> getList(BaseRoleType parentRole, int startRecord, int recordCount, OrganizationType organization) throws ArgumentException, FactoryException {

		if(parentRole == null) return Factories.getRoleFactory().getRoleList(startRecord, recordCount, organization);
		return Factories.getRoleFactory().getRoleList(parentRole,startRecord, recordCount, organization);
		
	}
	
	
	
}
