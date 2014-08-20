package org.cote.accountmanager.data.services;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.ParticipationUtil;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;

public class AuthorizationService {
	public static final Logger logger = Logger.getLogger(AuthorizationService.class.getName());

	public static <T> boolean authorizeRoleType(UserType adminUser, BaseRoleType targetRole, T bucket, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		NameIdType tBucket = (NameIdType)bucket;
		boolean out_bool = false;
		
		switch(tBucket.getNameType()){
			case DATA:
				DataType data = (DataType)bucket;
				if(
					switchData(adminUser, targetRole, data, getViewDataPermission(data.getGroup().getOrganization()), view)
					&&
					switchData(adminUser, targetRole, data, getEditDataPermission(data.getGroup().getOrganization()), edit)
					&&
					switchData(adminUser, targetRole, data, getDeleteDataPermission(data.getGroup().getOrganization()), delete)
					&&
					switchData(adminUser, targetRole, data, getCreateDataPermission(data.getGroup().getOrganization()), create)
				){
					out_bool = true;
				
					EffectiveAuthorizationService.pendDataUpdate(data);
				}

				break;
			case GROUP:
				BaseGroupType group = (BaseGroupType)bucket;
				if(
					switchGroup(adminUser, targetRole, group, getViewGroupPermission(group.getOrganization()), view)
					&&
					switchGroup(adminUser, targetRole, group, getEditGroupPermission(group.getOrganization()), edit)
					&&
					switchGroup(adminUser, targetRole, group, getDeleteGroupPermission(group.getOrganization()), delete)
					&&
					switchGroup(adminUser, targetRole, group, getCreateGroupPermission(group.getOrganization()), create)
				){
					out_bool = true;
					EffectiveAuthorizationService.pendGroupUpdate(group);
				}
				break;
			default:
				throw new ArgumentException("Unhandled bucket type: " + tBucket.getNameType());
			
		}
		if(out_bool == false){
			logger.warn(adminUser.getName() + " is not authorized to alter object for role " + targetRole.getName());
		}
		else{
			EffectiveAuthorizationService.pendRoleUpdate(targetRole);
		}
		return out_bool;
	}

	public static <T> boolean authorizeUserType(UserType adminUser, UserType targetUser, T bucket, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		return authorizeUserPersonAccountTypes(adminUser, targetUser, bucket, view, edit, delete, create);
	}
	public static <T> boolean authorizeAccountType(UserType adminUser, AccountType targetAccount, T bucket, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		return authorizeUserPersonAccountTypes(adminUser, targetAccount, bucket, view, edit, delete, create);
	}

	private static <T> boolean authorizeUserPersonAccountTypes(UserType adminUser, NameIdType targetUser, T bucket, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		boolean out_bool = false;
		NameIdType tBucket = (NameIdType)bucket;
		switch(tBucket.getNameType()){
			case DATA:
				DataType Data = (DataType)bucket;
				switchData(adminUser, targetUser, Data, getViewDataPermission(Data.getOrganization()), view);
				switchData(adminUser, targetUser, Data, getEditDataPermission(Data.getOrganization()), edit);
				switchData(adminUser, targetUser, Data, getDeleteDataPermission(Data.getOrganization()), delete);
				switchData(adminUser, targetUser, Data, getCreateDataPermission(Data.getOrganization()), create);
				out_bool = true;
				break;
			case ROLE:
				BaseRoleType role = (BaseRoleType)bucket;
				switchRole(adminUser, targetUser, role, getViewRolePermission(role.getOrganization()), view);
				switchRole(adminUser, targetUser, role, getEditRolePermission(role.getOrganization()), edit);
				switchRole(adminUser, targetUser, role, getDeleteRolePermission(role.getOrganization()), delete);
				switchRole(adminUser, targetUser, role, getCreateRolePermission(role.getOrganization()), create);
				out_bool = true;
				break;
			case GROUP:
				BaseGroupType group = (BaseGroupType)bucket;
				switchGroup(adminUser, targetUser, group, getViewGroupPermission(group.getOrganization()), view);
				switchGroup(adminUser, targetUser, group, getEditGroupPermission(group.getOrganization()), edit);
				switchGroup(adminUser, targetUser, group, getDeleteGroupPermission(group.getOrganization()), delete);
				switchGroup(adminUser, targetUser, group, getCreateGroupPermission(group.getOrganization()), create);
				out_bool = true;
				break;
		}
		if(targetUser.getNameType() == NameEnumType.USER) EffectiveAuthorizationService.pendUserUpdate((UserType)targetUser);
		else if(targetUser.getNameType() == NameEnumType.ACCOUNT) EffectiveAuthorizationService.pendAccountUpdate((AccountType)targetUser);
		else if(targetUser.getNameType() == NameEnumType.PERSON) EffectiveAuthorizationService.pendPersonUpdate((PersonType)targetUser);
		
		return out_bool;
	}

