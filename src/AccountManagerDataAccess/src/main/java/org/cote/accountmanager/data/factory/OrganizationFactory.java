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
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.OrganizationEnumType;

public class OrganizationFactory extends NameIdFactory {
	
	public OrganizationFactory(){
		super();
		this.scopeToOrganization = false;
		this.clusterByParent = true;
		this.hasParentId = true;
		this.hasUrn = true;
		this.hasObjectId = true;
		this.factoryType = FactoryEnumType.ORGANIZATION;
		this.primaryTableName = "organizations";
		this.tableNames.add(primaryTableName);
	}
	
	@Override
	protected void configureTableRestrictions(DataTable table){
		if(table.getName().equalsIgnoreCase("organizations")){
			/// table.setRestrictSelectColumn("logicalid", true);
		}
	}
	
	@Override
	public void initialize(Connection connection) throws FactoryException{
		super.initialize(connection);
		
	}
	@Override
	public <T> boolean update(T object) throws FactoryException
	{
		OrganizationType org = (OrganizationType)object;
		removeFromCache(org);
		return super.update(org);
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
			CONNECTION_TYPE connectionType = DBFactory.getConnectionType(conn);
			logger.warn("TODO: Refactor this query to better handle tables without organization_id");
			Statement stat = null;
			ResultSet rset = null;
			int delLimit = 1000;
			String limit1 =  (connectionType == CONNECTION_TYPE.SQL ? "SET ROWCOUNT " + delLimit + " " : "");
			String limit2 = (connectionType == CONNECTION_TYPE.MYSQL ? " LIMIT " + delLimit + " " : "");
			try {

				// String buildDeleteQuery = "SELECT '" + (connectionType == CONNECTION_TYPE.SQL ? "SET ROWCOUNT " + 1000 + " " : "") + "DELETE FROM ' || tablename || ' WHERE organizationid = ?" + (connectionType == CONNECTION_TYPE.MYSQL ? " LIMIT " + delLimit + " " : "") + ";' FROM pg_tables where schemaname = 'public' AND NOT tablename = 'audit' AND NOT tablename = 'devtable' AND NOT tablename = 'organizations' AND NOT tablename = 'objectreference' AND NOT tablename = 'objectscore' AND NOT tablename = 'objectdescription' AND NOT tablename = 'objectlocation' AND NOT tablename = 'objectdate'  AND NOT tablename = 'objectorderscore' AND NOT tablename = 'vaultkey';";
				String buildDeleteQuery = String.format("SELECT '%s DELETE FROM ' || tablename || ' WHERE organizationid = ?%s;' FROM pg_tables where schemaname = 'public' AND NOT tablename = 'audit' AND NOT tablename = 'devtable' AND NOT tablename = 'organizations' AND NOT tablename = 'objectreference' AND NOT tablename = 'objectscore' AND NOT tablename = 'objectdescription' AND NOT tablename = 'objectlocation' AND NOT tablename = 'objectdate'  AND NOT tablename = 'objectorderscore' AND NOT tablename = 'vaultkey';",limit1,limit2);
				logger.debug(buildDeleteQuery);
				stat = conn.createStatement();
				rset = stat.executeQuery(buildDeleteQuery);
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

			} catch (SQLException e) {
				
				logger.error("Trace",e);
			}
			finally{
				try {
					if(rset != null) rset.close();
					if(stat != null) stat.close();
					
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
		OrganizationType useMap = (OrganizationType)map;
		fields.add(QueryFields.getFieldReferenceId(useMap));
		fields.add(QueryFields.getFieldLogicalId(useMap));
		fields.add(QueryFields.getFieldOrganizationType(useMap));
	}

	public OrganizationType getOrganizationByName(String name, OrganizationType parent) throws FactoryException, ArgumentException
	{
		return getByNameInParent(name, (parent != null ? parent.getId() : 0),0L);
	}

	
	public OrganizationType getOrganizationById(long id) throws FactoryException, ArgumentException
	{

		OrganizationType outOrg = readCache(id);
		if (outOrg != null) return outOrg;

		List<NameIdType> orgs = getByField(new QueryField[] { QueryFields.getFieldId(id) }, 0);

		if (!orgs.isEmpty())
		{
			String keyName = id + "-" + orgs.get(0).getParentId();
			addToCache(orgs.get(0),keyName);
			return (OrganizationType)orgs.get(0);
		}
		return null;
	}
	
	@Override
	protected NameIdType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{

		OrganizationType newMap = new OrganizationType();
		newMap.setNameType(NameEnumType.ORGANIZATION);
		newMap.setLogicalId(rset.getLong("logicalid"));
		newMap.setReferenceId(rset.getLong("referenceid"));
		newMap.setOrganizationType(OrganizationEnumType.valueOf(rset.getString("organizationtype")));
		return super.read(rset,  newMap);
	}
	public OrganizationType addOrganization(String orgName, OrganizationEnumType orgType, OrganizationType orgParent) throws FactoryException, ArgumentException
	{
		long parentId = (orgParent != null ? orgParent.getId() : 0);
		OrganizationType newOrg = getByNameInParent(orgName, parentId,0L);
		if(newOrg == null){
			newOrg = new OrganizationType();
			newOrg.setNameType(NameEnumType.ORGANIZATION);
			newOrg.setOrganizationType(orgType);
			newOrg.setParentId(parentId);
			newOrg.setName(orgName);
			if(add(newOrg)){
				newOrg = this.getByNameInParent(orgName, parentId, 0L);
			}
		}
		return newOrg;

	}
	@Override
	public <T> boolean add(T object) throws FactoryException, ArgumentException
	{
		boolean outBool = false;
		OrganizationType newOrg = (OrganizationType)object;
		DataRow row = prepareAdd(newOrg, "organizations");
		try{
			row.setCellValue("organizationtype", newOrg.getOrganizationType().toString());
			row.setCellValue("logicalid", newOrg.getLogicalId());
			row.setCellValue("referenceid", newOrg.getReferenceId());
			if(insertRow(row)){
				newOrg = getByNameInParent(newOrg.getName(), newOrg.getParentId(),0L);
				if(KeyService.newOrganizationAsymmetricKey(newOrg.getId(), true) == null){
					throw new FactoryException("Unable to generate organization security keys for " + newOrg.getName() + "(#" + newOrg.getId() + ")");
				}
				((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).addDefaultGroups(newOrg.getId());
				outBool = true;
			}
		}
		catch(DataAccessException e){
			logger.error("Trace",e);
			throw new FactoryException(e.getMessage());
		}
		
		return outBool;
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
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T find(UserType user, String type, String path, long organizationId) throws FactoryException, ArgumentException
	{
		return (T)findOrganization(path);
	}
	
	public OrganizationType findOrganization(String path) throws FactoryException, ArgumentException
	{
		
		OrganizationType outOrg = null;
		if (path == null || path.length() == 0) throw new FactoryException("Invalid path");

		String[] paths = path.split("/");

		OrganizationType nestedOrg = null;

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
				nestedOrg = Factories.getRootOrganization();
				if (paths.length == 1)
				{
					logger.warn("Returning root for single path pair with zero length");
					break;
				}
			}
			else{
				nestedOrg = getOrganizationByName(paths[i], nestedOrg);
			}
		}
		outOrg = nestedOrg;

		return outOrg;
	}

}
