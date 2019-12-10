package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cote.accountmanager.data.factory.ApproverFactory;
import org.cote.accountmanager.data.factory.AttributeFactory;
import org.cote.accountmanager.data.factory.BulkFactory;
import org.cote.accountmanager.data.factory.ControlFactory;
import org.cote.accountmanager.data.factory.MessageFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.RequestFactory;
import org.cote.accountmanager.data.security.RequestService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.PolicyService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccessRequestType;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.ApplicationPermissionType;
import org.cote.accountmanager.objects.ApprovalEnumType;
import org.cote.accountmanager.objects.ApprovalResponseEnumType;
import org.cote.accountmanager.objects.ApprovalType;
import org.cote.accountmanager.objects.ApproverType;
import org.cote.accountmanager.objects.ControlActionEnumType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.objects.types.SpoolStatusEnumType;
import org.junit.Test;

public class TestAccessApproval extends BaseDataAccessTest {

	private boolean cleanupApprovers = true;
	private boolean cleanupRemovedRequests = false;
	/*
	 * 
	 * Request + Approval is structured as follows
	 *    Request [MessageSpoolType]
	 *       Some
	 *    Approval is 
	 * 
	 * NEW
	 * 
	 * I request [ACTION|ACCESS] to [RESOURCE] {on behalf of} == AccessRequestType
	 * [ACTION|ACCESS] to [RESOURCE] requires [ROLE|GROUP|PERSON] approval at level # == ApproverType
	 * [PERSON] [GRANT|DENY|PEND] [REQUEST] == ApprovalType
	 * [CONTROL] dictates [RESOURCE] requires [POLICY] = ControlType
	 * [POLICY] directs completion == PolicyRequestType (note: currently synchronous, so the process will have to fork, or define a pre-processor to detect and satisfy pending requirements)
	 * 
	 * 
	 * 
	 *
	 * The structure of the approval policy is as follows:
	 * Policy
	 *    Rule
	 *       Pattern [PatternType==APPROVAL,OPERATION=AccessApprovalOperation] - Evaluates Approval of SourceFact and MatchFact
	 *          --- OWNER
	 *          SourceFact[FactType==PARAMETER]==Entitlement
	 *          (OWNER) MatchFact[FactType==OPERATION,FactoryType==OPERATION,OPERATION=LookupOwnerOperation] - Evaluates to a ROLE, GROUP, or PERSON
	 *          
	 *          --- SPECIFIED
	 *         SourceFact[FactType==PARAMETER]==Entitlement
	 *         (OWNER) MatchFact[FactType==OPERATION,FactoryType==UNKNOWN, OPERATION=LookupApproverOperation, DATA={level}] - Evaluates to a ROLE, GROUP, or PERSON
	 *
	 * Request Processing Order
	 *    1) User submits an access request
	 * 
	 * ORIGINAL
	 * 
	 * Approvals work as follows:
	 * 1: Entitlements (as ApproverType) is Approver [PERSON, GROUP, ROLE] requests access [ROLE, PERMISSION, GROUP] to Resource [Implied (null) | Application (DirectoryGroupType) | ObjectType ] at a specific level
     * 2: Approver [PERSON, GROUP, ROLE] must evaluate to an array of PersonType[]
     * 3: Approver is a foreign key composite of the approver, access, and resource
     * 3: Request for Approval is of MessageSpoolType [via FirstContactMessageService] to Resource
     * 4: PolicyType/PatternEnumType dictates the approvals needed
     *    a) this is dynamic, in that it's all policies with patterns that include the entitlement
     *    b) this is static, in that a control directly requires an approval for a resource or entitlement
     *    c) where (b) is a condition of policy (a)
     *    d) and where policy eval is mandatory for (a), else the request is permitted/denied by default
     *    
     * So that, to request access:
     * User A submits Request [Spool] for Entitlement A
     * ApprovalMaintenanceThread scans for pending approval requests and matches to any related policies
     * ApprovalService (or FirstContactService) processes Approval by Level, with fallback delegates at each level if the approver size is greater than 1.
     * ApprovalRequest is entered as a spool entry related to the request
     * Approvers receive notification and acknowledge (approve | deny | pend) request
     * ApprovalMaintenanceThread continues processing, picking up from last policy processing point
     *    (Note: There is currently no state on policy processing, so this will either be a refactor or separate policy evaluations or same policy evaluation with the prior approval status being evaluated each time)
	 */

	
	@Test
	public void TestCreateEmptyRequest() {
		boolean error = false;
		try {
			cleanupRequestsForUser(testUser);
			AccessRequestType req = newAccessRequest(testUser, ActionEnumType.REQUEST, null, null, null, null, 0L);
			assertNotNull("Request is null",req);
			List<AccessRequestType> reqs = getAccessRequests(testUser, ActionEnumType.REQUEST, null, null, null, null, 0L);
			assertTrue("Request count should be one since all requests for this user were marked for removal",reqs.size() == 1);
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			logger.error(e);
			error = true;
		}
		assertFalse("An error occurred", error);
	}
	
	
	@Test
	public void TestCreateRequestForGroup() {
		boolean error = false;
		AccessRequestType request = null;
		DirectoryGroupType app1 = getApplication("Application 1");
		AccountGroupType group1 = getApplicationGroup("Group #1", GroupEnumType.ACCOUNT, app1);

		try {
			cleanupRequestsForUser(testUser);
			logger.info("Test creating an group access request");
			RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
			request = rFact.newAccessRequest(testUser, ActionEnumType.REQUEST, null, null, null, group1, 0L);
			assertNotNull("Request is null", request);
			assertTrue("Failed to add request",rFact.add(request));
			
			List<AccessRequestType> reqs = rFact.getAccessRequestsForType(testUser, null, null, group1, ApprovalResponseEnumType.REQUEST,0L, testUser.getOrganizationId());
			logger.info("Found " + reqs.size() + " requests for " + testUser.getUrn() + " to obtain " + group1.getUrn() + " access");
			assertTrue("Request count should be one since all requests for this user were marked for removal",reqs.size() == 1);

		} catch (FactoryException | ArgumentException | DataAccessException e) {
			error = true;
			e.printStackTrace();
			logger.error(e);
		}
		assertFalse("Test threw an error", error);
		assertNotNull("Request is null", request);
	}
	
