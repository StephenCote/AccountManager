/*
 * RocketCommunity is an adaption of the original RocketWeb service for community activities.
 * It's been pushed into the accelerant library then exposed back via the common interface
 * In the event somebody wants to make an alternate version
 */

package org.cote.rocket;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.ScriptEngine;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.ICommunityProvider;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.ScriptService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserPermissionType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.service.rest.BaseService;
import org.cote.accountmanager.util.DataUtil;
import org.cote.propellant.objects.EventType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.LocationType;
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.TraitType;
import org.cote.propellant.objects.types.GeographyEnumType;
import org.cote.propellant.objects.types.TraitEnumType;
import org.cote.rocket.factory.EventFactory;
import org.cote.rocket.factory.LifecycleFactory;
import org.cote.rocket.factory.LocationFactory;
import org.cote.rocket.factory.ProjectFactory;
import org.cote.rocket.factory.TraitFactory;
import org.cote.rocket.util.DataGeneratorUtil;



public class RocketCommunity implements ICommunityProvider {
	private static Map<String,String> geoIdToPost = new HashMap<>();
	private static Map<String,LocationType> locByCode = new HashMap<>();
	
	public static final Logger logger = LogManager.getLogger(RocketCommunity.class);
	private static final CSVFormat csvFileFormat = CSVFormat.DEFAULT.withDelimiter('\t').withAllowMissingColumnNames(true).withQuote(null);
	public RocketCommunity(){
		
	}
	public void clearCache(){
		geoIdToPost.clear();
		locByCode.clear();
	}
	public boolean importLocationTraits(UserType user, AuditEnumType auditType, String objectId, String locationPath, String featuresFileName){
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Location Features",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LOCATION, objectId);
		NameIdDirectoryGroupType obj = null;
		try {
			obj = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.valueOf(auditType.toString()))).getByObjectId(objectId, user.getOrganizationId());
			if(obj == null){
				AuditService.denyResult(audit, "Null community object");
				return out_bool;
			}
			BaseService.normalize(user, obj);
			if(AuthorizationService.canChange(user, obj)==false){
				AuditService.denyResult(audit, "User is not authorized to change lifecycle");
				return out_bool;
			}
			DirectoryGroupType lcGroup = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, obj.getGroupPath(), user.getOrganizationId());
			DirectoryGroupType locDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Traits", lcGroup, user.getOrganizationId());

			if(AuthorizationService.canChange(user, locDir) == false){
				AuditService.denyResult(audit, "User is not authorized to change the directory");
				return out_bool;
			}
			
			

			String path = locationPath + featuresFileName;
			logger.info("Loading traits from " + path + " into " + locDir.getUrn());

			BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
			CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();

			for(CSVRecord record : csvFileParser){
				TraitType trait = ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).newTrait(user, locDir.getId());
				trait.setName(record.get(0));
				trait.setDescription(record.get(2));
				trait.getAttributes().add(Factories.getAttributeFactory().newAttribute(trait, "code", record.get(1)));
				trait.setTraitType(TraitEnumType.LOCATION);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TRAIT, trait);
			}
			bir.close();
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			
			AuditService.permitResult(audit, "Loaded traits");
			out_bool = true;
			
		} catch (FactoryException | ArgumentException | DataAccessException | IOException e) {
			// TODO Auto-generated catch block
			logger.error("Error",e);
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		}
		
		/*
		 * 		dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newDirectoryGroup(user,path.substring(path.lastIndexOf("/") + 1, path.length()), parentDir, user.getOrganizationId());
				if(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).add(dir)){
					dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, path, user.getOrganizationId());
				}
				else{
					logger.error("Failed to add group");
					return;
				}
			}
			if(AuthorizationService.canChange(user, dir) == false){
				logger.error("Cannot change specified path");
				return;
			}
		 */
		return out_bool;
	}
	
	public boolean importLocationCountryInfo(UserType user, AuditEnumType auditType, String objectId, String locationPath, String featuresFileName){
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Location Features",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LOCATION, objectId);
		NameIdDirectoryGroupType obj = null;
		try {
			obj = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.valueOf(auditType.toString()))).getByObjectId(objectId, user.getOrganizationId());
			if(obj == null){
				AuditService.denyResult(audit, "Null community object");
				return out_bool;
			}
			BaseService.normalize(user, obj);
			if(AuthorizationService.canChange(user, obj)==false){
				AuditService.denyResult(audit, "User is not authorized to change lifecycle");
				return out_bool;
			}
			DirectoryGroupType lcGroup = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, obj.getGroupPath(), user.getOrganizationId());
			DirectoryGroupType locDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Locations", lcGroup, user.getOrganizationId());

			if(AuthorizationService.canChange(user, locDir) == false){
				AuditService.denyResult(audit, "User is not authorized to change the directory");
				return out_bool;
			}
			
			

			String path = locationPath + featuresFileName;
			logger.info("Loading traits from " + path + " into " + locDir.getUrn());

			BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
			CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			
			locByCode.clear();
			for(CSVRecord record : csvFileParser){
				String iso = record.get(0);
				if(iso.startsWith("#")) continue;
				LocationType location = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(user, locDir.getId());
				location.setName(record.get(4));
				location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "name", record.get(4)));
				location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "iso", record.get(0)));
				location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "iso3", record.get(1)));
				location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "capital", record.get(5)));
				location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "currencyCode", record.get(10)));
				location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "currencyName", record.get(11)));
				location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "languages", record.get(15)));
				location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "neighbors", record.get(17)));

				location.setGeographyType(GeographyEnumType.PHYSICAL);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LOCATION, location);
			}
			bir.close();
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			
			AuditService.permitResult(audit, "Loaded country info");
			out_bool = true;
			
		} catch (FactoryException | ArgumentException | DataAccessException | IOException e) {
			// TODO Auto-generated catch block
			logger.error("Error",e);
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		}
		
		return out_bool;
	}
	
	public boolean importLocationAdmin1Codes(UserType user, AuditEnumType auditType, String objectId, String locationPath, String featuresFileName){
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Location Features",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LOCATION, objectId);
		NameIdDirectoryGroupType obj = null;
		try {
			obj = ((NameIdGroupFactory)Factories.getFactory(FactoryEnumType.valueOf(auditType.toString()))).getByObjectId(objectId, user.getOrganizationId());
			if(obj == null){
				AuditService.denyResult(audit, "Null community object");
				return out_bool;
			}
			BaseService.normalize(user, obj);
			if(AuthorizationService.canChange(user, obj)==false){
				AuditService.denyResult(audit, "User is not authorized to change lifecycle");
				return out_bool;
			}
			DirectoryGroupType lcGroup = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, obj.getGroupPath(), user.getOrganizationId());
			DirectoryGroupType locDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Locations", lcGroup, user.getOrganizationId());

			if(AuthorizationService.canChange(user, locDir) == false){
				AuditService.denyResult(audit, "User is not authorized to change the directory");
				return out_bool;
			}
			
			

			String path = locationPath + featuresFileName;
			logger.info("Loading Admin1Codes from " + path + " into " + locDir.getUrn());
			logger.info("Reading Admin 1 Codes");
			BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
			CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

			//((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).clearCache();
			
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			//Map <String, Integer> nameMap = new HashMap<>();
			locByCode.clear();
			for(CSVRecord record : csvFileParser){
				String codePair = record.get(0);
				if(codePair == null || codePair.length() == 0){
					logger.info("Skip invalid code");
					continue;
				}
				String[] code = codePair.split("\\.");
				if(code.length != 2){
					logger.info("Invalid pattern for '" + codePair + "'");
					continue;
				}
				String countryIso = code[0];
				String adminCode = code[1];
				String name = record.get(3).trim();
				LocationType countryLoc = findLocationByAdminCode(null, locDir, "iso", countryIso, user.getOrganizationId());
				if(countryLoc == null){
					logger.error("Failed to find country '" + countryIso + "'");
					continue;
				}

				LocationType admin1Loc = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(user, countryLoc);
				admin1Loc.setName(name);
				admin1Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin1Loc, "codeType", "admin1"));
				admin1Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin1Loc, "name", record.get(1).trim()));
				admin1Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin1Loc, "code", codePair));
				admin1Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin1Loc, "geonameid", record.get(3)));
				admin1Loc.setGeographyType(GeographyEnumType.PHYSICAL);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LOCATION, admin1Loc);

				
			}
			bir.close();
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			
			AuditService.permitResult(audit, "Loaded admin 1 codes");
			out_bool = true;
			
		} catch (FactoryException | ArgumentException | DataAccessException | IOException e) {
			// TODO Auto-generated catch block
			logger.error("Error",e);
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		}
		
		return out_bool;
	}
	
	public boolean importLocationAdmin2Codes(UserType user, AuditEnumType auditType, String objectId, String locationPath, String featuresFileName){
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Location Features",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LOCATION, objectId);
		NameIdDirectoryGroupType obj = null;
		try {
			obj = ((NameIdGroupFactory)Factories.getFactory(FactoryEnumType.valueOf(auditType.toString()))).getByObjectId(objectId, user.getOrganizationId());
			if(obj == null){
				AuditService.denyResult(audit, "Null community object");
				return out_bool;
			}
			BaseService.normalize(user, obj);
			if(AuthorizationService.canChange(user, obj)==false){
				AuditService.denyResult(audit, "User is not authorized to change lifecycle");
				return out_bool;
			}
			DirectoryGroupType lcGroup = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, obj.getGroupPath(), user.getOrganizationId());
			DirectoryGroupType locDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Locations", lcGroup, user.getOrganizationId());

			if(AuthorizationService.canChange(user, locDir) == false){
				AuditService.denyResult(audit, "User is not authorized to change the directory");
				return out_bool;
			}
			
			

			String path = locationPath + featuresFileName;
			logger.info("Loading Admin2Codes from " + path + " into " + locDir.getUrn());
			logger.info("Reading Admin 2 Codes");
			BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
			CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

			String sessionId = BulkFactories.getBulkFactory().newBulkSession();

			String lastAdminLoc = null;
			LocationType lastLoc = null;
			//Map <String, Integer> nameMap = new HashMap<>();
			int counter = 0;
			for(CSVRecord record : csvFileParser){
				String codePair = record.get(0);
				if(codePair == null || codePair.length() == 0){
					logger.info("Skip invalid code");
					continue;
				}
				String[] code = codePair.split("\\.");
				if(code.length != 3){
					logger.info("Invalid pattern for '" + codePair + "'");
					continue;
				}
				
				String countryIso = code[0];
				String adminCode1 = code[1];
				String adminCode2 = code[1];
				//String name = record.get(2).trim();
				String name = record.get(3).trim();
				LocationType admin1Loc = null;
				String adminCode = countryIso + "." + adminCode1;
				if(lastLoc == null || lastAdminLoc == null || adminCode.equals(lastAdminLoc) == false){
					lastAdminLoc = adminCode;
					
					admin1Loc = findLocationByAdminCode(null, locDir, "code", adminCode, user.getOrganizationId());

					if(admin1Loc == null){
						logger.warn("Failed to find admin location '" + countryIso + "." + adminCode1);
						admin1Loc = findLocationByAdminCode(null, locDir, "iso", countryIso, user.getOrganizationId());
						if(admin1Loc == null){
							logger.error("Failed to find country '" + countryIso + "'");
							continue;
						}
					}
					lastLoc = admin1Loc;
				}
				else{
					admin1Loc = lastLoc;
				}

				LocationType admin2Loc = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(user, admin1Loc);
				admin2Loc.setName(name);
				admin2Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin2Loc, "codeType", "admin2"));
				admin2Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin2Loc, "code", codePair));
				admin2Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin2Loc, "geonameid", record.get(3)));
				admin2Loc.getAttributes().add(Factories.getAttributeFactory().newAttribute(admin2Loc, "name", record.get(2).trim()));
				admin2Loc.setGeographyType(GeographyEnumType.PHYSICAL);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LOCATION, admin2Loc);

				if(counter++ > 0 && (counter % 1000) == 0){
					BulkFactories.getBulkFactory().write(sessionId);
					BulkFactories.getBulkFactory().close(sessionId);
					sessionId = BulkFactories.getBulkFactory().newBulkSession();
				}
				
				
			}
			bir.close();
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			AuditService.permitResult(audit, "Loaded admin 2 codes");
			out_bool = true;
			
		} catch (FactoryException | ArgumentException | DataAccessException | IOException e) {
			// TODO Auto-generated catch block
			logger.error("Error",e);
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		}
		
		return out_bool;
	}
	
	public boolean importLocationCountryData(UserType user, AuditEnumType auditType, String objectId, String locationPath, String codes, String alternate){
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Location Features",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LOCATION, objectId);
		NameIdDirectoryGroupType obj = null;
		try {
			obj = ((NameIdGroupFactory)Factories.getFactory(FactoryEnumType.valueOf(auditType.toString()))).getByObjectId(objectId, user.getOrganizationId());
			if(obj == null){
				AuditService.denyResult(audit, "Null community object");
				return out_bool;
			}
			BaseService.normalize(user, obj);
			if(AuthorizationService.canChange(user, obj)==false){
				AuditService.denyResult(audit, "User is not authorized to change lifecycle");
				return out_bool;
			}
			DirectoryGroupType lcGroup = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, obj.getGroupPath(), user.getOrganizationId());
			DirectoryGroupType locDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Locations", lcGroup, user.getOrganizationId());

			if(AuthorizationService.canChange(user, locDir) == false){
				AuditService.denyResult(audit, "User is not authorized to change the directory");
				return out_bool;
			}
			
			

			//String path = locationPath + featuresFileName;
			//logger.info("Loading country from " + path + " into " + locDir.getUrn());
			if(alternate != null && alternate.length() > 0){
				bufferPostalCodes(csvFileFormat, locationPath + alternate);
			}
			
			BufferedReader bir = null;
			
			CSVParser  csvFileParser = null;
			String sessionId = null;
			//Map <String, Integer> nameMap = new HashMap<>();


			String[] countryList = codes.split(",");
			
			for(int c = 0; c < countryList.length; c++){
				logger.info("Reading country data ... " + countryList[c]);
				String path = locationPath + countryList[c] + ".txt";

				LocationType countryLoc = findLocationByAdminCode(null, locDir,"iso", countryList[c], user.getOrganizationId());
				if(countryLoc == null){
					logger.error("Failed to find parent country for " + countryList[c]);
					continue;
				}
				bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
				csvFileParser = new CSVParser(bir, csvFileFormat);
	 
				sessionId = BulkFactories.getBulkFactory().newBulkSession();
				int counter = 0;
				//nameMap.clear();
				locByCode.clear();
				Set<String> adminKey = new HashSet<>();
				for(CSVRecord record : csvFileParser){
					String geoid = record.get(0);
					String name = geoid;
					String feature = record.get(6) + "." + record.get(7);
					if(!feature.equals("P.PPL")){
						continue;
					}
					if(counter++ > 0 && (counter % 1000) == 0){
						BulkFactories.getBulkFactory().write(sessionId);
						BulkFactories.getBulkFactory().close(sessionId);
						sessionId = BulkFactories.getBulkFactory().newBulkSession();
					}
					
					/// Need to map in value from alternate name
					///
					String adminCode1 = record.get(10);
					String adminCode2 = record.get(11);
					if(adminCode1 == null){
						logger.error("No admin code defined for '" + name + "'");
						continue;
					}
					String regionLocationCode = countryList[c] + "." + adminCode1 + (adminCode2 != null && adminCode2.length() > 0 ? "." + adminCode2 : "");
					LocationType regionLocation = findLocationByAdminCode(null, locDir,"code", regionLocationCode, user.getOrganizationId());
					if(regionLocation == null){
						logger.debug("Failed to find region location for " + geoid + " with code: '" + regionLocationCode + "' in group " + locDir.getUrn());
						if(regionLocation == null && adminCode2 != null){
							regionLocation = findLocationByAdminCode(null, locDir, "code", countryList[c] + "." + adminCode1, user.getOrganizationId());
						}
						if(regionLocation == null){
							logger.error("Failed to find region location for " + geoid);
							continue;
						}
						
						continue;
					}
					
					LocationType location = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(user, regionLocation);
					location.setName(name);
					location.setGeographyType(GeographyEnumType.PHYSICAL);
					location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "geonameid", geoid));
					location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "name", record.get(1).trim()));

					location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "latitude", record.get(4)));
					location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "longitude", record.get(5)));
					

					location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "feature", feature));
					if(geoIdToPost.containsKey(geoid)){
						location.getAttributes().add(Factories.getAttributeFactory().newAttribute(location, "post", geoIdToPost.get(geoid)));
					}
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LOCATION, location);

				}
				BulkFactories.getBulkFactory().write(sessionId);
				BulkFactories.getBulkFactory().close(sessionId);
				bir.close();
			}
			
			
			AuditService.permitResult(audit, "Loaded country data");
			out_bool = true;
			
		} catch (FactoryException | ArgumentException | DataAccessException | IOException e) {
			// TODO Auto-generated catch block
			logger.error("Error",e);
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		}
		
		return out_bool;
	}
	
	private static void bufferPostalCodes(CSVFormat csvFileFormat, String path) throws IOException{
		
		if(geoIdToPost.size() > 0){
			logger.info("Postal codes already buffered");
			return;
		}
		logger.info("Buffering postal values ...");
		geoIdToPost.clear();
		BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
		CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

		int counter = 0;

		for(CSVRecord record : csvFileParser){
			if(record.get(2).equals("post") && record.get(3) != null && record.get(3).length() > 0){
				geoIdToPost.put(record.get(1), record.get(3));
			}
		}
		
		bir.close();
		logger.info("Mapped " + geoIdToPost.keySet().size() + " postal codes");
	}
	private static  LocationType findLocationByAdminCode(LocationType parent, DirectoryGroupType dir, String codeName, String code, long organizationId){
		LocationType location = null;
		
		String key = dir.getId() + "-" + codeName + "-" + code;
		if(locByCode.containsKey(key)) return locByCode.get(key);
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setJoinAttribute(true);

		List<QueryField> fields = new ArrayList<>();
		fields.add(QueryFields.getFieldGroup(dir.getId()));
		fields.add(QueryFields.getStringField("ATR.name", codeName));
		fields.add(QueryFields.getStringField("ATR.value", code));
		//fields.add(QueryFields.getBigIntField("ATR.organizationId", organizationId))
		if(parent != null){
			fields.add(QueryFields.getFieldParent(parent));
		}
		try{
			List<LocationType> locations = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).list(fields.toArray(new QueryField[0]), instruction, organizationId);
			if(locations.size() > 0){
				location = locations.get(0);
				locByCode.put(key, location);
			}
		} catch (ArgumentException | FactoryException e) {
			
			logger.error("Error",e);
		
		}

		return location;
	}
	
	public BaseRoleType getCommunityProjectRoleBase(UserType user, String projectId){

		BaseRoleType outRole = null;
		ProjectType proj = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "Get Role Base",AuditEnumType.USER, user.getUrn());
		try {
			proj = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getByObjectId(projectId,user.getOrganizationId());
			if(proj == null){
				AuditService.denyResult(audit, "Invalid project: " + projectId);
				return null;
			}
			UserRoleType lcr = RocketSecurity.getProjectRoleBucket(proj);
			if(lcr == null){
				AuditService.denyResult(audit, "Invalid project role bucket");
				return null;
			}
			AuditService.targetAudit(audit, AuditEnumType.ROLE,lcr.getName());
			if(AuthorizationService.canView(user, lcr)){
				outRole = lcr;
				AuditService.permitResult(audit, "Authorized to view roles");
			}
			else{
				AuditService.denyResult(audit, "Not authorized to view roles");
			}
			
		} catch (FactoryException | ArgumentException e) {
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return outRole;
	}
	
	public BasePermissionType getCommunityProjectPermissionBase(UserType user, String projectId){

		BasePermissionType outPermission = null;
		ProjectType proj = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Get Permission Base",AuditEnumType.USER, user.getUrn());

		try {
			proj = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getByObjectId(projectId, user.getOrganizationId());
			if(proj == null){
				AuditService.denyResult(audit, "Invalid project: " + projectId);
				return null;
			}
			UserPermissionType lcr = RocketSecurity.getProjectPermissionBucket(proj);
			if(lcr == null){
				AuditService.denyResult(audit, "Invalid project permission bucket");
				return null;
			}
			AuditService.targetAudit(audit, AuditEnumType.PERMISSION,lcr.getName());
			if(AuthorizationService.canView(user, lcr)){
				outPermission = lcr;
				AuditService.permitResult(audit, "Authorized to view permissions");
			}
			else{
				AuditService.denyResult(audit, "Not authorized to view permissions");
			}

		} catch (FactoryException | ArgumentException e) {

			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return outPermission;
	}
	
	public <T> T getCommunity(UserType user, String name){
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Get Community Roles",AuditEnumType.USER,user.getUrn());

		DirectoryGroupType dir=null;
		LifecycleType lc = null;
		try {
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(name, Rocket.getLifecycleGroup(user.getOrganizationId()),user.getOrganizationId());
			if(dir != null) lc = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getByNameInGroup(name, dir);
			AuditService.targetAudit(audit, AuditEnumType.LIFECYCLE, (lc != null ? lc.getUrn() : "null"));
			if(lc != null && RocketSecurity.canReadLifecycle(user, lc)){
				AuditService.permitResult(audit, "Authorized to view");
			}
			else{
				lc = null;
				AuditService.denyResult(audit, "Lifecycle is null or user is not authorized to view");
			}
		} catch (FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit,"Error: " + e.getMessage());
		}
		
		return (T)lc;
	}
	
	public <T> T getCommunityProject(UserType user, String communityName, String projectName){

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "getCommunityProject",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.PROJECT, projectName);

		LifecycleType lc = null;
		ProjectType proj = null;
		try {
			lc = Rocket.getLifecycle(communityName, user.getOrganizationId());
		} catch (FactoryException | ArgumentException e) {
			
			logger.error("Error",e);
		}
		if(lc == null){
			AuditService.denyResult(audit,"Lifecycle " + communityName + " doesn't exist.");
			return null;
		}
		try {
			proj = Rocket.getProject(projectName, lc, user.getOrganizationId());
			if(proj == null){
				AuditService.denyResult(audit, "Project " + projectName + " doesn't exist");
				return null;
			}
			if(RocketSecurity.canReadProject(user,proj) == false){
				AuditService.denyResult(audit, "Permission denied");
				return null;
			}
			AuditService.permitResult(audit, "Returning project " + projectName + " in lifecycle " + communityName);
		} catch (FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, "Error: "  + e.getMessage());
			logger.error("Error",e);
		} 
		return (T) proj;

	}
	
	public List<BaseRoleType> getCommunityProjectRoles(UserType user, String projectId){
		List<BaseRoleType> roles = new ArrayList<BaseRoleType>();
		ProjectType proj = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Get Roles",AuditEnumType.USER, user.getUrn());
		try {
			proj = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getByObjectId(projectId, user.getOrganizationId());
			if(proj == null){
				AuditService.denyResult(audit, "Invalid project: " + projectId);
				return roles;
			}
			UserRoleType lcr = RocketSecurity.getProjectRoleBucket(proj);
			if(lcr == null){
				AuditService.denyResult(audit, "Invalid project role bucket");
				return roles;
			}
			AuditService.targetAudit(audit, AuditEnumType.ROLE,lcr.getName());
			if(AuthorizationService.canView(user, lcr)){
				roles = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleList(RoleEnumType.USER, lcr, 0,0,user.getOrganizationId());
				AuditService.permitResult(audit, "Authorized to view roles");
			}
			else{
				AuditService.denyResult(audit, "Not authorized to view roles");
			}
			
		} catch (FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return roles;
	}
	public List<BaseRoleType> getCommunityRoles(UserType user, String communityId){
		List<BaseRoleType> roles = new ArrayList<BaseRoleType>();
		LifecycleType out_lc = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Get Roles",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LIFECYCLE, Rocket.getBasePath() + "/Lifecycles");
		try {
			out_lc = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getByObjectId(communityId, user.getOrganizationId());
			if(out_lc == null){
				AuditService.denyResult(audit, "Invalid lifecycle: " + communityId);
				return roles;
			}
			UserRoleType lcr = RocketSecurity.getLifecycleRoleBucket(out_lc);
			if(lcr == null){
				AuditService.denyResult(audit, "Invalid lifecycle role bucket");
				return roles;
			}
			AuditService.targetAudit(audit, AuditEnumType.ROLE,lcr.getName());
			if(AuthorizationService.canView(user, lcr)){
				roles = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleList(RoleEnumType.USER, lcr, 0,0,user.getOrganizationId());
				AuditService.permitResult(audit, "Authorized to view roles");
			}
			else{
				AuditService.denyResult(audit, "Not authorized to view roles");
			}
		} catch (FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return roles;
	}
	public List<BaseRoleType> getCommunitiesRoles(UserType user){
		List<BaseRoleType> roles = new ArrayList<BaseRoleType>();
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Get Community Roles",AuditEnumType.USER,user.getUrn());

		try {
			UserRoleType cr = RocketSecurity.getRocketRoles(user.getOrganizationId());
			AuditService.targetAudit(audit, AuditEnumType.ROLE,cr.getName());
			if(AuthorizationService.canView(user, cr)){
				roles = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleList(RoleEnumType.USER, cr, 0,0,user.getOrganizationId());
				AuditService.permitResult(audit, "Authorized to view roles");
			}
			else{
				AuditService.denyResult(audit, "Not authorized to view roles");
			}
		} catch (FactoryException | ArgumentException e) {		
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return roles;
	}
	
	public boolean configureCommunity(UserType adminUser){
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Configure community in organization #" + adminUser.getOrganizationId(), AuditEnumType.USER,adminUser.getUrn());

		try {
			boolean isSysAdmin = RoleService.getIsUserInRole(RoleService.getSystemAdministratorUserRole(adminUser.getOrganizationId()), adminUser);
			if(isSysAdmin == false){
			//if(RoleService.getIsUserInEffectiveRole(RocketSecurity.getAdminRole(adminUser.getOrganizationId()),adminUser) == false && RoleService.isFactoryAdministrator(adminUser, ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT))) == false){
				AuditService.denyResult(audit, "User is not an administrator");
				return out_bool;
			}
			out_bool = Rocket.configureApplicationEnvironment(adminUser);
		} catch (FactoryException | DataAccessException | ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		}
		if(out_bool = true){
			AuditService.permitResult(audit, "Configured community");
		}
		return out_bool;
	}
	public boolean enrollAdminInCommunities(UserType adminUser, String userId){
		boolean out_bool = false;

		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Enroll in community roles",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "Admin roles");

		try{
			UserType user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByObjectId(userId,adminUser.getOrganizationId());
			if(user == null){
				AuditService.denyResult(audit, "Invalid user id");
				return false;
			}
			if(RoleService.getIsUserInEffectiveRole(RocketSecurity.getAdminRole(adminUser.getOrganizationId()),adminUser) == false && RoleService.isFactoryAdministrator(adminUser, ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT))) == false){
				AuditService.denyResult(audit, "Admin user is not an administrator");
				return out_bool;
			}
			
			out_bool = Rocket.enrollAdminInCommunity(audit, user);
			if(out_bool == true){
				AuditService.permitResult(audit, "Configured " + user.getUrn() + " as a community administrator");
			}
		}
		catch(FactoryException e){
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		} catch (ArgumentException e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		}
		

		return out_bool;
	}
	
	public boolean enrollReaderInCommunities(UserType adminUser, String userId){
		boolean out_bool = false;

		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Enroll in community roles",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "Reader roles");

		try{
			UserType user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByObjectId(userId,adminUser.getOrganizationId());
			if(user == null){
				AuditService.denyResult(audit, "Invalid user id");
				return false;
			}
			if(RoleService.getIsUserInEffectiveRole(RocketSecurity.getAdminRole(adminUser.getOrganizationId()),adminUser) == false && RoleService.isFactoryAdministrator(adminUser, ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT))) == false){
				AuditService.denyResult(audit, "Admin user is not an administrator");
				return out_bool;
			}
			
			out_bool = Rocket.enrollReaderInCommunity(audit, user);
			if(out_bool == true){
				AuditService.permitResult(audit, "Configured " + user.getUrn() + " as a community reader");
			}
		}
		catch(FactoryException e){
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		} catch (ArgumentException e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		}
		

		return out_bool;
	}
	
	public boolean enrollReaderInCommunityProject(UserType adminUser, String userId, String communityId, String projectId){
		boolean out_bool = false;

		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Enroll in community project reader roles",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "Reader roles");
		UserType user = null;
		BasePermissionType permission=null;
		try {
			user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByObjectId(userId,adminUser.getOrganizationId());
			if(user == null){
				AuditService.denyResult(audit, "Invalid user id");
				return false;
			}
			permission = AuthorizationService.getViewPermissionForMapType(NameEnumType.GROUP, adminUser.getOrganizationId());
		} catch (FactoryException | ArgumentException e) {

			logger.error("Error",e);
		}
		if(permission == null) return false;
		return enrollInCommunityProject(audit, adminUser, user, permission, communityId, projectId,false);
	}

	public boolean enrollReaderInCommunity(UserType adminUser, String userId, String communityId){
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Enroll in community project reader roles",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "Reader roles");

		BasePermissionType permission=null;
		UserType user = null;
		try {
			user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByObjectId(userId,adminUser.getOrganizationId());
			if(user == null){
				AuditService.denyResult(audit, "Invalid user id");
				return false;
			}
			permission = AuthorizationService.getViewPermissionForMapType(NameEnumType.GROUP,adminUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		if(permission == null) return false;
		return enrollInCommunityLifecycle(audit, adminUser, user, permission, communityId, false);
	}
	
	public boolean enrollAdminInCommunity(UserType adminUser, String communityId, String userId){
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Enroll in community admin role",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "Admin roles");

		BasePermissionType permission=null;
		UserType user = null;
		try {
			user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByObjectId(userId,adminUser.getOrganizationId());
			if(user == null){
				AuditService.denyResult(audit, "Invalid user id");
				return false;
			}
			permission = AuthorizationService.getViewPermissionForMapType(NameEnumType.GROUP,adminUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		if(permission == null) return false;
		return enrollInCommunityLifecycle(audit, adminUser, user, permission, communityId,true);
	}
	
	
	private boolean enrollInCommunityLifecycle(AuditType audit, UserType adminUser, UserType user, BasePermissionType permission, String communityId, boolean isAdmin){
		boolean out_bool = false;


		try{
			if(RoleService.getIsUserInEffectiveRole(RocketSecurity.getAdminRole(adminUser.getOrganizationId()),adminUser) == false && RoleService.isFactoryAdministrator(adminUser, ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT))) == false){
				AuditService.denyResult(audit, "User is not an administrator");
				return out_bool;
			}
			
			LifecycleType lc = ((NameIdGroupFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getByObjectId(communityId, adminUser.getOrganizationId());
			if(lc == null){
				AuditService.denyResult(audit, "Lifecycle is null");
				return false;
			}
			
			UserRoleType role = (isAdmin ? RocketSecurity.getLifecycleAdminRole(lc) : RocketSecurity.getLifecycleUserRole(lc));
			
			if(Rocket.enrollInCommunityLifecycle(user, lc,role,permission)){
				out_bool = true;
				AuditService.permitResult(audit, "Enrolled " + user.getUrn() + " in Lifecycle " + lc.getUrn());

			}
			else{
				AuditService.denyResult(audit, "Failed to enrolled " + user.getUrn() + " in Lifecycle " + lc.getUrn());
			}
		}
		catch(FactoryException | ArgumentException e) {
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		}


		return out_bool;
	

	}
	
	private boolean enrollInCommunityProject(AuditType audit, UserType adminUser, UserType user, BasePermissionType permission, String communityId, String projectId, boolean isAdmin){
		boolean out_bool = false;

		try{
			
			LifecycleType lc = ((NameIdGroupFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getByObjectId(communityId, adminUser.getOrganizationId());
			if(lc == null){
				logger.warn("Lifecycle is null");
				return false;
			}
			ProjectType proj = ((NameIdGroupFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getByObjectId(projectId, adminUser.getOrganizationId());
			if(proj == null){
				logger.warn("Project is null");
				return false;
			}

			
			if(RoleService.getIsUserInEffectiveRole(RocketSecurity.getAdminRole(adminUser.getOrganizationId()),adminUser) == false && RoleService.isFactoryAdministrator(adminUser, ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT))) == false){
				AuditService.denyResult(audit, "User is not an administrator");
				return out_bool;
			}
			UserRoleType role = (isAdmin ? RocketSecurity.getProjectAdminRole(proj) : RocketSecurity.getProjectUserRole(proj));
			
			if(Rocket.enrollInCommunityProject(user, lc, proj, role,permission)){
				out_bool = true;
				AuditService.permitResult(audit, "Enrolled " + user.getUrn() + " in Lifecycle " + lc.getUrn() + " Project " + proj.getUrn());
			}
			else{
				AuditService.denyResult(audit, "Failed to enroll " + user.getUrn() + " in Lifecycle " + lc.getUrn() + " Project " + proj.getUrn());
			}
		}
		catch(FactoryException e){
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		} catch (ArgumentException e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		}

		
		return out_bool;

	}
	
	public boolean deleteCommunityProject(UserType adminUser, String projectId){
		boolean out_bool = false;

		ProjectType out_proj = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.DELETE, "deleteProject",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.PROJECT, projectId);
		try {
			out_proj = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getByObjectId(projectId, adminUser.getOrganizationId());
		} catch (FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		if(out_proj == null){
			AuditService.denyResult(audit,"Project doesn't exist");
			return false;
		}
		AuditService.targetAudit(audit, AuditEnumType.PROJECT, out_proj.getUrn());
		if(RocketSecurity.canChangeProject(adminUser,out_proj) == false){
			AuditService.denyResult(audit, "Permission denied");
			return false;
		}
		out_bool = Rocket.deleteProject(out_proj);
		if(out_bool) AuditService.permitResult(audit, "Deleted project: " + out_proj.getUrn());
		else AuditService.denyResult(audit, "Failed to delete project: " + out_proj.getUrn());
		return out_bool;
	}
	
	public boolean deleteCommunity(UserType adminUser, String communityId){
		boolean out_bool = false;

		LifecycleType lc = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.DELETE, "deleteLifecycle",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LIFECYCLE, communityId);

		try {
			lc = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getByObjectId(communityId, adminUser.getOrganizationId());
		} catch (FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		if(lc == null){
			AuditService.denyResult(audit,"Project doesn't exist");
			return false;
		}
		AuditService.targetAudit(audit, AuditEnumType.LIFECYCLE, lc.getUrn());
		if(RocketSecurity.canChangeLifecycle(adminUser,lc) == false){
			AuditService.denyResult(audit, "Permission denied");
			return false;
		}
		out_bool = Rocket.deleteLifecycle(lc);
		if(out_bool) AuditService.permitResult(audit, "Deleted lifecycle: " + lc.getUrn());
		else AuditService.denyResult(audit, "Failed to delete lifecycle: " + lc.getUrn());
		return out_bool;
	}
	
	public boolean configureEntitlements(UserType adminUser,String communityId, String projectId, String groupId){
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Configure community in organization #" + adminUser.getOrganizationId(), AuditEnumType.USER,adminUser.getUrn());

		try {
			LifecycleType lc = ((NameIdGroupFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getByObjectId(communityId, adminUser.getOrganizationId());
			if(lc == null){
				logger.warn("Lifecycle is null");
				return false;
			}
			ProjectType proj = ((NameIdGroupFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getByObjectId(projectId, adminUser.getOrganizationId());
			if(proj == null){
				logger.warn("Project is null");
				return false;
			}
			
			if(RocketSecurity.canChangeProject(adminUser, proj) == false){
				AuditService.denyResult(audit, "Not authorized to change project");
				return false;
			}

			DirectoryGroupType adir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getByObjectId(groupId, adminUser.getOrganizationId());
			if(adir != null){
				if(AuthorizationService.canChange(adminUser,adir) == false){
					AuditService.denyResult(audit, "Not authorized to change groups");
					return false;
				}
				UserRoleType bRole = RocketSecurity.getProjectRoleBucket(proj);
				UserRoleType lRole = RocketSecurity.getLifecycleRoleBucket(lc);
				UserRoleType rRole = RocketSecurity.getRocketRoles(lc.getOrganizationId());

				RocketSecurity.applyRolesToProjectDirectory(null, rRole, lRole, bRole, adir);
				EffectiveAuthorizationService.rebuildPendingRoleCache();
				out_bool = true;
				AuditService.permitResult(audit, "Configured community group entitlements");
			}
			else{
				AuditService.denyResult(audit, "Failed to configure community group entitlements");
			}
		} catch (FactoryException | DataAccessException | ArgumentException e) {
			logger.error(e);
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		}
		return out_bool;
	}
	
	public boolean createCommunity(UserType adminUser, String communityName){
		boolean out_bool = false;
		LifecycleType out_lc = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "createLifecycle",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LIFECYCLE, Rocket.getBasePath() + "/Lifecycles");
		if(RocketSecurity.canCreateLifecycle(adminUser) == false){
			AuditService.denyResult(audit, "Permission denied");
			return false;
		}
		try {
			out_lc = Rocket.createLifecycle(adminUser, communityName);
			if(out_lc != null){
				AuditService.permitResult(audit, "Created lifecycle: " + communityName);
				out_bool = true;
			}
			else AuditService.denyResult(audit, "Failed to create lifecycle");
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return out_bool;
	}
	
	public boolean createCommunityProject(UserType adminUser, String communityId, String projectName){
		boolean out_bool = false;
		ProjectType out_proj = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "createProject",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LIFECYCLE, Rocket.getBasePath() + "/Lifecycles");
		LifecycleType out_lc = getLifecycleToChange(audit, adminUser, communityId);
	

		if(RocketSecurity.canCreateProject(adminUser, out_lc) == false){
			AuditService.denyResult(audit, "Permission denied");
			return false;
		}
		try {
			out_proj = Rocket.createProject(adminUser, out_lc, projectName);
			if(out_proj != null){
				RocketModel.addDefaults(adminUser, out_proj.getGroupId());
				//logger.warn("Community defaults (Waterfall/Agile models) due to open issue with RocketModel and bulk sessions");
				AuditService.permitResult(audit, "Created project: " + projectName);
				out_bool = true;
				
			}
			else AuditService.denyResult(audit, "Failed to create project: " + projectName);
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return out_bool;
	}
	
	
	public boolean generateCommunityProjectRegion(UserType user, String communityId, String projectId, int locationSize, int seedSize, String dictionaryPath, String namesPath){
		boolean out_bool = false;
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Generate Region",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.PROJECT, projectId);

		try {
			LifecycleType lc = getLifecycle(audit, user, communityId);
			if(lc == null) return false;
			ProjectType proj = getProjectToChange(audit, user, projectId);
			if(proj == null) return false;
			
			DataGeneratorUtil dutil = getGenerator(audit, user, lc.getName(), proj.getName(), lc.getGroupPath() + "/Locations", lc.getGroupPath() + "/Traits", dictionaryPath, namesPath);
			if(dutil == null) return false;
			
			int eventCount = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).countInGroup(dutil.getEventsDir());
			boolean initSetup = (eventCount == 0);
			if(initSetup == false){
				AuditService.denyResult(audit, "One or more events already exists");
				return out_bool;
			}
			if(initSetup){
				logger.info("START Populating " + dutil.getProject().getName() + " data ...");
				String sessionId = BulkFactories.getBulkFactory().newBulkSession();
				
				EventType regionCreation = dutil.generateRegion(sessionId, locationSize, seedSize);
				logger.info("Writing session");
				BulkFactories.getBulkFactory().write(sessionId);
				BulkFactories.getBulkFactory().close(sessionId);
				((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate(regionCreation);
				List<EventType> events = regionCreation.getChildEvents();
				logger.info("Created " + events.size() + " events");
				logger.info("END Populating");
				out_bool = true;
			}

			
		} catch (FactoryException | ArgumentException | DataAccessException  e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return out_bool;
	}
	
	public boolean evolveCommunityProjectRegion(UserType user, String communityId, String projectId, int epochSize, int epochEvolutions, String dictionaryPath, String namesPath){
		boolean out_bool = false;
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Generate Region",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.PROJECT, projectId);

		try {
			LifecycleType lc = getLifecycle(audit, user, communityId);
			if(lc == null) return false;
			ProjectType proj = getProjectToChange(audit, user, projectId);
			if(proj == null) return false;
			
			DataGeneratorUtil dutil = getGenerator(audit, user, lc.getName(), proj.getName(), lc.getGroupPath() + "/Locations", lc.getGroupPath() + "/Traits", dictionaryPath, namesPath);
			if(dutil == null) return false;
			
			int eventCount = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).countInGroup(dutil.getEventsDir());
			boolean initSetup = (eventCount == 0);
			if(initSetup == true){
				AuditService.denyResult(audit, "Origination event does not exist");
				return out_bool;
			}
			
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();

			EventType epoch = null;
			for(int i = 0; i < epochSize; i++){
				int count = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).countInGroup(dutil.getEventsDir());
				boolean modeled = (count >= (2+i));
				if(modeled) continue;
				logger.info("MODEL Epoch " + (i + 1));
				epoch = dutil.generateEpoch(sessionId, epochEvolutions,1);
			}
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);

			out_bool = true;
			
		} catch (FactoryException | ArgumentException | DataAccessException  e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return out_bool;
	}
	
	
	
	public String reportCommunityProjectRegion(UserType user, String communityId, String projectId, String dictionaryPath, String namesPath){
		StringBuffer buff = new StringBuffer();
		
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Generate Region",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.PROJECT, projectId);

		try {
			
			LifecycleType lc = getLifecycle(audit, user, communityId);
			if(lc == null) return null;
			ProjectType proj = getProject(audit, user, projectId);
			if(proj == null) return null;
			DataGeneratorUtil dutil = getGenerator(audit, user, lc.getName(), proj.getName(), lc.getGroupPath() + "/Locations", lc.getGroupPath() + "/Traits", dictionaryPath, namesPath);
			if(dutil == null) return null;
			
			List<EventType> events = dutil.getEvents();
			for(int i = 0; i < events.size(); i++){
				EventType evt = events.get(i);
				((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate(evt);
				//LocationType location = evt.getLocation();
				//buff.append("* " + location.getName() + " is " + Factories.getAttributeFactory().getAttributeValueByName(location, "alignment"));
				buff.append("* " + evt.getName() + " - " + evt.getChildEvents().size() + " sub events\n");
				
				for(EventType cevt : evt.getChildEvents()){
					((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate(cevt);
					LocationType loc = cevt.getLocation();
					Factories.getAttributeFactory().populateAttributes(loc);
					buff.append("\t* " + Factories.getAttributeFactory().getAttributeValueByName(loc,"name") + " - (" + loc.getName() + ")\n");
					buff.append("\t" + cevt.getName() + " - " + cevt.getChildEvents().size() + " sub events\n");
					//buff.append("\t" + cevt.getName() + "\n");
					/*
					if(cevt.getChildEvents().size() > 0){
						for(EventType scevt : cevt.getChildEvents()){
							((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate(scevt);
							buff.append("\t\t* " + scevt.getName() + "\n");
						}
					}
					*/
					if(i == (events.size()-1)){
						Map<String,List<PersonType>> demo = dutil.getDemographics(cevt);
						buff.append("\t* Demographics\n");
						for(String key : demo.keySet()){
							buff.append("\t" + key + " : " + demo.get(key).size() + "\n");
						}
						List<PersonType> people = demo.get("Alive");
						buff.append("\t* Population - " + people.size() + "\n");
						
						for(PersonType person : people){
							((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).populate(person);
							buff.append("\t" + person.getName() + " " + Factories.getAttributeFactory().getAttributeValueByName(person,"alignment") + "\n");
						}
					}
					//buff.append(dutil.getDemographics(evt));
					buff.append("\n");
				}
			}

		}
		catch (FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return buff.toString();
	}
	
	
	public boolean updateCommunityProjectScript(UserType user, String communityId, String projectId, String name, String dataStr){
		boolean out_bool = false;
		DataType data = getCommunityProjectScriptData(user, communityId, projectId, name);
		if(data == null) return false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Community Project Script: " + data.getUrn(),AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.DATA, data.getUrn());
		DirectoryGroupType dataDir = null;
		try {
			if(AuthorizationService.canChange(user, data)==false){
				AuditService.denyResult(audit,"User not authorized to change " + data.getUrn());
				return false;
			}
			DataUtil.setValueString(data, dataStr);
			if(((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).update(data)==false){
				AuditService.denyResult(audit,"Failed to update data");
				return false;
			}
			else{
				AuditService.permitResult(audit, "Updated data");
				out_bool = true;
			}
		} catch (FactoryException | ArgumentException | DataException e) {
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return out_bool;
	}
	private String processTokens(AuditType audit, UserType user, String communityId, String projectId, DataType data, Map<String,Object> params) throws DataException, FactoryException, ArgumentException{

		
		LifecycleType lc = getLifecycle(audit, user, communityId);
		ProjectType proj = getProject(audit,user, projectId);
		if(lc == null || proj == null) return null;
		
		DataGeneratorUtil dutil = getGenerator(audit, user, lc.getName(), proj.getName(), lc.getGroupPath() + "/Locations", lc.getGroupPath() + "/Traits", null, null);
		List<EventType> evts = dutil.getEvents();
		EventType lastEvent = null;
		List<EventType> lastChildren = new ArrayList<>();
		if(evts.size() > 0){
			lastEvent = evts.get(evts.size()-1);
			((NameIdFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate(lastEvent);
			lastChildren = lastEvent.getChildEvents();
			for(EventType cevt : lastChildren){
				((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate(cevt);
				LocationType loc = cevt.getLocation();
				Factories.getAttributeFactory().populateAttributes(loc);
			}
		}
		
		params.put("lastEvent", lastEvent);
		
		String out_str = ScriptService.processTokens(user, DataUtil.getValueString(data))
				.replaceAll("\\$\\{communityName\\}", lc.getName())
				.replaceAll("\\$\\{projectName\\}", proj.getName())
				.replaceAll("\\$\\{communityUrn\\}", lc.getUrn())
				.replaceAll("\\$\\{projectUrn\\}", proj.getUrn())
				.replaceAll("\\$\\{scriptName\\}", data.getName())
				.replaceAll("\\$\\{scriptUrn\\}", data.getUrn())
				.replaceAll("\\$\\{lastEventName\\}", lastEvent.getName())
				.replaceAll("\\$\\{lastEventUrn\\}", lastEvent.getUrn())
		;
		
		return out_str;

	}
	public Object executeCommunityProjectScript(UserType user, String communityId, String projectId, String name){
		long startTime = System.currentTimeMillis();
		Object obj = null;
		DataType data = getCommunityProjectScriptData(user, communityId, projectId, name);
		if(data == null) return obj;
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.EXECUTE, "Community Project Script: " + data.getUrn(),AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.DATA, data.getUrn());
		
		Map<String,Object> params = ScriptService.getCommonParameterMap(user);
		
		try {
			String dataStr = processTokens(audit,user,communityId, projectId,data, params);
			if(dataStr == null){
				AuditService.denyResult(audit, "Error pre-processing script source");
			}
			obj = ScriptService.run(user, data.getUrn(), dataStr, params);
			AuditService.permitResult(audit, "Executed script");
		} catch (ArgumentException | DataException | FactoryException e) {
			logger.error(e);
			AuditService.denyResult(audit, "Error: " + e.getMessage());
		}
		long stopTime = System.currentTimeMillis();
		logger.info("Executed in " + (stopTime - startTime) + "ms");
		return obj;
	}	
	public DataType getCommunityProjectScriptData(UserType user, String communityId, String projectId, String name){
		DataType data = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "Community Project Script: " + name,AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.DATA, name);
		LifecycleType lc = getLifecycle(audit, user, communityId);
		ProjectType proj = getProject(audit,user, projectId);
		if(lc == null || proj == null) return null;
		DirectoryGroupType dataDir = null;
		try {
			dataDir =(DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, proj.getGroupPath() + "/Data", proj.getOrganizationId());
			data = (DataType)((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name, false, dataDir);
			if(data != null){
				if(AuthorizationService.canView(user, data)==false){
					AuditService.denyResult(audit,"User not authorized to view " + data.getUrn());
					return null;
				}
			}
			else{
				if(AuthorizationService.canChange(user, dataDir)==false){
					AuditService.denyResult(audit,"User not authorized to create a new community script");
					return null;
				}
				data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(user, dataDir.getId());
				data.setMimeType("text/javascript");
				data.setName(name);
				DataUtil.setValueString(data, generateCommunityProjectScript(user,lc, proj));
				if(((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(data)==false){
					AuditService.denyResult(audit,"Failed to add new data");
					return null;
				}
				data = (DataType)((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name, false, dataDir);
			}
			if(data == null){
				AuditService.denyResult(audit,"Failed to retrieve community script");
				return null;
			}
			else{
				AuditService.permitResult(audit, "Retrieved community script");
			}
		} catch (FactoryException | ArgumentException | DataException e) {
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
			data = null;
		}
		return data;

	}
	
	private String generateCommunityProjectScript(UserType user, LifecycleType lifecycle, ProjectType project){
		StringBuffer buff = new StringBuffer();
		buff.append("/*\n\t" + lifecycle.getUrn() + "\n\t" + project.getUrn() + "\n*/\n\nvar result;\n\n");
		buff.append("/// HELP\n");
		buff.append("logger.info(\"${communityName} - ${projectName} - ${scriptName}\");\n");
		buff.append("/// SCRIPT\n\n");
		buff.append("\n/// Declare result as the last object since this runs outside of a function\nresult;\n");
		return buff.toString();
	}
	
	public String getCommunityProjectScript(UserType user, String communityId, String projectId, String name){
		StringBuffer buff = new StringBuffer();
		
		DataType data = getCommunityProjectScriptData(user, communityId, projectId, name);
		if(data == null) return buff.toString();
		
		try {
			String scriptData = DataUtil.getValueString(data);
			if(scriptData != null && scriptData.length() > 0) buff.append(scriptData);

			
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return buff.toString();
	}
	
	/// PRIVATE METHODS
	private LifecycleType getLifecycleToChange(AuditType audit, UserType user, String communityId){
		return getLifecycle(audit, user, communityId, true);
	}
	private LifecycleType getLifecycle(AuditType audit, UserType user, String communityId){
		return getLifecycle(audit, user, communityId, false);
	}
	private LifecycleType getLifecycle(AuditType audit, UserType user, String communityId, boolean toChange){
		LifecycleType lc = null;
		try{
			lc = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getByObjectId(communityId, user.getOrganizationId());
			if(lc == null){
				AuditService.denyResult(audit, "Null community object");
				return null;
			}
			BaseService.normalize(user, lc);
			if((toChange == true && RocketSecurity.canChangeLifecycle(user, lc)==false) || (toChange == false && RocketSecurity.canReadLifecycle(user, lc)==false)){

				AuditService.denyResult(audit, "User is not authorized to view lifecycle");
				return null;
			}
		}
		catch(FactoryException | ArgumentException e){
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return lc;
	}
	private ProjectType getProjectToChange(AuditType audit, UserType user, String communityId){
		return getProject(audit, user, communityId, true);
	}
	private ProjectType getProject(AuditType audit, UserType user, String communityId){
		return getProject(audit, user, communityId, false);
	}
	private ProjectType getProject(AuditType audit, UserType user, String communityId, boolean toChange){
		ProjectType proj = null;
		try{
			proj = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getByObjectId(communityId, user.getOrganizationId());
			if(proj == null){
				AuditService.denyResult(audit, "Null community object");
				return null;
			}
			BaseService.normalize(user, proj);
			if((toChange == true && RocketSecurity.canChangeProject(user, proj)==false) || (toChange == false && RocketSecurity.canReadProject(user, proj)==false)){
				AuditService.denyResult(audit, "User is not authorized to view project");
				return null;
			}
		}
		catch(FactoryException | ArgumentException e){
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
		}
		return proj;
	}
	
	private DataGeneratorUtil getGenerator(AuditType audit, UserType user, String communityName, String projectName, String locationPath, String traitPath, String dictionaryPath, String namesPath){
		DataGeneratorUtil dutil = new DataGeneratorUtil(
				user,
				communityName,
				projectName,
				locationPath,
				traitPath,
				dictionaryPath,
				namesPath
			);
		try{
			if(dutil.initialize() == false){
				AuditService.denyResult(audit, "Failed to initialize data generator");
				return null;
			}
			if(dutil.getProject() == null){
				AuditService.denyResult(audit, "Failed to load project via data generator");
				return null;
			}
		}
		catch(ArgumentException | FactoryException e){
			AuditService.denyResult(audit, "Error: " + e.getMessage());
			logger.error("Error",e);
			dutil = null;
		}
		return dutil;
	}

}