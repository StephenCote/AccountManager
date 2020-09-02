package org.cote.accountmanager.data.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.ControlFactory;
import org.cote.accountmanager.data.factory.FactFactory;
import org.cote.accountmanager.data.factory.INameIdGroupFactory;
import org.cote.accountmanager.data.factory.OperationFactory;
import org.cote.accountmanager.data.factory.PatternFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.factory.RequestFactory;
import org.cote.accountmanager.data.factory.RuleFactory;
import org.cote.accountmanager.data.factory.FunctionFactory;
import org.cote.accountmanager.data.security.RequestService;
import org.cote.accountmanager.data.util.UrnUtil;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccessRequestType;
import org.cote.accountmanager.objects.ApprovalResponseEnumType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.ControlActionEnumType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FunctionEnumType;
import org.cote.accountmanager.objects.FunctionType;
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
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.FileUtil;
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
	private static List<RuleType> importRules(UserType user, String sessionId, DirectoryGroupType policyParentDir, List<RuleType> rules) throws ArgumentException, FactoryException, DataException {
		DirectoryGroupType rdir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", policyParentDir.getPath() + "/Rules", user);
		List<RuleType> outRules = new ArrayList<>();
		for(RuleType rule : rules) {
			List<RuleType> childRules = importRules(user, sessionId, policyParentDir, rule.getRules());
			List<PatternType> childPatterns = importPatterns(user, sessionId, policyParentDir, rule.getPatterns());
			
			RuleType outRule = BaseService.readByName(AuditEnumType.RULE, rdir, rule.getName(),user);
			if(outRule == null){
				outRule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).newRule(user, rdir.getId());
				outRule.setName(rule.getName());
				outRule.setGroupPath(rdir.getPath());
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.RULE, outRule);

			}
			else {
				BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.RULE, outRule);

			}
			outRule.setRuleType(rule.getRuleType());
			outRule.setCondition(rule.getCondition());
			outRule.getRules().clear();
			outRule.getPatterns().clear();
			outRule.getPatterns().addAll(childPatterns);
			outRule.getRules().addAll(childRules);
			outRules.add(outRule);
		}
		return outRules;
	}
	private static List<PatternType> importPatterns(UserType user, String sessionId, DirectoryGroupType policyParentDir, List<PatternType> patterns) throws ArgumentException, FactoryException, DataException {
		DirectoryGroupType pdir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", policyParentDir.getPath() + "/Patterns", user);
		List<PatternType> outPatterns = new ArrayList<>();
		for(PatternType pattern : patterns) {
			
			
			PatternType outPattern = BaseService.readByName(AuditEnumType.PATTERN, pdir, pattern.getName(),user);
			if(outPattern == null){
				outPattern = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).newPattern(user, pdir.getId());
				outPattern.setName(pattern.getName());
				outPattern.setGroupPath(pdir.getPath());
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PATTERN, outPattern);
			}
			else {
				BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.PATTERN, outPattern);
			}
			if(pattern.getPatternType() != null) outPattern.setPatternType(pattern.getPatternType());
			if(pattern.getComparator() != null) outPattern.setComparator(pattern.getComparator());


			if(pattern.getOperation() != null) outPattern.setOperation(importOperation(user, sessionId, policyParentDir, pattern.getOperation()));
			if(pattern.getFact() != null) outPattern.setFact(importFact(user,sessionId,policyParentDir,pattern.getFact()));
			if(pattern.getMatch() != null) outPattern.setMatch(importFact(user,sessionId,policyParentDir,pattern.getMatch()));
			if(outPattern.getFact() != null) outPattern.setFactUrn(UrnUtil.getUrn(outPattern.getFact()));
			if(outPattern.getMatch() != null) outPattern.setMatchUrn(UrnUtil.getUrn(outPattern.getMatch()));
			outPatterns.add(outPattern);
		}
		return outPatterns;
	}
	private static FactType importFact(UserType user, String sessionId, DirectoryGroupType policyParentDir, FactType inFact) throws ArgumentException, FactoryException, DataException {
		DirectoryGroupType fdir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", policyParentDir.getPath() + "/Facts", user);

		FactType outFact = BaseService.readByName(AuditEnumType.FACT, fdir, inFact.getName(), user);
		if(outFact == null) {
			outFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(user, fdir.getId());
			outFact.setName(inFact.getName());
			outFact.setGroupPath(fdir.getPath());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.FACT, outFact);
		}
		else {
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.FACT, outFact);
		}
		outFact.setDescription(inFact.getDescription());
		if(inFact.getFactoryType() != null) outFact.setFactoryType(inFact.getFactoryType());
		if(inFact.getFactType() != null) outFact.setFactType(inFact.getFactType());
		outFact.setSourceType(inFact.getSourceType());
		outFact.setSourceUrl(inFact.getSourceUrl());
		outFact.setSourceUrn(inFact.getSourceUrn());
		if(outFact.getFactType().equals(FactEnumType.FUNCTION) && outFact.getSourceUrl() != null) {
			FunctionType func = importFunction(user, sessionId, policyParentDir, outFact.getSourceUrl());
			if(func != null) outFact.setSourceUrn(UrnUtil.getUrn(func));
		}
		
		return outFact;
	}
	private static OperationType importOperation(UserType user, String sessionId, DirectoryGroupType policyParentDir, OperationType operation) throws ArgumentException, FactoryException {
		DirectoryGroupType odir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", policyParentDir.getPath() + "/Operations", user);

		OperationType expOp = BaseService.readByName(AuditEnumType.OPERATION, odir, operation.getName(), user);

		if(expOp == null){
			expOp = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).newOperation(user, odir.getId());
			expOp.setName(operation.getName());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.OPERATION, expOp);
		}
		else {
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.OPERATION, expOp);
		}
		if(operation.getOperationType() != null) expOp.setOperationType(operation.getOperationType());
		expOp.setOperation(operation.getOperation());
		expOp.setGroupPath(odir.getPath());
		return expOp;
	}
	private static DataType importTextData(UserType user, String sessionId, DirectoryGroupType policyParentDir, String name, String contents) throws ArgumentException, FactoryException, DataException {
		DirectoryGroupType ddir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", policyParentDir.getPath() + "/Data", user);
		DataType outData = BaseService.readByName(AuditEnumType.DATA, ddir, name, user);
		if(outData == null) {
			outData = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(user, ddir.getId());
			outData.setMimeType("text/plain");
			outData.setName(name);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.DATA, outData);
		}
		else {
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.DATA, outData);
		}
		DataUtil.setValue(outData, contents.getBytes());
		return outData;

	}
	private static FunctionType importFunction(UserType user, String sessionId, DirectoryGroupType policyParentDir, String functionUrl) throws ArgumentException, FactoryException, DataException {
		DirectoryGroupType fudir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", policyParentDir.getPath() + "/Functions", user);

		FunctionType expFunc = null;
		String functionName = null;
		String functionDataText = null;
		/// logger.info("Handling: " + functionUrl);
		if(functionUrl.startsWith("file://")) {
			functionUrl = functionUrl.replace("file://", "");
			File f = new File(functionUrl);
			if(!f.exists()) {
				logger.error("File " + functionUrl + " does not exist");
				return null;
			}
			functionName = f.getName();
			functionDataText = FileUtil.getFileAsString(f);
			if(functionDataText == null)
			{
				logger.error("File contents for " + functionUrl  + " were null");
				return null;
			}
			expFunc = BaseService.readByName(AuditEnumType.FUNCTION, fudir, functionName, user); 
		}
		else {
			logger.error("Unhandled protocol: " + functionUrl);
			return null;
			//BaseService.readByName(AuditEnumType.FUNCTION, fudir, function.getName(), user);
		}
		if(expFunc == null){
			expFunc = ((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).newFunction(user, fudir.getId());
			expFunc.setName(functionName);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.FUNCTION, expFunc);
		}
		else {
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.FUNCTION, expFunc);
		}
		expFunc.setFunctionType(FunctionEnumType.JAVASCRIPT);
		DataType data = importTextData(user, sessionId, policyParentDir, functionName, functionDataText);
		if(data != null) expFunc.setSourceUrn(UrnUtil.getUrn(data));
		expFunc.setGroupPath(fudir.getPath());
		return expFunc;
	}
	public static PolicyType importPolicy(UserType user, DirectoryGroupType policyParentDir, String policyJson) throws ArgumentException, FactoryException, DataAccessException, DataException {
		PolicyType outPolicy = null;
		PolicyType impPolicy = JSONUtil.importObject(policyJson, PolicyType.class);
		if(impPolicy == null) {
			logger.error("Failed to import policy from provided JSON");
			return outPolicy;
		}

		DirectoryGroupType podir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", policyParentDir.getPath() + "/Policies", user);
		if(podir == null) {
			logger.error("Policy directory is null from " + policyParentDir.getPath() + "/Policies");
			return outPolicy;
		}
		outPolicy = BaseService.readByName(AuditEnumType.POLICY, podir, impPolicy.getName(), user);
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		
		if(outPolicy == null) {
			outPolicy = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).newPolicy(user, podir.getId());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.POLICY, outPolicy);
		}
		else {
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.POLICY, outPolicy);
		}
		if(impPolicy.getCondition() != null) outPolicy.setCondition(impPolicy.getCondition());
		outPolicy.setName(impPolicy.getName());
		outPolicy.setEnabled(impPolicy.getEnabled());
		outPolicy.setGroupPath(podir.getPath());

		outPolicy.getRules().clear();
		outPolicy.getRules().addAll(importRules(user, sessionId, policyParentDir, impPolicy.getRules()));
		BulkFactories.getBulkFactory().write(sessionId);
		outPolicy = BaseService.readByName(AuditEnumType.POLICY, podir, impPolicy.getName(), user);
		return outPolicy;
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
			/// pat.setPatternType(PatternEnumType.APPROVAL);
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
