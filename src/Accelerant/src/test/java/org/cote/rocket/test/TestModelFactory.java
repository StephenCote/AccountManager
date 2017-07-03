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
import org.cote.propellant.objects.ModelType;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.ModelFactory;
import org.junit.Test;
public class TestModelFactory extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestModelFactory.class);
	private static String sessionId = null;
	private static String ModelName = null;
	private static long ModelId = 0;
	
	
	
	@Test
	public void testCreateModel(){
		boolean added = false;
		assertTrue("User is not populated", testUser.getPopulated());
		try{
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			ModelType obj = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).newModel(testUser, dir.getId());
			ModelName = UUID.randomUUID().toString();
			obj.setName(ModelName);
			obj.setDescription("Example Model");
			added = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).add(obj);
			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		}
		assertTrue("Model was not added", added);
	}
	@Test
	public void testGetModelByName(){
		ModelType Model = null;
		DirectoryGroupType dir = null;
		try{
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "Rocket", testUser.getHomeDirectory(), testUser.getOrganizationId());
			Model = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).getByNameInGroup(ModelName, dir);
			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertNotNull("Model '" + ModelName + "' in group '" + dir.getId() + "' was not found", Model);
		ModelId = Model.getId();
	}
	@Test
	public void testGetModelById(){
		ModelType Model = null;
		try{
			Model = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).getById(ModelId, testUser.getOrganizationId());
			
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertNotNull("Model '" + ModelId + "' was not found", Model);

	}
	@Test
	public void testUpdateModelById(){
		ModelType Model = null;
		boolean updated = false;
		try{
			Model = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).getById(ModelId, testUser.getOrganizationId());
			Model.setDescription("New description");
			updated = ((ModelFactory)Factories.getFactory(FactoryEnumType.MODEL)).update(Model);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		assertTrue("Model was not updated",updated);

	}
}