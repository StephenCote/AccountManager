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
package org.cote.accountmanager.data.sod;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.FactoryEnumType;



public class SoDPolicyUtil {
	public static final Logger logger = Logger.getLogger(SoDPolicyUtil.class.getName());
	private static Map<String,List<Long>> activityPermissions = new HashMap<String,List<Long>>();
	public static List<Long> getActivityPermissions(String activityUrn){
		if(activityPermissions.containsKey(activityUrn) == false){
			activityPermissions.put(activityUrn, new ArrayList<Long>());
		}
		List<Long> perms = activityPermissions.get(activityUrn);
		if(perms.size() == 0){
			try{
				DirectoryGroupType dir = Factories.getGroupFactory().getByUrn(activityUrn);
				List<EntitlementType> ents = EffectiveAuthorizationService.getEffectiveMemberEntitlements(dir, null, new BasePermissionType[0],false);
				Set<Long> perSet = new HashSet<Long>();
				for(int i = 0; i < ents.size(); i++){
					EntitlementType ent = ents.get(i);
					BasePermissionType per = Factories.getPermissionFactory().getById(ent.getEntitlementId(), ent.getOrganizationId());
					if(perSet.contains(per.getUrn()) == false){
						perSet.add(per.getId());
					}
				}
				perms.addAll(Arrays.asList(perSet.toArray(new Long[0])));
			}
			catch(FactoryException | ArgumentException e){
				logger.error(e.getMessage());
			}
		}

		/*
		Connection conn = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(conn);
		String token = DBFactory.getParamToken(connectionType);
		
		if(perms.size() == 0){
			try{
				PreparedStatement statement = conn.prepareStatement("select distinct permissionUrn from groupEntitlementsCache WHERE groupurn = " + token);
				statement.setString(1, activityUrn);
				ResultSet rset = statement.executeQuery();
				while(rset.next()){
					perms.add(rset.getString(1));
				}
				rset.close();
				statement.close();

			}
			catch(SQLException sqe){
				logger.error(sqe.getMessage());
			}
			finally{
				try{
					conn.close();
				}
				catch(SQLException sqe){
					logger.error(sqe.getMessage());
				}
			}
		}
		*/
		return perms;
	}
	/*
	public static List<Long> getActivityPermissionsForAccount(String activityUrn, String urn){
		return getActivityPermissionsForType("account", activityUrn, urn);
	}
	public static List<Long> getActivityPermissionsForPerson(String activityUrn, String urn){
		return getActivityPermissionsForType("person", activityUrn, urn);
	}
	*/
	/// NOTE: This could differentiate between any and all permissions
	/// XXX - making this all XXX  At the moment, it's just ANY - but for all, the returned permission size must be greater than (but should be equal to) the activity permission size
	///
	public static List<Long> getActivityPermissionsForType(String activityUrn, NameIdType reference){
		List<Long> perms = new ArrayList<Long>();
		List<Long> actPerms = getActivityPermissions(activityUrn);
		if(actPerms.size() == 0){
			logger.warn("Zero permissions found for " + activityUrn);
			return perms;
		}
		try{
			NameIdType object = Factories.getGroupFactory().getByUrn(activityUrn);
			List<EntitlementType> ents = EffectiveAuthorizationService.getEffectiveMemberEntitlements(object,reference , actPerms.toArray(new Long[0]),true);
			Set<Long> perSet = new HashSet<Long>();
			for(int i = 0; i < ents.size(); i++){
				EntitlementType ent = ents.get(i);
				BasePermissionType per = Factories.getPermissionFactory().getById(ent.getEntitlementId(), ent.getOrganizationId());
				if(perSet.contains(per.getUrn()) == false){
					perSet.add(per.getId());
				}
			}
			perms.addAll(Arrays.asList(perSet.toArray(new Long[0])));
		}
		catch(FactoryException | ArgumentException e){
			logger.error(e.getMessage());
		}
		/*
		Connection conn = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(conn);
		String token = DBFactory.getParamToken(connectionType);
		
		
		try{
			StringBuffer buff = new StringBuffer();
			for(int i = 0; i < actPerms.size();i++){
				if(i > 0 ) buff.append(",");
				buff.append("'" + actPerms.get(i) + "'");
			}
			
			/// actPerms contains a list of permissions associated with an activity
			/// Look for permissions for the account where they are not directly associated with that activity (which, at the moment, may happen if a role is directly attached to an activity)
			///
			PreparedStatement statement = conn.prepareStatement("select distinct personUrn,accountUrn,permissionUrn from groupEntitlementsCache where permissionurn in (" + buff.toString() + ") AND groupurn <> " + token + " AND " + type + "urn = " + token + ";");
			
			statement.setString(1, activityUrn);
			statement.setString(2, urn);
			
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				perms.add(rset.getString(1));
			}
			rset.close();
			statement.close();

		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
		}
		finally{
			try{
				conn.close();
			}
			catch(SQLException sqe){
				logger.error(sqe.getMessage());
			}
		}
		*/
		logger.info("Found " + perms.size() + " permissions for " + reference.getUrn() + " in activity " + activityUrn);
		if(perms.size() > 0 && perms.size() != actPerms.size()){
			logger.error("CASE ALL: Returned permission size was not equal to the activity permission size, so a negative match is being returned");
			perms.clear();
		}
		return perms;
	}
}
