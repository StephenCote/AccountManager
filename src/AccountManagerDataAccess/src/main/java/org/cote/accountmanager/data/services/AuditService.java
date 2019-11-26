/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.data.services;

import java.util.Calendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.LevelEnumType;
import org.cote.accountmanager.objects.types.ResponseEnumType;
import org.cote.accountmanager.objects.types.RetentionEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class AuditService {
	public static final Logger logger = LogManager.getLogger(AuditService.class);
	public static final int maximumAuditSpoolSize = 5000;
	public static String getAuditString(AuditType audit){
		return audit.getAuditResultType() + " (" + audit.getAuditResultData() + ") "
				+ audit.getAuditSourceType() + " (" + audit.getAuditSourceData() + ") "
				+ audit.getAuditActionType() + " (" + audit.getAuditActionSource() + ") "
				+ audit.getAuditTargetType() + " (" + audit.getAuditTargetData() + ") "
				+ " in " + ((audit.getAuditResultDate().toGregorianCalendar().getTimeInMillis() - audit.getAuditDate().toGregorianCalendar().getTimeInMillis())) + " ms"
			;
	}
	private static boolean add(AuditType audit){
		boolean added = false;
		try {
			
			Calendar now = Calendar.getInstance();
			audit.setAuditResultDate(CalendarUtil.getXmlGregorianCalendar(now.getTime()));
			added = Factories.getAuditFactory().addAudit(audit);
			
			if(Factories.getAuditFactory().getDataTables().get(0).getRows().size() >= maximumAuditSpoolSize){
				logger.warn("Force flush audit spool - this should be handled by the maintenance thread before reaching the " + maximumAuditSpoolSize + " limit");
				Factories.getAuditFactory().flushSpool();
			}
			String auditStr = getAuditString(audit);
			logger.debug("*** Audit *** " + auditStr);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
	public static void validateResult(AuditType audit, String msg){
		audit.setAuditResultType(ResponseEnumType.VALID);
		audit.setAuditResultData(msg);
		add(audit);
	}
	
	public static void invalidateResult(AuditType audit, String msg){
		audit.setAuditResultType(ResponseEnumType.INVALID);
		audit.setAuditLevelType(LevelEnumType.WARNING);
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
