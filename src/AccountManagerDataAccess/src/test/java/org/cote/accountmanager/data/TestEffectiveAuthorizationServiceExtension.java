package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cote.accountmanager.data.factory.FactoryDefaults;
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
	// FactoryDefaults.createPermissionsForAuthorizationFactories(testUser.getOrganizationId());
	
	@Test
	public void TestPolicyBasedAuthorization(){
		DirectoryGroupType odir = null;
		DirectoryGroupType pdir = null;
		DirectoryGroupType rdir = null;
		DirectoryGroupType padir = null;
		DirectoryGroupType fdir = null;
		PolicyType policy = null;
		RuleType roleRule = null;
		RuleType roleRule1 = null;
		RuleType roleRule2 = null;
		/*
		RuleType roleRule1 = null;
		RuleType roleRule2 = null;
		RuleType roleRule3 = null;
		RuleType roleRule4 = null;
		*/
		
		PatternType pattern1 = null;
		PatternType pattern2 = null;
		PatternType pattern3 = null;
		PatternType pattern4 = null;
		
		OperationType oper1 = null;
		FactType fact1 = null;
		FactType fact2 = null;
		
		RuleType objectRule = null;
		
		String policyName = "Can View Type";
		String operationName= "Compare Name Type";
		String roleRuleName = "View Actor Is Role";
		String roleRule1Name = "Actor is Role";
		String pattern1Name = "Actor is Role";
		String fact1Name = "Name Type Parameter";
		String fact2Name = "Role Name Type";
		
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
			

			fact1 = Factories.getFactFactory().getByNameInGroup(fact1Name,fdir);
			if(fact1 == null){
				fact1 = Factories.getFactFactory().newFact(testUser, fdir.getId());
				fact1.setFactType(FactEnumType.PARAMETER);
				fact1.setFactoryType(FactoryEnumType.UNKNOWN);
				fact1.setName(fact1Name);
				Factories.getFactFactory().addFact(fact1);
				fact1 = Factories.getFactFactory().getByNameInGroup(fact1Name,fdir);
			}
			fact2 = Factories.getFactFactory().getByNameInGroup(fact2Name,fdir);
			if(fact2 == null){
				fact2 = Factories.getFactFactory().newFact(testUser, fdir.getId());
				fact2.setFactType(FactEnumType.ROLE);
				fact2.setName(fact2Name);
				Factories.getFactFactory().addFact(fact2);
				fact2 = Factories.getFactFactory().getByNameInGroup(fact2Name,fdir);
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
				Factories.getPatternFactory().addPattern(pattern1);
				pattern1 = Factories.getPatternFactory().getByNameInGroup(pattern1Name, padir);	

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
				roleRule1.setRuleType(RuleEnumType.PERMIT);
				Factories.getRuleFactory().addRule(roleRule1);
				roleRule1 = Factories.getRuleFactory().getByNameInGroup(roleRule1Name, rdir);	
			}
			Factories.getRuleFactory().populate(roleRule1);
			
			roleRule = Factories.getRuleFactory().getByNameInGroup(roleRuleName, rdir);
			if(cleanup && roleRule != null){
				Factories.getRuleFactory().deleteRule(roleRule);
				roleRule = null;
			}

			if(roleRule == null){
				roleRule = Factories.getRuleFactory().newRule(testUser, rdir.getId());
				roleRule.getRules().add(roleRule1);
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
				Factories.getPolicyFactory().addPolicy(policy);
				policy = Factories.getPolicyFactory().getByNameInGroup(policyName, pdir);
			}
			Factories.getPolicyFactory().populate(policy);
			for(int i = 0; i < policy.getRules().size(); i++){
				Factories.getRuleFactory().populate(policy.getRules().get(i));
				for(int p = 0; p < policy.getRules().get(i).getRules().size(); p++){
					Factories.getRuleFactory().populate(policy.getRules().get(i).getRules().get(p));
				}
			}
			
			logger.info(JSONUtil.exportObject(policy));
			
			//roleRule2 = Factories.getRuleFactory().newRule(testUser, rdir.getId());
			

			
			
			//roleRule.getPatterns()
		} catch (FactoryException | ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void TestExtensionService(){
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
				Factories.getPersonFactory().updatePerson(person1);
			} catch (FactoryException | DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(person2.getAccounts().size() == 0){
			person2.getAccounts().add(account2);
			try {
				Factories.getPersonFactory().updatePerson(person2);
			} catch (FactoryException | DataAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		

		
		
		try {
			DirectoryGroupType dir = Factories.getGroupFactory().getCreateDirectory(testUser, "Policies", testUser.getHomeDirectory(), testUser.getOrganizationId());
			RoleService.addPersonToRole(person3, perRole1);

			PolicyType policy = getCreatePolicy(testUser, "Test Policy", dir);
			assertNotNull("Policy is null", policy);
			logger.info("Deauthorizing object");
			AuthorizationService.deauthorize(testUser, policy);
			
			BasePermissionType permission = AuthorizationService.getViewPermissionForMapType(policy.getNameType(), policy.getOrganizationId());
			BasePermissionType permissionE = AuthorizationService.getExecutePermissionForMapType(policy.getNameType(), policy.getOrganizationId());
			
			assertNotNull("Permission was null", permission);
			logger.info("Using permission " + permission.getUrn());
			
			//testGenericAuthorization(testUser, policy, testUser2, )
			
			boolean authZ = AuthorizationService.authorize(testUser, policy, testUser2, permission, true);
			boolean authZ2 = AuthorizationService.authorize(testUser, policy, perRole1, permissionE, true);
			assertTrue("Failed to authorize", authZ);
			
			
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			EffectiveAuthorizationService.rebuildCache(NameEnumType.POLICY, testUser.getOrganizationId());
			boolean isAuthZ = AuthorizationService.isAuthorized(policy, testUser, new BasePermissionType[]{permission});
			assertTrue("Failed to identify expected authorization", isAuthZ);
		} catch (ArgumentException | FactoryException | DataAccessException e) {
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
			isAuthZ = AuthorizationService.isAuthorized(object, checkMember, new BasePermissionType[]{AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId())});
			deAuthZ = AuthorizationService.authorize(admin, object, setMember, AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId()), false);
			notAuthZ = AuthorizationService.isAuthorized(object, checkMember, new BasePermissionType[]{AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId())});
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
