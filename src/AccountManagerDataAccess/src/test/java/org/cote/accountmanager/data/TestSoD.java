package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.GroupService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseEnumType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.junit.Test;

public class TestSoD extends BaseDataAccessTest{
	//
	private static long testRefId = 0;

	private BaseRoleType getRoleBase(UserType user) throws FactoryException, ArgumentException, DataAccessException{
		BaseRoleType p = Factories.getRoleFactory().getUserRole(user, RoleEnumType.ACCOUNT, user.getOrganizationId());
		return Factories.getRoleFactory().getCreateRole(user, "ApplicationRoles", RoleEnumType.ACCOUNT, p, user.getOrganizationId());
	}
	
	private BasePermissionType getPermissionBase(UserType user) throws FactoryException, ArgumentException, DataAccessException{
		BasePermissionType p = Factories.getPermissionFactory().getUserPermission(user, PermissionEnumType.ACCOUNT, user.getOrganizationId());
		return Factories.getPermissionFactory().getCreatePermission(user, "SoD", PermissionEnumType.ACCOUNT, p, user.getOrganizationId());
	}
	
	private DirectoryGroupType getSoDBase(UserType user) throws FactoryException, ArgumentException{
		return Factories.getGroupFactory().getCreatePath(user, "~/SoD", testUser.getOrganizationId());
	}
	
	private DirectoryGroupType getActivityBase(UserType user) throws FactoryException, ArgumentException{
		return Factories.getGroupFactory().getCreatePath(user, "~/SoD/Activities", testUser.getOrganizationId());
	}
	
	private DirectoryGroupType getSoDActivity(UserType user, String name, DirectoryGroupType parent) throws FactoryException, ArgumentException, DataAccessException{
		if(parent == null) parent = getActivityBase(user);
		Factories.getGroupFactory().denormalize(parent);
		DirectoryGroupType group = Factories.getGroupFactory().getCreatePath(user, parent.getPath() + "/" + name, user.getOrganizationId());
		GroupService.addGroupToGroup(group, parent);
		return group;
	}
	
	private PersonType getSoDContextPerson(UserType user,String name) throws FactoryException, ArgumentException{
		PersonType per = Factories.getPersonFactory().getByNameInGroup(name, getActivityBase(user));
		if(per == null){
			per = Factories.getPersonFactory().newPerson(user, getActivityBase(user).getId());
			per.setName(name);
			Factories.getPersonFactory().add(per);
			per = Factories.getPersonFactory().getByNameInGroup(name, getActivityBase(user));
		}
		return per;
	}
	private AccountType getSoDContextAccount(UserType user, String name) throws FactoryException, ArgumentException{
		AccountType per = Factories.getAccountFactory().getByNameInGroup(name, getActivityBase(user));
		if(per == null){
			per = Factories.getAccountFactory().newAccount(user, name,AccountEnumType.NORMAL, AccountStatusEnumType.NORMAL,getActivityBase(user).getId());

			Factories.getAccountFactory().add(per);
			per = Factories.getAccountFactory().getByNameInGroup(name, getActivityBase(user));
		}
		return per;
	}

