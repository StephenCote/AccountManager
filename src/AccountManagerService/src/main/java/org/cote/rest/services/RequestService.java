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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.RequestFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.policy.PolicyEvaluator;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.AccessRequestType;
import org.cote.accountmanager.objects.ApprovalResponseEnumType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PolicyRequestType;
import org.cote.accountmanager.objects.PolicyResponseEnumType;
import org.cote.accountmanager.objects.PolicyResponseType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.util.ServiceUtil;

@DeclareRoles({"admin","user"})
@Path("/request")
public class RequestService {
	private static final Logger logger = LogManager.getLogger(RequestService.class);

	@RolesAllowed({"user"})
	@GET
	@Path("/list/{type:[A-Za-z]+}/{objectId:[0-9A-Za-z\\-]+}/{startIndex:[\\d]+}/{count:[\\d]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response listForType(@PathParam("type") String type, @PathParam("objectId") String id, @PathParam("startIndex") long startIndex, @PathParam("count") int count, @Context HttpServletRequest request){
		List<AccessRequestType> ctls = new ArrayList<>();
		UserType user = ServiceUtil.getUserFromSession(request);
		NameIdType object = BaseService.readByObjectId(AuditEnumType.valueOf(type), id, user);
		if(object != null) {
			try {
				if(org.cote.accountmanager.data.services.RequestService.canViewMemberRequests(user, object)) {
					ctls = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST)).getAccessRequestsForType(object, null, null, null, ApprovalResponseEnumType.UNKNOWN, 0L, startIndex, count, user.getOrganizationId());
				}
				else {
					logger.warn("User " + user.getUrn() + " is not authorized to view member requests for " + object.getUrn());
				}
			} catch (FactoryException | ArgumentException e) {
				logger.error(e.getMessage());
				logger.error(e);
			}
		}
		else {
			logger.error("Object " + type + " " + id + " was null or not accessible");
		}
		return Response.status(200).entity(ctls).build();
	}

	@RolesAllowed({"user"})
	@GET
	@Path("/count/{type:[A-Za-z]+}/{objectId:[0-9A-Za-z\\-]+}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response countForType(@PathParam("type") String type, @PathParam("objectId") String id, @Context HttpServletRequest request){
		int count = 0;
		UserType user = ServiceUtil.getUserFromSession(request);
		NameIdType object = BaseService.readByObjectId(AuditEnumType.valueOf(type), id, user);
		
		if(object != null) {

				try {
					if(org.cote.accountmanager.data.services.RequestService.canViewMemberRequests(user, object)) {
						count = ((RequestFactory)Factories.getFactory(FactoryEnumType.REQUEST)).countAccessRequestsForType(object, null, null, null, ApprovalResponseEnumType.UNKNOWN, 0L, user.getOrganizationId());
					}
					else {
						logger.warn("User " + user.getUrn() + " is not authorized to view member requests for " + object.getUrn());
					}
				} catch (FactoryException | ArgumentException e) {
					logger.error(e.getMessage());
					logger.error(e);
				}

		}
		else {
			logger.error("Object " + type + " " + id + " was null or not accessible");
		}
		return Response.status(200).entity(count).build();
	}
	
}
