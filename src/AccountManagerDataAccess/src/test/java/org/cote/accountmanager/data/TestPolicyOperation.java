package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;

import org.cote.accountmanager.data.policy.PolicyDefinitionUtil;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.data.services.PolicyService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.PolicyDefinitionType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.util.JSONUtil;
import org.junit.Test;

public class TestPolicyOperation extends BaseDataAccessTest {

	
	
	@Test
	public void TestLoadPolicy() {
		String policyData = getResourceAsString("./basicPolicyFunctionStructure.json");
		assertNotNull("Policy data is null", policyData);
		PolicyType policy = JSONUtil.importObject(policyData, PolicyType.class);
		assertNotNull("Policy object is null", policy);
		PolicyType impPolicy = null;
		try {
			impPolicy = PolicyService.importPolicy(testUser, testUser.getHomeDirectory(), policyData);
		} catch (NullPointerException | ArgumentException | FactoryException | DataAccessException | DataException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		assertNotNull("Policy failed to import", impPolicy);
		PolicyDefinitionType pdt = null;
		PolicyRequestType prt = null;
		PolicyResponseType prr = null;
		try {
			pdt = PolicyDefinitionUtil.generatePolicyDefinition(impPolicy);
			assertNotNull("Policy definition is null", pdt);
			prt = PolicyDefinitionUtil.generatePolicyRequest(pdt);
			assertNotNull("Policy request is null", prt);
			prr = PolicyEvaluator.evaluatePolicyRequest(prt);
		} catch (FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		assertNotNull("Policy response is null", prr);
		//logger.info(JSONUtil.exportObject(impPolicy));
	}
	
}
