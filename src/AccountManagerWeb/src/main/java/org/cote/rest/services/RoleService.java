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


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.services.RoleServiceImpl;

@Path("/role")
public class RoleService{

	public static final Logger logger = Logger.getLogger(RoleService.class.getName());
	private static SchemaBean schemaBean = null;	
	public RoleService(){
		//JSONConfiguration.mapped().rootUnwrapping(false).build();

	}

	@GET @Path("/setRoleForGroup/{peid : [0-9]+}/{rid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setRoleForGroup(@PathParam("peid") long groupId, @PathParam("rid") long roleId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return RoleServiceImpl.setRole(user, roleId, AuditEnumType.GROUP, groupId, enable);
	}	
	@GET @Path("/setRoleForPerson/{peid : [0-9]+}/{rid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setRoleForPerson(@PathParam("peid") long personId, @PathParam("rid") long roleId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return RoleServiceImpl.setRole(user, roleId, AuditEnumType.PERSON, personId, enable);
	}

	@GET @Path("/setRoleForAccount/{peid : [0-9]+}/{rid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setRoleForAccount(@PathParam("peid") long accountId, @PathParam("rid") long roleId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return RoleServiceImpl.setRole(user, roleId, AuditEnumType.ACCOUNT, accountId, enable);
	}

	@GET @Path("/setRoleForUser/{peid : [0-9]+}/{rid : [0-9]+}/{enable:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean setRoleForUser(@PathParam("peid") long userId, @PathParam("rid") long roleId, @PathParam("enable") boolean enable, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return RoleServiceImpl.setRole(user, roleId, AuditEnumType.USER, userId, enable);
	}

	
	@GET @Path("/clearCache") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean flushCache(@Context HttpServletRequest request){
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "clearCache",AuditEnumType.SESSION, ServiceUtil.getSessionId(request));
		AuditService.targetAudit(audit, AuditEnumType.INFO, "Request clear factory cache");
		UserType user = ServiceUtil.getUserFromSession(audit,request);
		if(user==null){
			AuditService.denyResult(audit, "Deny for anonymous user");
			return false;
		}
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "Role Factory");
		Factories.getRoleFactory().clearCache();
		AuditService.permitResult(audit,user.getName() + " flushed Role Factory cache");
		return true;
	}

	@GET @Path("/authorizeUser/{organizationId:[\\d]+}/{userId:[\\d]+}/{roleId:[\\d]+}/{view:(true|false)}/{edit:(true|false)}/{delete:(true|false)}/{create:(true|false)}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean authorizeUser(@PathParam("organizationId") long organizationId,@PathParam("userId") long userId,@PathParam("roleId") long roleId,@PathParam("view") boolean view,@PathParam("edit") boolean edit,@PathParam("delete") boolean delete,@PathParam("create") boolean create,@Context HttpServletRequest request){
		return RoleServiceImpl.authorizeUser(organizationId, userId, roleId, view, edit, delete, create, request);
	}
	@GET @Path("/count/{organizationId:[\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int count(@PathParam("organizationId") long organizationId,@Context HttpServletRequest request){
		return RoleServiceImpl.count(organizationId,request);
	}	
	@GET @Path("/countInParent/{organizationId:[\\d]+}/{parentId:[\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int countInParent(@PathParam("organizationId") long organizationId,@PathParam("parentId") long parentId,@Context HttpServletRequest request){
		return RoleServiceImpl.countInParent(organizationId, parentId,request);
	}
	@POST @Path("/delete") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean delete(BaseRoleType bean,@Context HttpServletRequest request){
		return RoleServiceImpl.delete(bean, request);
	}
	
	@POST @Path("/add") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean add(BaseRoleType bean,@Context HttpServletRequest request){
		return RoleServiceImpl.add(bean, request);
	}
	
	@POST @Path("/update") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public boolean update(BaseRoleType bean,@Context HttpServletRequest request){
		return RoleServiceImpl.update(bean, request);
	}
	
	@GET @Path("/getRootRole/{organizationId:[\\d]+}/{type : [~%\\s0-9a-zA-Z\\/]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BaseRoleType getRootRole(@PathParam("organizationId") long organizationId,@PathParam("type") String type,@Context HttpServletRequest request){
		
		UserType user = ServiceUtil.getUserFromSession(request);
		return RoleServiceImpl.getRootRole(organizationId,user,type,request);

		//return RoleServiceImpl.getUserRole(ServiceUtil.getOrganizationFromRequest(request),request);
	}
	@GET @Path("/getUserRole/{organizationId:[\\d]+}/{type : [~%\\s0-9a-zA-Z\\/]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BaseRoleType getUserRole(@PathParam("organizationId") long organizationId,@PathParam("type") String type,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return RoleServiceImpl.getUserRole(organizationId,user,type,request);

		//return RoleServiceImpl.getUserRole(ServiceUtil.getOrganizationFromRequest(request),request);
	}
	/*
	@GET @Path("/read/{name: [%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BaseRoleType read(@PathParam("name") String name,@Context HttpServletRequest request){
		return RoleServiceImpl.readByOrganizationId(ServiceUtil.getOrganizationFromRequest(request).getId(),name, request);
	}
	@GET @Path("/readByOrganizationId/{orgId:[0-9]+}/{name: [%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BaseRoleType readByOrganizationId(@PathParam("name") String name,@PathParam("orgId") long orgId,@Context HttpServletRequest request){
		return RoleServiceImpl.readByOrganizationId(orgId, name, request);
	}
	*/
	@GET @Path("/readByParentId/{orgId: [0-9]+}/{parentId:[0-9]+}/{type: [%\\sa-zA-Z_0-9\\-]+}/{name: [%\\sa-zA-Z_0-9\\-]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BaseRoleType readByParentId(@PathParam("name") String name,@PathParam("type") String type, @PathParam("orgId") long orgId,@PathParam("parentId") long parentId,@Context HttpServletRequest request){

		return RoleServiceImpl.readByParent(orgId, parentId, name, type, request);
	}
	@GET @Path("/readById/{id: [0-9]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public BaseRoleType readById(@PathParam("id") long id,@Context HttpServletRequest request){
		return RoleServiceImpl.readById(id, request);
	}
	@GET @Path("/getPath/{id: [0-9]+}") @Produces(MediaType.TEXT_PLAIN) @Consumes(MediaType.TEXT_PLAIN)
	public String getPath(@PathParam("id") long id,@Context HttpServletRequest request){
		String path = null;
		BaseRoleType role = RoleServiceImpl.readById(id, request);
	
			try {
				if(role != null) path = Factories.getRoleFactory().getRolePath(role);
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return path;
	}

	@GET @Path("/listGroups/{orgId : [\\d]+}/{recordId : [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<UserGroupType> listGroups(@PathParam("orgId") long orgId,@PathParam("recordId") long recordId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		OrganizationType targOrg = null;
		UserRoleType targRole = null;
		try {
			targOrg = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(targOrg != null) targRole = Factories.getRoleFactory().getById(recordId, orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		if(targRole != null) return RoleServiceImpl.getListOfGroups(user, targRole);
		return new ArrayList<UserGroupType>();
	}
	

	@GET @Path("/listPersons/{orgId : [\\d]+}/{recordId : [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<PersonType> listPersons(@PathParam("orgId") long orgId,@PathParam("recordId") long recordId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		OrganizationType targOrg = null;
		PersonRoleType targRole = null;
		try {
			targOrg = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(targOrg != null) targRole = Factories.getRoleFactory().getById(recordId, orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		if(targRole != null) return RoleServiceImpl.getListOfPersons(user, targRole);
		return new ArrayList<PersonType>();
	}
	@GET @Path("/listForPerson/{orgId : [\\d]+}/{recordId : [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<PersonRoleType> listForPerson(@PathParam("orgId") long orgId,@PathParam("recordId") long recordId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		PersonType targPerson = null;
		OrganizationType targOrg = null;
		try {
			targOrg = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(targOrg != null) targPerson = Factories.getPersonFactory().getById(recordId, orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(targPerson == null){
			logger.error("Null user specified for org id " + orgId + " and user id " + recordId);
			return new ArrayList<PersonRoleType>();
		}
		return RoleServiceImpl.getListForPerson(user, targPerson);
	}
	@GET @Path("/listAccounts/{orgId : [\\d]+}/{recordId : [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<AccountType> listAccounts(@PathParam("orgId") long orgId,@PathParam("recordId") long recordId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		OrganizationType targOrg = null;
		AccountRoleType targRole = null;
		try {
			targOrg = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(targOrg != null) targRole = Factories.getRoleFactory().getById(recordId, orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		if(targRole != null) return RoleServiceImpl.getListOfAccounts(user, targRole);
		return new ArrayList<AccountType>();
	}
	@GET @Path("/listForAccount/{orgId : [\\d]+}/{recordId : [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<AccountRoleType> listForAccount(@PathParam("orgId") long orgId,@PathParam("recordId") long recordId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		AccountType targAccount = null;
		OrganizationType targOrg = null;
		try {
			targOrg = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(targOrg != null) targAccount = Factories.getAccountFactory().getById(recordId, orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(targAccount == null){
			logger.error("Null user specified for org id " + orgId + " and user id " + recordId);
			return new ArrayList<AccountRoleType>();
		}
		return RoleServiceImpl.getListForAccount(user, targAccount);
	}
	
	@GET @Path("/listUsers/{orgId : [\\d]+}/{recordId : [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<UserType> listUsers(@PathParam("orgId") long orgId,@PathParam("recordId") long recordId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		OrganizationType targOrg = null;
		UserRoleType targRole = null;
		try {
			targOrg = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(targOrg != null) targRole = Factories.getRoleFactory().getById(recordId, orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		if(targRole != null) return RoleServiceImpl.getListOfUsers(user, targRole);
		return new ArrayList<UserType>();
	}
	
	@GET @Path("/listForUser/{orgId : [\\d]+}/{recordId : [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<UserRoleType> listForUser(@PathParam("orgId") long orgId,@PathParam("recordId") long recordId,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		UserType targUser = null;
		OrganizationType targOrg = null;
		try {
			targOrg = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(targOrg != null) targUser = Factories.getUserFactory().getById(recordId, orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(targUser == null){
			logger.error("Null user specified for org id " + orgId + " and user id " + recordId);
			return new ArrayList<UserRoleType>();
		}
		return RoleServiceImpl.getListForUser(user, targUser);
	}
	/*
	@GET @Path("/list") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BaseRoleType> list(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		return RoleServiceImpl.getListInOrganization(user, user.getOrganization(),0,0);
	}
	*/
	@GET @Path("/listInParent/{orgId : [\\d]+}/{parentId : [\\d]+}/{type: [%\\sa-zA-Z_0-9\\-]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BaseRoleType> listInParent(@PathParam("orgId") long orgId,@PathParam("parentId") long parentId,@PathParam("type") String type, @PathParam("startIndex") long startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		BaseRoleType parent = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getById(orgId, 0L);
			if(org != null && parentId > 0L) parent =Factories.getRoleFactory().getById(parentId, orgId);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		/*
		if(parent == null){
			System.out.println("Null role for id " + parentId + " in org " + org);
			return new ArrayList<BaseRoleType>();
		}
		*/
		return RoleServiceImpl.getListInParent(user, type, parent, startIndex, recordCount );

	}
	/*
	@GET @Path("/listInOrganization/{orgId : [\\d]+}/{startIndex: [\\d]+}/{recordCount: [\\d]+}") @Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public List<BaseRoleType> listInOrganization(@PathParam("orgId") long orgId,@PathParam("startIndex") long startIndex,@PathParam("recordCount") int recordCount,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		BaseRoleType parent = null;
		OrganizationType org = null;
		try {
			org = Factories.getOrganizationFactory().getById(orgId, null);

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(org == null){
			return new ArrayList<BaseRoleType>();
		}
		return RoleServiceImpl.getListInOrganization(user, org, startIndex, recordCount );

	}
	*/
	 @GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
}