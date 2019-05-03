/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.GroupParticipationFactory;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.IParticipationFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.PersonParticipationFactory;
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
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.GroupParticipantType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserPermissionType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
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
import org.cote.rocket.util.CommunityProjectUtil;
import org.cote.rocket.util.DataGeneratorData;
import org.cote.rocket.util.DataGeneratorUtil;

/*
 * 2017/10/16 - AUTHORIZATION NOTE
 * Community authorization for lifecycles will get bridged to direct authorization checks due to the Policy Extension being applied on initial setup
 * This means that authorization checks targeting the lifecycle itself versus its containment group will fail for anyone other than the owner because the authorization was not set
 */

public class RocketCommunity implements ICommunityProvider {
	private static Map<String,String> geoIdToPost = new HashMap<>();
	private static Map<String,LocationType> locByCode = new HashMap<>();
	
	protected static final String LOCATION_FEATURES = "Location Features";
	protected static final String ERROR_PERMISSION_DENIED = "Permission denied";
	protected static final String ERROR_NULL_COMMUNITY = "Null community object";
	protected static final String ERROR_COMMUNITY_AUTHORIZATION = "User is not authorized to change lifecycle";
	protected static final String ROLE_AUTHORIZATION = "Authorized to view roles";
	protected static final String ERROR_ROLE_AUTHORIZATION = "Not authorized to view roles";
	protected static final String ERROR_DIRECTORY_CHANGE_AUTHORIZATION = "User is not authorized to change the directory";
	public static final Logger logger = LogManager.getLogger(RocketCommunity.class);
	private static final  CSVFormat csvFileFormat = CSVFormat.DEFAULT.withDelimiter('\t').withAllowMissingColumnNames(true).withQuote(null);
	private boolean randomizeSeedPopulation = false;
	private boolean organizePersonManagement = false;
	
	public RocketCommunity(){
		/// Public init
	}
	
	public boolean isOrganizePersonManagement() {
		return organizePersonManagement;
	}

	public void setOrganizePersonManagement(boolean organizePersonManagement) {
		this.organizePersonManagement = organizePersonManagement;
	}

	public boolean isRandomizeSeedPopulation() {
		return randomizeSeedPopulation;
	}

	public void setRandomizeSeedPopulation(boolean randomizeSeedPopulation) {
		this.randomizeSeedPopulation = randomizeSeedPopulation;
	}
	public void clearCache(){
		geoIdToPost.clear();
		locByCode.clear();
	}
	
	public boolean addProjectArtifacts(UserType user, AuditEnumType auditType, String objectId){
		boolean outBool = false;
		/// TODO: Add AuthZ check
		/// And refactor the API to avoid the duplication here
		///
		logger.warn("**** Refactor warning: Authorization check not yet in place");
		try{
			if(auditType == AuditEnumType.LIFECYCLE){
				LifecycleType lc = BaseService.readByObjectId(auditType, objectId, user);
				outBool = (lc != null
					&& RocketModel.addAgileArtifacts(user, lc)
					&& RocketModel.addWaterfallArtifacts(user, lc)
				);
			}
			else if(auditType == AuditEnumType.PROJECT){
				ProjectType pj = BaseService.readByObjectId(auditType, objectId, user);
				outBool = (pj != null
					&& RocketModel.addAgileArtifacts(user, pj)
					&& RocketModel.addWaterfallArtifacts(user, pj)
				);
			}
		}
		catch(DataAccessException | FactoryException | ArgumentException e){
			logger.error(e);
		}
		return outBool;
	}
	
	public boolean importLocationTraits(UserType user, AuditEnumType auditType, String objectId, String locationPath, String featuresFileName){
		boolean outBool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, LOCATION_FEATURES,AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, auditType, objectId);
		NameIdDirectoryGroupType obj = null;
		try {
			obj = ((INameIdFactory)Factories.getFactory(FactoryEnumType.valueOf(auditType.toString()))).getByObjectId(objectId, user.getOrganizationId());
			if(obj == null){
				AuditService.denyResult(audit, ERROR_NULL_COMMUNITY);
				return outBool;
			}
			BaseService.normalize(user, obj);
			if(!AuthorizationService.canChange(user, obj)){
				AuditService.denyResult(audit, ERROR_COMMUNITY_AUTHORIZATION);
				return outBool;
			}
			DirectoryGroupType lcGroup = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, obj.getGroupPath(), user.getOrganizationId());
			DirectoryGroupType locDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Traits", lcGroup, user.getOrganizationId());

			if(!AuthorizationService.canChange(user, locDir)){
				AuditService.denyResult(audit, ERROR_DIRECTORY_CHANGE_AUTHORIZATION);
				return outBool;
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
			csvFileParser.close();
			bir.close();
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			
			AuditService.permitResult(audit, "Loaded traits");
			outBool = true;
			
		} catch (FactoryException | ArgumentException | DataAccessException | IOException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
		}
		

