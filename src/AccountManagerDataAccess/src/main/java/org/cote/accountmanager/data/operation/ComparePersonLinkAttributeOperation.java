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
			logger.error("Failed to find person " + sourceFact.getSourceUrn() + " in organization " + referenceFact.getOrganization().getName());
			return OperationResponseEnumType.FAILED;
		}
		logger.info("Found person " + sourceFact.getSourceUrn() + " in organization " + referenceFact.getOrganization().getName() + " having id " + obj.getId());
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

