/* 
 * Based on https://github.com/pingidentity/scim2 
 * and https://github.com/pingidentity/scim2/blob/master/scim2-sdk-server/src/test/java/com/unboundid/scim2/server/TestResourceEndpoint.java

 * NOTE: Because AccountManager uses a very flexible concept of users, a SCIM user could be an AccountManager UserType, PersonType, or AccountType
 * At the moment, the type is fixed to a UserType.  My preference would be to use AccountType or PersonType, and I may change over to that, but then the query would need to include the context of WHERE that PersonType or AccountType resides, which would complicate the client call
*/

package org.cote.rest.scim;

import static com.unboundid.scim2.common.utils.ApiConstants.MEDIA_TYPE_SCIM;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.util.ServiceUtil;

import com.unboundid.scim2.common.ScimResource;
import com.unboundid.scim2.common.exceptions.ResourceNotFoundException;
import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.types.UserResource;
import com.unboundid.scim2.server.annotations.ResourceType;
import com.unboundid.scim2.server.utils.ResourcePreparer;
import com.unboundid.scim2.server.utils.ResourceTypeDefinition;
import com.unboundid.scim2.server.utils.SimpleSearchResults;

/**
 * A per resource life cycle Resource Endpoint implementation.
 */
@ResourceType(
    description = "User Account",
    name = "User",
    schema = ResourceService.class)
/*@Path("/v2/Users")*/
@Path("/{type:[A-Za-z]+}/{objectId:[0-9A-Za-z\\\\-]+}/v2/Users")
public class ResourceService
{
	private static final Logger logger = LogManager.getLogger(ResourceService.class);
  private static final ResourceTypeDefinition RESOURCE_TYPE_DEFINITION =
      ResourceTypeDefinition.fromJaxRsResource(
          ResourceService.class);

  /**
   * This method will simply return a poorly formated SCIM exception and
   * error response code.
   *
   * @return returns a json document that is not in the proper error response
   * format.
   */
  @POST
  @Path("badException")
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public Response getBadException()
  {
    return Response.status(Response.Status.CONFLICT).
        type(MediaType.APPLICATION_JSON).entity(
            "{\n" +
            "    \"Errors\": [\n" +
            "        {\n" +
            "            \"code\": 409, \n" +
            "            \"description\": \"Insert failed. First exception on" +
                " row 0; first error: FIELD_INTEGRITY_EXCEPTION, Salesforce " +
                "CRM Content User is not allowed for this License Type.: " +
                "__MISSING_LABEL_FOR_common.udd.impl.UddInfoImpl@4d7b688d: " +
                "[UserPermissions]\"\n" +
            "        }\n" +
            "    ]\n" +
            "}").build();
  }

  /**
   * Test SCIM search.
   *
   * @param uriInfo The UriInfo.
   * @return The results.
   * @throws ScimException if an error occurs.
   */
  @GET
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public SimpleSearchResults<UserResource> search(
	  @PathParam("type") String type, @PathParam("objectId") String objectId, 
      @Context final UriInfo uriInfo, @Context HttpServletRequest request) throws ScimException
  {
	 UserType user = ServiceUtil.getUserFromSession(request);
	AuditEnumType useType = AuditEnumType.fromValue(type);
	MultivaluedMap<String, String> params = uriInfo.getQueryParameters(true);
	int limit = 10;
	long startRecord = 0;
	if(params.containsKey("limit")) {
		limit = Integer.parseInt(params.getFirst("limit"));
	}
	if(params.containsKey("startIndex")) {
		startRecord = Long.parseLong(params.getFirst("startIndex"));
	}
    SimpleSearchResults<UserResource> results =
        new SimpleSearchResults<UserResource>(
            RESOURCE_TYPE_DEFINITION, uriInfo);
    
    List<NameIdType> objList = new ArrayList<>();

    if(!useType.equals(AuditEnumType.USER)) {
    	DirectoryGroupType dir = BaseService.readByObjectId(AuditEnumType.GROUP, objectId, user);
    	objList = BaseService.listByGroup(useType, (dir != null ? "DATA" : "UNKNOWN"), (dir != null ? dir.getObjectId() : null), startRecord, limit, user);
    }
    else {
    	logger.info("List by organization: " + user.getOrganizationId());
    	objList = BaseService.listByOrganization(useType, startRecord, limit, user);
    }
    logger.info("Listing " + useType.toString() + " : StartIndex=" + startRecord + " | RecordCount=" + limit + " | Returning=" + objList.size());
    for(NameIdType obj : objList) {
    	UserResource rec = SCIMUtil.convert(user, obj);
    	if(rec != null) results.add(rec);
    }
    

    return results;
  }

  /**
   * Test SCIM retrieve by ID.
   *
   * @param id The ID of the resource to retrieve.
   * @param uriInfo The UriInfo.
   * @return The result.
   * @throws ScimException if an error occurs.
   */
  @Path("{id}")
  @GET
  @Produces({MEDIA_TYPE_SCIM, MediaType.APPLICATION_JSON})
  public ScimResource retrieve(
		  @PathParam("type") String type, @PathParam("objectId") String objectId, @PathParam("id") final String id, @Context final UriInfo uriInfo, @Context HttpServletRequest request)
      throws ScimException
  {
    UserType user = ServiceUtil.getUserFromSession(request);
	logger.info("Processing request: " + id);
	
	AuditEnumType useType = AuditEnumType.fromValue(type);
	if(useType.equals(AuditEnumType.USER) || useType.equals(AuditEnumType.ACCOUNT) || useType.equals(AuditEnumType.PERSON)) {
		NameIdType targObj = BaseService.readByObjectId(useType, id, user);
		if(targObj != null) {
			UserResource resource = SCIMUtil.convert(user, targObj);
			ResourcePreparer<UserResource> resourcePreparer = new ResourcePreparer<UserResource>(RESOURCE_TYPE_DEFINITION, uriInfo);
			return resourcePreparer.trimRetrievedResource(resource);
		}
		throw new ResourceNotFoundException("No resource with ID " + id);
	}
	throw new ResourceNotFoundException("No resource with type " + type);

	/*
    if(id.equals("123"))
    {
      UserResource resource = new UserResource().setUserName("test");
      resource.setId("123");
      resource.setDisplayName("UserDisplayName");
      resource.setNickName("UserNickName");

      ResourcePreparer<UserResource> resourcePreparer =
          new ResourcePreparer<UserResource>(RESOURCE_TYPE_DEFINITION, uriInfo);
      return resourcePreparer.trimRetrievedResource(resource);
    }
    throw new ResourceNotFoundException("No resource with ID " + id);
    */
  }
}
