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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Map;

import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.GroupService;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.junit.Test;
public class TestEffectiveAuthorization extends BaseDataAccessTest {
	
	@Test
	public void TestAccountDataAuthorization(){
		AccountGroupType acctGrp1 = null;
		AccountRoleType acctRole1 = null;
		PersonRoleType perRole1 = null;
		DirectoryGroupType app1 = null;
		BaseRoleType baseRole = null;
		try{
			Factories.coolDown();
			Factories.warmUp();
			
			Map<FactoryEnumType, FactoryEnumType> factories = AuthorizationService.getAuthorizationFactories();
			assertTrue("Unexpected number of registered authorization providers: " + factories.size(), factories.size() >= 5);
			
			app1 = getApplication("AuthZ Application #1");
			acctGrp1 = getGroup(testUser,"Account Group 1",GroupEnumType.ACCOUNT,app1);
			
		
			baseRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getUserRole(testUser,RoleEnumType.USER,testUser.getOrganizationId());
			assertNotNull("Base role is null", baseRole);
			acctRole1 = getRole(testUser,"Account Role 1",RoleEnumType.ACCOUNT,baseRole);
			perRole1 = getRole(testUser,"Person Role 1",RoleEnumType.PERSON,baseRole);
		} catch (FactoryException | ArgumentException e2) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e2);
		}
		
		assertNotNull("Group is null", acctGrp1);
		assertNotNull("Role is null",acctRole1);
		
		PersonType person1 = getApplicationPerson("Person #1",app1);
		PersonType person2 = getApplicationPerson("Person #2",app1);
		PersonType person3 = getApplicationPerson("Person #3",app1);
		AccountType account1 = getApplicationAccount("Account #1",app1);
		AccountType account2 = getApplicationAccount("Account #2",app1);
		AccountType account3 = getApplicationAccount("Account #3",app1);
		AccountType account4 = getApplicationAccount("Account #4",app1);
		
		if(person1.getAccounts().isEmpty()){
			person1.getAccounts().add(account1);
			try {
				((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).update(person1);
			} catch (FactoryException  e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		if(person2.getAccounts().isEmpty()){
			person2.getAccounts().add(account2);
			try {
				((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).update(person2);
			} catch (FactoryException  e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		DataType data = newTextData("Data 1","This is the text data",testUser,app1);
		DataType data2 = newTextData("Data 2","This is the text data",testUser,app1);
		DataType data3 = newTextData("Data 3","This is the text data",testUser,app1);
		
		try {
			if(GroupService.getIsAccountInGroup(acctGrp1, account4) == false) GroupService.addAccountToGroup(account4, acctGrp1);
			
			logger.info("Cleaning test objects");
			AuthorizationService.deauthorize(testUser, app1);
			AuthorizationService.deauthorize(testUser, data);
			logger.info("PASS #1: Test direct user auth");
			testGenericAuthorization(testUser,data,testUser2,testUser2,AuthorizationService.getViewPermissionForMapType(data.getNameType(), data.getOrganizationId()));
			logger.info("PASS #2: Test direct account auth");
			testGenericAuthorization(testUser,data,account1,account1,AuthorizationService.getViewPermissionForMapType(data.getNameType(), data.getOrganizationId()));
			logger.info("PASS #3: Test account auth via person");
			testGenericAuthorization(testUser,data,account2,person2,AuthorizationService.getViewPermissionForMapType(data.getNameType(), data.getOrganizationId()));
			logger.info("PASS #4: Test account view group");
			testGenericAuthorization(testUser,app1,account3,account3,AuthorizationService.getViewPermissionForMapType(app1.getNameType(), app1.getOrganizationId()));
			logger.info("PASS #5: Test account group view data");
			testGenericAuthorization(testUser,data2,acctGrp1,account4,AuthorizationService.getViewPermissionForMapType(data2.getNameType(), data2.getOrganizationId()));
		} catch (FactoryException | ArgumentException | DataAccessException e1) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e1);
		}

		
	}
	
	private void testGenericAuthorization(UserType admin, NameIdType object, NameIdType setMember, NameIdType checkMember, BasePermissionType permission){
		boolean setAuthZ = false;
		boolean isAuthZ = false;
		boolean notAuthZ = false;
		boolean deAuthZ = false;
		assertNotNull("Object is null",object);
		assertNotNull("Set Member is null", setMember);
		assertNotNull("Check member is null", checkMember);
		assertNotNull("Permission for " + object.getUrn() + " is null",permission);
		String authZStr = EffectiveAuthorizationService.getEntitlementCheckString(object, checkMember, new BasePermissionType[]{permission});
		logger.info("TEST AUTHORIZATION " + authZStr);
		try {
			
			//assertTrue("Owner cannot change permission",AuthorizationService.isAuthorized(permission, admin, new BasePermissionType[]{AuthorizationService.getViewPermissionForMapType(permission.getNameType(), permission.getOrganizationId())}));
			setAuthZ = AuthorizationService.authorize(admin, setMember, object, AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId()), true);
			isAuthZ = AuthorizationService.isAuthorized(checkMember, object, AuthorizationService.PERMISSION_VIEW, new BasePermissionType[]{AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId())});
			deAuthZ = AuthorizationService.authorize(admin, setMember, object, AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId()), false);
			notAuthZ = AuthorizationService.isAuthorized(checkMember,object, AuthorizationService.PERMISSION_VIEW, new BasePermissionType[]{AuthorizationService.getViewPermissionForMapType(object.getNameType(), object.getOrganizationId())});
		} catch (NullPointerException | FactoryException | DataAccessException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		assertTrue("Failed to set: " + authZStr,setAuthZ);
		assertTrue("Authorization check failed: " + authZStr,isAuthZ); 
		assertTrue("Failed to deauthorize: " + object.getUrn(),deAuthZ); 
		assertFalse("Succeeded when failured expected: " + authZStr,notAuthZ); 
	}
}