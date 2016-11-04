package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.Test;

public class TestBulkDelete extends BaseDataAccessTest {
	
	@Test
	public void TestDeletePerson(){
		boolean success = false;
		DirectoryGroupType pDir = null;
		try{
			pDir = Factories.getGroupFactory().getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganizationId());
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			
			String uid = UUID.randomUUID().toString();
			PersonType person = Factories.getPersonFactory().newPerson(testUser, pDir.getId());
			person.setName(uid);
			assertTrue("Failed to add person",Factories.getPersonFactory().add(person));
			person = Factories.getPersonFactory().getByNameInGroup(uid, pDir);
			assertNotNull("Person is null",person);
			
			BulkFactories.getBulkFactory().deleteBulkEntry(sessionId, FactoryEnumType.PERSON, person);

			BulkFactories.getBulkFactory().write(sessionId);
			
			assertNull("Person should be null",Factories.getPersonFactory().getByNameInGroup(uid, pDir));
			
		}
		catch(FactoryException | ArgumentException | DataAccessException  e){
			logger.error(e);
		}
	}
}
