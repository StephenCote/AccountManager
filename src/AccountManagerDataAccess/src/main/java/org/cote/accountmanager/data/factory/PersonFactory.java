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
package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.lang3.ArrayUtils;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.BulkFactories;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserParticipantType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.accountmanager.util.CalendarUtil;


public class PersonFactory extends NameIdGroupFactory {
	static{
		//registerProvider();
	}
	@Override
	public void registerProvider(){
		AuthorizationService.registerAuthorizationProviders(
				FactoryEnumType.PERSON,
				NameEnumType.PERSON,
				Factories.getPersonParticipationFactory()
			);	
	}
	public PersonFactory(){
		super();
		this.hasParentId=true;
		this.hasUrn = true;
		this.hasObjectId = true;
		this.tableNames.add("persons");
		factoryType = FactoryEnumType.PERSON;
	}
	
	@Override
	public <T> String getCacheKeyName(T obj){
		NameIdDirectoryGroupType t = (NameIdDirectoryGroupType)obj;
		return t.getName() + "-" + t.getParentId() + "-" + t.getGroupId();
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("persons")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	@Override
	public<T> void depopulate(T obj) throws FactoryException, ArgumentException
	{
		
	}
	@Override
	public <T> void populate(T obj) throws FactoryException, ArgumentException
	{
		PersonType person = (PersonType)obj;
		if(person.getPopulated() == true) return;
		person.getPartners().addAll(Factories.getPersonParticipationFactory().getPartnersFromParticipation(person));
		//logger.info("Populated " + person.getPartners().size() + " partners");
		person.getDependents().addAll(Factories.getPersonParticipationFactory().getDependentsFromParticipation(person));
		person.getNotes().addAll(Factories.getPersonParticipationFactory().getDatasFromParticipation(person));
		person.getAccounts().addAll(Factories.getPersonParticipationFactory().getAccountsFromParticipation(person));
		person.getUsers().addAll(Factories.getPersonParticipationFactory().getUsersFromParticipation(person));
		if(person.getContactInformation() != null) Factories.getContactInformationFactory().populate(person.getContactInformation());
		person.setPopulated(true);
		
		updateToCache(person);
	}
	public PersonType newPerson(UserType user, PersonType parentPerson) throws ArgumentException
	{
		if (user == null || user.getDatabaseRecord() == false) throw new ArgumentException("Invalid owner");
		
		PersonType obj = newPerson(user,parentPerson.getGroupId());
		obj.setParentId(parentPerson.getId());
		return obj;
	}
	public PersonType newPerson(UserType user, long groupId) throws ArgumentException
	{
		if (user == null || user.getId().equals(0L)) throw new ArgumentException("Invalid owner");
		PersonType obj = new PersonType();
		obj.setAlias("");
		obj.setDescription("");
		obj.setFirstName("");
		obj.setGender("");
		obj.setLastName("");
		obj.setMiddleName("");
		obj.setPrefix("");
		obj.setSuffix("");
		obj.setTitle("");
		obj.setOrganizationId(user.getOrganizationId());
		obj.setOwnerId(user.getId());
		obj.setGroupId(groupId);
		obj.setNameType(NameEnumType.PERSON);
		Calendar now = Calendar.getInstance();
		now.setTimeInMillis(0);
		XMLGregorianCalendar cal = CalendarUtil.getXmlGregorianCalendar(now.getTime()); 
		obj.setBirthDate(cal);
		
		return obj;
	}
	
	public boolean addPerson(PersonType obj) throws FactoryException
	{

		if (obj.getGroupId().compareTo(0L) == 0) throw new FactoryException("Cannot add new Person without a group");

		DataRow row = prepareAdd(obj, "persons");


		try{
			row.setCellValue("birthdate",obj.getBirthDate());
			row.setCellValue("description",obj.getDescription());
			row.setCellValue("gender",obj.getGender());
			row.setCellValue("firstname",obj.getFirstName());
			row.setCellValue("middlename",obj.getMiddleName());
			row.setCellValue("lastname",obj.getLastName());
			row.setCellValue("alias",obj.getAlias());
			row.setCellValue("prefix",obj.getPrefix());
			row.setCellValue("suffix",obj.getSuffix());
			row.setCellValue("title",obj.getTitle());
			
			/// TODO: Deprecate contactinformationid field
			///
			row.setCellValue("contactinformationid", (obj.getContactInformation() != null ? obj.getContactInformation().getId() : 0));
			
			row.setCellValue("groupid", obj.getGroupId());
			
			if (insertRow(row)){
				PersonType cobj = (bulkMode ? obj : (PersonType)getByNameInGroup(obj.getName(), obj.getGroupId(),obj.getOrganizationId()));
				if(cobj == null) throw new FactoryException("Failed to retrieve new user cobject");
				if(bulkMode){
					if(cobj.getContactInformation() == null){
						//logger.info("Provisioning contact information for bulk person id " + cobj.getId());
						ContactInformationType cinfo = Factories.getContactInformationFactory().newContactInformation(cobj);
						cinfo.setOwnerId(cobj.getOwnerId());
						cobj.setContactInformation(cinfo);
						BulkFactories.getBulkContactInformationFactory().addContactInformation(cobj.getContactInformation());
						BulkFactories.getBulkFactory().setDirty(FactoryEnumType.CONTACTINFORMATION);
					}

				}
				else{
					if(cobj.getContactInformation() == null){
						ContactInformationType cinfo = Factories.getContactInformationFactory().newContactInformation(cobj);
						cinfo.setOwnerId(cobj.getOwnerId());
						logger.debug("Adding cinfo for person in org " + cobj.getOrganizationId());
						cobj.setContactInformation(cinfo);
					}
					
					/// 2013/09/05 - ContactInformation relationship is inverted from the original User<->ContactInformation 
					/// And trying to keep the foreign key on the person means it winds up with two references: Contact Id on the Person, and Person Id on the Contact
					/// At the moment it's being automatically added/created when the person is created
					///
					if(Factories.getContactInformationFactory().addContactInformation(cobj.getContactInformation()) == false) throw new FactoryException("Failed to assign contact information for user #" + cobj.getId());
					cobj.setContactInformation(Factories.getContactInformationFactory().getContactInformationForPerson(cobj));
					if(cobj.getContactInformation() == null) throw new FactoryException("Failed to retrieve contact information for user #" + cobj.getId());
					if(updatePerson(cobj) == false) throw new FactoryException("Failed to update person cobject");
					
					/// TODO: Deprecate the contactinformationid field, which makes this unnecessary
					///

				}
				
				BaseParticipantType part = null;

				for(int i = 0; i < obj.getPartners().size();i++){
					part = Factories.getPersonParticipationFactory().newPartnerPersonParticipation(cobj,obj.getPartners().get(i));
					if(bulkMode) BulkFactories.getBulkPersonParticipationFactory().addParticipant(part);
					else Factories.getPersonParticipationFactory().addParticipant(part);
				}
				for(int i = 0; i < obj.getDependents().size();i++){
					part = Factories.getPersonParticipationFactory().newDependentPersonParticipation(cobj,obj.getDependents().get(i));
					if(bulkMode) BulkFactories.getBulkPersonParticipationFactory().addParticipant(part);
					else Factories.getPersonParticipationFactory().addParticipant(part);
				}
				for(int i = 0; i < obj.getNotes().size();i++){
					part = Factories.getPersonParticipationFactory().newDataPersonParticipation(cobj,obj.getNotes().get(i));
					if(bulkMode) BulkFactories.getBulkPersonParticipationFactory().addParticipant(part);
					else Factories.getPersonParticipationFactory().addParticipant(part);
				}
				for(int i = 0; i < obj.getAccounts().size();i++){
					part = Factories.getPersonParticipationFactory().newAccountPersonParticipation(cobj,obj.getAccounts().get(i));
					if(bulkMode) BulkFactories.getBulkPersonParticipationFactory().addParticipant(part);
					else Factories.getPersonParticipationFactory().addParticipant(part);
				}
				for(int i = 0; i < obj.getUsers().size();i++){
					part = Factories.getPersonParticipationFactory().newUserPersonParticipation(cobj,obj.getUsers().get(i));
					if(bulkMode) BulkFactories.getBulkPersonParticipationFactory().addParticipant(part);
					else Factories.getPersonParticipationFactory().addParticipant(part);
				}
				return true;
			}
		}
		catch(DataAccessException dae){
			throw new FactoryException(dae.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return false;
	}
	
	

	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException,ArgumentException
	{
		PersonType new_obj = new PersonType();
		new_obj.setNameType(NameEnumType.PERSON);
		super.read(rset, new_obj);
		readGroup(rset, new_obj);
		new_obj.setContactInformation(Factories.getContactInformationFactory().getContactInformationForPerson(new_obj));
		
		new_obj.setBirthDate(CalendarUtil.getXmlGregorianCalendar(rset.getTimestamp("birthdate")));
		new_obj.setDescription(rset.getString("description"));
		new_obj.setFirstName(rset.getString("firstname"));
		new_obj.setGender(rset.getString("gender"));
		new_obj.setMiddleName(rset.getString("middlename"));
		new_obj.setAlias(rset.getString("alias"));
		new_obj.setPrefix(rset.getString("prefix"));
		new_obj.setSuffix(rset.getString("suffix"));
		new_obj.setLastName(rset.getString("lastname"));
		new_obj.setTitle(rset.getString("title"));
		
		return new_obj;
	}
	public boolean updatePerson(PersonType data) throws FactoryException, DataAccessException
	{	
		boolean out_bool = false;
		removeFromCache(data);
		if(update(data, null)){
			try{
				/// Partners
				///
				BaseParticipantType part = null;
				List<Long> delIds = new ArrayList<Long>();
				if(bulkMode) BulkFactories.getBulkFactory().setDirty(FactoryEnumType.PERSONPARTICIPATION);
				Set<Long> set = new HashSet<Long>();
				BaseParticipantType[] maps = Factories.getPersonParticipationFactory().getPartnerParticipations(data).toArray(new BaseParticipantType[0]);
				//logger.info("Updating " + maps.length + " Partner References");
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getPartners().size();i++){
					if(set.contains(data.getPartners().get(i).getId())== false){
						part = Factories.getPersonParticipationFactory().newPartnerPersonParticipation(data,data.getPartners().get(i));
						if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.PERSONPARTICIPATION, part);
						else Factories.getPersonParticipationFactory().addParticipant(part);
					}
					else{
						set.remove(data.getPartners().get(i).getId());
					}
				}
				//Factories.getPersonParticipationFactory().deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
				/// Dependents
				///
				set = new HashSet<Long>();
				maps = Factories.getPersonParticipationFactory().getDependentParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getDependents().size();i++){
					if(set.contains(data.getDependents().get(i).getId())== false){
						part = Factories.getPersonParticipationFactory().newDependentPersonParticipation(data,data.getDependents().get(i));
						if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.PERSONPARTICIPATION, part);
						else Factories.getPersonParticipationFactory().addParticipant(part);
					}
					else{
						set.remove(data.getDependents().get(i).getId());
					}
				}
				//Factories.getPersonParticipationFactory().deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
				
				/// Datas
				///
				set = new HashSet<Long>();
				maps = Factories.getPersonParticipationFactory().getDataParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getNotes().size();i++){
					if(set.contains(data.getNotes().get(i).getId())== false){
						part = Factories.getPersonParticipationFactory().newDataPersonParticipation(data,data.getNotes().get(i));
						if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.PERSONPARTICIPATION, part);
						else Factories.getPersonParticipationFactory().addParticipant(part);
					}
					else{
						set.remove(data.getNotes().get(i).getId());
					}
				}
				//Factories.getPersonParticipationFactory().deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
				
