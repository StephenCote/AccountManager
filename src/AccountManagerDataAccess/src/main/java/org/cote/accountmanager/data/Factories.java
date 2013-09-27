package org.cote.accountmanager.data;

import java.sql.Connection;
import java.sql.SQLException;

import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.AddressFactory;
import org.cote.accountmanager.data.factory.AuditFactory;
import org.cote.accountmanager.data.factory.ContactFactory;
import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.data.factory.ContactInformationParticipationFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.DataParticipationFactory;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.GroupParticipationFactory;
import org.cote.accountmanager.data.factory.MessageFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.PersonParticipationFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.data.factory.SecurityTokenFactory;
import org.cote.accountmanager.data.factory.SessionDataFactory;
import org.cote.accountmanager.data.factory.SessionFactory;
import org.cote.accountmanager.data.factory.StatisticsFactory;
import org.cote.accountmanager.data.factory.TagFactory;
import org.cote.accountmanager.data.factory.TagParticipationFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;

public class Factories {
	
	
	private static OrganizationType rootOrganization = null;
	private static OrganizationType developmentOrganization = null;
	private static OrganizationType systemOrganization = null;
	private static OrganizationType publicOrganization = null;
	private static ContactFactory contactFactory = null;
	private static AddressFactory addressFactory = null;
	private static PersonFactory personFactory = null;
	private static PersonParticipationFactory personParticipationFactory = null;
	private static AuditFactory auditFactory = null;
	private static TagFactory tagFactory = null;
	private static TagParticipationFactory tagParticipationFactory = null;
	private static PermissionFactory permissionFactory = null;
	private static RoleFactory roleFactory = null;
	private static RoleParticipationFactory roleParticipationFactory = null;
	private static UserFactory userFactory = null;
	private static AccountFactory accountFactory = null;
	private static StatisticsFactory statisticsFactory = null;
	private static ContactInformationFactory contactInformationFactory = null;
	private static ContactInformationParticipationFactory contactInformationParticipationFactory = null;
	private static OrganizationFactory orgFactory = null;
	private static GroupParticipationFactory groupParticipationFactory = null;
	private static GroupFactory groupFactory = null;
	private static DataParticipationFactory dataParticipationFactory = null;
	private static DataFactory dataFactory = null;
	private static MessageFactory messageFactory = null;
	private static SecurityTokenFactory securityTokenFactory = null;
	private static SessionFactory sessionFactory = null;
	private static SessionDataFactory sessionDataFactory = null;
	static{
		getOrganizationFactory();
	}
	public static UserType getDocumentControl(OrganizationType org){
		UserType user = null;
		try {
			user = Factories.getUserFactory().getUserByName("Document Control", org);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}
	public static OrganizationType getRootOrganization() {
		return rootOrganization;
	}

	public static OrganizationType getDevelopmentOrganization() {
		return developmentOrganization;
	}

	public static OrganizationType getSystemOrganization() {
		return systemOrganization;
	}

	public static OrganizationType getPublicOrganization() {
		return publicOrganization;
	}
	public static AuditFactory getAuditFactory(){
		if(auditFactory == null){
			auditFactory = new AuditFactory();
			initializeFactory(auditFactory);
		}
		return auditFactory;
	}
	public static SessionDataFactory getSessionDataFactory(){
		if(sessionDataFactory == null){
			sessionDataFactory = new SessionDataFactory();
		}
		return sessionDataFactory;
	}
	public static SessionFactory getSessionFactory(){
		if(sessionFactory == null){
			sessionFactory = new SessionFactory();
			initializeFactory(sessionFactory);
		}
		return sessionFactory;
	}
	public static ContactFactory getContactFactory(){
		if(contactFactory == null){
			contactFactory = new ContactFactory();
			initializeFactory(contactFactory);
		}
		return contactFactory;
	}
	public static AddressFactory getAddressFactory(){
		if(addressFactory == null){
			addressFactory = new AddressFactory();
			initializeFactory(addressFactory);
		}
		return addressFactory;
	}
	public static PersonFactory getPersonFactory(){
		if(personFactory == null){
			personFactory = new PersonFactory();
			initializeFactory(personFactory);
		}
		return personFactory;
	}
	public static PersonParticipationFactory getPersonParticipationFactory(){
		if(personParticipationFactory == null){
			personParticipationFactory = new PersonParticipationFactory();
			initializeFactory(personParticipationFactory);
		}
		return personParticipationFactory;
	}
	public static SecurityTokenFactory getSecurityTokenFactory(){
		if(securityTokenFactory == null){
			securityTokenFactory = new SecurityTokenFactory();
			initializeFactory(securityTokenFactory);
		}
		return securityTokenFactory;
	}
	public static MessageFactory getMessageFactory(){
		if(messageFactory == null){
			messageFactory = new MessageFactory();
			initializeFactory(messageFactory);
		}
		return messageFactory;
	}
	public static TagParticipationFactory getTagParticipationFactory(){
		if(tagParticipationFactory == null){
			tagParticipationFactory = new TagParticipationFactory();
			initializeFactory(tagParticipationFactory);
		}
		return tagParticipationFactory;
	}
	public static TagFactory getTagFactory(){
		if(tagFactory == null){
			tagFactory = new TagFactory();
			initializeFactory(tagFactory);
		}
		return tagFactory;
	}
	public static PermissionFactory getPermissionFactory(){
		if(permissionFactory == null){
			permissionFactory = new PermissionFactory();
			initializeFactory(permissionFactory);
		}
		return permissionFactory;
	}
	public static RoleFactory getRoleFactory(){
		if(roleFactory == null){
			roleFactory = new RoleFactory();
			initializeFactory(roleFactory);
		}
		return roleFactory;
	}
	public static RoleParticipationFactory getRoleParticipationFactory(){
		if(roleParticipationFactory == null){
			roleParticipationFactory = new RoleParticipationFactory();
			initializeFactory(roleParticipationFactory);
		}
		return roleParticipationFactory;
	}
	public static StatisticsFactory getStatisticsFactory(){
		if(statisticsFactory == null){
			statisticsFactory = new StatisticsFactory();
			initializeFactory(statisticsFactory);
		}
		return statisticsFactory;
	}
	public static UserFactory getUserFactory(){
		if(userFactory == null){
			userFactory = new UserFactory();
			initializeFactory(userFactory);
		}
		return userFactory;
	}
	public static AccountFactory getAccountFactory(){
		if(accountFactory == null){
			accountFactory = new AccountFactory();
			initializeFactory(accountFactory);
		}
		return accountFactory;
	}
	public static ContactInformationParticipationFactory getContactInformationParticipationFactory(){
		if(contactInformationParticipationFactory == null){
			contactInformationParticipationFactory = new ContactInformationParticipationFactory();
			initializeFactory(contactInformationParticipationFactory);
		}
		return contactInformationParticipationFactory;
	}
	public static ContactInformationFactory getContactInformationFactory(){
		if(contactInformationFactory == null){
			contactInformationFactory = new ContactInformationFactory();
			initializeFactory(contactInformationFactory);
		}
		return contactInformationFactory;
	}
	public static DataFactory getDataFactory(){
		if(dataFactory == null){
			dataFactory = new DataFactory();
			initializeFactory(dataFactory);
			
		}
		return dataFactory;
	}	
	public static DataParticipationFactory getDataParticipationFactory(){
		if(dataParticipationFactory == null){
			dataParticipationFactory = new DataParticipationFactory();
			initializeFactory(dataParticipationFactory);
		}
		return dataParticipationFactory;
	}
	public static GroupParticipationFactory getGroupParticipationFactory(){
		if(groupParticipationFactory == null){
			groupParticipationFactory = new GroupParticipationFactory();
			initializeFactory(groupParticipationFactory);
		}
		return groupParticipationFactory;
	}
	public static GroupFactory getGroupFactory(){
		if(groupFactory == null){
			groupFactory = new GroupFactory();
			initializeFactory(groupFactory);

		}
		return groupFactory;
	}
	
	/// Recycle organization factories for use in development environments
	/// This is primarily used when deleting and recreating the dev, system, root, and/or public organizations
	/// Otherwise, just delete/add/clearCache through the org factory
	///
	public static void recycleOrganizationFactory(){
		rootOrganization = null;
		systemOrganization = null;
		publicOrganization = null;
		developmentOrganization = null;
		orgFactory.clearCache();
		orgFactory = null;
		getOrganizationFactory();
	}
	public static OrganizationFactory getOrganizationFactory(){
		if(orgFactory == null){
			orgFactory = new OrganizationFactory();
			initializeFactory(orgFactory);
			populate(orgFactory);

		}
		return orgFactory;
	}
	private static boolean populate(OrganizationFactory orgFactory){
		boolean out_bool = false;
		if(orgFactory.isInitialized()){
			try{
				rootOrganization = orgFactory.addOrganization("Global", OrganizationEnumType.ROOT, null);
				systemOrganization = orgFactory.addOrganization("System", OrganizationEnumType.SYSTEM, rootOrganization);
				publicOrganization = orgFactory.addOrganization("Public", OrganizationEnumType.PUBLIC, rootOrganization);
				developmentOrganization = orgFactory.addOrganization("Development", OrganizationEnumType.DEVELOPMENT, rootOrganization);
				out_bool = true;
				
			}
			catch(FactoryException fe){
				fe.printStackTrace();
				System.out.println(fe.getMessage());
				rootOrganization = null;
				systemOrganization = null;
				publicOrganization = null;
				developmentOrganization = null;
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println(e.getMessage());
				rootOrganization = null;
				systemOrganization = null;
				publicOrganization = null;
				developmentOrganization = null;
			}
		}
		return out_bool;
	}
	public static boolean initializeFactory(FactoryBase factory){
		boolean init = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		try {
			factory.initialize(connection);
			init = true;
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return init;
	}
	
	public static boolean isSetup(OrganizationType org){
		boolean out_bool = false;

		if(org != null){
			try{
				UserType adminUser = Factories.getUserFactory().getUserByName("Admin", org);
				if(adminUser != null){
					out_bool = true;
				}
				else{
					System.out.println("Could not find 'Admin' user in org " + org.getName());
				}
			}
			catch(FactoryException fe){
				System.out.println(fe.getMessage());
				fe.printStackTrace();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			System.out.println("Organization is null");
		}
		return out_bool;
	}
	
	public static boolean clearCaches(){
		getAddressFactory().clearCache();
		getContactFactory().clearCache();
		getPersonFactory().clearCache();
		getContactInformationParticipationFactory().clearCache();
		getAccountFactory().clearCache();
		getContactInformationFactory().clearCache();
		getDataFactory().clearCache();
		getDataParticipationFactory().clearCache();
		getGroupFactory().clearCache();
		getGroupParticipationFactory().clearCache();
		getMessageFactory().clearCache();
		getPermissionFactory().clearCache();
		getRoleFactory().clearCache();
		getRoleParticipationFactory().clearCache();
		getSecurityTokenFactory().clearCache();
		getSessionFactory().clearCache();
		getStatisticsFactory().clearCache();
		getTagFactory().clearCache();
		getTagParticipationFactory().clearCache();
		getUserFactory().clearCache();
		EffectiveAuthorizationService.clearCache();
		
		return true;
	}
	public static <T> T getBulkFactory(FactoryEnumType factoryType){
		T fact = null;
		switch(factoryType){
			case PERSON:
				fact = (T)BulkFactories.getBulkPersonFactory();
				break;
			case CONTACT:
				fact = (T)BulkFactories.getBulkContactFactory();
				break;
			case ADDRESS:
				fact = (T)BulkFactories.getBulkAddressFactory();
				break;
			case PERSONPARTICIPATION:
				fact = (T)BulkFactories.getBulkPersonParticipationFactory();
				break;
				
			case CONTACTINFORMATION:
				fact = (T)BulkFactories.getBulkContactInformationFactory();
				break;
			case CONTACTINFORMATIONPARTICIPATION:
				fact = (T)BulkFactories.getBulkContactInformationParticipationFactory();
				break;
			case USER:
				fact = (T)BulkFactories.getBulkUserFactory();
				break;
			case STATISTICS:
				fact = (T)BulkFactories.getBulkStatisticsFactory();
				break;
			case ROLE:
				fact = (T)BulkFactories.getBulkRoleFactory();
				break;
			case ROLEPARTICIPATION:
				fact = (T)BulkFactories.getBulkRoleParticipationFactory();
				break;
			case GROUP:
				fact = (T)BulkFactories.getBulkGroupFactory();
				break;
			case GROUPPARTICIPATION:
				fact = (T)BulkFactories.getBulkGroupParticipationFactory();
				break;
			case TAG:
				fact = (T)BulkFactories.getBulkTagFactory();
				break;
			case TAGPARTICIPATION:
				fact = (T)BulkFactories.getBulkTagParticipationFactory();
				break;
			case DATA:
				fact = (T)BulkFactories.getBulkDataFactory();
				break;
			case DATAPARTICIPATION:
				fact = (T)BulkFactories.getBulkDataParticipationFactory();
				break;
		}
		return fact;
	}	
	public static <T> T getFactory(FactoryEnumType factoryType){
		T fact = null;
		switch(factoryType){
			case PERSON:
				fact = (T)getPersonFactory();
				break;
			case PERSONPARTICIPATION:
				fact = (T)getPersonParticipationFactory();
				break;
			case ADDRESS:
				fact = (T)getAddressFactory();
				break;
			case CONTACT:
				fact = (T)getContactFactory();
				break;
			case CONTACTINFORMATIONPARTICIPATION:
				fact = (T)getContactInformationParticipationFactory();
				break;
			case CONTACTINFORMATION:
				fact = (T)getContactInformationFactory();
				break;
			case USER:
				fact = (T)getUserFactory();
				break;
			case STATISTICS:
				fact = (T)getStatisticsFactory();
				break;
			case ROLE:
				fact = (T)getRoleFactory();
				break;
			case ROLEPARTICIPATION:
				fact = (T)getRoleParticipationFactory();
				break;
			case GROUP:
				fact = (T)getGroupFactory();
				break;
			case GROUPPARTICIPATION:
				fact = (T)getGroupParticipationFactory();
				break;
			case TAG:
				fact = (T)getTagFactory();
				break;
			case TAGPARTICIPATION:
				fact = (T)getTagParticipationFactory();
				break;
			case DATA:
				fact = (T)getDataFactory();
				break;
			case DATAPARTICIPATION:
				fact = (T)getDataParticipationFactory();
				break;
		}
		return fact;
	}
}
