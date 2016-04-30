package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.Test;


public class TestBulkPerson extends BaseDataAccessTest{
	/*
	@Test
	public void TestBulkPersonWithNoInfo(){
		boolean success = false;
		DirectoryGroupType pDir = null;
		try{
			pDir = Factories.getGroupFactory().getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganization());
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			String guid = UUID.randomUUID().toString();
			PersonType new_person = Factories.getPersonFactory().newPerson(testUser,pDir);
			new_person.setName("BulkPerson-" + guid);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, new_person);
			
			
			logger.info("Retrieving Bulk Person");
			PersonType check = Factories.getPersonFactory().getByNameInGroup("BulkPerson-" + guid,pDir);
			assertNotNull("Failed person cache check",check);
			
			logger.info("Retrieving Person By Id");
			check = Factories.getPersonFactory().getById(new_person.getId(),pDir.getOrganization());
			assertNotNull("Failed id cache check",check);
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			success = true;
		}
		catch(FactoryException fe){
			fe.printStackTrace();
		}  catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("Success bit is false",success);
	}
	*/
	
	@Test
	public void TestBulkPersonWithInfo(){
		boolean success = false;
		DirectoryGroupType pDir = null;
		try{
			pDir = Factories.getGroupFactory().getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganizationId());
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			String guid = UUID.randomUUID().toString();
			PersonType new_person = Factories.getPersonFactory().newPerson(testUser,pDir.getId());
			new_person.setName("BulkPerson-" + guid);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, new_person);
			
			ContactInformationType cit = Factories.getContactInformationFactory().newContactInformation(new_person);
			cit.setOwnerId(testUser.getId());

			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACTINFORMATION, cit);
			
			new_person.setContactInformation(cit);
			
			AddressType addr = Factories.getAddressFactory().newAddress(testUser,pDir.getId());
			addr.setName(new_person.getName());
			addr.setPreferred(true);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ADDRESS, addr);
			cit.getAddresses().add(addr);
			
			addr = Factories.getAddressFactory().newAddress(testUser,pDir.getId());
			addr.setName(new_person.getName() + "-2");
			addr.setPreferred(false);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ADDRESS, addr);
			cit.getAddresses().add(addr);
			
			ContactType ct = Factories.getContactFactory().newContact(testUser, pDir.getId());
			ct.setName(new_person.getName());
			ct.setPreferred(true);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACT, ct);

			
			cit.getContacts().add(ct);
			
			UserType user = Factories.getUserFactory().newUser(new_person.getName(), UserEnumType.DEVELOPMENT, UserStatusEnumType.RESTRICTED, new_person.getOrganizationId());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.USER, user);
			CredentialService.newCredential(CredentialEnumType.HASHED_PASSWORD,sessionId, user, user, "password1".getBytes("UTF-8"), true,true,false);

			
			//new_person.getUsers().add(user);
			
			
			logger.info("Retrieving Bulk Person");
			PersonType check = Factories.getPersonFactory().getByNameInGroup("BulkPerson-" + guid,pDir);
			assertNotNull("Failed person cache check",check);
			
			logger.info("Retrieving Person By Id");
			check = Factories.getPersonFactory().getById(new_person.getId(),pDir.getOrganizationId());
			assertNotNull("Failed id cache check",check);
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			
			
			check = Factories.getPersonFactory().getById(new_person.getId(),pDir.getOrganizationId());
			Factories.getPersonFactory().populate(check);
			assertNotNull("Failed person check",check);
			assertTrue("Person is still cached with bulk id",check.getId() > 0);
			assertNotNull("Failed contact check",check.getContactInformation());
			assertTrue("Contact is still cached with bulk id",check.getContactInformation().getId() > 0);
			assertTrue("Failed contact value check",check.getContactInformation().getContacts().size() > 0);
			assertTrue("Failed address value check",check.getContactInformation().getAddresses().size() > 0);
			
			
			success = true;
		}
		catch(FactoryException fe){
			fe.printStackTrace();
		}  catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch(Exception e){
			logger.error("Unknown Exception: " + e.getMessage());
			e.printStackTrace();
		}
		assertTrue("Success bit is false",success);
	}

	
/*	
	@Test
	public void TestAddPersonWithStuff(){
		DirectoryGroupType pDir = null;
		String new_name = UUID.randomUUID().toString();
		String new_partner_name = UUID.randomUUID().toString();
		String new_child_name = UUID.randomUUID().toString();
		PersonType person = null;
		
		try{
			pDir = Factories.getGroupFactory().getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganization());
			
			PersonType child = getCreatePerson(new_child_name, pDir);
			PersonType partner = getCreatePerson(new_partner_name, pDir);
			
			person = Factories.getPersonFactory().newPerson(testUser,pDir);
			person.setName(new_name);
			person.getDependents().add(child);
			person.getPartners().add(partner);

			assertTrue("Failed to add new person",Factories.getPersonFactory().addPerson(person));
			person = Factories.getPersonFactory().getByNameInGroup(new_name, pDir);
			Factories.getPersonFactory().populate(person);
			assertNotNull("Person is null",person);
			assertTrue("Partner not retrieved",person.getPartners().size() == 1);
			assertTrue("Child not retrieved",person.getDependents().size() == 1);

		}
		catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
*/


	
}