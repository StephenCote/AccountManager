package org.cote.accountmanager.data.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.query.QueryField;

import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;



/*
 
 The Effective Authorization Service leverages the indirect maps and caches created or stored in the following database tables, views, and functions:
 view groupUserRights - direct group<-user permission assignments
 view dataUserRights - direct data<-user permission assignments
 function roles_from_leaf(id,orgId) - computes a role hierarchy up from a given node.  Permissions accumulate up (per RBAC spec)
 ALT: function roles_to_leaf(id,org) - computes a role hierarchy down from a given node. Permissions accumulate down (inverse spec)
 view effectiveUserRoles - computed permission<->role distribution, following the role hierarchy and mapping to users or groups of users
 function cache_user_roles(id,orgId) - caches the result of effectiveUserRoles for a specified user into the userrolecache table
 view effectiveGroupRoleRights - maps userrolecache to groupparticipation to yield all effective permissions by group id by user
 view effectiveDataRoleRights - maps userrolecache to dataparticipation to yield all effective permissions by data id by user
 view groupRights - Union of effectiveGroupRoleRights and groupUserRights.  Can be pretty efficient when used with a userid and organization id
 view dataRights - Union of effectiveDataRoleRights and dataUserRights.  Can be pretty efficient when used with a userid and organization id
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
	private static Map<Long,UserType> rebuildUsers = new HashMap<Long,UserType>();
	/// userRoleMap - caches users who participate in a role
	private static Map<Long,Map<Long,Boolean>> userRoleMap = new HashMap<Long,Map<Long,Boolean>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> userGroupMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> userDataMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> roleRoleMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> roleGroupMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	private static Map<Long,Map<Long,Map<Long,Boolean>>> roleDataMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();

	/// userRolePerMap - caches users with explicit rights to alter roles
	///
	private static Map<Long,Map<Long,Map<Long,Boolean>>> userRolePerMap = new HashMap<Long,Map<Long,Map<Long,Boolean>>>();
	private static Map<Long,BaseGroupType> rebuildGroups = new HashMap<Long,BaseGroupType>();
	private static Map<Long,UserRoleType> rebuildRoles = new HashMap<Long,UserRoleType>();
	private static Map<Long,DataType> rebuildData = new HashMap<Long,DataType>();
	
	public static void pendUserRoleUpdate(UserRoleType role){
		rebuildRoles.put(role.getId(), role);
	}

	public static void pendDataRoleUpdate(DataType data){
		rebuildData.put(data.getId(), data);
	}

	
	public static void pendUserUpdate(UserType user){
		rebuildUsers.put(user.getId(), user);
	}

	public static void pendGroupUpdate(BaseGroupType group){
		rebuildGroups.put(group.getId(), group);
	}

	
	public static void clearCache(){
		userRoleMap.clear();
		userRolePerMap.clear();
		userGroupMap.clear();
		userDataMap.clear();
		roleRoleMap.clear();
		roleGroupMap.clear();
		roleDataMap.clear();
		rebuildUsers.clear();
		rebuildGroups.clear();
		rebuildRoles.clear();
		rebuildData.clear();
	}
	public static void clearCache(NameIdType object) throws ArgumentException{
		switch(object.getNameType()){
			case GROUP:
				BaseGroupType group = (BaseGroupType)object;
				if(group.getGroupType() == GroupEnumType.DATA){
					clearPerCache(userGroupMap, group);
					clearPerCache(roleGroupMap, group);
				}
				rebuildGroups.remove(group.getId());
			case DATA:
				DataType data = (DataType)object;
				clearPerCache(userDataMap, data);
				clearPerCache(roleDataMap, data);
				rebuildData.remove(data.getId());
				break;
			case USER:
				UserType user = (UserType)object;
				userRoleMap.keySet().remove(user.getId());
				rebuildUsers.remove(user.getId());
				break;
			case ROLE:
				BaseRoleType role = (BaseRoleType)object;
				rebuildRoles.remove(role.getId());
				clearCache(userRoleMap, role);
				clearPerCache(userRolePerMap, role);
				clearPerCache(roleRoleMap,role);
				break;
			default:
				throw new ArgumentException("Invalid name type for object " + object.getName() + " (" + object.getId() + ")");
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
	protected static boolean getCacheValue(UserType user, BaseRoleType role){
		return getCacheValue(userRoleMap, user, role);
	}
	private static boolean getCacheValue(Map<Long,Map<Long,Boolean>> map, UserType user, NameIdType obj){
		if(map.containsKey(user.getId()) && map.get(user.getId()).containsKey(obj.getId())){
			return map.get(user.getId()).get(obj.getId());
		}
		return false;
	}
	protected static boolean hasCache(UserType user, BaseRoleType role, BasePermissionType[] permissions){
		return hasPerCache(userRolePerMap, user, role, permissions);
	}
	protected static boolean hasCache(UserType user, BaseGroupType group, BasePermissionType[] permissions){
		return hasPerCache(userGroupMap, user, group, permissions);
	}
	protected static boolean hasCache(UserType user, DataType data, BasePermissionType[] permissions){
		return hasPerCache(userDataMap, user, data, permissions);
	}
	protected static boolean hasCache(BaseRoleType actor, BaseRoleType role, BasePermissionType[] permissions){
		return hasPerCache(roleRoleMap, actor, role, permissions);
	}
	protected static boolean hasCache(BaseRoleType actor, BaseGroupType group, BasePermissionType[] permissions){
		return hasPerCache(roleGroupMap, actor, group, permissions);
	}
	protected static boolean hasCache(BaseRoleType actor, DataType data, BasePermissionType[] permissions){
		return hasPerCache(roleDataMap, actor, data, permissions);
	}
	private static boolean getPerCacheValue(Map<Long,Map<Long,Map<Long,Boolean>>> map, NameIdType actor, NameIdType obj, BasePermissionType[] permissions){
		boolean out_bool = false;
		if(map.containsKey(actor.getId()) && map.get(actor.getId()).containsKey(obj.getId())){
			Map<Long,Boolean> pmap = map.get(actor.getId()).get(obj.getId());
			for(int p = 0; p < permissions.length;p++){
				if(pmap.containsKey(permissions[p].getId())){
					out_bool = pmap.get(permissions[p].getId());
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
		if(map.containsKey(actor.getId()) && map.get(actor.getId()).containsKey(obj.getId())){
			Map<Long,Boolean> pmap = map.get(actor.getId()).get(obj.getId());
			for(int p = 0; p < permissions.length;p++){
				if(pmap.containsKey(permissions[p].getId())){
					out_bool = true;
					//out_bool = pmap.get(permissions[p].getId());
					break;
				}
			}
		}
		return out_bool;		
	}
	protected static boolean hasCache(UserType user, BaseRoleType role){
		return hasCache(userRoleMap, user, role);
	}
	private static boolean hasCache(Map<Long,Map<Long,Boolean>> map, UserType user, NameIdType obj){
		if(map.containsKey(user.getId()) && map.get(user.getId()).containsKey(obj.getId())){
			return true;///map.get(user.getId()).get(role.getId());
		}
		return false;		
	}
	protected static void addToCache(UserType user, BaseRoleType role, boolean val){
		addToCache(userRoleMap, user, role, val);
	}
	protected static void addToCache(UserType user, BaseGroupType group, BasePermissionType[] permissions, boolean val){
		addToPerCache(userGroupMap, user, group, permissions, val);
	}
	protected static void addToCache(UserType user, BaseRoleType role, BasePermissionType[] permissions, boolean val){
		addToPerCache(userRolePerMap, user, role, permissions, val);
	}
	protected static void addToCache(UserType user, DataType data, BasePermissionType[] permissions, boolean val){
		addToPerCache(userDataMap, user, data, permissions, val);
	}
	protected static void addToCache(BaseRoleType actor,BaseGroupType group, BasePermissionType[] permissions, boolean val){
		addToPerCache(roleGroupMap, actor, group, permissions, val);
	}
	protected static void addToCache(BaseRoleType actor,BaseRoleType role, BasePermissionType[] permissions, boolean val){
		addToPerCache(roleRoleMap, actor, role, permissions, val);
	}
	protected static void addToCache(BaseRoleType actor,DataType data, BasePermissionType[] permissions, boolean val){
		addToPerCache(roleDataMap, actor, data, permissions, val);
	}

	private static void addToCache(Map<Long,Map<Long,Boolean>> map, UserType user, NameIdType obj, boolean val){
		if(map.containsKey(user.getId()) == false) map.put(user.getId(), new HashMap<Long,Boolean>());
		map.get(user.getId()).put(obj.getId(),val);
		
	}
	private static void addToPerCache(Map<Long,Map<Long,Map<Long,Boolean>>> map, NameIdType actor, NameIdType obj, BasePermissionType[] permissions, boolean val){
		if(map.containsKey(actor.getId()) == false) map.put(actor.getId(), new HashMap<Long,Map<Long,Boolean>>());
		if(map.get(actor.getId()).containsKey(obj.getId())==false) map.get(actor.getId()).put(obj.getId(), new HashMap<Long,Boolean>());
		for(int p = 0;p < permissions.length;p++){
			Map<Long,Boolean> pmap = map.get(actor.getId()).get(obj.getId());
			pmap.put(permissions[p].getId(),val);
		}
		
	}
	private static boolean getAuthorization(String tableName,String idColumnName,String matchColumnName, NameIdType actor, NameIdType obj, BasePermissionType[] permissions)  throws ArgumentException, FactoryException
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
		try{
			PreparedStatement stat = conn.prepareStatement("SELECT distinct " + idColumnName + " FROM " + tableName + " WHERE " + idColumnName + " = " + token + " AND " + matchColumnName + " = " + token + " AND affecttype = '" + AffectEnumType.GRANT_PERMISSION.toString() + "' AND affectid IN (" + buff.toString() + ") and organizationid = " + token);
			stat.setLong(1, actor.getId());
			stat.setLong(2,obj.getId());
			stat.setLong(3, obj.getOrganization().getId());
			ResultSet rset = stat.executeQuery();
			if(rset.next()){
				long match_id = rset.getLong(1);
				//addToCache(user,role);
				out_bool = true;
				logger.debug("Matched " + actor.getNameType() + " " + match_id + " having at least one permission in (" + buff.toString() + ") for " + obj.getNameType() + " " + obj.getId() + " in org " + obj.getOrganization().getId());

			}
			else{
				logger.debug("Did not match " + actor.getNameType() + " " + actor.getId() + " having at least one permission in (" + buff.toString() + ") for " + obj.getNameType() + " " + obj.getId() + " in org " + obj.getOrganization().getId());
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
	private static boolean REFACTORgetUserAuthorization(String tableName,String columnName, UserType user, NameIdType obj, BasePermissionType[] permissions)  throws ArgumentException, FactoryException
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
		try{
			PreparedStatement stat = conn.prepareStatement("SELECT distinct userid FROM " + tableName + " WHERE userid = " + token + " AND " + columnName + " = " + token + " AND affecttype = '" + AffectEnumType.GRANT_PERMISSION.toString() + "' AND affectid IN (" + buff.toString() + ") and organizationid = " + token);
			stat.setLong(1, user.getId());
			stat.setLong(2,obj.getId());
			stat.setLong(3, obj.getOrganization().getId());
			ResultSet rset = stat.executeQuery();
			if(rset.next()){
				long match_id = rset.getLong(1);
				//addToCache(user,role);
				out_bool = true;
				logger.debug("Matched " + match_id + " having at least one permission in (" + buff.toString() + ") for data " + obj.getId() + " in org " + obj.getOrganization().getId());

			}
			else{
				logger.debug("Did not match " + user.getId() + " having at least one permission in (" + buff.toString() + ") for data " + obj.getId() + " in org " + obj.getOrganization().getId());
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
	public static boolean getDataAuthorization(UserType user, DataType data, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasCache(user, data, permissions)){
			logger.debug("Cached match " + user.getNameType() + " " + user.getId() + " checking data " + data.getId() + " in org " + data.getOrganization().getId());
			return getCacheValue(user,data,permissions);

		}

		out_bool = getAuthorization("datarights","userid","dataid",user,data,permissions);
		addToCache(user,data,permissions,out_bool);
		return out_bool;
		
	}

	public static boolean getRoleAuthorization(UserType user, BaseRoleType role, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasCache(user, role, permissions)){
			logger.debug("Cached match " + user.getNameType() + " " + user.getId() + " checking role " + role.getId() + " in org " + role.getOrganization().getId());
			return getCacheValue(user,role,permissions);

		}

		out_bool = getAuthorization("rolerights","userid","roleid",user,role,permissions);
		addToCache(user,role,permissions,out_bool);
		return out_bool;
		
	}
	public static boolean getGroupAuthorization(UserType user, BaseGroupType group, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasCache(user, group, permissions)){
			logger.debug("Cached match " + user.getNameType() + " " + user.getId() + " checking group " + group.getId() + " in org " + group.getOrganization().getId());
			return getCacheValue(user,group,permissions);

		}

		out_bool = getAuthorization("grouprights","userid","groupid",user,group,permissions);
		addToCache(user,group,permissions,out_bool);
		return out_bool;
		
	}
	
	public static boolean getDataAuthorization(BaseRoleType actor, DataType data, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasCache(actor, data, permissions)){
			logger.debug("Cached match " + actor.getNameType() + " " + actor.getId() + " checking data " + data.getId() + " in org " + data.getOrganization().getId());
			return getCacheValue(actor,data,permissions);

		}

		out_bool = getAuthorization("effectivedataroles","effectiveroleid","dataid",actor,data,permissions);
		addToCache(actor,data,permissions,out_bool);
		return out_bool;
		
	}

	public static boolean getRoleAuthorization(BaseRoleType actor, BaseRoleType role, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasCache(actor, role, permissions)){
			logger.debug("Cached match " + actor.getNameType() + " " + actor.getId() + " checking role " + role.getId() + " in org " + role.getOrganization().getId());
			return getCacheValue(actor,role,permissions);

		}

		out_bool = getAuthorization("effectiveroleroles","effectiveroleid","roleid",actor,role,permissions);
		addToCache(actor,role,permissions,out_bool);
		return out_bool;
		
	}
	public static boolean getGroupAuthorization(BaseRoleType actor, BaseGroupType group, BasePermissionType[] permissions) throws ArgumentException, FactoryException
	{
		boolean out_bool = false;
		if(permissions.length == 0) throw new ArgumentException("At least one permission must be specified");
		
		if(hasCache(actor, group, permissions)){
			logger.debug("Cached match " + actor.getNameType() + " " + actor.getId() + " checking group " + group.getId() + " in org " + group.getOrganization().getId());
			return getCacheValue(actor,group,permissions);

		}

		out_bool = getAuthorization("effectivegrouproles","effectiveroleid","groupid",actor,group,permissions);
		addToCache(actor,group,permissions,out_bool);
		return out_bool;
		
	}
	
	public static boolean getIsUserInEffectiveRole(BaseRoleType role, UserType user) throws ArgumentException, FactoryException
	{
		return getIsUserInEffectiveRole(role, user, null, AffectEnumType.UNKNOWN);
	}	
	public static boolean getIsUserInEffectiveRole(BaseRoleType role, UserType user, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		if(affect_type != AffectEnumType.UNKNOWN) throw new ArgumentException("AffectType is not supported for checking role participation (at the moment)");
		if(hasCache(user,role)){
			logger.debug("Cached match " + user.getNameType() + " " + user.getId() + " checking role " + role.getId() + " in org " + role.getOrganization().getId());
			return getCacheValue(user,role);
		}
		boolean out_bool = false;
		Connection conn = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connType = DBFactory.getConnectionType(conn);
		String token = DBFactory.getParamToken(connType);
		try{
			PreparedStatement stat = conn.prepareStatement("SELECT distinct userid FROM userrolecache WHERE userid = " + token + " AND effectiveroleid = " + token + " and organizationid = " + token);
			stat.setLong(1, user.getId());
			stat.setLong(2,role.getId());
			stat.setLong(3, role.getOrganization().getId());
			ResultSet rset = stat.executeQuery();
			if(rset.next()){
				long match_id = rset.getLong(1);
				addToCache(user,role,true);
				out_bool = true;
				logger.debug("Matched " + user.getNameType() + " " + match_id + " having role " + role.getId() + " in org " + role.getOrganization().getId());
			}
			else{
				addToCache(user,role,false);
				logger.warn("Failed to match " + user.getNameType() + " " + user.getName() + " (" + user.getId() + ") with role " + role.getName() + " (" + role.getId() + ") in organization (" + role.getOrganization().getId() + ")");
				/*
				StackTraceElement[] stack = Thread.currentThread().getStackTrace();
				for(int i = 0; i < stack.length;i++){
					logger.info(stack[i].toString());
				}
				*/
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
	public static boolean hasPendingEntries(){
		return (rebuildUsers.size() > 0 || rebuildRoles.size() > 0 || rebuildGroups.size() > 0 || rebuildData.size() > 0);
	}
	public static boolean rebuildPendingRoleCache() throws FactoryException, ArgumentException{
		boolean out_bool = false;
		long start = System.currentTimeMillis();
		UserType user = null;
		UserRoleType role = null;
		BaseGroupType group = null;
		synchronized(rebuildUsers){
			synchronized(rebuildRoles){
				List<UserRoleType> roles = Arrays.asList(rebuildRoles.values().toArray(new UserRoleType[0]));
				for(int i = 0; i < roles.size();i++){
					role =roles.get(i);
					/// negative id indicates possible bulk entry
					/// 
					if(role.getId() < 0){
						throw new ArgumentException("Role BulkEntry with id " + role.getId() + " detected.  The Bulk Session must be written before rebuilding the cache.");
					}
					List<UserType> rusers = Factories.getRoleParticipationFactory().getUsersInRole(role);
					for(int r = 0; r < rusers.size();r++){
						user = rusers.get(r);
						if(rebuildUsers.containsKey(user.getId())==false){
							rebuildUsers.put(user.getId(),user);
						}
					}
					clearCache(roles.get(i));
				}
				logger.info("Rebuilding role cache for " + roles.size() + " roles");
				if(roles.size() > 0){
					rebuildRoleRoleCache(roles,roles.get(0).getOrganization());
					rebuildGroups.clear();
				}
				rebuildRoles.clear();
			}
			synchronized(rebuildGroups){
				List<BaseGroupType> groups = Arrays.asList(rebuildGroups.values().toArray(new BaseGroupType[0]));
				for(int i = 0; i < groups.size();i++){
					group = groups.get(i);
					/// negative id indicates possible bulk entry
					/// 
					if(group.getId() < 0){
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
					clearCache(groups.get(i));
				}
				logger.info("Rebuilding role cache for " + groups.size() + " groups");
				if(groups.size() > 0){
					rebuildGroupRoleCache(groups,groups.get(0).getOrganization());
					rebuildGroups.clear();
				}
				
			}
			List<UserType> users = Arrays.asList(rebuildUsers.values().toArray(new UserType[0]));
			logger.info("Rebuilding role cache for " + users.size() + " users");
			if(users.size() > 0){
				out_bool = rebuildUserRoleCache(users,users.get(0).getOrganization());
				rebuildUsers.clear();
			}
			List<DataType> data = Arrays.asList(rebuildData.values().toArray(new DataType[0]));
			logger.info("Rebuilding role cache for " + data.size() + " data");
			if(data.size() > 0){
				out_bool = rebuildDataRoleCache(data,data.get(0).getOrganization());
				rebuildData.clear();
			}
		}
		long stop = System.currentTimeMillis();
		if(out_bool) logger.info("Time to rebuild cache: " + (stop - start) + " ms");
		else logger.info("Did not rebuild cache: " + (stop - start) + " ms");
		return out_bool;
		
	}
	public static boolean rebuildGroupRoleCache(BaseGroupType user) throws ArgumentException{
		return rebuildRoleCache("cache_group_roles",Arrays.asList(user),user.getOrganization());
	}
	public static boolean rebuildGroupRoleCache(List<BaseGroupType> users, OrganizationType org) throws ArgumentException{
		return rebuildRoleCache("cache_group_roles",users,org);
	}
	public static boolean rebuildDataRoleCache(DataType data) throws ArgumentException{
		return rebuildRoleCache("cache_data_roles",Arrays.asList(data),data.getOrganization());
	}
	public static boolean rebuildDataRoleCache(List<DataType> users, OrganizationType org) throws ArgumentException{
		return rebuildRoleCache("cache_data_roles",users,org);
	}
	public static boolean rebuildRoleRoleCache(UserRoleType user) throws ArgumentException{
		return rebuildRoleCache("cache_role_roles",Arrays.asList(user),user.getOrganization());
	}
	public static boolean rebuildRoleRoleCache(List<UserRoleType> users, OrganizationType org) throws ArgumentException{
		return rebuildRoleCache("cache_role_roles",users,org);
	}
	public static boolean rebuildUserRoleCache(UserType user) throws ArgumentException{
		return rebuildRoleCache("cache_user_roles",Arrays.asList(user),user.getOrganization());
	}
	public static boolean rebuildUserRoleCache(List<UserType> users, OrganizationType org) throws ArgumentException{
		return rebuildRoleCache("cache_user_roles",users,org);
	}
	private static <T> boolean rebuildRoleCache(String functionName, List<T> objects,OrganizationType org) throws ArgumentException{
		boolean out_bool = false;
		Connection conn = ConnectionFactory.getInstance().getConnection();
		int maxIn = 250;
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
				String sql = "SELECT " + functionName + "(ARRAY[" + buffs.get(i).toString() + "]," + org.getId() + ")";
				//logger.debug(sql);
				ResultSet rset = stat.executeQuery(sql);
				while(rset.next()){
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
		logger.info("Rebuilt role cache For " + updated + " objects");
		return (updated > 0);
	}
	public static boolean rebuildGroupRoleCache(OrganizationType org){
		return rebuildRoleCache("cache_all_group_roles",org);
	}
	public static boolean rebuildRoleRoleCache(OrganizationType org){
		return rebuildRoleCache("cache_all_role_roles",org);
	}
	public static boolean rebuildDataRoleCache(OrganizationType org){
		return rebuildRoleCache("cache_all_data_roles",org);
	}
	public static boolean rebuildUserRoleCache(OrganizationType org){
		return rebuildRoleCache("cache_all_user_roles",org);
	}
	private static boolean rebuildRoleCache(String functionName,OrganizationType org){
		boolean out_bool = false;
		Connection conn = ConnectionFactory.getInstance().getConnection();

		CONNECTION_TYPE connType = DBFactory.getConnectionType(conn);
		String token = DBFactory.getParamToken(connType);
		try{
			//PreparedStatement stat = conn.prepareStatement("SELECT U.id,cache_user_roles(U.id,U.organizationid) from users U WHERE U.organizationid = " + token);
			Statement stat = conn.createStatement();
			ResultSet rset = stat.executeQuery("SELECT " + functionName + "(" + org.getId() + ");");
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
	
}
