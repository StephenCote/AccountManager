package org.cote.accountmanager.data.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class MatchSystemRoleOperation implements IOperation {
	public static final Logger logger = LogManager.getLogger(MatchSystemRoleOperation.class);

	public <T> T read(FactType sourceFact,final FactType referenceFact){
		return FactUtil.factoryRead(sourceFact, referenceFact);
	}
	public OperationResponseEnumType operate(final PatternType pattern, FactType sourceFact,final FactType referenceFact){
		if(sourceFact.getFactReference() == null || sourceFact.getFactReference().getNameType() == NameEnumType.UNKNOWN){
			logger.error("Invalid argument");
			return OperationResponseEnumType.ERROR;
		}
		if(!sourceFact.getFactReference().getNameType().equals(NameEnumType.valueOf(referenceFact.getFactType().toString()))){
			logger.warn("Source type " + sourceFact.getFactReference().getNameType().toString() + " doesn't match " + referenceFact.getFactType().toString());
			return OperationResponseEnumType.FAILED;
		}
		BaseRoleType systemRole = null;
		try {
			systemRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleByName(referenceFact.getSourceUrn(), null, ((BaseRoleType)sourceFact.getFactReference()).getRoleType(), referenceFact.getOrganizationId());
				
			
		} catch (FactoryException | ArgumentException e) {
			
			logger.error("Error",e);
		}
		
		if(systemRole == null){
			logger.error("Invalid " + ((BaseRoleType)sourceFact.getFactReference()).getRoleType().toString() + " role '" + referenceFact.getSourceUrn() + "' in #" + referenceFact.getOrganizationId());
			return OperationResponseEnumType.FAILED;
		}
		if(systemRole.getId().equals(sourceFact.getFactReference().getId())){
			return OperationResponseEnumType.SUCCEEDED;
		}
		logger.debug("Role " + systemRole.getUrn() + " does not match parameter role with id " + sourceFact.getFactReference().getUrn());
		return OperationResponseEnumType.FAILED;
		
		//getAccountRole("DataAdministrators", null, organizationId);
		//logger.debug("Supplied fact reference matches type " + referenceFact.getFactoryType().toString());
		
		
	}

}
