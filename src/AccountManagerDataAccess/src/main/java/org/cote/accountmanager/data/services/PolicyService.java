package org.cote.accountmanager.data.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.ControlFactory;
import org.cote.accountmanager.data.factory.FactFactory;
import org.cote.accountmanager.data.factory.INameIdGroupFactory;
import org.cote.accountmanager.data.factory.OperationFactory;
import org.cote.accountmanager.data.factory.PatternFactory;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.factory.RequestFactory;
import org.cote.accountmanager.data.factory.RuleFactory;
import org.cote.accountmanager.data.security.RequestService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccessRequestType;
import org.cote.accountmanager.objects.ApprovalResponseEnumType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.ControlActionEnumType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationEnumType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.JSONUtil;

public class PolicyService {
	public static final Logger logger = LogManager.getLogger(PolicyService.class);

	private static Map<Long,UserType> policyUserMap = new HashMap<>();
	private static String policyUserName = "PolicyUser";
	public static UserType getPolicyUser(long organizationId) throws FactoryException, ArgumentException{
		if(policyUserMap.containsKey(organizationId)) return policyUserMap.get(organizationId);
		UserType chkUser = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(policyUserName, organizationId);
		if(chkUser == null){
			AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "ApiConnectionConfigurationService", AuditEnumType.USER, policyUserName);
			PersonService.createUserAsPerson(audit, policyUserName, UUID.randomUUID().toString(), "PolicyUser@example.com", UserEnumType.SYSTEM,UserStatusEnumType.RESTRICTED , organizationId);
			chkUser = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(policyUserName, organizationId);
		}
		if(chkUser != null) policyUserMap.put(organizationId, chkUser);
		return chkUser;
	}
	
	public static PatternType getCreatePattern(UserType user, String name, String factUrn, String matchUrn, DirectoryGroupType dir){

		PatternType pattern = null;

		try {
			pattern = BaseService.readByName(AuditEnumType.PATTERN, dir, name,user);
			if(pattern == null){
				pattern = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).newPattern(user, dir.getId());
				pattern.setName(name);
				pattern.setPatternType(PatternEnumType.EXPRESSION);
				pattern.setComparator(ComparatorEnumType.EQUALS);

				pattern.setFactUrn(factUrn);
				pattern.setMatchUrn(matchUrn);
				pattern.setGroupPath(dir.getPath());
				if(BaseService.add(AuditEnumType.PATTERN, pattern, user)){
					pattern = BaseService.readByName(AuditEnumType.PATTERN, dir, name, user);
				}
				else pattern = null;
			}
		} catch (ArgumentException | FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return pattern;
	}
	
	public static PolicyType getCreatePolicy(UserType user, String name, DirectoryGroupType dir){
		return getCreatePolicy(user, name, dir, new RuleType[0]);
	}
	public static PolicyType getCreatePolicy(UserType user, String name, DirectoryGroupType dir, RuleType[] rules){

		PolicyType policy = null;

		try {
			policy = BaseService.readByName(AuditEnumType.POLICY, dir, name,user);
			if(policy == null){
				policy = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).newPolicy(user, dir.getId());
				policy.setCondition(ConditionEnumType.ALL);
				policy.setName(name);
				policy.setEnabled(true);
				policy.getRules().addAll(Arrays.asList(rules));
				policy.setGroupPath(dir.getPath());
				if(BaseService.add(AuditEnumType.POLICY, policy, user)){
					policy = BaseService.readByName(AuditEnumType.POLICY, dir, name, user);
				}
				else policy = null;
			}
		} catch (ArgumentException | FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return policy;
	}
	
	public static RuleType getCreateRule(UserType user, String name, DirectoryGroupType dir){
		return getCreateRule(user,name,RuleEnumType.PERMIT,dir);
	}
	public static RuleType getCreateRule(UserType user, String name, RuleEnumType ruleType, DirectoryGroupType dir){
		return getCreateRule(user, name, ruleType, dir, new PatternType[0]);
	}
	public static RuleType getCreateRule(UserType user, String name, RuleEnumType ruleType, DirectoryGroupType dir, PatternType[] patterns){
		RuleType rule = null;

		try {
			rule = BaseService.readByName(AuditEnumType.RULE, dir, name,user);
			if(rule == null){
				rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).newRule(user, dir.getId());
				rule.setName(name);
				rule.setRuleType(ruleType);
				rule.setCondition(ConditionEnumType.ALL);
				rule.getPatterns().addAll(Arrays.asList(patterns));
				rule.setGroupPath(dir.getPath());
				if(BaseService.add(AuditEnumType.RULE, rule, user)){
					rule = BaseService.readByName(AuditEnumType.RULE, dir, name,user);
				}
				else rule = null;
			}
		} catch (ArgumentException | FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return rule;
	}
	
	public static OperationType getCreateOperation(UserType user, String name, String className, DirectoryGroupType dir) throws FactoryException, ArgumentException{
		OperationType op = BaseService.readByName(AuditEnumType.OPERATION, dir, name,user);

		if(op == null){
			op = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).newOperation(user, dir.getId());
			op.setName(name);
			op.setOperationType(OperationEnumType.INTERNAL);
			op.setOperation(className);
			op.setGroupPath(dir.getPath());
			if(BaseService.add(AuditEnumType.OPERATION, op, user)) {
				logger.info("Added new operation '" + name + "'");
				op = BaseService.readByName(AuditEnumType.OPERATION, dir, name, user);
			}
			else {
				logger.error("Failed to add new operation '" + name + "'");
				logger.error(JSONUtil.exportObject(op));
				op = null;
			}
		}
		return op;
	}
	
	public static FactType getCreateEntitlementParamFact(UserType user,String name, DirectoryGroupType fdir) throws ArgumentException, FactoryException{
		FactType srcFact = BaseService.readByName(AuditEnumType.FACT, fdir, name,user);
		if(srcFact != null) return srcFact;
		srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(user, fdir.getId());
		srcFact.setName(name);
		srcFact.setFactType(FactEnumType.PARAMETER);
		srcFact.setFactoryType(FactoryEnumType.UNKNOWN);
		srcFact.setGroupPath(fdir.getPath());
		if(BaseService.add(AuditEnumType.FACT, srcFact, user))
			srcFact = BaseService.readByName(AuditEnumType.FACT, fdir, name,user);
		else
			srcFact = null;
		return srcFact;
	}
	public static FactType getCreateOperationFact(UserType user,String name, String opUrn, DirectoryGroupType fdir) throws ArgumentException, FactoryException{
		FactType srcFact = BaseService.readByName(AuditEnumType.FACT, fdir, name,user);
		if(srcFact != null) return srcFact;
		srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(user, fdir.getId());
		srcFact.setName(name);
		srcFact.setFactType(FactEnumType.OPERATION);
		srcFact.setFactoryType(FactoryEnumType.OPERATION);
		srcFact.setSourceUrn(opUrn);
		srcFact.setGroupPath(fdir.getPath());
		if(BaseService.add(AuditEnumType.FACT, srcFact, user))
			srcFact = BaseService.readByName(AuditEnumType.FACT, fdir, name,user);
		else
			srcFact = null;
		return srcFact;
	}

	public static List<AccessRequestType> getAccessRequests(NameIdType object, UserType contextUser) throws ArgumentException, FactoryException{
		List<AccessRequestType> reqs = new ArrayList<>();
		if(BaseService.canChangeType(AuditEnumType.valueOf(object.getNameType().toString()),contextUser, object)) {
			RequestFactory rFact = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST));
			reqs = rFact.getAccessRequestsForType(contextUser, null, null, object, ApprovalResponseEnumType.REQUEST,0L, contextUser.getOrganizationId());
		}
		else {
			logger.warn("User " + contextUser.getUrn() + " not authorized to access requests for " + object.getUrn());
		}
		return reqs;
	}
	
	public static List<ControlType> getAccessControls(NameIdType object) throws FactoryException, ArgumentException{
		return getControls(object, ControlActionEnumType.ACCESS, true, false);
	}	
	
	private static List<ControlType> getControls(NameIdType object, ControlActionEnumType controlActionType, boolean includeGlobal, boolean onlyGlobal) throws FactoryException, ArgumentException{
		return ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).getControlsForType(object, ControlEnumType.POLICY, 0L, controlActionType, includeGlobal, onlyGlobal);
	}
	
	public static List<PolicyType> getRequestPolicies(AccessRequestType art){
		List<PolicyType> pols = new ArrayList<>();
		try {
			pols= RequestService.getRequestPolicies(art);
		} catch (FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
		}
		return pols;
	}
	
	public static boolean attachOwnerPolicyControl(UserType owner, NameIdType object) throws ArgumentException, FactoryException {
		PolicyType ownerPolicy = getOwnerApprovalPolicy(owner.getOrganizationId());
		return attachPolicyControl(owner, object, ownerPolicy);
	}

	public static boolean attachPolicyControl(UserType owner, NameIdType object, PolicyType policy) throws ArgumentException, FactoryException {
		ControlType control = ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).newControl(owner, object);
		control.setControlId(policy.getId());
		control.setControlType(ControlEnumType.POLICY);
		control.setControlAction(ControlActionEnumType.ACCESS);
		return ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).add(control);
	}
	
	public static List<PolicyType> getSystemPolicies(long organizationId) {
		return getSystemObjects(FactoryEnumType.POLICY, "~/Policies", organizationId);
	}
	public static List<RuleType> getSystemRules(long organizationId) {
		return getSystemObjects(FactoryEnumType.RULE, "~/Rules", organizationId);
	}
	public static List<PolicyType> getSystemPatterns(long organizationId) {
		return getSystemObjects(FactoryEnumType.PATTERN, "~/Patterns", organizationId);
	}
	public static List<PolicyType> getSystemFacts(long organizationId) {
		return getSystemObjects(FactoryEnumType.FACT, "~/Facts", organizationId);
	}
	public static List<PolicyType> getSystemOperations(long organizationId) {
		return getSystemObjects(FactoryEnumType.OPERATION, "~/Operations", organizationId);
	}
	protected static <T> List<T> getSystemObjects(FactoryEnumType fType, String path, long organizationId) {
		UserType polUser = null;
		List<T> objs = new ArrayList<>();
		try {
			polUser = getPolicyUser(organizationId);
			DirectoryGroupType podir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", path, polUser);
			INameIdGroupFactory iFact = (INameIdGroupFactory)Factories.getFactory(fType);
			objs = iFact.listInGroup(podir, 0L, 0, organizationId);
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		return objs;
	}
	public static PolicyType getOwnerApprovalPolicy(long organizationId) {
		UserType polUser = null;
		PolicyType policy = null;
		try {
			polUser = getPolicyUser(organizationId);
			enablePublicReadOnPolicy(polUser);
			policy = getOwnerApprovalPolicy(polUser);
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			logger.error(e.getMessage());
		}
		return policy;
		
	}
	
	public static PolicyType getOwnerApprovalPolicy(UserType user){
		return getCreateOperationPolicy(
			user,
			"Owner Access Approval Policy",
			"Owner Access Approval Rule",
			"Owner Approval Pattern",
			"Access Approval Operation",
			"org.cote.accountmanager.data.operation.AccessApprovalOperation",
			"Lookup Owner Operation",
			"org.cote.accountmanager.data.operation.LookupOwnerOperation",
			"Entitlement Parameter",
			"Entitlement Owner"
		);
	}
	
	public static PolicyType getPrincipalApprovalPolicy(long organizationId) {
		UserType polUser = null;
		PolicyType policy = null;
		try {
			polUser = getPolicyUser(organizationId);
			enablePublicReadOnPolicy(polUser);
			policy = getPrincipalApprovalPolicy(polUser);
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			logger.error(e.getMessage());
		}
		return policy;
		
	}
	
	public static PolicyType getPrincipalApprovalPolicy(UserType user){
		return getCreateOperationPolicy(
			user,
			"Principal Access Approval Policy",
			"Principal Access Approval Rule",
			"Principal Approval Pattern",
			"Access Approval Operation",
			"org.cote.accountmanager.data.operation.AccessApprovalOperation",
			"Lookup Access Operation",
			"org.cote.accountmanager.data.operation.LookupAccessOperation",
			"Entitlement Parameter",
			"Entitlement Approver"
		);
	}
	
	public static void enablePublicReadOnPolicy(UserType owner) throws DataAccessException, FactoryException, ArgumentException {
		UserRoleType acctUsers = RoleService.getAccountUsersRole(owner.getOrganizationId());
		DirectoryGroupType rdir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Rules", owner);
		DirectoryGroupType pdir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Patterns", owner);
		DirectoryGroupType fdir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Facts", owner);
		DirectoryGroupType podir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Policies", owner);
		DirectoryGroupType odir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Operations", owner);
		if(!AuthorizationService.canView(acctUsers, rdir)) {
			AuthorizationService.authorizeType(owner, acctUsers, rdir, true, false, false, false);
			AuthorizationService.authorizeType(owner, acctUsers, pdir, true, false, false, false);
			AuthorizationService.authorizeType(owner, acctUsers, fdir, true, false, false, false);
			AuthorizationService.authorizeType(owner, acctUsers, podir, true, false, false, false);
			AuthorizationService.authorizeType(owner, acctUsers, odir, true, false, false, false);
		}
		
	}
	
	private static PolicyType getCreateOperationPolicy(
			UserType owner,
			String policyName,
			String ruleName,
			String patternName,
			String patternOperationName,
			String patternOperationClass,
			String factOperationName,
			String factOperationClass,
			String parameterFactName,
			String matchFactName
		){

		PolicyType pol = null;

		try {

			DirectoryGroupType rdir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Rules", owner);
			DirectoryGroupType pdir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Patterns", owner);
			DirectoryGroupType fdir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Facts", owner);
			DirectoryGroupType podir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Policies", owner);
			DirectoryGroupType odir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Operations", owner);

			/// Access Approval Operation is an operation used at the Pattern-level
			/// 
			OperationType rgOp = getCreateOperation(owner,patternOperationName,patternOperationClass,odir);

			/// Lookup Owner Operation is an operation used at the Fact-level
			OperationType rgOp2 = getCreateOperation(owner,factOperationName,factOperationClass,odir);
			
			if(rgOp == null || rgOp2 == null) {
				logger.warn("Failed to obtain operation object");
				return null;
			}
			
			FactType approveEntitlementParamFact = org.cote.accountmanager.data.services.PolicyService.getCreateEntitlementParamFact(owner,parameterFactName,fdir);
			FactType ownerEntitlementFact = org.cote.accountmanager.data.services.PolicyService.getCreateOperationFact(owner,matchFactName,rgOp2.getUrn(),fdir);

			pol = getCreatePolicy(owner,policyName,podir);
			pol.setEnabled(true);
			RuleType useRule = getCreateRule(owner,ruleName,rdir);

			useRule.setRuleType(RuleEnumType.PERMIT);

			PatternType pat = org.cote.accountmanager.data.services.PolicyService.getCreatePattern(owner,patternName,approveEntitlementParamFact.getUrn(),ownerEntitlementFact.getUrn(),pdir);
			pat.setPatternType(PatternEnumType.APPROVAL);
			pat.setPatternType(PatternEnumType.OPERATION);
			pat.setOperationUrn(rgOp.getUrn());
			((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).update(pat);
			useRule.getPatterns().clear();
			useRule.getPatterns().add(pat);
			((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).update(useRule);
			pol.getRules().clear();
			pol.getRules().add(useRule);
			((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).update(pol);
		}
		catch(FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
		} 
		return pol;
	}
	
	
}
