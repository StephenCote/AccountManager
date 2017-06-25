package org.cote.accountmanager.data.services;

import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
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
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AddressType;
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
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.JAXBUtil;
import org.cote.accountmanager.util.MapUtil;
import org.cote.accountmanager.util.MimeUtil;

public class TypeSanitizer implements ITypeSanitizer{
	public static final Logger logger = LogManager.getLogger(TypeSanitizer.class);
	public TypeSanitizer(){
		
	}
	
	public <T> boolean usePostFetch(AuditEnumType type, T object){
		return (type.equals(AuditEnumType.DATA));
	}
	public <T> boolean useAlternateDelete(AuditEnumType type, T object){
		return (type.equals(AuditEnumType.GROUP) && ((BaseGroupType)object).getGroupType().equals(GroupEnumType.DATA));
	}
	public <T> boolean useAlternateUpdate(AuditEnumType type, T object){
		return false;
	}
	public <T> boolean useAlternateAdd(AuditEnumType type, T object){
		return (type.equals(AuditEnumType.ACCOUNT) || type.equals(AuditEnumType.USER));
	}
	public <T> boolean update(AuditEnumType type, UserType owner, T object) throws FactoryException, ArgumentException{
		return false;
	}
	public <T> T postFetch(AuditEnumType type, T object){
		T outObj = object;
		if(type.equals(AuditEnumType.DATA)){
			DataType d = (DataType)object;
			if(d.getDetailsOnly()){
				logger.error("Data is details only.  Was expecting full data");
			}
			if((BaseService.contextVault != null && d.getVaulted()) || d.getCompressed() || d.getPointer()){
				/// Make a copy of the object so as to operate on the copy and not a cached copy from the factory
				///
				d = JAXBUtil.clone(DataType.class, d);
				try {
					byte[] data = new byte[0];
					if(BaseService.contextVault != null && d.getVaulted()){
						try {
							data = BaseService.contextVaultService.extractVaultData(BaseService.contextVault, d);
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
					
					logger.error("Error",e);
				}
				
			}
		}
		return outObj;
	}
	public <T> boolean delete(AuditEnumType type, T object) throws FactoryException, ArgumentException{
		boolean out_bool = false;
		INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(type.toString()));
		if(type.equals(AuditEnumType.GROUP)){
			BaseGroupType group = (BaseGroupType)object;
			if(group.getGroupType().equals(GroupEnumType.DATA)){
				out_bool = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).deleteDirectoryGroup((DirectoryGroupType)group);

			}
		}
		return out_bool;
	}
	public <T> boolean add(AuditEnumType type, UserType owner, T object) throws FactoryException, ArgumentException{
		boolean out_bool = false;
		logger.info("Processing alternate add for type " + type.toString());
		INameIdFactory iFact = Factories.getNameIdFactory(FactoryEnumType.valueOf(type.toString()));
		if(type.equals(AuditEnumType.USER)){
			out_bool = ((UserFactory)iFact).add(object, true);
		}
		else if(type.equals(AuditEnumType.ACCOUNT)){
			out_bool = ((AccountFactory)iFact).add(object, true);
		}
		return out_bool;
	}
	public <T> T sanitizeNewObject(AuditEnumType type, UserType user, T in_obj) throws ArgumentException, FactoryException, DataException{
		T out_obj = null;
		INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(type.toString()));
		switch(type){
			case TAG:
				BaseTagType vtbean = (BaseTagType)in_obj;
				BaseTagType new_tag = ((TagFactory)iFact).newTag(user,vtbean.getName(),vtbean.getTagType(),vtbean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(vtbean, new_tag);
				out_obj = (T)new_tag;
				break;
			case ACCOUNT:
				AccountType v1bean = (AccountType)in_obj;
				AccountType new_acct = ((AccountFactory)iFact).newAccount(user,v1bean.getName(),v1bean.getAccountType(), v1bean.getAccountStatus(), v1bean.getGroupId());
				MapUtil.shallowCloneNameIdDirectoryType(v1bean, new_acct);
				out_obj = (T)new_acct;
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
				out_obj = (T)new_ct;
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
				out_obj = (T)new_fa;
				break;
			case FUNCTION:
				FunctionType v5bean = (FunctionType)in_obj;
				FunctionType new_fu = ((FunctionFactory)iFact).newFunction(user, v5bean.getGroupId());
				
				MapUtil.shallowCloneAznType(v5bean, new_fu);
				new_fu.setFunctionType(v5bean.getFunctionType());
				new_fu.setFunctionData(v5bean.getFunctionData());
				new_fu.setSourceUrl(v5bean.getSourceUrl());
				new_fu.setSourceUrn(v5bean.getSourceUrn());
				out_obj = (T)new_fu;
				break;
			case FUNCTIONFACT:
				FunctionFactType v6bean = (FunctionFactType)in_obj;
				FunctionFactType new_fuf = ((FunctionFactFactory)iFact).newFunctionFact(user, v6bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v6bean, new_fuf);
				new_fuf.setFactUrn(v6bean.getFactUrn());
				new_fuf.setFunctionUrn(v6bean.getFunctionUrn());
				out_obj = (T)new_fuf;
				break;
			case OPERATION:
				OperationType v7bean = (OperationType)in_obj;
				OperationType new_op = ((OperationFactory)iFact).newOperation(user, v7bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v7bean, new_op);
				new_op.setOperationType(v7bean.getOperationType());
				new_op.setOperation(v7bean.getOperation());
				out_obj = (T)new_op;
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
				out_obj = (T)new_pa;
				break;
			case POLICY:
				PolicyType v9bean = (PolicyType)in_obj;
				PolicyType new_po = ((PolicyFactory)iFact).newPolicy(user, v9bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v9bean, new_po);
				new_po.setDecisionAge(v9bean.getDecisionAge());
				new_po.setExpiresDate(v9bean.getExpiresDate());
				new_po.setEnabled(v9bean.getEnabled());
				new_po.getRules().addAll(v9bean.getRules());
				new_po.setCondition(v9bean.getCondition());
				
				out_obj = (T)new_po;
				break;
			case RULE:
				RuleType v10bean = (RuleType)in_obj;
				RuleType new_ru = ((RuleFactory)iFact).newRule(user, v10bean.getGroupId());
	
				MapUtil.shallowCloneAznType(v10bean, new_ru);
				new_ru.setRuleType(v10bean.getRuleType());
				new_ru.setCondition(v10bean.getCondition());
				new_ru.getRules().addAll(v10bean.getRules());
				new_ru.getPatterns().addAll(v10bean.getPatterns());
				out_obj = (T)new_ru;
				break;
			case PERSON:
				PersonType v11bean = (PersonType)in_obj;
				PersonType new_per = ((PersonFactory)iFact).newPerson(user, v11bean.getGroupId());
	
				MapUtil.shallowCloneNameIdDirectoryType(v11bean, new_per);
				new_per.setAlias(v11bean.getAlias());
				if(v11bean.getBirthDate() != null) new_per.setBirthDate(v11bean.getBirthDate());
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
				out_obj = (T)new_per;
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
				out_obj = (T)new_addr;
				break;
			case ROLE:
				BaseRoleType rlbean = (BaseRoleType)in_obj;
				BaseRoleType parentRole = null;
				if(rlbean.getParentId() > 0L){
					parentRole = iFact.getById(rlbean.getParentId(), rlbean.getOrganizationId());
					if(parentRole == null) throw new ArgumentException("Role parent #" + rlbean.getParentId() + " is invalid");
				}
				BaseRoleType new_role = ((RoleFactory)iFact).newRoleType(rlbean.getRoleType(),user, rlbean.getName(), parentRole);
				out_obj = (T)new_role;
				break;
				
			case PERMISSION:
				BasePermissionType perbean = (BasePermissionType)in_obj;
				BasePermissionType parentPermission = null;
				if(perbean.getParentId() > 0L){
					parentPermission = iFact.getById(perbean.getParentId(), perbean.getOrganizationId());
					if(parentPermission == null) throw new ArgumentException("Permission parent #" + perbean.getParentId() + " is invalid");
				}
				BasePermissionType new_per2 = ((PermissionFactory)iFact).newPermission(user, perbean.getName(), perbean.getPermissionType(), parentPermission, perbean.getOrganizationId());
				out_obj = (T)new_per2;
				break;
				
			case GROUP:
				BaseGroupType gbean = (BaseGroupType)in_obj;
				BaseGroupType parentGroup = null;
				if(gbean.getParentId() > 0L){
					parentGroup = iFact.getById(gbean.getParentId(), gbean.getOrganizationId());
				}
				BaseGroupType new_group = ((GroupFactory)iFact).newGroup(user, gbean.getName(), gbean.getGroupType(), parentGroup, gbean.getOrganizationId());
				out_obj = (T)new_group;
				break;
				
			case USER:
				UserType ubean = (UserType)in_obj;
				UserType new_user = ((UserFactory)iFact).newUser(ubean.getName(), UserEnumType.NORMAL, UserStatusEnumType.NORMAL, ubean.getOrganizationId());
				out_obj = (T)new_user;
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
				if(BaseService.contextVault != null && rbean.getVaulted()){
					rbean.setVaulted(false);
					try {
						BaseService.contextVaultService.setVaultBytes(BaseService.contextVault, new_rec, DataUtil.getValue(rbean));
					} catch (UnsupportedEncodingException e) {
						logger.error(e);
					}
				}
				else{
					DataUtil.setValue(new_rec, DataUtil.getValue(rbean));
				}
				if(rbean.getPointer() == true){
					logger.error("Creating data pointers is forbidden for sanitized objects regardless of configuration");
					return null;
				}
				out_obj = (T)new_rec;
				break;				
		}
		return out_obj;
	}
}
