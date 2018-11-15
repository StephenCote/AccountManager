package org.cote.accountmanager.data.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;

public class LookupOwnerOperation  implements IOperation {
	public static final Logger logger = LogManager.getLogger(LookupOwnerOperation.class);

	public <T> T read(FactType sourceFact,final FactType referenceFact){
		return FactUtil.factoryRead(sourceFact, referenceFact);
	}
	public OperationResponseEnumType operate(final PatternType pattern, FactType sourceFact,final FactType referenceFact){
		logger.info("Lookup owner....");
		if(sourceFact.getFactData() == null){
			logger.error("Invalid argument.  Expecting ...");
			return OperationResponseEnumType.ERROR;
		}
		if(referenceFact.getFactData() == null){
			logger.error("Invalid argument.  Expecting ...");
			return OperationResponseEnumType.ERROR;
		}

//		return OperationResponseEnumType.SUCCEEDED;
		return OperationResponseEnumType.FAILED;

	}

}
