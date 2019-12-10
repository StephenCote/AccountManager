/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.IParticipationFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.util.JSONUtil;

public class AuthorizationService {
	public static final Logger logger = LogManager.getLogger(AuthorizationService.class);

	private static Map<FactoryEnumType,FactoryEnumType> partFactories = new HashMap<>();
	private static Map<NameEnumType, FactoryEnumType> factoryProviders = new HashMap<>();
	private static final NameEnumType[] actors = new NameEnumType[]{NameEnumType.ACCOUNT, NameEnumType.PERSON, NameEnumType.USER, NameEnumType.ROLE}; 

	public static final String PERMISSION_CREATE = "Create";
	public static final String PERMISSION_DELETE = "Delete";
	public static final String PERMISSION_VIEW = "View";
	public static final String PERMISSION_EDIT = "Edit";
	public static final String PERMISSION_EXECUTE = "Execute";
	protected static final String[] PERMISSION_BASE = new String[]{PERMISSION_CREATE, PERMISSION_DELETE, PERMISSION_VIEW, PERMISSION_EDIT, PERMISSION_EXECUTE};

	public static String[] getDefaultPermissionBase(){
		return PERMISSION_BASE;
	}
	
	public static void clearProviders(){
		logger.warn("Clearing Authorization Providers");
		partFactories.clear();
		factoryProviders.clear();
	}
	
	public static IParticipationFactory getRegisteredProvider(FactoryEnumType fType){
		IParticipationFactory pfact = null;
		if(partFactories.containsKey(fType)){
			try {
				pfact = Factories.getFactory(partFactories.get(fType));
			} catch (FactoryException e) {
				logger.error(e);
			}
		}
		return pfact;
	}
	
