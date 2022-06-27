/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.FactFactory;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.FunctionFactFactory;
import org.cote.accountmanager.data.factory.FunctionFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OperationFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.PatternFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RuleFactory;
import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.ApplicationPermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.ConditionEnumType;
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
import org.cote.accountmanager.util.JSONUtil;
import org.junit.Test;
public class TestPolicyService extends BaseDataAccessTest{
	
	@Test
	public void TestFactoriesReady(){
		try{
		Factories.getFactory(FactoryEnumType.FACT);
		Factories.getFactory(FactoryEnumType.FUNCTIONFACT);
		Factories.getFactory(FactoryEnumType.FUNCTION);
		Factories.getFactory(FactoryEnumType.FUNCTIONPARTICIPATION);
		Factories.getFactory(FactoryEnumType.OPERATION);
		Factories.getFactory(FactoryEnumType.PATTERN);
		Factories.getFactory(FactoryEnumType.POLICY);
		Factories.getFactory(FactoryEnumType.RULE);
		}
		catch(FactoryException f){
			logger.error(f);
		}
	}


	/*
	@Test
	public void TestFact(){
		FactType fact = null;
		String name = UUID.randomUUID().toString();
		try {
			DirectoryGroupType funcDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Facts", testUser.getOrganizationId());
			fact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, funcDir);
			fact.setName(name);
			fact.setUrn(name);
			fact.setFactType(FactEnumType.STATIC);
			fact.setFactData("Demo data");
			if(((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(fact)){
				fact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(name, funcDir);
			}
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		} catch (FactoryException e) {
			
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
			DirectoryGroupType funcDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Facts", testUser.getOrganizationId());
			for(int i = 0; i < 10; i++){
				FactType func = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, funcDir);
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
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Failed to write bulk session",wrote);
		logger.info("Wrote bulk session");
	}
	*/
	
