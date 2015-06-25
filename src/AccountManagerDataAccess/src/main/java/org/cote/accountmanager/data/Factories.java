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
package org.cote.accountmanager.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.AccountFactory;
import org.cote.accountmanager.data.factory.AddressFactory;
import org.cote.accountmanager.data.factory.AsymmetricKeyFactory;
import org.cote.accountmanager.data.factory.AttributeFactory;
import org.cote.accountmanager.data.factory.AuditFactory;
import org.cote.accountmanager.data.factory.ContactFactory;
import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.data.factory.ContactInformationParticipationFactory;
import org.cote.accountmanager.data.factory.ControlFactory;
import org.cote.accountmanager.data.factory.CredentialFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.DataParticipationFactory;
import org.cote.accountmanager.data.factory.FactFactory;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.FunctionFactFactory;
import org.cote.accountmanager.data.factory.FunctionFactory;
import org.cote.accountmanager.data.factory.FunctionParticipationFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.GroupParticipationFactory;
import org.cote.accountmanager.data.factory.MessageFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.OperationFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.PatternFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.PersonFactory;
import org.cote.accountmanager.data.factory.PersonParticipationFactory;
import org.cote.accountmanager.data.factory.PolicyFactory;
import org.cote.accountmanager.data.factory.PolicyParticipationFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.RoleParticipationFactory;
import org.cote.accountmanager.data.factory.RuleFactory;
import org.cote.accountmanager.data.factory.RuleParticipationFactory;
import org.cote.accountmanager.data.factory.SecurityTokenFactory;
import org.cote.accountmanager.data.factory.SessionDataFactory;
import org.cote.accountmanager.data.factory.SessionFactory;
import org.cote.accountmanager.data.factory.StatisticsFactory;
import org.cote.accountmanager.data.factory.SymmetricKeyFactory;
import org.cote.accountmanager.data.factory.TagFactory;
import org.cote.accountmanager.data.factory.TagParticipationFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;


public class Factories {
	
	public static final Logger logger = Logger.getLogger(Factories.class.getName());
	
	private static OrganizationType rootOrganization = null;
	private static OrganizationType developmentOrganization = null;
	private static OrganizationType systemOrganization = null;
	private static OrganizationType publicOrganization = null;
	
	private static AsymmetricKeyFactory asymmetricKeyFactory = null;
	private static SymmetricKeyFactory symmetricKeyFactory = null;
	private static CredentialFactory credentialFactory = null;
	private static ControlFactory controlFactory = null;
	private static FactFactory factFactory = null;
	private static FunctionFactory functionFactory = null;
	private static FunctionFactFactory functionFactFactory = null;
	private static FunctionParticipationFactory functionParticipationFactory = null;
	private static PolicyParticipationFactory policyParticipationFactory = null;
	private static RuleParticipationFactory ruleParticipationFactory = null;
	private static OperationFactory operationFactory = null;
	private static PatternFactory patternFactory = null;
	private static PolicyFactory policyFactory = null;
	private static RuleFactory ruleFactory = null;
	
	private static AttributeFactory attributeFactory = null;
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
	private static String documentControlName = "Document Control";
	
