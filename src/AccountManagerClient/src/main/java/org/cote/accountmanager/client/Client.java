package org.cote.accountmanager.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.client.util.CacheUtil;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;

public class Client {
	public static final Logger logger = LogManager.getLogger(Client.class);
	
	/// Epoch evolutions is the number of evolution events created for each epoch.
	/// The number is kept at 12 so that each evolution may be considered a month and each epoch a standard Earth year
	///
	private static int epochEvolutions = 12;
	private Client() {
		
	}
	
	public static int countPeople(ClientContext context, NameIdDirectoryGroupType parent)
	{
		return countObjects(context, parent, NameEnumType.PERSON, "Persons");
	}
	public static int countTraits(ClientContext context, NameIdDirectoryGroupType parent)
	{
		return countObjects(context, parent, NameEnumType.TRAIT, "Traits");
	}
	public static int countLocations(ClientContext context, NameIdDirectoryGroupType parent)
	{
		return countObjects(context, parent, NameEnumType.LOCATION, "Locations");
	}
	public static int countEvents(ClientContext context, NameIdDirectoryGroupType parent)
	{
		return countObjects(context, parent, NameEnumType.EVENT, "Events", false);
	}
	public static boolean evolveProjectRegions(ClientContext context, LifecycleType community, ProjectType project, int epochCount) {
		boolean outBool = false;
		int ecount = countEvents(context, project);
		logger.info("Epoch size: " + ecount);
		if(ecount == 0) {
			logger.error("Epoch count must be 1 (by loading project region)");
			return outBool;
		}
		outBool = AM6Util.evolveCommunityProjectRegion(context,  Boolean.class, community.getObjectId(), project.getObjectId(), epochCount, epochEvolutions);
		return outBool;
	}
	public static boolean loadProjectRegions(ClientContext context, LifecycleType community, ProjectType project, int locationSize, int seedSize) {
		boolean outBool = false;
		int pcount = countPeople(context, project);
		logger.info("People count: " + pcount);
		if(pcount == 0) {
			boolean loadRegion = AM6Util.configureCommunityProjectRegion(context, Boolean.class, community.getObjectId(), project.getObjectId(), locationSize, seedSize);
			if(!loadRegion) {
				logger.error("Failed to configure community region");
				return false;
			}
		}
		outBool = true;
		return outBool;
	}
	public static boolean loadCommunityCountryInformation(ClientContext context, LifecycleType community, String countryCodes) {
		boolean outBool = false;
		
		int tcount = countTraits(context, community);
		logger.info("Trait count: " + tcount);
		if(tcount == 0) {
			boolean traits = AM6Util.configureCommunityTraits(context, Boolean.class, community.getObjectId());
			if(!traits) {
				logger.error("Failed to configure traits");
				return false;
			}
		}
		int lcount = countLocations(context, community);
		if(lcount == 0) {
			
			/// Note: These are all synchronous calls, so there will likely be connection timeouts for large volumes of data
			/// These need to be made async calls
			
			logger.info("Configuring country data in community " + community.getName());
			boolean cinfo = AM6Util.configureCommunityCountryInfo(context, Boolean.class, community.getObjectId());
			if(!cinfo) {
				logger.error("Failed to configure country information");
				return false;
			}
			logger.info("Configuring administrative codes in community " + community.getName());
			boolean admin1Codes = AM6Util.configureAdmin1Codes(context, Boolean.class, community.getObjectId());
			if(!admin1Codes) {
				logger.error("Failed to configure admin1codes");
				return false;
			}	
			logger.info("Configuring additional administrative codes in community " + community.getName());
			boolean admin2Codes = AM6Util.configureAdmin2Codes(context, Boolean.class, community.getObjectId());
			if(!admin2Codes) {
				logger.error("Failed to configure admin2codes");
				return false;
			}
			logger.info("Configuring country data " + countryCodes + " in community " + community.getName());
			boolean countryData = AM6Util.configureCommunityCountryData(context, Boolean.class, community.getObjectId(), countryCodes);
			if(!countryData) {
				logger.error("Failed to load country data");
				return false;
			}
			outBool = true;
		}
		else {
			outBool = true;
		}
		return outBool;
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