	private FactType getCreateStaticFact(UserType user, DirectoryGroupType dir, String name, String urn, String value){
		FactType fact = null;
		try {
			fact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(name,dir);
			if(fact == null){
				
				fact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(user, dir.getId());
				fact.setName(name);
				//fact.setUrn(urn);
				fact.setFactType(FactEnumType.STATIC);
				fact.setFactData(value);
				if(((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(fact)){
					fact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(name, dir);
				}
				else fact = null;
			}
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
		}
		return fact;
	}
	
	private FunctionFactType getCreateFunctionFact(UserType user, FunctionType func, FactType fact, int order, DirectoryGroupType dir){
		FunctionFactType ffact = null;
		String name = func.getUrn() + "::" + fact.getUrn();
		try {
			ffact = ((FunctionFactFactory)Factories.getFactory(FactoryEnumType.FUNCTIONFACT)).getByNameInGroup(name, dir);
			if(ffact == null){
				ffact = ((FunctionFactFactory)Factories.getFactory(FactoryEnumType.FUNCTIONFACT)).newFunctionFact(user, dir.getId());
				ffact.setFactUrn(fact.getUrn());
				ffact.setFunctionUrn(func.getUrn());
				ffact.setName(name);
				//ffact.setUrn(name);
				ffact.setLogicalOrder(order);
				if(((FunctionFactFactory)Factories.getFactory(FactoryEnumType.FUNCTIONFACT)).add(ffact)){
					ffact = ((FunctionFactFactory)Factories.getFactory(FactoryEnumType.FUNCTIONFACT)).getByNameInGroup(name,dir);
				}
			}
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return ffact;
	}
	
	private FunctionType getCreateFunction(UserType user, String name, String urn, String sourceUrl, String sourceUrn, DirectoryGroupType dir){

		FunctionType func = null;

		try {
			func = ((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).getByNameInGroup(name, dir);
			if(func == null){
				func = ((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).newFunction(testUser, dir.getId());
				func.setName(name);
				//func.setUrn(name);
				func.setFunctionType(FunctionEnumType.JAVASCRIPT);
				func.setSourceUrl(sourceUrl);
				func.setSourceUrn(sourceUrn);
				
				if(((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).add(func)){
					func = ((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).getByNameInGroup(name, dir);
				}
				else func = null;
			}
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return func;
	}

	private PolicyType getCreatePolicy(UserType user, String name, String urn, DirectoryGroupType dir){

		PolicyType policy = null;

		try {
			policy = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).getByNameInGroup(name, dir);
			if(policy == null){
				policy = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).newPolicy(testUser, dir.getId());
				policy.setCondition(ConditionEnumType.ALL);
				policy.setName(name);
				//policy.setUrn(name);

				if(((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).add(policy)){
					policy = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).getByNameInGroup(name, dir);
				}
				else policy = null;
			}
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return policy;
	}
	private PatternType getCreatePattern(UserType user, String name, String urn, String factUrn, String matchUrn, DirectoryGroupType dir){

		PatternType pattern = null;

		try {
			pattern = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(name, dir);
			if(pattern == null){
				pattern = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).newPattern(testUser, dir.getId());
				pattern.setName(name);
				//pattern.setUrn(name);
				pattern.setPatternType(PatternEnumType.UNKNOWN);
				pattern.setComparator(ComparatorEnumType.EQUALS);
				pattern.setFactUrn(factUrn);
				pattern.setMatchUrn(matchUrn);
				if(((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).add(pattern)){
					pattern = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(name, dir);
				}
				else pattern = null;
			}
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return pattern;
	}
	private RuleType getCreateRule(UserType user, String name, String urn, DirectoryGroupType dir){

		RuleType rule = null;

		try {
			rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(name, dir);
			if(rule == null){
				rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).newRule(testUser, dir.getId());
				rule.setName(name);
				//rule.setUrn(name);
				rule.setRuleType(RuleEnumType.PERMIT);
				
				if(((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).add(rule)){
					rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(name, dir);
				}
				else rule = null;
			}
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return rule;
	}
	private <T> T getCreatePermission(UserType user, String name, PermissionEnumType type, DirectoryGroupType basePath) throws FactoryException, ArgumentException, DataAccessException{
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(basePath);
		return ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).makePath(user, type, basePath.getPath() + "/" + name,user.getOrganizationId());
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
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(user);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(user.getHomeDirectory());
			AccountRoleType demoRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).makePath(user, RoleEnumType.ACCOUNT, user.getHomeDirectory().getPath() + "/Roles/DemoRole",user.getOrganizationId());
			ApplicationPermissionType perm = getCreatePermission(user,"Entitlement1",PermissionEnumType.APPLICATION,user.getHomeDirectory());
			DirectoryGroupType demoGroup = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "DemoGroup", user.getHomeDirectory(), user.getOrganizationId());
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(demoGroup);

			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(demoGroup.getParentId(),demoGroup.getOrganizationId()));
			rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(rname, rdir);
			
			if(rule != null){
				((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).delete(rule);
				rule = null;
			}
			
			if(rule == null){
				logger.info("Creating test rule");
				rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).newRule(testUser, rdir.getId());
				rule.setName(rname);
				//rule.setUrn(rname);
				rule.setRuleType(RuleEnumType.PERMIT);
				rule.setCondition(ConditionEnumType.ALL);
				
				PatternType pat = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pname,pdir);
				if(pat != null){
					((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).delete(pat);
					pat = null;
				}
				
				if(pat == null){
					pat = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).newPattern(testUser, pdir.getId());
					pat.setName(pname);
					//pat.setUrn(pname);
					pat.setPatternType(PatternEnumType.AUTHORIZATION);
					//pat.setFactUrn(pfname);
					//pat.setMatchUrn(pmname);
					pat.setComparator(ComparatorEnumType.EQUALS);
					pat.setLogicalOrder(1);
					FactType srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(pfname, fdir);
					if(srcFact != null){
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).delete(srcFact);
						srcFact = null;
					}
					if(srcFact == null){
						srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
						srcFact.setName(pfname);
						//srcFact.setUrn(pfname);
						srcFact.setFactType(FactEnumType.PARAMETER);
						srcFact.setFactoryType(FactoryEnumType.PERSON);
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(srcFact);
					}
					FactType mFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(pmname, fdir);
					if(mFact != null){
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).delete(mFact);
						mFact = null;
					}
					if(mFact == null){
						mFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
						mFact.setName(pmname);
						// mFact.setUrn(pmname);
						
						mFact.setFactType(FactEnumType.ROLE);
						mFact.setFactoryType(FactoryEnumType.ROLE);
						mFact.setSourceUrn(demoRole.getName());
						BaseRoleType brole = (BaseRoleType)((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleById(demoRole.getParentId(), demoRole.getOrganizationId());
						logger.info(JSONUtil.exportObject(brole));
						String rolePath = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRolePath(brole);
						mFact.setSourceUrl((demoRole.getParentId() > 0L ? rolePath : null));
						mFact.setSourceType(demoRole.getRoleType().toString());
						/*
						mFact.setFactType(FactEnumType.PERMISSION);
						mFact.setFactoryType(FactoryEnumType.GROUP);
						mFact.setSourceUrn(demoGroup.getName());
						mFact.setSourceUrl(demoGroup.getParentGroup().getPath());
						mFact.setFactData(perm.getId().toString());
						*/
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(mFact);
					}
					pat.setMatchUrn(mFact.getUrn());
					pat.setFactUrn(srcFact.getUrn());
					if(((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).add(pat)){
						pat = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pname, pdir);
						rule.getPatterns().add(pat);
					}
				}
				
				
				
				if(((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).add(rule)){
					rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(rname, rdir);
				}
				else rule = null;
			}
			((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).populate(rule);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(rname, rdir);
			
			if(rule != null){
				((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).delete(rule);
				rule = null;
			}
			
			if(rule == null){
				logger.info("Creating test rule");
				rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).newRule(testUser, rdir.getId());
				rule.setName(rname);
				//rule.setUrn(rname);
				rule.setRuleType(RuleEnumType.PERMIT);
				rule.setCondition(ConditionEnumType.ANY);
				
				OperationType op = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).getByNameInGroup(oname, odir);
				if(op != null){
					((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).delete(op);
					op = null;
				}
				if(op == null){
					op = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).newOperation(testUser, odir.getId());
					op.setName(oname);
					//op.setUrn(oname);
					op.setOperationType(OperationEnumType.INTERNAL);
					op.setOperation("org.cote.accountmanager.data.operation.ComparePersonLinkAttributeOperation");
					logger.info("Op: " + oname + " in " + odir.getPath());
					((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).add(op);
				}
				
				PatternType pat = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pname,pdir);
				if(pat != null){
					((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).delete(pat);
					pat = null;
				}
				
				if(pat == null){
					pat = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).newPattern(testUser, pdir.getId());
					pat.setName(pname);
					//pat.setUrn(pname);
					pat.setPatternType(PatternEnumType.OPERATION);
					pat.setOperationUrn(oname);
					//pat.setFactUrn(pfname);
					//pat.setMatchUrn(pmname);
					pat.setComparator(ComparatorEnumType.EQUALS);
					pat.setLogicalOrder(1);
					FactType srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(pfname, fdir);
					if(srcFact != null){
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).delete(srcFact);
						srcFact = null;
					}
					if(srcFact == null){
						srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
						srcFact.setName(pfname);
						srcFact.setFactType(FactEnumType.PARAMETER);
						srcFact.setFactoryType(FactoryEnumType.PERSON);
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(srcFact);
					}
					FactType mFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(pmname, fdir);
					if(mFact != null){
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).delete(mFact);
						mFact = null;
					}
					if(mFact == null){
						mFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
						mFact.setName(pmname);

						mFact.setFactType(FactEnumType.ATTRIBUTE);
						mFact.setFactoryType(FactoryEnumType.PERSON);
						mFact.setSourceUrn("code");
						mFact.setFactData("11");
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(mFact);
					}
					pat.setMatchUrn(mFact.getUrn());
					pat.setFactUrn(srcFact.getUrn());
					if(((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).add(pat)){
						pat = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pname, pdir);
						rule.getPatterns().add(pat);
					}
				}
				
				PatternType pat2 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pname2,pdir);
				if(pat2 != null){
					((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).delete(pat2);
					pat2 = null;
				}
				
				if(pat2 == null){
					pat2 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).newPattern(testUser, pdir.getId());
					pat2.setName(pname2);
					//pat2.setUrn(pname2);
					pat2.setPatternType(PatternEnumType.OPERATION);
					pat2.setOperationUrn(oname);
					//pat2.setFactUrn(pfname);
					//pat2.setMatchUrn(pmname2);
					pat2.setComparator(ComparatorEnumType.EQUALS);
					pat2.setLogicalOrder(2);
					FactType srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(pfname, fdir);
					FactType mFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(pmname2, fdir);
					if(mFact != null){
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).delete(mFact);
						mFact = null;
					}
					if(mFact == null){
						mFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
						mFact.setName(pmname2);
						//mFact.setUrn(pmname2);
						mFact.setFactType(FactEnumType.ATTRIBUTE);
						mFact.setFactoryType(FactoryEnumType.ACCOUNT);
						mFact.setSourceUrn("code");
						mFact.setFactData("11");
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(mFact);
					}
					pat2.setMatchUrn(mFact.getUrn());
					pat2.setFactUrn(srcFact.getUrn());
					if(((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).add(pat2)){
						pat2 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pname2, pdir);
						rule.getPatterns().add(pat2);
					}
				}
				
				if(((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).add(rule)){
					rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(rname, rdir);
				}
				else rule = null;
			}
			((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).populate(rule);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(rname, rdir);
			
			if(rule != null){
				((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).delete(rule);
				rule = null;
			}
			
			if(rule == null){
				logger.info("Creating test rule");
				rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).newRule(testUser, rdir.getId());
				rule.setName(rname);
				//rule.setUrn(rname);
				rule.setRuleType(RuleEnumType.PERMIT);
				
				PatternType pat = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pname,pdir);
				if(pat != null){
					((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).delete(pat);
					pat = null;
				}
				
				if(pat == null){
					pat = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).newPattern(testUser, pdir.getId());
					pat.setName(pname);
					//pat.setUrn(pname);
					pat.setPatternType(PatternEnumType.OPERATION);
					pat.setLogicalOrder(1);
					pat.setOperationUrn(oname);
					//pat.setFactUrn(pfname);
					//pat.setMatchUrn(pmname);
					pat.setComparator(ComparatorEnumType.EQUALS);
					//pat.setMatchUrn("true");
					
					FactType srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(pfname, fdir);
					if(srcFact != null){
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).delete(srcFact);
						srcFact = null;
					}
					if(srcFact == null){
						srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
						srcFact.setName(pfname);
						//srcFact.setUrn(pfname);
						srcFact.setFactType(FactEnumType.PARAMETER);
						srcFact.setFactoryType(FactoryEnumType.USER);
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(srcFact);
					}
					FactType mFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(pmname, fdir);
					if(mFact != null){
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).delete(mFact);
						mFact = null;
					}
					if(mFact == null){
						mFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
						mFact.setName(pmname);
						//mFact.setUrn(pmname);
						mFact.setFactType(FactEnumType.FACTORY);
						//mFact.setSourceUrl(oname);
						mFact.setFactoryType(FactoryEnumType.USER);
						//mFact.setFactoryType(FactoryEnumType.USER);
						((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(mFact);
					}
					OperationType op = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).getByNameInGroup(oname, odir);
					if(op != null){
						((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).delete(op);
						op = null;
					}
					if(op == null){
						op = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).newOperation(testUser, odir.getId());
						op.setName(oname);
						//op.setUrn(oname);
						op.setOperationType(OperationEnumType.INTERNAL);
						op.setOperation("org.cote.accountmanager.data.operation.LookupUserOperation");
						((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).add(op);
					}
					pat.setMatchUrn(mFact.getUrn());
					pat.setFactUrn(srcFact.getUrn());
					PatternType pat2 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pname2,pdir);
					if(pat2 != null){
						((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).delete(pat2);
						pat2 = null;
					}
					
					if(pat2 == null){
						pat2 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).newPattern(testUser, pdir.getId());
						pat2.setLogicalOrder(2);
						pat2.setName(pname2);
						//pat2.setUrn(pname2);
						pat2.setPatternType(PatternEnumType.OPERATION);
						pat2.setOperationUrn(oname2);
						//pat2.setFactUrn(pfname2);
						//pat2.setMatchUrn(pmname2);
						pat2.setComparator(ComparatorEnumType.GREATER_THAN_OR_EQUALS);

						FactType srcFact2 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(pfname2, fdir);
						if(srcFact2 != null){
							((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).delete(srcFact2);
							srcFact2 = null;
						}
						if(srcFact2 == null){
							srcFact2 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
							srcFact2.setName(pfname2);
							//srcFact2.setUrn(pfname2);
							srcFact2.setFactType(FactEnumType.PARAMETER);
							srcFact2.setFactoryType(FactoryEnumType.DATA);

							((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(srcFact2);
						}
						FactType mFact2 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(pmname2, fdir);
						if(mFact2 != null){
							((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).delete(mFact2);
							mFact2 = null;
						}
						if(mFact2 == null){
							mFact2 = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(testUser, fdir.getId());
							mFact2.setName(pmname2);
							//mFact2.setUrn(pmname2);
							mFact2.setFactType(FactEnumType.ATTRIBUTE);
							//mFact2.setSourceUrl(oname2);
							mFact2.setSourceUrn("level");
							mFact2.setFactData("7");
							mFact2.setFactoryType(FactoryEnumType.DATA);
							//mFact.setFactoryType(FactoryEnumType.USER);
							((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(mFact2);
						}
						pat2.setMatchUrn(mFact2.getUrn());
						pat2.setFactUrn(srcFact2.getUrn());
						
						OperationType op2 = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).getByNameInGroup(oname2, odir);
						if(op2 != null){
							((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).delete(op2);
							op2 = null;
						}
						if(op2 == null){
							op2 = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).newOperation(testUser, odir.getId());
							op2.setName(oname2);
							//op2.setUrn(oname2);
							op2.setOperationType(OperationEnumType.INTERNAL);
							op2.setOperation("org.cote.accountmanager.data.operation.CompareAttributeOperation");
							((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).add(op2);
						}
					}
					
					
					
					if(((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).add(pat) && ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).add(pat2)){
						pat = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pname, pdir);
						pat2 = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(pname2, pdir);
						rule.getPatterns().add(pat);
						rule.getPatterns().add(pat2);
					}
				}
				if(((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).add(rule)){
					rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(rname, rdir);
				}
				else rule = null;
			}
			((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).populate(rule);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			DirectoryGroupType pdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Persons", testUser.getOrganizationId());
			DirectoryGroupType adir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Accounts", testUser.getOrganizationId());
			AccountRoleType demoRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).makePath(testUser, RoleEnumType.ACCOUNT, testUser.getHomeDirectory().getPath() + "/Roles/DemoRole",testUser.getOrganizationId());
			
			person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup("Policy Test Person", pdir);
			if(person == null){
				person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser, pdir.getId());
				person.setName("Policy Test Person");
				if(((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).add(person)){
					person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup("Policy Test Person", pdir);
				}
				else{
					person = null;
				}
			}
			
			account = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getByNameInGroup("Policy Test Account", adir);
			if(account == null){
				account = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).newAccount(testUser,"Policy Test Account", AccountEnumType.DEVELOPMENT, AccountStatusEnumType.UNREGISTERED, adir.getId());
				account.setName("Policy Test Account");
				if(((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).add(account)){
					account = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getByNameInGroup("Policy Test Account", adir);
				}
				else{
					account = null;
				}
			}
			
			ApplicationPermissionType perm = getCreatePermission(testUser,"Entitlement1",PermissionEnumType.APPLICATION,testUser.getHomeDirectory());
			DirectoryGroupType demoGroup = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "DemoGroup", testUser.getHomeDirectory(), testUser.getOrganizationId());
			AuthorizationService.authorize(testUser, account, demoGroup, perm, true);
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
			
			((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).populate(person);
			if(person.getAccounts().isEmpty()){
				person.getAccounts().add(account);
				((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).update(person);
			}
			
			//PolicyRequestType prt = new PolicyRequestType();
			PolicyDefinitionType pdt = PolicyDefinitionUtil.generatePolicyDefinition(pol);
			PolicyRequestType prt = PolicyDefinitionUtil.generatePolicyRequest(pdt);
			//prt.setUrn(pol.getUrn());
			prt.setOrganizationPath(((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationPath(pol.getOrganizationId()));
			assertTrue("Expected more than one parameters",prt.getFacts().size() > 0);
			assertNotNull("Fact urn is null",prt.getFacts().get(0).getUrn());
			logger.info("Parameter Count: " + prt.getFacts().size());
			FactType userFact = prt.getFacts().get(0);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(pdir);
			userFact.setSourceUrl(pdir.getPath());
			userFact.setSourceUrn("Policy Test Person");
			//prt.getFacts().add(userFact);
			
			PolicyResponseType prr = PolicyEvaluator.evaluatePolicyRequest(prt);
			logger.info("Policy Evaluation for " + prr.getUrn() + " = " + prr.getResponse().toString());
			
			assertTrue("Policy response was " + prr.getResponse() + "; was expecting PERMIT",prr.getResponse() == PolicyResponseEnumType.PERMIT);
			
			
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			prt.setOrganizationPath(((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationPath(pol.getOrganizationId()));
			
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
			
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(testData.getGroup());
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
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	*/
	private boolean evaluatePattern(PatternType pattern,List<FactType> facts){
		boolean outBool = false;
		try {
			((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).populate(pattern);
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		FactType fact = pattern.getFact();
		FactType mfact = pattern.getMatch();
		if(fact == null){
			logger.error("Pattern fact definition is null");
			return outBool;
		}
		if(mfact == null){
			logger.error("Pattern match fact definition is null");
			return outBool;
		}
		
		FactType sfact = fact;
		
		/// factory lookup --- PARAM->FACT
		///
		if(fact.getFactType() == FactEnumType.PARAMETER && mfact.getFactType() == FactEnumType.FACTORY){
			sfact = getFactParameter(fact,facts);
			if(sfact == null){
				logger.error("Parameter value not specified for urn " + fact.getUrn());
				return outBool;
			}
			try{
				FactoryBase factory = Factories.getFactory(mfact.getFactoryType());
				if(factory == null){
					logger.error("Invalid factory for type " + mfact.getFactoryType());
				}
				NameIdType obj = null;
				if(mfact.getFactoryType() == FactoryEnumType.USER){
					//obj = ((UserFactory)factory);
				}
			}
			catch(FactoryException f){
				logger.error(f);
			}
		}
		
		return outBool;
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
				rdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Rules", testUser.getOrganizationId());
				pdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Patterns", testUser.getOrganizationId());
				fdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Facts", testUser.getOrganizationId());
				podir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Policies", testUser.getOrganizationId());
				odir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Operations", testUser.getOrganizationId());
				pol = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).getByNameInGroup(pname, podir);
				if(pol != null){
					((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).delete(pol);
					pol = null;
				}
				if(pol == null){
					pol = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).newPolicy(testUser, podir.getId());
					pol.setName(pname);
					//pol.setUrn(pname);
					pol.setEnabled(true);
					pol.setCondition(ConditionEnumType.ALL);
					idRule = getCreatePersonAuthorizationRule(testUser, rdir, pdir,fdir,odir);
					if(idRule != null){
						pol.getRules().add(idRule);
						
						if(((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).add(pol)){
							pol = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).getByNameInGroup(pname, podir);
						}
						else pol = null;
					}
				}
				if(pol != null){
					((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).populate(pol);
					//evaluatePolicy(pol);
					logger.info(PolicyDefinitionUtil.printPolicy(pol));
				}
				
				
			} catch (FactoryException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			} catch (ArgumentException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
				rdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Rules", testUser.getOrganizationId());
				pdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Patterns", testUser.getOrganizationId());
				fdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Facts", testUser.getOrganizationId());
				podir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Policies", testUser.getOrganizationId());
				odir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Operations", testUser.getOrganizationId());
				pol = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).getByNameInGroup(pname, podir);
				if(pol != null){
					((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).delete(pol);
					pol = null;
				}
				if(pol == null){
					pol = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).newPolicy(testUser, podir.getId());
					pol.setCondition(ConditionEnumType.ALL);
					pol.setName(pname);
					//pol.setUrn(pname);
					pol.setEnabled(true);
					idRule = getCreatePersonAccountRule(testUser, rdir, pdir,fdir,odir);
					if(idRule != null){
						pol.getRules().add(idRule);
						
						if(((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).add(pol)){
							pol = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).getByNameInGroup(pname, podir);
						}
						else pol = null;
					}
				}
				if(pol != null){
					((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).populate(pol);
					//evaluatePolicy(pol);
					logger.info(PolicyDefinitionUtil.printPolicy(pol));
				}
				
				
			} catch (FactoryException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			} catch (ArgumentException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
				rdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Rules", testUser.getOrganizationId());
				pdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Patterns", testUser.getOrganizationId());
				fdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Facts", testUser.getOrganizationId());
				odir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Operations", testUser.getOrganizationId());
				podir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Policies", testUser.getOrganizationId());
				pol = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).getByNameInGroup(pname, podir);
				if(pol != null){
					((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).delete(pol);
					pol = null;
				}
				if(pol == null){
					pol = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).newPolicy(testUser, podir.getId());
					pol.setCondition(ConditionEnumType.ALL);
					pol.setName(pname);
					//pol.setUrn(pname);
					pol.setEnabled(true);
					idRule = getCreateIdentityRule(testUser, rdir, pdir,fdir,odir);
					if(idRule != null){
						pol.getRules().add(idRule);
						
						if(((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).add(pol)){
							pol = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).getByNameInGroup("urn:am.policy.identity", podir);
						}
						else pol = null;
					}
				}
				if(pol != null){
					((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).populate(pol);
					//evaluatePolicy(pol);
					logger.info(PolicyDefinitionUtil.printPolicy(pol));
				}
				
				
			} catch (FactoryException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			} catch (ArgumentException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Rules", testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Policies", testUser.getOrganizationId());
			rdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Rules", testUser.getOrganizationId());
			pdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Patterns", testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).updateRule(rule1);
			((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).updateRule(rule2);
			up = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).updatePolicy(pol);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			DirectoryGroupType funcDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Functions", testUser.getOrganizationId());
			DirectoryGroupType factDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Facts", testUser.getOrganizationId());
			DirectoryGroupType funFactDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/FunctionFacts", testUser.getOrganizationId());
			func = ((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).newFunction(testUser, funcDir);
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
			
			if(((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).addFunction(func)){
				func = ((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).getByNameInGroup(name, funcDir);
			}
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertNotNull("Function is null",func);
		logger.info("Created and retrieved function " + name);
		try {
			((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).populate(func);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			DirectoryGroupType funcDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Functions", testUser.getOrganizationId());
			for(int i = 0; i < 10; i++){
				FunctionType func = ((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).newFunction(testUser, funcDir);
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
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Failed to write bulk session",wrote);
		logger.info("Wrote bulk session");
	}
	*/
}
