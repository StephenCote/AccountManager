package org.cote.accountmanager.data;

import java.util.List;
import java.util.UUID;

import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.objects.AttributeType;
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
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
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
				fact.setUrn(urn);
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
				ffact.setUrn(name);
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
				func.setUrn(name);
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
				policy.setName(name);
				policy.setUrn(name);

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
				pattern.setUrn(name);
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
				rule.setUrn(name);
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
				rule.setUrn(rname);
				rule.setRuleType(RuleEnumType.PERMIT);
				
				PatternType pat = Factories.getPatternFactory().getByName(pname,pdir);
				if(pat != null){
					Factories.getPatternFactory().deletePattern(pat);
					pat = null;
				}
				
				if(pat == null){
					pat = Factories.getPatternFactory().newPattern(testUser, pdir);
					pat.setName(pname);
					pat.setUrn(pname);
					pat.setPatternType(PatternEnumType.OPERATION);
					
					pat.setOperationUrn(oname);
					pat.setFactUrn(pfname);
					pat.setMatchUrn(pmname);
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
						srcFact.setUrn(pfname);
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
						mFact.setUrn(pmname);
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
						op.setUrn(oname);
						op.setOperationType(OperationEnumType.INTERNAL);
						op.setOperation("org.cote.accountmanager.data.operation.LookupUserOperation");
						Factories.getOperationFactory().addOperation(op);
					}
					
					
					PatternType pat2 = Factories.getPatternFactory().getByName(pname2,pdir);
					if(pat2 != null){
						Factories.getPatternFactory().deletePattern(pat2);
						pat2 = null;
					}
					
					if(pat2 == null){
						pat2 = Factories.getPatternFactory().newPattern(testUser, pdir);
						pat2.setName(pname2);
						pat2.setUrn(pname2);
						pat2.setPatternType(PatternEnumType.OPERATION);
						pat2.setOperationUrn(oname2);
						pat2.setFactUrn(pfname2);
						pat2.setMatchUrn(pmname2);
						pat2.setComparator(ComparatorEnumType.GREATER_THAN_OR_EQUALS);

						FactType srcFact2 = Factories.getFactFactory().getByName(pfname2, fdir);
						if(srcFact2 != null){
							Factories.getFactFactory().deleteFact(srcFact2);
							srcFact2 = null;
						}
						if(srcFact2 == null){
							srcFact2 = Factories.getFactFactory().newFact(testUser, fdir);
							srcFact2.setName(pfname2);
							srcFact2.setUrn(pfname2);
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
							mFact2.setUrn(pmname2);
							mFact2.setFactType(FactEnumType.ATTRIBUTE);
							//mFact2.setSourceUrl(oname2);
							mFact2.setSourceUrn("level");
							mFact2.setFactData("7");
							mFact2.setFactoryType(FactoryEnumType.DATA);
							//mFact.setFactoryType(FactoryEnumType.USER);
							Factories.getFactFactory().addFact(mFact2);
						}
						OperationType op2 = Factories.getOperationFactory().getByName(oname2, odir);
						if(op2 != null){
							Factories.getOperationFactory().deleteOperation(op2);
							op2 = null;
						}
						if(op2 == null){
							op2 = Factories.getOperationFactory().newOperation(testUser, odir);
							op2.setName(oname2);
							op2.setUrn(oname2);
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
			/*
			userFact.setFactoryType(value);
			   .type = FACTORY
			   .factoryType = USER
			   .sourceDataType = sName
			   .sourceUri = sOrgPath
			   .urn = “urn:am.objects.user”[static]
			}
			*/
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
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
					pol.setName(pname);
					pol.setUrn(pname);
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
					printPolicy(pol);
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
	private void printPolicy(PolicyType pol) throws FactoryException, ArgumentException{
		Factories.getPolicyFactory().populate(pol);
		logger.info("POLICY " + pol.getName());
		logger.info("\turn\t" + pol.getUrn());
		logger.info("\tenabled\t" + pol.getEnabled());
		logger.info("\tcreated\t" + pol.getCreated().toString());
		logger.info("\texpires\t" + pol.getExpires().toString());
		List<RuleType> rules = pol.getRules();
		for(int i = 0; i < rules.size();i++){
			RuleType rule = rules.get(i);
			Factories.getRuleFactory().populate(rule);
			logger.info("\tRULE " + rule.getName());
			logger.info("\t\turn\t" + rule.getUrn());
			logger.info("\t\ttype\t" + rule.getRuleType());
			logger.info("\t\tcondition\t" + rule.getCondition());
			List<PatternType> patterns = rule.getPatterns();
			for(int p = 0; p < patterns.size();p++){
				PatternType pattern = patterns.get(p);
				Factories.getPatternFactory().populate(pattern);
				logger.info("\t\tPATTERN " + pattern.getName());
				logger.info("\t\t\turn\t" + pattern.getUrn());
				logger.info("\t\t\ttype\t" + pattern.getPatternType());
				FactType srcFact = pattern.getFact();
				FactType mFact = pattern.getMatch();
				logger.info("\t\t\tSOURCE FACT " + (srcFact != null ? srcFact.getName() : "IS NULL"));
				if(srcFact != null){
					logger.info("\t\t\t\turn\t" + srcFact.getUrn());
					logger.info("\t\t\t\ttype\t" + srcFact.getFactType());
					logger.info("\t\t\t\tfactoryType\t" + srcFact.getFactoryType());
					logger.info("\t\t\t\tsourceUrl\t" + srcFact.getSourceUrl());
					logger.info("\t\t\t\tsourceUrn\t" + srcFact.getSourceUrn());
					logger.info("\t\t\t\tfactData\t" + srcFact.getFactData());
				}
				logger.info("\t\t\tCOMPARATOR " + pattern.getComparator());
				logger.info("\t\t\tMATCH FACT " + (mFact != null ? mFact.getName() : "IS NULL"));
				if(mFact != null){
					logger.info("\t\t\t\turn\t" + mFact.getUrn());
					logger.info("\t\t\t\ttype\t" + mFact.getFactType());
					logger.info("\t\t\t\tfactoryType\t" + mFact.getFactoryType());
					logger.info("\t\t\t\tsourceUrl\t" + mFact.getSourceUrl());
					logger.info("\t\t\t\tsourceUrn\t" + mFact.getSourceUrn());
					logger.info("\t\t\t\tfactData\t" + mFact.getFactData());
					if(mFact.getFactType() == FactEnumType.OPERATION){
						logger.info("\t\t\t\tOPERATION\t" + (mFact.getSourceUrl() != null ? mFact.getSourceUrl() : "IS NULL"));
						if(mFact.getSourceUrl() != null){
							OperationType op = Factories.getOperationFactory().getByUrn(mFact.getSourceUrl(), mFact.getOrganization());
							logger.info("\t\t\t\turn\t" + op.getUrn());
							logger.info("\t\t\t\toperationType\t" + op.getOperationType());
							logger.info("\t\t\t\toperation\t" + op.getOperation());
						}
						
					}
				}

			}
			
		}
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
