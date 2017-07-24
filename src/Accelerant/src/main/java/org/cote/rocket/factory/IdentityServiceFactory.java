/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.rocket.factory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.rocket.Rocket;
import org.cote.rocket.services.IdentityService;
import org.cote.rocket.services.IdentityServiceDAL;

public class IdentityServiceFactory {
	private static IdentityService identityService = null;
	private static IdentityServiceDAL sd = null;
	 public static final Logger logger = LogManager.getLogger(IdentityServiceFactory.class);

	private static String adminCredential = null;
	public static void setAdminCredential(String cred){
		adminCredential = cred;
	}
	public static IdentityService getIdentityService(OrganizationType org){
		if(identityService == null){
			if(Rocket.isApplicationEnvironmentConfigured(org.getId()) == false){
				logger.info("Configuring organization for Rocket");
				try {
					Rocket.configureApplicationEnvironment(org.getId(), adminCredential);
				} catch (FactoryException e) {
					
					logger.error(e.getMessage());
					logger.error("Error",e);
				} catch (DataAccessException e) {
					
					logger.error(e.getMessage());
					logger.error("Error",e);
				} catch (ArgumentException e) {
					
					logger.error(e.getMessage());
					logger.error("Error",e);
				}
			}
			if(Rocket.isApplicationEnvironmentConfigured(org.getId()) == false){
				logger.error("Organization " + org.getName() + " could not be configured for Rocket.");
				return null;
			}
				
			identityService = new IdentityService(org);
		}
		return identityService;
	}
	public static IdentityServiceDAL getIdentityServiceDAL(OrganizationType org){
		if(sd == null){
			sd = new IdentityServiceDAL(getIdentityService(org));
		}
		return sd;
	}

}
