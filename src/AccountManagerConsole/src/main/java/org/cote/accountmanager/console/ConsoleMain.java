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
package org.cote.accountmanager.console;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.accountmanager.util.StreamUtil;


public class ConsoleMain {
	public static final Logger logger = Logger.getLogger(ConsoleMain.class.getName());
	
	/// This bit is provided to reset passwords without authenticating
	/// This is to recover admin passwords lost to key rotations or, in this case
	/// sweeping credential changes such as with the AM5.1 CredentialType system
	///
	public static boolean enableUnauthenticatedResets = true;
	
	public static void main(String[] args){
		
		Properties props = new Properties();
		try {
			InputStream fis = ClassLoader.getSystemResourceAsStream("resource.properties"); 
					//new FileInputStream("./resource.properties");
			
			props.load(fis);
			fis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		PropertyConfigurator.configure(getLogProps());
		logger.info("AccountManagerConsole");
		
		Options options = new Options();
		options.addOption("organization",true,"AccountManager Organization Path");
		options.addOption("username", true, "AccountManager user name");
		options.addOption("password",true,"AccountManager password");
		options.addOption("importData",true,"Local path or file");
		options.addOption("pointer",false,"Load data objects as filesystem pointers");
		options.addOption("tag",false,"Apply the supplied tags");
		options.addOption("file",true,"File reference");
		options.addOption("batchSize",true,"Maximum data batch size");
		options.addOption("patch",false,"Patch the current system");
		options.addOption("reset",false,"Bit indicating a reset operation");
		options.addOption("name",true,"Variable name");
		options.addOption("addUser",false,"Add a new user");
		options.addOption("addOrganization",false,"Add a new organization");
		options.addOption("deleteOrganization",false,"Add a new organization");
		options.addOption("migrateData",false,"Migrate data from a pre-configured target");
		options.addOption("ownerId",true,"Migrate data from a pre-configured target");
		
		options.addOption("setup",false,"Setup Account Manager");
		options.addOption("email",true,"Email address");
		options.addOption("confirm",false,"Confirm the activity");
		options.addOption("schema",true,"Account Manager Database Schema");
		options.addOption("rootPassword",true,"Account Manager Root Password");
		options.addOption("adminPassword",true,"Account Manager Admin Password");
		
		// options.addOption("importProject",true,"Local path or file");
		// options.addOption("projectName",true,"Name of the imported project");
		// options.addOption("lifecycleName",true,"Name of the lifecycle to which the project belongs");
		options.addOption("path",true,"AccountManager directory group");
		// options.addOption("test",false,"Run Tests");
		CommandLineParser parser = new PosixParser();
		try {
			
			setupConnectionFactory(props);
			
			CommandLine cmd = parser.parse( options, args);
			if(cmd.hasOption("patch") && cmd.hasOption("organization")){
				try {
					OrganizationType org = Factories.getOrganizationFactory().findOrganization(cmd.getOptionValue("organization"));
					if(org != null){
						logger.info("Patching " + org.getName());
						SecurityBean asymmKey = KeyService.getPrimaryAsymmetricKey(org);
						if(asymmKey == null){
							logger.info("Creating primary asymmetric key");
							KeyService.newOrganizationAsymmetricKey(org, true);
						}
						else{
							logger.info("Checked asymmetric key");
						}
						SecurityBean symmKey = KeyService.getPrimarySymmetricKey(org);
						if(symmKey == null){
							logger.info("Creating primary symmetric key");
							KeyService.newOrganizationSymmetricKey(org, true);
						}
						else{
							logger.info("Checked symmetric key");
						}
						List<UserType> users = Factories.getUserFactory().getUserList(0, 0, org);
						logger.info("Checking " + users.size() + " users for valid credentials");
						for(int i = 0; i < users.size();i++){
							CredentialType cred = CredentialService.getPrimaryCredential(users.get(i));
							if(cred == null){
								if(users.get(i).getName().equals(Factories.getDocumentControlName())){
									logger.info("Resetting Document Control credential");
									CredentialService.newHashedPasswordCredential(users.get(i), users.get(i), UUID.randomUUID().toString(), true);
								}
								else{
									logger.warn("Missing primary credential for " + users.get(i).getName() + " (#" + users.get(i).getId() + ")");
								}
							}
						}

					}
					else{
						logger.error("Organization does not exist");
					}
				} catch (FactoryException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
					e.printStackTrace();
				} catch (ArgumentException e) {
					// TODO Auto-generated catch block
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}
			else if(cmd.hasOption("setup") && cmd.hasOption("rootPassword") && cmd.hasOption("schema")){
				if(cmd.hasOption("confirm") == false){
					logger.warn("Setting up Account Manager will completely replace the Account Manager schema.  Any data will be lost.  If you are sure, add the -confirm parameter and try again.");
				}
				else{
					
					if(SetupAction.setupAccountManager(cmd.getOptionValue("rootPassword"),cmd.getOptionValue("schema"))){
						logger.info("Configured Account Manager");
					}
					else{
						logger.error("Failed to configure Account Manager");
					}
				}
			}
			else if(cmd.hasOption("deleteOrganization") && cmd.hasOption("organization") && cmd.hasOption("name") && cmd.hasOption("adminPassword")){
				OrganizationCommand.deleteOrganization(cmd.getOptionValue("organization"), cmd.getOptionValue("name"), cmd.getOptionValue("adminPassword"),enableUnauthenticatedResets);
			}
			else if(cmd.hasOption("addOrganization") && cmd.hasOption("password") && cmd.hasOption("organization") && cmd.hasOption("adminPassword") && cmd.hasOption("name")){
				OrganizationCommand.addOrganization(cmd.getOptionValue("organization"), cmd.getOptionValue("name"), cmd.getOptionValue("adminPassword"), cmd.getOptionValue("password"),enableUnauthenticatedResets);
			}
			else if(cmd.hasOption("addUser") && cmd.hasOption("password") && cmd.hasOption("organization") && cmd.hasOption("adminPassword") && cmd.hasOption("name")){
				UserCommand.addUser(cmd.getOptionValue("organization"), cmd.getOptionValue("name"), cmd.getOptionValue("adminPassword"), cmd.getOptionValue("password"), cmd.getOptionValue("email"));
			}
			else if(cmd.hasOption("organization") && cmd.hasOption("username") && cmd.hasOption("password")){
				
				try{
					OrganizationType org = Factories.getOrganizationFactory().findOrganization(cmd.getOptionValue("organization"));
					if(org != null){
						String password = cmd.getOptionValue("password");
						if(cmd.hasOption("reset")){
							if(enableUnauthenticatedResets == false){
								logger.info("Unauthenticated password reset capability is disabled");
							}
							else{
								UserType user = Factories.getUserFactory().getUserByName(cmd.getOptionValue("username"),org);
								if(user != null){
									if(password != null && password.length() > 5){
										logger.info("Creating new primary credential");
										CredentialType cred = CredentialService.newHashedPasswordCredential(user, user, password, true);
									}
									else{
										logger.warn("Invalid password");
									}
								}
								else{
									logger.warn("User does not exist");
								}
							}
						}
						else{
							UserType user = SessionSecurity.login(cmd.getOptionValue("username"),CredentialEnumType.HASHED_PASSWORD, password, org);
							if(user != null){
								processAction(user,cmd);
								SessionSecurity.logout(user);
							}
							else{
								logger.error("Invalid credentials for specified organization");
							}
						}
					}
					else{
						logger.error("Organization was not found");
					}
				}
				catch(FactoryException fe){
					fe.printStackTrace();
				} catch (ArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			else{
				logger.info("Syntax");
				logger.info("Setup: -setup -rootPassword password -schema ../AM4_PG9_Schema.txt");
				
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Factories.getAuditFactory().flushSpool();
		
	}
	/*
	public static void runTests(UserType user){
		ConnectionFactory cf = ConnectionFactory.getInstance();
		try{
			DirectoryGroupType tDir = Factories.getGroupFactory().getCreateDirectory(user, "Tasks",user.getHomeDirectory(), user.getOrganization());
			long start = System.currentTimeMillis();
			String random = UUID.randomUUID().toString();
			for(int i = 0; i < 100;i++){
				try {
					TaskType t = Factories.getTaskFactory().newTask(user, tDir);
					t.setName(random + "-" + i);
					Factories.getTaskFactory().addTask(t);
				} catch (ArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			long stop = System.currentTimeMillis();
			logger.info("Connection speed: " + (stop - start) + "ms");
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	*/
	public static void setupConnectionFactory(Properties props){
		ConnectionFactory cf = ConnectionFactory.getInstance();
		cf.setConnectionType(CONNECTION_TYPE.SINGLE);
		cf.setDriverClassName(props.getProperty("db.driver"));
		cf.setUserName(props.getProperty("db.user"));
		cf.setUserPassword(props.getProperty("db.password"));
		cf.setUrl(props.getProperty("db.url"));
		  try
		    {
		        Class.forName("org.postgresql.Driver");
		    }
		    catch(Throwable e)
		    {
		        e.printStackTrace();
		    }
	}
	public static void processAction(UserType user, CommandLine cmd){
		if(cmd.hasOption("importData") && cmd.hasOption("path")){
			if(cmd.hasOption("batchSize")) DataAction.setMaximumLoad(Integer.parseInt(cmd.getOptionValue("batchSize")));
			DataAction.importDataPath(user, cmd.getOptionValue("importData"), cmd.getOptionValue("path"), cmd.hasOption("pointer"));
		}
		if(cmd.hasOption("migrateData") && cmd.hasOption("ownerId")){
			DataAction.migrateData(user,Long.parseLong(cmd.getOptionValue("ownerId")));
		}
		if(cmd.hasOption("tag") && cmd.hasOption("file")){
			DataAction.tagData(user, cmd.getOptionValue("file"));
		}
		/*
		if(cmd.hasOption("importProject") && cmd.hasOption("projectName") && cmd.hasOption("lifecycleName")){
			logger.error("Project import being moved.  Refer to RocketConsole for console implementation");
			/// ProjectAction.importProjectFile(user, cmd.getOptionValue("lifecycleName"), cmd.getOptionValue("projectName"), cmd.getOptionValue("importProject"));
		}
		if(cmd.hasOption("test")){
			runTests(user);
		}
		*/
	}
	public static Properties getLogProps(){
		Properties logProps = new Properties();
		try {
			logProps.load(ClassLoader.getSystemResourceAsStream("logging.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return logProps;
	}
}
