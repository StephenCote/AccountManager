package org.cote.accountmanager.data.security;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.SecurityTokenFactory;
import org.cote.accountmanager.objects.AuthorizationType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.JSONUtil;

public class TokenService {
	public static final Logger logger = Logger.getLogger(TokenService.class.getName());
	
	
	/*
	 * The "Security Token" is a spool entry under the SECURITY_TOKEN message bucket
	 * It's an 
	 */
	public static SecuritySpoolType newSecurityToken(UserType owner){
		return newSecurityToken(owner, new byte[0], SecurityTokenFactory.TOKEN_EXPIRY_10_MINUTES);
	}
	
	public static SecuritySpoolType newSecurityToken(UserType owner, byte[] data, int expirySeconds){
		SecuritySpoolType tokenType = null;
		
		try{
			//SecurityBean bean = new SecurityBean();
			//SecurityFactory.getSecurityFactory().generateSecretKey(bean);
			tokenType = newToken(owner, SpoolNameEnumType.GENERAL,"Security Token", data,  expirySeconds);
			//tokenType.setData(SecurityFactory.getSecurityFactory().serializeCipher(bean));
		}
		catch(FactoryException | ArgumentException e){
			logger.error(e.getMessage());
			logger.error(e.getStackTrace());
		}
		return tokenType;
	}
	
	private static SecuritySpoolType newToken(UserType owner, SpoolNameEnumType bucketNameType, String name, byte[] data, int expirySeconds) throws FactoryException, ArgumentException{
		SecuritySpoolType tokenType = Factories.getSecurityTokenFactory().newSecurityToken(name, owner.getOrganizationId());
		tokenType.setOwnerId(owner.getId());
		tokenType.setSpoolBucketName(bucketNameType);
		if(expirySeconds > 0){
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.SECOND, expirySeconds);
			tokenType.setExpiration(CalendarUtil.getXmlGregorianCalendar(cal.getTime()));
			tokenType.setExpires(true);
		}
		tokenType.setData(data);
		if(Factories.getSecurityTokenFactory().addSecurityToken(tokenType) == false){
			//AuditService.denyResult(audit, "Failed to persist token");
			logger.error("Failed to persist tokens");
			tokenType = null;
		}
		else{
			//AuditService.permitResult(audit, "Created token");
			logger.info("Created new token with guid: " + tokenType.getGuid());
		}
		return tokenType;
	}
	
	
	public static SecuritySpoolType newAuthorizationToken(UserType owner, NameIdType object, int expirySeconds){
		
		AuthorizationType authZ = new AuthorizationType();
		
		SecuritySpoolType tokenType = null;
		try{
			byte[] authZExp = JSONUtil.exportObject(authZ).getBytes("UTF-8");
			newToken(owner, SpoolNameEnumType.AUTHORIZATION, object.getUrn() + " Authorization Claim",authZExp, expirySeconds);
		}
		catch(FactoryException | UnsupportedEncodingException | ArgumentException e){
			logger.error(e.getMessage());
			logger.error(e.getStackTrace());
		}
		return tokenType;
	}
	/*
	public static String newMaterializedToken(UserType owner, NameIdType object){
		StringBuffer buff = new StringBuffer();
		buff.append(owner.getUrn());
		buff.append("-");
		buff.append(object.getUrn());
		return buff.toString();
	}
	*/
}
