package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.security.TokenService;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.junit.Test;

public class TestTokenService extends BaseDataAccessTest {
	public static final Logger logger = Logger.getLogger(TestTokenService.class.getName());
	
	@Test
	public void TestCreateSecuritySpoolToken(){
		SecuritySpoolType token = TokenService.newSecurityToken(testUser);
		assertNotNull("Token is null", token);
	}
	@Test
	public void TestMaterializedToken(){
		
		assertNotNull("Token is null", null);
	}
	
}
