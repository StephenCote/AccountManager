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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;
import java.util.regex.Matcher;

import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.util.UrnUtil;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.junit.Test;

public class TestUrnUtil extends BaseDataAccessTest {
	@Test
	public void TestNewObjects(){
		String urn = null;
		DirectoryGroupType group = null;
		try{
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(testUser);
			group = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newDirectoryGroup(testUser,UUID.randomUUID().toString(),testUser.getHomeDirectory(), testUser.getOrganizationId());
			urn = UrnUtil.getUrn(group);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		}
		logger.info("Urn=" + urn);
		assertNotNull("Urn is null for new object",urn);
		
	}
	@Test
	public void TestPatterns(){
		String orgUrn = "am:organization:Public";
		String orgUrn2 = orgUrn + ":foo";
		Matcher m1 = UrnUtil.factoryPattern.matcher(orgUrn);
		Matcher m2 = UrnUtil.factoryPattern.matcher(orgUrn2);
		assertTrue("Failed to find urn #1",m1.find());
		assertTrue("Failed to find groups in urn #1",m1.groupCount() > 0);
		assertTrue("Failed to find urn #2",m2.find());
		assertTrue("Failed to find groups in urn #2",m2.groupCount() > 0);
	}
	
	/*
	@Test
	public void TestDeconstructOrgUrn(){
		String orgUrn = "am:organization:Public";
		String organizationPath = UrnUtil.extractOrganizationPath(orgUrn);
		assertNotNull("Org path is null",organizationPath);
		logger.info("Extracted path " + organizationPath);
		OrganizationType org = null;
		try {
			org = UrnUtil.getOrganization(orgUrn);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertNotNull("Org is null for path " + organizationPath,org);
		
		String testUrn = UrnUtil.getUrn(org);
		
		assertNotNull("Test urn is null",testUrn);
		logger.info("Test urn: " + testUrn);
		assertTrue("Urns do not match",orgUrn.equals(testUrn));
	}
	*/
	@Test
	public void TestDeconstructDataGroupUrn(){
		try {
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(testUser);
			/// force an update on the test group while migrating to include the new urn column, because the values for existing entries will be auto values
			///
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).update(testUser.getHomeDirectory());

		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		DirectoryGroupType dir = testUser.getHomeDirectory();

		String testUrn = UrnUtil.getUrn(dir);
		logger.info(testUrn);
		assertNotNull("Group urn is null", testUrn);
		DirectoryGroupType tdir = null;
		//OrganizationType org = null;

			/*
			org = UrnUtil.getOrganization(testUrn);
			assertNotNull("Org was null from test urn " + testUrn,org);
			*/
			tdir = UrnUtil.getByUrn(testUrn);
	
		assertNotNull("Group is null for urn " + testUrn,tdir);
		assertTrue("Group ids " + tdir.getId() + " and " + dir.getId() + " don't match",tdir.getId().equals(dir.getId()));
		logger.info("Found group from urn " + testUrn);
	}
	
}
