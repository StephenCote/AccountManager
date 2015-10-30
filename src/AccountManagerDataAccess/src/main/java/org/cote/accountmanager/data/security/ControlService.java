package org.cote.accountmanager.data.security;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
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

public class ControlService {
	
	public static final Logger logger = Logger.getLogger(ControlService.class.getName());
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
		boolean out_bool = false;
		if(action == ControlActionEnumType.UNKNOWN || action == ControlActionEnumType.ANY){
			logger.error(action.toString() + " is unsupported as a validation action");
		}
		else if(control.getControlAction() != ControlActionEnumType.ANY && control.getControlAction() != action){
			logger.warn("Control with action " + control.getControlAction().toString() + " does not cover validation action " + action.toString());
		}
		else if(control.getControlType() == ControlEnumType.POLICY && control.getControlId().compareTo(0L) > 0){
			logger.info("Execute policy " + control.getControlId());
			try{
				PolicyType policy = Factories.getPolicyFactory().getById(control.getControlId(), control.getOrganizationId());
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
						out_bool = true;
					}
				}
			}
			catch(ArgumentException e){
				logger.error(e.getMessage());
			}
			catch (FactoryException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			/*
			
			*/
		}
		else{
			logger.error("Control does not define a valid type or reference");
		}
		
		return out_bool;
	}
	
}
