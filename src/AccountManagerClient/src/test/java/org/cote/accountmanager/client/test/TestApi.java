package org.cote.accountmanager.client.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cote.accountmanager.client.ClientSigningKeyResolver;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.junit.Test;

import io.jsonwebtoken.Jwts;

public class TestApi extends BaseClientTest {

	/// Note: Currently, the test expects the ClientReceiver user exists and is an account administrator in the /System organization
	/// And, the configured communities (lifecycle) and projects exist
	/// And, the test user exists
	/// In other words: This test will fail if a) the service is not running, and b) the previous users don't exist
	@Test
	public void TestTokenIntrospection() {
		String token = new String(testUserContext.getAuthenticationCredential().getCredential());
		//JwtParser parser = Jwts.parser();
		String subject = Jwts.parser().setSigningKeyResolver(new ClientSigningKeyResolver()).parseClaimsJws(token).getBody().getSubject();
		//Header h = Jwts.parser().parse(token).getHeader();
		logger.info("Subject: " + subject);
	}
	
	@Test
	public void TestConnectivity() {
		assertNotNull("Test user is null",testUser);
		assertNotNull("Context user is null",testUserContext.getUser());
	}
	
	@Test
	public void TestClearCache() {
		assertTrue("Failed to clear object cache",AM6Util.clearCache(testUserContext, NameEnumType.GROUP));
		assertTrue("Failed to clear all cache",AM6Util.clearCache(testUserContext, NameEnumType.UNKNOWN));
	}
	
	@Test
	public void TestCreateData() {
		String testDataName = "Test Data 1";
		DirectoryGroupType homeDirectory = AM6Util.findObject(testUserContext, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", "~");
		assertNotNull("Couldn't Find Home Directory",homeDirectory);
		String testPath = AM6Util.getEncodedPath("~/TestData");

		DirectoryGroupType subDirectory = AM6Util.findObject(testUserContext, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", testPath);

		if(subDirectory == null) {
			subDirectory = new DirectoryGroupType();
			subDirectory.setNameType(NameEnumType.GROUP);
			subDirectory.setGroupType(GroupEnumType.DATA);
			subDirectory.setName("TestData");
			subDirectory.setParentId(homeDirectory.getId());
			assertTrue("Failed to add directory",AM6Util.updateObject(testUserContext, Boolean.class, subDirectory));
			subDirectory = AM6Util.findObject(testUserContext, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", testPath);
		}
		assertNotNull("Couldn't find sub directory",subDirectory);
		// logger.info(JSONUtil.exportObject(subDirectory));
		DataType testData = AM6Util.getObjectByName(testUserContext, DataType.class, NameEnumType.DATA, subDirectory.getObjectId(), testDataName, false);
		if(testData != null) {
			assertTrue("Failed to delete data", AM6Util.deleteObject(testUserContext, Boolean.class, NameEnumType.DATA, testData.getObjectId()));
			testData = null;
		}
		if(testData == null) {
			DataType data = new DataType();
			data.setNameType(NameEnumType.DATA);
			data.setMimeType("text/plain");
			try {
				DataUtil.setValue(data, "This is the example data".getBytes());
			} catch (DataException e) {
				logger.error(e);
			}
			assertTrue("Data contains no value", data.getDataBytesStore().length > 0);
			data.setName(testDataName);
			data.setGroupPath(subDirectory.getPath());
			assertTrue("Failed to add data",AM6Util.updateObject(testUserContext, Boolean.class, data));
			testData = AM6Util.getObjectByName(testUserContext, DataType.class, NameEnumType.DATA, subDirectory.getObjectId(), testDataName, false);

		}
		assertNotNull("Couldn't find test data",testData);
		assertNotNull("Couldn't find test dir",subDirectory);
		
		int count = AM6Util.count(testUserContext, NameEnumType.DATA, subDirectory.getObjectId());
		logger.info("Count: " + count);
		
		List<DataType> dataList = AM6Util.list(testUserContext, DataType.class, NameEnumType.DATA, subDirectory.getObjectId(), 0L, 10);
		logger.info("List size: " + dataList.size());
	}
	
	@Test
	public void TestUpdateData() {
		String testDataName = "Test Data 1";
		DirectoryGroupType homeDirectory = AM6Util.findObject(testUserContext, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", "~");
		assertNotNull("Couldn't Find Home Directory",homeDirectory);
		String testPath = AM6Util.getEncodedPath("~/TestData");

		DirectoryGroupType subDirectory = AM6Util.findObject(testUserContext, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", testPath);

		if(subDirectory == null) {
			subDirectory = new DirectoryGroupType();
			subDirectory.setNameType(NameEnumType.GROUP);
			subDirectory.setGroupType(GroupEnumType.DATA);
			subDirectory.setName("TestData");
			subDirectory.setParentId(homeDirectory.getId());
			assertTrue("Failed to add directory",AM6Util.updateObject(testUserContext, Boolean.class, subDirectory));
			subDirectory = AM6Util.findObject(testUserContext, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", testPath);
		}
		assertNotNull("Couldn't find sub directory",subDirectory);
		// logger.info(JSONUtil.exportObject(subDirectory));
		DataType testData = AM6Util.getObjectByName(testUserContext, DataType.class, NameEnumType.DATA, subDirectory.getObjectId(), testDataName, false);
		if(testData != null) {
			assertTrue("Failed to delete data", AM6Util.deleteObject(testUserContext, Boolean.class, NameEnumType.DATA, testData.getObjectId()));
			testData = null;
		}
		if(testData == null) {
			DataType data = new DataType();
			data.setNameType(NameEnumType.DATA);
			data.setMimeType("text/plain");
			try {
				DataUtil.setValue(data, "This is the example data".getBytes());
			} catch (DataException e) {
				logger.error(e);
			}
			assertTrue("Data contains no value", data.getDataBytesStore().length > 0);
			data.setName(testDataName);
			data.setGroupPath(subDirectory.getPath());
			assertTrue("Failed to add data",AM6Util.updateObject(testUserContext, Boolean.class, data));
			testData = AM6Util.getObjectByName(testUserContext, DataType.class, NameEnumType.DATA, subDirectory.getObjectId(), testDataName, false);

		}
		assertNotNull("Couldn't find test data",testData);
		assertNotNull("Couldn't find test dir",subDirectory);
		
		int count = AM6Util.count(testUserContext, NameEnumType.DATA, subDirectory.getObjectId());
		logger.info("Count: " + count);
		
		List<DataType> dataList = AM6Util.list(testUserContext, DataType.class, NameEnumType.DATA, subDirectory.getObjectId(), 0L, 10);
		logger.info("List size: " + dataList.size());
	}
}
