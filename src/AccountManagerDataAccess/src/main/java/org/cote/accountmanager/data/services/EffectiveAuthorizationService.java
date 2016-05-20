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
/// 2014/09/15 - in progress - creating generic cache mechanism
/// The newer style is slightly more generic, though it probably would work better if the factories register their own types
/// The code is currently commented out while it's being worked on
/// These changes will greatly extend data-level authorization coverage to any configured participation table without having to add a bunch of boilerplate in this class
///

package org.cote.accountmanager.data.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonGroupType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;



/*
 
 Updates
 	2015/10/15 - Added database functions to unwind group and role hierarchies when evaluating entitlements.  The Effective Authorization service will still be used for computing effective roles, but the entitlement checks will be migrated from the *Rights views to use the
 	*_member_entitlement functions, because (a) it will make it more generic, and (b) will get used by the more generic authorize/unauthorize methods.
 
    2014/08/04 - Extended authorization query to view rights inherited from linked user and account entities.  Does not include siblings or dependency persons. This allows for asserting whether a person has an entitlement because their account or user has that entitlement.
    Example Query:
    	SELECT distinct referenceid FROM grouprights WHERE referenceid = 8 AND groupid = 5307 AND affecttype = 'GRANT_PERMISSION' AND affectid IN (343) and organizationid = 4 and referencetype = 'PERSON'
		UNION ALL
		SELECT distinct participantid FROM grouprights GR2
		inner join personparticipation PU on GR2.referencetype = PU.participanttype AND PU.participantid = GR2.referenceid 
		WHERE PU.participationid = 8 AND GR2.groupid = 5307 AND GR2.affecttype = 'GRANT_PERMISSION' AND GR2.affectid IN (343) and GR2.organizationid = 4 and (GR2.referencetype = 'ACCOUNT' or GR2.referencetype = 'USER')

 	This hasn't been incorporated in the role check code yet, only the entitlement level.  While this denormalized views breaks any abstraction, it does allow for simpler authorization checks that can target person types for functional roles.
 	
 VIEW NOTES
 * There may be more views than described here
 * 
 The Effective Authorization Service leverages the indirect maps and caches created or stored in the following database tables, views, and functions:
 view groupUserRights - direct group<-user permission assignments
 view dataUserRights - direct data<-user permission assignments
 function roles_from_leaf(id,orgId) - computes a role hierarchy up from a given node.  Permissions accumulate up (per RBAC spec)
 ALT: function roles_to_leaf(id,organizationId) - computes a role hierarchy down from a given node. Permissions accumulate down (inverse spec)
 view effectiveUserRoles - computed permission<->role distribution, following the role hierarchy and mapping to users or groups of users
 function cache_user_roles(id,orgId) - caches the result of effectiveUserRoles for a specified user into the userrolecache table
 view effectiveGroupRoleRights - maps userrolecache to groupparticipation to yield all effective permissions by group id by user
 view effectiveDataRoleRights - maps userrolecache to dataparticipation to yield all effective permissions by data id by user
 view groupRights - Union of effectiveGroupRoleRights and groupUserRights.  Can be pretty efficient when used with a userid and organization id
 view dataRights - Union of effectiveDataRoleRights and dataUserRights.  Can be pretty efficient when used with a userid and organization id
 
 THESE HAVE BEEN DEPRECATED
 DEP: view groupRoleTrace - maps permissions<->roles<->users - not as efficient
 DEP: view groupRoleGroupRights
 DEP: view groupRoleRights
 DEP: view dataRoleTrace - maps permissions<->roles<->users - not as efficient
 DEP: view dataRoleGroupRights
 DEP: view dataRoleRights
 DEP: cache_roles - maps all group and data rights by role id into the data and group cache tables.  Not as efficient
  
  
  
 When making changes to authorization, such as adding/removing role participants, groups, or data permissions, even if done through AuthorizationUtil, it is necessary to:
 1) Rebuild the corresponding cache if making indirect changes, such as group or roles,
 2) And/or, clear the memory cache for the specified object(s) if making direct permission changes.  While the underlying factory participations will be reset, the authorization service creates denormalized permission assertions, and won't clear any local cache until explicitly instructed to for bulk/perf reasons. 
    a) Note: In memory cache is per node.  Therefore, managing permissions from a distributed setup requires a cache reset across nodes once the permissions change 
  
 */
public class EffectiveAuthorizationService {
	

	
	public static final Logger logger = Logger.getLogger(EffectiveAuthorizationService.class.getName());

	/// 2014/09/14 - in progress - creating generic cache mechanism
	
	/// rebuildType == id :: object relationship by type
	///
	//private static Map<NameEnumType,Map<Long,NameIdType>> rebuildType = new HashMap<NameEnumType,Map<Long,NameIdType>>();

	/// typeTypeMap == Type to type1 id to map of type2 id to permit/denied bit
	///
	//private static Map<NameEnumType,Map<NameEnumType,Map<Long,Map<Long,Map<Long,Boolean>>>>> typeTypeMap = new HashMap<NameEnumType,Map<NameEnumType,Map<Long,Map<Long,Map<Long,Boolean>>>>>();
	
	/// This big ugly construct is used to hash the previous cache hashes against the object types for quick look-up and to avoid code duplication
	///
	private static Map<NameEnumType,Map<NameEnumType,Map<Long,Map<Long,Map<Long,Boolean>>>>> actorMap = new HashMap<NameEnumType,Map<NameEnumType,Map<Long,Map<Long,Map<Long,Boolean>>>>>();
	
	
	private static Map<Long,UserType> rebuildUsers = new HashMap<Long,UserType>();
	private static Map<Long,AccountType> rebuildAccounts = new HashMap<Long,AccountType>();
	private static Map<Long,PersonType> rebuildPersons = new HashMap<Long,PersonType>();

	public static int maximum_insert_size = 250;
	
	/// TODO - these individual caches coud be consolidated - though then it would be more difficult to unwind, and not sure about the perf savings
	/// 2016/05/10 - Refactor this to use a TypeToType structure
	/// Let the factories base register these types in
	/// eg: TypeMapClass::instance{
	///   xActor[NameIdType]
	///   yObject[NameIdType]
	///   Map<> map = ...
	/// eg: RebuildMapType::instance
	///   xObject[NameIdType]
	///   Long[] ids
	/// userRoleMap - caches users who participate in a role
	private static Map<Long,Map<Long,Boolean>> userRoleMap = new HashMap<Long,Map<Long,Boolean>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> userGroupMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> userDataMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	
	private static Map<Long,Map<Long,Boolean>> accountRoleMap = new HashMap<Long,Map<Long,Boolean>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> accountGroupMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> accountDataMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();

	private static Map<Long,Map<Long,Boolean>> personRoleMap = new HashMap<Long,Map<Long,Boolean>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> personGroupMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> personDataMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();

	private static Map<Long,Map<Long,Map<Long,Boolean>>> roleRoleMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> roleGroupMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> roleDataMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();

	/// userRolePerMap - caches users with explicit rights to alter roles
	///
	private static Map<Long,Map<Long,Map<Long,Boolean>>> userRolePerMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> accountRolePerMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> personRolePerMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	private static Map<Long,BaseGroupType> rebuildGroups = new HashMap<Long,BaseGroupType>();
	private static Map<Long,BaseRoleType> rebuildRoles = new HashMap<Long,BaseRoleType>();
	private static Map<Long,DataType> rebuildData = new HashMap<Long,DataType>();
	
	/// 2016/05/11 - Refactor for generalized type - there's an implication here that the corresponding database functions and views will exist
	/// DB Requirements
	///   - cache_all_{objectType}_roles functions (there are 2)
	///   - {objectType}rolecache table
	///   - effective{objectType}Roles view
	///   - effective{objectType}{actorType}RoleRights
	///   - {objectType}rights
	///   - {objectType}{actorType}Rights
	///   - {
	///
	private static Map<NameEnumType,Map<NameEnumType,AuthorizationMapType>> objectMap = new HashMap<>();
	private static Map<NameEnumType,RebuildMap> rebuildMap = new HashMap<>();
	
	private static Object lockObject = new Object();
	
	public static boolean registerType(NameEnumType objectType, NameEnumType actorType){
		
		if(objectMap.containsKey(objectType) == false){
			objectMap.put(objectType, new HashMap<>());
		}
		if(objectMap.get(objectType).containsKey(actorType) == true){
			logger.error("Actor " + actorType + " already registered for " + objectType);
			return false;
		}
		
		if(rebuildMap.containsKey(objectType) == false){
			RebuildMap rmap = new RebuildMap(objectType);
			rebuildMap.put(objectType, rmap);
		}
		logger.debug("Registering " + objectType.toString() + " <-> " + actorType.toString());
		AuthorizationMapType authZ = new AuthorizationMapType(objectType, actorType);
		objectMap.get(objectType).put(actorType, authZ);
		return true;
	}
	
