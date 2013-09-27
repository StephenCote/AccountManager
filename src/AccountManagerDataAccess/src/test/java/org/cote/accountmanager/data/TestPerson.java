package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.junit.Test;


public class TestPerson extends BaseDataAccessTest{
	private static String test_person_name = "TestPerson";
	private static String test_partner_name = "TestPartner";
	
/*
	@Test
	public void TestAddPersonNoContact(){
		DirectoryGroupType pDir = null;
		String new_name = UUID.randomUUID().toString();
		PersonType person = null;
		try {
			pDir = Factories.getGroupFactory().getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganization());
			person = getCreatePerson(new_name,pDir);
			assertNotNull("Contact information was not allotted",person.getContact());

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
*/
/*
	@Test
	public void TestAddContact(){
		DirectoryGroupType pDir = null;
		String new_name = UUID.randomUUID().toString();
		ContactType contact = null;
		try {
			pDir = Factories.getGroupFactory().getCreateDirectory(testUser, "Contacts", testUser.getHomeDirectory(), testUser.getOrganization());
			contact = getCreateContact(new_name,pDir);
			assertNotNull("Contact information was not allotted",contact);

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	*/
	/*
	@Test
	public void TestAddAddress(){
		DirectoryGroupType pDir = null;
		String new_name = UUID.randomUUID().toString();
		AddressType address = null;
		try {
			pDir = Factories.getGroupFactory().getCreateDirectory(testUser, "Addresss", testUser.getHomeDirectory(), testUser.getOrganization());
			address = getCreateAddress(new_name,pDir);
			assertNotNull("Address information was not allotted",address);

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	
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
			addContactValues(child, new_child_name);
			PersonType partner = getCreatePerson(new_partner_name, pDir);
			addContactValues(child, new_partner_name);
			person = Factories.getPersonFactory().newPerson(testUser,pDir);
			person.setName(new_name);
			person.getDependents().add(child);
			person.getPartners().add(partner);

			assertTrue("Failed to add new person",Factories.getPersonFactory().addPerson(person));
			
			person = Factories.getPersonFactory().getByName(new_name, pDir);
			addContactValues(person,new_name);
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
/*
	
	@Test
	public void TestUpdatePerson(){
		DirectoryGroupType pDir = null;

		PersonType person = null;
		PersonType partner = null;
		try {
			pDir = Factories.getGroupFactory().getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganization());
			person = getCreatePerson(test_person_name,pDir);
			partner = getCreatePerson(test_partner_name,pDir);
			/// Once populated, it's possible to add in the same object more than once
			/// This will violate the constraint - it's a choice between catching it up front, or letting the DB error out, and it affects all participation types
			///
			person.getPartners().clear();
			person.getPartners().add(partner);
			assertTrue("Failed to update person",Factories.getPersonFactory().updatePerson(person));
			person = getCreatePerson(test_person_name,pDir);

			assertTrue("Partners not retrieved",person.getPartners().size() == 1);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	@Test
	public void TestDeletePerson(){
		DirectoryGroupType pDir = null;

		PersonType person = null;
		PersonType partner = null;
		String testName = "DeletePerson-" + UUID.randomUUID().toString();
		try {
			pDir = Factories.getGroupFactory().getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganization());
			person = getCreatePerson(testName,pDir);
			partner = getCreatePerson(test_partner_name,pDir);
			/// Once populated, it's possible to add in the same object more than once
			/// This will violate the constraint - it's a choice between catching it up front, or letting the DB error out, and it affects all participation types
			///
			person.getPartners().clear();
			person.getPartners().add(partner);
			assertTrue("Failed to update person",Factories.getPersonFactory().updatePerson(person));
			
			boolean deleted = Factories.getPersonFactory().deletePerson(person);

			assertTrue("Person not deleted",deleted);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	*/
	/*
	@Test
	public void TestSearchPersons(){
		DirectoryGroupType pDir = null;
		try {
			pDir = Factories.getGroupFactory().getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganization());
			for(int i = 0; i < 20;i++){
				PersonType p = getCreatePerson("SearchPerson-" + (i + 1),pDir);
				/ *
				if(p.getContact().getFirstName() == null){
					p.getContact().setFirstName("SearchPerson-" + (i + 1) + "First");
					p.getContact().setLastName("SearchPerson-" + (i + 1) + "Last");
					Factories.getContactInformationFactory().updateContactInformation(p.getContact());
				}
				* /
			}

			List<PersonType> persons = Factories.getPersonFactory().searchPersons("SearchPerson*",0,10,pDir);
			assertTrue("Expected count is 10",persons.size() == 10);
			for(int i = 0; i < persons.size();i++){
				logger.info("Search Result " + (i + 1) + ":" + persons.get(i).getName());
			}
			persons = Factories.getPersonFactory().searchPersons("SearchPerson*",10,20,pDir);
			assertTrue("Expected count is 10",persons.size() == 10);
			for(int i = 0; i < persons.size();i++){
				logger.info("Search Result " + (i + 1) + ":" + persons.get(i).getName());
			}

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	private AddressType getCreateAddress(String name, DirectoryGroupType pDir){
		AddressType address = null;
		try{
			address = Factories.getAddressFactory().getByName(name, pDir);
			if(address == null){
				address = Factories.getAddressFactory().newAddress(testUser,pDir);
				address.setName(name);
				assertTrue("Failed to add new address",Factories.getAddressFactory().addAddress(address));
				address = Factories.getAddressFactory().getByName(name, pDir);
				assertNotNull("Address is null",address);
			}
			Factories.getAddressFactory().populate(address);
		}
		catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return address;
	}
	private ContactType getCreateContact(String name, DirectoryGroupType pDir){
		ContactType contact = null;
		try{
			contact = Factories.getContactFactory().getByName(name, pDir);
			if(contact == null){
				contact = Factories.getContactFactory().newContact(testUser,pDir);
				contact.setName(name);
				assertTrue("Failed to add new contact",Factories.getContactFactory().addContact(contact));
				contact = Factories.getContactFactory().getByName(name, pDir);
				assertNotNull("Contact is null",contact);
			}
			Factories.getContactFactory().populate(contact);
		}
		catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return contact;
	}
	private PersonType getCreatePerson(String name, DirectoryGroupType pDir){
		PersonType person = null;
		try{
			person = Factories.getPersonFactory().getByName(name, pDir);
			if(person == null){
				person = Factories.getPersonFactory().newPerson(testUser,pDir);
				person.setName(name);
				assertTrue("Failed to add new person",Factories.getPersonFactory().addPerson(person));
				person = Factories.getPersonFactory().getByName(name, pDir);
				assertNotNull("Person is null",person);
			}
			Factories.getPersonFactory().populate(person);
		}
		catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return person;
	}
	private void addContactValues(PersonType person, String name){
		boolean bUp = false;
		try{
			if(person.getContact().getAddresses().size() == 0){
				AddressType homeAddr = getCreateAddress(name,person.getGroup());
				setDemoAddressValues(homeAddr);
				Factories.getAddressFactory().updateAddress(homeAddr);
				person.getContact().getAddresses().add(homeAddr);
				bUp = true;
			}
			if(person.getContact().getContacts().size() == 0){
				ContactType homeEmail = getCreateContact(name,person.getGroup());
				setHomeEmailValues(homeEmail);
				Factories.getContactFactory().updateContact(homeEmail);
				person.getContact().getContacts().add(homeEmail);
				bUp = true;
			}
			if(bUp){
				Factories.getContactInformationFactory().updateContactInformation(person.getContact());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (DataAccessException e) {
			logger.error(e.getMessage());

			e.printStackTrace();
		}
	}
	private void setHomeEmailValues(ContactType ct){
		ct.setContactType(ContactEnumType.EMAIL);
		ct.setLocationType(LocationEnumType.HOME);
		ct.setContactValue("sw.cote@gmail.com");
	}
	private void setDemoAddressValues(AddressType addr){
		addr.setAddressLine1("6808 Denny Peak DR SE");
		addr.setAddressLine2("[blank]");
		addr.setCity("Snoqualmie");
		addr.setState("WA");
		addr.setCountry("US");
		addr.setRegion("[blank]");
		addr.setPostalCode("98065");
		addr.setLocationType(LocationEnumType.HOME);
	}
	
}