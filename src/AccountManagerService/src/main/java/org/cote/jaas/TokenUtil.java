package org.cote.jaas;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.SecurityUtil;

public class TokenUtil {
	public static final Logger logger = LogManager.getLogger(TokenUtil.class);
	public static SecurityBean getJWTSecurityBean(UserType user){
		SecuritySpoolType tokenType = null;
		SecurityBean outBean = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.REQUEST, "newSecurityToken", AuditEnumType.USER, user.getUrn());
		String refId = "jwt";
		INameIdFactory iFact = Factories.getFactory(FactoryEnumType.USER);
		
		try{
			iFact.populate(user);
			List<SecuritySpoolType> tokens = Factories.getSecurityTokenFactory().getSecurityTokenByNameInGroup(refId, user.getHomeDirectory().getId(), user.getOrganizationId());
//			tokenType = Factories.getSecurityTokenFactory().
			if(tokens.size() > 0){
				tokenType = tokens.get(0);
			}
			if(tokenType == null){
				SecurityBean bean = new SecurityBean();
				SecurityFactory.getSecurityFactory().generateSecretKey(bean);
				tokenType = Factories.getSecurityTokenFactory().newSecurityToken(user.getSession().getSessionId(), user.getOrganizationId());
				tokenType.setOwnerId(user.getId());
				tokenType.setName(refId);
				tokenType.setGroupId(user.getHomeDirectory().getId());
				//tokenType.setData(SecurityFactory.getSecurityFactory().serializeCipher(bean));
				tokenType.setData(SecurityUtil.serializeToXml(bean, false, false, true).getBytes());
				AuditService.targetAudit(audit, AuditEnumType.SECURITY_TOKEN, tokenType.getGuid());
				if(Factories.getSecurityTokenFactory().addSecurityToken(tokenType) == false){
					AuditService.denyResult(audit, "Failed to persist token");
					logger.error("Failed to persist tokens");
					tokenType = null;
				}
				else{
					AuditService.permitResult(audit, "Created token");
					outBean = bean;
				}
			}
			else{
				outBean = new SecurityBean();
				SecurityFactory.getSecurityFactory().importSecurityBean(outBean, tokenType.getData(), false);
			}
		}
		catch(FactoryException | ArgumentException e){
			logger.error(e.getMessage());
			logger.error("Error",e);
		}
		//return tokenType;
		return outBean;
	}
}