	/*
	private static Map<NameEnumType,Map<Long,Map<Long,Map<Long,Boolean>>>> getActorMap(NameEnumType type){
		return actorMap.get(type);
	}
	private static void addActorMap(NameEnumType actor, NameEnumType object){
		if(actorMap.containsKey(actor) == false) actorMap.put(actor, new HashMap<NameEnumType,Map<Long,Map<Long,Map<Long,Boolean>>>>());
		if(typeTypeMap.containsKey(actor) == false) typeTypeMap.put(actor, new HashMap<NameEnumType,Map<Long,Map<Long,Map<Long,Boolean>>>>());
		if(typeTypeMap.get(actor).containsKey(object) == false) typeTypeMap.get(actor).put(object, new HashMap<Long,Map<Long,Map<Long,Boolean>>>());
		if(actorMap.get(actor).containsKey(object) == false) actorMap.get(actor).put(object, typeTypeMap.get(actor).get(object));
		if(rebuildType.containsKey(actor) == false) rebuildType.put(actor, new HashMap<Long,NameIdType>());
		if(rebuildType.containsKey(object) == false) rebuildType.put(object, new HashMap<Long,NameIdType>());
	}
	*/
	static{
		actorMap.put(NameEnumType.PERSON, new HashMap<NameEnumType,Map<Long,Map<Long,Map<Long,Boolean>>>>());
		actorMap.put(NameEnumType.USER, new HashMap<NameEnumType,Map<Long,Map<Long,Map<Long,Boolean>>>>());
		actorMap.put(NameEnumType.ACCOUNT, new HashMap<NameEnumType,Map<Long,Map<Long,Map<Long,Boolean>>>>());
		actorMap.put(NameEnumType.ROLE, new HashMap<NameEnumType,Map<Long,Map<Long,Map<Long,Boolean>>>>());
		actorMap.get(NameEnumType.PERSON).put(NameEnumType.GROUP, personGroupMap);
		actorMap.get(NameEnumType.PERSON).put(NameEnumType.DATA, personDataMap);
		actorMap.get(NameEnumType.PERSON).put(NameEnumType.ROLE, personRolePerMap);
		actorMap.get(NameEnumType.ACCOUNT).put(NameEnumType.GROUP, accountGroupMap);
		actorMap.get(NameEnumType.ACCOUNT).put(NameEnumType.DATA, accountDataMap);
		actorMap.get(NameEnumType.ACCOUNT).put(NameEnumType.ROLE, accountRolePerMap);
		actorMap.get(NameEnumType.USER).put(NameEnumType.GROUP, userGroupMap);
		actorMap.get(NameEnumType.USER).put(NameEnumType.DATA, userDataMap);
		actorMap.get(NameEnumType.USER).put(NameEnumType.ROLE, userRolePerMap);
		actorMap.get(NameEnumType.ROLE).put(NameEnumType.GROUP, roleGroupMap);
		actorMap.get(NameEnumType.ROLE).put(NameEnumType.DATA, roleDataMap);
		actorMap.get(NameEnumType.ROLE).put(NameEnumType.ROLE, roleRoleMap);
		
	}
	
	
	public static void pendUpdate(NameIdType map){
		if(rebuildMap.containsKey(map.getNameType())){
			RebuildMap rMap = rebuildMap.get(map.getNameType());
			if(rMap.getMap().contains(map.getId()) == false){
				logger.debug("Pending update for " + map.getNameType().toString() + " #" + map.getId());
				rMap.getMap().add(map.getId());
			}
			return;
		}
		logger.debug("OLD PEND SYSTEM " + map.getNameType() + " #" + map.getId());
		switch(map.getNameType()){
			case ACCOUNT: pendAccountUpdate((AccountType)map); break;
			case USER: pendUserUpdate((UserType)map); break;
			case PERSON: pendPersonUpdate((PersonType)map); break;
			case ROLE: pendRoleUpdate((BaseRoleType)map); break;
			case GROUP: pendGroupUpdate((BaseGroupType)map); break;
			case DATA: pendDataUpdate((DataType)map); break;
			default:
				logger.error("Invalid NameIdType: " + map.getNameType());
				break;
		}

	}
	
	public static void pendRoleUpdate(BaseRoleType role){
		rebuildRoles.put(role.getId(), role);
		/// logger.debug("Pend role " + role.getName() + " #" + role.getId() + " for total count " + rebuildRoles.size());
	}

	public static void pendDataUpdate(DataType data){
		rebuildData.put(data.getId(), data);
		
	}

	
	public static void pendUserUpdate(UserType user){
		rebuildUsers.put(user.getId(), user);
	}
	public static void pendAccountUpdate(AccountType account){
		rebuildAccounts.put(account.getId(), account);
	}
	public static void pendPersonUpdate(PersonType person){
		rebuildPersons.put(person.getId(), person);
	}
	public static void pendGroupUpdate(BaseGroupType group){
		rebuildGroups.put(group.getId(), group);
		/// logger.debug("Pend group " + group.getName() + " #" + group.getId() + " for total count " + rebuildGroups.size());
	}

	
	public static void clearCache(){
		logger.debug("Clearing Authorization Cache");
		for(NameEnumType key : objectMap.keySet()){
			//for(NameEnumType subKey : objectMap.get(key).keySet()){
			for(AuthorizationMapType aMap : objectMap.get(key).values()){
				aMap.getMap().clear();
			}
			return;
		}
		logger.debug("OLD OLD OLD Clear Authorization Cache");
		accountRoleMap.clear();
		accountRolePerMap.clear();
		accountGroupMap.clear();
		accountDataMap.clear();
		personRoleMap.clear();
		personRolePerMap.clear();
		personGroupMap.clear();
		personDataMap.clear();
		userRoleMap.clear();
		userRolePerMap.clear();
		userGroupMap.clear();
		userDataMap.clear();
		roleRoleMap.clear();
		roleGroupMap.clear();
		roleDataMap.clear();
		rebuildAccounts.clear();
		rebuildPersons.clear();
		rebuildUsers.clear();
		rebuildGroups.clear();
		rebuildRoles.clear();
		rebuildData.clear();
	}
	public static void clearCache(NameIdType object) throws ArgumentException{
		logger.debug("Clear Authorization Cache for " + object.getNameType().toString() + " " + object.getName());
		
		if(objectMap.containsKey(object.getNameType())){
			for(AuthorizationMapType aMap : objectMap.get(object.getNameType()).values()){
				// logger.debug("Clearing " + object.getNameType() + "<->" + aMap.getActor().toString() + " cache for object " + object.getUrn());
				clearPerCache(aMap.getMap(),object);
				rebuildMap.get(object.getNameType()).getMap().remove(object.getId());
			}
			return;
		}
		logger.debug("OLD CACHE SYSTEM");
		switch(object.getNameType()){
			case GROUP:
				BaseGroupType group = (BaseGroupType)object;
				if(group.getGroupType() == GroupEnumType.DATA){
					clearPerCache(userGroupMap, group);
					clearPerCache(accountGroupMap, group);
					clearPerCache(personGroupMap, group);
					clearPerCache(roleGroupMap, group);
				}
				rebuildGroups.remove(group.getId());
				break;
			case DATA:
				DataType data = (DataType)object;
				clearPerCache(userDataMap, data);
				clearPerCache(accountDataMap,data);
				clearPerCache(personDataMap,data);
				clearPerCache(roleDataMap, data);
				rebuildData.remove(data.getId());
				break;
			case USER:
				UserType user = (UserType)object;
				userRoleMap.keySet().remove(user.getId());
				rebuildUsers.remove(user.getId());
				break;
			case ACCOUNT:
				AccountType account = (AccountType)object;
				accountRoleMap.keySet().remove(account.getId());
				rebuildAccounts.remove(account.getId());
				break;
			case PERSON:
				PersonType person = (PersonType)object;
				personRoleMap.keySet().remove(person.getId());
				rebuildPersons.remove(person.getId());
				break;
			case ROLE:
				BaseRoleType role = (BaseRoleType)object;
				rebuildRoles.remove(role.getId());
				clearCache(accountRoleMap, role);
				clearCache(personRoleMap, role);
				clearCache(userRoleMap, role);
				clearPerCache(personRolePerMap, role);
				clearPerCache(accountRolePerMap, role);
				clearPerCache(userRolePerMap, role);
				clearPerCache(roleRoleMap,role);
				break;
			default:
				throw new ArgumentException("Invalid name type " + object.getNameType() + " for object " + object.getName() + " (" + object.getId() + ")");
		}
	}

	private static void clearPerCache(Map<Long,Map<Long,Map<Long,Boolean>>> map,NameIdType obj){
		Iterator<Map<Long,Map<Long,Boolean>>> iter = map.values().iterator();
		while(iter.hasNext()){
			iter.next().keySet().remove(obj.getId());
		}		
	}
	private static void clearCache(Map<Long,Map<Long,Boolean>> map,NameIdType obj){
		Iterator<Map<Long,Boolean>> iter = map.values().iterator();
		while(iter.hasNext()){
			iter.next().keySet().remove(obj.getId());
		}		
	}

