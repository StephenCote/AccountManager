package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.security.TokenService;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.Test;

public class TestTokenService extends BaseDataAccessTest {
	public static final Logger logger = LogManager.getLogger(TestTokenService.class);
	
	@Test
	public void TestCreateSecuritySpoolToken(){
		SecuritySpoolType token = TokenService.newSecurityToken(testUser);
		assertNotNull("Token is null", token);
	}
	
	
	@Test
	public void TestMaterializedToken(){
		try {
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(testUser);
		} catch (FactoryException | ArgumentException e) {
			
			logger.error("Error",e);
		}
		String materialToken = null; //TokenService.newMaterializedToken(testUser, testUser.getHomeDirectory());
		assertNotNull("Token is null", materialToken);
		logger.info("Material Token: " + materialToken);
	}
	
}
