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
import org.cote.accountmanager.objects.BaseGroupType;
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
import org.cote.accountmanager.util.ServiceUtil;

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
	
	public static List<PolicyType> getGroupList(UserType user, String path, int startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.POLICY, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.POLICY, groupId, request);
	}
	
	public static PolicyResponseType evaluatePolicy(PolicyRequestType policyRequest,HttpServletRequest request){
		PolicyResponseType policyResponse = new PolicyResponseType();
		policyResponse.setResponse(PolicyResponseEnumType.UNKNOWN);
		policyResponse.setUrn(policyRequest.getUrn());
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, policyRequest.getUrn(),AuditEnumType.SESSION,request.getSession(true).getId());
		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return policyResponse;
		logger.warn("TODO: Add policy evaluator role check");

		try {
			policyResponse = PolicyEvaluator.evaluatePolicyRequest(policyRequest);
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
		AuditType audit = AuditService.beginAudit(ActionEnumType.AUTHORIZE, "Self Authorization Policy",AuditEnumType.SESSION,request.getSession(true).getId());
		UserType user = ServiceUtil.getUserFromSession(request);
		if(user != null){
			AuditService.sourceAudit(audit,AuditEnumType.USER,user.getName() + " (#" + user.getId() + ") in Org " + user.getOrganization().getName() + " (#" + user.getOrganization().getId() + ")");
			pol.setAuthenticated(user.getSessionStatus() == SessionStatusEnumType.AUTHENTICATED);
			try{
				pol.setAccountAdministrator(AuthorizationService.isAccountAdministratorInOrganization(user, user.getOrganization()));
				pol.setDataAdministrator(AuthorizationService.isDataAdministratorInOrganization(user, user.getOrganization()));
				pol.setAccountReader(AuthorizationService.isAccountReaderInOrganization(user, user.getOrganization()));
				pol.setRoleReader(AuthorizationService.isRoleReaderInOrganization(user, user.getOrganization()));
				pol.getRoles().addAll(EffectiveAuthorizationService.getEffectiveRolesForUser(user));
				pol.setAuthenticationId(request.getSession(true).getId());
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
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, Long.toString(id),AuditEnumType.SESSION,request.getSession(true).getId());
		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return def;
		try {
			PolicyType pol = Factories.getPolicyFactory().getById(id, user.getOrganization());
			if(pol == null){
				AuditService.denyResult(audit, "Invalid id: " + Long.toString(id));
				return def;
			}
			AuditService.targetAudit(audit, AuditEnumType.POLICY, "Policy " + pol.getName() + " (#" + Long.toString(pol.getId()) + ")");
			if(AuthorizationService.canViewGroup(user, pol.getGroup()) == false){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + pol.getGroup().getName() + " (#" + pol.getGroup().getId() + ")");
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
