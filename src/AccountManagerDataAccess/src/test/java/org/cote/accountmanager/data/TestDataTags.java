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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.IParticipationFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.TagFactory;
import org.cote.accountmanager.data.factory.TagParticipationFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.ServiceUtil;
import org.cote.accountmanager.data.services.TagService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.DataTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.objects.types.TagEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.FileUtil;
import org.junit.Test;

public class TestDataTags extends BaseDataAccessTest {
	
	private static String testTagUser1 = "Tag User 1"; 
	private static String testTagUser2 = "Tag User 2"; 

	@Test
	public void testCreateDataTag(){
		BaseService.populate(AuditEnumType.USER, testUser);
		DirectoryGroupType tDir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Tags", testUser);
		DirectoryGroupType dDir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Data", testUser);
		assertNotNull("Directory is null", tDir);
		assertNotNull("Directory is null", dDir);
		DataType data = this.newTextData("Test tag data 1", "Test data data text", testUser, dDir);
		assertNotNull("Data is null", data);
		
		BaseTagType dataTag = this.getTag(testUser, tDir, TagEnumType.DATA, "Test tag 1");
		assertNotNull("Tag is null", dataTag);
		boolean error = false;
		try {
			TagService.applyTags(testUser, new BaseTagType[]{dataTag}, new NameIdType[]{data});
		} catch (ArgumentException | FactoryException | DataAccessException e) {
			logger.error(e);
		}
		assertFalse("An error occurred", error);
	}
	
	@Test
	public void testCreatePersonTag(){
		BaseService.populate(AuditEnumType.USER, testUser);
		DirectoryGroupType tDir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Tags", testUser);
		DirectoryGroupType dDir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Persons", testUser);
		assertNotNull("Directory is null", tDir);
		assertNotNull("Directory is null", dDir);
		PersonType person = this.getApplicationPerson("Test Person 1", dDir);
		PersonType person2 = this.getApplicationPerson("Test Person 2", dDir);
		assertNotNull("Person is null", person);
		
		BaseTagType perTag = this.getTag(testUser, tDir, TagEnumType.PERSON, "Test tag 1");
		BaseTagType perTag2 = this.getTag(testUser, tDir, TagEnumType.PERSON, "Test tag 2");
		BaseTagType perTag3 = this.getTag(testUser, tDir, TagEnumType.PERSON, "Test tag 3");
		BaseTagType perTag4 = this.getTag(testUser, tDir, TagEnumType.PERSON, "Test tag 4");
		assertNotNull("Tag is null", perTag);
		boolean error = false;
		try {
			TagService.applyTags(testUser, new BaseTagType[]{perTag}, new NameIdType[]{person});
			TagService.applyTags(testUser, new BaseTagType[]{perTag4}, new NameIdType[]{person2});
			TagService.applyTags(testUser, new BaseTagType[]{perTag2,perTag3}, new NameIdType[]{person,person2});
		} catch (ArgumentException | FactoryException | DataAccessException e) {
			logger.error(e);
		}
		assertFalse("An error occurred", error);
	}
	
