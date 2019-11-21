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
package org.cote.jaas;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.exceptions.FactoryException;
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
		
		try{
			INameIdFactory iFact = Factories.getFactory(FactoryEnumType.USER);

			iFact.populate(user);
			List<SecuritySpoolType> tokens = Factories.getSecurityTokenFactory().getSecurityTokenByNameInGroup(refId, user.getHomeDirectory().getId(), user.getOrganizationId());
			if(!tokens.isEmpty()){
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
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		//return tokenType;
		return outBean;
	}
}
