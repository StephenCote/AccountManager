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

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.FactoryService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.data.util.UrnUtil;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FunctionFactType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.JAXBUtil;
import org.cote.accountmanager.util.MapUtil;

public class BaseService{
	public static final Logger logger = Logger.getLogger(BaseService.class.getName());
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
			obj.setOrganizationPath(Factories.getOrganizationFactory().getOrganizationPath(user.getOrganizationId()));
		}
		if(obj.getNameType() == NameEnumType.DATA){
			Factories.getDataFactory().normalize(object);
		}
		else if(
			obj.getNameType() == NameEnumType.GROUP
			||

			obj.getNameType() == NameEnumType.PERMISSION
			||
			obj.getNameType() == NameEnumType.ROLE
			||
			obj.getNameType() == NameEnumType.USER
		){
			NameIdFactory fact = Factories.getFactory(FactoryEnumType.valueOf(obj.getNameType().toString()));
			fact.normalize(object);
		}

		else if(FactoryService.isDirectoryType(obj.getNameType())){
			NameIdGroupFactory fact = Factories.getFactory(FactoryEnumType.valueOf(obj.getNameType().toString()));
			if(user != null){
				BaseGroupType group = Factories.getGroupFactory().findGroup(user, GroupEnumType.DATA, ((NameIdDirectoryGroupType)obj).getGroupPath(),user.getOrganizationId());
				if(group != null){
					Factories.getGroupFactory().denormalize(group);
					((NameIdDirectoryGroupType)obj).setGroupPath(group.getPath());
				}
			}
			fact.normalize(object);
		}
		else{
			
			throw new ArgumentException("Unsupported type: " + obj.getNameType().toString());
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
			throw new ArgumentException("Invalid object");
		}

		if(
			obj.getNameType() == NameEnumType.GROUP
			||
			obj.getNameType() == NameEnumType.DATA
			||
			obj.getNameType() == NameEnumType.PERMISSION
			||
			obj.getNameType() == NameEnumType.ROLE
			||
			obj.getNameType() == NameEnumType.USER

		){
			NameIdFactory fact = Factories.getFactory(FactoryEnumType.valueOf(obj.getNameType().toString()));
			fact.denormalize(object);
		}
		else if(FactoryService.isDirectoryType(obj.getNameType())){
			NameIdGroupFactory fact = Factories.getFactory(FactoryEnumType.valueOf(obj.getNameType().toString()));
			fact.denormalize(object);
		}
		else{
			throw new ArgumentException("Unsupported type: " + obj.getNameType().toString());
		}

	}
	
	/// don't blindly accept values 
	///
	private static <T> boolean sanitizeAddNewObject(AuditEnumType type, UserType user, T in_obj) throws ArgumentException, FactoryException, DataException, DataAccessException{
		boolean out_bool = false;
		
		switch(type){
			case TAG:
				BaseTagType vtbean = (BaseTagType)in_obj;
				BaseTagType new_tag = Factories.getTagFactory().newTag(user,vtbean.getName(),vtbean.getTagType(),vtbean.getGroupId());
				/// Note: Older style TagType factory still being migrated, so specify the ownerid here in order to allow for contact information to be created
				///
				new_tag.setOwnerId(user.getId());
				MapUtil.shallowCloneNameIdDirectoryType(vtbean, new_tag);
				out_bool = Factories.getTagFactory().addTag(new_tag);
				break;
			case ACCOUNT:
				AccountType v1bean = (AccountType)in_obj;
				AccountType new_acct = Factories.getAccountFactory().newAccount(user,v1bean.getName(),v1bean.getAccountType(), v1bean.getAccountStatus(), v1bean.getGroupId());
				/// Note: Older style AccountType factory still being migrated, so specify the ownerid here in order to allow for contact information to be created
				///
				new_acct.setOwnerId(user.getId());
				MapUtil.shallowCloneNameIdDirectoryType(v1bean, new_acct);
				out_bool = Factories.getAccountFactory().addAccount(new_acct,true);
				break;
			case CONTACT:
				ContactType v2bean = (ContactType)in_obj;
				ContactType new_ct = Factories.getContactFactory().newContact(user, v2bean.getGroupId());
	
				MapUtil.shallowCloneNameIdDirectoryType(v2bean, new_ct);
				new_ct.setContactType(v2bean.getContactType());
				new_ct.setDescription(v2bean.getDescription());
				new_ct.setLocationType(v2bean.getLocationType());
				new_ct.setContactValue(v2bean.getContactValue());
				new_ct.setPreferred(v2bean.getPreferred());
				out_bool = Factories.getContactFactory().addContact(new_ct);
				break;
				/*
			case CONTACTINFORMATION:
				ContactInformationType v3bean = (ContactInformationType)in_obj;
				ContactInformationType new_cti = new ContactInformationType();
				new_cti.setReferenceId(v3bean.getReferenceId());
				/// MapUtil.shallowCloneNameIdDirectoryType(v3bean, new_cti);
				new_cti.setContactInformationType(v3bean.getContactInformationType());
				new_cti.setOwnerId(v3bean.getOwnerId());
				out_bool = Factories.getContactInformationFactory().addContactInformation(new_cti);
				break;
				*/
			case FACT:
				FactType v4bean = (FactType)in_obj;
				FactType new_fa = Factories.getFactFactory().newFact(user, v4bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v4bean, new_fa);
				new_fa.setFactType(v4bean.getFactType());
				new_fa.setFactData(v4bean.getFactData());
				new_fa.setFactoryType(v4bean.getFactoryType());
				new_fa.setSourceDataType(v4bean.getSourceDataType());
				new_fa.setSourceUrl(v4bean.getSourceUrl());
				new_fa.setSourceUrn(v4bean.getSourceUrn());
				out_bool = Factories.getFactFactory().addFact(new_fa);
				break;
			case FUNCTION:
				FunctionType v5bean = (FunctionType)in_obj;
				FunctionType new_fu = Factories.getFunctionFactory().newFunction(user, v5bean.getGroupId());
				
				MapUtil.shallowCloneAznType(v5bean, new_fu);
				new_fu.setFunctionType(v5bean.getFunctionType());
				new_fu.setFunctionData(v5bean.getFunctionData());
				new_fu.setSourceUrl(v5bean.getSourceUrl());
				new_fu.setSourceUrn(v5bean.getSourceUrn());
				out_bool = Factories.getFunctionFactory().addFunction(new_fu);
				break;
			case FUNCTIONFACT:
				FunctionFactType v6bean = (FunctionFactType)in_obj;
				FunctionFactType new_fuf = Factories.getFunctionFactFactory().newFunctionFact(user, v6bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v6bean, new_fuf);
				new_fuf.setFactUrn(v6bean.getFactUrn());
				new_fuf.setFunctionUrn(v6bean.getFunctionUrn());
				out_bool = Factories.getFunctionFactFactory().addFunctionFact(new_fuf);
				break;
			case OPERATION:
				OperationType v7bean = (OperationType)in_obj;
				OperationType new_op = Factories.getOperationFactory().newOperation(user, v7bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v7bean, new_op);
				new_op.setOperationType(v7bean.getOperationType());
				new_op.setOperation(v7bean.getOperation());
				out_bool = Factories.getOperationFactory().addOperation(new_op);
				break;
			case PATTERN:
				PatternType v8bean = (PatternType)in_obj;
				PatternType new_pa = Factories.getPatternFactory().newPattern(user, v8bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v8bean, new_pa);
				new_pa.setPatternType(v8bean.getPatternType());
				new_pa.setComparator(v8bean.getComparator());
				new_pa.setFactUrn(v8bean.getFactUrn());
				new_pa.setMatchUrn(v8bean.getMatchUrn());
				new_pa.setOperationUrn(v8bean.getOperationUrn());
				out_bool = Factories.getPatternFactory().addPattern(new_pa);
				break;
			case POLICY:
				PolicyType v9bean = (PolicyType)in_obj;
				PolicyType new_po = Factories.getPolicyFactory().newPolicy(user, v9bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v9bean, new_po);
				new_po.setDecisionAge(v9bean.getDecisionAge());
				new_po.setExpiresDate(v9bean.getExpiresDate());
				new_po.setEnabled(v9bean.getEnabled());
				new_po.getRules().addAll(v9bean.getRules());
				new_po.setCondition(v9bean.getCondition());
				
				//new_po.setCreated(v9bean.getCreated());
				//new_po.setModified(v9bean.getModified());
				//logger.info("PD: " + new_po.getCreatedDate() + " / " + new_po.getModifiedDate() + " / " + new_po.getExpiresDate());
				out_bool = Factories.getPolicyFactory().addPolicy(new_po);
				break;
			case RULE:
				RuleType v10bean = (RuleType)in_obj;
				RuleType new_ru = Factories.getRuleFactory().newRule(user, v10bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v10bean, new_ru);
				new_ru.setRuleType(v10bean.getRuleType());
				new_ru.setCondition(v10bean.getCondition());
				new_ru.getRules().addAll(v10bean.getRules());
				new_ru.getPatterns().addAll(v10bean.getPatterns());
				out_bool = Factories.getRuleFactory().addRule(new_ru);
				break;
			case PERSON:
				PersonType v11bean = (PersonType)in_obj;
				PersonType new_per = Factories.getPersonFactory().newPerson(user, v11bean.getGroupId());
	
				MapUtil.shallowCloneNameIdDirectoryType(v11bean, new_per);
				new_per.setAlias(v11bean.getAlias());
				new_per.setBirthDate(v11bean.getBirthDate());
				//new_per.setContact(v11bean.getContact());
				new_per.setDescription(v11bean.getDescription());
				new_per.setFirstName(v11bean.getFirstName());
				new_per.setGender(v11bean.getGender());
				new_per.setLastName(v11bean.getLastName());
				new_per.setMiddleName(v11bean.getMiddleName());
				//new_per.setParentId(v11bean.getParentId());
				new_per.setPrefix(v11bean.getPrefix());
				new_per.setSuffix(v11bean.getSuffix());
				new_per.setTitle(v11bean.getTitle());
				
				new_per.getAccounts().addAll(v11bean.getAccounts());
				new_per.getDependents().addAll(v11bean.getDependents());
				new_per.getPartners().addAll(v11bean.getPartners());
				new_per.getUsers().addAll(v11bean.getUsers());
				out_bool = Factories.getPersonFactory().addPerson(new_per);
				break;
			case ADDRESS:
				AddressType v12bean = (AddressType)in_obj;
				AddressType new_addr = Factories.getAddressFactory().newAddress(user, v12bean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(v12bean, new_addr);
				new_addr.setAddressLine1(v12bean.getAddressLine1());
				new_addr.setAddressLine2(v12bean.getAddressLine2());
				new_addr.setCity(v12bean.getCity());
				new_addr.setCountry(v12bean.getCountry());
				new_addr.setDescription(v12bean.getDescription());
				new_addr.setLocationType(v12bean.getLocationType());
				new_addr.setPostalCode(v12bean.getPostalCode());
				new_addr.setPreferred(v12bean.getPreferred());
				new_addr.setRegion(v12bean.getRegion());
				new_addr.setState(v12bean.getState());
				out_bool = Factories.getAddressFactory().addAddress(new_addr);
				break;
			case ROLE:
				BaseRoleType rlbean = (BaseRoleType)in_obj;
				BaseRoleType parentRole = null;
				if(rlbean.getParentId() > 0L){
					parentRole = Factories.getRoleFactory().getById(rlbean.getParentId(), rlbean.getOrganizationId());
					if(parentRole == null) throw new ArgumentException("Role parent #" + rlbean.getParentId() + " is invalid");
				}
				BaseRoleType new_role = Factories.getRoleFactory().newRoleType(rlbean.getRoleType(),user, rlbean.getName(), parentRole);
				out_bool = Factories.getRoleFactory().addRole(new_role);
				break;
				
			case PERMISSION:
				BasePermissionType perbean = (BasePermissionType)in_obj;
				BasePermissionType parentPermission = null;
				if(perbean.getParentId() > 0L){
					parentPermission = Factories.getPermissionFactory().getById(perbean.getParentId(), perbean.getOrganizationId());
					if(parentPermission == null) throw new ArgumentException("Permission parent #" + perbean.getParentId() + " is invalid");
				}
				BasePermissionType new_per2 = Factories.getPermissionFactory().newPermission(user, perbean.getName(), perbean.getPermissionType(), parentPermission, perbean.getOrganizationId());
				out_bool = Factories.getPermissionFactory().addPermission(new_per2);
				break;
				
			case GROUP:
				BaseGroupType gbean = (BaseGroupType)in_obj;
				BaseGroupType parentGroup = null;
				if(gbean.getParentId() > 0L){
					parentGroup = Factories.getGroupFactory().getById(gbean.getParentId(), gbean.getOrganizationId());
				}
				BaseGroupType new_group = Factories.getGroupFactory().newGroup(user, gbean.getName(), gbean.getGroupType(), parentGroup, gbean.getOrganizationId());
				out_bool = Factories.getGroupFactory().addGroup(new_group);
				break;
				
			case USER:
				UserType ubean = (UserType)in_obj;
				UserType new_user = Factories.getUserFactory().newUser(ubean.getName(), UserEnumType.NORMAL, UserStatusEnumType.NORMAL, ubean.getOrganizationId());
				//new_user.setContactInformation(ubean.getContactInformation());
				out_bool = Factories.getUserFactory().addUser(new_user,true);
				break;
			case DATA:
				DataType rbean = (DataType)in_obj;
				DataType new_rec = Factories.getDataFactory().newData(user, rbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(rbean, new_rec);
				new_rec.setDescription(rbean.getDescription());
				new_rec.setDimensions(rbean.getDimensions());
				if(rbean.getExpiryDate() != null) new_rec.setExpiryDate(rbean.getExpiryDate());
				new_rec.setMimeType(rbean.getMimeType());
				new_rec.setRating(rbean.getRating());

				DataUtil.setValue(new_rec, DataUtil.getValue(rbean));
				if(rbean.getPointer() == true){
					logger.error("Creating data pointers from the web FE is forbidden regardless of configuration");
					return false;
				}
				out_bool = Factories.getDataFactory().addData(new_rec);
				break;				
		}
		return out_bool;
	}
	private static <T> boolean updateObject(AuditEnumType type, T in_obj) throws ArgumentException, FactoryException, DataAccessException {
		boolean out_bool = false;
		switch(type){
			case ACCOUNT:
				out_bool = Factories.getAccountFactory().updateAccount((AccountType)in_obj);
				break;
			case TAG:
				out_bool = Factories.getTagFactory().updateTag((BaseTagType)in_obj);
				break;

			case PERSON:
				out_bool = Factories.getPersonFactory().updatePerson((PersonType)in_obj);
				break;
			case ADDRESS:
				out_bool = Factories.getAddressFactory().updateAddress((AddressType)in_obj);
				break;

			case CONTACT:
				out_bool = Factories.getContactFactory().updateContact((ContactType)in_obj);
				break;
			case PERMISSION:
				out_bool = Factories.getPermissionFactory().updatePermission((BasePermissionType)in_obj);
				break;
			/*
			case CONTACTINFORMATION:
				out_bool = Factories.getContactInformationFactory().updateContactInformation((ContactInformationType)in_obj);
				break;
			*/
			case FACT:
				out_bool = Factories.getFactFactory().updateFact((FactType)in_obj);
				break;
			case FUNCTION:
				out_bool = Factories.getFunctionFactory().updateFunction((FunctionType)in_obj);
				break;
			case FUNCTIONFACT:
				out_bool = Factories.getFunctionFactFactory().updateFunctionFact((FunctionFactType)in_obj);
				break;
			case OPERATION:
				out_bool = Factories.getOperationFactory().updateOperation((OperationType)in_obj);
				break;
			case PATTERN:
				out_bool = Factories.getPatternFactory().updatePattern((PatternType)in_obj);
				break;
			case POLICY:
				out_bool = Factories.getPolicyFactory().updatePolicy((PolicyType)in_obj);
				break;
			case RULE:
				out_bool = Factories.getRuleFactory().updateRule((RuleType)in_obj);
				break;
			case ROLE:
				out_bool = Factories.getRoleFactory().updateRole((BaseRoleType)in_obj);
				break;
			case USER:
				UserType tuser = (UserType)in_obj;
				out_bool = Factories.getUserFactory().updateUser((UserType)in_obj);
				break;
			case DATA:
				out_bool = Factories.getDataFactory().updateData((DataType)in_obj);
				break;
			case GROUP:
				out_bool = Factories.getGroupFactory().updateGroup((BaseGroupType)in_obj);
				break;
		}
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
		switch(type){
			case ACCOUNT:
				out_bool = Factories.getAccountFactory().deleteAccount((AccountType)in_obj);
				break;
			case TAG:
				out_bool = Factories.getTagFactory().deleteTag((BaseTagType)in_obj);
				break;

			case PERSON:
				out_bool = Factories.getPersonFactory().deletePerson((PersonType)in_obj);
				break;
			case ADDRESS:
				out_bool = Factories.getAddressFactory().deleteAddress((AddressType)in_obj);
				break;
			case CONTACT:
				out_bool = Factories.getContactFactory().deleteContact((ContactType)in_obj);
				break;
			/*
			case CONTACTINFORMATION:
				out_bool = Factories.getContactInformationFactory().deleteContactInformation((ContactInformationType)in_obj);
				break;
			*/
			case FACT:
				out_bool = Factories.getFactFactory().deleteFact((FactType)in_obj);
				break;
			case FUNCTION:
				out_bool = Factories.getFunctionFactory().deleteFunction((FunctionType)in_obj);
				break;
			case FUNCTIONFACT:
				out_bool = Factories.getFunctionFactFactory().deleteFunctionFact((FunctionFactType)in_obj);
				break;
			case OPERATION:
				out_bool = Factories.getOperationFactory().deleteOperation((OperationType)in_obj);
				break;
			case PATTERN:
				out_bool = Factories.getPatternFactory().deletePattern((PatternType)in_obj);
				break;
			case POLICY:
				out_bool = Factories.getPolicyFactory().deletePolicy((PolicyType)in_obj);
				break;
			case RULE:
				out_bool = Factories.getRuleFactory().deleteRule((RuleType)in_obj);
				break;
			case ROLE:
				out_bool = Factories.getRoleFactory().deleteRole((BaseRoleType)in_obj);
				break;
			case PERMISSION:
				out_bool = Factories.getPermissionFactory().deletePermission((BasePermissionType)in_obj);
				break;
			case USER:
				out_bool = Factories.getUserFactory().deleteUser((UserType)in_obj);
				break;
			case DATA:
				out_bool = Factories.getDataFactory().deleteData((DataType)in_obj);
				break;
			case GROUP:
				BaseGroupType gobj = (BaseGroupType)in_obj;
				if(gobj.getGroupType() == GroupEnumType.DATA) out_bool = Factories.getGroupFactory().deleteDirectoryGroup((DirectoryGroupType)in_obj);
				else Factories.getGroupFactory().deleteGroup(gobj);
				break;

		}
		return out_bool;
	}
	public static <T> T getFactory(AuditEnumType type){
		T out_obj = null;
		switch(type){
			case PERMISSION:
				out_obj = (T)Factories.getPermissionFactory();
				break;
			case TAG:
				out_obj = (T)Factories.getTagFactory();
				break;

			case ACCOUNT:
				out_obj = (T)Factories.getAccountFactory();
				break;
			case PERSON:
				out_obj = (T)Factories.getPersonFactory();
				break;
			case ADDRESS:
				out_obj = (T)Factories.getAddressFactory();
				break;
			case CONTACT:
				out_obj = (T)Factories.getContactFactory();
				break;
				/*
			case CONTACTINFORMATION:
				out_obj = (T)Factories.getContactInformationFactory();
				break;
				*/
			case FACT:
				out_obj = (T)Factories.getFactFactory();
				break;
			case FUNCTION:
				out_obj = (T)Factories.getFunctionFactory();
				break;
			case FUNCTIONFACT:
				out_obj = (T)Factories.getFunctionFactFactory();
				break;
			case OPERATION:
				out_obj = (T)Factories.getOperationFactory();
				break;
			case PATTERN:
				out_obj = (T)Factories.getPatternFactory();
				break;
			case POLICY:
				out_obj = (T)Factories.getPolicyFactory();
				break;
			case RULE:
				out_obj = (T)Factories.getRuleFactory();
				break;
			case ROLE:
				out_obj = (T)Factories.getRoleFactory();
				break;
			case USER:
				out_obj = (T)Factories.getUserFactory();
				break;
			case DATA:
				out_obj = (T)Factories.getDataFactory();
				break;
			case GROUP:
				out_obj = (T)Factories.getGroupFactory();
				break;
			
		}
		return out_obj;
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

		if(enableExtendedAttributes){
			Factories.getAttributeFactory().populateAttributes((NameIdType)obj);
		}
		
		switch(type){
			case DATA:
				DataType d = (DataType)obj;
				if(d.getDetailsOnly()){
					logger.error("Data is details only.  Was expecting full data");
				}
				if(d.getCompressed() || d.getPointer()){
					/// Make a copy of the object so as to operate on the copy and not a cached copy from the factory
					///
					d = JAXBUtil.clone(DataType.class, d);
					try {
						byte[] data = DataUtil.getValue(d);
						d.setCompressed(false);
						//d.setPointer(false);
						d.setDataBytesStore(data);
						d.setReadDataBytes(false);
						obj = (T)d;
					} catch (DataException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
				break;
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
		switch(type){
			case GROUP:
				GroupEnumType grpType = GroupEnumType.fromValue(otype);
				out_obj = (T)Factories.getGroupFactory().getGroupByName(name, grpType, (BaseGroupType)parent, parent.getOrganizationId());
				break;
			case ROLE:
				RoleEnumType rolType = RoleEnumType.fromValue(otype);
				out_obj = (T)Factories.getRoleFactory().getRoleByName(name, (BaseRoleType)parent, rolType, parent.getOrganizationId());
				break;
			case PERMISSION:
				PermissionEnumType perType = PermissionEnumType.fromValue(otype);
				out_obj = (T)Factories.getPermissionFactory().getPermissionByName(name, perType, (BasePermissionType)parent, parent.getOrganizationId());
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
	
	
	private static <T> T getByNameInGroup(AuditEnumType type, String name, DirectoryGroupType group) throws ArgumentException, FactoryException {
		
		T out_obj = null;
		switch(type){
			case TAG:
			case ACCOUNT:
			case PERSON:
			case ADDRESS:
			case CONTACT:
			case FACT:
			case FUNCTION:
			case FUNCTIONFACT:
			case OPERATION:
			case PATTERN:
			case POLICY:
			case RULE:
				out_obj = ((NameIdGroupFactory)getFactory(type)).getByNameInGroup(name, group);;
				break;
			case DATA:
				out_obj = (T)Factories.getDataFactory().getDataByName(name, group);
				if(out_obj == null){
					logger.error("Data '" + name + "' is null");
					return out_obj;
				}
				DataType d = (DataType)out_obj;
				if(d.getCompressed() || d.getPointer()){
					d = JAXBUtil.clone(DataType.class, d);
					try {
						byte[] data = DataUtil.getValue(d);
						d.setCompressed(false);
						//d.setPointer(false);
						d.setDataBytesStore(data);
						d.setReadDataBytes(false);
						out_obj = (T)d;
					} catch (DataException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
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
	private static <T> T getByNameInGroup(AuditEnumType type, String name, long organizationId) throws ArgumentException, FactoryException {
		
		T out_obj = null;
		switch(type){
			case ROLE:
				out_obj = (T)Factories.getRoleFactory().getRoleByName(name, organizationId);
				break;
			case USER:
				out_obj = (T)Factories.getUserFactory().getUserByName(name, organizationId);
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
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	
	}
	public static boolean canViewType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		if(AuthorizationService.isMapOwner(user, obj)) return true;
		switch(type){
			case TAG:
			case ACCOUNT:
			case PERSON:
			case ADDRESS:
			case CONTACT:
			case FACT:
			case FUNCTION:
			case FUNCTIONFACT:
			case OPERATION:
			case PATTERN:
			case POLICY:
			case RULE:
				out_bool = AuthorizationService.canView(user,Factories.getGroupFactory().getGroupById(((NameIdDirectoryGroupType)obj).getGroupId(),obj.getOrganizationId()));
				break;
			case ROLE:
				out_bool = AuthorizationService.canView(user, (BaseRoleType)obj);
				break;
			case PERMISSION:
				out_bool = AuthorizationService.canView(user, (BasePermissionType)obj);
				break;

			case DATA:
				out_bool = AuthorizationService.canView(user, (DataType)obj);
				break;
			case USER:
				// allow for user requesting self
				// this does not register true for 'isMapOwner' for the user object as a user does not own itslef
				//
				out_bool = (user.getId().compareTo(obj.getId())==0 && user.getOrganizationId().compareTo(obj.getOrganizationId())==0);
				if(!out_bool)  out_bool = AuthorizationService.isMapOwner(user, obj);
				if(!out_bool) out_bool = RoleService.isFactoryAdministrator(user, Factories.getAccountFactory(),obj.getOrganizationId());
				if(!out_bool) out_bool = RoleService.isFactoryReader(user, Factories.getAccountFactory(),obj.getOrganizationId());
				break;
			case GROUP:
				out_bool = AuthorizationService.canView(user, (BaseGroupType)obj);
				break;
		}
		return out_bool;
	}
	public static boolean canCreateType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		switch(type){
			case TAG:
			case ACCOUNT:
			case PERSON:
			case ADDRESS:
			case CONTACT:
			case FACT:
			case FUNCTION:
			case FUNCTIONFACT:
			case OPERATION:
			case PATTERN:
			case POLICY:
			case RULE:
				out_bool = AuthorizationService.canChange(user,Factories.getGroupFactory().getGroupById(((NameIdDirectoryGroupType)obj).getGroupId(),obj.getOrganizationId()));
				break;
			case PERMISSION:
				if(obj.getParentId() > 0L){
					BasePermissionType parent = Factories.getPermissionFactory().getById(obj.getParentId(),obj.getOrganizationId());
					out_bool = AuthorizationService.canChange(user, parent);
				}
				if(!out_bool){
					out_bool = RoleService.isFactoryAdministrator(user, Factories.getAccountFactory(),obj.getOrganizationId());
				}
				break;
			case ROLE:

				if(obj.getParentId() > 0L){
					BaseRoleType parent = Factories.getRoleFactory().getById(obj.getParentId(),obj.getOrganizationId());
					out_bool = AuthorizationService.canChange(user, parent);
				}
				if(!out_bool){
					out_bool = RoleService.isFactoryAdministrator(user, Factories.getAccountFactory(),obj.getOrganizationId());
				}
				break;
			case DATA:
				out_bool = AuthorizationService.canChange(user, Factories.getGroupFactory().getGroupById(((DataType)obj).getGroupId(),obj.getOrganizationId()));
				break;
			case USER:
				out_bool = RoleService.isFactoryAdministrator(user, Factories.getAccountFactory(),obj.getOrganizationId());
				break;
			case GROUP:
				if(obj.getParentId() > 0L){
					BaseGroupType parent = Factories.getGroupFactory().getById(obj.getParentId(),obj.getOrganizationId());
					out_bool = AuthorizationService.canCreate(user, parent);
				}
				break;
		}
		return out_bool;
	}
	public static boolean canChangeType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		if(AuthorizationService.isMapOwner(user, obj)) return true;
		switch(type){
			case TAG:
			case ACCOUNT:
			case PERSON:
			case ADDRESS:
			case CONTACT:
			case FACT:
			case FUNCTION:
			case FUNCTIONFACT:
			case OPERATION:
			case PATTERN:
			case POLICY:
			case RULE:
				out_bool = AuthorizationService.canChange(user,Factories.getGroupFactory().getGroupById(((NameIdDirectoryGroupType)obj).getGroupId(),obj.getOrganizationId()));
				break;
			case PERMISSION:
				out_bool = AuthorizationService.canChange(user, (BasePermissionType)obj);
				break;
			case ROLE:
				out_bool = AuthorizationService.canChange(user, (BaseRoleType)obj);
	
				break;
			case DATA:
				/*
				out_bool = AuthorizationService.canChangeData(user, (DataType)obj);
				if(!out_bool)
				*/
				/// 2015/06/22 - Relaxing the direct data constraint for general group constraint
				/// This is temporary to fix an issue where data can be pushed into groups a user doesn't own
				/// 
				out_bool = AuthorizationService.canChange(user, Factories.getGroupFactory().getGroupById(((DataType)obj).getGroupId(),obj.getOrganizationId()));
				break;
			case USER:
				// allow for user requesting self
				// this does not register true for 'isMapOwner' for the user object as a user does not own itslef
				//
				out_bool = (user.getId().compareTo(obj.getId())==0 && user.getOrganizationId().compareTo(obj.getOrganizationId())==0);
				if(!out_bool)  out_bool = AuthorizationService.isMapOwner(user, obj);
				if(!out_bool) out_bool = RoleService.isFactoryAdministrator(user, Factories.getAccountFactory(),obj.getOrganizationId());
				break;
			case GROUP:
				BaseGroupType edir = Factories.getGroupFactory().getById(obj.getId(), user.getOrganizationId());
				BaseGroupType opdir = Factories.getGroupFactory().getById(edir.getParentId(), user.getOrganizationId());
				BaseGroupType pdir = Factories.getGroupFactory().getById(((BaseGroupType)obj).getParentId(), user.getOrganizationId());
				if(opdir == null){
					logger.error("Original Parent group (#" + ((BaseGroupType)obj).getParentId() + ") doesn't exist in organization " + user.getOrganizationId());
					return false;
				}
				if(pdir == null){
					logger.error("Specified Parent group (#" + ((BaseGroupType)obj).getParentId()+ ") doesn't exist in organization " + user.getOrganizationId());
					return false;
				}
				if(opdir.getId() != pdir.getId() && !AuthorizationService.canCreate(user, pdir)){
					logger.error("User " + user.getName() + " (#" + user.getId() + ") is not authorized to create in group " + pdir.getName() + " (#" + pdir.getId() + ")");
					return false;
				}

				out_bool = AuthorizationService.canChange(user, (BaseGroupType)obj);
				break;
		}
		return out_bool;
	}
	public static boolean canDeleteType(AuditEnumType type, UserType user, NameIdType obj) throws ArgumentException, FactoryException{
		boolean out_bool = false;
		if(AuthorizationService.isMapOwner(user, obj)) return true;
		switch(type){
			case TAG:
			case ACCOUNT:
			case PERSON:
			case ADDRESS:
			case CONTACT:
			case FACT:
			case FUNCTION:
			case FUNCTIONFACT:
			case OPERATION:
			case PATTERN:
			case POLICY:
			case RULE:
				out_bool = AuthorizationService.canChange(user,Factories.getGroupFactory().getGroupById(((NameIdDirectoryGroupType)obj).getGroupId(),obj.getOrganizationId()));
				break;
			case PERMISSION:
				out_bool = AuthorizationService.canDelete(user, (BasePermissionType)obj);
				break;
			case ROLE:
				out_bool = AuthorizationService.canDelete(user, (BaseRoleType)obj);
				break;
			case GROUP:
				out_bool = AuthorizationService.canDelete(user,(BaseGroupType)obj);
				break;
			case DATA:
				out_bool = AuthorizationService.canDelete(user, (DataType)obj);
				//if(out_bool) out_bool = AuthorizationService.canChange(user, ((DataType)obj).getGroupId());
				break;
			case USER:
				// allow for user deleting self
				// this does not register true for 'isMapOwner' for the user object as a user does not own itslef
				//
				out_bool = (user.getId().compareTo(obj.getId())==0 && user.getOrganizationId().compareTo(obj.getOrganizationId())==0);
				if(out_bool) throw new FactoryException("Self deletion not supported via Web interface");
				if(!out_bool)  out_bool = AuthorizationService.isMapOwner(user, obj);
				if(!out_bool) out_bool = RoleService.isFactoryAdministrator(user, Factories.getAccountFactory(),obj.getOrganizationId());
				break;				
		}
		return out_bool;
	}
	
	/// Duped in AuthorizationService, except the type is taken from the object instead of from the AuditEnumType
	private static <T> boolean authorizeRoleType(AuditEnumType type, UserType adminUser, BaseRoleType targetRole, T bucket, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		boolean out_bool = false;
		switch(type){
			case DATA:
				DataType data = (DataType)bucket;
				AuthorizationService.authorizeType(adminUser, targetRole, data, view, edit, delete, create);
				out_bool = true;
				break;
			case GROUP:
				BaseGroupType group = (BaseGroupType)bucket;
				AuthorizationService.authorizeType(adminUser, targetRole, group, view, edit, delete, create);
				out_bool = true;
				break;
		}
		
		return out_bool;
	}
	/// Duped in AuthorizationService, except the type is taken from the object instead of from the AuditEnumType
	private static <T> boolean authorizeUserType(AuditEnumType type, UserType adminUser, UserType targetUser, T bucket, boolean view, boolean edit, boolean delete, boolean create) throws FactoryException, DataAccessException, ArgumentException{
		boolean out_bool = false;
		switch(type){
			case DATA:
				DataType data = (DataType)bucket;
				out_bool = true;
				AuthorizationService.authorizeType(adminUser, targetUser, data, view, edit, delete, create);

				break;
			case ROLE:
				BaseRoleType role = (BaseRoleType)bucket;
				AuthorizationService.authorizeType(adminUser, targetUser, role, view, edit, delete, create);

				out_bool = true;
				break;
			case GROUP:
				BaseGroupType group = (BaseGroupType)bucket;
				AuthorizationService.authorizeType(adminUser, targetUser, group, view, edit, delete, create);

				out_bool = true;
				break;
		}
		
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
				targetRole = Factories.getRoleFactory().getById(targetRoleId, organizationId);
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
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
				targetUser = Factories.getUserFactory().getById(targetUserId, organizationId);
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
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
			AuditService.denyResult(audit, e1.getMessage());
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			AuditService.denyResult(audit, e1.getMessage());
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			AuditService.denyResult(audit, e1.getMessage());
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, e.getMessage());
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			AuditService.denyResult(audit, e1.getMessage());
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			AuditService.denyResult(audit, e1.getMessage());
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			AuditService.targetAudit(audit, type, ((NameIdType)dirType).getUrn());
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

		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
			AuditService.targetAudit(audit, type, ((NameIdType)dirType).getUrn());
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

		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
			dir = Factories.getGroupFactory().getCreateUserDirectory(user, getDefaultGroupName(type));
		}
		 catch (FactoryException e1) {
			 logger.error(e1.getMessage());
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
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
			dir = Factories.getGroupFactory().getById(groupId, user.getOrganizationId());
		}
		 catch (FactoryException e1) {
			// TODO Auto-generated catch block
			 logger.error(e1.getMessage());
			e1.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
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
			//DirectoryGroupType group = Factories.getGroupFactory().getCreateUserDirectory(user, getDefaultGroupName(type));
			Factories.getGroupFactory().populate(dir);
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
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_obj;
	}
	
	public static <T> T readByNameInOrganization(AuditEnumType type, long organizationId, String name,HttpServletRequest request){


		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return null;

		return readByName(audit,type, user, organizationId, name, request);
	}
	/*
	public static <T> T readByName(AuditEnumType type, long organizationId, String name,HttpServletRequest request){
		T out_obj = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "readByName",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, name);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;
		return readByName(audit,type, user, org, name, request);
	}
	*/
	public static <T> T readByName(AuditType audit,AuditEnumType type, UserType user, long organizationId, String name,HttpServletRequest request){
		T out_obj = null;
		try {
			//DirectoryGroupType group = Factories.getGroupFactory().getCreateUserDirectory(user, getDefaultGroupName(type));

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
			// TODO Auto-generated catch block
			e1.printStackTrace();
			out_obj = null;
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			out_obj = null;
		} 

		return out_obj;
	}
	
	public static int countByGroup(AuditEnumType type, String path, HttpServletRequest request){
		BaseGroupType dir = null;

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "count",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, path);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return 0;

		try{
			dir = (BaseGroupType)Factories.getGroupFactory().findGroup(user, GroupEnumType.UNKNOWN,path, user.getOrganizationId());
			//dir = Factories.getGroupFactory().getById(groupId, user.getOrganizationId());
		}
		 catch (FactoryException e1) {
			// TODO Auto-generated catch block
			 logger.error(e1.getMessage());
			e1.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
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
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_count;
	}
	private static int count(AuditEnumType type, BaseGroupType group) throws ArgumentException, FactoryException {
		
		NameIdFactory factory = getFactory(type);
		if(type == AuditEnumType.DATA) return ((DataFactory)factory).getCount((DirectoryGroupType)group);
		return ((NameIdGroupFactory)factory).getCount(group);		
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
		if(user==null) return 0;

		return countInParent(audit,type, user, parent, request);
	}
	public static int countByOrganization(AuditType audit,AuditEnumType type, UserType user, long organizationId, HttpServletRequest request){
		int out_count = 0;
		try {
			if(
					RoleService.isFactoryAdministrator(user, Factories.getDataFactory(),organizationId)
				||
				RoleService.isFactoryAdministrator(user, Factories.getAccountFactory(),organizationId)
				||
				((type == AuditEnumType.USER || type == AuditEnumType.ACCOUNT) &&  RoleService.isFactoryReader(user, Factories.getAccountFactory(),organizationId))
				||
				(type == AuditEnumType.ROLE && RoleService.isFactoryReader(user, Factories.getRoleFactory(),organizationId))
				||
				(type == AuditEnumType.GROUP && RoleService.isFactoryReader(user, Factories.getGroupFactory(),organizationId))
				
			){
				out_count = count(type, organizationId);
				AuditService.permitResult(audit, "Count " + out_count + " of " + type.toString());
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to count directly in organization " + organizationId);
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_count;
	}
	/*
	 * TODO: What uses this?
	public static int count(AuditType audit,AuditEnumType type, UserType user, long organizationId, HttpServletRequest request){
		int out_count = 0;
		try {
			OrganizationType org = Factories.getOrganizationFactory().getOrganizationById(organizationId);
			if(canViewType(type,user, org) == true){
				out_count = count(type, organizationId);
				AuditService.permitResult(audit, "Count " + out_count + " of " + type.toString());
			}
			else{
				AuditService.denyResult(audit,"User is not authorized to view user lists in the specified organization '" + org.getName() + "' #" + organizationId);
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_count;
	}
	*/
	private static int count(AuditEnumType type, long organization_id) throws ArgumentException, FactoryException {
		NameIdFactory factory = getFactory(type);
		return factory.getCount(organization_id);
	}
	private static int countInParent(AuditEnumType type, NameIdType parent) throws ArgumentException, FactoryException {
		NameIdFactory factory = getFactory(type);
		return factory.getCountInParent(parent);
	}
	
	public static <T> List<T> listByParentObjectId(AuditEnumType type, String parentType, String parentId, long startRecord, int recordCount, HttpServletRequest request){
		///return BaseService.getGroupList(AuditEnumType.ROLE, user, path, startRecord, recordCount);
		List<T> out_obj = new ArrayList<>();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "listByParentObjectId",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, type, parentId);
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null) return out_obj;

		//AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All " + type.toString() + " objects",AuditEnumType.GROUP,(user == null ? "Null" : user.getName()));
		NameIdType parentObj = BaseService.readByObjectId(type, parentId, request);
		if(parentObj == null){
			AuditService.denyResult(audit, "Null parent id");
			return out_obj;
		}
		
		AuditService.targetAudit(audit, type, parentObj.getUrn());
		
		/// This is duplicative if the REST service is enforcing this
		///
		if(SessionSecurity.isAuthenticated(user) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}

		try {
			///AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(
				AuthorizationService.canView(user, parentObj)
				||
				RoleService.isFactoryReader(user,Factories.getGroupFactory(),parentObj.getOrganizationId())
			){
				AuditService.permitResult(audit, "Access authorized to list objects");
				out_obj = getListByParent(type,parentType,parentObj,startRecord,recordCount,parentObj.getOrganizationId() );
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list roles.");
				return out_obj;
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_obj;
	}
	
	private static <T> List<T> getListByParent(AuditEnumType type,String parentType, NameIdType parentObj, long startIndex, int recordCount, long organizationId){
		//BaseGroupType dir = findGroup(groupType, path, request);
		List<T> dirs = new ArrayList<>();
		//GroupEnumType groupType = GroupEnumType.valueOf(type);
		if(parentObj == null) return dirs;
		try {
			switch(type){
				case GROUP:
					dirs = Factories.getGroupFactory().getListByParent(GroupEnumType.valueOf(parentType), (BaseGroupType)parentObj,  startIndex, recordCount, parentObj.getOrganizationId());
					break;
				case ROLE:
					dirs = Factories.getRoleFactory().getRoleList(RoleEnumType.valueOf(parentType), (BaseRoleType)parentObj,  startIndex, recordCount, parentObj.getOrganizationId());
					break;
				case PERMISSION:
					/// TODO: Inconsistent method calls
					///
					dirs = Factories.getPermissionFactory().getPermissionList((BasePermissionType)parentObj, PermissionEnumType.valueOf(parentType),  startIndex, recordCount, parentObj.getOrganizationId());
					break;
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(BaseService.enableExtendedAttributes){
			for(int i = 0; i < dirs.size(); i++){
				Factories.getAttributeFactory().populateAttributes((NameIdType)dirs.get(i));
			}
		}
		return dirs;
	}
	
	
	private static <T> List<T> getListByGroup(AuditEnumType type, BaseGroupType group,long startRecord, int recordCount) throws ArgumentException, FactoryException {
		NameIdGroupFactory factory = getFactory(type);
		List<T> out_obj = factory.getListByGroup(group, startRecord, recordCount, group.getOrganizationId());

		for(int i = 0; i < out_obj.size();i++){
			NameIdDirectoryGroupType ngt = (NameIdDirectoryGroupType)out_obj.get(i);
			denormalize(ngt);
			if(enableExtendedAttributes){
				Factories.getAttributeFactory().populateAttributes(ngt);
			}
		
			/*
			
			if(ngt.getGroupId().getPopulated() == false || ngt.getGroupPath() == null){
				Factories.getGroupFactory().populate(ngt.getGroupId());
			}
			*/
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
			BaseGroupType dir = (BaseGroupType)Factories.getGroupFactory().findGroup(user, GroupEnumType.UNKNOWN, path, user.getOrganizationId());
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
				//out_Lifecycles = Factories.getLifecycleFactory().getListByGroup(dir, 0, 0, user.getOrganizationId());
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return out_obj;
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
			BaseGroupType dir = Factories.getGroupFactory().findGroup(user, groupType, path, user.getOrganizationId());
			if(dir == null){
				AuditService.denyResult(audit, "Invalid path: " + groupType.toString() + " " + path);
				return bean;
			}
			if(AuthorizationService.canView(user, dir) == false){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return bean;
			}
			Factories.getGroupFactory().populate(dir);	
			denormalize(dir);
			bean = (T)dir;

			AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getUrn());
			AuditService.permitResult(audit, "Access authorized to group " + dir.getName());
			
			
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bean;
	}
	
	
}