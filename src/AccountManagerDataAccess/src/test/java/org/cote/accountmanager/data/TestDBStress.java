package org.cote.accountmanager.data;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ResponseEnumType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class TestDBStress{
	public static final Logger logger = LogManager.getLogger(TestDBStress.class);
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
	public void testConnectionRoundRobin(){
		Connection connection = null;
		for(int i = 0; i < 50; i++){
			connection = ConnectionFactory.getInstance().getConnection();
			try {
				connection.close();
			} catch (SQLException e) {
				
				logger.error("Error",e);
			}
		}
	}
	
	@Test
	public void testFactoryFlood(){
		for(int i = 0; i < 50; i++){
			AuditType audit = Factories.getAuditFactory().newAudit();
			audit.setAuditActionType(ActionEnumType.ADD);
			audit.setAuditSourceType(AuditEnumType.DATA);
			audit.setAuditTargetType(AuditEnumType.DATA);
			audit.setAuditResultType(ResponseEnumType.INFO);
			try {
				Factories.getAuditFactory().addAudit(audit);
			} catch (FactoryException e) {
				
				logger.error("Error",e);
			}
			Factories.getAuditFactory().flushSpool();
		}
	}
}
