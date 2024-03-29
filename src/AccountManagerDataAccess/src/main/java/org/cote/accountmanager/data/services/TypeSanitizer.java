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

import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.VaultBean;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.AddressFactory;
import org.cote.accountmanager.data.factory.ContactFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.FactFactory;
import org.cote.accountmanager.data.factory.FunctionFactFactory;
import org.cote.accountmanager.data.factory.FunctionFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.OperationFactory;
import org.cote.accountmanager.data.factory.PatternFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RuleFactory;
import org.cote.accountmanager.data.factory.TagFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccessRequestType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.ApproverType;
import org.cote.accountmanager.objects.AttributeType;
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
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.JAXBUtil;
import org.cote.accountmanager.util.MapUtil;
import org.cote.accountmanager.util.MimeUtil;

public class TypeSanitizer implements ITypeSanitizer{
	public static final Logger logger = LogManager.getLogger(TypeSanitizer.class);
	private static final VaultService vaultService = new VaultService();
	private static Set<String> pointerKeys = new HashSet<>();
	public TypeSanitizer(){
		
	}
	public static void addPointerKey(String key) {
		pointerKeys.add(key);
	}
	public <T> boolean usePostFetch(AuditEnumType type, T object){
		return (type.equals(AuditEnumType.DATA));
	}
	public <T> boolean useAlternateDelete(AuditEnumType type, T object){
		return (type.equals(AuditEnumType.GROUP) && ((BaseGroupType)object).getGroupType().equals(GroupEnumType.DATA));
	}
	public <T> boolean useAlternateUpdate(AuditEnumType type, T object){
		return (type.equals(AuditEnumType.DATA));
	}
	public <T> boolean useAlternateAdd(AuditEnumType type, T object){
		return (type.equals(AuditEnumType.ACCOUNT) || type.equals(AuditEnumType.USER));
	}
	
	private boolean updateVaultData(UserType user, DataType data) throws ArgumentException{
		boolean outBool = false;
		if(data.getDetailsOnly() == false && data.getVaulted() && data.getVaultId() != null){
			data.setVaulted(false);
			VaultBean vaultBean = vaultService.getVaultByUrn(user, data.getVaultId());
			if(vaultBean == null) throw new ArgumentException("Vault '" + data.getVaultId() + "' does not exist");
			try {
				if(vaultBean.getActiveKeyId() == null) vaultService.newActiveKey(vaultBean);
				vaultService.setVaultBytes(vaultBean, data, DataUtil.getValue(data));
				outBool = true;
			} catch (UnsupportedEncodingException | DataException | FactoryException e) {
				logger.error(e);
			}
		}
		return outBool;
	}
	
