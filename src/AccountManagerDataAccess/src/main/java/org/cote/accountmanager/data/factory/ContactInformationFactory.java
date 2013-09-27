package org.cote.accountmanager.data.factory;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseParticipantType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ContactInformationEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class ContactInformationFactory extends NameIdFactory {
	
	
	public ContactInformationFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasName = false;
		this.tableNames.add("contactinformation");
		this.factoryType = FactoryEnumType.CONTACTINFORMATION;
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("groups")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	public void populate(ContactInformationType obj) throws FactoryException,ArgumentException{
		if(obj.getPopulated() == true || obj.getId().equals(0L)) return;
		obj.getContacts().addAll(Factories.getContactInformationParticipationFactory().getContactsFromParticipation(obj));
		obj.getAddresses().addAll(Factories.getContactInformationParticipationFactory().getAddressesFromParticipation(obj));
		obj.setPopulated(true);
		
		updateToCache(obj);
	}
	@Override
	public <T> String getCacheKeyName(T obj){
		ContactInformationType t = (ContactInformationType)obj;
		return t.getReferenceId() + "-" + t.getContactInformationType().toString() + "-" + t.getOrganization().getId();
	}

	public boolean deleteContactInformationByReferenceType(NameIdType map) throws FactoryException
	{
		return deleteContactInformationByReferenceIds(new long[]{map.getId()},map.getOrganization().getId());
	}
	public boolean deleteContactInformationByReferenceIds(long[] ids, long organizationId){
		int deleted = deleteByBigIntField("referenceid",ids,organizationId);
		return (deleted > 0);
	}
	public boolean deleteContactInformation(ContactInformationType cinfo) throws FactoryException
	{
		int deleted = deleteById(cinfo.getId(), cinfo.getOrganization().getId());
		if(deleted > 0){
			Factories.getContactInformationParticipationFactory().deleteParticipations(new long[]{cinfo.getId()},cinfo.getOrganization());
		}
		return (deleted > 0);
	}
	public boolean updateContactInformation(ContactInformationType cinfo) throws FactoryException, DataAccessException
	{
		boolean out_bool = false;
		removeFromCache(cinfo);
		if(update(cinfo)){
			try{
				/// Contacts
				///
			
				Set<Long> set = new HashSet<Long>();
				BaseParticipantType[] maps = Factories.getContactInformationParticipationFactory().getContactParticipations(cinfo).toArray(new BaseParticipantType[0]);
				logger.info("Updating " + maps.length + " Contact References");
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < cinfo.getContacts().size();i++){
					if(set.contains(cinfo.getContacts().get(i).getId())== false){
						Factories.getContactInformationParticipationFactory().addParticipant(Factories.getContactInformationParticipationFactory().newContactParticipation(cinfo,cinfo.getContacts().get(i)));
					}
					else{
						set.remove(cinfo.getContacts().get(i).getId());
					}
				}
				Factories.getContactInformationParticipationFactory().deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), cinfo, cinfo.getOrganization());
				
				set.clear();
				maps = Factories.getContactInformationParticipationFactory().getAddressParticipations(cinfo).toArray(new BaseParticipantType[0]);
				logger.info("Updating " + maps.length + " Address References");
				for(int i = 0; i < maps.length;i++) set.add(maps[i].getParticipantId());
				
				for(int i = 0; i < cinfo.getAddresses().size();i++){
					if(set.contains(cinfo.getAddresses().get(i).getId())== false){
						Factories.getContactInformationParticipationFactory().addParticipant(Factories.getContactInformationParticipationFactory().newAddressParticipation(cinfo,cinfo.getAddresses().get(i)));
					}
					else{
						set.remove(cinfo.getAddresses().get(i).getId());
					}
				}
				Factories.getContactInformationParticipationFactory().deleteParticipantsForParticipation(ArrayUtils.toPrimitive(set.toArray(new Long[0])), cinfo, cinfo.getOrganization());
				
				out_bool = true;
			}
			catch(ArgumentException ae){
				throw new FactoryException(ae.getMessage());
			}

		}
		return out_bool;
	}
	
	@Override
	public void mapBulkIds(NameIdType map){
		super.mapBulkIds(map);
		ContactInformationType cit = (ContactInformationType)map;
		long tmpId = 0;
		if(cit.getReferenceId() < 0){
			tmpId = BulkFactories.getBulkFactory().getMappedId(cit.getReferenceId());
			if(tmpId > 0) cit.setReferenceId(tmpId);
		}
	}
	public boolean addContactInformation(ContactInformationType new_info) throws FactoryException
	{
		if (new_info.getReferenceId().equals(0L)) throw new FactoryException("Cannot add information without a corresponding reference id");
		if (new_info.getOrganization() == null || new_info.getOrganization().getId() <= 0) throw new FactoryException("Cannot add contact information to invalid organization");
		if(new_info.getOwnerId().equals(0L)) throw new FactoryException("Contact Information must now have an owner id");
		DataRow row = prepareAdd(new_info, "contactinformation");
		try{
			row.setCellValue("referenceid",new_info.getReferenceId());
			row.setCellValue("contactinformationtype", new_info.getContactInformationType().toString());
			row.setCellValue("description",new_info.getDescription());
			if (insertRow(row)){
				ContactInformationType cobj = (bulkMode ? new_info : (ContactInformationType)getContactInformationByReferenceId(new_info.getReferenceId(),new_info.getContactInformationType(),new_info.getOrganization()));
				if(cobj == null) throw new FactoryException("Failed to retrieve new user cobject");

				BulkFactories.getBulkFactory().setDirty(factoryType);
				BaseParticipantType part = null;

				for(int i = 0; i < new_info.getContacts().size();i++){
					part = Factories.getContactInformationParticipationFactory().newContactParticipation(cobj,new_info.getContacts().get(i));
					if(bulkMode) BulkFactories.getBulkContactInformationParticipationFactory().addParticipant(part);
					else Factories.getContactInformationParticipationFactory().addParticipant(part);
				}
				
				for(int i = 0; i < new_info.getAddresses().size();i++){
					part = Factories.getContactInformationParticipationFactory().newAddressParticipation(cobj,new_info.getAddresses().get(i));
					if(bulkMode) BulkFactories.getBulkContactInformationParticipationFactory().addParticipant(part);
					else Factories.getContactInformationParticipationFactory().addParticipant(part);
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
	
	public ContactInformationType getContactInformationForPerson(PersonType map) throws FactoryException, ArgumentException
	{
		return getContactInformationByReferenceId(map.getId(), ContactInformationEnumType.PERSON, map.getOrganization());
	}
	public ContactInformationType getContactInformationForUser(UserType map) throws FactoryException, ArgumentException
	{
		return getContactInformationByReferenceId(map.getId(), ContactInformationEnumType.USER, map.getOrganization());
	}
	public ContactInformationType getContactInformationForAccount(AccountType map) throws FactoryException, ArgumentException
	{
		return getContactInformationByReferenceId(map.getId(), ContactInformationEnumType.ACCOUNT, map.getOrganization());
	}
	public ContactInformationType getContactInformationByReferenceId(long reference_id, ContactInformationEnumType type, OrganizationType organization) throws FactoryException, ArgumentException
	{
		List<NameIdType> cinfo = getByField(new QueryField[]{QueryFields.getFieldReferenceId(reference_id),QueryFields.getFieldContactInformationType(type)},organization.getId());
		if (cinfo.size() > 0) return (ContactInformationType)cinfo.get(0);
		return null;
	}

	
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		ContactInformationType use_map = (ContactInformationType)map;
		fields.add(QueryFields.getFieldReferenceId(use_map.getReferenceId()));
		fields.add(QueryFields.getFieldContactInformationType(use_map.getContactInformationType()));
		fields.add(QueryFields.getFieldDescription(use_map.getDescription()));
	}
	public ContactInformationType newContactInformation(PersonType map)
	{
		return newContactInformation(map,ContactInformationEnumType.PERSON);
	}
	public ContactInformationType newContactInformation(AccountType map)
	{
		return newContactInformation(map,ContactInformationEnumType.ACCOUNT);
	}	
	public ContactInformationType newContactInformation(UserType map)
	{
		ContactInformationType cit = newContactInformation(map,ContactInformationEnumType.USER);
		cit.setOwnerId(map.getId());
		return cit;
	}
	protected ContactInformationType newContactInformation(NameIdType map, ContactInformationEnumType type)
	{
		ContactInformationType cinfo = new ContactInformationType();
		cinfo.setReferenceId(map.getId());
		cinfo.setOrganization(map.getOrganization());
		cinfo.setContactInformationType(type);
		cinfo.setNameType(NameEnumType.CONTACTINFORMATION);
		return cinfo;
	}
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		ContactInformationType cinfo = new ContactInformationType();
		cinfo.setContactInformationType(ContactInformationEnumType.valueOf(rset.getString("contactinformationtype")));
		cinfo.setReferenceId(rset.getLong("referenceid"));
		cinfo.setDescription(rset.getString("description"));
		cinfo.setNameType(NameEnumType.CONTACTINFORMATION);
		return super.read(rset, cinfo);
	}


}
