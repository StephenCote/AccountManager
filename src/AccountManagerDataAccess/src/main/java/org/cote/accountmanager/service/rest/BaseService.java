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
/*
 * ContactInformation is currently commented out until the factory gets refactored
 */

package org.cote.accountmanager.service.rest;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.DataParticipationFactory;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.GroupParticipationFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.INameIdGroupFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.GroupService;
import org.cote.accountmanager.data.services.ITypeSanitizer;
import org.cote.accountmanager.data.services.PermissionService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.UserService;
import org.cote.accountmanager.data.util.UrnUtil;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.BaseSearchRequestType;
import org.cote.accountmanager.objects.BucketGroupType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.SortQueryType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.OrderEnumType;
import org.cote.accountmanager.objects.types.QueryEnumType;
import org.cote.accountmanager.service.util.ServiceUtil;


public class BaseService {
	public static final Logger logger = LogManager.getLogger(BaseService.class);
	private static boolean enableExtendedAttributes = false;
	private static boolean allowDataPointers = false;
	private static boolean autoDataPointers = false;
	private static String autoDataPointersPath = null;
	private static long autoDataPointersThreshold = 0;
	
	private BaseService(){
		
	}
	public static boolean getEnableExtendedAttributes(){
		return enableExtendedAttributes;
	}
	public static void setEnableExtendedAttributes(boolean b){
		enableExtendedAttributes = b;
	}
	public static void setAllowDataPointers(boolean b){
		allowDataPointers = b;
	}

	public static boolean isAutoDataPointers() {
		return autoDataPointers;
	}
	public static void setAutoDataPointers(boolean autoDataPointers) {
		BaseService.autoDataPointers = autoDataPointers;
	}
	public static String getAutoDataPointersPath() {
		return autoDataPointersPath;
	}
	public static void setAutoDataPointersPath(String autoDataPointersPath) {
		BaseService.autoDataPointersPath = autoDataPointersPath;
	}
	public static long getAutoDataPointersThreshold() {
		return autoDataPointersThreshold;
	}
	public static void setAutoDataPointersThreshold(long autoDataPointersThreshold) {
		BaseService.autoDataPointersThreshold = autoDataPointersThreshold;
	}
	public static boolean isAllowDataPointers() {
		return allowDataPointers;
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
			default:
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
		if(obj.getNameType() == NameEnumType.UNKNOWN){
			throw new ArgumentException("Invalid object: A NameType was not specified");
		}
		if(user != null && obj.getOrganizationPath() == null){
			logger.debug("Organization path not specified. Using context user's organization");
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
			iFact.normalize(obj);
		}
		else
		{ 
			iFact.normalize(obj);
		}


	}
	