	protected static boolean getCacheValue(BaseRoleType actor, BaseRoleType role, BasePermissionType[] permissions){
		return getPerCacheValue(roleRoleMap, actor, role, permissions);
	}
	protected static boolean getCacheValue(BaseRoleType actor,BaseGroupType group, BasePermissionType[] permissions){
		return getPerCacheValue(roleGroupMap, actor, group, permissions);
	}
	protected static boolean getCacheValue(BaseRoleType actor,DataType data, BasePermissionType[] permissions){
		return getPerCacheValue(roleDataMap, actor, data, permissions);
	}
	protected static boolean getCacheValue(UserType user, BaseRoleType role, BasePermissionType[] permissions){
		return getPerCacheValue(userRolePerMap, user, role, permissions);
	}
	protected static boolean getCacheValue(UserType user, BaseGroupType group, BasePermissionType[] permissions){
		return getPerCacheValue(userGroupMap, user, group, permissions);
	}
	protected static boolean getCacheValue(UserType user, DataType data, BasePermissionType[] permissions){
		return getPerCacheValue(userDataMap, user, data, permissions);
	}
	protected static boolean getCacheValue(AccountType account, BaseRoleType role, BasePermissionType[] permissions){
		return getPerCacheValue(accountRolePerMap, account, role, permissions);
	}
	protected static boolean getCacheValue(AccountType account, BaseGroupType group, BasePermissionType[] permissions){
		return getPerCacheValue(accountGroupMap, account, group, permissions);
	}
	protected static boolean getCacheValue(AccountType account, DataType data, BasePermissionType[] permissions){
		return getPerCacheValue(accountDataMap, account, data, permissions);
	}
	protected static boolean getCacheValue(PersonType person, BaseRoleType role, BasePermissionType[] permissions){
		return getPerCacheValue(personRolePerMap, person, role, permissions);
	}
	protected static boolean getCacheValue(PersonType person, BaseGroupType group, BasePermissionType[] permissions){
		return getPerCacheValue(personGroupMap, person, group, permissions);
	}
	protected static boolean getCacheValue(PersonType person, DataType data, BasePermissionType[] permissions){
		return getPerCacheValue(personDataMap, person, data, permissions);
	}

	/*
	protected static boolean getCacheValue(UserType user, BaseRoleType role){
		return getCacheValue(userRoleMap, user, role);
	}

	protected static boolean getCacheValue(AccountType account, BaseRoleType role){
		return getCacheValue(accountRoleMap, account, role);
	}
	*/
	protected static boolean getCacheValue(NameIdType actor, BaseRoleType role){
		boolean out_bool = false;
		switch(actor.getNameType()){
			case USER:
				out_bool = getCacheValue(userRoleMap, (UserType)actor, role);
				break;
			case ACCOUNT:
				out_bool = getCacheValue(accountRoleMap, (AccountType)actor, role);
				break;
			case PERSON:
				out_bool = getCacheValue(personRoleMap, (PersonType)actor, role);
				break;
			default:
				logger.error("Unhandled actor type: " + actor.getNameType().toString());
				break;
		}
		return out_bool;
		
	}
	
	private static boolean getCacheValue(Map<Long,Map<Long,Boolean>> map, NameIdType actor, NameIdType obj){
		if(map.containsKey(actor.getId()) && map.get(actor.getId()).containsKey(obj.getId())){
			return map.get(actor.getId()).get(obj.getId());
		}
		return false;
	}

	
	/// TODO - need to refactor this - its checking role membership, but is too generic, causing bloat elsewhere
	///
	protected static boolean hasCache(NameIdType actor, BaseRoleType role){
		boolean out_bool = false;
		switch(actor.getNameType()){
			case USER:
				out_bool = hasCache(userRoleMap, (UserType)actor, role);
				break;
			case ACCOUNT:
				out_bool = hasCache(accountRoleMap, (AccountType)actor, role);
				break;
			case PERSON:
				out_bool = hasCache(personRoleMap, (PersonType)actor, role);
				break;
			default:
				logger.error("Unhandled actor type: " + actor.getNameType().toString());
				break;
		}
		return out_bool;
		
	}
	/*
	protected static boolean hasCache(UserType user, BaseRoleType role){
		return hasCache(userRoleMap, user, role);
	}
	protected static boolean hasCache(AccountType account, BaseRoleType role){
		return hasCache(accountRoleMap, account, role);
	}
	*/
	private static boolean hasCache(Map<Long,Map<Long,Boolean>> map, UserType user, NameIdType obj){
		if(map.containsKey(user.getId()) && map.get(user.getId()).containsKey(obj.getId())){
			return true;///map.get(user.getId()).get(role.getId());
		}
		return false;		
	}

	private static Map<Long,Map<Long,Map<Long,Boolean>>> getActorMap(NameEnumType actorType, NameEnumType objectType){
		//Map<Long,Map<Long,Map<Long,Boolean>>> map = null;
		if(objectMap.containsKey(objectType) && objectMap.get(objectType).containsKey(actorType)){
			return objectMap.get(objectType).get(actorType).getMap();
		}
		if(actorMap.containsKey(actorType) && actorMap.get(actorType).containsKey(objectType)){
			logger.info("OLD OLD getActorMap for " + actorType + " " + objectType);
			return actorMap.get(actorType).get(objectType);
		}
		return null;
	}
	
	protected static AuthorizationMapType getAuthorizationMap(NameEnumType objectType, NameEnumType actorType){
		if(objectMap.containsKey(objectType) && objectMap.get(objectType).containsKey(actorType)){
			return objectMap.get(objectType).get(actorType);
		}
		return null;
	}
	
	protected static boolean hasPerCache(NameIdType actor, NameIdType object, BasePermissionType[] permission){
		if(objectMap.containsKey(object.getNameType())){
			AuthorizationMapType map = getAuthorizationMap(object.getNameType(),actor.getNameType());
			if(map == null){
				logger.error("Object map " + object.getNameType() + " doesn't include " + actor.getNameType() + " registered for authorization");
				return false;
			}
			return hasPerCache(map.getMap(),actor,object,permission);
		}
		logger.debug("OLD OLD PER CACHE");
		return hasPerCache(getActorMap(actor.getNameType(),object.getNameType()), actor, object, permission);
	}

	private static boolean getPerCacheValue(Map<Long,Map<Long,Map<Long,Boolean>>> map, NameIdType actor, NameIdType obj, BasePermissionType[] permissions){
		boolean out_bool = false;
		if(map.containsKey(actor.getId()) && map.get(actor.getId()).containsKey(obj.getId())){
			Map<Long,Boolean> pmap = map.get(actor.getId()).get(obj.getId());
			for(int p = 0; p < permissions.length;p++){
				if(pmap.containsKey(permissions[p].getId())){
					out_bool = pmap.get(permissions[p].getId());
					//logger.debug("*** Per Cache Value == " + out_bool);
					/// only break on a positive
					/// this is setup to work as an OR case when fed a list of permissions
					///
					if(out_bool) break;
				}
			}
		}
		return out_bool;		
	}
	private static boolean hasPerCache(Map<Long,Map<Long,Map<Long,Boolean>>> map, NameIdType actor, NameIdType obj, BasePermissionType[] permissions){
		boolean out_bool = false;
		if(map == null){
			logger.error("Null map");
		}
		if(actor == null || obj == null){
			logger.error("Null actor or target object");
		}
		if(map.containsKey(actor.getId()) && map.get(actor.getId()).containsKey(obj.getId())){
			Map<Long,Boolean> pmap = map.get(actor.getId()).get(obj.getId());
			for(int p = 0; p < permissions.length;p++){
				if(pmap.containsKey(permissions[p].getId())){
					//logger.debug("*** hasPerCache " + actor.getId() + " " + obj.getId() + " " + pmap.get(permissions[p].getId()));
					out_bool = true;
					//out_bool = pmap.get(permissions[p].getId());
					break;
				}
			}
		}
		return out_bool;		
	}
	
	private static boolean hasCache(Map<Long,Map<Long,Boolean>> map, NameIdType actor, NameIdType obj){
		if(map.containsKey(actor.getId()) && map.get(actor.getId()).containsKey(obj.getId())){
			return true;///map.get(user.getId()).get(role.getId());
		}
		return false;		
	}
	protected static void addToCache(NameIdType actor, BaseRoleType role, boolean val){
		switch(actor.getNameType()){
			case USER:
				addToCache(userRoleMap, (UserType)actor, role, val);
				break;
			case ACCOUNT:
				addToCache(accountRoleMap, (AccountType)actor, role, val);
				break;
			case PERSON:
				addToCache(personRoleMap, (PersonType)actor, role, val);
				break;

			default:
				logger.error("Unhandled actor type: " + actor.getNameType().toString());
				break;
		}
		
	}

	protected static void addToPerCache(NameIdType actor, NameIdType object, BasePermissionType[] permissions, boolean val){
		if(objectMap.containsKey(object.getNameType())){
			AuthorizationMapType map = getAuthorizationMap(object.getNameType(),actor.getNameType());
			if(map == null){
				logger.error("Object map " + object.getNameType() + " doesn't include " + actor.getNameType() + " registered for authorization");
				return;
			}
			addToPerCache(map.getMap(),actor,object,permissions,val);
			return;
		}
		logger.debug("OLD OLD PER CACHE");
		addToPerCache(getActorMap(actor.getNameType(),object.getNameType()),actor,object,permissions,val);
	}

