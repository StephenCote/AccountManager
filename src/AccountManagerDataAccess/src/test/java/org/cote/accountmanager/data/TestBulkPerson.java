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

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.cote.accountmanager.data.factory.AddressFactory;
import org.cote.accountmanager.data.factory.BulkFactory;
import org.cote.accountmanager.data.factory.ContactFactory;
import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
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
import org.junit.Test;

public class TestBulkPerson extends BaseDataAccessTest{
	
	
	@Test
	public void TestBulkPolyPersonWithInfo(){
		boolean success = false;
		DirectoryGroupType pDir = null;
		String personPrefix = "BulkPolyPerson-";
		try{
			PersonFactory pFact = (PersonFactory)Factories.getFactory(FactoryEnumType.PERSON);
			UserFactory uFact = (UserFactory)Factories.getFactory(FactoryEnumType.USER);
			GroupFactory gFact = (GroupFactory)Factories.getFactory(FactoryEnumType.GROUP); 
			BulkFactory bFact = BulkFactories.getBulkFactory();
			
			uFact.populate(testUser);
			pDir = gFact.getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganizationId());
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			String guid = UUID.randomUUID().toString();
			

			PersonType new_person = pFact.newPerson(testUser,pDir.getId());
			new_person.setName(personPrefix + guid);
			bFact.createBulkEntry(sessionId, FactoryEnumType.PERSON, new_person);
			addressPerson(testUser, pDir, new_person, sessionId);
			bFact.write(sessionId);
			bFact.close(sessionId);
			sessionId = BulkFactories.getBulkFactory().newBulkSession();
			
			new_person = pFact.getByNameInGroup(personPrefix + guid, pDir);
			assertNotNull("New Person is null", new_person);

			
			PersonType new_baby = pFact.newPerson(testUser,pDir.getId());
			new_baby.setName(personPrefix + "Baby-" + guid);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, new_baby);
			addressPerson(testUser, pDir, new_baby, sessionId);
			new_person.getDependents().add(new_baby);
			BulkFactories.getBulkFactory().modifyBulkEntry(sessionId, FactoryEnumType.PERSON, new_person);
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			
			
			success = true;
		}
		catch(FactoryException | ArgumentException | DataAccessException | UnsupportedEncodingException e){
			logger.error("Unknown Exception: " + e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Success bit is false",success);
	}
	
	@Test
	public void TestBulkPersonWithInfo(){
		boolean success = false;
		DirectoryGroupType pDir = null;
		try{
			((UserFactory)Factories.getFactory(FactoryEnumType.USER)).populate(testUser);
			pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganizationId());
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			String guid = UUID.randomUUID().toString();
			
			PersonType new_person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser,pDir.getId());
			new_person.setName("BulkPerson-" + guid);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, new_person);
			addressPerson(testUser, pDir, new_person, sessionId);

			PersonType new_baby = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser,pDir.getId());
			new_baby.setName("BulkPerson-Baby-" + guid);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, new_baby);
			addressPerson(testUser, pDir, new_baby, sessionId);

			logger.info("Retrieving Bulk Person");
			PersonType check = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup("BulkPerson-" + guid,pDir);
			assertNotNull("Failed person cache check",check);
			
			logger.info("Retrieving Person By Id");
			check = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getById(new_person.getId(),pDir.getOrganizationId());
			assertNotNull("Failed id cache check",check);
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			
			
			check = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getById(new_person.getId(),pDir.getOrganizationId());
			((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).populate(check);
			assertNotNull("Failed person check",check);
			assertTrue("Person is still cached with bulk id",check.getId() > 0);
			assertNotNull("Failed contact check",check.getContactInformation());
			assertTrue("Contact is still cached with bulk id",check.getContactInformation().getId() > 0);
			assertTrue("Failed contact value check",check.getContactInformation().getContacts().size() > 0);
			assertTrue("Failed address value check",check.getContactInformation().getAddresses().size() > 0);
			
			success = true;
		}
		catch(FactoryException | ArgumentException | DataAccessException | UnsupportedEncodingException e){
			logger.error("Unknown Exception: " + e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Success bit is false",success);
	}
	
	@Test
	public void TestBulkPersonWithNoInfo(){
		boolean success = false;
		DirectoryGroupType pDir = null;
		try{
			pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganizationId());
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			String guid = UUID.randomUUID().toString();
			PersonType new_person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser,pDir.getId());
			new_person.setName("BulkPerson-" + guid);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, new_person);
			
			
			logger.info("Retrieving Bulk Person");
			PersonType check = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup("BulkPerson-" + guid,pDir);
			assertNotNull("Failed person cache check",check);
			
			logger.info("Retrieving Person By Id");
			check = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getById(new_person.getId(),pDir.getOrganizationId());
			assertNotNull("Failed id cache check",check);
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			success = true;
		}
		catch(FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertTrue("Success bit is false",success);
	}
	
	
	private void addressPerson(UserType owner, DirectoryGroupType pDir, PersonType person, String sessionId) throws FactoryException, ArgumentException, UnsupportedEncodingException {
		ContactInformationType cit = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation(person);
		cit.setOwnerId(owner.getId());

		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACTINFORMATION, cit);
		
		person.setContactInformation(cit);
		
		AddressType addr = ((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).newAddress(owner,pDir.getId());
		addr.setName(person.getName());
		addr.setPreferred(true);
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ADDRESS, addr);
		cit.getAddresses().add(addr);
		
		addr = ((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).newAddress(owner,pDir.getId());
		addr.setName(person.getName() + "-2");
		addr.setPreferred(false);
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ADDRESS, addr);
		cit.getAddresses().add(addr);
		
		ContactType ct = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).newContact(owner, pDir.getId());
		ct.setName(person.getName());
		ct.setPreferred(true);
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACT, ct);

		
		cit.getContacts().add(ct);
		
		UserType user = ((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).newUser(person.getName(), UserEnumType.DEVELOPMENT, UserStatusEnumType.RESTRICTED, person.getOrganizationId());
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.USER, user);
		CredentialService.newCredential(CredentialEnumType.HASHED_PASSWORD,sessionId, user, user, "password1".getBytes("UTF-8"), true,true);

	}
	

	
}