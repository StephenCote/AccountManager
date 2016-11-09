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
package org.cote.accountmanager.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.service.rest.BaseService;

public class PermissionServiceImpl  {
	
	public static final String defaultDirectory = "~/Permissions";
	public static final Logger logger = LogManager.getLogger(PermissionServiceImpl.class);
	
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
	public static int count(long orgId, HttpServletRequest request){
		return BaseService.countByOrganization(AuditEnumType.PERMISSION, orgId, request);
	}
	public static int countInParent(long orgId, long parentId, HttpServletRequest request){
		OrganizationType org = null;
		BasePermissionType permission = null;
		try{
			org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationById(orgId);
			if(org != null) permission = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getById(parentId, orgId);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
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
			org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationById(orgId);
			if(org != null) parent = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getById(parentId, orgId);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
		} catch (ArgumentException e) {
			
			
			logger.error("Error",e);
		}
		if(parent == null) return null;
		return BaseService.readByNameInParent(AuditEnumType.PERMISSION, parent, name, type, request);
	}
	public static BasePermissionType readByParent(BasePermissionType parent, String name,String type, HttpServletRequest request){
		BasePermissionType role = BaseService.readByNameInParent(AuditEnumType.PERMISSION, parent, name, type, request);
		if(role != null) Factories.getAttributeFactory().populateAttributes(role);
		return role;
	}
	
	public static BasePermissionType getUserPermission(UserType user, String type, HttpServletRequest request){
		PermissionEnumType ptype = PermissionEnumType.fromValue(type);
		BasePermissionType per = null;
		try {
			logger.warn("MISSING ENTITLEMENT CHECK");
			per = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getUserPermission(user, ptype, user.getOrganizationId());
			((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).denormalize(per);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
		return per;
	}
	
	public static List<BasePermissionType> getListInParent(UserType user, String type, BasePermissionType parentPermission, long startRecord, int recordCount){
		///return BaseService.getGroupList(AuditEnumType.PERMISSION, user, path, startRecord, recordCount);
		

		List<BasePermissionType> out_obj = new ArrayList<BasePermissionType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All permissions",AuditEnumType.PERMISSION,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.PERMISSION, parentPermission.getUrn());
		
		if(SessionSecurity.isAuthenticated(user) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}

		try {
			///AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(
				AuthorizationService.canChange(user, parentPermission)
			){
				AuditService.permitResult(audit, "Access authorized to list permissions");
				out_obj = getList(type,parentPermission,startRecord,recordCount,parentPermission.getOrganizationId() );
				for(int i = 0; i < out_obj.size();i++){
					((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).denormalize(out_obj.get(i));
				}
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list permissions.");
				return out_obj;
			}
		} catch (ArgumentException e1) {
			
			logger.error("Error",e1);
		} catch (FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_obj;
		
	}
	private static List<BasePermissionType> getList(String type, BasePermissionType parentPermission, long startRecord, int recordCount, long organizationId) throws ArgumentException, FactoryException {

		PermissionEnumType ptype = PermissionEnumType.fromValue(type);
		return ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionList(parentPermission,ptype,startRecord, recordCount, organizationId);
		
	}

	public static boolean setPermission(UserType user, AuditEnumType srcType, long srcId, AuditEnumType targType, long targId, long permissionId, boolean enable){
		NameIdType src = null;
		NameIdType targ = null;
		BasePermissionType perm = null;
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Permission " + permissionId,AuditEnumType.PERMISSION,(user == null ? "Null" : user.getName()));
		try {
			if(srcType != AuditEnumType.DATA) src = ((NameIdFactory)BaseService.getFactory(srcType)).getById(srcId, user.getOrganizationId());
			else src = ((DataFactory)BaseService.getFactory(srcType)).getDataById(srcId, true, user.getOrganizationId());
			if(targType != AuditEnumType.DATA) targ = ((NameIdFactory)BaseService.getFactory(targType)).getById(targId, user.getOrganizationId());
			else targ = ((DataFactory)BaseService.getFactory(srcType)).getDataById(targId, true, user.getOrganizationId());
			perm = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getById(permissionId, user.getOrganizationId());
			if(src == null || targ == null || perm == null){
				AuditService.denyResult(audit, "One or more reference ids were invalid: " + (src == null ? " " + srcType.toString() + " #" +srcId + " Source is null." : "") + (targ == null ? " " + targType.toString() + " #" +srcId + " Target is null." : "") + (perm == null ? " #" +srcId + " Permission is null." : ""));
				return false;
			}
			AuditService.sourceAudit(audit, AuditEnumType.PERMISSION, srcType.toString() + " " + src.getName() + " (#" + src.getId() + ")");
			AuditService.targetAudit(audit, targType, targ.getUrn());
			/// To set the permission on or off, the user must:
			/// 1) be able to change src,
			/// 2) be able to change targ,
			/// 3) be able to change permission
			if(
				BaseService.canChangeType(srcType, user,src)
				&&
				BaseService.canChangeType(targType, user, targ)
				&&
				AuthorizationService.canChange(user, perm)
			){
				boolean set = false;
				if(srcType == AuditEnumType.GROUP && targType == AuditEnumType.ROLE){
					logger.info("Setting permission for role on group");
					set = AuthorizationService.authorize(user,(BaseRoleType)targ,(BaseGroupType)src,perm,enable);
					
				}
				else if(srcType == AuditEnumType.DATA && targType == AuditEnumType.ROLE){
					logger.info("Setting permission for role on data");
					set = AuthorizationService.authorize(user,(BaseRoleType)targ,(DataType)src,perm,enable);
				}
				else if(srcType == AuditEnumType.GROUP){
					logger.info("Setting permission for entity on group");
					set = AuthorizationService.authorize(user,targ,(BaseGroupType)src,perm,enable);
				}
				else if(srcType == AuditEnumType.ROLE){
					logger.info("Setting permission for entity on role");
					set = AuthorizationService.authorize(user,targ,(BaseRoleType)src,perm,enable);
				}
				else if(srcType == AuditEnumType.DATA){
					logger.info("Setting permission for entity on data");
					set = AuthorizationService.authorize(user,targ,(DataType)src,perm,enable);
				}
				else{
					AuditService.denyResult(audit, "Unhandled type combination");
				}

				if(set){
					EffectiveAuthorizationService.pendUpdate(targ);
					EffectiveAuthorizationService.rebuildPendingRoleCache();
					out_bool = true;
					AuditService.permitResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is authorized to change the permission.");
				}
				else AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") did not change the permission.");
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to set the permission.");
			}

		} catch (FactoryException e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return out_bool;
	}

	

	
}
