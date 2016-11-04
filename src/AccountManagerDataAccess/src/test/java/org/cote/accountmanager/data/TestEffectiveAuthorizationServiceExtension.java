package org.cote.accountmanager.data;


/*
 * In order for this unit test to work, the authorization extension for POLICY must be configured with AccountManagerConsole
 */
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationEnumType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.util.JSONUtil;
import org.junit.Test;

public class TestEffectiveAuthorizationServiceExtension extends BaseDataAccessTest {
	
	
	/*
	@Test
	public void TestPolicyBasedAuthorization(){
		// FactoryDefaults.createPermissionsForAuthorizationFactories(testUser.getOrganizationId());

		DirectoryGroupType odir = null;
		DirectoryGroupType pdir = null;
		DirectoryGroupType rdir = null;
		DirectoryGroupType padir = null;
		DirectoryGroupType fdir = null;
		PolicyType policy = null;
		RuleType roleRule = null;
		RuleType roleRule1 = null;
		RuleType roleRule2 = null;
		
		RuleType roleRule1 = null;
		RuleType roleRule2 = null;
		RuleType roleRule3 = null;
		RuleType roleRule4 = null;
		
		
		PatternType pattern1 = null;
		PatternType pattern2 = null;
		PatternType pattern3 = null;
		PatternType pattern4 = null;
		
		OperationType oper1 = null;
		OperationType oper2 = null;
		FactType fact1 = null;
		FactType fact2 = null;
		FactType fact3 = null;
		FactType fact4 = null;
		FactType fact5 = null;
		RuleType objectRule = null;
		
		String policyName = "Can View Type";
		String operationName= "Compare Name Type";
		String operationName2= "Match System Role";
		String roleRuleName = "View Actor Is Role";
		String roleRule1Name = "Actor is Role";
		String roleRule2Name = "Object Authorized To Role";
		String pattern1Name = "Actor is Role";
		String pattern2Name = "Role Is TypeAdmin";
		String pattern3Name = "Role Is TypeReader";
		String fact1Name = "Actor Type Parameter";
		String fact2Name = "Role Name Type";
		String fact3Name = "Role TypeAdmin";
		String fact5Name = "Role TypeReader";
		String fact4Name = "Object Type Parameter";
		
		boolean cleanup = true;
		try {
			odir = Factories.getGroupFactory().getCreatePath(testUser, "~/Operations", testUser.getOrganizationId());
			pdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Policies", testUser.getOrganizationId());
			rdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Rules", testUser.getOrganizationId());
			padir = Factories.getGroupFactory().getCreatePath(testUser, "~/Patterns", testUser.getOrganizationId());
			fdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Facts", testUser.getOrganizationId());

			oper1 = Factories.getOperationFactory().getByNameInGroup(operationName, odir);
			if(oper1 == null){
				oper1 = Factories.getOperationFactory().newOperation(testUser, odir.getId());
				oper1.setOperationType(OperationEnumType.INTERNAL);
				oper1.setOperation("org.cote.accountmanager.data.operation.CompareNameTypeOperation");
				oper1.setName(operationName);
				Factories.getOperationFactory().addOperation(oper1);
				oper1 = Factories.getOperationFactory().getByNameInGroup(operationName, odir);
			}
			
			oper2 = Factories.getOperationFactory().getByNameInGroup(operationName2, odir);
			if(oper2 == null){
				oper2 = Factories.getOperationFactory().newOperation(testUser, odir.getId());
				oper2.setOperationType(OperationEnumType.INTERNAL);
				oper2.setOperation("org.cote.accountmanager.data.operation.MatchSystemRoleOperation");
				oper2.setName(operationName2);
				Factories.getOperationFactory().addOperation(oper2);
				oper2 = Factories.getOperationFactory().getByNameInGroup(operationName2, odir);
			}
			
			
			fact1 = Factories.getFactFactory().getByNameInGroup(fact1Name,fdir);
			if(cleanup && fact1 != null){
				Factories.getFactFactory().deleteFact(fact1);
				fact1 = null;
			}
			if(fact1 == null){
				fact1 = Factories.getFactFactory().newFact(testUser, fdir.getId());
				fact1.setFactType(FactEnumType.PARAMETER);
				fact1.setFactoryType(FactoryEnumType.UNKNOWN);
				fact1.setName(fact1Name);
				fact1.setDescription("ActorType");
				Factories.getFactFactory().addFact(fact1);
				fact1 = Factories.getFactFactory().getByNameInGroup(fact1Name,fdir);
			}
			
			fact2 = Factories.getFactFactory().getByNameInGroup(fact2Name,fdir);
			if(cleanup && fact2 != null){
				Factories.getFactFactory().deleteFact(fact2);
				fact2 = null;
			}
			if(fact2 == null){
				fact2 = Factories.getFactFactory().newFact(testUser, fdir.getId());
				fact2.setFactType(FactEnumType.ROLE);
				fact2.setFactoryType(FactoryEnumType.ROLE);
				fact2.setName(fact2Name);
				Factories.getFactFactory().addFact(fact2);
				fact2 = Factories.getFactFactory().getByNameInGroup(fact2Name,fdir);
			}
			fact3 = Factories.getFactFactory().getByNameInGroup(fact3Name,fdir);
			if(cleanup && fact3 != null){
				Factories.getFactFactory().deleteFact(fact3);
				fact3 = null;
			}
			if(fact3 == null){
				fact3 = Factories.getFactFactory().newFact(testUser, fdir.getId());
				fact3.setFactType(FactEnumType.ROLE);
				fact3.setFactoryType(FactoryEnumType.UNKNOWN);
				fact3.setSourceUrn("DataAdministrators");
				fact3.setName(fact3Name);
				Factories.getFactFactory().addFact(fact3);
				fact3 = Factories.getFactFactory().getByNameInGroup(fact3Name,fdir);
			}
			
			fact5 = Factories.getFactFactory().getByNameInGroup(fact5Name,fdir);
			if(cleanup && fact5 != null){
				Factories.getFactFactory().deleteFact(fact5);
				fact5 = null;
			}
			if(fact5 == null){
				fact5 = Factories.getFactFactory().newFact(testUser, fdir.getId());
				fact5.setFactType(FactEnumType.ROLE);
				fact5.setFactoryType(FactoryEnumType.UNKNOWN);
				fact5.setSourceUrn("DataReaders");
				fact5.setName(fact5Name);
				Factories.getFactFactory().addFact(fact5);
				fact5 = Factories.getFactFactory().getByNameInGroup(fact5Name,fdir);
			}
			
			fact4 = Factories.getFactFactory().getByNameInGroup(fact4Name,fdir);
			if(cleanup && fact4 != null){
				Factories.getFactFactory().deleteFact(fact4);
				fact4 = null;
			}
			if(fact4 == null){
				fact4 = Factories.getFactFactory().newFact(testUser, fdir.getId());
				fact4.setFactType(FactEnumType.PARAMETER);
				fact4.setFactoryType(FactoryEnumType.UNKNOWN);
				fact4.setName(fact4Name);
				fact4.setDescription("ObjectType");
				Factories.getFactFactory().addFact(fact4);
				fact4 = Factories.getFactFactory().getByNameInGroup(fact4Name,fdir);
			}
			pattern1 = Factories.getPatternFactory().getByNameInGroup(pattern1Name, padir);
			if(cleanup && pattern1 != null){
				Factories.getPatternFactory().deletePattern(pattern1);
				pattern1 = null;
			}
			if(pattern1 == null){
				pattern1 = Factories.getPatternFactory().newPattern(testUser, padir.getId());
				pattern1.setName(pattern1Name);
				pattern1.setPatternType(PatternEnumType.OPERATION);
				pattern1.setOperationUrn(oper1.getUrn());
				pattern1.setComparator(ComparatorEnumType.EQUALS);
				pattern1.setFactUrn(fact1.getUrn());
				pattern1.setMatchUrn(fact2.getUrn());
				pattern1.setLogicalOrder(1);
				Factories.getPatternFactory().addPattern(pattern1);
				pattern1 = Factories.getPatternFactory().getByNameInGroup(pattern1Name, padir);	

			}
			
			pattern2 = Factories.getPatternFactory().getByNameInGroup(pattern2Name, padir);
			if(cleanup && pattern2 != null){
				Factories.getPatternFactory().deletePattern(pattern2);
				pattern2 = null;
			}
			if(pattern2 == null){
				pattern2 = Factories.getPatternFactory().newPattern(testUser, padir.getId());
				pattern2.setName(pattern2Name);
				pattern2.setPatternType(PatternEnumType.OPERATION);
				pattern2.setOperationUrn(oper2.getUrn());
				pattern2.setComparator(ComparatorEnumType.EQUALS);
				pattern2.setFactUrn(fact1.getUrn());
				pattern2.setMatchUrn(fact3.getUrn());
				pattern2.setLogicalOrder(2);
				Factories.getPatternFactory().addPattern(pattern2);
				pattern2 = Factories.getPatternFactory().getByNameInGroup(pattern2Name, padir);	

			}
			
			pattern3 = Factories.getPatternFactory().getByNameInGroup(pattern3Name, padir);
			if(cleanup && pattern3 != null){
				Factories.getPatternFactory().deletePattern(pattern3);
				pattern3 = null;
			}
			if(pattern3 == null){
				pattern3 = Factories.getPatternFactory().newPattern(testUser, padir.getId());
				pattern3.setName(pattern3Name);
				pattern3.setPatternType(PatternEnumType.OPERATION);
				pattern3.setOperationUrn(oper2.getUrn());
				pattern3.setComparator(ComparatorEnumType.EQUALS);
				pattern3.setFactUrn(fact1.getUrn());
				pattern3.setMatchUrn(fact5.getUrn());
				pattern3.setLogicalOrder(3);
				Factories.getPatternFactory().addPattern(pattern3);
				pattern3 = Factories.getPatternFactory().getByNameInGroup(pattern3Name, padir);	

			}
			
			roleRule1 = Factories.getRuleFactory().getByNameInGroup(roleRule1Name, rdir);
			if(cleanup && roleRule1 != null){
				Factories.getRuleFactory().deleteRule(roleRule1);
				roleRule1 = null;
			}
			if(roleRule1 == null){
				roleRule1 = Factories.getRuleFactory().newRule(testUser, rdir.getId());
				roleRule1.getPatterns().add(pattern1);
				roleRule1.setCondition(ConditionEnumType.ANY);
				roleRule1.setName(roleRule1Name);
				roleRule1.setLogicalOrder(1);
				roleRule1.setRuleType(RuleEnumType.PERMIT);
				Factories.getRuleFactory().addRule(roleRule1);
				roleRule1 = Factories.getRuleFactory().getByNameInGroup(roleRule1Name, rdir);	
			}
			Factories.getRuleFactory().populate(roleRule1);

			roleRule2 = Factories.getRuleFactory().getByNameInGroup(roleRule2Name, rdir);
			if(cleanup && roleRule2 != null){
				Factories.getRuleFactory().deleteRule(roleRule2);
				roleRule2 = null;
			}
			if(roleRule2 == null){
				roleRule2 = Factories.getRuleFactory().newRule(testUser, rdir.getId());
				roleRule2.getPatterns().add(pattern2);
				roleRule2.getPatterns().add(pattern3);
				roleRule2.setCondition(ConditionEnumType.ANY);
				roleRule2.setName(roleRule2Name);
				roleRule2.setLogicalOrder(2);
				roleRule2.setRuleType(RuleEnumType.PERMIT);
				Factories.getRuleFactory().addRule(roleRule2);
				roleRule2 = Factories.getRuleFactory().getByNameInGroup(roleRule2Name, rdir);	
			}
			Factories.getRuleFactory().populate(roleRule2);
			
			roleRule = Factories.getRuleFactory().getByNameInGroup(roleRuleName, rdir);
			if(cleanup && roleRule != null){
				Factories.getRuleFactory().deleteRule(roleRule);
				roleRule = null;
			}

			if(roleRule == null){
				roleRule = Factories.getRuleFactory().newRule(testUser, rdir.getId());
				roleRule.getRules().add(roleRule1);
				roleRule.getRules().add(roleRule2);
				roleRule.setRuleType(RuleEnumType.PERMIT);
				roleRule.setCondition(ConditionEnumType.ALL);
				roleRule.setName(roleRuleName);
				Factories.getRuleFactory().addRule(roleRule);
				roleRule = Factories.getRuleFactory().getByNameInGroup(roleRuleName, rdir);
			}
			Factories.getRuleFactory().populate(roleRule);
			logger.info("Rules: " + roleRule.getRules().size());
			
			policy = Factories.getPolicyFactory().getByNameInGroup(policyName, pdir);
			if(cleanup && policy != null){
				Factories.getPolicyFactory().deletePolicy(policy);
				policy = null;
			}

			if(policy == null){
				policy = Factories.getPolicyFactory().newPolicy(testUser, pdir.getId());
				policy.setCondition(ConditionEnumType.ANY);
				policy.setEnabled(true);
				policy.setName(policyName);
				policy.getRules().add(roleRule);
				//policy.getRules().add(roleRule1);
				Factories.getPolicyFactory().addPolicy(policy);
				policy = Factories.getPolicyFactory().getByNameInGroup(policyName, pdir);
			}
			Factories.getPolicyFactory().populate(policy);
			for(int i = 0; i < policy.getRules().size(); i++){
				Factories.getRuleFactory().populate(policy.getRules().get(i));
				for(int p = 0; p < policy.getRules().get(i).getRules().size(); p++){
					Factories.getRuleFactory().populate(policy.getRules().get(i).getRules().get(p));
					RuleType rule = policy.getRules().get(i).getRules().get(p);
					for(int q = 0; q < rule.getPatterns().size();q++){
						Factories.getPatternFactory().populate(rule.getPatterns().get(q));
					}
				}
			}
			
			//logger.info(JSONUtil.exportObject(fact1));
			//logger.info(JSONUtil.exportObject(fact2));
			logger.info(JSONUtil.exportObject(policy));
			
			//roleRule2 = Factories.getRuleFactory().newRule(testUser, rdir.getId());
			
			PolicyDefinitionType pdef = PolicyDefinitionUtil.generatePolicyDefinition(policy);
			PolicyRequestType prt = PolicyDefinitionUtil.generatePolicyRequest(pdef);
			logger.info(JSONUtil.exportObject(pdef));
			
			DirectoryGroupType app1 = getApplication("AuthZ Application #1");
			PersonType person1 = getApplicationPerson("Person #1",app1);
			PersonType person3 = getApplicationPerson("Person #3",app1);
			PersonRoleType perRole1 = getTestRole();
			RoleService.addPersonToRole(person3, perRole1);
			
			assertTrue("Expected at least one parameter",prt.getFacts().size() > 0);
			
			prt.getFacts().get(0).setFactReference(perRole1);
			PolicyResponseType prr = PolicyEvaluator.evaluatePolicyRequest(prt);
			
			logger.info(JSONUtil.exportObject(prr));
			//roleRule.getPatterns()
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	private PersonRoleType getTestRole(){
		PersonRoleType perRole1 = null;
		try {
			BaseRoleType baseRole = Factories.getRoleFactory().getUserRole(testUser,RoleEnumType.USER,testUser.getOrganizationId());
			//assertNotNull("Base role is null", baseRole);
			//acctRole1 = getRole(testUser,"Account Role 1",RoleEnumType.ACCOUNT,baseRole);
			perRole1 = getRole(testUser,"Person Role 1",RoleEnumType.PERSON,baseRole);
		} catch (FactoryException | ArgumentException | DataAccessException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		return perRole1;
	}
	*/
	
