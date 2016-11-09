package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
public class TestFindOrganization{
	public static final Logger logger = LogManager.getLogger(TestFindOrganization.class);

	
	@Before
	public void setUp() throws Exception {

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
			org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(path);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		
		assertNotNull("Org is null",org);
		assertTrue("Unexpected org: '" + org.getName() + "'", org.getName().equalsIgnoreCase("global"));
	}
	
	@Test
	public void testFindPublic(){
		
		String path = "/Public";
		OrganizationType org = null;
		
		try {
			org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(path);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		
		assertNotNull("Org is null",org);
		assertTrue("Unexpected org: '" + org.getName() + "'", org.getName().equalsIgnoreCase("public"));
	}
}