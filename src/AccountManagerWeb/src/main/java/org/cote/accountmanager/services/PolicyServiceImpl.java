/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.AuthorizationPolicyType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseEnumType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.SessionStatusEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.util.ServiceUtil;

public class PolicyServiceImpl  {
	
	public static final String defaultDirectory = "~/Policies";
	public static final Logger logger = Logger.getLogger(PolicyServiceImpl.class.getName());
	public static boolean delete(PolicyType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.POLICY, bean, request);
	}
	
	public static boolean add(PolicyType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.POLICY, bean, request);
	}
	public static boolean update(PolicyType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.POLICY, bean, request);
	}
	public static PolicyType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.POLICY, name, request);
	}
	public static PolicyType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.POLICY, groupId, name, request);
	}	
	public static PolicyType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.POLICY, id, request);
	}
	
	public static List<PolicyType> getGroupList(UserType user, String path, long startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.POLICY, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.POLICY, groupId, request);
	}
	
	public static PolicyResponseType evaluatePolicy(PolicyRequestType policyRequest,HttpServletRequest request){
		PolicyResponseType policyResponse = new PolicyResponseType();
		policyResponse.setResponse(PolicyResponseEnumType.UNKNOWN);
		policyResponse.setUrn(policyRequest.getUrn());
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, policyRequest.getUrn(),AuditEnumType.SESSION,ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return policyResponse;
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Policy evaluation response: " + policyResponse.getResponse());
		return policyResponse;
	}
	public static AuthorizationPolicyType getPolicy(HttpServletRequest request){
		AuthorizationPolicyType pol = new AuthorizationPolicyType();
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, "Self Authorization Policy",AuditEnumType.SESSION,ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(request);
		if(user != null){
			AuditService.sourceAudit(audit,AuditEnumType.USER,user.getName() + " (#" + user.getId() + ") in Org " + " (#" + user.getOrganizationId() + ")");
			pol.setAuthenticated(user.getSessionStatus() == SessionStatusEnumType.AUTHENTICATED);
			try{
				pol.setAccountAdministrator(AuthorizationService.isAccountAdministratorInOrganization(user, user.getOrganizationId()));
				pol.setDataAdministrator(AuthorizationService.isDataAdministratorInOrganization(user, user.getOrganizationId()));
				pol.setAccountReader(AuthorizationService.isAccountReaderInOrganization(user, user.getOrganizationId()));
				pol.setRoleReader(AuthorizationService.isRoleReaderInOrganization(user, user.getOrganizationId()));
				pol.getRoles().addAll(EffectiveAuthorizationService.getEffectiveRolesForUser(user));
				pol.setAuthenticationId(ServiceUtil.getSessionId(request));
				pol.setFactoryType(FactoryEnumType.USER);
				setAuthorizationPolicyFields(pol,user);
				AuditService.permitResult(audit, "Returning authorization information for authenticated user");
			}
			catch(FactoryException e){
				AuditService.denyResult(audit, "Error: " + e.getMessage());
				e.printStackTrace();
				
			} catch (ArgumentException e) {
				AuditService.denyResult(audit, "Error: " + e.getMessage());
				e.printStackTrace();
			}
		}
		else{
			AuditService.permitResult(audit, "Returning authorization information for anonymous user");
		}
		
		return pol;
	}
	private static void setAuthorizationPolicyFields(AuthorizationPolicyType pol,NameIdType type){
		pol.setContextId(type.getId());
		pol.setContextName(type.getName());
		pol.setContextType(type.getNameType());
	}
	public static PolicyDefinitionType getPolicyDefinition(long id,HttpServletRequest request){
		PolicyDefinitionType def = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, Long.toString(id),AuditEnumType.SESSION,ServiceUtil.getSessionId(request));
		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return def;
		try {
			PolicyType pol = Factories.getPolicyFactory().getById(id, user.getOrganizationId());
			if(pol == null){
				AuditService.denyResult(audit, "Invalid id: " + Long.toString(id));
				return def;
			}
			AuditService.targetAudit(audit, AuditEnumType.POLICY, pol.getUrn());
			if(AuthorizationService.canViewGroup(user, Factories.getGroupFactory().getGroupById(pol.getGroupId(),pol.getOrganizationId())) == false){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + " (#" + pol.getGroupId() + ")");
				return def;
			}
			AuditService.permitResult(audit, "Generating policy definition");
			def = PolicyDefinitionUtil.generatePolicyDefinition(pol);
			
		}
		catch(FactoryException e){
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		}
		return def;
	}
}