	@Test
	public void TestExtensionService(){
		//FactoryDefaults.createPermissionsForAuthorizationFactories(testUser.getOrganizationId());

		logger.info("Testing Effective Authorization General/Extension Service");
		DirectoryGroupType app1 = getApplication("AuthZ Application #1");
		
		AccountGroupType acctGrp1 = getGroup(testUser,"Account Group 1",GroupEnumType.ACCOUNT,app1);
		AccountRoleType acctRole1 = null;
		PersonRoleType perRole1 = null;
		try {
			BaseRoleType baseRole = Factories.getRoleFactory().getUserRole(testUser,RoleEnumType.USER,testUser.getOrganizationId());
			assertNotNull("Base role is null", baseRole);
			acctRole1 = getRole(testUser,"Account Role 1",RoleEnumType.ACCOUNT,baseRole);
			perRole1 = getRole(testUser,"Person Role 1",RoleEnumType.PERSON,baseRole);
		} catch (FactoryException | ArgumentException | DataAccessException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		PersonType person1 = getApplicationPerson("Person #1",app1);
		PersonType person2 = getApplicationPerson("Person #2",app1);
		PersonType person3 = getApplicationPerson("Person #3",app1);
		AccountType account1 = getApplicationAccount("Account #1",app1);
		AccountType account2 = getApplicationAccount("Account #2",app1);
		AccountType account3 = getApplicationAccount("Account #3",app1);
		AccountType account4 = getApplicationAccount("Account #4",app1);
		
		if(person1.getAccounts().size() == 0){
			person1.getAccounts().add(account1);
			try {
				Factories.getPersonFactory().update(person1);
			} catch (FactoryException  e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(person2.getAccounts().size() == 0){
			person2.getAccounts().add(account2);
			try {
				Factories.getPersonFactory().update(person2);
			} catch (FactoryException  e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		
		
		try {
			DirectoryGroupType dir = Factories.getGroupFactory().getCreateDirectory(testUser, "Policies", testUser.getHomeDirectory(), testUser.getOrganizationId());
			RoleService.addPersonToRole(person3, perRole1);

			PolicyType policy = getCreatePolicy(testUser, "Test Policy", dir);
			assertNotNull("Policy is null", policy);
			assertNotNull("Userr is null", testUser);
			logger.info("Deauthorizing object");
			AuthorizationService.deauthorize(testUser, policy);
			
			BasePermissionType permissionR = AuthorizationService.getViewPermissionForMapType(policy.getNameType(), policy.getOrganizationId());
			BasePermissionType permissionE = AuthorizationService.getExecutePermissionForMapType(policy.getNameType(), policy.getOrganizationId());
			
			assertNotNull("Permission was null", permissionR);
			
			boolean authZ = AuthorizationService.authorize(testUser, testUser2, policy, permissionR, true);
			boolean authZ2 = AuthorizationService.authorize(testUser, perRole1, policy, permissionE, true);
			assertTrue("Failed to authorize", authZ);
			
			
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			EffectiveAuthorizationService.rebuildCache(NameEnumType.POLICY, testUser.getOrganizationId());
			boolean isAuthZ = AuthorizationService.isAuthorized(testUser, policy, new BasePermissionType[]{permissionR});
			assertTrue("Failed to identify expected authorization", isAuthZ);

			boolean canView = AuthorizationService.canView(testUser2, policy);
			logger.info("Can testUser2 View: (true) " + canView);
			assertTrue("testUser2 should be able to view",canView);
			boolean canView3 = AuthorizationService.canView(person3, policy);
			logger.info("Can person3 View: (false) " + canView3);
			assertFalse("person3 should not be able to view", canView3);
			boolean canView4 = AuthorizationService.canExecute(perRole1, policy);
			assertTrue("Role should be able to execute",canView4);
		
		} catch (NullPointerException | ArgumentException | FactoryException | DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void testGenericAuthorization(UserType admin, NameIdType object, NameIdType setMember, NameIdType checkMember, BasePermissionType permission){
		boolean setAuthZ = false;
		boolean isAuthZ = false;
		boolean notAuthZ = false;
		boolean deAuthZ = false;
		
		String authZStr = EffectiveAuthorizationService.getEntitlementCheckString(object, checkMember, new BasePermissionType[]{permission});
		logger.info("TEST AUTHORIZATION " + authZStr);
		try {
			
			//assertTrue("Owner cannot change permission",AuthorizationService.isAuthorized(permission, admin, new BasePermissionType[]{AuthorizationService.getViewPermissionForMapType(permission.getNameType(), permission.getOrganizationId())}));
			setAuthZ = AuthorizationService.authorize(admin, object, setMember, AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId()), true);
			isAuthZ = AuthorizationService.isAuthorized(checkMember, object, new BasePermissionType[]{AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId())});
			deAuthZ = AuthorizationService.authorize(admin, object, setMember, AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId()), false);
			notAuthZ = AuthorizationService.isAuthorized(checkMember, object, new BasePermissionType[]{AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId())});
		} catch (FactoryException | DataAccessException | ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertTrue("Failed to set: " + authZStr,setAuthZ);
		assertTrue("Authorization check failed: " + authZStr,isAuthZ); 
		assertTrue("Failed to deauthorize: " + object.getUrn(),deAuthZ); 
		assertFalse("Succeeded when failured expected: " + authZStr,notAuthZ); 
	}
	
}
