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

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.AccountGroupType;
import org.cote.accountmanager.objects.AccountParticipantType;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.PersonGroupType;
import org.cote.accountmanager.objects.PersonParticipantType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;

public class GroupService{
	public static final Logger logger = Logger.getLogger(GroupService.class.getName());
	
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
		return Factories.getGroupParticipationFactory().getIsUserInGroup(group, user,permission,affect_type);
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
			UserParticipantType ap = Factories.getGroupParticipationFactory().newUserGroupParticipation(group, account);
			if (Factories.getGroupParticipationFactory().addParticipant(ap))
			{
				EffectiveAuthorizationService.pendUserUpdate(account);
				return true;
			}
		}
		return false;
	}
	public static boolean removeUserFromGroup(UserGroupType group, UserType account) throws FactoryException, ArgumentException
	{
		if (Factories.getGroupParticipationFactory().deleteUserGroupParticipants(group, account))
		{
			EffectiveAuthorizationService.pendUserUpdate(account);
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
		return Factories.getGroupParticipationFactory().getIsAccountInGroup(group, account,permission,affect_type);
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
			AccountParticipantType ap = Factories.getGroupParticipationFactory().newAccountGroupParticipation(group, account);
			if (Factories.getGroupParticipationFactory().addParticipant(ap))
			{
				EffectiveAuthorizationService.pendAccountUpdate(account);
				return true;
			}
		}
		return false;
	}
	public static boolean removeAccountFromGroup(AccountGroupType group, AccountType account) throws FactoryException, ArgumentException
	{
		if (Factories.getGroupParticipationFactory().deleteAccountGroupParticipants(group, account))
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
		return Factories.getGroupParticipationFactory().getIsPersonInGroup(group, person,permission,affect_type);
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
			PersonParticipantType ap = Factories.getGroupParticipationFactory().newPersonGroupParticipation(group, account);
			if (Factories.getGroupParticipationFactory().addParticipant(ap))
			{
				EffectiveAuthorizationService.pendPersonUpdate(account);
				return true;
			}
		}
		return false;
	}
	public static boolean removePersonFromGroup(PersonGroupType group, PersonType account) throws FactoryException, ArgumentException
	{
		if (Factories.getGroupParticipationFactory().deletePersonGroupParticipants(group, account))
		{
			EffectiveAuthorizationService.pendPersonUpdate(account);
			return true;
		}
		return false;
	}
}