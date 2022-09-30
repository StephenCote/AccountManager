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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.SecurityUtil;

import io.jsonwebtoken.CompressionCodecs;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class TokenUtil {
	public static final Logger logger = LogManager.getLogger(TokenUtil.class);
	public static final String DEFAULT_REFERENCE_SUFFIX = "jwt";
	public static SecurityBean getJWTSecurityBean(NameIdType actor){
		return getJWTSecurityBean(actor, DEFAULT_REFERENCE_SUFFIX);
	}
	public static SecurityBean getJWTSecurityBean(NameIdType actor, String referenceName){
		SecuritySpoolType tokenType = null;
		SecurityBean outBean = null;
		AuditEnumType actorType = AuditEnumType.fromValue(actor.getNameType().toString());
		if(!actorType.equals(AuditEnumType.USER) && !actorType.equals(AuditEnumType.PERSON) && !actorType.equals(AuditEnumType.ACCOUNT)) {
			logger.error("Actor type not supported: {}", actorType);
			return null;
		}

		AuditType audit = AuditService.beginAudit(ActionEnumType.REQUEST, "newSecurityToken", actorType, actor.getUrn());
		
		try{
			BaseService.populate(actorType, actor);
			long ownerId = 0L;
			DirectoryGroupType dir = null;
			if(actorType.equals(AuditEnumType.USER)) {
				dir = ((UserType)actor).getHomeDirectory();
				ownerId = actor.getId();
			}
			else{
				dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupById(((NameIdDirectoryGroupType)actor).getGroupId(),actor.getOrganizationId());
				ownerId = actor.getOwnerId();
			}
			List<SecuritySpoolType> tokens = Factories.getSecurityTokenFactory().getSecurityTokenByNameInGroup(referenceName, dir.getId(), actor.getOrganizationId());
			if(!tokens.isEmpty()){
				tokenType = tokens.get(0);
			}
			if(tokenType == null){
				SecurityBean bean = new SecurityBean();
				SecurityFactory.getSecurityFactory().generateSecretKey(bean);
				tokenType = Factories.getSecurityTokenFactory().newSecurityToken(actor.getObjectId(), actor.getOrganizationId());
				tokenType.setOwnerId(ownerId);
				tokenType.setName(referenceName);
				tokenType.setGroupId(dir.getId());
				tokenType.setData(SecurityUtil.serializeToXml(bean, false, false, true).getBytes());
				AuditService.targetAudit(audit, AuditEnumType.SECURITY_TOKEN, tokenType.getObjectId());
				if(!Factories.getSecurityTokenFactory().addSecurityToken(tokenType)){
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
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return outBean;
	}
	
	public static String validateJWTToken(String token){
		logger.info("Validating token: '" + token + "'");
		return Jwts.parser().setSigningKeyResolver(new AM5SigningKeyResolver()).parseClaimsJws(token).getBody().getSubject();
	}
	
	
	
	public static String getJWTToken(UserType user){
		
		SecurityBean bean = TokenUtil.getJWTSecurityBean(user);
		if(bean == null){
			logger.error("Null security bean");
			return null;
		}
		if(bean.getSecretKey() == null){
			logger.error("Null secret key");
			logger.error(JSONUtil.exportObject(bean));
			return null;
		}
		
		Map<String,Object> claims = new HashMap<>();
		claims.put("objectId", user.getObjectId());
		claims.put("organizationPath", user.getOrganizationPath());
		return Jwts.builder()
		  .setClaims(claims)
		  .setSubject(user.getName())
		  .setId(user.getUrn())
		  .compressWith(CompressionCodecs.GZIP)
		  .signWith(SignatureAlgorithm.HS512, bean.getSecretKey())
		  .compact();
	}
	
}
