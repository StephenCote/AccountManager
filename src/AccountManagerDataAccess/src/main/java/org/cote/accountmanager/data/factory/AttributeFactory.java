package org.cote.accountmanager.data.factory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.AttributeType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;


public class AttributeFactory extends NameIdFactory{
	public static final Logger logger = Logger.getLogger(AttributeFactory.class.getName());
	private int maximumInsBatch = 2000;
	public AttributeFactory(){
		super();
		this.scopeToOrganization = true;
		this.hasParentId = false;
		this.hasOwnerId = false;
		this.hasName = true;
		this.tableNames.add("attribute");
		this.bulkMode = false;
	}
	
	public AttributeType newAttribute(NameIdType obj, String name, String val){
		AttributeType attr = newAttribute(obj);
		attr.setName(name);
		attr.getValues().add(val);
		return attr;
	}
	public AttributeType newAttribute(NameIdType obj){
		AttributeType attr = new AttributeType();
		/// Note: Reference id will be 0L for new objects, and < 0L for bulk objects
		///
		attr.setReferenceId(obj.getId());
		attr.setDataType(SqlDataEnumType.VARCHAR);
		attr.setReferenceType(obj.getNameType());
		attr.setOrganizationId(obj.getOrganization().getId());
		return attr;
	}
	public void repopulateAttributes(NameIdType obj){
		obj.setAttributesPopulated(false);
		populateAttributes(obj);
	}
	public void populateAttributes(NameIdType obj){
		if(obj.getAttributesPopulated()) return;
		try {
			obj.getAttributes().clear();
			obj.getAttributes().addAll(getAttributes(obj));
			obj.setAttributesPopulated(true);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public boolean hasAttribute(NameIdType obj, String name, String value){
		return hasAttribute(obj.getAttributes(), name, value);
	}
	public boolean hasAttribute(List<AttributeType> attrs, String name, String value){
		boolean out_bool = false;
		AttributeType comp = null;
		for(int i = 0; i < attrs.size();i++){
			comp = attrs.get(i);
			if(comp.getName().equals(name)){
				for(int v = 0; v < comp.getValues().size();v++){
					if(comp.getValues().get(v).equals(value)){
						out_bool = true;
						break;
					}
				}
			}
		}
		return out_bool;
	}
	public String getAttributeValueByName(NameIdType obj, String name){
		return getAttributeValueByName(obj.getAttributes(), name);
	}
	public String getAttributeValueByName(List<AttributeType> attrs, String name){
		AttributeType attr = getAttributeByName(attrs, name);
		if(attr == null || attr.getValues().size() == 0) return null;
		return attr.getValues().get(0);
	}

	public AttributeType getAttributeByName(NameIdType obj, String name){
		return getAttributeByName(obj.getAttributes(), name);
	}
	public AttributeType getAttributeByName(List<AttributeType> attrs, String name){
		AttributeType attr = null;
		for(int i = 0; i < attrs.size();i++){
			if(attrs.get(i).getName().equals(name)){
				attr = attrs.get(i);
				break;
			}
		}
		return attr;
	}
	public boolean updateAttributes(NameIdType obj){
		return updateAttributes(new NameIdType[]{obj});
	}
	public boolean updateAttributes(NameIdType[] obj){
		/// Todo: Add bulk support for delete
		///
		for(int i = 0; i < obj.length;i++){
			deleteAttributes(obj[0],true);
		}
		addAttributes(obj);
		return true;
	}
	public boolean addAttributes(NameIdType obj){
		return addAttributes(new NameIdType[]{obj});
		//DataTable table = getDataTable("attribute");
	}

	public boolean addAttributes(NameIdType[] objs){
		boolean out_bool = false;
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connection_type = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(connection_type);

		String sql = "INSERT INTO attribute (referenceid, referencetype, name, datatype, valueindex, value, organizationid) VALUES (" + token + ", " + token + ", " + token + ", " + token + ", " + token + ", " + token + ", " + token + ");";

		try{
			PreparedStatement psa = connection.prepareStatement(sql);
			int aiter = 0;
			for(int o = 0; o < objs.length;o++){
				NameIdType obj = objs[o];
				for(int a = 0; a < obj.getAttributes().size();a++){
					AttributeType attr = obj.getAttributes().get(a);
					for(int v = 0; v < attr.getValues().size();v++){
						/// logger.info("Adding attribute " + attr.getName());
						psa.setLong(1, obj.getId());
						psa.setString(2, obj.getNameType().toString());
						psa.setString(3,attr.getName());
						psa.setString(4, attr.getDataType().toString());
						psa.setInt(5, v);
						psa.setString(6,attr.getValues().get(v));
						psa.setLong(7,obj.getOrganization().getId());
						psa.addBatch();
						if(aiter++ >= maximumInsBatch){
							psa.executeBatch();
							//connection.commit();
							psa.clearBatch();
							aiter=0;
						}
						
					}
					}
			}
				if(aiter > 0){
					psa.executeBatch();
					//connection.commit();
					psa.clearBatch();
				}
				psa.close();
				out_bool = true;
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				if(e.getNextException() != null) logger.error(e.getNextException().getMessage());
			}
		finally{
			try{
				if(connection != null) connection.close();
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			}
		}
		logger.info("Out Bool = " + out_bool);
		return out_bool;
	}

	public List<AttributeType> getAttributes(NameIdType obj) throws FactoryException, ArgumentException{
		List<AttributeType> attributes = new ArrayList<AttributeType>();
		List<QueryField> fieldList = new ArrayList<QueryField>();
		fieldList.add(QueryFields.getFieldReferenceId(obj.getId()));
		fieldList.add(QueryFields.getFieldReferenceType(obj.getNameType()));
		QueryField[] fields = fieldList.toArray(new QueryField[0]);
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setOrderClause("NAME ASC");
		List<AttributeType> out_list = new ArrayList<AttributeType>();

		if(this.dataTables.size() > 1) throw new FactoryException("Multiple table select statements not yet supported");
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		DataTable table = this.dataTables.get(0);
		String selectString = getSelectTemplate(table, instruction);
		String sqlQuery = assembleQueryString(selectString, fields, connectionType, instruction, obj.getOrganization().getId());
		logger.debug(sqlQuery);
		try {
			PreparedStatement statement = connection.prepareStatement(sqlQuery);
			DBFactory.setStatementParameters(fields, statement);
			ResultSet rset = statement.executeQuery();
			
			String lastName = null;
			AttributeType currentAttribute = null;
			int valueIndex = 0;
			while(rset.next()){
				String name = rset.getString("name");
				if(lastName == null || lastName.equals(name) == false){
					currentAttribute = new AttributeType();
					valueIndex = 0;
					lastName = name;
					currentAttribute.setName(name);
					currentAttribute.setDataType(SqlDataEnumType.valueOf(rset.getString("datatype")));
					currentAttribute.setReferenceType(NameEnumType.valueOf(rset.getString("referencetype")));
					currentAttribute.setOrganizationId(rset.getLong("organizationid"));
					currentAttribute.setReferenceId(rset.getLong("referenceid"));
					attributes.add(currentAttribute);
				}
				currentAttribute.getValues().add(rset.getString("value"));
				currentAttribute.setIndex(valueIndex);
				valueIndex++;
			}
			rset.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new FactoryException(e.getMessage());
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

		
		return attributes;
	}
	public boolean deleteAttributes(NameIdType object){
		return deleteAttributes(object,false);
	}
	public boolean deleteAttributes(NameIdType object, boolean preserveValues){
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldReferenceId(object.getId()));
		fields.add(QueryFields.getFieldReferenceType(object.getNameType()));

		boolean out_bool = false;
		try {
			int delCount = deleteByField(fields.toArray(new QueryField[0]),object.getOrganization().getId());
			out_bool = (delCount > 0);
			if(!preserveValues) object.getAttributes().clear();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_bool;
	}
	public boolean deleteAttribute(AttributeType attribute){
		List<QueryField> fields = new ArrayList<QueryField>();
		fields.add(QueryFields.getFieldReferenceId(attribute.getReferenceId()));
		fields.add(QueryFields.getFieldReferenceType(attribute.getReferenceType()));
		fields.add(QueryFields.getFieldName(attribute.getName()));
		boolean out_bool = false;
		try {
			int delCount = deleteByField(fields.toArray(new QueryField[0]),attribute.getOrganizationId());
			out_bool = (delCount > 0);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_bool;
	}
	/*
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		AttributeType new_attr = new AttributeType();
		new_attr.setDataType(SqlDataEnumType.valueOf(rset.getString("datatype")));
		new_attr.setReferenceType(NameEnumType.valueOf(rset.getString("referencetype")));
		new_attr.setOrganizationId(rset.getLong("organizationid"));
		new_attr.setReferenceId(rset.get)
		new_account.setDatabaseRecord(true);
		new_account.setNameType(NameEnumType.ACCOUNT);
		new_account.setAccountId(rset.getString("accountid"));
		new_account.setReferenceId(rset.getLong("referenceid"));
		new_account.setAccountStatus(AccountStatusEnumType.valueOf(rset.getString("accountstatus")));
		new_account.setAccountType(AccountEnumType.valueOf(rset.getString("accounttype")));
		return super.read(rset, new_account);
	}
	*/
	
}
