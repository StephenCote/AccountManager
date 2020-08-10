package org.cote.accountmanager.client;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.client.util.AuthenticationUtil;
import org.cote.accountmanager.client.util.CacheUtil;
import org.cote.accountmanager.objects.ApiClientConfigurationType;
import org.cote.accountmanager.objects.AuthenticationResponseType;
import org.cote.accountmanager.objects.DirectoryGroupType;


public class ClientMain {
	public static final Logger logger = LogManager.getLogger(ClientMain.class);
	
	public static void main(String[] args){
		
		Options options = new Options();
		options.addOption("organization",true,"AccountManager Organization Path");
		options.addOption("username", true, "AccountManager user name");
		options.addOption("password",true,"AccountManager password");
		options.addOption("server",true,"Add/Update a server configuration");
		options.addOption("url",true,"A url value");
		options.addOption("console",false,"Start console mode");
		CommandLineParser parser = new PosixParser();
		ClientContext context = new ClientContext();
		try {
			CommandLine cmd = parser.parse( options, args);
			if(cmd.hasOption("server") && cmd.hasOption("url")){
				ApiClientConfigurationType api = AuthenticationUtil.getApiConfiguration(cmd.getOptionValue("url"));
				CacheUtil.cache(cmd.getOptionValue("server"), api);
				logger.info("Saved " + cmd.getOptionValue("server") + " configuration");
			}
			if(cmd.hasOption("server") && cmd.hasOption("username") && cmd.hasOption("organization")){
				AuthenticationResponseType authResp = AuthenticationUtil.authenticate(context, cmd.getOptionValue("server"),cmd.getOptionValue("organization"),cmd.getOptionValue("username"),cmd.getOptionValue("password"));
				if(authResp != null){
					if(options.hasOption("console")){
						ConsoleProcessor console = new ConsoleProcessor();
						console.runConsole(context);
					}
					logger.info("Logging out: " + AuthenticationUtil.logout(context));	
				}
				else{
					logger.error("Failed to authenticate");
				}
			}
			else{
				logger.error("Incomplete credentials");
			}
		} catch (ParseException e) {
			
			logger.error(e.getMessage());
			logger.error("Error",e);
		}

	}
}

