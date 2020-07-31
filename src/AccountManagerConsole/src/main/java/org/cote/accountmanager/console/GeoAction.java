/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cote.accountmanager.console;

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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.propellant.objects.LocationType;
import org.cote.propellant.objects.TraitType;
import org.cote.propellant.objects.types.GeographyEnumType;
import org.cote.propellant.objects.types.TraitEnumType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.factory.LocationFactory;
import org.cote.rocket.factory.TraitFactory;
/// This is a legacy method for manual/console operations only. Use the service for import operations, along with the configuration defined in the service web.xml for the data locations.
//
public class GeoAction {
	public static final Logger logger = LogManager.getLogger(GeoAction.class);
	
	private static Map<String,String> geoIdToPost = new HashMap<>();
	private static Map<String,LocationType> locByCode = new HashMap<>();
	
	public static void processGeoAction(UserType user, CommandLine cmd){
		CSVFormat csvFileFormat = CSVFormat.DEFAULT.withDelimiter('\t').withAllowMissingColumnNames(true).withQuote(null);
		DirectoryGroupType dir = null;
		try {
			String path = cmd.getOptionValue("path");
			logger.info("Path: " + path);
			dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, path, user.getOrganizationId());
			if(dir == null){
				String parentPath = path.substring(0,path.lastIndexOf("/"));
				DirectoryGroupType parentDir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, parentPath, user.getOrganizationId());
				if(parentDir == null){
					logger.error("Invalid path: " + parentPath);
					return;
				}
				if(AuthorizationService.canChange(user, parentDir) == false){
					logger.error("Cannot create specified path");
					return;
				}
				dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newDirectoryGroup(user,path.substring(path.lastIndexOf("/") + 1, path.length()), parentDir, user.getOrganizationId());
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
			if(cmd.hasOption("clean")){
				logger.info("Cleaning ...");
				((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).deleteLocationsInGroup(dir);
				Factories.cleanupOrphans();
			}
			if(cmd.hasOption("basePath") == false){
				return;
			}
			if(cmd.hasOption("countryInfo")){
				importCountries(user, csvFileFormat,dir,cmd.getOptionValue("basePath") + cmd.getOptionValue("countryInfo"));
			}

			if(cmd.hasOption("features")){
				importTraits(user, csvFileFormat,dir,cmd.getOptionValue("basePath") + cmd.getOptionValue("features"));
			}
			if(cmd.hasOption("admin1")){
				importAdmin1Codes(user, csvFileFormat, dir, cmd.getOptionValue("basePath") + cmd.getOptionValue("admin1"));
			}
			if(cmd.hasOption("admin2")){
				importAdmin2Codes(user, csvFileFormat, dir, cmd.getOptionValue("basePath") + cmd.getOptionValue("admin2"));
			}
			if(cmd.hasOption("countries")){
				if(cmd.hasOption("alternate")){
					bufferPostalCodes(csvFileFormat, cmd.getOptionValue("basePath") + cmd.getOptionValue("alternate"));
				}
				importCountryData(user, csvFileFormat, dir, cmd.getOptionValue("basePath"),cmd.getOptionValue("countries").split(","));
			}
			
		} catch (FactoryException | ArgumentException | DataAccessException | IOException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

	}
	
	private static void bufferPostalCodes(CSVFormat csvFileFormat, String path) throws IOException{
		

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
	
	
	private static LocationType findLocationByAdminCode(LocationType parent, DirectoryGroupType dir, String codeName, String code, long organizationId){
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
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		
		}

		return location;
	}
	
	private static void importTraits(UserType owner, CSVFormat csvFileFormat, DirectoryGroupType dir, String path) throws ArgumentException, FactoryException, DataAccessException, IOException{
		logger.info("Reading traits ...");
		BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));

		CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		for(CSVRecord record : csvFileParser){
			TraitType trait = ((TraitFactory)Factories.getFactory(FactoryEnumType.TRAIT)).newTrait(owner, dir.getId());
			trait.setName(record.get(0));
			trait.setDescription(record.get(2));
			trait.getAttributes().add(Factories.getAttributeFactory().newAttribute(trait, "code", record.get(1)));
			trait.setTraitType(TraitEnumType.LOCATION);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.TRAIT, trait);
		}
		bir.close();
		BulkFactories.getBulkFactory().write(sessionId);
		BulkFactories.getBulkFactory().close(sessionId);
	}

	
	private static void importCountries(UserType owner, CSVFormat csvFileFormat, DirectoryGroupType dir, String path) throws ArgumentException, FactoryException, DataAccessException, IOException{
		logger.info("Reading countries ...");
		BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
		CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		
		locByCode.clear();
		for(CSVRecord record : csvFileParser){
			String iso = record.get(0);
			if(iso.startsWith("#")) continue;
			LocationType location = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(owner, dir.getId());
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

	}
	private static void importAdmin1Codes(UserType owner, CSVFormat csvFileFormat, DirectoryGroupType dir, String path) throws ArgumentException, FactoryException, DataAccessException, IOException{

		logger.info("Reading Admin 1 Codes");
		BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
		CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

		((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).clearCache();
		
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		Map <String, Integer> nameMap = new HashMap<>();
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
			LocationType countryLoc = findLocationByAdminCode(null, dir, "iso", countryIso, owner.getOrganizationId());
			if(countryLoc == null){
				logger.error("Failed to find country '" + countryIso + "'");
				continue;
			}

			LocationType admin1Loc = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(owner, countryLoc);
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
		
	}
	
	private static void importAdmin2Codes(UserType owner, CSVFormat csvFileFormat, DirectoryGroupType dir, String path) throws ArgumentException, FactoryException, DataAccessException, IOException{
		logger.info("Reading Admin 2 Codes");
		BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
		CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

		String sessionId = BulkFactories.getBulkFactory().newBulkSession();

		String lastAdminLoc = null;
		LocationType lastLoc = null;
		Map <String, Integer> nameMap = new HashMap<>();
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
				
				admin1Loc = findLocationByAdminCode(null, dir, "code", adminCode, owner.getOrganizationId());

				if(admin1Loc == null){
					logger.warn("Failed to find admin location '" + countryIso + "." + adminCode1);
					admin1Loc = findLocationByAdminCode(null, dir, "iso", countryIso, owner.getOrganizationId());
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

			LocationType admin2Loc = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(owner, admin1Loc);
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
	}
	private static void importCountryData(UserType owner, CSVFormat csvFileFormat, DirectoryGroupType dir, String basePath, String[] countryList) throws ArgumentException, FactoryException, DataAccessException, IOException{
		BufferedReader bir = null;
		
		CSVParser  csvFileParser = null;
		String sessionId = null;
		Map <String, Integer> nameMap = new HashMap<>();



		
		for(int c = 0; c < countryList.length; c++){
			logger.info("Reading country data ... " + countryList[c]);
			String path = basePath + countryList[c] + ".txt";

			LocationType countryLoc = findLocationByAdminCode(null, dir,"iso", countryList[c], owner.getOrganizationId());
			if(countryLoc == null){
				logger.error("Failed to find parent country for " + countryList[c]);
				continue;
			}
			bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
			csvFileParser = new CSVParser(bir, csvFileFormat);
 
			sessionId = BulkFactories.getBulkFactory().newBulkSession();
			int counter = 0;
			nameMap.clear();
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
				LocationType regionLocation = findLocationByAdminCode(null, dir,"code", regionLocationCode, owner.getOrganizationId());
				if(regionLocation == null){
					logger.warn("Failed to find region location for " + geoid + " with code: '" + regionLocationCode + "'");
					if(regionLocation == null && adminCode2 != null){
						regionLocation = findLocationByAdminCode(null, dir, "code", countryList[c] + "." + adminCode1, owner.getOrganizationId());
					}
					if(regionLocation == null){
						logger.error("Failed to find region location for " + geoid);
						continue;
					}
					
					continue;
				}
				
				LocationType location = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).newLocation(owner, regionLocation);
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
	}
	
}