	public static boolean isMapOwner(NameIdType test_owner, NameIdType map)
	{
		//logger.debug("Map Owner == " + test_owner.getId() + "=" + map.getOwnerId() + " == (" + (test_owner.getId() == map.getOwnerId()) + ")");
		return (test_owner.getId().compareTo(map.getOwnerId())==0);
	}
	public static boolean isAccountAdministratorInMapOrganization(AccountType account, NameIdType map) throws ArgumentException, FactoryException
	{
		return isAccountAdministratorInOrganization(account,map.getOrganization());
	}
	public static boolean isDataAdministratorInMapOrganization(AccountType account, NameIdType map) throws ArgumentException, FactoryException
	{
		return isDataAdministratorInOrganization(account,map.getOrganization());
	}
	public static boolean isAccountAdministratorInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isAccountAdministratorInOrganization(user,map.getOrganization());
	}
	public static boolean isDataAdministratorInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isDataAdministratorInOrganization(user,map.getOrganization());
	}
	public static boolean isObjectAdministratorInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isObjectAdministratorInOrganization(user,map.getOrganization());
	}
	public static boolean isObjectReaderInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isObjectReaderInOrganization(user,map.getOrganization());
	}
	public static boolean isAccountReaderInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isAccountReaderInOrganization(user,map.getOrganization());
	}
	public static boolean isRoleReaderInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isRoleReaderInOrganization(user,map.getOrganization());
	}
	public static boolean isGroupReaderInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isGroupReaderInOrganization(user,map.getOrganization());
	}
	public static boolean isDataReaderInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isDataReaderInOrganization(user,map.getOrganization());
	}
	public static boolean isAccountAdministratorInOrganization(AccountType account, OrganizationType organization) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsAccountInRole(RoleService.getAccountAdministratorAccountRole(organization), account)
				||
				RoleService.getIsAccountInRole(RoleService.getAccountAdministratorUserRole(organization), account)
			);
	}
	public static boolean isDataAdministratorInOrganization(AccountType account, OrganizationType organization) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsAccountInRole(RoleService.getDataAdministratorAccountRole(organization), account)
				||
				RoleService.getIsAccountInRole(RoleService.getDataAdministratorUserRole(organization), account)
				);
	}
	public static boolean isAccountAdministratorInOrganization(UserType user, OrganizationType organization) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getAccountAdministratorAccountRole(organization), user)
				||
				RoleService.getIsUserInRole(RoleService.getAccountAdministratorUserRole(organization), user)
			);
	}
	public static boolean isDataAdministratorInOrganization(UserType user, OrganizationType organization) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getDataAdministratorAccountRole(organization), user)
				||
				RoleService.getIsUserInRole(RoleService.getDataAdministratorUserRole(organization), user)
				);
	}
	public static boolean isObjectAdministratorInOrganization(UserType user, OrganizationType organization) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getObjectAdministratorAccountRole(organization), user)
				||
				RoleService.getIsUserInRole(RoleService.getObjectAdministratorUserRole(organization), user)
				);
	}
	public static boolean isAccountReaderInOrganization(UserType user, OrganizationType organization) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getAccountUsersReaderAccountRole(organization), user)
				||
				RoleService.getIsUserInRole(RoleService.getAccountUsersReaderUserRole(organization), user)
			);
	}
	public static boolean isRoleReaderInOrganization(UserType user, OrganizationType organization) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getRoleReaderAccountRole(organization), user)
				||
				RoleService.getIsUserInRole(RoleService.getRoleReaderUserRole(organization), user)
			);
	}
	public static boolean isGroupReaderInOrganization(UserType user, OrganizationType organization) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getGroupReaderAccountRole(organization), user)
				||
				RoleService.getIsUserInRole(RoleService.getGroupReaderUserRole(organization), user)
			);
	}
	public static boolean isDataReaderInOrganization(UserType user, OrganizationType organization) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getDataReaderAccountRole(organization), user)
				||
				RoleService.getIsUserInRole(RoleService.getDataReaderUserRole(organization), user)
			);
	}
	public static boolean isObjectReaderInOrganization(UserType user, OrganizationType organization) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getObjectReaderAccountRole(organization), user)
				||
				RoleService.getIsUserInRole(RoleService.getObjectReaderUserRole(organization), user)
			);
	}
	public static boolean checkDataPermissions(
		BaseRoleType role,
		DataType data,
		BasePermissionType[] permissions
	) throws ArgumentException, FactoryException
	{
		boolean out_boolean = false;
		for (int i = 0; i < permissions.length; i++)
		{
			BaseParticipantType bp = getDataPermissionParticipant(role, data, permissions[i]);
			if (bp != null)
			{
				out_boolean = true;
				break;
			}
		}
		return out_boolean;
	}
	public static BaseParticipantType getDataPermissionParticipant(
		BaseRoleType role,
		DataType data,
		BasePermissionType permission
	) throws ArgumentException, FactoryException
	{
		return getDataPermissionParticipant(role, data, AffectEnumType.GRANT_PERMISSION, permission);
	}
	public static BaseParticipantType getDataPermissionParticipant(
		BaseRoleType role,
		DataType data,
		AffectEnumType affect,
		BasePermissionType permission
	) throws ArgumentException, FactoryException
	{
		return Factories.getDataParticipationFactory().getRoleDataParticipant(data, role, permission, affect);
	}

	public static boolean checkDataPermissions(
		NameIdType map,
		DataType data,
		BasePermissionType[] permissions
	) throws ArgumentException, FactoryException
	{
		boolean out_boolean = false;
		for (int i = 0; i < permissions.length; i++)
		{
			BaseParticipantType bp = getDataPermissionParticipant(map, data, permissions[i]);
			if (bp != null)
			{
				out_boolean = true;
				break;
			}
		}
		return out_boolean;
	}
	public static BaseParticipantType getDataPermissionParticipant(
		NameIdType map,
		DataType data,
		BasePermissionType permission
	) throws ArgumentException, FactoryException
	{
		return getDataPermissionParticipant(map, data, AffectEnumType.GRANT_PERMISSION, permission);
	}
	public static BaseParticipantType getDataPermissionParticipant(
		NameIdType map,
		DataType data,
		AffectEnumType affect,
		BasePermissionType permission
	) throws ArgumentException, FactoryException
	{
		ParticipantEnumType part_type = ParticipantEnumType.valueOf(map.getNameType().toString());
		return Factories.getDataParticipationFactory().getParticipant(data, map, part_type, permission, affect);
		//return Factories.getDataParticipationFactory().getAccountDataParticipant(data, account, permission, affect);
	}


	public static boolean checkGroupPermissions(
		BaseRoleType role,
		BaseGroupType group,
		BasePermissionType[] permissions
	) throws ArgumentException, FactoryException
	{
		boolean out_boolean = false;
		for (int i = 0; i < permissions.length; i++)
		{
			BaseParticipantType bp = getGroupPermissionParticipant(role, group, permissions[i]);
			if (bp != null)
			{
				out_boolean = true;
				break;
			}
		}
		return out_boolean;
	}
	public static BaseParticipantType getGroupPermissionParticipant(
		BaseRoleType role,
		BaseGroupType group,
		BasePermissionType permission
	) throws ArgumentException, FactoryException
	{
		return getGroupPermissionParticipant(role, group, AffectEnumType.GRANT_PERMISSION, permission);
	}
	public static BaseParticipantType getGroupPermissionParticipant(
		BaseRoleType role,
		BaseGroupType group,
		AffectEnumType affect,
		BasePermissionType permission
	) throws ArgumentException, FactoryException
	{
		return Factories.getGroupParticipationFactory().getRoleGroupParticipant(group, role, permission, affect);
	}

	public static boolean checkGroupPermissions(
		NameIdType map,
		BaseGroupType group,
		BasePermissionType[] permissions
	) throws ArgumentException, FactoryException
	{
		boolean out_boolean = false;
		for (int i = 0; i < permissions.length; i++)
		{
			BaseParticipantType bp = getGroupPermissionParticipant(map, group, permissions[i]);
			if (bp != null)
			{
				out_boolean = true;
				break;
			}
		}
		return out_boolean;
	}
	public static BaseParticipantType getGroupPermissionParticipant(
		NameIdType map,
		BaseGroupType group,
		BasePermissionType permission
	) throws ArgumentException, FactoryException
	{
		return getGroupPermissionParticipant(map, group, AffectEnumType.GRANT_PERMISSION, permission);
	}
	public static BaseParticipantType getGroupPermissionParticipant(
		NameIdType map,
		BaseGroupType group,
		AffectEnumType affect,
		BasePermissionType permission
	) throws ArgumentException, FactoryException
	{
		ParticipantEnumType participant_type = ParticipantEnumType.valueOf(map.getNameType().toString());
		return Factories.getGroupParticipationFactory().getGroupParticipant(group, map, participant_type, permission, affect);
	}

	public static boolean checkRolePermissions(
		NameIdType map,
		BaseRoleType role,
		BasePermissionType[] permissions
	) throws ArgumentException, FactoryException
	{
		boolean out_boolean = false;
		for (int i = 0; i < permissions.length; i++)
		{
			BaseParticipantType bp = getRolePermissionParticipant(map, role, permissions[i]);
			if (bp != null)
			{
				out_boolean = true;
				break;
			}
		}
		return out_boolean;
	}
	public static BaseParticipantType getRolePermissionParticipant(
		NameIdType map,
		BaseRoleType role,
		BasePermissionType permission
	) throws ArgumentException, FactoryException
	{
		return getRolePermissionParticipant(map, role, AffectEnumType.GRANT_PERMISSION,permission);
	}
	public static BaseParticipantType getRolePermissionParticipant(
		NameIdType map, 
		BaseRoleType role, 
		AffectEnumType affect,
		BasePermissionType permission
	) throws ArgumentException, FactoryException{
		ParticipantEnumType participant_type = ParticipantEnumType.valueOf(map.getNameType().toString());
		return Factories.getRoleParticipationFactory().getParticipant(role, map, participant_type, permission, affect);
	}

	public static BasePermissionType getPermission(String name, PermissionEnumType type, OrganizationType organization) throws FactoryException, ArgumentException
	{
		return Factories.getPermissionFactory().getPermissionByName(name, type, organization);
	}
	public static BasePermissionType getEditRolePermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("RoleEdit", PermissionEnumType.ROLE, organization);
	}
	public static BasePermissionType getCreateRolePermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("RoleCreate", PermissionEnumType.ROLE, organization);
	}
	public static BasePermissionType getViewRolePermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("RoleView", PermissionEnumType.ROLE, organization);
	}
	public static BasePermissionType getDeleteRolePermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("RoleDelete", PermissionEnumType.ROLE, organization);
	}
	public static BasePermissionType getEditObjectPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("ObjectEdit", PermissionEnumType.OBJECT, organization);
	}
	public static BasePermissionType getCreateObjectPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("ObjectCreate", PermissionEnumType.OBJECT,  organization);
	}
	public static BasePermissionType getViewObjectPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("ObjectView", PermissionEnumType.OBJECT, organization);
	}
	public static BasePermissionType getDeleteObjectPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("ObjectDelete", PermissionEnumType.OBJECT, organization);
	}
	
	public static BasePermissionType getEditApplicationPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("ApplicationEdit", PermissionEnumType.APPLICATION, organization);
	}
	public static BasePermissionType getCreateApplicationPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("ApplicationCreate", PermissionEnumType.APPLICATION, organization);
	}
	public static BasePermissionType getViewApplicationPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("ApplicationView", PermissionEnumType.APPLICATION, organization);
	}
	public static BasePermissionType getDeleteApplicationPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("ApplicationDelete", PermissionEnumType.APPLICATION, organization);
	}
	
	public static BasePermissionType getEditDataPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("DataEdit", PermissionEnumType.DATA, organization);
	}
	public static BasePermissionType getCreateDataPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("DataCreate", PermissionEnumType.DATA, organization);
	}
	public static BasePermissionType getViewDataPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("DataView", PermissionEnumType.DATA, organization);
	}
	public static BasePermissionType getDeleteDataPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("DataDelete", PermissionEnumType.DATA, organization);
	}
	public static BasePermissionType getEditGroupPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("GroupEdit", PermissionEnumType.GROUP, organization);
	}
	public static BasePermissionType getCreateGroupPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("GroupCreate", PermissionEnumType.GROUP, organization);
	}
	public static BasePermissionType getViewGroupPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("GroupView", PermissionEnumType.GROUP, organization);
	}
	public static BasePermissionType getDeleteGroupPermission(OrganizationType organization) throws FactoryException, ArgumentException
	{
		return getPermission("GroupDelete", PermissionEnumType.GROUP, organization);
	}
	public static boolean setPermission(UserType admin, NameIdType map, BaseRoleType role, BasePermissionType permission, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		return switchRole(admin, map, role, permission, enable);
	}
	public static boolean switchRole(UserType admin, NameIdType map, BaseRoleType role, BasePermissionType permission, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		if (!canChangeRole(admin, role)) return false;
		BaseParticipantType bp = getRolePermissionParticipant(map, role, permission);
		boolean out_boolean = false;
		if (enable)
		{
			if (bp != null) return true;
			ParticipantEnumType part_type = ParticipantEnumType.valueOf(map.getNameType().toString());
			bp = Factories.getRoleParticipationFactory().newParticipant(role, map, part_type, permission, AffectEnumType.GRANT_PERMISSION);
			out_boolean = Factories.getRoleParticipationFactory().addParticipant(bp);
		}
		else
		{
			if (bp == null) out_boolean = true;
			else out_boolean = Factories.getRoleParticipationFactory().deleteParticipant(bp);
		}
		if(out_boolean && (role.getRoleType() == RoleEnumType.PERSON || role.getRoleType() == RoleEnumType.USER || role.getRoleType() == RoleEnumType.ACCOUNT)){
			EffectiveAuthorizationService.pendRoleUpdate(role);
		}
		return out_boolean;
	}
	public static boolean canDeletePermission(UserType user, BasePermissionType permission) throws ArgumentException, FactoryException{
		return canChangePermission(user,permission);
	}

	public static boolean canViewPermission(UserType user, BasePermissionType permission) throws ArgumentException, FactoryException{
		return canChangePermission(user,permission);
	}
	public static boolean canChangePermission(UserType user, BasePermissionType permission) throws ArgumentException, FactoryException{

		// OK if the account owns the permission
		// 
		if (isMapOwner(user, permission))
		{
			return true;
		}

		// OK if the permission is an account administrator in the referenced organization
		//
		if (isAccountAdministratorInMapOrganization(user,permission))
		{
			return true;
		}
		logger.warn("TODO: Permission level authorization pending");
		return false;
		//return EffectiveAuthorizationService.getRoleAuthorization(user,role, new BasePermissionType[] { getEditRolePermission(role.getOrganization())} );
	}
	public static boolean canChangeRole(UserType user, BaseRoleType role) throws ArgumentException, FactoryException{

		// OK if the account owns the role
		// 
		if (isMapOwner(user, role))
		{
			return true;
		}

		// OK if the account is an administrator in the referenced organization
		//
		if (isAccountAdministratorInMapOrganization(user,role))
		{
			return true;
		}

		return EffectiveAuthorizationService.getRoleAuthorization(user,role, new BasePermissionType[] { getEditRolePermission(role.getOrganization())} );
	}

	public static boolean canViewRole(UserType user, BaseRoleType role) throws ArgumentException, FactoryException{

		// OK if the account owns the role
		// 
		if (isMapOwner(user, role))
		{
			return true;
		}
		if(isRoleReaderInMapOrganization(user,role)){
			return true;
		}
		// OK if the account is an administrator in the referenced organization
		//
		if (isAccountAdministratorInMapOrganization(user,role))
		{
			return true;
		}

		return EffectiveAuthorizationService.getRoleAuthorization(user,role, new BasePermissionType[] { getViewRolePermission(role.getOrganization())} );
	}
	public static boolean canDeleteRole(UserType user, BaseRoleType role) throws ArgumentException, FactoryException{

		// OK if the account owns the role
		// 
		if (isMapOwner(user, role))
		{
			return true;
		}

		// OK if the account is an administrator in the referenced organization
		//
		if (isAccountAdministratorInMapOrganization(user,role))
		{
			return true;
		}

		return EffectiveAuthorizationService.getRoleAuthorization(user,role, new BasePermissionType[] { getDeleteRolePermission(role.getOrganization())} );
	}
	
	/// This is doubling as a write to container 
	/// where role is the parent role into which a new role needs to be created
	///
	public static boolean canCreateRole(UserType user, BaseRoleType role) throws ArgumentException, FactoryException{

		// OK if the account owns the role
		// 
		if (isMapOwner(user, role))
		{
			return true;
		}

		// OK if the account is an administrator in the referenced organization
		//
		if (isAccountAdministratorInMapOrganization(user,role))
		{
			return true;
		}

		return EffectiveAuthorizationService.getRoleAuthorization(user,role, new BasePermissionType[] { getCreateRolePermission(role.getOrganization())} );
	}
	public static boolean setPermission(UserType admin, NameIdType map, DataType data, BasePermissionType permission, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		return switchData(admin, map, data, permission, enable);
	}
	public static boolean switchData(UserType admin, NameIdType map, DataType data, BasePermissionType permission, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		if (!canChangeData(admin, data)){
			logger.warn("User " + admin.getName() + " (#" + admin.getId() + ")" + " is not authorized to change data " + data.getName() + " (#" + data.getId() + ")");
			return false;
		}
		BaseParticipantType bp = getDataPermissionParticipant(map, data, permission);
		boolean out_boolean = false;
		if (enable)
		{
			if (bp != null) return true;
			ParticipantEnumType part_type = ParticipantEnumType.valueOf(map.getNameType().toString());
			bp = Factories.getDataParticipationFactory().newParticipant(data, map, part_type, permission, AffectEnumType.GRANT_PERMISSION);
			out_boolean = Factories.getDataParticipationFactory().addParticipant(bp);
		}
		else
		{
			if (bp == null) out_boolean = true;
			else out_boolean = Factories.getDataParticipationFactory().deleteParticipant(bp);
		}
		return out_boolean;
	}
	public static boolean setPermission(UserType admin, BaseRoleType role, DataType data, BasePermissionType permission, boolean enable) throws FactoryException, ArgumentException, DataAccessException
	{
		return switchData(admin, role, data, permission, enable);
	}
	public static boolean switchData(UserType admin, BaseRoleType role, DataType data, BasePermissionType permission, boolean enable) throws FactoryException, ArgumentException, DataAccessException
	{
		if (!canChangeData(admin, data)) return false;
		BaseParticipantType bp = getDataPermissionParticipant(role, data, permission);
		boolean out_boolean = false;
		if (enable)
		{
			if (bp != null) return true;
			bp = Factories.getDataParticipationFactory().newRoleDataParticipation(data, role, permission, AffectEnumType.GRANT_PERMISSION);
			out_boolean = Factories.getDataParticipationFactory().addParticipant(bp);
		}
		else
		{
			
			if (bp == null) out_boolean = true;
			else out_boolean = Factories.getDataParticipationFactory().deleteParticipant(bp);
		}
		if(out_boolean) EffectiveAuthorizationService.pendDataUpdate(data);
		return out_boolean;
	}
	public static boolean switchData(UserType admin, DataTagType tag, DataType data, boolean enable) throws FactoryException, ArgumentException, DataAccessException
	{
		if (!canChangeData(admin, data)){
			
			return false;
		}
		DataParticipantType bp = Factories.getTagParticipationFactory().getDataParticipant(tag, data);
		boolean out_boolean = false;
		if (enable)
		{
			if (bp != null) return true;
			bp = Factories.getTagParticipationFactory().newDataTagParticipation(tag, data);
			out_boolean = Factories.getTagParticipationFactory().addParticipant(bp);
		}
		else
		{
			if (bp == null) out_boolean = true;
			else out_boolean = Factories.getTagParticipationFactory().deleteParticipant(bp);
		}
		return out_boolean;
	}
	public static boolean canDeleteData(BaseRoleType role, DataType data) throws FactoryException, ArgumentException
	{

		if (RoleService.getDataAdministratorAccountRole(data.getOrganization()).getId().compareTo(role.getId())==0)
		{
			return true;
		}

        /// TODO: Note, this currently favors the parent permission oven the granular permission
        ///
        return (
    		EffectiveAuthorizationService.getGroupAuthorization(role,data.getGroup(), new BasePermissionType[] { getDeleteGroupPermission(data.getOrganization())} )
    		||
    		EffectiveAuthorizationService.getDataAuthorization(role,data, new BasePermissionType[] { getDeleteDataPermission(data.getOrganization())} )
        );

		//return false;
	}
	public static boolean canDeleteData(UserType user, DataType data) throws ArgumentException, FactoryException
	{

		if (isMapOwner(user, data))
		{
			return true;
		}

		if (isDataAdministratorInMapOrganization(user, data.getGroup()))
		{
			return true;
		}

        /// TODO: Note, this currently favors the parent permission oven the granular permission
        ///
        return (
    		EffectiveAuthorizationService.getGroupAuthorization(user,data.getGroup(), new BasePermissionType[] { getDeleteGroupPermission(data.getOrganization())} )
    		||
    		EffectiveAuthorizationService.getDataAuthorization(user,data, new BasePermissionType[] { getDeleteDataPermission(data.getOrganization())} )
        );

	}
	public static boolean canChangeData(BaseRoleType role, DataType data) throws FactoryException, ArgumentException
	{

		if (RoleService.getDataAdministratorAccountRole(data.getOrganization()).getId().compareTo(role.getId())==0)
		{
			return true;
		}
		/*

		if (
			checkDataPermissions(role, data, new BasePermissionType[] { getEditDataPermission(data.getOrganization()), getCreateDataPermission(data.getGroup().getOrganization()) })
		)
		{
			return true;
		}

		return false;
		*/
        /// TODO: Note, this currently favors the parent permission oven the granular permission
        ///
        return (
    		EffectiveAuthorizationService.getGroupAuthorization(role,data.getGroup(), new BasePermissionType[] { getEditGroupPermission(data.getOrganization())} )
    		||
    		EffectiveAuthorizationService.getDataAuthorization(role,data, new BasePermissionType[] { getEditDataPermission(data.getOrganization())} )
        );

	}
	public static boolean canChangeData(UserType user, DataType data) throws ArgumentException, FactoryException
	{

		if (isMapOwner(user, data))
		{
			return true;
		}

		if (isDataAdministratorInMapOrganization(user,data.getGroup()))
		{
			return true;
		}


        /// TODO: Note, this currently favors the parent permission oven the granular permission
        ///
        return (
    		EffectiveAuthorizationService.getGroupAuthorization(user,data.getGroup(), new BasePermissionType[] { getEditGroupPermission(data.getOrganization())} )
    		||
    		EffectiveAuthorizationService.getDataAuthorization(user,data, new BasePermissionType[] { getEditDataPermission(data.getOrganization())} )
        );

	}
    public static boolean canViewData(BaseRoleType role, DataType data) throws FactoryException, ArgumentException
    {

        if (RoleService.getDataAdministratorAccountRole(data.getOrganization()).getId().compareTo(role.getId())==0)
        {
            return true;
        }
        if (RoleService.getDataReaderAccountRole(data.getOrganization()).getId().compareTo(role.getId())==0)
        {
            return true;
        }
        
        /// TODO: Note, this currently favors the parent permission oven the granular permission
        ///
        return (
    		EffectiveAuthorizationService.getGroupAuthorization(role,data.getGroup(), new BasePermissionType[] { getViewGroupPermission(data.getOrganization())} )
    		||
    		EffectiveAuthorizationService.getDataAuthorization(role,data, new BasePermissionType[] { getViewDataPermission(data.getOrganization())} )
        );
    }
    public static boolean canViewData(UserType user, DataType data) throws ArgumentException, FactoryException
    {

        if (isMapOwner(user, data))
        {
            return true;
        }
        if(isDataReaderInMapOrganization(user,data)){
        	return true;
        }
        if (isDataAdministratorInMapOrganization(user, data))
        {
            return true;
        }

        /// TODO: Note, this currently favors the parent permission oven the granular permission
        ///
        return (
    		EffectiveAuthorizationService.getGroupAuthorization(user,data.getGroup(), new BasePermissionType[] { getViewGroupPermission(data.getOrganization())} )
    		||
    		EffectiveAuthorizationService.getDataAuthorization(user,data, new BasePermissionType[] { getViewDataPermission(data.getOrganization())} )
        );

    }
    
    public static boolean setPermission(UserType admin, NameIdType map, BaseGroupType group, BasePermissionType permission, boolean enable) throws FactoryException, ArgumentException, DataAccessException
    {
    	return switchGroup(admin, map, group, permission, enable);
    }
    /// TODO: Refactor the name here
    ///
	public static boolean switchGroup(UserType admin, NameIdType map, BaseGroupType group, BasePermissionType permission, boolean enable) throws FactoryException, ArgumentException, DataAccessException
	{
		if (!canChangeGroup(admin, group)) return false;
		BaseParticipantType bp = getGroupPermissionParticipant(map, group, permission);
		boolean out_boolean = false;
		if (enable)
		{
			if (bp != null) return true;
			ParticipantEnumType part_type = ParticipantEnumType.valueOf(map.getNameType().toString());
			//logger.debug("Part Type " + part_type);
			bp = Factories.getGroupParticipationFactory().newParticipant(group, map, part_type, permission, AffectEnumType.GRANT_PERMISSION);
			out_boolean = Factories.getGroupParticipationFactory().addParticipant(bp);
		}
		else
		{
			if (bp == null) out_boolean = true;
			else out_boolean = Factories.getGroupParticipationFactory().deleteParticipant(bp);
		}
		//if(out_boolean && map.getNameType() == NameEnumType.USER) EffectiveAuthorizationService.pendUserUpdate((UserType)map);
		return out_boolean;
	}
	public static boolean setPermission(UserType admin, BaseRoleType role, BaseGroupType group, BasePermissionType permission, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		return switchGroup(admin, role, group, permission, enable);
	}
	public static boolean switchGroup(UserType admin, BaseRoleType role, BaseGroupType group, BasePermissionType permission, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		if (!canChangeGroup(admin, group)) return false;
		BaseParticipantType bp = getGroupPermissionParticipant(role, group, permission);
		boolean out_boolean = false;
		if (enable)
		{
			if (bp != null) return true;
			bp = Factories.getGroupParticipationFactory().newRoleGroupParticipation(group, role, permission, AffectEnumType.GRANT_PERMISSION);
			out_boolean = Factories.getGroupParticipationFactory().addParticipant(bp);
		}
		else
		{
			
			if (bp == null) out_boolean = true;
			else out_boolean = Factories.getGroupParticipationFactory().deleteParticipant(bp);
		}
		//if(out_boolean && role.getRoleType() == RoleEnumType.USER) EffectiveAuthorizationService.pendUserRoleUpdate((UserRoleType)role);
		if(out_boolean){
			EffectiveAuthorizationService.pendGroupUpdate(group);
		}
		return out_boolean;
	}
	public static boolean canChangeGroup(BaseRoleType role, BaseGroupType group) throws FactoryException, ArgumentException
	{

		// OK if the account is an administrator in the referenced organization
		//
		if (
			(
				(group.getGroupType() == GroupEnumType.DATA || group.getGroupType() == GroupEnumType.BUCKET)
				&&
				RoleService.getDataAdministratorAccountRole(group.getOrganization()).getId() == role.getId()
			)
			||
			(
				(group.getGroupType() == GroupEnumType.ACCOUNT || group.getGroupType() == GroupEnumType.USER || group.getGroupType() == GroupEnumType.PERSON)
				&&
				RoleService.getAccountAdministratorAccountRole(group.getOrganization()).getId() == role.getId()
			)
		)
		{
			return true;
		}

		// OK if the account participation on the group is affected with GroupEdit or GroupCreate permissions 
		//

		//Factories.getRoleParticipationFactory().GetAccountRoleParticipants(role, account);
		/*
		if (
			checkGroupPermissions(role, group, new BasePermissionType[] { getEditGroupPermission(group.getOrganization()), getCreateGroupPermission(group.getOrganization()) })
		)
		{
			return true;
		}

		return false;
		*/
		return EffectiveAuthorizationService.getGroupAuthorization(role,group, new BasePermissionType[] { getEditGroupPermission(group.getOrganization())} );
	}
	public static boolean canChangeGroup(AccountType account, BaseGroupType group) throws ArgumentException, FactoryException{
		return EffectiveAuthorizationService.getGroupAuthorization(account,group, new BasePermissionType[] { getEditGroupPermission(group.getOrganization())} );
	}
	public static boolean canChangeGroup(PersonType person, BaseGroupType group) throws ArgumentException, FactoryException{
		return EffectiveAuthorizationService.getGroupAuthorization(person,group, new BasePermissionType[] { getEditGroupPermission(group.getOrganization())} );
	}
	public static boolean canChangeGroup(UserType user, BaseGroupType group) throws ArgumentException, FactoryException
	{

		// OK if the account owns the role
		// 
		if (isMapOwner(user, group))
		{
			return true;
		}

		// OK if the account is an administrator in the referenced organization
		//
		if (
			( 
				(group.getGroupType() == GroupEnumType.DATA || group.getGroupType() == GroupEnumType.BUCKET)
				&&
				isDataAdministratorInMapOrganization(user, group)
			)
			||
			(
				(group.getGroupType() == GroupEnumType.ACCOUNT || group.getGroupType() == GroupEnumType.USER || group.getGroupType() == GroupEnumType.PERSON)
				&&
				isAccountAdministratorInMapOrganization(user, group)
			)
		)
		{
			return true;
		}

		// OK if the account participation on the group is affected with GroupEdit or GroupCreate permissions 
		//

		//Factories.getRoleParticipationFactory().GetAccountRoleParticipants(role, account);
		/*
		if (
			checkGroupPermissions(user, group, new BasePermissionType[] { getEditGroupPermission(group.getOrganization()), getCreateGroupPermission(group.getOrganization()) })
		)
		{
			return true;
		}
        long part_id = ParticipationUtil.getParticipationForMapFromGroupRole(user, group, getEditGroupPermission(group.getOrganization()), AffectEnumType.GRANT_PERMISSION);
        if(part_id > 0){
        	return true;
        }
        
		return false;
		*/
		return EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getEditGroupPermission(group.getOrganization())} );
	}
    public static boolean canDeleteGroup(BaseRoleType role, BaseGroupType group) throws FactoryException, ArgumentException
    {

        // OK if the account is an administrator in the referenced organization
        //
        if (
            (
                (group.getGroupType() == GroupEnumType.DATA || group.getGroupType() == GroupEnumType.BUCKET)
                &&
                RoleService.getDataAdministratorAccountRole(group.getOrganization()).getId() == role.getId()
            )
            ||
            (
                (group.getGroupType() == GroupEnumType.ACCOUNT || group.getGroupType() == GroupEnumType.PERSON)
                &&
                RoleService.getAccountAdministratorAccountRole(group.getOrganization()).getId() == role.getId()
            )
        )
        {
            return true;
        }

        // OK if the account participation on the group is affected with GroupDelete permissions 
        //

        //Factories.getRoleParticipationFactory().GetAccountRoleParticipants(role, account);
        /*
        if (
            checkGroupPermissions(role, group, new BasePermissionType[] { getDeleteGroupPermission(group.getOrganization())})
        )
        {
            return true;
        }

        return false;
        */
        return EffectiveAuthorizationService.getGroupAuthorization(role,group, new BasePermissionType[] { getDeleteGroupPermission(group.getOrganization())} );
    }
    public static boolean canDeleteGroup(AccountType user, BaseGroupType group) throws ArgumentException, FactoryException
    {
        return EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getDeleteGroupPermission(group.getOrganization())} );
    }
    public static boolean canDeleteGroup(PersonType user, BaseGroupType group) throws ArgumentException, FactoryException
    {
        return EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getDeleteGroupPermission(group.getOrganization())} );
    }
    public static boolean canDeleteGroup(UserType user, BaseGroupType group) throws ArgumentException, FactoryException
    {

        // OK if the account owns the role
        // 
        if (isMapOwner(user, group))
        {
            return true;
        }

        // OK if the account is an administrator in the referenced organization
        //
        if (
            (
                (group.getGroupType() == GroupEnumType.DATA || group.getGroupType() == GroupEnumType.BUCKET)
                &&
                isDataAdministratorInMapOrganization(user, group)
            )
            ||
            (
                (group.getGroupType() == GroupEnumType.ACCOUNT || group.getGroupType() == GroupEnumType.PERSON)
                &&
                isAccountAdministratorInMapOrganization(user, group)
            )
        )
        {
            return true;
        }

        return EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getDeleteGroupPermission(group.getOrganization())} );
    }
	public static boolean canViewGroup(BaseRoleType role, BaseGroupType group) throws FactoryException, ArgumentException
	{

		// OK if the account is an administrator in the referenced organization
		//
		if (
			(
				(group.getGroupType() == GroupEnumType.DATA || group.getGroupType() == GroupEnumType.BUCKET)
				&&
				RoleService.getDataAdministratorAccountRole(group.getOrganization()).getId() == role.getId()
			)
			||
			(
				(group.getGroupType() == GroupEnumType.ACCOUNT || group.getGroupType() == GroupEnumType.PERSON)
				&&
				RoleService.getAccountAdministratorAccountRole(group.getOrganization()).getId() == role.getId()
			)
			||
			(
				RoleService.getGroupReaderUserRole(group.getOrganization()).getId() == role.getId()
			)
		)
		{
			return true;
		}

		return EffectiveAuthorizationService.getGroupAuthorization(role,group, new BasePermissionType[] { getViewGroupPermission(group.getOrganization())} );
	}
	public static boolean canViewGroup(AccountType account, BaseGroupType group) throws ArgumentException, FactoryException
	{
		return EffectiveAuthorizationService.getGroupAuthorization(account,group, new BasePermissionType[] { getViewGroupPermission(group.getOrganization())} );
	}
	public static boolean canViewGroup(PersonType person, BaseGroupType group) throws ArgumentException, FactoryException
	{
		return EffectiveAuthorizationService.getGroupAuthorization(person,group, new BasePermissionType[] { getViewGroupPermission(group.getOrganization())} );
	}
	public static boolean canViewGroup(UserType user, BaseGroupType group) throws ArgumentException, FactoryException
	{

		if (isMapOwner(user, group))
		{
			return true;
		}

		if (
			(
				(group.getGroupType() == GroupEnumType.DATA || group.getGroupType() == GroupEnumType.BUCKET)
				&&
				isDataAdministratorInMapOrganization(user, group)
			)
			||
			(
				(group.getGroupType() == GroupEnumType.ACCOUNT || group.getGroupType() == GroupEnumType.PERSON)
				&&
				isAccountAdministratorInMapOrganization(user, group)
			)
			||
			(
				isGroupReaderInMapOrganization(user,group)
			)
		)
		{
			return true;
		}

		return EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getViewGroupPermission(group.getOrganization())} );
	}
	// TODO: This is doubling for a Write to group permission, and there needs to be 
	// separate delete from group and write to group permissions
	//
	public static boolean canCreateGroup(BaseRoleType role, BaseGroupType group) throws FactoryException, ArgumentException
	{

		// OK if the account is an administrator in the referenced organization
		//
		if (
			(
				(group.getGroupType() == GroupEnumType.DATA || group.getGroupType() == GroupEnumType.BUCKET)
				&&
				RoleService.getDataAdministratorAccountRole(group.getOrganization()).getId() == role.getId()
			)
			||
			(
				(group.getGroupType() == GroupEnumType.ACCOUNT || group.getGroupType() == GroupEnumType.PERSON)
				&&
				RoleService.getAccountAdministratorAccountRole(group.getOrganization()).getId() == role.getId()
			)
		)
		{
			return true;
		}

		
		return EffectiveAuthorizationService.getGroupAuthorization(role,group, new BasePermissionType[] { getCreateGroupPermission(group.getOrganization())} );
	}
	public static boolean canCreateGroup(UserType user, BaseGroupType group) throws ArgumentException, FactoryException
	{

		if (isMapOwner(user, group))
		{
			return true;
		}

		if (
			(
				(group.getGroupType() == GroupEnumType.DATA || group.getGroupType() == GroupEnumType.BUCKET)
				&&
				isDataAdministratorInMapOrganization(user, group)
			)
			||
			(
				(group.getGroupType() == GroupEnumType.ACCOUNT || group.getGroupType() == GroupEnumType.PERSON)
				&&
				isAccountAdministratorInMapOrganization(user, group)
			)
		)
		{
			return true;
		}
		return EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getCreateGroupPermission(group.getOrganization())} );
	}
	
	
	
	
	
}
