/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.FactoryEnumType;



public class SoDPolicyUtil {
	public static final Logger logger = LogManager.getLogger(SoDPolicyUtil.class);
	private static Map<String,List<Long>> activityPermissions = new HashMap<String,List<Long>>();
	public static List<Long> getActivityPermissions(String activityUrn){
		if(activityPermissions.containsKey(activityUrn) == false){
			activityPermissions.put(activityUrn, new ArrayList<Long>());
		}
		List<Long> perms = activityPermissions.get(activityUrn);
		if(perms.size() == 0){
			try{
				DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getByUrn(activityUrn);
				List<EntitlementType> ents = EffectiveAuthorizationService.getEffectiveMemberEntitlements(dir, null, new BasePermissionType[0],false);
				Set<Long> perSet = new HashSet<Long>();
				for(int i = 0; i < ents.size(); i++){
					EntitlementType ent = ents.get(i);
					BasePermissionType per = Factories.getNameIdFactory(FactoryEnumType.PERMISSION).getById(ent.getEntitlementId(), ent.getOrganizationId());
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

		return perms;
	}

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
			NameIdType object = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getByUrn(activityUrn);
			List<EntitlementType> ents = EffectiveAuthorizationService.getEffectiveMemberEntitlements(object,reference , actPerms.toArray(new Long[0]),true);
			Set<Long> perSet = new HashSet<Long>();
			for(int i = 0; i < ents.size(); i++){
				EntitlementType ent = ents.get(i);
				BasePermissionType per = Factories.getNameIdFactory(FactoryEnumType.PERMISSION).getById(ent.getEntitlementId(), ent.getOrganizationId());
				if(perSet.contains(per.getUrn()) == false){
					perSet.add(per.getId());
				}
			}
			perms.addAll(Arrays.asList(perSet.toArray(new Long[0])));
		}
		catch(FactoryException | ArgumentException e){
			logger.error(e.getMessage());
		}
		
		logger.info("Found " + perms.size() + " permissions for " + reference.getUrn() + " in activity " + activityUrn);
		if(perms.size() > 0 && perms.size() != actPerms.size()){
			logger.error("CASE ALL: Returned permission size was not equal to the activity permission size, so a negative match is being returned");
			perms.clear();
		}
		return perms;
	}
}
