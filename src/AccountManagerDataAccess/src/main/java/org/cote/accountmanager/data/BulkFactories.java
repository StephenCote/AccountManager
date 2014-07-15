package org.cote.accountmanager.data;

import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.bulk.BulkAccountFactory;
import org.cote.accountmanager.data.factory.bulk.BulkAddressFactory;
import org.cote.accountmanager.data.factory.bulk.BulkContactFactory;
import org.cote.accountmanager.data.factory.bulk.BulkContactInformationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkContactInformationParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkDataFactory;
import org.cote.accountmanager.data.factory.bulk.BulkDataParticipationFactory;
import org.cote.accountmanager.data.factory.BulkFactory;
import org.cote.accountmanager.data.factory.bulk.BulkFactFactory;
import org.cote.accountmanager.data.factory.bulk.BulkFunctionFactFactory;
import org.cote.accountmanager.data.factory.bulk.BulkFunctionFactory;
import org.cote.accountmanager.data.factory.bulk.BulkFunctionParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkGroupFactory;
import org.cote.accountmanager.data.factory.bulk.BulkGroupParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkOperationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkPatternFactory;
import org.cote.accountmanager.data.factory.bulk.BulkPersonFactory;
import org.cote.accountmanager.data.factory.bulk.BulkPersonParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkPolicyFactory;
import org.cote.accountmanager.data.factory.bulk.BulkPolicyParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkRoleFactory;
import org.cote.accountmanager.data.factory.bulk.BulkRoleParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkRuleFactory;
import org.cote.accountmanager.data.factory.bulk.BulkRuleParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkStatisticsFactory;
import org.cote.accountmanager.data.factory.bulk.BulkTagFactory;
import org.cote.accountmanager.data.factory.bulk.BulkTagParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkUserFactory;

public class BulkFactories{
	private static BulkFactory bulkFactory = null;
	private static BulkAccountFactory bulkAccountFactory = null;
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
	
	private static BulkFactFactory bulkFactFactory = null;
	private static BulkFunctionFactFactory bulkFunctionFactFactory = null;
	private static BulkFunctionFactory bulkFunctionFactory = null;
	private static BulkFunctionParticipationFactory bulkFunctionParticipationFactory = null;
	private static BulkPolicyParticipationFactory bulkPolicyParticipationFactory = null;
	private static BulkRuleParticipationFactory bulkRuleParticipationFactory = null;
	private static BulkOperationFactory bulkOperationFactory = null;
	private static BulkPatternFactory bulkPatternFactory = null;
	private static BulkPolicyFactory bulkPolicyFactory = null;
	private static BulkRuleFactory bulkRuleFactory = null;
	
	public static BulkFactory getBulkFactory(){
		if(bulkFactory == null){
			bulkFactory = new BulkFactory();
		}
		return bulkFactory;
	}
	public static BulkFactFactory getBulkFactFactory() {
		if(bulkFactFactory == null){
			bulkFactFactory = new BulkFactFactory();
			Factories.initializeFactory(bulkFactFactory);
		}
		return bulkFactFactory;
	}
	public static BulkFunctionFactory getBulkFunctionFactory() {
		if(bulkFunctionFactory == null){
			bulkFunctionFactory = new BulkFunctionFactory();
			Factories.initializeFactory(bulkFunctionFactory);
		}
		return bulkFunctionFactory;
	}
	public static BulkFunctionParticipationFactory getBulkFunctionParticipationFactory() {
		if(bulkFunctionParticipationFactory == null){
			bulkFunctionParticipationFactory = new BulkFunctionParticipationFactory();
			Factories.initializeFactory(bulkFunctionParticipationFactory);
		}
		return bulkFunctionParticipationFactory;
	}
	public static BulkPolicyParticipationFactory getBulkPolicyParticipationFactory() {
		if(bulkPolicyParticipationFactory == null){
			bulkPolicyParticipationFactory = new BulkPolicyParticipationFactory();
			Factories.initializeFactory(bulkPolicyParticipationFactory);
		}
		return bulkPolicyParticipationFactory;
	}
	public static BulkRuleParticipationFactory getBulkRuleParticipationFactory() {
		if(bulkRuleParticipationFactory == null){
			bulkRuleParticipationFactory = new BulkRuleParticipationFactory();
			Factories.initializeFactory(bulkRuleParticipationFactory);
		}
		return bulkRuleParticipationFactory;
	}
	public static BulkFunctionFactFactory getBulkFunctionFactFactory() {
		if(bulkFunctionFactFactory == null){
			bulkFunctionFactFactory = new BulkFunctionFactFactory();
			Factories.initializeFactory(bulkFunctionFactFactory);
		}
		return bulkFunctionFactFactory;
	}
	public static BulkOperationFactory getBulkOperationFactory() {
		if(bulkOperationFactory == null){
			bulkOperationFactory = new BulkOperationFactory();
			Factories.initializeFactory(bulkOperationFactory);
		}
		return bulkOperationFactory;
	}
	public static BulkPatternFactory getBulkPatternFactory() {
		if(bulkPatternFactory == null){
			bulkPatternFactory = new BulkPatternFactory();
			Factories.initializeFactory(bulkPatternFactory);
		}
		return bulkPatternFactory;
	}
	public static BulkPolicyFactory getBulkPolicyFactory() {
		if(bulkPolicyFactory == null){
			bulkPolicyFactory = new BulkPolicyFactory();
			Factories.initializeFactory(bulkPolicyFactory);
		}
		return bulkPolicyFactory;
	}
	public static BulkRuleFactory getBulkRuleFactory() {
		if(bulkRuleFactory == null){
			bulkRuleFactory = new BulkRuleFactory();
			Factories.initializeFactory(bulkRuleFactory);
		}
		return bulkRuleFactory;
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
	public static BulkAccountFactory getBulkAccountFactory() {
		if(bulkAccountFactory == null){
			bulkAccountFactory = new BulkAccountFactory();
			Factories.initializeFactory(bulkAccountFactory);
		}
		return bulkAccountFactory;
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