	private BaseRoleType getApplicationRole(UserType user, String name, BaseRoleType parent) throws FactoryException, ArgumentException, DataAccessException{
		return Factories.getRoleFactory().getCreateRole(user, name, RoleEnumType.ACCOUNT, getRoleBase(user), user.getOrganizationId());
	}	
	private BasePermissionType getSodPermission(UserType user, String name, BasePermissionType parent) throws FactoryException, ArgumentException, DataAccessException{
		return Factories.getPermissionFactory().getCreatePermission(user, name, PermissionEnumType.ACCOUNT, getPermissionBase(user), user.getOrganizationId());
	}
/*
	private PolicyType getCreatePolicy(UserType user, String name, DirectoryGroupType dir){

		PolicyType policy = null;

		try {
			policy = Factories.getPolicyFactory().getByNameInGroup(name, dir);
			if(policy == null){
				policy = Factories.getPolicyFactory().newPolicy(testUser, dir);
				policy.setCondition(ConditionEnumType.ALL);
				policy.setName(name);
				if(Factories.getPolicyFactory().addPolicy(policy)){
					policy = Factories.getPolicyFactory().getByNameInGroup(name, dir);
				}
				else policy = null;
			}
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return policy;
	}
	*/
	private PatternType getCreateSoDPattern(UserType user, String name, String factUrn, String matchUrn, DirectoryGroupType dir){

		PatternType pattern = null;

		try {
			pattern = Factories.getPatternFactory().getByNameInGroup(name, dir);
			if(pattern == null){
				pattern = Factories.getPatternFactory().newPattern(testUser, dir.getId());
				pattern.setName(name);
				pattern.setPatternType(PatternEnumType.SEPARATION_OF_DUTY);
				pattern.setComparator(ComparatorEnumType.EQUALS);
				pattern.setFactUrn(factUrn);
				pattern.setMatchUrn(matchUrn);

				if(Factories.getPatternFactory().add(pattern)){
					pattern = Factories.getPatternFactory().getByNameInGroup(name, dir);
				}
				else pattern = null;
			}
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pattern;
	}
/*
	private RuleType getCreateRule(UserType user, String name, DirectoryGroupType dir){

		RuleType rule = null;

		try {
			rule = Factories.getRuleFactory().getByNameInGroup(name, dir);
			if(rule == null){
				rule = Factories.getRuleFactory().newRule(testUser, dir);
				rule.setName(name);
				rule.setRuleType(RuleEnumType.DENY);
				rule.setCondition(ConditionEnumType.ALL);
				if(Factories.getRuleFactory().addRule(rule)){
					rule = Factories.getRuleFactory().getByNameInGroup(name, dir);
				}
				else rule = null;
			}
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rule;
	}
	*/
	private FactType getCreateGroupFact(UserType user, String name, DirectoryGroupType dir, DirectoryGroupType fDir){
		FactType fact = null;
		try {
			fact = Factories.getFactFactory().getByNameInGroup(name,dir);
			if(fact == null){
				
				fact = Factories.getFactFactory().newFact(user, dir.getId());
				fact.setName(name);
				//fact.setUrn(urn);
				fact.setFactType(FactEnumType.GROUP);
				fact.setFactoryType(FactoryEnumType.GROUP);
				fact.setSourceType(fDir.getGroupType().toString());
				//fact.setSourceUrn(fDir.getName());
				fact.setSourceUrn(fDir.getUrn());
				//fact.setSourceUrl(Factories.getGroupFactory().getPath(Factories.getGroupFactory().getGroupById(fDir.getParentId(), fDir.getOrganizationId())));
				if(Factories.getFactFactory().add(fact)){
					fact = Factories.getFactFactory().getByNameInGroup(name, dir);
				}
				else fact = null;
			}
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		return fact;
	}

	private FactType getCreateAccountParameterFact(UserType user, String name, DirectoryGroupType dir){
		FactType fact = null;
		try {
			fact = Factories.getFactFactory().getByNameInGroup(name,dir);
			if(fact == null){
				
				fact = Factories.getFactFactory().newFact(user, dir.getId());
				fact.setName(name);
				//fact.setUrn(urn);
				fact.setFactType(FactEnumType.PARAMETER);
				fact.setFactoryType(FactoryEnumType.ACCOUNT);
				if(Factories.getFactFactory().add(fact)){
					fact = Factories.getFactFactory().getByNameInGroup(name, dir);
				}
				else fact = null;
			}
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		return fact;
	}
	@Test
	public void TestStructure1(){
		configureTestData();
		PolicyType sodPolicy = getTestSoDPolicy1();
		assertNotNull("SoD Policy Is Null", sodPolicy);
		
		try {
			logger.info(PolicyDefinitionUtil.printPolicy(sodPolicy));
			PolicyDefinitionType pdt = PolicyDefinitionUtil.generatePolicyDefinition(sodPolicy);
			PolicyRequestType prt = PolicyDefinitionUtil.generatePolicyRequest(pdt);
			assertTrue("Params Length Is 0",prt.getFacts().size() > 0);
			
			FactType accountFact = prt.getFacts().get(0);
			DirectoryGroupType adir = getActivityBase(testUser);
			Factories.getGroupFactory().denormalize(adir);
			//accountFact.setSourceUrl(adir.getPath());
			accountFact.setSourceUrn( getSoDContextAccount(testUser,"Demo Account 2").getUrn());

			PolicyResponseType prr = PolicyEvaluator.evaluatePolicyRequest(prt);
			logger.info("Expect DENY For Policy " + prr.getUrn() + " with account #2 returned " + prr.getResponse().toString());
			assertTrue("EXPECT DENY: Policy Evaluation for " + prr.getUrn() + " = " + prr.getResponse().toString(),prr.getResponse() == PolicyResponseEnumType.DENY);

			accountFact.setSourceUrn(getSoDContextAccount(testUser,"Demo Account 1").getUrn());
			PolicyResponseType prr2 = PolicyEvaluator.evaluatePolicyRequest(prt);
			logger.info("Expect Permit For Policy " + prr2.getUrn() + " with account #1 returned " + prr2.getResponse().toString());
			assertTrue("EXPECT PERMIT: Policy Evaluation for " + prr2.getUrn() + " = " + prr2.getResponse().toString(),prr2.getResponse() == PolicyResponseEnumType.PERMIT);
			//assertTrue("Policy response was " + prr.getResponse() + "; was expecting PERMIT",prr.getResponse() == PolicyResponseEnumType.PERMIT);
			
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	public void configureTestData(){
		BasePermissionType sodPer1 = null;
		BasePermissionType sodPer2 = null;
		BasePermissionType sodPer3 = null;
		BasePermissionType sodPer4 = null;
		BasePermissionType sodPer5 = null;
		BasePermissionType sodPer6 = null;
		
		PersonType sodContextPer = null;
		AccountType sodContextAcct = null;
		
		PersonType demoPerson1 = null;
		PersonType demoPerson2 = null;
		AccountType demoAccount1 = null;
		AccountType demoAccount2 = null;

		DirectoryGroupType sodParentAct1 = null;
		DirectoryGroupType sodChildAct1 = null;
		DirectoryGroupType sodGrandChildAct1 = null;
		DirectoryGroupType sodParentAct2 = null;
		DirectoryGroupType sodChildAct2 = null;
		DirectoryGroupType sodGrandChildAct2 = null;
		DirectoryGroupType sodApplication1 = null;
		BaseRoleType sodRole1 = null;
		BaseRoleType sodRole2 = null;

		try {
			sodContextPer = getSoDContextPerson(testUser,"SoDContext");
			sodContextAcct = getSoDContextAccount(testUser,"SoDContext");
			
			demoPerson1 = getSoDContextPerson(testUser,"Demo Person 1");
			demoPerson2 = getSoDContextPerson(testUser,"Demo Person 2");
			demoAccount1 = getSoDContextAccount(testUser,"Demo Account 1");
			demoAccount2 = getSoDContextAccount(testUser,"Demo Account 2");
			demoPerson1.getAccounts().clear();
			demoPerson1.getAccounts().add(demoAccount1);
			Factories.getPersonFactory().update(demoPerson1);
			
			demoPerson2.getAccounts().clear();
			demoPerson2.getAccounts().add(demoAccount2);
			Factories.getPersonFactory().update(demoPerson2);
			
			sodApplication1 = Factories.getGroupFactory().getCreatePath(testUser, "~/Applications/Application 1", testUser.getOrganizationId());
			
			sodParentAct1 = getSoDActivity(testUser, "Business Activity 1", null);
			sodChildAct1 = getSoDActivity(testUser, "Business Activity Child 1", sodParentAct1);
			sodGrandChildAct1 = getSoDActivity(testUser, "Business Activity Grand Child 1", sodChildAct1);
			sodParentAct2 = getSoDActivity(testUser, "Business Activity 2", null);
			sodChildAct2 = getSoDActivity(testUser, "Business Activity Child 2", sodParentAct2);
			sodGrandChildAct2 = getSoDActivity(testUser, "Business Activity Grand Child 2", sodChildAct2);
			
			sodPer1 = getSodPermission(testUser, "Permission 1",null);
			sodPer2 = getSodPermission(testUser, "Permission 2",null);
			sodPer3 = getSodPermission(testUser, "Permission 3",null);
			sodPer4 = getSodPermission(testUser, "Permission 4",null);
			sodPer5 = getSodPermission(testUser, "Permission 5",null);
			sodPer6 = getSodPermission(testUser, "Permission 6",null);
			
			/// SoD role is actually an organization or application role, not a specific SoD construct
			///
			sodRole1 = getApplicationRole(testUser,"Role 1",null);
			sodRole2 = getApplicationRole(testUser,"Role 2",null);
			
			/// Adding the context person or accout to the role to make sure there is at least one entity in the role
			/// Otherwise, the participation table values won't exist in the database
			///
			if(sodRole1.getRoleType() == RoleEnumType.ACCOUNT) RoleService.addAccountToRole(sodContextAcct, (AccountRoleType)sodRole1);
			else if(sodRole1.getRoleType() == RoleEnumType.PERSON) RoleService.addPersonToRole(sodContextPer, (PersonRoleType)sodRole1);
			if(sodRole2.getRoleType() == RoleEnumType.ACCOUNT) RoleService.addAccountToRole(sodContextAcct, (AccountRoleType)sodRole2);
			else if(sodRole2.getRoleType() == RoleEnumType.PERSON) RoleService.addPersonToRole(sodContextPer, (PersonRoleType)sodRole2);
			
			/// Add demoAccount to role to simulate actual use
			/// account1 has role1
			/// account2 has role1 and role2
			///
			RoleService.addAccountToRole(demoAccount1, (AccountRoleType)sodRole1);
			RoleService.addAccountToRole(demoAccount2, (AccountRoleType)sodRole1);
			RoleService.addAccountToRole(demoAccount2, (AccountRoleType)sodRole2);
			
			/// Assign the roles and permissions to the Applications
			AuthorizationService.authorize(testUser,sodRole1,sodApplication1,sodPer2,true);
			AuthorizationService.authorize(testUser,sodRole1,sodApplication1,sodPer3,true);
			AuthorizationService.authorize(testUser,sodRole2,sodApplication1,sodPer5,true);
			AuthorizationService.authorize(testUser,sodRole2,sodApplication1,sodPer6,true);
			
			/// Assign the roles and permissions to the SoD Activities
			/// NOTE: This means that to be useful, at least for roles, the operation of attaching and detaching the role will have to replicate the current assignments over for that role
			/// I need to double check this versus just giving it a dummy read permisson and then pulling straight off the source application which would probably make more sense
			///
			AuthorizationService.authorize(testUser, sodRole1, sodChildAct1, sodPer2, true);
			AuthorizationService.authorize(testUser, sodRole1, sodChildAct1, sodPer3, true);
			AuthorizationService.authorize(testUser, sodContextPer, sodGrandChildAct1, sodPer1, true);
			
			AuthorizationService.authorize(testUser, sodRole2, sodChildAct2, sodPer5, true);
			AuthorizationService.authorize(testUser, sodRole2, sodChildAct2, sodPer6, true);
			AuthorizationService.authorize(testUser, sodContextPer, sodGrandChildAct2, sodPer4, true);

			EffectiveAuthorizationService.rebuildPendingRoleCache();


		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public PolicyType getTestSoDPolicy1(){


		DirectoryGroupType sodParentAct1 = null;
		DirectoryGroupType sodChildAct1 = null;
		DirectoryGroupType sodGrandChildAct1 = null;
		DirectoryGroupType sodParentAct2 = null;
		DirectoryGroupType sodChildAct2 = null;
		DirectoryGroupType sodGrandChildAct2 = null;

		PolicyType sodPol1 = null;
		PatternType sodPat2 = null;
		PatternType sodPat1 = null;

		RuleType sodRule1 = null;
		FactType pFact = null;
		FactType fact1 = null;
		FactType fact2 = null;

		try {
			sodParentAct1 = getSoDActivity(testUser, "Business Activity 1", null);
			sodChildAct1 = getSoDActivity(testUser, "Business Activity Child 1", sodParentAct1);
			sodGrandChildAct1 = getSoDActivity(testUser, "Business Activity Grand Child 1", sodChildAct1);
			sodParentAct2 = getSoDActivity(testUser, "Business Activity 2", null);
			sodChildAct2 = getSoDActivity(testUser, "Business Activity Child 2", sodParentAct2);
			sodGrandChildAct2 = getSoDActivity(testUser, "Business Activity Grand Child 2", sodChildAct2);


			sodPol1 = getCreatePolicy(testUser, "SoD Policy 1",Factories.getGroupFactory().getCreatePath(testUser, "~/Policies", testUser.getOrganizationId()));
			sodRule1 = getCreateRule(testUser,"SoD Rule 1",RuleEnumType.DENY,Factories.getGroupFactory().getCreatePath(testUser, "~/Rules", testUser.getOrganizationId()));
			
			sodPat2 =  getCreateSoDPattern(testUser,"SoD Pattern 2",null,null,Factories.getGroupFactory().getCreatePath(testUser, "~/Patterns", testUser.getOrganizationId()));
			sodPat1 =  getCreateSoDPattern(testUser,"SoD Pattern 1",null,null,Factories.getGroupFactory().getCreatePath(testUser, "~/Patterns", testUser.getOrganizationId()));
			
			pFact = getCreateAccountParameterFact(testUser,"Account Parameter", Factories.getGroupFactory().getCreatePath(testUser, "~/Facts", testUser.getOrganizationId()));
			fact1 = getCreateGroupFact(testUser,"Activity 1", Factories.getGroupFactory().getCreatePath(testUser, "~/Facts", testUser.getOrganizationId()),sodChildAct1);
			fact2 = getCreateGroupFact(testUser,"Activity 2", Factories.getGroupFactory().getCreatePath(testUser, "~/Facts", testUser.getOrganizationId()),sodChildAct2);
			sodPat2.setFactUrn(pFact.getUrn());
			sodPat2.setMatchUrn(fact2.getUrn());
			sodPat2.setLogicalOrder(2);
			Factories.getPatternFactory().update(sodPat2);
			sodPat2 =  getCreateSoDPattern(testUser,"SoD Pattern 2",null,null,Factories.getGroupFactory().getCreatePath(testUser, "~/Patterns", testUser.getOrganizationId()));
			
			sodPat1.setFactUrn(pFact.getUrn());
			sodPat1.setMatchUrn(fact1.getUrn());
			sodPat1.setLogicalOrder(1);
			Factories.getPatternFactory().update(sodPat1);
			sodPat1 =  getCreateSoDPattern(testUser,"SoD Pattern 1",null,null,Factories.getGroupFactory().getCreatePath(testUser, "~/Patterns", testUser.getOrganizationId()));
			sodRule1.getPatterns().clear();
			//sodRule1.getPatterns().add(sodPat0);
			sodRule1.getPatterns().add(sodPat1);
			sodRule1.getPatterns().add(sodPat2);
			sodRule1.setRuleType(RuleEnumType.DENY);
			Factories.getRuleFactory().update(sodRule1);
			sodRule1 = getCreateRule(testUser,"SoD Rule 1",Factories.getGroupFactory().getCreatePath(testUser, "~/Rules", testUser.getOrganizationId()));
			sodPol1.getRules().clear();
			sodPol1.getRules().add(sodRule1);
			sodPol1.setEnabled(true);
			Factories.getPolicyFactory().update(sodPol1);
			sodPol1 = getCreatePolicy(testUser, "SoD Policy 1",Factories.getGroupFactory().getCreatePath(testUser, "~/Policies", testUser.getOrganizationId()));
			
			
			//logger.info("Permission 1 " + Factories.getPermissionFactory().getPermissionPath(sodPer1));
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		/*
		assertNotNull("SoD Policy 1 is null", sodPol1);
		assertNotNull("SoD Rule 1 is null", sodRule1);
		assertNotNull("SoD Pattern 1 is null", sodPat1);
		assertNotNull("SoDContext Person is null", sodContextPer);
		assertNotNull("SoDContext Account is null", sodContextAcct);
		assertNotNull("Activity 1 is null", sodParentAct1);
		assertNotNull("Child Activity 1 is null", sodChildAct1);
		assertNotNull("Grand Child Activity 1 is null", sodGrandChildAct1);
		assertNotNull("Activity 2 is null", sodParentAct2);
		assertNotNull("Child Activity 2 is null", sodChildAct2);
		assertNotNull("Grand Child Activity 2 is null", sodGrandChildAct2);
		assertNotNull("Permission 1 is null", sodPer1);
		assertNotNull("Role 1 is null", sodRole1);
		
		try {
			AuthorizationService.authorize(testUser, sodRole1, sodChildAct1, sodPer2, true);
			AuthorizationService.authorize(testUser, sodRole1, sodChildAct1, sodPer3, true);
			AuthorizationService.authorize(testUser, sodContextPer, sodGrandChildAct1, sodPer1, true);
			
			AuthorizationService.authorize(testUser, sodRole2, sodChildAct2, sodPer5, true);
			AuthorizationService.authorize(testUser, sodRole2, sodChildAct2, sodPer6, true);
			AuthorizationService.authorize(testUser, sodContextPer, sodGrandChildAct2, sodPer4, true);

			EffectiveAuthorizationService.rebuildPendingRoleCache();

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		return sodPol1;
		
	}
}