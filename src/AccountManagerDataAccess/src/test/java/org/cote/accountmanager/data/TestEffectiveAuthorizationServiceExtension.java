/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.data;


/*
 * In order for this unit test to work, the authorization extension for POLICY must be configured with AccountManagerConsole
 */
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
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
			odir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Operations", testUser.getOrganizationId());
			pdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Policies", testUser.getOrganizationId());
			rdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Rules", testUser.getOrganizationId());
			padir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Patterns", testUser.getOrganizationId());
			fdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Facts", testUser.getOrganizationId());

			oper1 = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).getByNameInGroup(operationName, odir);
			if(oper1 == null){
				oper1 = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).newOperation(testUser, odir.getId());
				oper1.setOperationType(OperationEnumType.INTERNAL);
				oper1.setOperation("org.cote.accountmanager.data.operation.CompareNameTypeOperation");
				oper1.setName(operationName);
				((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).addOperation(oper1);
				oper1 = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).getByNameInGroup(operationName, odir);
			}
			
			oper2 = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).getByNameInGroup(operationName2, odir);
			if(oper2 == null){
				oper2 = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).newOperation(testUser, odir.getId());
				oper2.setOperationType(OperationEnumType.INTERNAL);
				oper2.setOperation("org.cote.accountmanager.data.operation.MatchSystemRoleOperation");
				oper2.setName(operationName2);
				((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).addOperation(oper2);
				oper2 = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).getByNameInGroup(operationName2, odir);
			}
			
			
			fact1 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(fact1Name,fdir);
			if(cleanup && fact1 != null){
				((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).deleteFact(fact1);
				fact1 = null;
			}
			if(fact1 == null){
				fact1 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
				fact1.setFactType(FactEnumType.PARAMETER);
				fact1.setFactoryType(FactoryEnumType.UNKNOWN);
				fact1.setName(fact1Name);
				fact1.setDescription("ActorType");
				((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).addFact(fact1);
				fact1 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(fact1Name,fdir);
			}
			
			fact2 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(fact2Name,fdir);
			if(cleanup && fact2 != null){
				((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).deleteFact(fact2);
				fact2 = null;
			}
			if(fact2 == null){
				fact2 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
				fact2.setFactType(FactEnumType.ROLE);
				fact2.setFactoryType(FactoryEnumType.ROLE);
				fact2.setName(fact2Name);
				((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).addFact(fact2);
				fact2 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(fact2Name,fdir);
			}
			fact3 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(fact3Name,fdir);
			if(cleanup && fact3 != null){
				((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).deleteFact(fact3);
				fact3 = null;
			}
			if(fact3 == null){
				fact3 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
				fact3.setFactType(FactEnumType.ROLE);
				fact3.setFactoryType(FactoryEnumType.UNKNOWN);
				fact3.setSourceUrn("DataAdministrators");
				fact3.setName(fact3Name);
				((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).addFact(fact3);
				fact3 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(fact3Name,fdir);
			}
			
			fact5 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(fact5Name,fdir);
			if(cleanup && fact5 != null){
				((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).deleteFact(fact5);
				fact5 = null;
			}
			if(fact5 == null){
				fact5 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
				fact5.setFactType(FactEnumType.ROLE);
				fact5.setFactoryType(FactoryEnumType.UNKNOWN);
				fact5.setSourceUrn("DataReaders");
				fact5.setName(fact5Name);
				((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).addFact(fact5);
				fact5 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(fact5Name,fdir);
			}
			
			fact4 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(fact4Name,fdir);
			if(cleanup && fact4 != null){
				((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).deleteFact(fact4);
				fact4 = null;
			}
			if(fact4 == null){
				fact4 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
				fact4.setFactType(FactEnumType.PARAMETER);
				fact4.setFactoryType(FactoryEnumType.UNKNOWN);
				fact4.setName(fact4Name);
				fact4.setDescription("ObjectType");
				((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).addFact(fact4);
				fact4 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(fact4Name,fdir);
			}
			pattern1 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pattern1Name, padir);
			if(cleanup && pattern1 != null){
				((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).deletePattern(pattern1);
				pattern1 = null;
			}
			if(pattern1 == null){
				pattern1 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).newPattern(testUser, padir.getId());
				pattern1.setName(pattern1Name);
				pattern1.setPatternType(PatternEnumType.OPERATION);
				pattern1.setOperationUrn(oper1.getUrn());
				pattern1.setComparator(ComparatorEnumType.EQUALS);
				pattern1.setFactUrn(fact1.getUrn());
				pattern1.setMatchUrn(fact2.getUrn());
				pattern1.setLogicalOrder(1);
				((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).addPattern(pattern1);
				pattern1 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pattern1Name, padir);	

			}
			
			pattern2 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pattern2Name, padir);
			if(cleanup && pattern2 != null){
				((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).deletePattern(pattern2);
				pattern2 = null;
			}
			if(pattern2 == null){
				pattern2 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).newPattern(testUser, padir.getId());
				pattern2.setName(pattern2Name);
				pattern2.setPatternType(PatternEnumType.OPERATION);
				pattern2.setOperationUrn(oper2.getUrn());
				pattern2.setComparator(ComparatorEnumType.EQUALS);
				pattern2.setFactUrn(fact1.getUrn());
				pattern2.setMatchUrn(fact3.getUrn());
				pattern2.setLogicalOrder(2);
				((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).addPattern(pattern2);
				pattern2 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pattern2Name, padir);	

			}
			
			pattern3 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pattern3Name, padir);
			if(cleanup && pattern3 != null){
				((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).deletePattern(pattern3);
				pattern3 = null;
			}
			if(pattern3 == null){
				pattern3 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).newPattern(testUser, padir.getId());
				pattern3.setName(pattern3Name);
				pattern3.setPatternType(PatternEnumType.OPERATION);
				pattern3.setOperationUrn(oper2.getUrn());
				pattern3.setComparator(ComparatorEnumType.EQUALS);
				pattern3.setFactUrn(fact1.getUrn());
				pattern3.setMatchUrn(fact5.getUrn());
				pattern3.setLogicalOrder(3);
				((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).addPattern(pattern3);
				pattern3 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pattern3Name, padir);	

			}
			
			roleRule1 = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(roleRule1Name, rdir);
			if(cleanup && roleRule1 != null){
				((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).deleteRule(roleRule1);
				roleRule1 = null;
			}
			if(roleRule1 == null){
				roleRule1 = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).newRule(testUser, rdir.getId());
				roleRule1.getPatterns().add(pattern1);
				roleRule1.setCondition(ConditionEnumType.ANY);
				roleRule1.setName(roleRule1Name);
				roleRule1.setLogicalOrder(1);
				roleRule1.setRuleType(RuleEnumType.PERMIT);
				((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).addRule(roleRule1);
				roleRule1 = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(roleRule1Name, rdir);	
			}
			((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).populate(roleRule1);

			roleRule2 = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(roleRule2Name, rdir);
			if(cleanup && roleRule2 != null){
				((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).deleteRule(roleRule2);
				roleRule2 = null;
			}
			if(roleRule2 == null){
				roleRule2 = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).newRule(testUser, rdir.getId());
				roleRule2.getPatterns().add(pattern2);
				roleRule2.getPatterns().add(pattern3);
				roleRule2.setCondition(ConditionEnumType.ANY);
				roleRule2.setName(roleRule2Name);
				roleRule2.setLogicalOrder(2);
				roleRule2.setRuleType(RuleEnumType.PERMIT);
				((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).addRule(roleRule2);
				roleRule2 = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(roleRule2Name, rdir);	
			}
			((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).populate(roleRule2);
			
			roleRule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(roleRuleName, rdir);
			if(cleanup && roleRule != null){
				((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).deleteRule(roleRule);
				roleRule = null;
			}

			if(roleRule == null){
				roleRule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).newRule(testUser, rdir.getId());
				roleRule.getRules().add(roleRule1);
				roleRule.getRules().add(roleRule2);
				roleRule.setRuleType(RuleEnumType.PERMIT);
				roleRule.setCondition(ConditionEnumType.ALL);
				roleRule.setName(roleRuleName);
				((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).addRule(roleRule);
				roleRule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(roleRuleName, rdir);
			}
			((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).populate(roleRule);
			logger.info("Rules: " + roleRule.getRules().size());
			
			policy = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).getByNameInGroup(policyName, pdir);
			if(cleanup && policy != null){
				((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).deletePolicy(policy);
				policy = null;
			}

			if(policy == null){
				policy = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).newPolicy(testUser, pdir.getId());
				policy.setCondition(ConditionEnumType.ANY);
				policy.setEnabled(true);
				policy.setName(policyName);
				policy.getRules().add(roleRule);
				//policy.getRules().add(roleRule1);
				((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).addPolicy(policy);
				policy = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).getByNameInGroup(policyName, pdir);
			}
			((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).populate(policy);
			for(int i = 0; i < policy.getRules().size(); i++){
				((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).populate(policy.getRules().get(i));
				for(int p = 0; p < policy.getRules().get(i).getRules().size(); p++){
					((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).populate(policy.getRules().get(i).getRules().get(p));
					RuleType rule = policy.getRules().get(i).getRules().get(p);
					for(int q = 0; q < rule.getPatterns().size();q++){
						((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).populate(rule.getPatterns().get(q));
					}
				}
			}
			
			//logger.info(JSONUtil.exportObject(fact1));
			//logger.info(JSONUtil.exportObject(fact2));
			logger.info(JSONUtil.exportObject(policy));
			
			//roleRule2 = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).newRule(testUser, rdir.getId());
			
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
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
	}
	private PersonRoleType getTestRole(){
		PersonRoleType perRole1 = null;
		try {
			BaseRoleType baseRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(testUser,RoleEnumType.USER,testUser.getOrganizationId());
			//assertNotNull("Base role is null", baseRole);
			//acctRole1 = getRole(testUser,"Account Role 1",RoleEnumType.ACCOUNT,baseRole);
			perRole1 = getRole(testUser,"Person Role 1",RoleEnumType.PERSON,baseRole);
		} catch (FactoryException | ArgumentException | DataAccessException e2) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e2);
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
			BaseRoleType baseRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(testUser,RoleEnumType.USER,testUser.getOrganizationId());
			assertNotNull("Base role is null", baseRole);
			acctRole1 = getRole(testUser,"Account Role 1",RoleEnumType.ACCOUNT,baseRole);
			perRole1 = getRole(testUser,"Person Role 1",RoleEnumType.PERSON,baseRole);
		} catch (FactoryException | ArgumentException  e2) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e2);
		}
		
		
		PersonType person1 = getApplicationPerson("Person #1",app1);
		PersonType person2 = getApplicationPerson("Person #2",app1);
		PersonType person3 = getApplicationPerson("Person #3",app1);
		AccountType account1 = getApplicationAccount("Account #1",app1);
		AccountType account2 = getApplicationAccount("Account #2",app1);
		AccountType account3 = getApplicationAccount("Account #3",app1);
		AccountType account4 = getApplicationAccount("Account #4",app1);
		boolean error = false;
		if(person1.getAccounts().isEmpty()){
			person1.getAccounts().add(account1);
			try {
				((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).update(person1);
			} catch (FactoryException  e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				error = true;
			}
		}
		assertFalse("An error occurred",error);
		if(person2.getAccounts().isEmpty()){
			person2.getAccounts().add(account2);
			try {
				((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).update(person2);
			} catch (FactoryException  e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				error = true;
			}
		}
		assertFalse("An error occurred",error);
		

		
		
		try {
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Policies", testUser.getHomeDirectory(), testUser.getOrganizationId());
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
			boolean isAuthZ = AuthorizationService.isAuthorized(testUser, policy, AuthorizationService.PERMISSION_VIEW, new BasePermissionType[]{permissionR});
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
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			error = true;
			
		}
		assertFalse("An error occurred",error);
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
			isAuthZ = AuthorizationService.isAuthorized(checkMember, object, AuthorizationService.PERMISSION_VIEW, new BasePermissionType[]{AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId())});
			deAuthZ = AuthorizationService.authorize(admin, object, setMember, AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId()), false);
			notAuthZ = AuthorizationService.isAuthorized(checkMember, object, AuthorizationService.PERMISSION_VIEW, new BasePermissionType[]{AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId())});
		} catch (FactoryException | DataAccessException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		assertTrue("Failed to set: " + authZStr,setAuthZ);
		assertTrue("Authorization check failed: " + authZStr,isAuthZ); 
		assertTrue("Failed to deauthorize: " + object.getUrn(),deAuthZ); 
		assertFalse("Succeeded when failured expected: " + authZStr,notAuthZ); 
	}
	
}
