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
package org.cote.rest.services;

import javax.annotation.security.DeclareRoles;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
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
		UserType user = ServiceUtil.getUserFromSession(request);
		policyRequest.setContextUser(user);
		PolicyResponseType policyResponse = new PolicyResponseType();
		policyResponse.setResponse(PolicyResponseEnumType.UNKNOWN);
		policyResponse.setUrn(policyRequest.getUrn());

		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, policyRequest.getUrn(),AuditEnumType.SESSION,ServiceUtil.getSessionId(request));
		//UserType user = ServiceUtil.getUserFromSession(audit, request);
		//if(user == null) return policyResponse;
		logger.warn("TODO: Add policy evaluator role check");

		try {
			policyResponse = PolicyEvaluator.evaluatePolicyRequest(policyRequest);
			if(policyResponse ==  null){
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
	public PolicyDefinitionType define(@PathParam("objectId") String id,@Context HttpServletRequest request){
		PolicyDefinitionType def = null;
		logger.info("Defining policy for " + id);
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
