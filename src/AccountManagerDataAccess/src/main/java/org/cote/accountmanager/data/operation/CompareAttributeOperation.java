package org.cote.accountmanager.data.operation;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.rule.RuleUtil;
import org.cote.accountmanager.objects.AttributeType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;

public class CompareAttributeOperation implements IOperation {
	public static final Logger logger = Logger.getLogger(CompareAttributeOperation.class.getName());
	public <T> T read(FactType sourceFact,final FactType referenceFact){
		return OperationUtil.factoryRead(sourceFact, referenceFact);
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
			logger.error("Failed to find object " + sourceFact.getSourceUrn() + " in organization " + referenceFact.getOrganization().getName());
			return OperationResponseEnumType.FAILED;
		}
		logger.info("Found object " + sourceFact.getSourceUrn() + " in organization " + referenceFact.getOrganization().getName() + " having user id " + obj.getId());
		
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
