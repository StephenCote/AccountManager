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
package org.cote.rocket.services;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.AddressFactory;
import org.cote.accountmanager.data.factory.ContactFactory;
import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.GroupParticipationFactory;
import org.cote.accountmanager.data.factory.IParticipationFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.PersonParticipationFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.ApplicationPermissionType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.GroupParticipantType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.JAXBUtil;
import org.cote.propellant.objects.IdentityConnectionType;
import org.cote.propellant.objects.IdentityDataEnumType;
import org.cote.propellant.objects.IdentityDataImportType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.ResourceType;
import org.cote.rocket.BulkFactories;
import org.cote.rocket.Factories;
import org.cote.rocket.Rocket;
import org.cote.rocket.RocketSecurity;
import org.cote.rocket.util.IdentityEntityUtil;

public class IdentityService {
	public static final Logger logger = LogManager.getLogger(IdentityService.class);

	private static String lifecycleName = "Identity Service";
	private static String lifecycleAdmin = "identityadmin";
	private static CredentialType lifecycleAdminCredential = null;
	public static final int MAX_ATTRIBUTES = 10;
	protected static final String [] HEADER_ACCOUNT = {"uid","owner","accountType","email","attribute1","attribute2","attribute3","attribute4","attribute5","attribute6","attribute7","attribute8","attribute9","attribute10"};
	protected static final String [] HEADER_PERSON = {"uid","firstName","middleName","lastName","gender","birthdate","personType","email","manager","homeAddress","homeCity","homeRegion","homeState","homePostalCode","homeCountry","workAddress","workCity","workRegion","workState","workPostalCode","workCountry","attribute1","attribute2","attribute3","attribute4","attribute5","attribute6","attribute7","attribute8","attribute9","attribute10"};
	protected static final String [] HEADER_PERMISSION = {"uid","description","type"};
	protected static final String [] HEADER_MAP = {"pid","aid"};
	protected static final String [] HEADER_ENTITLEMENTMAP = {"pid","gid"};
	protected static final String [] HEADER_GROUP = {"gid","type"};
	protected static final String [] HEADER_GROUPMAP = {"gid","aid"};


	private UserType adminUser = null;
	private LifecycleType lifecycle = null;
	private boolean initialized = false;
	
	private OrganizationType productOrganization = null;
	
	public IdentityService(OrganizationType org){
		productOrganization = org;
	}
	
	public static String getLifecycleAdmin() {
		return lifecycleAdmin;
	}
	public static void setLifecycleAdmin(String lifecycleAdmin) {
		IdentityService.lifecycleAdmin = lifecycleAdmin;
	}
	public static void setLifecycleAdminCredential(CredentialType lifecycleAdminDefaultPassword) {
			IdentityService.lifecycleAdminCredential = lifecycleAdminDefaultPassword;
	}
	public boolean isInitialized(){
		return initialized;
	}
	public void setLifecycle(LifecycleType l){
		this.lifecycle = l;
	}
	public LifecycleType getLifecycle(){
		return lifecycle;
	}
	public UserType getAdminUser(){
		return adminUser;
	}
	public OrganizationType getOrganization(){
		return productOrganization;
	}
	
