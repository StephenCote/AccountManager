/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.console;

import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.security.ApiClientConfigurationBean;
import org.cote.accountmanager.data.security.ApiConnectionConfigurationService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.FileUtil;
import org.cote.accountmanager.util.JSONUtil;

public class ApiConfigAction {
	public static final Logger logger = LogManager.getLogger(ApiConfigAction.class);
	public static void configureApi(String orgPath, String adminPassword, String apiFile, String identity, String credential){
		try{
		OrganizationType org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(orgPath);
			if(org == null){
				logger.error("Null organization");
				return;
			}
			UserType adminUser = SessionSecurity.login("Admin", CredentialEnumType.HASHED_PASSWORD,adminPassword, org.getId());
			if(adminUser == null){
				logger.error("Null admin");
				return;
			}
			String file = FileUtil.getFileAsString(apiFile);
			if(file == null || file.length() == 0){
				logger.error("Null file");
				return;
			}
			
			ApiClientConfigurationBean chkConfig = JSONUtil.importObject(file,ApiClientConfigurationBean.class);
			if(chkConfig == null){
				logger.error("Failed to import file");
				return;
			}
			
			byte[] idb = identity.getBytes("UTF-8");
			byte[] credb = credential.getBytes("UTF-8");
			
			ApiClientConfigurationBean apiConfig = ApiConnectionConfigurationService.getApiClientConfiguration(chkConfig.getServiceType(), chkConfig.getName(), org.getId());
			if(apiConfig == null){
				apiConfig = ApiConnectionConfigurationService.addApiClientConfiguration(chkConfig.getServiceType(), chkConfig.getName(), chkConfig.getServiceUrl(), idb, credb, chkConfig.getAttributes(),org.getId());
				if(apiConfig != null){
					logger.error("Added " + chkConfig.getName() + " configuration");
				}
				else{
					logger.error("Failed to add " + chkConfig.getName() + " configuration");
				}
			}
			else{
				DataType chkData = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getByUrn(apiConfig.getDataUrn());
				if(chkData == null){
					logger.error("Config data is null");
				}
				else{
					logger.warn("Update not implemented");
				}
			}
			
			SessionSecurity.logout(adminUser);
		}
		catch(FactoryException e){
			logger.error(e.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (UnsupportedEncodingException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}

	}

}
