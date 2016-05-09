/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.data.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.ParticipationFactory;
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
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;

public class AuthorizationService {
	public static final Logger logger = Logger.getLogger(AuthorizationService.class.getName());
	private static Map<FactoryEnumType,ParticipationFactory> partFactories = new HashMap<FactoryEnumType,ParticipationFactory>();
	
	public static void registerParticipationFactory(FactoryEnumType factType,ParticipationFactory fact){
		partFactories.put(factType, fact);
	}
	/// Return true if the factor type has a corresponding participation table
	///
	public static boolean canBeAuthorized(FactoryEnumType factType){
		boolean out_bool = false;
		return partFactories.containsKey(factType);
		/*
		switch(factType){
			case GROUP:
			case DATA:
			case FUNCTION:
			case PERSON:
			case CONTACTINFORMATION:
			case POLICY:
			case ROLE:
			case RULE:
			case TAG:
			/// Rocket Factories
			case CASE:
			case FORMELEMENT:
			case FORM:
			case GOAL:
			case LIFECYCLE:
			case METHODOLOGY:
			case MODEL:
			case MODULE:
			case PROCESS:
			case PROCESSSTEP:
			case PROJECT:
			case SCHEDULE:
			case TASK:
			case TICKET:
			case VALIDATIONRULE:
			case WORK:
				out_bool = true;
				break;

				
			default:
				logger.warn(factType.toString() + " does not have a participation factory");
				break;
		}
		*/
		//return out_bool;
	}
	
