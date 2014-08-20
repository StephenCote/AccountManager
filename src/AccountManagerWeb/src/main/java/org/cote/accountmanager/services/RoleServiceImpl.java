package org.cote.accountmanager.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.util.ServiceUtil;





public class RoleServiceImpl  {
	
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
	
	public static boolean requestAccessToReadUserRole(UserType user) throws FactoryException, ArgumentException, DataAccessException{
		return requestUserRoleAccess(user, RoleService.getAccountUsersReaderUserRole(user.getOrganization()));
	}
	public static boolean requestAccessToReadRoleRole(UserType user) throws FactoryException, ArgumentException, DataAccessException{
		return requestUserRoleAccess(user, RoleService.getRoleReaderUserRole(user.getOrganization()));
	}

	public static boolean requestUserRoleAccess(UserType user, UserRoleType role) throws FactoryException, ArgumentException, DataAccessException{
		boolean bAdd = false;
		if(RoleService.getIsUserInRole(role, user)==false){
			AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, "Role Access", AuditEnumType.USER, user.getName());
			AuditService.targetAudit(audit, AuditEnumType.ROLE, role.getName());
			if(RoleService.addUserToRole(user, role)){
				bAdd = true;
				AuditService.permitResult(audit, "Granted access to role");
			}
			else{
				AuditService.denyResult(audit, "Access to role not granted.");
			}
		}
		return bAdd;
	}
	public static BaseRoleType getUserRole(UserType user, String type, HttpServletRequest request){
		BaseRoleType targetRole = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, "authorizeRole", AuditEnumType.SESSION, request.getSession(true).getId());
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "User role");
		if(user==null) return null;
		AuditService.targetAudit(audit, AuditEnumType.ROLE, user.getName() + " user role");
		RoleEnumType roleType = RoleEnumType.fromValue(type);
		try{
			targetRole = Factories.getRoleFactory().getUserRole(user, roleType, user.getOrganization());
			if(targetRole == null){
				logger.error("Account manager objects not correctly setup.  User role is missing.");
				return null;
			}
			/*
			boolean bAddReadUsers = requestAccessToReadUserRole(user);
			boolean bAddReadRoles = requestAccessToReadRoleRole(user); 
			if(bAddReadUsers || bAddReadRoles){
				EffectiveAuthorizationService.rebuildUserRoleCache(user);
			}
			AuditService.permitResult(audit, "User authorized to read user role");
			*/
			AuditService.permitResult(audit, "Returning user role");
			
		}
		catch(FactoryException fe){
			logger.info(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			logger.info(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			logger.info(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return targetRole;

	}
	
	public static boolean setRole(UserType user, long roleId, AuditEnumType objType, long objId, boolean enable){
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Role " + roleId,objType,"Object #" + objId);
		BaseRoleType role = null;
		NameIdType obj = null;
		try {
			role = Factories.getRoleFactory().getRoleById(roleId, user.getOrganization());
			if(role == null){
				AuditService.denyResult(audit, "Role does not exist");
				return out_bool;
			}
			if(RoleEnumType.fromValue(objType.toString()) != role.getRoleType()){
				AuditService.denyResult(audit, "Role type must match the object type");
				return out_bool;
			}
			if(objType != AuditEnumType.DATA) obj = ((NameIdFactory)BaseService.getFactory(objType)).getById(objId, user.getOrganization());
			else obj = ((DataFactory)BaseService.getFactory(objType)).getDataById(objId, true, user.getOrganization());
			if(obj == null){
				AuditService.denyResult(audit, "Object does not exist");
				return out_bool;
			}
			if(
				BaseService.canViewType(objType, user,obj)
				&&
				BaseService.canChangeType(AuditEnumType.ROLE, user, role)
			){
				boolean set = false;
				switch(objType){
					case PERSON:
						if(enable) set = RoleService.addPersonToRole((PersonType)obj, (PersonRoleType)role);
						else set = RoleService.removePersonFromRole((PersonRoleType)role,(PersonType)obj);
						break;
					case ACCOUNT:
						if(enable) set = RoleService.addAccountToRole((AccountType)obj, (AccountRoleType)role);
						else set = RoleService.removeAccountFromRole((AccountRoleType)role,(AccountType)obj);
						break;
					case USER:
						if(enable) set = RoleService.addUserToRole((UserType)obj, (UserRoleType)role);
						else set = RoleService.removeUserFromRole((UserRoleType)role,(UserType)obj);
						break;
					case GROUP:
						logger.warn("Group to role implementation needs to be expanded to include groups of accounts and groups of persons.");
						if(enable) set = RoleService.addGroupToRole((UserGroupType)obj, (UserRoleType)role);
						else set = RoleService.removeGroupFromRole((UserRoleType)role,(UserGroupType)obj);
						break;
				}
				if(set){
					EffectiveAuthorizationService.pendUpdate(role);
					EffectiveAuthorizationService.pendUpdate(obj);
					EffectiveAuthorizationService.rebuildPendingRoleCache();
					AuditService.permitResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is authorized to change the role.");
					out_bool = true;
				}
				else{
					AuditService.denyResult(audit, "Unable to change the role");
				}
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is not authorized to change the role with this object.");
				return out_bool;
			}

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_bool;
	}
	
	public static BaseRoleType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.ROLE, name, request);
	}
	public static BaseRoleType readByParent(long orgId, long parentId, String name, String type, HttpServletRequest request){
		OrganizationType org = null;
		BaseRoleType parent = null;
		logger.info("Reading " + name + " in #" + parentId + " in org #" + orgId);
		try{
			org = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(org != null) parent = Factories.getRoleFactory().getById(parentId, org);
			else logger.error("Organization id #" + orgId + " is null");
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
		if(parent == null){
			logger.error("Parent id #" + parentId + " is null in organization #" + orgId);
			return null;
		}
		return BaseService.readByNameInParent(AuditEnumType.ROLE, parent, name, type, request);
	}
	public static BaseRoleType readByParent(BaseRoleType parent, String name, String type, HttpServletRequest request){
		BaseRoleType role = BaseService.readByNameInParent(AuditEnumType.ROLE, parent, name, type, request);
		Factories.getAttributeFactory().populateAttributes(role);
		return role;
	}
	public static BaseRoleType readByOrganizationId(long orgId, String name,HttpServletRequest request){
		return BaseService.readByNameInOrganization(AuditEnumType.ROLE, orgId, name, request);
	}	
	public static BaseRoleType readById(long id,HttpServletRequest request){
		BaseRoleType role = BaseService.readById(AuditEnumType.ROLE, id, request);
		Factories.getAttributeFactory().populateAttributes(role);
		return role;
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
	
	public static List<BaseRoleType> getListOfRoles(UserType user, NameIdType type){
		List<BaseRoleType> out_obj = new ArrayList<BaseRoleType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All roles",AuditEnumType.ROLE,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "All roles");
		
		if(SessionSecurity.isAuthenticated(user) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}
		if(type == null){
			AuditService.denyResult(audit, "Target type is null");
			return null;
		}

		try {
			///AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(
				AuthorizationService.isMapOwner(user, type)
				||
				AuthorizationService.isAccountAdministratorInOrganization(user,type.getOrganization())
				||
				//(AuthorizationService.isRoleReaderInOrganization(user, type.getOrganization()) && AuthorizationService.isAccountReaderInOrganization(user, type.getOrganization()))
				AuthorizationService.isRoleReaderInOrganization(user, type.getOrganization())
			){
				AuditService.permitResult(audit, "Access authorized to list roles");
				switch(type.getNameType()){
					case GROUP:
						out_obj = Factories.getGroupParticipationFactory().getRolesInGroup((BaseGroupType)type);
						break;
					case DATA:
						out_obj = Factories.getDataParticipationFactory().getRolesForData((DataType)type);
						break;
					
				}
				
				
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
	
	public static List<UserGroupType> getListOfGroups(UserType user, UserRoleType targRole){
		List<UserGroupType> out_obj = new ArrayList<UserGroupType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All groups in role",AuditEnumType.ROLE,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "All groups in role");
		
		if(SessionSecurity.isAuthenticated(user) == false){
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
				(AuthorizationService.isRoleReaderInOrganization(user, targRole.getOrganization()) && AuthorizationService.isAccountReaderInOrganization(user, targRole.getOrganization()))
			){
				AuditService.permitResult(audit, "Access authorized to list groups in role");
				out_obj = Factories.getRoleParticipationFactory().getGroupsInRole(targRole);
				
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
	public static List<UserType> getListOfUsers(UserType user, UserRoleType targRole){
		List<UserType> out_obj = new ArrayList<UserType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All roles",AuditEnumType.ROLE,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "All roles");
		
		if(SessionSecurity.isAuthenticated(user) == false){
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
				(AuthorizationService.isRoleReaderInOrganization(user, targRole.getOrganization()) && AuthorizationService.isAccountReaderInOrganization(user, targRole.getOrganization()))
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
		
		if(SessionSecurity.isAuthenticated(user) == false){
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
	/*
	public static List<BaseRoleType> getListInOrganization(UserType user, OrganizationType org, int startRecord, int recordCount){
		///return BaseService.getGroupList(AuditEnumType.ROLE, user, path, startRecord, recordCount);
		

		List<BaseRoleType> out_obj = new ArrayList<BaseRoleType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All roles",AuditEnumType.ROLE,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "All roles");
		
		if(SessionSecurity.isAuthenticated(user) == false){
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
	*/
	public static List<BaseRoleType> getListInParent(UserType user, String type, BaseRoleType parentRole, int startRecord, int recordCount){
		///return BaseService.getGroupList(AuditEnumType.ROLE, user, path, startRecord, recordCount);
		

		List<BaseRoleType> out_obj = new ArrayList<BaseRoleType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All roles",AuditEnumType.ROLE,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "All roles");
		
		if(SessionSecurity.isAuthenticated(user) == false){
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
				out_obj = getList(type,parentRole,startRecord,recordCount,parentRole.getOrganization() );
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
	private static List<BaseRoleType> getList(String type,BaseRoleType parentRole, int startRecord, int recordCount, OrganizationType organization) throws ArgumentException, FactoryException {
		//if(parentRole == null) return Factories.getRoleFactory().getRoleList(startRecord, recordCount, organization);
		RoleEnumType roleType = RoleEnumType.fromValue(type);
		List<BaseRoleType> roles = Factories.getRoleFactory().getRoleList(roleType,parentRole,startRecord, recordCount, organization);
		if(BaseService.enableExtendedAttributes){
			for(int i = 0; i < roles.size(); i++){
				Factories.getAttributeFactory().populateAttributes((NameIdType)roles.get(i));
			}
		}
		return roles;
	}
	
	
	
}
