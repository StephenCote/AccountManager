package org.cote.accountmanager.data.services;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
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
}