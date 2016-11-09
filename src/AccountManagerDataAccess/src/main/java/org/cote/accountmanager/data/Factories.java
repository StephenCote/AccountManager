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
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.INameIdGroupFactory;
import org.cote.accountmanager.data.factory.IParticipationFactory;
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
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.EffectiveAuthorizationService;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;


public class Factories {
	
	public static final Logger logger = LogManager.getLogger(Factories.class);
	
	private static Map<FactoryEnumType, Class> factoryClasses = new HashMap<>();
	private static Map<FactoryEnumType, Object> factoryInstances = new HashMap<>();
	static {
	    Factories.registerClass(FactoryEnumType.ACCOUNT, AccountFactory.class); 
	    Factories.registerClass(FactoryEnumType.ADDRESS, AddressFactory.class); 
	    Factories.registerClass(FactoryEnumType.ASYMMETRICKEY, AsymmetricKeyFactory.class); 
	    Factories.registerClass(FactoryEnumType.CONTACT, ContactFactory.class); 
	    Factories.registerClass(FactoryEnumType.CONTACTINFORMATION, ContactInformationFactory.class); 
	    Factories.registerClass(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION, ContactInformationParticipationFactory.class); 
	    Factories.registerClass(FactoryEnumType.CONTROL, ControlFactory.class); 
	    Factories.registerClass(FactoryEnumType.CREDENTIAL, CredentialFactory.class); 
	    Factories.registerClass(FactoryEnumType.DATA, DataFactory.class); 
	    Factories.registerClass(FactoryEnumType.DATAPARTICIPATION, DataParticipationFactory.class); 
	    Factories.registerClass(FactoryEnumType.FACT, FactFactory.class); 
	    Factories.registerClass(FactoryEnumType.FUNCTIONFACT, FunctionFactFactory.class); 
	    Factories.registerClass(FactoryEnumType.FUNCTION, FunctionFactory.class); 
	    Factories.registerClass(FactoryEnumType.FUNCTIONPARTICIPATION, FunctionParticipationFactory.class); 
	    Factories.registerClass(FactoryEnumType.GROUP, GroupFactory.class); 
	    Factories.registerClass(FactoryEnumType.GROUPPARTICIPATION, GroupParticipationFactory.class); 
	    Factories.registerClass(FactoryEnumType.MESSAGE, MessageFactory.class); 
	    Factories.registerClass(FactoryEnumType.OPERATION, OperationFactory.class); 
	    Factories.registerClass(FactoryEnumType.ORGANIZATION, OrganizationFactory.class); 
	    Factories.registerClass(FactoryEnumType.PATTERN, PatternFactory.class); 
	    Factories.registerClass(FactoryEnumType.PERMISSION, PermissionFactory.class); 
	    Factories.registerClass(FactoryEnumType.PERSON, PersonFactory.class); 
	    Factories.registerClass(FactoryEnumType.PERSONPARTICIPATION, PersonParticipationFactory.class); 
	    Factories.registerClass(FactoryEnumType.POLICY, PolicyFactory.class); 
	    Factories.registerClass(FactoryEnumType.POLICYPARTICIPATION, PolicyParticipationFactory.class); 
	    Factories.registerClass(FactoryEnumType.ROLE, RoleFactory.class); 
	    Factories.registerClass(FactoryEnumType.ROLEPARTICIPATION, RoleParticipationFactory.class); 
	    Factories.registerClass(FactoryEnumType.RULE, RuleFactory.class); 
	    Factories.registerClass(FactoryEnumType.RULEPARTICIPATION, RuleParticipationFactory.class); 
	    Factories.registerClass(FactoryEnumType.SECURITYTOKEN, SecurityTokenFactory.class); 
	    Factories.registerClass(FactoryEnumType.STATISTICS, StatisticsFactory.class); 
	    Factories.registerClass(FactoryEnumType.SYMMETRICKEY, SymmetricKeyFactory.class); 
	    Factories.registerClass(FactoryEnumType.TAG, TagFactory.class); 
	    Factories.registerClass(FactoryEnumType.TAGPARTICIPATION, TagParticipationFactory.class); 
	    Factories.registerClass(FactoryEnumType.USER, UserFactory.class); 
	}
	
	
	private static String documentControlName = "Document Control";
	
	private static OrganizationType rootOrganization = null;
	private static OrganizationType developmentOrganization = null;
	private static OrganizationType systemOrganization = null;
	private static OrganizationType publicOrganization = null;
	
