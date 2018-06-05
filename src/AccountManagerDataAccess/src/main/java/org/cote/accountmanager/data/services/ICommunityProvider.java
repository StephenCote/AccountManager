/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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

import java.util.List;

import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public interface ICommunityProvider {
	public boolean isCommunityConfigured(long organizationId);
	public boolean addProjectArtifacts(UserType user, AuditEnumType auditType, String objectId);
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
	public <T> boolean saveCommunityProject(T project,UserType user);
	public void deepPopulate(NameIdType object, UserType user);
	public boolean importLocationTraits(UserType user, AuditEnumType type, String objectId, String locationPath, String fileName);
	public boolean importLocationCountryInfo(UserType user, AuditEnumType type, String objectId, String locationPath, String fileName);
	public boolean importLocationAdmin1Codes(UserType user, AuditEnumType type, String objectId, String locationPath, String fileName);
	public boolean importLocationAdmin2Codes(UserType user, AuditEnumType type, String objectId, String locationPath, String fileName);
	public boolean importLocationCountryData(UserType user, AuditEnumType type, String objectId, String locationPath, String codes, String alternate);
	
	public boolean generateCommunityProjectApplication(UserType user, String communityId, String projectId, String appName, boolean usePermissions, boolean useGroups, int seed, int max, double distribution, String dictionaryPath, String namesPath);
	public boolean generateCommunityProjectRegion(UserType user, String communityId, String projectId, int locationSize, int seedSize, String dictionaryPath, String namesPath);
	public boolean evolveCommunityProjectRegion(UserType user, String communityId, String projectId, int epochSize, int epochEvolutions, String dictionaryPath, String namesPath);
	public String reportCommunityProjectRegion(UserType user, String communityId, String projectId, String dictionaryPath, String namesPath);

	public Object executeCommunityProjectScript(UserType user, String communityId, String projectId, String name);

	public String getCommunityProjectScript(UserType user, String communityId, String projectId, String name);
	public boolean updateCommunityProjectScript(UserType user, String communityId, String projectId, String name, String dataStr);
	public boolean isRandomizeSeedPopulation();
	public void setRandomizeSeedPopulation(boolean randomizeSeedPopulation);
	public boolean isOrganizePersonManagement();

	public void setOrganizePersonManagement(boolean organizePersonManagement);

}
