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
package org.cote.accountmanager.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.data.services.GroupService;
import org.cote.accountmanager.data.services.RoleService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonGroupType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.service.rest.BaseService;




public class GroupServiceImpl  {
	
	public static final Logger logger = Logger.getLogger(GroupServiceImpl.class.getName());
	
	
	public static boolean delete(BaseGroupType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.GROUP, bean, request);
	}
	public static boolean add(BaseGroupType bean, HttpServletRequest request){
		return BaseService.add(AuditEnumType.GROUP, bean, request);
	}
	public static boolean update(BaseGroupType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.GROUP, bean, request);
	}
	public static BaseGroupType readByParent(long orgId, long parentId, String name, String type, HttpServletRequest request){
		OrganizationType org = null;
		BaseGroupType parent = null;
		logger.info("Reading " + name + " in #" + parentId + " in org #" + orgId);
		try{
			org = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(org != null && parentId > 0L) parent = Factories.getGroupFactory().getById(parentId, orgId);
			else logger.error("Organization id #" + orgId + " is null");
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			
			e.printStackTrace();
		}
		
		if(parent == null){
			logger.error("Parent id #" + parentId + " is null in organization #" + orgId);
			return null;
		}
		
		return BaseService.readByNameInParent(AuditEnumType.GROUP, parent, name, type, request);
	}
	public static BaseGroupType readByParent(BaseGroupType parent, String name, String type, HttpServletRequest request){
		BaseGroupType role = BaseService.readByNameInParent(AuditEnumType.GROUP, parent, name, type, request);
		Factories.getAttributeFactory().populateAttributes(role);
		return role;
	}
	public static int countInParent(long orgId, long parentId, HttpServletRequest request){
		OrganizationType org = null;
		BaseGroupType group = null;
		try{
			org = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(org != null) group = Factories.getGroupFactory().getById(parentId, orgId);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(group == null){
			System.out.println("Invalid parentId reference: " + parentId);
			return 0;
		}
		return BaseService.countInParent(AuditEnumType.GROUP, group, request);
	}

	public static List<BaseGroupType> getListInParent(UserType user, String type, BaseGroupType parentGroup, long startRecord, int recordCount){
		///return BaseService.getGroupList(AuditEnumType.ROLE, user, path, startRecord, recordCount);
		

		List<BaseGroupType> out_obj = new ArrayList<BaseGroupType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All groups",AuditEnumType.GROUP,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.GROUP, parentGroup.getUrn());
		
		if(SessionSecurity.isAuthenticated(user) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}

		try {
			///AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(
				AuthorizationService.canView(user, parentGroup)
				||
				RoleService.isFactoryReader(user,Factories.getGroupFactory(),parentGroup.getOrganizationId())
			){
				AuditService.permitResult(audit, "Access authorized to list groups");
				out_obj = getList(type,parentGroup,startRecord,recordCount,parentGroup.getOrganizationId() );
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list roles.");
				return out_obj;
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_obj;
		
	}
	public static BaseGroupType readById(long id,HttpServletRequest request){
		BaseGroupType role = BaseService.readById(AuditEnumType.GROUP, id, request);
		if(role != null) Factories.getAttributeFactory().populateAttributes(role);
		return role;
	}
	public static List<BaseGroupType> listInGroup(UserType user,String type, String path, long startIndex, int recordCount){
		BaseGroupType dir = BaseService.findGroup(user,GroupEnumType.valueOf(type), path);
		return getListInParent(user,type,dir,startIndex,recordCount);
	}
	private static List<BaseGroupType> getList(String type, BaseGroupType parentGroup, long startIndex, int recordCount, long organizationId){
		//BaseGroupType dir = findGroup(groupType, path, request);
		List<BaseGroupType> dirs = new ArrayList<BaseGroupType>();
		GroupEnumType groupType = GroupEnumType.valueOf(type);
		if(parentGroup == null) return dirs;
		try {
			dirs = Factories.getGroupFactory().getListByParent(groupType, parentGroup,  startIndex, recordCount, parentGroup.getOrganizationId());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(BaseService.enableExtendedAttributes){
			for(int i = 0; i < dirs.size(); i++){
				Factories.getAttributeFactory().populateAttributes((NameIdType)dirs.get(i));
			}
		}
		return dirs;
	}
	
	public static boolean setGroup(UserType user, long groupId, AuditEnumType objType, long objId, boolean enable){
		boolean out_bool = false;
		AuditType audit = AuditService.beginAudit(ActionEnumType.MODIFY, "Group " + groupId,objType,"Object #" + objId);
		BaseGroupType group = null;
		NameIdType obj = null;
		try {
			group = Factories.getGroupFactory().getGroupById(groupId, user.getOrganizationId());
			if(group == null){
				AuditService.denyResult(audit, "Group does not exist");
				return out_bool;
			}
			if(GroupEnumType.fromValue(objType.toString()) != group.getGroupType()){
				AuditService.denyResult(audit, "Group type must match the object type");
				return out_bool;
			}
			obj = ((NameIdFactory)BaseService.getFactory(objType)).getById(objId, user.getOrganizationId());

			if(obj == null){
				AuditService.denyResult(audit, "Object does not exist");
				return out_bool;
			}
			if(
				BaseService.canViewType(objType, user,obj)
				&&
				BaseService.canChangeType(AuditEnumType.GROUP, user, group)
			){
				boolean set = false;
				switch(objType){
					case PERSON:
						if(enable) set = GroupService.addPersonToGroup((PersonType)obj, (PersonGroupType)group);
						else set = GroupService.removePersonFromGroup((PersonGroupType)group,(PersonType)obj);
						break;
					case ACCOUNT:
						if(enable) set = GroupService.addAccountToGroup((AccountType)obj, (AccountGroupType)group);
						else set = GroupService.removeAccountFromGroup((AccountGroupType)group,(AccountType)obj);
						break;
					case USER:
						if(enable) set = GroupService.addUserToGroup((UserType)obj, (UserGroupType)group);
						else set = GroupService.removeUserFromGroup((UserGroupType)group,(UserType)obj);
						break;

				}
				if(set){
					EffectiveAuthorizationService.pendUpdate(group);
					EffectiveAuthorizationService.pendUpdate(obj);
					EffectiveAuthorizationService.rebuildPendingRoleCache();
					AuditService.permitResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is authorized to change the group.");
					out_bool = true;
				}
				else{
					AuditService.denyResult(audit, "Unable to change the group");
				}
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") is not authorized to change the group with this object.");
				return out_bool;
			}

		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_bool;
	}
	public static List<AccountType> getListOfAccounts(UserType user, AccountGroupType targGroup){
		return getListOfMembers(user, targGroup, FactoryEnumType.ACCOUNT);
	}
	public static List<PersonType> getListOfPersons(UserType user, PersonGroupType targGroup){
		return getListOfMembers(user, targGroup, FactoryEnumType.PERSON);
	}
	public static List<UserType> getListOfUsers(UserType user, UserGroupType targGroup){
		return getListOfMembers(user, targGroup, FactoryEnumType.USER);
	}
	private static <T> List<T> getListOfMembers(UserType user, BaseGroupType targGroup, FactoryEnumType memberType){
		List<T> out_obj = new ArrayList<T>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "All groups in group",AuditEnumType.GROUP,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.GROUP, targGroup.getUrn());
		
		if(SessionSecurity.isAuthenticated(user) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}
		if(targGroup == null){
			AuditService.denyResult(audit, "Target group is null");
			return null;
		}

		try {
			///AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(
				AuthorizationService.isMapOwner(user, targGroup)
				||
				RoleService.isFactoryAdministrator(user, Factories.getAccountFactory())
				||
				AuthorizationService.canView(user, targGroup)
				||
				RoleService.isFactoryReader(user, Factories.getGroupFactory())
			){
				AuditService.permitResult(audit, "Access authorized to list groups in group");
				switch(memberType){

					case ACCOUNT:
						out_obj = FactoryBase.convertList(Factories.getGroupParticipationFactory().getAccountsInGroup((AccountGroupType)targGroup));
						break;
					case PERSON:
						out_obj = FactoryBase.convertList(Factories.getGroupParticipationFactory().getPersonsInGroup((PersonGroupType)targGroup));
						break;
					case USER:
						out_obj = FactoryBase.convertList(Factories.getGroupParticipationFactory().getUsersInGroup((UserGroupType)targGroup));
						break;
					default:
						break;
				}
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to list groups.");
				return out_obj;
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_obj;
		
	}
	
}
