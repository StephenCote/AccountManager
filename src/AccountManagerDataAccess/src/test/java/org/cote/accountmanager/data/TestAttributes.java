package org.cote.accountmanager.data;

import java.util.UUID;

import org.cote.accountmanager.objects.AttributeType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.junit.Test;
import static org.junit.Assert.assertTrue;

public class TestAttributes extends BaseDataAccessTest{
	@Test
	public void TestGetAttributes(){
		DataType data = getData(testUser,"Test Data");
		Factories.getAttributeFactory().populateAttributes(data);
	}
	/*
	@Test
	public void TestDeleteAttributes(){
		DataType data = getData(testUser, "Test Data");
		Factories.getAttributeFactory().deleteAttributes(data);
	}
	*/
	@Test
	public void TestAddAttributes(){
		DataType data = getData(testUser, "Test Data");
		DataType data2 = getData(testUser, "Test Data 2");
		Factories.getAttributeFactory().deleteAttributes(data);
		Factories.getAttributeFactory().deleteAttributes(data2);
		AttributeType attr = Factories.getAttributeFactory().newAttribute(data);
		String testName = UUID.randomUUID().toString();
		attr.setName(testName);
		//attr.setDataType(SqlDataEnumType.VARCHAR);
		attr.getValues().add("Test value 1");
		attr.getValues().add("Test value 2");
		attr.getValues().add("Test value 3");
		data.getAttributes().add(attr);
		attr = Factories.getAttributeFactory().newAttribute(data2);
		attr.setName(testName);
		//attr.setDataType(SqlDataEnumType.VARCHAR);
		attr.getValues().add("Test value 1");
		attr.getValues().add("Test value 2");
		attr.getValues().add("Test value 3");
		data2.getAttributes().add(attr);
		assertTrue("Failed to update attributes",Factories.getAttributeFactory().addAttributes(new NameIdType[]{data,data2}));

		Factories.getAttributeFactory().populateAttributes(data);
		Factories.getAttributeFactory().populateAttributes(data2);
		
		assertTrue("Expected one attribute and got " + data.getAttributes().size(),data.getAttributes().size() == 1);
		assertTrue("Expected one attribute and got " + data2.getAttributes().size(),data2.getAttributes().size() == 1);
		
	
	}
}
