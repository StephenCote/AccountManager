package org.cote.accountmanager.data;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.PersonService;
import org.cote.accountmanager.data.services.ServiceUtil;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
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
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.junit.After;
import org.junit.Before;


public class BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(BaseDataAccessTest.class.getName());
	protected static String testUserName = "RocketQAUser";
	protected static UserType testUser = null;
	protected static String sessionId = null;

	protected static String testUserName2 = "RocketQAUser2";
	protected static UserType testUser2 = null;
	protected static String sessionId2 = null;

	
	@Before
	public void setUp() throws Exception {
		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}
		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.SINGLE);
		cf.setDriverClassName("org.postgresql.Driver");
		cf.setUserName("devuser");
		cf.setUserPassword("password");
		cf.setUrl("jdbc:postgresql://127.0.0.1:5432/devdb");
		sessionId = UUID.randomUUID().toString();
		sessionId2 = UUID.randomUUID().toString();

		try{
			testUser = SessionSecurity.login(sessionId, testUserName, CredentialEnumType.HASHED_PASSWORD,"password", Factories.getDevelopmentOrganization().getId());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}
		catch(ArgumentException fe){
			logger.error(fe.getMessage());
		}
		if(testUser == null){
			AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Test", AuditEnumType.USER, testUserName);
			
			UserType new_user = Factories.getUserFactory().newUser(testUserName, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization().getId());
			if(PersonService.createUserAsPerson(audit, testUserName, "password", testUserName + "@example.com", UserEnumType.DEVELOPMENT,UserStatusEnumType.RESTRICTED , Factories.getDevelopmentOrganization().getId())){
				new_user = Factories.getUserFactory().getUserByName(testUserName, Factories.getDevelopmentOrganization().getId());
				testUser = SessionSecurity.login(sessionId, testUserName, CredentialEnumType.HASHED_PASSWORD,"password", Factories.getDevelopmentOrganization().getId());
			}
		}
		
		try{
			testUser2 = SessionSecurity.login(sessionId2, testUserName2, CredentialEnumType.HASHED_PASSWORD,"password", Factories.getDevelopmentOrganization().getId());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}
		catch(ArgumentException fe){
			logger.error(fe.getMessage());
		}
		if(testUser2 == null){
			AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Test", AuditEnumType.USER, testUserName2);
			
			UserType new_user = Factories.getUserFactory().newUser(testUserName2, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization().getId());
			if(PersonService.createUserAsPerson(audit, testUserName2, "password", testUserName2 + "@example.com", UserEnumType.DEVELOPMENT,UserStatusEnumType.RESTRICTED , Factories.getDevelopmentOrganization().getId())){
				new_user = Factories.getUserFactory().getUserByName(testUserName2, Factories.getDevelopmentOrganization().getId());
				testUser2 = SessionSecurity.login(sessionId2, testUserName2, CredentialEnumType.HASHED_PASSWORD,"password", Factories.getDevelopmentOrganization().getId());
			}
		
		}
		assertNotNull("QA User #1 is null",testUser);
		assertNotNull("QA User #2 is null",testUser2);
	}
	
	@After
	public void tearDown() throws Exception{
		//logger.info("Cleanup session: " + sessionId);
		SessionSecurity.logout(sessionId,  Factories.getDevelopmentOrganization().getId());
		SessionSecurity.logout(sessionId2,  Factories.getDevelopmentOrganization().getId());
	}
	
	

	public DataType newTextData(String name, String value, UserType owner, DirectoryGroupType dir){
		DataType data = null;
		
		try{
			data = Factories.getDataFactory().getDataByName(name, dir);
			if(data != null){
				return data;
			}
			data = Factories.getDataFactory().newData(owner, dir.getId());
			data.setName(name);
			data.setMimeType("text/plain");
			DataUtil.setValue(data, value.getBytes("UTF-8"));
			Factories.getDataFactory().addData(data);
			data = Factories.getDataFactory().getDataByName(name, dir);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}


	protected DataType getData(UserType user, String data_name){
		
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());

		
		DataType data = null;
		try{
			DirectoryGroupType dir = Factories.getGroupFactory().getCreateDirectory(user,"examples", user.getHomeDirectory(), user.getOrganizationId());
			data = Factories.getDataFactory().getDataByName(data_name,  dir);
			if(data == null){
				data = Factories.getDataFactory().newData(user,  dir.getId());
				data.setName(data_name);
				data.setMimeType("text/plain");
				DataUtil.setValueString(data, "Example Data");
				Factories.getDataFactory().addData(data);
				data = Factories.getDataFactory().getDataByName(data_name,  dir);
			}
		}
		catch(FactoryException fe){
			fe.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	
	public DataTagType getTag(UserType user, String tag_name){
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());
		DataTagType tag = null;
		
		try {
			DirectoryGroupType dir = Factories.getGroupFactory().getCreateDirectory(user,"tags", user.getHomeDirectory(), user.getOrganizationId());
			tag = Factories.getTagFactory().getDataTagByName(tag_name, dir);
			if(tag == null){
				tag = Factories.getTagFactory().newDataTag(user,tag_name, dir.getId());
				Factories.getTagFactory().addTag(tag);
				tag = Factories.getTagFactory().getDataTagByName(tag_name, dir);
			}
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
		
		return tag;
	}
	
	public UserRoleType getRole(UserType owner, String roleName, UserRoleType parent){
		UserRoleType role = null;
		try {
			role = Factories.getRoleFactory().getRoleByName(roleName, parent, owner.getOrganizationId());
			if(role == null){
				role = Factories.getRoleFactory().newUserRole(owner, roleName, parent);
				Factories.getRoleFactory().addRole(role);
				role = Factories.getRoleFactory().getUserRoleByName(roleName,parent, owner.getOrganizationId());
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return role;
	}
	
	public UserType getUser(String user_name, String password){
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());
		
		UserType user = null;
		try {
			user = Factories.getUserFactory().getUserByName(user_name, Factories.getDevelopmentOrganization().getId());
			if(user == null){
				user = Factories.getUserFactory().newUser(user_name, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization().getId());
				Factories.getUserFactory().addUser(user);
				user = Factories.getUserFactory().getUserByName(user_name, Factories.getDevelopmentOrganization().getId());
				CredentialService.newHashedPasswordCredential(user, user, password, true,false);
			}
			Factories.getUserFactory().populate(user);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}
	
	public <T> T getCreatePermission(UserType user, String name, PermissionEnumType type, T parent, OrganizationType org){
		T per = null;
		try {
			per = (T)Factories.getPermissionFactory().getPermissionByName(name, type, (BasePermissionType)parent,org.getId());
			if(per == null){
				per = (T)Factories.getPermissionFactory().newPermission(user, name, type, (BasePermissionType)parent, org.getId());
				if(Factories.getPermissionFactory().addPermission((BasePermissionType)per)){
					per = (T)Factories.getPermissionFactory().getPermissionByName(name, type, (BasePermissionType)parent,org.getId());
				}
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return per;
	}
	

	public FactType getCreateStaticFact(UserType user,String name, String val, DirectoryGroupType fdir) throws ArgumentException, FactoryException{
		FactType srcFact = Factories.getFactFactory().getByNameInGroup(name, fdir);
		if(srcFact != null) return srcFact;
		srcFact = Factories.getFactFactory().newFact(user, fdir.getId());
		srcFact.setName(name);
		srcFact.setFactType(FactEnumType.STATIC);
		srcFact.setFactData(val);
		Factories.getFactFactory().addFact(srcFact);
		return srcFact;
	}
	public PatternType getCreatePattern(UserType user, String name, String factUrn, String matchUrn, DirectoryGroupType dir){

		PatternType pattern = null;

		try {
			pattern = Factories.getPatternFactory().getByNameInGroup(name, dir);
			/*
			if(pattern != null){
				Factories.getPatternFactory().deletePattern(pattern);
				pattern = null;
			}
			*/
			if(pattern == null){
				pattern = Factories.getPatternFactory().newPattern(testUser, dir.getId());
				pattern.setName(name);
				pattern.setPatternType(PatternEnumType.EXPRESSION);
				pattern.setComparator(ComparatorEnumType.EQUALS);

				pattern.setFactUrn(factUrn);
				pattern.setMatchUrn(matchUrn);
				if(Factories.getPatternFactory().addPattern(pattern)){
					pattern = Factories.getPatternFactory().getByNameInGroup(name, dir);
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
	public RuleType getCreateRule(UserType user, String name, DirectoryGroupType dir){
		return getCreateRule(user,name,RuleEnumType.PERMIT,dir);
	}
	public RuleType getCreateRule(UserType user, String name, RuleEnumType ruleType, DirectoryGroupType dir){

		RuleType rule = null;

		try {
			rule = Factories.getRuleFactory().getByNameInGroup(name, dir);
			/*
			if(rule != null){
				Factories.getRuleFactory().deleteRule(rule);
				rule = null;
			}
			*/
			if(rule == null){
				rule = Factories.getRuleFactory().newRule(testUser, dir.getId());
				rule.setName(name);
				rule.setRuleType(ruleType);
				rule.setCondition(ConditionEnumType.ALL);
				if(Factories.getRuleFactory().addRule(rule)){
					rule = Factories.getRuleFactory().getByNameInGroup(name, dir);
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
	
	public OperationType getCreateOperation(UserType user, String name, String className, DirectoryGroupType dir) throws FactoryException, ArgumentException{
		OperationType op = Factories.getOperationFactory().getByNameInGroup(name, dir);
		/*
		if(op2 != null){
			Factories.getOperationFactory().deleteOperation(op2);
			op2 = null;
		}
		*/
		if(op == null){
			op = Factories.getOperationFactory().newOperation(testUser, dir.getId());
			op.setName(name);
			op.setOperationType(OperationEnumType.INTERNAL);
			op.setOperation(className);
			Factories.getOperationFactory().addOperation(op);
			op = Factories.getOperationFactory().getByNameInGroup(name, dir);
		}
		return op;
	}
	
	public PolicyType getCreatePolicy(UserType user, String name, DirectoryGroupType dir){

		PolicyType policy = null;

		try {
			policy = Factories.getPolicyFactory().getByNameInGroup(name, dir);
			/*
			if(policy != null){
				Factories.getPolicyFactory().deletePolicy(policy);
				policy = null;
			}
			*/
			if(policy == null){
				policy = Factories.getPolicyFactory().newPolicy(testUser, dir.getId());
				policy.setCondition(ConditionEnumType.ALL);
				policy.setName(name);

				if(Factories.getPolicyFactory().addPolicy(policy)){
					policy = Factories.getPolicyFactory().getByNameInGroup(name, dir);
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
	
	public FactType getCreateCredentialParamFact(UserType user,String name, DirectoryGroupType fdir) throws ArgumentException, FactoryException{
		FactType srcFact = Factories.getFactFactory().getByNameInGroup(name, fdir);
		if(srcFact != null) return srcFact;
		srcFact = Factories.getFactFactory().newFact(user, fdir.getId());
		srcFact.setName(name);
		srcFact.setFactType(FactEnumType.PARAMETER);
		srcFact.setFactoryType(FactoryEnumType.CREDENTIAL);
		Factories.getFactFactory().addFact(srcFact);
		return srcFact;
	}
	public static FunctionType getCreateFunction(UserType user, String name, DataType data, DirectoryGroupType dir){
		FunctionType func = null;
		try{
			func = Factories.getFunctionFactory().getByNameInGroup(name, dir);
			
			if(func != null){
				Factories.getFunctionFactory().deleteFunction(func);
				func = null;
			}
			
			if(func == null){
				func = Factories.getFunctionFactory().newFunction(user, dir.getId());
				func.setName(name);
				func.setFunctionType(FunctionEnumType.JAVA);
				func.setFunctionData(data);
				Factories.getFunctionFactory().addFunction(func);
				func = Factories.getFunctionFactory().getByNameInGroup(name, dir);
			}
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} 
		return func;
	}
	public static DataType getCreateTextData(UserType user, String name, String script, DirectoryGroupType dir){
		
		DataType bsh = null;
		try{
			bsh = Factories.getDataFactory().getDataByName(name,false,dir);
			DirectoryGroupType ddir = Factories.getGroupFactory().getCreateDirectory(user, "Data", user.getHomeDirectory(), user.getOrganizationId());
			if(bsh != null){
				Factories.getDataFactory().deleteData(bsh);
				bsh = null;
			}
			if(bsh == null){
				bsh = Factories.getDataFactory().newData(user, ddir.getId());
				bsh.setName(name);
				bsh.setMimeType("text/plain");
				DataUtil.setValueString(bsh,script);
				Factories.getDataFactory().addData(bsh);
				bsh = Factories.getDataFactory().getDataByName(name,false,ddir);
			}
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} catch (DataException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		return bsh;
	}
	public <T> T getRole(UserType owner, String name, RoleEnumType type, BaseRoleType parent){
		T dir = null;
		try {
			dir = (T)Factories.getRoleFactory().getRoleByName(name, parent, type, parent.getOrganizationId());
			if(dir == null){
				dir = (T)Factories.getRoleFactory().newRoleType(type, owner, name, parent);
				if(Factories.getRoleFactory().addRole((BaseRoleType)dir)){
					dir = (T)Factories.getRoleFactory().getRoleByName(name, parent, type, parent.getOrganizationId());
				}
			}
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
		return dir;
	}
	public <T> T getGroup(UserType owner, String name, GroupEnumType type, BaseGroupType parent){
		T dir = null;
		try {
			dir = (T)Factories.getGroupFactory().getGroupByName(name, type, parent, parent.getOrganizationId());
			if(dir == null){
				dir = (T)Factories.getGroupFactory().newGroup(owner, name, type, parent, parent.getOrganizationId());
				if(Factories.getGroupFactory().addGroup((BaseGroupType)dir)){
					dir = (T)Factories.getGroupFactory().getGroupByName(name, type, parent, parent.getOrganizationId());
				}
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dir;
	}

	public DirectoryGroupType getApplication(String name){
		DirectoryGroupType dir = null;
		try {
			dir =Factories.getGroupFactory().getCreatePath(testUser, "~/Applications/" + name, testUser.getOrganizationId());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dir;
	}

	public PersonType getApplicationPerson(String name,DirectoryGroupType dir){
		PersonType acct = null;
		try {
			acct = Factories.getPersonFactory().getByNameInGroup(name, dir);
			if(acct == null){
				acct = Factories.getPersonFactory().newPerson(testUser, dir.getId());
				acct.setName(name);
				if(Factories.getPersonFactory().addPerson(acct)){
					acct = Factories.getPersonFactory().getByNameInGroup(name, dir);
				}
			}
			if(acct != null) Factories.getPersonFactory().populate(acct);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return acct;
	}
	
	public AccountType getApplicationAccount(String name,DirectoryGroupType dir){
		AccountType acct = null;
		try {
			acct = Factories.getAccountFactory().getAccountByName(name, dir);
			if(acct == null){
				acct = Factories.getAccountFactory().newAccount(testUser, name, AccountEnumType.DEVELOPMENT, AccountStatusEnumType.RESTRICTED, dir.getId());
				if(Factories.getAccountFactory().addAccount(acct)){
					acct = Factories.getAccountFactory().getAccountByName(name, dir);
				}
			}
			if(acct != null) Factories.getAccountFactory().populate(acct);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return acct;
	}

	
	public <T> T getApplicationPermission(String name,PermissionEnumType type, DirectoryGroupType dir){
		T per = null;
		try {
			Factories.getGroupFactory().populate(dir);
			Factories.getGroupFactory().denormalize(dir);
			String perPath = dir.getPath() + "/" + name;
			per = Factories.getPermissionFactory().makePath(testUser, type, perPath, dir.getOrganizationId());
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
		return per;
	}
	
	public <T> T getApplicationRole(String name,RoleEnumType type,DirectoryGroupType dir){
		T role = null;
		try {
			Factories.getGroupFactory().populate(dir);
			String perPath = dir.getPath() + "/" + name;
			role = Factories.getRoleFactory().makePath(testUser, type, perPath, dir.getOrganizationId());
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
		return role;
	}

	
}