	static{
		getOrganizationFactory();
	}
	public static String getDocumentControlName(){
		return documentControlName;
	}
	public static UserType getDocumentControl(OrganizationType org){
		UserType user = null;
		try {
			user = Factories.getUserFactory().getUserByName(documentControlName, org);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return user;
	}
	public static UserType getAdminUser(OrganizationType org){
		UserType u = null;
		try {
			u = Factories.getUserFactory().getUserByName("Admin", org);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return u;
	}
	public static UserType getRootUser(){
		UserType u = null;
		try {
			u = Factories.getUserFactory().getUserByName("Root", getSystemOrganization());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return u;
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
	public static AsymmetricKeyFactory getAsymmetricKeyFactory(){
		if(asymmetricKeyFactory == null){
			asymmetricKeyFactory = new AsymmetricKeyFactory();
			initializeFactory(asymmetricKeyFactory);
		}
		return asymmetricKeyFactory;
	}
	public static SymmetricKeyFactory getSymmetricKeyFactory(){
		if(symmetricKeyFactory == null){
			symmetricKeyFactory = new SymmetricKeyFactory();
			initializeFactory(symmetricKeyFactory);
		}
		return symmetricKeyFactory;
	}
	public static CredentialFactory getCredentialFactory(){
		if(credentialFactory == null){
			credentialFactory = new CredentialFactory();
			initializeFactory(credentialFactory);
		}
		return credentialFactory;
	}
	public static ControlFactory getControlFactory(){
		if(controlFactory == null){
			controlFactory = new ControlFactory();
			initializeFactory(controlFactory);
		}
		return controlFactory;
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
	public static AttributeFactory getAttributeFactory(){
		if(attributeFactory == null){
			attributeFactory = new AttributeFactory();
			initializeFactory(attributeFactory);
		}
		return attributeFactory;
	}
	public static ContactFactory getContactFactory(){
		if(contactFactory == null){
			contactFactory = new ContactFactory();
			initializeFactory(contactFactory);
		}
		return contactFactory;
	}
	public static FactFactory getFactFactory(){
		if(factFactory == null){
			factFactory = new FactFactory();
			initializeFactory(factFactory);
		}
		return factFactory;
	}
	public static FunctionFactory getFunctionFactory(){
		if(functionFactory == null){
			functionFactory = new FunctionFactory();
			initializeFactory(functionFactory);
		}
		return functionFactory;
	}
	public static FunctionFactFactory getFunctionFactFactory(){
		if(functionFactFactory == null){
			functionFactFactory = new FunctionFactFactory();
			initializeFactory(functionFactFactory);
		}
		return functionFactFactory;
	}
	public static FunctionParticipationFactory getFunctionParticipationFactory(){
		if(functionParticipationFactory == null){
			functionParticipationFactory = new FunctionParticipationFactory();
			initializeFactory(functionParticipationFactory);
		}
		return functionParticipationFactory;
	}
	public static PolicyParticipationFactory getPolicyParticipationFactory(){
		if(policyParticipationFactory == null){
			policyParticipationFactory = new PolicyParticipationFactory();
			initializeFactory(policyParticipationFactory);
		}
		return policyParticipationFactory;
	}
	public static RuleParticipationFactory getRuleParticipationFactory(){
		if(ruleParticipationFactory == null){
			ruleParticipationFactory = new RuleParticipationFactory();
			initializeFactory(ruleParticipationFactory);
		}
		return ruleParticipationFactory;
	}
	public static OperationFactory getOperationFactory(){
		if(operationFactory == null){
			operationFactory = new OperationFactory();
			initializeFactory(operationFactory);
		}
		return operationFactory;
	}
	public static PatternFactory getPatternFactory(){
		if(patternFactory == null){
			patternFactory = new PatternFactory();
			initializeFactory(patternFactory);
		}
		return patternFactory;
	}
	public static PolicyFactory getPolicyFactory(){
		if(policyFactory == null){
			policyFactory = new PolicyFactory();
			initializeFactory(policyFactory);
		}
		return policyFactory;
	}
	public static RuleFactory getRuleFactory(){
		if(ruleFactory == null){
			ruleFactory = new RuleFactory();
			initializeFactory(ruleFactory);
		}
		return ruleFactory;
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
				logger.error(fe.getMessage());
				rootOrganization = null;
				systemOrganization = null;
				publicOrganization = null;
				developmentOrganization = null;
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e.getMessage());
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
					logger.info("Organization not configured.  Could not find 'Admin' user in org " + org.getName());
				}
			}
			catch(FactoryException fe){
				logger.error(fe.getMessage());
				fe.printStackTrace();
			} catch (ArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			logger.error("Organization is null");
		}
		return out_bool;
	}
	
	public static boolean clearCaches(){
		getSymmetricKeyFactory().clearCache();
		getAsymmetricKeyFactory().clearCache();
		getControlFactory().clearCache();
		getCredentialFactory().clearCache();
		getFactFactory().clearCache();
		getFunctionFactory().clearCache();
		getFunctionFactFactory().clearCache();
		getPatternFactory().clearCache();
		getPolicyFactory().clearCache();
		getOperationFactory().clearCache();
		getRuleFactory().clearCache();
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
		getOrganizationFactory().clearCache();
		EffectiveAuthorizationService.clearCache();
		
		return true;
	}
	public static <T> T getBulkFactory(FactoryEnumType factoryType){
		T fact = null;
		switch(factoryType){
			case SYMMETRICKEY:
				fact = (T)BulkFactories.getBulkSymmetricKeyFactory();
				break;
			case ASYMMETRICKEY:
				fact = (T)BulkFactories.getBulkAsymmetricKeyFactory();
				break;
			
			case CREDENTIAL:
				fact = (T)BulkFactories.getBulkCredentialFactory();
				break;
			case CONTROL:
				fact = (T)BulkFactories.getBulkControlFactory();
				break;
			case FACT:
				fact = (T)BulkFactories.getBulkFactFactory();
				break;
			case FUNCTIONFACT:
				fact = (T)BulkFactories.getBulkFunctionFactFactory();
				break;
			case FUNCTION:
				fact = (T)BulkFactories.getBulkFunctionFactory();
				break;
			case FUNCTIONPARTICIPATION:
				fact = (T)BulkFactories.getBulkFunctionParticipationFactory();
				break;
			case POLICYPARTICIPATION:
				fact = (T)BulkFactories.getBulkPolicyParticipationFactory();
				break;
			case RULEPARTICIPATION:
				fact = (T)BulkFactories.getBulkRuleParticipationFactory();
				break;

			case OPERATION:
				fact = (T)BulkFactories.getBulkOperationFactory();
				break;
			case PATTERN:
				fact = (T)BulkFactories.getBulkPatternFactory();
				break;
			case POLICY:
				fact = (T)BulkFactories.getBulkPolicyFactory();
				break;
			case RULE:
				fact = (T)BulkFactories.getBulkRuleFactory();
				break;

			case PERSON:
				fact = (T)BulkFactories.getBulkPersonFactory();
				break;
			case ACCOUNT:
				fact = (T)BulkFactories.getBulkAccountFactory();
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
			case PERMISSION:
				fact = (T)BulkFactories.getBulkPermissionFactory();
				break;
		}
		return fact;
	}	
	public static <T> T getFactory(FactoryEnumType factoryType){
		T fact = null;
		switch(factoryType){
			case SYMMETRICKEY:
				fact = (T)Factories.getSymmetricKeyFactory();
				break;
			case ASYMMETRICKEY:
				fact = (T)Factories.getAsymmetricKeyFactory();
				break;
			case CREDENTIAL:
				fact = (T)Factories.getCredentialFactory();
				break;
			case CONTROL:
				fact = (T)Factories.getControlFactory();
				break;
			case FACT:
				fact = (T)Factories.getFactFactory();
				break;
			case FUNCTIONFACT:
				fact = (T)Factories.getFunctionFactFactory();
				break;
			case FUNCTION:
				fact = (T)Factories.getFunctionFactory();
				break;
			case FUNCTIONPARTICIPATION:
				fact = (T)Factories.getFunctionParticipationFactory();
				break;
			case POLICYPARTICIPATION:
				fact = (T)Factories.getPolicyParticipationFactory();
				break;
			case RULEPARTICIPATION:
				fact = (T)Factories.getRuleParticipationFactory();
				break;

			case OPERATION:
				fact = (T)Factories.getOperationFactory();
				break;
			case PATTERN:
				fact = (T)Factories.getPatternFactory();
				break;
			case POLICY:
				fact = (T)Factories.getPolicyFactory();
				break;
			case RULE:
				fact = (T)Factories.getRuleFactory();
				break;

			case ACCOUNT:
				fact = (T)getAccountFactory();
				break;
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
			case PERMISSION:
				fact = (T)Factories.getPermissionFactory();
				break;
		}
		return fact;
	}
	
	public static <T> void populate(FactoryEnumType factoryType, T object) throws FactoryException, ArgumentException{
		switch(factoryType){

			case ADDRESS:
				getAddressFactory().populate((AddressType)object);
				break;
			case CONTACTINFORMATION:
				getContactInformationFactory().populate((ContactInformationType)object);
				break;
			case USER:
				getUserFactory().populate((UserType)object);
				break;
			case PERSON:
				getPersonFactory().populate((PersonType)object);
				break;
			case GROUP:
				getGroupFactory().populate((BaseGroupType)object);
				break;
			case ROLE:
			case TAG:
			case DATA:
			default:
				break;
		
		}
	}
	
	public static boolean cleanupOrphans(){
		boolean out_bool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		try {
			Statement stat = connection.createStatement();
			stat.executeQuery("SELECT * FROM cleanup_orphans();");
			out_bool = true;
		} catch (SQLException e) {
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
		clearCaches();
		return out_bool;
	}
	
}
