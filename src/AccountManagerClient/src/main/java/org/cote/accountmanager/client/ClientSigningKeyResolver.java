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
package org.cote.accountmanager.client;

import java.security.Key;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.client.util.CacheUtil;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.SecurityType;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.SigningKeyResolver;

public class ClientSigningKeyResolver implements SigningKeyResolver{
	public static final Logger logger = LogManager.getLogger(ClientSigningKeyResolver.class);
	private static ClientContext resolverContext = null;
	public static void setResolverContext(ClientContext context) {
		resolverContext = context;
	}
	@SuppressWarnings("rawtypes")
	@Override
	public Key resolveSigningKey(JwsHeader arg0, Claims arg1) {
		String urn = arg1.getId();
		Key key = null;
		if(resolverContext == null) {
			logger.error("Resolver context is null");
			return null;
		}
		if(urn != null){
			logger.info("Resolving: '" + urn + "'");
			String keyName = "ResolverKey-" + urn;
			SecurityType secType = CacheUtil.readCache(resolverContext, keyName, SecurityType.class);
			if(secType == null) {
				secType = AM6Util.getJwtTokenKey(resolverContext, SecurityType.class, urn);
				if(secType != null) {
					CacheUtil.cache(resolverContext, keyName, secType);
				}
					
			}
			if(secType == null) {
				logger.error("Security key for " + urn + " is null or could not be retrieved");
				return null;
			}
			logger.info("Working with key for " + urn);
			SecurityBean bean = new SecurityBean();
			SecurityFactory.getSecurityFactory().setSecretKey(bean, secType.getCipherKey(), secType.getCipherIV(), false);
			key = bean.getSecretKey();
		}
		return key;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Key resolveSigningKey(JwsHeader arg0, String arg1) {
		return null;
	}
	
}
