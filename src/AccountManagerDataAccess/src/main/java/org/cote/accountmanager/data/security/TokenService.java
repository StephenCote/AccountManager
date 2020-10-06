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
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.SecurityTokenFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuthorizationType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.UserType;
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

	public static Jws<Claims> extractJWTClaims(String token){
		return Jwts.parser().setSigningKeyResolver(new AM5SigningKeyResolver()).parseClaimsJws(token);
	}
	public static Claims validateJWTToken(String token){
		return extractJWTClaims(token).getBody();
	}
	/// This is still (hopefully obvious) very loose per the spec and likely very wrong
	/// however, it's being used primarily for node-to-node communication versus trying to provide third party access
	///
	public static String getJWTToken(UserType contextUser, NameIdType persona){
		return getJWTToken(contextUser, persona, SecurityTokenFactory.TOKEN_EXPIRY_10_MINUTES);
	}
	public static String getJWTToken(UserType contextUser, NameIdType persona, int expiryMinutes){
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
	    claims.put("scopes", Arrays.asList(buff));
		claims.put("objectId", persona.getObjectId());
		claims.put("organizationPath", persona.getOrganizationPath());
		claims.put("subjectType",persona.getNameType());
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
