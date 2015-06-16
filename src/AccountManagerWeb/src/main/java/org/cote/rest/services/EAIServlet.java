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
package org.cote.rest.services;

import java.io.IOException;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.cote.util.AccountManagerCallbackHandler;

/**
 * Servlet implementation class EAIServlet
 */
public class EAIServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final Logger logger = Logger.getLogger(EAIServlet.class.getName());
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EAIServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	//	if(request.getUserPrincipal())
		//request.authenticate(arg0)
		// p = request.getContext().getRealm().authenticate(username, (String) credential);

		response.getWriter().write("EAI Servlet");
		//response.getWriter().write("\nLogin=" + doLogin());
		//request.login("TestUser1", "password");
	}
	
	private boolean doLogin(){
		try {
			LoginContext ctx = new LoginContext("accountmanager5",new AccountManagerCallbackHandler("TestUser1","password"));
			ctx.login();
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		boolean didLogin = false;
		return didLogin;

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String targetUser = request.getParameter("samEAIUser");
		String redirectUrl = request.getParameter("samEAIRedirect");
		if(targetUser != null){
			/// Perfom principal validation
			/// Perform target user validation against LDAP
			response.setHeader("am-eai-user-id",targetUser);
			response.setHeader("am-eai-redir-url", redirectUrl);
		}
		/*
		 * redirect-url-hdr-name =am-eai-redir-url
			pac-hdr-name =am-eai-pac
			pac-svc-id-hdr-name =am-eai-pac-svc
			user-id-hdr-name =am-eai-user-id
			user-auth-level-hdr-name =am-eai-auth-level
			user-qop-hdr-name =am-eai-qop
			user-ext-attr-list-hdr-name =am-eai-xattrs
		 */
	}

}