	/// 2015/10/21
	/// By design, isAuthorized will check privilege in the following order:
	/// 1) Ownership
	/// 2) System privilege
	/// 3) If object is scoped to a directory:
	///	   a) Group ownership
	///    b) Entitled access to group
	/// 4) Entitled access to object
	/// While DENY_PERMISSION is supported at a persistence level, it is not yet factored into the database queries or authorization logic
	/// Therefore, don't grant system admin or group edit privileges to members for who entitlement checks will be exercised in that same scope
	/// because it will always return true
	///
	public static boolean isAuthorized(NameIdType object, NameIdType member, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{

		if(object == null || member == null || permissions== null || permissions.length == 0){
			logger.error("Null reference");
			return false;
		}
		String authStr = EffectiveAuthorizationService.getEntitlementCheckString(object, member, permissions);
		if (member.getNameType() == NameEnumType.USER && isMapOwner(member, object))
		{
			logger.warn("Authorized As Object Owner: " + authStr);
			return true;
		}
		
		if (RoleService.isMemberActor(member)){
			BaseRoleType role = RoleService.getSystemRoleForMemberByMapType(object, member);
			// NOTE: This role check DOES include effective role membership
			//
			if(role != null && RoleService.getIsMemberInEffectiveRole(member, role)){
				logger.warn("Authorized With System Privilege: " + authStr);
				return true;
			}
		}
		boolean isCreate = isCreateAuthorization(permissions,object.getOrganizationId());
		if(isAuthorizedByTypeRule(object, member, permissions,isCreate)){
			logger.warn("Authorized By Rule: " + authStr);
			return true;
		}
    	/*
		StringBuffer buff = new StringBuffer();
    	for(int i = 0; i < permissions.length;i++){
    		if(i > 0) buff.append(", ");
    		buff.append(permissions[i].getUrn());
    	}
    	*/
		if(isCreate && (object.getNameType() == NameEnumType.PERMISSION || object.getNameType() == NameEnumType.ROLE || FactoryService.isDirectoryType(object.getNameType()))){
			/// Should have been authorized by 
			logger.warn("Did Not Authorize By Rule For Create Permission on Qualifying Objects: " + authStr);
			return false;
		}

		FactoryEnumType factType = FactoryEnumType.fromValue(object.getNameType().toString());
        if (
        	canBeAuthorized(factType)
        	&&
        	EffectiveAuthorizationService.getEntitlementsGrantAccess(object,member, permissions)
        ){

        	logger.info("Authorized By Entitlement: " + authStr);
        	return true;
        }
        else{
        	logger.warn("Did Not Authorize By Entitlement: " + authStr);
        }
	       
        return false;

	}
	
	/*
	 * isAuthorizedByTypeRule includes specialized business rules for certain cases, including:
	 * 1) Rule for taking group level authZ over object authZ for DirectoryGroupType objects,
	 * 2) Rule for permitting read access to 
	 */
	
	private static boolean isAuthorizedByTypeRule(NameIdType object, NameIdType member, BasePermissionType[] permissions, boolean isCreate) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		/// DIRECTORYGROUPTYPE
        /// TODO: Note, this currently favors the parent permission oven the granular permission
        /// 2015/06/22 - temporarly included direct ownership of the parent directory
		///
		if(FactoryService.isDirectoryType(object.getNameType())){
			BaseGroupType group = Factories.getGroupFactory().getGroupById(((NameIdDirectoryGroupType)object).getGroupId(),object.getOrganizationId());
			if (member.getNameType() == NameEnumType.USER && isMapOwner(member, object))
			{
				logger.warn("Authorized " + member.getUrn() + " for " + object.getUrn() + " as group owner of " + group.getUrn());
				out_bool = true;
			}
			else if(EffectiveAuthorizationService.getEntitlementsGrantAccess(group,member, new BasePermissionType[] { getEditGroupPermission(group.getOrganizationId())})){
				logger.warn("Authorized " + member.getUrn() + " for " + object.getUrn() + " with group edit privileges");
				out_bool = true;				
			}
		}
		else if(isCreate && (object.getNameType() == NameEnumType.PERMISSION || object.getNameType() == NameEnumType.ROLE)){
			logger.info("*** AUTHORIZATION: CHECK CREATE APPROVAL FOR PARENT OBJECT");
			NameIdFactory fact = Factories.getFactory(FactoryEnumType.valueOf(object.getNameType().toString()));
			NameIdType parent = fact.getById(object.getParentId(), object.getOrganizationId());
			if(parent != null){
				out_bool = isAuthorized(parent,member,new BasePermissionType[]{AuthorizationService.getEditPermissionForMapType(object.getNameType(), object.getOrganizationId())});
			}
		}
		/// GRANT_ALL to PERMISSION for USER if USER has PermissionReader Role
		/// TODO: Change this to permission admin
		///
		else if(object.getNameType() == NameEnumType.PERMISSION && member.getNameType() == NameEnumType.USER){
			out_bool = RoleService.getIsUserInEffectiveRole(RoleService.getPermissionReaderUserRole(member.getOrganizationId()),(UserType)member);
		}
		/// Imply ROLEREAD for USER on ROLE if USER has RoleReader Role
		///
		else if(object.getNameType() == NameEnumType.ROLE && member.getNameType() == NameEnumType.USER && containsPermission(permissions,getViewRolePermission(object.getOrganizationId()))){
			out_bool = isRoleReaderInOrganization((UserType)member,object.getOrganizationId());
		}
		/// Imply USERREAD for USER on USER if USER has AccountReader Role
		/// NOTE: AccountReader is currently a misnomer because it applies to both Accounts and Users, stemming from the library originally being centric to accounts and users being added later on
		///
		else if(object.getNameType() == NameEnumType.USER && member.getNameType() == NameEnumType.USER && containsPermission(permissions,getViewObjectPermission(object.getOrganizationId()))){
			out_bool = isAccountReaderInOrganization((UserType)member,object.getOrganizationId());
		}
		return out_bool;
	}
	private static boolean isCreateAuthorization(BasePermissionType[] permissions,long organizationId) throws FactoryException, ArgumentException{
		boolean out_bool = false;
		if(
			containsPermission(permissions,getCreateApplicationPermission(organizationId))
			||
			containsPermission(permissions,getCreateDataPermission(organizationId))
			||
			containsPermission(permissions,getCreateGroupPermission(organizationId))
			||
			containsPermission(permissions,getCreateObjectPermission(organizationId))
			||
			containsPermission(permissions,getCreateRolePermission(organizationId))
		){
			out_bool = true;
		}

		return out_bool;
	}
	private static boolean containsPermission(BasePermissionType[] permissions, BasePermissionType permission){
		boolean incl = false;
		for(int i = 0; i < permissions.length;i++){
			if(permissions[i].getId().compareTo(permission.getId())==0L){
				incl = true;
				break;
			}
		}
		return incl;
	}
	
