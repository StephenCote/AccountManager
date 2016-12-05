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
/*
 * ContactInformation is currently commented out until the factory gets refactored
 */

package org.cote.accountmanager.service.rest;

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
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.INameIdGroupFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.FactoryService;
import org.cote.accountmanager.data.services.ITypeSanitizer;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.data.util.UrnUtil;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.service.util.ServiceUtil;

public class BaseService {
	public static final Logger logger = LogManager.getLogger(BaseService.class);
	public static boolean enableExtendedAttributes = false;
	private static boolean allowDataPointers = false;
	private static boolean checkConfigDataPoint = false;

	protected static boolean isAllowDataPointers(HttpServletRequest request){
		if(checkConfigDataPoint) return allowDataPointers;
		checkConfigDataPoint = true;
		allowDataPointers = getBoolParam(request,"data.pointers.enabled");
		return allowDataPointers;
	}
	protected static boolean getBoolParam(HttpServletRequest request, String name){
		boolean ret = false;
		String iV = request.getServletContext().getInitParameter(name);
		if(iV != null && iV.length() > 0){
			ret = Boolean.parseBoolean(iV);
		}
		return ret;
	}
	public static String getDefaultGroupName(AuditEnumType type){
		String out_path = "~";
		switch(type){
			case DATA:
				out_path = "Data";
				break;
			case GROUP:
				out_path = "";
				break;
		}
		return out_path;
	}
	public static String getDefaultPath(AuditEnumType type){
		return "~/" + getDefaultGroupName(type);
	}
	
	/// Restore organization and group ids from 'path' values on inbound objects
	///
	public static <T> void normalize(UserType user, T object) throws ArgumentException, FactoryException{
		
		if(object == null){
			throw new ArgumentException("Null object");
		}
		NameIdType obj = (NameIdType)object;
		/* obj.getOrganizationPath() == null ||  */
		if(obj.getNameType() == NameEnumType.UNKNOWN){
			throw new ArgumentException("Invalid object");
		}
		if(user != null && obj.getOrganizationPath() == null){
			logger.warn("Organization path not specified. Using context user's organization");
			obj.setOrganizationPath(((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationPath(user.getOrganizationId()));
		}
		INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(obj.getNameType().toString()));
		
		if(iFact.isClusterByGroup() || iFact.getFactoryType().equals(FactoryEnumType.DATA)){
			if(user != null){
				BaseGroupType group = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, ((NameIdDirectoryGroupType)obj).getGroupPath(),user.getOrganizationId());
				if(group != null){
					((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(group);
					((NameIdDirectoryGroupType)obj).setGroupPath(group.getPath());
				}
			}
			iFact.normalize(object);
		}
		else /*if(iFact.isClusterByParent()){*/ 
		{ 
			iFact.normalize(object);
		}
		/*
		else{
			throw new ArgumentException("Unsupported type: " + obj.getNameType().toString());
		}
		*/

	}
	
	/// Apply 'path' values to outbound objects
	///
	public static <T> void denormalize(T object) throws ArgumentException, FactoryException{
		
		if(object == null){
			throw new ArgumentException("Null object");
		}
		NameIdType obj = (NameIdType)object;
		if(obj.getOrganizationId().compareTo(0L) == 0 || obj.getNameType() == NameEnumType.UNKNOWN){
			throw new ArgumentException("Invalid object");
		}
		INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(obj.getNameType().toString()));
		iFact.denormalize(object);
		/*
		if(iFact.isClusterByGroup() || iFact.isClusterByParent() || obj.getNameType().equals(NameEnumType.DATA)){
			iFact.denormalize(object);
		}
		
		else{
			throw new ArgumentException("Unsupported type: " + obj.getNameType().toString());
		}
		*/

	}

	/// don't blindly accept values 
	///
	private static <T> boolean sanitizeAddNewObject(AuditEnumType type, UserType user, T in_obj) throws ArgumentException, FactoryException, DataException, DataAccessException{
		boolean out_bool = false;
		INameIdFactory iFact = Factories.getNameIdFactory(FactoryEnumType.valueOf(type.toString()));
		ITypeSanitizer sanitizer = Factories.getSanitizer(NameEnumType.valueOf(type.toString()));
		if(sanitizer == null){
			logger.error("Sanitizer is null");
			return false;
		}
		T san_obj = sanitizer.sanitizeNewObject(type, user, in_obj);
		if(sanitizer.useAlternateAdd(type, san_obj)){
			out_bool = sanitizer.add(type, user, san_obj);
		}
		else{
			out_bool = iFact.add(san_obj);
		}
		return out_bool;
	}
	private static <T> boolean updateObject(AuditEnumType type, T in_obj) throws ArgumentException, FactoryException, DataAccessException {
		boolean out_bool = false;
		INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(type.toString()));
		out_bool = iFact.update(in_obj);
		
		if(out_bool && enableExtendedAttributes){
			out_bool = Factories.getAttributeFactory().updateAttributes((NameIdType)in_obj);
		}

		return out_bool;		
	}
	private static <T> boolean deleteObject(AuditEnumType type, T in_obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		if(enableExtendedAttributes){
			out_bool = Factories.getAttributeFactory().deleteAttributes((NameIdType)in_obj);
			if(out_bool == false){
				logger.warn("No extended attributes deleted for " + ((NameIdType)in_obj).getName());
			}
		}
		
		ITypeSanitizer sanitizer = Factories.getSanitizer(NameEnumType.valueOf(type.toString()));
		if(sanitizer == null){
			logger.error("Sanitizer is null");
			return false;
		}

		INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(type.toString()));
		if(sanitizer.useAlternateDelete(type, in_obj)){
			out_bool = sanitizer.delete(type, in_obj);
		}
		else{
			out_bool = iFact.delete(in_obj);
		}
	
