package org.cote.accountmanager.data.policy;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.data.operation.IOperation;
import org.cote.accountmanager.data.operation.OperationUtil;
import org.cote.accountmanager.data.rule.RuleUtil;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
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

public class PolicyEvaluator {
	private static DatatypeFactory dtFactory = null;
	static{
		try {
			dtFactory = DatatypeFactory.newInstance();
		} catch (DatatypeConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
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
	public static PolicyResponseType evaluatePolicyRequest(PolicyRequestType prt) throws FactoryException, ArgumentException{
		logger.info("Evaluating Policy " + prt.getUrn() + " in Organization " + prt.getOrganizationPath());
		PolicyType pol = getPolicyFromRequest(prt);
		PolicyResponseType prr = new PolicyResponseType();
		prr.setUrn(prt.getUrn());
		if(pol == null){
			prr.setResponse(PolicyResponseEnumType.INVALID_ARGUMENT);
			return prr;
		}

		GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
		cal.add(GregorianCalendar.MILLISECOND, pol.getDecisionAge().intValue());
		prr.setExpires(dtFactory.newXMLGregorianCalendar(cal));
		
		List<FactType> facts = prt.getFacts();
		if(pol.getEnabled() == false){
			prr.setResponse(PolicyResponseEnumType.DISABLED);
			prr.setMessage("Policy is disabled");
		}
		else{
			evaluatePolicy(pol,facts,prr);
		}
		
		return prr;
		
	}
	public static void evaluatePolicy(PolicyType pol, List<FactType> facts, PolicyResponseType prr) throws FactoryException, ArgumentException{
		List<RuleType> rules = pol.getRules();
		int pass = 0;
		int size = rules.size();
		for(int i = 0; i < size;i++){
			RuleType rule = rules.get(i);
			Factories.getRuleFactory().populate(rule);
			if(evaluateRule(rule, facts,prr)){
				pass++;
			}
		}
		if(pass == size) prr.setResponse(PolicyResponseEnumType.PERMIT);
		else prr.setResponse(PolicyResponseEnumType.DENY);
	}
	public static boolean evaluateRule(RuleType rule, List<FactType> facts, PolicyResponseType prr) throws FactoryException, ArgumentException{
		List<PatternType> patterns = rule.getPatterns();
		int pass = 0;
		int size = patterns.size();
		for(int i = 0; i < patterns.size(); i++){
			PatternType pat = patterns.get(i);
			Factories.getPatternFactory().populate(pat);
			boolean bPat = evaluatePattern(pat,facts,prr);
			if(
				(rule.getRuleType() == RuleEnumType.PERMIT && bPat)
				||
				(rule.getRuleType() == RuleEnumType.DENY && !bPat)
			) pass++;
		}
		if(pass == size) prr.setScore(prr.getScore() + rule.getScore());
		return (pass == size);
	}
	public static boolean evaluatePattern(PatternType pattern, List<FactType> facts,PolicyResponseType prr) throws ArgumentException{
		FactType fact = pattern.getFact();
		FactType mfact = pattern.getMatch();
		FactType pfact = fact;
		OperationResponseEnumType opr = OperationResponseEnumType.UNKNOWN;
		boolean out_bool = false;
		if(fact == null){
			throw new ArgumentException("Pattern fact is null");
		}
		if(mfact == null){
			throw new ArgumentException("Match fact is null");
		}
		pfact = getFactParameter(pfact,facts);
		/*
		if(pfact.getFactType() == FactEnumType.PARAMETER){
			pfact = getFactParameter(pfact,facts);
			if(pfact == null){
				throw new ArgumentException("Parameter " + fact.getUrn() + " fact is null");
			}
		}
		*/
		/// Operation - fork processing over to a custom-defined class or function
		///
		prr.getPatternChain().add(pattern.getUrn());
		if(pattern.getPatternType() == PatternEnumType.OPERATION){
			opr = evaluateOperation(pattern, pfact,mfact,pattern.getOperationUrn());
		}
		/// Expression - simple in-line expression/comparison
		else if(pattern.getPatternType() == PatternEnumType.EXPRESSION){
			opr = evaluateExpression(pattern, pfact,mfact);
		}
		else if(mfact.getFactType() == FactEnumType.OPERATION){
			opr = evaluateOperation(pattern, pfact,mfact,mfact.getSourceUrl());
		}

		if(opr == OperationResponseEnumType.SUCCEEDED){
			out_bool = true;
			prr.setScore(prr.getScore() + pattern.getScore());
		}

		return out_bool;
	}
	public static OperationResponseEnumType evaluateExpression(PatternType pattern, FactType fact, FactType matchFact) throws ArgumentException{
		OperationResponseEnumType out_response = OperationResponseEnumType.UNKNOWN;
		
		String chkData = FactUtil.getFactValue(fact, matchFact);
		String mData = FactUtil.getMatchFactValue(fact, matchFact);

		if(RuleUtil.compareValue(chkData, pattern.getComparator(), mData)) out_response = OperationResponseEnumType.SUCCEEDED;
		else out_response = OperationResponseEnumType.FAILED;
		return out_response;
	}
	public static OperationResponseEnumType evaluateOperation(PatternType pattern, FactType fact, FactType matchFact, String operation) throws ArgumentException{
		OperationResponseEnumType out_response = OperationResponseEnumType.UNKNOWN;
		logger.info("Evaluating operation: " + operation);
		OperationType op = Factories.getOperationFactory().getByUrn(operation, pattern.getOrganization());
		if(op == null){
			throw new ArgumentException("Operation is null");
		}
		switch(op.getOperationType()){
			case INTERNAL:
				IOperation oper = OperationUtil.getOperationInstance(op.getOperation());
				if(oper == null) out_response = OperationResponseEnumType.ERROR;
				else out_response = oper.operate(pattern, fact, matchFact);
				break;
			default:
				logger.error("Unhandled operation type: " + op.getOperationType());
		}
		return out_response;
	}
	public static FactType getFactParameter(FactType fact, List<FactType> facts){
		FactType ofact = fact;
		FactType mfact = null;
		for(int i = 0; i < facts.size();i++){
			mfact = facts.get(i);
			if(
				fact.getFactType() == FactEnumType.PARAMETER && mfact.getFactType() == FactEnumType.PARAMETER &&
			//fact.getFactType() == mfact.getFactType()
			//&&
			mfact.getUrn().equals(fact.getUrn())){
				ofact = mfact;
				break;
			}
		}
		return ofact;
	}
	
	
}