		return outBool;
	}
	
	public boolean importLocationCountryInfo(UserType user, AuditEnumType auditType, String objectId, String locationPath, String featuresFileName){
		boolean outBool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, LOCATION_FEATURES,AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LOCATION, objectId);
		NameIdDirectoryGroupType obj = null;
		try {
			obj = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.valueOf(auditType.toString()))).getByObjectId(objectId, user.getOrganizationId());
			if(obj == null){
				AuditService.denyResult(audit, ERROR_NULL_COMMUNITY);
				return outBool;
			}
			BaseService.normalize(user, obj);
			if(AuthorizationService.canChange(user, obj)==false){
				AuditService.denyResult(audit, ERROR_COMMUNITY_AUTHORIZATION);
				return outBool;
			}
			DirectoryGroupType lcGroup = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, obj.getGroupPath(), user.getOrganizationId());
			DirectoryGroupType locDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Locations", lcGroup, user.getOrganizationId());

			if(!AuthorizationService.canChange(user, locDir)){
				AuditService.denyResult(audit, ERROR_DIRECTORY_CHANGE_AUTHORIZATION);
				return outBool;
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
			csvFileParser.close();
			bir.close();
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			
			AuditService.permitResult(audit, "Loaded country info");
			outBool = true;
			
		} catch (FactoryException | ArgumentException | DataAccessException | IOException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
		}
		
		return outBool;
	}
	
	public boolean importLocationAdmin1Codes(UserType user, AuditEnumType auditType, String objectId, String locationPath, String featuresFileName){
		boolean outBool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, LOCATION_FEATURES,AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LOCATION, objectId);
		NameIdDirectoryGroupType obj = null;
		try {
			obj = ((NameIdGroupFactory)Factories.getFactory(FactoryEnumType.valueOf(auditType.toString()))).getByObjectId(objectId, user.getOrganizationId());
			if(obj == null){
				AuditService.denyResult(audit, ERROR_NULL_COMMUNITY);
				return outBool;
			}
			BaseService.normalize(user, obj);
			if(AuthorizationService.canChange(user, obj)==false){
				AuditService.denyResult(audit, ERROR_COMMUNITY_AUTHORIZATION);
				return outBool;
			}
			DirectoryGroupType lcGroup = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, obj.getGroupPath(), user.getOrganizationId());
			DirectoryGroupType locDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Locations", lcGroup, user.getOrganizationId());

			if(AuthorizationService.canChange(user, locDir) == false){
				AuditService.denyResult(audit, ERROR_DIRECTORY_CHANGE_AUTHORIZATION);
				return outBool;
			}
			
			

			String path = locationPath + featuresFileName;
			logger.info("Loading Admin1Codes from " + path + " into " + locDir.getUrn());
			logger.info("Reading Admin 1 Codes");
			BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
			CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
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
			csvFileParser.close();
			bir.close();
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			
			AuditService.permitResult(audit, "Loaded admin 1 codes");
			outBool = true;
			
		} catch (FactoryException | ArgumentException | DataAccessException | IOException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
		}
		
		return outBool;
	}
	
	public boolean importLocationAdmin2Codes(UserType user, AuditEnumType auditType, String objectId, String locationPath, String featuresFileName){
		boolean outBool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, LOCATION_FEATURES,AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LOCATION, objectId);
		NameIdDirectoryGroupType obj = null;
		try {
			obj = ((NameIdGroupFactory)Factories.getFactory(FactoryEnumType.valueOf(auditType.toString()))).getByObjectId(objectId, user.getOrganizationId());
			if(obj == null){
				AuditService.denyResult(audit, ERROR_NULL_COMMUNITY);
				return outBool;
			}
			BaseService.normalize(user, obj);
			if(AuthorizationService.canChange(user, obj)==false){
				AuditService.denyResult(audit, ERROR_COMMUNITY_AUTHORIZATION);
				return outBool;
			}
			DirectoryGroupType lcGroup = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, obj.getGroupPath(), user.getOrganizationId());
			DirectoryGroupType locDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Locations", lcGroup, user.getOrganizationId());

			if(AuthorizationService.canChange(user, locDir) == false){
				AuditService.denyResult(audit, ERROR_DIRECTORY_CHANGE_AUTHORIZATION);
				return outBool;
			}
			
			

			String path = locationPath + featuresFileName;
			logger.info("Loading Admin2Codes from " + path + " into " + locDir.getUrn());
			logger.info("Reading Admin 2 Codes");
			BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
			CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

			String sessionId = BulkFactories.getBulkFactory().newBulkSession();

			String lastAdminLoc = null;
			LocationType lastLoc = null;
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
			csvFileParser.close();
			bir.close();
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			AuditService.permitResult(audit, "Loaded admin 2 codes");
			outBool = true;
			
		} catch (FactoryException | ArgumentException | DataAccessException | IOException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
		}
		
		return outBool;
	}
	
	public boolean importLocationCountryData(UserType user, AuditEnumType auditType, String objectId, String locationPath, String codes, String alternate){
		boolean outBool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, LOCATION_FEATURES,AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LOCATION, objectId);
		NameIdDirectoryGroupType obj = null;
		try {
			obj = ((NameIdGroupFactory)Factories.getFactory(FactoryEnumType.valueOf(auditType.toString()))).getByObjectId(objectId, user.getOrganizationId());
			if(obj == null){
				AuditService.denyResult(audit, ERROR_NULL_COMMUNITY);
				return outBool;
			}
			BaseService.normalize(user, obj);
			if(!AuthorizationService.canChange(user, obj)){
				AuditService.denyResult(audit, ERROR_COMMUNITY_AUTHORIZATION);
				return outBool;
			}
			DirectoryGroupType lcGroup = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA, obj.getGroupPath(), user.getOrganizationId());
			DirectoryGroupType locDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Locations", lcGroup, user.getOrganizationId());

			if(!AuthorizationService.canChange(user, locDir)){
				AuditService.denyResult(audit, ERROR_DIRECTORY_CHANGE_AUTHORIZATION);
				return outBool;
			}
			
			if(alternate != null && alternate.length() > 0){
				bufferPostalCodes(csvFileFormat, locationPath + alternate);
			}
			
			BufferedReader bir = null;
			
			CSVParser  csvFileParser = null;
			String sessionId = null;

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
				locByCode.clear();

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
			outBool = true;
			
		} catch (FactoryException | ArgumentException | DataAccessException | IOException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
		}
		
		return outBool;
	}
	
	private static void bufferPostalCodes(CSVFormat csvFileFormat, String path) throws IOException{
		if(!geoIdToPost.isEmpty()){
			logger.info("Postal codes already buffered");
			return;
		}
		logger.info("Buffering postal values ...");
		geoIdToPost.clear();
		BufferedReader bir = new BufferedReader(new InputStreamReader(new FileInputStream(path),"UTF-8"));
		CSVParser csvFileParser = new CSVParser(bir, csvFileFormat);

		for(CSVRecord record : csvFileParser){
			if(record.get(2).equals("post") && record.get(3) != null && record.get(3).length() > 0){
				geoIdToPost.put(record.get(1), record.get(3));
			}
		}
		
		csvFileParser.close();
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
		if(parent != null){
			fields.add(QueryFields.getFieldParent(parent));
		}
		try{
			List<LocationType> locations = ((LocationFactory)Factories.getFactory(FactoryEnumType.LOCATION)).list(fields.toArray(new QueryField[0]), instruction, organizationId);
			if(!locations.isEmpty()){
				location = locations.get(0);
				locByCode.put(key, location);
			}
		} catch (ArgumentException | FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		
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
				AuditService.permitResult(audit, ROLE_AUTHORIZATION);
			}
			else{
				AuditService.denyResult(audit, ERROR_ROLE_AUTHORIZATION);
			}
			
		} catch (FactoryException | ArgumentException e) {
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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

			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return outPermission;
	}
	
	@SuppressWarnings("unchecked")
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
			
			AuditService.denyResult(audit,String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
		}
		
		return (T)lc;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getCommunityProject(UserType user, String communityName, String projectName){

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "getCommunityProject",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.PROJECT, projectName);

		LifecycleType lc = null;
		ProjectType proj = null;
		try {
			lc = Rocket.getLifecycle(communityName, user.getOrganizationId());
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
				AuditService.denyResult(audit, ERROR_PERMISSION_DENIED);
				return null;
			}
			AuditService.permitResult(audit, "Returning project " + projectName + " in lifecycle " + communityName);
		} catch (FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, "Error: "  + e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		return (T) proj;

	}
	
	public List<BaseRoleType> getCommunityProjectRoles(UserType user, String projectId){
		List<BaseRoleType> roles = new ArrayList<>();
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
				AuditService.permitResult(audit, ROLE_AUTHORIZATION);
			}
			else{
				AuditService.denyResult(audit, ERROR_ROLE_AUTHORIZATION);
			}
			
		} catch (FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return roles;
	}
	public List<BaseRoleType> getCommunityRoles(UserType user, String communityId){
		List<BaseRoleType> roles = new ArrayList<>();
		LifecycleType outLc = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Get Roles",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LIFECYCLE, Rocket.getBasePath() + "/Lifecycles");
		try {
			outLc = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getByObjectId(communityId, user.getOrganizationId());
			if(outLc == null){
				AuditService.denyResult(audit, "Invalid lifecycle: " + communityId);
				return roles;
			}
			UserRoleType lcr = RocketSecurity.getLifecycleRoleBucket(outLc);
			if(lcr == null){
				AuditService.denyResult(audit, "Invalid lifecycle role bucket");
				return roles;
			}
			AuditService.targetAudit(audit, AuditEnumType.ROLE,lcr.getName());
			if(AuthorizationService.canView(user, lcr)){
				roles = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleList(RoleEnumType.USER, lcr, 0,0,user.getOrganizationId());
				AuditService.permitResult(audit, ROLE_AUTHORIZATION);
			}
			else{
				AuditService.denyResult(audit, ERROR_ROLE_AUTHORIZATION);
			}
		} catch (FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return roles;
	}
	public List<BaseRoleType> getCommunitiesRoles(UserType user){
		List<BaseRoleType> roles = new ArrayList<>();
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Get Community Roles",AuditEnumType.USER,user.getUrn());

		try {
			UserRoleType cr = RocketSecurity.getRocketRoles(user.getOrganizationId());
			AuditService.targetAudit(audit, AuditEnumType.ROLE,cr.getName());
			if(AuthorizationService.canView(user, cr)){
				roles = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleList(RoleEnumType.USER, cr, 0,0,user.getOrganizationId());
				AuditService.permitResult(audit, ROLE_AUTHORIZATION);
			}
			else{
				AuditService.denyResult(audit, ERROR_ROLE_AUTHORIZATION);
			}
		} catch (FactoryException | ArgumentException e) {		
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return roles;
	}
	public boolean isCommunityConfigured(long organizationId){
		DirectoryGroupType applicationDirectory = Rocket.getRocketApplicationGroup(organizationId);
		return (applicationDirectory != null);
		
	}
	public boolean configureCommunity(UserType adminUser){
		boolean outBool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Configure community in organization #" + adminUser.getOrganizationId(), AuditEnumType.USER,adminUser.getUrn());

		try {
			boolean isSysAdmin = RoleService.getIsUserInRole(RoleService.getSystemAdministratorUserRole(adminUser.getOrganizationId()), adminUser);
			if(isSysAdmin == false){
				AuditService.denyResult(audit, "User is not an administrator");
				return outBool;
			}
			outBool = Rocket.configureApplicationEnvironment(adminUser);
		} catch (FactoryException | DataAccessException | ArgumentException e) {
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
		}
		if(outBool){
			AuditService.permitResult(audit, "Configured community");
		}
		return outBool;
	}
	public boolean enrollAdminInCommunities(UserType adminUser, String userId){
		boolean outBool = false;

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
				return outBool;
			}
			
			outBool = Rocket.enrollAdminInCommunity(audit, user);
			if(outBool){
				AuditService.permitResult(audit, "Configured " + user.getUrn() + " as a community administrator");
			}
		}
		catch(FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
		}
		

		return outBool;
	}
	
	public boolean enrollReaderInCommunities(UserType adminUser, String userId){
		boolean outBool = false;

		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Enroll in community roles",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.ROLE, "Reader roles");

		try{
			UserType user = ((UserFactory)Factories.getFactory(FactoryEnumType.USER)).getByObjectId(userId,adminUser.getOrganizationId());
			if(user == null){
				AuditService.denyResult(audit, "Invalid user id");
				return false;
			}
			if(!RoleService.getIsUserInEffectiveRole(RocketSecurity.getAdminRole(adminUser.getOrganizationId()),adminUser) && !RoleService.isFactoryAdministrator(adminUser, ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)))){
				AuditService.denyResult(audit, "Admin user is not an administrator");
				return outBool;
			}
			
			outBool = Rocket.enrollReaderInCommunity(audit, user);
			if(outBool){
				AuditService.permitResult(audit, "Configured " + user.getUrn() + " as a community reader");
			}
		}
		catch(FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
		}
		

		return outBool;
	}
	
	public boolean enrollReaderInCommunityProject(UserType adminUser, String userId, String communityId, String projectId){

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

			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
				AuditService.denyResult(audit, "Invalid user id '" + userId + "' in organization #" + adminUser.getOrganizationId());
				return false;
			}
			permission = AuthorizationService.getViewPermissionForMapType(NameEnumType.GROUP,adminUser.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		if(permission == null) return false;
		return enrollInCommunityLifecycle(audit, adminUser, user, permission, communityId,true);
	}
	
	
	private boolean enrollInCommunityLifecycle(AuditType audit, UserType adminUser, UserType user, BasePermissionType permission, String communityId, boolean isAdmin){
		boolean outBool = false;


		try{
			if(RoleService.getIsUserInEffectiveRole(RocketSecurity.getAdminRole(adminUser.getOrganizationId()),adminUser) == false && RoleService.isFactoryAdministrator(adminUser, ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT))) == false){
				AuditService.denyResult(audit, "User is not an administrator");
				return outBool;
			}
			
			LifecycleType lc = ((NameIdGroupFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getByObjectId(communityId, adminUser.getOrganizationId());
			if(lc == null){
				AuditService.denyResult(audit, "Lifecycle is null");
				return false;
			}
			
			UserRoleType role = (isAdmin ? RocketSecurity.getLifecycleAdminRole(lc) : RocketSecurity.getLifecycleUserRole(lc));
			
			if(Rocket.enrollInCommunityLifecycle(user, lc,role,permission)){
				outBool = true;
				AuditService.permitResult(audit, "Enrolled " + user.getUrn() + " in Lifecycle " + lc.getUrn());

			}
			else{
				AuditService.denyResult(audit, "Failed to enrolled " + user.getUrn() + " in Lifecycle " + lc.getUrn());
			}
		}
		catch(FactoryException | ArgumentException e) {
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
		}


		return outBool;
	

	}
	
	private boolean enrollInCommunityProject(AuditType audit, UserType adminUser, UserType user, BasePermissionType permission, String communityId, String projectId, boolean isAdmin){
		boolean outBool = false;

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
				return outBool;
			}
			UserRoleType role = (isAdmin ? RocketSecurity.getProjectAdminRole(proj) : RocketSecurity.getProjectUserRole(proj));
			
			if(Rocket.enrollInCommunityProject(user, lc, proj, role,permission)){
				outBool = true;
				AuditService.permitResult(audit, "Enrolled " + user.getUrn() + " in Lifecycle " + lc.getUrn() + " Project " + proj.getUrn());
			}
			else{
				AuditService.denyResult(audit, "Failed to enroll " + user.getUrn() + " in Lifecycle " + lc.getUrn() + " Project " + proj.getUrn());
			}
		}
		catch(FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
		}

		
		return outBool;

	}
	
	public boolean deleteCommunityProject(UserType adminUser, String projectId){
		boolean outBool = false;

		ProjectType outProj = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.DELETE, "deleteProject",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.PROJECT, projectId);
		try {
			outProj = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getByObjectId(projectId, adminUser.getOrganizationId());
		} catch (FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		if(outProj == null){
			AuditService.denyResult(audit,"Project doesn't exist");
			return false;
		}
		AuditService.targetAudit(audit, AuditEnumType.PROJECT, outProj.getUrn());
		if(RocketSecurity.canChangeProject(adminUser,outProj) == false){
			AuditService.denyResult(audit, ERROR_PERMISSION_DENIED);
			return false;
		}
		outBool = Rocket.deleteProject(outProj);
		if(outBool) AuditService.permitResult(audit, "Deleted project: " + outProj.getUrn());
		else AuditService.denyResult(audit, "Failed to delete project: " + outProj.getUrn());
		return outBool;
	}
	
	public boolean deleteCommunity(UserType adminUser, String communityId){
		boolean outBool = false;

		LifecycleType lc = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.DELETE, "deleteLifecycle",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LIFECYCLE, communityId);

		try {
			lc = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getByObjectId(communityId, adminUser.getOrganizationId());
		} catch (FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		if(lc == null){
			AuditService.denyResult(audit,"Project doesn't exist");
			return false;
		}
		AuditService.targetAudit(audit, AuditEnumType.LIFECYCLE, lc.getUrn());
		if(RocketSecurity.canChangeLifecycle(adminUser,lc) == false){
			AuditService.denyResult(audit, ERROR_PERMISSION_DENIED);
			return false;
		}
		outBool = Rocket.deleteLifecycle(lc);
		if(outBool) AuditService.permitResult(audit, "Deleted lifecycle: " + lc.getUrn());
		else AuditService.denyResult(audit, "Failed to delete lifecycle: " + lc.getUrn());
		return outBool;
	}
	
	public boolean configureEntitlements(UserType adminUser,String communityId, String projectId, String groupId){
		boolean outBool = false;
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
			
			if(!RocketSecurity.canChangeProject(adminUser, proj)){
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
				outBool = true;
				AuditService.permitResult(audit, "Configured community group entitlements");
			}
			else{
				AuditService.denyResult(audit, "Failed to configure community group entitlements");
			}
		} catch (FactoryException | DataAccessException | ArgumentException e) {
			logger.error(e);
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
		}
		return outBool;
	}
	
	public boolean createCommunity(UserType adminUser, String communityName){
		boolean outBool = false;
		LifecycleType outLc = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "createLifecycle",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LIFECYCLE, Rocket.getBasePath() + "/Lifecycles");
		if(!RocketSecurity.canCreateLifecycle(adminUser) ){
			AuditService.denyResult(audit, ERROR_PERMISSION_DENIED);
			return false;
		}
		try {
			outLc = Rocket.createLifecycle(adminUser, communityName);
			if(outLc != null){
				AuditService.permitResult(audit, "Created lifecycle: " + communityName);
				outBool = true;
			}
			else AuditService.denyResult(audit, "Failed to create lifecycle");
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return outBool;
	}
	
	public boolean createCommunityProject(UserType adminUser, String communityId, String projectName){
		boolean outBool = false;
		ProjectType outProj = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "createProject",AuditEnumType.USER, adminUser.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.LIFECYCLE, Rocket.getBasePath() + "/Lifecycles");
		LifecycleType outLc = getLifecycleToChange(audit, adminUser, communityId);
	

		if(RocketSecurity.canCreateProject(adminUser, outLc) == false){
			AuditService.denyResult(audit, ERROR_PERMISSION_DENIED);
			return false;
		}
		try {
			outProj = Rocket.createProject(adminUser, outLc, projectName);
			if(outProj != null){
				RocketModel.addDefaults(adminUser, outProj.getGroupId());
				AuditService.permitResult(audit, "Created project: " + projectName);
				outBool = true;
				
			}
			else AuditService.denyResult(audit, "Failed to create project: " + projectName);
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return outBool;
	}
	
	private BasePermissionType getApplicationPermissionBase(UserType user, ProjectType project, DirectoryGroupType svc){
		BasePermissionType parent = RocketSecurity.getProjectPermissionBucket(project);
		BasePermissionType outPer = null;
		try {
			BasePermissionType appBase = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getCreatePermission(user, "Applications",PermissionEnumType.APPLICATION,parent, project.getOrganizationId());
			outPer = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getCreatePermission(user, svc.getName(),PermissionEnumType.APPLICATION,appBase, project.getOrganizationId());
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return outPer;
	}
	
	public boolean generateCommunityProjectApplication(UserType user, String communityId, String projectId, String appName, boolean usePermissions, boolean useGroups, int seed, int max, double distribution, String dictionaryPath, String namesPath){
		boolean outBool = false;
		Random r = new Random();
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Generate Application",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.PROJECT, projectId);

		try {
			LifecycleType lc = getLifecycle(audit, user, communityId);
			if(lc == null){
				logger.error("Failed to retrieve lifecycle");
				return false;
			}
			ProjectType proj = getProjectToChange(audit, user, projectId);
			if(proj == null){
				logger.error("Failed to retrieve project");
				return false;
			}
			
			DirectoryGroupType appDir = RocketSecurity.getProjectDirectory(user, proj, "Applications");
			if(appDir == null){
				logger.error("Failed to retrieve applications root");
				return false;
			}
			
			DirectoryGroupType newDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newDirectoryGroup(appName, appDir, appDir.getOrganizationId());
			if(!BaseService.add(AuditEnumType.GROUP, newDir, user)){
				logger.error("Failed to create application group");
				return false;
			}
			
			newDir = BaseService.readByNameInParent(AuditEnumType.GROUP, appDir, appName, "DATA", user);
			if(newDir == null){
				logger.error("Failed to find application group");
				return false;
			}
			
			if(!configureEntitlements(user, communityId, projectId, newDir.getObjectId())){
				logger.error("Failed to configure application group");
				return false;
			}
			
			DataGeneratorUtil dutil = getGenerator(audit, user, lc.getName(), proj.getName(), lc.getGroupPath() + "/Locations", lc.getGroupPath() + "/Traits", dictionaryPath, namesPath);
			if(dutil == null) return false;
			dutil.setRandomizeSeedPopulation(randomizeSeedPopulation);
			
			BasePermissionType[] permissions = (usePermissions ? DataGeneratorData.randomApplicationPermissions(seed, max) : new BasePermissionType[0]);
			BaseGroupType[] groups = (useGroups ? DataGeneratorData.randomAccountGroups(seed, max) : new BaseGroupType[0]);
			
			List<PersonType> persons = BaseService.listByGroup(AuditEnumType.PERSON, "DATA", dutil.getPersonsDir().getObjectId(), 0L, 0, user);
			int plen = persons.size();
			logger.info("Working with " + plen + " people");
			
			BasePermissionType basePerm = getApplicationPermissionBase(user, proj, newDir);
			if(basePerm == null){
				logger.error("Invalid base permission");
				return false;
			}
			
			List<AccountType> accounts = new ArrayList<>();
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			
			for(int i = 0; i < plen; i++){
				AccountType acct = dutil.randomAccount(user, newDir);
				acct.getAttributes().add(Factories.getAttributeFactory().newAttribute(acct, "owner", persons.get(i).getObjectId()));
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ACCOUNT, acct);
				BaseParticipantType part = ((PersonParticipationFactory)Factories.getFactory(FactoryEnumType.PERSONPARTICIPATION)).newAccountPersonParticipation(persons.get(i),acct);
				((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.PERSONPARTICIPATION)).add(part);
				accounts.add(acct);
			}

			for(int i = 0; i < groups.length; i++){
				BaseGroupType g = groups[i];
				g.setOwnerId(user.getId());
				g.setParentId(newDir.getId());
				g.setOrganizationId(proj.getOrganizationId());
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUP, g);
				for(int a = 0; a < accounts.size();a++){
					if(distribution < 1.0 && r.nextDouble() > distribution) continue;
					AccountParticipantType part = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newAccountGroupParticipation(g, accounts.get(a));
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUPPARTICIPATION, part);
				}
			}
			
			for(int i = 0; i < permissions.length; i++){
				BasePermissionType p = permissions[i];
				p.setOwnerId(user.getId());
				p.setParentId(basePerm.getId());
				p.setOrganizationId(proj.getOrganizationId());
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERMISSION, p);
				if(groups.length > 0){
					for(int a = 0; a < groups.length;a++){
						if(distribution < 1.0 && r.nextDouble() > distribution) continue;
						GroupParticipantType part = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newGroupGroupParticipation(newDir, groups[a], p, AffectEnumType.GRANT_PERMISSION);
						BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUPPARTICIPATION, part);
					}
				}
				else{
					for(int a = 0; a < accounts.size();a++){
						if(distribution < 1.0 && r.nextDouble() > distribution) continue;
						AccountParticipantType part = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newAccountGroupParticipation(newDir, accounts.get(a), p, AffectEnumType.GRANT_PERMISSION);
						BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUPPARTICIPATION, part);
					}
				}
			}
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
				
			outBool = true;

			
		} catch (ArgumentException | FactoryException | DataAccessException  e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return outBool;
	}
	
	public boolean generateCommunityProjectRegion(UserType user, String communityId, String projectId, int locationSize, int seedSize, String dictionaryPath, String namesPath){
		boolean outBool = false;
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Generate Region",AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.PROJECT, projectId);

		try {
			LifecycleType lc = getLifecycle(audit, user, communityId);
			if(lc == null) return false;
			ProjectType proj = getProjectToChange(audit, user, projectId);
			if(proj == null) return false;
			
			DataGeneratorUtil dutil = getGenerator(audit, user, lc.getName(), proj.getName(), lc.getGroupPath() + "/Locations", lc.getGroupPath() + "/Traits", dictionaryPath, namesPath);
			if(dutil == null) return false;
			dutil.setRandomizeSeedPopulation(randomizeSeedPopulation);
			dutil.setOrganizePersonManagement(organizePersonManagement);
			int eventCount = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).countInGroup(dutil.getEventsDir());
			boolean initSetup = (eventCount == 0);
			if(!initSetup){
				AuditService.denyResult(audit, "One or more events already exists");
				return outBool;
			}

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
			outBool = true;


			
		} catch (FactoryException | ArgumentException | DataAccessException  e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return outBool;
	}
	
	public boolean evolveCommunityProjectRegion(UserType user, String communityId, String projectId, int epochSize, int epochEvolutions, String dictionaryPath, String namesPath){
		boolean outBool = false;
		
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
			if(initSetup){
				AuditService.denyResult(audit, "Origination event does not exist");
				return outBool;
			}
			
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();

			int count = ((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).countInGroup(dutil.getEventsDir());

			for(int i = count; i < epochSize; i++){
				// boolean modeled = (count >= (2+i));
				// if(modeled) continue;
				logger.info("MODEL Epoch " + (i + 1));
				dutil.generateEpoch(sessionId, epochEvolutions,1);
			}
			
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);

			outBool = true;
			
		} catch (FactoryException | ArgumentException | DataAccessException  e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return outBool;
	}
	
	
	
	public String reportCommunityProjectRegion(UserType user, String communityId, String projectId, String dictionaryPath, String namesPath){
		StringBuilder buff = new StringBuilder();
		
		
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
				buff.append("* " + evt.getName() + " - " + evt.getChildEvents().size() + " sub events\n");
				
				for(EventType cevt : evt.getChildEvents()){
					((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate(cevt);
					LocationType loc = cevt.getLocation();
					Factories.getAttributeFactory().populateAttributes(loc);
					buff.append("\t* " + Factories.getAttributeFactory().getAttributeValueByName(loc,"name") + " - (" + loc.getName() + ")\n");
					buff.append("\t" + cevt.getName() + " - " + cevt.getChildEvents().size() + " sub events\n");
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
					buff.append("\n");
				}
			}

		}
		catch (FactoryException | ArgumentException e) {
			
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return buff.toString();
	}
	
	
	
	public <T> boolean saveCommunityProject(T project,UserType user){
		return CommunityProjectUtil.saveCommunityProject((ProjectType)project, user);
	}
	
	public void deepPopulate(NameIdType object, UserType user){
		CommunityProjectUtil.deepPopulate(AuditEnumType.valueOf(object.getNameType().toString()), object, user);
	}
	
	
	public boolean updateCommunityProjectScript(UserType user, String communityId, String projectId, String name, String dataStr){
		boolean outBool = false;
		DataType data = getCommunityProjectScriptData(user, communityId, projectId, name);
		if(data == null) return false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Community Project Script: " + data.getUrn(),AuditEnumType.USER, user.getUrn());
		AuditService.targetAudit(audit, AuditEnumType.DATA, data.getUrn());
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
				outBool = true;
			}
		} catch (FactoryException | ArgumentException | DataException e) {
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return outBool;
	}
	private String processTokens(AuditType audit, UserType user, String communityId, String projectId, DataType data, Map<String,Object> params) throws DataException, FactoryException, ArgumentException{
		LifecycleType lc = getLifecycle(audit, user, communityId);
		ProjectType proj = getProject(audit,user, projectId);
		if(lc == null || proj == null) return null;
		
		DataGeneratorUtil dutil = getGenerator(audit, user, lc.getName(), proj.getName(), lc.getGroupPath() + "/Locations", lc.getGroupPath() + "/Traits", null, null);
		if(dutil == null) return null;
		List<EventType> evts = dutil.getEvents();
		EventType lastEvent = null;
		if(!evts.isEmpty()){
			lastEvent = evts.get(evts.size()-1);
			((NameIdFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate(lastEvent);
			List<EventType> lastChildren = lastEvent.getChildEvents();
			for(EventType cevt : lastChildren){
				((EventFactory)Factories.getFactory(FactoryEnumType.EVENT)).populate(cevt);
				LocationType loc = cevt.getLocation();
				Factories.getAttributeFactory().populateAttributes(loc);
			}
		}
		
		params.put("lastEvent", lastEvent);
		
		return ScriptService.processTokens(user, DataUtil.getValueString(data))
				.replaceAll("\\$\\{communityName\\}", lc.getName())
				.replaceAll("\\$\\{projectName\\}", proj.getName())
				.replaceAll("\\$\\{communityUrn\\}", lc.getUrn())
				.replaceAll("\\$\\{projectUrn\\}", proj.getUrn())
				.replaceAll("\\$\\{scriptName\\}", data.getName())
				.replaceAll("\\$\\{scriptUrn\\}", data.getUrn())
				.replaceAll("\\$\\{lastEventName\\}", (lastEvent != null ? lastEvent.getName() : null))
				.replaceAll("\\$\\{lastEventUrn\\}",  (lastEvent != null ? lastEvent.getUrn() : null))
		;
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
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
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
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			data = null;
		}
		return data;

	}
	
	private String generateCommunityProjectScript(UserType user, LifecycleType lifecycle, ProjectType project){
		StringBuilder buff = new StringBuilder();
		buff.append("/*\n\t" + lifecycle.getUrn() + "\n\t" + project.getUrn() + "\n*/\n\nvar result;\n\n");
		buff.append("/// HELP\n");
		buff.append("logger.info(\"${communityName} - ${projectName} - ${scriptName}\");\n");
		buff.append("/// SCRIPT\n\n");
		buff.append("\n/// Declare result as the last object since this runs outside of a function\nresult;\n");
		return buff.toString();
	}
	
	public String getCommunityProjectScript(UserType user, String communityId, String projectId, String name){
		StringBuilder buff = new StringBuilder();
		
		DataType data = getCommunityProjectScriptData(user, communityId, projectId, name);
		if(data == null) return buff.toString();
		
		try {
			String scriptData = DataUtil.getValueString(data);
			if(scriptData != null && scriptData.length() > 0) buff.append(scriptData);

			
		} catch (DataException e) {
			logger.error(e);
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
				AuditService.denyResult(audit, ERROR_NULL_COMMUNITY);
				return null;
			}
			BaseService.normalize(user, lc);
			if((toChange && RocketSecurity.canChangeLifecycle(user, lc)==false) || (toChange == false && RocketSecurity.canReadLifecycle(user, lc)==false)){

				AuditService.denyResult(audit, "User is not authorized to view lifecycle");
				return null;
			}
		}
		catch(FactoryException | ArgumentException e){
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
				AuditService.denyResult(audit, ERROR_NULL_COMMUNITY);
				return null;
			}
			BaseService.normalize(user, proj);
			if((toChange && RocketSecurity.canChangeProject(user, proj)==false) || (toChange == false && RocketSecurity.canReadProject(user, proj)==false)){
				AuditService.denyResult(audit, "User is not authorized to view project");
				return null;
			}
		}
		catch(FactoryException | ArgumentException e){
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			if(!dutil.initialize()){
				AuditService.denyResult(audit, "Failed to initialize data generator");
				return null;
			}
			if(dutil.getProject() == null){
				AuditService.denyResult(audit, "Failed to load project via data generator");
				return null;
			}
		}
		catch(ArgumentException | FactoryException e){
			AuditService.denyResult(audit, String.format(FactoryException.LOGICAL_EXCEPTION_MSG, e.getMessage()));
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			dutil = null;
		}
		return dutil;
	}

}
