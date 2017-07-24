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
package org.cote.rest.services;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;

@Path("/login")
public class LoginService {
	private static final Logger logger = LogManager.getLogger(LoginService.class);
	private static SchemaBean schemaBean = null;
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
	
	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public Response login(CredentialType credential, @Context HttpServletRequest request, @Context HttpServletResponse response){
		boolean out_bool = false;
		boolean loginSuccess = true;
		
		try {
			request.getSession();
			request.login(credential.getOrganizationPath() + "/" + credential.getName(), new String(credential.getCredential()));
		} catch (ServletException e) {
			
			logger.error("Error",e);
			loginSuccess = false;
		}
		/*
		AccountManagerCallbackHandler callback = new AccountManagerCallbackHandler(credential);

		//LoginContext lcontext = null;
        
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
		*/
		return Response.status(200).entity(loginSuccess).build();
	}
	
}
