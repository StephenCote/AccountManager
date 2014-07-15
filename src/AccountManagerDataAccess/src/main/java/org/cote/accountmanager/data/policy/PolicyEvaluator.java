package org.cote.accountmanager.data.policy;

import java.util.List;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.operation.IOperation;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseEnumType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleType;

public class PolicyEvaluator {
	public static final Logger logger = Logger.getLogger(PolicyEvaluator.class.getName());
	public static PolicyType getPolicyFromRequest(PolicyRequestType prt) throws FactoryException, ArgumentException{
		OrganizationType org = Factories.getOrganizationFactory().findOrganization(prt.getOrganizationPath());
		if(org == null){
			logger.error("Organization not found for path " + prt.getOrganizationPath());
			return null;
		}
		PolicyType pol = Factories.getPolicyFactory().getByUrn(prt.getUrn(), org);
		if(pol == null){
			logger.error("Policy not found for urn " + prt.getUrn() + " in org " + prt.getOrganizationPath());
			return null;
		}
		Factories.getPolicyFactory().populate(pol);
		return pol;

	}
	public static PolicyResponseEnumType evaluatePolicyRequest(PolicyRequestType prt) throws FactoryException, ArgumentException{
		logger.info("Evaluating Policy " + prt.getUrn() + " in Organization " + prt.getOrganizationPath());
		PolicyType pol = getPolicyFromRequest(prt);
		if(pol == null){
			return PolicyResponseEnumType.INVALID_ARGUMENT;
		}
		List<FactType> facts = prt.getFacts();
		return evaluatePolicy(pol,facts);
	}
	public static PolicyResponseEnumType evaluatePolicy(PolicyType pol, List<FactType> facts) throws FactoryException, ArgumentException{
		List<RuleType> rules = pol.getRules();
		for(int i = 0; i < rules.size();i++){
			RuleType rule = rules.get(i);
			Factories.getRuleFactory().populate(rule);
			evaluateRule(rule, facts);
		}
		return PolicyResponseEnumType.UNKNOWN;
	}
	public static boolean evaluateRule(RuleType rule, List<FactType> facts) throws FactoryException, ArgumentException{
		List<PatternType> patterns = rule.getPatterns();
		for(int i = 0; i < patterns.size(); i++){
			PatternType pat = patterns.get(i);
			Factories.getPatternFactory().populate(pat);
			evaluatePattern(pat,facts);
		}
		return false;
	}
	public static boolean evaluatePattern(PatternType pattern, List<FactType> facts) throws ArgumentException{
		FactType fact = pattern.getFact();
		FactType mfact = pattern.getMatch();
		FactType pfact = fact;
		if(fact == null){
			throw new ArgumentException("Pattern fact is null");
		}
		if(mfact == null){
			throw new ArgumentException("Match fact is null");
		}
		if(pfact.getFactType() == FactEnumType.PARAMETER){
			pfact = getFactParameter(pfact,facts);
			if(pfact == null){
				throw new ArgumentException("Parameter " + fact.getUrn() + " fact is null");
			}
		}
		if(mfact.getFactType() == FactEnumType.OPERATION){
			OperationResponseEnumType opr = evaluateOperation(pattern, pfact,mfact);
			if(opr == OperationResponseEnumType.SUCCEEDED) return true;
		}
		return false;
	}
	public static OperationResponseEnumType evaluateOperation(PatternType pattern, FactType fact, FactType operationFact) throws ArgumentException{
		OperationResponseEnumType out_response = OperationResponseEnumType.UNKNOWN;
		logger.info("Evaluating operation: " + operationFact.getSourceUrl());
		OperationType op = Factories.getOperationFactory().getByUrn(operationFact.getSourceUrl(), operationFact.getOrganization());
		if(op == null){
			throw new ArgumentException("Operation is null");
		}
		switch(op.getOperationType()){
			case INTERNAL:
				try {
					Class cls = Class.forName(op.getOperation());
					IOperation oper = (IOperation)cls.newInstance();
					out_response = oper.operate(pattern, fact, operationFact);
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				break;
			default:
				logger.error("Unhandled operation type: " + op.getOperationType());
		}
		return out_response;
	}
	public static FactType getFactParameter(FactType fact, List<FactType> facts){
		FactType ofact = null;
		FactType mfact = null;
		for(int i = 0; i < facts.size();i++){
			mfact = facts.get(i);
			if(fact.getFactType() == FactEnumType.PARAMETER && mfact.getFactType() == FactEnumType.PARAMETER && mfact.getUrn().equals(fact.getUrn())){
				ofact = mfact;
				break;
			}
		}
		return ofact;
	}
	
	
}
