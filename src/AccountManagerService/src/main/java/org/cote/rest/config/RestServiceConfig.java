package org.cote.rest.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.services.AuditDataMaintenance;
import org.cote.accountmanager.data.services.DatabaseMaintenance;
import org.cote.accountmanager.data.services.SessionDataMaintenance;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.util.ServiceUtil;
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

    	private static DatabaseMaintenance dbMaintenance = null;
    	private static AuditDataMaintenance auditThread = null;
    	private static SessionDataMaintenance sessionThread = null;
        
        @Override
        public void onShutdown(Container container) {
            logger.info("Cleaning up AccountManager");

            if(dbMaintenance != null){
            	dbMaintenance.requestStop();
            	dbMaintenance = null;
            }
            if(auditThread != null){
            	auditThread.requestStop();
            	auditThread = null;
            }
            if(sessionThread != null){
            	sessionThread.requestStop();
            	sessionThread = null;
            }
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
			
			logger.info("Priming Factories");
			/// invoke clear caches to queue up the table schemas
			///
			Factories.clearCaches();
			
			logger.info("Starting Maintenance Threads");
			dbMaintenance = new DatabaseMaintenance();
			
			auditThread = new AuditDataMaintenance();
			auditThread.setThreadDelay(10000);
			
			sessionThread = new SessionDataMaintenance();
			
		
			BaseService.enableExtendedAttributes = Boolean.parseBoolean(context.getInitParameter("extended.attributes.enabled"));
			logger.info("Extended attributes enabled: " + BaseService.enableExtendedAttributes);

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
