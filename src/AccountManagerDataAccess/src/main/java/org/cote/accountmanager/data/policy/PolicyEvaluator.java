/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.data.policy;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.operation.IOperation;
import org.cote.accountmanager.data.operation.OperationUtil;
import org.cote.accountmanager.data.rule.RuleUtil;
//import org.cote.accountmanager.data.services.BshService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.ScriptService;
import org.cote.accountmanager.data.sod.SoDPolicyUtil;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AttributeType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseEnumType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
public class PolicyEvaluator {
	private static DatatypeFactory dtFactory = null;
	public static final Logger logger = LogManager.getLogger(PolicyEvaluator.class);

	static{
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	private PolicyEvaluator(){
		
	}
	public static PolicyResponseType evaluatePolicyRequest(PolicyRequestType prt) throws FactoryException, ArgumentException{
		PolicyResponseType prr = new PolicyResponseType();
		if(prt.getUrn() == null){
			logger.error("Policy Request Urn is null");
			prr.setResponse(PolicyResponseEnumType.INVALID_ARGUMENT);
		}

		logger.debug("Evaluating Policy Request " + prt.getUrn() + " in Organization " + prt.getOrganizationPath());
		PolicyType pol = getPolicyFromRequest(prt);
		
		prr.setUrn(prt.getUrn());
		if(pol == null){
			logger.error("Failed to retrieve policy from urn '" + prt.getUrn() + "'");
			prr.setResponse(PolicyResponseEnumType.INVALID_ARGUMENT);
			return prr;
		}

		GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
		cal.add(GregorianCalendar.MILLISECOND, pol.getDecisionAge().intValue());
		prr.setExpiresDate(dtFactory.newXMLGregorianCalendar(cal));
		
		List<FactType> facts = prt.getFacts();
		if(!pol.getEnabled()){
			prr.setResponse(PolicyResponseEnumType.DISABLED);
			prr.setMessage("Policy is disabled");
			logger.error("Policy is disabled");
		}
		else{
			evaluatePolicy(pol,facts,prt,prr);
		}
		
		return prr;
		
	}
	
	private static PolicyType getPolicyFromRequest(PolicyRequestType prt) throws FactoryException, ArgumentException{
		OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(prt.getOrganizationPath());
		if(org == null){
			logger.error("Organization not found for path " + prt.getOrganizationPath());
			return null;
		}
		PolicyType pol = Factories.getNameIdFactory(FactoryEnumType.POLICY).getByUrn(prt.getUrn());
		if(pol == null){
			logger.error("Policy not found for urn " + prt.getUrn() + " in org " + prt.getOrganizationPath());
			return null;
		}
		Factories.getNameIdFactory(FactoryEnumType.POLICY).populate(pol);
		return pol;

	}

	private static void evaluatePolicy(PolicyType pol, List<FactType> facts, PolicyRequestType prt, PolicyResponseType prr) throws FactoryException, ArgumentException{
		List<RuleType> rules = pol.getRules();
		int pass = 0;
		int size = rules.size();
		logger.debug("Evaluating Policy " + pol.getUrn() + " " + pol.getCondition().toString());
		for(int i = 0; i < size;i++){
			RuleType rule = rules.get(i);
			Factories.getNameIdFactory(FactoryEnumType.RULE).populate(rule);
			if(evaluateRule(rule, facts,prt, prr)){
				pass++;
				if(pol.getCondition() == ConditionEnumType.ANY){
					logger.debug("Breaking on Policy Condition " + pol.getCondition());
					break;
				}
			}
		}
		boolean success = (
				(pol.getCondition() == ConditionEnumType.ANY && pass > 0)
				||
				(pol.getCondition() == ConditionEnumType.ALL && pass > 0 && pass == size)
				||
				(pol.getCondition() == ConditionEnumType.NONE && pass == 0)
			);
		//logger.info("TODO: Policy needs a condition type like rules have");
		if(success){
			prr.setScore(prr.getScore() + pol.getScore());
			prr.setResponse(PolicyResponseEnumType.PERMIT);
		}
		else prr.setResponse(PolicyResponseEnumType.DENY);
		logger.info("Policy " + pol.getUrn() + ": " + prr.getResponse().toString());
	}
	private static boolean evaluateRule(RuleType rule, List<FactType> facts, PolicyRequestType prt, PolicyResponseType prr) throws FactoryException, ArgumentException{
		logger.debug("Evaluating Rule " + rule.getUrn() + " " + rule.getRuleType().toString() + " " + rule.getCondition().toString());
		int pass = 0;

		List<PatternType> patterns = rule.getPatterns();
		List<RuleType> rules = rule.getRules();
		int size = (patterns.size() + rules.size());

		for(int i = 0; i < rules.size(); i++){
			RuleType crule = rules.get(i);
			Factories.getNameIdFactory(FactoryEnumType.RULE).populate(crule);
			boolean bRule = evaluateRule(crule, facts, prt, prr);
			if(bRule){
				pass++;
				if(rule.getCondition() == ConditionEnumType.ANY){
					logger.info("Breaking on " + crule.getUrn() + " with rule " + rule.getRuleType() + " " + rule.getCondition());
					break;
				}
			}
			else if(rule.getCondition() == ConditionEnumType.ALL && !bRule){
				logger.info("Breaking on " + crule.getUrn() + " with rule " + rule.getRuleType() + " " + rule.getCondition() + " failure");
				break;
				
			
			}
		}
		for(int i = 0; i < patterns.size(); i++){
			PatternType pat = patterns.get(i);
			Factories.getNameIdFactory(FactoryEnumType.PATTERN).populate(pat);
			boolean bPat = evaluatePattern(pat,facts,prt,prr);
			
			if(
				/// 2014/12/08 - moving deny check to the bottom of the method because it should be the inverse of the rule return
				/// and not tied to the pattern result
				///
				bPat
			){
			
				/// Success Condition
				/// If the compare operation is any, then break here
				pass++;
				if(rule.getCondition() == ConditionEnumType.ANY){
					logger.info("Breaking on " + pat.getUrn() + " with rule " + rule.getRuleType() + " " + rule.getCondition());
					break;
				}
			}
			else if(rule.getCondition() == ConditionEnumType.ALL && !bPat){
				logger.info("Breaking on " + pat.getUrn() + " with rule " + rule.getRuleType() + " " + rule.getCondition() + " failure");
				break;
				
			}
		}
		logger.debug("Rule Result: " + rule.getCondition().toString() + " " + pass + ":" + size);
		boolean success = (
				(rule.getCondition() == ConditionEnumType.ANY && pass > 0)
				||
				(rule.getCondition() == ConditionEnumType.ALL && pass > 0 && pass == size)
				||
				(rule.getCondition() == ConditionEnumType.NONE && pass == 0)
			);
		
		if(rule.getRuleType() == RuleEnumType.DENY){
			logger.info("Inverting rule success for DENY condition");
			success = (!success);
		}
		

		if(success){
			prr.setScore(prr.getScore() + rule.getScore());
		}
		return success;
	}
	private static boolean evaluatePattern(PatternType pattern, List<FactType> facts,PolicyRequestType prt,PolicyResponseType prr) throws ArgumentException, NumberFormatException, FactoryException{
		logger.debug("Evaluating Pattern " + pattern.getUrn() + " " + pattern.getPatternType().toString());
		FactType fact = pattern.getFact();
		FactType mfact = pattern.getMatch();
		FactType pfact = fact;
		OperationResponseEnumType opr = OperationResponseEnumType.UNKNOWN;
		boolean outBool = false;
		if(fact == null){
			throw new ArgumentException("Pattern fact is null");
		}
		if(mfact == null){
			throw new ArgumentException("Match fact is null");
		}
		pfact = getFactParameter(pfact,facts);

		/// Operation - fork processing over to a custom-defined class or function
		///
		prr.getPatternChain().add(pattern.getUrn());
		
		/// this allows parameters to be passed in through a pattern bucket
		///
		if(pattern.getPatternType() == PatternEnumType.PARAMETER){
			opr = OperationResponseEnumType.SUCCEEDED;
		}
		else if(pattern.getPatternType() == PatternEnumType.OPERATION){
			opr = evaluateOperation(prt,prr,pattern, pfact,mfact,pattern.getOperationUrn());
		}
		/// Expression - simple in-line expression/comparison
		else if(pattern.getPatternType() == PatternEnumType.EXPRESSION){
			opr = evaluateExpression(prt,prr,pattern, pfact,mfact);
		}
		else if(pattern.getPatternType() == PatternEnumType.AUTHORIZATION){
			opr = evaluateAuthorization(prt,prr,pattern, pfact, mfact);
		}
		else if(pattern.getPatternType() == PatternEnumType.SEPARATION_OF_DUTY){
			opr = evaluateSoD(prt,prr,pattern, pfact, mfact);
		}
		else if(mfact.getFactType() == FactEnumType.OPERATION){
			opr = evaluateOperation(prt,prr,pattern, pfact,mfact,mfact.getSourceUrl());
		}
		else if(mfact.getFactType() == FactEnumType.FUNCTION) {
			opr = evaluateForFact(prt, prr, pattern, pfact, mfact);
		}
		
		else{
			logger.error("Pattern type not supported: " + pattern.getPatternType());
		}

		if(opr == OperationResponseEnumType.SUCCEEDED){
			outBool = true;
			prr.setScore(prr.getScore() + pattern.getScore());
		}

		return outBool;
	}
	private static OperationResponseEnumType evaluateForFact(PolicyRequestType prt,PolicyResponseType prr, PatternType pattern, FactType fact, FactType matchFact) throws ArgumentException{
		OperationResponseEnumType outResponse;
		
		//String chkData = FactUtil.getFactValue(prt, prr, fact, matchFact);
		String mData = FactUtil.getMatchFactValue(prt, prr,fact, matchFact);

		if(mData != null) {
			AttributeType attr = new AttributeType();
			attr.setName(matchFact.getName());
			attr.getValues().add(mData);
			prr.getAttributes().add(attr);
			outResponse = OperationResponseEnumType.SUCCEEDED;
			logger.info("Received: '" + mData + "'");
		}
		else{
			logger.error("No value returned from the function");
			outResponse = OperationResponseEnumType.FAILED;
		}
		return outResponse;
	}
	private static OperationResponseEnumType evaluateExpression(PolicyRequestType prt,PolicyResponseType prr, PatternType pattern, FactType fact, FactType matchFact) throws ArgumentException{
		OperationResponseEnumType outResponse;
		
		String chkData = FactUtil.getFactValue(prt, prr, fact, matchFact);
		String mData = FactUtil.getMatchFactValue(prt, prr,fact, matchFact);

		if(RuleUtil.compareValue(chkData, pattern.getComparator(), mData)) outResponse = OperationResponseEnumType.SUCCEEDED;
		else outResponse = OperationResponseEnumType.FAILED;
		return outResponse;
	}
	private static OperationResponseEnumType evaluateSoD(PolicyRequestType prt,PolicyResponseType prr, PatternType pattern, FactType fact, FactType matchFact) throws ArgumentException{
		OperationResponseEnumType outResponse;
		NameIdType p = FactUtil.factoryRead(fact, matchFact);
		NameIdType g = FactUtil.factoryRead(matchFact, matchFact);
		if(p == null || g == null){
			logger.error("The " + (g == null ? "match ":"") + "fact reference " + (g == null ? matchFact.getUrn() : fact.getUrn()) + " was null");
			return OperationResponseEnumType.ERROR;
		}

		if(p.getNameType() != NameEnumType.ACCOUNT && p.getNameType() != NameEnumType.PERSON){
			logger.error("Source fact of account or person is expected");
			return OperationResponseEnumType.ERROR;
		}
		if(g.getNameType() != NameEnumType.GROUP){
			logger.error("Match fact of group is expected");
			return OperationResponseEnumType.ERROR;			
		}
		List<Long> perms = SoDPolicyUtil.getActivityPermissionsForType(g.getUrn(), p);

		if(!perms.isEmpty()) outResponse = OperationResponseEnumType.SUCCEEDED;
		else outResponse = OperationResponseEnumType.FAILED;
		return outResponse;
	}	
	private static OperationResponseEnumType evaluateAuthorization(PolicyRequestType prt,PolicyResponseType prr, PatternType pattern, FactType fact, FactType matchFact) throws ArgumentException, NumberFormatException, FactoryException{
		OperationResponseEnumType outResponse = OperationResponseEnumType.UNKNOWN;
		if(fact.getFactoryType() == FactoryEnumType.UNKNOWN || matchFact.getFactoryType() == FactoryEnumType.UNKNOWN){
			logger.error("Expected both fact and match fact to define a factory type");
			return OperationResponseEnumType.ERROR;
		}
		NameIdType p = FactUtil.factoryRead(fact, matchFact);
		NameIdType g = FactUtil.factoryRead(matchFact, matchFact);
		if(p == null || g == null){
			logger.error("The " + (g == null ? "match ":"") + "fact reference " + (g == null ? matchFact.getUrn() : fact.getUrn()) + " was null");
			return OperationResponseEnumType.ERROR;
		}
		if(matchFact.getFactType() == FactEnumType.PERMISSION){
			
			BasePermissionType perm = null;
			if(matchFact.getFactoryType() == FactoryEnumType.PERMISSION) perm = (BasePermissionType)g;
			else if(FactUtil.idPattern.matcher(matchFact.getFactData()).matches()) perm = Factories.getNameIdFactory(FactoryEnumType.PERMISSION).getById(Long.parseLong(matchFact.getFactData()), matchFact.getOrganizationId());
			else perm = Factories.getNameIdFactory(FactoryEnumType.PERMISSION).getByUrn(matchFact.getFactData());
			if(perm == null){
				logger.error("Permission reference does not exist");
				return OperationResponseEnumType.ERROR;
			}
			outResponse = evaluatePermissionAuthorization(prt,prr,pattern, p, g, perm);
		}
		else if(matchFact.getFactType() == FactEnumType.ROLE && matchFact.getFactoryType() == FactoryEnumType.ROLE){
			outResponse = evaluateRoleAuthorization(prt,prr,pattern, p, g, (BaseRoleType)g);
		}

		return outResponse;
	}
	private static OperationResponseEnumType evaluateRoleAuthorization(PolicyRequestType prt,PolicyResponseType prr, PatternType pattern, NameIdType src, NameIdType targ, BaseRoleType role) throws ArgumentException, FactoryException{
		OperationResponseEnumType outResponse = OperationResponseEnumType.UNKNOWN;
		boolean authZ = false;
		if(targ.getNameType() == NameEnumType.ROLE){
			switch(src.getNameType()){
				case USER:
				case PERSON:
				case ACCOUNT:
					authZ = EffectiveAuthorizationService.getIsActorInEffectiveRole(role, src);
					break;
				default:
					logger.error("Unexpected source type: " + src.getNameType());
					break;

			}
		}
		if(authZ){
			outResponse = OperationResponseEnumType.SUCCEEDED;
		}
		return outResponse;
	}
	private static OperationResponseEnumType evaluatePermissionAuthorization(PolicyRequestType prt,PolicyResponseType prr, PatternType pattern, NameIdType src, NameIdType targ, BasePermissionType permission) throws ArgumentException, FactoryException{
		OperationResponseEnumType outResponse = OperationResponseEnumType.UNKNOWN;
		boolean authZ = false;
		logger.info("Evaluating Permission Authorization on " + targ.getNameType() + " (#" + targ.getId() + ") for " + src.getNameType() + " (#" + src.getId() + ") having " + permission.getName() + " (#" + permission.getId() + ")");
		if(targ.getNameType() == NameEnumType.GROUP){

			BaseGroupType group = (BaseGroupType)targ;
			switch(src.getNameType()){
				case USER:
				case PERSON:
				case ROLE:
				case ACCOUNT:
					authZ = EffectiveAuthorizationService.getAuthorization(src, group,  new BasePermissionType[] {permission});
					break;
				default:
					logger.error("Unexpected source type: " + src.getNameType());
					break;
			}
		}
		else if(targ.getNameType() == NameEnumType.DATA){
			DataType data = (DataType)targ;
			switch(src.getNameType()){
				case USER:
				case PERSON:
				case ROLE:
				case ACCOUNT:
					authZ = EffectiveAuthorizationService.getAuthorization(src, data,  new BasePermissionType[] {permission});
					break;
				default:
					logger.error("Unexpected source type: " + src.getNameType());
					break;
			}
		}
		else if(targ.getNameType() == NameEnumType.ROLE){
			BaseRoleType role = (BaseRoleType)targ;
			switch(src.getNameType()){
				case USER:
				case PERSON:
				case ROLE:
				case ACCOUNT:
					authZ = EffectiveAuthorizationService.getAuthorization(src, role,  new BasePermissionType[] {permission});
					break;
				default:
					logger.error("Unexpected source type: " + src.getNameType());
					break;
			}
		}
		if(authZ){
			outResponse = OperationResponseEnumType.SUCCEEDED;
		}
		return outResponse;
	}

	private static OperationResponseEnumType evaluateOperation(PolicyRequestType prt,PolicyResponseType prr, PatternType pattern, FactType fact, FactType matchFact, String operation) throws ArgumentException, FactoryException{
		OperationResponseEnumType outResponse = OperationResponseEnumType.UNKNOWN;
		logger.debug("Evaluating Operation: " + operation);
		OperationType op = Factories.getNameIdFactory(FactoryEnumType.OPERATION).getByUrn(operation);
		if(op == null){
			throw new ArgumentException("Operation is null");
		}
		switch(op.getOperationType()){
			case INTERNAL:
				IOperation oper = OperationUtil.getOperationInstance(op.getOperation());
				if(oper == null) outResponse = OperationResponseEnumType.ERROR;
				else outResponse = oper.operate(prt, prr, pattern, fact, matchFact);
				
				break;
			case FUNCTION:
				//logger.error("NEED TO REFACTOR. THIS IS ONLY AN INITIAL STUB");
				FunctionType func = Factories.getNameIdFactory(FactoryEnumType.FUNCTION).getByUrn(op.getOperation());
				if(func == null){
					throw new ArgumentException("Operation Function '" + op.getOperation() + "' is null");
				}
				Map<String,Object> params = ScriptService.getCommonParameterMap(null);
				params.put("pattern", pattern);
				params.put("fact", fact);
				params.put("match", matchFact);
				// Object resp = BshService.run(null, params, func);
				outResponse = ScriptService.run(OperationResponseEnumType.class, params, func);
				/*
				if(resp == null){
					logger.error("Script did not return an OperationResponseEnumType value");
					outResponse = OperationResponseEnumType.ERROR;
				}
				else{
					//outResponse = (OperationResponseEnumType)resp;
					outResponse = ((org.graalvm.polyglot.Value)resp).as(OperationResponseEnumType.class);
				}
				*/
				break;
			default:
				logger.error("Unhandled operation type: " + op.getOperationType());
		}
		return outResponse;
	}
	private static FactType getFactParameter(FactType fact, List<FactType> facts){
		FactType ofact = fact;
		FactType mfact = null;
		for(int i = 0; i < facts.size();i++){
			mfact = facts.get(i);
			if(
				fact.getFactType() == FactEnumType.PARAMETER && mfact.getFactType() == FactEnumType.PARAMETER &&
			mfact.getUrn().equals(fact.getUrn())){
				ofact = mfact;
				break;
			}
		}
		return ofact;
	}
	
	
}
