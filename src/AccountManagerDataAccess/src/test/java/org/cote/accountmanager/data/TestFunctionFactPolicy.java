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

import java.io.BufferedInputStream;
import java.io.IOException;

import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FunctionEnumType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.StreamUtil;
import org.junit.Test;

public class TestFunctionFactPolicy extends BaseDataAccessTest {

	@Test
	public void TestPolicyWithFunctionFact(){
		String scriptStr = null;
		try {
			scriptStr = StreamUtil.streamToString(new BufferedInputStream(ClassLoader.getSystemResourceAsStream("./testFunctionServiceAccess.js")));
		} catch (IOException e) {
			logger.error(e);
		} 
		assertTrue("Script is empty or null", scriptStr != null && scriptStr.length() > 0);
		;
		PolicyType policy = null;
		PolicyDefinitionType policyDef = null;
		PolicyRequestType policyReq = null;
		PolicyResponseType policyResp = null;

		try{
			DataType functionData = this.getCreateTextData(testUser, "Test Fact Function", scriptStr, this.getCreatePath(testUser, "~/Data"));
			FunctionType func = this.getCreateFunction(testUser, "Test Static Function", FunctionEnumType.JAVASCRIPT, functionData, this.getCreatePath(testUser, "~/Functions"));

			FactType paramFact = this.getCreateParameterFact(testUser,"Test Function Argument", this.getCreatePath(testUser, "~/Facts"));
			FactType functionFact = this.getCreateFunctionFact(testUser,"Test Function Fact", func, this.getCreatePath(testUser, "~/Facts"));
			PatternType pattern = this.getCreatePattern(testUser, "Test Parameter for Test Function", paramFact.getUrn(), functionFact.getUrn(), this.getCreatePath(testUser, "~/Patterns"));
			RuleType rule = this.getCreateRule(testUser, "Test Parameter Supplied To Fuction Is True", RuleEnumType.PERMIT, this.getCreatePath(testUser,"~/Rules"),new PatternType[]{pattern});
			policy = this.getCreatePolicy(testUser, "Test Policy With Function Fact", this.getCreatePath(testUser,"~/Policies"), new RuleType[]{rule});
			policyDef = PolicyDefinitionUtil.generatePolicyDefinition(policy);
			policyReq = PolicyDefinitionUtil.generatePolicyRequest(policyDef);
			policyReq.setContextUser(testUser);
			policyReq.setSubject(testUser2.getUrn());
			policyReq.setSubjectType(FactoryEnumType.USER);
			policyReq.getFacts().get(0).setFactData("3");

			policyResp = PolicyEvaluator.evaluatePolicyRequest(policyReq);
		}
		catch(ClassCastException | FactoryException | ArgumentException e){
			logger.error(e);
			e.printStackTrace();
		}
		assertNotNull("Policy is null",policy);
		assertNotNull("Policy definition is null", policyDef);
		assertNotNull("Policy request is null",policyReq);
		assertNotNull("Policy response is null", policyResp);
		//logger.info(JSONUtil.exportObject(policyReq));
		logger.info(JSONUtil.exportObject(policyResp));
		
	}
	
	
}
