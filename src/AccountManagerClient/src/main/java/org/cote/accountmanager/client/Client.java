package org.cote.accountmanager.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.client.util.CacheUtil;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.types.NameEnumType;

public class Client {
	public static final Logger logger = LogManager.getLogger(Client.class);
	private Client() {
		
	}
	public static int countObjects(ClientContext context, NameIdDirectoryGroupType parent, NameEnumType objectType, String directoryName)
	{
		return countObjects(context, parent, objectType, directoryName, true);
	}
	public static int countObjects(ClientContext context, NameIdDirectoryGroupType parent, NameEnumType objectType, String directoryName, boolean useCache)
	{
		int count = 0;
		String path = parent.getGroupPath() + "/" + directoryName;
		DirectoryGroupType dir = getCacheGroup(context, path);
		if(dir != null) {
			count = (useCache ? getCacheCount(context, dir.getObjectId(), objectType) : AM6Util.count(context, objectType, dir.getObjectId()));;
		}
		return count;
	}
	public static DirectoryGroupType getCacheGroup(ClientContext context, String path) {
		String key = "GROUP-DATA-" + path;
		DirectoryGroupType dir = CacheUtil.readCache(context, key, DirectoryGroupType.class);
		if(dir == null) {
			dir = AM6Util.findObject(context, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", path);
			if(dir != null) {
				CacheUtil.cache(context, key, dir);
			}
		}
		return dir;
	}
	public static int getCacheCount(ClientContext context, String dirObjectId, NameEnumType type) {
		String key = "COUNT-" + type.toString() + "-" + dirObjectId;
		Integer countI = CacheUtil.readCache(context, key, Integer.class);
		int count = 0;
		if(countI != null) count = countI.intValue();
		else {
			count = AM6Util.count(context, type, dirObjectId);
			CacheUtil.cache(context, key, count);
		}
		return count;
	}
}
