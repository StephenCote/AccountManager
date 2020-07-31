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
package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserPermissionType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.service.util.ServiceUtil;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.ServletRequestMock;
import org.junit.Test;

public class TestBaseService extends BaseDataAccessTest{

	private ServletRequestMock getRequest(String sessionId,long organizationId){
		return new ServletRequestMock(null,sessionId,organizationId);
	}
	
	@Test
	public void TestServiceAccess(){
		ServletRequestMock request = getRequest(sessionId,testUser.getOrganizationId());
		UserType user = ServiceUtil.getUserFromSession(request);
		assertNotNull("Failed to get user from session " + sessionId + ". User has session " + testUser.getSession().getSessionId(),user);
		logger.info("Obtained user " + user.getName() + " from session " + ServiceUtil.getSessionId(request));
		//BaseGroupType group = BaseService.readByName(type, name, request)
	}
	
	@Test
	public void TestGroupAccess(){
		ServletRequestMock request = new ServletRequestMock(null,this.sessionId,testUser.getOrganizationId());
		DirectoryGroupType homeDir = null;
		try {
			homeDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getUserDirectory(testUser);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertNotNull("Home directory is null",homeDir);
		DirectoryGroupType dir = BaseService.readById(AuditEnumType.GROUP, homeDir.getId(), request);
		assertNotNull("Directory is null",dir);
		//BaseGroupType group = BaseService.readByName(type, name, request)
		
		assertNotNull("Org Path is null",dir.getOrganizationPath());
		logger.info("Org Path: " + dir.getOrganizationPath());
		assertNotNull("Group Path is null",dir.getPath());
		
		logger.info("Group Path: " + dir.getPath());
	}
	
	@Test
	public void TestDataAccess(){
		DataType data = null;
		DirectoryGroupType dir = null;
		String dataName = "TestData-" + UUID.randomUUID().toString();
		ServletRequestMock request = new ServletRequestMock(null,this.sessionId,testUser.getOrganizationId());
		boolean add = false;
		dir = BaseService.findGroup(GroupEnumType.DATA, "~/Data", request);
		assertNotNull("User data directory is null",dir);
		data = JSONUtil.importObject(getSampleDataString(dataName), DataType.class);
		add = BaseService.add(AuditEnumType.DATA, data, request);
		assertTrue("Failed to add new data",add);
		data = BaseService.readByName(AuditEnumType.DATA, dir.getId(), dataName, request);
		assertNotNull("Data is null",data);
		assertNotNull("Org path is null",data.getOrganizationPath());
		assertNotNull("Group path is null",data.getGroupPath());
		//logger.info(JSONUtil.exportObject(data));
	}
	
	@Test
	public void TestRoleAccess(){
		
		
		try {
			UserRoleType role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(testUser,RoleEnumType.USER,testUser.getOrganizationId());
			assertNotNull("Role is null",role);
			((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).denormalize(role);
			assertNotNull("Path is null",role.getParentPath());
			logger.info("Role parent path = '" + role.getParentPath() + "'");
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	
	@Test
	public void TestPermissionAccess(){
		
		
		try {
			UserPermissionType permission = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getUserPermission(testUser,PermissionEnumType.USER,testUser.getOrganizationId());
			assertNotNull("Permission is null",permission);
			((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).denormalize(permission);
			assertNotNull("Path is null",permission.getParentPath());
			logger.info("Permission parent path = '" + permission.getParentPath() + "'");
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
	}
	
	
	private String getSampleDataString(String name){
		return 
		"{"
		+"	  \"attributes\" : [],\n"
		+"	  \"name\" : \"" + name + "\",\n"
		+"	  \"nameType\" : \"DATA\",\n"
		//  + "\"id\" : 15854,
		//  + "\"ownerId\" : 356,
		//  + "\"populated\" : true,
		//  + "\"urn\" : \"am:data:development:data:home.rocketqauser.data:vgvzderhdgetntc3nwjjodctmjflzc00yzc3lwfjmmutndgxntq4yzk2mmfj\",\n"
		//  + "\"organizationId\" : 69,
		  + "\"organizationPath\" : \"/Development\",\n"
		//  + "\"groupId\" : 10647,
		  + "\"groupPath\" : \"/Home/RocketQAUser/Data\",\n"
		  + "\"dataBytesStore\" : \"RGVtbyBkZW1vIGRlbW8=\",\n"
		  + "\"mimeType\" : \"text/plain\",\n"
		  + "\"blob\" : \"true\"\n"
		+"	}";
	}
	
}
