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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.GroupParticipationFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserPermissionType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.propellant.objects.LifecycleType;
import org.cote.propellant.objects.ProjectType;
import org.cote.rocket.factory.LifecycleFactory;
import org.cote.rocket.factory.ProjectFactory;

public class RocketSecurity {
	public static final Logger logger = LogManager.getLogger(RocketSecurity.class);
	private static String[] containerPermissions = new String[]{};
	private static String[] containerRoles = new String[]{"AdminRole","UserRole","AuditRole","ManagerRole","TesterRole","ArchitectRole","DeveloperRole","AuthorRole","EditorRole"};
	/// "Projects" is intentionally left out because this list appears at both the lifecycle and project level
	///
	private static String[] directoryGroups = new String[]{"Schedules","Goals","Budgets",
		"Estimates","Cases","Data","Times","Costs","Requirements","Methodologies","Processes","ProcessSteps","ValidationRules",
		"Forms","FormTemplates","FormElements","Work","Stages","Artifacts","Dependencies","Tasks","Tickets","Stories","Resources","Notes","Modules","Models","Accounts","Persons",
		"Policies","Facts","Patterns","Functions","FunctionFacts","Operations","Rules","Applications","Contacts","Addresses",
		"Locations","Traits","Events","Populations"
	};
	
	public static String[] getRoleNames(){
		return containerRoles;
	}