	public void testTagSearch(){
		try{
			Pattern limitNames = Pattern.compile("([^A-Za-z0-9\\-_\\.\\s])",Pattern.MULTILINE);
			Pattern limitPath = Pattern.compile("([^A-Za-z0-9\\-_\\.\\s\\/\\~])",Pattern.MULTILINE);

			UserType user = this.getUser("TestUser1", "password");
			assertNotNull("User is null",user);
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(user);

			DirectoryGroupType tagDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Tags", user.getHomeDirectory(), user.getOrganizationId());
			/*
			List<BaseTagType> tags = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).listTags(tagDir,0,0,tagDir.getOrganization());
			logger.info("Got the tags: " + tags.size());
			*/
			/*
			ProcessingInstructionType instruction = new ProcessingInstructionType();
			instruction.setPaginate(true);
			instruction.setStartIndex(0L);
			instruction.setRecordCount(10);
			instruction.setHavingClause("count(participantid) = " + 2);
			instruction.setGroupClause("participantid");
			instruction.setOrderClause("participantid");
			assertTrue("Instruction is not ready for pagination",DBFactory.isInstructionReadyForPagination(instruction));
			*/
			DataTagType tag1 = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).getByNameInGroup("xx", tagDir);
			DataTagType tag2 = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).getByNameInGroup("xx", tagDir);
			DataTagType tag3 = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).getByNameInGroup("xx", tagDir);
			boolean canRead = AuthorizationService.canView(user, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupById(tag1.getGroupId(),user.getOrganizationId()));
			//List<DataParticipantType> tagParts = ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).getTagParticipations(new DataTagType[]{tag1,tag2}, ParticipantEnumType.DATA);
			int count = ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).countTagParticipations(new DataTagType[]{tag1,tag2}, ParticipantEnumType.DATA);
			List<DataType> dataList = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).getForTags(FactoryEnumType.DATA, new DataTagType[]{tag1,tag2}, 10,10,user.getOrganizationId());
			assertTrue("Parts to List don't match: " + dataList.size() + " != " + 10,dataList.size() == 10 && count > 0);
			logger.info("Found " + dataList.size() + " data tags");
			for(int i = 0; i < 250 && i < dataList.size();i++){
				logger.info(dataList.get(i).getGroupId() + "/" + dataList.get(i).getName());
			}
		}
		catch(NullPointerException | FactoryException | ArgumentException e) {
			
			logger.error(e);
			e.printStackTrace();
			
		} 
	}
	
	public void testTagETL2(){
		Map<String,Map<String,List<String>>> tagMap = new HashMap<String,Map<String,List<String>>>();
		
		try{
			((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TAGPARTICIPATION)).setBatchSize(1000);
			((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).setAggressiveKeyFlush(false);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).setUseThreadSafeCollections(false);
			((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).setUseThreadSafeCollections(false);
			((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).setUseThreadSafeCollections(false);

			Pattern limitNames = Pattern.compile("([^A-Za-z0-9\\-_\\.\\s])",Pattern.MULTILINE);
			Pattern limitPath = Pattern.compile("([^A-Za-z0-9\\-_\\.\\s\\/\\~])",Pattern.MULTILINE);
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/Accelerant/Rocket");
			assertNotNull("Org is null",org);
			UserType user = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("TestUser1", org.getId());
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(user);
			assertNotNull("User is null",user);
			DirectoryGroupType tagDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Tags", user.getHomeDirectory(), org.getId());
			String[] dataFile = FileUtil.getFileAsString("/Users/Steve/Temp/tagstest.csv").split("\n");
			logger.info("Reading " + dataFile.length + " lines");
			String match = "Root/Home/product_user/Media";
			String replace = "~/GalleryHome/rd";
			
			Map<String,BaseTagType> dataTags = new HashMap<>();
			
			BaseTagType nTag = null;
			long startMap = System.currentTimeMillis();
			
			for(int i = 0; i < dataFile.length;i++){
				String[] pairs = dataFile[i].split("\t");
				if(pairs.length != 4) logger.warn("Unexpected length");
				String tagName = pairs[2];
				String path = pairs[3].replace(match,replace).trim();
				String name = limitNames.matcher(pairs[1]).replaceAll("0");
				if(tagMap.containsKey(tagName) == false){
					tagMap.put(tagName, new HashMap<String,List<String>>());
				}
				if(tagMap.get(tagName).containsKey(path)==false){
					tagMap.get(tagName).put(path, new ArrayList<String>());
				}
				tagMap.get(tagName).get(path).add(name);

			}
			logger.info("Time to build map: " + (System.currentTimeMillis() - startMap));
			startMap = System.currentTimeMillis();
			String[] tags = tagMap.keySet().toArray(new String[0]);
			for(int i = 0; i < tags.length;i++){
				long startTag = System.currentTimeMillis();
				String sessionId = BulkFactories.getBulkFactory().newBulkSession();
				if(dataTags.containsKey(tags[i])==false){
					BaseTagType tag = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).newTag(user,tags[i],TagEnumType.DATA, tagDir.getId());
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TAG, tag);
					dataTags.put(tags[i], tag);
					nTag = tag;
				}
				else nTag = dataTags.get(tags[i]);
				String[] paths = tagMap.get(tags[i]).keySet().toArray(new String[0]);
				//logger.info("Tag Path Size = " + paths.length);
				for(int g = 0; g < paths.length; g++){
					long startLookup = System.currentTimeMillis();
					String path = limitPath.matcher(paths[g]).replaceAll("");
					DirectoryGroupType dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA,path, org.getId());
					//logger.info("Group Lookup: " + (System.currentTimeMillis() - startLookup));
					if(dir == null){
						logger.warn("Failed to find path '" + path + "'");
						continue;
					}
					
					StringBuffer names = new StringBuffer();
					List<String> dNames = tagMap.get(tags[i]).get(paths[g]);
					//logger.info("Tag Data Size = " + dNames.size());
					for(int n = 0; n < dNames.size();n++){
						if(n > 0) names.append(",");
						names.append("'" + dNames.get(n) + "'");
					}
					List<QueryField> fields = new ArrayList<>();
					QueryField nameField = new QueryField(SqlDataEnumType.VARCHAR,"name",names.toString());
					nameField.setComparator(ComparatorEnumType.ANY);
					fields.add(nameField);
					fields.add(QueryFields.getFieldGroup(dir.getId()));
					
					startLookup = System.currentTimeMillis();
					List<DataType> data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataList(fields.toArray(new QueryField[0]), null,true,dir.getOrganizationId());
					//logger.info("Data Lookup: " + (System.currentTimeMillis() - startLookup));
					
					if(data.isEmpty()){
						logger.warn("Empty data size for: " + names.toString() + " in group id " + dir.getId());
					}
					startLookup = System.currentTimeMillis();
					for(int d = 0; d < data.size(); d++){
						BaseParticipantType dpt = ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).newTagParticipation(nTag, data.get(d));
						BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TAGPARTICIPATION, dpt);
					}
					//logger.info("Bulk Queue: " + (System.currentTimeMillis() - startLookup));
	
				}
				//logger.info("Prepare Write: " + (System.currentTimeMillis() - startTag)); 
				BulkFactories.getBulkFactory().write(sessionId);
				
				//if(true) break;
			}
			
			logger.info("Time to import: " + (System.currentTimeMillis() - startMap));
				
			}
			catch(FactoryException e){
				logger.error(e.getMessage());
			} catch (ArgumentException e) {
				
				logger.error(e.getMessage());
			} catch (DataAccessException e) {
				
				logger.error(e.getMessage());
			}
			
	}
	
	public void testTagETL(){
		Map<String,Map<String,String>> tagMap = new HashMap<String,Map<String,String>>();
		try{
			Pattern limitNames = Pattern.compile("([^A-Za-z0-9\\-_\\.\\s])",Pattern.MULTILINE);
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/Accelerant/Rocket");
			assertNotNull("Org is null",org);
			UserType user = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("TestUser1", org.getId());
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(user);
			assertNotNull("User is null",user);
			DirectoryGroupType tagDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Tags", user.getHomeDirectory(), org.getId());
			String[] dataFile = FileUtil.getFileAsString("/Users/Steve/Temp/tagstest.csv").split("\n");
			logger.info("Reading " + dataFile.length + " lines");
			String match = "Root/Home/product_user/Media";
			String replace = "~/GalleryHome/rd";
			
			Map<String,BaseTagType> tags = new HashMap<>();
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			BaseTagType nTag = null;
			for(int i = 0; i < dataFile.length;i++){
				String[] pairs = dataFile[i].split("\t");
				if(pairs.length != 4) logger.warn("Unexpected length");
				if(tags.containsKey(pairs[2])==false){
					BaseTagType tag = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).newTag(user,pairs[2],TagEnumType.DATA, tagDir.getId());
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TAG, tag);
					tags.put(pairs[2], tag);
					nTag = tag;
				}
				else nTag = tags.get(pairs[2]);
				String path = pairs[3].replace(match,replace).trim();
				DirectoryGroupType dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA,path, org.getId());
				if(dir == null){
					logger.warn("Failed to find path '" + path + "'");
					continue;
				}
				String name = limitNames.matcher(pairs[1]).replaceAll("0");
				DataType data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name, true, dir);
				if(data == null){
					logger.warn("Failed to find data '" + name + "' in '" + path +"'");
					continue;
				}
				BaseParticipantType dpt = ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).newTagParticipation(nTag, data);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TAGPARTICIPATION, dpt);
				//logger.info("Found '" + name + "' in '" + path + "'");
				/*
				if(i > 10){
					logger.info("Break on debug");
					break;
				}
				*/
			}
			BulkFactories.getBulkFactory().write(sessionId);
			
			
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		} catch (DataAccessException e) {
			
			logger.error(e.getMessage());
		}
	}
	
	
	public void testDataTags(){
		assertTrue("Account Manager Service is not setup correctly",ServiceUtil.isFactorySetup());
		UserType user1 = getUser(testTagUser1,"password1");
		UserType user2 = getUser(testTagUser2,"password1");
		DataType data1 = getData(user1, "testdata1");
		DataType data2 = getData(user1, "testdata2");
		DataType data3 = getData(user2, "testdata3");
		DirectoryGroupType tDir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Tags", testUser);
		BaseTagType tag1 = getTag(user1,tDir, TagEnumType.DATA,"tag1");
		BaseTagType tag2 = getTag(user1,tDir, TagEnumType.DATA,"tag2");
		try {
			assertTrue("Unable to tag data", AuthorizationService.switchParticipant(user1, tag1, data1, true));
			assertTrue("Unable to tag data", AuthorizationService.switchParticipant(user1, tag1, data2, true));
			assertTrue("Unable to tag data", AuthorizationService.switchParticipant(user2, tag1, data3, true));
			assertTrue("Unable to tag data", AuthorizationService.switchParticipant(user1, tag2, data1, true));
			assertTrue("Unable to tag data", AuthorizationService.switchParticipant(user2, tag2, data3, true));
			/*
			List<BaseParticipantType> parts = ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).convertList(((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).getParticipations(new DataTagType[]{tag1}, ParticipantEnumType.DATA));
			assertTrue("Unexpected count", parts.size() == 3);
			logger.info("Parts = " + parts.size());
			List<DataType> data_list = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).getDataForTag(tag1, Factories.getDevelopmentOrganization().getId());
			assertTrue("Unexpected count", data_list.size() == 3);
			logger.info("Data for parts = " + data_list.size());
			
			parts = ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).convertList(((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).getParticipations(new DataTagType[]{tag2}, ParticipantEnumType.DATA));
			assertTrue("Unexpected count", parts.size() == 2);
			logger.info("Parts = " + parts.size());
			data_list = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).getDataForTag(tag2, Factories.getDevelopmentOrganization().getId());
			assertTrue("Unexpected count", data_list.size() == 2);
			logger.info("Data for parts = " + data_list.size());

			parts = ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).convertList(((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).getTagParticipations(new DataTagType[]{tag1,tag2}, null,ParticipantEnumType.DATA));
			logger.info("Parts = " + parts.size());
			logger.info("Perf Note/Bug: getTagParticipations returns N instances of participant ids instead of just 1.  This doesn't affect the result, but does add duplicate entries to the query.");
			//assertTrue("Unexpected count", parts.size() == 4);

			data_list = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).getDataForTags(new DataTagType[]{tag1,tag2}, 0,0,Factories.getDevelopmentOrganization().getId());
			logger.info("Data for parts = " + data_list.size());
			assertTrue("Unexpected count", data_list.size() == 2);

			assertTrue("Unable to tag data", AuthorizationService.switchParticipant(user2, tag2, data3, false));
			data_list = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).getDataForTags(new DataTagType[]{tag1,tag2}, 0,0,Factories.getDevelopmentOrganization().getId());
			logger.info("Data for parts = " + data_list.size());
			assertTrue("Unexpected count", data_list.size() == 1);	
			
*/
			
			//((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).GetDataFromParticipations(list, detailsOnly, startRecord, recordCount, organization)
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

	}
}
