package org.cote.accountmanager.data.operation;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;

public class AccessApprovalOperation  implements IOperation {
	public static final Logger logger = LogManager.getLogger(AccessApprovalOperation.class);

	public <T> T read(FactType sourceFact,final FactType referenceFact){
		return FactUtil.factoryRead(sourceFact, referenceFact);
	}
	public OperationResponseEnumType operate(final PatternType pattern, FactType sourceFact,final FactType referenceFact){
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
