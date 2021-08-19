package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.util.StringUtil;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.AuthorizedSearchService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseSearchRequestType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.EntitlementType;
import org.cote.accountmanager.objects.FieldMatch;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ObjectSearchRequestType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.SortQueryType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ColumnEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.OrderEnumType;
import org.cote.accountmanager.objects.types.QueryEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.junit.Test;

public class TestObjectEntitlementQuery extends BaseDataAccessTest {

	private void setupTestData() {
		DirectoryGroupType dDir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Data", testUser);
		assertNotNull("Directory is null", dDir);
		DataType data = newTextData("Search Data 1","This is the text data",testUser,dDir);
		if(data.getDescription() == null) {
			data.setDescription("Inside description 1");
			BaseService.update(AuditEnumType.DATA, data, testUser);
		}
		DataType data2 = newTextData("Search Data 2","This is the text data",testUser,dDir);
		if(data2.getDescription() == null) {
			data2.setDescription("Inside description 2");
			BaseService.update(AuditEnumType.DATA, data2, testUser);
		}
		DataType data3 = newTextData("Search Data 3","This is the text data",testUser,dDir);
		if(data3.getDescription() == null) {
			data3.setDescription("Inside description 1");
			BaseService.update(AuditEnumType.DATA, data3, testUser);
		}
	}
	
	@Test
	public void TestCountObjectByFieldName() {
		setupTestData();
		logger.info("Test search count");
		

		/// NameEnumType.DATA
		DirectoryGroupType dDir = BaseService.makeFind(AuditEnumType.GROUP, "DATA", "~/Data", testUser);
		DataType data3 = newTextData("Search Data 3","This is the text data",testUser,dDir);
		BasePermissionType per1 = null;
		BasePermissionType per2 = null;
		try  {

			per1 = AuthorizationService.getViewPermissionForMapType(NameEnumType.DATA, testUser.getOrganizationId());
			per2 = AuthorizationService.getViewPermissionForMapType(NameEnumType.GROUP, testUser.getOrganizationId());
			AuthorizationService.authorizeType(testUser, testUser2, dDir, true, false, false, false);
			AuthorizationService.authorizeType(testUser, testUser2, data3, false, false, false, false);
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			
		} catch (FactoryException | DataAccessException | ArgumentException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		assertNotNull("Permission is null", per1);
		ObjectSearchRequestType search = new ObjectSearchRequestType();
		search.setObjectType(NameEnumType.DATA);
		SortQueryType sort = new SortQueryType();
		sort.setSortField(QueryEnumType.NAME);
		sort.setSortOrder(OrderEnumType.ASCENDING);
		search.setSort(sort);
		search.setStartRecord(0L);
		search.setRecordCount(10);
		search.setIncludeThumbnail(false);
		search.setGroupScope(dDir.getObjectId());
		FieldMatch m = new FieldMatch();
		m.setDataType(SqlDataEnumType.TEXT);
		m.setComparator(ComparatorEnumType.LIKE);
		m.setEncodedValue("text*");
		m.setFieldName(ColumnEnumType.MIMETYPE);
		search.getFields().add(m);

		List<EntitlementType> ents = AuthorizedSearchService.searchForEffectiveMemberEntitlements(search, testUser2, new Long[] {per1.getId(),per2.getId()});
		logger.info("Received " + ents.size());
		assertTrue("Expected a matching set of entitlements", ents.size() > 0);

		List<BaseGroupType> dataL = AuthorizedSearchService.searchByEffectiveMemberEntitlement(search, testUser2);
		logger.info("Results: " + dataL.size());
	}
	
}
