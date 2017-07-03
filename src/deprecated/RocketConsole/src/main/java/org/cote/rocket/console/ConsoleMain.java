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
package org.cote.rocket.console;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.propellant.objects.IdentityDataEnumType;
import org.cote.propellant.objects.IdentityDataImportType;
import org.cote.rocket.Factories;
import org.cote.rocket.Rocket;
import org.cote.rocket.services.IdentityService;
import org.cote.accountmanager.data.factory.*;
import org.cote.rocket.factory.*;
public class ConsoleMain {
	public static final Logger logger = LogManager.getLogger(ConsoleMain.class);
	
	public static void main(String[] args){
		
		Properties props = new Properties();
		try {
			InputStream fis = ClassLoader.getSystemResourceAsStream("resource.properties"); 
					//new FileInputStream("./resource.properties");
			
			props.load(fis);
			fis.close();
		} catch (IOException e) {
			
			logger.error("Error",e);
			return;
		}
		
		logger.info("RocketConsole");
		Factories.prepare();
		Options options = new Options();
		options.addOption("organization",true,"AccountManager Organization Path");
		options.addOption("username", true, "AccountManager user name");
		options.addOption("password",true,"AccountManager password");
		options.addOption("importAccountData",true,"Local path or file");
		options.addOption("importPermissionData",true,"Local path or file");
		options.addOption("importMapData",true,"Local path or file");
		
		options.addOption("geo",false,"Import geonames data");
		options.addOption("basePath",true,"Base path for geonames data");
		options.addOption("clean",false,"Clean import destination before importing");
		options.addOption("countryInfo",true,"Import country info data");
		options.addOption("admin1",true,"Import admin1 codes data");
		options.addOption("admin2",true,"Import admin2 codes data");
		options.addOption("features",true,"Import feature data");
		options.addOption("alternate",true,"Include alternate data (inc. postal code) - use with countries import");
		options.addOption("countries",true,"Import country data");
		
		options.addOption("name",true,"Variable name");
		options.addOption("addUser",false,"Add a new user");
		//options.addOption("addOrganization",false,"Add a new user");
		
		options.addOption("listRoles",false,"List default rocket roles");
		options.addOption("list",false,"Variant list command");
		options.addOption("show",false,"Variant show command");
		
		options.addOption("setup",false,"Setup Rocket");
		options.addOption("email",true,"Email address");
		options.addOption("confirm",false,"Confirm the activity");
		options.addOption("schema",true,"Rocket Database Schema");
		options.addOption("rootPassword",true,"Account Manager Root Password");
		options.addOption("adminPassword",true,"Account Managesr Admin Password");
		
		options.addOption("importData",true,"Local path or file");
		options.addOption("importProject",true,"Local path or file");
		options.addOption("projectName",true,"Name of the project");
		options.addOption("applicationName",true,"Name of the application");
		options.addOption("lifecycleName",true,"Name of the lifecycle to which the project belongs");
		options.addOption("disenroll",false,"Remove the user from a lifecycle or project");
		options.addOption("enroll",false,"Enroll the user into a lifecycle or project");
		options.addOption("role",true,"Name of a rocket role");
		options.addOption("addLifecycle",false,"Add a new lifecycle");
		options.addOption("addProject",false,"Add a new project");
		options.addOption("addAgileMethod",false,"Add Agile artifacts.  Required before emitting sprints");
		options.addOption("addSprints",false,"Adds sprints to the project");
		options.addOption("sprintLabel",true,"Name to prefix sprints");
		options.addOption("sprintStart",true,"Date string when the sprint should start");
		options.addOption("sprintLength",true,"Number of weeks per sprint");
		options.addOption("sprints",true,"Number of sprints");
		//options.addOption("community",false,"Configure for community/multi-user");
		options.addOption("path",true,"AccountManager directory group");
		options.addOption("resources",true,"Comma separate list of resources");
		options.addOption("test",false,"Test a particular combination");
		options.addOption("console",false,"Start console mode");
		CommandLineParser parser = new PosixParser();
		try {
			
			setupConnectionFactory(props);
			
			logger.debug("Warming up factories");
			long startWarmUp = System.currentTimeMillis();
			Factories.warmUp();
			long stopWarmUp = System.currentTimeMillis();
			logger.debug("Completed warm up in " + (stopWarmUp - startWarmUp) + "ms");

			
			CommandLine cmd = parser.parse( options, args);
			
			OrganizationType org = null;
			try{
				if(cmd.hasOption("organization")) org = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).findOrganization(cmd.getOptionValue("organization"));
				else org = Rocket.getRocketOrganization();
			}
			catch(ArgumentException e){
				logger.error(e.getMessage());
				return;
			} catch (FactoryException e) {
				
				logger.error(e.getMessage());
				return;
			}
			/// && cmd.hasOption("rootPassword")
			if(cmd.hasOption("setup") && cmd.hasOption("adminPassword") && cmd.hasOption("schema")){
				if(cmd.hasOption("confirm") == false){
					logger.warn("Setting up Rocket will completely replace the Rocket schema.  Any data will be lost.  If you are sure, add the -confirm parameter and try again.");
				}
				else{
					
					if(SetupAction.setupRocket(cmd.getOptionValue("adminPassword"),cmd.getOptionValue("schema"))){
						logger.info("Configured Rocket");
					}
					else{
						logger.error("Failed to configure Rocket");
					}
				}
			}
			/*
			else if(cmd.hasOption("addOrganization") && cmd.hasOption("password") && cmd.hasOption("organization") && cmd.hasOption("adminPassword") && cmd.hasOption("name")){
				OrganizationCommand.addOrganization(cmd.getOptionValue("organization"), cmd.getOptionValue("name"), cmd.getOptionValue("adminPassword"), cmd.getOptionValue("password"));
			}
			
			else if(cmd.hasOption("addUser") && cmd.hasOption("password") && cmd.hasOption("organization") && cmd.hasOption("adminPassword") && cmd.hasOption("name")){
				UserCommand.addUser(cmd.getOptionValue("organization"), cmd.getOptionValue("name"), cmd.getOptionValue("adminPassword"), cmd.getOptionValue("password"), cmd.getOptionValue("email"));
			}
			*/
			else if(cmd.hasOption("username") && cmd.hasOption("password")){
				
				try{
					if(org != null){
						String password_hash = cmd.getOptionValue("password");
						UserType user = SessionSecurity.login(cmd.getOptionValue("username"), CredentialEnumType.HASHED_PASSWORD,password_hash, org.getId());
						if(user != null){
							processAction(user,cmd);
							SessionSecurity.logout(user);
						}
						else{
							logger.error("Invalid credentials for specified organization");
						}
					}
					else{
						logger.error("Organization was not found");
					}
				}
				catch(FactoryException fe){
					logger.error("Error",fe);
				} catch (ArgumentException e) {
					
					logger.error("Error",e);
				}
				
			}
			else{
				logger.info("Syntax");
				logger.info("Setup: -setup -rootPassword password -schema ../AM4_PG9_Schema.txt");
				
			}
		} catch (ParseException e) {
			
			logger.error("Error",e);
		}

