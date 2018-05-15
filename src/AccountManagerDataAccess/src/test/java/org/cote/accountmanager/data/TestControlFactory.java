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

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.ControlFactory;
import org.cote.accountmanager.data.factory.FactFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.PatternFactory;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.factory.RuleFactory;
import org.cote.accountmanager.data.security.ControlService;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ControlActionEnumType;
import org.cote.accountmanager.objects.ControlEnumType;
import org.cote.accountmanager.objects.ControlType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.PatternEnumType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.junit.Test;
public class TestControlFactory extends BaseDataAccessTest {
	public static final Logger logger = LogManager.getLogger(TestControlFactory.class);
	
	@Test
	public void TestControlCRUD(){
		CredentialType everyCred = new CredentialType();
		everyCred.setNameType(NameEnumType.CREDENTIAL);
		everyCred.setCredentialType(CredentialEnumType.HASHED_PASSWORD);
		everyCred.setOrganizationId(testUser.getOrganizationId());
		
		CredentialType cred = CredentialService.getPrimaryCredential(testUser2,CredentialEnumType.HASHED_PASSWORD,false);
		PolicyType pol = getPasswordStrengthPolicy(testUser);
		assertNotNull("Policy is null",pol);
		assertNotNull("Credential is null",cred);
		ControlType ct1 = null;
		ControlType ct2 = null;
		try {
			List<ControlType> ctls2 = ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).getControlsForType(cred, true, false);
			logger.info("All password controls: " + ctls2.size());
			
			logger.info("Deleting direct controls: " + ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).deleteControlsForType(cred));
			logger.info("Deleting global controls: " + ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).deleteControlsForType(everyCred));
			
			ct1 = ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).newControl(testUser, cred);
			ct2 = ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).newControl(testUser, everyCred);
			ct2.setControlId(pol.getId());
			ct2.setControlType(ControlEnumType.POLICY);
			ct2.setControlAction(ControlActionEnumType.WRITE);
			//ct1.setControlType(ControlEnumType.POLICY);
			
			((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).add(ct1);
			((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).add(ct2);
			/// direct only
			List<ControlType> ctls = ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).getControlsForType(cred, false, false);
			/// both direct and global
			ctls2 = ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).getControlsForType(cred, true, false);
			/// only global
			List<ControlType> ctls3 = ((ControlFactory)Factories.getFactory(FactoryEnumType.CONTROL)).getControlsForType(cred, false, true);
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
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			rdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Rules", testUser.getOrganizationId());
			pdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Patterns", testUser.getOrganizationId());
			fdir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Facts", testUser.getOrganizationId());
			podir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Policies", testUser.getOrganizationId());
			odir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(testUser, "~/Operations", testUser.getOrganizationId());
			
			FactType setCredParamFact = getCreateCredentialParamFact(testUser,"Set Credential Parameter",fdir);
			setCredParamFact.setFactoryType(FactoryEnumType.CREDENTIAL);
			((FactFactory)Factories.getFactory(FactoryEnumType.FACT)).update(setCredParamFact);
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
			((PatternFactory)Factories.getFactory(FactoryEnumType.PATTERN)).update(pat);
			useRule.getPatterns().clear();
			useRule.getPatterns().add(pat);
			((RuleFactory)Factories.getFactory(FactoryEnumType.RULE)).update(useRule);
			pol.getRules().clear();
			pol.getRules().add(useRule);
			((PolicyFactory)Factories.getFactory(FactoryEnumType.POLICY)).update(pol);
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		} 
		return pol;
	}
	


	
}
