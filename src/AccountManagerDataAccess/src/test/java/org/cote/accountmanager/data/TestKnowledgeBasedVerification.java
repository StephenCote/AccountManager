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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.FactFactory;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.FunctionFactFactory;
import org.cote.accountmanager.data.factory.FunctionFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.OperationFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.PatternFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RuleFactory;
import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.ApplicationPermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FunctionEnumType;
import org.cote.accountmanager.objects.FunctionFactType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationEnumType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
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
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.Test;
public class TestKnowledgeBasedVerification extends BaseDataAccessTest{
	
	private RuleType getCreateKBVRule(UserType user, String question, String answer){

		
		String questionHash = SecurityUtil.getDigestAsString(question.getBytes());
		String name = "Question - " + questionHash;
		String answerName = "Answer - " + questionHash;
		DataType questionData = this.getCreateProtectedData(user, name, question.getBytes(), this.getCreatePath(user, "~/Data"));
		DataType answerData = this.getCreateProtectedData(user, answerName, answer.getBytes(), this.getCreatePath(user, "~/Data"));
		RuleType rule = null;
		try{
		
			FactType questionFact = this.getCreateParameterFact(user, name, this.getCreatePath(user, "~/Facts"));
			questionFact.setFactoryType(FactoryEnumType.DATA);
			//questionFact.setFactReference(questionData);
			questionFact.setSourceUrn(questionData.getUrn());
			
			((INameIdFactory)Factories.getFactory(FactoryEnumType.FACT)).update(questionFact);
			
			FactType answerFact = this.getCreateParameterFact(user, answerName, this.getCreatePath(user, "~/Facts"));
			answerFact.setFactoryType(FactoryEnumType.DATA);
			//answerFact.setFactReference(answerData);
			answerFact.setSourceUrn(answerData.getUrn());
			answerFact.setFactType(FactEnumType.FACTORY);
			((INameIdFactory)Factories.getFactory(FactoryEnumType.FACT)).update(answerFact);
			
			PatternType pattern = this.getCreatePattern(user, name, questionFact.getUrn(), answerFact.getUrn(), this.getCreatePath(user, "~/Patterns"));
			pattern.setPatternType(PatternEnumType.VERIFICATION);
			((INameIdFactory)Factories.getFactory(FactoryEnumType.PATTERN)).update(pattern);
			rule = this.getCreateRule(user, name, RuleEnumType.PERMIT, this.getCreatePath(user, "~/Rules"), new PatternType[]{pattern});
		}
		catch(FactoryException | ArgumentException e){
			logger.error(e);
		}
	
		return rule;
	}
	
	@Test
	public void TestPolicySetup(){
		PolicyType policy = this.getCreatePolicy(testUser, "Test KBV 1", this.getCreatePath(testUser, "~/Policies"));
		RuleType rule1 = this.getCreateRule(testUser, "KBV Set 1", this.getCreatePath(testUser, "~/Rules"));
		rule1.setCondition(ConditionEnumType.GREATER_THAN_OR_EQUALS);
		rule1.setScore(3);
		RuleType ruleKBV1 = getCreateKBVRule(testUser, "What is the answer?","None!");
		RuleType ruleKBV2 = getCreateKBVRule(testUser, "What is the second answer?","Neither!");
		RuleType ruleKBV3 = getCreateKBVRule(testUser, "What is the third answer?","Nothing!");

		assertNotNull("KBV Rule is null", ruleKBV1);
		
		boolean updated = false;
		try {
			((INameIdFactory)Factories.getFactory(FactoryEnumType.RULE)).populate(rule1);
			rule1.getRules().clear();
			rule1.getRules().add(ruleKBV1);
			rule1.getRules().add(ruleKBV2);
			rule1.getRules().add(ruleKBV3);
			updated = ((INameIdFactory)Factories.getFactory(FactoryEnumType.RULE)).update(rule1);
			assertTrue("Failed to update", updated);
			((INameIdFactory)Factories.getFactory(FactoryEnumType.POLICY)).populate(policy);
			policy.getRules().clear();
			policy.getRules().add(rule1);
			updated = ((INameIdFactory)Factories.getFactory(FactoryEnumType.POLICY)).update(policy);
			//logger.info(PolicyDefinitionUtil.printPolicy(policy));
			
			PolicyDefinitionType pdt = PolicyDefinitionUtil.generatePolicyDefinition(policy);
			logger.info(JSONUtil.exportObject(pdt));
			PolicyRequestType prt = PolicyDefinitionUtil.generatePolicyRequest(pdt);
			logger.info(JSONUtil.exportObject(prt));
		}
		catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		assertTrue("Failed to update", updated);
		logger.info("Configured test rule");
		

		


	}

}
