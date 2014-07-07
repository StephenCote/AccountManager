package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.junit.Test;


public class TestRoleService extends BaseDataAccessTest{

	@Test
	public void testSystemRoles(){
		boolean isDataAdmin = false;
		try {
			isDataAdmin = AuthorizationService.isDataAdministratorInOrganization(testUser, testUser.getOrganization());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse("User should not be a data admin",isDataAdmin);
	}
	
}