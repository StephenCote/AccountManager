package org.cote.accountmanager.client.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.client.util.CacheUtil;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.junit.Test;

public class TestCache extends BaseClientTest {

	private int testDataSize = 1000;
	private boolean resetTestData = false;
	private String testDataBucket = "Data Set 1";
	
	@Test
	public void TestAtomic(){
		int testInt = 3;
		assertTrue("Failed to add int value to cache",CacheUtil.cache(testUserContext, "count1", testInt));
		int checkInt = CacheUtil.readCache(testUserContext, "count1", Integer.class);
		assertNotNull("Value is null", checkInt);
		assertTrue("Value should be equal", testInt == checkInt);
	}
	
	@Test
	public void TestPagination() {
		DirectoryGroupType homeDir = testUserContext.getHomeDirectory();
		assertNotNull("Home directory is null", homeDir);
		DirectoryGroupType testDataCont = getCreateDirectory(testUserContext, homeDir, testDataBucket);
		
		assertNotNull("Test directory is null", testDataCont);
		if(resetTestData && AM6Util.count(testUserContext, NameEnumType.DATA, testDataCont.getObjectId()) > 0) {
			AM6Util.deleteObject(testUserContext, Boolean.class, NameEnumType.GROUP, testDataCont.getObjectId());
			testDataCont = getCreateDirectory(testUserContext, homeDir, testDataBucket);
		}
		// 
		int popCount = populateDirectory(testUserContext, testDataCont);
		logger.info("Populated " + popCount + " items");
		
		long startRecord = 0L;
		int recordCount = 50;
		String countKey = "count-data-" + testDataCont.getObjectId();
		String listKeyPrefix = "list-data-";
		String listKeySuffix = "-" + testDataCont.getObjectId();
		try {
			Integer countI = CacheUtil.readCache(testUserContext, countKey, Integer.class);
			int count = (countI != null ? countI.intValue() : 0);
			if(count <= 0) {
				count = AM6Util.count(testUserContext, NameEnumType.DATA, testDataCont.getObjectId());
				CacheUtil.cache(testUserContext, countKey, count);
			}
			logger.info("Count: " + count);
			int pages = 0;
			if(count > 0) pages = (int)Math.nextUp(count/recordCount);
			for(int i = 0; i < pages; i++) {
				String listKey = listKeyPrefix + startRecord + "-" + (startRecord + recordCount) + listKeySuffix;
//				logger.info("List Key: " + listKey);
				startRecord += recordCount;
				List<DataType> dataList = CacheUtil.readCache(testUserContext, listKey, new ArrayList<DataType>().getClass());
				if(dataList == null) {
					dataList = AM6Util.list(testUserContext, new ArrayList<DataType>().getClass(), NameEnumType.DATA, testDataCont.getObjectId(), startRecord, recordCount);
					CacheUtil.cache(testUserContext, listKey, dataList);
				}
			}
		}
		catch(NullPointerException npe) {
			npe.printStackTrace();
		}
	}
	
	private int populateDirectory(ClientContext context, DirectoryGroupType dir) {
		int count = AM6Util.count(context, NameEnumType.DATA, dir.getObjectId());
		int diff = (testDataSize - count);
		int i = 0;
		for(; i < diff; i++) {
			DataType data = new DataType();
			data.setNameType(NameEnumType.DATA);
			data.setMimeType("text/plain");
			try {
				DataUtil.setValue(data, "This is the example data".getBytes());
			} catch (DataException e) {
				logger.error(e);
			}
			data.setName(UUID.randomUUID().toString());
			data.setGroupPath(dir.getPath());
			if(!AM6Util.updateObject(testUserContext, Boolean.class, data)) {
				logger.error("Failed to add data");
				break;
			}
			if(i%50==0) logger.info("Populated " + i + " of " + diff);

		}
		return i;
	}
}
