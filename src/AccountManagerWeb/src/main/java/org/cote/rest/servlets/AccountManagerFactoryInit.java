package org.cote.rest.servlets;


import java.io.IOException;
import java.security.Security;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;

/**
 * Servlet implementation class AccountManagerFactoryInit
 */
public class AccountManagerFactoryInit extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AccountManagerFactoryInit() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig config) throws ServletException {
		// TODO Auto-generated method stub
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.DS);
		cf.setJndiDataSource(config.getServletContext().getInitParameter("database.dsname"));
		cf.setDriverClassName(config.getServletContext().getInitParameter("database.driver"));
		cf.setCheckDriver(Boolean.parseBoolean(config.getServletContext().getInitParameter("database.checkdriver")));

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
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
