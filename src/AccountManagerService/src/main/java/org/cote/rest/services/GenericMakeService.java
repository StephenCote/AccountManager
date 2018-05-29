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

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.util.BinaryUtil;

@DeclareRoles({"admin","user"})
@Path("/make/{type:[A-Za-z]+}")
public class GenericMakeService {

	private static SchemaBean schemaBean = null;
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;
	
	private static final Logger logger = LogManager.getLogger(GenericMakeService.class);
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }

	@RolesAllowed({"user"})
	@GET
	@Path("/{objectType:[A-Za-z]+}/{path:[@\\.~\\/%\\sa-zA-Z_0-9\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response findMakeObject(@PathParam("type") String type, @PathParam("objectType") String objectType, @PathParam("path") String path, @Context HttpServletRequest request){
		logger.info("Request to find object from: " + type + " " + path + ", and if it doesn't exist, then make it");
		AuditEnumType auditType = AuditEnumType.valueOf(type);
		if(path.startsWith("B64-")) path = BinaryUtil.fromBase64Str(path.substring(4,path.length())).replaceAll("%3D", "=");
		else if(path.startsWith("~") == false && path.startsWith(".") == false){
			path = "/" + path;
			/// Check for base64 encoded values, prefaced with B64-
			///
			
			
			if(path.contains("..")) path = path.replaceAll("\\.\\.", "/");
			else path = path.replace('.', '/');
			logger.info("Alt path: " + path);
		}
		

		Object obj = BaseService.makeFind(auditType, objectType, path, request);
		return Response.status(200).entity(obj).build();
	}


}