				/// Accounts
				///
				set = new HashSet<Long>();
				maps = Factories.getPersonParticipationFactory().getAccountParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getAccounts().size();i++){
					if(set.contains(data.getAccounts().get(i).getId())== false){
						part = Factories.getPersonParticipationFactory().newAccountPersonParticipation(data,data.getAccounts().get(i));
						if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.PERSONPARTICIPATION, part);
						else Factories.getPersonParticipationFactory().addParticipant(part);
					}
					else{
						set.remove(data.getAccounts().get(i).getId());
					}
				}
				//Factories.getPersonParticipationFactory().deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
				
				/// Users
				///
				set = new HashSet<Long>();
				maps = Factories.getPersonParticipationFactory().getUserParticipations(data).toArray(new BaseParticipantType[0]);
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < data.getUsers().size();i++){
					if(set.contains(data.getUsers().get(i).getId())== false){
						part = Factories.getPersonParticipationFactory().newUserPersonParticipation(data,data.getUsers().get(i));
						if(bulkMode) BulkFactories.getBulkFactory().createBulkEntry(null, FactoryEnumType.PERSONPARTICIPATION, part);
						else Factories.getPersonParticipationFactory().addParticipant(part);
					}
					else{
						set.remove(data.getUsers().get(i).getId());
					}
				}
				//Factories.getPersonParticipationFactory().deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), data, data.getOrganizationId());
				delIds.addAll(Arrays.asList(set.toArray(new Long[0])));
				if(delIds.size() > 0) Factories.getPersonParticipationFactory().deleteParticipantsForParticipation(ArrayUtils.toPrimitive(delIds.toArray(new Long[0])), data, data.getOrganizationId());
				/// 2014/09/10
				/// Contact information is updated along with the parent object because it's a foreign-keyed object that is not otherwise easily referenced
				///
				out_bool = Factories.getContactInformationFactory().updateContactInformation(data.getContactInformation());
			
			}
			catch(ArgumentException ae){
				throw new FactoryException(ae.getMessage());
			}
		}
		return out_bool;
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		PersonType use_map = (PersonType)map;
		fields.add(QueryFields.getFieldContactInformationId(use_map.getContactInformation()));
		fields.add(QueryFields.getFieldGroup(use_map.getGroupId()));
		fields.add(QueryFields.getFieldBirthDate(use_map.getBirthDate()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
		fields.add(QueryFields.getFieldFirstName(use_map.getFirstName()));
		fields.add(QueryFields.getFieldLastName(use_map.getLastName()));
		fields.add(QueryFields.getFieldGender(use_map.getGender()));
		fields.add(QueryFields.getFieldMiddleName(use_map.getMiddleName()));
		fields.add(QueryFields.getFieldAlias(use_map.getAlias()));
		fields.add(QueryFields.getFieldPrefix(use_map.getPrefix()));
		fields.add(QueryFields.getFieldSuffix(use_map.getSuffix()));
		fields.add(QueryFields.getFieldTitle(use_map.getTitle()));
	}
	public int deletePersonsByUser(UserType user) throws FactoryException
	{
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldOwner(user.getId()) }, user.getOrganizationId());
		return deletePersonsByIds(ids, user.getOrganizationId());
	}

	public boolean deletePerson(PersonType obj) throws FactoryException
	{
		removeFromCache(obj);
		if(bulkMode) return true;
		
		int deleted = deleteById(obj.getId(), obj.getOrganizationId());
		return (deleted > 0);
	}
	public int deletePersonsByIds(long[] ids, long organizationId) throws FactoryException
	{
		int deleted = deleteById(ids, organizationId);
		if (deleted > 0)
		{
			Factories.getContactInformationFactory().deleteContactInformationByReferenceIds(ids,organizationId);
			Factories.getPersonParticipationFactory().deleteParticipations(ids, organizationId);
			/*
			Factory.DataParticipationFactoryInstance.DeleteParticipations(ids, organizationId);
			Factory.TagParticipationFactoryInstance.DeleteParticipants(ids, organizationId);
			*/
		}
		return deleted;
	}
	public int deletePersonsInGroup(DirectoryGroupType group)  throws FactoryException
	{
		// Can't just delete by group;
		// Need to get ids so as to delete participations as well
		//
		long[] ids = getIdByField(new QueryField[] { QueryFields.getFieldGroup(group.getId()) }, group.getOrganizationId());
		/// TODO: Delete participations
		///
		return deletePersonsByIds(ids, group.getOrganizationId());
	}
	
	public List<PersonType> getChildPersonList(PersonType parent) throws FactoryException,ArgumentException{

		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldParent(parent.getId()));
		fields.add(QueryFields.getFieldGroup(parent.getGroupId()));
		return getPersonList(fields.toArray(new QueryField[0]), 0,0,parent.getOrganizationId());

	}
	public List<PersonType>  getPersonList(QueryField[] fields, long startRecord, int recordCount, long organizationId)  throws FactoryException,ArgumentException
	{
		return getPaginatedList(fields, startRecord, recordCount, organizationId);
	}
	public List<PersonType> getPersonListByIds(long[] ids, long organizationId) throws FactoryException,ArgumentException
	{
		return getListByIds(ids, organizationId);
	}
	
	public PersonType getPersonByUser(UserType user) throws FactoryException, ArgumentException{
		return getPersonByUserId(user.getId(), user.getOrganizationId());
	}
	public PersonType getPersonByUserId(long userId, long organizationId) throws FactoryException, ArgumentException{
		PersonType person = null;
		DirectoryGroupType dir = Factories.getGroupFactory().getPersonsDirectory(organizationId);
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldGroup(dir.getId()));
		fields.add(QueryFields.getFieldUserId(userId));
		List<PersonType> persons = searchByIdInView("personusers", fields.toArray(new QueryField[0]),null,organizationId);
		if(persons.size() == 1){
			person = persons.get(0);
		}
		else if(persons.size() > 1){
			logger.warn("Unexpected number of person matches: " + persons.size() + ".  Not returning any value.");
		}
		return person;
	}
	/*
	@Override
	public <T> List<T> search(String searchValue, long startRecord, int recordCount, DirectoryGroupType dir) throws FactoryException{
	
		ProcessingInstructionType instruction = null;
		if(startRecord >= 0 && recordCount >= 0){
			instruction = new ProcessingInstructionType();
			instruction.setOrderClause("name ASC");
			instruction.setPaginate(true);
			instruction.setStartIndex(startRecord);
			instruction.setRecordCount(recordCount);
		}
		
		List<QueryField> fields = buildSearchQuery(searchValue, dir.getOrganizationId());
		fields.add(QueryFields.getFieldGroup(dir.getId()));
		return search(fields.toArray(new QueryField[0]), instruction, dir.getOrganizationId());
	}
	*/
	
	/// Person search uses a different query to join in contact information
	/// Otherwise, this could be the getPaginatedList method
	///
	/// public List<PersonType> search(QueryField[] filters, long organizationId){
	@Override
	public List<QueryField> buildSearchQuery(String searchValue, long organizationId) throws FactoryException{
		
		searchValue = searchValue.replaceAll("\\*","%");
		
		List<QueryField> filters = new ArrayList<QueryField>();
		QueryField search_filters = new QueryField(SqlDataEnumType.NULL,"searchgroup",null);
		search_filters.setComparator(ComparatorEnumType.GROUP_OR);
		QueryField name_filter = new QueryField(SqlDataEnumType.VARCHAR,"name",searchValue);
		name_filter.setComparator(ComparatorEnumType.LIKE);
		search_filters.getFields().add(name_filter);
		QueryField first_name_filter = new QueryField(SqlDataEnumType.VARCHAR,"firstname",searchValue);
		first_name_filter.setComparator(ComparatorEnumType.LIKE);
		search_filters.getFields().add(first_name_filter);
		filters.add(search_filters);
		return filters;
	}
	
	@Override
	public <T> List<T> search(QueryField[] filters, ProcessingInstructionType instruction, long organizationId){
		return searchByIdInView("personContact", filters,instruction,organizationId);

/*
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String sqlQuery = assembleQueryString("SELECT id FROM personContact", filters, connectionType, instruction, organization.getId());
		logger.info("Query=" + sqlQuery);
		List<Long> ids = new ArrayList<Long>();
		List<T> persons = new ArrayList<T>();
		
		try{
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(filters, statement);
			ResultSet rset = statement.executeQuery();
			while(rset.next()){
				ids.add(rset.getLong("id"));
			}
			rset.close();
			
			/// don't paginate the subsequent search for ids because it was already filtered.
			/// Create a new instruction and just copy the order clause
			///
			ProcessingInstructionType pi2 = new ProcessingInstructionType();
			pi2.setOrderClause(instruction.getOrderClause());
			persons = getListByIds(ArrayUtils.toPrimitive(ids.toArray(new Long[0])),pi2,organizationId);
			logger.info("Retrieved " + persons.size() + " from " + ids.size() + " ids");
		}
		catch(SQLException sqe){
			logger.error(sqe.getMessage());
			sqe.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		finally{
			
			try {
				connection.close();
			} catch (SQLException e) {
				logger.error(e.getMessage());
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		//return search(fields, instruction, organizationId);
		return persons;
*/
	}
	
	public UserType getUserPerson(PersonType person){
		UserType user = null;
		try{
			UserParticipantType[] parts = Factories.getPersonParticipationFactory().getUserParticipations(person).toArray(new UserParticipantType[0]);
			if(parts.length > 0){
				DirectoryGroupType pdir = Factories.getGroupFactory().getDirectoryByName("Persons",Factories.getGroupFactory().getRootDirectory(person.getOrganizationId()),person.getOrganizationId());
				if(pdir == null){
					logger.warn("Root Persons directory not found");
					return null;
				}
				QueryField field = QueryFields.getFieldParticipantIds(parts);
				QueryField group = QueryFields.getFieldGroup(pdir.getId());
				List<UserType> users =  Factories.getUserFactory().getUserList(new QueryField[]{ field,group }, 0, 0, person.getOrganizationId());
				if(users.size() == 1){
					user = users.get(0);
				}
				else{
					logger.warn("Found zero or more than one match for the user's person object");
					
				}
				//items = getUsersFromParticipations(parts, 0, 0, participation.getOrganizationId());
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return user;
	}


	
}
