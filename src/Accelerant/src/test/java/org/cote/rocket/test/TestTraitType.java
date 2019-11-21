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
package org.cote.rocket.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.propellant.objects.EventType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.EventFactory;
import org.cote.rocket.util.DataGeneratorUtil;
import org.junit.Test;

public class TestTraitType extends BaseAccelerantTest {



	private String testLifecycleName = "Event Type Lifecycle";
	private String testProjectNamePrefix = "Event Project ";
	//private int testProjectNameCounter = 1;
	private int testLocSize = 3;
	private int testPerSeed = 250;
	private int testEpochEvolutions = 10;
	private int testEpochCount = 15;
	
	private boolean resetLifecycle = false;
	private boolean resetProject = false;
	
	@Test
	public void TestCDATA(){
		DataGeneratorUtil dutil = new DataGeneratorUtil(
			testUser,
			testLifecycleName,
			testProjectNamePrefix + "1",
			"~/Locations",
			"~/Traits",
			"/Users/Steve/Downloads/wn3.1.dict/dict/",
			"/Users/Steve/Projects/workspace/RocketWeb/src/main/webapp/Scripts/"
			//"c:/Users/swcot/Downloads/wn3.1.dict.tar/dict/",
			//"c:/Users/swcot/workspace/RocketWeb/src/main/webapp/Scripts/"
		);
		
		boolean error = false;
		logger.info("Testing EventType Project CRUD");
		try {
			if(resetLifecycle) dutil.deleteLifecycle();
			else if(resetProject) dutil.deleteProject();
			
			assertTrue("Failed to initialize",dutil.initialize());
			assertNotNull("Project is null", dutil.getProject());

			assertNotNull(dutil.getEventsDir());
			int eventCount = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).countInGroup(dutil.getEventsDir());
			boolean initSetup = (eventCount == 0);

			if(initSetup){
				logger.info("START Populating " + dutil.getProject().getName() + " data ...");
				String sessionId = BulkFactories.getBulkFactory().newBulkSession();
				
				EventType regionCreation = dutil.generateRegion(sessionId, testLocSize, testPerSeed);
				logger.info("Writing session");
				BulkFactories.getBulkFactory().write(sessionId);
				BulkFactories.getBulkFactory().close(sessionId);
				((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate(regionCreation);
				List<EventType> events = regionCreation.getChildEvents();
				assertTrue("Generated events for a region were " + events.size() + " but should be double the location size",events.size() == (testLocSize*2));
				logger.info("Created " + events.size() + " events");
				//String sessionId = BulkFactories.getBulkFactory().newBulkSession();
				logger.info("END Populating");
			}
			//boolean modelEpoch1 = (((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).getCount(targetEventsDir) == 1);
			//if(modelEpoch1){
				
				String sessionId = BulkFactories.getBulkFactory().newBulkSession();

				//logger.info("Current event count: " + ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).getCount(dutil.getEventsDir()));
				EventType epoch = null;
				for(int i = 0; i < testEpochCount; i++){
					int count = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).countInGroup(dutil.getEventsDir());
					boolean modeled = (count >= (2+i));
					if(modeled) continue;
					logger.info("MODEL Epoch " + (i + 1));
					// NOTE: Can't iterate with the same bulk session because the current design marks objects as modified, but the bulk utility needs to more gracefully track whether an object is already pending modification - it looks like it takes in the same object twice, which can cause constraint problems with replacement values such as attributes
					//String sessionId = BulkFactories.getBulkFactory().newBulkSession();
					epoch = dutil.generateEpoch(sessionId, testEpochEvolutions,1);
					/*
					if(dutil.isDecimated(epoch.getLocation())){
						logger.warn("Location is decimated");
						break;
					}
					*/
					//BulkFactories.getBulkFactory().write(sessionId);
				}
				
				BulkFactories.getBulkFactory().write(sessionId);
				BulkFactories.getBulkFactory().close(sessionId);
				
				if(epoch != null){
					/// Reload the epoch to populate
					//((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).removeFromCache(epoch);
					epoch = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).getByNameInGroup(epoch.getName(), dutil.getEventsDir());
					((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate(epoch);;
					
					for(EventType cevent : epoch.getChildEvents()){
						logger.info("Demographics for: " + cevent.getName());
						Map<String,List<PersonType>> demo = dutil.getDemographics(cevent);
						for(String key : demo.keySet()){
							logger.info(key + " : " + demo.get(key).size());
						}
						//logger.info("Writing session");
					}
				}
				List<EventType> events = dutil.getEvents();
				for(EventType evt : events){
					((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate(evt);
				}
				FileUtil.emitFile("./data.json", JSONUtil.exportObject(events));
				logger.info("Emitted: " + events.size());
				/*
				//events = JSONUtil.importObject(FileUtil.getFile("./data.json"), ArrayList.class);
				
					String fileStr = FileUtil.getFileAsString("./data.json");
					ObjectMapper mapper = new ObjectMapper();
					try {
						TypeFactory t = TypeFactory.defaultInstance();
						events = mapper.readValue(fileStr, t.constructCollectionType(ArrayList.class, EventType.class));
						logger.info("Restored: " + events.size() + " events");
					} catch (IOException e) {
						
						logger.error(FactoryException.LOGICAL_EXCEPTION,e);
					}
					*/
					
					
				
				
				//logger.info("END Model Epoch 1");
			//}
			
			/// logger.info("START Event Modeling");
			
			logger.info("END");
		} catch (Exception e) {
			
			error = true;
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		assertFalse("An error occurred",error);

		/*
		List<EventType> events = generateRegion(sessionId,3, 100);
		logger.info("Region: " + events.size());
		for(EventType evt : events){
			logger.info(evt.getName());
			if(evt.getEventType() == EventEnumType.CONSTRUCT){
				
				logger.info("\tPre Construction");
				for(TraitType t : evt.getEntryTraits()){
					logger.info("\t\t" + Factories.getAttributeFactory().getAttributeValueByName(t,"code"));
				}
				logger.info("\tPost Construction");
				for(TraitType t : evt.getExitTraits()){
					logger.info("\t\t" + Factories.getAttributeFactory().getAttributeValueByName(t,"code"));
				}
			}
			if(evt.getEventType() == EventEnumType.INCEPT){
				for(PersonType person : evt.getActors()){
						logger.info("\t\t" + getPersonLabel(person));
				}
			}
		}
		*/

	}
	
	
	





	

	/*
	@Test
	public void TestRandomList(){
		DirectoryGroupType dir = null;
		try {
			dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(testUser, GroupEnumType.DATA, "~/Locations", testUser.getOrganizationId());
			assertNotNull("Directory is null",dir);
			

			
			List<LocationType> locations = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).getList(getRandomByGroup(dir).toArray(new QueryField[0]), getPaginatedInstruction(10), testUser.getOrganizationId());
			for(int i = 0; i < locations.size(); i++){
				LocationType loc = locations.get(i);
				Factories.getAttributeFactory().populateAttributes(loc);
				logger.info(Factories.getAttributeFactory().getAttributeValueByName(loc, "name"));
			}
			
			logger.info("Loc: " + locations.size());
			
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		//((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).
	}
	*/
	/*
	@Test
	public void TestLoadData(){

		List<String> adv = loadFile(adjPath);
		List<String> ver = loadFile(verPath);
		List<String> nou = loadFile(nouPath);
		List<String> adj = loadFile(adjPath);
		
		for(int i = 0; i < 20; i++){

			logger.info("Test : " + getWord(adj) + " " + getWord(nou) + " " + getWord(nou));
		}
	}
	*/
	
}

