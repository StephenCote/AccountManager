package org.cote.accountmanager.data;

import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.BulkAddressFactory;
import org.cote.accountmanager.data.factory.BulkContactFactory;
import org.cote.accountmanager.data.factory.BulkContactInformationFactory;
import org.cote.accountmanager.data.factory.BulkContactInformationParticipationFactory;
import org.cote.accountmanager.data.factory.BulkDataFactory;
import org.cote.accountmanager.data.factory.BulkDataParticipationFactory;
import org.cote.accountmanager.data.factory.BulkFactory;
import org.cote.accountmanager.data.factory.BulkGroupFactory;
import org.cote.accountmanager.data.factory.BulkGroupParticipationFactory;
import org.cote.accountmanager.data.factory.BulkPersonFactory;
import org.cote.accountmanager.data.factory.BulkPersonParticipationFactory;
import org.cote.accountmanager.data.factory.BulkRoleFactory;
import org.cote.accountmanager.data.factory.BulkRoleParticipationFactory;
import org.cote.accountmanager.data.factory.BulkStatisticsFactory;
import org.cote.accountmanager.data.factory.BulkTagFactory;
import org.cote.accountmanager.data.factory.BulkTagParticipationFactory;
import org.cote.accountmanager.data.factory.BulkUserFactory;

public class BulkFactories{
	private static BulkFactory bulkFactory = null;
	private static BulkPersonFactory bulkPersonFactory = null;
	private static BulkContactFactory bulkContactFactory = null;
	private static BulkAddressFactory bulkAddressFactory = null;
	private static BulkPersonParticipationFactory bulkPersonParticipationFactory = null;
	private static BulkContactInformationFactory bulkContactInformationFactory = null;
	private static BulkContactInformationParticipationFactory bulkContactInformationParticipationFactory = null;
	private static BulkStatisticsFactory bulkStatisticsFactory = null;
	private static BulkUserFactory bulkUserFactory = null;
	private static BulkDataFactory bulkDataFactory = null;
	private static BulkDataParticipationFactory bulkDataParticipationFactory = null;
	private static BulkGroupFactory bulkGroupFactory = null;
	private static BulkGroupParticipationFactory bulkGroupParticipationFactory = null;
	private static BulkRoleFactory bulkRoleFactory = null;
	private static BulkRoleParticipationFactory bulkRoleParticipationFactory = null;
	private static BulkTagFactory bulkTagFactory = null;
	private static BulkTagParticipationFactory bulkTagParticipationFactory = null;
	public static BulkFactory getBulkFactory(){
		if(bulkFactory == null){
			bulkFactory = new BulkFactory();
		}
		return bulkFactory;
	}
	public static BulkContactInformationFactory getBulkContactInformationFactory(){
		if(bulkContactInformationFactory == null){
			bulkContactInformationFactory = new BulkContactInformationFactory();
			Factories.initializeFactory(bulkContactInformationFactory);
		}
		return bulkContactInformationFactory;
	}
	public static BulkContactInformationParticipationFactory getBulkContactInformationParticipationFactory(){
		if(bulkContactInformationParticipationFactory == null){
			bulkContactInformationParticipationFactory = new BulkContactInformationParticipationFactory();
			Factories.initializeFactory(bulkContactInformationParticipationFactory);
		}
		return bulkContactInformationParticipationFactory;
	}
	public static BulkStatisticsFactory getBulkStatisticsFactory() {
		if(bulkStatisticsFactory == null){
			bulkStatisticsFactory = new BulkStatisticsFactory();
			Factories.initializeFactory(bulkStatisticsFactory);
		}
		return bulkStatisticsFactory;
	}
	public static BulkPersonParticipationFactory getBulkPersonParticipationFactory() {
		if(bulkPersonParticipationFactory == null){
			bulkPersonParticipationFactory = new BulkPersonParticipationFactory();
			Factories.initializeFactory(bulkPersonParticipationFactory);
		}
		return bulkPersonParticipationFactory;
	}
	public static BulkPersonFactory getBulkPersonFactory() {
		if(bulkPersonFactory == null){
			bulkPersonFactory = new BulkPersonFactory();
			Factories.initializeFactory(bulkPersonFactory);
		}
		return bulkPersonFactory;
	}
	public static BulkContactFactory getBulkContactFactory() {
		if(bulkContactFactory == null){
			bulkContactFactory = new BulkContactFactory();
			Factories.initializeFactory(bulkContactFactory);
		}
		return bulkContactFactory;
	}
	public static BulkAddressFactory getBulkAddressFactory() {
		if(bulkAddressFactory == null){
			bulkAddressFactory = new BulkAddressFactory();
			Factories.initializeFactory(bulkAddressFactory);
		}
		return bulkAddressFactory;
	}
	public static BulkUserFactory getBulkUserFactory() {
		if(bulkUserFactory == null){
			bulkUserFactory = new BulkUserFactory();
			Factories.initializeFactory(bulkUserFactory);
		}
		return bulkUserFactory;
	}
	public static BulkDataFactory getBulkDataFactory() {
		if(bulkDataFactory == null){
			bulkDataFactory = new BulkDataFactory();
			Factories.initializeFactory(bulkDataFactory);
		}
		return bulkDataFactory;
	}
	public static BulkDataParticipationFactory getBulkDataParticipationFactory() {
		if(bulkDataParticipationFactory == null){
			bulkDataParticipationFactory = new BulkDataParticipationFactory();
			Factories.initializeFactory(bulkDataParticipationFactory);
		}
		return bulkDataParticipationFactory;
	}
	public static BulkGroupFactory getBulkGroupFactory() {
		if(bulkGroupFactory == null){
			bulkGroupFactory = new BulkGroupFactory();
			Factories.initializeFactory(bulkGroupFactory);
		}
		return bulkGroupFactory;
	}
	public static BulkGroupParticipationFactory getBulkGroupParticipationFactory() {
		if(bulkGroupParticipationFactory == null){
			bulkGroupParticipationFactory = new BulkGroupParticipationFactory();
			Factories.initializeFactory(bulkGroupParticipationFactory);
		}
		return bulkGroupParticipationFactory;
	}
	public static BulkRoleFactory getBulkRoleFactory() {
		if(bulkRoleFactory == null){
			bulkRoleFactory = new BulkRoleFactory();
			Factories.initializeFactory(bulkRoleFactory);
		}
		return bulkRoleFactory;
	}
	public static BulkRoleParticipationFactory getBulkRoleParticipationFactory() {
		if(bulkRoleParticipationFactory == null){
			bulkRoleParticipationFactory = new BulkRoleParticipationFactory();
			Factories.initializeFactory(bulkRoleParticipationFactory);
		}
		return bulkRoleParticipationFactory;
	}
	public static BulkTagFactory getBulkTagFactory() {
		if(bulkTagFactory == null){
			bulkTagFactory = new BulkTagFactory();
			Factories.initializeFactory(bulkTagFactory);
		}
		return bulkTagFactory;
	}
	public static BulkTagParticipationFactory getBulkTagParticipationFactory() {
		if(bulkTagParticipationFactory == null){
			bulkTagParticipationFactory = new BulkTagParticipationFactory();
			Factories.initializeFactory(bulkTagParticipationFactory);
		}
		return bulkTagParticipationFactory;
	}
	
}