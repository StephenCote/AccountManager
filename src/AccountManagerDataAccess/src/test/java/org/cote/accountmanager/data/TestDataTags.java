package org.cote.accountmanager.data;

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
import org.cote.accountmanager.objects.DataParticipantType;
import org.cote.accountmanager.objects.DataTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.FileUtil;
import org.junit.Test;

public class TestDataTags extends BaseDataAccessTest {
	
	@Test
	public void testTagSearch(){
		try{
			Pattern limitNames = Pattern.compile("([^A-Za-z0-9\\-_\\.\\s])",Pattern.MULTILINE);
			Pattern limitPath = Pattern.compile("([^A-Za-z0-9\\-_\\.\\s\\/\\~])",Pattern.MULTILINE);
			OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization("/Accelerant/Rocket");
			assertNotNull("Org is null",org);
			UserType user = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("TestUser1", org.getId());
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(user);
			assertNotNull("User is null",user);
			DirectoryGroupType tagDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Tags", user.getHomeDirectory(), org.getId());
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
			List<DataType> dataList = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).getDataForTags(new DataTagType[]{tag1,tag2}, 10,10,org.getId());
			assertTrue("Parts to List don't match: " + dataList.size() + " != " + 10,dataList.size() == 10 && count > 0);
			logger.info("Found " + dataList.size() + " data tags");
			for(int i = 0; i < 250 && i < dataList.size();i++){
				logger.info(dataList.get(i).getGroupId() + "/" + dataList.get(i).getName());
			}
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		} 
	}
	
	public void testTagETL2(){
		Map<String,Map<String,List<String>>> tagMap = new HashMap<String,Map<String,List<String>>>();
		((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.TAGPARTICIPATION)).setBatchSize(1000);
		
		//((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).setUseThreadSafeCollections(false);
		((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).setAggressiveKeyFlush(false);
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).setUseThreadSafeCollections(false);
		((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).setUseThreadSafeCollections(false);
		((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).setUseThreadSafeCollections(false);
		
		try{
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
			
			Map<String,DataTagType> dataTags = new HashMap<String,DataTagType>();
			
			DataTagType nTag = null;
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
					DataTagType tag = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).newDataTag(user,tags[i],tagDir.getId());
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
					List<QueryField> fields = new ArrayList<QueryField>();
					QueryField nameField = new QueryField(SqlDataEnumType.VARCHAR,"name",names.toString());
					nameField.setComparator(ComparatorEnumType.IN);
					fields.add(nameField);
					fields.add(QueryFields.getFieldGroup(dir.getId()));
					
					startLookup = System.currentTimeMillis();
					List<DataType> data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataList(fields.toArray(new QueryField[0]), null,true,dir.getOrganizationId());
					//logger.info("Data Lookup: " + (System.currentTimeMillis() - startLookup));
					
					if(data.size() == 0){
						logger.warn("Empty data size for: " + names.toString() + " in group id " + dir.getId());
					}
					startLookup = System.currentTimeMillis();
					for(int d = 0; d < data.size(); d++){
						DataParticipantType dpt = ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).newDataTagParticipation(nTag, data.get(d));
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
			
			Map<String,DataTagType> tags = new HashMap<String,DataTagType>();
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			DataTagType nTag = null;
			for(int i = 0; i < dataFile.length;i++){
				String[] pairs = dataFile[i].split("\t");
				if(pairs.length != 4) logger.warn("Unexpected length");
				if(tags.containsKey(pairs[2])==false){
					DataTagType tag = ((TagFactory)Factories.getFactory(FactoryEnumType.TAG)).newDataTag(user,pairs[2],tagDir.getId());
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
				DataParticipantType dpt = ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).newDataTagParticipation(nTag, data);
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
		UserType user1 = getUser("testuser1","password1");
		UserType user2 = getUser("testuser2","password1");
		DataType data1 = getData(user1, "testdata1");
		DataType data2 = getData(user1, "testdata2");
		DataType data3 = getData(user2, "testdata3");
		DataTagType tag1 = getTag(user1,"tag1");
		DataTagType tag2 = getTag(user1,"tag2");
		try {
			assertTrue("Unable to tag data", AuthorizationService.switchParticipant(user1, tag1, data1, true));
			assertTrue("Unable to tag data", AuthorizationService.switchParticipant(user1, tag1, data2, true));
			assertTrue("Unable to tag data", AuthorizationService.switchParticipant(user2, tag1, data3, true));
			assertTrue("Unable to tag data", AuthorizationService.switchParticipant(user1, tag2, data1, true));
			assertTrue("Unable to tag data", AuthorizationService.switchParticipant(user2, tag2, data3, true));
			
			List<DataParticipantType> parts = ((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).convertList(((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).getParticipations(new DataTagType[]{tag1}, ParticipantEnumType.DATA));
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
			

			
			//((TagParticipationFactory)Factories.getFactory(FactoryEnumType.TAGPARTICIPATION)).GetDataFromParticipations(list, detailsOnly, startRecord, recordCount, organization)
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}

	}
}
