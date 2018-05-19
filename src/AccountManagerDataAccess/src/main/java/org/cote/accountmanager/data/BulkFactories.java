/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.factory.BulkFactory;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.INameIdFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.bulk.BulkAccountFactory;
import org.cote.accountmanager.data.factory.bulk.BulkAddressFactory;
import org.cote.accountmanager.data.factory.bulk.BulkAsymmetricKeyFactory;
import org.cote.accountmanager.data.factory.bulk.BulkContactFactory;
import org.cote.accountmanager.data.factory.bulk.BulkContactInformationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkContactInformationParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkControlFactory;
import org.cote.accountmanager.data.factory.bulk.BulkCredentialFactory;
import org.cote.accountmanager.data.factory.bulk.BulkDataFactory;
import org.cote.accountmanager.data.factory.bulk.BulkDataParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkFactFactory;
import org.cote.accountmanager.data.factory.bulk.BulkFunctionFactFactory;
import org.cote.accountmanager.data.factory.bulk.BulkFunctionFactory;
import org.cote.accountmanager.data.factory.bulk.BulkFunctionParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkGroupFactory;
import org.cote.accountmanager.data.factory.bulk.BulkGroupParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkOperationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkPatternFactory;
import org.cote.accountmanager.data.factory.bulk.BulkPermissionFactory;
import org.cote.accountmanager.data.factory.bulk.BulkPersonFactory;
import org.cote.accountmanager.data.factory.bulk.BulkPersonParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkPolicyFactory;
import org.cote.accountmanager.data.factory.bulk.BulkPolicyParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkRoleFactory;
import org.cote.accountmanager.data.factory.bulk.BulkRoleParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkRuleFactory;
import org.cote.accountmanager.data.factory.bulk.BulkRuleParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkStatisticsFactory;
import org.cote.accountmanager.data.factory.bulk.BulkSymmetricKeyFactory;
import org.cote.accountmanager.data.factory.bulk.BulkTagFactory;
import org.cote.accountmanager.data.factory.bulk.BulkTagParticipationFactory;
import org.cote.accountmanager.data.factory.bulk.BulkUserFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.types.FactoryEnumType;

public class BulkFactories{
	
	public static final Logger logger = LogManager.getLogger(BulkFactories.class);
	
	@SuppressWarnings("rawtypes")
	private static Map<FactoryEnumType, Class> factoryClasses = new HashMap<>();
	private static Map<FactoryEnumType, Object> factoryInstances = new HashMap<>();

    static{
    	BulkFactories.registerClass(FactoryEnumType.ACCOUNT, BulkAccountFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.ADDRESS, BulkAddressFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.ASYMMETRICKEY, BulkAsymmetricKeyFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.CONTACT, BulkContactFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.CONTACTINFORMATION, BulkContactInformationFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.CONTACTINFORMATIONPARTICIPATION, BulkContactInformationParticipationFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.CONTROL, BulkControlFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.CREDENTIAL, BulkCredentialFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.DATA, BulkDataFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.DATAPARTICIPATION, BulkDataParticipationFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.FACT, BulkFactFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.FUNCTIONFACT, BulkFunctionFactFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.FUNCTION, BulkFunctionFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.FUNCTIONPARTICIPATION, BulkFunctionParticipationFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.GROUP, BulkGroupFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.GROUPPARTICIPATION, BulkGroupParticipationFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.OPERATION, BulkOperationFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.PATTERN, BulkPatternFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.PERMISSION, BulkPermissionFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.PERSON, BulkPersonFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.PERSONPARTICIPATION, BulkPersonParticipationFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.POLICY, BulkPolicyFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.POLICYPARTICIPATION, BulkPolicyParticipationFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.ROLE, BulkRoleFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.ROLEPARTICIPATION, BulkRoleParticipationFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.RULE, BulkRuleFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.RULEPARTICIPATION, BulkRuleParticipationFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.STATISTICS, BulkStatisticsFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.SYMMETRICKEY, BulkSymmetricKeyFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.TAG, BulkTagFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.TAGPARTICIPATION, BulkTagParticipationFactory.class); 
	    BulkFactories.registerClass(FactoryEnumType.USER, BulkUserFactory.class);
    }
	
    public static void prepare(){
    	logger.debug("Touch Bulk Factories to initialize static registration");
    }
	
	private static BulkFactory bulkFactory = null;
	
	
	@SuppressWarnings("rawtypes")
	public static boolean registerClass(FactoryEnumType ftype, Class fClass){
		if(factoryClasses.containsKey(ftype)){
			logger.error("Factory " + ftype.toString() + " already registered");
			return false;
		}
		factoryClasses.put(ftype, fClass);
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	public static Map<FactoryEnumType, Class> getFactoryClasses() {
		return factoryClasses;
	}

	public static Map<FactoryEnumType, Object> getFactoryInstances() {
		return factoryInstances;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getInstance(FactoryEnumType ftype) throws FactoryException{
		T newObj = null;
		if(!factoryClasses.containsKey(ftype)){
			throw new FactoryException(String.format(FactoryException.TYPE_NOT_REGISTERED, ftype.toString()));
		}
		if(factoryInstances.containsKey(ftype)) return (T)factoryInstances.get(ftype);
		try {
			newObj = (T)factoryClasses.get(ftype).newInstance();
			if(newObj != null){
				Factories.initializeFactory((FactoryBase)newObj);
				factoryInstances.put(ftype, newObj);
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new FactoryException(e.getMessage());
		}
		if(newObj == null) throw new FactoryException(String.format(FactoryException.TYPE_NOT_REGISTERED, ftype.toString()));
		return newObj;
	}
	public static String reportCacheSize(){
		StringBuilder buff = new StringBuilder();
		for(Object fact : factoryInstances.values()){
			if(fact instanceof INameIdFactory){
				NameIdFactory ifact = ((NameIdFactory)fact);
				buff.append(ifact.reportCacheSize());
				buff.append("Rows\t" + ifact.getDataTables().get(0).getRows().size() + "\n");
			}
			
		}
		return buff.toString();
	}
	public static BulkFactory getBulkFactory(){
		if(bulkFactory == null){
			bulkFactory = new BulkFactory();
		}
		return bulkFactory;
	}
	
	public static void warmUp() throws FactoryException{
		logger.debug("Warming up bulk factory " + factoryClasses.size() + " factory instances");
		prepare();
		long startWarmUp = System.currentTimeMillis();
		
		for(FactoryEnumType f : factoryClasses.keySet()){
			if(factoryInstances.containsKey(f) || f.equals(FactoryEnumType.ORGANIZATION)) continue;
			getInstance(f);
		}
		logger.debug("Warmed up factories in " + (System.currentTimeMillis() - startWarmUp) + "ms");
	}
	
	public static void coolDown(){
		for(Object o : factoryInstances.values()){
			if(o instanceof INameIdFactory){
				INameIdFactory iFact = (INameIdFactory)o;
				iFact.clearCache();
			}
		}
		factoryInstances.clear();
	}

	
}