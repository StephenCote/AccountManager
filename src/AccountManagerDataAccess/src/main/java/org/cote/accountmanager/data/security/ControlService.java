/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.data.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ControlActionEnumType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseEnumType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;

public class ControlService {
	
	public static final Logger logger = LogManager.getLogger(ControlService.class);
	public static <T> void defineParameter(
			PolicyRequestType prt,
			FactType paramFact,
			UserType contextUser,
			ControlType control,
			ControlActionEnumType action,
			T contextObject
		) {
		if(paramFact.getFactType() != FactEnumType.PARAMETER){
			logger.warn("Fact is not defined as a parameter");
			return;
		}
		switch(paramFact.getFactoryType()){
			case CREDENTIAL:
				/// Credential writes - the contextObject is assumed to be a string value
				/// It can't be a reference to the object, because by then the value is already hashed or encrypted
				/// Need a way to be a bit more granular here
				/// 
				if(action == ControlActionEnumType.WRITE){
					paramFact.setFactData((String)contextObject);
				}
				else{
					logger.warn("Unhandled verb for credential fact: " + paramFact.getName());
				}
				break;
			
			default:
				logger.warn("Unhandled type: " + paramFact.getFactoryType() + " for " + paramFact.getName());
				break;
		}
	
	}
	public static <T> boolean validateControl(
		UserType contextUser,
		ControlType control,
		ControlActionEnumType action,
		T contextObject
	) {

		boolean outBool = false;
		if(action == ControlActionEnumType.UNKNOWN || action == ControlActionEnumType.ANY){
			logger.error(action.toString() + " is unsupported as a validation action");
		}
		else if(control.getControlAction() != ControlActionEnumType.ANY && control.getControlAction() != action){
			logger.warn("Control with action " + control.getControlAction().toString() + " does not cover validation action " + action.toString());
		}
		else if(control.getControlType() == ControlEnumType.POLICY && control.getControlId().compareTo(0L) > 0){
			logger.info("Execute policy " + control.getControlId());
			try{
				PolicyType policy = Factories.getNameIdFactory(FactoryEnumType.POLICY).getById(control.getControlId(), control.getOrganizationId());
				if(policy == null){
					logger.error("Invalid policy reference");
				}
				else{
					PolicyDefinitionType pdt = PolicyDefinitionUtil.generatePolicyDefinition(policy);
					PolicyRequestType prt = PolicyDefinitionUtil.generatePolicyRequest(pdt);
					for(int i = 0; i < prt.getFacts().size(); i++){
						defineParameter(prt,prt.getFacts().get(i),contextUser,control,action, contextObject);
					}
					//logger.info("Param facts: " + prt.getFacts().size());
					//prt.getFacts().get(0).setFactData("badpassword");
					PolicyResponseType prr = PolicyEvaluator.evaluatePolicyRequest(prt);
					if(prr.getResponse() == PolicyResponseEnumType.PERMIT){
						outBool = true;
					}
				}
			}
			catch(ArgumentException e){
				logger.error(e.getMessage());
			}
			catch (FactoryException e) {
					
					logger.error(e.getMessage());
					logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				}
			/*
			
			*/
		}
		else{
			logger.error("Control does not define a valid type or reference");
		}
		
		return outBool;
	}
	
}
