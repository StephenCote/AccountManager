package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;

import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.junit.Test;


public class TestRoleService extends BaseDataAccessTest{

	@Test
	public void testSystemRoles(){
		boolean isDataAdmin = false;
		try {
			isDataAdmin =RoleService.isFactoryAdministrator(testUser, Factories.getDataFactory());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		} catch (FactoryException e) {
			
			logger.error(e.getStackTrace());
		}
		assertFalse("User should not be a data admin",isDataAdmin);
	}
	
}