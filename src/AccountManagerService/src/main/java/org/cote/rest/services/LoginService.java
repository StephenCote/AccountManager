package org.cote.rest.services;

import java.io.IOException;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.jaas.AccountManagerCallbackHandler;

@Path("/login")
public class LoginService {
	private static final Logger logger = LogManager.getLogger(LoginService.class);
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(CredentialType credential, @Context HttpServletRequest request, @Context HttpServletResponse response){
		boolean out_bool = false;
		AccountManagerCallbackHandler callback = new AccountManagerCallbackHandler(credential);

		//LoginContext lcontext = null;
        boolean loginSuccess = true;
	      try{
				//request.getSession(true);
	    	  LoginContext lc = new LoginContext("AccountManagerService", callback);
				//LogicModule lm = new LoginModule();
				lc.login();
		        //RequestDispatcher requestDispatcher = request.getRequestDispatcher("/Protected");
		         
		       // requestDispatcher.forward(request, response);
		        
	         // lcontext = new LoginContext( "AccountManager",callback );
	          //lcontext.login( );
	      } catch (LoginException lge){
	    	  logger.error("ERROR",lge.getMessage());
	    	  logger.error("TRACE", lge.getStackTrace());
	          loginSuccess = false;
	      }
		return Response.status(200).cookie( new NewCookie( "JSESSIONID", request.getSession().getId() + ";path=/AccountManagerService;HttpOnly")).entity(loginSuccess).build();
	}
	
}
