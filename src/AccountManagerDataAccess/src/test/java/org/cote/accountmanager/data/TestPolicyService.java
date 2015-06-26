package org.cote.accountmanager.data;

import java.util.List;
import java.util.UUID;

import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.ApplicationPermissionType;
import org.cote.accountmanager.objects.AttributeType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FunctionEnumType;
import org.cote.accountmanager.objects.FunctionFactType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationEnumType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseEnumType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestPolicyService extends BaseDataAccessTest{
	
	@Test
	public void TestFactoriesReady(){
		Factories.getFactFactory();
		Factories.getFunctionFactFactory();
		Factories.getFunctionFactory();
		Factories.getFunctionParticipationFactory();
		Factories.getOperationFactory();
		Factories.getPatternFactory();
		Factories.getPolicyFactory();
		Factories.getRuleFactory();
	}


	/*
	@Test
	public void TestFact(){
		FactType fact = null;
		String name = UUID.randomUUID().toString();
		try {
			DirectoryGroupType funcDir = Factories.getGroupFactory().getCreatePath(testUser, "~/Facts", testUser.getOrganization());
			fact = Factories.getFactFactory().newFact(testUser, funcDir);
			fact.setName(name);
			fact.setUrn(name);
			fact.setFactType(FactEnumType.STATIC);
			fact.setFactData("Demo data");
			if(Factories.getFactFactory().addFact(fact)){
				fact = Factories.getFactFactory().getByName(name, funcDir);
			}
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		assertNotNull("Fact is null",fact);
		logger.info("Created and retrieved fact " + name);
	}
	
	@Test
	public void TestBulkFact(){
		boolean wrote = false;
		
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		try {
			DirectoryGroupType funcDir = Factories.getGroupFactory().getCreatePath(testUser, "~/Facts", testUser.getOrganization());
			for(int i = 0; i < 10; i++){
				FactType func = Factories.getFactFactory().newFact(testUser, funcDir);
				String name = UUID.randomUUID().toString();
				func.setName(name);
				func.setUrn(name);
				func.setDescription("Bulk Test");
				func.setFactType(FactEnumType.STATIC);
				func.setFactData("Bulk data");
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.FACT, func);
			}
			BulkFactories.getBulkFactory().write(sessionId);
			wrote = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Failed to write bulk session",wrote);
		logger.info("Wrote bulk session");
	}
	*/
	
	private FactType getCreateStaticFact(UserType user, DirectoryGroupType dir, String name, String urn, String value){
		FactType fact = null;
		try {
			fact = Factories.getFactFactory().getByName(name,dir);
			if(fact == null){
				
				fact = Factories.getFactFactory().newFact(user, dir);
				fact.setName(name);
				//fact.setUrn(urn);
				fact.setFactType(FactEnumType.STATIC);
				fact.setFactData(value);
				if(Factories.getFactFactory().addFact(fact)){
					fact = Factories.getFactFactory().getByName(name, dir);
				}
				else fact = null;
			}
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		return fact;
	}
	
	private FunctionFactType getCreateFunctionFact(UserType user, FunctionType func, FactType fact, int order, DirectoryGroupType dir){
		FunctionFactType ffact = null;
		String name = func.getUrn() + "::" + fact.getUrn();
		try {
			ffact = Factories.getFunctionFactFactory().getByName(name, dir);
			if(ffact == null){
				ffact = Factories.getFunctionFactFactory().newFunctionFact(user, dir);
				ffact.setFactUrn(fact.getUrn());
				ffact.setFunctionUrn(func.getUrn());
				ffact.setName(name);
				//ffact.setUrn(name);
				ffact.setLogicalOrder(order);
				if(Factories.getFunctionFactFactory().addFunctionFact(ffact)){
					ffact = Factories.getFunctionFactFactory().getByName(name,dir);
				}
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ffact;
	}
	
	private FunctionType getCreateFunction(UserType user, String name, String urn, String sourceUrl, String sourceUrn, DirectoryGroupType dir){

		FunctionType func = null;

		try {
			func = Factories.getFunctionFactory().getByName(name, dir);
			if(func == null){
				func = Factories.getFunctionFactory().newFunction(testUser, dir);
				func.setName(name);
				//func.setUrn(name);
				func.setFunctionType(FunctionEnumType.JAVASCRIPT);
				func.setSourceUrl(sourceUrl);
				func.setSourceUrn(sourceUrn);
				
				if(Factories.getFunctionFactory().addFunction(func)){
					func = Factories.getFunctionFactory().getByName(name, dir);
				}
				else func = null;
			}
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return func;
	}

	private PolicyType getCreatePolicy(UserType user, String name, String urn, DirectoryGroupType dir){

		PolicyType policy = null;

		try {
			policy = Factories.getPolicyFactory().getByName(name, dir);
			if(policy == null){
				policy = Factories.getPolicyFactory().newPolicy(testUser, dir);
				policy.setCondition(ConditionEnumType.ALL);
				policy.setName(name);
				//policy.setUrn(name);

				if(Factories.getPolicyFactory().addPolicy(policy)){
					policy = Factories.getPolicyFactory().getByName(name, dir);
				}
				else policy = null;
			}
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return policy;
	}
	private PatternType getCreatePattern(UserType user, String name, String urn, String factUrn, String matchUrn, DirectoryGroupType dir){

		PatternType pattern = null;

		try {
			pattern = Factories.getPatternFactory().getByName(name, dir);
			if(pattern == null){
				pattern = Factories.getPatternFactory().newPattern(testUser, dir);
				pattern.setName(name);
				//pattern.setUrn(name);
				pattern.setPatternType(PatternEnumType.UNKNOWN);
				pattern.setComparator(ComparatorEnumType.EQUALS);
				pattern.setFactUrn(factUrn);
				pattern.setMatchUrn(matchUrn);
				if(Factories.getPatternFactory().addPattern(pattern)){
					pattern = Factories.getPatternFactory().getByName(name, dir);
				}
				else pattern = null;
			}
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pattern;
	}
	private RuleType getCreateRule(UserType user, String name, String urn, DirectoryGroupType dir){

		RuleType rule = null;

		try {
			rule = Factories.getRuleFactory().getByName(name, dir);
			if(rule == null){
				rule = Factories.getRuleFactory().newRule(testUser, dir);
				rule.setName(name);
				//rule.setUrn(name);
				rule.setRuleType(RuleEnumType.PERMIT);
				
				if(Factories.getRuleFactory().addRule(rule)){
					rule = Factories.getRuleFactory().getByName(name, dir);
				}
				else rule = null;
			}
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rule;
	}
	private <T> T getCreatePermission(UserType user, String name, PermissionEnumType type, DirectoryGroupType basePath) throws FactoryException, ArgumentException, DataAccessException{
		Factories.getGroupFactory().populate(basePath);
		return Factories.getPermissionFactory().makePath(user, type, basePath.getPath() + "/" + name,user.getOrganization());
	}
	private RuleType getCreatePersonAuthorizationRule(UserType user, DirectoryGroupType rdir, DirectoryGroupType pdir, DirectoryGroupType fdir, DirectoryGroupType odir){
		String rname = "urn:am.rule.identity.person";
		//String oname = "urn:am.operation.lookup.linkattributes";
		String pname = "urn:am.pattern.authorize.person";
		//String pname2 = "urn:am.pattern.permission.entitlement1";
		String pfname = "urn:am.fact.parameter.person";
		String pmname = "urn:am.fact.permission.entitlement1";
		//String pmname2 = "urn:am.fact.lookup.account";
		RuleType rule = null;

		try {
			Factories.getUserFactory().populate(user);
			Factories.getGroupFactory().populate(user.getHomeDirectory());
			AccountRoleType demoRole = Factories.getRoleFactory().makePath(user, RoleEnumType.ACCOUNT, user.getHomeDirectory().getPath() + "/Roles/DemoRole",user.getOrganization());
			ApplicationPermissionType perm = getCreatePermission(user,"Entitlement1",PermissionEnumType.APPLICATION,user.getHomeDirectory());
			DirectoryGroupType demoGroup = Factories.getGroupFactory().getCreateDirectory(user, "DemoGroup", user.getHomeDirectory(), user.getOrganization());
			Factories.getGroupFactory().populate(demoGroup);
			Factories.getGroupFactory().populate(demoGroup.getParentGroup());
			rule = Factories.getRuleFactory().getByName(rname, rdir);
			
			if(rule != null){
				Factories.getRuleFactory().deleteRule(rule);
				rule = null;
			}
			
			if(rule == null){
				logger.info("Creating test rule");
				rule = Factories.getRuleFactory().newRule(testUser, rdir);
				rule.setName(rname);
				//rule.setUrn(rname);
				rule.setRuleType(RuleEnumType.PERMIT);
				rule.setCondition(ConditionEnumType.ALL);
				
				PatternType pat = Factories.getPatternFactory().getByName(pname,pdir);
				if(pat != null){
					Factories.getPatternFactory().deletePattern(pat);
					pat = null;
				}
				
				if(pat == null){
					pat = Factories.getPatternFactory().newPattern(testUser, pdir);
					pat.setName(pname);
					//pat.setUrn(pname);
					pat.setPatternType(PatternEnumType.AUTHORIZATION);
					//pat.setFactUrn(pfname);
					//pat.setMatchUrn(pmname);
					pat.setComparator(ComparatorEnumType.EQUALS);
					pat.setLogicalOrder(1);
					FactType srcFact = Factories.getFactFactory().getByName(pfname, fdir);
					if(srcFact != null){
						Factories.getFactFactory().deleteFact(srcFact);
						srcFact = null;
					}
					if(srcFact == null){
						srcFact = Factories.getFactFactory().newFact(testUser, fdir);
						srcFact.setName(pfname);
						//srcFact.setUrn(pfname);
						srcFact.setFactType(FactEnumType.PARAMETER);
						srcFact.setFactoryType(FactoryEnumType.PERSON);
						Factories.getFactFactory().addFact(srcFact);
					}
					FactType mFact = Factories.getFactFactory().getByName(pmname, fdir);
					if(mFact != null){
						Factories.getFactFactory().deleteFact(mFact);
						mFact = null;
					}
					if(mFact == null){
						mFact = Factories.getFactFactory().newFact(testUser, fdir);
						mFact.setName(pmname);
						// mFact.setUrn(pmname);
						
						mFact.setFactType(FactEnumType.ROLE);
						mFact.setFactoryType(FactoryEnumType.ROLE);
						mFact.setSourceUrn(demoRole.getName());
						mFact.setSourceUrl((demoRole.getParentId() > 0L ? Factories.getRoleFactory().getRolePath((BaseRoleType)Factories.getRoleFactory().getRoleById(demoRole.getParentId(), demoRole.getOrganization())) : null));
						mFact.setSourceType(demoRole.getRoleType().toString());
						/*
						mFact.setFactType(FactEnumType.PERMISSION);
						mFact.setFactoryType(FactoryEnumType.GROUP);
						mFact.setSourceUrn(demoGroup.getName());
						mFact.setSourceUrl(demoGroup.getParentGroup().getPath());
						mFact.setFactData(perm.getId().toString());
						*/
						Factories.getFactFactory().addFact(mFact);
					}
					pat.setMatchUrn(mFact.getUrn());
					pat.setFactUrn(srcFact.getUrn());
					if(Factories.getPatternFactory().addPattern(pat)){
						pat = Factories.getPatternFactory().getByName(pname, pdir);
						rule.getPatterns().add(pat);
					}
				}
				
				
				
				if(Factories.getRuleFactory().addRule(rule)){
					rule = Factories.getRuleFactory().getByName(rname, rdir);
				}
				else rule = null;
			}
			Factories.getRuleFactory().populate(rule);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rule;
	}
	
	private RuleType getCreatePersonAccountRule(UserType user, DirectoryGroupType rdir, DirectoryGroupType pdir, DirectoryGroupType fdir, DirectoryGroupType odir){
		String rname = "urn:am.rule.identity.person";
		String oname = "urn:am.operation.lookup.linkattributes";
		String pname = "urn:am.pattern.identity.person";
		String pname2 = "urn:am.pattern.identity.account";
		String pfname = "urn:am.fact.parameter.person";
		String pmname = "urn:am.fact.lookup.person";
		String pmname2 = "urn:am.fact.lookup.account";
		RuleType rule = null;

		try {
			rule = Factories.getRuleFactory().getByName(rname, rdir);
			
			if(rule != null){
				Factories.getRuleFactory().deleteRule(rule);
				rule = null;
			}
			
			if(rule == null){
				logger.info("Creating test rule");
				rule = Factories.getRuleFactory().newRule(testUser, rdir);
				rule.setName(rname);
				//rule.setUrn(rname);
				rule.setRuleType(RuleEnumType.PERMIT);
				rule.setCondition(ConditionEnumType.ANY);
				
				OperationType op = Factories.getOperationFactory().getByName(oname, odir);
				if(op != null){
					Factories.getOperationFactory().deleteOperation(op);
					op = null;
				}
				if(op == null){
					op = Factories.getOperationFactory().newOperation(testUser, odir);
					op.setName(oname);
					//op.setUrn(oname);
					op.setOperationType(OperationEnumType.INTERNAL);
					op.setOperation("org.cote.accountmanager.data.operation.ComparePersonLinkAttributeOperation");
					Factories.getOperationFactory().addOperation(op);
				}
				
				PatternType pat = Factories.getPatternFactory().getByName(pname,pdir);
				if(pat != null){
					Factories.getPatternFactory().deletePattern(pat);
					pat = null;
				}
				
				if(pat == null){
					pat = Factories.getPatternFactory().newPattern(testUser, pdir);
					pat.setName(pname);
					//pat.setUrn(pname);
					pat.setPatternType(PatternEnumType.OPERATION);
					pat.setOperationUrn(oname);
					//pat.setFactUrn(pfname);
					//pat.setMatchUrn(pmname);
					pat.setComparator(ComparatorEnumType.EQUALS);
					pat.setLogicalOrder(1);
					FactType srcFact = Factories.getFactFactory().getByName(pfname, fdir);
					if(srcFact != null){
						Factories.getFactFactory().deleteFact(srcFact);
						srcFact = null;
					}
					if(srcFact == null){
						srcFact = Factories.getFactFactory().newFact(testUser, fdir);
						srcFact.setName(pfname);
						srcFact.setFactType(FactEnumType.PARAMETER);
						srcFact.setFactoryType(FactoryEnumType.PERSON);
						Factories.getFactFactory().addFact(srcFact);
					}
					FactType mFact = Factories.getFactFactory().getByName(pmname, fdir);
					if(mFact != null){
						Factories.getFactFactory().deleteFact(mFact);
						mFact = null;
					}
					if(mFact == null){
						mFact = Factories.getFactFactory().newFact(testUser, fdir);
						mFact.setName(pmname);

						mFact.setFactType(FactEnumType.ATTRIBUTE);
						mFact.setFactoryType(FactoryEnumType.PERSON);
						mFact.setSourceUrn("code");
						mFact.setFactData("11");
						Factories.getFactFactory().addFact(mFact);
					}
					pat.setMatchUrn(mFact.getUrn());
					pat.setFactUrn(srcFact.getUrn());
					if(Factories.getPatternFactory().addPattern(pat)){
						pat = Factories.getPatternFactory().getByName(pname, pdir);
						rule.getPatterns().add(pat);
					}
				}
				
				PatternType pat2 = Factories.getPatternFactory().getByName(pname2,pdir);
				if(pat2 != null){
					Factories.getPatternFactory().deletePattern(pat2);
					pat2 = null;
				}
				
				if(pat2 == null){
					pat2 = Factories.getPatternFactory().newPattern(testUser, pdir);
					pat2.setName(pname2);
					//pat2.setUrn(pname2);
					pat2.setPatternType(PatternEnumType.OPERATION);
					pat2.setOperationUrn(oname);
					//pat2.setFactUrn(pfname);
					//pat2.setMatchUrn(pmname2);
					pat2.setComparator(ComparatorEnumType.EQUALS);
					pat2.setLogicalOrder(2);
					FactType srcFact = Factories.getFactFactory().getByName(pfname, fdir);
					FactType mFact = Factories.getFactFactory().getByName(pmname2, fdir);
					if(mFact != null){
						Factories.getFactFactory().deleteFact(mFact);
						mFact = null;
					}
					if(mFact == null){
						mFact = Factories.getFactFactory().newFact(testUser, fdir);
						mFact.setName(pmname2);
						//mFact.setUrn(pmname2);
						mFact.setFactType(FactEnumType.ATTRIBUTE);
						mFact.setFactoryType(FactoryEnumType.ACCOUNT);
						mFact.setSourceUrn("code");
						mFact.setFactData("11");
						Factories.getFactFactory().addFact(mFact);
					}
					pat2.setMatchUrn(mFact.getUrn());
					pat2.setFactUrn(srcFact.getUrn());
					if(Factories.getPatternFactory().addPattern(pat2)){
						pat2 = Factories.getPatternFactory().getByName(pname2, pdir);
						rule.getPatterns().add(pat2);
					}
				}
				
				if(Factories.getRuleFactory().addRule(rule)){
					rule = Factories.getRuleFactory().getByName(rname, rdir);
				}
				else rule = null;
			}
			Factories.getRuleFactory().populate(rule);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rule;
	}
	private RuleType getCreateIdentityRule(UserType user, DirectoryGroupType rdir, DirectoryGroupType pdir, DirectoryGroupType fdir, DirectoryGroupType odir){
		String rname = "urn:am.rule.identity.validate";
		
		String pname = "urn:am.pattern.identity.exists";
		String pfname = "urn:am.fact.parameter.user";
		String pmname = "urn:am.fact.lookup.user";
		String oname = "urn:am.operation.lookup.user";
		
		String pname2 = "urn:am.pattern.attribute.level";
		String pfname2 = "urn:am.fact.parameter.attribute.level";
		String pmname2 = "urn.am.fact.compare.attribute";
		String oname2 = "urn:am.operation.object.compare";
		RuleType rule = null;
		/*
		UserTypeFact = {
				   .type = FACTORY
				   .factoryType = USER
				   .sourceDataType = sName
				   .sourceUri = sOrgPath
				   .urn = “urn:am.objects.user”[static]
				}
		*/
				/*
	               factUrn = “urn:am.objects.user”
	               patternType = “operation”
	               matchUrn = “urn:am.authn.isauthenticated”
	              */
		try {
			rule = Factories.getRuleFactory().getByName(rname, rdir);
			
			if(rule != null){
				Factories.getRuleFactory().deleteRule(rule);
				rule = null;
			}
			
			if(rule == null){
				logger.info("Creating test rule");
				rule = Factories.getRuleFactory().newRule(testUser, rdir);
				rule.setName(rname);
				//rule.setUrn(rname);
				rule.setRuleType(RuleEnumType.PERMIT);
				
				PatternType pat = Factories.getPatternFactory().getByName(pname,pdir);
				if(pat != null){
					Factories.getPatternFactory().deletePattern(pat);
					pat = null;
				}
				
				if(pat == null){
					pat = Factories.getPatternFactory().newPattern(testUser, pdir);
					pat.setName(pname);
					//pat.setUrn(pname);
					pat.setPatternType(PatternEnumType.OPERATION);
					pat.setLogicalOrder(1);
					pat.setOperationUrn(oname);
					//pat.setFactUrn(pfname);
					//pat.setMatchUrn(pmname);
					pat.setComparator(ComparatorEnumType.EQUALS);
					//pat.setMatchUrn("true");
					
					FactType srcFact = Factories.getFactFactory().getByName(pfname, fdir);
					if(srcFact != null){
						Factories.getFactFactory().deleteFact(srcFact);
						srcFact = null;
					}
					if(srcFact == null){
						srcFact = Factories.getFactFactory().newFact(testUser, fdir);
						srcFact.setName(pfname);
						//srcFact.setUrn(pfname);
						srcFact.setFactType(FactEnumType.PARAMETER);
						srcFact.setFactoryType(FactoryEnumType.USER);
						Factories.getFactFactory().addFact(srcFact);
					}
					FactType mFact = Factories.getFactFactory().getByName(pmname, fdir);
					if(mFact != null){
						Factories.getFactFactory().deleteFact(mFact);
						mFact = null;
					}
					if(mFact == null){
						mFact = Factories.getFactFactory().newFact(testUser, fdir);
						mFact.setName(pmname);
						//mFact.setUrn(pmname);
						mFact.setFactType(FactEnumType.FACTORY);
						//mFact.setSourceUrl(oname);
						mFact.setFactoryType(FactoryEnumType.USER);
						//mFact.setFactoryType(FactoryEnumType.USER);
						Factories.getFactFactory().addFact(mFact);
					}
					OperationType op = Factories.getOperationFactory().getByName(oname, odir);
					if(op != null){
						Factories.getOperationFactory().deleteOperation(op);
						op = null;
					}
					if(op == null){
						op = Factories.getOperationFactory().newOperation(testUser, odir);
						op.setName(oname);
						//op.setUrn(oname);
						op.setOperationType(OperationEnumType.INTERNAL);
						op.setOperation("org.cote.accountmanager.data.operation.LookupUserOperation");
						Factories.getOperationFactory().addOperation(op);
					}
					pat.setMatchUrn(mFact.getUrn());
					pat.setFactUrn(srcFact.getUrn());
					PatternType pat2 = Factories.getPatternFactory().getByName(pname2,pdir);
					if(pat2 != null){
						Factories.getPatternFactory().deletePattern(pat2);
						pat2 = null;
					}
					
					if(pat2 == null){
						pat2 = Factories.getPatternFactory().newPattern(testUser, pdir);
						pat2.setLogicalOrder(2);
						pat2.setName(pname2);
						//pat2.setUrn(pname2);
						pat2.setPatternType(PatternEnumType.OPERATION);
						pat2.setOperationUrn(oname2);
						//pat2.setFactUrn(pfname2);
						//pat2.setMatchUrn(pmname2);
						pat2.setComparator(ComparatorEnumType.GREATER_THAN_OR_EQUALS);

						FactType srcFact2 = Factories.getFactFactory().getByName(pfname2, fdir);
						if(srcFact2 != null){
							Factories.getFactFactory().deleteFact(srcFact2);
							srcFact2 = null;
						}
						if(srcFact2 == null){
							srcFact2 = Factories.getFactFactory().newFact(testUser, fdir);
							srcFact2.setName(pfname2);
							//srcFact2.setUrn(pfname2);
							srcFact2.setFactType(FactEnumType.PARAMETER);
							srcFact2.setFactoryType(FactoryEnumType.DATA);

							Factories.getFactFactory().addFact(srcFact2);
						}
						FactType mFact2 = Factories.getFactFactory().getByName(pmname2, fdir);
						if(mFact2 != null){
							Factories.getFactFactory().deleteFact(mFact2);
							mFact2 = null;
						}
						if(mFact2 == null){
							mFact2 = Factories.getFactFactory().newFact(testUser, fdir);
							mFact2.setName(pmname2);
							//mFact2.setUrn(pmname2);
							mFact2.setFactType(FactEnumType.ATTRIBUTE);
							//mFact2.setSourceUrl(oname2);
							mFact2.setSourceUrn("level");
							mFact2.setFactData("7");
							mFact2.setFactoryType(FactoryEnumType.DATA);
							//mFact.setFactoryType(FactoryEnumType.USER);
							Factories.getFactFactory().addFact(mFact2);
						}
						pat2.setMatchUrn(mFact2.getUrn());
						pat2.setFactUrn(srcFact2.getUrn());
						
						OperationType op2 = Factories.getOperationFactory().getByName(oname2, odir);
						if(op2 != null){
							Factories.getOperationFactory().deleteOperation(op2);
							op2 = null;
						}
						if(op2 == null){
							op2 = Factories.getOperationFactory().newOperation(testUser, odir);
							op2.setName(oname2);
							//op2.setUrn(oname2);
							op2.setOperationType(OperationEnumType.INTERNAL);
							op2.setOperation("org.cote.accountmanager.data.operation.CompareAttributeOperation");
							Factories.getOperationFactory().addOperation(op2);
						}
					}
					
					
					
					if(Factories.getPatternFactory().addPattern(pat) && Factories.getPatternFactory().addPattern(pat2)){
						pat = Factories.getPatternFactory().getByName(pname, pdir);
						pat2 = Factories.getPatternFactory().getByName(pname2, pdir);
						rule.getPatterns().add(pat);
						rule.getPatterns().add(pat2);
					}
				}
				if(Factories.getRuleFactory().addRule(rule)){
					rule = Factories.getRuleFactory().getByName(rname, rdir);
				}
				else rule = null;
			}
			Factories.getRuleFactory().populate(rule);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return rule;
	}
	
	@Test
	public void TestEvaluatePolicy(){
		/// Make sure the test policy exists
		///
		PolicyType pol = getTestPersonAuthorizationPolicy();

		assertNotNull("Policy is null",pol);
		assertNotNull("Policy urn is null",pol.getUrn());
		
		PersonType person = null;
		AccountType account = null;
		
		/// Now test it from the point of view of a policy request
		try {
			DirectoryGroupType pdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Persons", testUser.getOrganization());
			DirectoryGroupType adir = Factories.getGroupFactory().getCreatePath(testUser, "~/Accounts", testUser.getOrganization());
			AccountRoleType demoRole = Factories.getRoleFactory().makePath(testUser, RoleEnumType.ACCOUNT, testUser.getHomeDirectory().getPath() + "/Roles/DemoRole",testUser.getOrganization());
			
			person = Factories.getPersonFactory().getByName("Policy Test Person", pdir);
			if(person == null){
				person = Factories.getPersonFactory().newPerson(testUser, pdir);
				person.setName("Policy Test Person");
				if(Factories.getPersonFactory().addPerson(person)){
					person = Factories.getPersonFactory().getByName("Policy Test Person", pdir);
				}
				else{
					person = null;
				}
			}
			
			account = Factories.getAccountFactory().getByName("Policy Test Account", adir);
			if(account == null){
				account = Factories.getAccountFactory().newAccount(testUser,"Policy Test Account", AccountEnumType.DEVELOPMENT, AccountStatusEnumType.UNREGISTERED, adir);
				account.setName("Policy Test Account");
				if(Factories.getAccountFactory().addAccount(account)){
					account = Factories.getAccountFactory().getByName("Policy Test Account", adir);
				}
				else{
					account = null;
				}
			}
			
			ApplicationPermissionType perm = getCreatePermission(testUser,"Entitlement1",PermissionEnumType.APPLICATION,testUser.getHomeDirectory());
			DirectoryGroupType demoGroup = Factories.getGroupFactory().getCreateDirectory(testUser, "DemoGroup", testUser.getHomeDirectory(), testUser.getOrganization());
			AuthorizationService.setPermission(testUser, account, demoGroup, perm, true);
			RoleService.addAccountToRole(account, demoRole);
			EffectiveAuthorizationService.rebuildCache();
			
			/*
			Factories.getAttributeFactory().populateAttributes(person);
			Factories.getAttributeFactory().populateAttributes(account);
			*/
			Factories.getAttributeFactory().deleteAttributes(person);
			Factories.getAttributeFactory().deleteAttributes(account);
			account.getAttributes().clear();
			person.getAttributes().add(Factories.getAttributeFactory().newAttribute(person, "level", "7"));
			account.getAttributes().add(Factories.getAttributeFactory().newAttribute(account, "level", "7"));
			account.getAttributes().add(Factories.getAttributeFactory().newAttribute(account, "code", "11"));
			
			Factories.getAttributeFactory().updateAttributes(new NameIdType[]{person, account});
			
			Factories.getPersonFactory().populate(person);
			if(person.getAccounts().size() == 0){
				person.getAccounts().add(account);
				Factories.getPersonFactory().updatePerson(person);
			}
			
			//PolicyRequestType prt = new PolicyRequestType();
			PolicyDefinitionType pdt = PolicyDefinitionUtil.generatePolicyDefinition(pol);
			PolicyRequestType prt = PolicyDefinitionUtil.generatePolicyRequest(pdt);
			//prt.setUrn(pol.getUrn());
			prt.setOrganizationPath(Factories.getOrganizationFactory().getOrganizationPath(pol.getOrganization()));
			assertTrue("Expected more than one parameters",prt.getFacts().size() > 0);
			assertNotNull("Fact urn is null",prt.getFacts().get(0).getUrn());
			logger.info("Parameter Count: " + prt.getFacts().size());
			FactType userFact = prt.getFacts().get(0);
			Factories.getGroupFactory().populate(pdir);
			userFact.setSourceUrl(pdir.getPath());
			userFact.setSourceUrn("Policy Test Person");
			//prt.getFacts().add(userFact);
			
			PolicyResponseType prr = PolicyEvaluator.evaluatePolicyRequest(prt);
			logger.info("Policy Evaluation for " + prr.getUrn() + " = " + prr.getResponse().toString());
			
			assertTrue("Policy response was " + prr.getResponse() + "; was expecting PERMIT",prr.getResponse() == PolicyResponseEnumType.PERMIT);
			
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	@Test
	public void TestEvaluatePolicy(){
		/// Make sure the test policy exists
		///
		PolicyType pol = getTestIdentityPolicy();
		assertNotNull("Policy is null",pol);
		
		/// Now test it from the point of view of a policy request
		try {
			PolicyRequestType prt = new PolicyRequestType();
			prt.setUrn(pol.getUrn());
			prt.setOrganizationPath(Factories.getOrganizationFactory().getOrganizationPath(pol.getOrganization()));
			
			FactType userFact = new FactType();
			userFact.setUrn("urn:am.fact.parameter.user");
			userFact.setFactType(FactEnumType.PARAMETER);
			userFact.setFactoryType(FactoryEnumType.USER);
			//userFact.setFactData("RocketQAUser2");
			userFact.setSourceUrn("RocketQAUser2");
			
			DataType testData = this.getData(testUser, "DemoData");
			//Factories.getAttributeFactory().populateAttributes(testData);

			testData.getAttributes().add(Factories.getAttributeFactory().newAttribute(testData, "level", "7"));
			Factories.getAttributeFactory().updateAttributes(testData);
			
			Factories.getGroupFactory().populate(testData.getGroup());
			FactType dataFact = new FactType();
			dataFact.setUrn("urn:am.fact.parameter.attribute.level");
			dataFact.setSourceUrl(testData.getGroup().getPath());
			dataFact.setSourceUrn(testData.getName());
			dataFact.setFactoryType(FactoryEnumType.DATA);
			dataFact.setFactType(FactEnumType.PARAMETER);

			prt.getFacts().add(userFact);
			prt.getFacts().add(dataFact);
			
			PolicyEvaluator.evaluatePolicyRequest(prt);

			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	private boolean evaluatePattern(PatternType pattern,List<FactType> facts){
		boolean out_bool = false;
		try {
			Factories.getPatternFactory().populate(pattern);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		FactType fact = pattern.getFact();
		FactType mfact = pattern.getMatch();
		if(fact == null){
			logger.error("Pattern fact definition is null");
			return out_bool;
		}
		if(mfact == null){
			logger.error("Pattern match fact definition is null");
			return out_bool;
		}
		
		FactType sfact = fact;
		
		/// factory lookup --- PARAM->FACT
		///
		if(fact.getFactType() == FactEnumType.PARAMETER && mfact.getFactType() == FactEnumType.FACTORY){
			sfact = getFactParameter(fact,facts);
			if(sfact == null){
				logger.error("Parameter value not specified for urn " + fact.getUrn());
				return out_bool;
			}
			FactoryBase factory = Factories.getFactory(mfact.getFactoryType());
			if(factory == null){
				logger.error("Invalid factory for type " + mfact.getFactoryType());
			}
			NameIdType obj = null;
			if(mfact.getFactoryType() == FactoryEnumType.USER){
				//obj = ((UserFactory)factory);
			}
		}
		
		return out_bool;
	}
	private FactType getFactParameter(FactType fact, List<FactType> facts){
		FactType ofact = null;
		for(int i = 0; i < facts.size();i++){
			if(facts.get(i).getUrn().equals(fact.getUrn())){
				ofact = facts.get(i);
				break;
			}
		}
		return ofact;
	}
	public PolicyType getTestPersonAuthorizationPolicy(){
		DirectoryGroupType pdir = null;
		DirectoryGroupType rdir = null;
		DirectoryGroupType podir = null;
		DirectoryGroupType odir = null;
		DirectoryGroupType fdir = null;
		PolicyType pol = null;
		RuleType idRule = null;
		String pname = "urn:am.policy.person.authorization";
			try {
				rdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Rules", testUser.getOrganization());
				pdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Patterns", testUser.getOrganization());
				fdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Facts", testUser.getOrganization());
				podir = Factories.getGroupFactory().getCreatePath(testUser, "~/Policies", testUser.getOrganization());
				odir = Factories.getGroupFactory().getCreatePath(testUser, "~/Operations", testUser.getOrganization());
				pol = Factories.getPolicyFactory().getByName(pname, podir);
				if(pol != null){
					Factories.getPolicyFactory().deletePolicy(pol);
					pol = null;
				}
				if(pol == null){
					pol = Factories.getPolicyFactory().newPolicy(testUser, podir);
					pol.setName(pname);
					//pol.setUrn(pname);
					pol.setEnabled(true);
					pol.setCondition(ConditionEnumType.ALL);
					idRule = getCreatePersonAuthorizationRule(testUser, rdir, pdir,fdir,odir);
					if(idRule != null){
						pol.getRules().add(idRule);
						
						if(Factories.getPolicyFactory().addPolicy(pol)){
							pol = Factories.getPolicyFactory().getByName(pname, podir);
						}
						else pol = null;
					}
				}
				if(pol != null){
					Factories.getPolicyFactory().populate(pol);
					//evaluatePolicy(pol);
					logger.info(PolicyDefinitionUtil.printPolicy(pol));
				}
				
				
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//assertNotNull("Policy is null", pol);
			//assertTrue("Policy is not populated",pol.getRules().size() > 0);		
			return pol;

		//assertTrue("Patterns not populated",idRule.getPatterns().size() > 0);
	}
	public PolicyType getTestPersonAccountPolicy(){
		DirectoryGroupType pdir = null;
		DirectoryGroupType rdir = null;
		DirectoryGroupType podir = null;
		DirectoryGroupType odir = null;
		DirectoryGroupType fdir = null;
		PolicyType pol = null;
		RuleType idRule = null;
		String pname = "urn:am.policy.personaccount";
			try {
				rdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Rules", testUser.getOrganization());
				pdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Patterns", testUser.getOrganization());
				fdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Facts", testUser.getOrganization());
				podir = Factories.getGroupFactory().getCreatePath(testUser, "~/Policies", testUser.getOrganization());
				odir = Factories.getGroupFactory().getCreatePath(testUser, "~/Operations", testUser.getOrganization());
				pol = Factories.getPolicyFactory().getByName(pname, podir);
				if(pol != null){
					Factories.getPolicyFactory().deletePolicy(pol);
					pol = null;
				}
				if(pol == null){
					pol = Factories.getPolicyFactory().newPolicy(testUser, podir);
					pol.setCondition(ConditionEnumType.ALL);
					pol.setName(pname);
					//pol.setUrn(pname);
					pol.setEnabled(true);
					idRule = getCreatePersonAccountRule(testUser, rdir, pdir,fdir,odir);
					if(idRule != null){
						pol.getRules().add(idRule);
						
						if(Factories.getPolicyFactory().addPolicy(pol)){
							pol = Factories.getPolicyFactory().getByName(pname, podir);
						}
						else pol = null;
					}
				}
				if(pol != null){
					Factories.getPolicyFactory().populate(pol);
					//evaluatePolicy(pol);
					logger.info(PolicyDefinitionUtil.printPolicy(pol));
				}
				
				
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//assertNotNull("Policy is null", pol);
			//assertTrue("Policy is not populated",pol.getRules().size() > 0);		
			return pol;

		//assertTrue("Patterns not populated",idRule.getPatterns().size() > 0);
	}
	public PolicyType getTestIdentityPolicy(){
		DirectoryGroupType pdir = null;
		DirectoryGroupType rdir = null;
		DirectoryGroupType podir = null;
		DirectoryGroupType fdir = null;
		DirectoryGroupType odir = null;
		PolicyType pol = null;
		RuleType idRule = null;
		String pname = "urn:am.policy.identity";
			try {
				rdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Rules", testUser.getOrganization());
				pdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Patterns", testUser.getOrganization());
				fdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Facts", testUser.getOrganization());
				odir = Factories.getGroupFactory().getCreatePath(testUser, "~/Operations", testUser.getOrganization());
				podir = Factories.getGroupFactory().getCreatePath(testUser, "~/Policies", testUser.getOrganization());
				pol = Factories.getPolicyFactory().getByName(pname, podir);
				if(pol != null){
					Factories.getPolicyFactory().deletePolicy(pol);
					pol = null;
				}
				if(pol == null){
					pol = Factories.getPolicyFactory().newPolicy(testUser, podir);
					pol.setCondition(ConditionEnumType.ALL);
					pol.setName(pname);
					//pol.setUrn(pname);
					pol.setEnabled(true);
					idRule = getCreateIdentityRule(testUser, rdir, pdir,fdir,odir);
					if(idRule != null){
						pol.getRules().add(idRule);
						
						if(Factories.getPolicyFactory().addPolicy(pol)){
							pol = Factories.getPolicyFactory().getByName("urn:am.policy.identity", podir);
						}
						else pol = null;
					}
				}
				if(pol != null){
					Factories.getPolicyFactory().populate(pol);
					//evaluatePolicy(pol);
					logger.info(PolicyDefinitionUtil.printPolicy(pol));
				}
				
				
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//assertNotNull("Policy is null", pol);
			//assertTrue("Policy is not populated",pol.getRules().size() > 0);		
			return pol;

		//assertTrue("Patterns not populated",idRule.getPatterns().size() > 0);
	}

	/*
	@Test
	public void TestRule(){
		DirectoryGroupType dir = null;
		try {
			dir = Factories.getGroupFactory().getCreatePath(testUser, "~/Rules", testUser.getOrganization());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String name = UUID.randomUUID().toString();
		RuleType pol = getCreateRule(testUser,name,name,dir);
		assertNotNull("Rule is null",pol);
	}
	*/
	/*
	@Test
	public void TestPolicy(){
		DirectoryGroupType dir = null;
		DirectoryGroupType rdir = null;
		DirectoryGroupType pdir = null;
		try {
			dir = Factories.getGroupFactory().getCreatePath(testUser, "~/Policies", testUser.getOrganization());
			rdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Rules", testUser.getOrganization());
			pdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Patterns", testUser.getOrganization());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String name = UUID.randomUUID().toString();
		String rname = UUID.randomUUID().toString();
		String rname2 = UUID.randomUUID().toString();
		String pname = UUID.randomUUID().toString();
		String pname2 = UUID.randomUUID().toString();
		PolicyType pol = getCreatePolicy(testUser,name,name,dir);
		RuleType rule1 = getCreateRule(testUser,rname,rname,rdir);
		PatternType pat1 = getCreatePattern(testUser,pname,pname,"urn:pattern1","true",pdir);
		rule1.getPatterns().add(pat1);
		
		RuleType rule2 = getCreateRule(testUser,rname2,rname2,rdir);
		PatternType pat2 = getCreatePattern(testUser,pname2,pname2,"urn:pattern2","true",pdir);
		rule2.getPatterns().add(pat2);
		pol.getRules().add(rule1);
		pol.getRules().add(rule2);
		boolean up = false;
		try {
			Factories.getRuleFactory().updateRule(rule1);
			Factories.getRuleFactory().updateRule(rule2);
			up = Factories.getPolicyFactory().updatePolicy(pol);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Policy not updated",up);
	}
	*/
	
	/*
	@Test
	public void TestFunction(){
		FunctionType func = null;
		String name = UUID.randomUUID().toString();
		try {
			DirectoryGroupType funcDir = Factories.getGroupFactory().getCreatePath(testUser, "~/Functions", testUser.getOrganization());
			DirectoryGroupType factDir = Factories.getGroupFactory().getCreatePath(testUser, "~/Facts", testUser.getOrganization());
			DirectoryGroupType funFactDir = Factories.getGroupFactory().getCreatePath(testUser, "~/FunctionFacts", testUser.getOrganization());
			func = Factories.getFunctionFactory().newFunction(testUser, funcDir);
			func.setName(name);
			func.setUrn(name);
			func.setFunctionType(FunctionEnumType.JAVASCRIPT);
			func.setSourceUrl("/Foo/Bar");
			func.setSourceUrn("Demo");
			
			/// Note there is a unique constraint on the urn
			///
			FactType fact1 = getCreateStaticFact(testUser, factDir, "Default Risk Score", "urn:risk.score.default", "10");
			FactType fact2 = getCreateStaticFact(testUser, factDir, "Mobile Support", "urn:capability.mobile.supported", "true");
			
			/// Note: functions are connected to facts with FunctionFacts
			/// This is an abstraction to allow for variable parameter order, and also allow some parameters to be volatile vs. persisted
			///
			FunctionFactType ffact1 = getCreateFunctionFact(testUser,func,fact1,1,funFactDir);
			FunctionFactType ffact2 = getCreateFunctionFact(testUser,func,fact2,2,funFactDir);
			func.getFacts().add(ffact1);
			func.getFacts().add(ffact2);
			
			if(Factories.getFunctionFactory().addFunction(func)){
				func = Factories.getFunctionFactory().getByName(name, funcDir);
			}
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull("Function is null",func);
		logger.info("Created and retrieved function " + name);
		try {
			Factories.getFunctionFactory().populate(func);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Failed to populated expected facts.  Found " + func.getFacts().size(),func.getFacts().size() == 2);
		logger.info("Populated " + func.getFacts().size() + " facts");
	}
	*/
	/*
	@Test
	public void TestBulkFunction(){
		boolean wrote = false;
		
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		try {
			DirectoryGroupType funcDir = Factories.getGroupFactory().getCreatePath(testUser, "~/Functions", testUser.getOrganization());
			for(int i = 0; i < 10; i++){
				FunctionType func = Factories.getFunctionFactory().newFunction(testUser, funcDir);
				String name = UUID.randomUUID().toString();
				func.setName(name);
				func.setUrn(name);
				func.setDescription("Bulk Test");
				func.setFunctionType(FunctionEnumType.JAVASCRIPT);
				func.setSourceUrl("/Foo/Bar");
				func.setSourceUrn("BulkDemo");
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.FUNCTION, func);
			}
			BulkFactories.getBulkFactory().write(sessionId);
			wrote = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Failed to write bulk session",wrote);
		logger.info("Wrote bulk session");
	}
	*/
}
