package org.cote.accountmanager.client.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.cote.accountmanager.client.CommunityContext;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;
import org.junit.Test;

import com.unboundid.scim2.client.ScimService;
import com.unboundid.scim2.common.exceptions.ScimException;
import com.unboundid.scim2.common.types.UserResource;

public class TestSCIM extends BaseClientTest {


	
	@Test
	public void TestSCIMPersonContact() {
		logger.info("Testing SCIM Contact with AM5 service");

		CommunityContext cc = prepareCommunityContext();
		assertNotNull("Community context is null", cc);
		DirectoryGroupType personsDir = cc.getProjectDirectory(testProjectName, "Persons");
		assertNotNull("Persons dir is null", personsDir);
		PersonType per = cc.getCreatePerson(testProjectName, testPerson1Name);
		assertNotNull("Person is null", per);
		/// Person 'testPerson1Name' should exist in the target system
		/// Now test that it's retrievable through the SCIM api
		/// In the following path, the scope doesn't matter when using the objectid
		/// The type does matter
		///
		Client client = ClientBuilder.newClient().register(OAuth2ClientSupport.feature(new String(testAdminContext.getAuthenticationCredential().getCredential())));;
		
		WebTarget target = client.target("http://localhost:8080/AccountManagerService/scim/PERSON/" + personsDir.getObjectId() + "/v2");
		ScimService scimService = new ScimService(target);
		
		UserResource user = null;
		try {
			user = scimService.retrieve("Users", per.getObjectId(), UserResource.class);
			logger.info("Received: " + user);
		} catch (ScimException e) {
			logger.error(e);
		}

		/// Switch over to test for the ACCOUNT, which is scoped to an Application below and adjacent to the PersonType scope within the community project
		/// For application accounts, these are scoped at the Application's group level, versus accounts that may be created under any specifically named ./Accounts group
		DirectoryGroupType appDir = cc.getApplication(testProjectName, testApplication1Name);
		assertNotNull("Application dir is null", appDir);
		AccountType account = cc.getCreateAccount(testProjectName, testApplication1Name, testAccount1Name);
		assertNotNull("Account " + testAccount1Name + " is null",account);
		
		target = client.target("http://localhost:8080/AccountManagerService/scim/ACCOUNT/" + appDir.getObjectId() + "/v2");
		scimService = new ScimService(target);
		
		/// NOTE:
		///		The PERSON shows the group as an entitlement because it picked it up from the dependent account having that group
		///		this is by design within the account manager entitlement engine, and how I wrote the SCIM utilit to unravel all inherited entitlements
		///		however, it doesn't show up in the groups list because the PERSON didn't directly have the group.
		///		Same condition will exist with roles
		///		The ACCOUNT shows the group in the groups and entitlements list because it directly has the group
		///		I didn't bother filtering the group off the entitlements, and need to dig around as to the expectation of showing an inherited group or role in those fields because it would imply it's directly set, when it wasn't, otherwise I could filter the directly applied roles and groups off the effective entitlement list
		
		user = null;
		try {
			user = scimService.retrieve("Users", account.getObjectId(), UserResource.class);
			logger.info("Received: " + user);
		} catch (ScimException e) {
			logger.error(e);
		}
		
		
		client.close();
		
		
		
		
	}
	

	
}
