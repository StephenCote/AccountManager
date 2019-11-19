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
package org.cote.accountmanager.data;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Properties;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.FactFactory;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.FunctionFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.OperationFactory;
import org.cote.accountmanager.data.factory.PatternFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RuleFactory;
import org.cote.accountmanager.data.factory.TagFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.PersonService;
import org.cote.accountmanager.data.services.ServiceUtil;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.DataTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FunctionEnumType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.OperationEnumType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.objects.types.TagEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.junit.After;
import org.junit.Before;


public class BaseDataAccessTest{
	public static final Logger logger = LogManager.getLogger(BaseDataAccessTest.class);
	protected static String testUserName = "RocketQAUser";
	protected static UserType testUser = null;
	protected static String sessionId = null;

	protected static String testUserName2 = "RocketQAUser2";
	protected static UserType testUser2 = null;
	protected static String sessionId2 = null;
	protected static Properties testProperties = null;
	
	protected static String testUserPassword = "password";
	
	@Before
	public void setUp() throws Exception {

		org.cote.accountmanager.service.util.ServiceUtil.setUseAccountManagerSession(false);
		
		File cacheDir = new File("./cache");
		if(cacheDir.exists() == false) cacheDir.mkdirs();
		FactoryBase.setEnableSchemaCache(false);
		FactoryBase.setSchemaCachePath("./cache");
		
		if(testProperties == null){
			testProperties = new Properties();
		
			try {
				InputStream fis = ClassLoader.getSystemResourceAsStream("./resource.properties"); 
						//new FileInputStream("./resource.properties");
				
				testProperties.load(fis);
				fis.close();
			} catch (IOException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				return;
			}
		}
		ConnectionFactory.setupConnectionFactory(testProperties);

		sessionId = UUID.randomUUID().toString();
		sessionId2 = UUID.randomUUID().toString();
		
		Factories.warmUp();
		BulkFactories.getInstance(FactoryEnumType.ACCOUNT);

		try{
			testUser = SessionSecurity.login(sessionId, testUserName, CredentialEnumType.HASHED_PASSWORD,testUserPassword, Factories.getDevelopmentOrganization().getId());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}
		catch(ArgumentException fe){
			logger.error(fe.getMessage());
		}
		if(testUser == null){
			AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Test", AuditEnumType.USER, testUserName);
			
			UserType new_user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).newUser(testUserName, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization().getId());
			if(PersonService.createUserAsPerson(audit, testUserName, testUserPassword, testUserName + "@example.com", UserEnumType.DEVELOPMENT,UserStatusEnumType.RESTRICTED , Factories.getDevelopmentOrganization().getId())){
				new_user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByName(testUserName, Factories.getDevelopmentOrganization().getId());
				testUser = SessionSecurity.login(sessionId, testUserName, CredentialEnumType.HASHED_PASSWORD,testUserPassword, Factories.getDevelopmentOrganization().getId());
			}
		}
		
