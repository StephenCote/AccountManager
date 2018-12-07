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
package org.cote.accountmanager.data.factory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.BulkEntryType;
import org.cote.accountmanager.objects.BulkSessionType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class BulkFactory {
	public static final Logger logger = LogManager.getLogger(BulkFactory.class);

	private static final String BULK_SESSIONID_NULL = "Session id is null";
	private static final String INVALID_SESSIONID = "Invalid session id: %s";
	
	protected static Map<String,BulkSessionType> sessions = new HashMap<>();
	protected static Map<Long,String> sessionIdMap = new HashMap<>();
	protected static Map<Long,Long> idMap = new HashMap<>();
	/// Dirty write should be moved to the session object
	///
	protected static Set<FactoryEnumType> dirtyWrite = new HashSet<>();
	protected static int maximumWritePasses = 3;
	
	/// globalSessionId is used to track ad-hoc writes that, for whatever reason, could not trace back to a particular session
	/// EG: Bulk adds routed through update requests where no bulk id is available to retrieve the sessionId
	///
	protected static String globalSessionId = null;
	protected static Object globalLock = new Object();
	/// Note - update cache being stored against the session opposed to the data level as an initial proof
	/// eventually ,this should be migrated to the data level
	///
	protected static Map<String,Map<FactoryEnumType,List<NameIdType>>> updateCache = new HashMap<>();
	protected static Map<String,Map<FactoryEnumType,List<NameIdType>>> deleteCache = new HashMap<>();
	protected static Map<String,Map<FactoryEnumType,Set<String>>> updateSet = new HashMap<>();
	protected static Map<String,Map<FactoryEnumType,Set<String>>> deleteSet = new HashMap<>();

	private Random rand = null;
	public BulkFactory(){
		rand = new Random();

	}

	public String getGlobalSessionId(){
		return globalSessionId;
	}
	public void setDirty(FactoryEnumType factoryType){
		dirtyWrite.add(factoryType);
	}
	public String getSessionForBulkId(long id){
		String outSess = null;
		if(sessionIdMap.containsKey(id)) outSess = sessionIdMap.get(id);
		return outSess;
	}
	public void close(String sessionId) throws ArgumentException{
		logger.debug("Closing bulk session " + sessionId);
		if(sessionId == null || sessionId.length() == 0){
			logger.error(BULK_SESSIONID_NULL);
			throw new ArgumentException(BULK_SESSIONID_NULL);
		}
		BulkSessionType session = sessions.get(sessionId);
		if(session == null){
			logger.error(String.format(INVALID_SESSIONID, sessionId));
			throw new ArgumentException(String.format(INVALID_SESSIONID, sessionId));
		}
		sessions.remove(sessionId);
		updateCache.remove(sessionId);
		updateSet.remove(sessionId);
		deleteSet.remove(sessionId);
		deleteCache.remove(sessionId);


		Iterator<Long> keys = sessionIdMap.keySet().iterator();
		while(keys.hasNext()){
			long val = keys.next();
			if(sessionIdMap.get(val).equals(sessionId)){
				keys.remove();
			}
			if(idMap.containsKey(val)) idMap.remove(val);
			}

	}
	public void write(String sessionId) throws ArgumentException, FactoryException, DataAccessException{
		write(sessionId, 1, 0);
	}
	protected void write(String sessionId, int pass, int offset) throws ArgumentException, FactoryException, DataAccessException{
		long start = System.currentTimeMillis();
		if(sessionId == null || sessionId.length() == 0){
			logger.error(BULK_SESSIONID_NULL);
			throw new ArgumentException(BULK_SESSIONID_NULL);
		}
		BulkSessionType session = sessions.get(sessionId);
		if(session == null){
			logger.error(String.format(INVALID_SESSIONID, "null"));
			throw new ArgumentException(String.format(INVALID_SESSIONID, "null"));
		}
		if(session.getPersisted()){
			logger.error("Session id '" + sessionId + "' is already persisted");
			throw new ArgumentException("Session id '" + sessionId + "' is already persisted");			
		}
		
		Map<FactoryEnumType,List<Long>> factoryIds = new HashMap<>();
		int eLen = 0;

		synchronized(session){
			eLen = session.getBulkEntries().size();
			logger.debug("Writing Bulk Session " + sessionId + " with " + (eLen - offset) + " objects");
			
			for(int i = offset; i < eLen; i++){
				BulkEntryType entry = session.getBulkEntries().get(i);
				if(entry.getPersisted()){
					logger.warn("Skipping persisted entry " + entry.getObject().getName());
					continue;
				}
				if(!factoryIds.containsKey(entry.getFactoryType())){
					long startFact = System.currentTimeMillis();
					factoryIds.put(entry.getFactoryType(), getFactoryIds(sessionId,entry.getFactoryType()));
					logger.debug("Retrieved factory ids for " + entry.getFactoryType().toString() + " in " + (System.currentTimeMillis() - startFact) + "ms");
				}
				List<Long> ids = factoryIds.get(entry.getFactoryType());
				long id = ids.remove(ids.size()-1);

				entry.setPersistentId(id);
				entry.setPersisted(true);
				/// don't set id until the cache is cleared for this object

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
			}

			/// 2013/06/26 - Second pass, map ids
			///
			for(int i = offset; i < eLen;i++){
				BulkEntryType entry = session.getBulkEntries().get(i);
				/// Only map ids if the 'persisted' bit is set to true (see previous iteration)
				/// The bit indicates that the final DB id has been assigned
				/// 
				if(!entry.getPersisted()){
					logger.warn("Skipping unpersisted entry " + entry.getObject().getName());
					continue;
				}
				mapObjectIds(entry);
			}

			/// 2013/06/26 - Third pass, write objects into the bulk table queues
			/// 2014/08/15 - Add attribute dump
			List<NameIdType> attrDump = new ArrayList<>();
			long totalAttrs = 0;
			for(int i = offset; i < eLen; i++){
				BulkEntryType entry = session.getBulkEntries().get(i);
				if(!entry.getObject().getAttributes().isEmpty()){
					totalAttrs += entry.getObject().getAttributes().size();
					attrDump.add(entry.getObject());
				}
				/// 2013/06/26 - Second pass, map ids
				///
				if(!entry.getPersisted()){
					logger.warn("Skipping unpersisted entry " + entry.getObject().getName());
					continue;
				}
				writeObject(session, entry);
				/// 2014/01/11  - need to update attributes, but in one bulk pass
			}

			logger.debug("Writing " + totalAttrs + " attributes for " + attrDump.size() + " objects");
			Factories.getAttributeFactory().addAttributes(attrDump.toArray(new NameIdType[0]));
			
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
				if(!dirtyWrite.isEmpty()){
					FactoryEnumType[] fTypes = dirtyWrite.toArray(new FactoryEnumType[0]);
					for(int i = 0; i < fTypes.length;i++){
						logger.debug("Writing dirty bulk spool for " + fTypes[i]);
						dirtyWrite.remove(fTypes[i]);
						writeSpool(fTypes[i]);
					}
				}
			}

			/// 2013/09/14 - Don't clear the dirtyWrite queue past this point- it should be cleaned up in the previous pass
			/// 

			/// 2016/07/22 - Add bulk delete
			/// 
			if(deleteCache.containsKey(sessionId)){
				int count = 0;
				for (Entry<FactoryEnumType,List<NameIdType>> entry : deleteCache.get(sessionId).entrySet()) {
					FactoryEnumType factoryType = entry.getKey();
					List<NameIdType> objs = entry.getValue();
					logger.debug("Processing delete cache for " + factoryType.toString() + " having " + objs.size() + " objects");
					deleteSpool(factoryType,objs);
					NameIdFactory factory = getBulkFactory(factoryType);
					logger.debug("Processing bulk modifications for " + factoryType.toString());
					factory.deleteBulk(objs,null);
					logger.debug("Processing bulk attribute modifications cache for " + factoryType.toString());
					Factories.getAttributeFactory().deleteAttributesForObjects(objs.toArray(new NameIdType[0]));
					count += objs.size();
					
				}
				logger.debug("Deleted " + count + " objects");
				deleteCache.get(sessionId).clear();
				deleteSet.get(sessionId).clear();
			}
			else{
				logger.debug("Delete cache is empty");
			}
			
			/// 2014/12/23 - Add bulk update hook
			if(updateCache.containsKey(sessionId)){
				int count = 0;

				/// 2016/07/27 - bug with the modify method where the updateCache gets multiple entries, even though the check catches it
				///

				for (Entry<FactoryEnumType,List<NameIdType>> entry : updateCache.get(sessionId).entrySet()) {
					FactoryEnumType factoryType = entry.getKey();
					List<NameIdType> objs = entry.getValue();
					logger.debug("Processing modification cache for " + factoryType.toString() + " having " + objs.size() + " objects");
					updateSpool(factoryType,objs);
					NameIdFactory factory = getBulkFactory(factoryType);
					logger.debug("Processing bulk modifications for " + factoryType.toString());
					factory.updateBulk(objs,null);
					logger.debug("Processing bulk attribute modifications cache for " + factoryType.toString());
					Factories.getAttributeFactory().updateAttributes(objs.toArray(new NameIdType[0]));
					count += objs.size();
					
				}
				logger.debug("Modified " + count + " objects");
				updateCache.get(sessionId).clear();
				updateSet.get(sessionId).clear();
			}
			else{
				logger.debug("Modification cache is empty");
			}

		}
		synchronized(globalLock){
			if(globalSessionId != null && !globalSessionId.equals(sessionId)){
				logger.info("Writing global session");
				write(globalSessionId);
				globalSessionId = null;
			}
		}
		
		/// 2013/09/13 - A session obtains dirty entries when the write operation results in additional bulk entries being created (as opposed to a bulk object being written directly to the table queue)
		/// Those objects cannot just be written because they need to obtain database id, and then have that id cross mapped (if needed) for any foreign keys
		///
		if(session.getBulkEntries().size() != eLen){
			if(pass < maximumWritePasses){
				logger.debug("Bulk Session is dirty with " + (session.getBulkEntries().size() - eLen) + " entries.  Attempting pass #" + (pass + 1));
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
	
	
	
	protected void deleteSpool(FactoryEnumType factoryType,List<NameIdType> objects) throws FactoryException, ArgumentException{
		/// The initial bulk update logic, until this is refactored into a pure data update (which is more work than doing it this initial way) is:
		/// 1) Invoke the updateType for each object in the list
		///		This will address any factory specific tweaks that are needed, and queue up dependencies
		/// 2) Pass the entire list to the factory's updateBulk.

		INameIdFactory iFact = Factories.getBulkFactory(factoryType);
		for(int i = 0; i < objects.size();i++){
			
			NameIdType object = objects.get(i);
			iFact.delete(object);

		}
	}
	
	
	protected void updateSpool(FactoryEnumType factoryType,List<NameIdType> objects) throws FactoryException {
		/// The initial bulk update logic, until this is refactored into a pure data update (which is more work than doing it this initial way) is:
		/// 1) Invoke the updateType for each object in the list
		///		This will address any factory specific tweaks that are needed, and queue up dependencies
		/// 2) Pass the entire list to the factory's updateBulk.
		
		INameIdFactory iFact = Factories.getBulkFactory(factoryType);

		for(int i = 0; i < objects.size();i++){
			NameIdType object = objects.get(i);
			iFact.update(object);
			
		} // end for
		
	}
	
	protected void writeSpool(FactoryEnumType factoryType) throws FactoryException{
		INameIdFactory iFact = Factories.getBulkFactory(factoryType);
		iFact.writeSpool();
		
	}
	protected void writeObject(BulkSessionType session, BulkEntryType entry) throws FactoryException, DataAccessException, ArgumentException{
		writePreparedObject(session,entry);
	}
	protected void mapObjectIds(BulkEntryType entry) throws FactoryException{
		/// Note: Not all types are supported, and operations should gracefully fall through 
		///
		INameIdFactory iFact = Factories.getBulkFactory(entry.getFactoryType());
		iFact.mapBulkIds(entry.getObject());
		
	}
	protected void writePreparedObject(BulkSessionType session,BulkEntryType entry) throws FactoryException, ArgumentException {

		INameIdFactory iFact = Factories.getBulkFactory(entry.getFactoryType());
		if(iFact.isParticipation()){
			updateParticipantIds((BaseParticipantType)entry.getObject());
		}
		iFact.add(entry.getObject());
		if(entry.getFactoryType().equals(FactoryEnumType.USER)){
			UserType user = (UserType)entry.getObject();
			if(user.getContactInformation() == null){
				ContactInformationType cit = ((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).newContactInformation((UserType)entry.getObject());
				createBulkEntry(session.getSessionId(), FactoryEnumType.CONTACTINFORMATION, cit);
			}
		}
		
	}
	public long getMappedId(long temporaryId){
		long outId = 0;
		if(idMap.containsKey(temporaryId)) outId = idMap.get(temporaryId);
		return outId;
	}
	protected void updateParticipantIds(BaseParticipantType part) throws ArgumentException{
		if(part.getParticipantId() < 0L){
			if(idMap.containsKey(part.getParticipantId())){
				part.setParticipantId(idMap.get(part.getParticipantId()));
			}
			else{
				throw new ArgumentException("Unable to correct participant id");
			}
		}
		if(part.getParticipationId() < 0L){
			if(idMap.containsKey(part.getParticipationId())){
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

		NameIdFactory factory = getBulkFactory(factoryType);
		if(factory == null){
			logger.error("Invalid factory type " + factoryType);
			throw new ArgumentException("Invalid factory type " + factoryType);
		}
		if(!factory.getBulkMap().containsKey(sessionId)){
			logger.error("Bulk ID Map is out of sync for type " + factoryType);
			throw new ArgumentException("Bulk ID Map is out of sync for type " + factoryType);
			
		}
		List<Long> tmpIds = factory.getBulkMap().get(sessionId);
		factory.getBulkMap().remove(sessionId);
		return factory.getNextIds(tmpIds.size());

	}
	protected NameIdFactory getBulkFactory(FactoryEnumType factoryType) throws FactoryException{
		return Factories.getBulkFactory(factoryType);
	}
	protected NameIdFactory getFactory(FactoryEnumType factoryType) throws FactoryException{
		return Factories.getFactory(factoryType);
	}
	public void deleteBulkEntry(String sessionId, FactoryEnumType factoryType, NameIdType object) throws ArgumentException, FactoryException{
		if(object == null){
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE, factoryType.toString()));
			throw new ArgumentException(String.format(FactoryException.OBJECT_NULL_TYPE, factoryType.toString()));
		}
		if(object.getNameType() == NameEnumType.UNKNOWN){
			logger.error(FactoryException.OBJECT_UNKNOWN_TYPE);
			throw new ArgumentException(FactoryException.OBJECT_UNKNOWN_TYPE);
		}

		if(object.getId() <= 0L) throw new ArgumentException("Object " + factoryType.toString() + " " + object.getName() + " #" + object.getId() + " does not have a valid id for a delete operation");

		if(!deleteCache.containsKey(sessionId)) deleteCache.put(sessionId, new HashMap<>());
		if(!deleteCache.get(sessionId).containsKey(factoryType)) deleteCache.get(sessionId).put(factoryType, new ArrayList<>());
		if(!deleteSet.containsKey(sessionId)) deleteSet.put(sessionId, new HashMap<>());
		if(!deleteSet.get(sessionId).containsKey(factoryType)) deleteSet.get(sessionId).put(factoryType, new HashSet<>());
		
		
		NameIdFactory factory = getFactory(factoryType);
		NameIdFactory bulkFactory = getBulkFactory(factoryType);
		
		if(factory == null || bulkFactory == null){
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE, factoryType));
			throw new ArgumentException(String.format(FactoryException.OBJECT_NULL_TYPE, factoryType));
		}
		
		BulkSessionType session = sessions.get(sessionId);
		if(session == null){
			logger.error(String.format(INVALID_SESSIONID, sessionId));
			throw new ArgumentException(String.format(INVALID_SESSIONID, sessionId));
		}
		
		/// drop from factory cache
		///
		String key = factory.getCacheKeyName(object);
		factory.removeFromCache(object,key);
		if(deleteSet.get(sessionId).get(factoryType).contains(key)){

			return;
		}
		deleteCache.get(sessionId).get(factoryType).add(object);
		deleteSet.get(sessionId).get(factoryType).add(key);
	}
	public void modifyBulkEntry(String sessionId, FactoryEnumType factoryType, NameIdType object) throws ArgumentException, FactoryException{
		if(object == null){
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE, factoryType.toString()));
			throw new ArgumentException(String.format(FactoryException.OBJECT_NULL_TYPE, factoryType.toString()));
		}
		if(object.getNameType() == NameEnumType.UNKNOWN){
			logger.error(FactoryException.OBJECT_UNKNOWN_TYPE);
			throw new ArgumentException(FactoryException.OBJECT_UNKNOWN_TYPE);
		}

		if(object.getId() <= 0L) throw new ArgumentException("Object " + factoryType.toString() + " " + object.getName() + " #" + object.getId() + " does not have a valid id for an update operation");

		if(!updateCache.containsKey(sessionId)) updateCache.put(sessionId, new HashMap<>());
		if(!updateSet.containsKey(sessionId)) updateSet.put(sessionId, new HashMap<>());
		if(!updateCache.get(sessionId).containsKey(factoryType)) updateCache.get(sessionId).put(factoryType, new ArrayList<>());
		if(!updateSet.get(sessionId).containsKey(factoryType)) updateSet.get(sessionId).put(factoryType, new HashSet<>());
		
		
		NameIdFactory factory = getFactory(factoryType);
		NameIdFactory bulkFactory = getBulkFactory(factoryType);
		
		if(factory == null || bulkFactory == null){
			logger.error("Factory or BulkFactory is null for type " + factoryType);
			throw new ArgumentException("Factory or BulkFactory is null for type " + factoryType);
		}
		
		BulkSessionType session = sessions.get(sessionId);
		if(session == null){
			logger.error(String.format(INVALID_SESSIONID, sessionId));
			throw new ArgumentException(String.format(INVALID_SESSIONID, sessionId));
		}
		
		/// rewrite factory cache in case the name was changed on the update
		///
		String key = factory.getCacheKeyName(object);
		if(!factory.updateToCache(object,key)){
			logger.warn("Failed to add object '" + object.getNameType().toString() + " " + object.getObjectId() + "' to " + factoryType.toString() + " factory cache with key name " + factory.getCacheKeyName(object));
		}
		
		if(updateSet.get(sessionId).get(factoryType).contains(key)){
			logger.warn(factoryType.toString() + " update set for session " + sessionId + " already includes " + key);
			return;
		}

		/// Add the object to the updateCache
		updateCache.get(sessionId).get(factoryType).add(object);
		updateSet.get(sessionId).get(factoryType).add(key);
	}
	public void createBulkEntry(String inSessionId, FactoryEnumType factoryType, NameIdType object) throws ArgumentException, FactoryException{
		long bulkId = (long)(rand.nextDouble()*1000000000L) * -1;
		String sessionId = inSessionId;
		
		if(sessionId == null){
			synchronized(globalLock){
				if(globalSessionId != null){
					sessionId = globalSessionId;
				}
				else{
					logger.info("Creating new global session.");
					globalSessionId = newBulkSession();
					sessionId = globalSessionId;
				}
			}
		}
		
		/// With large datasets, the random quickly starts to repeat.  Very bad Java.  Very bad.
		while(sessionIdMap.containsKey(bulkId)){
			bulkId = (long)(rand.nextDouble()*1000000000L) * -1;
		}
		
		if(object == null){
			logger.error(String.format(FactoryException.OBJECT_NULL_TYPE, factoryType.toString()));
			throw new ArgumentException(String.format(FactoryException.OBJECT_NULL_TYPE, factoryType.toString()));
		}

		
		if(sessionIdMap.containsKey(bulkId)){
			logger.error("Random id " + bulkId + " assigned to " + object.getName() + " already consumed");
			throw new ArgumentException("Random id " + bulkId + " assigned to " + object.getName() + " already consumed");			
		}
		
		if(object.getId() != 0L){
			logger.error("Object id is already set to " + object.getObjectId());
			throw new ArgumentException("Object id is already set");
		}
		if(object.getNameType() == NameEnumType.UNKNOWN){
			logger.error(FactoryException.OBJECT_UNKNOWN_TYPE);
			throw new ArgumentException(FactoryException.OBJECT_UNKNOWN_TYPE);
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
		
		object.setId(bulkId);
		/// 2015/06/25 - Assign object id for factories with the bit set to true
		/// This is otherwise done last minute, but thwarts foreign key references
		/// primarily between credentials, controls, and keys
		///
		if(factory.hasObjectId) object.setObjectId(UUID.randomUUID().toString());
		
		BulkEntryType entry = new BulkEntryType();
		entry.setFactoryType(factoryType);
		entry.setPersistentId(0L);
		entry.setTemporaryId(bulkId);
		entry.setObject(object);
		session.getBulkEntries().add(entry);
		
		bulkFactory.addBulkId(sessionId, bulkId);
		
		/// 2016/07/22 - Some objects do not have a name, such as participations
		///
		if(object.getName() != null && !factory.updateToCache(object,factory.getCacheKeyName(object))){
			logger.warn("Failed to add object '" + object.getName() + "' to factory cache with key name " + factory.getCacheKeyName(object));
		}
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
		logger.debug("Created Bulk Session '" + sessionId + "'.  Expires: " + sess.getSessionExpires().toString());
		return sessionId;
	}
	
}
