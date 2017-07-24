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
package org.cote.rocket.test;

import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.rocket.Factories;
import org.cote.rocket.Rocket;
import org.cote.rocket.util.ProjectImportUtil;
import org.junit.Test;

public class TestBulkFactory extends BaseAccelerantTest{
	public static final Logger logger = LogManager.getLogger(TestBulkFactory.class);
/*	
	@Test
	public void TestCreateLifecycle(){
		LifecycleType lc = null;
		try {
			lc = Rocket.createLifecycle(testUser, UUID.randomUUID().toString());
			assertNotNull("Lifecycle is null",lc);
			assertTrue("Lifecycle not created correrctly",lc.getId() > 0);
			logger.info("Lifecycle Id = " + lc.getId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
		
	}
	*/
	
	@Test
	public void TestImportProject(){
		logger.info("Test import project");
		LifecycleType lc = null;
		try {
			lc = Rocket.getLifecycle("UnitTestLifecycle", testUser.getOrganizationId());
			if(lc != null){
				Rocket.deleteLifecycle(lc);
				lc = null;
				Factories.cleanupOrphans();
			}
			if(lc == null){
				lc = Rocket.createLifecycle(testUser, "UnitTestLifecycle");
			}
			long start = System.currentTimeMillis();
			ProjectType proj = Rocket.createProject(testUser, lc, UUID.randomUUID().toString());
			long stop = System.currentTimeMillis();
			assertNotNull("Project is null",proj);
			logger.info("Project created in " + (stop - start) + "ms");
			start = System.currentTimeMillis();
			boolean imported = ProjectImportUtil.importProject(testUser,proj,"./test/test.mpp",false);
			stop = System.currentTimeMillis();
			logger.info("Project imported in " + (stop - start) + "ms");
			
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}

	}
	
	private LifecycleType getTestLifecycle(String name){
		LifecycleType lc = null;
		try {
			lc = Rocket.getLifecycle(name, testUser.getOrganizationId());
			if(lc == null) lc = Rocket.createLifecycle(testUser,name);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
		
		return lc;
	}
	
	/*
	
	@Test
	public void TestCreateLifecycleProject(){
		LifecycleType lc = getTestLifecycle("LifeCycleUnitTest5");
		assertNotNull("Lifecycle is null",lc);
		UserRoleType role = RocketSecurity.getLifecycleRoleBucket(lc);
		assertNotNull("Lifecycle Role Bucket is null",role);
		ProjectType proj = null;

		try {
			proj = Rocket.createProject(testUser, lc,UUID.randomUUID().toString());
			assertNotNull("Project is null",proj);
			assertTrue("Project not created correctly",proj.getId() > 0);
			logger.info("Project Id = " + proj.getId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
		

	}
	*/
	
	/*
	@Test
	public void TestBulkLifecycle(){
		
		try{
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(testUser);
			DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(testUser, "BulkLifecycle", testUser.getHomeDirectory(), testUser.getOrganizationId());
			
			LifecycleType lc = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).newLifecycle(testUser, dir);
			lc.setName(UUID.randomUUID().toString());
			lc.setDescription(UUID.randomUUID().toString());
			assertTrue("Failed to add test stand-alone lifecycle",((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).addLifecycle(lc));

			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			long start = System.currentTimeMillis();
			//Map<String,Long> bulkNameId = new HashMap<String,Long>();
			for(int i = 0; i < 10; i++){
				lc = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).newLifecycle(testUser, dir);
				lc.setName(UUID.randomUUID().toString());
				lc.setDescription(UUID.randomUUID().toString());
				
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LIFECYCLE, lc);
				assertTrue("Lifecycle id (" + lc.getId() + " not set to bulk entry",lc.getId() < 0);
				LifecycleType checkLifecycle = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getByNameInGroup(lc.getName(), dir);
				assertNotNull("Failed cache check for bulk lifecycle object by name",checkLifecycle);
				checkLifecycle = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getById(lc.getId(), lc.getOrganizationId());
				assertNotNull("Failed cache check for bulk lifecycle object by id",checkLifecycle);
				logger.info("Lifecycle Bulk entry id=" + lc.getId());
			}
			long stop = System.currentTimeMillis();
			logger.info("Time to loop bulk: " + (stop - start) + "ms");
			BulkFactories.getBulkFactory().write(sessionId);
			logger.info("Time to write: " + (System.currentTimeMillis() - start) + "ms");
			BulkFactories.getBulkFactory().close(sessionId);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		} 

	}
	*/
}