		try{
			testUser2 = SessionSecurity.login(sessionId2, testUserName2, CredentialEnumType.HASHED_PASSWORD,testUserPassword, Factories.getDevelopmentOrganization().getId());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}
		catch(ArgumentException fe){
			logger.error(fe.getMessage());
		}
		if(testUser2 == null){
			AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Test", AuditEnumType.USER, testUserName2);
			
			UserType new_user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).newUser(testUserName2, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization().getId());
			if(PersonService.createUserAsPerson(audit, testUserName2, testUserPassword, testUserName2 + "@example.com", UserEnumType.DEVELOPMENT,UserStatusEnumType.RESTRICTED , Factories.getDevelopmentOrganization().getId())){
				new_user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByName(testUserName2, Factories.getDevelopmentOrganization().getId());
				testUser2 = SessionSecurity.login(sessionId2, testUserName2, CredentialEnumType.HASHED_PASSWORD,testUserPassword, Factories.getDevelopmentOrganization().getId());
			}
		
		}
		assertNotNull("QA User #1 is null",testUser);
		assertNotNull("QA User #2 is null",testUser2);
	}
	
	@After
	public void tearDown() throws Exception{
		//logger.info("Cleanup session: " + sessionId);
		Factories.getAuditFactory().flushSpool();
		SessionSecurity.logout(sessionId,  Factories.getDevelopmentOrganization().getId());
		SessionSecurity.logout(sessionId2,  Factories.getDevelopmentOrganization().getId());
	}
	
	

	public DataType newTextData(String name, String value, UserType owner, DirectoryGroupType dir){
		DataType data = null;
		
		try{
			data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name, dir);
			if(data != null){
				return data;
			}
			data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(owner, dir.getId());
			data.setName(name);
			data.setMimeType("text/plain");
			DataUtil.setValue(data, value.getBytes("UTF-8"));
			((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(data);
			data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name, dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (UnsupportedEncodingException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return data;
	}


	protected DataType getData(UserType user, String data_name){
		
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());

		
		DataType data = null;
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user,"examples", user.getHomeDirectory(), user.getOrganizationId());
			data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(data_name,  dir);
			if(data == null){
				data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(user,  dir.getId());
				data.setName(data_name);
				data.setMimeType("text/plain");
				DataUtil.setValueString(data, "Example Data");
				((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(data);
				data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(data_name,  dir);
			}
		}
		catch(FactoryException fe){
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} catch (DataException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return data;
	}
	
	public BaseTagType getTag(UserType user, DirectoryGroupType dir, TagEnumType type, String tag_name){
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());
		BaseTagType tag = null;
		
		try {
			tag = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).getTagByName(tag_name, type, dir);
			if(tag == null){
				tag = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).newTag(user,tag_name, type, dir.getId());
				((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).add(tag);
				tag = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).getTagByName(tag_name, type, dir);
			}
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		
		return tag;
	}
	
	public UserRoleType getRole(UserType owner, String roleName, UserRoleType parent){
		UserRoleType role = null;
		try {
			role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleByName(roleName, parent, owner.getOrganizationId());
			if(role == null){
				role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).newUserRole(owner, roleName, parent);
				((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).add(role);
				role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRoleByName(roleName,parent, owner.getOrganizationId());
			}
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		return role;
	}
	
	public UserType getUser(String user_name, String password){
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());
		
		UserType user = null;
		try {
			user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByName(user_name, Factories.getDevelopmentOrganization().getId());
			if(user == null){
				user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).newUser(user_name, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization().getId());
				((UserFactory)Factories.getFactory(FactoryEnumType.USER)).add(user);
				user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByName(user_name, Factories.getDevelopmentOrganization().getId());
				CredentialService.newHashedPasswordCredential(user, user, password, true,false);
			}
			((UserFactory)Factories.getFactory(FactoryEnumType.USER)).populate(user);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return user;
	}
	
	public <T> T getCreatePermission(UserType user, String name, PermissionEnumType type, T parent, OrganizationType org){
		T per = null;
		try {
			per = (T)((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionByName(name, type, (BasePermissionType)parent,org.getId());
			if(per == null){
				per = (T)((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).newPermission(user, name, type, (BasePermissionType)parent, org.getId());
				if(((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).add((BasePermissionType)per)){
					per = (T)((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionByName(name, type, (BasePermissionType)parent,org.getId());
				}
			}
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		
		return per;
	}
	

	public FactType getCreateStaticFact(UserType user,String name, String val, DirectoryGroupType fdir) throws ArgumentException, FactoryException{
		FactType srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(name, fdir);
		if(srcFact != null) return srcFact;
		srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(user, fdir.getId());
		srcFact.setName(name);
		srcFact.setFactType(FactEnumType.STATIC);
		srcFact.setFactData(val);
		((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(srcFact);
		return srcFact;
	}
	public PatternType getCreatePattern(UserType user, String name, String factUrn, String matchUrn, DirectoryGroupType dir){

		PatternType pattern = null;

		try {
			pattern = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).getByNameInGroup(name, dir);
			/*
			if(pattern != null){
				((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).deletePattern(pattern);
				pattern = null;
			}
			*/
			if(pattern == null){
				pattern = ((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).newPattern(testUser, dir.getId());
				pattern.setName(name);
				pattern.setPatternType(PatternEnumType.EXPRESSION);
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
	public RuleType getCreateRule(UserType user, String name, DirectoryGroupType dir){
		return getCreateRule(user,name,RuleEnumType.PERMIT,dir);
	}
	public RuleType getCreateRule(UserType user, String name, RuleEnumType ruleType, DirectoryGroupType dir){
		return getCreateRule(user, name, ruleType, dir, new PatternType[0]);
	}
		public RuleType getCreateRule(UserType user, String name, RuleEnumType ruleType, DirectoryGroupType dir, PatternType[] patterns){
		RuleType rule = null;

		try {
			rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).getByNameInGroup(name, dir);
			/*
			if(rule != null){
				((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).deleteRule(rule);
				rule = null;
			}
			*/
			if(rule == null){
				rule = ((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).newRule(testUser, dir.getId());
				rule.setName(name);
				rule.setRuleType(ruleType);
				rule.setCondition(ConditionEnumType.ALL);
				rule.getPatterns().addAll(Arrays.asList(patterns));
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
	
	public OperationType getCreateOperation(UserType user, String name, String className, DirectoryGroupType dir) throws FactoryException, ArgumentException{
		OperationType op = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).getByNameInGroup(name, dir);
		/*
		if(op2 != null){
			((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).deleteOperation(op2);
			op2 = null;
		}
		*/
		if(op == null){
			op = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).newOperation(testUser, dir.getId());
			op.setName(name);
			op.setOperationType(OperationEnumType.INTERNAL);
			op.setOperation(className);
			((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).add(op);
			op = ((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).getByNameInGroup(name, dir);
		}
		return op;
	}
	public PolicyType getCreatePolicy(UserType user, String name, DirectoryGroupType dir){
		return getCreatePolicy(user, name, dir, new RuleType[0]);
	}
	public PolicyType getCreatePolicy(UserType user, String name, DirectoryGroupType dir, RuleType[] rules){

		PolicyType policy = null;

		try {
			policy = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).getByNameInGroup(name, dir);
			/*
			if(policy != null){
				((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).deletePolicy(policy);
				policy = null;
			}
			*/
			if(policy == null){
				policy = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).newPolicy(testUser, dir.getId());
				policy.setCondition(ConditionEnumType.ALL);
				policy.setName(name);
				policy.setEnabled(true);
				policy.getRules().addAll(Arrays.asList(rules));
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
	
	public FactType getCreateFunctionFact(UserType user,String name, FunctionType func, DirectoryGroupType fdir) throws ArgumentException, FactoryException{
		FactType srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(name, fdir);
		if(srcFact != null) return srcFact;
		srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(user, fdir.getId());
		srcFact.setName(name);
		srcFact.setFactType(FactEnumType.FUNCTION);
		srcFact.setFactoryType(FactoryEnumType.FUNCTION);
		srcFact.setSourceUrn(func.getUrn());
		((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(srcFact);
		return srcFact;
	}
	public FactType getCreateParameterFact(UserType user,String name, DirectoryGroupType fdir) throws ArgumentException, FactoryException{
		FactType srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(name, fdir);
		if(srcFact != null) return srcFact;
		srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(user, fdir.getId());
		srcFact.setName(name);
		srcFact.setFactType(FactEnumType.PARAMETER);
		srcFact.setFactoryType(FactoryEnumType.UNKNOWN);
		((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(srcFact);
		return srcFact;
	}
	public FactType getCreateCredentialParamFact(UserType user,String name, DirectoryGroupType fdir) throws ArgumentException, FactoryException{
		FactType srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(name, fdir);
		if(srcFact != null) return srcFact;
		srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(user, fdir.getId());
		srcFact.setName(name);
		srcFact.setFactType(FactEnumType.PARAMETER);
		srcFact.setFactoryType(FactoryEnumType.CREDENTIAL);
		((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(srcFact);
		return srcFact;
	}
	public FactType getCreateEntitlementParamFact(UserType user,String name, DirectoryGroupType fdir) throws ArgumentException, FactoryException{
		FactType srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(name, fdir);
		if(srcFact != null) return srcFact;
		srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(user, fdir.getId());
		srcFact.setName(name);
		srcFact.setFactType(FactEnumType.PARAMETER);
		srcFact.setFactoryType(FactoryEnumType.UNKNOWN);
		((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(srcFact);
		return srcFact;
	}
	public FactType getCreateOperationFact(UserType user,String name, String opUrn, DirectoryGroupType fdir) throws ArgumentException, FactoryException{
		FactType srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).getByNameInGroup(name, fdir);
		if(srcFact != null) return srcFact;
		srcFact = ((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).newFact(user, fdir.getId());
		srcFact.setName(name);
		srcFact.setFactType(FactEnumType.OPERATION);
		srcFact.setFactoryType(FactoryEnumType.OPERATION);
		srcFact.setSourceUrn(opUrn);
		((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).add(srcFact);
		return srcFact;
	}
	public static FunctionType getCreateFunction(UserType user, String name, FunctionEnumType funcType, DataType data, DirectoryGroupType dir){
		FunctionType func = null;
		try{
			func = ((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).getByNameInGroup(name, dir);
			
			if(func != null){
				((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).delete(func);
				func = null;
			}
			
			if(func == null){
				func = ((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).newFunction(user, dir.getId());
				func.setName(name);
				func.setFunctionType(funcType);
				func.setFunctionData(data);
				((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).add(func);
				func = ((FunctionFactory)Factories.getFactory(FactoryEnumType.FUNCTION)).getByNameInGroup(name, dir);
			}
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		} 
		return func;
	}
	public static DataType getCreateTextData(UserType user, String name, String script, DirectoryGroupType dir){
		
		DataType bsh = null;
		try{
			bsh = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name,false,dir);
			DirectoryGroupType ddir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Data", user.getHomeDirectory(), user.getOrganizationId());
			
			if(bsh != null){
				String cv = DataUtil.getValueString(bsh);
				if(cv.equals(script)==false){
					//DataUtil.setValueString(bsh,script);
					DataUtil.setValue(bsh, script.getBytes());
					((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).update(bsh);
				}
				//((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).deleteData(bsh);
				//bsh = null;
			}
			
			if(bsh == null){
				bsh = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(user, ddir.getId());
				bsh.setName(name);
				bsh.setMimeType("text/plain");
				//DataUtil.setValueString(bsh,script);
				DataUtil.setValue(bsh, script.getBytes());
				((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(bsh);
				bsh = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name,false,ddir);
			}
		}
		catch(FactoryException | ArgumentException | DataException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return bsh;
	}
	public static DataType getCreateProtectedData(UserType user, String name, byte[] data, DirectoryGroupType dir){
		
		DataType bsh = null;
		try{
			bsh = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name,false,dir);
			DirectoryGroupType ddir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Data", user.getHomeDirectory(), user.getOrganizationId());
			
			if(bsh != null){
				SecurityBean cipher = KeyService.getSymmetricKeyByObjectId(bsh.getKeyId(), bsh.getOrganizationId());
				if(cipher == null){
					logger.error("Cipher is null for key id '" + bsh.getKeyId() + "'");
				}
				DataUtil.setCipher(bsh,cipher);
				/// Reading the enciphered data to test it will nullify the key reference
				/// So it must be reset on the data object
				///
				if(Arrays.equals(data, DataUtil.getValue(bsh))==false){
					DataUtil.setCipher(bsh,cipher);
					DataUtil.setValue(bsh, data);
					((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).update(bsh);
				}
			}
			else{
				SecurityBean cipher = KeyService.newPersonalSymmetricKey(user,false);
				
				bsh = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(user, ddir.getId());
				bsh.setName(name);
				bsh.setMimeType("text/plain");
				bsh.setKeyId(cipher.getObjectId());
				DataUtil.setCipher(bsh,KeyService.promote(cipher));
				bsh.setEncipher(true);
				DataUtil.setValue(bsh, data);
				((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(bsh);
				bsh = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name,false,ddir);
			}
		}
		catch(FactoryException | ArgumentException | DataException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return bsh;
	}
	public <T> T getRole(UserType owner, String name, RoleEnumType type, BaseRoleType parent){
		T dir = null;
		try {
			dir = (T)((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleByName(name, parent, type, parent.getOrganizationId());
			if(dir == null){
				dir = (T)((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).newRoleType(type, owner, name, parent);
				if(((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).add((BaseRoleType)dir)){
					dir = (T)((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleByName(name, parent, type, parent.getOrganizationId());
				}
			}
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return dir;
	}
	public <T> T getPermission(UserType owner, String name, PermissionEnumType type, BasePermissionType parent){
		T dir = null;
		try {
			dir = (T)((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionByName(name, type, parent, parent.getOrganizationId());
			if(dir == null){
				dir = (T)((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).newPermission(owner, name, type, parent, parent.getOrganizationId());
				if(((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).add((BasePermissionType)dir)){
					dir = (T)((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionByName(name, type, parent, parent.getOrganizationId());
				}
			}
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return dir;
	}
	public <T> T getGroup(UserType owner, String name, GroupEnumType type, BaseGroupType parent){
		T dir = null;
		try {
			dir = (T)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupByName(name, type, parent, parent.getOrganizationId());
			if(dir == null){
				dir = (T)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newGroup(owner, name, type, parent, parent.getOrganizationId());
				if(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).add((BaseGroupType)dir)){
					dir = (T)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupByName(name, type, parent, parent.getOrganizationId());
				}
			}
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return dir;
	}

	public DirectoryGroupType getCreatePath(UserType user, String path){
		DirectoryGroupType dir = null;
		try {
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(user, path, user.getOrganizationId());
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		}
		return dir;
	}
	
	public DirectoryGroupType getApplication(String name){
		DirectoryGroupType dir = null;
		try {
			dir =((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Applications/" + name, testUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return dir;
	}

	public PersonType getApplicationPerson(String name,DirectoryGroupType dir){
		PersonType acct = null;
		try {
			acct = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup(name, dir);
			if(acct == null){
				acct = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser, dir.getId());
				acct.setName(name);
				if(((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).add(acct)){
					acct = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup(name, dir);
				}
			}
			if(acct != null) ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).populate(acct);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return acct;
	}
	
	public AccountType getApplicationAccount(String name,DirectoryGroupType dir){
		AccountType acct = null;
		try {
			acct = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName(name, dir);
			if(acct == null){
				acct = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).newAccount(testUser, name, AccountEnumType.DEVELOPMENT, AccountStatusEnumType.RESTRICTED, dir.getId());
				if(((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).add(acct)){
					acct = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName(name, dir);
				}
			}
			if(acct != null) ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).populate(acct);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return acct;
	}

	
	public <T> T getApplicationPermission(String name,PermissionEnumType type, DirectoryGroupType dir){
		T per = null;
		try {
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dir);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(dir);
			String perPath = dir.getPath() + "/" + name;
			per = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).makePath(testUser, type, perPath, dir.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return per;
	}
	
	public <T> T getApplicationRole(String name,RoleEnumType type,DirectoryGroupType dir){
		T role = null;
		try {
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(dir);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(dir);
			String perPath = dir.getPath() + "/" + name;
			role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).makePath(testUser, type, perPath, dir.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return role;
	}

	public <T> T getApplicationGroup(String name,GroupEnumType type,DirectoryGroupType dir){
		T group = null;
		try {
			GroupFactory iFact = (GroupFactory)Factories.getFactory(FactoryEnumType.GROUP);
			iFact.populate(dir);
			iFact.denormalize(dir);
			
			//group = iFact.makePath(testUser, type.toString(), dir.getPath() + "/" + name, dir.getOrganizationId());
			group = (T)iFact.getGroupByName(name, type, dir, dir.getOrganizationId());
			if(group == null) {
				BaseGroupType newGroup = iFact.newAccountGroup(testUser, name, dir, dir.getOrganizationId());
				iFact.add(newGroup);
				group = (T)iFact.getGroupByName(name, type, dir, dir.getOrganizationId());
			}
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return group;
	}
	
}