		Factories.getAuditFactory().flushSpool();
		
	}
	/*
	public static void runTests(UserType user){
		ConnectionFactory cf = ConnectionFactory.getInstance();
		try{
			DirectoryGroupType tDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getCreateDirectory(user, "Tasks",user.getHomeDirectory(), user.getOrganization());
			long start = System.currentTimeMillis();
			String random = UUID.randomUUID().toString();
			for(int i = 0; i < 100;i++){
				try {
					TaskType t = ((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).newTask(user, tDir);
					t.setName(random + "-" + i);
					((TaskFactory)Factories.getFactory(FactoryEnumType.TASK)).addTask(t);
				} catch (ArgumentException e) {
					
					logger.error("Error",e);
				}
				
			}
			long stop = System.currentTimeMillis();
			logger.info("Connection speed: " + (stop - start) + "ms");
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error("Error",fe);
		} catch (ArgumentException e) {
			
			logger.error("Error",e);
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
		        logger.error("Error",e);
		    }
	}
	public static void processAction(UserType user, CommandLine cmd){
		if(cmd.hasOption("geo") && cmd.hasOption("path")){
			GeoAction.processGeoAction(user, cmd);
			
		}
		if(cmd.hasOption("listRoles")){
			RocketAction.listRoles();
		}
		if(cmd.hasOption("lifecycleName") && cmd.hasOption("projectName") == false &&  cmd.hasOption("disenroll")){
			RocketAction.disenrollFromLifecycle(user, cmd.getOptionValue("lifecycleName"));
		}

		if(cmd.hasOption("lifecycleName") && cmd.hasOption("projectName") == false &&  cmd.hasOption("enroll")){
			RocketAction.enrollInLifecycle(user, cmd.getOptionValue("lifecycleName"),cmd.getOptionValue("role"));
		}
		if(cmd.hasOption("lifecycleName") && cmd.hasOption("projectName")  &&  cmd.hasOption("disenroll")){
			RocketAction.disenrollFromProject(user, cmd.getOptionValue("lifecycleName"),cmd.getOptionValue("projectName"));
		}

		if(cmd.hasOption("lifecycleName") && cmd.hasOption("projectName")  &&  cmd.hasOption("enroll")){
			RocketAction.enrollInProject(user, cmd.getOptionValue("lifecycleName"),cmd.getOptionValue("projectName"),cmd.getOptionValue("role"));
		}
		if(cmd.hasOption("lifecycleName") && cmd.hasOption("projectName")  &&  cmd.hasOption("role") && cmd.hasOption("test")){
			RocketAction.testProjectRole(user, cmd.getOptionValue("lifecycleName"),cmd.getOptionValue("projectName"),cmd.getOptionValue("role"));
		}

		if(cmd.hasOption("lifecycleName") == false && cmd.hasOption("list")){
			RocketAction.listLifecycles(user);
		}
		if(cmd.hasOption("lifecycleName") && cmd.hasOption("addLifecycle")){
			RocketAction.addLifecycle(user, cmd.getOptionValue("lifecycleName"));
		}
		if(cmd.hasOption("lifecycleName") && cmd.hasOption("projectName") == false && cmd.hasOption("list")){
			RocketAction.listProjects(user,cmd.getOptionValue("lifecycleName"));
		}
		if(cmd.hasOption("lifecycleName") && cmd.hasOption("projectName") && cmd.hasOption("addProject")){
			RocketAction.addProject(user, cmd.getOptionValue("lifecycleName"),cmd.getOptionValue("projectName"));
		}
		
		if(cmd.hasOption("lifecycleName") && cmd.hasOption("projectName") && cmd.hasOption("addAgileMethod")){
			RocketAction.addAgileMethodology(user,cmd.getOptionValue("lifecycleName"),cmd.getOptionValue("projectName"));
		}
		if(cmd.hasOption("lifecycleName") && cmd.hasOption("projectName") && cmd.hasOption("addSprints") && cmd.hasOption("sprintLabel") && cmd.hasOption("sprintStart") && cmd.hasOption("sprintLength") && cmd.hasOption("sprints")){
			RocketAction.addAgileSprints(user,cmd.getOptionValue("lifecycleName"),cmd.getOptionValue("projectName"),cmd.getOptionValue("sprintStart"),Integer.parseInt(cmd.getOptionValue("sprintLength")),Integer.parseInt(cmd.getOptionValue("sprints")),cmd.getOptionValue("resources"),cmd.getOptionValue("sprintLabel"));
		}
		if(cmd.hasOption("lifecycleName") && cmd.hasOption("projectName") && cmd.hasOption("applicationName") && (cmd.hasOption("importAccountData") || cmd.hasOption("importPermissionData") || cmd.hasOption("importMapData"))){
			List<IdentityDataImportType> imports = new ArrayList<IdentityDataImportType>();
			if(cmd.hasOption("importAccountData")) imports.add(IdentityService.newIdentityDataImport(cmd.getOptionValue("importAccountData"),IdentityDataEnumType.ACCOUNT));
			if(cmd.hasOption("importPermissionData")) imports.add(IdentityService.newIdentityDataImport(cmd.getOptionValue("importPermissionData"),IdentityDataEnumType.PERMISSION));
			if(cmd.hasOption("importMapData")) imports.add(IdentityService.newIdentityDataImport(cmd.getOptionValue("importMapData"),IdentityDataEnumType.MAP));
			ImportAction.importData(user, cmd.getOptionValue("lifecycleName"),cmd.getOptionValue("projectName"), cmd.getOptionValue("applicationName"), imports.toArray(new IdentityDataImportType[0]));
		}
		if(cmd.hasOption("console")){
			ConsoleAction ca = new ConsoleAction();
			ca.runConsole(user);
		}
		
		/*
		if(cmd.hasOption("importData") && cmd.hasOption("path")){
			DataAction.importDataPath(user, cmd.getOptionValue("importData"), cmd.getOptionValue("path"));
		}
		
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
			
			logger.error("Error",e);
		}
		return logProps;
	}
}
