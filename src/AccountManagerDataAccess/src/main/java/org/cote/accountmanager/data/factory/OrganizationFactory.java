package org.cote.accountmanager.data.factory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.security.OrganizationSecurity;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public class OrganizationFactory extends NameIdFactory {
	/// private static final String buildDeleteQuery = "SELECT 'DELETE FROM ' || tablename || ' WHERE organizationid = ?;' FROM pg_tables where schemaname = 'public' AND NOT tablename = 'audit' AND NOT tablename = 'devtable' AND NOT tablename = 'organizations';";
	public OrganizationFactory(){
		super();
		this.scopeToOrganization = false;
		this.tableNames.add("organizations");
	}
	
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("organizations")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	
	protected String getSelectTemplate(DataTable table, ProcessingInstructionType instruction){
		return table.getSelectFullTemplate();
	}
	public void initialize(Connection connection) throws FactoryException{
		super.initialize(connection);
		
	}
	public boolean updateOrganization(OrganizationType org) throws FactoryException
	{
		removeFromCache(org);
		return update(org);
	}
	public boolean deleteOrganization(OrganizationType organization) throws FactoryException
	{
		removeFromCache(organization);
		int deleted = deleteById(organization.getId());

		if (deleted > 0)
		{
			OrganizationSecurity.deleteSecurityKeys(organization);
			
			Connection conn = ConnectionFactory.getInstance().getConnection();
			CONNECTION_TYPE connection_type = DBFactory.getConnectionType(conn);
			try {
				/// String buildDeleteQuery = (connection_type == CONNECTION_TYPE.SQL ? "SET ROWCOUNT 200 " : "") + "DELETE FROM session WHERE sessionexpiration <= " + token  + (connection_type == CONNECTION_TYPE.MYSQL ? " LIMIT 200 " : "") + ";";
				int delLimit = 1000;
				String buildDeleteQuery = "SELECT '" + (connection_type == CONNECTION_TYPE.SQL ? "SET ROWCOUNT " + 1000 + " " : "") + "DELETE FROM ' || tablename || ' WHERE organizationid = ?" + (connection_type == CONNECTION_TYPE.MYSQL ? " LIMIT " + delLimit + " " : "") + ";' FROM pg_tables where schemaname = 'public' AND NOT tablename = 'audit' AND NOT tablename = 'devtable' AND NOT tablename = 'organizations';";
				logger.debug(buildDeleteQuery);
				Statement stat = conn.createStatement();
				ResultSet rset = stat.executeQuery(buildDeleteQuery);
				while(rset.next()){
					String delQuery = rset.getString(1);
					delQuery.replaceAll("\"","");
					PreparedStatement pstat = conn.prepareStatement(delQuery);
					logger.debug(delQuery);
					pstat.setLong(1, organization.getId());
					int del = pstat.executeUpdate();
					while(del >= delLimit){
						del = pstat.executeUpdate();
					}
					pstat.close();
				}
				rset.close();
				//conn.commit();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally{
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			
		}
		return (deleted > 0);
	}
	
	@Override
	public void setFactoryFields(List<QueryField> fields, NameIdType map, ProcessingInstructionType instruction){
		OrganizationType use_map = (OrganizationType)map;
		fields.add(QueryFields.getFieldReferenceId(use_map));
		fields.add(QueryFields.getFieldLogicalId(use_map));
		fields.add(QueryFields.getFieldOrganizationType(use_map));
	}

	public OrganizationType getOrganizationByName(String name, OrganizationType parent) throws FactoryException, ArgumentException
	{
		return getOrganizationByName(name, (parent != null ? parent.getId() : 0));
	}
	public OrganizationType getOrganizationByName(String name, long parent_id) throws FactoryException, ArgumentException
	{
		//OrganizationType out_org = null;
		String key_name = name + "-" + parent_id ;

		OrganizationType out_org = readCache(key_name);
		if (out_org != null) return out_org;

		List<NameIdType> orgs = getByField(new QueryField[] { QueryFields.getFieldName(name),QueryFields.getFieldParent(parent_id) }, 0);
			//GetByName(name);
		if (orgs.size() > 0)
		{
			addToCache(orgs.get(0),key_name);
			return (OrganizationType)orgs.get(0);
		}
		return null;
	}
	
	public OrganizationType getOrganizationById(long id) throws FactoryException, ArgumentException
	{

		OrganizationType out_org = readCache(id);
		if (out_org != null) return out_org;

		List<NameIdType> orgs = getByField(new QueryField[] { QueryFields.getFieldId(id) }, 0);

		if (orgs.size() > 0)
		{
			String key_name = id + "-" + orgs.get(0).getParentId();
			addToCache(orgs.get(0),key_name);
			return (OrganizationType)orgs.get(0);
		}
		return null;
	}
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
		///System.out.println("impl base");
		OrganizationType new_map = new OrganizationType();
		new_map.setNameType(NameEnumType.ORGANIZATION);
		new_map.setLogicalId(rset.getLong("logicalid"));
		new_map.setReferenceId(rset.getLong("referenceid"));
		new_map.setOrganizationType(OrganizationEnumType.valueOf(rset.getString("organizationtype")));
		return super.read(rset,  new_map);
	}
	public OrganizationType addOrganization(String orgName, OrganizationEnumType orgType, OrganizationType orgParent) throws FactoryException, ArgumentException
	{
		long parentId = (orgParent != null ? orgParent.getId() : 0);
		OrganizationType new_org = getOrganizationByName(orgName, parentId);
		if(new_org == null){
			new_org = new OrganizationType();
			new_org.setOrganizationType(orgType);
			new_org.setParentId(parentId);
			new_org.setName(orgName);
			new_org = addOrganization(new_org);
		}
		return new_org;

	}
	public OrganizationType addOrganization(OrganizationType new_org) throws FactoryException, ArgumentException
	{
		
		DataRow row = prepareAdd(new_org, "organizations");
		try{
			row.setCellValue("organizationtype", new_org.getOrganizationType().toString());
			row.setCellValue("logicalid", new_org.getLogicalId());
			row.setCellValue("referenceid", new_org.getReferenceId());
			if(insertRow(row)){
				//System.out.println("Inserted row");
				new_org = getOrganizationByName(new_org.getName(), new_org.getParentId());
				//System.out.println("Got new org: " + (new_org == null ? "NULL" : new_org.getId()));
				if(OrganizationSecurity.generateSecurityKeys(new_org) == false){
					throw new FactoryException("Unable to generate organization security keys for " + new_org.getName() + "(#" + new_org.getId() + ")");
				}
				Factories.getGroupFactory().addDefaultGroups(new_org);
				return new_org;
			}
		}
		catch(DataAccessException dae){
			dae.printStackTrace();
			throw new FactoryException(dae.getMessage());
		}
		
		return null;
	}
	public String getOrganizationPath(OrganizationType org) throws FactoryException, ArgumentException{
		String path = "";
		/// Note: Skip 'Global' Organization, which is always 1L
		/// (always == until it's not, but it's never been not because it must be setup first)
		if(org.getParentId() > 1L){
			path = getOrganizationPath(getOrganizationById(org.getParentId()));
		}
		path = path + "/" + org.getName();
		return path;
	}
	public OrganizationType findOrganization(String path) throws FactoryException, ArgumentException
	{
		
		OrganizationType out_org = null;
		if (path == null || path.length() == 0) throw new FactoryException("Invalid path");

		String[] paths = path.split("/");

		OrganizationType nested_org = null;

		String name = null;
		if (paths.length == 0 || path.equals("/"))
		{
			System.out.println("Empty or root path, returning root");
			return Factories.getRootOrganization();
		}
		if(paths.length == 0) throw new FactoryException("Invalid path list from '" + path + "'");

		for (int i = 0; i < paths.length; i++)
		{
			name = paths[i];
			
			if (name.length() == 0 && i == 0)
			{
				nested_org = Factories.getRootOrganization();
				if (paths.length == 1)
				{
					System.out.println("Returning root for single path pair with zero length");
					break;
				}
				name = paths[++i];
			}
			
			nested_org = getOrganizationByName(paths[i], nested_org);
		}
		out_org = nested_org;

		return out_org;
	}

}
