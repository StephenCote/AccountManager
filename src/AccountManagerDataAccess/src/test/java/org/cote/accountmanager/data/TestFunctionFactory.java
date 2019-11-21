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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.FactFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OperationFactory;
import org.cote.accountmanager.data.factory.PatternFactory;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.data.factory.RuleFactory;
import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.data.services.ScriptService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FunctionEnumType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.OperationEnumType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.Test;
public class TestFunctionFactory extends BaseDataAccessTest{
	public static final Logger logger = LogManager.getLogger(TestFunctionFactory.class);
	
	private static String getDebugJavaScript(){
		StringBuffer buff = new StringBuffer();
		buff.append("print('test');\nvar dt = new Date().getTime();\n");
		buff.append("var pub = org.cote.accountmanager.data.Factories.getPublicOrganization();");
		buff.append("\nprint('name: ' + user.getName());");
		buff.append("var u2 = org.cote.accountmanager.data.Factories.getNameIdFactory(FactoryEnumType.USER).getByName('RocketQAUser2',user.getOrganizationId());");
		buff.append("dt;");
		return buff.toString();
	}
	@Test
	public void TestJSCRUDShouldFail(){
		
		//Map<String,String> roleMap = JSONUtil.getMap("/Users/Steve/Projects/workspace/AccountManagerService/src/main/webapp/WEB-INF/resource/roleMap.json", String.class, String.class);
		//assertNotNull("Map is null", roleMap);
		try{
			//AccountRoleType role1 = RoleService.getAccountAdministratorAccountRole(testUser.getOrganizationId());
			//assertNotNull("Role is null",role1);
			//((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).denormalize(role1);
			//logger.info("ROLE: " + role1.getParentPath());
			//AccountRoleType role1c = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).findRole(RoleEnumType.ACCOUNT, "/AccountAdministrators", testUser.getOrganizationId());
			//logger.info("Got role? " + (role1c != null));
			
			UserType adminUser = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Admin", testUser.getOrganizationId());
			List<UserRoleType> roles = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getUserRoles(adminUser);
			logger.info("ROLES: " + roles.size());
			for(int i = 0; i < roles.size(); i++){
				logger.info("\t" + roles.get(i).getName());
			}
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(testUser);
			DirectoryGroupType ddir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Data", testUser.getHomeDirectory(), testUser.getOrganizationId());
			DataType js = getCreateTextData(testUser,"Test.js",getDebugJavaScript(),ddir); 
			
			DirectoryGroupType fdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Functions", testUser.getHomeDirectory(), testUser.getOrganizationId());
			FunctionType func = getCreateFunction(testUser,"TestJS1",FunctionEnumType.JAVASCRIPT,js,fdir);

			assertNotNull("Function is null",func);
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("debug",testUser);
			/// Expecting an error here because the namespace is blocked
			///
			Double resp = (Double)ScriptService.run(testUser,params,func);
			logger.info("Ran the script: " + resp.longValue());
		}
		catch(RuntimeException | FactoryException | ArgumentException  e) {
			logger.error("*** START EXPECTED ERROR ***");
			logger.error(e.getMessage());
			//logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			logger.error("*** STOP EXPECTED ERROR ***");
		} 

	}
	/*
	@Test
	public void TestBSHCRUD(){
		try{
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(testUser);
			DirectoryGroupType ddir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Data", testUser.getHomeDirectory(), testUser.getOrganizationId());
			DataType bsh = getCreateTextData(testUser,"Test.bsh",getDebugShellScript(),ddir); 
			
			DirectoryGroupType fdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Functions", testUser.getHomeDirectory(), testUser.getOrganizationId());
			FunctionType func = getCreateFunction(testUser,"TestBSH1",bsh,fdir);
			
			assertNotNull("Function is null",func);
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("debug",testUser);
			BshService.run(testUser,params,func);
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		} 

	}
	*/
	
	@Test
	public void TestFunctionOperation(){
		PolicyType policy = getCompuFuncPolicy(testUser);
		assertNotNull("Policy is null",policy);
		
		try{
			logger.info(PolicyDefinitionUtil.printPolicy(policy));
			PolicyDefinitionType pdt = PolicyDefinitionUtil.generatePolicyDefinition(policy);
			PolicyRequestType prt = PolicyDefinitionUtil.generatePolicyRequest(pdt);
			logger.info("Param facts: " + prt.getFacts().size());
			prt.getFacts().get(0).setFactData("badpassword");
			PolicyResponseType prr = PolicyEvaluator.evaluatePolicyRequest(prt);
			logger.info("Response: " + prr.getResponse().toString());
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		} 
	}
	