	private static AuditFactory auditFactory = null;
	private static AttributeFactory attributeFactory = null;
	private static SecurityTokenFactory securityTokenFactory = null;
	private static SessionFactory sessionFactory = null;
	private static SessionDataFactory sessionDataFactory = null;

	
	static{
		//getOrganizationFactory();
		/*
		 * 2016/05/17 - Warm up added because factories now support registering their own entitlement sets
		 * 
		 */
		
		/// 2016/11/07 - Refactoring factory references/instances
		
		/// warmUp();
		//AuthorizationService.registerParticipationFactory(FactoryEnumType.DATA,getDataParticipationFactory());
		//AuthorizationService.registerParticipationFactory(FactoryEnumType.GROUP,getGroupParticipationFactory());
		//AuthorizationService.registerParticipationFactory(FactoryEnumType.PERSON,getPersonParticipationFactory());
		//AuthorizationService.registerParticipationFactory(FactoryEnumType.ROLE,getRoleParticipationFactory());
	}
	
	protected static boolean registerClass(FactoryEnumType ftype, Class fClass){
		if(factoryClasses.containsKey(ftype)){
			logger.error("Factory " + ftype.toString() + " already registered");
			return false;
		}
		factoryClasses.put(ftype, fClass);
		return true;
	}
	
	public static Map<FactoryEnumType, Class> getFactoryClasses() {
		return factoryClasses;
	}

	public static Map<FactoryEnumType, Object> getFactoryInstances() {
		return factoryInstances;
	}

	protected static <T> T getInstance(FactoryEnumType ftype){
		T newObj = null;
		if(factoryClasses.containsKey(ftype) == false){
			logger.error("Factory type " + ftype.toString() + " not registered");
			return newObj;
		}
		if(factoryInstances.containsKey(ftype) == true) return (T)factoryInstances.get(ftype);
		try {
			newObj = (T)factoryClasses.get(ftype).newInstance();
			if(newObj != null){
				initializeFactory((FactoryBase)newObj);
				factoryInstances.put(ftype, newObj);
			}
		} catch (InstantiationException | IllegalAccessException e) {
			// TODO Auto-generated catch block
			logger.error("Error",e);
			newObj = null;
		}
		return newObj;
	}
	
