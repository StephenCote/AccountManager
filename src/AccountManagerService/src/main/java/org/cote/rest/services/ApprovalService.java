package org.cote.rest.services;

import java.util.ArrayList;
import java.util.List;

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
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.RequestFactory;
import org.cote.accountmanager.data.services.AuthorizedSearchService;
import org.cote.accountmanager.data.services.RequestService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccessRequestType;
import org.cote.accountmanager.objects.ApprovalResponseEnumType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ObjectSearchRequestType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RequestSearchRequestType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.service.util.ServiceUtil;

@DeclareRoles({"admin","user"})
@Path("/approval")
public class ApprovalService {
	
	private static SchemaBean schemaBean = null;
	@Context
	ServletContext context;
	
	@Context
	SecurityContext securityCtx;
	
	private static final Logger logger = LogManager.getLogger(ApprovalService.class);

	@GET @Path("/smd") @Produces(MediaType.APPLICATION_JSON)
	 public SchemaBean getSmdSchema(@Context UriInfo uri){
		 if(schemaBean != null) return schemaBean;
		 schemaBean = ServiceSchemaBuilder.modelRESTService(this.getClass(),uri.getAbsolutePath().getRawPath().replaceAll("/smd$", ""));
		 return schemaBean;
	 }
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/requests/open")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response listOpenAccessApprovalRequests(@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		List<AccessRequestType> reqs = new ArrayList<>();
		try {
			reqs = RequestService.listOpenAccessRequests(user);
		} catch (FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
		}
		return Response.status(200).entity(reqs).build();	
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/requestable/{type:[A-Za-z]+}/{objectId:[0-9A-Za-z\\\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response isRequestable(@PathParam("type") String type, @PathParam("objectId") String objectId, @PathParam("policyId") String policyId, @Context HttpServletRequest request){
		boolean isReq = false;
		NameIdType obj = BaseService.readByObjectId(AuditEnumType.valueOf(type), objectId, request);
		if(obj != null) {
			try {
				isReq = RequestService.isRequestable(obj);
				logger.info(obj.getUrn() + " is " + (isReq ? "" : "NOT") + " requestable");
			} catch (FactoryException | ArgumentException e) {
				logger.error(e.getMessage());
			}
		}
		return Response.status(200).entity(isReq).build();	
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/policy/attach/{type:[A-Za-z]+}/{objectId:[0-9A-Za-z\\\\-]+}/{policyId:[0-9A-Za-z\\\\\\\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response attachPolicyControl(@PathParam("type") String type, @PathParam("objectId") String objectId, @PathParam("policyId") String policyId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		NameIdType obj = BaseService.readByObjectId(AuditEnumType.valueOf(type), objectId, request);
		PolicyType policy = null;
		boolean attached = false;
		try {
			policy = ((INameIdFactory)Factories.getFactory(FactoryEnumType.POLICY)).getByObjectId(policyId, user.getOrganizationId());
			if(BaseService.canChangeType(AuditEnumType.valueOf(type), user, obj) && policy != null && obj != null) {
				attached = org.cote.accountmanager.data.services.PolicyService.attachPolicyControl(user, obj, policy);
			}
			else {
				logger.error("User not authorized to change object, or policy or object were null");
			}
		} catch (FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
		}
		return Response.status(200).entity(attached).build();	
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/requests/{type:[A-Za-z]+}/{objectId:[0-9A-Za-z\\\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response listApprovalRequestsByType(@PathParam("type") String type, @PathParam("objectId") String objectId, @Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		NameIdType obj = BaseService.readByObjectId(AuditEnumType.valueOf(type), objectId, request);

		List<AccessRequestType> reqs = new ArrayList<>();
		try {
			reqs = RequestService.getAccessRequests(obj, user);
		} catch (FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
		}
		return Response.status(200).entity(reqs).build();	
	}
	
	
	@RolesAllowed({"user"})
	@POST
	@Path("/requests/count")
	@Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public int countBy(RequestSearchRequestType searchRequest,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		NameIdType owner = null;
		NameIdType requestor = null;
		NameIdType delegate = null;
		NameIdType target = null;
		NameIdType entitlement = null;
		boolean readError = false;
		if(searchRequest.getOwnerId() != null) {
			owner = BaseService.readByObjectId(AuditEnumType.USER, searchRequest.getOwnerId(), user);
			if(owner == null) {
				logger.warn("Failed to read USER " + searchRequest.getOwnerId());
				readError = true;
			}
		}
		if(searchRequest.getRequestorId() != null && !searchRequest.getRequestorType().equals(NameEnumType.UNKNOWN)) {
			requestor = BaseService.readByObjectId(AuditEnumType.valueOf(searchRequest.getRequestorType().toString()), searchRequest.getRequestorId(), user);
			if(requestor == null) {
				logger.warn("Failed to read " + searchRequest.getRequestorType().toString() + " " + searchRequest.getRequestorId());
				readError = true;
			}
		}
		if(searchRequest.getDelegateId() != null && !searchRequest.getDelegateType().equals(NameEnumType.UNKNOWN)) {
			delegate = BaseService.readByObjectId(AuditEnumType.valueOf(searchRequest.getDelegateType().toString()), searchRequest.getDelegateId(), user);
			if(delegate == null) {
				logger.warn("Failed to read " + searchRequest.getDelegateType().toString() + " " + searchRequest.getDelegateId());
				readError = true;
			}
		}
		if(searchRequest.getTargetId() != null && !searchRequest.getTargetType().equals(NameEnumType.UNKNOWN)) {
			target = BaseService.readByObjectId(AuditEnumType.valueOf(searchRequest.getTargetType().toString()), searchRequest.getTargetId(), user);
			if(target == null) {
				logger.warn("Failed to read " + searchRequest.getTargetType().toString() + " " + searchRequest.getTargetId());
				readError = true;
			}
		}
		if(searchRequest.getEntitlementId() != null && !searchRequest.getEntitlementType().equals(NameEnumType.UNKNOWN)) {
			entitlement = BaseService.readByObjectId(AuditEnumType.valueOf(searchRequest.getEntitlementType().toString()), searchRequest.getEntitlementId(), user);
			if(entitlement == null) {
				logger.warn("Failed to read " + searchRequest.getEntitlementType().toString() + " " + searchRequest.getEntitlementId());
				readError = true;
			}
		}
		int count = 0;
		if(readError == false) {
			try {
				RequestFactory rFact = Factories.getFactory(FactoryEnumType.REQUEST);
				count = rFact.countAccessRequestsForType(requestor, delegate, target, entitlement, searchRequest.getApprovalResponse(), searchRequest.getRequestParentId(), user.getOrganizationId());
			} catch (FactoryException | ArgumentException e) {
				logger.error(e);
			}
		}
		
		logger.info("Returning " + count + " count");
		return count;
	}
	
	@RolesAllowed({"user"})
	@POST
	@Path("/requests/list")
	@Produces(MediaType.APPLICATION_JSON) @Consumes(MediaType.APPLICATION_JSON)
	public Response listBy(RequestSearchRequestType searchRequest,@Context HttpServletRequest request){
		UserType user = ServiceUtil.getUserFromSession(request);
		NameIdType owner = null;
		NameIdType requestor = null;
		NameIdType delegate = null;
		NameIdType target = null;
		NameIdType entitlement = null;
		boolean readError = false;
		if(searchRequest.getOwnerId() != null) {
			owner = BaseService.readByObjectId(AuditEnumType.USER, searchRequest.getOwnerId(), user);
			if(owner == null) {
				logger.warn("Failed to read USER " + searchRequest.getOwnerId());
				readError = true;
			}
		}
		if(searchRequest.getRequestorId() != null && !searchRequest.getRequestorType().equals(NameEnumType.UNKNOWN)) {
			requestor = BaseService.readByObjectId(AuditEnumType.valueOf(searchRequest.getRequestorType().toString()), searchRequest.getRequestorId(), user);
			if(requestor == null) {
				logger.warn("Failed to read " + searchRequest.getRequestorType().toString() + " " + searchRequest.getRequestorId());
				readError = true;
			}
		}
		if(searchRequest.getDelegateId() != null && !searchRequest.getDelegateType().equals(NameEnumType.UNKNOWN)) {
			delegate = BaseService.readByObjectId(AuditEnumType.valueOf(searchRequest.getDelegateType().toString()), searchRequest.getDelegateId(), user);
			if(delegate == null) {
				logger.warn("Failed to read " + searchRequest.getDelegateType().toString() + " " + searchRequest.getDelegateId());
				readError = true;
			}
		}
		if(searchRequest.getTargetId() != null && !searchRequest.getTargetType().equals(NameEnumType.UNKNOWN)) {
			target = BaseService.readByObjectId(AuditEnumType.valueOf(searchRequest.getTargetType().toString()), searchRequest.getTargetId(), user);
			if(target == null) {
				logger.warn("Failed to read " + searchRequest.getTargetType().toString() + " " + searchRequest.getTargetId());
				readError = true;
			}
		}
		if(searchRequest.getEntitlementId() != null && !searchRequest.getEntitlementType().equals(NameEnumType.UNKNOWN)) {
			entitlement = BaseService.readByObjectId(AuditEnumType.valueOf(searchRequest.getEntitlementType().toString()), searchRequest.getEntitlementId(), user);
			if(entitlement == null) {
				logger.warn("Failed to read " + searchRequest.getEntitlementType().toString() + " " + searchRequest.getEntitlementId());
				readError = true;
			}
		}
		List<Object> list = new ArrayList<>();
		if(readError == false) {
			try {
				RequestFactory rFact = Factories.getFactory(FactoryEnumType.REQUEST);
				list = FactoryBase.convertList(rFact.getAccessRequestsForType(requestor, delegate, target, entitlement, searchRequest.getApprovalResponse(), searchRequest.getRequestParentId(), searchRequest.getStartRecord(), searchRequest.getRecordCount(), user.getOrganizationId()));
			} catch (FactoryException | ArgumentException e) {
				logger.error(e);
			}
		}
		
		logger.info("Returning " + list.size() + " items");
		return Response.status(200).entity(list).build();
	}
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/request/policies/{requestId:[0-9A-Za-z\\\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response listRequestPolicies(@PathParam("requestId") String requestId, @Context HttpServletRequest request){

		UserType user = ServiceUtil.getUserFromSession(request);
		AccessRequestType art = null;
		
		try {
			art =((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST)).getByObjectId(requestId, user.getOrganizationId());
		} catch (FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
		}
		
		List<PolicyType> pols = org.cote.accountmanager.data.services.PolicyService.getRequestPolicies(art);
		return Response.status(200).entity(pols).build();	
	}
	
	
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/policy/owner")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getOwnerApprovalPolicy(@Context HttpServletRequest request){
		PolicyType pol = null;

		UserType user = ServiceUtil.getUserFromSession(request);
		if(user != null) {
			pol = org.cote.accountmanager.data.services.PolicyService.getOwnerApprovalPolicy(user.getOrganizationId());
		}
		return Response.status(200).entity(pol).build();	
	}
	
	
	
	/*
	@RolesAllowed({"admin","user"})
	@GET
	@Path("/policy/owner")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getOwnerApprovalPolicy(@Context HttpServletRequest request){
		PolicyType pol = null;
		UserType user = ServiceUtil.getUserFromSession(request);
		if(user != null) {
			pol = getOwnerApprovalPolicy(user);
		}
		return Response.status(200).entity(pol).build();	
	}
	*/

}