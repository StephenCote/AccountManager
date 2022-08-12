package org.cote.accountmanager.data.operation;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.data.services.RequestService;
import org.cote.accountmanager.objects.ApproverType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseType;

public class LookupAccessOperation  implements IOperation {
	public static final Logger logger = LogManager.getLogger(LookupAccessOperation.class);

	public <T> T read(FactType sourceFact,final FactType referenceFact){
		return FactUtil.factoryRead(sourceFact, referenceFact);
	}
	public OperationResponseEnumType operate(final PolicyRequestType prt,PolicyResponseType prr, final PatternType pattern, FactType sourceFact,final FactType referenceFact){
		logger.debug("Lookup access....");
		if(sourceFact.getFactType() != FactEnumType.PARAMETER || sourceFact.getFactReference() == null){
			logger.error("Invalid argument.  Expecting a parameter fact populated with a fact reference.");
			return OperationResponseEnumType.ERROR;
		}

		List<ApproverType> approvers = RequestService.findApprovers(sourceFact.getFactReference());
		if(approvers.size() > 0) {

			prr.getResponseData().addAll(approvers);
			return OperationResponseEnumType.SUCCEEDED;
		}
		return OperationResponseEnumType.FAILED;
	}
	



}
