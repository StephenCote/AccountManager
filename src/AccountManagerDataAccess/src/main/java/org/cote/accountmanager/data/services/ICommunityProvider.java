package org.cote.accountmanager.data.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public interface ICommunityProvider {
	public boolean configureCommunity(UserType adminUser);
	public boolean configureEntitlements(UserType adminUser, String communityId, String projectId, String groupId);
	public boolean enrollReaderInCommunities(UserType adminUser, String userId);
	public boolean enrollAdminInCommunities(UserType adminUser, String userId);
	public boolean enrollAdminInCommunity(UserType adminUser, String communityId,String userId);
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
	
	public boolean importLocationTraits(UserType user, AuditEnumType type, String objectId, String locationPath, String fileName);
	public boolean importLocationCountryInfo(UserType user, AuditEnumType type, String objectId, String locationPath, String fileName);
	public boolean importLocationAdmin1Codes(UserType user, AuditEnumType type, String objectId, String locationPath, String fileName);
	public boolean importLocationAdmin2Codes(UserType user, AuditEnumType type, String objectId, String locationPath, String fileName);
	public boolean importLocationCountryData(UserType user, AuditEnumType type, String objectId, String locationPath, String codes, String alternate);
	
	public boolean generateCommunityProjectRegion(UserType user, String communityId, String projectId, int locationSize, int seedSize, String dictionaryPath, String namesPath);
	public boolean evolveCommunityProjectRegion(UserType user, String communityId, String projectId, int epochSize, int epochEvolutions, String dictionaryPath, String namesPath);
	public String reportCommunityProjectRegion(UserType user, String communityId, String projectId, String dictionaryPath, String namesPath);
}