	/// Apply 'path' values to outbound objects
	///
	public static <T> void denormalize(T object) throws ArgumentException, FactoryException{
		
		if(object == null){
			throw new ArgumentException("Null object");
		}
		NameIdType obj = (NameIdType)object;
		if(obj.getOrganizationId().compareTo(0L) == 0 || obj.getNameType() == NameEnumType.UNKNOWN){
			throw new ArgumentException("Invalid object: A NameType or Organization was not specified.");
		}
		INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(obj.getNameType().toString()));
		iFact.denormalize(object);

	}

	/// don't blindly accept values 
	///

	private static <T> boolean sanitizeAddNewObject(AuditEnumType type, UserType user, T inObj, String sessionId) throws ArgumentException, FactoryException, DataException, DataAccessException{
		boolean outBool = false;
		INameIdFactory iFact = Factories.getNameIdFactory(FactoryEnumType.valueOf(type.toString()));
		ITypeSanitizer sanitizer = Factories.getSanitizer(NameEnumType.valueOf(type.toString()));
		if(sanitizer == null){
			logger.error(String.format(FactoryException.TYPE_NOT_REGISTERED,"Sanitizer"));
			return false;
		}
		T sanObj = sanitizer.sanitizeNewObject(type, user, inObj);
		if(sanitizer.useAlternateAdd(type, sanObj)){
			outBool = sanitizer.add(type, user, sanObj);
		}
		else{
			if(sessionId != null) {
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.valueOf(type.toString()), (NameIdType)sanObj);
				outBool = true;
			}
			else outBool = iFact.add(sanObj);
		}
		return outBool;
	}
	
	private static <T> boolean updateObject(AuditEnumType type, UserType user, T inObj) throws FactoryException, ArgumentException {
		boolean outBool = false;
		INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(type.toString()));
		ITypeSanitizer sanitizer = Factories.getSanitizer(NameEnumType.valueOf(type.toString()));
		
		if(sanitizer == null){
			logger.error(String.format(FactoryException.TYPE_NOT_REGISTERED,"Sanitizer"));
			return false;
		}
		if(sanitizer.useAlternateUpdate(type, inObj)){
			outBool = sanitizer.update(type, user, inObj);
		}
		else{
			outBool = iFact.update(inObj);
		}
		
		if(outBool && enableExtendedAttributes){
			outBool = Factories.getAttributeFactory().updateAttributes((NameIdType)inObj);
		}

		return outBool;		
	}
	
	private static <T> boolean deleteObject(AuditEnumType type, T inObj) throws ArgumentException, FactoryException{
		boolean outBool = false;
		if(enableExtendedAttributes){
			outBool = Factories.getAttributeFactory().deleteAttributes((NameIdType)inObj);
			if(outBool == false){
				logger.debug("No extended attributes deleted for " + ((NameIdType)inObj).getName());
			}
		}
		
		ITypeSanitizer sanitizer = Factories.getSanitizer(NameEnumType.valueOf(type.toString()));
		if(sanitizer == null){
			logger.error(String.format(FactoryException.TYPE_NOT_REGISTERED,"Sanitizer"));
			return false;
		}

		INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(type.toString()));
		if(sanitizer.useAlternateDelete(type, inObj)){
			outBool = sanitizer.delete(type, inObj);
		}
		else{
			outBool = iFact.delete(inObj);
		}
	
		return outBool;
	}
	public static <T> T getFactory(AuditEnumType type) throws FactoryException{
		return Factories.getFactory(FactoryEnumType.valueOf(type.toString()));
		
	}
	private static <T> T getByUrn(AuditEnumType type, UserType user, String urn) throws ArgumentException, FactoryException {
		NameIdFactory factory = getFactory(type);
		T outObj = factory.getByUrn(urn);
		
		if(outObj == null) return null;
		return postFetchObject(type, user, outObj);
	}
	private static <T> T getByObjectId(AuditEnumType type, UserType user, String id, long organizationId) throws ArgumentException, FactoryException {
		NameIdFactory factory = getFactory(type);
		T outObj = factory.getByObjectId(id, organizationId);
		
		if(outObj == null) return null;
		return postFetchObject(type, user, outObj);
	}
	private static <T> T postFetchObject(AuditEnumType type, UserType user, T obj) throws ArgumentException, FactoryException{
		populate(type, obj);
		denormalize(obj);

		ITypeSanitizer sanitizer = Factories.getSanitizer(NameEnumType.valueOf(type.toString()));
		if(sanitizer == null){
			logger.error(String.format(FactoryException.TYPE_NOT_REGISTERED,"Sanitizer"));
			return obj;
		}
		
		if(enableExtendedAttributes){
			Factories.getAttributeFactory().populateAttributes((NameIdType)obj);
		}
		if(sanitizer.usePostFetch(type, obj)){
			obj = sanitizer.postFetch(type, user, obj);
		}

		return obj;		
	}
	private static <T> T getById(AuditEnumType type, UserType user, long id, long organizationId) throws ArgumentException, FactoryException {
		NameIdFactory factory = getFactory(type);
		T outObj = factory.getById(id, organizationId);
		
		if(outObj == null) return null;
		
		return postFetchObject(type, user, outObj);
	}
	private static <T> T getByNameInParent(AuditEnumType type, String name, String otype, NameIdType parent) throws ArgumentException, FactoryException {
		
		T outObj = null;
		INameIdFactory factory = getFactory(type);
		if(factory.isClusterByGroup() && (otype == null || otype.length() == 0 || otype.equalsIgnoreCase("unknown"))){
			INameIdGroupFactory gfact = (INameIdGroupFactory)factory;
			outObj = gfact.getByNameInParent(name, otype, (NameIdDirectoryGroupType)parent);
		}
		else if(factory.isClusterByParent()){
			outObj = factory.getByNameInParent(name, otype, parent.getId(), parent.getOrganizationId());
		}
		if(outObj != null){
			populate(type, outObj);
			denormalize(outObj);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes((NameIdType)outObj);
			}

		}
		return outObj;		
	}
	
	
	@SuppressWarnings("unchecked")
	private static <T> T getByNameInGroup(AuditEnumType type, UserType user, String name, DirectoryGroupType group) throws ArgumentException, FactoryException {
		
		T outObj = null;
		INameIdFactory iFact = getFactory(type);
		if(iFact.isClusterByGroup()){
			outObj = ((INameIdGroupFactory)iFact).getByNameInGroup(name,group);
		}
		else if(type.equals(AuditEnumType.DATA)){
			outObj = (T)((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name, group);
			if(outObj == null){
				logger.debug("Data '" + name + "' is null");
				return outObj;
			}
			outObj = postFetchObject(type, user, outObj);
		}
		
		if(outObj != null){
			populate(type, outObj);
			denormalize(outObj);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes((NameIdType)outObj);
			}
		}
		return outObj;		
	}
	
	@SuppressWarnings("unchecked")
	private static <T> T getByNameInGroup(AuditEnumType type, String name, long organizationId) throws ArgumentException, FactoryException {
		logger.error("***** DEPRECATE getByNameInGroup AND ITS ENTIRE TRACE");
		T outObj = null;
		switch(type){
			case ROLE:
				outObj = (T)((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleByName(name, organizationId);
				break;
			case USER:
				outObj = (T)Factories.getNameIdFactory(FactoryEnumType.USER).getByName(name, organizationId);
				break;
			default:
				break;
		}
		if(outObj != null){
			populate(type, outObj);
			denormalize(outObj);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes((NameIdType)outObj);
			}

		}
		return outObj;		
	}
	
	public static <T> void populate(AuditEnumType type,T object){
		try {
			Factories.populate(FactoryEnumType.valueOf(type.toString()), object);
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
	
	}

	public static boolean canViewType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		return AuthorizationService.canView(user, obj);
	}
	public static boolean canCreateType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		return AuthorizationService.canCreate(user, obj);
	}
	public static boolean canChangeType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		return AuthorizationService.canChange(user, obj);
	}
	public static boolean canDeleteType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		return AuthorizationService.canDelete(user, obj);
	}
	public static boolean canExecuteType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		return AuthorizationService.canExecute(user, obj);
	}
	
	/// Duped in AuthorizationService, except the type is taken from the object instead of from the AuditEnumType
	private static <T> boolean authorizeRoleType(AuditEnumType type, UserType adminUser, BaseRoleType targetRole, T bucket, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		return AuthorizationService.authorizeType(adminUser, targetRole, (NameIdType)bucket, view, edit, delete, create);
	}
	/// Duped in AuthorizationService, except the type is taken from the object instead of from the AuditEnumType
	private static <T> boolean authorizeUserType(AuditEnumType type, UserType adminUser, UserType targetUser, T bucket, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		return AuthorizationService.authorizeType(adminUser, targetUser, (NameIdType)bucket, view, edit, delete, create);
	}
	
	public static <T> boolean authorizeRole(AuditEnumType type, long organizationId, long targetRoleId, T bucket, boolean view, boolean edit, boolean delete, boolean create, HttpServletRequest request){

		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, "authorizeRole", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;
		return authorizeRole(type, organizationId, targetRoleId, bucket, view, edit, delete, create, user);
	}
	public static <T> boolean authorizeRole(AuditEnumType type, long organizationId, long targetRoleId, T bucket, boolean view, boolean edit, boolean delete, boolean create, UserType user){
		boolean outBool = false;

		BaseRoleType targetRole = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, "authorizeRole", AuditEnumType.USER, user.getUrn());

		NameIdType typeBean = (NameIdType)bucket;
		AuditService.targetAudit(audit, type, (typeBean == null ? "null" : UrnUtil.getUrn(typeBean)));

		try {
			if(canChangeType(type, user, typeBean)){
				targetRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getById(targetRoleId, organizationId);
				if(targetRole != null){
					if(authorizeRoleType(type, user, targetRole, bucket, view, edit, delete, create)){
						EffectiveAuthorizationService.rebuildPendingRoleCache();
						AuditService.permitResult(audit, "Applied authorization policy updates for role #" + targetRoleId + " " + targetRole.getName());
						outBool = true;
					}
				}
				else{
					AuditService.denyResult(audit, "Target user #" + targetRoleId + " in organization #" + organizationId + " does not exist");
				}
			}
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(e);
			AuditService.denyResult(audit, e.getMessage());
		}
		return outBool;
	}
	public static <T> boolean authorizeUser(AuditEnumType type, long organizationId, long targetUserId, T bucket, boolean view, boolean edit, boolean delete, boolean create, HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, "authorizeUser", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;
		return authorizeUser(type, organizationId, targetUserId, bucket, view, edit, delete, create, user);
	}
	public static <T> boolean authorizeUser(AuditEnumType type, long organizationId, long targetUserId, T bucket, boolean view, boolean edit, boolean delete, boolean create, UserType user){
		boolean outBool = false;

		UserType targetUser = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, "authorizeUser", AuditEnumType.USER, user.getUrn());
		NameIdType typeBean = (NameIdType)bucket;
		AuditService.targetAudit(audit, type, (typeBean == null ? "null" : UrnUtil.getUrn(typeBean)));

		try {
			if(canChangeType(type, user, typeBean)){
				targetUser = Factories.getNameIdFactory(FactoryEnumType.USER).getById(targetUserId, organizationId);
				if(targetUser != null){
					if(authorizeUserType(type, user, targetUser, bucket, view, edit, delete, create)){
						EffectiveAuthorizationService.rebuildPendingRoleCache();
						AuditService.permitResult(audit, "Applied authorization policy updates for user #" + user.getId() + " " + user.getName());
						outBool = true;
					}
				}
				else{
					AuditService.denyResult(audit, "Target user #" + targetUserId + " in organization #" + organizationId + " does not exist");
				}
			}
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(e);
			AuditService.denyResult(audit, e.getMessage());
		}
		return outBool;
	}
	public static <T> boolean delete(AuditEnumType type, T bean, HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.DELETE, "delete", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;
		return delete(type, bean, user);
	}	
	public static <T> boolean delete(AuditEnumType type, T bean, UserType user){
		
		boolean outBool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.DELETE, "delete", AuditEnumType.USER, user.getUrn());
		NameIdType typeBean = (NameIdType)bean;
		AuditService.targetAudit(audit, type, (typeBean == null ? "null" : UrnUtil.getUrn(typeBean)));

		try {
			normalize(user,typeBean);
			if(typeBean.getId() <= 0){
				AuditService.denyResult(audit,"Bean contains invalid data");
				return outBool;
			}
			if(canDeleteType(type, user, typeBean)){
				outBool = deleteObject(type, bean);
				if(outBool) AuditService.permitResult(audit, "Deleted " + typeBean.getName());
				else AuditService.denyResult(audit, "Unable to delete " + typeBean.getName());
				
			}
			else{
				AuditService.denyResult(audit, "User is not authorized");
				logger.error("User is not authorized to delete the object object '" + typeBean.getName() + "' #" + typeBean.getId());
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
			AuditService.denyResult(audit, e1.getMessage());
		}

		return outBool;
	}
	public static <T> boolean add(AuditEnumType addType, T bean, HttpServletRequest request){
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "add", AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return false;
		return add(addType, bean, user);
	}
	public static <T> boolean add(AuditEnumType addType, T bean, UserType user){
		return add(addType, bean, user, null);
	}
	public static <T> boolean add(AuditEnumType addType, T bean, UserType user, String sessionId){
		
		boolean outBool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "add", AuditEnumType.USER, user.getUrn());
		NameIdType dirBean = (NameIdType)bean;
		AuditService.targetAudit(audit, addType, (dirBean == null ? "null" : dirBean.getName()));
		if(dirBean == null){
			AuditService.denyResult(audit, "Null directory");
			return false;
		}
		if(dirBean.getNameType() == NameEnumType.UNKNOWN){
			logger.warn("NameType not specified.  Setting as " + addType.toString());
			dirBean.setNameType(NameEnumType.valueOf(addType.toString()));
		}
		try {
			
			normalize(user,bean);

			if(canCreateType(addType, user, dirBean)){
				outBool = sanitizeAddNewObject(addType, user, bean, sessionId);
				if(sessionId == null && outBool && enableExtendedAttributes){
					NameIdType beanObj = (NameIdType)bean;
					if(beanObj.getAttributes().size() > 0){
						NameIdType obj = null;
						INameIdFactory iFact = getFactory(addType);
						if(iFact.isClusterByGroup() || addType == AuditEnumType.DATA) obj = readByName(addType,((NameIdDirectoryGroupType)bean).getGroupId(),((NameIdDirectoryGroupType)bean).getName(),user);
						else obj = readByName(addType,beanObj.getName(),user);
						if(obj != null){
							obj.getAttributes().addAll(beanObj.getAttributes());
							outBool = Factories.getAttributeFactory().updateAttributes((NameIdType)obj);
							if(!outBool) logger.warn("Failed to persist attributes");
						}
						else{
							logger.warn("Failed to update extended attributes");
						}
					}
					else{
						logger.info("No attributes defined for add operation");
					}
				}
				else {
					logger.debug("Skip adding attributes because " + sessionId + " || " + outBool + " || " + enableExtendedAttributes + " was false");
				}

				if(outBool) AuditService.permitResult(audit, "Added " + dirBean.getName());
				else AuditService.denyResult(audit, "Unable to add " + dirBean.getName());
				
			}
			else{
				AuditService.denyResult(audit, "User is not authorized");
				logger.error("User is not authorized to add the object  '" + dirBean.getName());
			}
		} catch (ArgumentException | FactoryException | DataException | DataAccessException e) {
			
			logger.error(e);
			AuditService.denyResult(audit, e.getMessage());
		}

		return outBool;
	}
	public static <T> boolean update(AuditEnumType type, T bean,HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "update",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) {
			logger.error("User is null for session ");
			return false;
		}
		return update(type, bean, user);
	}
	public static <T> boolean update(AuditEnumType type, T bean,UserType user){
		return update(type, bean, user, null);
	}
	public static <T> boolean update(AuditEnumType type, T bean,UserType user, String sessionId){
		boolean outBool = false;
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "update",AuditEnumType.USER, user.getUrn());
		NameIdType dirBean = (NameIdType)bean;
		AuditService.targetAudit(audit, type, (dirBean == null ? "null" : UrnUtil.getUrn(dirBean)));
		
		if(dirBean == null){
			logger.error("Object to update is null");
			AuditService.denyResult(audit, "Null value");
			return false;
		}

		try {
			normalize(user,bean);
			
			/// 2015/06/22
			/// Add in restriction to block ownership changes via an update
			///
			NameIdType matBean = readById(type,dirBean.getId(),user);
			if(matBean == null){
				AuditService.denyResult(audit, "Unable to read original object");
				return false;
			}

			if(dirBean.getOwnerId().compareTo(matBean.getOwnerId()) != 0){
				AuditService.denyResult(audit, "Chown operation is forbidden in an update operation");
				return false;
			}

			if(canChangeType(type, user, dirBean)){
				if(sessionId == null) outBool = updateObject(type, user, bean);
				else {
					 BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.valueOf(type.toString()), (NameIdType)bean);
				}
				if(outBool) AuditService.permitResult(audit, "Updated " + dirBean.getName() + " (#" + dirBean.getId() + ")");
				else AuditService.denyResult(audit, "Unable to update " + dirBean.getName() + " (#" + dirBean.getId() + ")");
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to change object '" + dirBean.getName() + "' #" + dirBean.getId());
			}
		} catch (ArgumentException | FactoryException e) {
			
			logger.error(e);
			AuditService.denyResult(audit, e.getMessage());
		}

		return outBool;
	}
	
	
	public static <T> T readByUrn(AuditEnumType type, String urn,HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByUrn",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;
		return readByUrn(type, urn, user);
	}
	@SuppressWarnings("unchecked")
	public static <T> T readByUrn(AuditEnumType type, String urn,UserType user){
		T outObj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByUrn",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, type, urn);
		
		try {
			
			NameIdType dirType = getByUrn(type,user, urn);
			if(dirType == null){
				AuditService.denyResult(audit, urn + " (" + type + ") doesn't exist in organization " + user.getOrganizationId());
				return null;
			}		
			AuditService.targetAudit(audit, type, dirType.getUrn());
			if(canViewType(type, user, dirType)){
				outObj = (T)dirType;
				if(dirType.getNameType().equals(NameEnumType.DATA) && ((DataType)outObj).getPointer() && allowDataPointers == false){
					AuditService.denyResult(audit, urn + " (" + type + ") is a data pointer, and reading data pointers from the Web FE is forbidden by configuration.");
					outObj = null;
				}
				else{
					AuditService.permitResult(audit, "Read " + dirType.getName() + " (#" + dirType.getId() + ")");
				}
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + dirType.getName() + "' #" + dirType.getId());
			}

		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
		} 

		return outObj;
	}
	
	public static <T> T readByObjectId(AuditEnumType type, String id,HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByObjectId",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;
		return readByObjectId(type, id, user);
	}
	@SuppressWarnings("unchecked")
	public static <T> T readByObjectId(AuditEnumType type, String id,UserType user){
		T outObj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByObjectId",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, type, id);
		
		try {
			
			NameIdType dirType = getByObjectId(type,user, id, user.getOrganizationId());
			if(dirType == null){
				AuditService.denyResult(audit, "#" + id + " (" + type + ") doesn't exist in organization " + user.getOrganizationId());
				return null;
			}		
			AuditService.targetAudit(audit, type, dirType.getUrn());
			if(canViewType(type, user, dirType)){
				outObj = (T)dirType;
				if(dirType.getNameType().equals(NameEnumType.DATA) && ((DataType)outObj).getPointer() && allowDataPointers == false){
					AuditService.denyResult(audit, "#" + id + " (" + type + ") is a data pointer, and reading data pointers from the Web FE is forbidden by configuration.");
					outObj = null;
				}
				else{
					AuditService.permitResult(audit, "Read " + dirType.getName() + " (#" + dirType.getId() + ")");
				}
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + dirType.getName() + "' #" + dirType.getId());
			}

		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
		} 

		return outObj;
	}
	public static <T> T readById(AuditEnumType type, long id,HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readById",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;
		return readById(type, id, user);
	}
	@SuppressWarnings("unchecked")
	public static <T> T readById(AuditEnumType type, long id,UserType user){
		T outObj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readById",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, type, Long.toString(id));

		try {
			
			NameIdType dirType = getById(type, user, id, user.getOrganizationId());
			if(dirType == null){
				AuditService.denyResult(audit, "#" + id + " (" + type + ") doesn't exist in organization " + user.getOrganizationId());
				return null;
			}		
			AuditService.targetAudit(audit, type, dirType.getUrn());
			if(canViewType(type, user, dirType)){
				outObj = (T)dirType;
				if(dirType.getNameType().equals(NameEnumType.DATA) && ((DataType)outObj).getPointer() && allowDataPointers == false){
					AuditService.denyResult(audit, "#" + id + " (" + type + ") is a data pointer, and reading data pointers from the Web FE is forbidden by configuration.");
					outObj = null;
				}
				else{
					AuditService.permitResult(audit, "Read " + dirType.getName() + " (#" + dirType.getId() + ")");
				}
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + dirType.getName() + "' #" + dirType.getId());
			}

		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
		} 

		return outObj;
	}	
	public static <T> T readByName(AuditEnumType type, String name,HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;
		return readByName(type, name, user);
	}
	public static <T> T readByName(AuditEnumType type, String name,UserType user){
		DirectoryGroupType dir = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, type, name);


		try{
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateUserDirectory(user, getDefaultGroupName(type));
		}
		 catch (FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(e);
		} 
		return readByName(audit,type, user, dir, name);
	}
	public static <T> T readByName(AuditEnumType type, long groupId, String name,HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;
		return readByName(type, groupId, name, user);
	}
	public static <T> T readByName(AuditEnumType type, long groupId, String name,UserType user){

		DirectoryGroupType dir = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, type, name);


		try{
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getById(groupId, user.getOrganizationId());
		}
		 catch (FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(e);
		} 
		return readByName(audit,type, user, dir, name);
	}
	public static <T> T readByName(AuditEnumType type, DirectoryGroupType dir, String name,HttpServletRequest request){
		T outObj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return outObj;
		return readByName(audit,type, user, dir, name);
	}
	public static <T> T readByName(AuditEnumType type, DirectoryGroupType dir, String name,UserType user){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, type, name);
		return readByName(audit,type, user, dir, name);
	}
	public static <T> T readByName(AuditType audit,AuditEnumType type, UserType user, DirectoryGroupType dir, String name){
		T outObj = null;
		if(dir == null){
			logger.error("Directory Group is null");
			return null;
		}
		try {
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dir);
			outObj = getByNameInGroup(type, user, name, dir);
			if(outObj == null){
				AuditService.denyResult(audit, "'" + name + "' doesn't exist");
				return null;
			}
			AuditService.targetAudit(audit, type, ((NameIdType)outObj).getUrn());
			if(canViewType(type, user, (NameIdType)outObj)){
				if(((NameIdType)outObj).getNameType().equals(NameEnumType.DATA) && ((DataType)outObj).getPointer() && allowDataPointers == false){
					AuditService.denyResult(audit, name + " is a data pointer, and reading data pointers from the Web FE is forbidden by configuration.");
					outObj = null;
				}
				else{
					AuditService.permitResult(audit, "Read " + name + " (#" + ((NameIdType)outObj).getId() + ")");
				}

			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + dir.getName() + "' #" + dir.getId());
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
		} 

		return outObj;
	}
	
	public static <T> T readByNameInParent(AuditEnumType type, NameIdType parent, String name, String otype, HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByNameInParent",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;
		return readByNameInParent(audit,type, user, parent, name, otype);
	}
	public static <T> T readByNameInParent(AuditEnumType type, NameIdType parent, String name, String otype, UserType user){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByNameInParent",AuditEnumType.USER, user.getUrn());
		return readByNameInParent(audit,type, user, parent, name, otype);
	}
	public static <T> T readByNameInParent(AuditType audit,AuditEnumType type, UserType user, NameIdType parent, String name,String otype){
		T outObj = null;
		AuditService.targetAudit(audit, type, name);
		try {

			outObj = getByNameInParent(type, name, otype, parent);
			if(outObj == null){
				AuditService.denyResult(audit, "'" + name + "' doesn't exist");
				return null;
			}
			if(canViewType(type, user, (NameIdType)outObj)){
				AuditService.permitResult(audit, "Read " + name + " (#" + ((NameIdType)outObj).getId() + ")");

			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + parent.getName() + "' #" + parent.getId());
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
		} 

		return outObj;
	}
	
	public static <T> T readByNameInOrganization(AuditEnumType type, long organizationId, String name,HttpServletRequest request){

		logger.debug("***** DEPRECATE readByNameInOrganization AND ITS ENTIRE TRACE");
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));

		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;

		return readByName(audit,type, user, organizationId, name);
	}
	public static <T> T readByNameInOrganization(AuditEnumType type, long organizationId, String name,UserType user){

		logger.debug("***** DEPRECATE readByNameInOrganization AND ITS ENTIRE TRACE");
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.USER, user.getUrn());
		return readByName(audit,type, user, organizationId, name);
	}

	public static <T> T readByName(AuditType audit,AuditEnumType type, UserType user, long organizationId, String name){
		T outObj = null;
		AuditService.targetAudit(audit, type, name);
		logger.debug("***** DEPRECATE readByName AND ITS ENTIRE TRACE");
		try {
			outObj = getByNameInGroup(type, name, organizationId);
			if(outObj == null){
				AuditService.denyResult(audit, "'" + name + "' doesn't exist");
				return null;
			}
			AuditService.targetAudit(audit, type, ((NameIdType)outObj).getUrn());
			if(canViewType(type, user, (NameIdType)outObj)){
				AuditService.permitResult(audit, "Read " + name + " (#" + ((NameIdType)outObj).getId() + ")");
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view object '" + ((NameIdType)outObj).getName() + "' #" + ((NameIdType)outObj).getId());
				outObj = null;
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
			outObj = null;
		} 

		return outObj;
	}
	
	public static int countByGroup(AuditEnumType type, BaseGroupType parentGroup, HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "count",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, parentGroup.getUrn());
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return 0;
		return count(audit,type, user, parentGroup);
	}
	public static int countByGroup(AuditEnumType type, BaseGroupType parentGroup, UserType user){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "count",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, type, parentGroup.getUrn());
		return count(audit,type, user, parentGroup);
	}

	public static int countByGroup(AuditEnumType type, String path, HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "count",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return 0;
		return countByGroup(type, path, request);
	}
	public static int countByGroup(AuditEnumType type, String path, UserType user){
		BaseGroupType dir = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "count",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, type, path);

		try{
			dir = (BaseGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.UNKNOWN,path, user.getOrganizationId());
		}
		 catch (FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(e);
		} 
		if(dir == null){
			AuditService.denyResult(audit, "Path '" + path + "' does not exist");
			return 0;
		}
		return count(audit,type, user, dir);
	}
	public static int count(AuditType audit,AuditEnumType type, UserType user, BaseGroupType dir){
		int outCount = 0;
		try {
			if(canViewType(AuditEnumType.GROUP, user, dir)){
				outCount = count(type, dir);
				AuditService.permitResult(audit, "Count " + outCount + " of " + type.toString() + " in '" + dir.getName() + "' #" + dir.getId());
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view objects in the specified group '" + dir.getName() + "' #" + dir.getId());
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
		} 

		return outCount;
	}
	private static int count(AuditEnumType type, BaseGroupType group) throws ArgumentException, FactoryException {
		
		NameIdFactory factory = getFactory(type);
		if(type == AuditEnumType.DATA){
			if(group.getGroupType() == GroupEnumType.DATA) return ((DataFactory)factory).getCount((DirectoryGroupType)group);
			return 0;
		}
		return ((NameIdGroupFactory)factory).countInGroup(group);		
	}
	
	public static int countByOrganization(AuditEnumType type, long organizationId, HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "count",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, Long.toString(organizationId));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return 0;
		return countByOrganization(audit,type, user, organizationId);
	}
	public static int countByOrganization(AuditEnumType type, long organizationId, UserType user){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "count",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, type, Long.toString(organizationId));
		return countByOrganization(audit,type, user, organizationId);
	}
	public static int countInParent(AuditEnumType type, NameIdType parent, HttpServletRequest request){

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "countByParent",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, UrnUtil.getUrn(parent));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user == null) return 0;

		return countInParent(audit,type, user, parent);
	}
	public static int countInParent(AuditEnumType type, NameIdType parent, UserType user){

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "countByParent",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, type, UrnUtil.getUrn(parent));
		return countInParent(audit,type, user, parent);
	}

	public static int countByOrganization(AuditType audit,AuditEnumType type, UserType user, long organizationId){
		int outCount = 0;
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
				outCount = count(type, organizationId);
				AuditService.permitResult(audit, "Count " + outCount + " of " + type.toString());
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to count directly in organization " + organizationId);
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
		} 

		return outCount;
	}
	public static int countInParent(AuditType audit,AuditEnumType type, UserType user, NameIdType parent){
		int outCount = 0;
		try {
			if(canViewType(type,user, parent)){
				outCount = countInParent(type, parent);
				AuditService.permitResult(audit, "Count " + outCount + " of " + type.toString());
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to count in parent '" + parent.getName() + "' #" + parent.getId());
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
		} 

		return outCount;
	}
	
	private static int count(AuditEnumType type, long organizationId) throws FactoryException {
		NameIdFactory factory = getFactory(type);
		return factory.countInOrganization(organizationId);
	}
	private static int countInParent(AuditEnumType type, NameIdType parent) throws FactoryException {
		NameIdFactory factory = getFactory(type);
		return factory.countInParent(parent);
	}
	
	public static <T> List<T> listSystemEntitlements(AuditEnumType type, HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "listSystemEntitlements",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		return listSystemEntitlements(type, user);
	}
	
	public static <T> List<T> listSystemEntitlements(AuditEnumType type, UserType user){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "listSystemEntitlements",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, type, "System entitlements");

		List<T> outList = new ArrayList<>();
		boolean canRead = false;
		BaseRoleType roleReader = null;
		if(type != AuditEnumType.ROLE && type != AuditEnumType.PERMISSION) {
			AuditService.denyResult(audit, "Only role and permission entitlement types are supported");
			return outList;
		}
		try {
			boolean accountAdmin = RoleService.isFactoryAdministrator(user,((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)));
			boolean dataAdmin = RoleService.isFactoryAdministrator(user, ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)));
			if(accountAdmin || dataAdmin) {
				canRead = true;
			}
			else {
				if(type == AuditEnumType.ROLE) roleReader = RoleService.getRoleReaderUserRole(user.getOrganizationId());
				else if(type == AuditEnumType.PERMISSION) roleReader = RoleService.getPermissionReaderUserRole(user.getOrganizationId());
				canRead = RoleService.getIsUserInRole(roleReader, user);
			}
			
		} catch (FactoryException | ArgumentException e) {
			e.printStackTrace();
		}
		if(!canRead) {
			AuditService.permitResult(audit, "User is not authorized to read system roles");
			return outList;
		}
		if(type == AuditEnumType.ROLE) outList = FactoryBase.convertList(RoleService.getSystemRoles(user.getOrganizationId()));
		else if(type == AuditEnumType.PERMISSION) outList = FactoryBase.convertList(PermissionService.getSystemPermissions(user.getOrganizationId()));
		
		AuditService.permitResult(audit, "Returning " + outList.size() + " entitlements");
		return outList;
	}
	
	public static <T> List<T> listByParentObjectId(AuditEnumType type, String parentType, String parentId, long startRecord, int recordCount, HttpServletRequest request){

		List<T> outObj = new ArrayList<>();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "listByParentObjectId",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return outObj;
		return listByParentObjectId(type, parentType, parentId, startRecord, recordCount, user);
	}
	
	public static <T> List<T> listByParentObjectId(AuditEnumType type, String parentType, String parentId, long startRecord, int recordCount, UserType user){

		List<T> outObj = new ArrayList<>();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "listByParentObjectId",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, type, parentId);
		
		NameIdType parentObj = BaseService.readByObjectId(type, parentId, user);
		if(parentObj == null){
			AuditService.denyResult(audit, "Null parent id");
			return outObj;
		}
		
		AuditService.targetAudit(audit, type, parentObj.getUrn());
		
		try {
			if(
				AuthorizationService.canView(user, parentObj)
				||
				RoleService.isFactoryReader(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)),parentObj.getOrganizationId())
			){
				AuditService.permitResult(audit, "Access authorized to list objects");
				outObj = getListByParent(type,parentType,parentObj,startRecord,recordCount,parentObj.getOrganizationId() );
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list roles.");
				return outObj;
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
		} 

		return outObj;
	}
	

	private static <T> List<T> getListByParent(AuditEnumType type,String parentType, NameIdType parentObj, long startIndex, int recordCount, long organizationId){
		List<T> dirs = new ArrayList<>();

		if(parentObj == null) return dirs;
		try {
			INameIdFactory iFact = getFactory(type);
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
			logger.error(e);
		}


		return dirs;
	}
	
	private static ProcessingInstructionType getSearchInstruction(BaseSearchRequestType search){
		
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		if(search != null){
			instruction.setPaginate(search.getRecordCount() > 0);
			instruction.setRecordCount(search.getRecordCount());
			instruction.setStartIndex(search.getStartRecord());
			if(search.getSort() != null){
				instruction.setOrderClause(search.getSort().getSortField().toString().toLowerCase() + " " + (search.getSort().getSortOrder() == OrderEnumType.ASCENDING ? "ASC" : "DESC"));
			}
		}
		return instruction;
	}
	
	private static <T> List<T> getListByGroup(AuditEnumType type, BaseGroupType group,BaseSearchRequestType search) throws ArgumentException, FactoryException {
		List<T> outObj = new ArrayList<>();
		INameIdFactory iFact = getFactory(type);
		if(iFact.isClusterByGroup()){
			INameIdGroupFactory iGFact = (INameIdGroupFactory)iFact;
			outObj = iGFact.listInGroup(group, search.getStartRecord(), search.getRecordCount(), group.getOrganizationId());
		}
		else if(iFact.getFactoryType().equals(FactoryEnumType.DATA)){
			ProcessingInstructionType inst = getSearchInstruction(search);
			if(group.getGroupType() == GroupEnumType.DATA) outObj = FactoryBase.convertList(((DataFactory)iFact).getDataListByGroup((DirectoryGroupType)group,inst, !search.getFullRecord(),search.getStartRecord(), search.getRecordCount(), group.getOrganizationId()));
			/// else if(group.getGroupType() == GroupEnumType.BUCKET) outObj = FactoryBase.convertList(((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getDataInGroup((BucketGroupType)group));
		}
		for(int i = 0; i < outObj.size();i++){

			NameIdType nt = (NameIdType)outObj.get(i);
			denormalize(nt);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes(nt);
			}

		}

		return outObj;			
	}
	private static <T> List<T> getListByOrganization(AuditEnumType type, long startRecord, int recordCount, long organizationId) throws FactoryException, ArgumentException{
		List<T> outObj = new ArrayList<>();
		INameIdFactory iFact = getFactory(type);
		if(type.equals(AuditEnumType.USER)){
			outObj = FactoryBase.convertList(((UserFactory)iFact).getUserList(startRecord, recordCount, organizationId));
		}
		for(int i = 0; i < outObj.size();i++){
			NameIdType nt = (NameIdType)outObj.get(i);
			denormalize(nt);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes(nt);
			}

		}

		return outObj;	
	}
	public static <T> List<T> listByOrganization(AuditEnumType type, long startRecord, int recordCount, HttpServletRequest request){
		List<T> outObj = new ArrayList<>();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All objects",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit,request);

		if(user == null) return outObj;
		return listByOrganization(type, startRecord, recordCount, user);
	}
	public static <T> List<T> listByOrganization(AuditEnumType type, long startRecord, int recordCount, UserType user){
		List<T> outObj = new ArrayList<>();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All objects",type,(user == null ? "Null" : user.getName()));
		if(user == null){
			AuditService.denyResult(audit, "Null user");
			return outObj;
		}
		AuditService.targetAudit(audit, AuditEnumType.USER, user.getUrn());

		try {
			if(
				RoleService.isFactoryAdministrator(user, ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)))
				||
				RoleService.isFactoryReader(user, ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)))
			){
				AuditService.permitResult(audit, "Access authorized to list users");
				outObj = getListByOrganization(type, startRecord,recordCount,user.getOrganizationId());
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list users.");
				return outObj;
			}
			
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
		} 

		return outObj;
	}

	public static <T> List<T> listByGroup(AuditEnumType type, String groupType, String groupId, long startRecord, int recordCount, HttpServletRequest request){
		List<T> outObj = new ArrayList<>();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "listByGroup",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, groupId);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return outObj;
		return listByGroup(type, groupType, groupId, startRecord, recordCount, user);
	}
	public static <T> List<T> listByGroup(AuditEnumType type, String groupType, String groupId, long startRecord, int recordCount, UserType user){
		BaseSearchRequestType search = new BaseSearchRequestType();
		SortQueryType sort = new SortQueryType();
		sort.setSortField(QueryEnumType.NAME);
		sort.setSortOrder(OrderEnumType.ASCENDING);
		search.setSort(sort);
		search.setPaginate(true);
		search.setRecordCount(recordCount);
		search.setStartRecord(startRecord);
		return listByGroup(type, groupType, groupId, search, user);
	}
	public static void prepareSearchReques(BaseSearchRequestType search){
		if(search == null) return;
		if(search.getSort() == null){
			SortQueryType sort = new SortQueryType();
			sort.setSortField(QueryEnumType.NAME);
			sort.setSortOrder(OrderEnumType.ASCENDING);
			search.setSort(sort);
		}
	}
	public static <T> List<T> listByGroup(AuditEnumType type, String groupType, String groupId, BaseSearchRequestType search, UserType user){
		List<T> outObj = new ArrayList<>();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "listByGroup",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, type, groupId);

		NameIdType parentObj = BaseService.readByObjectId(AuditEnumType.GROUP, groupId, user);
		if(parentObj == null){
			AuditService.denyResult(audit, "Null group object");
			return outObj;
		}
		
		AuditService.targetAudit(audit, type, parentObj.getUrn());
		try{
		if(AuthorizationService.canView(user, parentObj)){
			AuditService.permitResult(audit, "Access authorized to group " + parentObj.getName());
			outObj = getListByGroup(type,(BaseGroupType)parentObj,search);
		}
		else{
			AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + parentObj.getName() + " (#" + parentObj.getId() + ")");
			return outObj;
		}
		}
		catch(ArgumentException | FactoryException e){
			logger.error(e.getMessage());
			AuditService.denyResult(audit, e.getMessage());
		}
		return outObj;
	}

	public static <T> T findGroup(GroupEnumType groupType, String path, HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		if(user == null) return null;
		return findGroup(user,groupType,path);
	
	}
	@SuppressWarnings("unchecked")
	public static <T> T findGroup(UserType user,GroupEnumType groupType, String path){
		T bean = null;
		if(path == null || path.length() == 0) path = "~";
		if(path.startsWith("~") == false && path.startsWith("/") == false) path = "/" + path;
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.USER,user.getName());
		try {
			BaseGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, groupType, path, user.getOrganizationId());
			if(dir == null){
				AuditService.denyResult(audit, "Invalid type: " + groupType.toString() + " " + path);
				return bean;
			}
			if(!AuthorizationService.canView(user, dir)){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return bean;
			}
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dir);	
			denormalize(dir);
			bean = (T)dir;

			AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getUrn());
			AuditService.permitResult(audit, "Access authorized to group " + dir.getName());
			
			
			
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(e);
		}
		return bean;
	}
	public static <T> T makeFind(AuditEnumType auditType,String type, String path, HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.USER,request.getSession().getId());
		AuditService.targetAudit(audit, auditType, path);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user == null) return null;
		return makeFind(auditType, type, path, user);
	}
	public static <T> T makeFind(AuditEnumType auditType,String type, String path, UserType user){
		T obj = find(auditType, type, path, user);
		if(obj != null) return obj;
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.USER,user.getUrn());
		AuditService.targetAudit(audit, auditType, path);

		if(path == null){
			AuditService.denyResult(audit, "Null path");
			return null;
		}
		
		
		try{
			INameIdFactory iFact = getFactory(auditType);
			if(iFact.isClusterByParent()){
				/// This is a pretty specific implementation limited to paths within the user directory structure
				///
				if(path.startsWith("..") == false && path.startsWith("~") == false && path.startsWith("/Home/" + user.getName() + "/")==false){
					AuditService.denyResult(audit, "Paths can only be created from the home directory");
					return null;
				}
		
				obj = iFact.makePath(user, type, path, user.getOrganizationId());
				if(obj != null && canViewType(auditType, user, (NameIdType)obj) == false){
					AuditService.denyResult(audit, "User is not authorized to view object");
					obj = null;
				}
				else if(obj != null){
					iFact.denormalize(obj);
					iFact.populate(obj);
					AuditService.permitResult(audit, "User is authorized to create the path under their own path home");
				}
			}

			
		}
		catch(ArgumentException | FactoryException | DataAccessException e){
			logger.error(e.getMessage());
		}
		return obj;
		
	}
	public static <T> T find(AuditEnumType auditType,String type, String path, HttpServletRequest request){

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.USER,request.getSession().getId());
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;
		return find(auditType, type, path, user);
	}
	public static <T> T find(AuditEnumType auditType,String type, String path, UserType user){

		T outObj = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.USER,user.getUrn());
		AuditService.targetAudit(audit, auditType, path);

		if(path == null || path.length() == 0) path = "~";
		if(path.startsWith("~") == false && path.startsWith("/") == false) path = "/" + path;
		try {
			INameIdFactory iFact = getFactory(auditType);
			if(iFact.isClusterByParent()){
				outObj = iFact.find(user, type, path, user.getOrganizationId());
				if(outObj == null){
					AuditService.denyResult(audit, "Invalid path: " + type + " " + path);
					return null;
				}
				NameIdType objType = (NameIdType)outObj;
				AuditService.targetAudit(audit, auditType, objType.getUrn());
				if(AuthorizationService.canView(user, objType) == false){
					AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view object " + objType.getName() + " (#" + objType.getId() + ")");
					return null;
				}
				iFact.populate(outObj);
				denormalize(outObj);
				
				AuditService.permitResult(audit, "Access authorized to group " + objType.getName());
			}
			else{
				AuditService.denyResult(audit, "Factory does not support clustered find");
			}


		} catch (FactoryException | ArgumentException e) {
			
			logger.error(e);
		}
		return outObj;
	}
	
	// Convenience method for adding/removing members
	//
	public static boolean setMember(UserType user, AuditEnumType containerType, String containerId, AuditEnumType objType, String objId, boolean enable){

		boolean outBool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, containerType.toString() + " " + containerId,objType,"Object " + objId);
		NameIdType container = readByObjectId(containerType, containerId, user);
		NameIdType object = readByObjectId(objType, objId,user);
		if(container == null || object == null){
			AuditService.denyResult(audit, "Invalid container or object");
			return false;
		}
		try {
			if(BaseService.canChangeType(containerType, user, container) == false){
				AuditService.denyResult(audit, "User not permitted to change " + container.getUrn());
				return false;
			}

			switch(containerType){
				case GROUP:
					BaseGroupType group = (BaseGroupType)container;
					outBool = GroupService.switchActorInGroup(object, group, enable);
					break;
				case ROLE:
					BaseRoleType role = (BaseRoleType)container;
					outBool = RoleService.switchActorInRole(object, role, enable);
					break;
				default:
					logger.error(String.format(FactoryException.UNHANDLED_TYPE, containerType.toString()));
					break;
			}
			
			if(outBool){
				EffectiveAuthorizationService.pendUpdate(container);
				EffectiveAuthorizationService.pendUpdate(object);
				EffectiveAuthorizationService.rebuildPendingRoleCache();
				AuditService.permitResult(audit, "User " + user.getUrn() + " is authorized to change " + container.getUrn());
			}
			else{
				AuditService.denyResult(audit, "User " + user.getUrn() + " did not change authorization for " + container.getUrn());
			}
			

		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(e);
		}
		return outBool;
	}
	
	
	// TODO: aggregateEntitlementsForMember SHOULD be coming directly from EffectiveAuthorizationService.getEffectiveMemberEntitlements,
	// however, that method is currently hard-coded to require a specific object because to allow for a broad scan across uncached entitlement combinations
	// can cause a large db hit, particularly when unwinding nested group and role structures without a frame of reference
	//
	public static List<EntitlementType> aggregateEntitlementsForMember(UserType user, NameIdType obj){
		return aggregateEntitlementsForMember(user,obj,(obj.getNameType().equals(NameEnumType.USER)));
	}
	public static List<EntitlementType> aggregateEntitlementsForMember(UserType user, NameIdType obj, boolean unwindPersonUser){
		List<EntitlementType> ents = new ArrayList<>();
		FactoryEnumType memberType = FactoryEnumType.fromValue(obj.getNameType().toString());
		
		switch(obj.getNameType()) {
			case PERSON:
				PersonType p = (PersonType)obj;
				BaseService.populate(AuditEnumType.PERSON, p);
				if(unwindPersonUser == false) {
					for(UserType u : p.getUsers()) {
						ents.addAll(aggregateEntitlementsForMember(user, u, false));
					}
				}
				for(AccountType u : p.getAccounts()) {
					ents.addAll(aggregateEntitlementsForMember(user, u, false));
				}
			case ACCOUNT:
			case USER:
				if(unwindPersonUser && obj.getNameType().equals(NameEnumType.USER)) {
					UserType u = (UserType)obj;
					List<PersonType> persons = UserService.readPersonsForUser(user, u, false);
					for(PersonType pu : persons) {
						ents.addAll(aggregateEntitlementsForMember(user, pu, false));
					}
				}

				List<BaseGroupType> groups = listForMember(AuditEnumType.GROUP, user, obj, memberType);
				for(BaseGroupType group : groups){
					ents.add(EffectiveAuthorizationService.copyAsEntitlement(obj, null, group, obj.getOrganizationId()));
				}
				List<BaseRoleType> roles = listForMember(AuditEnumType.ROLE, user, obj, memberType);
				for(BaseRoleType role : roles) {
					ents.add(EffectiveAuthorizationService.copyAsEntitlement(obj, null, role, obj.getOrganizationId()));
				}
				logger.info("Aggregate " + groups.size() + " groups and " + roles.size() + " roles for " + obj.getUrn() + " in " + obj.getOrganizationId());
			default:
				break;
		}
		return ents;
	}
	public static <T> List<T> listForMember(AuditEnumType type, UserType user, NameIdType obj, FactoryEnumType memberType){
		List<T> outObj = new ArrayList<T>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All objects for member",type,(user == null ? "Null" : user.getUrn()));
		AuditService.targetAudit(audit, type, "All members for object");
		
		if(user == null){
			/* || !SessionSecurity.isAuthenticated(user) */
			AuditService.denyResult(audit, "User is null or not authenticated");
			return outObj;
		}
		if(obj == null){
			AuditService.denyResult(audit, "Target object is null");
			return outObj;
		}

		try {
			if(memberType == FactoryEnumType.GROUP || memberType == FactoryEnumType.ROLE){
				if(
						AuthorizationService.isMapOwner(user, obj)
						||
						RoleService.isFactoryAdministrator(user,((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)),user.getOrganizationId())
						||
						RoleService.isFactoryReader(user,((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)), user.getOrganizationId())
					){
						AuditService.permitResult(audit, "Access authorized to list roles");
						switch(memberType){
							case GROUP:
								outObj = FactoryBase.convertList(((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getRolesInGroup((BaseGroupType)obj));
								break;
							case DATA:
								outObj = FactoryBase.convertList(((DataParticipationFactory)Factories.getFactory(FactoryEnumType.DATAPARTICIPATION)).getRolesForData((DataType)obj));
								break;
							default:
								logger.error(String.format(FactoryException.UNHANDLED_TYPE, memberType));
								break;
							
						}
						
						
					}
					else{
						AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list roles.");
						return outObj;
					}
			}
			else if(type.equals(AuditEnumType.GROUP)) {
				if(AuthorizationService.canView(user,obj)) {
			
					AuditService.permitResult(audit, "Access authorized to list groups");
					switch(memberType){
	
						case ACCOUNT:
							outObj = FactoryBase.convertList(((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getGroupAccounts((AccountType)obj));
							break;
						case PERSON:
							outObj = FactoryBase.convertList(((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getGroupPersons((PersonType)obj));
							break;
						case USER:
							outObj = FactoryBase.convertList(((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getGroupUsers((UserType)obj));
							break;
						default:
							logger.error(String.format(FactoryException.UNHANDLED_TYPE, memberType));
							break;
					}
					for(int i = 0; i < outObj.size();i++){
						((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize((BaseGroupType)outObj.get(i));
					}
	
				}
				else{
					AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list roles.");
					return outObj;
				}
			}
			else if(type.equals(AuditEnumType.ROLE)) {
				if(AuthorizationService.canView(user,obj)) {
			
					AuditService.permitResult(audit, "Access authorized to list roles");
					switch(memberType){
	
						case ACCOUNT:
							outObj = FactoryBase.convertList(((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getAccountRoles((AccountType)obj));
							break;
						case PERSON:
							outObj = FactoryBase.convertList(((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getPersonRoles((PersonType)obj));
							break;
						case USER:
							outObj = FactoryBase.convertList(((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getUserRoles((UserType)obj));
							break;
						default:
							logger.error(String.format(FactoryException.UNHANDLED_TYPE, memberType));
							break;
					}
					for(int i = 0; i < outObj.size();i++){
						((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).denormalize((BaseRoleType)outObj.get(i));
					}
	
				}
				else{
					AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list roles.");
					return outObj;
				}
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
		} 

		return outObj;
		
	}
	
	
	/// Convenience method for populating membership lists, such as members of roles, permissions, etc
	
	public static <T> List<T> listMembers(AuditEnumType type, UserType user,NameIdType container, FactoryEnumType memberType){
		List<T> outObj = new ArrayList<>();
		if(user == null || container == null){
			logger.error("User or container is null");
			return outObj;
		}
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All " + memberType.toString() + "in " + type.toString() + " " + container.getUrn(),AuditEnumType.ROLE,user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "All " + memberType.toString() + "in " + type.toString() + " " + container.getUrn());
		logger.warn("Authorization implementation being reworked, so there is presently a hole here where a user can read objects as members, but may not be able to read the objects directly");
		try {
			if(
				RoleService.isFactoryAdministrator(user,((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)),container.getOrganizationId())
				||
				AuthorizationService.canView(user, container)
			){
				AuditService.permitResult(audit, "Access authorized to list " + memberType.toString() + "in " + type.toString() + " " + container.getUrn());
				if(type == AuditEnumType.ROLE){
					switch(memberType){
						case GROUP:
							outObj = FactoryBase.convertList(((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getGroupsInRole((BaseRoleType)container));
							break;
						case ACCOUNT:
							outObj = FactoryBase.convertList(((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getAccountsInRole((BaseRoleType)container));
							break;
						case PERSON:
							outObj = FactoryBase.convertList(((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getPersonsInRole((BaseRoleType)container));
							break;
						case USER:
							outObj = FactoryBase.convertList(((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getUsersInRole((BaseRoleType)container));

							break;
						default:
							logger.error(String.format(FactoryException.UNHANDLED_TYPE, memberType));
							break;

					}
				}
				else if(type == AuditEnumType.GROUP){
					switch(memberType){
						case ACCOUNT:
							outObj = FactoryBase.convertList(((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getAccountsInGroup((AccountGroupType)container));
							break;
						case PERSON:
							outObj = FactoryBase.convertList(((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getPersonsInGroup((PersonGroupType)container));
							break;
						case USER:
							outObj = FactoryBase.convertList(((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getUsersInGroup((UserGroupType)container));
							break;
						case DATA:
							outObj = FactoryBase.convertList(((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getDataInGroup((BucketGroupType)container));

							break;
						default:
							logger.error(String.format(FactoryException.UNHANDLED_TYPE, memberType));
							break;

					}
				}
				for(T nObj : outObj){
					NameIdType n = (NameIdType)nObj;
					((NameIdFactory)Factories.getFactory(FactoryEnumType.valueOf(n.getNameType().toString()))).denormalize(n);
				}
			}
			else{
				AuditService.denyResult(audit, "User " + user.getUrn() + " not authorized to list " + memberType.toString());
				return outObj;
			}
		} catch (ArgumentException | FactoryException e1) {
			
			logger.error(e1);
		}


		return outObj;
		
	}
	/// src is the object
	/// targ is the actor
	//
	public static boolean setPermission(UserType user, AuditEnumType objectType, String objectId, AuditEnumType actorType, String actorId, String permissionId, boolean enable){
		NameIdType src = null;
		NameIdType targ = null;
		BasePermissionType perm = null;
		boolean outBool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Permission " + permissionId,AuditEnumType.PERMISSION,(user == null ? "Null" : user.getName()));
		if(user == null){
			AuditService.denyResult(audit, "User is null");
			return outBool;
		}
		try {
			if(objectType != AuditEnumType.DATA) src = ((NameIdFactory)BaseService.getFactory(objectType)).getByObjectId(objectId, user.getOrganizationId());
			else src = ((DataFactory)BaseService.getFactory(objectType)).getDataByObjectId(objectId, true, user.getOrganizationId());
			if(actorType != AuditEnumType.DATA) targ = ((NameIdFactory)BaseService.getFactory(actorType)).getByObjectId(actorId, user.getOrganizationId());
			else targ = ((DataFactory)BaseService.getFactory(objectType)).getDataByObjectId(actorId, true, user.getOrganizationId());
			perm = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getByObjectId(permissionId, user.getOrganizationId());
			if(src == null || targ == null || perm == null){
				AuditService.denyResult(audit, "One or more reference ids were invalid: " + (src == null ? " " + objectType.toString() + " #" +objectId + " Source is null." : "") + (targ == null ? " " + actorType.toString() + " #" +objectId + " Target is null." : "") + (perm == null ? " #" +objectId + " Permission is null." : ""));
				return false;
			}
			AuditService.sourceAudit(audit, AuditEnumType.PERMISSION, objectType.toString() + " " + src.getUrn());
			AuditService.targetAudit(audit, actorType, targ.getUrn());
			/// To set the permission on or off, the user must:
			/// 1) be able to change src,
			/// 2) be able to change targ,
			/// 3) be able to change permission
			if(
				BaseService.canChangeType(objectType, user,src)
				&&
				BaseService.canChangeType(actorType, user, targ)
				&&
				AuthorizationService.canChange(user, perm)
			){
				boolean set = false;
				set = AuthorizationService.authorize(user,targ,src,perm,enable);
	
				if(set){
					EffectiveAuthorizationService.pendUpdate(targ);
					EffectiveAuthorizationService.rebuildPendingRoleCache();
					outBool = true;
					AuditService.permitResult(audit, "User " + user.getUrn() + " is authorized to change the permission.");
				}
				else AuditService.denyResult(audit, "User " + user.getUrn() + " did not change the permission.");
			}
			else{
				AuditService.denyResult(audit, "User " + user.getUrn() + " not authorized to set the permission.");
			}

		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(e);
		}
		return outBool;
	}
	
}