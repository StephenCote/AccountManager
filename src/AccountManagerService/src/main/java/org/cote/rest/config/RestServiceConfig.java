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
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.exceptions.FactoryException;
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
		ServiceUtil.setUseAccountManagerSession(false);
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
			String dsName = context.getInitParameter("database.dsname");
			ConnectionFactory cf = ConnectionFactory.getInstance();
			cf.setConnectionType(CONNECTION_TYPE.DS);
			cf.setJndiDataSource(dsName);
			cf.setDriverClassName(context.getInitParameter("database.driver"));
			cf.setCheckDriver(Boolean.parseBoolean(context.getInitParameter("database.checkdriver")));
	
			Connection c = cf.getConnection();
	
			try{
				if(c == null || c.isClosed()){
					logger.error("Warning: Connection is null or closed");
				}
				else{
					c.close();
				}
			}
			catch(SQLException sqe){
				logger.error(sqe.getMessage());
				logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
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

						if(facts[i].equals("org.cote.rocket.Factories")) org.cote.rocket.Factories.prepare();
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
						logger.error(FactoryException.TRACE_EXCEPTION, e);
					}
					
				}
			}
			try {
				Object obj = Factories.getBulkFactory(FactoryEnumType.LIFECYCLE);
				if(obj == null){
					logger.error("Failed to load bulk factory from extension library");
				}
				
				/// invoke clear caches to queue up the table schemas
				///
				Factories.warmUp();
			} catch (FactoryException e1) {
				logger.error(e1);
			}
			
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
						logger.error(FactoryException.TRACE_EXCEPTION, e);
					}
					
				}
			}
		
			try{
				Long dataCacheSize = Long.parseLong(context.getInitParameter("factories.data.cache"));
				((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).setMaximumCacheSize(dataCacheSize);
			}
			catch(Exception e){
				logger.error(e.getStackTrace());
				
			}
			
			BaseService.setEnableExtendedAttributes(Boolean.parseBoolean(context.getInitParameter("extended.attributes.enabled")));
			BaseService.setAllowDataPointers(Boolean.parseBoolean(context.getInitParameter("data.pointers.enabled")));

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
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
			finally{
				if(resourceContent != null)
					try {
						resourceContent.close();
					} catch (IOException e) {
						
						logger.error(FactoryException.LOGICAL_EXCEPTION,e);
					}
			}
			AM5LoginModule.setRoleMap(roleMap);
			//System.out.println("**** Loaded " + roleMap.toString() + " role maps");

		}
    }
}
