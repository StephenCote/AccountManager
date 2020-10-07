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
package org.cote.accountmanager.data.security;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.SecurityTokenFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuthorizationType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.SpoolBucketEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.JSONUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class TokenService {
	public static final Logger logger = LogManager.getLogger(TokenService.class);
	private static final Pattern personaType = Pattern.compile("^(USER|PERSON|ACCOUNT)$");
	
	public static final String CLAIM_TOKEN_ID = "tokenId";
	public static final String CLAIM_OBJECT_ID = "objectId";
	public static final String CLAIM_ORGANIZATION_PATH = "organizationPath";
	public static final String CLAIM_SCOPES = "scopes";
	public static final String CLAIM_SUBJECT_TYPE = "subjectType";
	
	public static Jws<Claims> extractJWTClaims(String token){
		return Jwts.parser().setSigningKeyResolver(new AM5SigningKeyResolver()).parseClaimsJws(token);
	}
	public static Claims validateJWTToken(String token) throws FactoryException, ArgumentException{
		return validateJWTToken(token, false, false);
	}
	public static Claims validateJWTToken(String token, boolean skipExpirationCheck, boolean skipSpoolCheck) throws FactoryException, ArgumentException{
		Claims c = extractJWTClaims(token).getBody();
		Date now = Calendar.getInstance().getTime();

		if(!skipExpirationCheck && c.getExpiration().getTime() < now.getTime()) {
			logger.error("Token for " + c.getSubject() + " has expired");
			return null;
		}
		if(!skipSpoolCheck) {
			String tokenId = c.get(CLAIM_TOKEN_ID, String.class);
			String organizationPath = c.get(CLAIM_ORGANIZATION_PATH, String.class);
			OrganizationFactory oF = (OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION);
			
			if(tokenId != null && organizationPath != null) {
				OrganizationType org = oF.find(organizationPath);
				if(org != null) {
					SecuritySpoolType sst = getGlobalJWTSecurityToken(tokenId, org.getId());
					if(sst != null &&
							(
								!sst.getExpires()
								||
								(sst.getExpires() == true && sst.getExpiration().toGregorianCalendar().getTimeInMillis() > now.getTime())
							)
					) {
						logger.info("Token not expired");
					}
					else {
						if(sst == null) logger.warn("Persisted token does not exist");
						else logger.warn("Persisted token type has expired: " + sst.getExpiration().toGregorianCalendar() + " is less than now " + now);
						c = null;
					}
				}
				else {
					logger.warn("Invalid organization path: " + organizationPath);
					c = null;
				}
			}
			else {
				logger.warn("Rejecting token without a specified tokenId or organization path");
				c = null;
			}
		}
		
		return c;
	}

	public static SecuritySpoolType newJWTToken(UserType contextUser, NameIdType persona) throws UnsupportedEncodingException, FactoryException, ArgumentException{
		return newJWTToken(contextUser, persona, SecurityTokenFactory.TOKEN_EXPIRY_10_MINUTES);
	}
	public static SecuritySpoolType newJWTToken(UserType contextUser, NameIdType persona, int expiryMinutes) throws UnsupportedEncodingException, FactoryException, ArgumentException{
		String tokenId = UUID.randomUUID().toString();
		String token = createJWTToken(contextUser, persona, tokenId, expiryMinutes);
		if(token == null) {
			logger.error("Failed to create JWT token");
			return null;
		}
		return newToken(contextUser, SpoolNameEnumType.AUTHORIZATION, tokenId + " " + TokenUtil.DEFAULT_REFERENCE_SUFFIX, token.getBytes("UTF-8"), (expiryMinutes * 60));
	}
	
	/// This is still (hopefully obvious) very loose per the spec and likely very wrong
	/// however, it's being used primarily for node-to-node communication versus trying to provide third party access
	///
	public static String createJWTToken(UserType contextUser, NameIdType persona){
		return createJWTToken(contextUser, persona, UUID.randomUUID().toString(), SecurityTokenFactory.TOKEN_EXPIRY_10_MINUTES);
	}
	public static String createJWTToken(UserType contextUser, NameIdType persona, String tokenId, int expiryMinutes){
		if(!personaType.matcher(persona.getNameType().toString()).find()){
			logger.error("Unsupported persona type: {0}", persona.getNameType());
			return null;
		}
		SecurityBean bean = TokenUtil.getJWTSecurityBean(persona);
		if(bean == null){
			logger.error("Null security bean");
			return null;
		}
		if(bean.getSecretKey() == null){
			logger.error("Null secret key");
			return null;
		}
		
		List<EntitlementType> ents = BaseService.aggregateEntitlementsForMember(contextUser, persona);
		List<String> buff = new ArrayList<>();
		for(EntitlementType ent : ents) {
			buff.add(ent.getEntitlementName());
		}
	    Claims claims = Jwts.claims().setSubject(persona.getName());
	    claims.put(CLAIM_SCOPES, Arrays.asList(buff));
		claims.put(CLAIM_OBJECT_ID, persona.getObjectId());
		claims.put(CLAIM_TOKEN_ID, tokenId);
		claims.put(CLAIM_ORGANIZATION_PATH, persona.getOrganizationPath());
		claims.put(CLAIM_SUBJECT_TYPE,persona.getNameType());
		Calendar cal = Calendar.getInstance();
		Date now = cal.getTime();
		cal.add(Calendar.MINUTE, expiryMinutes);
		Date expires = cal.getTime();
		return Jwts.builder()
		  .setClaims(claims)
		  .setIssuer(contextUser.getUrn())
		  .setIssuedAt(now)
		  .setExpiration(expires)
		  .setSubject(persona.getName())
		  .setId(persona.getUrn())
		  .compressWith(CompressionCodecs.GZIP)
		  .signWith(SignatureAlgorithm.HS512, bean.getSecretKey())
		  .compact();
	}
	public static SecuritySpoolType getGlobalJWTSecurityToken(String name, long organizationId) throws FactoryException, ArgumentException {
		List<SecuritySpoolType> tokens = Factories.getSecurityTokenFactory().getSecurityTokenByNameInGroup(SpoolBucketEnumType.SECURITY_TOKEN, name  + " " + TokenUtil.DEFAULT_REFERENCE_SUFFIX, 0L, organizationId);
		return (!tokens.isEmpty() ? tokens.get(0) : null);
	}
	/*
	 * The "Security Token" is a spool entry under the SECURITY_TOKEN message bucket
	 */
	public static SecuritySpoolType newSecurityToken(UserType owner){
		return newSecurityToken(owner, new byte[0], SecurityTokenFactory.TOKEN_EXPIRY_10_MINUTES);
	}
	
	public static SecuritySpoolType newSecurityToken(UserType owner, byte[] data, int expirySeconds){
		SecuritySpoolType tokenType = null;
		
		try{
			tokenType = newToken(owner, SpoolNameEnumType.GENERAL,"Security Token", data,  expirySeconds);
		}
		catch(FactoryException | ArgumentException e){
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			logger.error("Failed to persist tokens");
			tokenType = null;
		}
		else{
			logger.info("Created new token with guid: " + tokenType.getGuid());
		}
		return tokenType;
	}
	
	
	public static SecuritySpoolType newAuthorizationToken(UserType owner, NameIdType object, int expirySeconds){
		
		AuthorizationType authZ = new AuthorizationType();
		
		SecuritySpoolType tokenType = null;
		try{
			byte[] authZExp = JSONUtil.exportObject(authZ).getBytes("UTF-8");
			tokenType = newToken(owner, SpoolNameEnumType.AUTHORIZATION, object.getUrn() + " Authorization Claim",authZExp, expirySeconds);
		}
		catch(FactoryException | UnsupportedEncodingException | ArgumentException e){
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return tokenType;
	}
}
