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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;

@DeclareRoles({"admin","user"})
@Path("/community")
public class CommunityService {
	
	private static final Logger logger = LogManager.getLogger(CommunityService.class);
	//protected static Set<AuditEnumType> parentType = new HashSet<>(Arrays.asList(AuditEnumType.GROUP, AuditEnumType.ROLE, AuditEnumType.PERMISSION));
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
			provider.setRandomizeSeedPopulation(false);
			provider.setOrganizePersonManagement(true);
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
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/enroll/reader/{userId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response enrollReaderInCommunities(@PathParam("userId") String userId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean enrolled = false;
		if(cp != null) enrolled = cp.enrollReaderInCommunities(user, userId);
		return Response.status(200).entity(enrolled).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/enroll/reader/{userId:[0-9A-Za-z\\-]+}/{communityId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response enrollReaderInCommunity(@PathParam("userId") String userId, @PathParam("communityId") String communityId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean enrolled = false;
		if(cp != null) enrolled = cp.enrollReaderInCommunity(user, userId, communityId);
		return Response.status(200).entity(enrolled).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/communityRoles")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCommunityRoles(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		List<BaseRoleType> roles = new ArrayList<>();
		ICommunityProvider cp = getProvider();
		if(cp != null) roles = cp.getCommunityRoles(user);
		return Response.status(200).entity(roles).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/enroll/reader/{userId:[0-9A-Za-z\\-]+}/{communityId:[0-9A-Za-z\\-]+}/{projectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response enrollReaderInCommunityProject(@PathParam("userId") String userId, @PathParam("communityId") String communityId,  @PathParam("projectId") String projectId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean enrolled = false;
		if(cp != null) enrolled = cp.enrollReaderInCommunityProject(user, userId, communityId, projectId);
		return Response.status(200).entity(enrolled).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/enroll/admin/{userId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response enrollAdminInCommunities(@PathParam("userId") String userId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean enrolled = false;
		if(cp != null) enrolled = cp.enrollAdminInCommunities(user, userId);
		return Response.status(200).entity(enrolled).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/enroll/admin/{userId:[0-9A-Za-z\\-]+}/{communityId:[0-9A-Za-z\\\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response enrollAdminInCommunity(@PathParam("communityId") String communityId,@PathParam("userId") String userId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean enrolled = false;
		if(cp != null) enrolled = cp.enrollAdminInCommunity(user, communityId, userId);
		return Response.status(200).entity(enrolled).build();
	}
	
	@RolesAllowed({"admin","user"})
	@DELETE
	@Path("/{communityId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteCommunity(@PathParam("communityId") String communityId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean enrolled = false;
		if(cp != null) enrolled = cp.deleteCommunity(user, communityId);
		return Response.status(200).entity(enrolled).build();
	}
	
	@RolesAllowed({"admin","user"})
	@POST
	@Path("/project")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateCommunityProject(ProjectType project, @Context HttpServletRequest request){

		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean updated = false;
		if(cp != null) updated = cp.saveCommunityProject(project, user);
		return Response.status(200).entity(updated).build();
	}
	
	@RolesAllowed({"admin","user"})
	@DELETE
	@Path("/project/{projectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response deleteCommunityProject(@PathParam("projectId") String projectId, @Context HttpServletRequest request){

		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean enrolled = false;
		if(cp != null) enrolled = cp.deleteCommunityProject(user, projectId);
		return Response.status(200).entity(enrolled).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/project/{projectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCommunityProject(@PathParam("projectId") String projectId, @Context HttpServletRequest request){

		UserType user = ServiceUtil.getUserFromSession(request);
		if(user == null){
			logger.error("Null user");
			return Response.status(404).build();
		}
		ICommunityProvider cp = getProvider();
		ProjectType project = BaseService.readByObjectId(AuditEnumType.PROJECT, projectId, request);
		if(cp != null && project != null){
			cp.deepPopulate(project, user);
		}
		return Response.status(200).entity(project).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/new/{communityId:[0-9A-Za-z\\-]+}/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response newCommunityProject(@PathParam("communityId") String communityId, @PathParam("name") String name, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean enrolled = false;
		if(cp != null) enrolled = cp.createCommunityProject(user, communityId, name);
		return Response.status(200).entity(enrolled).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/new/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response newCommunity( @PathParam("name") String name, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean enrolled = false;
		if(cp != null) enrolled = cp.createCommunity(user, name);
		return Response.status(200).entity(enrolled).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/configure/{communityId:[0-9A-Za-z\\-]+}/{projectId:[0-9A-Za-z\\-]+}/{groupId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response configureCommunityProjectGroupEntitlements(@PathParam("communityId") String communityId, @PathParam("projectId") String projectId, @PathParam("groupId") String groupId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean enrolled = false;
		if(cp != null) enrolled = cp.configureEntitlements(user, communityId, projectId, groupId);
		return Response.status(200).entity(enrolled).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/find/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCommunity(@PathParam("name") String name, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		LifecycleType lc = null;
		if(cp != null) lc = cp.getCommunity(user, name);
		return Response.status(200).entity(lc).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/find/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}/{projectName: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCommunityProject(@PathParam("name") String name,@PathParam("projectName") String projectName, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		ProjectType lc = null;
		if(cp != null) lc = cp.getCommunityProject(user, name, projectName);
		return Response.status(200).entity(lc).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/userRoles/{userId:[0-9A-Za-z\\\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getUserCommunityRoles(@PathParam("userId") String userId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		UserType targUser = BaseService.readByObjectId(AuditEnumType.USER, userId, user);
		ICommunityProvider cp = getProvider();
		List<BaseRoleType> roles = new ArrayList<>();
		if(targUser != null && cp != null) roles = cp.getCommunitiesRoles(targUser);
		return Response.status(200).entity(roles).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/roles/{communityId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCommunityRoles(@PathParam("communityId") String communityId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		List<BaseRoleType> roles = new ArrayList<>();
		if(cp != null) roles = cp.getCommunityRoles(user, communityId);
		return Response.status(200).entity(roles).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/roles/project/{projectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCommunityProjectRoles(@PathParam("projectId") String projectId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		List<BaseRoleType> roles = new ArrayList<>();
		if(cp != null) roles = cp.getCommunityProjectRoles(user, projectId);
		return Response.status(200).entity(roles).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/role/base/{projectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCommunityProjectRoleBase(@PathParam("projectId") String projectId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		BaseRoleType role = null;
		if(cp != null) role = cp.getCommunityProjectRoleBase(user, projectId);
		return Response.status(200).entity(role).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/permission/base/{projectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getCommunityProjectPermissionBase(@PathParam("projectId") String projectId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		BasePermissionType role = null;
		if(cp != null) role = cp.getCommunityProjectPermissionBase(user, projectId);
		return Response.status(200).entity(role).build();
	}
	

	@RolesAllowed({"admin"})
	@GET
	@Path("/configure/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response configureCommunity(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean configured = false;
		if(cp != null) configured = cp.configureCommunity(user);
		return Response.status(200).entity(configured).build();
	}
	
	@RolesAllowed({"user","admin"})
	@GET
	@Path("/isconfigured/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response isCommunityConfigured(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean configured = false;
		if(cp != null) configured = cp.isCommunityConfigured(user.getOrganizationId());
		return Response.status(200).entity(configured).build();
	}
	
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/artifacts/{type:[A-Za-z]+}/{objectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response addProjectArtifacts(@PathParam("type") String type, @PathParam("objectId") String objectId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean addArt = cp.addProjectArtifacts(user, AuditEnumType.valueOf(type),objectId);
		return Response.status(200).entity(addArt).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/geo/traits/{type:[A-Za-z]+}/{objectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importGeoTraits(@PathParam("type") String type, @PathParam("objectId") String objectId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean imported = cp.importLocationTraits(user, AuditEnumType.valueOf(type),objectId,context.getInitParameter("data.generator.location"), "featureCodes_en.txt");
		return Response.status(200).entity(imported).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/geo/countryInfo/{type:[A-Za-z]+}/{objectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importGeoCountryInfo(@PathParam("type") String type, @PathParam("objectId") String objectId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean imported = cp.importLocationCountryInfo(user, AuditEnumType.valueOf(type),objectId,context.getInitParameter("data.generator.location"), "countryInfo.txt");
		return Response.status(200).entity(imported).build();
	}

	@RolesAllowed({"admin","user"})
	@GET
	@Path("/geo/admin1Codes/{type:[A-Za-z]+}/{objectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importGeoAdmin1Codes(@PathParam("type") String type, @PathParam("objectId") String objectId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean imported = cp.importLocationAdmin1Codes(user, AuditEnumType.valueOf(type),objectId,context.getInitParameter("data.generator.location"), "admin1CodesASCII.txt");
		return Response.status(200).entity(imported).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/geo/admin2Codes/{type:[A-Za-z]+}/{objectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importGeoAdmin2Codes(@PathParam("type") String type, @PathParam("objectId") String objectId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean imported = cp.importLocationAdmin2Codes(user, AuditEnumType.valueOf(type),objectId,context.getInitParameter("data.generator.location"), "admin2Codes.txt");
		return Response.status(200).entity(imported).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/geo/country/{type:[A-Za-z]+}/{objectId:[0-9A-Za-z\\-]+}/{codes:[A-Za-z\\,]+}/{alternate:(true|false)}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response importGeoCountryData(@PathParam("type") String type, @PathParam("objectId") String objectId, @PathParam("codes") String codes,@PathParam("alternate") boolean alternate,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean imported = cp.importLocationCountryData(user, AuditEnumType.valueOf(type),objectId,context.getInitParameter("data.generator.location"), codes,(alternate ? "alternateNames.txt" : null));
		return Response.status(200).entity(imported).build();
	}
	/*
	 * 	private int testLocSize = 3;
	private int testPerSeed = 250;
	private int testEpochEvolutions = 10;
	private int testEpochCount = 15;
	 */
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/generate/application/{communityId:[0-9A-Za-z\\-]+}/{projectId:[0-9A-Za-z\\-]+}/{appName: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}/{usePermissions:(true|false)}/{useGroups:(true|false)}/{seedSize:[\\d]+}/{maxSize:[\\d]+}/{distribution:[\\d\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response generateCommunityProjectApplication(@PathParam("communityId") String communityId, @PathParam("projectId") String projectId, @PathParam("appName") String appName, @PathParam("usePermissions") boolean usePermissions, @PathParam("useGroups") boolean useGroups, @PathParam("seedSize") int seedSize,  @PathParam("maxSize") int maxSize,  @PathParam("distribution") double distribution, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		
		boolean enrolled = false;
		if(cp != null){
			enrolled = cp.generateCommunityProjectApplication(user, communityId, projectId, appName, usePermissions, useGroups, seedSize, maxSize, distribution, context.getInitParameter("data.generator.dictionary"),context.getInitParameter("data.generator.names"));
		}
		return Response.status(200).entity(enrolled).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/generate/region/{communityId:[0-9A-Za-z\\-]+}/{projectId:[0-9A-Za-z\\-]+}/{locationSize:[\\d]+}/{seedSize:[\\d]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response generateCommunityProjectRegion(@PathParam("communityId") String communityId, @PathParam("projectId") String projectId, @PathParam("locationSize") int locationSize,  @PathParam("seedSize") int seedSize, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		
		boolean enrolled = false;
		if(cp != null){
			cp.setRandomizeSeedPopulation(false);
			enrolled = cp.generateCommunityProjectRegion(user, communityId, projectId, locationSize, seedSize, context.getInitParameter("data.generator.dictionary"), context.getInitParameter("data.generator.names"));
		}
		return Response.status(200).entity(enrolled).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/generate/evolve/{communityId:[0-9A-Za-z\\-]+}/{projectId:[0-9A-Za-z\\-]+}/{epochSize:[\\d]+}/{epochEvolutions:[\\d]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response evolveCommunityProjectRegion(@PathParam("communityId") String communityId, @PathParam("projectId") String projectId, @PathParam("epochSize") int epochSize, @PathParam("epochEvolutions") int epochEvolutions, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		boolean enrolled = false;
		if(cp != null) enrolled = cp.evolveCommunityProjectRegion(user, communityId, projectId, epochSize, epochEvolutions, context.getInitParameter("data.generator.dictionary"), context.getInitParameter("data.generator.names"));
		return Response.status(200).entity(enrolled).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/generate/report/{communityId:[0-9A-Za-z\\-]+}/{projectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response reportCommunityProjectRegion(@PathParam("communityId") String communityId, @PathParam("projectId") String projectId, @PathParam("epochSize") int epochSize, @PathParam("epochEvolutions") int epochEvolutions, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		ICommunityProvider cp = getProvider();
		String report = null;
		if(cp != null) report = cp.reportCommunityProjectRegion(user, communityId, projectId, context.getInitParameter("data.generator.dictionary"), context.getInitParameter("data.generator.names"));
		return Response.status(200).entity(report).build();
	}
	
}