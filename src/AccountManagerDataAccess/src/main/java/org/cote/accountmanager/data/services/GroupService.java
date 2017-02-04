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
package org.cote.accountmanager.data.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.GroupParticipationFactory;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.GroupParticipantType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonGroupType;
import org.cote.accountmanager.objects.PersonParticipantType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;

public class GroupService{
	public static final Logger logger = LogManager.getLogger(GroupService.class);
	
	public static boolean getIsUserInGroup(UserGroupType group, UserType user) throws ArgumentException, FactoryException{
		if(group == null){
			logger.error("Group is null");
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(group.getId() < 0) return true;
		return getIsUserInGroup(group, user, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsUserInGroup(BaseGroupType group, UserType user, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		if(group == null){
			logger.error("Group is null");
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(group.getId() < 0) return true;
		return ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getIsUserInGroup(group, user,permission,affect_type);
	}
	
	public static boolean addUserToGroup(UserType user, UserGroupType group) throws ArgumentException, DataAccessException, FactoryException
	{
		return addUserToGroup(user, group, null, AffectEnumType.UNKNOWN);

	}

	public static boolean addUserToGroup(UserType account, UserGroupType group, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, DataAccessException, FactoryException
	{
		/// accommodate bulk inserts with a negative id - skip the check for the getUserInGroup, which will return true for bulk jobs
		///
		if (group.getId() < 0 || getIsUserInGroup(group, account) == false)
		{
			UserParticipantType ap = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newUserGroupParticipation(group, account);
			if (((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).add(ap))
			{
				EffectiveAuthorizationService.pendUserUpdate(account);
				return true;
			}
		}
		return false;
	}
	public static boolean removeUserFromGroup(UserGroupType group, UserType account) throws FactoryException, ArgumentException
	{
		if (((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).deleteUserGroupParticipants(group, account))
		{
			EffectiveAuthorizationService.pendUserUpdate(account);
			return true;
		}
		return false;
	}
	
	
	public static boolean getIsGroupInGroup(BaseGroupType group, BaseGroupType member) throws ArgumentException, FactoryException{
		if(group == null){
			logger.error("Group is null");
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(group.getId() < 0) return true;
		return getIsGroupInGroup(group, member, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsGroupInGroup(BaseGroupType group, BaseGroupType member, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		if(group == null){
			logger.error("Group is null");
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(group.getId() < 0) return true;
		return ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getIsGroupInGroup(group, member,permission,affect_type);
	}
	
	public static boolean addGroupToGroup(BaseGroupType member, BaseGroupType group) throws ArgumentException, DataAccessException, FactoryException
	{
		return addGroupToGroup(member, group, null, AffectEnumType.UNKNOWN);

	}

	public static boolean addGroupToGroup(BaseGroupType member, BaseGroupType group, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, DataAccessException, FactoryException
	{
		/// accommodate bulk inserts with a negative id - skip the check for the getGroupInGroup, which will return true for bulk jobs
		///
		if (group.getId() < 0 || getIsGroupInGroup(group, member) == false)
		{
			GroupParticipantType ap = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newGroupGroupParticipation(group, member);
			if (((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).add(ap))
			{
				EffectiveAuthorizationService.pendGroupUpdate(member);
				return true;
			}
		}
		return false;
	}
	public static boolean removeGroupFromGroup(BaseGroupType group, BaseGroupType member) throws FactoryException, ArgumentException
	{

		if (((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).deleteGroupGroupParticipants(group, member))
		{
			EffectiveAuthorizationService.pendGroupUpdate(member);
			return true;
		}
		return false;
	}
	
	
	public static boolean getIsAccountInGroup(AccountGroupType group, AccountType account) throws ArgumentException, FactoryException{
		if(group == null){
			logger.error("Group is null");
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(group.getId() < 0) return true;
		return getIsAccountInGroup(group, account, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsAccountInGroup(BaseGroupType group, AccountType account, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		if(group == null){
			logger.error("Group is null");
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(group.getId() < 0) return true;
		return ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getIsAccountInGroup(group, account,permission,affect_type);
	}
	
	public static boolean addAccountToGroup(AccountType account, AccountGroupType group) throws ArgumentException, DataAccessException, FactoryException
	{
		return addAccountToGroup(account, group, null, AffectEnumType.UNKNOWN);

	}

	public static boolean addAccountToGroup(AccountType account, AccountGroupType group, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, DataAccessException, FactoryException
	{
		/// accommodate bulk inserts with a negative id - skip the check for the getAccountInGroup, which will return true for bulk jobs
		///
		if (group.getId() < 0 || getIsAccountInGroup(group, account) == false)
		{
			AccountParticipantType ap = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newAccountGroupParticipation(group, account);
			if (((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).add(ap))
			{
				EffectiveAuthorizationService.pendAccountUpdate(account);
				return true;
			}
		}
		return false;
	}
	public static boolean removeAccountFromGroup(AccountGroupType group, AccountType account) throws FactoryException, ArgumentException
	{
		if (((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).deleteAccountGroupParticipants(group, account))
		{
			EffectiveAuthorizationService.pendAccountUpdate(account);
			return true;
		}
		return false;
	}
	public static boolean getIsPersonInGroup(PersonGroupType group, PersonType person) throws ArgumentException, FactoryException{
		if(group == null){
			logger.error("Group is null");
			return false;
		}
		/// accommodate bulk inserts with a negative id; don't check the DB for the negative value
		///
		
		if(group.getId() < 0) return true;
		return getIsPersonInGroup(group, person, null, AffectEnumType.UNKNOWN);
	}
	public static boolean getIsPersonInGroup(BaseGroupType group, PersonType person, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, FactoryException
	{
		if(group == null){
			logger.error("Group is null");
			return false;
		}

		/// accommodate bulk inserts with a negative id
		///
		if(group.getId() < 0) return true;
		return ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).getIsPersonInGroup(group, person,permission,affect_type);
	}
	
	public static boolean switchActorInGroup(NameIdType actor, BaseGroupType group, boolean add) throws ArgumentException, DataAccessException, FactoryException{
		boolean out_bool = false;
		if(GroupEnumType.fromValue(actor.getNameType().toString()) != group.getGroupType()){
			return false;
		}
		switch(actor.getNameType()){
			case PERSON:
				if(add) out_bool = GroupService.addPersonToGroup((PersonType)actor, (PersonGroupType)group);
				else out_bool = GroupService.removePersonFromGroup((PersonGroupType)group,(PersonType)actor);
				break;
			case ACCOUNT:
				if(add) out_bool = GroupService.addAccountToGroup((AccountType)actor, (AccountGroupType)group);
				else out_bool = GroupService.removeAccountFromGroup((AccountGroupType)group,(AccountType)actor);
				break;
			case USER:
				if(add) out_bool = GroupService.addUserToGroup((UserType)actor, (UserGroupType)group);
				else out_bool = GroupService.removeUserFromGroup((UserGroupType)group,(UserType)actor);
				break;
			}

		return out_bool;
	}

	public static boolean addPersonToGroup(PersonType person, PersonGroupType group) throws ArgumentException, DataAccessException, FactoryException
	{
		return addPersonToGroup(person, group, null, AffectEnumType.UNKNOWN);

	}

	public static boolean addPersonToGroup(PersonType account, PersonGroupType group, BasePermissionType permission, AffectEnumType affect_type) throws ArgumentException, DataAccessException, FactoryException
	{
		/// accommodate bulk inserts with a negative id - skip the check for the getPersonInGroup, which will return true for bulk jobs
		///
		if (group.getId() < 0 || getIsPersonInGroup(group, account) == false)
		{
			PersonParticipantType ap = ((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).newPersonGroupParticipation(group, account);
			if (((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).add(ap))
			{
				EffectiveAuthorizationService.pendPersonUpdate(account);
				return true;
			}
		}
		return false;
	}
	public static boolean removePersonFromGroup(PersonGroupType group, PersonType account) throws FactoryException, ArgumentException
	{
		if (((GroupParticipationFactory)Factories.getFactory(FactoryEnumType.GROUPPARTICIPATION)).deletePersonGroupParticipants(group, account))
		{
			EffectiveAuthorizationService.pendPersonUpdate(account);
			return true;
		}
		return false;
	}
}