	public DirectoryGroupType getConfigDirectory(ProjectType proj){
		DirectoryGroupType dir = null;
		try {
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(getAdminUser(), "Config",((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(),proj.getOrganizationId()), productOrganization.getId());
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return dir;
	}
	
	private DataType getConfigData(ProjectType proj, String name){
		DataType data = null;
		try {
			data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(name, getConfigDirectory(proj));
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return data;
	}
	public IdentityConnectionType getConnectionConfig(ProjectType proj){
		DataType data = getConfigData(proj,"isim.config");
		if(data == null){
			logger.error("ISIM Configuration is not defined for project " + proj.getName());
			return null;
		}
		IdentityConnectionType conn = null;
		try{
			DataUtil.setCipher(data, KeyService.getPrimarySymmetricKey(productOrganization.getId()));
			conn = JAXBUtil.importObject(IdentityConnectionType.class, DataUtil.getValueString(data));
		}
		catch(DataException e){
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return conn;
	}
	public boolean setConnectionConfig(ProjectType proj, IdentityConnectionType cfg){

		DataType data = getConfigData(proj,"identity.connection.config");
		boolean outBool = false;
		boolean add = false;
		cfg.setProjectId(proj.getId());
		try {
			if(data == null){
				data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).newData(adminUser, getConfigDirectory(proj).getId());
				data.setName("identity.connection.config");
				data.setMimeType("application/xml");
				IdentityEntityUtil.applyDefaultValues(cfg);
				add = true;
			}
			DataUtil.setCipher(data, KeyService.getPrimarySymmetricKey(productOrganization.getId()));
			DataUtil.setValueString(data, JAXBUtil.exportObject(IdentityConnectionType.class,cfg));
			if(add){
				outBool = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).add(data);
			}
			else outBool = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).update(data);
		} catch (ArgumentException | DataException | FactoryException e) {
			logger.error(e);
		} 
		return outBool;
	}
	

	
	public boolean deleteProjectContents(ProjectType project, boolean deleteEntireProject, boolean deleteConfiguration){
		logger.info("Deleting project contents. Note: This does not delete the project structure itself, only the IdentityService specific project contents.");
		DirectoryGroupType svcDir = getApplicationsRoot(project);
		boolean outBool = false;

			
			logger.info("Deleting IdentityService Project " + project.getName() + " Data");
			
			try {
				if(deleteConfiguration){
					logger.info("Deleting configuration");
					((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).delete(getConfigDirectory(project));
				}
				logger.info("Deleting service accounts");
				List<DirectoryGroupType> svcs = this.getApplications(project);

				PersonRoleType baseRole = this.getProjectRoleBucket(project);
				((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).delete(baseRole);
				
				for(int i = 0; i < svcs.size(); i++){
					((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(svcs.get(i));
					((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(svcs.get(i));
					((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).deleteAccountsInGroup(svcs.get(i));
					ApplicationPermissionType perParent = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).findPermission(PermissionEnumType.APPLICATION,svcs.get(i).getPath(),productOrganization.getId());
					if(perParent != null){
						List<ApplicationPermissionType> perms = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionList(perParent, PermissionEnumType.APPLICATION, 0, 0, productOrganization.getId());
						long[] ids = new long[perms.size()];
						for(int p = 0;p<ids.length;p++) ids[p]=perms.get(p).getId();
						((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).deletePermissionsByIds(ids,productOrganization.getId());
						((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).delete(perParent);
					}
				}
				if(svcDir == null){
					logger.warn("Service directory does not exist");
				}
				else{
					logger.info("Deleting applications and service groups");
					((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).delete(svcDir);
				}
				logger.info("Deleting persons");
				DirectoryGroupType perDir = RocketSecurity.getProjectDirectory(adminUser, project, "Persons");
				((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).deletePersonsInGroup(perDir);
				
				if(deleteEntireProject){
					Rocket.deleteProject(project);
				}
				if(deleteEntireProject || deleteConfiguration) {
					logger.info("Cleaning up orphaned data references");
					Factories.cleanupOrphans();
				}
				
				outBool = true;
			} catch (FactoryException | ArgumentException | DataAccessException e) {
				logger.error(e);
			}
		
		return outBool;
	}
	public DirectoryGroupType getPersonsRoot(ProjectType project){
		
		// Note: Persons is a pre-allocated project group
		return RocketSecurity.getProjectDirectory(adminUser, project, "Persons");
	}
	public DirectoryGroupType getApplicationsRoot(ProjectType project){
		
		// Note: Applications is a pre-allocated project group
		return RocketSecurity.getProjectDirectory(adminUser,project,"Applications");
	}
	public DirectoryGroupType getApplication(ProjectType project, String name){
		DirectoryGroupType out_dir = null;
		try {
			out_dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(name, getApplicationsRoot(project), project.getOrganizationId());
		} catch (FactoryException | ArgumentException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return out_dir;
	}
	
	public ProjectType createProject(String name){
		if(initialized == false){
			logger.error("IdentityService is not initialized");
			return null;
		}
		ProjectType proj = null;
		try {
			proj = Rocket.createProject(adminUser, lifecycle, name);
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			logger.error(e.getMessage());
		}
		return proj;
	}
	
	public ProjectType getProject(String name){
		if(initialized == false){
			logger.error("IdentityService is not initialized");
			return null;
		}
		ProjectType proj = null;
		try {
			proj = Rocket.getProject(name, lifecycle, productOrganization.getId());
		} catch (FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
		}
		return proj;
	}

	public boolean initialize(){
		boolean outBool = false;
		initialized = false;
		if(Rocket.getIsSetup() == false){
			logger.error("Base product is not configured correctly.");
			return false;
		}
		if(productOrganization == null) productOrganization = Rocket.getRocketOrganization();
		else if(Factories.isSetup(productOrganization.getId()) == false){
			logger.error("Product organization is not configured correctly.");
			return false;
			
		}
		try{
			UserType chkUser = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(lifecycleAdmin, productOrganization.getId());
			if(chkUser == null){
				chkUser = ((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).newUser(lifecycleAdmin, UserEnumType.NORMAL, UserStatusEnumType.NORMAL, productOrganization.getId());
				if(Factories.getNameIdFactory(FactoryEnumType.USER).add(chkUser) == false){
					logger.error("Failed to create product admin user");
					return false;
				}
				chkUser = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(lifecycleAdmin, productOrganization.getId());
				CredentialService.newHashedPasswordCredential(chkUser, chkUser,new String(lifecycleAdminCredential.getCredential()), true);
			}
			adminUser = chkUser;
			
			LifecycleType chkLife = Rocket.getLifecycle(lifecycleName, productOrganization.getId());
			if(chkLife == null){
				chkLife = Rocket.createLifecycle(adminUser, lifecycleName);
				if(chkLife == null){
					logger.error("Failed to create product lifecycle");
					return false;
				}
			}
			
			lifecycle = chkLife;
			
			initialized = true;
			outBool = true;
		}
		catch (FactoryException | ArgumentException | DataAccessException e) {
			logger.error(e.getMessage());
		} 

		return outBool;
	}
	
	public int importApplications(String sessionId, ProjectType proj, List<DirectoryGroupType> groups){
		DirectoryGroupType rootDir = RocketSecurity.getProjectDirectory(adminUser, proj,"Applications");

		if(rootDir == null) return -1;
		for(int i = 0; i < groups.size();i++){
			groups.get(i).getAttributes().add(Factories.getAttributeFactory().newAttribute(groups.get(i),"projectid",Long.toString(proj.getId())));
		}
		return importDirectoryGroups(sessionId,rootDir, groups);
	}


	public List<DirectoryGroupType> getApplications(ProjectType project){
		DirectoryGroupType rootDir = RocketSecurity.getProjectDirectory(adminUser, project,"Applications");

		return getDirectoryGroups(rootDir);
	}
	public List<ApplicationPermissionType> getApplicationPermissions(DirectoryGroupType svc){
		List<ApplicationPermissionType> permissions = new ArrayList<ApplicationPermissionType>();
		try {
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(svc);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(svc);
			ApplicationPermissionType parent = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).findPermission(PermissionEnumType.APPLICATION, svc.getPath(), productOrganization.getId());
			permissions = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionList(parent, PermissionEnumType.APPLICATION, 0, 0, productOrganization.getId());
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(e);
		}
		return permissions;

	}
	private List<DirectoryGroupType> getDirectoryGroups(DirectoryGroupType svc){
		List<DirectoryGroupType> outList = new ArrayList<DirectoryGroupType>();
		try{
			outList = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getListByParent(GroupEnumType.DATA, svc, 0, 0, productOrganization.getId());
		}
		catch (FactoryException | ArgumentException  e) {
			logger.error(e);
		}
		return outList;
	}
	
	public List<AccountType> getAccounts(ProjectType proj,DirectoryGroupType service){
		return getAccountList(proj, service);
	}
	private List<AccountType> getAccountList(ProjectType proj, DirectoryGroupType svc){
		List<AccountType> outList = new ArrayList<AccountType>();
		
		try{
			outList = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).listInGroup(svc, 0, 0, productOrganization.getId());
		}
		catch (FactoryException | ArgumentException  e) {
			logger.error(e.getMessage());
		}
		return outList;
	}

	/// Note: for role import, it's currently expecting the role structure to be purged before the import
	///
	public int importRoles(String sessionId, ProjectType project, List<PersonRoleType> roles){
		int out_rec = 0;
		try {
			PersonRoleType baseRole = getProjectRoleBucket(project);
			
			List<BaseRoleType> currentList = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleList(baseRole, 0, 0, project.getOrganizationId());
			if(currentList.size() > 0) throw new FactoryException("Attempted to import roles on a non-empty structure");

			logger.info("Importing " + roles.size() + " roles");
			if(roles.isEmpty()) return 0;
			
			for(int i = 0; i < roles.size();i++){
				roles.get(i).getAttributes().add(Factories.getAttributeFactory().newAttribute(roles.get(i),"projectid",Long.toString(project.getId())));

				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ROLE, roles.get(i));
			}

		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		
		return out_rec;
	}
	public int importPermissions(String sessionId,ProjectType proj, DirectoryGroupType grp, List<BasePermissionType> permissions){
		int out_rec = 0;
		Set<String> currentSet = new HashSet<String>();
		List<BasePermissionType> importSet = new ArrayList<BasePermissionType>();
		
		try {
			BasePermissionType parent = getApplicationPermissionBase(proj,grp); 
					//((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).makePath(adminUser, PermissionEnumType.APPLICATION, grp);
			List<BasePermissionType> currentList = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionList(parent, PermissionEnumType.APPLICATION, 0, 0, productOrganization.getId());
			for(int i = 0; i < currentList.size();i++) currentSet.add(currentList.get(i).getName());
			
			for(int i = 0; i < permissions.size();i++){
				if(currentSet.contains(permissions.get(i).getName()) == false){
					importSet.add(permissions.get(i));
					permissions.get(i).setParentId(parent.getId());
				}
			}
			logger.info("Importing " + importSet.size() + " permissions out of available " + permissions.size() + " permissions");
			if(importSet.isEmpty()){
				return 0;
			}
			Factories.getAttributeFactory().populateAttributes(grp);
			for(int i = 0; i < importSet.size();i++){
				importSet.get(i).getAttributes().add(Factories.getAttributeFactory().newAttribute(importSet.get(i),"projectid",Long.toString(proj.getId())));
				importSet.get(i).getAttributes().add(Factories.getAttributeFactory().newAttribute(importSet.get(i),"applicationid",Long.toString(grp.getId())));

				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERMISSION, importSet.get(i));
			}

		} catch (FactoryException | ArgumentException e) {
			
			logger.error(e);
		} 
		
		return out_rec;
	}
	public int importDirectoryGroups(String sessionId, DirectoryGroupType grp, List<DirectoryGroupType> groups){
		int out_rec = 0;
		Set<String> currentSet = new HashSet<String>();
		List<DirectoryGroupType> importSet = new ArrayList<DirectoryGroupType>();
		try {
			List<BaseGroupType> currentList = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getListByParent(GroupEnumType.DATA, grp, 0, 0, productOrganization.getId());
			for(int i = 0; i < currentList.size();i++) currentSet.add(currentList.get(i).getName());
			for(int i = 0; i < groups.size();i++){
				if(currentSet.contains(groups.get(i).getName()) == false){
					importSet.add(groups.get(i));
				}
			}
			logger.info("Importing " + importSet.size() + " of " + groups.size());
			if(importSet.isEmpty()) return 0;
			
			for(int i = 0; i < importSet.size();i++){
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUP, importSet.get(i));
			}
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} 
		
		return out_rec;
	}


	public int importAccounts(String sessionId, ProjectType project, DirectoryGroupType svc, List<AccountType> accounts){
		return importAccountType(sessionId, project, svc, accounts);
	}

	public int importPermissionMembers(String sessionId, ProjectType project, DirectoryGroupType svc, BasePermissionType perm,List<AccountType> accounts, boolean replace){
		int out_rec = 0;
		try {
			// 2015/04/24 - Moved delete op up one level to batch them together
			//
			if(replace) ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).deleteParticipationsByAffect(svc,perm);
			
			for(int i = 0; i < accounts.size();i++){

				/// Authorization participants are automatically mapped to bulk records 
				///
				AccountType rec = accounts.get(i);
				if(rec.getId().compareTo(0L) == 0){
					continue;
				}
				/*
				 * /// 2015/04/02 - since the participants were just deleted, this is dog slow for late-adds because it forces a lookup on each record
				 * /// so, just add the bulk entry below 
				if(AuthorizationService.setPermission(adminUser,rec,svc,perm,true) == false){
					logger.warn("Failed to grant permission " + perm.getName() + " to " + rec.getName() + " on " + svc.getName());
				}
				*/
				AccountParticipantType part = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newAccountGroupParticipation(svc, rec, perm, AffectEnumType.GRANT_PERMISSION);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUPPARTICIPATION, part);
				out_rec++;
			}

		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		return out_rec;
	}
	public int importEntitlementMembers(String sessionId, ProjectType project, DirectoryGroupType svc, BasePermissionType perm,List<AccountGroupType> groups, boolean replace){
		int out_rec = 0;
		try {
			// 2015/04/24 - Moved delete operation up one level to batch them together
			//
			if(replace) ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).deleteParticipationsByAffect(svc,perm);
			for(int i = 0; i < groups.size();i++){

				/// Authorization participants are automatically mapped to bulk records 
				///
				AccountGroupType rec = groups.get(i);
				if(rec.getId().compareTo(0L) == 0){
					continue;
				}
				/*
				 * /// 2015/04/02 - since the participants were just deleted, this is dog slow for late-adds because it forces a lookup on each record
				 * /// so, just add the bulk entry below 
				if(AuthorizationService.setPermission(adminUser,rec,svc,perm,true) == false){
					logger.warn("Failed to grant permission " + perm.getName() + " to " + rec.getName() + " on " + svc.getName());
				}
				*/
				GroupParticipantType part = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newGroupGroupParticipation(svc, rec, perm, AffectEnumType.GRANT_PERMISSION);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUPPARTICIPATION, part);
				out_rec++;
			}
			//out_rec = groups.size();

		} catch (FactoryException | ArgumentException e) {
			
			logger.error(e);
		}
		
		return out_rec;
	}
	public int importGroupMembers(String sessionId, ProjectType project, DirectoryGroupType svc, BaseGroupType group,List<AccountType> accounts, boolean replace){
		int out_rec = 0;
		try {
			// 2015/04/24 - Moved delete op up one level to batch them together
			//
			if(replace) ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).deleteParticipations(group);
			for(int i = 0; i < accounts.size();i++){
				/// Authorization participants are automatically mapped to bulk records 
				///
				AccountType rec = accounts.get(i);
				if(rec.getId().compareTo(0L) == 0){
					continue;
				}
				AccountParticipantType part = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newAccountGroupParticipation(group, rec);
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUPPARTICIPATION, part);
				out_rec++;
			}

		} catch (FactoryException | ArgumentException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		return out_rec;
	}
	
	public int importAccountType(String sessionId, ProjectType project, DirectoryGroupType svc, List<AccountType> accounts){

		int out_rec = 0;
		Set<String> currentSet = new HashSet<String>();
		List<AccountType> importSet = new ArrayList<AccountType>();
		DirectoryGroupType personDir = RocketSecurity.getProjectDirectory(adminUser, project, "Persons");
		try {
			ProcessingInstructionType pi = new ProcessingInstructionType();
			pi.setOrderClause("NAME ASC");
			logger.info("Scanning " + importSet.size() + " potential imports for existing entries");
			List<AccountType> currentList = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).listInGroup(svc, 0, 0, productOrganization.getId());
			for(int i = 0; i < currentList.size();i++) currentSet.add(currentList.get(i).getName());
			for(int i = 0; i < accounts.size();i++){

				if(currentSet.contains(accounts.get(i).getName()) == false){
					importSet.add(accounts.get(i));
				}
			}
			logger.info("Importing " + importSet.size() + " accounts out of available " + accounts.size() + " accounts");
			if(importSet.isEmpty()){
				return 0;
			}
			
			for(int i = 0; i < importSet.size();i++){

				String owner = Factories.getAttributeFactory().getAttributeValueByName(importSet.get(i), "owner");
				importSet.get(i).getAttributes().add(Factories.getAttributeFactory().newAttribute(importSet.get(i),"projectid",Long.toString(project.getId())));
				importSet.get(i).getAttributes().add(Factories.getAttributeFactory().newAttribute(importSet.get(i),"applicationid",Long.toString(svc.getId())));
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ACCOUNT, importSet.get(i));
				if(owner != null){
					PersonType person = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).getByNameInGroup(owner, personDir);
					if(person != null){
						BaseParticipantType part = ((PersonParticipationFactory)Factories.getFactory(FactoryEnumType.PERSONPARTICIPATION)).newAccountPersonParticipation(person,importSet.get(i));
						((IParticipationFactory)Factories.getBulkFactory(FactoryEnumType.PERSONPARTICIPATION)).add(part);
					}
					else{
						logger.debug("Detected orphan account. No person exists for owner " + owner);
					}
				}
				else{
					logger.debug("Detected orphan account. No owner value specified");
				}
			}

		} catch (FactoryException | ArgumentException e) {
			
			logger.error(e);
		}
		
		return out_rec;
	}
	public int importGroups(String sessionId, UserType user, LifecycleType lc, ProjectType project, DirectoryGroupType service, List<AccountGroupType> recs) throws DataAccessException{
		return importGroupType(sessionId, user,lc, project, service, recs);
	}
	private int importGroupType(String sessionId, UserType user, LifecycleType lc,ProjectType project, DirectoryGroupType service, List<AccountGroupType> recs) throws DataAccessException{
		int out_rec = 0;
		Set<String> currentSet = new HashSet<String>();
		List<AccountGroupType> importSet = new ArrayList<AccountGroupType>();
		try {
			List<AccountGroupType> currentList = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getListByParent(GroupEnumType.ACCOUNT,service, 0, 0, productOrganization.getId());

			for(int i = 0; i < currentList.size();i++) currentSet.add(currentList.get(i).getName());
			for(int i = 0; i < recs.size();i++){

				if(currentSet.contains(recs.get(i).getName()) == false){
					importSet.add(recs.get(i));
				}
			}
			logger.info("Importing " + importSet.size() + " groups out of available " + recs.size() + " groups");
			if(importSet.isEmpty()) return 0;
			
			for(int i = 0; i < importSet.size();i++){
				AccountGroupType new_group = importSet.get(i);
				
				new_group.getAttributes().add(Factories.getAttributeFactory().newAttribute(new_group,"projectid",Long.toString(project.getId())));
				new_group.getAttributes().add(Factories.getAttributeFactory().newAttribute(new_group,"applicationid",Long.toString(service.getId())));
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUP, new_group);
				 RocketSecurity.configureProjectDirectory(user, lc, project, new_group);

			}
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
		} 
		
		return out_rec;
	}
	public int importPersons(String sessionId, ProjectType project, List<PersonType> recs){
		return importPersonType(sessionId, project, recs);
	}
	private int importPersonType(String sessionId, ProjectType project, List<PersonType> recs){
		int out_rec = 0;
		Set<String> currentSet = new HashSet<String>();
		List<PersonType> importSet = new ArrayList<PersonType>();
		try {
			List<PersonType> currentList = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).listInGroup(RocketSecurity.getProjectDirectory(adminUser, project, "Persons"), 0, 0, productOrganization.getId());
			for(int i = 0; i < currentList.size();i++) currentSet.add(currentList.get(i).getName());
			for(int i = 0; i < recs.size();i++){

				if(currentSet.contains(recs.get(i).getName()) == false){
					importSet.add(recs.get(i));
				}
			}
			logger.info("Importing " + importSet.size() + " persons out of available " + recs.size() + " persons");
			if(importSet.isEmpty()) return 0;
			
			for(int i = 0; i < importSet.size();i++){
				PersonType new_person = importSet.get(i);
				ContactInformationType tmpCinfo = new_person.getContactInformation();
				new_person.setContactInformation(null);
				
				new_person.getAttributes().add(Factories.getAttributeFactory().newAttribute(new_person,"projectid",Long.toString(project.getId())));
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERSON, new_person);
				
				ContactInformationType cit = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation(new_person);
				cit.setOwnerId(tmpCinfo != null ? tmpCinfo.getOwnerId() : project.getOwnerId());
				new_person.setContactInformation(cit);
				
				for(int c = 0; tmpCinfo != null && c < tmpCinfo.getContacts().size();c++){
					ContactType tmp = tmpCinfo.getContacts().get(c);
					ContactType newContact = new ContactType();
					newContact.setDescription("");
					newContact.setNameType(NameEnumType.CONTACT);
					newContact.setOrganizationId(tmp.getOrganizationId());
					newContact.setOwnerId(tmp.getOwnerId());
					newContact.setGroupId(tmp.getGroupId());
					newContact.setName(tmp.getName());
					newContact.setLocationType(tmp.getLocationType());
					newContact.setContactType(tmp.getContactType());
					newContact.setContactValue(tmp.getContactValue());
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACT, newContact);
					cit.getContacts().add(newContact);
				}
				for(int c = 0;  tmpCinfo != null && c < tmpCinfo.getAddresses().size();c++){
					AddressType tmp = tmpCinfo.getAddresses().get(c);
					AddressType newAddress = new AddressType();
					newAddress.setDescription("");
					newAddress.setOrganizationId(tmp.getOrganizationId());
					newAddress.setNameType(NameEnumType.ADDRESS);
					newAddress.setOwnerId(tmp.getOwnerId());
					newAddress.setGroupId(tmp.getGroupId());
					newAddress.setName(tmp.getName());
					newAddress.setLocationType(tmp.getLocationType());
					newAddress.setAddressLine1(tmp.getAddressLine1());
					newAddress.setAddressLine2("");
					newAddress.setCity(tmp.getCity());
					newAddress.setCountry(tmp.getCountry());
					newAddress.setPostalCode(tmp.getPostalCode());
					newAddress.setRegion(tmp.getRegion());
					newAddress.setState(tmp.getState());

					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ADDRESS, newAddress);
					cit.getAddresses().add(newAddress);
				}
				BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.CONTACTINFORMATION, cit);
			}
		} catch (FactoryException | ArgumentException e) {
			logger.error(e);
			logger.error(e.getStackTrace());
		} 
		
		return out_rec;
	}
	public String getLifecycleRoleBucketName(){
		return lifecycle.getName() + " Roles";
	}
	public String getProjectRoleBucketName(ProjectType project){
		return project.getName() + " Roles";
	}

	public void clearRoleParticipations(BaseRoleType roleBase) throws FactoryException, ArgumentException{
		IdentityServiceDAL sd = new IdentityServiceDAL(this);
		List<List<BaseRoleType>> broles = sd.getRoleHierarchy(roleBase.getId());
		for(int p = 0; p < broles.size();p++){
			List<BaseRoleType> roles = broles.get(p);
			for(int r = 0; r < roles.size(); r++){
				BaseRoleType role = roles.get(r);
				((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).deleteRoleParticipations(role);
				EffectiveAuthorizationService.pendUpdate(role);
			}
		}
		EffectiveAuthorizationService.rebuildPendingRoleCache();
	}
	
	public boolean synchronizeEntitlements(ProjectType project, boolean replace){
		/// TODO: This needs to be an extensible interface, not hard-coded
		///
		logger.warn("TODO: Import and load entitlements from identity service");
		return false;
	}
	/*
	 * EXAMPLE Synchronize
	public boolean synchronizeRoles(ProjectType project){
		boolean outBool = false;
		logger.warn("TODO: Implement synchronize roles");
		if(true) return false;
		PersonRoleType roleBase = getProjectRoleBucket(project);
		IdentityServiceDAL dal = new IdentityServiceDAL(this);
		try {
			logger.info("Deleting role cache");
			List<BaseRoleType> childRoles = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleList(roleBase, 0, 0, project.getOrganizationId());
			for(int i = 0; i < childRoles.size();i++){
				((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).deleteRole(childRoles.get(i));
			}
			List<PersonRoleType> impRoles = getRolesFromIdentityService(project);
			
			String sessionId = BulkFactories.getBulkFactory().newBulkSession();
			importRoles(sessionId, project, impRoles);
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			
			List<BaseRoleType> roles = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleList(roleBase,0,0,project.getOrganizationId());
			/// Now have to fix-up the hierarchy from ISIM
			int updated = 0;
			
			Map<String,BaseRoleType> roleDnMap = new HashMap<String,BaseRoleType>();
			logger.info("Creating role map with " + roles.size() + " roles");
			
			/// Build up a map of dns
			///
			for(int i = 0; i < roles.size();i++){
				BaseRoleType role = roles.get(i);
				Factories.getAttributeFactory().populateAttributes(role);
				roleDnMap.put(role.getName(),role);
			}
			
			sessionId = BulkFactories.getBulkFactory().newBulkSession();
			
			/// Now re-map roles to parents
			///
			for(int i = 0; i < roles.size();i++){
				BaseRoleType role = roles.get(i);
				String parentDn = Factories.getAttributeFactory().getAttributeValueByName(role, "erparent");

				List<ResourceType> subs = this.getRoleMembersFromIdentityService(project, role); 

				for(int m = 0; m < subs.size(); m++){
					if(roleDnMap.containsKey(subs.get(m).getName())==false){
						logger.warn("Undetected role: " + subs.get(m).getName());
						continue;
					}
					BaseRoleType crole = roleDnMap.get(subs.get(m).getName());
					
					///if(crole.getParentId() != role.getParentId()){
					///	logger.warn("Potential role-child duplication detected");
					///}
					
					crole.setParentId(role.getId());
					((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).updateRole(crole);
					updated++;
				}
				
				List<PersonType> members = dal.getRolePersonMembers(project, (PersonRoleType)role);
				for(int m = 0; m < members.size();m++){
					//PersonParticipantType ap = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).newPersonRoleParticipation(role, person);
					//if (((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).add(ap))
					PersonParticipantType ppt = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).newPersonRoleParticipation(role, members.get(m));
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ROLEPARTICIPATION, ppt);
				}
				
			}
			if(updated > 0){
				logger.info("Updated " + updated + " relationships");
			}
			BulkFactories.getBulkFactory().write(sessionId);
			BulkFactories.getBulkFactory().close(sessionId);
			EffectiveAuthorizationService.rebuildRoleRoleCache(roles,project.getOrganizationId());
			outBool = true;
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		
		return outBool;
	}
	*/
	public BasePermissionType getApplicationPermissionBase(ProjectType project, DirectoryGroupType svc){
		BasePermissionType parent = RocketSecurity.getProjectPermissionBucket(project);
		BasePermissionType outPer = null;
		try {
			BasePermissionType appBase = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getCreatePermission(adminUser, "Applications",PermissionEnumType.APPLICATION,parent, productOrganization.getId());
			outPer = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getCreatePermission(adminUser, svc.getName(),PermissionEnumType.APPLICATION,appBase, productOrganization.getId());
		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return outPer;
	}
	
	/// Note: the group name is actually stored in an extended attribute in the Account Manager schema since the isim dn is stored in the name field
	/// So, to query for the id, it needs to use one of the custom views
	///
	public ApplicationPermissionType getApplicationPermission(ProjectType project, String serviceName, String groupName){
		ApplicationPermissionType permission = null;
		DirectoryGroupType svc = null;
		
		try {
			
			svc = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(serviceName, getApplicationsRoot(project), productOrganization.getId());
			if(svc == null){
				logger.error("Null service '" + serviceName + "'");
				return null;
			}
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(svc);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(svc);
			permission = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).findPermission(PermissionEnumType.APPLICATION,svc.getPath() + "/" + groupName,productOrganization.getId());

			if(permission == null){
				logger.error("Null service permission '" + groupName + "'");
				return null;
			}

		} catch (FactoryException | ArgumentException | DataAccessException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return permission;
	}
	public PersonRoleType getProjectRole(ProjectType project,String name, PersonRoleType parent){
		PersonRoleType role = null;
		if(parent == null) parent = getProjectRoleBucket(project);
		try {
			role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getPersonRoleByName(name, parent,productOrganization.getId());
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return role;
	}
	public PersonRoleType getCreateProjectRole(ProjectType project,String name, PersonRoleType parent){
		PersonRoleType role = null;
		if(parent == null) parent = getProjectRoleBucket(project);
		try {
			role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreatePersonRole(adminUser, name, parent);
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return role;
	}
	public PersonRoleType getLifecycleRoleBucket(){
		PersonRoleType role = null;
		try {
			role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreatePersonRole(adminUser, getLifecycleRoleBucketName(), null);
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return role;
	}
	public PersonRoleType getProjectRoleBucket(ProjectType project){
		PersonRoleType role = null;
		try {
			role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreatePersonRole(adminUser, getProjectRoleBucketName(project), getLifecycleRoleBucket());
		} catch (FactoryException | ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return role;
	}

	
	public List<PersonRoleType> getRolesFromIdentityService(ProjectType project){
		List<PersonRoleType> out_roles = new ArrayList<PersonRoleType>();
		logger.warn("TODO: Connect to identity service");
		return out_roles;
	}
	
	public List<ResourceType> getRoleMembersFromIdentityService(ProjectType project, BaseRoleType role){
		List<ResourceType> out_accounts = new ArrayList<ResourceType>();
		logger.warn("TODO: Connect to identity service");
		return out_accounts;
	}
	
	public List<DirectoryGroupType> getApplicationsFromIdentityService(ProjectType proj){
		List<DirectoryGroupType> out_apps = new ArrayList<DirectoryGroupType>();
		logger.warn("TODO: Connect to identity service");
		return out_apps;
	}
	public List<PersonType> getPersonsFromIdentityService(ProjectType svc){
		List<PersonType> out_persons = new ArrayList<PersonType>();
		logger.warn("TODO: Connect to identity service");
		return out_persons;
	}
	public List<AccountType> getAccountsFromIdentityService(ProjectType project, DirectoryGroupType svc){
		List<AccountType> out_accounts = new ArrayList<AccountType>();
		return out_accounts;
	}
	public List<ApplicationPermissionType> getApplicationPermissionsFromIdentityService(ProjectType proj, DirectoryGroupType svc){
		List<ApplicationPermissionType> perms = new ArrayList<ApplicationPermissionType>();
		return perms;
	}
	public static IdentityDataImportType newIdentityDataImport(String name, IdentityDataEnumType type){
		IdentityDataImportType obj = new IdentityDataImportType();
		obj.setName(name);
		obj.setType(type);
		return obj;
	}
	public void setIdentityDataImportHeader(IdentityDataImportType imp){
		imp.getHeader().clear();
		switch(imp.getType()){
			case PERSON: imp.getHeader().addAll(Arrays.asList(HEADER_PERSON)); break;
			case ACCOUNT: imp.getHeader().addAll(Arrays.asList(HEADER_ACCOUNT)); break;
			case PERMISSION: imp.getHeader().addAll(Arrays.asList(HEADER_PERMISSION)); break;
			case MAP: imp.getHeader().addAll(Arrays.asList(HEADER_MAP)); break;
			case GROUP : imp.getHeader().addAll(Arrays.asList(HEADER_GROUP)); break;
			case GROUPMAP : imp.getHeader().addAll(Arrays.asList(HEADER_GROUPMAP)); break;
			case ENTITLEMENTMAP : imp.getHeader().addAll(Arrays.asList(HEADER_ENTITLEMENTMAP)); break;
		}
	}

	public void importApplicationData(UserType user, LifecycleType lc, ProjectType pj, DirectoryGroupType application, IdentityDataImportType[] imports){
		long start_process = System.currentTimeMillis();
		int maxRecordCount = 50000;
		int bufferSize = 0;

		boolean currentAccountAggression = true;
		boolean currentAccountSafety = true;
		boolean currentPersonAggression = true;
		boolean currentPersonSafety = true;
		boolean currentGroupPartAggression = true;
		boolean currentGroupPartSafety = true;
		try {
			currentAccountAggression = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).isAggressiveKeyFlush();
			currentAccountSafety = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).isUseThreadSafeCollections();
			currentPersonAggression = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).isAggressiveKeyFlush();
			currentPersonSafety = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).isUseThreadSafeCollections();
			currentGroupPartAggression = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).isAggressiveKeyFlush();
			currentGroupPartSafety = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).isUseThreadSafeCollections();
		
			((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).setAggressiveKeyFlush(false);
			((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).setUseThreadSafeCollections(false);
			((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).setAggressiveKeyFlush(false);
			((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).setUseThreadSafeCollections(false);
			((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).setAggressiveKeyFlush(false);
			((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).setUseThreadSafeCollections(false);

			DirectoryGroupType personDir = RocketSecurity.getProjectDirectory(user,pj, "Persons");
			DirectoryGroupType contactDir = RocketSecurity.getProjectDirectory(user,pj, "Contacts");
			DirectoryGroupType addressDir = RocketSecurity.getProjectDirectory(user,pj, "Addresses");
			
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(application);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(application);
			BasePermissionType permissionParent = getApplicationPermissionBase(pj, application);
			
			List<PersonType> persons = new ArrayList<PersonType>();
			List<AccountType> accounts = new ArrayList<AccountType>();
			List<AccountGroupType> groups = new ArrayList<AccountGroupType>();
			List<AccountType> mapAccounts = new ArrayList<AccountType>();
			List<AccountGroupType> mapGroups = new ArrayList<AccountGroupType>();
			List<BasePermissionType> permissions = new ArrayList<BasePermissionType>();
			List<Long> replacePerms = new ArrayList<>();
			List<Long> replaceGroups = new ArrayList<>();
			for(int d = 0; d < imports.length; d++){
				if(imports[d].getHeader().isEmpty()){
					setIdentityDataImportHeader(imports[d]);
				}
				if(imports[d].getHeader().isEmpty()){
					logger.error("Data import does not define an header.");
					continue;
				}
				DataType data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getDataByName(imports[d].getName(),application);
				String sessionId = BulkFactories.getBulkFactory().newBulkSession();
				if(data == null){
					logger.warn("Data '" + imports[d].getName() + "' doesn't exist in application path " + application.getPath());
					continue;
				}
				logger.info("Processing " + data.getName() + " (" + imports[d].getType().toString() + ") with " + data.getSize() + " bytes");	
				BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(DataUtil.getValue(data))));
				CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(imports[d].getHeader().toArray(new String[0]));
				CSVParser csvFileParser = new CSVParser(reader, csvFileFormat);
				String lastPer = null;
				String lastGrp = null;
				BasePermissionType mapPer = null;
				BaseGroupType mapGrp = null;
				mapAccounts.clear();
				mapGroups.clear();
				List csvRecords = csvFileParser.getRecords(); 
				 for (int i = 1; i < csvRecords.size(); i++) {
					 CSVRecord record = (CSVRecord) csvRecords.get(i);
					 if(imports[d].getType() == IdentityDataEnumType.PERMISSION){
						 BasePermissionType newPermission = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).newPermission(user,record.get("uid"), PermissionEnumType.APPLICATION, permissionParent,user.getOrganizationId());
						 newPermission.getAttributes().add(Factories.getAttributeFactory().newAttribute(newPermission, "businessDescription", record.get("description")));
						 permissions.add(newPermission);
					 }
					 if(imports[d].getType() == IdentityDataEnumType.GROUPMAP){
						 String grpName = record.get("gid");
						 String memName = record.get("aid");
						 if(memName.contains(";")){
							 mapAccounts.clear();
							 mapGrp = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupByName(grpName, GroupEnumType.ACCOUNT, application, user.getOrganizationId());
							 if(mapGrp == null){
								 logger.warn("Failed to find group '" + grpName + "'");
								 continue;
							 }
							 String[] aAcctNames = memName.split(";");
							 StringBuffer nameBuff = new StringBuffer();
							 for(int a = 0; a < aAcctNames.length;a++){
								 if(a > 0) nameBuff.append(",");
								 nameBuff.append("'" + aAcctNames[a]+ "'");
							 }
							 List<QueryField> fields = new ArrayList<>();
							 fields.add(QueryFields.getFieldGroup(application.getId()));
							 QueryField match = new QueryField(SqlDataEnumType.VARCHAR,"name",nameBuff.toString());
							 match.setComparator(ComparatorEnumType.IN);
							 fields.add(match);
							 mapAccounts = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountList(fields.toArray(new QueryField[0]), 0, 0, application.getOrganizationId());
								
							 if(mapGrp.getId() > 0L) replaceGroups.add(mapGrp.getId());
							 logger.info("Importing " + mapAccounts.size() + " compacted members for " + mapGrp.getName());
							 
							importGroupMembers(sessionId, pj, application, mapGrp, mapAccounts,false);
							 bufferSize += mapAccounts.size();
							 mapAccounts.clear();
							 if(bufferSize > maxRecordCount || (i == csvRecords.size() - 1)){
								 replacePerms.clear();
								 BulkFactories.getBulkFactory().write(sessionId);
								 BulkFactories.getBulkFactory().close(sessionId);
								 sessionId = BulkFactories.getBulkFactory().newBulkSession();
								 bufferSize = 0;
							 }
						 } // end compacted format
						 else{
							 if(lastGrp != grpName){
								 if(mapPer != null && mapAccounts.size() > 0){
									 //if(mapPer.getId() > 0L) replacePerms.add(mapPer.getId());
									 importGroupMembers(sessionId, pj, application, mapGrp, mapAccounts,true);
								 }
								 mapAccounts.clear();
								 lastGrp = grpName;
								 mapGrp = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupByName(grpName, GroupEnumType.ACCOUNT, application, user.getOrganizationId());
								 if(mapGrp == null){
									 logger.warn("Failed to find group '" + grpName + "'");
									 continue;
								 }
							 }
							 AccountType acct = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName(memName, application);
							 if(acct == null){
								 logger.warn("Failed to find account '" + memName + "'");
								 continue;
							 }
							 mapAccounts.add(acct);
						 } // end not compacted format
					 }
					 if(imports[d].getType() == IdentityDataEnumType.MAP){
						 String perName = record.get("pid");
						 String acctName = record.get("aid");
						 if(acctName.contains(";")){
							 mapAccounts.clear();
							 mapPer = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionByName(perName, PermissionEnumType.APPLICATION, permissionParent, user.getOrganizationId());
							 if(mapPer == null){
								 logger.warn("Failed to find permission '" + perName + "'");
								 continue;
							 }
							 String[] aAcctNames = acctName.split(";");
							 StringBuffer nameBuff = new StringBuffer();
							 for(int a = 0; a < aAcctNames.length;a++){
								 if(a > 0) nameBuff.append(",");
								 nameBuff.append("'" + aAcctNames[a]+ "'");
							 }
							 List<QueryField> fields = new ArrayList<>();
							 fields.add(QueryFields.getFieldGroup(application.getId()));
							 QueryField match = new QueryField(SqlDataEnumType.VARCHAR,"name",nameBuff.toString());
							 match.setComparator(ComparatorEnumType.IN);
							 fields.add(match);
							 mapAccounts = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountList(fields.toArray(new QueryField[0]), 0, 0, application.getOrganizationId());

							 if(mapPer.getId() > 0L) replacePerms.add(mapPer.getId());
							 logger.info("Importing " + mapAccounts.size() + " compacted members for " + mapPer.getName());
							 
							importPermissionMembers(sessionId, pj, application, mapPer, mapAccounts,false);
							 bufferSize += mapAccounts.size();
							 mapAccounts.clear();
							 if(bufferSize > maxRecordCount || (i == csvRecords.size() - 1)){
								 logger.info("Preparing to replace " + replacePerms.size() + " entitlement entries");
								 ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).deleteParticipationsByAffects(application,ArrayUtils.toPrimitive(replacePerms.toArray(new Long[0])));
								 replacePerms.clear();
								 BulkFactories.getBulkFactory().write(sessionId);
								 BulkFactories.getBulkFactory().close(sessionId);
								 sessionId = BulkFactories.getBulkFactory().newBulkSession();
								 bufferSize = 0;
							 }
						 } // end compacted format
						 else{
							 if(lastPer != perName){
								 if(mapPer != null && mapAccounts.size() > 0){
									 //if(mapPer.getId() > 0L) replacePerms.add(mapPer.getId());
									 importPermissionMembers(sessionId, pj, application, mapPer, mapAccounts,true);
								 }
								 mapAccounts.clear();
								 lastPer = perName;
								 mapPer = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionByName(perName, PermissionEnumType.APPLICATION, permissionParent, user.getOrganizationId());
								 if(mapPer == null){
									 logger.warn("Failed to find permission '" + perName + "'");
									 continue;
								 }
							 }
							 AccountType acct = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).getAccountByName(acctName, application);
							 if(acct == null){
								 logger.warn("Failed to find account '" + acctName + "'");
								 continue;
							 }
							 mapAccounts.add(acct);
						 } // end not compacted format
						 
					 }
					 if(imports[d].getType() == IdentityDataEnumType.ENTITLEMENTMAP){
						 String perName = record.get("pid");
						 String grpName = record.get("gid");
						 if(grpName.contains(";")){
							 mapGroups.clear();
							 mapPer = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionByName(perName, PermissionEnumType.APPLICATION, permissionParent, user.getOrganizationId());
							 if(mapPer == null){
								 logger.warn("Failed to find permission '" + perName + "'");
								 continue;
							 }
							 String[] aGrpNames = grpName.split(";");
							 StringBuffer nameBuff = new StringBuffer();
							 for(int a = 0; a < aGrpNames.length;a++){
								 if(a > 0) nameBuff.append(",");
								 nameBuff.append("'" + aGrpNames[a]+ "'");
							 }
							 List<QueryField> fields = new ArrayList<>();
							 //fields.add(QueryFields.getFieldGroup(application.getId()));
							 fields.add(QueryFields.getFieldParent(application.getId()));
							 QueryField match = new QueryField(SqlDataEnumType.VARCHAR,"name",nameBuff.toString());
							 match.setComparator(ComparatorEnumType.IN);
							 fields.add(match);
							 mapGroups = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getAccountGroups(fields, application.getOrganizationId());

							 if(mapPer.getId() > 0L) replacePerms.add(mapPer.getId());
							 logger.info("Importing " + mapGroups.size() + " compacted members for " + mapPer.getName());
							 
							importEntitlementMembers(sessionId, pj, application, mapPer, mapGroups,false);
							 bufferSize += mapGroups.size();
							 mapGroups.clear();
							 if(bufferSize > maxRecordCount || (i == csvRecords.size() - 1)){
								 replacePerms.clear();
								 BulkFactories.getBulkFactory().write(sessionId);
								 BulkFactories.getBulkFactory().close(sessionId);
								 sessionId = BulkFactories.getBulkFactory().newBulkSession();
								 bufferSize = 0;
							 }
						 } // end compacted format
						 else{
							 if(lastPer != perName){
								 if(mapPer != null && mapGroups.size() > 0){
									 //if(mapPer.getId() > 0L) replacePerms.add(mapPer.getId());
									 importEntitlementMembers(sessionId, pj, application, mapPer, mapGroups,true);
								 }
								 mapGroups.clear();
								 lastPer = perName;
								 mapPer = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionByName(perName, PermissionEnumType.APPLICATION, permissionParent, user.getOrganizationId());
								 if(mapPer == null){
									 logger.warn("Failed to find permission '" + perName + "'");
									 continue;
								 }
							 }
							 AccountGroupType grp = (AccountGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupByName(grpName, GroupEnumType.ACCOUNT,application,application.getOrganizationId());
							 if(grp == null){
								 logger.warn("Failed to find account '" + grpName + "'");
								 continue;
							 }
							 mapGroups.add(grp);
						 } // end not compacted format
						 
					 }
					 else if(imports[d].getType() == IdentityDataEnumType.GROUP){
						 AccountGroupType newGrp = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newAccountGroup(user, record.get("gid"), application,application.getOrganizationId());
						 groups.add(newGrp);
					 }
					 else if(imports[d].getType() == IdentityDataEnumType.ACCOUNT){
						 AccountType newAccount = ((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).newAccount(user, record.get("uid"), AccountEnumType.NORMAL, AccountStatusEnumType.NORMAL, application.getId());
						 newAccount.getAttributes().add(Factories.getAttributeFactory().newAttribute(newAccount, "owner", record.get("owner")));
						 newAccount.getAttributes().add(Factories.getAttributeFactory().newAttribute(newAccount, "accountType", record.get("accountType")));
						 for(int a = 0; a < MAX_ATTRIBUTES; a++){
							 String attrPair = record.get("attribute" + (a + 1));
							 if(attrPair==null || attrPair.length() == 0) continue;
							 String[] pairs = attrPair.split("=");
							 if(pairs.length != 2) continue;
							 newAccount.getAttributes().add(Factories.getAttributeFactory().newAttribute(newAccount, pairs[0], pairs[1]));
						 }
						 accounts.add(newAccount);

					 } // end type
					 else if(imports[d].getType() == IdentityDataEnumType.PERSON){
						 PersonType newPerson = ((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).newPerson(user,personDir.getId());
						 newPerson.setName(record.get("uid"));
						 newPerson.setFirstName(record.get("firstName"));
						 newPerson.setLastName(record.get("lastName"));
						 newPerson.setMiddleName(record.get("middleName"));
						 newPerson.setGender(record.get("gender"));
						 newPerson.setBirthDate(CalendarUtil.getXmlGregorianCalendar(CalendarUtil.importDateFromString(record.get("birthdate"),"yyyy-MM-dd")));
						 newPerson.setDescription("");
						 newPerson.setSuffix("");
						 newPerson.setAlias("");
						 newPerson.setPrefix("");
						 newPerson.setTitle("");
						 newPerson.getAttributes().add(Factories.getAttributeFactory().newAttribute(newPerson, "manager", record.get("manager")));
						 newPerson.getAttributes().add(Factories.getAttributeFactory().newAttribute(newPerson, "email", record.get("email")));
						 for(int a = 0; a < MAX_ATTRIBUTES; a++){
							 String attrPair = record.get("attribute" + (a + 1));
							 if(attrPair==null || attrPair.length() == 0) continue;
							 String[] pairs = attrPair.split("=");
							 if(pairs.length != 2) continue;
							 newPerson.getAttributes().add(Factories.getAttributeFactory().newAttribute(newPerson, pairs[0], pairs[1]));
						 }
						 
						 /// this is a temporary placeholder used to carry the data into the bulk load operation
						 /// it's copied properly in the importPersonType step
						 ///
						 ContactInformationType cit = new ContactInformationType();
						 cit.setOwnerId(user.getId());
						 newPerson.setContactInformation(cit);
						 ContactType email = ((ContactFactory)Factories.getFactory(FactoryEnumType.CONTACT)).newContact(user, contactDir.getId());
						 email.setContactType(ContactEnumType.EMAIL);
						 email.setContactValue(record.get("email"));
						 email.setName(newPerson.getName() + " Work Email");
						 email.setLocationType(LocationEnumType.WORK);
						 cit.getContacts().add(email);

						 AddressType home = ((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).newAddress(user, addressDir.getId());
						 home.setName(newPerson.getName() + " Home Address");
						 home.setLocationType(LocationEnumType.HOME);
						 home.setAddressLine1(record.get("homeAddress"));
						 home.setCity(record.get("homeCity"));
						 home.setState(record.get("homeState"));
						 home.setRegion(record.get("homeRegion"));
						 home.setPostalCode(record.get("homePostalCode"));
						 home.setCountry(record.get("homeCountry"));
						 cit.getAddresses().add(home);
						 AddressType work = ((AddressFactory)Factories.getFactory(FactoryEnumType.ADDRESS)).newAddress(user, addressDir.getId());
						 work.setName(newPerson.getName() + " Work Address");
						 work.setLocationType(LocationEnumType.WORK);
						 work.setAddressLine1(record.get("workAddress"));
						 work.setCity(record.get("workCity"));
						 work.setState(record.get("workState"));
						 work.setRegion(record.get("workRegion"));
						 work.setPostalCode(record.get("workPostalCode"));
						 work.setCountry(record.get("workCountry"));
						 cit.getAddresses().add(work);
						 persons.add(newPerson);

					 } // end type					 
					 
	
				 } // end records
				reader.close();
				if(imports[d].getType() == IdentityDataEnumType.ACCOUNT){
					importAccounts(sessionId, pj, application, accounts);
					accounts.clear();
				}
				else if(imports[d].getType() == IdentityDataEnumType.PERSON){
					importPersons(sessionId, pj, persons);
					persons.clear();
				}
				else if(imports[d].getType() == IdentityDataEnumType.GROUP){
					importGroups(sessionId, user,lc,pj, application, groups);
					groups.clear();
				}
				else if(imports[d].getType() == IdentityDataEnumType.PERMISSION){
					importPermissions(sessionId, pj, application, permissions);
					permissions.clear();
				}
				/// non-compacted remainder handler
				///
				else if(mapPer != null && mapAccounts.size() > 0){
					 importPermissionMembers(sessionId, pj, application, mapPer, mapAccounts,true);
				 }
				
				BulkFactories.getBulkFactory().write(sessionId);
				BulkFactories.getBulkFactory().close(sessionId);

				((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).delete(data);
				
			} // end imports
			
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			
		}
		catch(FactoryException | ArgumentException | IOException | DataException | DataAccessException e) {
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		finally{
			try{
				((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).setAggressiveKeyFlush(currentGroupPartAggression);
				((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).setUseThreadSafeCollections(currentGroupPartSafety);
				((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).setAggressiveKeyFlush(currentAccountAggression);
				((AccountFactory)Factories.getFactory(FactoryEnumType.ACCOUNT)).setUseThreadSafeCollections(currentAccountSafety);
				((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).setAggressiveKeyFlush(currentPersonAggression);
				((PersonFactory)Factories.getFactory(FactoryEnumType.PERSON)).setUseThreadSafeCollections(currentPersonSafety);
			}
			catch(FactoryException f){
				logger.error(f);
			}
		}
		logger.info("Processed data in " + (System.currentTimeMillis() - start_process) + "ms");
	}
}
