package org.cote.accountmanager.data.security;

import org.apache.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public class TokenService {
	public static final Logger logger = Logger.getLogger(TokenService.class.getName());
	public static SecuritySpoolType newSecurityToken(UserType owner){
		SecuritySpoolType tokenType = null;
		try{
			SecurityBean bean = new SecurityBean();
			SecurityFactory.getSecurityFactory().generateSecretKey(bean);
			tokenType = Factories.getSecurityTokenFactory().newSecurityToken("Security Token", owner.getOrganizationId());
			tokenType.setOwnerId(owner.getId());
			//tokenType.setData(SecurityFactory.getSecurityFactory().serializeCipher(bean));
			//AuditService.targetAudit(audit, AuditEnumType.SECURITY_TOKEN, tokenType.getGuid());
			if(Factories.getSecurityTokenFactory().addSecurityToken(tokenType) == false){
				//AuditService.denyResult(audit, "Failed to persist token");
				logger.error("Failed to persist tokens");
				tokenType = null;
			}
			else{
				//AuditService.permitResult(audit, "Created token");
				logger.info("Created new token with guid: " + tokenType.getGuid());
			}

		}
		catch(FactoryException | ArgumentException e){
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return tokenType;
	}
}
