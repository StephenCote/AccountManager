package org.cote.accountmanager.data.policy;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.types.FactoryEnumType;


public class PolicyDefinitionUtil {
	public static final Logger logger = Logger.getLogger(PolicyDefinitionUtil.class.getName());
	
	public static PolicyDefinitionType generatePolicyDefinition(PolicyType pol) throws FactoryException, ArgumentException{
		PolicyDefinitionType pdt = new PolicyDefinitionType();
		pdt.setCreated(pol.getCreated());
		pdt.setDecisionAge(pol.getDecisionAge());
		pdt.setEnabled(pol.getEnabled());
		pdt.setExpires(pol.getExpires());
		pdt.setModified(pol.getModified());
		pdt.setUrn(pol.getUrn());
		copyParameters(pdt,pol);
		return pdt;
	}
	private static void copyParameters(PolicyDefinitionType pdt, PolicyType pol) throws FactoryException, ArgumentException{
		Factories.getPolicyFactory().populate(pol);
		for(int i = 0;i < pol.getRules().size();i++){
			copyParameters(pdt,pol.getRules().get(i));
		}
		
	}
	private static void copyParameters(PolicyDefinitionType pdt, RuleType rule) throws FactoryException, ArgumentException{
		Factories.getRuleFactory().populate(rule);
		for(int i = 0; i < rule.getPatterns().size();i++){
			copyParameters(pdt,rule.getPatterns().get(i));
		}
		for(int i = 0; i < rule.getRules().size();i++){
			copyParameters(pdt,rule.getRules().get(i));
		}

	}
	private static void copyParameters(PolicyDefinitionType pdt, PatternType pattern) throws FactoryException, ArgumentException{
		Factories.getPatternFactory().populate(pattern);
		if(pattern.getFact() != null && pattern.getFact().getFactType() == FactEnumType.PARAMETER){
			logger.info(pdt.getUrn() + " Parameter " + pattern.getFactUrn());
			FactType parmFact = new FactType();
			parmFact.setUrn(pattern.getFactUrn());
			parmFact.setFactoryType(pattern.getFact().getFactoryType());
			parmFact.setFactType(pattern.getFact().getFactType());
			parmFact.setSourceDataType(pattern.getFact().getSourceDataType());
			parmFact.setSourceUrn(pattern.getFact().getSourceUrn());
			parmFact.setSourceUrl(pattern.getFact().getSourceUrl());
			pdt.getParameters().add(parmFact);
		}
		else{
			logger.info("SKIP " + pdt.getUrn() + " Fact " + pattern.getFactUrn());
		}
		
	}
}