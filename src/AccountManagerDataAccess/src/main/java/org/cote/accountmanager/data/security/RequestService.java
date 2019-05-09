package org.cote.accountmanager.data.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.ApprovalFactory;
import org.cote.accountmanager.data.factory.ApproverFactory;
import org.cote.accountmanager.data.factory.ControlFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.factory.RequestFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccessRequestType;
import org.cote.accountmanager.objects.ApprovalResponseEnumType;
import org.cote.accountmanager.objects.ApprovalType;
import org.cote.accountmanager.objects.ApproverEnumType;
import org.cote.accountmanager.objects.ApproverType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.ControlActionEnumType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseEnumType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class RequestService {
	
	/// Owner attribute must point to an ApproverType
	/// The reason is this attribute is used in the context of relating 'ownership', or responsibility of an item,
	/// versus the underlying system ownership, which, in AccountManager, is the UserType.
	/// Therefore, this method allows an 'owner' to be specified without actually owning the underlying object, for purposes of managing access
	/// And, the owner can be broadened to be a group, role, or person, versus an individual user
	///
	public static final String ATTRIBUTE_NAME_OWNER = "owner";
	public static final Logger logger = LogManager.getLogger(RequestService.class);
	
	public static List<ControlType> getRequestControls(NameIdType object, boolean includeParent) throws FactoryException, ArgumentException{
		INameIdFactory factory = Factories.getFactory(FactoryEnumType.valueOf(object.getNameType().toString()));

		List<ControlType> ctls = ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).getControlsForType(object, ControlEnumType.POLICY, ControlActionEnumType.ACCESS,true, false);
		if(includeParent) {
			NameIdType parentObj = null;
			if(factory.isClusterByGroup() || object.getNameType() == NameEnumType.DATA) {
				parentObj = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getById(((NameIdDirectoryGroupType)object).getGroupId(), object.getOrganizationId());
			}
			else if(factory.isClusterByParent() && object.getParentId().compareTo(0L) > 0) {
				parentObj = factory.getById(object.getParentId(), object.getOrganizationId());
			}
			
			if(parentObj != null) {
				ctls.addAll(((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).getControlsForType(parentObj, ControlEnumType.POLICY, ControlActionEnumType.ACCESS,true, false));
			}
		}
		return ctls;
	}
	
	public static boolean isRequestable(NameIdType object) throws FactoryException, ArgumentException {
		return (getRequestControls(object, true).size() > 0);
	}
	
	
	public static void processRequestPolicies(AccessRequestType request) {
		
	}
	
	public static List<ApprovalType> processRequestPolicies(AccessRequestType request, List<PolicyType> policies) throws FactoryException, ArgumentException {
		for(PolicyType policy : policies) {
			PolicyDefinitionType pdt = PolicyDefinitionUtil.generatePolicyDefinition(policies.get(0));
			PolicyRequestType prt = PolicyDefinitionUtil.generatePolicyRequest(pdt);
			//logger.info(JSONUtil.exportObject(prt));
		}
		return new ArrayList<>();
	}
	
	
	public static List<PolicyType> getRequestPolicies(AccessRequestType request) throws FactoryException, ArgumentException{
		
		INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(request.getEntitlementType().toString()));
		NameIdType obj = iFact.getById(request.getEntitlementId(), request.getOrganizationId());
		if(obj == null) {
			logger.error("Failed to retrieve entitlement " + request.getEntitlementType().toString() + " #" + request.getEntitlementId());
			return new ArrayList<>();
		}
		return getRequestPolicies(obj, true);
	}
	public static List<PolicyType> getRequestPolicies(NameIdType object, boolean includeParent) throws FactoryException, ArgumentException{
		return getRequestPolicies(getRequestControls(object,includeParent));
	}
	public static List<ApprovalType> evaluateRequestPolicies(AccessRequestType request, List<PolicyType> policies) throws FactoryException, ArgumentException, DataAccessException{
		List<ApprovalType> approvals = new ArrayList<>();
		List<ApproverType> approvers = new ArrayList<>();
		if(request == null) throw new ArgumentException("Request is null");
		if(policies.isEmpty()) throw new ArgumentException("Expecting one or more policy");

		int errors = 0;
		
		for(PolicyType policy : policies) {
			PolicyDefinitionType pdt = PolicyDefinitionUtil.generatePolicyDefinition(policy);
			PolicyRequestType prt = PolicyDefinitionUtil.generatePolicyRequest(pdt);
			prt.setOrganizationPath(request.getOrganizationPath());
			for(FactType fact : prt.getFacts()) {
				if(fact.getFactType() == FactEnumType.PARAMETER) {
					fact.setFactoryType(FactoryEnumType.valueOf(request.getEntitlementType().toString()));
					fact.setFactData(Long.toString(request.getEntitlementId()));
				}
			}
			PolicyResponseType prr = PolicyEvaluator.evaluatePolicyRequest(prt);
			logger.info("Response: " + prr.getResponse().toString());
			if(prr.getResponse() == PolicyResponseEnumType.PERMIT) {
				if(prr.getResponseData().size() == 0) {
					logger.error("Expected one or more approvers in the response data for a successful approver policy request");
					errors++;
				}
				else {
					approvers.addAll(Arrays.asList(prr.getResponseData().toArray(new ApproverType[0])));
				}
			}
			
		}
		if(approvers.size() > 0) {
			/// Now add new approvaltypes for the request
			/// Do this as a bulk session, so that the request status is updated at the same time
			///
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			for(ApproverType approver : approvers) {
				ApprovalType approval = ((ApprovalFactory)Factories.getFactory(FactoryEnumType.APPROVAL)).newApproval(request, approver);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.APPROVAL, approval);
				approvals.add(approval);
			}
			request.setApprovalStatus(ApprovalResponseEnumType.PENDING);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.REQUEST, request);
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
		}
		return approvals;
	}
	public static List<PolicyType> getRequestPolicies(List<ControlType> controls) throws FactoryException, ArgumentException{
		List<PolicyType> policies = new ArrayList<>();
		Long orgId = 0L;
		List<Long> ids = new ArrayList<>();
		for(ControlType ctl : controls){
			if(orgId <= 0L) orgId = ctl.getOrganizationId();
			ids.add(ctl.getControlId());
		}
		if(orgId.compareTo(0L) > 0 && ids.size() > 0) {
			logger.info("Requesting policy in organization " + orgId);
			policies = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).listByIds(ArrayUtils.toPrimitive(ids.toArray(new Long[0])), orgId);
		}
		else {
			logger.warn("Failed to issue query with missing organization or object id information");
		}
		return policies;
	}
	public static List<AccessRequestType> listOpenAccessRequests(NameIdType requestor) throws FactoryException, ArgumentException{
		RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
		return rFact.getAccessRequestsForType(requestor, null, null, null, ApprovalResponseEnumType.REQUEST,0L, requestor.getOrganizationId());

	}
	
	public static ApproverType findOwner(NameIdType object) {
		ApproverType owner = null;
		PersonType defOwner = null;
		try {
			INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(object.getNameType().toString()));
			iFact.populate(object);
			Factories.getAttributeFactory().populateAttributes(object);
			
			/// Last option: Get the person object for the user that owns the object
			String ownerId = Factories.getAttributeFactory().getAttributeValueByName(object, ATTRIBUTE_NAME_OWNER);
			if(ownerId == null) {
				logger.info("Attempting to locate owner from parent attribute reference");
				logger.debug("Checking for Application Model - where a data group exists as a parent for a permission, role, and group structure");
				String groupPath = null;
				switch(object.getNameType()) {
					case PERMISSION:
						groupPath = ((PermissionFactory)iFact).getPermissionPath((BasePermissionType)object);
						break;
					case ROLE:
						groupPath = ((RoleFactory)iFact).getRolePath((BaseRoleType)object);
						break;
					case GROUP:
						BaseGroupType group = ((BaseGroupType)object); 
						groupPath = ((GroupFactory)iFact).getPath(group);
						//logger.info(JSONUtil.exportObject(group));
						break;
					default:
						logger.warn("Unhandled factory type in finding owner: " + object.getNameType());
						break;
				}
				int lInd = 0;
				if(groupPath != null && (lInd = groupPath.lastIndexOf('/')) > 0) {
					groupPath = groupPath.substring(0, lInd);
					BaseGroupType group = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null, GroupEnumType.DATA, groupPath, object.getOrganizationId());
					if(group != null) {
						Factories.getAttributeFactory().populateAttributes(group);
						ownerId = Factories.getAttributeFactory().getAttributeValueByName(object, ATTRIBUTE_NAME_OWNER);
					}
					logger.info("*** Looking in " + groupPath + " for " + object.getNameType());
				}
				
			}
			if(ownerId != null) {
				logger.info("Locating owner based on owner attribute");
				owner = ((ApproverFactory)Factories.getFactory(FactoryEnumType.APPROVER)).getById(Long.parseLong(ownerId),object.getOrganizationId());
				
			}
			if(owner == null) {
				INameIdFactory iUFact = Factories.getFactory(FactoryEnumType.USER);
				UserType ownerUser = iUFact.getById(object.getOwnerId(), object.getOrganizationId());
				if(ownerUser != null) {
					defOwner = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getPersonByUser(ownerUser);
					if(defOwner != null) {
						logger.info("Using object owner's person for ownership approvals");
						owner = new ApproverType();
						owner.setNameType(NameEnumType.APPROVER);
						owner.setEntitlementId(object.getId());
						owner.setEntitlementType(ApproverEnumType.valueOf(object.getNameType().toString()));
						owner.setApproverType(ApproverEnumType.PERSON);
						owner.setApproverId(defOwner.getId());
						owner.setApproverLevel(0);
					}
				}
			}
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		return owner;
		
	}
	
}
