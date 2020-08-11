package org.cote.accountmanager.client.test;
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


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.client.util.AM6Util;
import org.cote.accountmanager.client.util.AuthenticationUtil;
import org.cote.accountmanager.client.util.CacheUtil;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ApiClientConfigurationType;
import org.cote.accountmanager.objects.AuthenticationResponseType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.StreamUtil;
import org.junit.After;
import org.junit.Before;
public class BaseClientTest{
	public static final Logger logger = LogManager.getLogger(BaseClientTest.class);
	private static String testUserName = null;
	private static String testUserOrganization = null;
	private static String testUserPassword = null;
	protected static UserType testUser = null;
	protected static ClientContext defaultContext = new ClientContext();
	protected static ClientContext testUserContext = null;
	private static String sessionId = null;
	protected static String serviceUrl = null;
	protected static String serviceName = null;
	private static Properties testProperties = null;
	

	@Before
	public void setUp() throws Exception {
		
		File cacheDir = new File("./cache");
		if(cacheDir.exists() == false) cacheDir.mkdirs();
		
		if(testProperties == null){
			testProperties = new Properties();
		
			try {
				InputStream fis = ClassLoader.getSystemResourceAsStream("./resource.properties"); 
				testProperties.load(fis);
				fis.close();
			} catch (IOException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
				return;
			}
		}
		
		
		
		testUserOrganization = testProperties.getProperty("test.user1.organization");
		testUserName = testProperties.getProperty("test.user1.name");
		testUserPassword = testProperties.getProperty("test.user1.password");
		testUserContext = new ClientContext();
		serviceUrl = testProperties.getProperty("service.url");
		serviceName = testProperties.getProperty("service.name");
		
		ApiClientConfigurationType api = AuthenticationUtil.getApiConfiguration(serviceUrl);
		CacheUtil.cache(defaultContext, serviceName, api);
		
		AuthenticationResponseType art = AuthenticationUtil.authenticate(testUserContext, serviceName, testUserOrganization, testUserName, testUserPassword);
		if(art != null) {
			testUser = art.getUser();
			assertNotNull("Test user is null",testUser);
			//logger.info("TestUser = " + (testUser == null ? " null " : " not null "));
		}
	}
	
	@After
	public void tearDown() throws Exception{

		/// SessionSecurity.logout(sessionId, testOrganization.getId());
	}
	
	protected DirectoryGroupType getCreateDirectory(ClientContext context, DirectoryGroupType parent, String name) {

		if(parent == null) {
			parent = AM6Util.findObject(context, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", "~");
		}
		assertNotNull("Parent directory is null",parent);
		//String testPath = AM6Util.getEncodedPath("~/TestData");

		logger.info("Attempt to find dir");
		DirectoryGroupType subDirectory = AM6Util.getObjectByName(context, DirectoryGroupType.class, NameEnumType.GROUP, parent.getObjectId(), name, false);
				//AM6Util.findObject(context, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", testPath);
		
		if(subDirectory == null) {
			subDirectory = new DirectoryGroupType();
			subDirectory.setNameType(NameEnumType.GROUP);
			subDirectory.setGroupType(GroupEnumType.DATA);
			subDirectory.setName(name);
			subDirectory.setParentId(parent.getId());
			logger.info("Attempt to add dir");
			assertTrue("Failed to add directory",AM6Util.updateObject(context, Boolean.class, subDirectory));
			
			subDirectory = AM6Util.getObjectByName(context, DirectoryGroupType.class, NameEnumType.GROUP, parent.getObjectId(), name, false);
			//subDirectory = AM6Util.findObject(context, DirectoryGroupType.class, NameEnumType.GROUP, "DATA", testPath);
		}
		return subDirectory;
	}

	public String getTestScript(String fileName) {
		String data = null;
		try {
			BufferedInputStream fis = new BufferedInputStream(ClassLoader.getSystemResourceAsStream("./" + fileName)); 
			data = StreamUtil.streamToString(fis);
		} catch (IOException e) {
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return data;
	}
	
}