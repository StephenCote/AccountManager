package org.cote.accountmanager.client.action;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.cote.accountmanager.client.ClientContext;
import org.cote.accountmanager.client.ConsoleProcessor;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.NameIdType;


public interface IClientAction {
	public Options getCommandLineOptions();
	public void setContext(ClientContext context);
	public void execute(ConsoleProcessor processor, CommandLine command, String[] line);
	public void list(BaseGroupType parent, long startRecord, int recordCount);
	public void read(String type,String path);
	public void add(NameIdType object);
	public void update(NameIdType object);
	public void delete(NameIdType object);
}
