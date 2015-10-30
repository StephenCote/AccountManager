/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.data.operation;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.rule.RuleUtil;
import org.cote.accountmanager.objects.AttributeType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;

public class ComparePersonLinkAttributeOperation implements IOperation {
	public static final Logger logger = Logger.getLogger(ComparePersonLinkAttributeOperation.class.getName());
	public <T> T read(FactType sourceFact,final FactType referenceFact){
		return FactUtil.factoryRead(sourceFact, referenceFact);
	}
	public OperationResponseEnumType operate(final PatternType pattern, FactType sourceFact,final FactType referenceFact){
		if(referenceFact.getSourceUrn() == null){
			logger.error("Match fact " + referenceFact.getUrn() + " doesn't define a source urn");
			return OperationResponseEnumType.ERROR;
					
		}
		if(sourceFact.getFactoryType() != FactoryEnumType.PERSON){
			logger.error("Expecting a PERSON factory type and received " + sourceFact.getFactoryType());
			return OperationResponseEnumType.ERROR;
		
		}
		PersonType obj = read(sourceFact,referenceFact);
		
		if(obj == null){
			logger.error("Failed to find person " + sourceFact.getSourceUrn() + " in organization " + referenceFact.getOrganizationId());
			return OperationResponseEnumType.FAILED;
		}
		logger.info("Found person " + sourceFact.getSourceUrn() + " in organization " + referenceFact.getOrganizationId() + " having id " + obj.getId());
		OperationResponseEnumType out_resp = OperationResponseEnumType.FAILED;
		try {
			
			if(referenceFact.getFactoryType() == FactoryEnumType.PERSON){
				Factories.getAttributeFactory().populateAttributes(obj);
				out_resp = compareLinkedAttributeValue(obj, referenceFact.getSourceUrn(),pattern.getComparator(), referenceFact.getFactData());
			}
			else if(referenceFact.getFactoryType() == FactoryEnumType.USER || referenceFact.getFactoryType() == FactoryEnumType.ACCOUNT){
				Factories.getPersonFactory().populate(obj);	
				if(referenceFact.getFactoryType() == FactoryEnumType.USER){
					for(int i = 0; i < obj.getUsers().size();i++){
						Factories.getAttributeFactory().populateAttributes(obj.getUsers().get(i));
						if(compareLinkedAttributeValue(obj.getUsers().get(i), referenceFact.getSourceUrn(),pattern.getComparator(), referenceFact.getFactData()) == OperationResponseEnumType.SUCCEEDED){
							out_resp = OperationResponseEnumType.SUCCEEDED;
							break;
						}
						
					}
				}
				else if(referenceFact.getFactoryType() == FactoryEnumType.ACCOUNT){
					for(int i = 0; i < obj.getAccounts().size();i++){
						Factories.getAttributeFactory().populateAttributes(obj.getAccounts().get(i));
						if(compareLinkedAttributeValue(obj.getAccounts().get(i), referenceFact.getSourceUrn(),pattern.getComparator(), referenceFact.getFactData()) == OperationResponseEnumType.SUCCEEDED){
							out_resp = OperationResponseEnumType.SUCCEEDED;
							break;
						}
						
					}					
				}
			}
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		

		return out_resp;
	}
	
	private static OperationResponseEnumType compareLinkedAttributeValue(NameIdType obj, String attrName, ComparatorEnumType comp, String matchValue){
		String attrVal = Factories.getAttributeFactory().getAttributeValueByName(obj, attrName);
		if(comp == ComparatorEnumType.IS_NULL){
			if(attrVal == null || attrVal.length() == 0){
				logger.info("Compared for null value");
				return OperationResponseEnumType.SUCCEEDED;
			}
		}
		else if(matchValue != null && RuleUtil.compareValue(attrVal, comp, matchValue)){
		
			logger.info("Comparation was true");
			return OperationResponseEnumType.SUCCEEDED;	
		}
		else{
			logger.info("Comparation was false");
		}
		return OperationResponseEnumType.FAILED;
	}
	
	
}

