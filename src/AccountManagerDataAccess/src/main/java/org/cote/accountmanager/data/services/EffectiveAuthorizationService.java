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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.GroupParticipationFactory;
import org.cote.accountmanager.data.factory.ParticipationFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
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
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;



/*
 
 Updates
 
 	2019/11/20 - Stripped out the legacy caching implementation and redundant method signatures, refactored how the cache is cleaned up
   
    2016/05/10 - Introduced more generic mechanism for handling object level entitlements, rather than hard coding the maps into this class
 
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
 function roles_from_leaf(id) - computes a role path up from a given node.
 function roles_to_leaf(id) - computes a role path down from a given node.
 view effective{Actor}Roles - computed permission<->role distribution for {Actor}, following the role path and mapping to actor or actor groups
 function cache_{actor}_roles(id) - caches the result of effective{Actor}Roles for a specified actor into the {actor}rolecache table
 view effectiveGroupRoleRights - maps {actor}rolecache to groupparticipation to yield all effective permissions by group id by user
 view effectiveDataRoleRights - maps {actor}rolecache to dataparticipation to yield all effective permissions by data id by user
 view groupRights - Union of effectiveGroupRoleRights and group{Actor}Rights.
 view dataRights - Union of effectiveDataRoleRights and data{Actor}Rights.
 
  When making changes to authorization, such as adding/removing role participants, groups, or data permissions, even if done through AuthorizationUtil, it is necessary to:
 1) Rebuild the corresponding cache if making indirect changes, such as group or roles,
 2) And/or, clear the memory cache for the specified object(s) if making direct permission changes.  While the underlying factory participations will be reset, the authorization service creates denormalized permission assertions, and won't clear any local cache until explicitly instructed to for bulk/perf reasons. 
    a) Note: In memory cache is per node.  Therefore, managing permissions from a distributed setup requires a cache reset across nodes once the permissions change 
  
 */
public class EffectiveAuthorizationService {
	

	
	public static final Logger logger = LogManager.getLogger(EffectiveAuthorizationService.class);

	/// This big ugly construct is used to hash the previous cache hashes against the object types for quick look-up and to avoid code duplication
	/// {ActorType}->{AuthorizableFactoryType}->{ActorId}->{ObjectId}->{PermissionId}->{Value}
	///
	private static Map<NameEnumType,Map<NameEnumType,Map<Long,Map<Long,Map<Long,Boolean>>>>> actorMap = new HashMap<>();
	private static Map<NameEnumType,Map<NameEnumType,AuthorizationMapType>> objectMap = new HashMap<>();
	
	private static Map<NameEnumType,RebuildMap> rebuildMap = new HashMap<>();

	
	/// rebuildUsers and rebuildAccounts are currently not handled with rebuildMap as that map is predicated on registering an authorization provider for a factory, and users and accounts do not have one.
	private static Map<Long,NameIdType> rebuildUsers = new HashMap<>();
	private static Map<Long,NameIdType> rebuildAccounts = new HashMap<>();

	public static final int maximum_insert_size = 2500;
	

	
	private static Object lockObject = new Object();
	