	public static void registerAuthorizationProviders(FactoryEnumType factType,NameEnumType objectType, FactoryEnumType pfact){
		registerParticipationFactory(factType, pfact);
		factoryProviders.put(objectType, factType);
		for(int i = 0; i < actors.length; i++){
			EffectiveAuthorizationService.registerType(objectType, actors[i]);	
		}		
	}
	public static void registerParticipationFactory(FactoryEnumType factType,FactoryEnumType pfact){
		logger.debug("Register participation factory: " + factType.toString());
		partFactories.put(factType, pfact);

	}
	public static Map<FactoryEnumType, FactoryEnumType> getAuthorizationFactories(){
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
	///    Because ownership is asserted by straight internal id mapping, it's important to not authorize any complex objects not directly materialized from the persistence layer 
	/// 2) System privilege
	/// 3) If object is scoped to a directory:
	///	   a) Group ownership
	///    b) Entitled access to group
	/// 4) Entitled access to object
	/// While DENY_PERMISSION is supported at a persistence level, it is not yet factored into the database queries or authorization logic
	/// Therefore, don't grant system admin or group edit privileges to members for who entitlement checks will be exercised in that same scope
	/// because it will always return true
	///
	public static boolean isAuthorized(NameIdType actor, NameIdType object, String permissionBase, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		return isAuthorized(actor, object, permissionBase, permissions, false);
	}
	public static boolean isAuthorized(NameIdType actor, NameIdType object, String permissionBase, BasePermissionType[] permissions, boolean noParent) throws ArgumentException, FactoryException
	{
		if(object == null || actor == null || permissions== null){
			logger.error("Null reference");
			return false;
		}
		String authStr = EffectiveAuthorizationService.getEntitlementCheckString(object, actor, permissions);
		
		if(isAuthorizedByInternalDefaultPolicy(actor, object, permissionBase, permissions, noParent)){
			logger.debug("Is Authorized By Internal Policy: " + authStr);
			return true;
		}

		FactoryEnumType factType = FactoryEnumType.fromValue(object.getNameType().toString());
        if (
        	canBeAuthorized(factType)
        	&&
        	EffectiveAuthorizationService.getEntitlementsGrantAccess(object,actor, permissions)
        ){
        	logger.debug("Is Authorized By Entitlement: " + authStr);

        	return true;
        }
        else{
        	logger.debug("Is Not Authorized By Entitlement: " + authStr);
        }
	       
        return false;

	}
	
	/*
	 * TODO: Inheritence from GROUP to OBJECT is currently broken due to the hard coded 'edit' permission bit on the entitlement check
	 * isAuthorizedByTypeRule includes specialized business rules for certain cases, including:
	 * 1) Rule for taking group level authZ over object authZ for DirectoryGroupType objects,
	 * 2) Rule for permitting read access to 
	 */
	
	private static boolean isAuthorizedByInternalDefaultPolicy(NameIdType actor, NameIdType object, String permissionBase, BasePermissionType[] permissions, boolean noParent) throws ArgumentException, FactoryException{
		boolean outBool = false;
		boolean isCreate = isCreateAuthorization(object.getNameType(), permissions,object.getOrganizationId());
		boolean isView = isViewAuthorization(object.getNameType(), permissions,object.getOrganizationId());
		String authStr = EffectiveAuthorizationService.getEntitlementCheckString(object, actor, permissions);
		
		if(actor == null || object == null || permissions == null){
			logger.error("Actor, object, or permission array is null");
			return outBool;
		}
		NameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(object.getNameType().toString()));
		if (isMapOwner(actor, object))
		{
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
		
		/// Sytem and Legacy Type Authorization
		if(object.getNameType() == NameEnumType.USER){
			if(
				(
					permissionBase.equals(PERMISSION_VIEW)
					&& 
					RoleService.isFactoryReader(actor, Factories.getFactory(FactoryEnumType.ACCOUNT),object.getOrganizationId())
				)
				||
				RoleService.isFactoryAdministrator(actor, Factories.getFactory(FactoryEnumType.ACCOUNT),object.getOrganizationId())
			){
				logger.warn("Authorized " + actor.getUrn() + " for " + object.getUrn() + " with legacy system entitlement.");
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
        /// NOTE: this currently favors the parent permission oven the granular permission
        /// 2015/06/22 - temporarily included direct ownership of the parent directory
		///
		if(!noParent && (iFact.isClusterByGroup() || object.getNameType() == NameEnumType.DATA || object.getNameType() == NameEnumType.GROUP)){

			long groupId = 0L;
			if(object.getNameType() == NameEnumType.DATA) groupId = ((DataType)object).getGroupId();
			else if(object.getNameType() == NameEnumType.GROUP) groupId = ((BaseGroupType)object).getParentId();
			else if (iFact.isClusterByGroup()) groupId = ((NameIdDirectoryGroupType)object).getGroupId();
			BaseGroupType group = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupById(groupId,object.getOrganizationId());
			if(group == null){
				logger.error(String.format(FactoryException.OBJECT_NULL_TYPE, object.getNameType().toString() + " from #" + groupId + " in organization #" + object.getOrganizationId()));
				logger.error(JSONUtil.exportObject(object));

				return outBool;
			}
			BasePermissionType perm = getPermission(actor, group, permissionBase);

			if(perm != null && EffectiveAuthorizationService.getEntitlementsGrantAccess(group,actor, new BasePermissionType[] { perm })){
				logger.info("Authorized " + actor.getUrn() + " for " + object.getUrn() + " with group edit privileges");
				outBool = true;				
			}
			else{
				logger.debug("Trying to authorize " + actor.getUrn() + " for " + object.getName() + " with group privileges");
				return isAuthorized(actor, group, permissionBase, new BasePermissionType[]{perm}, true);
			}
		}
		else if(!noParent && (permissionBase.equals(PERMISSION_CREATE) || isCreate) && (object.getNameType() == NameEnumType.PERMISSION || object.getNameType() == NameEnumType.ROLE)){
			logger.info("AUTHORIZATION: CHECK CREATE APPROVAL FOR PARENT OBJECT");
			NameIdFactory fact = Factories.getFactory(FactoryEnumType.valueOf(object.getNameType().toString()));
			NameIdType parent = fact.getById(object.getParentId(), object.getOrganizationId());
			if(parent != null){
				BasePermissionType perm = getPermission(actor, parent, permissionBase);
				outBool = isAuthorized(actor,parent,permissionBase, (perm != null ? new BasePermissionType[]{perm} : new BasePermissionType[]{}), true);
			}
		}
		/// GRANT_ALL to PERMISSION for USER if USER has PermissionReader Role
		/// TODO: Change this to permission admin
		///
		else if(permissionBase.equals(PERMISSION_VIEW) && object.getNameType() == NameEnumType.PERMISSION && actor.getNameType() == NameEnumType.USER){
			outBool = RoleService.getIsUserInEffectiveRole(RoleService.getPermissionReaderUserRole(actor.getOrganizationId()),(UserType)actor);
		}

		return outBool;
	}
	private static boolean isViewAuthorization(NameEnumType objectType, BasePermissionType[] permissions,long organizationId) throws FactoryException, ArgumentException{
		BasePermissionType checkPer = null;
		if(factoryProviders.containsKey(objectType)){
			checkPer = getPermissionForMapType(objectType, organizationId, PERMISSION_VIEW);
		}
		if(checkPer == null){
			logger.debug("Object type " + objectType + " does not define a view permission");
			/*
			StackTraceElement[] ste = Thread.currentThread().getStackTrace();
			if(ste.length > 0) logger.error(ste[0]);
			*/
			return false;
		}
		return containsPermission(permissions, checkPer);
	}
	private static boolean isCreateAuthorization(NameEnumType objectType, BasePermissionType[] permissions,long organizationId) throws FactoryException, ArgumentException{
		boolean outBool = false;
		BasePermissionType checkPer = null;
		if(factoryProviders.containsKey(objectType)){
			checkPer = getPermissionForMapType(objectType, organizationId, PERMISSION_CREATE);
		}
		if(checkPer != null){
			return containsPermission(permissions, checkPer);
		}
		logger.debug("Legacy isCreateAuthZ Check");

		if(
			containsPermission(permissions,getCreateApplicationPermission(organizationId))
			||
			containsPermission(permissions,getCreateObjectPermission(organizationId))
		){
			outBool = true;
		}

		return outBool;
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
		boolean outBool = false;
		FactoryEnumType factType = FactoryEnumType.fromValue(object.getNameType().toString());
		if(canBeAuthorized(factType) == false){
			logger.error("Factory " + factType.toString() + " does not support discrete authorization");
			return false;
		}
		IParticipationFactory partFactory = getRegisteredProvider(factType);
		if(partFactory == null){
			logger.error(String.format(FactoryException.PARTICIPATION_FACTORY_REGISTRATION_EXCEPTION, factType.toString()));
			return false;
		}
		if(!isAuthorized(admin,object,PERMISSION_EDIT, new BasePermissionType[]{getEditPermissionForMapType(admin.getNameType(), object.getOrganizationId())})){
			logger.warn("User " + admin.getName() + " (#" + admin.getId() + ")" + " is not authorized to change object " + object.getName() + " (#" + object.getId() + ")");
			return false;
		}
		outBool = partFactory.deleteParticipantsWithAffect(new long[]{object.getId()}, object.getOrganizationId());
		if(outBool){
			EffectiveAuthorizationService.clearCache(object);
			EffectiveAuthorizationService.pendUpdate(object);
		}
		return outBool;
	}

	public static boolean authorize(UserType admin, NameIdType actor, NameIdType object, BasePermissionType permission, boolean enable) throws FactoryException, DataAccessException, ArgumentException
	{
		FactoryEnumType factType = FactoryEnumType.fromValue(object.getNameType().toString());
		if(!canBeAuthorized(factType)){
			logger.error("Factory " + factType.toString() + " does not support discrete authorization");
			return false;
		}
		IParticipationFactory partFactory = getRegisteredProvider(factType);
		if(partFactory == null){
			logger.error(String.format(FactoryException.PARTICIPATION_FACTORY_REGISTRATION_EXCEPTION, factType.toString()));
			return false;
		}
		if(!isAuthorized(admin,object, PERMISSION_EDIT, new BasePermissionType[]{getEditPermissionForMapType(admin.getNameType(), object.getOrganizationId())})){
			logger.warn("User " + admin.getName() + " (#" + admin.getId() + ")" + " is not authorized to change object " + object.getName() + " (#" + object.getId() + ")");
			return false;
		}
		logger.debug((enable ? "A" : "Dea") + "uthorizing " + partFactory.getFactoryType().toString() + " " + EffectiveAuthorizationService.getEntitlementCheckString(object, actor, new BasePermissionType[]{permission}));
		ParticipantEnumType part_type = ParticipantEnumType.valueOf(actor.getNameType().toString());
		BaseParticipantType bp = partFactory.getParticipant(object, actor, part_type, permission, AffectEnumType.GRANT_PERMISSION);
		
		boolean outBoolean = false;
		if (enable)
		{
			if (bp != null) return true;
			bp = partFactory.newParticipant(object, actor, part_type, permission, AffectEnumType.GRANT_PERMISSION);
			outBoolean = partFactory.add(bp);
		}
		else
		{
			if (bp == null) outBoolean = true;
			else outBoolean = partFactory.delete(bp);
		}
		if(outBoolean){
			/// Flag the object and actor for cache updates
			/// These updates won't be processed until the rebuildPending method is called
			///
			/// TODO: clearing the actor won't clear it off the object authZ cache - clearing the object cache nukes all authZ checks
			/// obviously, this isn't optimal for scale, so the cache cleanup should be revised
			///
			EffectiveAuthorizationService.clearCache(actor);
			EffectiveAuthorizationService.clearCache(object);
			EffectiveAuthorizationService.pendUpdate(object);
			EffectiveAuthorizationService.pendUpdate(actor);
		}
		return outBoolean;
	}

	
	
	public static <T> boolean authorizeType(UserType adminUser, NameIdType actor, NameIdType object, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		boolean outBool = false;
		
		
		if(!factoryProviders.containsKey(object.getNameType()) || !partFactories.containsKey(factoryProviders.get(object.getNameType()))){
			logger.error("Object type " + object.getNameType() + " is not registered for authorization");
			return outBool;
		}
		
		if(!EffectiveAuthorizationService.hasAuthorizationMap(object.getNameType(), actor.getNameType())) {
			logger.error("Actor type " + actor.getNameType() + " is not registered for authorization");
			return outBool;
		}

		BasePermissionType viewPermission = getPermission(actor,(NameIdType)object,PERMISSION_VIEW);
		BasePermissionType editPermission = getPermission(actor,(NameIdType)object,PERMISSION_EDIT);
		BasePermissionType delPermission = getPermission(actor,(NameIdType)object,PERMISSION_DELETE);
		BasePermissionType createPermission = getPermission(actor,(NameIdType)object,PERMISSION_CREATE);
		if(viewPermission == null || editPermission == null || delPermission == null || createPermission == null){
			logger.error("One or more expected permissions were null");
			/// : " + viewPermission + " / " + editPermission + " / " + delPermission + " / " + createPermission);
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
			outBool = true;
		}
		
		return outBool;
	}

	
	public static boolean isMapOwner(NameIdType test_owner, NameIdType map)
	{
		if(test_owner == null){
			logger.error("Owner is null");
			return false;
		}
		if(map == null){
			logger.error("Map is null");
			return false;
		}
		if(test_owner.getNameType() != NameEnumType.USER){
			logger.debug("Invalid test owner object with type " + test_owner.getNameType().toString());
			return false;
		}
		/// Exception to allow for a user object to 'own' itself because it most likely is owned by the admin who created it
		///
		if(map.getNameType() == NameEnumType.USER && test_owner.getId().compareTo(map.getId())==0){
			logger.debug("Exception to indicate a user owns its own user object for purposes of authorization");
			return true;
		}
		logger.debug("Compare Map Owner: " + test_owner.getId() + " :: " + map.getOwnerId());
		return (test_owner.getId().compareTo(map.getOwnerId())==0);
	}
	
	private static BasePermissionType getPermissionForMapType(NameEnumType type, long organizationId, String permissionName) throws FactoryException, ArgumentException{
		IParticipationFactory partFact = getRegisteredProvider(factoryProviders.get(type));
		if(partFact == null){
			logger.error(String.format(FactoryException.PARTICIPATION_FACTORY_REGISTRATION_EXCEPTION, type.toString()));
			return null;
		}
		return getRootPermission(partFact.getPermissionPrefix() + permissionName, partFact.getDefaultPermissionType(), organizationId);
	}
	public static BasePermissionType getDeletePermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		if(factoryProviders.containsKey(type)){
			return getPermissionForMapType(type, organizationId, PERMISSION_DELETE);
		}
		return getDeleteObjectPermission(organizationId);

	}
	public static BasePermissionType getViewPermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		if(factoryProviders.containsKey(type)){
			return getPermissionForMapType(type, organizationId, PERMISSION_VIEW);
		}

		logger.warn("OLD Permission Map for " + type.toString());
		return getViewObjectPermission(organizationId);
	}
	public static BasePermissionType getExecutePermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		if(factoryProviders.containsKey(type)){
			return getPermissionForMapType(type, organizationId, PERMISSION_EXECUTE);
			}
		return null;
	}
	public static BasePermissionType getCreatePermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		if(factoryProviders.containsKey(type)){
			return getPermissionForMapType(type, organizationId, PERMISSION_CREATE);
		}
		return getCreateObjectPermission(organizationId);

	}
	public static BasePermissionType getEditPermissionForMapType(NameEnumType type, long organizationId) throws FactoryException, ArgumentException{
		if(factoryProviders.containsKey(type)){
			return getPermissionForMapType(type, organizationId, PERMISSION_EDIT);
		}
		return getEditObjectPermission(organizationId);
	}

	public static BasePermissionType getRootPermission(String name, PermissionEnumType type, long organizationId) throws FactoryException, ArgumentException
	{
		return ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionByName(name, type, organizationId);
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
	

	public static BasePermissionType getPermission(NameIdType actor, NameIdType object, String permissionBase) throws ArgumentException, FactoryException{
		if(actor == null || object == null || permissionBase == null){
			logger.warn(FactoryException.ARGUMENT_NULL);
			return null;
		}
		/// object.getNameType() != NameEnumType.PERMISSION  && 
		if(!factoryProviders.containsKey(object.getNameType())){
			logger.debug(object.getNameType() + " is not from a registered authorization provider");
			return null;
		}

		/// FactoryEnumType factType = (object.getNameType() != NameEnumType.PERMISSION ? factoryProviders.get(object.getNameType())  : FactoryEnumType.PERMISSION);
		FactoryEnumType factType = factoryProviders.get(object.getNameType());
		return getPermission(factType, permissionBase,object.getOrganizationId());
	}
	
	public static BasePermissionType getPermission(FactoryEnumType factType, String permissionBase, long organizationId) throws ArgumentException, FactoryException{

		if(!partFactories.containsKey(factType)){
			throw new ArgumentException(String.format(FactoryException.PARTICIPATION_FACTORY_REGISTRATION_EXCEPTION,factType.toString()));
		}
		IParticipationFactory pfact = getRegisteredProvider(factType);
		if(pfact == null){
			return null;
		}
		String permissionName = pfact.getPermissionPrefix() + permissionBase;

		BasePermissionType permission = getRootPermission(permissionName, pfact.getDefaultPermissionType(), organizationId);
		if(permission == null){
			logger.warn(permissionName + " permission does not exist in organization #" + organizationId);
		}
		return permission;
		
	}
	protected static boolean canDo(NameIdType actor, NameIdType object, String permissionBase, boolean checkReadAuthorization) throws ArgumentException, FactoryException{
		
		BasePermissionType permission = getPermission(actor, object, permissionBase);
		
		return isAuthorized(actor, object, permissionBase, (permission == null ? new BasePermissionType[]{} : new BasePermissionType[]{permission}));

	}
	public static boolean canView(NameIdType actor, NameIdType object) throws ArgumentException, FactoryException{
		return canDo(actor, object, PERMISSION_VIEW, true);
	}
	public static boolean canExecute(NameIdType actor, NameIdType object) throws ArgumentException, FactoryException{
		return canDo(actor, object, PERMISSION_EXECUTE, true);
	}
	public static boolean canDelete(NameIdType actor, NameIdType object) throws ArgumentException, FactoryException{
		return canDo(actor, object, PERMISSION_DELETE, true);
	}	
	public static boolean canCreate(NameIdType actor, NameIdType object) throws ArgumentException, FactoryException{
		return canDo(actor, object, PERMISSION_CREATE, true);
	}
	public static boolean canChange(NameIdType actor, NameIdType object) throws ArgumentException, FactoryException{
		return canDo(actor, object, PERMISSION_EDIT, true);
	}
   
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
		IParticipationFactory pfact = getRegisteredProvider(factoryProviders.get(object.getNameType()));
		if(pfact == null){
			return false;
		}
		ParticipantEnumType part_type = ParticipantEnumType.valueOf(actor.getNameType().toString());
		DataParticipantType bp = pfact.getParticipant(object, actor, part_type,null,null);
		boolean outBoolean = false;
		if (enable)
		{
			if (bp != null) return true;
			 pfact.newParticipant(object, actor,part_type, null, null);
			outBoolean = pfact.add(bp);
		}
		else
		{
			if (bp == null) outBoolean = true;
			else outBoolean = pfact.delete(bp);
		}
		return outBoolean;
	}
	
	
}
