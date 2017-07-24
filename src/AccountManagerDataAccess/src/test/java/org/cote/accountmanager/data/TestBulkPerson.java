/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
import org.cote.accountmanager.data.factory.UserFactory;
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
import org.junit.Test;

public class TestBulkPerson extends BaseDataAccessTest{
	/*
	@Test
	public void TestBulkPersonWithNoInfo(){
		boolean success = false;
		DirectoryGroupType pDir = null;
		try{
			pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganization());
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			String guid = UUID.randomUUID().toString();
			PersonType new_person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser,pDir);
			new_person.setName("BulkPerson-" + guid);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, new_person);
			
			
			logger.info("Retrieving Bulk Person");
			PersonType check = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup("BulkPerson-" + guid,pDir);
			assertNotNull("Failed person cache check",check);
			
			logger.info("Retrieving Person By Id");
			check = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getById(new_person.getId(),pDir.getOrganization());
			assertNotNull("Failed id cache check",check);
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			success = true;
		}
		catch(FactoryException fe){
			logger.error("Error",fe);
		}  catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
		assertTrue("Success bit is false",success);
	}
	*/
	
	@Test
	public void TestBulkPersonWithInfo(){
		boolean success = false;
		DirectoryGroupType pDir = null;
		try{
			pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganizationId());
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			String guid = UUID.randomUUID().toString();
			PersonType new_person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser,pDir.getId());
			new_person.setName("BulkPerson-" + guid);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, new_person);
			
			ContactInformationType cit = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation(new_person);
			cit.setOwnerId(testUser.getId());

			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACTINFORMATION, cit);
			
			new_person.setContactInformation(cit);
			
			AddressType addr = ((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).newAddress(testUser,pDir.getId());
			addr.setName(new_person.getName());
			addr.setPreferred(true);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ADDRESS, addr);
			cit.getAddresses().add(addr);
			
			addr = ((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).newAddress(testUser,pDir.getId());
			addr.setName(new_person.getName() + "-2");
			addr.setPreferred(false);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ADDRESS, addr);
			cit.getAddresses().add(addr);
			
			ContactType ct = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).newContact(testUser, pDir.getId());
			ct.setName(new_person.getName());
			ct.setPreferred(true);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACT, ct);

			
			cit.getContacts().add(ct);
			
			UserType user = ((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).newUser(new_person.getName(), UserEnumType.DEVELOPMENT, UserStatusEnumType.RESTRICTED, new_person.getOrganizationId());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.USER, user);
			CredentialService.newCredential(CredentialEnumType.HASHED_PASSWORD,sessionId, user, user, "password1".getBytes("UTF-8"), true,true,false);

			
			//new_person.getUsers().add(user);
			
			
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
		catch(FactoryException fe){
			logger.error("Error",fe);
		}  catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
		catch(Exception e){
			logger.error("Unknown Exception: " + e.getMessage());
			logger.error("Error",e);
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
			pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Persons", testUser.getHomeDirectory(), testUser.getOrganization());
			
			PersonType child = getCreatePerson(new_child_name, pDir);
			PersonType partner = getCreatePerson(new_partner_name, pDir);
			
			person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(testUser,pDir);
			person.setName(new_name);
			person.getDependents().add(child);
			person.getPartners().add(partner);

			assertTrue("Failed to add new person",((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).addPerson(person));
			person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup(new_name, pDir);
			((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).populate(person);
			assertNotNull("Person is null",person);
			assertTrue("Partner not retrieved",person.getPartners().size() == 1);
			assertTrue("Child not retrieved",person.getDependents().size() == 1);

		}
		catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
	}
*/


	
}