	@Test
	public void TestCreateRequestBasket() {
		boolean error = false;
		AccessRequestType request = null;
		AccessRequestType childRequest = null;
		DirectoryGroupType app1 = getApplication("Application 1");
		AccountGroupType group1 = getApplicationGroup("Group #1", GroupEnumType.ACCOUNT, app1);
		ApplicationPermissionType per1 = getApplicationPermission("Permission #1",PermissionEnumType.APPLICATION,app1);
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		BulkFactory bFact = BulkFactories.getBulkFactory();
		try {
			cleanupRequestsForUser(testUser);

			logger.info("Test creating a bulk request basket");
			RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
			request = rFact.newAccessRequest(testUser, ActionEnumType.REQUEST, null, null, null, null, 0L);
			bFact.createBulkEntry(sessionId, FactoryEnumType.REQUEST, request);

			/// request now has a temporary id that can be used for a parent value
			assertNotNull("Request is null", request);

			childRequest = rFact.newAccessRequest(testUser, ActionEnumType.REQUEST, null, null, null, group1, request.getId());
			bFact.createBulkEntry(sessionId, FactoryEnumType.REQUEST, childRequest);
	
			childRequest = rFact.newAccessRequest(testUser, ActionEnumType.REQUEST, null, null, app1, per1, request.getId());
			bFact.createBulkEntry(sessionId, FactoryEnumType.REQUEST, childRequest);
			
			bFact.write(sessionId);
			bFact.close(sessionId);
			
			List<AccessRequestType> reqs = rFact.getAccessRequestsForType(testUser, null, null, null, ApprovalResponseEnumType.REQUEST,0L, testUser.getOrganizationId());
			logger.info("Found " + reqs.size() + " requests for " + testUser.getUrn() + " to obtain " + group1.getUrn() + " access");
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			error = true;
			e.printStackTrace();
			logger.error(e);
		}

		assertFalse("Test threw an error", error);
		assertNotNull("Request is null", request);
	}
	
	
	/*
	 * To test access requests, the following conditions must be true:
	 * - a valid user to make the request
	 * - a valid user to approve the request
	 * - an entitlement
	 * - an approver defined for the entitlement, or parent application
	 * - at least on enabled policy (eg: Owner Approval Policy)
	 * - a control that correlates the entitlement to the policy
	 */
	
