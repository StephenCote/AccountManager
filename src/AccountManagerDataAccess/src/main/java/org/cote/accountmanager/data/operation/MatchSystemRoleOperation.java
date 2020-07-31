/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.data.operation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.fact.FactUtil;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationResponseEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class MatchSystemRoleOperation implements IOperation {
	public static final Logger logger = LogManager.getLogger(MatchSystemRoleOperation.class);

	public <T> T read(FactType sourceFact,final FactType referenceFact){
		return FactUtil.factoryRead(sourceFact, referenceFact);
	}
	public OperationResponseEnumType operate(final PolicyRequestType prt,PolicyResponseType prr, final PatternType pattern, FactType sourceFact,final FactType referenceFact){
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
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