	private static void addToCache(Map<Long,Map<Long,Boolean>> map, NameIdType actor, NameIdType obj, boolean val){
		if(map.containsKey(actor.getId()) == false) map.put(actor.getId(), new HashMap<Long,Boolean>());
		map.get(actor.getId()).put(obj.getId(),val);
		
	}
	private static void addToPerCache(Map<Long,Map<Long,Map<Long,Boolean>>> map, NameIdType actor, NameIdType obj, BasePermissionType[] permissions, boolean val){
		if(map.containsKey(actor.getId()) == false) map.put(actor.getId(), new HashMap<Long,Map<Long,Boolean>>());
		if(map.get(actor.getId()).containsKey(obj.getId())==false) map.get(actor.getId()).put(obj.getId(), new HashMap<Long,Boolean>());
		for(int p = 0;p < permissions.length;p++){
			Map<Long,Boolean> pmap = map.get(actor.getId()).get(obj.getId());
			pmap.put(permissions[p].getId(),val);
		}
		
	}
	
	
	public static boolean isMemberCacheable(NameIdType object){
		return (object.getNameType() == NameEnumType.ROLE || object.getNameType() == NameEnumType.PERSON || object.getNameType() == NameEnumType.ACCOUNT || object.getNameType() == NameEnumType.USER);
	}
	public static boolean isObjectCacheable(NameIdType object){
		return (object.getNameType() == NameEnumType.GROUP || object.getNameType() == NameEnumType.DATA || object.getNameType() == NameEnumType.ROLE);
	}
	public static String getEntitlementCheckString(NameIdType object, NameIdType member, BasePermissionType[] permissions){
		StringBuffer buff = new StringBuffer();
		buff.append(member.getNameType().toString() + " " + member.getName() + " (");
		for(int i = 0; i < permissions.length;i++){
			if(i > 0) buff.append(", ");
			buff.append(permissions[i].getName());
		}
		buff.append(") " + object.getNameType().toString() + " " + object.getName());
		return buff.toString();
	}
	public static boolean getEntitlementsGrantAccess(NameIdType object, NameIdType member, BasePermissionType[] permissions){
		/// The backing query and related database functions support zero permission length and a null member value
		/// but for direct effective authorization checks, these are required
		///
		if(object == null || member == null || permissions == null || permissions.length == 0){
			logger.error("Invalid parameters");
			return false;
		}
		boolean out_bool = false;
		boolean cache = (isMemberCacheable(member) && isObjectCacheable(object));
		String entChkStr = getEntitlementCheckString(object,member,permissions);
		if(cache && hasPerCache(member, object, permissions)){
			
			boolean cachedAuth = getPerCacheValue(getActorMap(member.getNameType(),object.getNameType()), member, object, permissions);
			logger.debug("*** CACHED AUTHORIZATION: " + entChkStr + " == " + cachedAuth);
			return cachedAuth;
		}
		
		List<EntitlementType> ents = getEffectiveMemberEntitlements(object, member, permissions,false);

		if(ents.size() > 0){
			out_bool = true;
		}
		/// Note: While this could be handled in the DB, I left it outside because I didn't want to have to fork to a different view or function
		/// that included unwinding the person dimension - there is a DB function that does this, but it doesn't scale beyond data and group types.
		///
		else if(member.getNameType() == NameEnumType.PERSON){
			PersonType person = (PersonType)member;
			try {
				Factories.getPersonFactory().populate(person);
			} catch (FactoryException | ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i = 0; i < person.getAccounts().size();i++){
				if(getEntitlementsGrantAccess(object, person.getAccounts().get(i),permissions)){
					out_bool = true;
					break;
				}
				//ents = getEffectiveMemberEntitlements(object, person.getAccounts().get(i), permissions);
				//if(ents.size() > 0) break;
			}
		}
		//logger.info("*** AUTHORIZING: " + entChkStr);
		/*
		for(int i = 0; i < ents.size();i++){
			logger.debug("*** AUTHORIZATION RELEVANCE: " + ents.get(i).getEntitlementType().toString() + " " + ents.get(i).getEntitlementId() + " to " + ents.get(i).getMemberType().toString() + " " + ents.get(i).getMemberId() + " for " + ents.get(i).getObjectType() + " " + ents.get(i).getObjectId());
		}
		*/
		if(cache){
			//logger.debug("*** CACHING AUTHORIZATION: " + entChkStr);
			addToPerCache(member,object,permissions,out_bool);
		}
		return out_bool;
	}
	/*
	 * Given some entity type with a participation table
	 * Find the effective entitlements for the specified member and specified entitlements.
	 * - Warning: The broader the search, the more recursion takes place at participation scope, such that a A permissions * B groups * C accounts can take a fair amount of time to unwind.
	*
	 * Oh my god, it's a dynamic query. Why isn't this a sproc?  What heresy!
	 *    - Because for N number of *participation tables, a sproc would basically wind up being the same thing,
	 *    - And I didn't like the options for dynamically constructing the table reference inside a function in PostGres
	 *    -
	 * Note: This intentionally does not use materialized views.  I was trying to find a query that could build a materialized view faster than the groupRights and dataRights query,
	 * But I also wanted to increase scope to all participation tables, and this query allows that without having to add in all the cache hooks, functions, and views that the other caches use.
	 * 
	 * Also note that this query does not directly hit the caches.  This is because the caches are for specific type combinations, while the functions used in the query remain agnostic
	 * So for specific checks, such as whether an object is in a role or group, use the corresponding method
	 * But when looking for an entitlement check, use this function because it will check across a broader set of possible combinations and then locally cache the entitlement check
	 *    
	 * There are a couple functions defined for this as well.
	 */
	public static List<EntitlementType> getEffectiveMemberEntitlements(NameIdType object, NameIdType member, BasePermissionType[] permissions, boolean exclusion){
		Long[] permissionIds = new Long[permissions.length];
		for(int i = 0; i < permissions.length;i++){
			//if(i > 0) buff.append(",");
			//buff.append(permissions[i].getId());
			if(permissions[i]==null)permissionIds[i] = 0L;
			else permissionIds[i] = permissions[i].getId();
		}
		return getEffectiveMemberEntitlements(object,member,permissionIds,exclusion);
	}
	public static List<EntitlementType> getEffectiveMemberEntitlements(NameIdType object, NameIdType member, Long[] permissionIds, boolean exclusion){
		List<EntitlementType> out_ents = new ArrayList<EntitlementType>();
		/// TODO: Need to add check that object type has a corresponding participation capability
		///
		if(object == null || object.getNameType() == NameEnumType.UNKNOWN){
			logger.error("Invalid object or object name type");
			return out_ents;
		}
	

		String referenceType = (member == null ? "" : member.getNameType().toString());
		long referenceId = (member == null ? 0L : member.getId());
		long objectId = object.getId();
		NameEnumType objectType = object.getNameType();
		Connection conn = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connType = DBFactory.getConnectionType(conn);
		String token = DBFactory.getParamToken(connType);

		  // Match the db function version - RETURNS TABLE (id BIGINT,affectid BIGINT, affecttype text,referenceid BIGINT, referencetype text)
		String sqlQuery =
		"SELECT participationid as id, affectid, affecttype, referenceid, referencetype FROM ("
		+ "SELECT PP.participationid,PP.affectid,PP.affecttype,"
		+"	CASE WHEN GM.referenceid > 0 THEN GM.referenceid"
		+"	ELSE PP.participantid END as referenceid,"
		+"	CASE WHEN GM.referencetype <> '' THEN GM.referencetype"
		+"	ELSE PP.participanttype END as referencetype"
		+"	FROM " + object.getNameType().toString().toLowerCase() + "participation PP"
		+"	LEFT JOIN group_membership(PP.participantid) GM ON (" + token + " = '' OR GM.referencetype = " + token + ") AND (" + token + " = 0 OR GM.referenceid = " + token + ")"
		+"	WHERE " + (exclusion ? "NOT ":"") + "PP.participationid = " + token + " AND (0 = cardinality(" + token + ") OR PP.affectId = ANY(" + token + ")) AND PP.participanttype = 'GROUP'"
		+"	UNION ALL"
		+"	SELECT PP.participationid,PP.affectid,PP.affecttype,"
		+"	CASE WHEN GM.referenceid > 0 THEN GM.referenceid"
		+"	ELSE PP.participantid END as referenceid,"
		+"	CASE WHEN GM.referencetype <> '' THEN GM.referencetype"
		+"	ELSE PP.participanttype END as referencetype"
		+"	FROM " + object.getNameType().toString().toLowerCase() + "participation PP"
		+"	LEFT JOIN role_membership(PP.participantid) GM ON (" + token + " = '' OR GM.referencetype = " + token + ") AND (" + token + " = 0 OR GM.referenceid = " + token + ")"
		+"	WHERE " + (exclusion ? "NOT ":"") + "PP.participationid = " + token + " AND (0 = cardinality(" + token + ") OR PP.affectId = ANY(" + token + ")) AND PP.participanttype = 'ROLE'	"
		+"	UNION ALL"
		+"	SELECT PP.participationid,PP.affectid,PP.affecttype,"
		+"	PP.participantid as referenceid,PP.participanttype as referencetype"
		+"	FROM " + object.getNameType().toString().toLowerCase() + "participation PP"
		+"	WHERE " + (exclusion ? "NOT ":"") + "PP.participationid = " + token + ""
		+"	AND (0 = cardinality(" + token + ") OR PP.affectId = ANY(" + token + "))"
		+"	AND NOT PP.participanttype IN('GROUP','ROLE')"
		+"	AND (" + token + " = '' OR PP.participanttype = " + token + ")"
		+"	AND (" + token + " = 0 OR PP.participantid =" + token + ")"
		+ ") DM WHERE (" + token + " = '' OR referencetype = " + token + ") AND (" + token + " = 0 OR referenceid =" + token + ") AND affectid > 0;"
		;
		
		//logger.info(sqlQuery);
		StringBuffer buff = new StringBuffer();
		//logger.info((exclusion ? "NOT " : "") + "Object: " + object.getId() + " / " + object.getUrn());
		//logger.info("Member: " + (member == null ? "null" : member.getId() + " / " + member.getUrn()));
		for(int i = 0; i < permissionIds.length;i++){
			if(i > 0) buff.append(", ");
			buff.append(Long.toString(permissionIds[i]));
		}
		//logger.info("permissions: " + buff.toString());
		/*
		 * PARAM ORDER
		 
		1: rtype
		2: rtype
		3: rid
		4: rid
		5: id
		6: per[]
		7: per[]
		8: rtype
		9: rtype
		10: rid
		11: rid
		12: id
		13: per[]
		14: per[]
		15: id
		16: per[]
		17: per[]
		18: rtype
		19: rtype
		20: rid
		21: rid
		22: rtype
		23: rtype
		24: rid
		25: rid
		*/
		try{
			long start_query = System.currentTimeMillis();
			PreparedStatement statement = conn.prepareStatement(sqlQuery);
			statement.setString(1, referenceType);
			statement.setString(2, referenceType);
			statement.setLong(3, referenceId);
			statement.setLong(4, referenceId);
			statement.setLong(5, objectId);
			statement.setArray(6, conn.createArrayOf("bigint", permissionIds));
			statement.setArray(7, conn.createArrayOf("bigint", permissionIds));
			statement.setString(8, referenceType);
			statement.setString(9, referenceType);
			statement.setLong(10, referenceId);
			statement.setLong(11, referenceId);
			statement.setLong(12, objectId);
			statement.setArray(13, conn.createArrayOf("bigint", permissionIds));
			statement.setArray(14, conn.createArrayOf("bigint", permissionIds));
			statement.setLong(15, objectId);
			statement.setArray(16, conn.createArrayOf("bigint", permissionIds));
			statement.setArray(17, conn.createArrayOf("bigint", permissionIds));
			statement.setString(18, referenceType);
			statement.setString(19, referenceType);
			statement.setLong(20, referenceId);
			statement.setLong(21, referenceId);
			statement.setString(22, referenceType);
			statement.setString(23, referenceType);
			statement.setLong(24, referenceId);
			statement.setLong(25, referenceId);
			
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				EntitlementType ent = new EntitlementType();
				ent.setObjectId(objectId);
				ent.setObjectType(objectType);
				ent.setMemberId(rset.getLong(4));
				ent.setMemberType(NameEnumType.valueOf(rset.getString(5)));
				ent.setEntitlementId(rset.getLong(2));
				ent.setEntitlementType(AffectEnumType.valueOf(rset.getString(3)));
				ent.setOrganizationId(object.getOrganizationId());
				out_ents.add(ent);
			}
			/*
			if(out_ents.size() > 0){
				logger.debug("Matched " + referenceType + " " + referenceId + " having at least one permission for " + object.getNameType() + " " + objectId);
			}
			else{
				logger.debug("Did not match " + referenceType + " " + referenceId + " having at least one permission for " + object.getNameType() + " " + objectId);
			}
			*/
			rset.close();
			statement.close();
			long stop_query = System.currentTimeMillis();
			long diff = (stop_query - start_query);
			//if(diff > 50L) 
			logger.debug("*** QUERY TIME: " + (stop_query - start_query) + "ms");
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			sqe.printStackTrace();
		}
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return out_ents;
	}
	
	private static boolean getAuthorization(String tableName,String idColumnName,String matchColumnName, NameIdType actor, boolean requireReference, NameIdType obj, BasePermissionType[] permissions)  throws ArgumentException, FactoryException
	{

		boolean out_bool = false;

		StringBuffer buff = new StringBuffer();
		for(int i = 0; i < permissions.length;i++){
			if(i > 0) buff.append(",");
			buff.append(permissions[i].getId());
		}

		Connection conn = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connType = DBFactory.getConnectionType(conn);
		String token = DBFactory.getParamToken(connType);
		boolean linkPerson = (requireReference && actor.getNameType() == NameEnumType.PERSON);;
		try{
			String sql = "SELECT distinct " + idColumnName + " FROM " + tableName + " WHERE " + idColumnName + " = " + token + " AND " + matchColumnName + " = " + token + " AND affecttype = '" + AffectEnumType.GRANT_PERMISSION.toString() + "' AND affectid IN (" + buff.toString() + ") and organizationid = " + token + (requireReference ? " and referencetype = " + token : "");

			if(linkPerson){
				sql += " UNION ALL SELECT distinct participantid FROM " + tableName + " T2 "
						+ " inner join personparticipation PU on T2.referencetype = PU.participanttype AND PU.participantid = T2.referenceid "
						+ " WHERE PU.participationid = " + token + " AND T2." + matchColumnName + " = " + token + " AND T2.affecttype = 'GRANT_PERMISSION' AND T2.affectid IN (" + buff.toString() + ") and T2.organizationid = " + token + " and (T2.referencetype = 'ACCOUNT' or T2.referencetype = 'USER')"
				;
				logger.debug("Extending query to link person authorization check to direct user and child participants.");
			}
			
			PreparedStatement stat = conn.prepareStatement(sql);
			logger.debug(sql);
			stat.setLong(1, actor.getId());
			stat.setLong(2,obj.getId());
			stat.setLong(3, obj.getOrganizationId());
			if(requireReference) stat.setString(4, actor.getNameType().toString());
			if(linkPerson){
				stat.setLong(5, actor.getId());
				stat.setLong(6, obj.getId());
				stat.setLong(7, obj.getOrganizationId());
			}
			ResultSet rset = stat.executeQuery();
			if(rset.next()){
				long match_id = rset.getLong(1);
				//addToCache(user,role);
				out_bool = true;
				logger.debug("Matched " + actor.getNameType() + " " + match_id + (linkPerson ? " (via person linkage)" : "") + " having at least one permission in (" + buff.toString() + ") for " + obj.getNameType() + " " + obj.getId() + " in org " + obj.getOrganizationId());

			}
			else{
				logger.debug("Did not match " + actor.getNameType() + " " + actor.getId() + " having at least one permission in (" + buff.toString() + ") for " + obj.getNameType() + " " + obj.getId() + " in org " + obj.getOrganizationId());
			}
			rset.close();
			stat.close();
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			sqe.printStackTrace();
		}
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return out_bool;
		
	}
	
	/// TODO: These can all be refactored into three statements, but the cache lookup has to change where the actor type differs between user/account/person and role
	///
	public static boolean getAuthorization(NameIdType actor, NameIdType object, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0){
			throw new ArgumentException("At least one permission must be specified");
		}
		if(!objectMap.containsKey(object.getNameType())){
			throw new ArgumentException("Object type " + object.getNameType() + " is not registered for authorization");
		}
		if(!objectMap.get(object.getNameType()).containsKey(actor.getNameType())){
			throw new ArgumentException("Actor type " + actor.getNameType() + " is not registered with " + object.getNameType() + " for authorization");
		}
		if(hasPerCache(actor, object, permissions)){
			//logger.debug("Cached match " + person.getNameType() + " " + person.getId() + " checking data " + data.getId() + " in org " + data.getOrganizationId());
			//return getCacheValue(person,data,permissions);
			return getPerCacheValue(getAuthorizationMap(object.getNameType(),actor.getNameType()).getMap(),actor,object,permissions);
		}
		String objType = object.getNameType().toString().toLowerCase();
		if(actor.getNameType() == NameEnumType.ROLE){
			out_bool = getAuthorization("effective" + objType + "roles","baseroleid",objType + "id",actor,false,object,permissions);
		}
		else{
			out_bool = getAuthorization(objType + "rights","referenceid",objType + "id",actor,true,object,permissions);
		}
		addToPerCache(actor,object,permissions,out_bool);
		return out_bool;
		
	}
	
	/// TODO: These can all be refactored into three statements, but the cache lookup has to change where the actor type differs between user/account/person and role
	///
	public static boolean getDataAuthorization(PersonType person, DataType data, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasPerCache(person, data, permissions)){
			//logger.debug("Cached match " + person.getNameType() + " " + person.getId() + " checking data " + data.getId() + " in org " + data.getOrganizationId());
			return getCacheValue(person,data,permissions);

		}

		out_bool = getAuthorization("datarights","referenceid","dataid",person,true,data,permissions);
		addToPerCache(person,data,permissions,out_bool);
		return out_bool;
		
	}

	public static boolean getRoleAuthorization(PersonType person, BaseRoleType role, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasPerCache(person, role, permissions)){
			//logger.debug("Cached match " + person.getNameType() + " " + person.getId() + " checking role " + role.getId() + " in org " + role.getOrganizationId());
			return getCacheValue(person,role,permissions);

		}

		out_bool = getAuthorization("rolerights","referenceid","roleid",person,true,role,permissions);
		addToPerCache(person,role,permissions,out_bool);
		return out_bool;
		
	}
	public static boolean getGroupAuthorization(PersonType person, BaseGroupType group, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasPerCache(person, group, permissions)){
			//logger.debug("Cached match " + person.getNameType() + " " + person.getId() + " checking group " + group.getId() + " in org " + group.getOrganizationId());
			return getCacheValue(person,group,permissions);

		}

		out_bool = getAuthorization("grouprights","referenceid","groupid",person,true,group,permissions);
		addToPerCache(person,group,permissions,out_bool);
		return out_bool;
		
	}
	
	public static boolean getDataAuthorization(AccountType account, DataType data, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasPerCache(account, data, permissions)){
			//logger.debug("Cached match " + account.getNameType() + " " + account.getId() + " checking data " + data.getId() + " in org " + data.getOrganizationId());
			return getCacheValue(account,data,permissions);

		}

		out_bool = getAuthorization("datarights","referenceid","dataid",account,true,data,permissions);
		addToPerCache(account,data,permissions,out_bool);
		return out_bool;
		
	}

	public static boolean getRoleAuthorization(AccountType account, BaseRoleType role, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasPerCache(account, role, permissions)){
			//logger.debug("Cached match " + account.getNameType() + " " + account.getId() + " checking role " + role.getId() + " in org " + role.getOrganizationId());
			return getCacheValue(account,role,permissions);

		}

		out_bool = getAuthorization("rolerights","referenceid","roleid",account,true,role,permissions);
		addToPerCache(account,role,permissions,out_bool);
		return out_bool;
		
	}
	public static boolean getGroupAuthorization(AccountType account, BaseGroupType group, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasPerCache(account, group, permissions)){
			//logger.debug("Cached match " + account.getNameType() + " " + account.getId() + " checking group " + group.getId() + " in org " + group.getOrganizationId());
			return getCacheValue(account,group,permissions);

		}

		out_bool = getAuthorization("grouprights","referenceid","groupid",account,true,group,permissions);
		addToPerCache(account,group,permissions,out_bool);
		return out_bool;
		
	}
	public static boolean getDataAuthorization(UserType user, DataType data, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasPerCache(user, data, permissions)){
			//logger.debug("Cached match " + user.getNameType() + " " + user.getId() + " checking data " + data.getId() + " in org " + data.getOrganizationId());
			return getCacheValue(user,data,permissions);

		}

		out_bool = getAuthorization("datarights","referenceid","dataid",user,true,data,permissions);
		addToPerCache(user,data,permissions,out_bool);
		return out_bool;
		
	}

	public static boolean getRoleAuthorization(UserType user, BaseRoleType role, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasPerCache(user, role, permissions)){
			//logger.debug("Cached match " + user.getNameType() + " " + user.getId() + " checking role " + role.getId() + " in org " + role.getOrganizationId());
			return getCacheValue(user,role,permissions);

		}

		out_bool = getAuthorization("rolerights","referenceid","roleid",user,true,role,permissions);
		addToPerCache(user,role,permissions,out_bool);
		return out_bool;
		
	}
	public static boolean getGroupAuthorization(UserType user, BaseGroupType group, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasPerCache(user, group, permissions)){
			//logger.debug("Cached match " + user.getNameType() + " " + user.getId() + " checking group " + group.getId() + " in org " + group.getOrganizationId());
			return getCacheValue(user,group,permissions);

		}

		out_bool = getAuthorization("grouprights","referenceid","groupid",user,true,group,permissions);
		addToPerCache(user,group,permissions,out_bool);
		return out_bool;
		
	}
	
	public static boolean getDataAuthorization(BaseRoleType actor, DataType data, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasPerCache(actor, data, permissions)){
			//logger.debug("Cached match " + actor.getNameType() + " " + actor.getId() + " checking data " + data.getId() + " in org " + data.getOrganizationId());
			return getCacheValue(actor,data,permissions);

		}

		out_bool = getAuthorization("effectivedataroles","baseroleid","dataid",actor,false,data,permissions);
		addToPerCache(actor,data,permissions,out_bool);
		return out_bool;
		
	}

	public static boolean getRoleAuthorization(BaseRoleType actor, BaseRoleType role, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasPerCache(actor, role, permissions)){
			//logger.debug("Cached match " + actor.getNameType() + " " + actor.getId() + " checking role " + role.getId() + " in org " + role.getOrganizationId());
			return getCacheValue(actor,role,permissions);

		}

		out_bool = getAuthorization("effectiveroleroles","baseroleid","roleid",actor,false,role,permissions);
		addToPerCache(actor,role,permissions,out_bool);
		return out_bool;
		
	}
	public static boolean getGroupAuthorization(BaseRoleType actor, BaseGroupType group, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasPerCache(actor, group, permissions)){
			//logger.debug("Cached match " + actor.getNameType() + " " + actor.getId() + " checking group " + group.getId() + " in org " + group.getOrganizationId());
			return getCacheValue(actor,group,permissions);

		}

		out_bool = getAuthorization("effectivegrouproles","baseroleid","groupid",actor,false,group,permissions);
		addToPerCache(actor,group,permissions,out_bool);
		return out_bool;
		
	}
	public static boolean getIsPersonInEffectiveRole(BaseRoleType role, PersonType person) throws ArgumentException, FactoryException
	{
		return getIsPersonInEffectiveRole(role, person, null, AffectEnumType.UNKNOWN);
	}	
	public static boolean getIsPersonInEffectiveRole(BaseRoleType role, PersonType person, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		return getIsActorInEffectiveRole(role, person, permission, affect_type);
	}
	public static boolean getIsAccountInEffectiveRole(BaseRoleType role, AccountType account) throws ArgumentException, FactoryException
	{
		return getIsAccountInEffectiveRole(role, account, null, AffectEnumType.UNKNOWN);
	}	
	public static boolean getIsAccountInEffectiveRole(BaseRoleType role, AccountType account, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		return getIsActorInEffectiveRole(role, account, permission, affect_type);
	}
	
	public static boolean getIsUserInEffectiveRole(BaseRoleType role, UserType user) throws ArgumentException, FactoryException
	{
		return getIsUserInEffectiveRole(role, user, null, AffectEnumType.UNKNOWN);
	}	
	public static boolean getIsUserInEffectiveRole(BaseRoleType role, UserType user, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		return getIsActorInEffectiveRole(role, user, permission, affect_type);
	}
	private static boolean getIsActorInEffectiveRole(BaseRoleType role, NameIdType actor, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
		{
		if(affect_type != AffectEnumType.UNKNOWN) throw new ArgumentException("AffectType is not supported for checking role participation (at the moment)");
		//logger.debug(getEffectiveMemberEntitlements(role, actor, new BasePermissionType[]{permission}, true));
		if(hasCache(actor,role)){
			//logger.debug("Cached match " + actor.getNameType() + " " + actor.getId() + " checking role " + role.getId() + " in org " + role.getOrganizationId());
			return getCacheValue(actor,role);
		}
		String prefix = null;
		boolean linkPerson = false;
		if(actor.getNameType() == NameEnumType.USER) prefix = "user";
		else if(actor.getNameType() == NameEnumType.ACCOUNT) prefix = "account";
		else if(actor.getNameType() == NameEnumType.PERSON){
			prefix = "person";
			linkPerson = true;
		}
		else throw new ArgumentException("Unexpected actor type " + actor.getNameType());
		
		boolean out_bool = false;
		Connection conn = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connType = DBFactory.getConnectionType(conn);
		String token = DBFactory.getParamToken(connType);
		try{
			String sql = "SELECT distinct " + prefix + "id FROM " + prefix + "rolecache WHERE " + prefix + "id = " + token + " AND effectiveroleid = " + token + " and organizationid = " + token;
			if(linkPerson){
				sql += " UNION ALL "
					+ " SELECT distinct participantid FROM userrolecache URC "
					+ " inner join personparticipation PU on URC.userid = PU.participantid AND PU.participanttype = 'USER' AND PU.participationid = " + token + " AND URC.effectiveroleid = " + token + " AND URC.organizationid = " + token
					+ " UNION ALL "
					+ " SELECT distinct participantid FROM accountrolecache ARC "
					+ " inner join personparticipation PU2 on ARC.accountid = PU2.participantid AND PU2.participanttype = 'ACCOUNT' AND PU2.participationid = " + token + " AND ARC.effectiveroleid = " + token + " AND ARC.organizationid = " + token;
			}
			PreparedStatement stat = conn.prepareStatement(sql);
			logger.debug(sql);

			stat.setLong(1, actor.getId());
			stat.setLong(2,role.getId());
			stat.setLong(3, role.getOrganizationId());
			if(linkPerson){
				stat.setLong(4, actor.getId());
				stat.setLong(5,role.getId());
				stat.setLong(6, role.getOrganizationId());
				stat.setLong(7, actor.getId());
				stat.setLong(8,role.getId());
				stat.setLong(9, role.getOrganizationId());
			}
			ResultSet rset = stat.executeQuery();
			if(rset.next()){
				long match_id = rset.getLong(1);

				/// TODO: Note, although deny's are stored at the datalevel, they are not currently being evaluated here
				/// So a DENY will actually turn into a grant in the code
				///
				out_bool = true;
				logger.debug("Matched " + actor.getNameType() + " " + match_id + (linkPerson ? " (via person linkage)" : "") + " having role " + role.getId() + " in org " + role.getOrganizationId());
			}
			else{
				logger.warn("Did not match " + actor.getNameType() + " " + actor.getName() + " (" + actor.getId() + ") with role " + role.getName() + " (" + role.getId() + ") in organization (" + role.getOrganizationId() + ")");
			}
			rset.close();
			stat.close();
			addToCache(actor,role,out_bool);
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			sqe.printStackTrace();
		}
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return out_bool;
		
	}
	public static List<BaseRoleType> getEffectiveRolesForUser(UserType user) throws ArgumentException, FactoryException
	{
		return getEffectiveRoles(user);
	}
	
	public static List<BaseRoleType> getEffectiveRolesForPerson(PersonType person) throws ArgumentException, FactoryException
	{
		return getEffectiveRoles(person);
	}

	public static List<BaseRoleType> getEffectiveRolesForAccount(AccountType account) throws ArgumentException, FactoryException
	{
		return getEffectiveRoles(account);
	}
	
	private static List<BaseRoleType> getEffectiveRoles(NameIdType actor) throws ArgumentException, FactoryException
	{
		List<BaseRoleType> roles = new ArrayList<BaseRoleType>();
		String prefix = null;
		boolean linkPerson = false;
		if(actor.getNameType() == NameEnumType.USER) prefix = "user";
		else if(actor.getNameType() == NameEnumType.ACCOUNT) prefix = "account";
		else if(actor.getNameType() == NameEnumType.PERSON){
			prefix = "person";
			linkPerson = true;
		}
		else throw new ArgumentException("Unexpected actor type " + actor.getNameType());
		
		Connection conn = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connType = DBFactory.getConnectionType(conn);
		String token = DBFactory.getParamToken(connType);
		try{
			String sql = "SELECT distinct effectiveroleid FROM " + prefix + "rolecache WHERE " + prefix + "id = " + token + " and organizationid = " + token;
			if(linkPerson){
				sql += " UNION ALL "
					+ " SELECT distinct effectiveroleid FROM userrolecache URC "
					+ " inner join personparticipation PU on URC.userid = PU.participantid AND PU.participanttype = 'USER' AND PU.participationid = " + token + " AND URC.organizationid = " + token
					+ " UNION ALL "
					+ " SELECT distinct effectiverole FROM accountrolecache ARC "
					+ " inner join personparticipation PU2 on ARC.accountid = PU2.participantid AND PU2.participanttype = 'ACCOUNT' AND PU2.participationid = " + token + " AND ARC.organizationid = " + token;
			}
			PreparedStatement stat = conn.prepareStatement(sql);
			logger.debug(sql);
	
			stat.setLong(1, actor.getId());
			stat.setLong(2, actor.getOrganizationId());
			if(linkPerson){
				stat.setLong(3, actor.getId());
				stat.setLong(4, actor.getOrganizationId());
				stat.setLong(5, actor.getId());
				stat.setLong(6, actor.getOrganizationId());
			}
			ResultSet rset = stat.executeQuery();
			List<Long> ids = new ArrayList<Long>();
			while(rset.next()){
				ids.add(rset.getLong(1));
			}
			rset.close();
			stat.close();
			roles = Factories.getRoleFactory().getListByIds(ArrayUtils.toPrimitive(ids.toArray(new Long[0])), actor.getOrganizationId());
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			sqe.printStackTrace();
		}
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return roles;
		
	}
	public static boolean hasPendingEntries(){
		return (rebuildPersons.size() > 0 || rebuildAccounts.size() > 0 || rebuildUsers.size() > 0 || rebuildRoles.size() > 0 || rebuildGroups.size() > 0 || rebuildData.size() > 0);
	}
	public static boolean rebuildPendingRoleCache() throws FactoryException, ArgumentException{
		boolean out_bool = false;
		long start = System.currentTimeMillis();
		UserType user = null;
		AccountType account = null;
		PersonType person = null;
		BaseRoleType role = null;
		BaseGroupType group = null;
		synchronized(lockObject){
			List<BaseRoleType> roles = Arrays.asList(rebuildRoles.values().toArray(new BaseRoleType[0]));
			for(int i = 0; i < roles.size();i++){
				role =roles.get(i);
				/// negative id indicates possible bulk entry
				/// 
				if(role.getId() < 0){
					logger.error("Role BulkEntry with id " + role.getId() + " detected.  The Bulk Session must be written before rebuilding the cache.");
					throw new ArgumentException("Role BulkEntry with id " + role.getId() + " detected.  The Bulk Session must be written before rebuilding the cache.");
				}
				if(role.getRoleType() == RoleEnumType.USER){
					List<UserType> rusers = Factories.getRoleParticipationFactory().getUsersInRole((UserRoleType)role);
					for(int r = 0; r < rusers.size();r++){
						user = rusers.get(r);
						if(rebuildUsers.containsKey(user.getId())==false){
							rebuildUsers.put(user.getId(),user);
						}
					}
				}
				else if(role.getRoleType() == RoleEnumType.ACCOUNT){
					List<AccountType> raccounts = Factories.getRoleParticipationFactory().getAccountsInRole((AccountRoleType)role);
					for(int r = 0; r < raccounts.size();r++){
						account = raccounts.get(r);
						if(rebuildAccounts.containsKey(account.getId())==false){
							rebuildAccounts.put(account.getId(),account);
						}
					}
				}
				else if(role.getRoleType() == RoleEnumType.PERSON){
					List<PersonType> rpersons = Factories.getRoleParticipationFactory().getPersonsInRole((PersonRoleType)role);
					for(int r = 0; r < rpersons.size();r++){
						person = rpersons.get(r);
						if(rebuildPersons.containsKey(person.getId())==false){
							rebuildPersons.put(person.getId(),person);
						}
					}
				}
				clearCache(roles.get(i));
			}
			logger.info("Rebuilding role cache for " + roles.size() + " roles");
			if(roles.size() > 0){
				rebuildRoleRoleCache(roles,roles.get(0).getOrganizationId());
				rebuildRoles.clear();
			}
			///rebuildRoles.clear();
			List<BaseGroupType> groups = Arrays.asList(rebuildGroups.values().toArray(new BaseGroupType[0]));
			for(int i = 0; i < groups.size();i++){
				group = groups.get(i);
				/// negative id indicates possible bulk entry
				/// 
				if(group.getId() < 0){
					logger.error("Group BulkEntry with id " + group.getId() + " detected.  The Bulk Session must be written before rebuilding the cache.");
					throw new ArgumentException("Group BulkEntry with id " + group.getId() + " detected.  The Bulk Session must be written before rebuilding the cache.");
				}
				if(group.getGroupType() == GroupEnumType.USER){
					List<UserType> gusers = Factories.getGroupParticipationFactory().getUsersInGroup((UserGroupType)group);
					for(int g = 0; g < gusers.size();g++){
						if(rebuildUsers.containsKey(gusers.get(g).getId()) == false){
							rebuildUsers.put(gusers.get(g).getId(), gusers.get(g));
						}
					}
				}
				else if(group.getGroupType() == GroupEnumType.ACCOUNT){
					List<AccountType> gaccounts = Factories.getGroupParticipationFactory().getAccountsInGroup((AccountGroupType)group);
					for(int g = 0; g < gaccounts.size();g++){
						if(rebuildAccounts.containsKey(gaccounts.get(g).getId()) == false){
							rebuildAccounts.put(gaccounts.get(g).getId(), gaccounts.get(g));
						}
					}
				}
				else if(group.getGroupType() == GroupEnumType.PERSON){
					List<PersonType> gpersons = Factories.getGroupParticipationFactory().getPersonsInGroup((PersonGroupType)group);
					for(int g = 0; g < gpersons.size();g++){
						if(rebuildPersons.containsKey(gpersons.get(g).getId()) == false){
							rebuildPersons.put(gpersons.get(g).getId(), gpersons.get(g));
						}
					}
				}

				clearCache(groups.get(i));
			}
			logger.info("Rebuilding role cache for " + groups.size() + " groups");
			if(groups.size() > 0){
				rebuildGroupRoleCache(groups,groups.get(0).getOrganizationId());
				rebuildGroups.clear();
			}
				
			
			List<UserType> users = Arrays.asList(rebuildUsers.values().toArray(new UserType[0]));
			logger.info("Rebuilding role cache for " + users.size() + " users");
			if(users.size() > 0){
				out_bool = rebuildUserRoleCache(users,users.get(0).getOrganizationId());
				rebuildUsers.clear();
			}
			
			List<AccountType> accounts = Arrays.asList(rebuildAccounts.values().toArray(new AccountType[0]));
			logger.info("Rebuilding role cache for " + accounts.size() + " accounts");
			if(accounts.size() > 0){
				out_bool = rebuildAccountRoleCache(accounts,accounts.get(0).getOrganizationId());
				rebuildAccounts.clear();
			}

			List<PersonType> persons = Arrays.asList(rebuildPersons.values().toArray(new PersonType[0]));
			logger.info("Rebuilding role cache for " + persons.size() + " persons");
			if(persons.size() > 0){
				out_bool = rebuildPersonRoleCache(persons,persons.get(0).getOrganizationId());
				rebuildPersons.clear();
			}
			
			List<DataType> data = Arrays.asList(rebuildData.values().toArray(new DataType[0]));
			logger.info("Rebuilding role cache for " + data.size() + " data");
			if(data.size() > 0){
				out_bool = rebuildDataRoleCache(data,data.get(0).getOrganizationId());
				rebuildData.clear();
			}
		}
		long stop = System.currentTimeMillis();
		if(out_bool) logger.info("Time to rebuild cache: " + (stop - start) + " ms");
		else logger.info("Did not rebuild cache: " + (stop - start) + " ms");
		return out_bool;
		
	}
	public static boolean rebuildCache(NameEnumType objectType, long organizationId){
		if(objectMap.containsKey(objectType)){
			String functionName = "cache_all_" + objectType.toString().toLowerCase() + "_roles";
			return rebuildRoleCache(functionName,organizationId);
		}
		return false;

	}
	public static boolean rebuildCache(NameIdType object) throws ArgumentException{
		return rebuildCache(Arrays.asList(object),object.getOrganizationId());
	}
	public static boolean rebuildCache(List<NameIdType> objects, long organizationId) throws ArgumentException{
		boolean outBool = false;
		NameIdType object = (objects.size() > 0 ? objects.get(0) : null);
		if(object != null && objectMap.containsKey(object.getNameType())){
			String functionName = "cache_" + object.getNameType().toString().toLowerCase() + "_roles";
			return rebuildRoleCache(functionName, objects, organizationId);
		}
		else{
			logger.error("Object type not registered or object is null");
		}
		return outBool;
	}
	public static boolean rebuildGroupRoleCache(BaseGroupType user) throws ArgumentException{
		return rebuildRoleCache("cache_group_roles",Arrays.asList(user),user.getOrganizationId());
	}
	public static boolean rebuildGroupRoleCache(List<BaseGroupType> users, long organizationId) throws ArgumentException{
		return rebuildRoleCache("cache_group_roles",users,organizationId);
	}
	public static boolean rebuildDataRoleCache(DataType data) throws ArgumentException{
		return rebuildRoleCache("cache_data_roles",Arrays.asList(data),data.getOrganizationId());
	}
	public static boolean rebuildDataRoleCache(List<DataType> users, long organizationId) throws ArgumentException{
		return rebuildRoleCache("cache_data_roles",users,organizationId);
	}
	public static boolean rebuildRoleRoleCache(BaseRoleType role) throws ArgumentException{
		return rebuildRoleCache("cache_role_roles",Arrays.asList(role),role.getOrganizationId());
	}
	public static boolean rebuildRoleRoleCache(List<BaseRoleType> roles, long organizationId) throws ArgumentException{
		return rebuildRoleCache("cache_role_roles",roles,organizationId);
	}
	public static boolean rebuildUserRoleCache(UserType user) throws ArgumentException{
		return rebuildRoleCache("cache_user_roles",Arrays.asList(user),user.getOrganizationId());
	}
	public static boolean rebuildUserRoleCache(List<UserType> users, long organizationId) throws ArgumentException{
		return rebuildRoleCache("cache_user_roles",users,organizationId);
	}
	public static boolean rebuildAccountRoleCache(AccountType account) throws ArgumentException{
		return rebuildRoleCache("cache_account_roles",Arrays.asList(account),account.getOrganizationId());
	}
	public static boolean rebuildAccountRoleCache(List<AccountType> accounts, long organizationId) throws ArgumentException{
		return rebuildRoleCache("cache_account_roles",accounts,organizationId);
	}
	public static boolean rebuildPersonRoleCache(PersonType person) throws ArgumentException{
		return rebuildRoleCache("cache_person_roles",Arrays.asList(person),person.getOrganizationId());
	}
	public static boolean rebuildPersonRoleCache(List<PersonType> persons, long organizationId) throws ArgumentException{
		return rebuildRoleCache("cache_person_roles",persons,organizationId);
	}

	private static <T> boolean rebuildRoleCache(String functionName, List<T> objects,long organizationId) throws ArgumentException{
		boolean out_bool = false;
		Connection conn = ConnectionFactory.getInstance().getConnection();
		int maxIn = maximum_insert_size;
		List<StringBuffer> buffs = new ArrayList<StringBuffer>();
		StringBuffer uBuff = new StringBuffer();
		NameIdType object = null;
		for(int i = 0; i < objects.size();i++){
			
			object = (NameIdType)objects.get(i);
			/// negative id indicates possible bulk entry
			/// 
			if(object.getId() < 0){
				logger.error("Skipping possible BulkEntry with id " + object.getId());
				continue;
			}

			if(uBuff.length() > 0) uBuff.append(",");
			uBuff.append(Long.toString(object.getId()));
			clearCache(object);
			if(i > 0 && (i % maxIn)==0){
				buffs.add(uBuff);
				uBuff = new StringBuffer();
			}
		}
		if(uBuff.length() > 0) buffs.add(uBuff);
		CONNECTION_TYPE connType = DBFactory.getConnectionType(conn);
		int updated = 0;
		try{
			Statement stat = conn.createStatement();
			
			for(int i = 0; i < buffs.size();i++){
				//String sql = "SELECT U.id,cache_user_roles(U.id,U.organizationid) from users U WHERE U.organizationid = " + org.getId() + " AND U.id IN (" + buffs.get(i).toString() + ")";
				String sql = "SELECT " + functionName + "(ARRAY[" + buffs.get(i).toString() + "]," + organizationId + ")";
				//logger.debug(sql);
				ResultSet rset = stat.executeQuery(sql);
				if(rset.next()){
					updated++;
				}
				rset.close();

			}
			stat.close();
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			sqe.printStackTrace();
		}
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		logger.info("Rebuilt role cache with " + updated + " operations");
		return (updated > 0);
	}
	public static boolean rebuildGroupRoleCache(long organizationId){
		return rebuildRoleCache("cache_all_group_roles",organizationId); 
	}
	public static boolean rebuildRoleRoleCache(long organizationId){
		return rebuildRoleCache("cache_all_role_roles",organizationId);
	}
	public static boolean rebuildDataRoleCache(long organizationId){
		return rebuildRoleCache("cache_all_data_roles",organizationId);
	}
	public static boolean rebuildUserRoleCache(long organizationId){
		return rebuildRoleCache("cache_all_user_roles",organizationId);
	}
	public static boolean rebuildAccountRoleCache(long organizationId){
		return rebuildRoleCache("cache_all_account_roles",organizationId);
	}
	public static boolean rebuildPersonRoleCache(long organizationId){
		return rebuildRoleCache("cache_all_person_roles",organizationId);
	}
	public static boolean rebuildRoleCache(long organizationId){
		return (
				rebuildRoleCache("cache_all_group_roles",organizationId)
				&&
				rebuildRoleCache("cache_all_role_roles",organizationId)
				&&
				rebuildRoleCache("cache_all_data_roles",organizationId)
				&&
				rebuildRoleCache("cache_all_user_roles",organizationId)
				&&
				rebuildRoleCache("cache_all_account_roles",organizationId)
				&&
				rebuildRoleCache("cache_all_person_roles",organizationId)
			);
	}

	private static boolean rebuildRoleCache(String functionName,long organizationId){
		boolean out_bool = false;
		Connection conn = ConnectionFactory.getInstance().getConnection();

		CONNECTION_TYPE connType = DBFactory.getConnectionType(conn);
		String token = DBFactory.getParamToken(connType);
		try{
			//PreparedStatement stat = conn.prepareStatement("SELECT U.id,cache_user_roles(U.id,U.organizationid) from users U WHERE U.organizationid = " + token);
			Statement stat = conn.createStatement();
			ResultSet rset = stat.executeQuery("SELECT " + functionName + "(" + organizationId + ");");
			if(rset.next()){
				out_bool = true;
				clearCache();
			}
			rset.close();
			stat.close();
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			sqe.printStackTrace();
		}
		finally{
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		return out_bool;
	}
	public static boolean rebuildCache(){
		boolean out_bool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		try {
			Statement stat = connection.createStatement();
			stat.executeQuery("SELECT * FROM cache_roles();");
			out_bool = true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return out_bool;
	}
	/// TODO: Remove. Not currently used
	///
	public static boolean rebuildEntitlementsCache(){
		boolean out_bool = false;
		logger.warn("DEV DEV - Still Under Development");
		long start = System.currentTimeMillis();
		Connection connection = ConnectionFactory.getInstance().getConnection();
		try {
			Statement stat = connection.createStatement();
			stat.executeQuery("SELECT * FROM cache_group_entitlements();");
			stat.executeQuery("SELECT * FROM cache_data_entitlements();");
			out_bool = true;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Rebuilt Entitlements Cache in " + (System.currentTimeMillis() - start) + "ms");
		return out_bool;
	}

	
}
class RebuildMap{
	private NameEnumType object = NameEnumType.UNKNOWN;
	private List<Long> map = new ArrayList<>();
	public RebuildMap(NameEnumType objectType){
		object = objectType;
	}
	public NameEnumType getObject() {
		return object;
	}
	public List<Long> getMap() {
		return map;
	}
	
}
class AuthorizationMapType{
	private NameEnumType actor = NameEnumType.UNKNOWN;
	private NameEnumType object = NameEnumType.UNKNOWN;
	/*
	 * 
	 */
	/// Using current format to be backwards compat : Actor<->Object<->Permission<->Granted
	/// XXXXXX Object<->Actor<->Permission<->Granted
	///
	private Map<Long,Map<Long,Map<Long,Boolean>>> map = new HashMap<>();
	public AuthorizationMapType(NameEnumType objectType, NameEnumType actorType){
		actor = actorType;
		object = objectType;
	}
	public NameEnumType getActor() {
		return actor;
	}
	public NameEnumType getObject() {
		return object;
	}
	public Map<Long,Map<Long,Map<Long,Boolean>>> getMap() {
		return map;
	}
	
}