	private PolicyType getCompuFuncPolicy(UserType user){
		DirectoryGroupType pdir = null;
		DirectoryGroupType rdir = null;
		DirectoryGroupType podir = null;
		DirectoryGroupType odir = null;
		DirectoryGroupType fdir = null;
		DirectoryGroupType ddir = null;
		DirectoryGroupType fudir = null;
		
		PolicyType pol = null;
		RuleType useRule = null;
		PatternType pat = null;
		
		String pname = "Function Policy";
		String rname = "Function Rule";
		String patName = "Function Pattern";
		//String clsRegExClass = "org.cote.accountmanager.data.operation.RegexOperation";
		String funcName = "FactComparator.func";
		
		try {
			fudir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Functions", user.getHomeDirectory(), testUser.getOrganizationId());
			rdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(user, "~/Rules", user.getOrganizationId());
			pdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(user, "~/Patterns", user.getOrganizationId());
			fdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(user, "~/Facts", user.getOrganizationId());
			podir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(user, "~/Policies", user.getOrganizationId());
			odir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(user, "~/Operations", user.getOrganizationId());
			ddir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Data", user.getHomeDirectory(), user.getOrganizationId());
			
			FactType setCredParamFact = getCreateCredentialParamFact(user,"Set Credential Parameter",fdir);
			setCredParamFact.setFactoryType(FactoryEnumType.CREDENTIAL);
			((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).update(setCredParamFact);
			FactType strengthFact = getCreateStaticFact(user,"Password Strength Expression","^(?=.*[A-Z].*[A-Z])(?=.*[!@#$&*])(?=.*[0-9].*[0-9])(?=.*[a-z].*[a-z].*[a-z]).{5,}$",fdir);

			//FactType credParamFact = getCreateCredentialParamFact(user,"Credential Parameter",fdir);
			
			DataType bsh = getCreateTextData(user,"TestOperation.bsh",getOperationShellScript(),ddir); 
			FunctionType func = getCreateFunction(user,"TestBshOperation",FunctionEnumType.JAVA,bsh,fudir);
			OperationType rgOp = getCreateOperation(user,"Test Function Operation",func.getUrn(),odir);
			rgOp.setOperationType(OperationEnumType.FUNCTION);
			((OperationFactory)Factories.getFactory(FactoryEnumType.OPERATION)).update(rgOp);
			pol = getCreatePolicy(user,pname,podir);
			pol.setEnabled(true);
			useRule = getCreateRule(user,rname,rdir);
			pat = getCreatePattern(user,patName,setCredParamFact.getUrn(),strengthFact.getUrn(),pdir);
			pat.setPatternType(PatternEnumType.OPERATION);
			pat.setFactUrn(setCredParamFact.getUrn());
			pat.setMatchUrn(strengthFact.getUrn());
			pat.setOperationUrn(rgOp.getUrn());
			((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).update(pat);
			useRule.getPatterns().clear();
			useRule.getPatterns().add(pat);
			((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).update(useRule);
			pol.getRules().clear();
			pol.getRules().add(useRule);
			((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).update(pol);
			pol = ((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).getByNameInGroup(pname,podir);
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		} 
		return pol;
	}
	
	private static String getOperationShellScript(){
		StringBuffer  buff = new StringBuffer();
		//buff.append("import org.apache.logging.log4j.LogManager;\nimport org.apache.logging.log4j.Logger;\n");
		//buff.append("import org.cote.accountmanager.objects.DirectoryGroupType;\n");
		//buff.append("import org.cote.accountmanager.objects.DirectoryGroupType;\n");
		//buff.append("import org.cote.accountmanager.objects.types.GroupEnumType;\n");
		//buff.append("import org.cote.accountmanager.data.Factories;\n");
		//buff.append("Logger logger = LogManager.getLogger(\"BeanShell\");\n");
		
		buff.append("DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null, GroupEnumType.DATA, \"/Home/TestUser1\", ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(\"/Accelerant/Rocket\").getId());");
		buff.append("logger.info(\"BeanShell: \" + dir.getName());");
		buff.append("OperationResponseEnumType respError = OperationResponseEnumType.ERROR;");
		buff.append("OperationResponseEnumType respSucceeded = OperationResponseEnumType.SUCCEEDED;");
		buff.append("OperationResponseEnumType respFailed = OperationResponseEnumType.FAILED;");
		buff.append("OperationResponseEnumType respUnknown = OperationResponseEnumType.UNKNOWN;");
		buff.append("logger.info(\"Comparing \" + fact.getUrn() + \" to \" + match.getUrn());\n");
		buff.append("return respSucceeded;");

		
		return buff.toString();
	}
	
	private static String getDebugShellScript(){
		StringBuffer  buff = new StringBuffer();
		//buff.append("import org.apache.logging.log4j.LogManager;\nimport org.apache.logging.log4j.Logger;\n");
		//buff.append("import org.cote.accountmanager.objects.DirectoryGroupType;\n");
		//buff.append("import org.cote.accountmanager.objects.DirectoryGroupType;\n");
		//buff.append("import org.cote.accountmanager.objects.types.GroupEnumType;\n");
		//buff.append("import org.cote.accountmanager.data.Factories;\n");
		//buff.append("Logger logger = LogManager.getLogger(\"BeanShell\");\n");
		
		buff.append("DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null, GroupEnumType.DATA, \"/Home/TestUser1\", ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(\"/Accelerant/Rocket\").getId());");
		buff.append("logger.info(\"BeanShell: \" + dir.getName());");
		buff.append("UserType user = (UserType)debug;\n");
		buff.append("logger.info(\"User = \" + user.getName());\n");
		//buff.append("NameIdType badtype = (NameIdType)bad;");
		buff.append("if(true==false){");
		buff.append("}");
		return buff.toString();
	}
	
}
	