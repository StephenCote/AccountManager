/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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

public class TestGroupAuthorization extends BaseDataAccessTest {
	/*
	 * This test largely depends on a pre-populated dataset that isn't being setup in the test
	@Test
	public void testEncryptedData(){
		OrganizationType org = null;
		UserType user = null;
		UserType user2 = null;
		try {
			org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/Accelerant/Rocket");
			assertNotNull("Organization is null");
			user = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("TestUser1", org.getId());
			user2 = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("TestUser2", org.getId());
			assertNotNull("User 1 is null",user);
			assertNotNull("User 2 is null",user2);
			
			List<BasePermissionType> permissions = new ArrayList<BasePermissionType>();
			permissions.add(AuthorizationService.getViewGroupPermission(org.getId()));
			List<Long> ids = AuthorizationService.getAuthorizedGroups(user, permissions.toArray(new BasePermissionType[0]), org.getId());
			assertTrue("Invalid group count",ids.size() > 0);
			List<Long> ids2 = AuthorizationService.getAuthorizedGroups(user2, permissions.toArray(new BasePermissionType[0]), org.getId());
			assertTrue("Invalid group count",ids2.size() > 0);
			assertTrue("Unexpected group count.  User 1 has more rights than User 2, so should have access to more groups",ids.size() != ids2.size());
			
			logger.info("Id count #1 = " + ids.size());
			logger.info("Id count #2 = " + ids2.size());
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		
		
		
		
	}
	*/
}