		return out_bool;
	}
	public static <T> T getFactory(AuditEnumType type){
		return Factories.getFactory(FactoryEnumType.valueOf(type.toString()));
		
	}
	private static <T> T getByObjectId(AuditEnumType type, String id, long organizationId) throws ArgumentException, FactoryException {
		NameIdFactory factory = getFactory(type);
		T out_obj = factory.getByObjectId(id, organizationId);
		
		if(out_obj == null) return null;
		return postFetchObject(type, out_obj);
	}
	private static <T> T postFetchObject(AuditEnumType type, T obj) throws ArgumentException, FactoryException{
		populate(type, obj);
		denormalize(obj);

		ITypeSanitizer sanitizer = Factories.getSanitizer(NameEnumType.valueOf(type.toString()));
		if(sanitizer == null){
			logger.error("Sanitizer is null");
			return obj;
		}
		
		if(enableExtendedAttributes){
			Factories.getAttributeFactory().populateAttributes((NameIdType)obj);
		}
		if(sanitizer.usePostFetch(type, obj)){
			obj = sanitizer.postFetch(type, obj);
		}

		return obj;		
	}
	private static <T> T getById(AuditEnumType type, long id, long organizationId) throws ArgumentException, FactoryException {
		NameIdFactory factory = getFactory(type);
		T out_obj = factory.getById(id, organizationId);
		
		if(out_obj == null) return null;
		
		return postFetchObject(type, out_obj);
	}
	private static <T> T getByNameInParent(AuditEnumType type, String name, String otype, NameIdType parent) throws ArgumentException, FactoryException {
		
		T out_obj = null;
		INameIdFactory factory = getFactory(type);
		if(factory.isClusterByParent() == true){
			out_obj = factory.getByNameInParent(name, otype, parent.getId(), parent.getOrganizationId());
		}
		if(out_obj != null){
			populate(type, out_obj);
			denormalize(out_obj);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes((NameIdType)out_obj);
			}

		}
		return out_obj;		
	}
	
	
	private static <T> T getByNameInGroup(AuditEnumType type, String name, DirectoryGroupType group) throws ArgumentException, FactoryException {
		
		T out_obj = null;
		INameIdFactory iFact = getFactory(type);
		if(iFact.isClusterByGroup()){
			out_obj = ((INameIdGroupFactory)iFact).getByNameInGroup(name,group);
		}
		else if(type.equals(AuditEnumType.DATA)){
			out_obj = (T)((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name, group);
			if(out_obj == null){
				logger.error("Data '" + name + "' is null");
				return out_obj;
			}
			out_obj = postFetchObject(type, out_obj);
		}
		
		if(out_obj != null){
			populate(type, out_obj);
			denormalize(out_obj);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes((NameIdType)out_obj);
			}
		}
		return out_obj;		
	}
	private static <T> T getByNameInGroup(AuditEnumType type, String name, long organizationId) throws ArgumentException, FactoryException {
		logger.error("***** DEPRECATE THIS AND ITS ENTIRE TRACE");
		T out_obj = null;
		switch(type){
			case ROLE:
				out_obj = (T)((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleByName(name, organizationId);
				break;
			case USER:
				out_obj = (T)Factories.getNameIdFactory(FactoryEnumType.USER).getByName(name, organizationId);
				break;
		}
		if(out_obj != null){
			populate(type, out_obj);
			denormalize(out_obj);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes((NameIdType)out_obj);
			}

		}
		return out_obj;		
	}
	
	private static <T> void populate(AuditEnumType type,T object){
		try {
			Factories.populate(FactoryEnumType.valueOf(type.toString()), object);
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		}
	
	}
	public static boolean canViewType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		if(AuthorizationService.isMapOwner(user, obj)) return true;
		INameIdFactory iFact = getFactory(type);
		if(type.equals(AuditEnumType.USER)){
			// allow for user requesting self
			// this does not register true for 'isMapOwner' for the user object as a user does not own itslef
			//
			out_bool = (user.getId().compareTo(obj.getId())==0 && user.getOrganizationId().compareTo(obj.getOrganizationId())==0);
			if(!out_bool)  out_bool = AuthorizationService.isMapOwner(user, obj);
			if(!out_bool) out_bool = RoleService.isFactoryAdministrator(user, Factories.getFactory(FactoryEnumType.ACCOUNT),obj.getOrganizationId());
			if(!out_bool) out_bool = RoleService.isFactoryReader(user, Factories.getFactory(FactoryEnumType.ACCOUNT),obj.getOrganizationId());
		}
		else if(iFact.isClusterByGroup()){
			out_bool = AuthorizationService.canView(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupById(((NameIdDirectoryGroupType)obj).getGroupId(),obj.getOrganizationId()));
		}
		else{
			out_bool = AuthorizationService.canView(user, obj);
		}

		return out_bool;
	}
	public static boolean canCreateType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		INameIdFactory iFact = getFactory(type);
		if(iFact.isClusterByGroup()){
			NameIdDirectoryGroupType nobj = (NameIdDirectoryGroupType)obj;
			if(nobj.getGroupId() <= 0L){
				logger.error("Invalid group id");
			}
			else{
				out_bool = AuthorizationService.canChange(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupById(nobj.getGroupId(),obj.getOrganizationId()));
			}
		}
		else if(iFact.isClusterByParent()){
			if(obj.getParentId() > 0L){
				NameIdType parent = iFact.getById(obj.getParentId(),obj.getOrganizationId());
				out_bool = AuthorizationService.canChange(user, parent);
			}
		}
		else if(type.equals(AuditEnumType.DATA)){
			out_bool = AuthorizationService.canChange(user, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupById(((DataType)obj).getGroupId(),obj.getOrganizationId()));
		}
		
		if(out_bool == false && type.equals(AuditEnumType.PERMISSION) || type.equals(AuditEnumType.ROLE)){
			logger.warn("***** REFACTOR - Why is this role check not in the Authorization service check?");
			out_bool = RoleService.isFactoryAdministrator(user, Factories.getFactory(FactoryEnumType.ACCOUNT),obj.getOrganizationId());
		}
	
		return out_bool;
	}
	public static boolean canChangeType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		if(AuthorizationService.isMapOwner(user, obj)) return true;
		INameIdFactory iFact = getFactory(type);
		if(iFact.isClusterByGroup()){
			out_bool = AuthorizationService.canChange(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupById(((NameIdDirectoryGroupType)obj).getGroupId(),obj.getOrganizationId()));
		}
		else if(type.equals(AuditEnumType.GROUP)){
			BaseGroupType edir = iFact.getById(obj.getId(), user.getOrganizationId());
			BaseGroupType opdir = iFact.getById(edir.getParentId(), user.getOrganizationId());
			BaseGroupType pdir = iFact.getById(((BaseGroupType)obj).getParentId(), user.getOrganizationId());
			if(opdir == null){
				logger.error("Original Parent group (#" + obj.getParentId() + ") doesn't exist in organization " + user.getOrganizationId());
				return false;
			}
			if(pdir == null){
				logger.error("Specified Parent group (#" + obj.getParentId()+ ") doesn't exist in organization " + user.getOrganizationId());
				return false;
			}
			if(opdir.getId() != pdir.getId() && !AuthorizationService.canCreate(user, pdir)){
				logger.error("User " + user.getName() + " (#" + user.getId() + ") is not authorized to create in group " + pdir.getName() + " (#" + pdir.getId() + ")");
				return false;
			}

			out_bool = AuthorizationService.canChange(user, obj);
		}
		else if(type.equals(AuditEnumType.USER)){
			out_bool = (user.getId().compareTo(obj.getId())==0 && user.getOrganizationId().compareTo(obj.getOrganizationId())==0);
			if(!out_bool)  out_bool = AuthorizationService.isMapOwner(user, obj);
			if(!out_bool) out_bool = RoleService.isFactoryAdministrator(user, Factories.getFactory(FactoryEnumType.ACCOUNT),obj.getOrganizationId());
		}
		else{
			out_bool = AuthorizationService.canChange(user, obj);
		}

		return out_bool;
	}
	public static boolean canDeleteType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		if(AuthorizationService.isMapOwner(user, obj)) return true;
		INameIdFactory iFact = getFactory(type);
		if(iFact.isClusterByGroup()){
			/// NOTE: testing whether the parent group can be changed, which propogates to deleting the contained object
			///
			out_bool = AuthorizationService.canChange(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupById(((NameIdDirectoryGroupType)obj).getGroupId(),obj.getOrganizationId()));
		}
		else if(type.equals(AuditEnumType.USER)){
			out_bool = (user.getId().compareTo(obj.getId())==0 && user.getOrganizationId().compareTo(obj.getOrganizationId())==0);
			if(out_bool) throw new FactoryException("Self deletion not supported via Web interface");
			if(!out_bool)  out_bool = AuthorizationService.isMapOwner(user, obj);
			if(!out_bool) out_bool = RoleService.isFactoryAdministrator(user, Factories.getFactory(FactoryEnumType.ACCOUNT),obj.getOrganizationId());
		}
		else{
			out_bool = AuthorizationService.canDelete(user,obj);
		}
	
		return out_bool;
	}
	
	/// Duped in AuthorizationService, except the type is taken from the object instead of from the AuditEnumType
	private static <T> boolean authorizeRoleType(AuditEnumType type, UserType adminUser, BaseRoleType targetRole, T bucket, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		boolean out_bool = false;
		out_bool = AuthorizationService.authorizeType(adminUser, targetRole, (NameIdType)bucket, view, edit, delete, create);
		return out_bool;
	}
	/// Duped in AuthorizationService, except the type is taken from the object instead of from the AuditEnumType
	private static <T> boolean authorizeUserType(AuditEnumType type, UserType adminUser, UserType targetUser, T bucket, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		boolean out_bool = false;
		out_bool = AuthorizationService.authorizeType(adminUser, targetUser, (NameIdType)bucket, view, edit, delete, create);
		return out_bool;
	}
	
	public static <T> boolean authorizeRole(AuditEnumType type, long organizationId, long targetRoleId, T bucket, boolean view, boolean edit, boolean delete, boolean create, HttpServletRequest request){
		boolean out_bool = false;

		BaseRoleType targetRole = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, "authorizeRole", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		NameIdType typeBean = (NameIdType)bucket;
		AuditService.targetAudit(audit, type, (typeBean == null ? "null" : UrnUtil.getUrn(typeBean)));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;

		try {
			if(canChangeType(type, user, typeBean)){
				targetRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getById(targetRoleId, organizationId);
				if(targetRole != null){
					if(authorizeRoleType(type, user, targetRole, bucket, view, edit, delete, create)){
						EffectiveAuthorizationService.rebuildPendingRoleCache();
						AuditService.permitResult(audit, "Applied authorization policy updates for role #" + targetRoleId + " " + targetRole.getName());
						out_bool = true;
					}
				}
				else{
					AuditService.denyResult(audit, "Target user #" + targetRoleId + " in organization #" + organizationId + " does not exist");
				}
			}
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error("Error",e);
			AuditService.denyResult(audit, e.getMessage());
		}
		return out_bool;
	}
	
	public static <T> boolean authorizeUser(AuditEnumType type, long organizationId, long targetUserId, T bucket, boolean view, boolean edit, boolean delete, boolean create, HttpServletRequest request){
		boolean out_bool = false;

		UserType targetUser = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, "authorizeUser", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		NameIdType typeBean = (NameIdType)bucket;
		AuditService.targetAudit(audit, type, (typeBean == null ? "null" : UrnUtil.getUrn(typeBean)));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;

		try {
			if(canChangeType(type, user, typeBean)){
				targetUser = Factories.getNameIdFactory(FactoryEnumType.USER).getById(targetUserId, organizationId);
				if(targetUser != null){
					if(authorizeUserType(type, user, targetUser, bucket, view, edit, delete, create)){
						EffectiveAuthorizationService.rebuildPendingRoleCache();
						AuditService.permitResult(audit, "Applied authorization policy updates for user #" + user.getId() + " " + user.getName());
						out_bool = true;
					}
				}
				else{
					AuditService.denyResult(audit, "Target user #" + targetUserId + " in organization #" + organizationId + " does not exist");
				}
			}
		} catch (FactoryException e) {
			
			logger.error("Error",e);
			AuditService.denyResult(audit, e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
			AuditService.denyResult(audit, e.getMessage());
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
			AuditService.denyResult(audit, e.getMessage());
		}
		return out_bool;
	}
	
	public static <T> boolean delete(AuditEnumType type, T bean, HttpServletRequest request){
		
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.DELETE, "delete", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		NameIdType typeBean = (NameIdType)bean;
		AuditService.targetAudit(audit, type, (typeBean == null ? "null" : UrnUtil.getUrn(typeBean)));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;

		try {
			normalize(user,typeBean);
			if(typeBean.getId() <= 0){
				AuditService.denyResult(audit,"Bean contains invalid data");
				return out_bool;
			}
			if(canDeleteType(type, user, typeBean)){
				out_bool = deleteObject(type, bean);
				if(out_bool) AuditService.permitResult(audit, "Deleted " + typeBean.getName());
				else AuditService.denyResult(audit, "Unable to delete " + typeBean.getName());
				
			}
			else{
				AuditService.denyResult(audit, "User is not authorized");
				System.out.println("User is not authorized to delete the object object '" + typeBean.getName() + "' #" + typeBean.getId());
			}
		} catch (ArgumentException e1) {
			
			logger.error("Error",e1);
			AuditService.denyResult(audit, e1.getMessage());
		} catch (FactoryException e1) {
			
			logger.error("Error",e1);
			AuditService.denyResult(audit, e1.getMessage());
		}

		return out_bool;
	}
	public static <T> boolean add(AuditEnumType addType, T bean, HttpServletRequest request){
		
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "add", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		NameIdType dirBean = (NameIdType)bean;
		AuditService.targetAudit(audit, addType, (dirBean == null ? "null" : dirBean.getName()));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;
		if(dirBean.getNameType() == NameEnumType.UNKNOWN){
			logger.warn("NameType not specified.  Setting as " + addType.toString());
			dirBean.setNameType(NameEnumType.valueOf(addType.toString()));
		}
		try {
			
			normalize(user,bean);
			
			if(canCreateType(addType, user, dirBean) == true){

				out_bool = sanitizeAddNewObject(addType, user, bean);

				if(out_bool && enableExtendedAttributes){
					NameIdType beanObj = (NameIdType)bean;
					if(beanObj.getAttributes().size() > 0){
						NameIdType obj = null;
						if(FactoryService.isDirectoryType(addType)) obj = readByName(addType,((NameIdDirectoryGroupType)bean).getGroupId(),((NameIdDirectoryGroupType)bean).getName(),request);
						else obj = readByName(addType,beanObj.getName(),request);
						if(obj != null){
							out_bool = Factories.getAttributeFactory().updateAttributes((NameIdType)obj);
						}
						else{
							logger.warn("Failed to update extended attributes");
						}
					}
					else{
						logger.info("No attributes defined for add operation");
					}
				}

				if(out_bool) AuditService.permitResult(audit, "Added " + dirBean.getName());
				else AuditService.denyResult(audit, "Unable to add " + dirBean.getName());
				
			}
			else{
				AuditService.denyResult(audit, "User is not authorized");
				System.out.println("User is not authorized to add the object  '" + dirBean.getName());
			}
		} catch (ArgumentException | FactoryException | DataException | DataAccessException e) {
			
			logger.error("Error",e);
			AuditService.denyResult(audit, e.getMessage());
		}

		return out_bool;
	}
	
	public static <T> boolean update(AuditEnumType type, T bean,HttpServletRequest request){
		boolean out_bool = false;
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "update",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		NameIdType dirBean = (NameIdType)bean;
		AuditService.targetAudit(audit, type, (dirBean == null ? "null" : UrnUtil.getUrn(dirBean)));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;
		
		if(dirBean == null){
			AuditService.denyResult(audit, "Null value");
			return false;
		}

		try {
			normalize(user,bean);
			
			/// 2015/06/22
			/// Add in restriction to block ownership changes via an update
			///
			NameIdType matBean = readById(type,dirBean.getId(),request);
			if(matBean == null){
				AuditService.denyResult(audit, "Unable to read original object");
				return false;
			}

			if(dirBean.getOwnerId().compareTo(matBean.getOwnerId()) != 0){
				AuditService.denyResult(audit, "Chown operation is forbidden in an update operation");
				return false;
			}

			if(canChangeType(type, user, dirBean) == true){
				out_bool = updateObject(type, bean); 	
				if(out_bool) AuditService.permitResult(audit, "Updated " + dirBean.getName() + " (#" + dirBean.getId() + ")");
				else AuditService.denyResult(audit, "Unable to update " + dirBean.getName() + " (#" + dirBean.getId() + ")");
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to change object '" + dirBean.getName() + "' #" + dirBean.getId());
			}
		} catch (ArgumentException | FactoryException | DataAccessException e) {
			
			logger.error("Error",e);
			AuditService.denyResult(audit, e.getMessage());
		}

		return out_bool;
	}
	public static <T> T readByObjectId(AuditEnumType type, String id,HttpServletRequest request){
		T out_obj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByObjectId",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, id);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;
		
		try {
			
			NameIdType dirType = getByObjectId(type,id, user.getOrganizationId());
			if(dirType == null){
				AuditService.denyResult(audit, "#" + id + " (" + type + ") doesn't exist in organization " + user.getOrganizationId());
				return null;
			}		
			AuditService.targetAudit(audit, type, dirType.getUrn());
			if(canViewType(type, user, dirType) == true){
				out_obj = (T)dirType;
				if(dirType.getNameType().equals(NameEnumType.DATA) && ((DataType)out_obj).getPointer() && isAllowDataPointers(request) == false){
					AuditService.denyResult(audit, "#" + id + " (" + type + ") is a data pointer, and reading data pointers from the Web FE is forbidden by configuration.");
					out_obj = null;
				}
				else{
					AuditService.permitResult(audit, "Read " + dirType.getName() + " (#" + dirType.getId() + ")");
				}
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + dirType.getName() + "' #" + dirType.getId());
			}

		} catch (ArgumentException | FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_obj;
	}	
	public static <T> T readById(AuditEnumType type, long id,HttpServletRequest request){
		T out_obj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readById",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, Long.toString(id));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;
		
		try {
			
			NameIdType dirType = getById(type,id, user.getOrganizationId());
			if(dirType == null){
				AuditService.denyResult(audit, "#" + id + " (" + type + ") doesn't exist in organization " + user.getOrganizationId());
				return null;
			}		
			AuditService.targetAudit(audit, type, dirType.getUrn());
			if(canViewType(type, user, dirType) == true){
				out_obj = (T)dirType;
				if(dirType.getNameType().equals(NameEnumType.DATA) && ((DataType)out_obj).getPointer() && isAllowDataPointers(request) == false){
					AuditService.denyResult(audit, "#" + id + " (" + type + ") is a data pointer, and reading data pointers from the Web FE is forbidden by configuration.");
					out_obj = null;
				}
				else{
					AuditService.permitResult(audit, "Read " + dirType.getName() + " (#" + dirType.getId() + ")");
				}
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + dirType.getName() + "' #" + dirType.getId());
			}

		} catch (ArgumentException | FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_obj;
	}	
	public static <T> T readByName(AuditEnumType type, String name,HttpServletRequest request){
		DirectoryGroupType dir = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;

		try{
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateUserDirectory(user, getDefaultGroupName(type));
		}
		 catch (FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		} 
		return readByName(audit,type, user, dir, name, request);
	}
	public static <T> T readByName(AuditEnumType type, long groupId, String name,HttpServletRequest request){
		DirectoryGroupType dir = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;

		try{
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getById(groupId, user.getOrganizationId());
		}
		 catch (FactoryException e1) {
			
			 logger.error(e1.getMessage());
			logger.error("Error",e1);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		} 
		return readByName(audit,type, user, dir, name, request);
	}
	public static <T> T readByName(AuditEnumType type, DirectoryGroupType dir, String name,HttpServletRequest request){
		T out_obj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;
		return readByName(audit,type, user, dir, name, request);
	}
	public static <T> T readByName(AuditType audit,AuditEnumType type, UserType user, DirectoryGroupType dir, String name,HttpServletRequest request){
		T out_obj = null;
		if(dir == null){
			logger.error("Directory Group is null");
			return null;
		}
		try {
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dir);
			out_obj = getByNameInGroup(type, name, dir);
			if(out_obj == null){
				AuditService.denyResult(audit, "'" + name + "' doesn't exist");
				return null;
			}
			AuditService.targetAudit(audit, type, ((NameIdType)out_obj).getUrn());
			if(canViewType(type, user, (NameIdType)out_obj)){
				if(((NameIdType)out_obj).getNameType().equals(NameEnumType.DATA) && ((DataType)out_obj).getPointer() && isAllowDataPointers(request) == false){
					AuditService.denyResult(audit, name + " is a data pointer, and reading data pointers from the Web FE is forbidden by configuration.");
					out_obj = null;
				}
				else{
					AuditService.permitResult(audit, "Read " + name + " (#" + ((NameIdType)out_obj).getId() + ")");
				}

			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + dir.getName() + "' #" + dir.getId());
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_obj;
	}
	
	public static <T> T readByNameInParent(AuditEnumType type, NameIdType parent, String name, String otype, HttpServletRequest request){
		T out_obj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByNameInParent",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;
		return readByNameInParent(audit,type, user, parent, name, otype, request);
	}
	public static <T> T readByNameInParent(AuditType audit,AuditEnumType type, UserType user, NameIdType parent, String name,String otype, HttpServletRequest request){
		T out_obj = null;
		try {

			out_obj = getByNameInParent(type, name, otype, parent);
			if(out_obj == null){
				AuditService.denyResult(audit, "'" + name + "' doesn't exist");
				return null;
			}
			if(canViewType(type, user, (NameIdType)out_obj)){
				AuditService.permitResult(audit, "Read " + name + " (#" + ((NameIdType)out_obj).getId() + ")");

			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + parent.getName() + "' #" + parent.getId());
			}
		} catch (ArgumentException e1) {
			
			logger.error("Error",e1);
		} catch (FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_obj;
	}
	
	public static <T> T readByNameInOrganization(AuditEnumType type, long organizationId, String name,HttpServletRequest request){

		logger.error("***** DEPRECATE THIS AND ITS ENTIRE TRACE");
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;

		return readByName(audit,type, user, organizationId, name, request);
	}

	public static <T> T readByName(AuditType audit,AuditEnumType type, UserType user, long organizationId, String name,HttpServletRequest request){
		T out_obj = null;
		logger.error("***** DEPRECATE THIS AND ITS ENTIRE TRACE");
		try {
			//DirectoryGroupType group = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateUserDirectory(user, getDefaultGroupName(type));

			out_obj = getByNameInGroup(type, name, organizationId);
			if(out_obj == null){
				AuditService.denyResult(audit, "'" + name + "' doesn't exist");
				return null;
			}
			AuditService.targetAudit(audit, type, ((NameIdType)out_obj).getUrn());
			if(canViewType(type, user, (NameIdType)out_obj)){
				AuditService.permitResult(audit, "Read " + name + " (#" + ((NameIdType)out_obj).getId() + ")");
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + ((NameIdType)out_obj).getName() + "' #" + ((NameIdType)out_obj).getId());
				out_obj = null;
			}
		} catch (ArgumentException e1) {
			
			logger.error("Error",e1);
			out_obj = null;
		} catch (FactoryException e1) {
			
			logger.error("Error",e1);
			out_obj = null;
		} 

		return out_obj;
	}
	
	public static int countByGroup(AuditEnumType type, BaseGroupType parentGroup, HttpServletRequest request){
		//BaseGroupType dir = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "count",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, parentGroup.getUrn());
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return 0;
		return count(audit,type, user, parentGroup, request);
	}

	public static int countByGroup(AuditEnumType type, String path, HttpServletRequest request){
		BaseGroupType dir = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "count",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, path);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return 0;

		try{
			dir = (BaseGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.UNKNOWN,path, user.getOrganizationId());
		}
		 catch (FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		} 
		if(dir == null){
			AuditService.denyResult(audit, "Path '" + path + "' does not exist");
			return 0;
		}
		return count(audit,type, user, dir, request);
	}
	public static int count(AuditType audit,AuditEnumType type, UserType user, BaseGroupType dir, HttpServletRequest request){
		int out_count = 0;
		try {
			if(canViewType(AuditEnumType.GROUP, user, dir) == true){
				out_count = count(type, dir);
				AuditService.permitResult(audit, "Count " + out_count + " of " + type.toString() + " in '" + dir.getName() + "' #" + dir.getId());
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view objects in the specified group '" + dir.getName() + "' #" + dir.getId());
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_count;
	}
	private static int count(AuditEnumType type, BaseGroupType group) throws ArgumentException, FactoryException {
		
		NameIdFactory factory = getFactory(type);
		if(type == AuditEnumType.DATA) return ((DataFactory)factory).getCount((DirectoryGroupType)group);
		return ((NameIdGroupFactory)factory).countInGroup(group);		
	}
	
	public static int countByOrganization(AuditEnumType type, long organizationId, HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "count",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, Long.toString(organizationId));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return 0;
		return countByOrganization(audit,type, user, organizationId, request);
	}
	public static int countInParent(AuditEnumType type, NameIdType parent, HttpServletRequest request){

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "countByParent",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, UrnUtil.getUrn(parent));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user == null) return 0;

		return countInParent(audit,type, user, parent, request);
	}
	public static int countByOrganization(AuditType audit,AuditEnumType type, UserType user, long organizationId, HttpServletRequest request){
		int out_count = 0;
		try {
			if(
				RoleService.isFactoryAdministrator(user, ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)),organizationId)
				||
				RoleService.isFactoryAdministrator(user, Factories.getFactory(FactoryEnumType.ACCOUNT),organizationId)
				||
				((type == AuditEnumType.USER || type == AuditEnumType.ACCOUNT) &&  RoleService.isFactoryReader(user, Factories.getFactory(FactoryEnumType.ACCOUNT),organizationId))
				||
				(type == AuditEnumType.ROLE && RoleService.isFactoryReader(user, ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)),organizationId))
				||
				(type == AuditEnumType.GROUP && RoleService.isFactoryReader(user, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)),organizationId))
				
			){
				out_count = count(type, organizationId);
				AuditService.permitResult(audit, "Count " + out_count + " of " + type.toString());
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to count directly in organization " + organizationId);
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_count;
	}
	public static int countInParent(AuditType audit,AuditEnumType type, UserType user, NameIdType parent, HttpServletRequest request){
		int out_count = 0;
		try {
			if(canViewType(type,user, parent) == true){
				out_count = countInParent(type, parent);
				AuditService.permitResult(audit, "Count " + out_count + " of " + type.toString());
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to count in parent '" + parent.getName() + "' #" + parent.getId());
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_count;
	}
	
	private static int count(AuditEnumType type, long organization_id) throws ArgumentException, FactoryException {
		NameIdFactory factory = getFactory(type);
		return factory.countInOrganization(organization_id);
	}
	private static int countInParent(AuditEnumType type, NameIdType parent) throws ArgumentException, FactoryException {
		NameIdFactory factory = getFactory(type);
		return factory.countInParent(parent);
	}
	
	public static <T> List<T> listByParentObjectId(AuditEnumType type, String parentType, String parentId, long startRecord, int recordCount, HttpServletRequest request){

		List<T> out_obj = new ArrayList<>();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "listByParentObjectId",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, parentId);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;

		NameIdType parentObj = BaseService.readByObjectId(type, parentId, request);
		if(parentObj == null){
			AuditService.denyResult(audit, "Null parent id");
			return out_obj;
		}
		
		AuditService.targetAudit(audit, type, parentObj.getUrn());
		
		try {
			if(
				AuthorizationService.canView(user, parentObj)
				||
				RoleService.isFactoryReader(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)),parentObj.getOrganizationId())
			){
				AuditService.permitResult(audit, "Access authorized to list objects");
				out_obj = getListByParent(type,parentType,parentObj,startRecord,recordCount,parentObj.getOrganizationId() );
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list roles.");
				return out_obj;
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_obj;
	}
	
	public static <T> List<T> getListByParent(AuditEnumType type,String parentType, NameIdType parentObj, long startIndex, int recordCount, long organizationId){
		//BaseGroupType dir = findGroup(groupType, path, request);
		List<T> dirs = new ArrayList<>();
		INameIdFactory iFact = getFactory(type);
		//GroupEnumType groupType = GroupEnumType.valueOf(type);
		if(parentObj == null) return dirs;
		try {
			if(iFact.isClusterByParent()){
				dirs = iFact.listInParent(parentType, parentObj.getId(), startIndex, recordCount, organizationId);
			}

			for(int i = 0; i < dirs.size(); i++){
				denormalize(dirs.get(i));
				if(BaseService.enableExtendedAttributes){
					Factories.getAttributeFactory().populateAttributes((NameIdType)dirs.get(i));
				}
			}
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		}


		return dirs;
	}
	
	
	public static <T> List<T> getListByGroup(AuditEnumType type, BaseGroupType group,long startRecord, int recordCount) throws ArgumentException, FactoryException {
		List<T> out_obj = new ArrayList<>();
		INameIdFactory iFact = getFactory(type);
		if(iFact.isClusterByGroup()){
			INameIdGroupFactory iGFact = (INameIdGroupFactory)iFact;
			out_obj = iGFact.listInGroup(group, startRecord, recordCount, group.getOrganizationId());
		}
		else if(iFact.getFactoryType().equals(FactoryEnumType.DATA)){
			out_obj = ((DataFactory)iFact).convertList(((DataFactory)iFact).getDataListByGroup((DirectoryGroupType)group, true,startRecord, recordCount, group.getOrganizationId()));
		}
		for(int i = 0; i < out_obj.size();i++){
			//NameIdDirectoryGroupType ngt = (NameIdDirectoryGroupType)out_obj.get(i);
			NameIdType nt = (NameIdType)out_obj.get(i);
			denormalize(nt);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes(nt);
			}

		}

		return out_obj;			
	}
	public static <T> List<T> listByGroup(AuditEnumType type, String groupType, String groupId, long startRecord, int recordCount, HttpServletRequest request){
		List<T> out_obj = new ArrayList<>();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "listByGroup",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, groupId);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;

		//AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All " + type.toString() + " objects",AuditEnumType.GROUP,(user == null ? "Null" : user.getName()));
		NameIdType parentObj = BaseService.readByObjectId(AuditEnumType.GROUP, groupId, request);
		if(parentObj == null){
			AuditService.denyResult(audit, "Null group object");
			return out_obj;
		}
		
		AuditService.targetAudit(audit, type, parentObj.getUrn());
		try{
		if(AuthorizationService.canView(user, parentObj) == true){
			AuditService.permitResult(audit, "Access authorized to group " + parentObj.getName());
			out_obj = getListByGroup(type,(BaseGroupType)parentObj,startRecord,recordCount);
		}
		else{
			AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + parentObj.getName() + " (#" + parentObj.getId() + ")");
			return out_obj;
		}
		}
		catch(ArgumentException | FactoryException e){
			logger.error(e.getMessage());
			AuditService.denyResult(audit, e.getMessage());
		}
		return out_obj;
	}
	public static <T> List<T> getGroupList(AuditEnumType type, UserType user, String path, long startRecord, int recordCount){
		List<T> out_obj = new ArrayList<T>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.USER,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, type, path);
		
		if(SessionSecurity.isAuthenticated(user) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}
		
		try {
			BaseGroupType dir = (BaseGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.UNKNOWN, path, user.getOrganizationId());
			if(dir == null){
				AuditService.denyResult(audit, "Invalid path: '" + path + "'");
				return out_obj;
			}
			///AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(AuthorizationService.canView(user, dir) == true){
				AuditService.permitResult(audit, "Access authorized to group " + dir.getName());
				out_obj = getListByGroup(type,dir,startRecord,recordCount);
				/*
				for(int i = 0; i < out_obj.size();i++){
					delink(type,out_obj.get(i));
				}
				*/
				//out_Lifecycles = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getListByGroup(dir, 0, 0, user.getOrganizationId());
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return out_obj;
			}
		} catch (ArgumentException e1) {
			
			logger.error("Error",e1);
		} catch (FactoryException e1) {
			
			logger.error("Error",e1);
		} 

		return out_obj;
	}
	
	public static <T> T findGroup(GroupEnumType groupType, String path, HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		if(user == null) return null;
		return findGroup(user,groupType,path);
	
	}
	public static <T> T findGroup(UserType user,GroupEnumType groupType, String path){
		T bean = null;
		if(path == null || path.length() == 0) path = "~";
		if(path.startsWith("~") == false && path.startsWith("/") == false) path = "/" + path;
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.USER,user.getName());
		try {
			BaseGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, groupType, path, user.getOrganizationId());
			if(dir == null){
				AuditService.denyResult(audit, "Invalid path: " + groupType.toString() + " " + path);
				return bean;
			}
			if(AuthorizationService.canView(user, dir) == false){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return bean;
			}
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dir);	
			denormalize(dir);
			bean = (T)dir;

			AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getUrn());
			AuditService.permitResult(audit, "Access authorized to group " + dir.getName());
			
			
			
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return bean;
	}
	
	public static <T> T makeFind(AuditEnumType auditType,String type, String path, HttpServletRequest request){
		T obj = find(auditType, type, path, request);
		if(obj != null) return obj;
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.USER,request.getSession().getId());
		AuditService.targetAudit(audit, auditType, path);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return obj;

		
		INameIdFactory iFact = getFactory(auditType);
		if(iFact.isClusterByParent()){
			/// This is a pretty specific implementation limited to paths within the user directory structure
			///
			if(path.startsWith("..") == false && path.startsWith("~") == false && path.startsWith("/Home/" + user.getName() + "/")==false){
				AuditService.denyResult(audit, "Paths can only be created from the home directory");
				return null;
			}
			try{
				obj = iFact.makePath(user, type, path, user.getOrganizationId());
				if(obj != null && canViewType(auditType, user, (NameIdType)obj) == false){
					AuditService.denyResult(audit, "User is not authorized to view object");
					obj = null;
				}
				else if(obj != null){
					iFact.populate(obj);
					AuditService.permitResult(audit, "User is authorized to create the path under their own path home");
				}
			}
			catch(ArgumentException | FactoryException | DataAccessException e){
				logger.error(e.getMessage());
			}
			
		}
		return obj;
		
	}
	public static <T> T find(AuditEnumType auditType,String type, String path, HttpServletRequest request){

		T out_obj = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.USER,request.getSession().getId());
		AuditService.targetAudit(audit, auditType, path);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;

		if(path == null || path.length() == 0) path = "~";
		if(path.startsWith("~") == false && path.startsWith("/") == false) path = "/" + path;
		try {
			INameIdFactory iFact = getFactory(auditType);
			if(iFact.isClusterByParent()){
				out_obj = iFact.find(user, type, path, user.getOrganizationId());
				if(out_obj == null){
					AuditService.denyResult(audit, "Invalid path: " + type + " " + path);
					return null;
				}
				NameIdType objType = (NameIdType)out_obj;
				AuditService.targetAudit(audit, auditType, objType.getUrn());
				if(AuthorizationService.canView(user, objType) == false){
					AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view object " + objType.getName() + " (#" + objType.getId() + ")");
					return null;
				}
				iFact.populate(out_obj);
				denormalize(out_obj);
				
				AuditService.permitResult(audit, "Access authorized to group " + objType.getName());
			}
			else{
				AuditService.denyResult(audit, "Factory does not support clustered find");
			}


		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return out_obj;
	}
}