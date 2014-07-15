package org.cote.accountmanager.data.operation;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.UserType;

public class LookupUserOperation implements IOperation {
	public static final Logger logger = Logger.getLogger(LookupUserOperation.class.getName());

	public <T> T read(FactType sourceFact,final FactType referenceFact){
		return OperationUtil.factoryRead(sourceFact, referenceFact);
	}
	public OperationResponseEnumType operate(final PatternType pattern, FactType sourceFact,final FactType referenceFact){
		if(sourceFact.getSourceUrn() == null){
			logger.error("Invalid argument");
			return OperationResponseEnumType.ERROR;
		}
		UserType u = read(sourceFact,referenceFact);
		if(u == null){
			logger.error("Failed to find user " + sourceFact.getSourceUrn() + " in organization " + referenceFact.getOrganization().getName());
			return OperationResponseEnumType.FAILED;
		}
		logger.info("Found user " + sourceFact.getSourceUrn() + " in organization " + referenceFact.getOrganization().getName() + " having user id " + u.getId());
		return OperationResponseEnumType.SUCCEEDED;
	}
	
}
