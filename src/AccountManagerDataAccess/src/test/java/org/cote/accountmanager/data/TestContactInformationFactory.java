package org.cote.accountmanager.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.junit.Test;

public class TestContactInformationFactory extends BaseDataAccessTest{
	//public static final Logger logger = Logger.getLogger(TestContactInformationFactory.class.getName());
	private static long testRefId = 0;
	
	
	
	private UserType getUserTypeMock(){
		OrganizationFactory of = Factories.getOrganizationFactory();
		
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
			Factories.getUserFactory().populate(testUser);
			DirectoryGroupType dir = Factories.getGroupFactory().getCreateDirectory(testUser, "Accounts", testUser.getHomeDirectory(), testUser.getOrganizationId());
			DirectoryGroupType adir = Factories.getGroupFactory().getCreateDirectory(testUser, "Addresses", testUser.getHomeDirectory(), testUser.getOrganizationId());
			acct = Factories.getAccountFactory().getAccountByName(testAcctName, dir);
			if(acct != null){
				Factories.getAccountFactory().deleteAccount(acct);
				acct = null;
			}
			if(acct == null){
				acct = Factories.getAccountFactory().newAccount(testUser, testAcctName, AccountEnumType.NORMAL,AccountStatusEnumType.NORMAL,dir.getId());
				if(Factories.getAccountFactory().addAccount(acct,true)){
					logger.info("Clearing address factory cache to break the foreign key link");
					Factories.getAccountFactory().clearCache();
					acct = Factories.getAccountFactory().getAccountByName(testAcctName, dir);
				}
				else{
					acct = null;
				}
			}
			assertNotNull("Account is null",acct);
			logger.info("Testing account #" + acct.getId() + " in group #" + dir.getId());
			assertNull("ContactInformation is not null",acct.getContactInformation());
			Factories.getAccountFactory().populate(acct);
			assertNotNull("ContactInformation is null",acct.getContactInformation());
			
			logger.info("Zero out cinfo references");
			acct.getContactInformation().getAddresses().clear();
			acct.getContactInformation().getContacts().clear();
			Factories.getAccountFactory().updateAccount(acct);
			//Factories.clearCaches();
			
			AddressType addr = Factories.getAddressFactory().getByNameInGroup("Test Address 1", adir);
			if(addr == null){
				addr = Factories.getAddressFactory().newAddress(testUser, adir.getId());
				addr.setName("Test Address 1");
				if(Factories.getAddressFactory().addAddress(addr)){
					//Factories.clearCaches();
					addr = Factories.getAddressFactory().getByNameInGroup("Test Address 1", adir);
				}
				else{
					addr = null;
				}
			}
			assertNotNull("Address is null", addr);
			
			acct.getContactInformation().getAddresses().add(addr);
			Factories.getAccountFactory().updateAccount(acct);
			long acctId = acct.getId();
			//Factories.clearCaches();
			acct = Factories.getAccountFactory().getAccountByName(testAcctName, dir);
			logger.info("Have Cache Id: " + Factories.getAccountFactory().haveCacheId(acctId));
			assertNotNull("Account is null",acct);
			Factories.getAccountFactory().populate(acct);
			assertTrue("Missing contact address",acct.getContactInformation().getAddresses().size() > 0);
			
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testAddContactInformation(){
		Random r = new Random();
		testRefId = r.nextInt();
		if(testRefId < 0) testRefId *= -1;
		
		logger.info("Add w/ Ref Id: " + testRefId);
		UserType user = getUserTypeMock();
		ContactInformationFactory cif = Factories.getContactInformationFactory();
		ContactInformationType cit = cif.newContactInformation(user);

		boolean add_cit = false;
		boolean error = false;
		try {
			add_cit = cif.addContactInformation(cit);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		ContactInformationFactory cif = Factories.getContactInformationFactory();
		ContactInformationType cit = null;
		boolean error = false;
		try {
			cit = cif.getContactInformationForUser(user);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse("Error occurred", error);
		assertNotNull("Unable to get contact information", cit);
		assertTrue("Contact information was not valid", cit.getReferenceId().intValue() == testRefId);
	}
	
	@Test
	public void testUpdateContactInformation(){
		logger.info("Update w/ Ref Id: " + testRefId);
		UserType user = getUserTypeMock();
		ContactInformationFactory cif = Factories.getContactInformationFactory();
		ContactInformationType cit = null;
		boolean error = false;
		boolean updated = false;
		try {
			cit = cif.getContactInformationForUser(user);
			assertNotNull("Unable to get contact information", cit);
			//cit.setEmail("wranlon@hotmail.com");
			cit.setDescription("Updated description");
			updated = cif.updateContactInformation(cit);
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
		}
		assertFalse("Error occurred", error);
		assertTrue("Unable to update contact information", updated);
	}
	
	@Test
	public void testDeleteContactInformation(){
		logger.info("Delete w/ Ref Id: " + testRefId);
		UserType user = getUserTypeMock();
		ContactInformationFactory cif = Factories.getContactInformationFactory();
		ContactInformationType cit = null;
		boolean error = false;
		boolean deleted = false;
		try {
			cit = cif.getContactInformationForUser(user);
			assertNotNull("Unable to get contact information", cit);
			deleted = cif.deleteContactInformation(cit);
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			error = true;
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse("Error occurred", error);
		assertTrue("Unable to delete contact information", deleted);
	}
}