package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.Before;
import org.junit.Test;

/// Intentionally does not inherit from base test to avoid auto starting anything
///
public class TestFactoryInterfaces {
	public static final Logger logger = LogManager.getLogger(TestFactoryInterfaces.class);
	@Before
	public void setUp() throws Exception {

		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.SINGLE);
		cf.setDriverClassName("org.postgresql.Driver");
		cf.setUserName("devuser");
		cf.setUserPassword("password");
		cf.setUrl("jdbc:postgresql://127.0.0.1:5432/devdb");
		
	}
	
	@Test
	public void TestInterfaceAccess(){
		logger.info("Registration size: " + Factories.getFactoryClasses().size());
		logger.info("Bulk Registration size: " + BulkFactories.getFactoryClasses().size());
		
		logger.info("Testing organization factory lookup");
		OrganizationFactory orgFact = Factories.getFactory(FactoryEnumType.ORGANIZATION);
		
		
		
		logger.info("Testing discrete factory lookup");
		INameIdFactory dataFact = Factories.getFactory(FactoryEnumType.DATA);
		assertNotNull("Data Factory is null", dataFact);
		
		logger.info("Testing factory warmup");
		long start = System.currentTimeMillis();
		Factories.warmUp();
		long stop = System.currentTimeMillis();
		logger.info("Warmed up in (" + (stop - start) + "ms)");
		
	}
	
}