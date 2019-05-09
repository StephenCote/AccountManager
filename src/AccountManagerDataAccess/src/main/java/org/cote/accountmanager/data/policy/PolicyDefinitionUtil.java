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
package org.cote.accountmanager.data.policy;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestEnumType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.DataUtil;

public class PolicyDefinitionUtil {
	public static final Logger logger = LogManager.getLogger(PolicyDefinitionUtil.class);
	
	public static PolicyRequestType generatePolicyRequest(PolicyDefinitionType pdt){
		PolicyRequestType prt = new PolicyRequestType();
		prt.setRequestType(PolicyRequestEnumType.DECIDE);
		prt.setOrganizationPath(pdt.getOrganizationPath());
		prt.setUrn(pdt.getUrn());
		for(int i = 0; i < pdt.getParameters().size();i++){
			FactType parm = pdt.getParameters().get(i);
			FactType fact = new FactType();
			fact.setFactoryType(parm.getFactoryType());
			fact.setUrn(parm.getUrn());
			fact.setName(parm.getName());
			fact.setFactType(parm.getFactType());
			fact.setFactData(parm.getFactData());
			fact.setNameType(parm.getNameType());
			fact.setObjectId(parm.getObjectId());
			fact.setLogicalOrder(parm.getLogicalOrder());
			fact.setParameter(parm.getParameter());
			fact.setSourceDataType(parm.getSourceDataType());
			fact.setSourceType(parm.getSourceType());
			fact.setSourceUrl(parm.getSourceUrl());
			fact.setSourceUrn(parm.getSourceUrn());
			prt.getFacts().add(fact);
		}
		return prt;

	}
	public static PolicyDefinitionType generatePolicyDefinition(PolicyType pol) throws FactoryException, ArgumentException{
		PolicyDefinitionType pdt = new PolicyDefinitionType();
		pdt.setCreatedDate(pol.getCreatedDate());
		pdt.setDecisionAge(pol.getDecisionAge());
		pdt.setEnabled(pol.getEnabled());
		pdt.setExpiresDate(pol.getExpiresDate());
		pdt.setModifiedDate(pol.getModifiedDate());
		pdt.setUrn(pol.getUrn());
		pdt.setOrganizationPath(((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationPath(pol.getOrganizationId()));
		copyParameters(pdt,pol);
		return pdt;
	}
	private static void copyParameters(PolicyDefinitionType pdt, PolicyType pol) throws FactoryException, ArgumentException{
		Factories.getNameIdFactory(FactoryEnumType.POLICY).populate(pol);
		logger.info("Processing " + pol.getRules().size() + " rules");
		for(int i = 0;i < pol.getRules().size();i++){
			copyParameters(pdt,pol.getRules().get(i));
		}
		
	}
	private static void copyParameters(PolicyDefinitionType pdt, RuleType rule) throws FactoryException, ArgumentException{
		Factories.getNameIdFactory(FactoryEnumType.RULE).populate(rule);
		logger.info("Processing " + rule.getPatterns().size() + " patterns");
		for(int i = 0; i < rule.getPatterns().size();i++){
			copyParameters(pdt,rule.getPatterns().get(i));
		}
		logger.info("Processing " + rule.getRules().size() + " child rules");
		for(int i = 0; i < rule.getRules().size();i++){
			copyParameters(pdt,rule.getRules().get(i));
		}

	}
	private static boolean haveParameter(PolicyDefinitionType pdt, FactType fact){
		boolean outBool = false;
		for(int i = 0; i < pdt.getParameters().size();i++){
			if(pdt.getParameters().get(i).getUrn().equals(fact.getUrn())){
				outBool = true;
				break;
			}
		}
		return outBool;
	}

	private static void copyParameters(PolicyDefinitionType pdt, PatternType pattern) throws FactoryException, ArgumentException{
		Factories.getNameIdFactory(FactoryEnumType.PATTERN).populate(pattern);
		if(pattern.getFact() != null && pattern.getFact().getFactType() == FactEnumType.PARAMETER){
			logger.info("Processing Parameter");
			if(haveParameter(pdt,pattern.getFact())){
				logger.info("Skipping duplicate parameter");
				return;
			}
			logger.info(pdt.getUrn() + " Parameter " + pattern.getFactUrn());
			FactType parmFact = new FactType();
			parmFact.setName(pattern.getFact().getName());
			parmFact.setNameType(NameEnumType.FACT);
			parmFact.setUrn(pattern.getFactUrn());
			parmFact.setFactoryType(pattern.getFact().getFactoryType());
			parmFact.setFactType(pattern.getFact().getFactType());
			parmFact.setSourceDataType(pattern.getFact().getSourceDataType());
			parmFact.setSourceUrn(pattern.getFact().getSourceUrn());
			parmFact.setSourceUrl(pattern.getFact().getSourceUrl());
			if(pattern.getPatternType() == PatternEnumType.VERIFICATION && pattern.getFact().getFactoryType() == FactoryEnumType.DATA){
				DataType data = FactUtil.getFactSource(parmFact);
				if(data != null){
					try{
						parmFact.setFactData(DataUtil.getValueString(data));
					}
					catch(DataException e){
						logger.error(e);
					}
				}
				else{
					logger.error("Null fact data reference");
				}
			}
			logger.info("Defining Parameter " + parmFact.getUrn());
			pdt.getParameters().add(parmFact);
		}
		else{
			logger.info("SKIP " + pdt.getUrn() + " Fact " + pattern.getFactUrn());
		}
		
	}
	public static String printPattern(PatternType pattern, int depth) throws FactoryException, ArgumentException{
		StringBuilder buff = new StringBuilder();
		StringBuilder baseTabBuff = new StringBuilder();
		for(int i = 0; i < depth; i++) baseTabBuff.append("\t");
		String baseTab = baseTabBuff.toString();
		String tab = baseTab.toString() + "\t";
		String subTab = tab + "\t";
		Factories.getNameIdFactory(FactoryEnumType.PATTERN).populate(pattern);
		buff.append(baseTab + "PATTERN " + pattern.getName()+ "\n");
		buff.append(tab + "urn\t" + pattern.getUrn()+ "\n");
		buff.append(tab + "type\t" + pattern.getPatternType()+ "\n");
		buff.append(tab + "order\t" + pattern.getLogicalOrder()+ "\n");
		if(pattern.getOperationUrn() != null) buff.append(tab + "operation\t" + pattern.getOperationUrn()+ "\n");
		FactType srcFact = pattern.getFact();
		FactType mFact = pattern.getMatch();
		buff.append(tab + "SOURCE FACT " + (srcFact != null ? srcFact.getName() : "IS NULL")+ "\n");
		if(srcFact != null){
			buff.append(subTab + "urn\t" + srcFact.getUrn()+ "\n");
			buff.append(subTab + "type\t" + srcFact.getFactType()+ "\n");
			buff.append(subTab + "factoryType\t" + srcFact.getFactoryType()+ "\n");
			buff.append(subTab + "sourceUrl\t" + srcFact.getSourceUrl()+ "\n");
			buff.append(subTab + "sourceUrn\t" + srcFact.getSourceUrn()+ "\n");
			buff.append(subTab + "sourceType\t" + srcFact.getSourceType()+ "\n");
			buff.append(subTab + "sourceDataType\t" + srcFact.getSourceDataType().toString()+ "\n");
			buff.append(subTab + "factData\t" + srcFact.getFactData()+ "\n");
		}
		buff.append(tab + "COMPARATOR " + pattern.getComparator()+ "\n");
		buff.append(tab + "MATCH FACT " + (mFact != null ? mFact.getName() : "IS NULL")+ "\n");
		if(mFact != null){
			buff.append(subTab + "urn\t" + mFact.getUrn()+ "\n");
			buff.append(subTab + "type\t" + mFact.getFactType()+ "\n");
			buff.append(subTab + "factoryType\t" + mFact.getFactoryType()+ "\n");
			buff.append(subTab + "sourceUrl\t" + mFact.getSourceUrl()+ "\n");
			buff.append(subTab + "sourceUrn\t" + mFact.getSourceUrn()+ "\n");
			buff.append(subTab + "sourceType\t" + mFact.getSourceType()+ "\n");
			buff.append(subTab + "sourceDataType\t" + mFact.getSourceDataType().toString()+ "\n");
			buff.append(subTab + "factData\t" + mFact.getFactData()+ "\n");
			if(mFact.getFactType() == FactEnumType.OPERATION){
				buff.append(subTab + "OPERATION\t" + (mFact.getSourceUrl() != null ? mFact.getSourceUrl() : "IS NULL")+ "\n");
				if(mFact.getSourceUrl() != null){
					OperationType op = Factories.getNameIdFactory(FactoryEnumType.OPERATION).getByUrn(mFact.getSourceUrl());
					buff.append(subTab + "urn\t" + op.getUrn()+ "\n");
					buff.append(subTab + "operationType\t" + op.getOperationType()+ "\n");
					buff.append(subTab + "operation\t" + op.getOperation()+ "\n");
				}
				
			}
		}
		return buff.toString();
	}
	public static String printRule(RuleType rule, int depth) throws FactoryException, ArgumentException{
		
		Factories.getNameIdFactory(FactoryEnumType.RULE).populate(rule);
		StringBuilder buff = new StringBuilder();
		StringBuilder baseTabBuff = new StringBuilder();
		for(int i = 0; i < depth; i++) baseTabBuff.append("\t");
		String baseTab = baseTabBuff.toString();
		String tab = baseTab.toString() + "\t";

		buff.append(baseTab + "RULE " + rule.getName()+ "\n");
		buff.append(tab + "urn\t" + rule.getUrn()+ "\n");
		buff.append(tab + "type\t" + rule.getRuleType()+ "\n");
		buff.append(tab + "condition\t" + rule.getCondition()+ "\n");
		buff.append(tab + "order\t" + rule.getLogicalOrder()+ "\n");
		
		List<RuleType> rules = rule.getRules();
		for(int p = 0; p < rules.size();p++){
			RuleType crule = rules.get(p);
			buff.append(printRule(crule,depth+1));
		}
		
		List<PatternType> patterns = rule.getPatterns();
		for(int p = 0; p < patterns.size();p++){
			PatternType pattern = patterns.get(p);
			buff.append(printPattern(pattern,depth+1));
		}
		return buff.toString();
	}
	public static String printPolicy(PolicyType pol) throws FactoryException, ArgumentException{
		StringBuilder buff = new StringBuilder();
		Factories.getNameIdFactory(FactoryEnumType.POLICY).populate(pol);
		buff.append("\nPOLICY " + pol.getName()+ "\n");
		buff.append("\turn\t" + pol.getUrn()+ "\n");
		buff.append("\tenabled\t" + pol.getEnabled()+ "\n");
		buff.append("\tcreated\t" + pol.getCreatedDate().toString()+ "\n");
		buff.append("\texpires\t" + pol.getExpiresDate().toString()+ "\n");
		List<RuleType> rules = pol.getRules();
		for(int i = 0; i < rules.size();i++){
			RuleType rule = rules.get(i);
			buff.append(printRule(rule, 1));
		}
		return buff.toString();
	}
	

	
}