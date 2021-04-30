package org.cote.accountmanager.client.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.objects.VaultType;
import org.junit.Test;

public class TestVaultApi extends BaseClientTest {

	
	@Test
	public void TestCreateVault() {
		String vaultName = "Test Vault - " + UUID.randomUUID().toString();
		logger.info("Creating vault '" + vaultName + "'");
		String vault = AM6Util.createVault(testUserContext, String.class, vaultName);
		assertNotNull("Vault is null", vault);
		
		List<String> vaultUrns = AM6Util.listVaults(testUserContext, new ArrayList<String>().getClass());
		assertTrue("Expected at least one URN", vaultUrns.size() > 0);
		for(String urn : vaultUrns) {
			logger.info("Iterating vault " + urn);
		}
		String vaultUrn = AM6Util.getVaultUrn(testUserContext, String.class, vaultName);
		assertNotNull("Expected a vault urn from the name", vaultUrn);
		logger.info("Cleaning up vault " + vaultUrn);
		boolean deleted = AM6Util.deleteVault(testUserContext, Boolean.class, vaultUrn);
		assertTrue("Failed to delete vault " + vaultUrn, deleted);
		
		
	}

	
}
