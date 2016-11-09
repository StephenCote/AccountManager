package org.cote.accountmanager.data;

import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.services.AuditDataMaintenance;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ResponseEnumType;
import org.cote.accountmanager.objects.types.RetentionEnumType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestAuditFactory{
	public static final Logger logger = LogManager.getLogger(TestAuditFactory.class);
	public AuditDataMaintenance auditThread = null;
	@Before
	public void setUp() throws Exception {

		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.SINGLE);
		cf.setDriverClassName("org.postgresql.Driver");
		cf.setUserName("devuser");
		cf.setUserPassword("password");
		cf.setUrl("jdbc:postgresql://127.0.0.1:5432/devdb");
		logger.info("Setup");
		auditThread = new AuditDataMaintenance();
		auditThread.setThreadDelay(500);
		//auditThread.run();
	}

	@After
	public void tearDown() throws Exception {
		auditThread.requestStop();
	}
	
	@Test
	public void testAddDenyWithNullData(){

		String id = UUID.randomUUID().toString();
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, null, AuditEnumType.GROUP, id);
		logger.info("Audit expires: " + audit.getAuditExpiresDate());
		AuditService.targetAudit(audit, AuditEnumType.USER, null);
		AuditService.denyResult(audit, "Denied");
		Factories.getAuditFactory().flushSpool();
		AuditType[] audits = new AuditType[0];
		try {
			audits = Factories.getAuditFactory().getAuditBySource(AuditEnumType.GROUP, id);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		assertTrue("Failed to lookup audit", audits.length > 0);
	}
	
	@Test
	public void testAddAuditNoData(){
		AuditType audit = Factories.getAuditFactory().newAudit();
		audit.setAuditActionType(ActionEnumType.ADD);
		audit.setAuditResultType(ResponseEnumType.PENDING);
		audit.setAuditRetentionType(RetentionEnumType.VOLATILE);
		audit.setAuditSourceType(AuditEnumType.INFO);
		audit.setAuditTargetType(AuditEnumType.INFO);
		audit.setAuditSourceData("123");
		int remainingRows = 0;
		boolean success = false;
		try {
			success = Factories.getAuditFactory().addAudit(audit);
			Factories.getAuditFactory().flushSpool();
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		remainingRows = Factories.getAuditFactory().getDataTable("audit").getRows().size();
		assertTrue("Audit was not added",success);
		assertTrue("Audit data was not flushed.  Remaining rows are " + remainingRows, remainingRows == 0);
	}
	
	@Test
	public void testGetAuditBySource(){
		AuditType[] audits = new AuditType[0];
		try {
			audits = Factories.getAuditFactory().getAuditBySource(AuditEnumType.INFO, "123");
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		logger.info("Retrieved " + audits.length + " audits");
		assertTrue("Expected at least one audit type", audits.length > 0);
	}
	

}