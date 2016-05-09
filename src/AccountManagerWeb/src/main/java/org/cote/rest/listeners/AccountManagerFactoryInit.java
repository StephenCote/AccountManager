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
package org.cote.rest.listeners;


import java.security.Security;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditDataMaintenance;
import org.cote.accountmanager.data.services.DatabaseMaintenance;
import org.cote.accountmanager.data.services.SessionDataMaintenance;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.util.ArticleUtil;

/**
 * Servlet implementation class AccountManagerFactoryInit
 */
public class AccountManagerFactoryInit implements ServletContextListener {
	public static final Logger logger = Logger.getLogger(AccountManagerFactoryInit.class.getName());
	private static final long serialVersionUID = 1L;
	private static DatabaseMaintenance dbMaintenance = null;
	private static AuditDataMaintenance auditThread = null;
	private static SessionDataMaintenance sessionThread = null;
    private ServletContext context = null;

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AccountManagerFactoryInit() {
    	System.out.println("AM Factory Init");

    }

      /*This method is invoked when the Web Application has been removed 
      and is no longer able to accept requests
      */

      public void contextDestroyed(ServletContextEvent event)
      {

        logger.info("Cleaning up AccountManager");
        this.context = null;
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


      //This method is invoked when the Web Application
      //is ready to service requests

      public void contextInitialized(ServletContextEvent event)
      {
    	logger.info("Initializing Account Manager");
        this.context = event.getServletContext();

        context.getSessionCookieConfig().setPath("/");
        
    	logger.info("Adding Security Provider");
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		
		ServiceUtil.useAccountManagerSession = Boolean.parseBoolean(context.getInitParameter("session.am5.enabled"));
		
		logger.info("Testing Database Connection");
		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.DS);
		cf.setJndiDataSource(context.getInitParameter("database.dsname"));
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
			sqe.printStackTrace();
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
		
		/// Prime the article roles
		logger.info("Priming Public Roles");
		String orgPath = context.getInitParameter("organization.default");
		OrganizationType org = null;
		if(orgPath != null && orgPath.length() > 0){
			try {
				org = Factories.getOrganizationFactory().findOrganization(context.getInitParameter("organization.default"));
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i = 0; i < ArticleUtil.ARTICLE_ROLES.length; i++){
				UserRoleType role = ArticleUtil.getRoleByName(ArticleUtil.ARTICLE_ROLES[i], org.getId());
			}
		}
		else{
			// logger.info("Skipping config check for default organization");
		}
		
		BaseService.enableExtendedAttributes = Boolean.parseBoolean(context.getInitParameter("extended.attributes.enabled"));
		logger.info("Extended attributes enabled: " + BaseService.enableExtendedAttributes);
		
	}



}
