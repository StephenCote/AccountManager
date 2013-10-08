package org.cote.accountmanager.console;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;

import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.FactoryDefaults;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.util.SecurityUtil;
import org.cote.accountmanager.util.StreamUtil;


public class ConsoleMain {
	public static final Logger logger = Logger.getLogger(ConsoleMain.class.getName());
	
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
		
		options.addOption("name",true,"Variable name");
		options.addOption("addUser",false,"Add a new user");
		options.addOption("addOrganization",false,"Add a new user");
		
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
			if(cmd.hasOption("setup") && cmd.hasOption("rootPassword") && cmd.hasOption("schema")){
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
			else if(cmd.hasOption("addOrganization") && cmd.hasOption("password") && cmd.hasOption("organization") && cmd.hasOption("adminPassword") && cmd.hasOption("name")){
				OrganizationCommand.addOrganization(cmd.getOptionValue("organization"), cmd.getOptionValue("name"), cmd.getOptionValue("adminPassword"), cmd.getOptionValue("password"));
			}
			else if(cmd.hasOption("addUser") && cmd.hasOption("password") && cmd.hasOption("organization") && cmd.hasOption("adminPassword") && cmd.hasOption("name")){
				UserCommand.addUser(cmd.getOptionValue("organization"), cmd.getOptionValue("name"), cmd.getOptionValue("adminPassword"), cmd.getOptionValue("password"), cmd.getOptionValue("email"));
			}

			else if(cmd.hasOption("organization") && cmd.hasOption("username") && cmd.hasOption("password")){
				
				try{
					OrganizationType org = Factories.getOrganizationFactory().findOrganization(cmd.getOptionValue("organization"));
					if(org != null){
						String password_hash = SecurityUtil.getSaltedDigest(cmd.getOptionValue("password"));
						UserType user = SessionSecurity.login(cmd.getOptionValue("username"), password_hash, org);
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
			DataAction.importDataPath(user, cmd.getOptionValue("importData"), cmd.getOptionValue("path"));
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