	public <T> boolean update(AuditEnumType type, UserType owner, T object) throws FactoryException, ArgumentException{
		boolean outBool = false;
		switch(type){
			case DATA:
				INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(type.toString()));
				DataType data = (DataType)object;

				if(data.getDetailsOnly() == false && data.getVaulted() && data.getVaultId() != null && !updateVaultData(owner,data)){
					logger.error("Failed to encipher vault data");
					return false;
				}
				/// && !popPointer(data)
				///
				if(data.getDetailsOnly() == false && data.getPointer()){
					logger.error("Updating pointers is forbidden for sanitized objects regardless of configuration.  The data must be deleted and recreated.");
					return false;
				}
				outBool = iFact.update(data);
				
				break;
			default:
				logger.error("Unhandled type: " + type.toString());
				break;
		}
		return outBool;
	}
	
	/// 2017/09/14
	/// TODO: there appears to be an issue with the way vaulted data is being extracted through TypeSanitizer - it's not being decrypted for some reason (even though the call is being made).
	///
	@SuppressWarnings("unchecked")
	public <T> T postFetch(AuditEnumType type, UserType user, T object){
		T outObj = object;
		if(type.equals(AuditEnumType.DATA)){
			DataType d = (DataType)object;
			if(d.getDetailsOnly()){
				logger.error("Data is details only.  Was expecting full data");
			}
			if((d.getVaulted()) || d.getCompressed() || d.getPointer()){
				/// Make a copy of the object so as to operate on the copy and not a cached copy from the factory
				///
				d = JAXBUtil.clone(DataType.class, d);
				try {
					byte[] data = new byte[0];
					if( d.getVaulted()){
						try {
							VaultBean vaultBean = vaultService.getVaultByUrn(user, d.getVaultId());
							data = vaultService.extractVaultData(vaultBean, d);
						} catch (FactoryException | ArgumentException e) {
							logger.error(e);
						}
					}
					else{
						data = DataUtil.getValue(d);
					}
					d.setCompressed(false);
					d.setDataBytesStore(data);
					d.setReadDataBytes(false);

					outObj = (T)d;
				} catch (DataException e) {
					
					logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				}
				
			}
		}
		return outObj;
	}
	public <T> boolean delete(AuditEnumType type, T object) throws FactoryException, ArgumentException{
		boolean outBool = false;
		if(type.equals(AuditEnumType.GROUP)){
			BaseGroupType group = (BaseGroupType)object;
			if(group.getGroupType().equals(GroupEnumType.DATA)){
				outBool = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).deleteDirectoryGroup((DirectoryGroupType)group);

			}
		}
		return outBool;
	}
	public <T> boolean add(AuditEnumType type, UserType owner, T object) throws FactoryException, ArgumentException{
		boolean outBool = false;
		logger.info("Processing alternate add for type " + type.toString());
		INameIdFactory iFact = Factories.getNameIdFactory(FactoryEnumType.valueOf(type.toString()));
		if(type.equals(AuditEnumType.USER)){
			//outBool = ((UserFactory)iFact).add(object, true);
			UserType userObj = (UserType)object;
			AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Create person as user", AuditEnumType.USER, owner.getUrn());
			AuditService.targetAudit(audit, AuditEnumType.PERSON, userObj.getName());
			outBool = PersonService.createUserAsPerson(audit, userObj.getName(), null, null, userObj.getUserType(), userObj.getUserStatus(), owner.getOrganizationId());
		}
		else if(type.equals(AuditEnumType.ACCOUNT)){
			outBool = ((AccountFactory)iFact).add(object, true);
		}
		return outBool;
	}
	@SuppressWarnings("unchecked")
	public <T> T sanitizeNewObject(AuditEnumType type, UserType user, T in_obj) throws ArgumentException, FactoryException, DataException{
		T outObj = null;
		INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(type.toString()));
		switch(type){
			case APPROVER:
				ApproverType apbean = (ApproverType)in_obj;
				ApproverType new_apr = new ApproverType();
				MapUtil.shallowCloneNameIdDirectoryType(apbean, new_apr);
				new_apr.setApprovalType(apbean.getApprovalType());
				new_apr.setApproverId(apbean.getApproverId());
				new_apr.setApproverLevel(apbean.getApproverLevel());
				new_apr.setApproverType(apbean.getApproverType());
				new_apr.setEntitlementId(apbean.getEntitlementId());
				new_apr.setEntitlementType(apbean.getEntitlementType());
				new_apr.setReferenceId(apbean.getReferenceId());
				new_apr.setReferenceType(apbean.getReferenceType());
				outObj = (T)new_apr;
				break;
			case REQUEST:
				AccessRequestType rqbean = (AccessRequestType)in_obj;
				AccessRequestType new_rq = new AccessRequestType();
				MapUtil.shallowCloneNameIdDirectoryType(rqbean, new_rq);
				new_rq.setActionType(rqbean.getActionType());
				new_rq.setCreatedDate(rqbean.getCreatedDate());
				new_rq.setDelegateId(rqbean.getDelegateId());
				new_rq.setDelegateType(rqbean.getDelegateType());
				new_rq.setDescription(rqbean.getDescription());
				new_rq.setEntitlementId(rqbean.getEntitlementId());
				new_rq.setEntitlementType(rqbean.getEntitlementType());
				new_rq.setExpiryDate(rqbean.getExpiryDate());
				new_rq.setModifiedDate(rqbean.getModifiedDate());
				new_rq.setReferenceId(rqbean.getReferenceId());
				new_rq.setReferenceType(rqbean.getReferenceType());
				new_rq.setRequestorId(rqbean.getRequestorId());
				new_rq.setRequestorType(rqbean.getRequestorType());

				break;
			case TAG:
				BaseTagType vtbean = (BaseTagType)in_obj;
				BaseTagType new_tag = ((TagFactory)iFact).newTag(user,vtbean.getName(),vtbean.getTagType(),vtbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(vtbean, new_tag);
				outObj = (T)new_tag;
				break;
			case ACCOUNT:
				AccountType v1bean = (AccountType)in_obj;
				AccountType new_acct = ((AccountFactory)iFact).newAccount(user,v1bean.getName(),v1bean.getAccountType(), v1bean.getAccountStatus(), v1bean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(v1bean, new_acct);
				outObj = (T)new_acct;
				break;
			case CONTACT:
				ContactType v2bean = (ContactType)in_obj;
				ContactType new_ct = ((ContactFactory)iFact).newContact(user, v2bean.getGroupId());
	
				MapUtil.shallowCloneNameIdDirectoryType(v2bean, new_ct);
				new_ct.setContactType(v2bean.getContactType());
				new_ct.setDescription(v2bean.getDescription());
				new_ct.setLocationType(v2bean.getLocationType());
				new_ct.setContactValue(v2bean.getContactValue());
				new_ct.setPreferred(v2bean.getPreferred());
				outObj = (T)new_ct;
				break;
	
			case FACT:
				FactType v4bean = (FactType)in_obj;
				FactType new_fa = ((FactFactory)iFact).newFact(user, v4bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v4bean, new_fa);
				new_fa.setFactType(v4bean.getFactType());
				new_fa.setFactData(v4bean.getFactData());
				new_fa.setFactoryType(v4bean.getFactoryType());
				new_fa.setSourceDataType(v4bean.getSourceDataType());
				new_fa.setSourceUrl(v4bean.getSourceUrl());
				new_fa.setSourceUrn(v4bean.getSourceUrn());
				outObj = (T)new_fa;
				break;
			case FUNCTION:
				FunctionType v5bean = (FunctionType)in_obj;
				FunctionType new_fu = ((FunctionFactory)iFact).newFunction(user, v5bean.getGroupId());
				
				MapUtil.shallowCloneAznType(v5bean, new_fu);
				new_fu.setFunctionType(v5bean.getFunctionType());
				new_fu.setFunctionData(v5bean.getFunctionData());
				new_fu.setSourceUrl(v5bean.getSourceUrl());
				new_fu.setSourceUrn(v5bean.getSourceUrn());
				outObj = (T)new_fu;
				break;
			case FUNCTIONFACT:
				FunctionFactType v6bean = (FunctionFactType)in_obj;
				FunctionFactType new_fuf = ((FunctionFactFactory)iFact).newFunctionFact(user, v6bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v6bean, new_fuf);
				new_fuf.setFactUrn(v6bean.getFactUrn());
				new_fuf.setFunctionUrn(v6bean.getFunctionUrn());
				outObj = (T)new_fuf;
				break;
			case OPERATION:
				OperationType v7bean = (OperationType)in_obj;
				OperationType new_op = ((OperationFactory)iFact).newOperation(user, v7bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v7bean, new_op);
				new_op.setOperationType(v7bean.getOperationType());
				new_op.setOperation(v7bean.getOperation());
				outObj = (T)new_op;
				break;
			case PATTERN:
				PatternType v8bean = (PatternType)in_obj;
				PatternType new_pa = ((PatternFactory)iFact).newPattern(user, v8bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v8bean, new_pa);
				new_pa.setPatternType(v8bean.getPatternType());
				new_pa.setComparator(v8bean.getComparator());
				new_pa.setFactUrn(v8bean.getFactUrn());
				new_pa.setMatchUrn(v8bean.getMatchUrn());
				new_pa.setOperationUrn(v8bean.getOperationUrn());
				outObj = (T)new_pa;
				break;
			case POLICY:
				PolicyType v9bean = (PolicyType)in_obj;
				PolicyType new_po = ((PolicyFactory)iFact).newPolicy(user, v9bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v9bean, new_po);
				new_po.setDecisionAge(v9bean.getDecisionAge());
				new_po.setExpiryDate(v9bean.getExpiryDate());
				new_po.setEnabled(v9bean.getEnabled());
				new_po.getRules().addAll(v9bean.getRules());
				new_po.setCondition(v9bean.getCondition());
				
				outObj = (T)new_po;
				break;
			case RULE:
				RuleType v10bean = (RuleType)in_obj;
				RuleType new_ru = ((RuleFactory)iFact).newRule(user, v10bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v10bean, new_ru);
				new_ru.setRuleType(v10bean.getRuleType());
				new_ru.setCondition(v10bean.getCondition());
				new_ru.getRules().addAll(v10bean.getRules());
				new_ru.getPatterns().addAll(v10bean.getPatterns());
				outObj = (T)new_ru;
				break;
			case PERSON:
				PersonType v11bean = (PersonType)in_obj;
				PersonType new_per = ((PersonFactory)iFact).newPerson(user, v11bean.getGroupId());
	
				MapUtil.shallowCloneNameIdDirectoryType(v11bean, new_per);
				new_per.setAlias(v11bean.getAlias());
				if(v11bean.getBirthDate() != null) new_per.setBirthDate(v11bean.getBirthDate());

				new_per.setDescription(v11bean.getDescription());
				new_per.setFirstName(v11bean.getFirstName());
				new_per.setGender(v11bean.getGender());
				new_per.setLastName(v11bean.getLastName());
				new_per.setMiddleName(v11bean.getMiddleName());
				new_per.setPrefix(v11bean.getPrefix());
				new_per.setSuffix(v11bean.getSuffix());
				new_per.setTitle(v11bean.getTitle());
				
				new_per.getAccounts().addAll(v11bean.getAccounts());
				new_per.getDependents().addAll(v11bean.getDependents());
				new_per.getPartners().addAll(v11bean.getPartners());
				new_per.getUsers().addAll(v11bean.getUsers());
				outObj = (T)new_per;
				break;
			case ADDRESS:
				AddressType v12bean = (AddressType)in_obj;
				AddressType new_addr = ((AddressFactory)iFact).newAddress(user, v12bean.getGroupId());
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
				outObj = (T)new_addr;
				break;
			case ROLE:
				BaseRoleType rlbean = (BaseRoleType)in_obj;
				BaseRoleType parentRole = null;
				if(rlbean.getParentId() > 0L){
					parentRole = iFact.getById(rlbean.getParentId(), user.getOrganizationId());
					if(parentRole == null) throw new ArgumentException("Role parent #" + rlbean.getParentId() + " is invalid");
				}
				BaseRoleType new_role = ((RoleFactory)iFact).newRoleType(rlbean.getRoleType(),user, rlbean.getName(), parentRole);
				outObj = (T)new_role;
				break;
				
			case PERMISSION:
				BasePermissionType perbean = (BasePermissionType)in_obj;
				BasePermissionType parentPermission = null;
				if(perbean.getParentId() > 0L){
					parentPermission = iFact.getById(perbean.getParentId(), user.getOrganizationId());
					if(parentPermission == null) throw new ArgumentException("Permission parent #" + perbean.getParentId() + " is invalid");
				}
				BasePermissionType new_per2 = ((PermissionFactory)iFact).newPermission(user, perbean.getName(), perbean.getPermissionType(), parentPermission, user.getOrganizationId());
				outObj = (T)new_per2;
				break;
				
			case GROUP:
				BaseGroupType gbean = (BaseGroupType)in_obj;
				BaseGroupType parentGroup = null;
				if(gbean.getParentId() > 0L){
					parentGroup = iFact.getById(gbean.getParentId(), gbean.getOrganizationId());
				}
				BaseGroupType new_group = ((GroupFactory)iFact).newGroup(user, gbean.getName(), gbean.getGroupType(), parentGroup, gbean.getOrganizationId());
				outObj = (T)new_group;
				break;
				
			case USER:
				UserType ubean = (UserType)in_obj;
				UserType new_user = ((UserFactory)iFact).newUser(ubean.getName(), UserEnumType.NORMAL, UserStatusEnumType.NORMAL, ubean.getOrganizationId());
				outObj = (T)new_user;
				break;
			case DATA:
				DataType rbean = (DataType)in_obj;
				DataType new_rec = ((DataFactory)iFact).newData(user, rbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(rbean, new_rec);
				new_rec.setDescription(rbean.getDescription());
				new_rec.setDimensions(rbean.getDimensions());
				
				if(rbean.getExpiryDate() != null) new_rec.setExpiryDate(rbean.getExpiryDate());
				if(rbean.getMimeType() == null){
					logger.warn("MimeType not specified.  Attempting to resolve using the name '" + rbean.getName() + "'");
					rbean.setMimeType(MimeUtil.getType(rbean.getName()));
				}
				new_rec.setMimeType(rbean.getMimeType());
				
				new_rec.setRating(rbean.getRating());
				if(rbean.getPointer()){
					if(popPointer(rbean)) {
						/// unset the pointer bit so only the pointer value is read, not the underlying value in the subsequent getValue call
						new_rec.setPointer(true);
						rbean.setPointer(false);
					}
					else {
						logger.error("Creating data pointers is forbidden for sanitized objects regardless of configuration");
						return null;
					}
				}
				if(rbean.getVaulted() && rbean.getVaultId() != null){
					rbean.setVaulted(false);
					VaultBean vaultBean = vaultService.getVaultByUrn(user, rbean.getVaultId());
					if(vaultBean == null) throw new ArgumentException("Vault '" + rbean.getVaultId() + "' does not exist");
					try {
						if(vaultBean.getActiveKeyId() == null) vaultService.newActiveKey(vaultBean);
						vaultService.setVaultBytes(vaultBean, new_rec, DataUtil.getValue(rbean));
					} catch (UnsupportedEncodingException e) {
						logger.error(e);
					}
				}
				else{
					if(rbean.getPointer()) DataUtil.setValue(new_rec, rbean.getDataBytesStore());
					else DataUtil.setValue(new_rec, DataUtil.getValue(rbean));
				}

				outObj = (T)new_rec;
				break;
			default:
				logger.warn(String.format(FactoryException.UNHANDLED_TYPE, type));
				break;
		}
		return outObj;
	}
	
	/// Check if the object set to use a pointer includes a key to permit its use
	/// For example: This is set when auto pointer is enabled so that large file uploads are stored to persistant storage outside of the database
	///
	private static boolean popPointer(DataType data) {
		boolean popped = false;
		AttributeType keyAttr = Factories.getAttributeFactory().getAttributeByName(data, "autoPointerKey");
		if(keyAttr != null) {
			if(keyAttr.getValues().size() > 0 && pointerKeys.contains(keyAttr.getValues().get(0))) {
				logger.info("Pointer permitted with key " + keyAttr.getValues().get(0) + ": " + data.getSize());
				pointerKeys.remove(keyAttr.getValues().get(0));
				popped = true;
			}
			data.getAttributes().remove(keyAttr);
		}
		return popped;
	}
}
