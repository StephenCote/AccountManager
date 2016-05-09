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

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.data.rule.RuleUtil;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;

public class CompareAttributeOperation implements IOperation {
	public static final Logger logger = Logger.getLogger(CompareAttributeOperation.class.getName());
	public <T> T read(FactType sourceFact,final FactType referenceFact){
		return FactUtil.factoryRead(sourceFact, referenceFact);
	}
	public OperationResponseEnumType operate(final PatternType pattern, FactType sourceFact,final FactType referenceFact){
		if(referenceFact.getSourceUrn() == null){
			logger.error("Match fact " + referenceFact.getUrn() + " doesn't define a source urn");
			return OperationResponseEnumType.ERROR;
					
		}
		/*
		if(referenceFact.getFactData() == null){
			logger.error("Match fact " + referenceFact.getUrn() + " doesn't define fact data");
			return OperationResponseEnumType.ERROR;
		}
		*/
		NameIdType obj = read(sourceFact,referenceFact);
		if(obj == null){
			logger.error("Failed to find object " + sourceFact.getSourceUrn() + " in organization " + referenceFact.getOrganizationId());
			return OperationResponseEnumType.FAILED;
		}
		logger.info("Found object " + sourceFact.getSourceUrn() + " in organization " + referenceFact.getOrganizationId() + " having user id " + obj.getId());
		
		Factories.getAttributeFactory().populateAttributes(obj);
		String attrVal = Factories.getAttributeFactory().getAttributeValueByName(obj, referenceFact.getSourceUrn());
		if(pattern.getComparator() == ComparatorEnumType.IS_NULL){
			if(attrVal == null || attrVal.length() == 0){
				logger.info("Compared for null value");
				return OperationResponseEnumType.SUCCEEDED;
			}
		}
		else if(referenceFact.getFactData() != null && RuleUtil.compareValue(attrVal, pattern.getComparator(), referenceFact.getFactData())){
		
			logger.info("Comparation was true");
			return OperationResponseEnumType.SUCCEEDED;	
		}
		else{
			logger.info("Comparation was false");
		}
		return OperationResponseEnumType.FAILED;
	}
	
	
}
