package org.cote.rest.listeners;


import java.io.IOException;
import java.security.Security;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContextListener;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditDataMaintenance;
import org.cote.accountmanager.data.services.SessionDataMaintenance;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.util.ArticleUtil;

/**
 * Servlet implementation class AccountManagerFactoryInit
 */
public class AccountManagerFactoryInit implements ServletContextListener {
	private static final long serialVersionUID = 1L;
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

    	  System.out.println("Cleanup AccountManager Factory Servlet");
        this.context = null;
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
        this.context = event.getServletContext();

    //public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.DS);
		cf.setJndiDataSource(context.getInitParameter("database.dsname"));
		cf.setDriverClassName(context.getInitParameter("database.driver"));
		cf.setCheckDriver(Boolean.parseBoolean(context.getInitParameter("database.checkdriver")));

		Connection c = cf.getConnection();

		try{
			if(c == null || c.isClosed() == true){
				System.out.println("Warning: Connection is null or closed");
			}
			else{
				c.close();
			}
		}
		catch(SQLException sqe){
			System.out.println(sqe.getMessage());
			sqe.printStackTrace();
		}
		
		auditThread = new AuditDataMaintenance();
		auditThread.setThreadDelay(10000);
		
		sessionThread = new SessionDataMaintenance();
		
		/// Prime the article roles
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
				UserRoleType role = ArticleUtil.getRoleByName(ArticleUtil.ARTICLE_ROLES[i], org);
			}
		}
		else{
			// logger.info("Skipping config check for default organization");
		}
		
	}



}
