/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.rocket.console;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.propellant.objects.IdentityDataImportType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.rocket.Rocket;
import org.cote.rocket.factory.IdentityServiceFactory;
import org.cote.rocket.services.IdentityService;
import org.cote.accountmanager.data.factory.*;
import org.cote.rocket.factory.*;
public class ImportAction {
	public static final Logger logger = LogManager.getLogger(ImportAction.class);
	private static String defaultLifecycleAdminName = "Admin";
	
	      

	public static void importData(UserType user, String lifecycleName, String projectName, String applicationName, IdentityDataImportType[] imports){
		
		IdentityService identityService = null;
		try {
			identityService = IdentityServiceFactory.getIdentityService(((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationById(user.getOrganizationId()));
			IdentityService.setLifecycleAdmin(defaultLifecycleAdminName);
			identityService.initialize();
			/*
			int maxRecordCount = 25000;
			int bufferSize = 0;
			*/

			LifecycleType lc = Rocket.getLifecycle(lifecycleName, user.getOrganizationId());
			if(lc == null){
				logger.error("Lifecycle '" + lifecycleName + "' doesn't exists");
				return;
			}
			
			ProjectType pj = Rocket.getProject(projectName, lc, lc.getOrganizationId());
			if(pj == null){
				logger.error("Project '" + projectName + "' doesn't exist in lifecycle '" + lifecycleName + "'");
				return;
			}
			DirectoryGroupType oApp = identityService.getApplication(pj, applicationName);
			if(oApp == null){
				logger.error("Application '" + applicationName + "' doesn't exist in project '" + pj.getName() + "'");
				return;
			}

			identityService.importApplicationData(user, lc, pj, oApp, imports);
			
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
			logger.error("Error",e);
		} catch (ArgumentException e) {
			logger.error(e.getMessage());
			logger.error("Error",e);
		} 
	}
}

