package org.cote.accountmanager.data.services;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.LevelEnumType;
import org.cote.accountmanager.objects.types.ResponseEnumType;
import org.cote.accountmanager.objects.types.RetentionEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class AuditService {
	public static final Logger logger = Logger.getLogger(AuditService.class.getName());
	public static String getAuditString(AuditType audit){
		return audit.getAuditResultType() + " (" + audit.getAuditResultData() + ") "
				+ audit.getAuditSourceType() + " (" + audit.getAuditSourceData() + ") "
				+ audit.getAuditActionType() + " (" + audit.getAuditActionSource() + ") "
				+ audit.getAuditTargetType() + " (" + audit.getAuditTargetData() + ") "
				+ " in " + ((audit.getAuditResultDate().toGregorianCalendar().getTimeInMillis() - audit.getAuditDate().toGregorianCalendar().getTimeInMillis())) + " ms"
			;
		//return audit.getAuditActionType() + " (" + audit.getAuditActionSource() + ") "
		//+ audit.getAuditSourceType() + " (" + audit.getAuditSourceData() + ") "
		// + audit.getAuditTargetType() + " (" + audit.getAuditTargetData() + ") "
		// + audit.getAuditResultType() + " (" + audit.getAuditResultData() + ")";
		// *** Audit *** READ (~/Lifecycles) USER (devuser2) LIFECYCLE (~/Lifecycles) PERMIT (Access authorized to group Lifecycles)
	}
	private static boolean add(AuditType audit){
		boolean added = false;
		try {
			
			Calendar now = Calendar.getInstance();
			audit.setAuditResultDate(CalendarUtil.getXmlGregorianCalendar(now.getTime()));
			added = Factories.getAuditFactory().addAudit(audit);
			String auditStr = getAuditString(audit);
			logger.info("*** Audit *** " + auditStr);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return added;
	}

	public static AuditType beginAudit(ActionEnumType actionType, String actionSrc, AuditEnumType srcType, String srcData){
	
		AuditType audit = Factories.getAuditFactory().newAudit();
		audit.setAuditActionSource(actionSrc);
		audit.setAuditActionType(actionType);
		audit.setAuditRetentionType(RetentionEnumType.OPERATIONAL);
		audit.setAuditLevelType(LevelEnumType.INFO);
		sourceAudit(audit,srcType, srcData);
		return audit;
	}
	public static void sourceAudit(AuditType audit, AuditEnumType srcType, String srcData){
		audit.setAuditSourceType(srcType);
		audit.setAuditSourceData(srcData);		
	}
	public static void targetAudit(AuditType audit, AuditEnumType targetType, String targetData){
		audit.setAuditTargetType(targetType);
		audit.setAuditTargetData(targetData);
	}
	public static void pendResult(AuditType audit, String msg){
		audit.setAuditResultType(ResponseEnumType.PENDING);
		audit.setAuditResultData(msg);
		add(audit);
	}
	
	public static void permitResult(AuditType audit, String msg){
			audit.setAuditResultType(ResponseEnumType.PERMIT);
			audit.setAuditResultData(msg);
			add(audit);
		}
		
	public static void denyResult(AuditType audit, String msg){
		audit.setAuditResultType(ResponseEnumType.DENY);
		audit.setAuditLevelType(LevelEnumType.WARNING);
		audit.setAuditResultData(msg);
		add(audit);
	}
}
