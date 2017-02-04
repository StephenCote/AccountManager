package org.cote.accountmanager.data.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.UserType;

public interface ICommunityProvider {
	public boolean configureCommunity(UserType adminUser);
	public boolean configureEntitlements(UserType adminUser, String communityId, String projectId, String groupId);
	public boolean enrollReaderInCommunities(UserType adminUser, String userId);
	public boolean enrollAdminInCommunities(UserType adminUser, String userId);
	public boolean enrollReaderInCommunityProject(UserType adminUser, String userId, String communityId, String projectId);
	public boolean enrollReaderInCommunity(UserType adminUser, String userId, String communityId);
	public boolean deleteCommunityProject(UserType adminUser, String projectId);
	public boolean deleteCommunity(UserType adminUser, String communityId);
	public boolean createCommunity(UserType adminUser, String communityName);
	public boolean createCommunityProject(UserType adminUser, String communityId, String projectName);
	public List<BaseRoleType> getCommunitiesRoles(UserType user);
	public List<BaseRoleType> getCommunityRoles(UserType user, String communityId);
	public List<BaseRoleType> getCommunityProjectRoles(UserType user, String projectId);
	public <T> T getCommunity(UserType user, String name);
	public <T> T getCommunityProject(UserType user, String communityName, String projectName);
	public BasePermissionType getCommunityProjectPermissionBase(UserType user, String projectId);
	public BaseRoleType getCommunityProjectRoleBase(UserType user, String projectId);
	
}
