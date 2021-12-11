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

import java.security.Key;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.FactoryEnumType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolver;

public class AM5SigningKeyResolver implements SigningKeyResolver{
	public static final Logger logger = LogManager.getLogger(AM5SigningKeyResolver.class);
	@SuppressWarnings("rawtypes")
	@Override
	public Key resolveSigningKey(JwsHeader arg0, Claims arg1) {
		String urn = arg1.getId();
		FactoryEnumType fet = FactoryEnumType.USER;
		String fetStr = arg1.get("subjectType", String.class);
		if(fetStr != null && fetStr.length() > 0) fet = FactoryEnumType.fromValue(fetStr);
		Key key = null;
		if(urn != null){
			// logger.info("Resolving: '" + urn + "' as " + fet);
			NameIdType persona = null;
			try{
				INameIdFactory iFact = Factories.getFactory(fet);
				persona = iFact.getByUrn(urn);
			}
			catch(FactoryException f){
				logger.error(f);
			}
			if(persona != null){
				SecurityBean bean = TokenUtil.getJWTSecurityBean(persona);
				if(bean != null && bean.getSecretKey() != null){
					key = bean.getSecretKey();
				}
				else {
					logger.error("Invalid security key for " + urn);
				}
			}
			else {
				logger.error("Failed to retrieve persona by urn " + urn);
			}
		}
		return key;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Key resolveSigningKey(JwsHeader arg0, String arg1) {
		return null;
	}
	
}
