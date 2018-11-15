package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cote.accountmanager.data.factory.ApproverFactory;
import org.cote.accountmanager.data.factory.ControlFactory;
import org.cote.accountmanager.data.factory.FactFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.PatternFactory;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.factory.RuleFactory;
import org.cote.accountmanager.data.security.RequestService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.ApplicationPermissionType;
import org.cote.accountmanager.objects.ApprovalEnumType;
import org.cote.accountmanager.objects.ApproverType;
import org.cote.accountmanager.objects.ControlActionEnumType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.util.JSONUtil;
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
	public void TestRequestAccess() {
		
		DirectoryGroupType app1 = getApplication("Application 1");
		PersonRoleType roleP = getApplicationRole("Role #1",RoleEnumType.PERSON,app1);
		AccountRoleType roleP2 = getApplicationRole("Role #2",RoleEnumType.ACCOUNT,app1);
		ApplicationPermissionType per1 = getApplicationPermission("Permission #1",PermissionEnumType.APPLICATION,app1);
		ApplicationPermissionType per2 = getApplicationPermission("Permission #2",PermissionEnumType.APPLICATION,app1);
		AccountGroupType group1 = getApplicationGroup("Group #1", GroupEnumType.ACCOUNT, app1);
		AccountGroupType group2 = getApplicationGroup("Group #2", GroupEnumType.ACCOUNT, app1);
		assertNotNull("Group 1 is null", group1);
		ApproverType apr1 = null;
		ApproverType apr2 = null;
		boolean error = false;
		
		try {
			ApproverFactory aFact = ((ApproverFactory)Factories.getFactory(FactoryEnumType.APPROVER));

			logger.info("Cleaning up pre-test conditions");
			//aFact.deleteApproversForType(null, group1);
			//aFact.deleteApproversForType(null, per1);
			
			List<ApproverType> aL = aFact.getApproversForType(null, group1, 1,ApprovalEnumType.ACCESS);
			List<ApproverType> aL2 = aFact.getApproversForType(app1, per1, 1,ApprovalEnumType.ACCESS);
			
			//logger.info("Group Approver size: " + aL.size());
			//logger.info("Perm Approver size: " + aL2.size());
			if(aL.size() == 1) apr1 = aL.get(0);
			else{
				apr1 = aFact.newApprover(testUser, null, group1, testUser, ApprovalEnumType.ACCESS, 1);
				assertTrue("Failed to add approver 1", aFact.add(apr1));
			}
			if(aL2.size() == 1) apr2 = aFact.newApprover(testUser, app1, per1, testUser, ApprovalEnumType.ACCESS, 1);
			else {
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
			//assertTrue("Entitlement " + per1.getName()  + " is not requestable", RequestService.isRequestable(per1));
			
		} catch (ArgumentException | FactoryException e) {
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