	public static String getDocumentControlName(){
		return documentControlName;
	}
	
	
	public static UserType getDocumentControl(long organizationId){
		UserType user = null;
		try {
			user = Factories.getNameIdFactory(FactoryEnumType.USER).getByName(documentControlName, organizationId);
		} catch (FactoryException | ArgumentException e) {
			logger.error("Trace",e);
		}
		return user;
	}
	public static UserType getAdminUser(long organizationId){
		UserType u = null;
		try {
			u = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Admin", organizationId);
		} catch (FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
			logger.error("Trace",e);
		}
		return u;
	}
	public static UserType getRootUser(){
		UserType u = null;
		try {
			u = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Root", getSystemOrganization().getId());
		} catch (FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
			logger.error("Trace",e);
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
	public static AuditFactory getAuditFactory(){
		if(auditFactory == null){
			auditFactory = new AuditFactory();
			initializeFactory(auditFactory);
		}
		return auditFactory;
	}

	public static SecurityTokenFactory getSecurityTokenFactory(){
		if(securityTokenFactory == null){
			securityTokenFactory = new SecurityTokenFactory();
			initializeFactory(securityTokenFactory);
		}
		return securityTokenFactory;
	}
	
	/// Recycle factories for development and new setups
	/// When the schema is nuked during setup, cached values may be null or invalid
	///
	public static void recycleFactories(){
		coolDown();
		recycleOrganizationFactory();
		warmUp();
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
		//factoryInstances.remove(FactoryEnumType.ORGANIZATION);
		//populate(getFactory(FactoryEnumType.ORGANIZATION));
		/*
		if(orgFactory != null) orgFactory.clearCache();
		orgFactory = null;
		getOrganizationFactory();
		*/
	}

	private static boolean populate(OrganizationFactory orgFactory){
		boolean out_bool = false;
		if(orgFactory.isInitialized()){
			try{
				rootOrganization = orgFactory.addOrganization("Global", OrganizationEnumType.ROOT, null);
				systemOrganization = orgFactory.addOrganization("System", OrganizationEnumType.SYSTEM, rootOrganization);
				publicOrganization = orgFactory.addOrganization("Public", OrganizationEnumType.PUBLIC, rootOrganization);
				developmentOrganization = orgFactory.addOrganization("Development", OrganizationEnumType.DEVELOPMENT, rootOrganization);
				((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).denormalize(rootOrganization);
				((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).denormalize(systemOrganization);
				((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).denormalize(developmentOrganization);
				((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).denormalize(publicOrganization);
				out_bool = true;
				
			}
			catch(FactoryException | ArgumentException e) {
				//logger.error("Trace",e);
				logger.error(e.getMessage());
				logger.error("Trace",e);
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
			
			logger.error("Trace",e);
		}
		finally{
			try {
				if(connection != null) connection.close();
			} catch (SQLException e) {
				
				logger.error("Trace",e);
			}
		}
		return init;
	}
	
	public static boolean isSetup(long organizationId){
		boolean out_bool = false;

		if(organizationId > 0L){
			try{
				UserType adminUser = Factories.getNameIdFactory(FactoryEnumType.USER).getByName("Admin", organizationId);
				if(adminUser != null){
					out_bool = true;
				}
				else{
					logger.info("Organization not configured.  Could not find 'Admin' user in org " + organizationId);
				}
			}
			catch(FactoryException | ArgumentException e){
				logger.error(e.getMessage());
				logger.error("Trace",e);
			}
		}
		else{
			logger.error("Organization is null");
		}
		return out_bool;
	}
	
	public static boolean clearCaches(){

		for(Object fact : factoryInstances.values()){
			if(fact instanceof INameIdFactory){
				((INameIdFactory)fact).clearCache();
			}
			else{
				logger.debug(((FactoryBase)fact).getFactoryType().toString() + " doesn't support caching");
			}
			
		}

		EffectiveAuthorizationService.clearCache();
		
		return true;
	}
	public static <T> T getBulkFactory(FactoryEnumType factoryType){
		return BulkFactories.getInstance(factoryType);
	}	
	public static <T> T getFactory(FactoryEnumType factoryType){
		return getInstance(factoryType);
	}
	public static INameIdFactory getNameIdFactory(FactoryEnumType factoryType){
		return getInstance(factoryType);
	}
	public static INameIdGroupFactory getNameIdGroupFactory(FactoryEnumType factoryType){
		return getInstance(factoryType);
	}
	public static IParticipationFactory getParticipationFactory(FactoryEnumType factoryType){
		return getInstance(factoryType);
	}
	
	public static <T> void populate(FactoryEnumType factoryType, T object) throws FactoryException, ArgumentException{
		NameIdFactory fact = Factories.getFactory(factoryType);
		if(fact != null)
			fact.populate(object);
		
	}
	
	public static void coolDown(){
		AuthorizationService.clearProviders();
		rootOrganization = null;
		developmentOrganization = null;
		systemOrganization = null;
		publicOrganization = null;


		for(Object o : factoryInstances.values()){
			if(o instanceof INameIdFactory){
				INameIdFactory iFact = (INameIdFactory)o;
				iFact.clearCache();
			}
		}
		factoryInstances.clear();
		attributeFactory = null;
		auditFactory = null;
		securityTokenFactory = null;
		sessionFactory = null;
		sessionDataFactory = null;

	}
    public static void prepare(){
    	logger.info("Touch Rocket to initialize static registration");
    	BulkFactories.prepare();
    }
	public static void warmUp(){
		logger.info("Warming up factory " + factoryClasses.size() + " factory instances");
		prepare();
		long startWarmUp = System.currentTimeMillis();
		//getOrganizationFactory();
		//getFactory(FactoryEnumType.ORGANIZATION);
		
		if(factoryInstances.containsKey(FactoryEnumType.ORGANIZATION) == false){
			populate(getFactory(FactoryEnumType.ORGANIZATION));
		}
		
		for(FactoryEnumType f : factoryClasses.keySet()){
			if(factoryInstances.containsKey(f) || f.equals(FactoryEnumType.ORGANIZATION)) continue;
			FactoryBase bFact = getFactory(f);
			//if()
			bFact.registerProvider();
		}
		logger.debug("Warmed up factories in " + (System.currentTimeMillis() - startWarmUp) + "ms");
	}
	public static boolean cleanupOrphans(){
		boolean out_bool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		logger.debug("Cleanup Orphans");
		try {
			Statement stat = connection.createStatement();
			stat.execute("SELECT * FROM cleanup_orphans();");
			stat.close();
			out_bool = true;
		} catch (SQLException e) {

			logger.error("Trace",e);
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {

				logger.error("Trace",e);
			}
		}
		clearCaches();
		return out_bool;
	}
	
}
