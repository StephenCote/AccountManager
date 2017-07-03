/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.rocket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserPermissionType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.propellant.objects.ResourceType;
import org.cote.propellant.objects.ScheduleType;
import org.cote.propellant.objects.types.ResourceEnumType;
import org.cote.rocket.factory.FactoryDefaults;
import org.cote.rocket.factory.LifecycleFactory;
import org.cote.rocket.factory.ProjectFactory;
import org.cote.rocket.factory.ResourceFactory;
import org.cote.rocket.factory.ScheduleFactory;

public class Rocket {
	public static final Logger logger = LogManager.getLogger(Rocket.class);
	
	private static OrganizationType rocketOrganization = null;
	private static DirectoryGroupType rocketDir = null;
	private static boolean is_setup = false;
	private static String basePath = "/Rocket";
	private static String lifecyclePath = "/Lifecycles";
	private static String projectPath = "/Projects";

	
	
	public static Map<String,FactoryEnumType> rocketGroupToTypeMap = new HashMap<String,FactoryEnumType>();
	static{
		rocketGroupToTypeMap.put("Stages", FactoryEnumType.STAGE);
		rocketGroupToTypeMap.put("Schedules", FactoryEnumType.SCHEDULE);
		rocketGroupToTypeMap.put("Goals", FactoryEnumType.GOAL);
		rocketGroupToTypeMap.put("Budgets", FactoryEnumType.BUDGET);
		rocketGroupToTypeMap.put("Estimates", FactoryEnumType.ESTIMATE);
		rocketGroupToTypeMap.put("Cases", FactoryEnumType.CASE);
		rocketGroupToTypeMap.put("Data", FactoryEnumType.DATA);
		rocketGroupToTypeMap.put("Times", FactoryEnumType.TIME);
		rocketGroupToTypeMap.put("Costs", FactoryEnumType.COST);
		rocketGroupToTypeMap.put("Requirements", FactoryEnumType.REQUIREMENT);
		rocketGroupToTypeMap.put("Methodologies", FactoryEnumType.METHODOLOGY);
		rocketGroupToTypeMap.put("Processes", FactoryEnumType.PROCESS);
		rocketGroupToTypeMap.put("ProcessSteps", FactoryEnumType.PROCESSSTEP);
		rocketGroupToTypeMap.put("ValidationRules", FactoryEnumType.VALIDATIONRULE);
		rocketGroupToTypeMap.put("Forms", FactoryEnumType.FORM);
		rocketGroupToTypeMap.put("FormTemplates", FactoryEnumType.FORM);
		rocketGroupToTypeMap.put("FormElements", FactoryEnumType.FORMELEMENT);
		rocketGroupToTypeMap.put("Work", FactoryEnumType.WORK);
		rocketGroupToTypeMap.put("Artifacts", FactoryEnumType.ARTIFACT);
		rocketGroupToTypeMap.put("Dependencies", FactoryEnumType.ARTIFACT);
		rocketGroupToTypeMap.put("Tasks", FactoryEnumType.TASK);
		rocketGroupToTypeMap.put("Tickets", FactoryEnumType.TICKET);
		rocketGroupToTypeMap.put("Stories", FactoryEnumType.TASK);
		rocketGroupToTypeMap.put("Resources", FactoryEnumType.RESOURCE);
		rocketGroupToTypeMap.put("Notes", FactoryEnumType.NOTE);
		rocketGroupToTypeMap.put("Modules", FactoryEnumType.MODULE);
		rocketGroupToTypeMap.put("Models", FactoryEnumType.MODEL);
		
		rocketGroupToTypeMap.put("Policies", FactoryEnumType.POLICY);
		rocketGroupToTypeMap.put("Facts", FactoryEnumType.FACT);
		rocketGroupToTypeMap.put("Patterns", FactoryEnumType.PATTERN);
		rocketGroupToTypeMap.put("Functions", FactoryEnumType.FUNCTION);
		rocketGroupToTypeMap.put("FunctionFacts", FactoryEnumType.FUNCTIONFACT);
		rocketGroupToTypeMap.put("Operations", FactoryEnumType.OPERATION);
		rocketGroupToTypeMap.put("Rules", FactoryEnumType.RULE);
	}
	
	
	public static boolean getIsSetup(){
		if(is_setup == true) return is_setup;
		
		is_setup = Factories.isSetup(getRocketOrganization().getId());
		return is_setup;
	}
	public static String getBasePath(){
		return basePath;
	}
	public static String getLifecyclePath(){
		return lifecyclePath;
	}
	public static String getProjectPath(){
		return projectPath;
	}
	public static DirectoryGroupType getLifecycleGroup(long organizationId){
		DirectoryGroupType group = null;
		try {
			group = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null, GroupEnumType.DATA,getBasePath() + getLifecyclePath(), organizationId);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return group;
	}
	public static DirectoryGroupType getProjectGroup(LifecycleType lc){
		DirectoryGroupType group = null;
		try {
			
			group = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null, GroupEnumType.DATA, getBasePath() + getLifecyclePath() + "/" + lc.getName() + getProjectPath(), lc.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return group;
	}
	public static boolean enrollReaderInCommunity(AuditType audit, UserType user){
		boolean out_bool = false;
		try {
			UserRoleType rocketUserRole = RocketSecurity.getUserRole(user.getOrganizationId());
			UserRoleType userReaderRole = RoleService.getAccountUsersReaderUserRole(user.getOrganizationId());
			UserRoleType roleReaderRole = RoleService.getRoleReaderUserRole(user.getOrganizationId());
			UserRoleType permissionReaderRole = RoleService.getPermissionReaderUserRole(user.getOrganizationId());
			RoleService.addUserToRole(user, rocketUserRole);
			RoleService.addUserToRole(user, userReaderRole);
			RoleService.addUserToRole(user, roleReaderRole);
			RoleService.addUserToRole(user, permissionReaderRole);
			AuditService.targetAudit(audit,AuditEnumType.ROLE,"Roles: " + rocketUserRole.getName() + ", " + userReaderRole.getName() + ", " + roleReaderRole.getName());
			AuditService.permitResult(audit, "Add user to roles");
			out_bool = true;
			EffectiveAuthorizationService.rebuildPendingRoleCache();
		} catch (FactoryException e) {
			
			logger.error("Error",e);
			AuditService.denyResult(audit,"Error: " + e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
			AuditService.denyResult(audit,"Error: " + e.getMessage());
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
			AuditService.denyResult(audit,"Error: " + e.getMessage());
		}
		return out_bool;
		
	}	
	public static boolean enrollAdminInCommunity(AuditType audit, UserType user){
		boolean out_bool = false;
		try {
			UserRoleType rocketAdminRole = RocketSecurity.getAdminRole(user.getOrganizationId());
			UserRoleType userReaderRole = RoleService.getAccountUsersReaderUserRole(user.getOrganizationId());
			UserRoleType roleReaderRole = RoleService.getRoleReaderUserRole(user.getOrganizationId());
			UserRoleType permissionReaderRole = RoleService.getPermissionReaderUserRole(user.getOrganizationId());
			RoleService.addUserToRole(user, rocketAdminRole);
			RoleService.addUserToRole(user, userReaderRole);
			RoleService.addUserToRole(user, roleReaderRole);
			RoleService.addUserToRole(user, permissionReaderRole);
			AuditService.targetAudit(audit,AuditEnumType.ROLE,"Roles: " + rocketAdminRole.getName() + ", " + userReaderRole.getName() + ", " + roleReaderRole.getName());
			AuditService.permitResult(audit, "Add user to roles");
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			out_bool = true;
		} catch (FactoryException e) {
			
			logger.error("Error",e);
			AuditService.denyResult(audit,"Error: " + e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
			AuditService.denyResult(audit,"Error: " + e.getMessage());
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
			AuditService.denyResult(audit,"Error: " + e.getMessage());
		}
		
		return out_bool;
		
	}
	public static boolean enrollInCommunityLifecycle(UserType user, String lifecycleName, BasePermissionType permission){
		LifecycleType out_lc = null;
		try {
			out_lc = Rocket.getLifecycle(lifecycleName, user.getOrganizationId());
		} catch (FactoryException e1) {
			
			logger.error("Error",e1);
			return false;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return enrollInCommunityLifecycle(user, out_lc,RocketSecurity.getLifecycleUserRole(out_lc),permission);
		
	}
	public static boolean enrollInCommunityLifecycle(UserType user, LifecycleType lifecycle, UserRoleType role, BasePermissionType permission){
		boolean out_bool = false;
		
		if(lifecycle == null){
			return false;
		}
		UserType adminUser = null;
		try {
			adminUser = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Admin", user.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		if(adminUser == null){
			return false;
		}
		try {
			out_bool = AuthorizationService.authorize(adminUser, user, role, permission, true);

			if(out_bool){
				DirectoryGroupType recDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Resources", ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lifecycle.getGroupId(), lifecycle.getOrganizationId()),lifecycle.getOrganizationId());
				ResourceType rec = ((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).getByNameInGroup(user.getName(), recDir);
				if(rec == null){
					rec = ((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).newResource(user, recDir.getId());
					rec.setResourceType(ResourceEnumType.USER);
					rec.setResourceDataId(user.getId());
					((ResourceFactory)Factories.getFactory(FactoryEnumType.RESOURCE)).add(rec);
				}
				EffectiveAuthorizationService.rebuildPendingRoleCache();
			}
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}

		return out_bool;
	}
	public static boolean enrollInCommunityProject(UserType user, String lifecycleName, String projectName, BasePermissionType permission){
		LifecycleType out_lc = null;
		ProjectType out_proj = null;
		
		try {
			out_lc = Rocket.getLifecycle(lifecycleName, user.getOrganizationId());
			if(out_lc != null) out_proj = Rocket.getProject(projectName, out_lc, user.getOrganizationId());
		} catch (FactoryException e1) {
			
			logger.error("Error",e1);
			return false;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		if(out_lc == null){
			return false;
		}
		if(out_proj == null){
			return false;
		}
		return enrollInCommunityProject(user, out_lc, out_proj, RocketSecurity.getProjectUserRole(out_proj), permission);
	}
	public static boolean enrollInCommunityProject(UserType user, LifecycleType lifecycle, ProjectType project, UserRoleType role, BasePermissionType permission){
		boolean out_bool = false;

		UserType adminUser = null;
		try {
			adminUser = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Admin", user.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		if(adminUser == null){
			return false;
		}
		try {
			out_bool = AuthorizationService.authorize(adminUser, user, role, permission, true);
			if(out_bool){
				EffectiveAuthorizationService.rebuildPendingRoleCache();
			}
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return out_bool;

	}
	
	public static boolean deleteProject(ProjectType proj){
		boolean out_bool = false;
		try {
			((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).delete(RocketSecurity.getProjectPermissionBucket(proj));
			((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).delete(RocketSecurity.getProjectRoleBucket(proj));
			((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).delete(proj);
			//((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).deleteDirectoryGroup(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(), proj.getOrganizationId()));
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).delete(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(), proj.getOrganizationId()));
			Factories.cleanupOrphans();
			/// Need to clean up remaining artifacts
			///
			out_bool = true;
			
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return out_bool;
	}
	public static boolean deleteLifecycle(LifecycleType lc){
		boolean out_bool = false;
		try {
			((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).delete(RocketSecurity.getLifecycleRoleBucket(lc));
			((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).delete(lc);
			//((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).deleteDirectoryGroup(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lc.getGroupId(), lc.getOrganizationId()));
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).delete(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lc.getGroupId(), lc.getOrganizationId()));
			Factories.cleanupOrphans();
			/// Need to clean up remaining artifacts
			///
			out_bool = true;
			
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return out_bool;
	}
	public static ProjectType getProject(String projectName, LifecycleType lc, long organizationId) throws FactoryException, ArgumentException{
		DirectoryGroupType dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null, GroupEnumType.DATA, basePath + "/Lifecycles/" + lc.getName() + "/Projects/" + projectName, organizationId);
		if(dir == null) return null;
		return ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getByNameInGroup(projectName, dir);
	}
	public static LifecycleType getLifecycle(String lifecycleName, long organizationId) throws FactoryException,ArgumentException{
		DirectoryGroupType dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null, GroupEnumType.DATA, basePath + "/Lifecycles/" + lifecycleName, organizationId);
		if(dir == null){
			logger.error("Lifecycle path for " + lifecycleName + " not found");
			return null;
		}
		return ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getByNameInGroup(lifecycleName, dir);
	}
	public static LifecycleType createLifecycle(UserType owner, String lifecycleName) throws FactoryException, ArgumentException, DataAccessException{
		LifecycleType out_type = null;
		DirectoryGroupType lcDir = RocketSecurity.getCreateLifecycleDirectory(owner, lifecycleName);
		if(lcDir == null) return out_type;
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		
		out_type = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).newLifecycle(owner, lcDir.getId());
		out_type.setName(lifecycleName);
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.LIFECYCLE, out_type);
		if(RocketSecurity.setupBulkLifecycleStructure(sessionId, out_type, owner)){
			ScheduleType sched = ((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).newSchedule(owner, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Schedules", lcDir, out_type.getOrganizationId()).getId());
			sched.setName("Default " + lifecycleName + " Schedule");
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.SCHEDULE, sched);
			out_type.getSchedules().add(sched);

			BulkFactories.getBulkFactory().write(sessionId);
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			//EffectiveAuthorizationService.rebuildGroupRoleCache(owner.getOrganizationId());
		}
		else{
			logger.error("Failed to setup lifecycle '" + lifecycleName + "'");
			out_type = null;
		}
		BulkFactories.getBulkFactory().close(sessionId);
		/*
		out_type = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).newLifecycle(owner, lcDir);
		out_type.setName(lifecycleName);
		if(((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).addLifecycle(out_type)==false){
			return null;
		}
		out_type = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getByNameInGroup(lifecycleName, lcDir);
		if(RocketSecurity.setupLifecycleStructure(out_type, owner)){
			RoleService.addUserToRole(owner, RocketSecurity.getLifecycleAdminRole(out_type));
		}
		*/
		return out_type;
	}
	public static ProjectType createProject(UserType owner, LifecycleType lc, String projectName) throws FactoryException, ArgumentException, DataAccessException{
		ProjectType out_type = null;
		DirectoryGroupType lcDir = RocketSecurity.getCreateProjectDirectory(owner, lc, projectName);
		if(lcDir == null) return out_type;
		
		logger.info("Creating " + lc.getName() + " :: " + projectName + " project");
		((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).repopulate(lc);
		
		String sessionId = BulkFactories.getBulkFactory().newBulkSession();
		out_type = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).newProject(owner, lcDir.getId());
		out_type.setName(projectName);
		
		BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PROJECT, out_type);
		if(RocketSecurity.setupBulkProjectStructure(sessionId,lc, out_type, owner)){
			//RoleService.addUserToRole(owner, RocketSecurity.getProjectAdminRole(out_type));
			ScheduleType sched = ((ScheduleFactory)Factories.getFactory(FactoryEnumType.SCHEDULE)).newSchedule(owner, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName("Schedules", lcDir, out_type.getOrganizationId()).getId());
			sched.setName("Default " + projectName + " Schedule");
			BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.SCHEDULE, sched);
			out_type.setSchedule(sched);
			BulkFactories.getBulkFactory().write(sessionId);
			if(out_type.getId() <= 0L) throw new FactoryException("Bulk Loaded Project not created correctly");
			lc.getProjects().add(out_type);
			/// BUG NOTE:
			/// If a lifecycle is added, not populated, and cached
			/// Then populating will duplicate values
			/// And the update will only match the first duplicate entry
			/// 
			((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).update(lc);
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			//EffectiveAuthorizationService.rebuildGroupRoleCache(lc.getOrganizationId());
		}
		else{
			logger.error("Unexpected result setting up project structure.");
		}
		BulkFactories.getBulkFactory().close(sessionId);
		/*
		out_type = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).newProject(owner, lcDir);
		out_type.setName(projectName);
		if(((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).addProject(out_type)==false){
			return null;
		}
		out_type = ((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).getByNameInGroup(projectName, lcDir);
		if(RocketSecurity.setupProjectStructure(lc, out_type, owner)){
			RoleService.addUserToRole(owner, RocketSecurity.getProjectAdminRole(out_type));
			lc.getProjects().add(out_type);
			((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).updateLifecycle(lc);
		}
		*/
		return out_type;
	}
	public static boolean isApplicationEnvironmentConfigured(long organizationId){
		boolean out_bool = false;
		if(getRocketApplicationGroup(organizationId) != null) out_bool = true;
		return out_bool;
	}
	public static boolean configureApplicationEnvironment(String adminPassword) throws FactoryException,DataAccessException, ArgumentException{
		OrganizationType org = getRocketOrganization();
		if(org == null) throw new FactoryException("Rocket Organization is not setup");
		return configureApplicationEnvironment(org.getId(), adminPassword);
	}
	
	public static boolean configureApplicationEnvironment(UserType adminUser) throws FactoryException, DataAccessException, ArgumentException{
		boolean is_configured = false;
		long organizationId = adminUser.getOrganizationId();
		if(RoleService.getIsUserInRole(RoleService.getSystemAdministratorUserRole(organizationId), adminUser) == false){
			logger.error("Only a system administrator may configure an organization for Rocket communities.");
			return false;
		}
		DirectoryGroupType applicationDirectory = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreatePath(adminUser, basePath, organizationId);
		if(applicationDirectory == null) return false;
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(applicationDirectory);
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(applicationDirectory);
		DirectoryGroupType lDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(adminUser, "Lifecycles", applicationDirectory, organizationId);
		if(lDir == null) return false;
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(lDir);
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(lDir);
		UserRoleType rocketRoles = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).makePath(adminUser, RoleEnumType.USER,applicationDirectory.getPath(), organizationId);
		UserPermissionType rocketPermissions = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).makePath(adminUser, PermissionEnumType.USER, applicationDirectory.getPath(),organizationId);

		UserRoleType lifecycleRoles = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).makePath(adminUser, RoleEnumType.USER,lDir.getPath(), organizationId);
		UserPermissionType lifecyclePermissions = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).makePath(adminUser, PermissionEnumType.USER, lDir.getPath(),organizationId);

		RocketSecurity.setupContainerPermissions(adminUser, rocketPermissions);
		RocketSecurity.setupContainerRoles(adminUser, rocketRoles);
		
		
		RocketSecurity.setupRolesToReadContainer(adminUser,rocketRoles,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},applicationDirectory);
		RocketSecurity.setupRolesToEditContainer(adminUser,rocketRoles,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},applicationDirectory);
		EffectiveAuthorizationService.pendGroupUpdate(applicationDirectory);
		RocketSecurity.setupRolesToReadContainer(adminUser,rocketRoles,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},lDir);
		RocketSecurity.setupRolesToEditContainer(adminUser,rocketRoles,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},lDir);
		EffectiveAuthorizationService.pendGroupUpdate(lDir);
		
