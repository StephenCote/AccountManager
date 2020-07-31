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
package org.cote.rocket.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.services.ThreadService;
import org.cote.propellant.objects.ApplicationRequestType;
import org.cote.propellant.objects.IdentityDataImportType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.IdentityServiceFactory;
import org.cote.rocket.factory.LifecycleFactory;
import org.cote.rocket.factory.ProjectFactory;
import org.cote.rocket.util.ImportTypeComparator;
public class ApplicationImportService extends ThreadService {
	private int cleanupPeriod = 15000;
	public static final Logger logger = LogManager.getLogger(ApplicationImportService.class);
	private static List<ApplicationRequestType> queue = new ArrayList<ApplicationRequestType>();
	private boolean processing = false;
	private static String lifecycleAdmin = "Admin";
	public ApplicationImportService(){
		super();
		this.setThreadDelay(cleanupPeriod);
	}
	public static void queueImport(ApplicationRequestType req){
		synchronized(queue){
			queue.add(req);
		}
	}
	public void execute(){
		if(processing){
			logger.info("Processes currently pending");
			return;
		}
		if(queue.isEmpty()){
			return;
		}
		processing = true;
		logger.info("Processing pending import activities");
		
		List<ApplicationRequestType> workingQueue = new ArrayList<ApplicationRequestType>();

		synchronized(queue){
			for(int i = 0; i < queue.size();i++){
				Collections.sort(queue.get(i).getImports(),new ImportTypeComparator());
				workingQueue.add(queue.get(i));
			}
			logger.info("Flushed queue into work queue");
			queue.clear();
		}

		try{
			for(int i = 0; i < workingQueue.size();i++){
				ApplicationRequestType art = workingQueue.get(i);
				if(art.getOrganizationId() <= 0L){
					logger.warn("Invalid organization id");
					continue;
				}
				if(art.getLifecycleId() <= 0L){
					logger.warn("Invalid lifecycle id");
					continue;
				}

				if(art.getProjectId() <= 0L){
					logger.warn("Invalid project id");
					continue;
				}
				if(art.getApplicationId() <= 0L){
					logger.warn("Invalid application id");
					continue;
				}
				if(art.getImports().isEmpty()){
					logger.warn("No imports were specified");
					continue;
				}
				try{
					OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationById(art.getOrganizationId());
					LifecycleType lc = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getById(art.getLifecycleId(), org.getId());
					ProjectType proj = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getById(art.getProjectId(), org.getId());
					DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getById(art.getApplicationId(), org.getId());
					IdentityService svc = IdentityServiceFactory.getIdentityService(org);
					svc.setLifecycleAdmin(lifecycleAdmin);
					svc.initialize();
					
					svc.importApplicationData(svc.getAdminUser(), lc, proj, dir, art.getImports().toArray(new IdentityDataImportType[0]));
				}
				catch(FactoryException | ArgumentException e) {
					logger.error(e.getMessage());
					logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				}
			}
		}
		catch(Exception e){
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		finally{
			logger.info("Completed data processing");
			processing = false;
		}

	}
}
