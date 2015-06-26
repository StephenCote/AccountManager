package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.data.security.ControlService;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.objects.ConditionEnumType;
import org.cote.accountmanager.objects.ControlActionEnumType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactEnumType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationEnumType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleEnumType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.junit.Test;

public class TestControlFactory extends BaseDataAccessTest {
	public static final Logger logger = Logger.getLogger(TestControlFactory.class.getName());
	
	@Test
	public void TestControlCRUD(){
		CredentialType everyCred = new CredentialType();
		everyCred.setNameType(NameEnumType.CREDENTIAL);
		everyCred.setCredentialType(CredentialEnumType.HASHED_PASSWORD);
		everyCred.setOrganization(testUser.getOrganization());
		
		CredentialType cred = CredentialService.getPrimaryCredential(testUser2,CredentialEnumType.HASHED_PASSWORD,false);
		PolicyType pol = getPasswordStrengthPolicy(testUser);
		assertNotNull("Policy is null",pol);
		assertNotNull("Credential is null",cred);
		ControlType ct1 = null;
		ControlType ct2 = null;
		try {
			List<ControlType> ctls2 = Factories.getControlFactory().getControlsForType(cred, true, false);
			logger.info("All password controls: " + ctls2.size());
			
			logger.info("Deleting direct controls: " + Factories.getControlFactory().deleteControlsForType(cred));
			logger.info("Deleting global controls: " + Factories.getControlFactory().deleteControlsForType(everyCred));
			
			ct1 = Factories.getControlFactory().newControl(testUser, cred);
			ct2 = Factories.getControlFactory().newControl(testUser, everyCred);
			ct2.setControlId(pol.getId());
			ct2.setControlType(ControlEnumType.POLICY);
			ct2.setControlAction(ControlActionEnumType.WRITE);
			//ct1.setControlType(ControlEnumType.POLICY);
			
			Factories.getControlFactory().addControl(ct1);
			Factories.getControlFactory().addControl(ct2);
			/// direct only
			List<ControlType> ctls = Factories.getControlFactory().getControlsForType(cred, false, false);
			/// both direct and global
			ctls2 = Factories.getControlFactory().getControlsForType(cred, true, false);
			/// only global
			List<ControlType> ctls3 = Factories.getControlFactory().getControlsForType(cred, false, true);
			logger.info("Direct: " + ctls.size() + ": Both: " + ctls2.size() + ": Global Only: " + ctls3.size());
			
			
			boolean valid = ControlService.validateControl(testUser, ct2, ControlActionEnumType.WRITE, "badpassword");
			logger.info("Validated (should be false): " + valid);
			
			valid = ControlService.validateControl(testUser, ct2, ControlActionEnumType.WRITE, "P@55woRd");
			logger.info("Validated (should be true): " + valid);
			
			/*
			PolicyDefinitionType pdt = PolicyDefinitionUtil.generatePolicyDefinition(pol);
			PolicyRequestType prt = PolicyDefinitionUtil.generatePolicyRequest(pdt);
			logger.info("Param facts: " + prt.getFacts().size());
			prt.getFacts().get(0).setFactData("badpassword");
			PolicyResponseType prr = PolicyEvaluator.evaluatePolicyRequest(prt);
			*/
			
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private PolicyType getPasswordStrengthPolicy(UserType user){
		DirectoryGroupType pdir = null;
		DirectoryGroupType rdir = null;
		DirectoryGroupType podir = null;
		DirectoryGroupType odir = null;
		DirectoryGroupType fdir = null;
		PolicyType pol = null;
		RuleType useRule = null;
		PatternType pat = null;
		
		String pname = "Credential Usage Policy";
		String rname = "Credential Usage Rule";
		String patName = "Password Strength Pattern";
		String clsRegExClass = "org.cote.accountmanager.data.operation.RegexOperation";
		try {
			rdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Rules", testUser.getOrganization());
			pdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Patterns", testUser.getOrganization());
			fdir = Factories.getGroupFactory().getCreatePath(testUser, "~/Facts", testUser.getOrganization());
			podir = Factories.getGroupFactory().getCreatePath(testUser, "~/Policies", testUser.getOrganization());
			odir = Factories.getGroupFactory().getCreatePath(testUser, "~/Operations", testUser.getOrganization());
			
			FactType setCredParamFact = getCreateCredentialParamFact(testUser,"Set Credential Parameter",fdir);
			setCredParamFact.setFactoryType(FactoryEnumType.CREDENTIAL);
			Factories.getFactFactory().updateFact(setCredParamFact);
			/* Expression based on StackOverflow comment : http://stackoverflow.com/questions/5142103/regex-for-password-strength
			^                         Start anchor
			(?=.*[A-Z].*[A-Z])        Ensure string has two uppercase letters.
			(?=.*[!@#$&*])            Ensure string has one special case letter.
			(?=.*[0-9].*[0-9])        Ensure string has two digits.
			(?=.*[a-z].*[a-z].*[a-z]) Ensure string has three lowercase letters.
			.{8}                      Ensure string is of length 8.
			$                         End anchor.
			(Original: ^(?=.*[A-Z].*[A-Z])(?=.*[!@#$&*])(?=.*[0-9].*[0-9])(?=.*[a-z].*[a-z].*[a-z]).{8}$)
			*/
			FactType strengthFact = getCreateStaticFact(testUser,"Password Strength Expression","^(?=.*[A-Z].*[A-Z])(?=.*[!@#$&*])(?=.*[0-9].*[0-9])(?=.*[a-z].*[a-z].*[a-z]).{5,}$",fdir);
			//FactType credParamFact = getCreateCredentialParamFact(testUser,"Credential Parameter",fdir);
			
			OperationType rgOp = getCreateOperation(testUser,"Regex Evaluator",clsRegExClass,odir);
			
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
	
	private PolicyType getCreatePolicy(UserType user, String name, DirectoryGroupType dir){

		PolicyType policy = null;

		try {
			policy = Factories.getPolicyFactory().getByName(name, dir);
			if(policy == null){
				policy = Factories.getPolicyFactory().newPolicy(testUser, dir);
				policy.setCondition(ConditionEnumType.ALL);
				policy.setName(name);

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
	private FactType getCreateCredentialParamFact(UserType user,String name, DirectoryGroupType fdir) throws ArgumentException, FactoryException{
		FactType srcFact = Factories.getFactFactory().getByName(name, fdir);
		if(srcFact != null) return srcFact;
		srcFact = Factories.getFactFactory().newFact(user, fdir);
		srcFact.setName(name);
		srcFact.setFactType(FactEnumType.PARAMETER);
		srcFact.setFactoryType(FactoryEnumType.CREDENTIAL);
		Factories.getFactFactory().addFact(srcFact);
		return srcFact;
	}

	private FactType getCreateStaticFact(UserType user,String name, String val, DirectoryGroupType fdir) throws ArgumentException, FactoryException{
		FactType srcFact = Factories.getFactFactory().getByName(name, fdir);
		if(srcFact != null) return srcFact;
		srcFact = Factories.getFactFactory().newFact(user, fdir);
		srcFact.setName(name);
		srcFact.setFactType(FactEnumType.STATIC);
		srcFact.setFactData(val);
		Factories.getFactFactory().addFact(srcFact);
		return srcFact;
	}
	private PatternType getCreatePattern(UserType user, String name, String factUrn, String matchUrn, DirectoryGroupType dir){

		PatternType pattern = null;

		try {
			pattern = Factories.getPatternFactory().getByName(name, dir);
			/*
			if(pattern != null){
				Factories.getPatternFactory().deletePattern(pattern);
				pattern = null;
			}
			*/
			if(pattern == null){
				pattern = Factories.getPatternFactory().newPattern(testUser, dir);
				pattern.setName(name);
				pattern.setPatternType(PatternEnumType.EXPRESSION);
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
	private RuleType getCreateRule(UserType user, String name, DirectoryGroupType dir){

		RuleType rule = null;

		try {
			rule = Factories.getRuleFactory().getByName(name, dir);
			if(rule != null){
				Factories.getRuleFactory().deleteRule(rule);
				rule = null;
			}
			if(rule == null){
				rule = Factories.getRuleFactory().newRule(testUser, dir);
				rule.setName(name);
				rule.setRuleType(RuleEnumType.PERMIT);
				rule.setCondition(ConditionEnumType.ALL);
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
	
	private OperationType getCreateOperation(UserType user, String name, String className, DirectoryGroupType dir) throws FactoryException, ArgumentException{
		OperationType op = Factories.getOperationFactory().getByName(name, dir);
		/*
		if(op2 != null){
			Factories.getOperationFactory().deleteOperation(op2);
			op2 = null;
		}
		*/
		if(op == null){
			op = Factories.getOperationFactory().newOperation(testUser, dir);
			op.setName(name);
			op.setOperationType(OperationEnumType.INTERNAL);
			op.setOperation(className);
			Factories.getOperationFactory().addOperation(op);
			op = Factories.getOperationFactory().getByName(name, dir);
		}
		return op;
	}
	
}
