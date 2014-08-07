package org.cote.accountmanager.data.factory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.BulkEntryType;
import org.cote.accountmanager.objects.BulkSessionType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.FunctionFactType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.StatisticsType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class BulkFactory {
	public static final Logger logger = Logger.getLogger(BulkFactory.class.getName());
	//private Map<String,Map<NameEnumType,Integer>> idScanMap = null;
	protected static Map<String,BulkSessionType> sessions = null;
	protected static Map<Long,String> sessionIdMap = null;
	protected static Map<Long,Long> idMap = null;
	/// Dirty write should be moved to the session object
	///
	protected static Set<FactoryEnumType> dirtyWrite = null;
	protected static int maximumWritePasses = 3;
	//protected static Map<String,String> persistentIdSessionMap = null;
	
	private Random rand = null;
	public BulkFactory(){
		rand = new Random();
		if(dirtyWrite == null) dirtyWrite = new HashSet<FactoryEnumType>();
		if(sessions == null) sessions = new HashMap<String,BulkSessionType>();
		if(sessionIdMap == null) sessionIdMap = new HashMap<Long,String>();
		//if(persistentIdSessionMap == null) persistentIdSessionMap = new HashMap<String,String>();
		if(idMap == null) idMap = new HashMap<Long,Long>();
		//idScanMap = new HashMap<String,Map<NameEnumType,Integer>>();
	}
	/*
	public String getSessionForPersistentId(FactoryEnumType factoryType, long id){
		String out_sess = null;
		String key = factoryType.toString() + "-" + id;
		if(persistentIdSessionMap.containsKey(key)) out_sess = persistentIdSessionMap.get(key);
		return out_sess;
	}
	*/
	public void setDirty(FactoryEnumType factoryType){
		//logger.debug("Marking " + factoryType + " for a dirty write");
		dirtyWrite.add(factoryType);
	}
	public String getSessionForBulkId(long id){
		String out_sess = null;
		if(sessionIdMap.containsKey(id)) out_sess = sessionIdMap.get(id);
		return out_sess;
	}
	public void close(String sessionId) throws ArgumentException{
		if(sessionId == null || sessionId.length() == 0){
			logger.error("Session id is null");
			throw new ArgumentException("Session id is null");
		}
		BulkSessionType session = sessions.get(sessionId);
		if(session == null){
			logger.error("Invalid session id '" + sessionId + "'");
			throw new ArgumentException("Invalid session id '" + sessionId + "'");
		}
		sessions.remove(sessionId);
		//synchronized(sessionIdMap){
			//while (persistentIdSessionMap.values().remove(sessionId));
			Iterator<Long> keys = sessionIdMap.keySet().iterator();
			while(keys.hasNext()){
				long val = keys.next();
				if(sessionIdMap.get(val).equals(sessionId)){
					//sessionIdMap.remove(val);
					keys.remove();
				}
				if(idMap.containsKey(val)) idMap.remove(val);
			}
		//}
	}
	public void write(String sessionId) throws ArgumentException, FactoryException, DataAccessException{
		write(sessionId, 1, 0);
	}
	protected void write(String sessionId, int pass, int offset) throws ArgumentException, FactoryException, DataAccessException{
		long start = System.currentTimeMillis();
		if(sessionId == null || sessionId.length() == 0){
			logger.error("Session id is null");
			throw new ArgumentException("Session id is null");
		}
		BulkSessionType session = sessions.get(sessionId);
		if(session == null){
			logger.error("Invalid session id '" + sessionId + "'");
			throw new ArgumentException("Invalid session id '" + sessionId + "'");
		}
		if(session.getPersisted() == true){
			logger.error("Session id '" + sessionId + "' is already persisted");
			throw new ArgumentException("Session id '" + sessionId + "' is already persisted");			
		}
		
		Map<FactoryEnumType,List<Long>> factoryIds = new HashMap<FactoryEnumType,List<Long>>();
		int eLen = 0;

		synchronized(session){
			eLen = session.getBulkEntries().size();
			logger.info("Writing Bulk Session " + sessionId + " with " + (eLen - offset) + " objects");
			for(int i = offset; i < eLen; i++){
				BulkEntryType entry = session.getBulkEntries().get(i);
				if(entry.getPersisted()){
					logger.warn("Skipping persisted entry " + entry.getObject().getName());
					continue;
				}
				if(factoryIds.containsKey(entry.getFactoryType())==false){
					factoryIds.put(entry.getFactoryType(), getFactoryIds(sessionId,entry.getFactoryType()));
				}
				List<Long> ids = factoryIds.get(entry.getFactoryType());
				long id = ids.remove(ids.size()-1);

				entry.setPersistentId(id);
				entry.setPersisted(true);
				/// don't set id until the cache is cleared for this object
				/// entry.getObject().setId(id);

				/// 2013/06/26 - moved up from writeObject
				/// Set the ids before invoking writeObject
				/// While this will cause a double-pass, it also
				/// allows for object with participant dependencies
				/// to be written out of any particular order
				///
				NameIdFactory factory = getFactory(entry.getFactoryType());
				factory.removeFromCache(entry.getObject(),factory.getCacheKeyName(entry.getObject()));
				entry.getObject().setId(entry.getPersistentId());
				idMap.put(entry.getTemporaryId(), entry.getPersistentId());
				//persistentIdSessionMap.put(entry.getFactoryType().toString() + "-" + entry.getPersistentId(), sessionId);
				
				//writeObject(session, entry);
				//eLen = session.getBulkEntries().size();
			}

			/// 2013/06/26 - Second pass, map ids
			///
			for(int i = offset; i < eLen;i++){
				BulkEntryType entry = session.getBulkEntries().get(i);
				/// Only map ids if the 'persisted' bit is set to true (see previous iteration)
				/// The bit indicates that the final DB id has been assigned
				/// 
				if(entry.getPersisted() == false){
					logger.warn("Skipping unpersisted entry " + entry.getObject().getName());
					continue;
				}
				mapObjectIds(entry);
			}

			/// 2013/06/26 - Third pass, write objects into the bulk table queues
			///
			for(int i = offset; i < eLen; i++){
				BulkEntryType entry = session.getBulkEntries().get(i);

				/// 2013/06/26 - Second pass, map ids
				///
				if(entry.getPersisted() == false){
					logger.warn("Skipping unpersisted entry " + entry.getObject().getName());
					continue;
				}
				writeObject(session, entry);
				/// 2014/01/11  - need to update attributes, but in one bulk pass
			}

			synchronized(dirtyWrite){
				Iterator<FactoryEnumType> keys = factoryIds.keySet().iterator();
				while(keys.hasNext()){
					FactoryEnumType factoryType = keys.next();
					dirtyWrite.remove(factoryType);
					writeSpool(factoryType);
				}
				
				/// A dirty write is when a factory adds an object to be bulk written
				/// But that object was not created as a bulk entry.  Participations are examples of dirty writes.
				///
				if(dirtyWrite.size() > 0){
					FactoryEnumType[] fTypes = dirtyWrite.toArray(new FactoryEnumType[0]);
					for(int i = 0; i < fTypes.length;i++){
						logger.debug("Writing dirty bulk spool for " + fTypes[i]);
						dirtyWrite.remove(fTypes[i]);
						writeSpool(fTypes[i]);
					}
				}
			}
			/// 2013/09/14 - Don't clear the dirtyWrite queue - it should be cleaned up in the previous pass
			///dirtyWrite.clear();
			
		}
		
		/// 2013/09/13 - A session obtains dirty entries when the write operation results in additional bulk entries being created (as opposed to a bulk object being written directly to the table queue)
		/// Those objects cannot just be written because they need to obtain database id, and then have that id cross mapped (if needed) for any foreign keys
		///
		if(session.getBulkEntries().size() != eLen){
			if(pass < maximumWritePasses){
				logger.info("Bulk Session is dirty with " + (session.getBulkEntries().size() - eLen) + " entries.  Attempting pass #" + (pass + 1));
				write(sessionId, pass+1,eLen);
			}
			else{
				logger.error("Bulk Session is still dirty with " + (session.getBulkEntries().size() - eLen) + " entries.  Halting attempts after " + pass + " passes");
			}
		}
		long stop = System.currentTimeMillis();
		if(pass == 1){
			session.setPersisted(true);
			logger.info("Wrote Bulk Session " + sessionId + " in " + (stop - start) + "ms");
		}
		
		
	}
	protected void writeSpool(FactoryEnumType factoryType) throws FactoryException{
		switch(factoryType){
			case FACT:
				BulkFactories.getBulkFactFactory().writeSpool(BulkFactories.getBulkFactFactory().getDataTables().get(0).getName());
				break;
			case FUNCTIONFACT:
				BulkFactories.getBulkFunctionFactFactory().writeSpool(BulkFactories.getBulkFunctionFactFactory().getDataTables().get(0).getName());
				break;
			case FUNCTION:
				BulkFactories.getBulkFunctionFactory().writeSpool(BulkFactories.getBulkFunctionFactory().getDataTables().get(0).getName());
				break;
			case FUNCTIONPARTICIPATION:
				BulkFactories.getBulkFunctionParticipationFactory().writeSpool(BulkFactories.getBulkFunctionParticipationFactory().getDataTables().get(0).getName());
				break;
			case POLICYPARTICIPATION:
				BulkFactories.getBulkPolicyParticipationFactory().writeSpool(BulkFactories.getBulkPolicyParticipationFactory().getDataTables().get(0).getName());
				break;
			case RULEPARTICIPATION:
				BulkFactories.getBulkRuleParticipationFactory().writeSpool(BulkFactories.getBulkRuleParticipationFactory().getDataTables().get(0).getName());
				break;

			case OPERATION:
				BulkFactories.getBulkOperationFactory().writeSpool(BulkFactories.getBulkOperationFactory().getDataTables().get(0).getName());
				break;
			case PATTERN:
				BulkFactories.getBulkPatternFactory().writeSpool(BulkFactories.getBulkPatternFactory().getDataTables().get(0).getName());
				break;
			case POLICY:
				BulkFactories.getBulkPolicyFactory().writeSpool(BulkFactories.getBulkPolicyFactory().getDataTables().get(0).getName());
				break;
			case RULE:
				BulkFactories.getBulkRuleFactory().writeSpool(BulkFactories.getBulkRuleFactory().getDataTables().get(0).getName());
				break;
			case PERMISSION:
				BulkFactories.getBulkPermissionFactory().writeSpool(BulkFactories.getBulkPermissionFactory().getDataTables().get(0).getName());
				break;

			case ACCOUNT:
				BulkFactories.getBulkAccountFactory().writeSpool(BulkFactories.getBulkAccountFactory().getDataTables().get(0).getName());
				break;
			case PERSON:
				BulkFactories.getBulkPersonFactory().writeSpool(BulkFactories.getBulkPersonFactory().getDataTables().get(0).getName());
				break;
			case PERSONPARTICIPATION:
				BulkFactories.getBulkPersonParticipationFactory().writeSpool(BulkFactories.getBulkPersonParticipationFactory().getDataTables().get(0).getName());
				break;
			case CONTACTINFORMATION:
				BulkFactories.getBulkContactInformationFactory().writeSpool(BulkFactories.getBulkContactInformationFactory().getDataTables().get(0).getName());
				break;
			case CONTACTINFORMATIONPARTICIPATION:
				BulkFactories.getBulkContactInformationParticipationFactory().writeSpool(BulkFactories.getBulkContactInformationParticipationFactory().getDataTables().get(0).getName());
				break;
			case CONTACT:
				BulkFactories.getBulkContactFactory().writeSpool(BulkFactories.getBulkContactFactory().getDataTables().get(0).getName());
				break;
			case ADDRESS:
				BulkFactories.getBulkAddressFactory().writeSpool(BulkFactories.getBulkAddressFactory().getDataTables().get(0).getName());
				break;
			case STATISTICS:
				BulkFactories.getBulkStatisticsFactory().writeSpool(BulkFactories.getBulkStatisticsFactory().getDataTables().get(0).getName());
				break;
			case USER:
				BulkFactories.getBulkUserFactory().writeSpool(BulkFactories.getBulkUserFactory().getDataTables().get(0).getName());
				break;
			case DATA:
				BulkFactories.getBulkDataFactory().writeSpool(BulkFactories.getBulkDataFactory().getDataTables().get(0).getName());
				break;
			case DATAPARTICIPATION:
				BulkFactories.getBulkDataParticipationFactory().writeSpool(BulkFactories.getBulkDataParticipationFactory().getDataTables().get(0).getName());
				break;
			case GROUP:
				BulkFactories.getBulkGroupFactory().writeSpool(BulkFactories.getBulkGroupFactory().getDataTables().get(0).getName());
				break;
			case GROUPPARTICIPATION:
				BulkFactories.getBulkGroupParticipationFactory().writeSpool(BulkFactories.getBulkGroupParticipationFactory().getDataTables().get(0).getName());
				break;
			case ROLE:
				BulkFactories.getBulkRoleFactory().writeSpool(BulkFactories.getBulkRoleFactory().getDataTables().get(0).getName());
				break;
			case ROLEPARTICIPATION:
				BulkFactories.getBulkRoleParticipationFactory().writeSpool(BulkFactories.getBulkRoleParticipationFactory().getDataTables().get(0).getName());
				break;
			case TAG:
				BulkFactories.getBulkTagFactory().writeSpool(BulkFactories.getBulkTagFactory().getDataTables().get(0).getName());
				break;
			case TAGPARTICIPATION:
				BulkFactories.getBulkTagParticipationFactory().writeSpool(BulkFactories.getBulkTagParticipationFactory().getDataTables().get(0).getName());
				break;
			default:
				throw new FactoryException("Unhandled factory type: " + factoryType);
		}		
	}
	protected void writeObject(BulkSessionType session, BulkEntryType entry) throws FactoryException, DataAccessException, ArgumentException{
		/// logger.debug("Spooling " + entry.getFactoryType() + " " + entry.getObject().getName() + " with Local id=" + entry.getTemporaryId() + " and DB id=" + entry.getPersistentId());
		/*
		NameIdFactory factory = getFactory(entry.getFactoryType());
		factory.removeFromCache(entry.getObject(),factory.getCacheKeyName(entry.getObject()));
		entry.getObject().setId(entry.getPersistentId());
		idMap.put(entry.getTemporaryId(), entry.getPersistentId());
		*/
		writePreparedObject(session,entry);
	}
	protected void mapObjectIds(BulkEntryType entry){
		/// TODO - Why is this not just looking up the factory type and invoking the method instead of the big switch here?
		/// Note: Not all types are supported, and operations should gracefully fall through 
		///
		
		switch(entry.getFactoryType()){
			case FACT:
				BulkFactories.getBulkFactFactory().mapBulkIds(entry.getObject());
				break;
			case FUNCTIONFACT:
				BulkFactories.getBulkFunctionFactFactory().mapBulkIds(entry.getObject());
				break;
			case FUNCTION:
				BulkFactories.getBulkFunctionFactory().mapBulkIds(entry.getObject());
				break;
			case OPERATION:
				BulkFactories.getBulkOperationFactory().mapBulkIds(entry.getObject());
				break;
			case PATTERN:
				BulkFactories.getBulkPatternFactory().mapBulkIds(entry.getObject());
				break;
			case POLICY:
				BulkFactories.getBulkPolicyFactory().mapBulkIds(entry.getObject());
				break;
			case RULE:
				BulkFactories.getBulkRuleFactory().mapBulkIds(entry.getObject());
				break;
			case PERMISSION:
				BulkFactories.getBulkPermissionFactory().mapBulkIds(entry.getObject());
				break;
			case ACCOUNT:
				BulkFactories.getBulkAccountFactory().mapBulkIds(entry.getObject());
				break;
			case PERSON:
				BulkFactories.getBulkPersonFactory().mapBulkIds(entry.getObject());
				break;
			case ADDRESS:
				BulkFactories.getBulkAddressFactory().mapBulkIds(entry.getObject());
				break;
			case CONTACT:
				BulkFactories.getBulkContactFactory().mapBulkIds(entry.getObject());
				break;
			case USER:
				BulkFactories.getBulkUserFactory().mapBulkIds(entry.getObject());
				break;
			case STATISTICS:
				BulkFactories.getBulkStatisticsFactory().mapBulkIds(entry.getObject());
				break;
			case CONTACTINFORMATION:
				BulkFactories.getBulkContactInformationFactory().mapBulkIds(entry.getObject());
				break;
			case DATA:
				BulkFactories.getBulkDataFactory().mapBulkIds(entry.getObject());
				break;
			case GROUP:
				BulkFactories.getBulkGroupFactory().mapBulkIds(entry.getObject());
				break;
			case ROLE:
				BulkFactories.getBulkRoleFactory().mapBulkIds(entry.getObject());
				break;
			case TAG:
				BulkFactories.getBulkTagFactory().mapBulkIds(entry.getObject());
				break;
		}
	}
	protected void writePreparedObject(BulkSessionType session,BulkEntryType entry) throws FactoryException, ArgumentException, DataAccessException{
		BaseParticipantType part = null;
		switch(entry.getFactoryType()){
			case FACT:
				BulkFactories.getBulkFactFactory().addFact((FactType)entry.getObject());
				break;
			case FUNCTIONFACT:
				BulkFactories.getBulkFunctionFactFactory().addFunctionFact((FunctionFactType)entry.getObject());
				break;
			case FUNCTION:
				BulkFactories.getBulkFunctionFactory().addFunction((FunctionType)entry.getObject());
				break;
			case OPERATION:
				BulkFactories.getBulkOperationFactory().addOperation((OperationType)entry.getObject());
				break;
			case PATTERN:
				BulkFactories.getBulkPatternFactory().addPattern((PatternType)entry.getObject());
				break;
			case POLICY:
				BulkFactories.getBulkPolicyFactory().addPolicy((PolicyType)entry.getObject());
				break;
			case RULE:
				BulkFactories.getBulkRuleFactory().addRule((RuleType)entry.getObject());
				break;
			case PERMISSION:
				BulkFactories.getBulkPermissionFactory().addPermission((BasePermissionType)entry.getObject());
				break;
			case ADDRESS:
				BulkFactories.getBulkAddressFactory().addAddress((AddressType)entry.getObject());
				break;
			case ACCOUNT:
				AccountType account = (AccountType)entry.getObject();
				BulkFactories.getBulkAccountFactory().addAccount(account,false);
				/// Do not allocate contact information through add user
				/// If other contact info is added during the same session then the bulk insert statements for the factory type will be different
				/// And this will cause unexpected results.
				/// The same problem affects any other dirty entity writes
				///
				/*
				if(account.getContactInformation() == null){
					ContactInformationType cit = Factories.getContactInformationFactory().newContactInformation((UserType)entry.getObject());
					createBulkEntry(session.getSessionId(), FactoryEnumType.CONTACTINFORMATION, cit);
				}
				*/
				break;
			case CONTACT:
				BulkFactories.getBulkContactFactory().addContact((ContactType)entry.getObject());
				break;
			case PERSON:
				BulkFactories.getBulkPersonFactory().addPerson((PersonType)entry.getObject());
				break;
			case PERSONPARTICIPATION:
				part = (BaseParticipantType)entry.getObject();
				updateParticipantIds(part);
				BulkFactories.getBulkPersonParticipationFactory().addParticipant(part);
				break;

			case STATISTICS:
				BulkFactories.getBulkStatisticsFactory().addStatistics((StatisticsType)entry.getObject());
				break;
			case CONTACTINFORMATION:
				BulkFactories.getBulkContactInformationFactory().addContactInformation((ContactInformationType)entry.getObject());
				break;
			case CONTACTINFORMATIONPARTICIPATION:
				part = (BaseParticipantType)entry.getObject();
				updateParticipantIds(part);
				BulkFactories.getBulkContactInformationParticipationFactory().addParticipant(part);
				break;

			case USER:
				/// Do not allocate contact information through add user
				/// If other contact info is added during the same session then the bulk insert statements for the factory type will be different
				/// And this will cause unexpected results.
				/// The same problem affects any other dirty entity writes
				///
				UserType user = (UserType)entry.getObject();
				BulkFactories.getBulkUserFactory().addUser(user,false);
				if(user.getContactInformation() == null){
					ContactInformationType cit = Factories.getContactInformationFactory().newContactInformation((UserType)entry.getObject());
					createBulkEntry(session.getSessionId(), FactoryEnumType.CONTACTINFORMATION, cit);
				}
				break;
			case DATA:
				
				BulkFactories.getBulkDataFactory().addData((DataType)entry.getObject());
				break;
			case DATAPARTICIPATION:
				part = (BaseParticipantType)entry.getObject();
				updateParticipantIds(part);
				BulkFactories.getBulkDataParticipationFactory().addParticipant(part);
				break;
			case GROUP:
				
				BulkFactories.getBulkGroupFactory().addGroup((BaseGroupType)entry.getObject());
				break;
			case GROUPPARTICIPATION:
				part = (BaseParticipantType)entry.getObject();
				updateParticipantIds(part);
				BulkFactories.getBulkGroupParticipationFactory().addParticipant(part);
				break;
			case ROLE:
				
				BulkFactories.getBulkRoleFactory().addRole((BaseRoleType)entry.getObject());
				break;
			case ROLEPARTICIPATION:
				part = (BaseParticipantType)entry.getObject();
				updateParticipantIds(part);
				BulkFactories.getBulkRoleParticipationFactory().addParticipant(part);
				break;
			case TAG:
				BulkFactories.getBulkTagFactory().addTag((BaseTagType)entry.getObject());
				break;
			case TAGPARTICIPATION:
				part = (BaseParticipantType)entry.getObject();
				updateParticipantIds(part);
				BulkFactories.getBulkTagParticipationFactory().addParticipant(part);
				break;
			default:
				throw new FactoryException("Unhandled factory type: " + entry.getFactoryType());
		}
	}
	protected long getMappedId(long temporaryId){
		long out_id = 0;
		if(idMap.containsKey(temporaryId)) out_id = idMap.get(temporaryId);
		return out_id;
	}
	protected void updateParticipantIds(BaseParticipantType part) throws ArgumentException{
		if(part.getParticipantId() < 0L){
			if(idMap.containsKey(part.getParticipantId())){
				//logger.debug("Remapping Participant Id " + part.getParticipantId() + " to " + idMap.get(part.getParticipantId()));
				part.setParticipantId(idMap.get(part.getParticipantId()));
			}
			else{
				throw new ArgumentException("Unable to correct participant id");
			}
		}
		if(part.getParticipationId() < 0L){
			if(idMap.containsKey(part.getParticipationId())){
				//logger.debug("Remapping Participation Id " + part.getParticipationId() + " to " + idMap.get(part.getParticipationId()));
				part.setParticipationId(idMap.get(part.getParticipationId()));
			}
			else{
				throw new ArgumentException("Unable to correct participation id");
			}
		}
		if(part.getAffectId() < 0L){
			if(idMap.containsKey(part.getAffectId())){
				part.setAffectId(idMap.get(part.getAffectId()));
			}
			else{
				throw new ArgumentException("Unable to correct affect id");
			}
		}
	}
	protected List<Long> getFactoryIds(String sessionId, FactoryEnumType factoryType) throws ArgumentException, FactoryException{
		List<Long> ids = new ArrayList<Long>();
		NameIdFactory factory = getBulkFactory(factoryType);
		if(factory == null){
			logger.error("Invalid factory type " + factoryType);
			throw new ArgumentException("Invalid factory type " + factoryType);
		}
		if(factory.getBulkMap().containsKey(sessionId) == false){
			logger.error("Bulk ID Map is out of sync for type " + factoryType);
			throw new ArgumentException("Bulk ID Map is out of sync for type " + factoryType);
			
		}
		List<Long> tmpIds = factory.getBulkMap().get(sessionId);
		factory.getBulkMap().remove(sessionId);
		ids = factory.getNextIds(tmpIds.size());
		return ids;
	}
	protected NameIdFactory getBulkFactory(FactoryEnumType factoryType){
		return Factories.getBulkFactory(factoryType);
	}
	protected NameIdFactory getFactory(FactoryEnumType factoryType){
		return Factories.getFactory(factoryType);
	}
	public void createBulkEntry(String sessionId, FactoryEnumType factoryType, NameIdType object) throws ArgumentException{
		long bulkId = (long)(rand.nextDouble()*1000000000L) * -1;
				///rand.nextLong() * -1;
		
		/// With large datasets, the random quickly starts to repeat.  Very bad Java.  Very bad.
		while(sessionIdMap.containsKey(bulkId) == true){
			bulkId = (long)(rand.nextDouble()*1000000000L) * -1;
		}
		
		if(object == null){
			logger.error("Object is null");
			throw new ArgumentException("Object is null");
		}

		
		if(sessionIdMap.containsKey(bulkId)){
			logger.error("Random id " + bulkId + " assigned to " + object.getName() + " already consumed");
			throw new ArgumentException("Random id " + bulkId + " assigned to " + object.getName() + " already consumed");			
		}
		
		if(object.getId() != 0){
			logger.error("Object id is already set");
			throw new ArgumentException("Object id is already set");
		}
		if(object.getNameType() == NameEnumType.UNKNOWN){
			logger.error("Object cannot be of an unknown type");
			throw new ArgumentException("Object cannot be of an unknown type");
		}
		
		NameIdFactory factory = getFactory(factoryType);
		NameIdFactory bulkFactory = getBulkFactory(factoryType);
		
		if(factory == null || bulkFactory == null){
			logger.error("Factory or BulkFactory is null for type " + factoryType);
			throw new ArgumentException("Factory or BulkFactory is null for type " + factoryType);
		}
		
		BulkSessionType session = sessions.get(sessionId);
		if(session == null){
			logger.error("Invalid session id '" + sessionId + "'");
			throw new ArgumentException("Invalid session id '" + sessionId + "'");
		}
		

		sessionIdMap.put(bulkId,sessionId);
		/*
		if(idScanMap.containsKey(sessionId) == false){
			logger.error("Scan map is unavailable");
			throw new ArgumentException("Scan map is unavailable");			
		}
		int currentCount = 0;
		Map<NameEnumType,Integer> scanMap = idScanMap.get(sessionId);
		if(scanMap.containsKey(object.getNameType()) == true){
			currentCount = scanMap.get(object.getNameType());
		}
		scanMap.put(object.getNameType(), currentCount + 1);
		*/
		/// logger.debug("Creating Bulk Entry #" + bulkId + " for " + object.getNameType().toString() + " " + object.getName());
		
		object.setId(bulkId);
		
		BulkEntryType entry = new BulkEntryType();
		entry.setFactoryType(factoryType);
		entry.setPersistentId(0L);
		entry.setTemporaryId(bulkId);
		entry.setObject(object);
		session.getBulkEntries().add(entry);
		
		bulkFactory.addBulkId(sessionId, bulkId);
		if(factory.addToCache(object,factory.getCacheKeyName(object))==false){
			logger.error("Failed to add object '" + object.getName() + "' to factory cache");
		}
		
		//sessionIdMap.put(bulkId, sessionId);
	}
	public String newBulkSession(){
		String sessionId = UUID.randomUUID().toString();
		BulkSessionType sess = new BulkSessionType();
		sess.setSessionId(sessionId);
		Calendar now = Calendar.getInstance();
		sess.setSessionCreated(CalendarUtil.getXmlGregorianCalendar(now.getTime()));
		now.add(Calendar.MINUTE, 5);
		sess.setSessionExpires(CalendarUtil.getXmlGregorianCalendar(now.getTime()));
		sessions.put(sessionId, sess);
		//idScanMap.put(sessionId, new HashMap<NameEnumType,Integer>());
		logger.info("Created Bulk Session '" + sessionId + "'.  Expires: " + sess.getSessionExpires().toString());
		return sessionId;
	}
	
}
