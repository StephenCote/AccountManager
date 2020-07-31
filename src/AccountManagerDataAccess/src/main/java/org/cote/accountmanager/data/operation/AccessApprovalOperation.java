package org.cote.accountmanager.data.operation;

import org.apache.commons.lang3.EnumUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ApproverEnumType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.util.JSONUtil;

public class AccessApprovalOperation  implements IOperation {
	public static final Logger logger = LogManager.getLogger(AccessApprovalOperation.class);

	public <T> T read(FactType sourceFact,final FactType referenceFact){
		return FactUtil.factoryRead(sourceFact, referenceFact);
	}
	
	public <T> T readEntitlement(FactType sourceFact, final FactType referenceFact) throws FactoryException, NumberFormatException, ArgumentException {
		INameIdFactory iFact = Factories.getFactory(sourceFact.getFactoryType());
		return iFact.getById(Long.parseLong(sourceFact.getFactData()), referenceFact.getOrganizationId());
	}
	public OperationResponseEnumType operate(final PolicyRequestType prt,PolicyResponseType prr, final PatternType pattern, FactType sourceFact,final FactType referenceFact){
		
		logger.debug("Operation Access Approval");

		if(sourceFact.getFactData() == null){
			logger.error("Null source fact data.");
			return OperationResponseEnumType.ERROR;
		}
		if(!EnumUtils.isValidEnum(ApproverEnumType.class, sourceFact.getFactoryType().toString())) {
			logger.error("Invalid parameter fact factory type");
			return OperationResponseEnumType.ERROR;
		}


		if(referenceFact.getFactType() != FactEnumType.OPERATION){
			logger.error("Invalid reference fact type.");
			return OperationResponseEnumType.ERROR;
		}
		IOperation operation = read(referenceFact, referenceFact);
		if(operation == null) {
			logger.error("Invalid operation for " + referenceFact.getSourceUrn());
			return OperationResponseEnumType.ERROR;			
		}
		//IOperation operation = OperationUtil.getOperationInstance(referenceFact.getSourceUrn());
		NameIdType ent = null;
		try {
			ent = readEntitlement(sourceFact, referenceFact);
		} catch (NumberFormatException | FactoryException | ArgumentException e) {
			logger.error(e);
		}
		if(ent == null) {
			logger.error("Invalid entitlement reference");
			return OperationResponseEnumType.ERROR;
		}
		
		/// Current apprSize
		int currentSize = prr.getResponseData().size();
		
		/// Now there should be both an operation and an entitlement: Send the facts to the operation in order to dig up the corresponding approver
		sourceFact.setFactReference(ent);
		
		OperationResponseEnumType opRep = operation.operate(prt, prr, pattern, sourceFact, referenceFact);
		if(opRep != OperationResponseEnumType.SUCCEEDED) {
			logger.error("Fact operation " + referenceFact.getSourceUrn() + " failed");
			return OperationResponseEnumType.FAILED;
		}
		if((prr.getResponseData().size() - currentSize) > 0) {
			return OperationResponseEnumType.SUCCEEDED;	
		}
		logger.error("Fact operation " + referenceFact.getSourceUrn() + " failed to find an approver");
		logger.error(JSONUtil.exportObject(sourceFact));
//		return OperationResponseEnumType.SUCCEEDED;
		return OperationResponseEnumType.FAILED;

	}

}
