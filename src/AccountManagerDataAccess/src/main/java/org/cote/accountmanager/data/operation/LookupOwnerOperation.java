package org.cote.accountmanager.data.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.data.security.RequestService;
import org.cote.accountmanager.objects.ApproverType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseType;

public class LookupOwnerOperation  implements IOperation {
	public static final Logger logger = LogManager.getLogger(LookupOwnerOperation.class);

	public <T> T read(FactType sourceFact,final FactType referenceFact){
		return FactUtil.factoryRead(sourceFact, referenceFact);
	}
	public OperationResponseEnumType operate(final PolicyRequestType prt,PolicyResponseType prr, final PatternType pattern, FactType sourceFact,final FactType referenceFact){
		logger.debug("Lookup owner....");
		if(sourceFact.getFactType() != FactEnumType.PARAMETER || sourceFact.getFactReference() == null){
			logger.error("Invalid argument.  Expecting a parameter fact populated with a fact reference.");
			return OperationResponseEnumType.ERROR;
		}

		ApproverType owner = RequestService.findOwner(sourceFact.getFactReference());
		if(owner != null) {
			/*
			sourceFact.setFactReference(owner);
			sourceFact.setFactoryType(FactoryEnumType.APPROVER);
			*/
			prr.getResponseData().add(owner);
			return OperationResponseEnumType.SUCCEEDED;
		}
		return OperationResponseEnumType.FAILED;
	}
	



}
