package org.cote.rest.services;

import javax.annotation.security.DeclareRoles;
import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
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
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AuthenticationRequestType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;

@DeclareRoles({"admin","user"})
@Path("/organization")
public class OrganizationService {

	private static SchemaBean schemaBean = null;
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;
	
	private static final Logger logger = LogManager.getLogger(OrganizationService.class);
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
	
	@RolesAllowed({"user"})
	@POST
	@Path("/{parentId:[0-9A-Za-z\\-]+}/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public static Response addOrganization(@PathParam("parentId") String parentId,@PathParam("name") String name,AuthenticationRequestType adminCredential, @Context HttpServletRequest request){
		boolean out_bool = false;
		String adminPassword = null;
		if(adminCredential == null || adminCredential.getCredentialType().equals(CredentialEnumType.HASHED_PASSWORD)==false){
			logger.error("Invalid admin credential");
			return Response.status(200).entity(false).build();
		}

		String credStr = (new String(adminCredential.getCredential())).trim();
		try{
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByObjectId(parentId, 0L);
			UserType user = ServiceUtil.getUserFromSession(request);
			if(org != null && user != null){
				OrganizationType uOrg = org;

				if(uOrg.getName().equals("Global") && uOrg.getParentId().equals(0L)) uOrg = Factories.getSystemOrganization();
				boolean sysAdmin = RoleService.getIsUserInRole(RoleService.getSystemAdministratorUserRole(uOrg.getId()), user);
				if(sysAdmin == true){
					OrganizationType newOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).addOrganization(name,OrganizationEnumType.PUBLIC,org);
					if(newOrg != null && FactoryDefaults.setupOrganization(newOrg, (new String(adminCredential.getCredential())).trim())){
						logger.info("Created organization " + name + " in " + uOrg.getName());
						UserType adminUser2 = SessionSecurity.login("Admin", CredentialEnumType.HASHED_PASSWORD,(new String(adminCredential.getCredential())).trim(), newOrg.getId());
						if(adminUser2 != null){
							logger.info("Verified new administrator user");
							SessionSecurity.logout(adminUser2);
							out_bool = true;
						}
						else{
							logger.error("Unable to verify new administrator user");
						}
					}
				}
				else{
					logger.error("User is not a system administrator in the parent organization");
				}
			}
			else{
				logger.error("Invalid parent organization");
			}
		}
		catch(ArgumentException | FactoryException | DataAccessException e) {
			
			logger.error("Exception",e);
		}

		return Response.status(200).entity(out_bool).build();
	}
	
	@RolesAllowed({"user"})
	@DELETE
	@Path("/{objectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public static Response deleteOrganization(@PathParam("objectId") String parentId, @Context HttpServletRequest request){
		boolean out_bool = false;
		String adminPassword = null;
		try{
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getByObjectId(parentId, 0L);
			UserType user = ServiceUtil.getUserFromSession(request);
			if(org != null && user != null){
				OrganizationType uOrg = org;
				if(uOrg.getName().equals("Global") && uOrg.getParentId().equals(0L)) uOrg = Factories.getSystemOrganization();
				boolean sysAdmin = RoleService.getIsUserInRole(RoleService.getSystemAdministratorUserRole(uOrg.getId()), user);

				if(sysAdmin == true){
					out_bool = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).delete(uOrg);
					if(out_bool = true){
						Factories.cleanupOrphans();
					}
					else{
						logger.error("Failed to delete organization");
					}
				}
				else{
					logger.error("User " + user.getUrn() + " is not a system administrator in organization #" + uOrg.getId() + " " + uOrg.getUrn());
				}
			}
			else{
				logger.error("Invalid parent organization");
			}
		}
		catch(ArgumentException | FactoryException | DataAccessException e) {
			
			logger.error(e.getMessage());
		}

		return Response.status(200).entity(out_bool).build();
	}

}