	@Test
	public void TestRequestAccess() {
		
		DirectoryGroupType app1 = getApplication("Application 1");
		Factories.getAttributeFactory().populateAttributes(app1);
		PersonRoleType roleP = getApplicationRole("Role #1",RoleEnumType.PERSON,app1);
		AccountRoleType roleP2 = getApplicationRole("Role #2",RoleEnumType.ACCOUNT,app1);
		ApplicationPermissionType per1 = getApplicationPermission("Permission #1",PermissionEnumType.APPLICATION,app1);
		ApplicationPermissionType per2 = getApplicationPermission("Permission #2",PermissionEnumType.APPLICATION,app1);
		AccountGroupType group1 = getApplicationGroup("Group #1", GroupEnumType.ACCOUNT, app1);
		AccountGroupType group2 = getApplicationGroup("Group #2", GroupEnumType.ACCOUNT, app1);
		assertNotNull("Group 1 is null", group1);

		boolean error = false;
		
		try {
			MessageFactory mFact = Factories.getFactory(FactoryEnumType.MESSAGE);
			ApproverFactory aFact = ((ApproverFactory)Factories.getFactory(FactoryEnumType.APPROVER));
			AttributeFactory atFact = Factories.getAttributeFactory();
			PersonType testPerson2 = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getPersonByUser(testUser2);
			assertNotNull("Test Person 2 is null", testPerson2);
			if(!RoleService.getIsMemberInRole(testPerson2, roleP)) assertTrue("Unable to add person to role",RoleService.addPersonToRole(testPerson2, roleP));
			
			if(cleanupApprovers) {
				logger.info("Cleaning up pre-test conditions");
				aFact.deleteApproversForType(null, group1);
				aFact.deleteApproversForType(null, per1);
			}
			
			List<ApproverType> aLo = getCreateApprovers(testUser, testUser, null, group1, 1, ApprovalEnumType.OWNER);
			if(atFact.getAttributeByName(app1, RequestService.ATTRIBUTE_NAME_OWNER) == null) {
				atFact.newAttribute(app1, RequestService.ATTRIBUTE_NAME_OWNER, Long.toString(testUser.getId()));
				Factories.getAttributeFactory().addAttributes(app1);
			}

			List<ApproverType> aL = getCreateApprovers(testUser, testUser, null, group1, 1,ApprovalEnumType.ACCESS);
			List<ApproverType> aL2 = getCreateApprovers(testUser, testUser, app1, per1, 1,ApprovalEnumType.ACCESS);
			List<ApproverType> aL3 = getCreateApprovers(testUser, roleP, null, group1, 2,ApprovalEnumType.ACCESS);
			
			ApproverType apro = aLo.get(0);
			ApproverType apr1 = aL.get(0);
			ApproverType apr2 = aL2.get(0);
			ApproverType apr3 = aL3.get(0);
			assertNotNull("Approval object is null", apro);
			assertNotNull("Approval object is null", apr1);
			assertNotNull("Approval object is null", apr2);
			assertNotNull("Approval object is null", apr3);
			
			PolicyType ownerPolicy = PolicyService.getOwnerApprovalPolicy(testUser.getOrganizationId());
			//logger.info(JSONUtil.exportObject(ownerPolicy));
			PolicyType principalPolicy = PolicyService.getPrincipalApprovalPolicy(testUser.getOrganizationId());
			//logger.info(JSONUtil.exportObject(principalPolicy));
			/// Attach a control to the ownership policy on the application parent:
			List<ControlType> ctls = getCreateAccessApproverControls(testUser, ownerPolicy, app1);
			
			assertTrue("Expected at least one control", ctls.size() > 0);
			
			assertTrue("Test user should be able to view the policy",AuthorizationService.canView(testUser, ownerPolicy));
			assertFalse("Test user should not be able to modify the policy", AuthorizationService.canChange(testUser, ownerPolicy));
			
			/// Attach a control to the ownership policy on the application parent:
			List<ControlType> ctls2 = getCreateAccessApproverControls(testUser, principalPolicy, app1);
			
			assertTrue("Entitlement " + group1.getName() + " is not requestable", RequestService.isRequestable(group1));
			
			cleanupRequestsForUser(testUser);
			List<AccessRequestType> reqs = RequestService.listOpenAccessRequests(testUser);
			assertTrue("Expected zero outstanding requests for user " + testUser.getUrn(), reqs.size() == 0);
			
			RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
			AccessRequestType request = rFact.newAccessRequest(testUser, ActionEnumType.REQUEST, null, null, null, group1, 0L);
			assertNotNull("Request is null", request);
			assertTrue("Failed to add request",rFact.add(request));
			
			reqs = rFact.getAccessRequestsForType(testUser, null, null, group1, ApprovalResponseEnumType.REQUEST,0L, testUser.getOrganizationId());
			assertTrue("Expected one outstanding request for user " + testUser.getUrn(), reqs.size() == 1);

			
			/// Each access request will result in one or more policies
			/// From an end user and approval perspective, these can be requested as a bucket/basket, and approval can be handled at that level as well (if desired)
			/// Behind the scenes, it's all individual records to track the request, the approval, etc.
			/// Therefore, in evaluating a request for approval, it's necessary to:
			/// 1) Get one or more pending request
			/// 2) For each request, obtain all of the policies for that request
			/// 3) For each policy for each request, evaluate the policy to obtain the approvers
			/// 4) Generate the approval entries per the policy
			/// 
			logger.info("Evaluating outstanding access requests: " + reqs.size());
			for(AccessRequestType arq : reqs) {
				List<PolicyType> reqPols= RequestService.getRequestPolicies(arq);
				assertTrue("Expected at least one policy", reqPols.size() > 0);
				logger.info("Applicable policy controls for request: " + reqPols.size());

				/// evaluateRequestPolicies will translate the corresponding approver for the request into a pending approval for the request
				///
				List<ApprovalType> approvals = RequestService.evaluateRequestPolicies(arq, reqPols);
				assertTrue("Expected at least one approval", approvals.size() > 0);
				logger.info("Obtained " + approvals.size());
				
				/// Do it again to make sure entries aren't double booked
				approvals = RequestService.evaluateRequestPolicies(arq, reqPols);
				logger.info("Obtained 2x " + approvals.size());
				
				/// Process request for approvals - this must be called AFTER evaluateRequestPolicies
				/// evaluateRequestPolicies can be called as many times as needed to add new entries mid-request if desired
				int msgCount = RequestService.processPendingRequestPolicies(arq, true, false);
				
				logger.info("Recorded " + msgCount + " internal messages");
				
				/// Process request again to test break on level, and check to throttle message flooding
				///
				msgCount = RequestService.processPendingRequestPolicies(arq, true, false);
				logger.info("Re-Recorded " + msgCount + " internal messages");
				/// Drop a note in the queue that the user has a pending approval request

				/// Check all outstanding messages for request
				List<MessageSpoolType> reqMsg = RequestService.getApprovalRequestMessages(testUser, SpoolStatusEnumType.UNKNOWN, arq.getId(), 0L, NameEnumType.UNKNOWN, 0L, testUser.getOrganizationId());			
				/// Should be 2 messages for processing at level 1 only
				assertTrue("There should only be two messages after processing at first level for this request",reqMsg.size() == 2);
				
				/// Get messages for testUser
				/// Note: testUser has 2 messages for the same request, one to approve as an owner, and one to approve the access
				///
				logger.info("Request messages: " + reqMsg.size());
				RequestService.approve(testUser,reqMsg.get(0),true);
				RequestService.approve(testUser,reqMsg.get(1),true);
				msgCount = RequestService.processPendingRequestPolicies(arq, true, false);
				reqMsg = RequestService.getApprovalRequestMessages(testUser2, SpoolStatusEnumType.UNKNOWN, arq.getId(), 0L, NameEnumType.UNKNOWN, 0L, testUser.getOrganizationId());			
				logger.info("Received " + reqMsg.size() + " messages");
				/// Should be 1 messages for processing at level 2 only
				assertTrue("There should only be two messages after processing at first level for this request",reqMsg.size() == 1);
				RequestService.approve(testUser2,reqMsg.get(0),true);
				
				/// Now, try to resolve the request
				ApprovalResponseEnumType res = RequestService.resolveRequest(arq);
				
				assertTrue("Request should be marked for approval", res == ApprovalResponseEnumType.APPROVE);
			}
			
		} catch (ArgumentException | FactoryException | DataAccessException | NullPointerException e) {
			logger.error(e);
			e.printStackTrace();
			error = true;
		}
		assertFalse("An error was encountered", error);
	}
	
	
	private void cleanupRequestsForUser(UserType user) throws FactoryException, ArgumentException, DataAccessException {
		List<AccessRequestType> reqs = RequestService.listOpenAccessRequests(testUser);
		if(reqs.size() > 0) {
			logger.info("Cleaning up outstanding requests: " + reqs.size());
			String reqUpdate = BulkFactories.getBulkFactory().newBulkSession();
			for(AccessRequestType arq : reqs) {
				arq.setApprovalStatus(ApprovalResponseEnumType.REMOVE);
				BulkFactories.getBulkFactory().modifyBulkEntry(reqUpdate, FactoryEnumType.REQUEST, arq);
			}
			BulkFactories.getBulkFactory().write(reqUpdate);
			BulkFactories.getBulkFactory().close(reqUpdate);
			reqs = RequestService.listOpenAccessRequests(testUser);
		}
		if(cleanupRemovedRequests) {
			RequestService.deleteAccessRequestsByStatus(null, ApprovalResponseEnumType.REMOVE, user.getOrganizationId());
		}
	}
	private void deleteRequestsForUser(UserType user) throws FactoryException, ArgumentException {
		RequestService.deleteAccessRequestsByOwner(user);
	}
	private AccessRequestType newAccessRequest(UserType owner, ActionEnumType action, NameIdType requestor, NameIdType delegate, NameIdType targetObject, NameIdType entitlement, long parentId) throws FactoryException, ArgumentException {
		RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
		
		AccessRequestType req= rFact.newAccessRequest(owner, action, requestor, delegate, targetObject, entitlement, parentId);
		assertNotNull("Request is null", req);
		assertTrue("Failed to add request",rFact.add(req));
		List<AccessRequestType> reqs = rFact.getAccessRequestsForType(testUser, requestor, delegate, targetObject, ApprovalResponseEnumType.REQUEST,parentId, testUser.getOrganizationId());
		if(reqs.size() > 0) req = reqs.get(reqs.size()-1);
		else req = null;
		return req;
	}
	
