package org.cote.accountmanager.data;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.data.security.OrganizationSecurity;
import org.cote.accountmanager.objects.DataTableType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.SecurityUtil;

public class TestOrganizationFactory{
	private static String testOrgName = null;
	public static final Logger logger = Logger.getLogger(TestOrganizationFactory.class.getName());
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
	}
	

	@Test
	public void testAddOrganization(){
		boolean error = false;
	
		assertFalse("An error occurred", error);
		testOrgName = "Example " + System.currentTimeMillis();
		OrganizationType new_org = new OrganizationType();
		new_org.setName(testOrgName);
		new_org.setOrganizationType(OrganizationEnumType.DEVELOPMENT);
		
		logger.info("Id: " + new_org.getId());
		logger.info("Ref Id: " + new_org.getReferenceId());
		OrganizationFactory org_factory = Factories.getOrganizationFactory();
		OrganizationType devOrg = Factories.getDevelopmentOrganization();
		new_org.setParentId(devOrg.getId());
		try {
			new_org = org_factory.addOrganization(new_org);
		} catch (FactoryException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			logger.error(e2.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		logger.info("Added " + testOrgName + " as " + new_org.getId());
		assertFalse("An error occurred", error);
	}
	
	@Test
	public void testAddOrphanOrganization(){
		boolean error = false;

		String orgName = "Example " + System.currentTimeMillis();
		OrganizationType new_org = new OrganizationType();
		new_org.setName(orgName);
		new_org.setOrganizationType(OrganizationEnumType.DEVELOPMENT);

		OrganizationFactory org_factory = Factories.getOrganizationFactory();
		OrganizationType devOrg = Factories.getDevelopmentOrganization();
		OrganizationType parentOrg = null;
		

		try {
			parentOrg = org_factory.getOrganizationByName(testOrgName, devOrg);
			new_org.setParentId(parentOrg.getId());
			new_org = org_factory.addOrganization(new_org);
		} catch (FactoryException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			logger.error(e2.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		logger.info("Added " + testOrgName + " as " + new_org.getId());
		assertFalse("An error occurred", error);
	}
	
	@Test
	public void testGetOrganization(){
		boolean error = false;
		
		OrganizationType new_org = null;
		try{
			logger.info("Read clean: " + testOrgName + " in " + Factories.getDevelopmentOrganization().getId());
			new_org = Factories.getOrganizationFactory().getOrganizationByName(testOrgName, Factories.getDevelopmentOrganization().getId());
			assertNotNull("Get organization " + testOrgName + "->" + Factories.getDevelopmentOrganization().getId() + " by name was null", new_org);
			logger.info("Read from cache by id: " + new_org.getId());
			new_org = Factories.getOrganizationFactory().getOrganizationById(new_org.getId());
			assertNotNull("Get organization from cache by id was null", new_org);
			logger.info("Read from cache by name and parent");
			new_org = Factories.getOrganizationFactory().getOrganizationByName(testOrgName, Factories.getDevelopmentOrganization().getId());
			assertNotNull("Get oranization from cache by name was null null", new_org);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse("Error",error);

		
		logger.info("Id: " + new_org.getId());
		logger.info("Ref Id: " + new_org.getReferenceId());
		
	}
	
	@Test
	public void testOrganizationCipher(){
		boolean error = false;

		
		OrganizationType new_org = null;
		try{
			logger.info("Read clean");
			new_org = Factories.getOrganizationFactory().getOrganizationByName(testOrgName, Factories.getDevelopmentOrganization().getId());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse("Error",error);
		assertNotNull("Org is null", new_org);
		

		SecurityBean bean = KeyService.getPrimaryAsymmetricKey(new_org.getId()); 
				//OrganizationSecurity.getSecurityBean(new_org);
		String test_data = "This is some test data.";
		byte[] enc = SecurityUtil.encipher(bean, test_data.getBytes());

		Factories.getOrganizationFactory().clearCache();
		try{
			new_org = Factories.getOrganizationFactory().getOrganizationByName(testOrgName, Factories.getDevelopmentOrganization().getId());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bean = KeyService.getPrimaryAsymmetricKey(new_org.getId());
				//OrganizationSecurity.getSecurityBean(new_org);
		byte[] dec = SecurityUtil.decipher(bean, enc);
		logger.info("Decrypted: " + (new String(dec)));
		logger.info("Bean: " + (bean == null ? "Null":"Retrieved"));
		assertNotNull("Bean is null", bean);
	}
	@Test
	public void testUpdateOrganization(){
		boolean updated = false;
		boolean error = false;
		String newName = "Updated Example - " + System.currentTimeMillis();
		try{
			OrganizationType org = Factories.getOrganizationFactory().getOrganizationByName(testOrgName, Factories.getDevelopmentOrganization().getId());
			org.setName(newName);
			updated = Factories.getOrganizationFactory().update(org);
			if(updated){
				testOrgName = newName;
			}

		}
		catch(FactoryException fe){
			fe.printStackTrace();
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse("An error occurred", error);
		assertTrue("Organization was not updated", updated);
	}
	@Test
	public void testDeleteOrganization(){
		boolean deleted = false;
		boolean error = false;
		try{
			OrganizationType org = Factories.getOrganizationFactory().getOrganizationByName(testOrgName, Factories.getDevelopmentOrganization().getId());

			deleted = Factories.getOrganizationFactory().deleteOrganization(org);
		}
		catch(FactoryException fe){
			fe.printStackTrace();
			logger.error(fe.getMessage());
			error = true;
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertFalse("An error occurred", error);
		assertTrue("Did not delete org", deleted);
		logger.info("Deleted organizations " + testOrgName);
	}

	@After
	public void tearDown() throws Exception {
	}
}