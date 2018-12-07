package org.cote.accountmanager.data.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.data.factory.ApproverFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.security.RequestService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ApproverEnumType;
import org.cote.accountmanager.objects.ApproverType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.util.JSONUtil;

public class LookupOwnerOperation  implements IOperation {
	public static final Logger logger = LogManager.getLogger(LookupOwnerOperation.class);

	public <T> T read(FactType sourceFact,final FactType referenceFact){
		return FactUtil.factoryRead(sourceFact, referenceFact);
	}
	public OperationResponseEnumType operate(final PolicyRequestType prt,PolicyResponseType prr, final PatternType pattern, FactType sourceFact,final FactType referenceFact){
		logger.info("Lookup owner....");
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
