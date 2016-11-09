package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.junit.Test;

public class TestAggregateCount extends BaseDataAccessTest{
	public static final Logger logger = LogManager.getLogger(TestAggregateCount.class);
	private static String testDirGroupName = null;
	
	@Test
	public void testCountData(){
		UserType sessionUser = testUser;
		int count = 0;
		DirectoryGroupType dir1 = null;
		try {
			Factories.getNameIdFactory(FactoryEnumType.USER).populate(sessionUser);
			dir1 = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(sessionUser, "Count Dir 1",sessionUser.getHomeDirectory(), sessionUser.getOrganizationId());
			
			for(int i = 0; i < 20;i++){
				DataType data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName("Test-" + (i + 1), dir1);
				if(data == null){
					data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(sessionUser, dir1.getId());
					data.setName("Test-" + (i + 1));
					data.setMimeType("text/plain");
					DataUtil.setValueString(data, "Example Data");
					((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(data);
					data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName("Test-" + (i + 1), dir1);
				}
				assertNotNull("Example data is null",data);
			}
			
			count = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getCount(dir1);
			assertTrue("Unexpected count - " + count, count == 20);
			
			List<DataType> dataList1 = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataListByGroup(dir1, true, 1, 10, dir1.getOrganizationId());
			assertTrue("List size is not 10",dataList1.size() == 10);
			//assertTrue("Last item is " + dataList1.get(9).getName() + ", not Test-10",dataList1.get(9).getName().equals("Test-10"));
			
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataException e) {
			
			logger.error("Error",e);
		}
		assertNotNull("Test directory is null");
		logger.info("Counted " + count + " data");
	}
}