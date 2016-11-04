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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.security.CredentialService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.CredentialEnumType;
import org.cote.accountmanager.objects.CredentialType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.NameEnumType;


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
			
			logger.error(e.getStackTrace());
			return;
		}
		
		PropertyConfigurator.configure(getLogProps());
		logger.info("AccountManagerConsole");
		logger.info("\tNote: Slow startup time due in part to cryptographic libraries being loaded and initialized");
		
		Options options = new Options();
		options.addOption("generate",false,"Generate DAL classes and schema for a particular type");
		options.addOption("type",true,"Type of factory to generate");
		options.addOption("organization",true,"AccountManager Organization Path");
		options.addOption("username", true, "AccountManager user name");
		options.addOption("password",true,"AccountManager password");
		options.addOption("identity", true, "Identity information");
		options.addOption("credential", true, "Credential information");
		options.addOption("importData",true,"Local path or file");
		options.addOption("pointer",false,"Load data objects as filesystem pointers");
		options.addOption("tag",false,"Apply the supplied tags");
		options.addOption("configureApi",false,"Apply the API Configuration");
		options.addOption("file",true,"File reference");
		options.addOption("batchSize",true,"Maximum data batch size");
		options.addOption("patch",false,"Patch the current system");
		options.addOption("reset",false,"Bit indicating a reset operation");
		options.addOption("name",true,"Variable name");
		options.addOption("addUser",false,"Add a new user");
		options.addOption("addOrganization",false,"Add a new organization");
		options.addOption("deleteOrganization",false,"Delete a new organization");
		options.addOption("migrateData",false,"Migrate data from a pre-configured target");
		options.addOption("ownerId",true,"Migrate data from a pre-configured target");
		options.addOption("execute",false,"Execute an action");
		options.addOption("setup",false,"Setup Account Manager");
		options.addOption("email",true,"Email address");
		options.addOption("confirm",false,"Confirm the activity");
		options.addOption("schema",true,"Account Manager Database Schema");
		options.addOption("rootPassword",true,"Account Manager Root Password");
		options.addOption("adminPassword",true,"Account Manager Admin Password");
		
		
		options.addOption("openssl",false,"Perform openssl activities");
		options.addOption("dn",true,"DN of the openssl request");
		options.addOption("expiry",true,"Number of days until the certificate expires");
		options.addOption("cn",true,"CN of the openssl request (used in lieu of dn)");
		options.addOption("root",false,"Create a self-signed root certificate");
		options.addOption("request",false,"Request a CSR");
		options.addOption("sign",false,"Sign a CSR");
		options.addOption("signer",true,"CA used to sign or validate a certificate or request");
		options.addOption("signerPassword",true,"Password of the CA");
		options.addOption("export",false,"Export keys as pkcs12");
		options.addOption("store",true,"Name of a store");
		options.addOption("storePassword",true,"Password of the store");
		options.addOption("setCertificate",false,"Set certificate to an object");
		options.addOption("issueCertificate",false,"Create a certificate for a user in a given organization, and sign with that organization's certificate");
		options.addOption("testCertificate",false,"Test certificate set to an object");
		options.addOption("trust",false,"Bit indicating the store is a trust store");
		options.addOption("private",false,"Bit indicating certificate containing the private key (a PKCS12 file) should be imported");
		
		//String storeName, char[] storePassword, boolean isTrust, String alias, char[] password, boolean isPrivate
		
		
		// options.addOption("importProject",true,"Local path or file");
		// options.addOption("projectName",true,"Name of the imported project");
		// options.addOption("lifecycleName",true,"Name of the lifecycle to which the project belongs");
		options.addOption("path",true,"AccountManager directory group");
		// options.addOption("test",false,"Run Tests");
		
		
			
		CommandLineParser parser = new PosixParser();
		try {
			logger.debug("Setting up connection factory");
			setupConnectionFactory(props);
			logger.debug("Warming up factories");
			long startWarmUp = System.currentTimeMillis();
			Factories.warmUp();
			long stopWarmUp = System.currentTimeMillis();
			logger.debug("Completed warm up in " + (stopWarmUp - startWarmUp) + "ms");

			CommandLine cmd = parser.parse( options, args);
			if(cmd.hasOption("patch") && cmd.hasOption("organization")){
				logger.debug("Applying patch ...");
				try {
					OrganizationType org = Factories.getOrganizationFactory().findOrganization(cmd.getOptionValue("organization"));
					logger.info("Patching " + org.getName());
					logger.info("Updating permissions ...");
					if(org != null){
						FactoryDefaults.createPermissionsForAuthorizationFactories(org.getId());

						/*
						
						SecurityBean asymmKey = KeyService.getPrimaryAsymmetricKey(org.getId());
						if(asymmKey == null){
							logger.info("Creating primary asymmetric key");
							KeyService.newOrganizationAsymmetricKey(org.getId(), true);
						}
						else{
							logger.info("Checked asymmetric key");
						}
						SecurityBean symmKey = KeyService.getPrimarySymmetricKey(org.getId());
						if(symmKey == null){
							logger.info("Creating primary symmetric key");
							KeyService.newOrganizationSymmetricKey(org.getId(), true);
						}
						else{
							logger.info("Checked symmetric key");
						}
						List<UserType> users = Factories.getUserFactory().getUserList(0, 0, org.getId());
						logger.info("Checking " + users.size() + " users for valid credentials");
						for(int i = 0; i < users.size();i++){
							CredentialType cred = CredentialService.getPrimaryCredential(users.get(i));
							if(cred == null){
								if(users.get(i).getName().equals(Factories.getDocumentControlName())){
									logger.info("Resetting Document Control credential");
									CredentialService.newHashedPasswordCredential(users.get(i), users.get(i), UUID.randomUUID().toString(), true,false);
								}
								else{
									logger.warn("Missing primary credential for " + users.get(i).getName() + " (#" + users.get(i).getId() + ")");
								}
							}
						}
						*/
					}
					else{
						logger.error("Organization does not exist");
					}
				} catch (FactoryException e) {
					
					logger.error(e.getMessage());
					logger.error(e.getStackTrace());
				} catch (ArgumentException e) {
					
					logger.error(e.getMessage());
					logger.error(e.getStackTrace());
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
			else if(cmd.hasOption("configureApi") && cmd.hasOption("organization") && cmd.hasOption("file") && cmd.hasOption("identity")&& cmd.hasOption("credential")  && cmd.hasOption("adminPassword")){
				//logger.info("Configure API");
				ApiConfigAction.configureApi(cmd.getOptionValue("organization"),cmd.getOptionValue("adminPassword"),cmd.getOptionValue("file"),cmd.getOptionValue("identity"),cmd.getOptionValue("credential"));
			}
			else if (cmd.hasOption("openssl")){
				String sslBinary = props.getProperty("ssl.binary");
				String localPath = props.getProperty("ssl.ca.path");
				OpenSSLAction sslAction = new OpenSSLAction(sslBinary, localPath);
				if(cmd.hasOption("root") && cmd.hasOption("name") && cmd.hasOption("password") && cmd.hasOption("expiry")){
					//String dn = (cmd.hasOption("dn") ? cmd.getOptionValue("dn") || OpenSSLUtil.)
					sslAction.generateRootCertificate(cmd.getOptionValue("name"),cmd.getOptionValue("dn"),cmd.getOptionValue("password").toCharArray(),Integer.parseInt(cmd.getOptionValue("expiry")));
				}

				if(cmd.hasOption("request") && cmd.hasOption("name") && cmd.hasOption("password") && cmd.hasOption("expiry")){
					sslAction.generateCertificateRequest(cmd.getOptionValue("name"),cmd.getOptionValue("dn"),cmd.getOptionValue("password").toCharArray(),Integer.parseInt(cmd.getOptionValue("expiry")));
				}
				if(cmd.hasOption("sign") && cmd.hasOption("signer") && cmd.hasOption("expiry")){
					sslAction.signCertificate(cmd.getOptionValue("name"), cmd.getOptionValue("signer"), Integer.parseInt(cmd.getOptionValue("expiry")));
				}

				if(cmd.hasOption("export") && cmd.hasOption("signer") && cmd.hasOption("expiry") && cmd.hasOption("password")){
					sslAction.exportPKCS12Certificate(cmd.getOptionValue("name"), cmd.getOptionValue("password").toCharArray(), cmd.getOptionValue("signer"));
				}

			}
			else if(cmd.hasOption("store")){
				//String storeName, char[] storePassword, boolean isTrust, String alias, char[] password, boolean isPrivate
				String keytoolBinary = props.getProperty("keytool.binary");
				String localPath = props.getProperty("ssl.ca.path");

				KeyStoreAction keyAct = new KeyStoreAction(keytoolBinary, localPath);
				if(cmd.hasOption("storePassword") && cmd.hasOption("password") && cmd.hasOption("private") && cmd.hasOption("name")){
					keyAct.importPKCS12(cmd.getOptionValue("store"), cmd.getOptionValue("storePassword").toCharArray(), cmd.hasOption("trust"), cmd.getOptionValue("name"), cmd.getOptionValue("password").toCharArray(), cmd.hasOption("private"));
				}
				else if(cmd.hasOption("storePassword") && cmd.hasOption("name")){
					keyAct.importCertificate(cmd.getOptionValue("store"), cmd.getOptionValue("storePassword").toCharArray(), cmd.hasOption("trust"), cmd.getOptionValue("name"));
				}
			
			}
			else if(cmd.hasOption("setCertificate") && cmd.hasOption("organization") && cmd.hasOption("name") && cmd.hasOption("password") && cmd.hasOption("adminPassword")){
				OrganizationCommand.setOrganizationCertificate(cmd.getOptionValue("organization"),  props.getProperty("ssl.ca.path"), cmd.getOptionValue("name"), cmd.getOptionValue("password").toCharArray(), cmd.getOptionValue("adminPassword"));
			}
			else if(cmd.hasOption("testCertificate") && cmd.hasOption("organization")  && cmd.hasOption("adminPassword")){
				OrganizationCommand.testOrganizationCertificate(cmd.getOptionValue("organization"),  props.getProperty("ssl.ca.path"), cmd.getOptionValue("adminPassword"));
			}
			else if(cmd.hasOption("organization") && cmd.hasOption("username") && cmd.hasOption("password")){
				logger.debug("Authenticating user");
				try{
					logger.debug("Finding organization");
					OrganizationType org = Factories.getOrganizationFactory().findOrganization(cmd.getOptionValue("organization"));
					if(org != null){
						String password = cmd.getOptionValue("password");
						if(cmd.hasOption("reset")){
							logger.debug("Resetting credential");
							if(enableUnauthenticatedResets == false){
								logger.info("Unauthenticated password reset capability is disabled");
							}
							else{
								UserType user = Factories.getUserFactory().getByName(cmd.getOptionValue("username"),org.getId());
								if(user != null){
									if(password != null && password.length() > 5){
										logger.info("Creating new primary credential");
										CredentialType cred = CredentialService.newHashedPasswordCredential(user, user, password, true, false);
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
							logger.debug("Logging in");
							UserType user = SessionSecurity.login(cmd.getOptionValue("username"),CredentialEnumType.HASHED_PASSWORD, password, org.getId());
							if(user != null){
								logger.debug("Processing action");
								processAction(user,cmd);
								logger.debug("Logging out");
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
					logger.error(fe.getStackTrace());
				} catch (ArgumentException e) {
					
					logger.error(e.getStackTrace());
				}
				
			}
			else if(cmd.hasOption("generate") && cmd.hasOption("type")){
				GenerateAction.generate(NameEnumType.valueOf(cmd.getOptionValue("type")),cmd.hasOption("execute"), cmd.hasOption("export"),cmd.getOptionValue("path"));
			}
			else{
				logger.info("Syntax");
				logger.info("Setup: -setup -rootPassword password -schema ../AM4_PG9_Schema.txt");
				
			}
		} catch (ParseException e) {
			
			logger.error(e.getStackTrace());
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
					
					logger.error(e.getStackTrace());
				}
				
			}
			long stop = System.currentTimeMillis();
			logger.info("Connection speed: " + (stop - start) + "ms");
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(fe.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
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
		        logger.error(e.getStackTrace());
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
			
			logger.error(e.getStackTrace());
		}
		return logProps;
	}
}
