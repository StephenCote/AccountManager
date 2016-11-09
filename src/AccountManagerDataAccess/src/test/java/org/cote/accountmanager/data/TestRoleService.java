package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;

import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.Test;

public class TestRoleService extends BaseDataAccessTest{

	@Test
	public void testSystemRoles(){
		boolean isDataAdmin = false;
		try {
			isDataAdmin =RoleService.isFactoryAdministrator(testUser, ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)));
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		assertFalse("User should not be a data admin",isDataAdmin);
	}
	
}