	public static boolean registerType(NameEnumType objectType, NameEnumType actorType){
		
		if(objectMap.containsKey(objectType) == false){
			objectMap.put(objectType, new HashMap<>());
		}
		if(objectMap.get(objectType).containsKey(actorType)){
			logger.debug("Actor " + actorType + " already registered for " + objectType);
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
	
	static{
		actorMap.put(NameEnumType.PERSON, new HashMap<>());
		actorMap.put(NameEnumType.USER, new HashMap<>());
		actorMap.put(NameEnumType.ACCOUNT, new HashMap<>());
		actorMap.put(NameEnumType.ROLE, new HashMap<>());
		actorMap.get(NameEnumType.PERSON).put(NameEnumType.GROUP, new HashMap<>());
		actorMap.get(NameEnumType.PERSON).put(NameEnumType.DATA, new HashMap<>());
		actorMap.get(NameEnumType.PERSON).put(NameEnumType.ROLE, new HashMap<>());
		actorMap.get(NameEnumType.ACCOUNT).put(NameEnumType.GROUP, new HashMap<>());
		actorMap.get(NameEnumType.ACCOUNT).put(NameEnumType.DATA, new HashMap<>());
		actorMap.get(NameEnumType.ACCOUNT).put(NameEnumType.ROLE, new HashMap<>());
		actorMap.get(NameEnumType.USER).put(NameEnumType.GROUP, new HashMap<>());
		actorMap.get(NameEnumType.USER).put(NameEnumType.DATA, new HashMap<>());
		actorMap.get(NameEnumType.USER).put(NameEnumType.ROLE, new HashMap<>());
		actorMap.get(NameEnumType.ROLE).put(NameEnumType.GROUP, new HashMap<>());
		actorMap.get(NameEnumType.ROLE).put(NameEnumType.DATA, new HashMap<>());
		actorMap.get(NameEnumType.ROLE).put(NameEnumType.ROLE, new HashMap<>());
		
	}
	
	public static void pendUpdate(List<NameIdType> map){
		for(NameIdType obj : map) pendUpdate(obj);
	}
	public static void pendUpdate(NameIdType map){
		if(rebuildMap.containsKey(map.getNameType())){
			RebuildMap rMap = rebuildMap.get(map.getNameType());
			if(!rMap.getMap().containsKey(map.getId())){
				logger.debug("Pending object update for " + map.getNameType().toString() + " #" + map.getId());
				rMap.getMap().put(map.getId(), map);
			}
			return;
		}

		logger.debug("OLD PEND SYSTEM " + map.getNameType() + " #" + map.getId());
		switch(map.getNameType()){
			case ACCOUNT: pendAccountUpdate((AccountType)map); break;
			case USER: pendUserUpdate((UserType)map); break;
			default:
				logger.error("Invalid NameIdType: " + map.getNameType());
				break;
		}
		
	}
	

	public static void pendUserUpdate(UserType user){
		rebuildUsers.put(user.getId(), user);
	}
	public static void pendAccountUpdate(AccountType account){
		rebuildAccounts.put(account.getId(), account);
	}
	public static  Map<NameEnumType,Map<NameEnumType,Map<Long,Map<Long,Map<Long,Boolean>>>>> getActorMap(){
		return actorMap;
	}
	public static Map<NameEnumType,Map<NameEnumType,AuthorizationMapType>> getObjectMap(){
		return objectMap;
	}
	public static String reportCacheSize(){
		StringBuilder buff = new StringBuilder(); 
		buff.append("Effective Authorization Cache Report\n");
		buff.append("Object Map\n");
		for(NameEnumType key : objectMap.keySet()){
			for(AuthorizationMapType aMap : objectMap.get(key).values()){
				buff.append(key.toString() + " " + aMap.getActor().toString() + "\t" + aMap.getMap().keySet().size() + "\n");
			}
		}
		buff.append("Actor Map\n");
		for(NameEnumType key : actorMap.keySet()) {
			for(NameEnumType key2 : actorMap.get(key).keySet()) {
				buff.append(key.toString() + " " +key2.toString() + "\t" + actorMap.get(key).get(key2).keySet().size() + "\n");
			}
		}
		return buff.toString();
	}
	public static void clearCache(){
		logger.debug("Clearing Authorization Cache");
		for(NameEnumType key : objectMap.keySet()){
			for(AuthorizationMapType aMap : objectMap.get(key).values()){
				aMap.getMap().clear();
			}
		}
		for(NameEnumType key : actorMap.keySet()){
			for(NameEnumType key2 : actorMap.get(key).keySet()) {
				actorMap.get(key).get(key2).clear();
			}
		}
		for(NameEnumType key : rebuildMap.keySet()){
			rebuildMap.put(key, new RebuildMap(key));
		}

		rebuildAccounts.clear();
		rebuildUsers.clear();

	}
	
	public static void clearCache(NameIdType object) throws ArgumentException{
		if(object == null) {
			logger.error("Object is null");
			return;
		}
		for(NameEnumType key : objectMap.keySet()) {
			if(objectMap.get(key).containsKey(object.getNameType())) {
				AuthorizationMapType aMap = objectMap.get(key).get(object.getNameType());
				clearPerCache(aMap.getMap(),key, object.getNameType(),object);
			}
		}
		if(objectMap.containsKey(object.getNameType())){
			for(AuthorizationMapType aMap : objectMap.get(object.getNameType()).values()){
				clearPerCache(aMap.getMap(),object.getNameType(),aMap.getObject(), object);
			}
		}
		
		if(rebuildMap.containsKey(object.getNameType())) rebuildMap.get(object.getNameType()).getMap().remove(object.getId());
		
		/// logger.debug("OLD CACHE SYSTEM: " + object.getNameType());

		switch(object.getNameType()){
			case USER:
				rebuildUsers.remove(object.getId());
				break;
			case ACCOUNT:
				rebuildAccounts.remove(object.getId());
				break;
			default:
				/// throw new ArgumentException("Invalid name type " + object.getNameType() + " for object " + object.getName() + " (" + object.getId() + ")");
				break;
		}
		
	}

	private static void clearPerCache(Map<Long,Map<Long,Map<Long,Boolean>>> map,NameEnumType actorType, NameEnumType objectType, NameIdType obj){
		
		if(actorType.equals(obj.getNameType())) {
			/// logger.info("Remove actor " + actorType + " " + obj.getId() + " " + map.containsKey(obj.getId()));
			map.remove(obj.getId());
			
		}
		if(objectType.equals(obj.getNameType())) {
			Iterator<Map<Long,Map<Long,Boolean>>> iter = map.values().iterator();
			while(iter.hasNext()){
				Map<Long,Map<Long,Boolean>> iMap = iter.next();
				/// logger.info("Remove object " + objectType + " " + obj.getId() + " " + iMap.containsKey(obj.getId()));
				iMap.remove(obj.getId());
			}		
		}
	}
	
	/*
	private static void clearCache(Map<Long,Map<Long,Boolean>> map,NameIdType obj){
		Iterator<Map<Long,Boolean>> iter = map.values().iterator();
		while(iter.hasNext()){
			iter.next().keySet().remove(obj.getId());
		}		
	}
	*/
	
	protected static boolean getCacheValue(NameIdType actor, BaseRoleType role){
		boolean outBool = false;
		switch(actor.getNameType()){
			case USER:
				outBool = getCacheValue((UserType)actor, role);
				break;
			case ACCOUNT:
				outBool = getCacheValue((AccountType)actor, role);
				break;
			case PERSON:
				outBool = getCacheValue((PersonType)actor, role);
				break;
			default:
				logger.error(String.format(FactoryException.UNHANDLED_ACTOR_TYPE, actor.getNameType().toString()));
				break;
		}
		return outBool;
		
	}
	
	/*
	private static boolean getCacheValue(Map<Long,Map<Long,Boolean>> map, NameIdType actor, NameIdType obj){
		if(map.containsKey(actor.getId()) && map.get(actor.getId()).containsKey(obj.getId())){
			return map.get(actor.getId()).get(obj.getId());
		}
		return false;
	}
	*/

	
	/// TODO - need to refactor this - its checking role membership, but is too generic, causing bloat elsewhere
	///
	protected static boolean hasCache(NameIdType actor, BaseRoleType role){
		boolean outBool = false;
		switch(actor.getNameType()){
			case USER:
			case ACCOUNT:
			case PERSON:
				outBool = hasCache(actor, role);
				break;
			default:
				logger.error(String.format(FactoryException.UNHANDLED_ACTOR_TYPE, actor.getNameType().toString()));
				break;
		}
		return outBool;
		
	}

	/*
	private static boolean hasCache(Map<Long,Map<Long,Boolean>> map, UserType user, NameIdType obj){
		if(map.containsKey(user.getId()) && map.get(user.getId()).containsKey(obj.getId())){
			return true;
		}
		return false;		
	}
	*/
	
	private static Map<Long,Map<Long,Map<Long,Boolean>>> getActorMap(NameEnumType actorType, NameEnumType objectType){
		if(objectMap.containsKey(objectType) && objectMap.get(objectType).containsKey(actorType)){
			return objectMap.get(objectType).get(actorType).getMap();
		}
		if(actorMap.containsKey(actorType) && actorMap.get(actorType).containsKey(objectType)){
			logger.warn("OLD OLD getActorMap for " + actorType + " " + objectType);
			return actorMap.get(actorType).get(objectType);
		}
		return null;
	}
	
	public static boolean hasAuthorizationMap(NameEnumType objectType, NameEnumType actorType) {
		return (getAuthorizationMap(objectType, actorType) != null);
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
		logger.warn("OLD OLD PER CACHE");
		Map<Long,Map<Long,Map<Long,Boolean>>> map = getActorMap(actor.getNameType(), object.getNameType());
		if(map != null) return hasPerCache(map, actor, object, permission);
		return false;
	}

	private static boolean getCacheValue(NameIdType actor, NameIdType obj, BasePermissionType[] permissions) {
		Map<Long,Map<Long,Map<Long,Boolean>>> map = null;
		AuthorizationMapType amap = getAuthorizationMap(actor.getNameType(), obj.getNameType());
		if(amap != null) map = amap.getMap();
		else {
			logger.warn("OLD OLD PER CACHE");
			map = getActorMap(actor.getNameType(), obj.getNameType());
		}
		if(map != null) return hasPerCache(map, actor, obj, permissions);
		return false;
	}
	private static boolean getPerCacheValue(Map<Long,Map<Long,Map<Long,Boolean>>> map, NameIdType actor, NameIdType obj, BasePermissionType[] permissions){
		boolean outBool = false;
		Map<Long,Boolean> pmap = getPerCache(map, actor, obj);
		if(pmap != null){
			for(int p = 0; p < permissions.length;p++){
				if(permissions[p] == null) continue;
				if(pmap.containsKey(permissions[p].getId())){
					outBool = pmap.get(permissions[p].getId());
					/// only break on a positive
					/// this is setup to work as an OR case when fed a list of permissions
					///
					if(outBool) break;
				}
			}
		}
		return outBool;		
	}
	private static Map<Long,Boolean> getPerCache(Map<Long,Map<Long,Map<Long,Boolean>>> map, NameIdType actor, NameIdType obj){
		Map<Long,Boolean> outMap = null;
		if(map.containsKey(actor.getId()) && map.get(actor.getId()).containsKey(obj.getId())){
			outMap = map.get(actor.getId()).get(obj.getId());
		}
		return outMap;		
	}
	private static boolean hasPerCache(Map<Long,Map<Long,Map<Long,Boolean>>> map, NameIdType actor, NameIdType obj, BasePermissionType[] permissions){
		boolean outBool = false;
		if(map == null){
			logger.error("Null map");
			return outBool;
		}
		if(actor == null || obj == null){
			logger.error("Null actor or target object");
			return outBool;
		}
		if(map.containsKey(actor.getId()) && map.get(actor.getId()).containsKey(obj.getId())){
			Map<Long,Boolean> pmap = map.get(actor.getId()).get(obj.getId());
			for(int p = 0; p < permissions.length;p++){
				if(permissions[p] == null) continue;
				if(pmap.containsKey(permissions[p].getId())){
					outBool = true;
					break;
				}
			}
		}
		return outBool;		
	}
	
	/*
	private static boolean hasCache(Map<Long,Map<Long,Boolean>> map, NameIdType actor, NameIdType obj){
		if(map.containsKey(actor.getId()) && map.get(actor.getId()).containsKey(obj.getId())){
			return true;
		}
		return false;		
	}
	*/

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
		logger.warn("OLD OLD PER CACHE");
		Map<Long,Map<Long,Map<Long,Boolean>>> map = getActorMap(actor.getNameType(), object.getNameType());
		if(map != null) addToPerCache(map,actor,object,permissions,val);
		
	}


	private static void addToPerCache(Map<Long,Map<Long,Map<Long,Boolean>>> map, NameIdType actor, NameIdType obj, BasePermissionType[] permissions, boolean val){
		if(!map.containsKey(actor.getId())) map.put(actor.getId(), new HashMap<Long,Map<Long,Boolean>>());
		if(!map.get(actor.getId()).containsKey(obj.getId())) map.get(actor.getId()).put(obj.getId(), new HashMap<Long,Boolean>());
		for(int p = 0;p < permissions.length;p++){
			if(permissions[p]==null) continue;
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
		StringBuilder buff = new StringBuilder();
		buff.append(member.getNameType().toString() + " " + member.getUrn() + " #" + member.getId() + " (");
		for(int i = 0; i < permissions.length;i++){
			if(i > 0) buff.append(", ");
			buff.append((permissions[i] == null ? "Null" : permissions[i].getName() + " #" + permissions[i].getId()));
		}
		buff.append(") " + object.getNameType().toString() + " " + object.getUrn() + " #" +object.getId());
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
		boolean outBool = false;
		boolean cache = (isMemberCacheable(member) && isObjectCacheable(object));
		String entChkStr = getEntitlementCheckString(object,member,permissions);
		if(cache && hasPerCache(member, object, permissions)){
			Map<Long,Map<Long,Map<Long,Boolean>>> map = getActorMap(member.getNameType(), object.getNameType());
			if(map != null){
				boolean cachedAuth = getPerCacheValue(map, member, object, permissions);
				logger.debug("*** CACHED AUTHORIZATION: " + entChkStr + " == " + cachedAuth);
				return cachedAuth;
			}
		}
		
		List<EntitlementType> ents = getEffectiveMemberEntitlements(object, member, permissions,false);
		logger.debug(entChkStr + " " + ents.size());
		if(!ents.isEmpty()){
			outBool = true;
		}
		/// Note: While this could be handled in the DB, I left it outside because I didn't want to have to fork to a different view or function
		/// that included unwinding the person dimension - there is a DB function that does this, but it doesn't scale beyond data and group types.
		///
		else if(member.getNameType() == NameEnumType.PERSON){
			PersonType person = (PersonType)member;
			try {
				Factories.getNameIdFactory(FactoryEnumType.PERSON).populate(person);
			} catch (FactoryException | ArgumentException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
			for(int i = 0; i < person.getAccounts().size();i++){
				if(getEntitlementsGrantAccess(object, person.getAccounts().get(i),permissions)){
					outBool = true;
					break;
				}
			}
		}
		if(cache){
			logger.debug("*** CACHING AUTHORIZATION: " + entChkStr + " == " + outBool);
			addToPerCache(member,object,permissions,outBool);
		}
		return outBool;
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
			if(permissions[i]==null)permissionIds[i] = 0L;
			else permissionIds[i] = permissions[i].getId();
		}
		return getEffectiveMemberEntitlements(object,member,permissionIds,exclusion);
	}
	public static EntitlementType copyAsEntitlement(NameIdType member, NameIdType object, NameIdType entitlement, long orgId) {
		EntitlementType ent = new EntitlementType();
		if(object != null) {
			ent.setObjectId(object.getId());
			ent.setObjectName(object.getName());
			ent.setObjectGuid(object.getObjectId());
			ent.setObjectType(object.getNameType());
		}
		if(member != null) {
			ent.setMemberId(member.getId());
			ent.setMemberGuid(member.getObjectId());
			ent.setMemberName(member.getName());
			ent.setMemberType(member.getNameType());
		}
		if(entitlement != null) {
			ent.setEntitlementId(entitlement.getId());
			ent.setEntitlementGuid(entitlement.getObjectId());
			ent.setEntitlementName(entitlement.getName());
			ent.setEntitlementType(entitlement.getNameType());
			ent.setEntitlementAffectType(AffectEnumType.GRANT_PERMISSION);
		} 
		ent.setOrganizationId(orgId);
		return ent;
		
	}
	public static List<EntitlementType> getEffectiveMemberEntitlements(NameIdType object, NameIdType member, Long[] permissionIds, boolean exclusion){
		List<EntitlementType> out_ents = new ArrayList<>();
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
		String.format("SELECT participationid as id, affectid, affecttype, referenceid, referencetype FROM ("
		+ "SELECT PP.participationid,PP.affectid,PP.affecttype,"
		+"	CASE WHEN GM.referenceid > 0 THEN GM.referenceid"
		+"	ELSE PP.participantid END as referenceid,"
		+"	CASE WHEN GM.referencetype <> '' THEN GM.referencetype"
		+"	ELSE PP.participanttype END as referencetype"
		+"	FROM %sparticipation PP"
		+"	LEFT JOIN group_membership(PP.participantid) GM ON (%s = '' OR GM.referencetype = %s) AND (%s = 0 OR GM.referenceid = %s)"
		+"	WHERE %s PP.participationid = %s AND (0 = cardinality(%s) OR PP.affectId = ANY(%s)) AND PP.participanttype = 'GROUP'"
		+"	UNION ALL"
		+"	SELECT PP.participationid,PP.affectid,PP.affecttype,"
		+"	CASE WHEN GM.referenceid > 0 THEN GM.referenceid"
		+"	ELSE PP.participantid END as referenceid,"
		+"	CASE WHEN GM.referencetype <> '' THEN GM.referencetype"
		+"	ELSE PP.participanttype END as referencetype"
		+"	FROM %sparticipation PP"
		+"	LEFT JOIN role_membership(PP.participantid) GM ON (%s = '' OR GM.referencetype = %s) AND (%s = 0 OR GM.referenceid = %s)"
		+"	WHERE %sPP.participationid = %s AND (0 = cardinality(%s) OR PP.affectId = ANY(%s)) AND PP.participanttype = 'ROLE'	"
		+"	UNION ALL"
		+"	SELECT PP.participationid,PP.affectid,PP.affecttype,"
		+"	PP.participantid as referenceid,PP.participanttype as referencetype"
		+"	FROM %sparticipation PP"
		+"	WHERE %sPP.participationid = %s"
		+"	AND (0 = cardinality(%s) OR PP.affectId = ANY(%s))"
		+"	AND NOT PP.participanttype IN('GROUP','ROLE')"
		+"	AND (%s = '' OR PP.participanttype = %s)"
		+"	AND (%s = 0 OR PP.participantid =%s)"
		+ ") DM WHERE (%s = '' OR referencetype = %s) AND (%s = 0 OR referenceid =%s) AND affectid > 0;"
		,object.getNameType().toString().toLowerCase(),token,token,token,token,(exclusion ? "NOT ":""),token,token,token
		,object.getNameType().toString().toLowerCase(),token,token,token,token,(exclusion ? "NOT ":""),token,token,token
		, object.getNameType().toString().toLowerCase(),(exclusion ? "NOT ":""),token,token,token,token,token,token,token
		,token,token,token,token
		);
		
		/*
		StringBuilder buff = new StringBuilder();
		for(int i = 0; i < permissionIds.length;i++){
			if(i > 0) buff.append(", ");
			buff.append(Long.toString(permissionIds[i]));
		}
		*/
		/// logger.info(sqlQuery);
		PreparedStatement statement = null;
		ResultSet rset = null;
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
			long startQuery = System.currentTimeMillis();
			statement = conn.prepareStatement(sqlQuery);
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
			/// logger.info(statement);
			rset = statement.executeQuery();
			while(rset.next()){
				EntitlementType ent = new EntitlementType();
				ent.setObjectId(objectId);
				ent.setObjectType(objectType);
				ent.setMemberId(rset.getLong(4));
				ent.setMemberType(NameEnumType.valueOf(rset.getString(5)));
				ent.setEntitlementId(rset.getLong(2));
				ent.setEntitlementAffectType(AffectEnumType.valueOf(rset.getString(3)));
				ent.setOrganizationId(object.getOrganizationId());
				out_ents.add(ent);
			}

			long stopQuery = System.currentTimeMillis();
			long diff = (stopQuery - startQuery);

			logger.debug("*** QUERY TIME: " + diff + "ms");
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		finally{
			try {
				if(rset != null) rset.close();
				if(statement != null) statement.close();
				conn.close();
			} catch (SQLException e) {
				
				logger.error(e.getMessage());
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return out_ents;
	}
	
	private static boolean getAuthorization(String tableName,String idColumnName,String matchColumnName, NameIdType actor, boolean requireReference, NameIdType obj, BasePermissionType[] permissions)  throws ArgumentException, FactoryException
	{

		boolean outBool = false;

		StringBuilder buff = new StringBuilder();
		for(int i = 0; i < permissions.length;i++){
			if(i > 0) buff.append(",");
			buff.append(permissions[i].getId());
		}

		Connection conn = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connType = DBFactory.getConnectionType(conn);
		String token = DBFactory.getParamToken(connType);
		PreparedStatement stat = null;
		ResultSet rset = null;
		boolean linkPerson = (requireReference && actor.getNameType() == NameEnumType.PERSON);
		StringBuilder sql = new StringBuilder();
		try{
			sql.append(String.format("SELECT distinct %s FROM %s WHERE %s = %s AND %s = %s AND affecttype = '%s' AND affectid IN (%s) and organizationid = %s %s"
				,idColumnName,tableName,idColumnName,token,matchColumnName,token,AffectEnumType.GRANT_PERMISSION.toString(), buff.toString(),token,(requireReference ? " and referencetype = " + token : "")));

			if(linkPerson){
				sql.append(String.format("UNION ALL SELECT distinct participantid FROM %s T2 "
						+ " inner join personparticipation PU on T2.referencetype = PU.participanttype AND PU.participantid = T2.referenceid "
						+ " WHERE PU.participationid = %s AND T2.%s = %s AND T2.affecttype = '%s' AND T2.affectid IN (%s) and T2.organizationid = %s and (T2.referencetype = 'ACCOUNT' or T2.referencetype = 'USER')"
				,tableName,token,matchColumnName,token,AffectEnumType.GRANT_PERMISSION.toString(), buff.toString(),token));
				logger.debug("Extending query to link person authorization check to direct user and child participants.");
			}
			
			stat = conn.prepareStatement(sql.toString());
			stat.setLong(1, actor.getId());
			stat.setLong(2,obj.getId());
			stat.setLong(3, obj.getOrganizationId());
			if(requireReference) stat.setString(4, actor.getNameType().toString());
			if(linkPerson){
				stat.setLong(5, actor.getId());
				stat.setLong(6, obj.getId());
				stat.setLong(7, obj.getOrganizationId());
			}
			rset = stat.executeQuery();
			if(rset.next()){
				long matchId = rset.getLong(1);
				outBool = true;
				logger.debug("Matched " + actor.getNameType() + " " + matchId + (linkPerson ? " (via person linkage)" : "") + " having at least one permission in (" + buff.toString() + ") for " + obj.getNameType() + " " + obj.getId() + " in org " + obj.getOrganizationId());

			}
			else{
				logger.debug("Did not match " + actor.getNameType() + " " + actor.getId() + " having at least one permission in (" + buff.toString() + ") for " + obj.getNameType() + " " + obj.getId() + " in org " + obj.getOrganizationId());
			}
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		finally{
			try {
				if(rset != null) rset.close();
				if(stat != null) stat.close();
				conn.close();
			} catch (SQLException e) {
				
				logger.error(e.getMessage());
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return outBool;
		
	}
	
	public static boolean getAuthorization(NameIdType actor, NameIdType object, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean outBool = false;
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
			AuthorizationMapType map = getAuthorizationMap(actor.getNameType(), object.getNameType());
			if(map != null){
				return getPerCacheValue(map.getMap(),actor,object,permissions);
			}
		}
		String objType = object.getNameType().toString().toLowerCase();
		if(actor.getNameType() == NameEnumType.ROLE){
			outBool = getAuthorization("effective" + objType + "roles","baseroleid",objType + "id",actor,false,object,permissions);
		}
		else{
			outBool = getAuthorization(objType + "rights","referenceid",objType + "id",actor,true,object,permissions);
		}
		addToPerCache(actor,object,permissions,outBool);
		return outBool;
		
	}
	
	public static boolean getIsActorInEffectiveRole(BaseRoleType role, NameIdType actor) throws ArgumentException, FactoryException
	{
		return getIsActorInEffectiveRole(role, actor, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsActorInEffectiveRole(BaseRoleType role, NameIdType actor, BasePermissionType permission, AffectEnumType affectType) throws ArgumentException, FactoryException
		{
		if(affectType != AffectEnumType.UNKNOWN) throw new ArgumentException("AffectType is not supported for checking role participation (at the moment)");
		
		if(hasPerCache(actor, role, new BasePermissionType[] {permission})) {
			return getCacheValue(actor,role,new BasePermissionType[] {permission});
		}
		/*
		if(hasCache(actor,role)){
			return getCacheValue(actor,role);
		}
		*/
		String prefix = null;
		boolean linkPerson = false;
		if(actor.getNameType() == NameEnumType.USER) prefix = "user";
		else if(actor.getNameType() == NameEnumType.ACCOUNT) prefix = "account";
		else if(actor.getNameType() == NameEnumType.PERSON){
			prefix = "person";
			linkPerson = true;
		}
		else throw new ArgumentException("Unexpected actor type " + actor.getNameType());
		
		boolean outBool = false;
		Connection conn = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connType = DBFactory.getConnectionType(conn);
		String token = DBFactory.getParamToken(connType);
		PreparedStatement stat = null;
		ResultSet rset = null;
		StringBuilder sql = new StringBuilder();
		try{
			sql.append(String.format("SELECT distinct objectid FROM %srolecache WHERE objectid = %s AND effectiveroleid = %s and organizationid = %s",prefix,token,token,token));
			if(linkPerson){
				sql.append(String.format(" UNION ALL "
					+ " SELECT distinct participantid FROM userrolecache URC "
					+ " inner join personparticipation PU on URC.objectid = PU.participantid AND PU.participanttype = 'USER' AND PU.participationid = %s AND URC.effectiveroleid = %s AND URC.organizationid = %s"
					+ " UNION ALL "
					+ " SELECT distinct participantid FROM accountrolecache ARC "
					+ " inner join personparticipation PU2 on ARC.objectid = PU2.participantid AND PU2.participanttype = 'ACCOUNT' AND PU2.participationid = %s AND ARC.effectiveroleid = %s AND ARC.organizationid = %s"
					,token,token,token,token,token,token));
			}
			stat = conn.prepareStatement(sql.toString());


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
			rset = stat.executeQuery();
			if(rset.next()){
				long matchId = rset.getLong(1);
				logger.debug("Matched on id " + matchId);
				/// NOTE: Although deny's are stored at the datalevel, they are not currently being evaluated here
				/// So a DENY will actually turn into a grant in the code
				///
				outBool = true;
			}
			else{
				logger.debug("Did not match " + actor.getNameType() + " " + actor.getName() + " (" + actor.getId() + ") with role " + role.getName() + " (" + role.getId() + ") in organization (" + role.getOrganizationId() + ")");
			}

			addToPerCache(actor,role,new BasePermissionType[] {permission}, outBool);
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		finally{
			try {
				if(rset != null) rset.close();
				if(stat != null) stat.close();
				conn.close();
			} catch (SQLException e) {
				
				logger.error(e.getMessage());
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return outBool;
		
	}
	
	public static List<BaseRoleType> getEffectiveRoles(NameIdType actor) throws ArgumentException, FactoryException
	{
		List<BaseRoleType> roles = new ArrayList<>();
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
		PreparedStatement stat = null;
		ResultSet rset = null;
		StringBuilder sql = new StringBuilder();
		try{
			sql.append(String.format("SELECT distinct effectiveroleid FROM %srolecache WHERE objectid = %s and organizationid = %s",prefix,token,token));
			if(linkPerson){
				sql.append(String.format(" UNION ALL "
					+ " SELECT distinct effectiveroleid FROM userrolecache URC "
					+ " inner join personparticipation PU on URC.objectid = PU.participantid AND PU.participanttype = 'USER' AND PU.participationid = %s AND URC.organizationid = %s"
					+ " UNION ALL "
					+ " SELECT distinct effectiveroleid FROM accountrolecache ARC "
					+ " inner join personparticipation PU2 on ARC.objectid = PU2.participantid AND PU2.participanttype = 'ACCOUNT' AND PU2.participationid = %s AND ARC.organizationid = %s"
					,token,token,token,token));
			}
			/// logger.info(sql.toString());
			stat = conn.prepareStatement(sql.toString());
	
			stat.setLong(1, actor.getId());
			stat.setLong(2, actor.getOrganizationId());
			if(linkPerson){
				stat.setLong(3, actor.getId());
				stat.setLong(4, actor.getOrganizationId());
				stat.setLong(5, actor.getId());
				stat.setLong(6, actor.getOrganizationId());
			}
			rset = stat.executeQuery();
			List<Long> ids = new ArrayList<>();
			while(rset.next()){
				ids.add(rset.getLong(1));
			}

			roles = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).listByIds(ArrayUtils.toPrimitive(ids.toArray(new Long[0])), actor.getOrganizationId());
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		finally{
			try {
				if(rset != null) rset.close();
				if(stat != null) stat.close();
				conn.close();
			} catch (SQLException e) {
				
				logger.error(e.getMessage());
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return roles;
		
	}
	public static boolean hasPendingEntries(){
		logger.warn("***** TODO: Refactor report of pending entries");
		return false;
		////return (rebuildPersons.size() > 0 || rebuildAccounts.size() > 0 || rebuildUsers.size() > 0 || rebuildRoles.size() > 0 || rebuildGroups.size() > 0 || rebuildData.size() > 0);
	}
	public static boolean rebuildPendingRoleCache() throws FactoryException, ArgumentException{
		boolean outBool = false;
		long start = System.currentTimeMillis();
		UserType user = null;
		AccountType account = null;
		PersonType person = null;
		BaseRoleType role = null;
		BaseGroupType group = null;
		
		Map<Long,NameIdType> rebuildData = new HashMap<>();
		Map<Long,NameIdType> rebuildRoles = new HashMap<>();
		Map<Long,NameIdType> rebuildPersons = new HashMap<>();
		Map<Long,NameIdType> rebuildGroups = new HashMap<>();

		synchronized(lockObject){
			

			for(NameEnumType ntype : rebuildMap.keySet().toArray(new NameEnumType[0])) {
				RebuildMap rb = rebuildMap.get(ntype);

				FactoryEnumType factType = FactoryEnumType.fromValue(ntype.toString());
				if(!AuthorizationService.getAuthorizationFactories().containsKey(factType)) {
					logger.error("Factory type '" + ntype.toString()  + "' is not registered for authorization");
					continue;
				}
				
				/// ParticipationFactory pFact = Factories.getFactory(AuthorizationService.getAuthorizationFactories().get(factType));
				
				if(ntype.equals(NameEnumType.ROLE)) rebuildRoles = rb.getMap();
				else if(ntype.equals(NameEnumType.USER)) rebuildUsers.putAll(rb.getMap());
				else if(ntype.equals(NameEnumType.GROUP)) rebuildGroups = rb.getMap();
				else if(ntype.equals(NameEnumType.ACCOUNT)) rebuildAccounts.putAll(rb.getMap());
				else if(ntype.equals(NameEnumType.PERSON)) rebuildPersons = rb.getMap();
				else if(ntype.equals(NameEnumType.DATA)) rebuildData = rb.getMap();
				else {
					List<NameIdType> ids = new ArrayList<>();
					for(Long l : rb.getMap().keySet()) {
						NameIdType nt = rb.getMap().get(l);
						ids.add(nt);
					}
					if(ids.size() > 0) {
						/// logger.info("***** NEW PEND REBUILD: " + ntype.toString() + " : " + rb.getMap().size());
						String funcName = "cache_" + ntype.toString().toLowerCase() + "_roles";
						/// logger.info("Rebuild with " + funcName);
						rebuildRoleCache(funcName,ids,0L);
					}
				}
			}
			
			logger.debug("***** LEGACY PEND REBUILD");
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
					List<UserType> rusers = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getUsersInRole((UserRoleType)role);
					for(int r = 0; r < rusers.size();r++){
						user = rusers.get(r);
						if(rebuildUsers.containsKey(user.getId())==false){
							rebuildUsers.put(user.getId(),user);
						}
					}
				}
				else if(role.getRoleType() == RoleEnumType.ACCOUNT){
					List<AccountType> raccounts = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getAccountsInRole((AccountRoleType)role);
					for(int r = 0; r < raccounts.size();r++){
						account = raccounts.get(r);
						if(rebuildAccounts.containsKey(account.getId())==false){
							rebuildAccounts.put(account.getId(),account);
						}
					}
				}
				else if(role.getRoleType() == RoleEnumType.PERSON){
					List<PersonType> rpersons = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getPersonsInRole((PersonRoleType)role);
					for(int r = 0; r < rpersons.size();r++){
						person = rpersons.get(r);
						if(rebuildPersons.containsKey(person.getId())==false){
							rebuildPersons.put(person.getId(),person);
						}
					}
				}
				clearCache(roles.get(i));
			}
			
			if(!roles.isEmpty()){
				logger.debug("Rebuilding role cache for " + roles.size() + " roles");
				rebuildRoleCache(roles,roles.get(0).getOrganizationId());
				rebuildRoles.clear();
			}

			List<BaseGroupType> groups = Arrays.asList(rebuildGroups.values().toArray(new BaseGroupType[0]));
			for(int i = 0; i < groups.size();i++){
				group = groups.get(i);
				/// negative id indicates possible bulk entry
				/// 
				if(group.getId() < 0L){
					logger.error("Group BulkEntry with id " + group.getId() + " detected.  The Bulk Session must be written before rebuilding the cache.");
					throw new ArgumentException("Group BulkEntry with id " + group.getId() + " detected.  The Bulk Session must be written before rebuilding the cache.");
				}
				if(group.getGroupType() == GroupEnumType.USER){
					List<UserType> gusers = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getUsersInGroup((UserGroupType)group);
					for(int g = 0; g < gusers.size();g++){
						if(!rebuildUsers.containsKey(gusers.get(g).getId())){
							rebuildUsers.put(gusers.get(g).getId(), gusers.get(g));
						}
					}
				}
				else if(group.getGroupType() == GroupEnumType.ACCOUNT){
					List<AccountType> gaccounts = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getAccountsInGroup((AccountGroupType)group);
					for(int g = 0; g < gaccounts.size();g++){
						if(!rebuildAccounts.containsKey(gaccounts.get(g).getId())){
							rebuildAccounts.put(gaccounts.get(g).getId(), gaccounts.get(g));
						}
					}
				}
				else if(group.getGroupType() == GroupEnumType.PERSON){
					List<PersonType> gpersons = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getPersonsInGroup((PersonGroupType)group);
					for(int g = 0; g < gpersons.size();g++){
						if(!rebuildPersons.containsKey(gpersons.get(g).getId())){
							rebuildPersons.put(gpersons.get(g).getId(), gpersons.get(g));
						}
					}
				}

				clearCache(groups.get(i));
			}
			
			if(!groups.isEmpty()){
				logger.debug("Rebuilding role cache for " + groups.size() + " groups");
				outBool = rebuildRoleCache(groups,groups.get(0).getOrganizationId());
				rebuildGroups.clear();
			}
				
			
			List<UserType> users = Arrays.asList(rebuildUsers.values().toArray(new UserType[0]));
			
			if(!users.isEmpty()){
				logger.debug("Rebuilding role cache for " + users.size() + " users");
				outBool = rebuildRoleCache(users,users.get(0).getOrganizationId());
				rebuildUsers.clear();
				for(UserType u2 : users) clearCache(u2);
			}
			
			List<AccountType> accounts = Arrays.asList(rebuildAccounts.values().toArray(new AccountType[0]));
			
			if(!accounts.isEmpty()){
				logger.debug("Rebuilding role cache for " + accounts.size() + " accounts");
				outBool = rebuildRoleCache(accounts,accounts.get(0).getOrganizationId());
				rebuildAccounts.clear();
				for(AccountType a2 : accounts) clearCache(a2);
			}

			List<PersonType> persons = Arrays.asList(rebuildPersons.values().toArray(new PersonType[0]));
			if(!persons.isEmpty()){
				logger.debug("Rebuilding role cache for " + persons.size() + " persons");
				outBool = rebuildRoleCache(persons,persons.get(0).getOrganizationId());
				rebuildPersons.clear();
				for(PersonType p2 : persons) clearCache(p2);
			}
			
			List<DataType> data = Arrays.asList(rebuildData.values().toArray(new DataType[0]));
			if(!data.isEmpty()){
				logger.debug("Rebuilding role cache for " + data.size() + " data");
				outBool = rebuildRoleCache(data,data.get(0).getOrganizationId());
				rebuildData.clear();
				for(DataType d2 : data) clearCache(d2);
			}
		}
		long stop = System.currentTimeMillis();
		if(outBool) logger.info("Rebuilt role cache in " + (stop - start) + " ms");
		else logger.info("Did not rebuild role cache in " + (stop - start) + " ms");
		return outBool;
		
	}
	public static boolean rebuildCache(NameEnumType objectType, long organizationId){
		if(objectMap.containsKey(objectType)){
			String functionName = "cache_all_" + objectType.toString().toLowerCase() + "_roles";
			return rebuildRoleCache(functionName,organizationId);
		}
		return false;

	}
	public static boolean rebuildRoleCache(NameIdType object) throws ArgumentException{
		return rebuildRoleCache(Arrays.asList(object),object.getOrganizationId());
	}
	public static <T> boolean rebuildRoleCache(List<T> objects, long organizationId) throws ArgumentException{
		boolean outBool = false;
		NameIdType object = (NameIdType)(!objects.isEmpty() ? objects.get(0) : null);
		if(object != null && 
				/// 2019/11/20 - Move the last two object types from the old authZ cache system into an exception case
				(
						objectMap.containsKey(object.getNameType())
						||
						object.getNameType().equals(NameEnumType.ACCOUNT)
						||
						object.getNameType().equals(NameEnumType.USER)
				)
		){
			String functionName = "cache_" + object.getNameType().toString().toLowerCase() + "_roles";
			return rebuildRoleCache(functionName, objects, organizationId);
		}
		else{
			logger.error("Object type " + (object == null ? "[null]" : object.getNameType().toString()) + " not registered or object is null");
		}
		return outBool;
	}

	private static <T> boolean rebuildRoleCache(String functionName, List<T> objects,long organizationId) throws ArgumentException{

		Connection conn = ConnectionFactory.getInstance().getConnection();
		int maxIn = maximum_insert_size;
		List<StringBuilder> buffs = new ArrayList<>();
		StringBuilder uBuff = new StringBuilder();
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
				uBuff = new StringBuilder();
			}
		}
		if(uBuff.length() > 0) buffs.add(uBuff);
		int updated = 0;
		Statement stat = null;
		ResultSet rset = null;
		try{
			stat = conn.createStatement();
			
			for(int i = 0; i < buffs.size();i++){
				String sql = String.format("SELECT %s(ARRAY[%s],%s)",functionName,buffs.get(i).toString(),organizationId);
				if(rset != null) rset.close();
				rset = stat.executeQuery(sql);
				if(rset.next()){
					updated++;
				}
				rset.close();

			}
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		finally{
			try {
				if(stat != null) stat.close();
				conn.close();
			} catch (SQLException e) {
				
				logger.error(e.getMessage());
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		logger.debug("Rebuilt role cache with " + updated + " operations");
		return (updated > 0);
	}
	
	public static boolean rebuildRoleCache(long organizationId){
		return (
				rebuildRoleCache(NameEnumType.GROUP,organizationId)
				&&
				rebuildRoleCache(NameEnumType.ROLE,organizationId)
				&&
				rebuildRoleCache(NameEnumType.DATA,organizationId)
				&&
				rebuildRoleCache(NameEnumType.USER,organizationId)
				&&
				rebuildRoleCache(NameEnumType.ACCOUNT,organizationId)
				&&
				rebuildRoleCache(NameEnumType.PERSON,organizationId)
			);
	}
	public static boolean rebuildRoleCache(NameEnumType nType, long organizationId){
		return rebuildRoleCache("cache_all_" + nType.toString().toLowerCase() + "_roles", organizationId);
	}
	private static boolean rebuildRoleCache(String functionName,long organizationId){
		boolean outBool = false;
		Connection conn = ConnectionFactory.getInstance().getConnection();

		Statement stat = null;
		ResultSet rset = null;
		try{
			stat = conn.createStatement();
			rset = stat.executeQuery(String.format("SELECT %s(%s);",functionName,organizationId));
			if(rset.next()){
				outBool = true;
				clearCache();
			}
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		}
		finally{
			try {
				if(rset != null) rset.close();
				if(stat != null) stat.close();
				conn.close();
			} catch (SQLException e) {
				
				logger.error(e.getMessage());
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		return outBool;
	}
	public static boolean rebuildCache(){
		boolean outBool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		Statement stat = null;
		try {
			stat = connection.createStatement();
			stat.executeQuery("SELECT * FROM cache_roles();");
			outBool = true;
		} catch (SQLException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		finally{
			try {
				if(stat != null) stat.close();
				connection.close();
			} catch (SQLException e) {
				logger.error(e);
			}
		}
		
		return outBool;
	}
	
}
class RebuildMap{
	private NameEnumType objectType = NameEnumType.UNKNOWN;
	private Map<Long,NameIdType> map = new HashMap<>();
	public RebuildMap(NameEnumType oType){
		objectType = oType;
	}
	public NameEnumType getObjectType() {
		return objectType;
	}
	public Map<Long, NameIdType> getMap() {
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
