package org.cote.rocket.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
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
		if(queue.size() == 0){
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
				if(art.getImports().size() == 0){
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
				catch(FactoryException e){
					logger.error(e.getMessage());
					logger.error("Error",e);
				} catch (ArgumentException e) {
					logger.error(e.getMessage());
					logger.error("Error",e);
				}
			}
		}
		catch(Exception e){
			logger.error(e.getMessage());
			logger.error("Error",e);
		}
		finally{
			logger.info("Completed data processing");
			processing = false;
		}

	}
}
