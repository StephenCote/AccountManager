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
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.util.ServiceUtil;


public class PermissionServiceImpl  {
	
	public static final String defaultDirectory = "~/Permissions";
	public static final Logger logger = Logger.getLogger(PermissionServiceImpl.class.getName());
	
	public static boolean delete(BasePermissionType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.PERMISSION, bean, request);
	}
	
	public static boolean add(BasePermissionType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.PERMISSION, bean, request);
	}
	public static boolean update(BasePermissionType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.PERMISSION, bean, request);
	}
	public static BasePermissionType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.PERMISSION, name, request);
	}
		
	public static BasePermissionType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.PERMISSION, id, request);
	}
	public static int countInParent(long orgId, long parentId, HttpServletRequest request){
		OrganizationType org = null;
		BasePermissionType permission = null;
		try{
			org = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(org != null) permission = Factories.getPermissionFactory().getById(parentId, org);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(permission == null){
			System.out.println("Invalid parentId reference: " + parentId);
			return 0;
		}
		return BaseService.countInParent(AuditEnumType.PERMISSION, permission, request);
	}
	
	public static BasePermissionType readByParent(long orgId, long parentId, String name, String type, HttpServletRequest request){
		OrganizationType org = null;
		BasePermissionType parent = null;
		try{
			org = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(org != null) parent = Factories.getPermissionFactory().getById(parentId, org);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
		if(parent == null) return null;
		return BaseService.readByNameInParent(AuditEnumType.PERMISSION, parent, name, type, request);
	}
	public static BasePermissionType readByParent(BasePermissionType parent, String name,String type, HttpServletRequest request){
		BasePermissionType role = BaseService.readByNameInParent(AuditEnumType.PERMISSION, parent, name, type, request);
		Factories.getAttributeFactory().populateAttributes(role);
		return role;
	}
	
	public static BasePermissionType getUserPermission(UserType user, String type, HttpServletRequest request){
		PermissionEnumType ptype = PermissionEnumType.fromValue(type);
		BasePermissionType per = null;
		try {
			per = Factories.getPermissionFactory().getUserPermission(user, ptype, user.getOrganization());
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
		return per;
	}
	
	public static List<BasePermissionType> getListInParent(UserType user, String type, BasePermissionType parentPermission, int startRecord, int recordCount){
		///return BaseService.getGroupList(AuditEnumType.PERMISSION, user, path, startRecord, recordCount);
		

		List<BasePermissionType> out_obj = new ArrayList<BasePermissionType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All permissions",AuditEnumType.PERMISSION,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.PERMISSION, "All permissions");
		
		if(SessionSecurity.isAuthenticated(user) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}

		try {
			///AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(
				AuthorizationService.canChangePermission(user, parentPermission)
			){
				AuditService.permitResult(audit, "Access authorized to list permissions");
				out_obj = getList(type,parentPermission,startRecord,recordCount,parentPermission.getOrganization() );
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list permissions.");
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
	private static List<BasePermissionType> getList(String type, BasePermissionType parentPermission, int startRecord, int recordCount, OrganizationType organization) throws ArgumentException, FactoryException {

		PermissionEnumType ptype = PermissionEnumType.fromValue(type);
		return Factories.getPermissionFactory().getPermissionList(parentPermission,ptype,startRecord, recordCount, organization);
		
	}
	private static boolean setPermission(UserType user, AuditEnumType srcType, long srcId, AuditEnumType targType, long targId, long permissionId, boolean enable){
		NameIdType src = null;
		NameIdType targ = null;
		BasePermissionType perm = null;
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Permission " + permissionId,AuditEnumType.PERMISSION,(user == null ? "Null" : user.getName()));
		try {
			if(srcType != AuditEnumType.DATA) src = ((NameIdFactory)BaseService.getFactory(srcType)).getById(srcId, user.getOrganization());
			else src = ((DataFactory)BaseService.getFactory(srcType)).getDataById(srcId, true, user.getOrganization());
			if(targType != AuditEnumType.DATA) targ = ((NameIdFactory)BaseService.getFactory(targType)).getById(targId, user.getOrganization());
			else targ = ((DataFactory)BaseService.getFactory(srcType)).getDataById(targId, true, user.getOrganization());
			perm = Factories.getPermissionFactory().getById(permissionId, user.getOrganization());
			
			/// To set the permission on or off, the user must:
			/// 1) be able to change src,
			/// 2) be able to change targ,
			/// 3) be able to change permission
			if(
				BaseService.canChangeType(srcType, user,src)
				&&
				BaseService.canChangeType(targType, user, targ)
				&&
				AuthorizationService.canChangePermission(user, perm)
			){
				AuditService.permitResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is authorized to change the permission.");
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to set the permission.");
			}

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_bool;
	}
	public static boolean setPermissionOnGroupForUser(UserType user, long groupId, long userId, long permissionId, boolean enable){
		return setPermission(user, AuditEnumType.GROUP, groupId, AuditEnumType.USER, userId, permissionId, enable);
	}
	

	
}