	private List<AccessRequestType> getAccessRequests(UserType owner, ActionEnumType action, NameIdType requestor, NameIdType delegate, NameIdType targetObject, NameIdType entitlement, long parentId) throws FactoryException, ArgumentException {
		RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
		return rFact.getAccessRequestsForType(testUser, requestor, delegate, targetObject, ApprovalResponseEnumType.REQUEST,parentId, testUser.getOrganizationId());
	}
	
	
	private List<ApproverType> getCreateApprovers(UserType user, NameIdType approver, NameIdType object, NameIdType entitlement, int level, ApprovalEnumType approvalType) throws FactoryException, ArgumentException{
		ApproverFactory aFact = ((ApproverFactory)Factories.getFactory(FactoryEnumType.APPROVER));
		List<ApproverType> apprs = aFact.getApproversForType(object, entitlement, level,approvalType);
		if(apprs.size() == 0) {
			ApproverType apro = aFact.newApprover(testUser, object, entitlement, approver, approvalType, level);
			assertTrue("Failed to add approver 1", aFact.add(apro));
			apprs = aFact.getApproversForType(object, entitlement, level,approvalType);
		}
		assertTrue("Expected one or more approvers", apprs.size() > 0);
		return apprs;
	}
	
	private List<ControlType> getCreateAccessApproverControls(UserType owner, PolicyType policy, NameIdType object) throws FactoryException, ArgumentException{
		ControlActionEnumType action = ControlActionEnumType.ACCESS;
		ControlFactory cFact = ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL));
		List<ControlType> ctls = cFact.getControlsForType(object,ControlEnumType.POLICY, policy.getId(), action,true,false);
		if(ctls.size() == 0) {
			ControlType ownerControl = cFact.newControl(owner, object);
			ownerControl.setControlId(policy.getId());
			ownerControl.setControlType(ControlEnumType.POLICY);
			ownerControl.setControlAction(action);
			assertTrue("Failed to add owner control",cFact.add(ownerControl));
			ctls = cFact.getControlsForType(object,ControlEnumType.POLICY, policy.getId(),action,true,false);
		}
		return ctls;
	}

}
