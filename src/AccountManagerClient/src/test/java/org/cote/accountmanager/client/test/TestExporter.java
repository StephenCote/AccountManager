package org.cote.accountmanager.client.test;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.JSONUtil;
import org.junit.Test;

public class TestExporter extends BaseClientTest {

	@Test
	public void TestUserExport() {
		logger.info("Test Exorter");
		
		
		testUserContext.getUser();
		
		assertTrue("Failed to cache user", cacheUser(testUserContext,testUserContext.getUser(),"./export"));
		
		
	}
	
	private boolean cacheUser(ClientContext context, UserType user, String cacheBase) {
		String cachePath = cacheBase + user.getOrganizationPath() + "/" + user.getName();
		
		if(!FileUtil.makePath(cachePath)) {
			logger.error("Failed to emit path: " + cachePath);
			return false;
		}
		if(!FileUtil.emitFile(cachePath + "/$user.json",JSONUtil.exportObject(user))) {
			logger.error("Failed to export user object");
			return false;
		}
		return cacheDataGroups(context, user, cachePath, user.getHomeDirectory());
		// return true;
	}
	
	private boolean cacheDataGroups(ClientContext context, UserType user, String cacheBase, BaseGroupType group) {
		String cachePath = cacheBase + "/" + group.getName();
		FileUtil.makePath(cachePath);
		FileUtil.emitFile(cachePath + "/$group.json",JSONUtil.exportObject(group));
		
		
		List<DirectoryGroupType> subGroups = AM6Util.list(context, DirectoryGroupType.class, NameEnumType.GROUP, group.getObjectId(), 0L, 0);
		for(BaseGroupType subGroup : subGroups) {
			cacheDataGroups(context, user, cachePath, subGroup);
		}
		

		List<DataType> dataL = AM6Util.list(context, DataType.class, NameEnumType.DATA,  group.getObjectId(), 0L, 0);

		for(DataType data : dataL) {
			DataType uData = data;
			if(uData.getDetailsOnly()) {
				uData = AM6Util.getObject(context, DataType.class, NameEnumType.DATA, uData.getObjectId());
			}
			FileUtil.emitFile(cachePath + "/" + uData.getName(),JSONUtil.exportObject(uData));
			
		}
		

		
		return true;
	}
	
}
