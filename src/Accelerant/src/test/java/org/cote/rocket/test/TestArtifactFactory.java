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
package org.cote.rocket.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.propellant.objects.ArtifactType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.ArtifactFactory;
import org.junit.Test;
public class TestArtifactFactory extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestArtifactFactory.class);
	private static String testUserName = "RocketQAUser";
	private static String artifactName = null;
	private static long artifactId = 0;
	

	
	@Test
	public void testCreateArtifact(){
		boolean added = false;
		assertTrue("User is not populated", testUser.getPopulated());
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			ArtifactType obj = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).newArtifact(testUser, dir.getId());
			artifactName = UUID.randomUUID().toString();
			obj.setName(artifactName);
			obj.setDescription("Example Artifact");
			added = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).add(obj);
			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		}
		assertTrue("Artifact was not added", added);
	}
	@Test
	public void testGetArtifactByName(){
		ArtifactType Artifact = null;
		DirectoryGroupType dir = null;
		try{
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			Artifact = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).getByNameInGroup(artifactName, dir);
			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertNotNull("Artifact '" + artifactName + "' in group '" + dir.getId() + "' was not found", Artifact);
		artifactId = Artifact.getId();
	}
	@Test
	public void testGetArtifactById(){
		ArtifactType Artifact = null;
		try{
			Artifact = ((ArtifactFactory)Factories.getFactory(FactoryEnumType.ARTIFACT)).getById(artifactId, testUser.getOrganizationId());
			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertNotNull("Artifact '" + artifactId + "' was not found", Artifact);

	}
}