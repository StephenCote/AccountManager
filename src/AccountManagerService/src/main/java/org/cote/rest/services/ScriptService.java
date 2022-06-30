/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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

import java.util.Map;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
import org.cote.accountmanager.data.services.ICommunityProvider;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.accountmanager.util.DataUtil;
import org.graalvm.polyglot.PolyglotException;

@DeclareRoles({"admin","user"})
@Path("/script")
public class ScriptService {
	
	private static final Logger logger = LogManager.getLogger(ScriptService.class);
	private static SchemaBean schemaBean = null;
	private static ICommunityProvider provider = null;
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;

	private ICommunityProvider getProvider(){
		if(provider != null) return provider;
		String pcls = context.getInitParameter("factories.community");
		try {
			logger.info("Initializing community provider " + pcls);
			Class<?> cls = Class.forName(pcls);
			ICommunityProvider f = (ICommunityProvider)cls.newInstance();
			provider = f;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			logger.error(FactoryException.TRACE_EXCEPTION, e);
		}
		
		return provider;
	}
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
	
	private Map<String, Object> getParams(UserType user, DataType data){
		/// logger, user, organizationPath
		///
		Map<String, Object> params = org.cote.accountmanager.data.services.ScriptService.getCommonParameterMap(user);
		if(data != null) params.put("source", data.getUrn());
		return params;
	}
	private Response execute(UserType user, DataType script) {
		Object result = null;
		String id = null;
		String scriptText = null;
		if(script == null) {
			logger.error("Null script");
		}
		else {
			try {
				scriptText = DataUtil.getValueString(script);
				if(scriptText != null && scriptText.length() > 0) {
					Map<String, Object> params = getParams(user, script);
					result = org.cote.accountmanager.data.services.ScriptService.run(Object.class, scriptText, params);
				}
				else {
					logger.error("Null or empty script text");
				}
			} catch (PolyglotException | DataException | ArgumentException e) {
				logger.error(e);
			}
		}
		return Response.status(200).entity(new ScriptResponseType(user.getUrn(), script.getUrn(), result)).build();	
	}
	
	@RolesAllowed({"admin", "user", "script"})
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/template")
	public Response getTemplate(@Context HttpServletRequest request){
		/// UserType user = ServiceUtil.getUserFromSession(request);
		String template = 
"""
/*jslint browser */
/*global console, logger, user*/
/// let AuditEnumType = Java.type("org.cote.accountmanager.objects.types.AuditEnumType");		
/// let GroupEnumType = Java.type("org.cote.accountmanager.objects.types.GroupEnumType");
/// let OperationResponseEnumType = Java.type("org.cote.accountmanager.objects.OperationResponseEnumType");
/// var BaseService =  Java.type("org.cote.accountmanager.service.rest.BaseService");
/// let GroupEnumType = Java.type("org.cote.accountmanager.objects.types.GroupEnumType");
/// let homeDirectory = BaseService.findGroup(user, GroupEnumType.DATA, "~");
/// let dirs = BaseService.listByGroup(AuditEnumType.GROUP, "DATA", homeDirectory.objectId, 0, 10, user);
/// logger.info("Found: " + dirs.length);
""";
		return Response.status(200).entity(template).build();	
		
	}
	
	@RolesAllowed({"script"})
	@GET
	@Path("/execid/{objectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response executeByObjectId(@PathParam("objectId") String objectId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		DataType data = BaseService.readByObjectId(AuditEnumType.DATA, objectId, user);
		return execute(user, data);
		
	}
	
	@RolesAllowed({"script"})
	@GET
	@Path("/execurn/{urn: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.:]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response executeByUrn(@PathParam("urn") String urn, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		DataType data = BaseService.readByUrn(AuditEnumType.DATA, urn, user);
		return execute(user, data);
	}
	
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/community/exec/{communityId:[0-9A-Za-z\\-]+}/{projectId:[0-9A-Za-z\\-]+}/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response executeCommunityProjectScript(@PathParam("communityId") String communityId, @PathParam("projectId") String projectId, @PathParam("name") String name, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		Object report = null;
		if(cp != null) report = cp.executeCommunityProjectScript(user, communityId, projectId, name);
		return Response.status(200).entity(report).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/community/{communityId:[0-9A-Za-z\\-]+}/{projectId:[0-9A-Za-z\\-]+}/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCommunityProjectScript(@PathParam("communityId") String communityId, @PathParam("projectId") String projectId, @PathParam("name") String name, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		String report = null;
		if(cp != null) report = cp.getCommunityProjectScript(user, communityId, projectId, name);
		return Response.status(200).entity(report).build();
	}
	
	@RolesAllowed({"admin","user"})
	@POST
	@Path("/community/{communityId:[0-9A-Za-z\\-]+}/{projectId:[0-9A-Za-z\\-]+}/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public Response updateCommunityProjectScript(@PathParam("communityId") String communityId, @PathParam("projectId") String projectId, @PathParam("name") String name, String dataStr,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean outBool = false;
		if(cp != null) outBool = cp.updateCommunityProjectScript(user, communityId, projectId, name, dataStr);
		return Response.status(200).entity(outBool).build();
	}

}

class ScriptResponseType{
	private String userUrn = null;
	private String Urn = null;
	private Object result = null;
	public ScriptResponseType(String userUrn, String scriptUrn, Object scriptResult) {
		Urn = scriptUrn;
		result = scriptResult;
		this.userUrn = userUrn;
	}
	public String getUrn() {
		return Urn;
	}
	public Object getResult() {
		return result;
	}
	public String getUserUrn() {
		return userUrn;
	}
	
}
