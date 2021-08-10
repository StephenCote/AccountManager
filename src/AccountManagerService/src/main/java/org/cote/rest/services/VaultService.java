package org.cote.rest.services;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.security.RolesAllowed;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.beans.VaultBean;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.VaultType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.SecurityUtil;

@Path("/vault")
public class VaultService {
	public static final Logger logger = LogManager.getLogger(VaultService.class);

	private static org.cote.accountmanager.data.services.VaultService vaultService = new org.cote.accountmanager.data.services.VaultService();
	private static SchemaBean schemaBean = null;
	private String vaultPath = null;
	private String vaultCredentialPath = null;
	
	private String getVaultPath(){
		if(vaultPath == null){
			vaultPath = context.getInitParameter("vault.path");
		}
		return vaultPath;
	}
	
	private String getVaultCredentialPath(){
		if(vaultCredentialPath == null){
			vaultCredentialPath = context.getInitParameter("vault.credential.path");
		}
		return vaultCredentialPath;
	}
	
	@Context
	ServletContext context;
	
	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	public SchemaBean getSmdSchema(@Context UriInfo uri){
		if(schemaBean != null) return schemaBean;
		schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		return schemaBean;
	}
	
	
	@RolesAllowed({"api","user","admin"})
	@GET
	@Path("/{name: [\\:\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getVaultUrn(@PathParam("name") String name, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		logger.info("Get vault urn for " + name);
		BaseService.populate(AuditEnumType.USER, user);
		DirectoryGroupType vaultGroup = BaseService.readByNameInParent(AuditEnumType.GROUP, user.getHomeDirectory(), ".vault", "DATA", user);
		if(vaultGroup == null) {
			logger.error("Failed to obtain vault base group");
			return Response.status(200).entity(null).build();
		}
		DataType data = BaseService.readByName(AuditEnumType.DATA, vaultGroup, name, user);
		if(data == null) {
			logger.error("Vault data object is null");
			return Response.status(200).entity(null).build();
		}
		logger.info("Returning " + data.getUrn());
		return Response.status(200).entity(data.getUrn()).build();
		
	}
	
	@RolesAllowed({"api","user","admin"})
	@DELETE
	@Path("/{urn: [\\:\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteVault(@PathParam("urn") String urn, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		logger.info("Delete vault " + urn);
		VaultBean vault = vaultService.getVaultByUrn(user, urn);
		
		if(vault == null){
			logger.error("Vault is null");
			return Response.status(200).entity(false).build();
		}
		DataType data = BaseService.readByUrn(AuditEnumType.DATA, urn, user);
		if(data == null) {
			logger.error("Vault object is null");
			return Response.status(200).entity(false).build();
		}
	
		boolean deleted = false;
		try{
			if(!BaseService.canChangeType(AuditEnumType.DATA, user, data)) {
				logger.error("User is not authorized to change data object for " + urn);
				return Response.status(200).entity(false).build();
			}

			deleted = vaultService.deleteVault(vault);
		}
		catch(FactoryException | ArgumentException e){
			logger.error(e);
			e.printStackTrace();
			return Response.status(200).entity(false).build();
		}
		
		if(deleted){
			logger.info("Deleted vault");
		}
		else{
			logger.error("Failed to delete vault");
		}
		return Response.status(200).entity(deleted).build();
		
	}
	
	@RolesAllowed({"api","user","admin"})
	@GET
	@Path("/list")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listVaults(@PathParam("userId") String userId, @Context HttpServletRequest request){

		UserType user = ServiceUtil.getUserFromSession(request);
		List<Object> vaults = new ArrayList<>();
		try {
			List<VaultType> vaultList = vaultService.listVaultsByOwner(user);
			for(VaultType vault : vaultList) vaults.add(vault.getVaultDataUrn());
		} catch (FactoryException | ArgumentException | DataException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return Response.status(200).entity(vaults).build();
	}
	
	@RolesAllowed({"api","user","admin"})
	@GET
	@Path("/list/{objectId:[0-9A-Za-z\\\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response listVaultsForUser(@PathParam("userId") String userId, @Context HttpServletRequest request){

		UserType user = ServiceUtil.getUserFromSession(request);
		UserType targetUser = null;
		List<Object> vaults = new ArrayList<>();
		if(user.getObjectId().contentEquals(userId)) targetUser = user;
		else {
			targetUser = BaseService.readByObjectId(AuditEnumType.USER, userId, user);
		}
		if(targetUser != null) {
			try {
				List<VaultType> vaultList = vaultService.listVaultsByOwner(targetUser);
				for(VaultType vault : vaultList) vaults.add(vault.getVaultDataUrn());
			} catch (FactoryException | ArgumentException | DataException e) {
				logger.error(e.getMessage());
			}
		}
		else {
			logger.error("Unable to access target user: " + userId);
		}

		return Response.status(200).entity(vaults).build();
	}
	
	@RolesAllowed({"api","user","admin"})
	@GET
	@Path("/create/{name: [\\(\\)@%\\sa-zA-Z_0-9\\-\\.]+}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response createVault(@PathParam("name") String vaultName, @Context HttpServletRequest request){

		UserType user = ServiceUtil.getUserFromSession(request);
		VaultType vaultObj = null;
		
		BaseService.populate(AuditEnumType.USER, user);
		VaultBean vault = new VaultBean();
		String credentialPath = getVaultCredentialPath() + SecurityUtil.getDigestAsString(user.getObjectId() + "-" + vaultName) + ".credential.json";
		try {
			if(!vaultService.createProtectedCredentialFile(user, credentialPath, UUID.randomUUID().toString().getBytes())){
				logger.error("Failed to create credential file at " + credentialPath);
				return Response.status(500).entity(null).build();
			}
			CredentialType cred = vaultService.loadProtectedCredential(credentialPath);
			if(cred == null){
				logger.error("Failed to load credential file at " + credentialPath);
				return Response.status(500).entity(null).build();
			}
			String vaultPath = getVaultPath() + user.getUrn().replaceAll(":", ".") + "/";
			if(!FileUtil.makePath(vaultPath)) {
				logger.error("Failed to emit vault path: " + vaultPath);
				return Response.status(500).entity(null).build();
			}
			vault = vaultService.newVault(user, vaultPath, vaultName);
			vaultService.setProtectedCredentialPath(vault, credentialPath);
			if(!vaultService.createVault(vault, cred)){
				logger.error("Failed to create vault");
				return Response.status(500).entity(null).build();
			}
			VaultBean chkVault =  vaultService.loadVault(vaultPath, vaultName, true);
			if(chkVault == null){
				logger.error("Failed to restore vault");
				return Response.status(500).entity(null).build();
			}

			vaultService.initialize(vault, cred);
			SecurityBean key = vaultService.getVaultKey(vault);
			if(key == null){
				logger.error("Failed to extract and decrypt vault key");
				return Response.status(500).entity(null).build();
			} 

		}
		catch(FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return Response.status(500).entity(null).build();
		}
		logger.info("Created vault " + vault.getVaultDataUrn());

		return Response.status(200).entity(vault.getVaultDataUrn()).build();

	}

}
