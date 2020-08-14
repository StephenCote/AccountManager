package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseSearchRequestType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.ServletRequestMock;
import org.cote.accountmanager.util.StreamUtil;
import org.junit.Test;

public class TestDataService extends BaseDataAccessTest {
	
	private String testDataName1 = "testSampleData.txt";
	private String testDataPath = "~/DataCRUDTest";
	@Test
	public void TestMetaDataUpdateService() {
		ServletRequestMock request = getRequest(sessionId,testUser.getOrganizationId());
		DataType checkData = getNewResourceData(request, testDataName1);
		
		
		assertNotNull("Check data is null", checkData);
		byte[] origData = getDataBytes(checkData);	
		byte[] modData = new byte[0];
		
		DirectoryGroupType dir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", testDataPath, request);
		assertNotNull("Directory is null",dir);
		BaseSearchRequestType searchReq = new BaseSearchRequestType();
		List<DataType> dataList = BaseService.listByGroup(AuditEnumType.DATA, "DATA", dir.getObjectId(), 0L, 10, testUser);
		assertTrue("Should only be two items in the list", dataList.size() == 2);
		/// Don't check objectId here as checkData hasn't been assigned one yet

		assertTrue("Data name should be the same as the check data name", dataList.get(0).getName().equals(checkData.getName()));

		/// Work with the detailsOnly version of the object
		checkData = dataList.get(0);
		/// Make some small change
		checkData.setDimensions("123");
		BaseService.update(AuditEnumType.DATA, checkData, request);
		checkData = getResourceData(request, testDataName1);
		assertNotNull("Updated data is null", checkData);
		modData = getDataBytes(checkData);
		assertTrue("Data length should be the same", modData.length == origData.length);
		
		/// 
		
	}
	
	@Test
	public void TestFullDataUpdateService() {
		ServletRequestMock request = getRequest(sessionId,testUser.getOrganizationId());
		DataType checkData = getNewResourceData(request, testDataName1);

		assertNotNull("Check data is null", checkData);
		byte[] origData = getDataBytes(checkData);
		byte[] modData = new byte[0];	
		
		/// Make some small change
		checkData.setDimensions("123");
		BaseService.update(AuditEnumType.DATA, checkData, request);
		checkData = getResourceData(request, testDataName1);
		assertNotNull("Updated data is null", checkData);
		modData = getDataBytes(checkData);
		assertTrue("Data length should be the same", modData.length == origData.length);
		
		
		/// Try to push a full data back to details only and then update
		/// 2020/08/14 - there was a bug where setting a data object back to details only is failing to properly keep other meta data in sync
		/// This was introduced when trying to update large data objects (e.g.: reparenting the object) and wanting to only patch some of the attributes versus the whole object
		/// The error was due to the compressed bit being set to false after the data was extracted, and then being updated.
		/// The correction is to only persist the compressed bit and type along with the data when the byte array is updated.

		checkData.setDimensions("456");

		checkData.setDetailsOnly(true);
		checkData.setDataBytesStore(new byte[0]);

		BaseService.update(AuditEnumType.DATA, checkData, request);
		checkData = getResourceData(request, testDataName1);
		assertNotNull("Updated data is null", checkData);
		modData = getDataBytes(checkData);
		assertTrue("Data length of client-demoted meta data " + modData.length + " should be the same as the original " + origData.length, modData.length == origData.length);
	}
	private byte[] getDataBytes(DataType data) {
		byte[] dataB = new byte[0];
		try {
			dataB = DataUtil.getValue(data);
		} catch (DataException e) {
			logger.error(e);
		}	
		return dataB;
	}
	private ServletRequestMock getRequest(String sessionId,long organizationId){
		return new ServletRequestMock(null,sessionId,organizationId);
	}
	private byte[] getResourceContent(String resourceName) {
		byte[] outData = new byte[0];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			StreamUtil.copyStream(new BufferedInputStream(ClassLoader.getSystemResourceAsStream("./" + resourceName)), baos);
			outData = baos.toByteArray();
		} catch (IOException e) {
			logger.error(e);
		} 
		return outData;
	}
	private DataType getNewResourceData(ServletRequestMock request, String resourceName) {
		return queryResourceData(request, resourceName, true);
	}
	private DataType getResourceData(ServletRequestMock request, String resourceName) {
		return queryResourceData(request, resourceName, false);
	}

	private DataType queryResourceData(ServletRequestMock request, String resourceName, boolean recreate) {
		byte[] data = getResourceContent(testDataName1);
		assertTrue("Data is empty",data.length > 0);
		// logger.info("Working with " + data.length + " bytes of data");
		
		DirectoryGroupType dir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/DataCRUDTest", request);
		assertNotNull("Directory is null", dir);
		// logger.info("Working with directory " + dir.getPath());
		DataType checkData = BaseService.readByName(AuditEnumType.DATA, dir, resourceName, request);
		if(recreate == false) {
			return checkData;
		}
		if(checkData != null) {
			assertTrue("Failed to delete data", BaseService.delete(AuditEnumType.DATA, checkData, request));
			DataType compData = BaseService.readByName(AuditEnumType.DATA, dir, resourceName + ".compare", request);
			if(compData != null) assertTrue("Failed to delete comp data", BaseService.delete(AuditEnumType.DATA, compData, request));


			checkData = null;
		}
		try {
			checkData = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(testUser, dir.getId());
			checkData.setGroupPath(dir.getPath());
			checkData.setMimeType("text/plain");
			checkData.setName(resourceName);
			DataUtil.setValue(checkData, data);
			boolean updated = BaseService.add(AuditEnumType.DATA, checkData, request);
			assertTrue("Failed to add data", updated);
			
			
			DataType compData = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(testUser, dir.getId());
			compData.setGroupPath(dir.getPath());
			compData.setMimeType("text/plain");
			compData.setName(resourceName + ".compare");
			DataUtil.setValue(compData, data);

			BaseService.add(AuditEnumType.DATA, compData, request);
			checkData = BaseService.readByName(AuditEnumType.DATA, dir, resourceName, request);

		} catch ( DataException| NullPointerException | ArgumentException | FactoryException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return checkData;
	}
	

}
