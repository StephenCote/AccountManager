package org.cote.accountmanager.data.services;

import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.services.ThreadService;

public class SessionDataMaintenance extends ThreadService {
	private int cleanupPeriod = 60000;
	public SessionDataMaintenance(){
		super();
		this.setThreadDelay(cleanupPeriod);
	}
	public void execute(){
		boolean cleanup = DataMaintenance.cleanupSessions();
		//System.out.println("SessionDataMaintenance: Cleanup sessions: " + cleanup);

	}
}
