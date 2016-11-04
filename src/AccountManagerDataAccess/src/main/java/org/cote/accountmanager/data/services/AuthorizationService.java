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
import org.cote.accountmanager.objects.DirectoryGroupType;
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
	private static Map<FactoryEnumType,ParticipationFactory> partFactories = new HashMap<>();
	private static Map<NameEnumType, FactoryEnumType> factoryProviders = new HashMap<>();
	private static final NameEnumType[] actors = new NameEnumType[]{NameEnumType.ACCOUNT, NameEnumType.PERSON, NameEnumType.USER, NameEnumType.ROLE}; 
	public static final String[] PERMISSION_BASE = new String[]{"Create","Delete","View","Edit","Execute"};

	public static void clearProviders(){
		partFactories.clear();
		factoryProviders.clear();
	}
	
	public static void registerAuthorizationProviders(FactoryEnumType factType,NameEnumType objectType, ParticipationFactory fact){
		registerParticipationFactory(factType, fact);
		factoryProviders.put(objectType, factType);
		for(int i = 0; i < actors.length; i++){
			EffectiveAuthorizationService.registerType(objectType, actors[i]);	
		}		
	};
	public static void registerParticipationFactory(FactoryEnumType factType,ParticipationFactory fact){
		logger.debug("Register participation factory: " + factType.toString());
		partFactories.put(factType, fact);

	}
	public static Map<FactoryEnumType, ParticipationFactory> getAuthorizationFactories(){
		return partFactories;
	}
	/// Return true if the factor type has a corresponding participation table
	///
	public static boolean canBeAuthorized(FactoryEnumType factType){
		return partFactories.containsKey(factType);
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
	public static boolean isAuthorized(NameIdType actor, NameIdType object, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		/// || permissions.length == 0
		if(object == null || actor == null || permissions== null){
			logger.error("Null reference");
			return false;
		}
		String authStr = EffectiveAuthorizationService.getEntitlementCheckString(object, actor, permissions);
		logger.debug("*** Assert Authorization: " + authStr);

		
		if(isAuthorizedByInternalDefaultPolicy(actor, object, permissions)){
			// logger.warn("*** Authorized By Rule: " + authStr);
			return true;
		}

		FactoryEnumType factType = FactoryEnumType.fromValue(object.getNameType().toString());
        if (
        	canBeAuthorized(factType)
        	&&
        	EffectiveAuthorizationService.getEntitlementsGrantAccess(object,actor, permissions)
        ){

        	//logger.info("*** Authorized By Entitlement: " + authStr);
        	return true;
        }
        else{
        	logger.warn("*** Did Not Authorize By Entitlement: " + authStr);
        }
	       
        return false;

	}
	
	/*
	 * isAuthorizedByTypeRule includes specialized business rules for certain cases, including:
	 * 1) Rule for taking group level authZ over object authZ for DirectoryGroupType objects,
	 * 2) Rule for permitting read access to 
	 */
	
	private static boolean isAuthorizedByInternalDefaultPolicy(NameIdType actor, NameIdType object, BasePermissionType[] permissions) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		boolean isCreate = isCreateAuthorization(object.getNameType(), permissions,object.getOrganizationId());
		boolean isView = isViewAuthorization(object.getNameType(), permissions,object.getOrganizationId());
		String authStr = EffectiveAuthorizationService.getEntitlementCheckString(object, actor, permissions);
		if (isMapOwner(actor, object))
		{
			//logger.debug("Authorized As Object Owner: " + authStr);
			return true;
		}
		
		if (RoleService.isMemberActor(actor)){
			BaseRoleType role = RoleService.getSystemRoleForMemberByMapType(object, actor);
			// NOTE: This role check DOES include effective role actorship
			//
			if(role != null && RoleService.getIsMemberInEffectiveRole(actor, role)){
				logger.debug("Authorized With System Privilege: " + authStr);
				return true;
			}
		}
		
		/// Check if the actor is a factory admin, or if the permission includes a view and the actor is a factory reader
		///
		if(factoryProviders.containsKey(object.getNameType())){
			NameIdFactory fact = Factories.getFactory(factoryProviders.get(object.getNameType()));
			if(
				RoleService.isFactoryAdministrator(actor, fact)
				||
				(isView && RoleService.isFactoryReader(actor, fact))
			){
				logger.debug("Authorized as factory type administrator or reader");
				return true;
			}
			
		}
		/// DIRECTORYGROUPTYPE
        /// TODO: Note, this currently favors the parent permission oven the granular permission
        /// 2015/06/22 - temporarly included direct ownership of the parent directory
		///
		if(FactoryService.isDirectoryType(object.getNameType())){
			BaseGroupType group = Factories.getGroupFactory().getGroupById(((NameIdDirectoryGroupType)object).getGroupId(),object.getOrganizationId());
			if (actor.getNameType() == NameEnumType.USER && isMapOwner(actor, object))
			{
				logger.warn("Authorized " + actor.getUrn() + " for " + object.getUrn() + " as group owner of " + group.getUrn());
				out_bool = true;
			}
			else if(EffectiveAuthorizationService.getEntitlementsGrantAccess(group,actor, new BasePermissionType[] { getEditPermissionForMapType(NameEnumType.GROUP,group.getOrganizationId())})){
				logger.warn("Authorized " + actor.getUrn() + " for " + object.getUrn() + " with group edit privileges");
				out_bool = true;				
			}
		}
		else if(isCreate && (object.getNameType() == NameEnumType.PERMISSION || object.getNameType() == NameEnumType.ROLE)){
			logger.info("*** AUTHORIZATION: CHECK CREATE APPROVAL FOR PARENT OBJECT");
			NameIdFactory fact = Factories.getFactory(FactoryEnumType.valueOf(object.getNameType().toString()));
			NameIdType parent = fact.getById(object.getParentId(), object.getOrganizationId());
			if(parent != null){
				out_bool = isAuthorized(actor,parent,new BasePermissionType[]{getEditPermissionForMapType(object.getNameType(), object.getOrganizationId())});
			}
		}
		/// GRANT_ALL to PERMISSION for USER if USER has PermissionReader Role
		/// TODO: Change this to permission admin
		///
		else if(object.getNameType() == NameEnumType.PERMISSION && actor.getNameType() == NameEnumType.USER){
			out_bool = RoleService.getIsUserInEffectiveRole(RoleService.getPermissionReaderUserRole(actor.getOrganizationId()),(UserType)actor);
		}

		return out_bool;
	}
	private static boolean isViewAuthorization(NameEnumType objectType, BasePermissionType[] permissions,long organizationId) throws FactoryException, ArgumentException{
		BasePermissionType checkPer = null;
		if(factoryProviders.containsKey(objectType)){
			ParticipationFactory partFact = partFactories.get(factoryProviders.get(objectType));
			checkPer = getRootPermission(partFact.getPermissionPrefix() + "View", partFact.getDefaultPermissionType(), organizationId);
			if(checkPer == null){
				logger.warn("Permission " + partFact.getPermissionPrefix() + "View was null");
			}
		}

		return containsPermission(permissions, checkPer);

	}
	private static boolean isCreateAuthorization(NameEnumType objectType, BasePermissionType[] permissions,long organizationId) throws FactoryException, ArgumentException{
		boolean out_bool = false;
		BasePermissionType checkPer = null;
		if(factoryProviders.containsKey(objectType)){
			ParticipationFactory partFact = partFactories.get(factoryProviders.get(objectType));
			checkPer = getRootPermission(partFact.getPermissionPrefix() + "Create", partFact.getDefaultPermissionType(), organizationId);
		}
		if(checkPer != null){
			return containsPermission(permissions, checkPer);
		}
		logger.warn("OLD OLD isCreateAuthZ Check");

		if(
			containsPermission(permissions,getCreateApplicationPermission(organizationId))
			||
			containsPermission(permissions,getCreateObjectPermission(organizationId))
		){
			out_bool = true;
		}

		return out_bool;
	}
	private static boolean containsPermission(BasePermissionType[] permissions, BasePermissionType permission){
		boolean incl = false;
		if(permission == null){
			logger.error("Check permission is null");
			return incl;
		}
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
		if(partFactory == null){
			logger.error("Participation factory for " + factType.toString() + " is not registered for authorization");
			return false;
		}
		if(!isAuthorized(admin,object,new BasePermissionType[]{getEditPermissionForMapType(admin.getNameType(), object.getOrganizationId())})){
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

	public static boolean authorize(UserType admin, NameIdType actor, NameIdType object, BasePermissionType permission, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		FactoryEnumType factType = FactoryEnumType.fromValue(object.getNameType().toString());
		if(canBeAuthorized(factType) == false){
			logger.error("Factory " + factType.toString() + " does not support discrete authorization");
			return false;
		}
		ParticipationFactory partFactory = partFactories.get(factType);
		if(!isAuthorized(admin,object, new BasePermissionType[]{getEditPermissionForMapType(admin.getNameType(), object.getOrganizationId())})){
			logger.warn("User " + admin.getName() + " (#" + admin.getId() + ")" + " is not authorized to change object " + object.getName() + " (#" + object.getId() + ")");
			return false;
		}
		logger.info("AUTHORIZE " + partFactory.getFactoryType().toString() + " " + EffectiveAuthorizationService.getEntitlementCheckString(object, actor, new BasePermissionType[]{permission}));
		ParticipantEnumType part_type = ParticipantEnumType.valueOf(actor.getNameType().toString());
		BaseParticipantType bp = partFactory.getParticipant(object, actor, part_type, permission, AffectEnumType.GRANT_PERMISSION);
		
		boolean out_boolean = false;
		if (enable)
		{
			if (bp != null) return true;
			bp = partFactory.newParticipant(object, actor, part_type, permission, AffectEnumType.GRANT_PERMISSION);
			out_boolean = partFactory.add(bp);
		}
		else
		{
			if (bp == null) out_boolean = true;
			else out_boolean = partFactory.delete(bp);
		}
		if(out_boolean){
			/// Flag the object and actor for cache updates
			/// These updates won't be processed until the rebuildPending method is called
			///
			/// TODO: clearing the actor won't clear it off the object authZ cache - clearing the object cache nukes all authZ checks
			/// obviously, this won't scale, sothe cache cleanup needs to be revised
			///
			EffectiveAuthorizationService.clearCache(actor);
			EffectiveAuthorizationService.clearCache(object);
			EffectiveAuthorizationService.pendUpdate(object);
			EffectiveAuthorizationService.pendUpdate(actor);
		}
		return out_boolean;
	}

	
	
	public static <T> boolean authorizeType(UserType adminUser, NameIdType actor, NameIdType object, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		boolean out_bool = false;
		if(!factoryProviders.containsKey(actor.getNameType()) || !partFactories.containsKey(factoryProviders.get(actor.getNameType()))){
			logger.error("Actor type " + actor.getNameType() + " is not registered for authorization");
			return out_bool;
		}
		String permissionPrefix = partFactories.get(factoryProviders.get(actor.getNameType())).getPermissionPrefix();
		BasePermissionType viewPermission = getPermission(actor,(NameIdType)object,permissionPrefix + "View");
		BasePermissionType editPermission = getPermission(actor,(NameIdType)object,permissionPrefix + "Edit");
		BasePermissionType delPermission = getPermission(actor,(NameIdType)object,permissionPrefix + "Delete");
		BasePermissionType createPermission = getPermission(actor,(NameIdType)object,permissionPrefix + "Create");
		if(viewPermission == null || editPermission == null || delPermission == null || createPermission == null){
			logger.error("One or more expected permissions were null");
			return false;
		}

		if(
			authorize(adminUser, actor, object, viewPermission,view)
			&&
			authorize(adminUser, actor, object, editPermission,edit)
			&&
			authorize(adminUser, actor, object, delPermission,delete)
			&&
			authorize(adminUser, actor, object, createPermission,create)
		){
			out_bool = true;
		}
		
		return out_bool;
	}

	
	public static boolean isMapOwner(NameIdType test_owner, NameIdType map)
	{
		if(test_owner.getNameType() != NameEnumType.USER){
			return false;
		}
		//logger.debug("Map Owner == " + test_owner.getId() + "=" + map.getOwnerId() + " == (" + (test_owner.getId() == map.getOwnerId()) + ")");
		return (test_owner.getId().compareTo(map.getOwnerId())==0);
	}
	

	public static BasePermissionType getDeletePermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		BasePermissionType per = null;
		if(factoryProviders.containsKey(type)){
			ParticipationFactory partFact = partFactories.get(factoryProviders.get(type));
			return getRootPermission(partFactories.get(factoryProviders.get(type)).getPermissionPrefix() + "Delete", partFact.getDefaultPermissionType(), organizationId);
		}
		switch(type){

			default:
				per = getDeleteObjectPermission(organizationId);
				break;
		}
		return per;
	}
	public static BasePermissionType getViewPermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		BasePermissionType per = null;
		if(factoryProviders.containsKey(type)){
			ParticipationFactory partFact = partFactories.get(factoryProviders.get(type));
			
			return getRootPermission(partFact.getPermissionPrefix() + "View", partFact.getDefaultPermissionType(), organizationId);
		}
		/// Old method
		///
		switch(type){

			default:
				per = getViewObjectPermission(organizationId);
				break;
		}
		return per;
	}
	public static BasePermissionType getExecutePermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		BasePermissionType per = null;
		if(factoryProviders.containsKey(type)){
			ParticipationFactory partFact = partFactories.get(factoryProviders.get(type));
			per = getRootPermission(partFact.getPermissionPrefix() + "Execute", partFact.getDefaultPermissionType(), organizationId);
		}
		return per;
		
	}
	public static BasePermissionType getCreatePermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		BasePermissionType per = null;
		if(factoryProviders.containsKey(type)){
			ParticipationFactory partFact = partFactories.get(factoryProviders.get(type));
			return getRootPermission(partFact.getPermissionPrefix() + "Create", partFact.getDefaultPermissionType(), organizationId);
		}
		switch(type){

			default:
				per = getCreateObjectPermission(organizationId);
				break;
		}
		return per;
	}
	public static BasePermissionType getEditPermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		BasePermissionType per = null;
		if(factoryProviders.containsKey(type)){
			ParticipationFactory partFact = partFactories.get(factoryProviders.get(type));
			return getRootPermission(partFact.getPermissionPrefix() + "Edit", partFact.getDefaultPermissionType(), organizationId);
		}
		switch(type){

			default:
				per = getEditObjectPermission(organizationId);
				break;
		}
		return per;
	}

	public static BasePermissionType getRootPermission(String name, PermissionEnumType type, long organizationId) throws FactoryException, ArgumentException
	{

		return Factories.getPermissionFactory().getPermissionByName(name, type, organizationId);
	}
	
	public static BasePermissionType getEditObjectPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getRootPermission("ObjectEdit", PermissionEnumType.OBJECT, organizationId);
	}
	public static BasePermissionType getCreateObjectPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getRootPermission("ObjectCreate", PermissionEnumType.OBJECT,  organizationId);
	}
	public static BasePermissionType getViewObjectPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getRootPermission("ObjectView", PermissionEnumType.OBJECT, organizationId);
	}
	public static BasePermissionType getDeleteObjectPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getRootPermission("ObjectDelete", PermissionEnumType.OBJECT, organizationId);
	}
	
	public static BasePermissionType getEditApplicationPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getRootPermission("ApplicationEdit", PermissionEnumType.APPLICATION, organizationId);
	}
	public static BasePermissionType getCreateApplicationPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getRootPermission("ApplicationCreate", PermissionEnumType.APPLICATION, organizationId);
	}
	public static BasePermissionType getViewApplicationPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getRootPermission("ApplicationView", PermissionEnumType.APPLICATION, organizationId);
	}
	public static BasePermissionType getDeleteApplicationPermission(long organizationId) throws FactoryException, ArgumentException
	{
		return getRootPermission("ApplicationDelete", PermissionEnumType.APPLICATION, organizationId);
	}
	

	/*
	 * GENERAL REFACTOR
	 *    Pattern - canViewType
	 *    Rule
	 *       (actor == TypeRole)
	 *       AND
	 *       	(actorRole == TypeAdminRole)
	 *       	OR
	 *       	(actorRole == TypeReaderRole)
	 *       	OR
	 *       	(actorRole HAS ViewType permission ON TypeObject Container)
	 *       	OR
	 *      	(actorRole HAS ViewType permission ON TypeObject)
	 *    
	 */
	public static BasePermissionType getPermission(NameIdType actor, NameIdType object, String permissionBase) throws ArgumentException, FactoryException{
		if(object.getNameType() != NameEnumType.PERMISSION && !factoryProviders.containsKey(object.getNameType())){
			throw new ArgumentException(object.getNameType() + " is not from a registered authorization provider");
		}

		FactoryEnumType factType = (object.getNameType() != NameEnumType.PERMISSION ? factoryProviders.get(object.getNameType())  : FactoryEnumType.PERMISSION);
		return getPermission(factType, permissionBase,object.getOrganizationId());
	}
	public static BasePermissionType getPermission(FactoryEnumType factType, String permissionBase, long organizationId) throws ArgumentException, FactoryException{

		if(factType == FactoryEnumType.PERMISSION){
			//permissionName = 
			return null;
		}

		if(!partFactories.containsKey(factType)){
			throw new ArgumentException("Factory type " + factType.toString() + " does not have a registered participation factory");
		}
		ParticipationFactory pfact = partFactories.get(factType);
		String permissionName = pfact.getPermissionPrefix() + permissionBase;

		BasePermissionType permission = getRootPermission(permissionName, pfact.getDefaultPermissionType(), organizationId);
		if(permission == null){
			logger.warn(permissionName + " permission does not exist in organization #" + organizationId);
		}
		return permission;
		
	}
	protected static boolean canDo(NameIdType actor, NameIdType object, String permissionBase, boolean checkReadAuthorization) throws ArgumentException, FactoryException{
		
		BasePermissionType permission = getPermission(actor, object, permissionBase);
		
		return isAuthorized(actor, object, (permission == null ? new BasePermissionType[]{} : new BasePermissionType[]{permission}));

	}
	public static boolean canView(NameIdType actor, NameIdType object) throws ArgumentException, FactoryException{
		return canDo(actor, object, "View", true);
	}
	public static boolean canExecute(NameIdType actor, NameIdType object) throws ArgumentException, FactoryException{
		return canDo(actor, object, "Execute", true);
	}
	public static boolean canDelete(NameIdType actor, NameIdType object) throws ArgumentException, FactoryException{
		return canDo(actor, object, "Delete", true);
	}	
	public static boolean canCreate(NameIdType actor, NameIdType object) throws ArgumentException, FactoryException{
		return canDo(actor, object, "Create", true);
	}
	public static boolean canChange(NameIdType actor, NameIdType object) throws ArgumentException, FactoryException{
		return canDo(actor, object, "Edit", true);
	}
   
    /*
     * GENERAL REFACTOR
     * Pattern - canViewType (Combine with previous)
     * Rule
     *    (actorType == TypeUser)
     *    AND
     *       (actor == TypeObject Owner)
     *       OR
     *       (actor == TypeReaderRole member)
     *       OR
     *       (actor == TypeAdminRole member)
     *       OR
     *       (actor HAS ViewType permission ON TypeObject container)
     *       OR
     *       (actor HAS ViewType permission ON TypeObject)
     */
  
	public static boolean switchParticipant(UserType admin, NameIdType actor, NameIdType object, boolean enable) throws FactoryException, ArgumentException, DataAccessException
	{
		if (!canChange(admin, object)){
			
			return false;
		}
		if(!factoryProviders.containsKey(object.getNameType())
			||
			!partFactories.containsKey(factoryProviders.get(object.getNameType())))
		{
			logger.error("Object " + object.getNameType().toString() + " is not registered for authorization");
			return false;
		}
		ParticipationFactory pfact = partFactories.get(factoryProviders.get(object.getNameType()));
		ParticipantEnumType part_type = ParticipantEnumType.valueOf(actor.getNameType().toString());
		DataParticipantType bp = pfact.getParticipant(object, actor, part_type,null,null);
		boolean out_boolean = false;
		if (enable)
		{
			if (bp != null) return true;
			 pfact.newParticipant(object, actor,part_type, null, null);
			out_boolean = pfact.add(bp);
		}
		else
		{
			if (bp == null) out_boolean = true;
			else out_boolean = pfact.delete(bp);
		}
		return out_boolean;
	}
	
	
}
