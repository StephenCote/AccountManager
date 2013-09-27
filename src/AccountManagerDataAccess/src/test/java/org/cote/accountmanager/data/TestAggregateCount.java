package org.cote.accountmanager.data;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestAggregateCount{
	public static final Logger logger = Logger.getLogger(TestAggregateCount.class.getName());
	private static String testDirGroupName = null;
	
	private static String testUserName1 = "TestSessionUser";

	private UserType sessionUser = null;
	private static String sessionId = null;
	
	@Before
	public void setUp() throws Exception {
		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}
		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.SINGLE);
		cf.setDriverClassName("org.postgresql.Driver");
		cf.setUserName("devuser");
		cf.setUserPassword("password");
		cf.setUrl("jdbc:postgresql://127.0.0.1:5432/devdb");
		
		try{
			sessionUser = Factories.getUserFactory().getUserByName(testUserName1,Factories.getDevelopmentOrganization());
			if(sessionUser == null){
				UserType new_user = Factories.getUserFactory().newUser(testUserName1, SecurityUtil.getSaltedDigest("password1"), UserEnumType.NORMAL, UserStatusEnumType.NORMAL, Factories.getDevelopmentOrganization());
				if(Factories.getUserFactory().addUser(new_user,  false)){
					sessionUser = Factories.getUserFactory().getUserByName(testUserName1,Factories.getDevelopmentOrganization());
				}
			}

		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}
	}

	@After
	public void tearDown() throws Exception {

	}
	
	@Test
	public void testCountData(){
		
		int count = 0;
		DirectoryGroupType dir1 = null;
		try {
			Factories.getUserFactory().populate(sessionUser);
			dir1 = Factories.getGroupFactory().getCreateDirectory(sessionUser, "Count Dir 1",sessionUser.getHomeDirectory(), sessionUser.getOrganization());
			
			for(int i = 0; i < 20;i++){
				DataType data = Factories.getDataFactory().getDataByName("Test-" + (i + 1), dir1);
				if(data == null){
					data = Factories.getDataFactory().newData(sessionUser, dir1);
					data.setName("Test-" + (i + 1));
					data.setMimeType("text/plain");
					DataUtil.setValueString(data, "Example Data");
					Factories.getDataFactory().addData(data);
					data = Factories.getDataFactory().getDataByName("Test-" + (i + 1), dir1);
				}
				assertNotNull("Example data is null",data);
			}
			
			count = Factories.getDataFactory().getCount(dir1);
			assertTrue("Unexpected count - " + count, count == 20);
			
			List<DataType> dataList1 = Factories.getDataFactory().getDataListByGroup(dir1, true, 1, 10, dir1.getOrganization());
			assertTrue("List size is not 10",dataList1.size() == 10);
			//assertTrue("Last item is " + dataList1.get(9).getName() + ", not Test-10",dataList1.get(9).getName().equals("Test-10"));
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull("Test directory is null");
		logger.info("Counted " + count + " data");
	}
}