	protected static void setupBulkContainerRoles(String sessionId, UserType adminUser, UserRoleType parentRole) throws FactoryException, DataAccessException, ArgumentException{
		for(int i = 0; i < containerRoles.length; i++){
			UserRoleType rt = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).newUserRole(adminUser, containerRoles[i], parentRole);
			BulkFactories.getBulkFactory().createBulkEntry(sessionId,FactoryEnumType.ROLE, rt);
			//((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateUserRole(adminUser, containerRoles[i], parentRole);
		}

		
	}	
	protected static void setupBulkContainerPermissions(String sessionId, UserType adminUser, BasePermissionType parentPermission) throws ArgumentException{
		for(int i = 0; i < containerPermissions.length; i++){
			BasePermissionType rt = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).newPermission(adminUser, containerPermissions[i], PermissionEnumType.USER,parentPermission,parentPermission.getOrganizationId());
			BulkFactories.getBulkFactory().createBulkEntry(sessionId,FactoryEnumType.PERMISSION, rt);
		}
	}
	protected static void setupContainerRoles(UserType adminUser, UserRoleType parentRole) throws FactoryException, DataAccessException, ArgumentException{
		for(int i = 0; i < containerRoles.length; i++){
			UserRoleType rt = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).newUserRole(adminUser, containerRoles[i], parentRole);
			((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).add(rt);

			//((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateUserRole(adminUser, containerRoles[i], parentRole);
		}
	}
	protected static void setupContainerPermissions(UserType adminUser, BasePermissionType parentPermission) throws DataAccessException, FactoryException, ArgumentException{
		for(int i = 0; i < containerPermissions.length; i++){
			BasePermissionType rt = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).newPermission(adminUser, containerPermissions[i], PermissionEnumType.USER,parentPermission,parentPermission.getOrganizationId());
			((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).add(rt);
		}
	}


	protected static <T> void setupRolesToReadContainer(UserType adminUser, UserRoleType parentRole, String[] roles, T bucket) throws FactoryException, DataAccessException, ArgumentException{
		BasePermissionType[] bpt = new BasePermissionType[]{
				AuthorizationService.getViewPermissionForMapType(NameEnumType.GROUP,parentRole.getOrganizationId()),
				/*
				AuthorizationService.getEditGroupPermission(parentRole.getOrganizationId()),
				AuthorizationService.getDeleteGroupPermission(parentRole.getOrganizationId()),
				AuthorizationService.getCreateGroupPermission(parentRole.getOrganizationId())
				*/
				
		};
		for(int i = 0; i < roles.length;i++){
			BaseRoleType role = getRole(roles[i], parentRole, parentRole.getOrganizationId());
			if(role == null) throw new FactoryException("Role '" + roles[i] + "' in parentRole '" + parentRole.getName() + " #(" + parentRole.getId() + ") is null");
			EffectiveAuthorizationService.pendRoleUpdate((UserRoleType)role);
			//AuthorizationService.authorizeRoleType(adminUser, role, bucket, true, false, false, false);
			for(int p = 0; p < bpt.length; p++){
				BaseParticipantType bp = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newRoleGroupParticipation((BaseGroupType)bucket, role, bpt[p], AffectEnumType.GRANT_PERMISSION);
				((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).add(bp);
			}
		}
	}
	protected static <T> void setupRolesToEditContainer(UserType adminUser, UserRoleType parentRole, String[] roles, T bucket) throws FactoryException, DataAccessException, ArgumentException{

		BasePermissionType[] bpt = new BasePermissionType[]{
				AuthorizationService.getViewPermissionForMapType(NameEnumType.GROUP, parentRole.getOrganizationId()),
				AuthorizationService.getEditPermissionForMapType(NameEnumType.GROUP, parentRole.getOrganizationId()),
				AuthorizationService.getDeletePermissionForMapType(NameEnumType.GROUP, parentRole.getOrganizationId()),
				AuthorizationService.getCreatePermissionForMapType(NameEnumType.GROUP,parentRole.getOrganizationId())
		};
		for(int i = 0; i < roles.length;i++){
			BaseRoleType role = getRole(roles[i], parentRole, parentRole.getOrganizationId());
			EffectiveAuthorizationService.pendRoleUpdate((UserRoleType)role);
			//AuthorizationService.authorizeRoleType(adminUser, role, bucket, true, true, true, true);
			for(int p = 0; p < bpt.length; p++){
				BaseParticipantType bp = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newRoleGroupParticipation((BaseGroupType)bucket, role, bpt[p], AffectEnumType.GRANT_PERMISSION);
				((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).add(bp);
			}

		}
	}
	public static DirectoryGroupType getCreateProjectDirectory(UserType user, LifecycleType lc, String name) throws FactoryException,ArgumentException
	{

		DirectoryGroupType dir = (DirectoryGroupType)((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).findGroup(user, GroupEnumType.DATA,Rocket.getBasePath() + "/Lifecycles/" + lc.getName() + "/Projects", user.getOrganizationId());
		if(dir == null) return null;
		DirectoryGroupType pDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, name, dir, user.getOrganizationId());
		return pDir;
	}
	public static DirectoryGroupType getCreateLifecycleDirectory(UserType user, String name) throws FactoryException,ArgumentException
	{
		DirectoryGroupType appOrg = Rocket.getRocketApplicationGroup(user.getOrganizationId());
		if(appOrg == null) throw new FactoryException("Application group is null for organization " + user.getOrganizationId());
		DirectoryGroupType lDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Lifecycles", Rocket.getRocketApplicationGroup(user.getOrganizationId()), user.getOrganizationId());
		DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, name, lDir, user.getOrganizationId());
		return dir;
 	}
	public static DirectoryGroupType getLifecycleDirectory(UserType user, LifecycleType lc, String name){
		DirectoryGroupType dir = null;
		try {
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(name, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lc.getGroupId(),lc.getOrganizationId()), lc.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return null;
	}
	public static DirectoryGroupType getProjectDirectory(UserType user, ProjectType proj, String name){
		DirectoryGroupType dir = null;
		try {
			
			dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryByName(name, ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(),proj.getOrganizationId()), proj.getOrganizationId());
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		}
		return dir;
	}

	public static boolean setupBulkProjectStructure(String sessionId, LifecycleType lc, ProjectType proj, UserType adminUser){
		boolean out_bool = false;
		/*
		 * Project Structure:
		 * Directory Base: [org]/[lc-name]/Projects/[proj-name]
		 */
	
				try{
					if(setupBulkProjectRoles(sessionId,proj, adminUser) == true){
					
						DirectoryGroupType pjDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(),proj.getOrganizationId());
						DirectoryGroupType dir = pjDir;
						UserRoleType bRole = getProjectRoleBucket(proj);
						UserRoleType lRole = getLifecycleRoleBucket(lc);
						UserRoleType rRole = getRocketRoles(lc.getOrganizationId());
						//UserRoleType lcRole = getLifecycle();
						
						setupRolesToReadContainer(adminUser,rRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
						setupRolesToEditContainer(adminUser,rRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);
						
						setupRolesToReadContainer(adminUser,bRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
						setupRolesToEditContainer(adminUser,bRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);
				
						setupRolesToReadContainer(adminUser,lRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","ArchitectRole","DeveloperRole","AuthorRole","EditorRole"},dir);
						setupRolesToEditContainer(adminUser,lRole,new String[]{"AdminRole"},dir);
						
						
						/// Because the bulk operation in this implementation directly manipulates the participation table, it's necessary to tell the authorizaton service that it needs to update the related object
						///
						EffectiveAuthorizationService.pendGroupUpdate(dir);
						
						for(int i = 0; i < directoryGroups.length; i++){
								//dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(adminUser, directoryGroups[i], pjDir, proj.getOrganizationId());
								dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newDirectoryGroup(adminUser, directoryGroups[i], pjDir, proj.getOrganizationId());
								BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUP, dir);
								applyRolesToProjectDirectory(adminUser,rRole, lRole, bRole,dir);
/*
								setupRolesToReadContainer(adminUser,rRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
								setupRolesToEditContainer(adminUser,rRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);
								setupRolesToReadContainer(adminUser,bRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
								setupRolesToEditContainer(adminUser,bRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);
								setupRolesToReadContainer(adminUser,lRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
								setupRolesToEditContainer(adminUser,lRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);
*/
								
						}
					} // end if
				} // end try
				catch(ArgumentException ae){
					logger.error("Error",ae);
				} catch (DataAccessException e) {
					
					logger.error("Error",e);
				} catch (FactoryException e) {
					
					logger.error("Error",e);
				}

				
				out_bool = true;

		return out_bool;
	}
	public static void configureProjectDirectory(UserType adminUser, LifecycleType lc, ProjectType proj, BaseGroupType dir) throws FactoryException, DataAccessException, ArgumentException{
		UserRoleType bRole = RocketSecurity.getProjectRoleBucket(proj);
		UserRoleType lRole = RocketSecurity.getLifecycleRoleBucket(lc);
		UserRoleType rRole = RocketSecurity.getRocketRoles(lc.getOrganizationId());

		RocketSecurity.applyRolesToProjectDirectory(null, rRole, lRole, bRole, dir);
	}
	public static void applyRolesToProjectDirectory(UserType adminUser, UserRoleType rocketRole, UserRoleType lifecycleRole, UserRoleType projectRole, BaseGroupType dir) throws FactoryException, DataAccessException, ArgumentException{
		setupRolesToReadContainer(adminUser,rocketRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
		setupRolesToEditContainer(adminUser,rocketRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);
		setupRolesToReadContainer(adminUser,projectRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
		setupRolesToEditContainer(adminUser,projectRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);
		setupRolesToReadContainer(adminUser,lifecycleRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
		setupRolesToEditContainer(adminUser,lifecycleRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);
		EffectiveAuthorizationService.pendGroupUpdate(dir);
	}
	
	public static boolean setupBulkLifecycleStructure(String sessionId, LifecycleType lc, UserType adminUser) throws DataAccessException, FactoryException, ArgumentException{
		boolean out_bool = false;
		/*
		 * Lifecycle Structure:
		 * Directory Base: [org]/lc-name/
		 */

				if(setupBulkLifecycleRoles(sessionId, lc, adminUser) == true){
					DirectoryGroupType lcDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lc.getGroupId(),lc.getOrganizationId());
					DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newDirectoryGroup(adminUser, "Projects", lcDir, lc.getOrganizationId());
					BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUP, dir);
					UserRoleType rRole = getRocketRoles(lc.getOrganizationId());
					UserRoleType bRole = getLifecycleRoleBucket(lc);

					/// apply Rocket-level roles on lifecycle directory
					setupRolesToReadContainer(adminUser,rRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},lcDir);
					setupRolesToEditContainer(adminUser,rRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},lcDir);

					/// apply Lifecycle-level roles on lifecycle directory
					setupRolesToReadContainer(adminUser,bRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},lcDir);
					setupRolesToEditContainer(adminUser,bRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},lcDir);

					/// Because the bulk operation in this implementation directly manipulates the participation table, it's necessary to tell the authorizaton service that it needs to update the related object
					///
					EffectiveAuthorizationService.pendGroupUpdate(lcDir);
					
					/// apply rocket-level roles on project directory
					setupRolesToReadContainer(adminUser,rRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
					setupRolesToEditContainer(adminUser,rRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);
					
					/// apply lifecycle-level roles on project directory
					setupRolesToReadContainer(adminUser,bRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
					setupRolesToEditContainer(adminUser,bRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);

					EffectiveAuthorizationService.pendGroupUpdate(dir);

					for(int i = 0; i < directoryGroups.length; i++){
							dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).newDirectoryGroup(adminUser, directoryGroups[i], lcDir, lc.getOrganizationId());
							BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.GROUP, dir);
							setupRolesToReadContainer(adminUser,rRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
							setupRolesToEditContainer(adminUser,rRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);
							setupRolesToReadContainer(adminUser,bRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
							setupRolesToEditContainer(adminUser,bRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);
							EffectiveAuthorizationService.pendGroupUpdate(dir);
					}
				} /// end if roles setup

				out_bool = true;

		return out_bool;
	}
	public static boolean setupLifecycleStructure(LifecycleType lc, UserType adminUser){
		boolean out_bool = false;
		/*
		 * Lifecycle Structure:
		 * Directory Base: [org]/lc-name/
		 */
		synchronized(((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION))){
			synchronized(((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION))){
				DataTable dt1 = ((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).getDataTables().get(0);
				DataTable dt2 = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getDataTables().get(0);
				dt1.setBulkInsert(true);
				dt2.setBulkInsert(true);
				try{
					if(setupLifecycleRoles(lc, adminUser) == true){
						DirectoryGroupType lcDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lc.getGroupId(),lc.getOrganizationId());
						DirectoryGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(adminUser, "Projects", lcDir, lc.getOrganizationId());
						UserRoleType bRole = getLifecycleRoleBucket(lc);
						/// setup roles on lifecycle directory
						setupRolesToReadContainer(adminUser,bRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},lcDir);
						setupRolesToEditContainer(adminUser,bRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},lcDir);
						/// setup roles on project directory
						setupRolesToReadContainer(adminUser,bRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
						setupRolesToEditContainer(adminUser,bRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);


						for(int i = 0; i < directoryGroups.length; i++){
								dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(adminUser, directoryGroups[i], lcDir, lc.getOrganizationId());
								setupRolesToReadContainer(adminUser,bRole,new String[]{"UserRole","AuditRole","ManagerRole","TesterRole","AuthorRole","EditorRole"},dir);
								setupRolesToEditContainer(adminUser,bRole,new String[]{"AdminRole","ArchitectRole","DeveloperRole"},dir);
						}
					} /// end if roles setup
				}
				catch(ArgumentException ae){
					logger.error("Error",ae);
				} catch (DataAccessException e) {
					
					logger.error("Error",e);
				} catch (FactoryException e) {
					
					logger.error("Error",e);
				}
				((RoleParticipationFactory)Factories.getFactory(FactoryEnumType.ROLEPARTICIPATION)).writeSpool(dt1.getName());
				((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).writeSpool(dt2.getName());
				dt1.setBulkInsert(false);
				dt2.setBulkInsert(false);
				out_bool = true;
			} /// end sync
		} /// end sync
		return out_bool;
	}
	private static boolean setupBulkProjectRoles(String sessionId,ProjectType proj, UserType adminUser) throws DataAccessException, FactoryException, ArgumentException{
		boolean out_bool = false;
		if(proj == null) throw new ArgumentException("Invalid project");
		//((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(proj.getGroupId());
		((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).denormalize(proj);
		BasePermissionType projectBucketPermission = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).makePath(adminUser, PermissionEnumType.USER, proj.getGroupPath(),proj.getOrganizationId());
				//((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).newPermission(adminUser, "ProjectPermissions-" + proj.getObjectId(), PermissionEnumType.USER,getProjectsPermissionBucket(proj.getOrganizationId()),proj.getOrganizationId());
		//BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERMISSION, projectBucketPermission);

		UserRoleType projectBucketRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).makePath(adminUser, RoleEnumType.USER, proj.getGroupPath(),proj.getOrganizationId());
			//((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).newUserRole(adminUser, "ProjectRoles-" + proj.getObjectId(), getProjectsRoleBucket(proj.getOrganizationId()));
		//BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ROLE, projectBucketRole);
		setupBulkContainerPermissions(sessionId,adminUser,projectBucketPermission);
		setupBulkContainerRoles(sessionId,adminUser, projectBucketRole);
		if(RoleService.addUserToRole(adminUser, getProjectAdminRole(proj)) == false){
			throw new FactoryException("Failed to assign admin user '" + adminUser.getName() + "' to Project Admin Role");
		}
		out_bool = true;
		return out_bool;
	}

	private static boolean setupProjectRoles(ProjectType proj, UserType adminUser) throws DataAccessException, FactoryException, ArgumentException{
		boolean out_bool = false;
		if(proj == null || proj.getId().compareTo(0L)==0) throw new ArgumentException("Invalid project");
		//((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(proj.getGroupId());
		((ProjectFactory)Factories.getFactory(FactoryEnumType.PROJECT)).denormalize(proj);
		BasePermissionType projectBucketPermission = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).makePath(adminUser, PermissionEnumType.USER, proj.getGroupPath(),proj.getOrganizationId());
				//((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getCreatePermission(adminUser, "ProjectPermissions-" + proj.getObjectId(), PermissionEnumType.USER,getProjectsPermissionBucket(proj.getOrganizationId()),proj.getOrganizationId());
		UserRoleType projectBucketRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).makePath(adminUser, RoleEnumType.USER, proj.getGroupPath(),proj.getOrganizationId());
				//((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateUserRole(adminUser, "ProjectRoles-" + proj.getObjectId(), getProjectsRoleBucket(proj.getOrganizationId()));
		setupContainerPermissions(adminUser,projectBucketPermission);
		setupContainerRoles(adminUser, projectBucketRole);
		RoleService.addUserToRole(adminUser, getProjectAdminRole(proj));
		out_bool = true;
		return out_bool;
	}
	private static boolean setupBulkLifecycleRoles(String sessionId, LifecycleType lc, UserType adminUser) throws DataAccessException, FactoryException, ArgumentException{
		boolean out_bool = false;
		if(adminUser == null || adminUser.getId().compareTo(0L)==0) throw new ArgumentException("Invalid admin user");
		if(lc == null || lc.getGroupId().compareTo(0L) == 0 || lc.getOrganizationId().compareTo(0L) == 0) throw new ArgumentException("Invalid lifecycle");
		
		//((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lc.getGroupId(),lc.getOrganizationId()));
		((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).denormalize(lc);
		
		BasePermissionType lifecycleBucketPermission = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).makePath(adminUser, PermissionEnumType.USER, lc.getGroupPath(),lc.getOrganizationId()); 
				//((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).newPermission(adminUser, "LifecyclePermissions-" + lc.getObjectId(), PermissionEnumType.USER,getLifecyclesPermissionBucket(lc.getOrganizationId()),lc.getOrganizationId());
		//BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.PERMISSION, lifecycleBucketPermission);

		UserRoleType lifecycleBucketRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).makePath(adminUser, RoleEnumType.USER, lc.getGroupPath(),lc.getOrganizationId());
				//((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).newUserRole(adminUser, "LifecycleRoles-" + lc.getObjectId(), getLifecyclesRoleBucket(lc.getOrganizationId()));
		//BulkFactories.getBulkFactory().createBulkEntry(sessionId, FactoryEnumType.ROLE, lifecycleBucketRole);
		setupBulkContainerPermissions(sessionId,adminUser,lifecycleBucketPermission);
		setupBulkContainerRoles(sessionId, adminUser, lifecycleBucketRole);
		
		//UserRoleType bRole = getLifecycleRoleBucket(lc);
		//if(bRole == null) throw new ArgumentException("Bucket role is null");
		
		UserRoleType adminRole = getLifecycleAdminRole(lc);
		if(adminRole == null) throw new ArgumentException("Admin role is null for Lifecycle " + lc.getName() + " (#" + lc.getId() + ") in organization " + lc.getOrganizationId());
		if(RoleService.addUserToRole(adminUser, adminRole) == false){
			throw new FactoryException("Failed to assign admin user '" + adminUser.getName() + "' to Lifecycle Admin Role");
		}
		out_bool = true;
		return out_bool;
	}	
	private static boolean setupLifecycleRoles(LifecycleType lc, UserType adminUser) throws DataAccessException, FactoryException, ArgumentException{
		boolean out_bool = false;
		if(adminUser == null || adminUser.getId().compareTo(0L)==0) throw new ArgumentException("Invalid admin user");
		if(lc == null || lc.getId().compareTo(0L)==0 || lc.getGroupId() == null || lc.getOrganizationId().compareTo(0L) == 0) throw new ArgumentException("Invalid lifecycle");
		((LifecycleFactory)Factories.getFactory(FactoryEnumType.LIFECYCLE)).denormalize(lc);
		//BasePermissionType lifecycleBucketPermission = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getCreatePermission(adminUser, "LifecyclePermissions-" + lc.getObjectId(), PermissionEnumType.USER,getLifecyclesPermissionBucket(lc.getOrganizationId()),lc.getOrganizationId());
		//UserRoleType lifecycleBucketRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getCreateUserRole(adminUser, "LifecycleRoles-" + lc.getObjectId(), getLifecyclesRoleBucket(lc.getOrganizationId()));
		BasePermissionType lifecycleBucketPermission = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).makePath(adminUser, PermissionEnumType.USER, lc.getGroupPath(),lc.getOrganizationId());
		UserRoleType lifecycleBucketRole = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).makePath(adminUser, RoleEnumType.USER, lc.getGroupPath(),lc.getOrganizationId());
		setupContainerPermissions(adminUser,lifecycleBucketPermission);
		setupContainerRoles(adminUser, lifecycleBucketRole);
		UserRoleType bRole = getLifecycleRoleBucket(lc);
		if(bRole == null) throw new ArgumentException("Bucket role is null");
		UserRoleType adminRole = getLifecycleAdminRole(lc);
		if(adminRole == null) throw new ArgumentException("Admin role is null for Lifecycle " + lc.getName() + " (#" + lc.getId() + ") in organization " + lc.getOrganizationId());
		RoleService.addUserToRole(adminUser, adminRole);
		out_bool = true;
		return out_bool;
	}
	public static UserRoleType getProjectAdminRole(ProjectType lc){
		return getProjectRoleByName(lc,"AdminRole");
	}
	public static UserRoleType getProjectUserRole(ProjectType lc){
		return getProjectRoleByName(lc,"UserRole");
	}
	public static UserRoleType getProjectAuditRole(ProjectType lc){
		return getProjectRoleByName(lc,"AuditRole");
	}
	public static UserRoleType getProjectManagerRole(ProjectType lc){
		return getProjectRoleByName(lc,"ManagerRole");
	}
	public static UserRoleType getProjectTesterRole(ProjectType lc){
		return getProjectRoleByName(lc,"TesterRole");
	}
	public static UserRoleType getProjectArchitectRole(ProjectType lc){
		return getProjectRoleByName(lc,"ArchitectRole");
	}
	public static UserRoleType getProjectDeveloperRole(ProjectType lc){
		return getProjectRoleByName(lc,"DeveloperRole");
	}
	public static UserRoleType getProjectAuthorRole(ProjectType lc){
		return getProjectRoleByName(lc,"AuthorRole");
	}
	public static UserRoleType getProjectEditorRole(ProjectType lc){
		return getProjectRoleByName(lc,"EditorRole");
	}
	public static UserRoleType getLifecycleAdminRole(LifecycleType lc){
		return getLifecycleRoleByName(lc,"AdminRole");
	}
	public static UserRoleType getLifecycleUserRole(LifecycleType lc){
		return getLifecycleRoleByName(lc,"UserRole");
	}
	public static UserRoleType getLifecycleAuditRole(LifecycleType lc){
		return getLifecycleRoleByName(lc,"AuditRole");
	}
	public static UserRoleType getLifecycleManagerRole(LifecycleType lc){
		return getLifecycleRoleByName(lc,"ManagerRole");
	}
	public static UserRoleType getLifecycleTesterRole(LifecycleType lc){
		return getLifecycleRoleByName(lc,"TesterRole");
	}
	public static UserRoleType getLifecycleArchitectRole(LifecycleType lc){
		return getLifecycleRoleByName(lc,"ArchitectRole");
	}
	public static UserRoleType getLifecycleDeveloperRole(LifecycleType lc){
		return getLifecycleRoleByName(lc,"DeveloperRole");
	}
	public static UserRoleType getLifecycleAuthorRole(LifecycleType lc){
		return getLifecycleRoleByName(lc,"AuthorRole");
	}
	public static UserRoleType getLifecycleEditorRole(LifecycleType lc){
		return getLifecycleRoleByName(lc,"EditorRole");
	}
	public static UserRoleType getLifecycleRoleBucket(LifecycleType lc){
		return getRoleByGroup(null,lc.getGroupId(),lc.getOrganizationId());
	}
	public static UserPermissionType getLifecyclePermissionBucket(LifecycleType lc){
		return getPermissionByGroup(null,lc.getGroupId(),lc.getOrganizationId());
	}
	public static UserRoleType getProjectRoleBucket(ProjectType proj){
		//return getRole("ProjectRoles-" + proj.getObjectId(),getProjectsRoleBucket(proj.getOrganizationId()), proj.getOrganizationId());
		return getRoleByGroup(null,proj.getGroupId(),proj.getOrganizationId());
	}
	public static UserPermissionType getProjectPermissionBucket(ProjectType proj){
		return getPermissionByGroup(null,proj.getGroupId(),proj.getOrganizationId());
		//return ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).findPermission(PermissionEnumType.USER, pathBase, organizationId)
		//return getPermission("ProjectPermissions-" + proj.getObjectId(),getProjectsPermissionBucket(proj.getOrganizationId()), proj.getOrganizationId());
	}
	public static UserRoleType getLifecycleRoleByName(LifecycleType lc, String name){
		return getRoleByGroup(name,lc.getGroupId(),lc.getOrganizationId());
	}
	public static UserRoleType getProjectRoleByName(ProjectType proj, String name){
		return getRoleByGroup(name,proj.getGroupId(),proj.getOrganizationId());
	}
	public static UserPermissionType getLifecyclePermissionByName(LifecycleType lc, String name){
		return getPermissionByGroup(name,lc.getGroupId(),lc.getOrganizationId());
	}
	public static UserPermissionType getProjectPermissionByName(ProjectType proj, String name){
		return getPermissionByGroup(name,proj.getGroupId(),proj.getOrganizationId());
	}
	/*
	private static UserRoleType getProjectsRoleBucket(long organizationId){
		return getRole("ProjectRoles", getRocketRoles(organizationId), organizationId);
	}
	private static UserPermissionType getProjectsPermissionBucket(long organizationId){
		return getPermission("ProjectPermissions", getRocketPermissions(organizationId), organizationId);
	}
	*/
	private static UserRoleType getLifecyclesRoleBucket(long organizationId){
		return getRoleByGroup("Lifecycles", Rocket.getRocketApplicationGroup(organizationId).getId(), organizationId);
	}
	
	private static UserPermissionType getLifecyclesPermissionBucket(long organizationId){
		return getPermissionByGroup("Lifecycles", Rocket.getRocketApplicationGroup(organizationId).getId(), organizationId);
	}
	
	public static UserRoleType getAdminRole(long organizationId){
		return getRoleByGroup("AdminRole", Rocket.getRocketApplicationGroup(organizationId).getId(), organizationId);
	}
	public static UserRoleType getUserRole(long organizationId){
		return getRoleByGroup("UserRole", Rocket.getRocketApplicationGroup(organizationId).getId(), organizationId);
	}
	public static UserRoleType getAuditRole(long organizationId){
		return getRoleByGroup("AuditRole", Rocket.getRocketApplicationGroup(organizationId).getId(), organizationId);
	}
	
	public static UserRoleType getRocketRoles(long organizationId){
		return getRoleByGroup(null, Rocket.getRocketApplicationGroup(organizationId).getId(), organizationId);
	}
	
	public static UserPermissionType getRocketPermissions(long organizationId){
		return getPermissionByGroup(null, Rocket.getRocketApplicationGroup(organizationId).getId(), organizationId);
	}
	private static UserRoleType getRoleByGroup(String name, long parentId, long organizationId){
		UserRoleType role = null;
		try {
			DirectoryGroupType parent = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(parentId, organizationId);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(parent);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(parent);
			role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).findRole(RoleEnumType.USER,  parent.getPath() + (name != null ? "/" + name : ""), organizationId);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
		return role;
	}
	private static UserRoleType getRole(String name, BaseRoleType parent, long organizationId){
		UserRoleType role = null;
		try {
			
			role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleByName(name,parent,RoleEnumType.USER,  organizationId);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return role;
	}
	private static UserPermissionType getPermissionByGroup(String name, long parentId, long organizationId){
		UserPermissionType permission = null;
		try {
			DirectoryGroupType parent = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(parentId, organizationId);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(parent);
			((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(parent);
			permission = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).findPermission(PermissionEnumType.USER, parent.getPath() + (name != null ? "/" + name : ""), organizationId);
			//permission = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionByName(name, PermissionEnumType.USER,parent,organizationId);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (DataAccessException e) {
			
			logger.error("Error",e);
		}
		return permission;
	}
	private static UserPermissionType getPermission(String name, BasePermissionType parent, long organizationId){
		UserPermissionType permission = null;
		try {
			
			permission = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionByName(name,PermissionEnumType.USER, parent, organizationId);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		}
		return permission;
	}
	public static boolean canCreateProject(UserType user,LifecycleType lc){
		boolean out_bool = false;
		try {
			out_bool = AuthorizationService.canChange(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lc.getGroupId(), lc.getOrganizationId()));
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		return out_bool;
	}	
	public static boolean canCreateLifecycle(UserType user){
		boolean out_bool = false;
		try {
			out_bool = AuthorizationService.canChange(user,Rocket.getLifecycleGroup(user.getOrganizationId()));
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		return out_bool;
	}
	public static boolean canChangeLifecycle(UserType user,LifecycleType lc){
		boolean out_bool = false;
		try {
			out_bool = AuthorizationService.canChange(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lc.getGroupId(), lc.getOrganizationId()));
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		return out_bool;
	}
	public static boolean canReadLifecycle(UserType user,LifecycleType lc){
		boolean out_bool = false;
		try {
			out_bool = AuthorizationService.canView(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(lc.getGroupId(), lc.getOrganizationId()));
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		return out_bool;
	}
	public static boolean canChangeProject(UserType user, ProjectType proj){
		boolean out_bool = false;
		try {
			out_bool = AuthorizationService.canChange(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(), proj.getOrganizationId()));
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		return out_bool;
	}
	public static boolean canReadProject(UserType user, ProjectType proj){
		boolean out_bool = false;
		try {
			out_bool = AuthorizationService.canView(user,((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getDirectoryById(proj.getGroupId(),proj.getOrganizationId()));
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
		} catch (FactoryException e) {
			
			logger.error("Error",e);
		}
		return out_bool;
	}

}
