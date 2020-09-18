package org.cote.accountmanager.data.security;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.ApprovalFactory;
import org.cote.accountmanager.data.factory.ApproverFactory;
import org.cote.accountmanager.data.factory.BulkFactory;
import org.cote.accountmanager.data.factory.ControlFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.MessageFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.factory.RequestFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.UserService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccessRequestType;
import org.cote.accountmanager.objects.ApprovalEnumType;
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
import org.cote.accountmanager.objects.MessageSpoolType;
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
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.objects.types.SpoolBucketEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.SpoolStatusEnumType;

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

		List<ControlType> ctls = ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).getControlsForType(object, ControlEnumType.POLICY, 0L, ControlActionEnumType.ACCESS,true, false);
		if(includeParent) {
			NameIdType parentObj = null;
			if(factory.isClusterByGroup() || object.getNameType() == NameEnumType.DATA) {
				parentObj = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getById(((NameIdDirectoryGroupType)object).getGroupId(), object.getOrganizationId());
			}
			else if(factory.isClusterByParent() && object.getParentId().compareTo(0L) > 0) {
				parentObj = factory.getById(object.getParentId(), object.getOrganizationId());
			}
			
			if(parentObj != null) {
				ctls.addAll(((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).getControlsForType(parentObj, ControlEnumType.POLICY, 0L, ControlActionEnumType.ACCESS,true, false));
			}
		}
		return ctls;
	}
	
	public static boolean isRequestable(NameIdType object) throws FactoryException, ArgumentException {
		return (getRequestControls(object, true).size() > 0);
	}
	
	public static List<MessageSpoolType> getApprovalRequestMessages(UserType user, SpoolStatusEnumType spoolStatus, long requestId, long approvalId, NameEnumType principalType, long principalId, long organizationId) throws FactoryException, ArgumentException{
		MessageFactory mFact = Factories.getFactory(FactoryEnumType.MESSAGE);
		return mFact.getMessages(user, SpoolBucketEnumType.APPROVAL, SpoolNameEnumType.ACCESS, spoolStatus, (user != null ? mFact.getUserMessagesGroup(user) : null), null, FactoryEnumType.APPROVAL, approvalId, FactoryEnumType.valueOf(principalType.toString()),principalId,FactoryEnumType.REQUEST, requestId, organizationId);

	}
	
	/// breakOnLevel means to only process requests (add messages to the queue) for the active level
	/// Invoking the method subsequently will add messages to the same users versus moving to the next level
	public static int processPendingRequestPolicies(AccessRequestType request, boolean breakOnLevel, boolean resendMessage) throws FactoryException, ArgumentException {

		int outCount = 0;
		ApprovalFactory aFact = Factories.getFactory(FactoryEnumType.APPROVAL);
		MessageFactory mFact = Factories.getFactory(FactoryEnumType.MESSAGE);
		RoleParticipationFactory rFact = Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION);
		PersonFactory pFact = Factories.getFactory(FactoryEnumType.PERSON);
		List<ApprovalType> approvals = aFact.listApprovalsForRequest(request);
		int currentLevel = 0;
		if(approvals.size() == 0) throw new ArgumentException("Request " + request.getObjectId() + " has not been evaluated to include any approvals");

		for(ApprovalType approval : approvals) {

			
			if(approval.getResponse() != ApprovalResponseEnumType.PENDING && approval.getResponse() != ApprovalResponseEnumType.REQUEST) {
				logger.info("Approval " + approval.getApproverType() + " " + approval.getApproverId() + " already responded with " + approval.getResponse());
				continue;
			}
			if(currentLevel > 0 && currentLevel < approval.getApproverLevel() && breakOnLevel) {
				logger.info("Breaking request queue processing at level " + currentLevel + " < " + approval.getApproverLevel());
				break;
			}
			currentLevel = approval.getApproverLevel();
			INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(approval.getApproverType().toString()));
			NameIdType obj = iFact.getById(approval.getApproverId(), approval.getOrganizationId());
			
			List<UserType> users = new ArrayList<>();
			switch(obj.getNameType()) {
				case USER:
					users.add((UserType)obj);
					break;
				case ROLE:
					BaseRoleType brt = (BaseRoleType)obj;
					switch(brt.getRoleType()) {
						case USER:
							users.addAll(rFact.getUsersInRole(brt));
							break;
						case PERSON:
							List<PersonType> pers = rFact.getPersonsInRole(brt);
							for(PersonType per : pers) {
								pFact.populate(per);
								users.addAll(per.getUsers());
							}
							break;
						default:
							logger.warn("Unhandled role type: " + brt.getRoleType());
							break;
					}
					
					break;
				case PERSON:
					PersonType per = (PersonType)obj;
					pFact.populate(per);
					users.addAll(per.getUsers());
					break;
				default:
					logger.warn("Unhandled actor type: " + obj.getNameType());
					break;
			}
			logger.info("Process approver " + obj.getUrn() + " with " + users.size() + " users");
			
			for(UserType user : users) {
				if(!resendMessage) {
					///List<MessageSpoolType> aMsg = mFact.getMessages(user, SpoolBucketEnumType.APPROVAL, SpoolNameEnumType.ACCESS, SpoolStatusEnumType.UNKNOWN, mFact.getUserMessagesGroup(user),null, FactoryEnumType.APPROVAL, approval.getId(), FactoryEnumType.valueOf(obj.getNameType().toString()),obj.getId(),user.getOrganizationId());
					List<MessageSpoolType> aMsg = getApprovalRequestMessages(user, SpoolStatusEnumType.UNKNOWN, request.getId(), approval.getId(), obj.getNameType(), obj.getId(), user.getOrganizationId());
					if(aMsg.size() > 0) {
						logger.info("User " + user.getUrn() + " already received a message regarding this request");
						continue;
					}
				}
				MessageSpoolType msg = mFact.newMessage(SpoolNameEnumType.ACCESS, user);
				msg.setSpoolBucketType(SpoolBucketEnumType.APPROVAL);
				msg.setExpiration(approval.getExpiryDate());
				/// Note, the status is transmitted in that it only internal at this point
				/// Subsequent request processing can chuck this message and create a new one to transmit outside of Account Manager for notification
				/// 
				msg.setSpoolStatus(SpoolStatusEnumType.TRANSMITTED);
				msg.setTransportType(FactoryEnumType.REQUEST);
				msg.setTransportId(request.getId());
				msg.setReferenceType(FactoryEnumType.APPROVAL);
				msg.setReferenceId(approval.getId());
				msg.setRecipientType(FactoryEnumType.valueOf(obj.getNameType().toString()));
				msg.setRecipientId(obj.getId());
				msg.setName(approval.getResponse().toString() + " Level " + approval.getApproverLevel() + " Approval for " + request.getRequestorType().toString() + " " + request.getRequestorId() + " to Access " + request.getEntitlementType() + " " + request.getEntitlementId());
				if(mFact.addMessage(msg)) outCount++;
			}
		}
		return outCount;
	}
	public static ApprovalResponseEnumType resolveRequest(AccessRequestType request) throws FactoryException, ArgumentException {

		RequestFactory rFact = Factories.getFactory(FactoryEnumType.REQUEST);
		ApprovalFactory aFact = Factories.getFactory(FactoryEnumType.APPROVAL);
		List<ApprovalType> approvals = aFact.listApprovalsForRequest(request);

		if(request.getApprovalStatus() != ApprovalResponseEnumType.PENDING) {
			logger.warn("Only pending requests may be resolved");
			return  request.getApprovalStatus();
		}
		int approveCount = 0;
		int denyCount = 0;
		boolean updated = false;
		for(ApprovalType appr : approvals) {
			if(appr.getResponse() == ApprovalResponseEnumType.APPROVE) approveCount++;
			else if(appr.getResponse() == ApprovalResponseEnumType.DENY) denyCount++;
		}
		if(denyCount > 0) {
			logger.info("Request " + request.getObjectId() + " is denied");
			request.setApprovalStatus(ApprovalResponseEnumType.DENY);
			updated = true;
		}
		else if(approveCount == approvals.size()) {
			logger.info("Request " + request.getObjectId() + " is granted");
			request.setApprovalStatus(ApprovalResponseEnumType.APPROVE);
			updated = true;
		}
		if(updated) rFact.update(request);
		return request.getApprovalStatus();
	}
	public static boolean isAccessRequestMessage(MessageSpoolType message) {
		return (
			message != null
			&&
			message.getSpoolBucketName() == SpoolNameEnumType.ACCESS
			&&
			message.getSpoolBucketType() == SpoolBucketEnumType.APPROVAL
			&&
			message.getTransportType() == FactoryEnumType.REQUEST
			&&
			message.getReferenceType() == FactoryEnumType.APPROVAL
		);
	}
	public static boolean approve(NameIdType approver, MessageSpoolType message, boolean unfold) throws FactoryException, ArgumentException {
		return updateStatus(approver, message, unfold, ApprovalResponseEnumType.APPROVE);
	}
	public static boolean reject(NameIdType approver, MessageSpoolType message, boolean unfold) throws FactoryException, ArgumentException {
		return updateStatus(approver, message, unfold, ApprovalResponseEnumType.DENY);
	}
	protected static boolean updateStatus(NameIdType approver, MessageSpoolType message, boolean unfold, ApprovalResponseEnumType response) throws FactoryException, ArgumentException {
		MessageFactory mFact = Factories.getFactory(FactoryEnumType.MESSAGE);
		RequestFactory rFact = Factories.getFactory(FactoryEnumType.REQUEST);
		ApprovalFactory aFact = Factories.getFactory(FactoryEnumType.APPROVAL);
		boolean outBool = false;
		if(!isAccessRequestMessage(message)) {
			logger.error("Invalid access request message");
			throw new ArgumentException("Invalid access request message");
		}

		AccessRequestType req = rFact.getById(message.getTransportId(), message.getOrganizationId());
		if(req == null) {
			logger.error("Invalid request id: " + message.getTransportId());
			return outBool;
		}
		ApprovalType appr = aFact.getById(message.getReferenceId(), message.getOrganizationId());
		if(appr == null) {
			logger.error("Invalid approval id: " + message.getReferenceId());
			return outBool;
		}

		outBool = updateStatus(approver, req, appr, unfold, response);
		message.setSpoolStatus(SpoolStatusEnumType.RESPONDED);
		mFact.update(message);
		return outBool;

	}
	protected static boolean updateStatus(NameIdType approver, AccessRequestType req, ApprovalType appr, boolean unfold, ApprovalResponseEnumType response) throws FactoryException, ArgumentException {
	
		boolean outBool = false;
		
		RoleFactory roFact = Factories.getFactory(FactoryEnumType.ROLE);
		ApprovalFactory aFact = Factories.getFactory(FactoryEnumType.APPROVAL);
		
		
		/// The approver must be the same object reference for the approval
		/// For Roles and Groups, the reference needs to be found and membership tested, then the approval is modified to the member who approves
		/// Delegation is handled by issuing another approval entry, not on the approval entry itself
		/// TODO: The delegation field on the request object itself should be moved to the approval schema
		///
		boolean allowPromote = false;
		NameIdType useApprover = approver;
		if(appr.getApproverType() == ApproverEnumType.ROLE && (approver.getNameType() == NameEnumType.PERSON || approver.getNameType() == NameEnumType.USER)) {
			BaseRoleType role = roFact.getById(appr.getApproverId(), appr.getOrganizationId());
			
			/// Undo the current limited messaging where the message targeted a user versus the object type
			/// This means the check to see if a user is in an effective role won't pass for a person because the authorization pattern only looks from person to user, and not visa-versa
			/// Therefore, if a user approver is provided, but the approver record is for a PERSON role, then check for a person object
			///
			if(role.getRoleType() == RoleEnumType.PERSON && approver.getNameType() == NameEnumType.USER) {
				NameIdType checkPer = UserService.readSystemPersonForUser((UserType)approver, (UserType)approver);
				if(checkPer != null) {
					logger.info("Elevating user object to person object within approval");
					approver = checkPer;
				}
			}
			if(
				(role.getRoleType() == RoleEnumType.PERSON && approver.getNameType() == NameEnumType.PERSON && RoleService.getIsPersonInRole(role, (PersonType)approver))
				||
				(role.getRoleType() == RoleEnumType.USER && approver.getNameType() == NameEnumType.USER && RoleService.getIsUserInRole(role, (UserType)approver))
			) {
				useApprover = role;
				allowPromote = true;
			}
			else {
				logger.warn("Approver " + approver.getUrn() + " is not in role " + role.getUrn());
			}
		}

		if(!useApprover.getId().equals(appr.getApproverId()) || appr.getApproverType() != ApproverEnumType.valueOf(useApprover.getNameType().toString())) {
			logger.error("Approver does not match the request approval record: " + useApprover.getId() + "==" + appr.getApproverId() + " && " + appr.getApproverType() + " == " + useApprover.getNameType());
			/// logger.info(JSONUtil.exportObject(useApprover));
			/// logger.info(JSONUtil.exportObject(appr));
			return outBool;
		}
		if(allowPromote) {
			appr.setApproverId(approver.getId());
			appr.setApproverType(ApproverEnumType.valueOf(approver.getNameType().toString()));
		}
		logger.info("Responding to request with " + response.toString());
		appr.setResponse(response);
		outBool = aFact.update(appr);
		return outBool;
	}
	/*
	public static List<ApprovalType> processRequestPolicies(AccessRequestType request, List<PolicyType> policies) throws FactoryException, ArgumentException {
		for(PolicyType policy : policies) {
			PolicyDefinitionType pdt = PolicyDefinitionUtil.generatePolicyDefinition(policies.get(0));
			PolicyRequestType prt = PolicyDefinitionUtil.generatePolicyRequest(pdt);
			//logger.info(JSONUtil.exportObject(prt));
		}
		return new ArrayList<>();
	}
	*/
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
		ApprovalFactory aFact = ((ApprovalFactory)Factories.getFactory(FactoryEnumType.APPROVAL));
		BulkFactory bFact = BulkFactories.getBulkFactory();

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

			if(prr.getResponse() == PolicyResponseEnumType.PERMIT) {
				if(prr.getResponseData().size() == 0) {
					logger.error("Expected one or more approvers in the response data for a successful approver policy request");
					errors++;
				}
				else {
					approvers.addAll(Arrays.asList(prr.getResponseData().toArray(new ApproverType[0])));
				}
			}
			else {
				logger.warn("Access request policy was not permitted with: " + prr.getResponse().toString());
			}
			
		}
		List<ApprovalType> cApprs = aFact.listApprovalsForRequest(request);

		Map<String,ApprovalType> apprSet = new HashMap<>();
		for(ApprovalType appr : cApprs) {
			apprSet.put(appr.getRequestId() + "-" + appr.getApprovalType() + "-" + appr.getApproverType() + "-" + appr.getApproverId() + "-" + appr.getApproverLevel(), appr);
		}

		if(approvers.size() > 0) {
			/// Now add new approvaltypes for the request
			/// Do this as a bulk session, so that the request status is updated at the same time
			/// TODO: Add a constraint on the approval so there don't wind up being duplicates for the same approval on the same requestid
			///
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			for(ApproverType approver : approvers) {
				String key = request.getObjectId() + "-" + approver.getApprovalType() + "-" + approver.getApproverType() + "-" + approver.getApproverId() + "-" + approver.getApproverLevel();
				ApprovalType approval = null;
				if(!apprSet.containsKey(key)){
					approval = aFact.newApproval(request, approver);
					bFact.createBulkEntry(sessionId, FactoryEnumType.APPROVAL, approval);
				}
				else {
					logger.debug("Request " + request.getObjectId() + " already includes " + approver.getApproverType() + " approver " + approver.getApproverId() + " at level " + approver.getApproverLevel());
					approval = apprSet.get(key);
				}
				approvals.add(approval);
			}
			request.setApprovalStatus(ApprovalResponseEnumType.PENDING);
			bFact.modifyBulkEntry(sessionId, FactoryEnumType.REQUEST, request);
			bFact.write(sessionId);
			bFact.close(sessionId);
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
	public static boolean deleteAccessRequestsByStatus(UserType owner, ApprovalResponseEnumType approvalType, long organizationId) throws FactoryException, ArgumentException {
		RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
		rFact.deleteByStatus(owner, approvalType, organizationId);
		return true;
	}
	public static boolean deleteAccessRequestsByRequestor(NameIdType requestor, ApprovalResponseEnumType approvalType) throws FactoryException, ArgumentException{
		RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
	    rFact.deleteByRequestor(requestor, approvalType, requestor.getOrganizationId());
	    return true;
	}	
	public static boolean deleteAccessRequestsByOwner(UserType owner) throws FactoryException, ArgumentException{
		RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
	    rFact.deleteByOwner(owner);
	    return true;
	}
	
	public static List<ApproverType> findApprovers(NameIdType object){
		List<ApproverType> approvers = new ArrayList<>();
		ApproverFactory aFact;
		try {
			aFact = Factories.getFactory(FactoryEnumType.APPROVER);
			approvers = aFact.getApproversForType(null,object,0, ApprovalEnumType.ACCESS);
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		
		return approvers;
	}
	
	/// The order of precedence in looking up an owner related to access requests is:
	/// 1) Look for an attribute on the object, and on the object's relative group parent
	///    a) This may need to be refactored, particularly for permissions and roles, but was done this way because the relative group parent reflects the entry for an application/system while the role and path structures are in an adjacent tree
    /// 2) Look for an approver entry for the object, which otherwise could have been related in #1 as an object attribute
	/// 3) Look for the actual object owner and use the corresponding person entry
	public static ApproverType findOwner(NameIdType object) {
		ApproverType owner = null;
		PersonType defOwner = null;
		try {
			ApproverFactory aFact = Factories.getFactory(FactoryEnumType.APPROVER);
			INameIdFactory iFact = Factories.getFactory(FactoryEnumType.valueOf(object.getNameType().toString()));
			iFact.populate(object);
			Factories.getAttributeFactory().populateAttributes(object);
			
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
					logger.debug("Looking in " + groupPath + " for " + object.getNameType());
				}
				
			}
			/// Lookup by an attribute designating on owner (as an approver entry) at the object level
			/// 
			if(ownerId != null) {
				logger.info("Locating owner based on owner attribute");
				owner = aFact.getById(Long.parseLong(ownerId),object.getOrganizationId());
			}
			/// Lookup the owner approvers for this object
			///
			if(owner == null) {
				logger.info("Looking for owner based on approver entry");
				List<ApproverType> ownerApprovers = aFact.getApproversForType(null,object,0, ApprovalEnumType.OWNER);
				if(ownerApprovers.size() > 0) owner = ownerApprovers.get(0);
			}
			/// Lookup the owner based on the default AccountManager object ownership schema
			/// Cast-up to the person
			/// This method for ownership evaluation will default to approver level 1
			///
			if(owner == null) {
				INameIdFactory iUFact = Factories.getFactory(FactoryEnumType.USER);
				UserType ownerUser = iUFact.getById(object.getOwnerId(), object.getOrganizationId());
				
				if(ownerUser != null) {
					defOwner = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getSystemPersonByUser(ownerUser);
					if(defOwner != null) {
						logger.info("Using object owner's person for ownership approvals.");
						
						owner = new ApproverType();
						owner.setNameType(NameEnumType.APPROVER);
						owner.setEntitlementId(object.getId());
						owner.setEntitlementType(ApproverEnumType.valueOf(object.getNameType().toString()));
						owner.setApproverType(ApproverEnumType.PERSON);
						owner.setApprovalType(ApprovalEnumType.OWNER);
						owner.setApproverId(defOwner.getId());
						owner.setApproverLevel(1);
						owner.setOrganizationId(object.getOrganizationId());
					}
				}
			}
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		return owner;
		
	}
	
}
