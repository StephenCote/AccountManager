package org.cote.rest.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.accountmanager.services.ThreadService;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.StreamUtil;
import org.cote.jaas.AM5LoginModule;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.server.spi.AbstractContainerLifecycleListener;
import org.glassfish.jersey.server.spi.Container;

public class RestServiceConfig extends ResourceConfig{
	private static final Logger logger = LogManager.getLogger(RestServiceConfig.class);
	
	public RestServiceConfig(@Context ServletContext servletContext){
		ServiceUtil.useAccountManagerSession = false;
		register(StartupHandler.class);
		packages("org.cote.rest.services");
		register(RolesAllowedDynamicFeature.class);
		
	}
	
    private static class StartupHandler extends  AbstractContainerLifecycleListener {
        @Context
        ServletContext context;

        private List<ThreadService> maintenanceThreads = new ArrayList<>();
        /*
        static DatabaseMaintenance dbMaintenance = null;
    	private static AuditDataMaintenance auditThread = null;
    	private static SessionDataMaintenance sessionThread = null;
        */
        @Override
        public void onShutdown(Container container) {
            logger.info("Cleaning up AccountManager");

            try {
            	logger.info("Stopping maintenance threads");
                for(ThreadService svc : maintenanceThreads){
                	svc.requestStop();
                }
                /// Sleep to give the threads a chance to shut down
                ///
				Thread.sleep(1000);

			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            maintenanceThreads.clear();

        }

    	
        @Override
        public void onStartup(Container container) {
        	initializeAccountManager();
        }

		private void initializeAccountManager(){
			logger.info("Initializing Account Manager");
			//System.out.println("******");
			String dsName = context.getInitParameter("database.dsname");
			//logger.debug("DS Name: " + dsName);
			ConnectionFactory cf = ConnectionFactory.getInstance();
			cf.setConnectionType(CONNECTION_TYPE.DS);
			cf.setJndiDataSource(dsName);
			cf.setDriverClassName(context.getInitParameter("database.driver"));
			cf.setCheckDriver(Boolean.parseBoolean(context.getInitParameter("database.checkdriver")));
	
			Connection c = cf.getConnection();
	
			try{
				if(c == null || c.isClosed() == true){
					logger.error("Warning: Connection is null or closed");
				}
				else{
					c.close();
				}
			}
			catch(SQLException sqe){
				logger.error(sqe.getMessage());
				logger.error("Error",sqe);
			}
			
			logger.info("Priming Additional Factories");
			String addFact = context.getInitParameter("factories.add");
			if(addFact != null){
				String[] facts = addFact.split(",");
				for(int i = 0; i < facts.length;i++){
					try {
						logger.info("Priming " + facts[0]);
						Class cls = Class.forName(facts[i]);
						Factories f = (Factories)cls.newInstance();
						logger.warn("Refactor to an interface - this is only preparing the base service");
						//f.prepare();
						if(facts[i].equals("org.cote.rocket.Factories")) org.cote.rocket.Factories.prepare();
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						logger.error("Trace", e);
					}
					
				}
			}
			
			Object obj = Factories.getBulkFactory(FactoryEnumType.LIFECYCLE);
			if(obj == null){
				logger.error("Failed to load bulk factory from extension library");
			}
			/// invoke clear caches to queue up the table schemas
			///
			//Factories.clearCaches();
			Factories.warmUp();
			
			logger.info("Starting Maintenance Threads");
			String addJob = context.getInitParameter("maintenance.jobs");
			if(addJob != null){
				String[] jobs = addJob.split(",");
				for(int i = 0; i < jobs.length;i++){
					try {
						logger.info("Starting " + jobs[i]);
						Class cls = Class.forName(jobs[i]);
						ThreadService f = (ThreadService)cls.newInstance();
						maintenanceThreads.add(f);
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						logger.error("Trace", e);
					}
					
				}
			}
		
			BaseService.enableExtendedAttributes = Boolean.parseBoolean(context.getInitParameter("extended.attributes.enabled"));
			logger.info("Extended attributes enabled: " + BaseService.enableExtendedAttributes);

			
			/// Set any default vault on the BaseService
			///
			/*
			String vaultName = context.getInitParameter("vault.name");
			String vaultPath = context.getInitParameter("vault.path");
			String vaultCredential = context.getInitParameter("vault.credential");
			
			if(vaultName != null && vaultPath != null && vaultCredential != null){
				logger.info("Initializing vault '" + vaultName + "'");
				VaultService service = new VaultService();
				VaultBean vault = service.loadVault(vaultPath, vaultName, true);
				CredentialType cred = service.loadProtectedCredential(vaultCredential);
				if(cred != null && vault != null){
					try {
						service.initialize(vault, cred);
						BaseService.contextVault = vault;
						BaseService.contextVaultService = service;
						logger.info("Initialized vault");
					} catch (ArgumentException | FactoryException e) {
						logger.error(e);
					}
				}
				else{
					logger.error("Failed to initialize vault from " + vaultPath + " or credential from " + vaultCredential);
				}
			}
			*/
			String roleAuth = context.getInitParameter("amauthrole");
			if(roleAuth != null && roleAuth.length() > 0){
				AM5LoginModule.setAuthenticatedRole(roleAuth);
			}
			
			String roleMapPath = context.getInitParameter("amrolemap");
			InputStream resourceContent = null;
			Map<String,String> roleMap = new HashMap<>();
			try {
				resourceContent = context.getResourceAsStream(roleMapPath);
				roleMap = JSONUtil.getMap(StreamUtil.getStreamBytes(resourceContent), String.class, String.class);
			} catch (IOException e) {
				
				logger.error("Error",e);
			}
			finally{
				if(resourceContent != null)
					try {
						resourceContent.close();
					} catch (IOException e) {
						
						logger.error("Error",e);
					}
			}
			AM5LoginModule.setRoleMap(roleMap);
			//System.out.println("**** Loaded " + roleMap.toString() + " role maps");

		}
    }
}
