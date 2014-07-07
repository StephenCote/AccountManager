package org.cote.rest.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.util.ServiceUtil;

/**
 * Servlet implementation class AccountManagerLogoutServlet
 */
public class AccountManagerLogoutServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public AccountManagerLogoutServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		UserType user = null;
		try {
			user = SessionSecurity.getUserBySession(request.getSession().getId(), ServiceUtil.getOrganizationFromRequest(request));
			if(user != null) SessionSecurity.logout(user);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//logger.info("Invalidate the session because the JEE LoginModule was used.");		
		request.getSession().invalidate();
		ServiceUtil.clearCookie(response, "OrganizationId");
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
