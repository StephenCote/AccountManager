package org.cote.accountmanager.data.policy;

import java.util.ArrayList;
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
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.sod.SoDPolicyUtil;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseEnumType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

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
		PolicyType pol = Factories.getPolicyFactory().getByUrn(prt.getUrn());
		if(pol == null){
			logger.error("Policy not found for urn " + prt.getUrn() + " in org " + prt.getOrganizationPath());
			return null;
		}
		Factories.getPolicyFactory().populate(pol);
		return pol;

	}
	public static PolicyResponseType evaluatePolicyRequest(PolicyRequestType prt) throws FactoryException, ArgumentException{
		PolicyResponseType prr = new PolicyResponseType();
		if(prt.getUrn() == null){
			logger.error("Policy Request Urn is null");
			prr.setResponse(PolicyResponseEnumType.INVALID_ARGUMENT);
		}
		
		logger.info("Evaluating Policy Request " + prt.getUrn() + " in Organization " + prt.getOrganizationPath());
		PolicyType pol = getPolicyFromRequest(prt);
		
		prr.setUrn(prt.getUrn());
		if(pol == null){
			prr.setResponse(PolicyResponseEnumType.INVALID_ARGUMENT);
			return prr;
		}

		GregorianCalendar cal = new GregorianCalendar();
	    cal.setTime(new Date());
		cal.add(GregorianCalendar.MILLISECOND, pol.getDecisionAge().intValue());
		prr.setExpiresDate(dtFactory.newXMLGregorianCalendar(cal));
		
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
		logger.info("Evaluating Policy " + pol.getUrn() + " " + pol.getCondition().toString());
		for(int i = 0; i < size;i++){
			RuleType rule = rules.get(i);
			Factories.getRuleFactory().populate(rule);
			if(evaluateRule(rule, facts,prr)){
				pass++;
				if(pol.getCondition() == ConditionEnumType.ANY){
					logger.info("Breaking on Policy Condition " + pol.getCondition());
					break;
				}
			}
		}
		boolean success = (
				(pol.getCondition() == ConditionEnumType.ANY && pass > 0)
				||
				(pol.getCondition() == ConditionEnumType.ALL && pass == size)
				||
				(pol.getCondition() == ConditionEnumType.NONE && pass == 0)
			);
		//logger.info("TODO: Policy needs a condition type like rules have");
		if(success){
			prr.setScore(prr.getScore() + pol.getScore());
			prr.setResponse(PolicyResponseEnumType.PERMIT);
		}
		else prr.setResponse(PolicyResponseEnumType.DENY);
	}
	public static boolean evaluateRule(RuleType rule, List<FactType> facts, PolicyResponseType prr) throws FactoryException, ArgumentException{
		logger.info("Evaluating Rule " + rule.getUrn() + " " + rule.getRuleType().toString() + " " + rule.getCondition().toString());
		int pass = 0;

		List<PatternType> patterns = rule.getPatterns();
		
		int size = patterns.size();
		/*
		List<RuleType> rules = rule.getRules();
		int rsize = rules.size();
		for(int i = 0; i < rsize;i++){
			if(evaluateRule(rules.get(i), facts, prr)){
				pass++;
			}
		}
		*/
		for(int i = 0; i < patterns.size(); i++){
			PatternType pat = patterns.get(i);
			Factories.getPatternFactory().populate(pat);
			boolean bPat = evaluatePattern(pat,facts,prr);
			
			if(
				/// 2014/12/08 - moving deny check to the bottom of the method because it should be the inverse of the rule return
				/// and not tied to the pattern result
				///
				/*
				(rule.getRuleType() == RuleEnumType.PERMIT && bPat)
				||
				(rule.getRuleType() == RuleEnumType.DENY && !bPat)
				*/
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
		logger.info("Rule Result: " + rule.getCondition().toString() + " " + pass + ":" + size);
		boolean success = (
				(rule.getCondition() == ConditionEnumType.ANY && pass > 0)
				||
				(rule.getCondition() == ConditionEnumType.ALL && pass == size)
				||
				(rule.getCondition() == ConditionEnumType.NONE && pass == 0)
			);
		
		if(rule.getRuleType() == RuleEnumType.DENY){
			logger.info("Inverting rule success for DENY condition");
			success = (success == false);
		}
		

		if(success){
			prr.setScore(prr.getScore() + rule.getScore());
		}
		return success;
	}
	public static boolean evaluatePattern(PatternType pattern, List<FactType> facts,PolicyResponseType prr) throws ArgumentException, NumberFormatException, FactoryException{
		logger.info("Evaluating Pattern " + pattern.getUrn() + " " + pattern.getPatternType().toString());
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
		/// this allows parameters to be passed in through a patttern bucket
		///
		if(pattern.getPatternType() == PatternEnumType.PARAMETER){
			opr = OperationResponseEnumType.SUCCEEDED;
		}
		if(pattern.getPatternType() == PatternEnumType.OPERATION){
			opr = evaluateOperation(pattern, pfact,mfact,pattern.getOperationUrn());
		}
		/// Expression - simple in-line expression/comparison
		else if(pattern.getPatternType() == PatternEnumType.EXPRESSION){
			opr = evaluateExpression(pattern, pfact,mfact);
		}
		else if(pattern.getPatternType() == PatternEnumType.AUTHORIZATION){
			opr = evaluateAuthorization(pattern, pfact, mfact);
		}
		else if(pattern.getPatternType() == PatternEnumType.SEPARATION_OF_DUTY){
			opr = evaluateSoD(pattern, pfact, mfact);
		}
		else if(mfact.getFactType() == FactEnumType.OPERATION){
			opr = evaluateOperation(pattern, pfact,mfact,mfact.getSourceUrl());
		}
		
		else{
			logger.error("Pattern type not supported: " + pattern.getPatternType());
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
	public static OperationResponseEnumType evaluateSoD(PatternType pattern, FactType fact, FactType matchFact) throws ArgumentException{
		OperationResponseEnumType out_response = OperationResponseEnumType.UNKNOWN;
		PersonType person = null;
		AccountType account = null;
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
		List<String> perms = new ArrayList<String>();
		if(p.getNameType() == NameEnumType.ACCOUNT){
			perms = SoDPolicyUtil.getActivityPermissionsForAccount(g.getUrn(), p.getUrn());
		}
		if(p.getNameType() == NameEnumType.PERSON){
			perms = SoDPolicyUtil.getActivityPermissionsForPerson(g.getUrn(), p.getUrn());
		}
		if(perms.size() > 0) out_response = OperationResponseEnumType.SUCCEEDED;
		else out_response = OperationResponseEnumType.FAILED;
		return out_response;
	}	
	public static OperationResponseEnumType evaluateAuthorization(PatternType pattern, FactType fact, FactType matchFact) throws ArgumentException, NumberFormatException, FactoryException{
		OperationResponseEnumType out_response = OperationResponseEnumType.UNKNOWN;
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
			BasePermissionType perm = Factories.getPermissionFactory().getPermissionById(Long.parseLong(matchFact.getFactData()), matchFact.getOrganization());
			if(perm == null){
				logger.error("Permission reference does not exist");
				return OperationResponseEnumType.ERROR;
			}
			out_response = evaluatePermissionAuthorization(pattern, p, g, perm);
		}
		else if(matchFact.getFactType() == FactEnumType.ROLE && matchFact.getFactoryType() == FactoryEnumType.ROLE){
			//BaseRoleType role = Factories.getRoleFactory().getRoleById(Long.parseLong(matchFact.getFactData()), matchFact.getOrganization());
			/*
			if(role == null){
				logger.error("Role reference does not exist");
				return OperationResponseEnumType.ERROR;
			}
			*/
			out_response = evaluateRoleAuthorization(pattern, p, g, (BaseRoleType)g);
		}

		//out_response = OperationResponseEnumType.FAILED;
		return out_response;
	}
	public static OperationResponseEnumType evaluateRoleAuthorization(PatternType pattern, NameIdType src, NameIdType targ, BaseRoleType role) throws ArgumentException, FactoryException{
		OperationResponseEnumType out_response = OperationResponseEnumType.UNKNOWN;
		boolean authZ = false;
		if(targ.getNameType() == NameEnumType.ROLE){
			switch(src.getNameType()){
				case USER:
					authZ = EffectiveAuthorizationService.getIsUserInEffectiveRole(role, (UserType)src);
					break;
				case PERSON:
					authZ = EffectiveAuthorizationService.getIsPersonInEffectiveRole(role, (PersonType)src);
					break;
				case ACCOUNT:
					authZ = EffectiveAuthorizationService.getIsAccountInEffectiveRole(role, (AccountType)src);
					break;
				default:
					logger.error("Unexpected source type: " + src.getNameType());
					break;

			}
		}
		if(authZ){
			out_response = OperationResponseEnumType.SUCCEEDED;
		}
		return out_response;
	}
	public static OperationResponseEnumType evaluatePermissionAuthorization(PatternType pattern, NameIdType src, NameIdType targ, BasePermissionType permission) throws ArgumentException, FactoryException{
		OperationResponseEnumType out_response = OperationResponseEnumType.UNKNOWN;
		boolean authZ = false;
		logger.info("Evaluating Permission Authorization on " + targ.getNameType() + " (#" + targ.getId() + ") for " + src.getNameType() + " (#" + src.getId() + ") having " + permission.getName() + " (#" + permission.getId() + ")");
		if(targ.getNameType() == NameEnumType.GROUP){

			BaseGroupType group = (BaseGroupType)targ;
			switch(src.getNameType()){
				case USER:
					authZ = EffectiveAuthorizationService.getGroupAuthorization((UserType)src, group, new BasePermissionType[]{permission});
					break;
				case PERSON:
					authZ = EffectiveAuthorizationService.getGroupAuthorization((PersonType)src, group, new BasePermissionType[]{permission});
					break;
				case ROLE:
					authZ = EffectiveAuthorizationService.getGroupAuthorization((BaseRoleType)src, group, new BasePermissionType[]{permission});
					break;
				case ACCOUNT:
					authZ = EffectiveAuthorizationService.getGroupAuthorization((AccountType)src, group, new BasePermissionType[]{permission});
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
					authZ = EffectiveAuthorizationService.getDataAuthorization((UserType)src, data, new BasePermissionType[]{permission});
					break;
				case PERSON:
					authZ = EffectiveAuthorizationService.getDataAuthorization((PersonType)src, data, new BasePermissionType[]{permission});
					break;
				case ROLE:
					authZ = EffectiveAuthorizationService.getDataAuthorization((BaseRoleType)src, data, new BasePermissionType[]{permission});
					break;
				case ACCOUNT:
					authZ = EffectiveAuthorizationService.getDataAuthorization((AccountType)src, data, new BasePermissionType[]{permission});
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
					authZ = EffectiveAuthorizationService.getRoleAuthorization((UserType)src, role, new BasePermissionType[]{permission});
					break;
				case PERSON:
					authZ = EffectiveAuthorizationService.getRoleAuthorization((PersonType)src, role, new BasePermissionType[]{permission});
					break;
				case ROLE:
					authZ = EffectiveAuthorizationService.getRoleAuthorization((BaseRoleType)src, role, new BasePermissionType[]{permission});
					break;
				case ACCOUNT:
					authZ = EffectiveAuthorizationService.getRoleAuthorization((AccountType)src, role, new BasePermissionType[]{permission});
					break;
				default:
					logger.error("Unexpected source type: " + src.getNameType());
					break;
			}
		}
		if(authZ){
			out_response = OperationResponseEnumType.SUCCEEDED;
		}
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
