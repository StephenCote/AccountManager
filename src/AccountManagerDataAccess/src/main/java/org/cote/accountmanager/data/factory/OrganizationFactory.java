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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.data.security.KeyService;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;

public class OrganizationFactory extends NameIdFactory {
	/// static{ org.cote.accountmanager.data.Factories.registerClass(FactoryEnumType.ORGANIZATION, OrganizationFactory.class); }
	public OrganizationFactory(){
		super();
		this.scopeToOrganization = false;
		this.hasUrn = true;
		this.hasObjectId = true;
		this.tableNames.add("organizations");
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("organizations")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	
	/*
	@Override
	protected String getSelectTemplate(DataTable table, ProcessingInstructionType instruction){
		return table.getSelectFullTemplate();
	}
	*/

	@Override
	public void initialize(Connection connection) throws FactoryException{
		super.initialize(connection);
		
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		OrganizationType org = (OrganizationType)object;
		removeFromCache(org);
		return update(org);
	}
	@Override
	public <T> boolean delete(T object) throws FactoryException
	{
		OrganizationType organization = (OrganizationType)object;
		removeFromCache(organization);
		int deleted = deleteById(organization.getId());

		if (deleted > 0)
		{
			KeyService.deleteKeys(organization.getId());
			Connection conn = ConnectionFactory.getInstance().getConnection();
			CONNECTION_TYPE connection_type = DBFactory.getConnectionType(conn);
			logger.warn("TODO: Refactor this query to better handle tables without organization_id");
			try {
				int delLimit = 1000;
				String buildDeleteQuery = "SELECT '" + (connection_type == CONNECTION_TYPE.SQL ? "SET ROWCOUNT " + 1000 + " " : "") + "DELETE FROM ' || tablename || ' WHERE organizationid = ?" + (connection_type == CONNECTION_TYPE.MYSQL ? " LIMIT " + delLimit + " " : "") + ";' FROM pg_tables where schemaname = 'public' AND NOT tablename = 'audit' AND NOT tablename = 'devtable' AND NOT tablename = 'organizations' AND NOT tablename = 'objectreference' AND NOT tablename = 'objectscore' AND NOT tablename = 'objectdescription' AND NOT tablename = 'objectlocation' AND NOT tablename = 'objectdate'  AND NOT tablename = 'objectorderscore' AND NOT tablename = 'vaultkey';";
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
				stat.close();
				//conn.commit();
			} catch (SQLException e) {
				
				logger.error("Trace",e);
			}
			finally{
				try {
					conn.close();
				} catch (SQLException e) {
					
					logger.error("Trace",e);
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
		return getByNameInParent(name, (parent != null ? parent.getId() : 0),0L);
	}

	
	public OrganizationType getOrganizationById(long id) throws FactoryException, ArgumentException
	{

		OrganizationType out_org = readCache(id);
		if (out_org != null) return out_org;

		List<NameIdType> orgs = getByField(new QueryField[] { QueryFields.getFieldId(id) }, 0);

		if (orgs.isEmpty() == false)
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
		OrganizationType new_org = getByNameInParent(orgName, parentId,0L);
		if(new_org == null){
			new_org = new OrganizationType();
			new_org.setNameType(NameEnumType.ORGANIZATION);
			new_org.setOrganizationType(orgType);
			new_org.setParentId(parentId);
			new_org.setName(orgName);
			if(add(new_org)){
				new_org = this.getByNameInParent(orgName, parentId, 0L);
			}
		}
		return new_org;

	}
	@Override
	public <T> boolean add(T object) throws FactoryException, ArgumentException
	{
		boolean out_bool = false;
		OrganizationType new_org = (OrganizationType)object;
		DataRow row = prepareAdd(new_org, "organizations");
		try{
			row.setCellValue("organizationtype", new_org.getOrganizationType().toString());
			row.setCellValue("logicalid", new_org.getLogicalId());
			row.setCellValue("referenceid", new_org.getReferenceId());
			if(insertRow(row)){
				new_org = getByNameInParent(new_org.getName(), new_org.getParentId(),0L);
				if(KeyService.newOrganizationAsymmetricKey(new_org.getId(), true) == null){
					throw new FactoryException("Unable to generate organization security keys for " + new_org.getName() + "(#" + new_org.getId() + ")");
				}
				((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).addDefaultGroups(new_org.getId());
				out_bool = true;
			}
		}
		catch(DataAccessException e){
			logger.error("Trace",e);
			throw new FactoryException(e.getMessage());
		}
		
		return out_bool;
	}
	public String getOrganizationPath(long organizationId) throws FactoryException, ArgumentException{
		OrganizationType org = getOrganizationById(organizationId);
		return getOrganizationPath(org);
	}
	public String getOrganizationPath(OrganizationType org) throws FactoryException, ArgumentException{
		String path = "";
		if(org == null){
			logger.debug("Organization not found.  This may occur if a reference object is used to set the organization id, and that object is not scoped to organizations");
			return null;
		}
		/// Note: Skip 'Global' Organization, which is always 1L
		/// (always == until it's not, but it's never been not because it must be setup first)
		if(org.getParentId() > 1L){
			path = getOrganizationPath(org.getParentId());
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

		if (paths.length == 0 || path.equals("/"))
		{
			logger.warn("Empty or root path, returning root");
			return Factories.getRootOrganization();
		}
		if(paths.length == 0) throw new FactoryException("Invalid path list from '" + path + "'");

		for (int i = 0; i < paths.length; i++)
		{

			
			if (paths[i].length() == 0 && i == 0)
			{
				nested_org = Factories.getRootOrganization();
				if (paths.length == 1)
				{
					logger.warn("Returning root for single path pair with zero length");
					break;
				}
			}
			else{
				nested_org = getOrganizationByName(paths[i], nested_org);
			}
		}
		out_org = nested_org;

		return out_org;
	}

}
