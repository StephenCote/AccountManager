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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.AddressFactory;
import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.Test;
public class TestContactInformationFactory extends BaseDataAccessTest{
	//public static final Logger logger = LogManager.getLogger(TestContactInformationFactory.class);
	private static long testRefId = 0;
	
	
	
	private UserType getUserTypeMock(){
		OrganizationFactory of = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION));
		
		UserType type = new UserType();
		type.setId(testRefId);
		type.setName("example");
		type.setOrganizationId(Factories.getDevelopmentOrganization().getId());
		return type;
	}
	
	@Test
	public void testAccountContactInfo(){
		AccountType acct = null;
		String testAcctName = "Test Account 1";
		try {
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(testUser);
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Accounts", testUser.getHomeDirectory(), testUser.getOrganizationId());
			DirectoryGroupType adir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Addresses", testUser.getHomeDirectory(), testUser.getOrganizationId());
			acct = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName(testAcctName, dir);
			if(acct != null){
				((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).delete(acct);
				acct = null;
			}
			if(acct == null){
				acct = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).newAccount(testUser, testAcctName, AccountEnumType.NORMAL,AccountStatusEnumType.NORMAL,dir.getId());
				if(((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).add(acct,true)){
					logger.info("Clearing address factory cache to break the foreign key link");
					((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).clearCache();
					acct = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName(testAcctName, dir);
				}
				else{
					acct = null;
				}
			}
			assertNotNull("Account is null",acct);
			logger.info("Testing account #" + acct.getId() + " in group #" + dir.getId());
			assertNull("ContactInformation is not null",acct.getContactInformation());
			((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).populate(acct);
			assertNotNull("ContactInformation is null",acct.getContactInformation());
			
			logger.info("Zero out cinfo references");
			acct.getContactInformation().getAddresses().clear();
			acct.getContactInformation().getContacts().clear();
			((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).update(acct);
			//Factories.clearCaches();
			
			AddressType addr = ((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).getByNameInGroup("Test Address 1", adir);
			if(addr == null){
				addr = ((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).newAddress(testUser, adir.getId());
				addr.setName("Test Address 1");
				if(((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).add(addr)){
					//Factories.clearCaches();
					addr = ((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).getByNameInGroup("Test Address 1", adir);
				}
				else{
					addr = null;
				}
			}
			assertNotNull("Address is null", addr);
			
			acct.getContactInformation().getAddresses().add(addr);
			((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).update(acct);
			long acctId = acct.getId();
			//Factories.clearCaches();
			acct = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName(testAcctName, dir);
			logger.info("Have Cache Id: " + ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).haveCacheId(acctId));
			assertNotNull("Account is null",acct);
			((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).populate(acct);
			assertTrue("Missing contact address",acct.getContactInformation().getAddresses().size() > 0);
			
			
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		
	}
	
	@Test
	public void testAddContactInformation(){
		Random r = new Random();
		testRefId = r.nextInt();
		if(testRefId < 0) testRefId *= -1;
		
		logger.info("Add w/ Ref Id: " + testRefId);
		UserType user = getUserTypeMock();
		ContactInformationFactory cif = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION));
		ContactInformationType cit = cif.newContactInformation(user);

		boolean add_cit = false;
		boolean error = false;
		try {
			add_cit = cif.add(cit);
		} catch (FactoryException | ArgumentException e) {
			
			logger.error("Error",e);
			error = true;
			logger.error(e.getMessage());
		}
		assertFalse("Error occurred", error);
		assertTrue("Unable to add contact information", add_cit);
	}
	
	@Test
	public void testGetContactInformation(){
		logger.info("Get w/ Ref Id: " + testRefId);
		UserType user = getUserTypeMock();
		ContactInformationFactory cif = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION));
		ContactInformationType cit = null;
		boolean error = false;
		try {
			cit = cif.getContactInformationForUser(user);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
			error = true;
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertFalse("Error occurred", error);
		assertNotNull("Unable to get contact information", cit);
		assertTrue("Contact information was not valid", cit.getReferenceId().intValue() == testRefId);
	}
	
	@Test
	public void testUpdateContactInformation(){
		logger.info("Update w/ Ref Id: " + testRefId);
		UserType user = getUserTypeMock();
		ContactInformationFactory cif = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION));
		ContactInformationType cit = null;
		boolean error = false;
		boolean updated = false;
		try {
			cit = cif.getContactInformationForUser(user);
			assertNotNull("Unable to get contact information", cit);
			//cit.setEmail("wranlon@hotmail.com");
			cit.setDescription("Updated description");
			updated = cif.update(cit);
			
		} catch (FactoryException e) {
			
			logger.error("Error",e);
			error = true;
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
			error = true;
		}
		assertFalse("Error occurred", error);
		assertTrue("Unable to update contact information", updated);
	}
	
	@Test
	public void testDeleteContactInformation(){
		logger.info("Delete w/ Ref Id: " + testRefId);
		UserType user = getUserTypeMock();
		ContactInformationFactory cif = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION));
		ContactInformationType cit = null;
		boolean error = false;
		boolean deleted = false;
		try {
			cit = cif.getContactInformationForUser(user);
			assertNotNull("Unable to get contact information", cit);
			deleted = cif.delete(cit);
			
		} catch (FactoryException e) {
			
			logger.error("Error",e);
			error = true;
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertFalse("Error occurred", error);
		assertTrue("Unable to delete contact information", deleted);
	}
}