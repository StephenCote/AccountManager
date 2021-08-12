package org.cote.accountmanager.client.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.FieldMatch;
import org.cote.accountmanager.objects.ObjectSearchRequestType;
import org.cote.accountmanager.objects.SortQueryType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.OrderEnumType;
import org.cote.accountmanager.objects.types.QueryEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.junit.Test;

public class TestSearch extends BaseClientTest {

	private void createData(ClientContext context, String[] dataNames) {
		for(String name : dataNames) {
			DataType data1 = getCreateData(context, "SearchDirectory",name,"This is some text data");
			assertNotNull("Failed to create data", data1);
		}
	}
	
	private ObjectSearchRequestType getSearchRequest(NameEnumType objectType, List<FieldMatch> fields, long startIndex, int recordCount) {
		ObjectSearchRequestType request = new ObjectSearchRequestType();
		request.setObjectType(objectType);
		SortQueryType sort = new SortQueryType();
		sort.setSortField(QueryEnumType.NAME);
		sort.setSortOrder(OrderEnumType.ASCENDING);
		request.setSort(sort);
		request.getFields().addAll(fields);
		return request;
	}
	
	private FieldMatch getStringFieldMatch(ColumnEnumType fieldName, ComparatorEnumType compType, String pattern) {
		FieldMatch match = new FieldMatch();
		match.setComparator(compType);
		match.setDataType(SqlDataEnumType.TEXT);
		match.setEncodedValue(pattern);
		match.setFieldName(fieldName);
		return match;
	}
	
	@Test
	public void TestSearchForData() {
		logger.info("Test Search For Data");
		createData(testUserContext, new String[] {
			"Search Data Alpha",
			"Search Data Bravo",
			"Search Data Charlie",
			"Search Data Delta",
			"Search Data Echo",
			"Search Data FoxTrot",
			"Search Data Golf",
			"Search Data Hotel",
			"Search Data Indigo",
			"Search Data Juliet",
			"Search Data Lima"
		});

		ObjectSearchRequestType request = getSearchRequest(NameEnumType.DATA, Arrays.asList(getStringFieldMatch(ColumnEnumType.NAME, ComparatorEnumType.LIKE, "Search Data*")),0,0);
		int count = AM6Util.count(testUserContext, request);
		logger.info("Received count: " + count);
		assertTrue("Expected a positive count", count > 0);
		
		List<DataType> searchData = AM6Util.search(testUserContext, DataType.class, request);
		logger.info("Received " + searchData.size() + " items");
		assertTrue("Expected a positive set of results", searchData.size() > 0);
	}
}
