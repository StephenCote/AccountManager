package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.data.services.BshService;
import org.cote.accountmanager.data.services.ScriptService;
import org.cote.accountmanager.exceptions.DataException;
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
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.junit.Test;

public class TestFunctionFactory extends BaseDataAccessTest{
	public static final Logger logger = Logger.getLogger(TestFunctionFactory.class.getName());
	
	private static String getDebugJavaScript(){
		StringBuffer buff = new StringBuffer();
		buff.append("print('test');\nvar dt = new Date().getTime();\n");
		buff.append("var pub = org.cote.accountmanager.data.Factories.getPublicOrganization();");
		buff.append("\nprint('name: ' + user.getName());");
		buff.append("var u2 = org.cote.accountmanager.data.Factories.getUserFactory().getUserByName('RocketQAUser2',user.getOrganizationId());");
		buff.append("dt;");
		return buff.toString();
	}
	@Test
	public void TestJSCRUD(){
		try{
			Factories.getUserFactory().populate(testUser);
			DirectoryGroupType ddir = Factories.getGroupFactory().getCreateDirectory(testUser, "Data", testUser.getHomeDirectory(), testUser.getOrganizationId());
			DataType js = getCreateTextData(testUser,"Test.js",getDebugJavaScript(),ddir); 
			
			DirectoryGroupType fdir = Factories.getGroupFactory().getCreateDirectory(testUser, "Functions", testUser.getHomeDirectory(), testUser.getOrganizationId());
			FunctionType func = getCreateFunction(testUser,"TestJS1",js,fdir);
			if(func.getFunctionType() != FunctionEnumType.JAVASCRIPT){
				func.setFunctionType(FunctionEnumType.JAVASCRIPT);
				Factories.getFunctionFactory().updateFunction(func);
			}
			assertNotNull("Function is null",func);
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("debug",testUser);

			Double resp = (Double)ScriptService.run(testUser,params,func);
			logger.info("Ran the script: " + resp.longValue());
		}
		catch(NullPointerException | FactoryException | ArgumentException | DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} 

	}
	/*
	@Test
	public void TestBSHCRUD(){
		try{
			Factories.getUserFactory().populate(testUser);
			DirectoryGroupType ddir = Factories.getGroupFactory().getCreateDirectory(testUser, "Data", testUser.getHomeDirectory(), testUser.getOrganizationId());
			DataType bsh = getCreateTextData(testUser,"Test.bsh",getDebugShellScript(),ddir); 
			
			DirectoryGroupType fdir = Factories.getGroupFactory().getCreateDirectory(testUser, "Functions", testUser.getHomeDirectory(), testUser.getOrganizationId());
			FunctionType func = getCreateFunction(testUser,"TestBSH1",bsh,fdir);
			
			assertNotNull("Function is null",func);
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("debug",testUser);
			BshService.run(testUser,params,func);
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} 

	}
	*/
	/*
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
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} 
	}
	*/
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
			fudir = Factories.getGroupFactory().getCreateDirectory(user, "Functions", user.getHomeDirectory(), testUser.getOrganizationId());
			rdir = Factories.getGroupFactory().getCreatePath(user, "~/Rules", user.getOrganizationId());
			pdir = Factories.getGroupFactory().getCreatePath(user, "~/Patterns", user.getOrganizationId());
			fdir = Factories.getGroupFactory().getCreatePath(user, "~/Facts", user.getOrganizationId());
			podir = Factories.getGroupFactory().getCreatePath(user, "~/Policies", user.getOrganizationId());
			odir = Factories.getGroupFactory().getCreatePath(user, "~/Operations", user.getOrganizationId());
			ddir = Factories.getGroupFactory().getCreateDirectory(user, "Data", user.getHomeDirectory(), user.getOrganizationId());
			
