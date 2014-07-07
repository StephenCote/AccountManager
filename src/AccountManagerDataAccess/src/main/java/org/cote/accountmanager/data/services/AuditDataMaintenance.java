package org.cote.accountmanager.data.services;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.objects.types.RetentionEnumType;
import org.cote.accountmanager.services.ThreadService;

public class AuditDataMaintenance extends ThreadService {
	public static final Logger logger = Logger.getLogger(AuditDataMaintenance.class.getName());
	private int spoolFlushDelay = 1000;
	public AuditDataMaintenance(){
		super();
		this.setThreadDelay(spoolFlushDelay);
	}
	public void execute(){
		logger.debug("AuditDataMaintenance: Flush Spool - " + Factories.getAuditFactory().getDataTable("audit").getRows().size());
		Factories.getAuditFactory().flushSpool();
		DataMaintenance.cleanupExpiredAudits(RetentionEnumType.OPERATIONAL);
		
	}
}