		/*
		/// Give admin users rights to edit the entire application directory ("/Rocket")
		AuthorizationService.authorizeRoleType(adminUser, adminRole, applicationDirectory, true, true, true, true);

		/// Allow audit and user role users to view the application directory ("/Rocket")
		AuthorizationService.authorizeRoleType(adminUser, auditRole, applicationDirectory, true, false, false, false);
		AuthorizationService.authorizeRoleType(adminUser, userRole, applicationDirectory, true, false, false, false);
		
		/// authorize the admin role to read/write the parent lifecycle directory, which is needed to obtain the list of all lifecycles
		AuthorizationService.authorizeRoleType(adminUser, adminRole, lDir, true, true, true, false);
		/// authorize the reader role to read/write the parent lifecycle directory, which is needed to obtain the list of all lifecycles
		AuthorizationService.authorizeRoleType(adminUser, auditRole, lDir, true, false, false, false);
		AuthorizationService.authorizeRoleType(adminUser, userRole, lDir, true, false, false, false);
		*/
		return is_configured;
	}
	
	public static boolean configureApplicationEnvironment(long organizationId, String adminPassword) throws FactoryException, DataAccessException, ArgumentException{
		boolean is_configured = false;
		UserType adminUser = SessionSecurity.login("Admin", CredentialEnumType.HASHED_PASSWORD,adminPassword, organizationId);
		if(adminUser == null){
			logger.error("Invalid administrator credential");
			return false;
		}
		is_configured = configureApplicationEnvironment(adminUser);
		SessionSecurity.logout(adminUser);
		
		EffectiveAuthorizationService.rebuildPendingRoleCache();
		
		return is_configured;
	}
	public static DirectoryGroupType getRocketApplicationGroup(long organizationId){
		if(rocketDir != null) return rocketDir;
		try {
			rocketDir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(null, GroupEnumType.DATA,basePath, organizationId);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return rocketDir;
	}
	public static void clearCache(){
		rocketOrganization = null;
	}
	public static OrganizationType getRocketOrganization(){
		if(rocketOrganization != null) return rocketOrganization;
		try{
			rocketOrganization = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).addOrganization("Rocket", OrganizationEnumType.PUBLIC, FactoryDefaults.getAccelerantOrganization());
		}
		catch(FactoryException fe){
			logger.error("Error",fe);
			System.out.println(fe.getMessage());
			rocketOrganization = null;
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return rocketOrganization;
	}
	public static List<DirectoryGroupType> getLifecycleGroups(LifecycleType lc){
		return getApplicationGroups(lc.getGroupId(),lc.getOrganizationId());
	}
	public static List<DirectoryGroupType> getProjectGroups(ProjectType proj){
		return getApplicationGroups(proj.getGroupId(),proj.getOrganizationId());
	}

	private static List<DirectoryGroupType> getApplicationGroups(long parentId,long organizationId){
		List<DirectoryGroupType> subs = new ArrayList<DirectoryGroupType>();
		try {
			DirectoryGroupType parent = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(parentId, organizationId);
			subs = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryGroups(parent);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return subs;
	}
}