/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.cote.accountmanager.data.factory.AddressFactory;
import org.cote.accountmanager.data.factory.ContactFactory;
import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
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
			pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganization());
			person = getCreatePerson(new_name,pDir);
			assertNotNull("Contact information was not allotted",person.getContactInformation());

		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Contacts", testUser.getHomeDirectory(), testUser.getOrganization());
			contact = getCreateContact(new_name,pDir);
			assertNotNull("Contact information was not allotted",contact);

		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Addresss", testUser.getHomeDirectory(), testUser.getOrganization());
			address = getCreateAddress(new_name,pDir);
			assertNotNull("Address information was not allotted",address);

		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganizationId());
			
			PersonType child = getCreatePerson(new_child_name, pDir);
			addContactValues(child, new_child_name);
			PersonType partner = getCreatePerson(new_partner_name, pDir);
			addContactValues(child, new_partner_name);
			person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser,pDir.getId());
			person.setName(new_name);
			person.getDependents().add(child);
			person.getPartners().add(partner);

			assertTrue("Failed to add new person",((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).add(person));
			
			person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup(new_name, pDir);
			addContactValues(person,new_name);
			((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).populate(person);
			assertNotNull("Person is null",person);
			assertTrue("Partner not retrieved",person.getPartners().size() == 1);
			assertTrue("Child not retrieved",person.getDependents().size() == 1);

		}
		catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
/*
	
	@Test
	public void TestUpdatePerson(){
		DirectoryGroupType pDir = null;

		PersonType person = null;
		PersonType partner = null;
		try {
			pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganization());
			person = getCreatePerson(test_person_name,pDir);
			partner = getCreatePerson(test_partner_name,pDir);
			/// Once populated, it's possible to add in the same object more than once
			/// This will violate the constraint - it's a choice between catching it up front, or letting the DB error out, and it affects all participation types
			///
			person.getPartners().clear();
			person.getPartners().add(partner);
			assertTrue("Failed to update person",((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).updatePerson(person));
			person = getCreatePerson(test_person_name,pDir);

			assertTrue("Partners not retrieved",person.getPartners().size() == 1);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		
	}

	@Test
	public void TestDeletePerson(){
		DirectoryGroupType pDir = null;

		PersonType person = null;
		PersonType partner = null;
		String testName = "DeletePerson-" + UUID.randomUUID().toString();
		try {
			pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganization());
			person = getCreatePerson(testName,pDir);
			partner = getCreatePerson(test_partner_name,pDir);
			/// Once populated, it's possible to add in the same object more than once
			/// This will violate the constraint - it's a choice between catching it up front, or letting the DB error out, and it affects all participation types
			///
			person.getPartners().clear();
			person.getPartners().add(partner);
			assertTrue("Failed to update person",((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).updatePerson(person));
			
			boolean deleted = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).deletePerson(person);

			assertTrue("Person not deleted",deleted);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		
	}
	*/
	/*
	@Test
	public void TestSearchPersons(){
		DirectoryGroupType pDir = null;
		try {
			pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganization());
			for(int i = 0; i < 20;i++){
				PersonType p = getCreatePerson("SearchPerson-" + (i + 1),pDir);
				/ *
				if(p.getContactInformation().getFirstName() == null){
					p.getContactInformation().setFirstName("SearchPerson-" + (i + 1) + "First");
					p.getContactInformation().setLastName("SearchPerson-" + (i + 1) + "Last");
					((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).updateContactInformation(p.getContactInformation());
				}
				* /
			}

			List<PersonType> persons = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).searchPersons("SearchPerson*",0,10,pDir);
			assertTrue("Expected count is 10",persons.size() == 10);
			for(int i = 0; i < persons.size();i++){
				logger.info("Search Result " + (i + 1) + ":" + persons.get(i).getName());
			}
			persons = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).searchPersons("SearchPerson*",10,20,pDir);
			assertTrue("Expected count is 10",persons.size() == 10);
			for(int i = 0; i < persons.size();i++){
				logger.info("Search Result " + (i + 1) + ":" + persons.get(i).getName());
			}

		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	*/
	private AddressType getCreateAddress(String name, DirectoryGroupType pDir){
		AddressType address = null;
		try{
			address = ((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).getByNameInGroup(name, pDir);
			if(address == null){
				address = ((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).newAddress(testUser,pDir.getId());
				address.setName(name);
				assertTrue("Failed to add new address",((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).add(address));
				address = ((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).getByNameInGroup(name, pDir);
				assertNotNull("Address is null",address);
			}
			((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).populate(address);
		}
		catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return address;
	}
	private ContactType getCreateContact(String name, DirectoryGroupType pDir){
		ContactType contact = null;
		try{
			contact = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).getByNameInGroup(name, pDir);
			if(contact == null){
				contact = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).newContact(testUser,pDir.getId());
				contact.setName(name);
				assertTrue("Failed to add new contact",((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).add(contact));
				contact = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).getByNameInGroup(name, pDir);
				assertNotNull("Contact is null",contact);
			}
			((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).populate(contact);
		}
		catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return contact;
	}
	private PersonType getCreatePerson(String name, DirectoryGroupType pDir){
		PersonType person = null;
		try{
			person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup(name, pDir);
			if(person == null){
				person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser,pDir.getId());
				person.setName(name);
				assertTrue("Failed to add new person",((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).add(person));
				person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup(name, pDir);
				assertNotNull("Person is null",person);
			}
			((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).populate(person);
		}
		catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return person;
	}
	private void addContactValues(PersonType person, String name) throws ArgumentException{
		boolean bUp = false;

		try{
			((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).populate(person);
			assertNotNull("Person is null",person);
			assertNotNull("Contact info is null",person.getContactInformation());
			if(person.getContactInformation().getAddresses().isEmpty()){
				AddressType homeAddr = getCreateAddress(name,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(person.getGroupId(),person.getOrganizationId()));
				setDemoAddressValues(homeAddr);
				((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).update(homeAddr);
				person.getContactInformation().getAddresses().add(homeAddr);
				bUp = true;
			}
			if(person.getContactInformation().getContacts().isEmpty()){
				ContactType homeEmail = getCreateContact(name,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(person.getGroupId(),person.getOrganizationId()));
				setHomeEmailValues(homeEmail);
				((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).update(homeEmail);
				person.getContactInformation().getContacts().add(homeEmail);
				bUp = true;
			}
			if(bUp){
				((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).update(person.getContactInformation());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,fe);
		} 
	}
	private void setHomeEmailValues(ContactType ct){
		ct.setContactType(ContactEnumType.EMAIL);
		ct.setLocationType(LocationEnumType.HOME);
		ct.setContactValue("email");
	}
	private void setDemoAddressValues(AddressType addr){
		addr.setAddressLine1("street");
		addr.setAddressLine2("[blank]");
		addr.setCity("city");
		addr.setState("ST");
		addr.setCountry("CO");
		addr.setRegion("[blank]");
		addr.setPostalCode("00000");
		addr.setLocationType(LocationEnumType.HOME);
	}
	
}