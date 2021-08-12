package org.cote.accountmanager.client.test;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.DataUtil;
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
		if(group.getName().equals(".thumbnail")) return false;
		String cachePath = cacheBase + "/" + group.getName();
		logger.info("Caching " + group.getUrn());
		FileUtil.makePath(cachePath);
		FileUtil.emitFile(cachePath + "/$group.json",JSONUtil.exportObject(group));
		

		List<DirectoryGroupType> subGroups = AM6Util.list(context, DirectoryGroupType.class, NameEnumType.GROUP, group.getObjectId(), 0L, 0);
		if(subGroups.size() > 0) logger.info("Processing " + subGroups.size() + " child groups");
		
		for(BaseGroupType subGroup : subGroups) {
			cacheDataGroups(context, user, cachePath, subGroup);
		}
		
		if(group.getGroupType().equals(GroupEnumType.DATA)) {
			logger.info("Caching data groups");
			List<?> dataL = AM6Util.list(context, DataType.class, NameEnumType.DATA,  group.getObjectId(), 0L, 0);
			cacheGroupList(context, (List<NameIdType>)dataL, cachePath);
			dataL = AM6Util.list(context, BaseTagType.class, NameEnumType.TAG,  group.getObjectId(), 0L, 0);
			cacheGroupList(context, (List<NameIdType>)dataL, cachePath);
			dataL = AM6Util.list(context, AccountType.class, NameEnumType.ACCOUNT,  group.getObjectId(), 0L, 0);
			cacheGroupList(context, (List<NameIdType>)dataL, cachePath);
			dataL = AM6Util.list(context, PersonType.class, NameEnumType.PERSON,  group.getObjectId(), 0L, 0);
			cacheGroupList(context, (List<NameIdType>)dataL, cachePath);
		}
		else if(group.getGroupType().equals(GroupEnumType.BUCKET)) {
			List<?> members = AM6Util.listMembers(context, DataType.class, "GROUP", group.getObjectId(), "DATA");
			cacheMemberList(context, (List<NameIdType>)members, cachePath);

		}


		/*


		for(DataType data : dataL) {
			DataType uData = data;
			if(uData.getDetailsOnly()) {
				uData = AM6Util.getObject(context, DataType.class, NameEnumType.DATA, uData.getObjectId());
			}
			FileUtil.emitFile(cachePath + "/" + uData.getName(),JSONUtil.exportObject(uData));
			
		}
		*/

		
		return true;
	}
	private void cacheMemberList(ClientContext context, List<NameIdType> objs, String cachePath) {
		List<String> members = new ArrayList<String>();
		for(NameIdType obj : objs) {
			members.add(obj.getUrn());
		}
		FileUtil.emitFile(cachePath + "/$members.json",JSONUtil.exportObject(members));
	}
	private void cacheGroupList(ClientContext context, List<NameIdType> objs, String cachePath) {
		for(NameIdType obj : objs) {
			NameIdType useObj = obj;
			if(obj.getNameType().equals(NameEnumType.DATA)) {
				DataType uData = (DataType)obj;
				if(uData.getDetailsOnly()) {
					useObj = AM6Util.getObject(context, DataType.class, NameEnumType.DATA, uData.getObjectId());
				}
				((DataType)useObj).setPointer(false);
			}
			if(useObj == null) {
				logger.error("Null object: " + (obj != null ? obj.getUrn() : " in " + cachePath));
				continue;
			}

			FileUtil.emitFile(cachePath + "/" + useObj.getName() + ".json",JSONUtil.exportObject(useObj));
			if(obj.getNameType().equals(NameEnumType.DATA)) {
				try {
					FileUtil.emitFile(cachePath + "/" + useObj.getName(),DataUtil.getValue((DataType)useObj));
				} catch (DataException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}
	
}
