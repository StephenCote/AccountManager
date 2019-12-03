package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cote.accountmanager.data.factory.ApproverFactory;
import org.cote.accountmanager.data.factory.ControlFactory;
import org.cote.accountmanager.data.factory.PatternFactory;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.factory.RequestFactory;
import org.cote.accountmanager.data.factory.RuleFactory;
import org.cote.accountmanager.data.security.RequestService;
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
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.junit.Test;

public class TestAccessApproval extends BaseDataAccessTest {

	
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
		AccessRequestType request = null;
		try {
			logger.info("Test creating an empty request to use for containment");
			RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
			request = rFact.newAccessRequest(testUser, ActionEnumType.REQUEST, null, null, null, null, 0L);
			assertNotNull("Request is null", request);
			//logger.info(JSONUtil.exportObject(request));
			assertTrue("Failed to add request",rFact.add(request));
			
			List<AccessRequestType> reqs = rFact.getAccessRequestsForType(testUser, null, null, null, ApprovalResponseEnumType.REQUEST,0L, testUser.getOrganizationId());
			//logger.info("Found " + reqs.size() + " requests for " + testUser.getUrn());
		} catch (FactoryException | ArgumentException e) {
			error = true;
			e.printStackTrace();
			logger.error(e);
		}
		assertFalse("Test threw an error", error);
		assertNotNull("Request is null", request);
	}
	
	@Test
	public void TestCreateRequestForGroup() {
		boolean error = false;
		AccessRequestType request = null;
		DirectoryGroupType app1 = getApplication("Application 1");
		AccountGroupType group1 = getApplicationGroup("Group #1", GroupEnumType.ACCOUNT, app1);

		try {
			logger.info("Test creating an group access request");
			RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
			request = rFact.newAccessRequest(testUser, ActionEnumType.REQUEST, null, null, null, group1, 0L);
			assertNotNull("Request is null", request);
			//logger.info(JSONUtil.exportObject(request));
			assertTrue("Failed to add request",rFact.add(request));
			
			List<AccessRequestType> reqs = rFact.getAccessRequestsForType(testUser, null, null, group1, ApprovalResponseEnumType.REQUEST,0L, testUser.getOrganizationId());
			//logger.info("Found " + reqs.size() + " requests for " + testUser.getUrn() + " to obtain " + group1.getUrn() + " access");
		} catch (FactoryException | ArgumentException e) {
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
		try {
			logger.info("Test creating a bulk request basket");
			RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
			request = rFact.newAccessRequest(testUser, ActionEnumType.REQUEST, null, null, null, null, 0L);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.REQUEST, request);
			/// request now has a temporary id that can be used for a parent value
			
			assertNotNull("Request is null", request);
			//logger.info("Bulk object:\n" + JSONUtil.exportObject(request));

			childRequest = rFact.newAccessRequest(testUser, ActionEnumType.REQUEST, null, null, null, group1, request.getId());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.REQUEST, childRequest);
	
			childRequest = rFact.newAccessRequest(testUser, ActionEnumType.REQUEST, null, null, app1, per1, request.getId());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.REQUEST, childRequest);
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			//logger.info(JSONUtil.exportObject(request));
			//assertTrue("Failed to add request",rFact.add(request));
			
			//List<AccessRequestType> reqs = rFact.getAccessRequestsForType(testUser, null, null, group1, 0L, testUser.getOrganizationId());
			//logger.info("Found " + reqs.size() + " requests for " + testUser.getUrn() + " to obtain " + group1.getUrn() + " access");
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
		ApproverType apro = null;
		ApproverType apr1 = null;
		ApproverType apr2 = null;
		boolean error = false;
		
		try {
			ApproverFactory aFact = ((ApproverFactory)Factories.getFactory(FactoryEnumType.APPROVER));

			//logger.info("Cleaning up pre-test conditions");
			//aFact.deleteApproversForType(null, group1);
			//aFact.deleteApproversForType(null, per1);
			
			List<ApproverType> aLo = aFact.getApproversForType(null, group1, 1,ApprovalEnumType.OWNER);
			List<ApproverType> aL = aFact.getApproversForType(null, group1, 1,ApprovalEnumType.ACCESS);
			List<ApproverType> aL2 = aFact.getApproversForType(app1, per1, 1,ApprovalEnumType.ACCESS);
			
			//logger.info("Group Approver size: " + aL.size());
			//logger.info("Perm Approver size: " + aL2.size());
			if(aLo.size() == 1) apro = aLo.get(0);
			else{
				apro = aFact.newApprover(testUser, null, group1, testUser, ApprovalEnumType.OWNER, 1);
				assertTrue("Failed to add approver 1", aFact.add(apro));
				apro = aFact.getApproversForType(null, group1, 1,ApprovalEnumType.OWNER).get(0);

				Factories.getAttributeFactory().newAttribute(app1, RequestService.ATTRIBUTE_NAME_OWNER, Long.toString(apro.getId()));
				Factories.getAttributeFactory().addAttributes(app1);
			}
			if(aL.size() == 1) apr1 = aL.get(0);
			else{
				apr1 = aFact.newApprover(testUser, null, group1, testUser, ApprovalEnumType.ACCESS, 1);
				assertTrue("Failed to add approver 1", aFact.add(apr1));
			}
			if(aL2.size() == 1) apr2 = aL2.get(0);
			else {
				apr2 = aFact.newApprover(testUser, app1, per1, testUser, ApprovalEnumType.ACCESS, 1);
				assertTrue("Failed to add approver 2", aFact.add(apr2));
			}
			assertNotNull("Approval object is null", apr1);
			assertNotNull("Approval object is null", apr2);
			//logger.info(JSONUtil.exportObject(apr1));
			
			PolicyType ownerPolicy = getOwnerApprovalPolicy();
			//logger.info(JSONUtil.exportObject(ownerPolicy));
			
			/// Attach a control to the ownership policy on the application parent:
			List<ControlType> ctls = ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).getControlsForType(app1,ControlEnumType.POLICY, ControlActionEnumType.ACCESS,true,false);
			if(ctls.size() == 0) {
				ControlType ownerControl = ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).newControl(testUser, app1);
				ownerControl.setControlId(ownerPolicy.getId());
				ownerControl.setControlType(ControlEnumType.POLICY);
				ownerControl.setControlAction(ControlActionEnumType.ACCESS);
				((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).add(ownerControl);
			}
			assertTrue("Entitlement " + group1.getName() + " is not requestable", RequestService.isRequestable(group1));
			
			
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
			
		
			//logger.info("Open requests (there should be zero): " + reqs.size());
			
			assertTrue("Expected zero outstanding requests for user " + testUser.getUrn(), reqs.size() == 0);
			
			RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
			AccessRequestType request = rFact.newAccessRequest(testUser, ActionEnumType.REQUEST, null, null, null, group1, 0L);
			assertNotNull("Request is null", request);
			assertTrue("Failed to add request",rFact.add(request));
			
			reqs = rFact.getAccessRequestsForType(testUser, null, null, group1, ApprovalResponseEnumType.REQUEST,0L, testUser.getOrganizationId());
			assertTrue("Expected one outstanding request for user " + testUser.getUrn(), reqs.size() == 1);

			
			/// Each access request will result in one or more policies
			/// From an end user and approval perspective, these can be requested as a bucket/basket, and approval can be handled at that level as well (if desired)
			/// Behind the scenese, it's all individual records to track the request, the approval, etc.
			/// Therefore, in evaluating a request for approval, it's necessary to:
			/// 1) Get one or more pending request
			/// 2) For each request, obtain all of the policies for that request
			/// 3) For each policy for each request, evaluate the policy to obtain the approvers
			/// 4) Generate the approval entries per the policy
			/// 
			for(AccessRequestType arq : reqs) {
				//List<PolicyType> reqPols= RequestService.getRequestPolicies(group1, true);
				List<PolicyType> reqPols= RequestService.getRequestPolicies(arq);
				assertTrue("Expected at least one policy", reqPols.size() > 0);
				logger.info("Applicable policy controls for request: " + reqPols.size());
				List<ApprovalType> approvals = RequestService.evaluateRequestPolicies(arq, reqPols);
				assertTrue("Expected at least one approval", approvals.size() > 0);

			}
			//prt.setUrn(pol.getUrn());
			//prt.setOrganizationPath(((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationPath(pol.getOrganizationId()));

			
			//assertTrue("Entitlement " + per1.getName()  + " is not requestable", RequestService.isRequestable(per1));
			
		} catch (ArgumentException | FactoryException | DataAccessException e) {
			logger.error(e);
			e.printStackTrace();
			error = true;
		}
		assertFalse("An error was encountered", error);
	}
	
	
	
	
	private PolicyType getOwnerApprovalPolicy(){
		DirectoryGroupType pdir = null;
		DirectoryGroupType rdir = null;
		DirectoryGroupType podir = null;
		DirectoryGroupType odir = null;
		DirectoryGroupType fdir = null;
		PolicyType pol = null;
		RuleType useRule = null;
		PatternType pat = null;
		
		String pname = "Access Approval Policy";
		String rname = "Access Approval Rule";
		String patName = "Owner Approval Pattern";
		String clsAccAprClass = "org.cote.accountmanager.data.operation.AccessApprovalOperation";
		String clsLookupOwnerClass = "org.cote.accountmanager.data.operation.LookupOwnerOperation";
		try {
			rdir = getCreatePath(testUser, "~/Rules");
			pdir = getCreatePath(testUser, "~/Patterns");
			fdir = getCreatePath(testUser, "~/Facts");
			podir = getCreatePath(testUser, "~/Policies");
			odir = getCreatePath(testUser, "~/Operations");
			
			OperationType rgOp = getCreateOperation(testUser,"Access Approval Operation",clsAccAprClass,odir);
			OperationType rgOp2 = getCreateOperation(testUser,"Lookup Owner Operation",clsLookupOwnerClass,odir);
			
			FactType approveEntitlementParamFact = getCreateEntitlementParamFact(testUser,"Entitlement Parameter",fdir);
			FactType ownerEntitlementFact = getCreateOperationFact(testUser,"Entitlement Owner",rgOp2.getUrn(),fdir);

			pol = getCreatePolicy(testUser,pname,podir);
			pol.setEnabled(true);
			useRule = getCreateRule(testUser,rname,rdir);

			useRule.setRuleType(RuleEnumType.PERMIT);

			pat = getCreatePattern(testUser,patName,approveEntitlementParamFact.getUrn(),ownerEntitlementFact.getUrn(),pdir);
			pat.setPatternType(PatternEnumType.APPROVAL);
			pat.setPatternType(PatternEnumType.OPERATION);
			pat.setOperationUrn(rgOp.getUrn());
			((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).update(pat);
			useRule.getPatterns().clear();
			useRule.getPatterns().add(pat);
			((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).update(useRule);
			pol.getRules().clear();
			pol.getRules().add(useRule);
			((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).update(pol);
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		} 
		return pol;
	}
	
}
