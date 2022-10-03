package org.cote.listeners;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.sockets.WebSocketService;

public class AccountManagerContextListener implements ServletContextListener{
	public static final Logger logger = LogManager.getLogger(AccountManagerContextListener.class);
	
	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
    	logger.info("Chirping users");
    	WebSocketService.activeUsers().forEach(user ->{
    		WebSocketService.chirpUser(user, new String[] {"Service going offline"});
    	});
		logger.info("Context destroyed");
	}

        //Run this before web application is started
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		logger.info("Context initialized");	
	}
}