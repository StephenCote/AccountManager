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
package org.cote.rocket.console;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.rocket.Factories;
import org.cote.rocket.Rocket;
import org.cote.rocket.RocketModel;
import org.cote.rocket.RocketSecurity;
import org.cote.accountmanager.data.factory.*;
import org.cote.rocket.factory.*;
public class RocketAction {
public static final Logger logger = LogManager.getLogger(RocketAction.class);
	public static void listRoles(){
		logger.info("Roles:");
		String[] roles = RocketSecurity.getRoleNames();
		for(int i = 0; i < roles.length;i++){
			logger.info("\t" + roles[i]);
		}
	}
	public static void addAgileSprints(UserType user, String lifecycleName, String projectName, String startDate, int sprintLength, int sprints, String resourceList, String sprintLabel){
		try {
			String[] resources = new String[0];
			if(resourceList != null) resources = resourceList.split(",");
			LifecycleType lc = Rocket.getLifecycle(lifecycleName, user.getOrganizationId());
			if(lc == null){
				logger.error("Lifecycle '" + lifecycleName + "' doesn't exists");
				return;
			}
			
			if(projectName != null){
				ProjectType pj = Rocket.getProject(projectName, lc, lc.getOrganizationId());
				if(pj == null){
					logger.error("Project '" + projectName + "' doesn't exist in lifecycle '" + lifecycleName + "'");
					return;
				}
	
				RocketModel.emitIterations(user, pj, resources, startDate, sprintLabel, sprints, sprintLength);
				logger.info("Added Agile Artifacts to Project " + pj.getName());
			}
			else{
				RocketModel.addAgileArtifacts(user, lc);
				logger.info("Added Agile Artifacts to Lifecycle " + lc.getName());
			}
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}		
	}
	public static void addAgileMethodology(UserType user, String lifecycleName, String projectName){
		try {
			LifecycleType lc = Rocket.getLifecycle(lifecycleName, user.getOrganizationId());
			if(lc == null){
				logger.error("Lifecycle '" + lifecycleName + "' doesn't exists");
				return;
			}
			
			if(projectName != null){
				ProjectType pj = Rocket.getProject(projectName, lc, lc.getOrganizationId());
				if(pj == null){
					logger.error("Project '" + projectName + "' doesn't exist in lifecycle '" + lifecycleName + "'");
					return;
				}
				RocketModel.addDefaults(user, pj.getGroupId());
				RocketModel.addAgileArtifacts(user, pj);
				logger.info("Added Agile Artifacts to Project " + pj.getName());
			}
			else{
				RocketModel.addDefaults(user, lc.getGroupId());
				RocketModel.addAgileArtifacts(user, lc);
				logger.info("Added Agile Artifacts to Lifecycle " + lc.getName());
			}
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
	}
	public static void addWaterfallMethodology(UserType user, String lifecycleName, String projectName){
		try {
			LifecycleType lc = Rocket.getLifecycle(lifecycleName, user.getOrganizationId());
			if(lc == null){
				logger.error("Lifecycle '" + lifecycleName + "' doesn't exists");
				return;
			}
			
			if(projectName != null){
				ProjectType pj = Rocket.getProject(projectName, lc, lc.getOrganizationId());
				if(pj == null){
					logger.error("Project '" + projectName + "' doesn't exist in lifecycle '" + lifecycleName + "'");
					return;
				}
				RocketModel.addDefaults(user, pj.getGroupId());
				RocketModel.addWaterfallArtifacts(user, pj);
				logger.info("Added Waterfall Artifacts to Project " + pj.getName());
			}
			else{
				RocketModel.addDefaults(user, lc.getGroupId());
				RocketModel.addWaterfallArtifacts(user, lc);
				logger.info("Added Waterfall Artifacts to Lifecycle " + lc.getName());
			}
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
	}
	public static void testProjectRole(UserType user, String lifecycleName, String projectName, String roleName){
		try {
			/*
			EffectiveAuthorizationService.rebuildGroupRoleCache(user.getOrganizationId());
			
			EffectiveAuthorizationService.u(user.getOrganizationId());
			
			EffectiveAuthorizationService.rebuildRoleRoleCache(user.getOrganizationId());
			*/
			LifecycleType lc = Rocket.getLifecycle(lifecycleName, user.getOrganizationId());
			if(lc == null){
				logger.error("Lifecycle '" + lifecycleName + "' doesn't exists");
				return;
			}
			
			ProjectType pj = Rocket.getProject(projectName, lc, lc.getOrganizationId());
			if(pj == null){
				logger.error("Project '" + projectName + "' doesn't exist in lifecycle '" + lifecycleName + "'");
				return;
			}
			
			UserRoleType projectRole = RocketSecurity.getProjectRoleByName(pj, roleName);
			if(projectRole == null){
				logger.error("Project level role '" + roleName + "' does not exist");
				return;
			}
			
			UserRoleType lifecycleRole = RocketSecurity.getLifecycleRoleByName(lc, roleName);
			if(lifecycleRole == null){
				logger.error("Lifecycle level role '" + roleName + "' does not exist");
				return;
			}
			boolean inLRole = RoleService.getIsUserInRole(lifecycleRole, user);
			boolean inEffLRole = RoleService.getIsUserInEffectiveRole(lifecycleRole, user);

			boolean inRole = RoleService.getIsUserInRole(projectRole, user);
			boolean inEffRole = RoleService.getIsUserInEffectiveRole(projectRole, user);

			logger.info(user.getName() + " in role " + roleName + " at lifecycle level = " + inLRole);
			logger.info(user.getName() + " in effective role " + roleName + " at lifecycle level = " + inEffLRole);

			
			logger.info(user.getName() + " in role " + roleName + " at project level = " + inRole);
			logger.info(user.getName() + " in effective role " + roleName + " at project level = " + inEffRole);
			
			if((inLRole || inEffLRole) && !(inRole || inEffRole)){
				logger.warn("Role hierarchy in RocketSecurity needs to be refactored so Project roles roll up into lifecycle roles, and be effective if a user is in the lifecycle role. At the moment, both the lifecycle and project roles are granted permission at the project level");
			}
			
			if(!inLRole && !inEffLRole && !inRole && !inEffRole){
				logger.warn("User not in LC role #" + lifecycleRole.getId() + " or proj role #" + projectRole.getId());
				return;
			}
			DirectoryGroupType dir = RocketSecurity.getProjectDirectory(user, pj, "Tasks");
			if(dir == null){
				logger.error("Failed to find 'Tasks' directory in project");
				return;
			}
			
			logger.info("View Project Group 'Tasks' = " + AuthorizationService.canView(user, dir));
			logger.info("Edit Project Group 'Tasks' = " + AuthorizationService.canChange(user, dir));
			logger.info("Create Project Group 'Tasks' = " + AuthorizationService.canCreate(user, dir));
			logger.info("Delete Project Group 'Tasks' = " + AuthorizationService.canDelete(user, dir));

			

		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
	}
	public static void disenrollFromLifecycle(UserType user, String lifecycleName){
		try {
			LifecycleType lc = Rocket.getLifecycle(lifecycleName, user.getOrganizationId());
			if(lc == null){
				logger.error("Lifecycle '" + lifecycleName + "' doesn't exists");
				return;
			}
			String[] roles = RocketSecurity.getRoleNames();
			for(int i = 0; i < roles.length;i++){
				UserRoleType userRole = RocketSecurity.getLifecycleRoleByName(lc, roles[i]);
				RoleService.removeUserFromRole(userRole,user);
			}
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			EffectiveAuthorizationService.clearCache();

		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
	}
	
	public static void disenrollFromProject(UserType user, String lifecycleName, String projectName){
		try {
			LifecycleType lc = Rocket.getLifecycle(lifecycleName, user.getOrganizationId());
			if(lc == null){
				logger.error("Lifecycle '" + lifecycleName + "' doesn't exists");
				return;
			}
			
			ProjectType pj = Rocket.getProject(projectName, lc, lc.getOrganizationId());
			if(pj == null){
				logger.error("Project '" + projectName + "' doesn't exist in lifecycle '" + lifecycleName + "'");
				return;
			}
			String[] roles = RocketSecurity.getRoleNames();
			for(int i = 0; i < roles.length;i++){
				UserRoleType userRole = RocketSecurity.getProjectRoleByName(pj, roles[i]);
				RoleService.removeUserFromRole(userRole,user);
			}
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			EffectiveAuthorizationService.clearCache();

		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
	}
	public static void enrollInLifecycle(UserType user, String lifecycleName, String roleName){
		try {
			LifecycleType lc = Rocket.getLifecycle(lifecycleName, user.getOrganizationId());
			if(lc == null){
				logger.error("Lifecycle '" + lifecycleName + "' doesn't exists");
				return;
			}
			
			
			disenrollFromLifecycle(user,lifecycleName);
			
			addBaseRoles(user);
			
			UserRoleType lcRole = null;
			if(roleName!=null) lcRole = RocketSecurity.getLifecycleRoleByName(lc,roleName);
			else{
				lcRole = RocketSecurity.getLifecycleUserRole(lc);
				if(lcRole != null) roleName = lcRole.getName();
			}
			if(lcRole == null){
				logger.error("Invalid role name '" + roleName + "'");
				return;
			}

			if(Rocket.enrollInCommunityLifecycle(user, lc, lcRole, AuthorizationService.getViewPermissionForMapType(NameEnumType.GROUP,lc.getOrganizationId()))){
				logger.info("Enrolled " + user.getName() + " as " + roleName + " in " + lc.getName());
			}
			else{
				logger.error("Failed to enroll " + user.getName() + " as " + roleName + " in " + lc.getName());
			}
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			//EffectiveAuthorizationService.clearCache();

		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
	}
	public static void enrollInProject(UserType user, String lifecycleName, String projectName, String roleName){
		try {
			LifecycleType lc = Rocket.getLifecycle(lifecycleName, user.getOrganizationId());
			if(lc == null){
				logger.error("Lifecycle '" + lifecycleName + "' doesn't exists");
				return;
			}
			
			ProjectType pj = Rocket.getProject(projectName, lc, lc.getOrganizationId());
			if(pj == null){
				logger.error("Project '" + projectName + "' doesn't exist in lifecycle '" + lifecycleName + "'");
				return;
			}

			disenrollFromProject(user,lifecycleName,projectName);
			
			addBaseRoles(user);
			
			UserRoleType lcRole = null;
			if(roleName!=null) lcRole = RocketSecurity.getProjectRoleByName(pj,roleName);
			else{
				lcRole = RocketSecurity.getProjectUserRole(pj);
				if(lcRole != null) roleName = lcRole.getName();
			}
			if(lcRole == null){
				logger.error("Invalid role name '" + roleName + "'");
				return;
			}

			if(Rocket.enrollInCommunityProject(user, lc, pj, lcRole, AuthorizationService.getViewPermissionForMapType(NameEnumType.GROUP,lc.getOrganizationId()))){
				logger.info("Enrolled " + user.getName() + " as " + roleName + " in " + lc.getName() + " project " + projectName);
			}
			else{
				logger.error("Failed to enroll " + user.getName() + " as " + roleName + " in " + lc.getName() + " project " + projectName);
			}
			EffectiveAuthorizationService.rebuildPendingRoleCache();
			//EffectiveAuthorizationService.clearCache();

		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
	}
	private static void addBaseRoles(UserType user){
		/// Make sure user is in base required roles
		/// TODO: rocketUserRole should be added to userReader and roleReader roles
		///
		try{
			UserRoleType rocketUserRole = RocketSecurity.getUserRole(user.getOrganizationId());
			UserRoleType userReaderRole = RoleService.getAccountUsersReaderUserRole(user.getOrganizationId());
			UserRoleType roleReaderRole = RoleService.getRoleReaderUserRole(user.getOrganizationId());
			RoleService.addUserToRole(user, rocketUserRole);
			RoleService.addUserToRole(user, userReaderRole);
			RoleService.addUserToRole(user, roleReaderRole);
		}
		catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
	}
	public static void listLifecycles(UserType user){
		try {
			List<DirectoryGroupType> dc = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryGroups(Rocket.getLifecycleGroup(user.getOrganizationId()));
			//List<LifecycleType> lc = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getLifecycleListByGroup(Rocket.getLifecycleGroup(user.getOrganizationId()),0,0,user.getOrganizationId());
			logger.info("Lifecycles:");
			for(int i = 0; i < dc.size();i++){
				logger.info("\t" + dc.get(i).getName());
			}
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
	}
	
	public static void addLifecycle(UserType user, String name){
		try {
			LifecycleType lc = Rocket.getLifecycle(name, user.getOrganizationId());
			if(lc != null){
				logger.error("Lifecycle '" + name + "' already exists");
				return;
			}
			lc = Rocket.createLifecycle(user, name);
			if(lc != null) logger.info("Created lifecycle " + name);
			else logger.error("Failed to create lifecycle " + name);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
	}
	
	public static void listProjects(UserType user, String lname){
		try {
			LifecycleType lc = Rocket.getLifecycle(lname, user.getOrganizationId());
			if(lc == null){
				logger.error("Invalid lifecycle '" + lname + '"');
				return;
			}
			DirectoryGroupType pgd = Rocket.getProjectGroup(lc);
			
			//List<LifecycleType> lc = ((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).getLifecycleListByGroup(Rocket.getLifecycleGroup(user.getOrganizationId()),0,0,user.getOrganizationId());
			logger.info("Projects:");
			if(pgd == null) return;
			List<DirectoryGroupType> dc = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryGroups(pgd);
			for(int i = 0; i < dc.size();i++){
				logger.info("\t" + dc.get(i).getName());
			}
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
	}
	
	public static void addProject(UserType user, String lname, String pname){
		try {
			LifecycleType lc = Rocket.getLifecycle(lname, user.getOrganizationId());
			if(lc == null){
				logger.error("Invalid lifecycle '" + lname + '"');
				return;
			}
			((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).populate(lc);
			ProjectType pt = Rocket.getProject(pname,lc, lc.getOrganizationId());
			if(pt != null){
				logger.error("Project " + pname + " already exists in lifecycle " + lname);
				return;
			}
			pt = Rocket.createProject(user, lc, pname);
			if(pt != null) logger.info("Created project " + pname);
			else logger.error("Failed to create project " + pname);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
	}
	
}
