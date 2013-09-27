package org.cote.accountmanager.data;

import java.util.Random;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestContactInformationFactory{
	public static final Logger logger = Logger.getLogger(TestContactInformationFactory.class.getName());
	private static long testRefId = 0;
	
	
	@Before
	public void setUp() throws Exception {
		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}
		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.SINGLE);
		cf.setDriverClassName("org.postgresql.Driver");
		cf.setUserName("devuser");
		cf.setUserPassword("password");
		cf.setUrl("jdbc:postgresql://127.0.0.1:5432/devdb");
		logger.info("Setup");
	}

	@After
	public void tearDown() throws Exception {
	}
	
	private UserType getUserTypeMock(){
		OrganizationFactory of = Factories.getOrganizationFactory();
		
		UserType type = new UserType();
		type.setId(testRefId);
		type.setName("example");
		type.setOrganization(Factories.getDevelopmentOrganization());
		return type;
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
		/*
		cit.setAddressLine1("6808 Denny Peak DR SE");
		cit.setCity("Snoqualmie");
		cit.setPhone("206-669-7995");
		cit.setState("Washington");
		cit.setCountry("US");
		cit.setEmail("sw.cote@gmail.com");
		cit.setAlias("Steve");
		cit.setFirstName("Stephen");
		cit.setMiddleName("William");
		cit.setLastName("Cote");
		cit.setDescription("Example contact info");
		cit.setTitle("Example");
		cit.setGender("M");
		cit.setWebsite("http://www.whitefrost.com");
		*/
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