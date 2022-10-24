package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.Test;

public class TestHierarchyConstraint extends BaseDataAccessTest {

	@Test
	public void TestParentConstraint() {
		String parentName = UUID.randomUUID().toString();
		DirectoryGroupType dir = null;
		DirectoryGroupType tdir = null;
		DirectoryGroupType gdir = null;
		DirectoryGroupType pdir = null;
		DirectoryGroupType cdir = null;
		DirectoryGroupType gcdir = null;
		GroupFactory gfact = null;
		boolean valid = false;
		boolean invalid = false;
		try {
			gfact = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP));
			dir = gfact.getCreateDirectory(testUser, "HierTest", testUser.getHomeDirectory(), testUser.getOrganizationId());
			tdir = gfact.getCreateDirectory(testUser, parentName, dir, testUser.getOrganizationId());
			gdir = gfact.getCreateDirectory(testUser, "Grandparent", tdir, testUser.getOrganizationId());
			pdir = gfact.getCreateDirectory(testUser, "Parent", gdir, testUser.getOrganizationId());
			cdir = gfact.getCreateDirectory(testUser, "Child", pdir, testUser.getOrganizationId());
			gcdir = gfact.getCreateDirectory(testUser, "Grandchild", cdir, testUser.getOrganizationId());
			assertNotNull("Expected grandchild node", gcdir);
			valid = gfact.validateHierarchy(gcdir);
			
			tdir.setParentId(gcdir.getId());
			
			invalid = gfact.validateHierarchy(tdir);
			
		} catch (FactoryException | ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		assertTrue("Expected hierarchy to be valid", valid);
		assertFalse("Expected hierarchy to be invalid", invalid);
		
		
		
	}
	
}