			FactType setCredParamFact = getCreateCredentialParamFact(user,"Set Credential Parameter",fdir);
			setCredParamFact.setFactoryType(FactoryEnumType.CREDENTIAL);
			Factories.getFactFactory().updateFact(setCredParamFact);
			FactType strengthFact = getCreateStaticFact(user,"Password Strength Expression","^(?=.*[A-Z].*[A-Z])(?=.*[!@#$&*])(?=.*[0-9].*[0-9])(?=.*[a-z].*[a-z].*[a-z]).{5,}$",fdir);

			//FactType credParamFact = getCreateCredentialParamFact(user,"Credential Parameter",fdir);
			
			DataType bsh = getCreateTextData(user,"TestOperation.bsh",getOperationShellScript(),ddir); 
			FunctionType func = getCreateFunction(user,"TestBshOperation",bsh,fudir);
			OperationType rgOp = getCreateOperation(user,"Test Function Operation",func.getUrn(),odir);
			rgOp.setOperationType(OperationEnumType.FUNCTION);
			Factories.getOperationFactory().updateOperation(rgOp);
			pol = getCreatePolicy(user,pname,podir);
			pol.setEnabled(true);
			useRule = getCreateRule(user,rname,rdir);
			pat = getCreatePattern(user,patName,setCredParamFact.getUrn(),strengthFact.getUrn(),pdir);
			pat.setPatternType(PatternEnumType.OPERATION);
			pat.setFactUrn(setCredParamFact.getUrn());
			pat.setMatchUrn(strengthFact.getUrn());
			pat.setOperationUrn(rgOp.getUrn());
			Factories.getPatternFactory().updatePattern(pat);
			useRule.getPatterns().clear();
			useRule.getPatterns().add(pat);
			Factories.getRuleFactory().updateRule(useRule);
			pol.getRules().clear();
			pol.getRules().add(useRule);
			Factories.getPolicyFactory().updatePolicy(pol);
			pol = Factories.getPolicyFactory().getByNameInGroup(pname,podir);
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		} catch (DataAccessException e) {
			logger.error(e.getMessage());
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pol;
	}
	
	private static String getOperationShellScript(){
		StringBuffer  buff = new StringBuffer();
		//buff.append("import org.apache.log4j.Logger;\n");
		//buff.append("import org.cote.accountmanager.objects.DirectoryGroupType;\n");
		//buff.append("import org.cote.accountmanager.objects.DirectoryGroupType;\n");
		//buff.append("import org.cote.accountmanager.objects.types.GroupEnumType;\n");
		//buff.append("import org.cote.accountmanager.data.Factories;\n");
		//buff.append("Logger logger = Logger.getLogger(\"BeanShell\");\n");
		
		buff.append("DirectoryGroupType dir = Factories.getGroupFactory().findGroup(null, GroupEnumType.DATA, \"/Home/TestUser1\", Factories.getOrganizationFactory().findOrganization(\"/Accelerant/Rocket\").getId());");
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
		//buff.append("import org.apache.log4j.Logger;\n");
		//buff.append("import org.cote.accountmanager.objects.DirectoryGroupType;\n");
		//buff.append("import org.cote.accountmanager.objects.DirectoryGroupType;\n");
		//buff.append("import org.cote.accountmanager.objects.types.GroupEnumType;\n");
		//buff.append("import org.cote.accountmanager.data.Factories;\n");
		//buff.append("Logger logger = Logger.getLogger(\"BeanShell\");\n");
		
		buff.append("DirectoryGroupType dir = Factories.getGroupFactory().findGroup(null, GroupEnumType.DATA, \"/Home/TestUser1\", Factories.getOrganizationFactory().findOrganization(\"/Accelerant/Rocket\").getId());");
		buff.append("logger.info(\"BeanShell: \" + dir.getName());");
		buff.append("UserType user = (UserType)debug;\n");
		buff.append("logger.info(\"User = \" + user.getName());\n");
		//buff.append("NameIdType badtype = (NameIdType)bad;");
		buff.append("if(true==false){");
		buff.append("}");
		return buff.toString();
	}
	
}
	