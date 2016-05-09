package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.objects.OrganizationType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestFindOrganization{
	public static final Logger logger = Logger.getLogger(TestFindOrganization.class.getName());

	
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
		
	}

	@After
	public void tearDown() throws Exception {


	}
	
	
	@Test
	public void testFindRoot(){
		
		String path = "/";
		OrganizationType org = null;
		
		try {
			org = Factories.getOrganizationFactory().findOrganization(path);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertNotNull("Org is null",org);
		assertTrue("Unexpected org: '" + org.getName() + "'", org.getName().equalsIgnoreCase("global"));
	}
	
	@Test
	public void testFindPublic(){
		
		String path = "/Public";
		OrganizationType org = null;
		
		try {
			org = Factories.getOrganizationFactory().findOrganization(path);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		assertNotNull("Org is null",org);
		assertTrue("Unexpected org: '" + org.getName() + "'", org.getName().equalsIgnoreCase("public"));
	}
}