	public static boolean deauthorize(UserType admin, NameIdType object) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		FactoryEnumType factType = FactoryEnumType.fromValue(object.getNameType().toString());
		if(canBeAuthorized(factType) == false){
			logger.error("Factory " + factType.toString() + " does not support discrete authorization");
			return false;
		}
		ParticipationFactory partFactory = partFactories.get(factType);
		if(!isAuthorized(object,admin,new BasePermissionType[]{getEditPermissionForMapType(admin.getNameType(), object.getOrganizationId())})){
			logger.warn("User " + admin.getName() + " (#" + admin.getId() + ")" + " is not authorized to change object " + object.getName() + " (#" + object.getId() + ")");
			return false;
		}
		out_bool = partFactory.deleteParticipantsWithAffect(new long[]{object.getId()}, object.getOrganizationId());
		if(out_bool == true){
			EffectiveAuthorizationService.clearCache(object);
			EffectiveAuthorizationService.pendUpdate(object);
		}
		return out_bool;
	}
	public static boolean authorizeToDelete(UserType admin, NameIdType object, NameIdType member, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		return authorize(admin, object, member, AuthorizationService.getDeletePermissionForMapType(object.getNameType(), object.getOrganizationId()),enable);
	}
	public static boolean authorizeToEdit(UserType admin, NameIdType object, NameIdType member, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		return authorize(admin, object, member, AuthorizationService.getEditPermissionForMapType(object.getNameType(), object.getOrganizationId()),enable);
	}
	public static boolean authorizeToCreate(UserType admin, NameIdType object, NameIdType member, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		return authorize(admin, object, member, AuthorizationService.getCreatePermissionForMapType(object.getNameType(), object.getOrganizationId()),enable);
	}
	public static boolean authorizeToView(UserType admin, NameIdType object, NameIdType member, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		return authorize(admin, object, member, AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId()),enable);
	}
	public static boolean authorize(UserType admin, NameIdType object, NameIdType member, BasePermissionType permission, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		FactoryEnumType factType = FactoryEnumType.fromValue(object.getNameType().toString());
		if(canBeAuthorized(factType) == false){
			logger.error("Factory " + factType.toString() + " does not support discrete authorization");
			return false;
		}
		ParticipationFactory partFactory = partFactories.get(factType);
		if(!isAuthorized(object,admin,new BasePermissionType[]{getEditPermissionForMapType(admin.getNameType(), object.getOrganizationId())})){
			logger.warn("User " + admin.getName() + " (#" + admin.getId() + ")" + " is not authorized to change object " + object.getName() + " (#" + object.getId() + ")");
			return false;
		}
		logger.info("AUTHORIZE " + partFactory.getFactoryType().toString() + " " + EffectiveAuthorizationService.getEntitlementCheckString(object, member, new BasePermissionType[]{permission}));
		ParticipantEnumType part_type = ParticipantEnumType.valueOf(member.getNameType().toString());
		BaseParticipantType bp = partFactory.getParticipant(object, member, part_type, permission, AffectEnumType.GRANT_PERMISSION);
		
		boolean out_boolean = false;
		if (enable)
		{
			if (bp != null) return true;
			bp = partFactory.newParticipant(object, member, part_type, permission, AffectEnumType.GRANT_PERMISSION);
			out_boolean = partFactory.addParticipant(bp);
		}
		else
		{
			if (bp == null) out_boolean = true;
			else out_boolean = partFactory.deleteParticipant(bp);
		}
		if(out_boolean){
			/// Flag the object and member for cache updates
			/// These updates won't be processed until the rebuildPending method is called
			///
			/// TODO: clearing the member won't clear it off the object authZ cache - clearing the object cache nukes all authZ checks
			/// obviously, this won't scale, sothe cache cleanup needs to be revised
			///
			EffectiveAuthorizationService.clearCache(member);
			EffectiveAuthorizationService.clearCache(object);
			EffectiveAuthorizationService.pendUpdate(object);
			EffectiveAuthorizationService.pendUpdate(member);
		}
		return out_boolean;
	}

	
	
	public static <T> boolean authorizeRoleType(UserType adminUser, BaseRoleType targetRole, T bucket, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		NameIdType tBucket = (NameIdType)bucket;
		boolean out_bool = false;
		
		switch(tBucket.getNameType()){
			case DATA:
				DataType data = (DataType)bucket;
				if(
					switchData(adminUser, targetRole, data, getViewDataPermission(data.getOrganizationId()), view)
					&&
					switchData(adminUser, targetRole, data, getEditDataPermission(data.getOrganizationId()), edit)
					&&
					switchData(adminUser, targetRole, data, getDeleteDataPermission(data.getOrganizationId()), delete)
					&&
					switchData(adminUser, targetRole, data, getCreateDataPermission(data.getOrganizationId()), create)
				){
					out_bool = true;
				
					EffectiveAuthorizationService.pendDataUpdate(data);
				}

				break;
			case GROUP:
				BaseGroupType group = (BaseGroupType)bucket;
				if(
					switchGroup(adminUser, targetRole, group, getViewGroupPermission(group.getOrganizationId()), view)
					&&
					switchGroup(adminUser, targetRole, group, getEditGroupPermission(group.getOrganizationId()), edit)
					&&
					switchGroup(adminUser, targetRole, group, getDeleteGroupPermission(group.getOrganizationId()), delete)
					&&
					switchGroup(adminUser, targetRole, group, getCreateGroupPermission(group.getOrganizationId()), create)
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
				switchData(adminUser, targetUser, Data, getViewDataPermission(Data.getOrganizationId()), view);
				switchData(adminUser, targetUser, Data, getEditDataPermission(Data.getOrganizationId()), edit);
				switchData(adminUser, targetUser, Data, getDeleteDataPermission(Data.getOrganizationId()), delete);
				switchData(adminUser, targetUser, Data, getCreateDataPermission(Data.getOrganizationId()), create);
				out_bool = true;
				break;
			case ROLE:
				BaseRoleType role = (BaseRoleType)bucket;
				switchRole(adminUser, targetUser, role, getViewRolePermission(role.getOrganizationId()), view);
				switchRole(adminUser, targetUser, role, getEditRolePermission(role.getOrganizationId()), edit);
				switchRole(adminUser, targetUser, role, getDeleteRolePermission(role.getOrganizationId()), delete);
				switchRole(adminUser, targetUser, role, getCreateRolePermission(role.getOrganizationId()), create);
				out_bool = true;
				break;
			case GROUP:
				BaseGroupType group = (BaseGroupType)bucket;
				switchGroup(adminUser, targetUser, group, getViewGroupPermission(group.getOrganizationId()), view);
				switchGroup(adminUser, targetUser, group, getEditGroupPermission(group.getOrganizationId()), edit);
				switchGroup(adminUser, targetUser, group, getDeleteGroupPermission(group.getOrganizationId()), delete);
				switchGroup(adminUser, targetUser, group, getCreateGroupPermission(group.getOrganizationId()), create);
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
		return isAccountAdministratorInOrganization(account,map.getOrganizationId());
	}
	public static boolean isDataAdministratorInMapOrganization(AccountType account, NameIdType map) throws ArgumentException, FactoryException
	{
		return isDataAdministratorInOrganization(account,map.getOrganizationId());
	}
	public static boolean isAccountAdministratorInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isAccountAdministratorInOrganization(user,map.getOrganizationId());
	}
	public static boolean isDataAdministratorInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isDataAdministratorInOrganization(user,map.getOrganizationId());
	}
	public static boolean isObjectAdministratorInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isObjectAdministratorInOrganization(user,map.getOrganizationId());
	}
	public static boolean isObjectReaderInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isObjectReaderInOrganization(user,map.getOrganizationId());
	}
	public static boolean isAccountReaderInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isAccountReaderInOrganization(user,map.getOrganizationId());
	}
	public static boolean isRoleReaderInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isRoleReaderInOrganization(user,map.getOrganizationId());
	}
	public static boolean isGroupReaderInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isGroupReaderInOrganization(user,map.getOrganizationId());
	}
	public static boolean isDataReaderInMapOrganization(UserType user, NameIdType map) throws ArgumentException, FactoryException
	{
		return isDataReaderInOrganization(user,map.getOrganizationId());
	}
	public static boolean isAccountAdministratorInOrganization(AccountType account, long organizationId) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsAccountInRole(RoleService.getAccountAdministratorAccountRole(organizationId), account)
				||
				RoleService.getIsAccountInRole(RoleService.getAccountAdministratorUserRole(organizationId), account)
			);
	}
	public static boolean isDataAdministratorInOrganization(AccountType account, long organizationId) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsAccountInRole(RoleService.getDataAdministratorAccountRole(organizationId), account)
				||
				RoleService.getIsAccountInRole(RoleService.getDataAdministratorUserRole(organizationId), account)
				);
	}
	public static boolean isAccountAdministratorInOrganization(UserType user, long organizationId) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getAccountAdministratorAccountRole(organizationId), user)
				||
				RoleService.getIsUserInRole(RoleService.getAccountAdministratorUserRole(organizationId), user)
			);
	}
	public static boolean isDataAdministratorInOrganization(UserType user, long organizationId) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getDataAdministratorAccountRole(organizationId), user)
				||
				RoleService.getIsUserInRole(RoleService.getDataAdministratorUserRole(organizationId), user)
				);
	}
	public static boolean isObjectAdministratorInOrganization(UserType user, long organizationId) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getObjectAdministratorAccountRole(organizationId), user)
				||
				RoleService.getIsUserInRole(RoleService.getObjectAdministratorUserRole(organizationId), user)
				);
	}
	public static boolean isAccountReaderInOrganization(UserType user, long organizationId) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getAccountUsersReaderAccountRole(organizationId), user)
				||
				RoleService.getIsUserInRole(RoleService.getAccountUsersReaderUserRole(organizationId), user)
			);
	}
	public static boolean isRoleReaderInOrganization(UserType user, long organizationId) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getRoleReaderAccountRole(organizationId), user)
				||
				RoleService.getIsUserInRole(RoleService.getRoleReaderUserRole(organizationId), user)
			);
	}
	public static boolean isGroupReaderInOrganization(UserType user, long organizationId) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getGroupReaderAccountRole(organizationId), user)
				||
				RoleService.getIsUserInRole(RoleService.getGroupReaderUserRole(organizationId), user)
			);
	}
	public static boolean isDataReaderInOrganization(UserType user, long organizationId) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getDataReaderAccountRole(organizationId), user)
				||
				RoleService.getIsUserInRole(RoleService.getDataReaderUserRole(organizationId), user)
			);
	}
	public static boolean isObjectReaderInOrganization(UserType user, long organizationId) throws ArgumentException, FactoryException
	{
		return (
				RoleService.getIsUserInRole(RoleService.getObjectReaderAccountRole(organizationId), user)
				||
				RoleService.getIsUserInRole(RoleService.getObjectReaderUserRole(organizationId), user)
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
	
	public static BasePermissionType getDeletePermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		BasePermissionType per = null;
		switch(type){
			case DATA:
				per = getDeleteDataPermission(organizationId);
				break;
			case GROUP:
				per = getDeleteGroupPermission(organizationId);
				break;
			case ROLE:
				per = getDeleteRolePermission(organizationId);
				break;
			default:
				per = getDeleteObjectPermission(organizationId);
				break;
		}
		return per;
	}
	public static BasePermissionType getViewPermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		BasePermissionType per = null;
		switch(type){
			case DATA:
				per = getViewDataPermission(organizationId);
				break;
			case GROUP:
				per = getViewGroupPermission(organizationId);
				break;
			case ROLE:
				per = getViewRolePermission(organizationId);
				break;
			default:
				per = getViewObjectPermission(organizationId);
				break;
		}
		return per;
	}
	public static BasePermissionType getCreatePermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		BasePermissionType per = null;
		switch(type){
			case DATA:
				per = getCreateDataPermission(organizationId);
				break;
			case GROUP:
				per = getCreateGroupPermission(organizationId);
				break;
			case ROLE:
				per = getCreateRolePermission(organizationId);
				break;
			default:
				per = getCreateObjectPermission(organizationId);
				break;
		}
		return per;
	}
	public static BasePermissionType getEditPermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		BasePermissionType per = null;
		switch(type){
			case DATA:
				per = getEditDataPermission(organizationId);
				break;
			case GROUP:
				per = getEditGroupPermission(organizationId);
				break;
			case ROLE:
				per = getEditRolePermission(organizationId);
				break;
			default:
				per = getEditObjectPermission(organizationId);
				break;
		}
		return per;
	}

	public static BasePermissionType getPermission(String name, PermissionEnumType type, long organizationId) throws FactoryException, ArgumentException
	{
		return Factories.getPermissionFactory().getPermissionByName(name, type, organizationId);
	}
	public static BasePermissionType getEditRolePermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("RoleEdit", PermissionEnumType.ROLE, organizationId);
	}
	public static BasePermissionType getCreateRolePermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("RoleCreate", PermissionEnumType.ROLE, organizationId);
	}
	public static BasePermissionType getViewRolePermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("RoleView", PermissionEnumType.ROLE, organizationId);
	}
	public static BasePermissionType getDeleteRolePermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("RoleDelete", PermissionEnumType.ROLE, organizationId);
	}
	public static BasePermissionType getEditObjectPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("ObjectEdit", PermissionEnumType.OBJECT, organizationId);
	}
	public static BasePermissionType getCreateObjectPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("ObjectCreate", PermissionEnumType.OBJECT,  organizationId);
	}
	public static BasePermissionType getViewObjectPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("ObjectView", PermissionEnumType.OBJECT, organizationId);
	}
	public static BasePermissionType getDeleteObjectPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("ObjectDelete", PermissionEnumType.OBJECT, organizationId);
	}
	
	public static BasePermissionType getEditApplicationPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("ApplicationEdit", PermissionEnumType.APPLICATION, organizationId);
	}
	public static BasePermissionType getCreateApplicationPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("ApplicationCreate", PermissionEnumType.APPLICATION, organizationId);
	}
	public static BasePermissionType getViewApplicationPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("ApplicationView", PermissionEnumType.APPLICATION, organizationId);
	}
	public static BasePermissionType getDeleteApplicationPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("ApplicationDelete", PermissionEnumType.APPLICATION, organizationId);
	}
	
	public static BasePermissionType getEditDataPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("DataEdit", PermissionEnumType.DATA, organizationId);
	}
	public static BasePermissionType getCreateDataPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("DataCreate", PermissionEnumType.DATA, organizationId);
	}
	public static BasePermissionType getViewDataPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("DataView", PermissionEnumType.DATA, organizationId);
	}
	public static BasePermissionType getDeleteDataPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("DataDelete", PermissionEnumType.DATA, organizationId);
	}
	public static BasePermissionType getEditGroupPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("GroupEdit", PermissionEnumType.GROUP, organizationId);
	}
	public static BasePermissionType getCreateGroupPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("GroupCreate", PermissionEnumType.GROUP, organizationId);
	}
	public static BasePermissionType getViewGroupPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("GroupView", PermissionEnumType.GROUP, organizationId);
	}
	public static BasePermissionType getDeleteGroupPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getPermission("GroupDelete", PermissionEnumType.GROUP, organizationId);
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
		if(RoleService.getIsUserInEffectiveRole(RoleService.getPermissionReaderUserRole(user.getOrganizationId()), user)){
			logger.info("TODO: Need to clarify CRUD entitlements for permissions.  Presently, permitting change operations for PermissionReader participation.");
			return true;
		}
		logger.warn("TODO: Permission level authorization pending");
		return false;
		//return EffectiveAuthorizationService.getRoleAuthorization(user,role, new BasePermissionType[] { getEditRolePermission(role.getOrganizationId())} );
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

		return EffectiveAuthorizationService.getRoleAuthorization(user,role, new BasePermissionType[] { getEditRolePermission(role.getOrganizationId())} );
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

		return EffectiveAuthorizationService.getRoleAuthorization(user,role, new BasePermissionType[] { getViewRolePermission(role.getOrganizationId())} );
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

		return EffectiveAuthorizationService.getRoleAuthorization(user,role, new BasePermissionType[] { getDeleteRolePermission(role.getOrganizationId())} );
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

		return EffectiveAuthorizationService.getRoleAuthorization(user,role, new BasePermissionType[] { getCreateRolePermission(role.getOrganizationId())} );
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

		if (RoleService.getDataAdministratorAccountRole(data.getOrganizationId()).getId().compareTo(role.getId())==0)
		{
			return true;
		}

        /// TODO: Note, this currently favors the parent permission oven the granular permission
        ///
		BaseGroupType group = Factories.getGroupFactory().getGroupById(data.getGroupId(),data.getOrganizationId());
        return (
    		EffectiveAuthorizationService.getGroupAuthorization(role,group, new BasePermissionType[] { getDeleteGroupPermission(data.getOrganizationId())} )
    		||
    		EffectiveAuthorizationService.getDataAuthorization(role,data, new BasePermissionType[] { getDeleteDataPermission(data.getOrganizationId())} )
        );

		//return false;
	}
	public static boolean canDeleteData(UserType user, DataType data) throws ArgumentException, FactoryException
	{

		if (isMapOwner(user, data))
		{
			return true;
		}

		if (isDataAdministratorInMapOrganization(user, data))
		{
			return true;
		}

        /// TODO: Note, this currently favors the parent permission oven the granular permission
        /// 2015/06/22 - temporarly included direct ownership of the parent directory
		///
		BaseGroupType group = Factories.getGroupFactory().getGroupById(data.getGroupId(),data.getOrganizationId());
        return (
        	isMapOwner(user,data)
        	||
    		EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getDeleteGroupPermission(data.getOrganizationId())} )
    		||
    		EffectiveAuthorizationService.getDataAuthorization(user,data, new BasePermissionType[] { getDeleteDataPermission(data.getOrganizationId())} )
        );

	}
	public static boolean canChangeData(BaseRoleType role, DataType data) throws FactoryException, ArgumentException
	{

		if (RoleService.getDataAdministratorAccountRole(data.getOrganizationId()).getId().compareTo(role.getId())==0)
		{
			return true;
		}
		/*

		if (
			checkDataPermissions(role, data, new BasePermissionType[] { getEditDataPermission(data.getOrganizationId()), getCreateDataPermission(data.getOrganizationId()) })
		)
		{
			return true;
		}

		return false;
		*/
        /// TODO: Note, this currently favors the parent permission oven the granular permission
        ///
		BaseGroupType group = Factories.getGroupFactory().getGroupById(data.getGroupId(),data.getOrganizationId());
        return (
    		EffectiveAuthorizationService.getGroupAuthorization(role,group, new BasePermissionType[] { getEditGroupPermission(data.getOrganizationId())} )
    		||
    		EffectiveAuthorizationService.getDataAuthorization(role,data, new BasePermissionType[] { getEditDataPermission(data.getOrganizationId())} )
        );

	}
	public static boolean canChangeData(UserType user, DataType data) throws ArgumentException, FactoryException
	{

		if (isMapOwner(user, data))
		{
			return true;
		}

		if (isDataAdministratorInMapOrganization(user,data))
		{
			return true;
		}


        /// TODO: Note, this currently favors the parent permission oven the granular permission
        /// 2015/06/22 - temporarly included direct ownership of the parent directory
		///
		BaseGroupType group = Factories.getGroupFactory().getGroupById(data.getGroupId(),data.getOrganizationId());
        return (
        	isMapOwner(user,group)
        	||
    		EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getEditGroupPermission(data.getOrganizationId())} )
    		||
    		EffectiveAuthorizationService.getDataAuthorization(user,data, new BasePermissionType[] { getEditDataPermission(data.getOrganizationId())} )
        );

	}
    public static boolean canViewData(BaseRoleType role, DataType data) throws FactoryException, ArgumentException
    {

        if (RoleService.getDataAdministratorAccountRole(data.getOrganizationId()).getId().compareTo(role.getId())==0)
        {
            return true;
        }
        if (RoleService.getDataReaderAccountRole(data.getOrganizationId()).getId().compareTo(role.getId())==0)
        {
            return true;
        }
        
        /// TODO: Note, this currently favors the parent permission oven the granular permission
        ///
        return (
    		EffectiveAuthorizationService.getGroupAuthorization(role,Factories.getGroupFactory().getGroupById(data.getGroupId(),data.getOrganizationId()), new BasePermissionType[] { getViewGroupPermission(data.getOrganizationId())} )
    		||
    		EffectiveAuthorizationService.getDataAuthorization(role,data, new BasePermissionType[] { getViewDataPermission(data.getOrganizationId())} )
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
        BaseGroupType group = Factories.getGroupFactory().getGroupById(data.getGroupId(),data.getOrganizationId());
        return (
    		EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getViewGroupPermission(data.getOrganizationId())} )
    		||
    		EffectiveAuthorizationService.getDataAuthorization(user,data, new BasePermissionType[] { getViewDataPermission(data.getOrganizationId())} )
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
		if (!canChangeGroup(admin, group)){
			logger.error("User " + admin.getName() + " is not authorized to change group " + group.getName());
			return false;
		}
		BaseParticipantType bp = getGroupPermissionParticipant(map, group, permission);
		boolean out_boolean = false;
		if (enable)
		{
			if (bp != null){
				logger.debug("Participation for " + map.getName() + " and " + group.getName() + " with " + permission.getName() + " already exists (" + Factories.getGroupParticipationFactory().getCacheKeyName(bp) + ")");
				return true;
			}
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
		if(out_boolean){
			EffectiveAuthorizationService.pendGroupUpdate(group);
		}
		return out_boolean;
	}
	public static boolean setPermission(UserType admin, BaseRoleType role, BaseGroupType group, BasePermissionType permission, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		return switchGroup(admin, role, group, permission, enable);
	}
	public static boolean switchGroup(UserType admin, BaseRoleType role, BaseGroupType group, BasePermissionType permission, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		if (!canChangeGroup(admin, group)){
			logger.error("User " + admin.getName() + " is not authorized to change group " + group.getName());
			return false;
		}

		BaseParticipantType bp = getGroupPermissionParticipant(role, group, permission);
		boolean out_boolean = false;
		if (enable)
		{
			if (bp != null){
				logger.debug("Participation for " + role.getName() + " and " + group.getName() + " with " + permission.getName() + " already exists");
				return true;
			}

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
				RoleService.getDataAdministratorAccountRole(group.getOrganizationId()).getId() == role.getId()
			)
			||
			(
				(group.getGroupType() == GroupEnumType.ACCOUNT || group.getGroupType() == GroupEnumType.USER || group.getGroupType() == GroupEnumType.PERSON)
				&&
				RoleService.getAccountAdministratorAccountRole(group.getOrganizationId()).getId() == role.getId()
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
			checkGroupPermissions(role, group, new BasePermissionType[] { getEditGroupPermission(group.getOrganizationId()), getCreateGroupPermission(group.getOrganizationId()) })
		)
		{
			return true;
		}

		return false;
		*/
		return EffectiveAuthorizationService.getGroupAuthorization(role,group, new BasePermissionType[] { getEditGroupPermission(group.getOrganizationId())} );
	}
	public static boolean canChangeGroup(AccountType account, BaseGroupType group) throws ArgumentException, FactoryException{
		return EffectiveAuthorizationService.getGroupAuthorization(account,group, new BasePermissionType[] { getEditGroupPermission(group.getOrganizationId())} );
	}
	public static boolean canChangeGroup(PersonType person, BaseGroupType group) throws ArgumentException, FactoryException{
		return EffectiveAuthorizationService.getGroupAuthorization(person,group, new BasePermissionType[] { getEditGroupPermission(group.getOrganizationId())} );
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
			checkGroupPermissions(user, group, new BasePermissionType[] { getEditGroupPermission(group.getOrganizationId()), getCreateGroupPermission(group.getOrganizationId()) })
		)
		{
			return true;
		}
        long part_id = ParticipationUtil.getParticipationForMapFromGroupRole(user, group, getEditGroupPermission(group.getOrganizationId()), AffectEnumType.GRANT_PERMISSION);
        if(part_id > 0){
        	return true;
        }
        
		return false;
		*/
		return EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getEditGroupPermission(group.getOrganizationId())} );
	}
    public static boolean canDeleteGroup(BaseRoleType role, BaseGroupType group) throws FactoryException, ArgumentException
    {

        // OK if the account is an administrator in the referenced organization
        //
        if (
            (
                (group.getGroupType() == GroupEnumType.DATA || group.getGroupType() == GroupEnumType.BUCKET)
                &&
                RoleService.getDataAdministratorAccountRole(group.getOrganizationId()).getId() == role.getId()
            )
            ||
            (
                (group.getGroupType() == GroupEnumType.ACCOUNT || group.getGroupType() == GroupEnumType.PERSON)
                &&
                RoleService.getAccountAdministratorAccountRole(group.getOrganizationId()).getId() == role.getId()
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
            checkGroupPermissions(role, group, new BasePermissionType[] { getDeleteGroupPermission(group.getOrganizationId())})
        )
        {
            return true;
        }

        return false;
        */
        return EffectiveAuthorizationService.getGroupAuthorization(role,group, new BasePermissionType[] { getDeleteGroupPermission(group.getOrganizationId())} );
    }
    public static boolean canDeleteGroup(AccountType user, BaseGroupType group) throws ArgumentException, FactoryException
    {
        return EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getDeleteGroupPermission(group.getOrganizationId())} );
    }
    public static boolean canDeleteGroup(PersonType user, BaseGroupType group) throws ArgumentException, FactoryException
    {
        return EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getDeleteGroupPermission(group.getOrganizationId())} );
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

        return EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getDeleteGroupPermission(group.getOrganizationId())} );
    }
	public static boolean canViewGroup(BaseRoleType role, BaseGroupType group) throws FactoryException, ArgumentException
	{

		// OK if the account is an administrator in the referenced organization
		//
		if (
			(
				(group.getGroupType() == GroupEnumType.DATA || group.getGroupType() == GroupEnumType.BUCKET)
				&&
				RoleService.getDataAdministratorAccountRole(group.getOrganizationId()).getId() == role.getId()
			)
			||
			(
				(group.getGroupType() == GroupEnumType.ACCOUNT || group.getGroupType() == GroupEnumType.PERSON)
				&&
				RoleService.getAccountAdministratorAccountRole(group.getOrganizationId()).getId() == role.getId()
			)
			||
			(
				RoleService.getGroupReaderUserRole(group.getOrganizationId()).getId() == role.getId()
			)
		)
		{
			return true;
		}

		return EffectiveAuthorizationService.getGroupAuthorization(role,group, new BasePermissionType[] { getViewGroupPermission(group.getOrganizationId())} );
	}
	public static boolean canViewGroup(AccountType account, BaseGroupType group) throws ArgumentException, FactoryException
	{
		return EffectiveAuthorizationService.getGroupAuthorization(account,group, new BasePermissionType[] { getViewGroupPermission(group.getOrganizationId())} );
	}
	public static boolean canViewGroup(PersonType person, BaseGroupType group) throws ArgumentException, FactoryException
	{
		return EffectiveAuthorizationService.getGroupAuthorization(person,group, new BasePermissionType[] { getViewGroupPermission(group.getOrganizationId())} );
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

		return EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getViewGroupPermission(group.getOrganizationId())} );
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
				RoleService.getDataAdministratorAccountRole(group.getOrganizationId()).getId() == role.getId()
			)
			||
			(
				(group.getGroupType() == GroupEnumType.ACCOUNT || group.getGroupType() == GroupEnumType.PERSON)
				&&
				RoleService.getAccountAdministratorAccountRole(group.getOrganizationId()).getId() == role.getId()
			)
		)
		{
			return true;
		}

		
		return EffectiveAuthorizationService.getGroupAuthorization(role,group, new BasePermissionType[] { getCreateGroupPermission(group.getOrganizationId())} );
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
		return EffectiveAuthorizationService.getGroupAuthorization(user,group, new BasePermissionType[] { getCreateGroupPermission(group.getOrganizationId())} );
	}
	
	public static List<Long> getAuthorizedGroups(UserType user, BasePermissionType[] permissions, long organizationId) throws ArgumentException{
		
		List<Long> ids = new ArrayList<Long>();
		
		if(permissions.length == 0) throw new ArgumentException("One or more rights must be specified");
		
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(connectionType);
		StringBuffer perm = new StringBuffer();
		for(int i = 0; i < permissions.length; i++){
			if(i > 0) perm.append(",");
			perm.append(permissions[i].getId());
		}
		String query = "SELECT DISTINCT groupid FROM ("
				+ "SELECT groupid FROM groupRights " 
				+ "WHERE referencetype = 'USER' "
				+ "AND referenceid = " + token + " "
				+ "AND affectid IN (" + perm.toString() + ") "
				+ "AND organizationId = " + token + " "
				+ "UNION ALL "
				+ "SELECT id AS groupid FROM groups "
				+ "WHERE ownerid = " + token + " "
				+ "AND organizationId = " + token + " "
				+ ") AS userGroupRights"
			;

		try{
			PreparedStatement statement = connection.prepareStatement(query);
			statement.setLong(1, user.getId());
			statement.setLong(2, organizationId);
			statement.setLong(3, user.getId());
			statement.setLong(4, organizationId);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				ids.add(rset.getLong(1));
			}
			rset.close();
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			sqe.printStackTrace();
		}
		finally{
			
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ids;
	}
	
	
	
	
}
