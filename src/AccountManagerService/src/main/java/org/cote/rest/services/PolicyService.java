package org.cote.rest.services;

import javax.annotation.security.DeclareRoles;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.data.services.AuditService;

import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.AuthorizationPolicyType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseEnumType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.util.ServiceUtil;

@DeclareRoles({"admin","user"})
@Path("/policy")
public class PolicyService {
	private static final Logger logger = LogManager.getLogger(PolicyService.class);
	
	@POST
	@Path("/evaluate")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public PolicyResponseType evaluate(PolicyRequestType policyRequest, @Context HttpServletRequest request){
		PolicyResponseType policyResponse = new PolicyResponseType();
		policyResponse.setResponse(PolicyResponseEnumType.UNKNOWN);
		policyResponse.setUrn(policyRequest.getUrn());

		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, policyRequest.getUrn(),AuditEnumType.SESSION,ServiceUtil.getSessionId(request));
		//UserType user = ServiceUtil.getUserFromSession(audit, request);
		//if(user == null) return policyResponse;
		logger.warn("TODO: Add policy evaluator role check");

		try {
			policyResponse = PolicyEvaluator.evaluatePolicyRequest(policyRequest);
			if(policyResponse == null){
				AuditService.denyResult(audit, "Response is null");
			}
			else if(policyResponse.getResponse() == PolicyResponseEnumType.PERMIT || policyResponse.getResponse() == PolicyResponseEnumType.AUTHENTICATED || policyResponse.getResponse() == PolicyResponseEnumType.PENDING || policyResponse.getResponse() == PolicyResponseEnumType.PENDING_OPERATION){
				AuditService.permitResult(audit, "Permitting result with response " + policyResponse.getResponse().toString());
			}
			else{
				AuditService.denyResult(audit, "Denying result with response " +  policyResponse.getResponse().toString());
			}
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		logger.info("Policy evaluation response: " + policyResponse.getResponse());
		return policyResponse;


	}
	
	@GET
	@Path("/define/{objectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public PolicyDefinitionType define(@PathParam("id") String id,@Context HttpServletRequest request){
		PolicyDefinitionType def = null;

		PolicyType policy = BaseService.readByObjectId(AuditEnumType.POLICY, id, request);
		if(policy != null){
			try {
				def = PolicyDefinitionUtil.generatePolicyDefinition(policy);
			} catch (FactoryException | ArgumentException e) {
				logger.error(e);
			}
		}
		
	
		return def;
	}

	@GET
	@Path("/contextPolicy/{type:[\\S]+]}/{id: [0-9]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public AuthorizationPolicyType contextPolicy(@PathParam("type") String type,@PathParam("id") long id,@Context HttpServletRequest request){
		//return PolicyServiceImpl.getPolicyDefinition(id, request);
		return null;
	}
}
