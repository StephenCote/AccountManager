/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.rocket.services;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.data.DBFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.FactoryBase;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ApplicationPermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.types.ComparatorEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;
import org.cote.propellant.objects.ProjectType;
public class IdentityServiceDAL {
	public static final Logger logger = LogManager.getLogger(IdentityServiceDAL.class);
	
	private Map<String,ApplicationPermissionType> servicePermissionMapCache = null;
	private IdentityService is = null;
	public IdentityServiceDAL(IdentityService isf){
		is = isf;
		servicePermissionMapCache = new HashMap<String,ApplicationPermissionType>();
	}

	public List<List<BaseRoleType>> getRoleHierarchy(long baseId){
		List<List<BaseRoleType>> outList = new ArrayList<List<BaseRoleType>>();
		
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(connectionType);
		Map<Long,Integer> levels = new HashMap<Long,Integer>();
		long organizationId = 0L;
		List<Long> ids = new ArrayList<>();
		try {
			PreparedStatement stat = connection.prepareStatement("SELECT level, leafid, roleid, parentid, organizationid FROM leveled_roles_from_leaf(" + token + ") ORDER BY level,parentid;");
			stat.setLong(1, baseId);
			ResultSet rset = stat.executeQuery();
			while(rset.next()){
				int level = rset.getInt("level");
				levels.put(rset.getLong("roleid"), level - 1);
				if(organizationId <= 0L) organizationId = rset.getLong("organizationid");
				ids.add(rset.getLong("roleid"));
				if(outList.size() <= level){
					outList.add(new ArrayList<BaseRoleType>());
				}
			}
			rset.close();
			List<BaseRoleType> roles = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).listByIds(ArrayUtils.toPrimitive(ids.toArray(new Long[0])), organizationId);
			for(int i = 0; i < roles.size();i++){
				//BaseRoleType role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleById(rset.getLong("roleid"), ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationById(rset.getLong("organizationid")));
				BaseRoleType role = roles.get(i);
				Factories.getAttributeFactory().populateAttributes(role);
				int level = levels.get(role.getId());

				outList.get(level).add(role);
			}
			
		} catch (SQLException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		
		
		return outList;
	}
	public PersonRoleType getRoleByNameInHierarchy(String name,long baseId){
		
		PersonRoleType role = null;
		
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(connectionType);
		String sql = "SELECT LRF.level, LRF.leafid, LRF.roleid, LRF.parentid, LRF.organizationid,R.name as roledn,A1.value as rolename FROM leveled_roles_from_leaf(" + token + ") LRF JOIN roles R on R.id=LRF.roleid JOIN attribute A1 on A1.referenceid = R.id AND A1.referenceType = 'ROLE' AND A1.name='name' WHERE (R.name = " + token + " OR A1.value = " + token + ") ORDER BY level,parentid,A1.value;";
		try {
			logger.info(sql);
			PreparedStatement stat = connection.prepareStatement(sql);
			stat.setLong(1, baseId);
			stat.setString(2,name);
			stat.setString(3,name);
			ResultSet rset = stat.executeQuery();
			if(rset.next()){
				role = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRoleById(rset.getLong("roleid"), rset.getLong("organizationid"));
				Factories.getAttributeFactory().populateAttributes(role);
			}
			rset.close();
			
		} catch (SQLException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (FactoryException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		finally{
			try {
				connection.close();
			} catch (SQLException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		
		
		return role;
	}
	
	/*
	public int countServiceGroups(ProjectType project, long serviceId, String permissionName, String permissionDn){
		int count = 0;
		
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setPaginate(false);
		
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);

		String selectString = "SELECT count(servicegroupid) FROM simservicegroups";
		QueryField[] qFields = getSearchFields(project, serviceId, permissionName, permissionDn);
		String sql = assembleQueryString(selectString, qFields, connectionType, instruction);
		
		try{
			logger.info(sql);
			PreparedStatement stat = connection.prepareStatement(sql);
			DBFactory.setStatementParameters(qFields, stat);
			ResultSet rset = stat.executeQuery();
			List<Long> matchIds = new ArrayList<>();
			if(rset.next()){
				count = rset.getInt(1);
			}
			rset.close();
		}
		catch(SQLException sqe){
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		finally{
			try{
				connection.close();
			}
			catch(SQLException sqe){
				logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
			}
		}
		///((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getList(fields, instruction, startRecord, recordCount, organization);
		
		return count;
	}
	
	public ApplicationPermissionType getServicePermissionByNameAndObjectClass(ProjectType project, String servicedn, String name, String className){
		ApplicationPermissionType dir = null;
		String key = SecurityUtil.getDigestAsString(project.getId() + "-" + servicedn + "-" + name + "-" + className);
		if(servicePermissionMapCache.containsKey(key)) return servicePermissionMapCache.get(key);
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);
		String token = DBFactory.getParamToken(connectionType);
		String sql = "SELECT projectname, projectid, servicename, servicedn, serviceid, servicegroupname, servicegroupdn, servicegroupid,servicegroupobjectclass FROM simservicegroupobjectclasses"
				+ " WHERE projectid = " + token + " AND servicedn = " + token + " AND servicegroupname = " + token + " AND servicegroupobjectclass = " + token; 
		;
		
		try{
			logger.info(sql + " with " + project.getId() + ", " + servicedn + ", " + name + ", " + className);
			PreparedStatement stat = connection.prepareStatement(sql);
			stat.setLong(1, project.getId());
			stat.setString(2, servicedn);
			stat.setString(3, name);
			stat.setString(4, className);
			ResultSet rset = stat.executeQuery();

			if(rset.next()){
				logger.info("Get group for id " + rset.getLong("servicegroupid"));
				dir = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getById(rset.getLong("servicegroupid"),project.getOrganization());
				if(dir != null){
					Factories.getAttributeFactory().populateAttributes(dir);
					servicePermissionMapCache.put(key, dir);
				}
			}
			else{
				logger.warn("No results returned");
			}
			rset.close();
		}
		catch(SQLException sqe){
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		finally{
			try{
				connection.close();
			}
			catch(SQLException sqe){
				logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
			}
		}
		return dir;
	}
	
	public List<ApplicationPermissionType> searchServicePermissions(ProjectType project, long serviceId, String permissionName, String permissionDn, long startRecord, int recordCount){
		List<ApplicationPermissionType> res = new ArrayList<ApplicationPermissionType>();
		
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setPaginate(true);
		instruction.setStartIndex(startRecord);
		instruction.setRecordCount(recordCount);
		instruction.setOrderClause("servicegroupname ASC");
		
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);

		String selectString = "SELECT projectname, projectid, servicename, servicedn, serviceid, servicegroupname, servicegroupdn, servicegroupid FROM simservicegroups";
		QueryField[] qFields = getSearchFields(project, serviceId, permissionName, permissionDn);
		String sql = assembleQueryString(selectString, qFields, connectionType, instruction);
		logger.info(sql);
		try{
			logger.info(sql);
			PreparedStatement stat = connection.prepareStatement(sql);
			DBFactory.setStatementParameters(qFields, stat);
			ResultSet rset = stat.executeQuery();
			List<Long> matchIds = new ArrayList<>();
			while(rset.next()){
				matchIds.add(rset.getLong("servicegroupid"));
			}
			res = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).listByIds(ArrayUtils.toPrimitive(matchIds.toArray(new Long[0])), null, project.getOrganization());
			for(int i = 0; i < res.size();i++){
				Factories.getAttributeFactory().populateAttributes(res.get(i));
			}
			rset.close();
		}
		catch(SQLException sqe){
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		finally{
			try{
				connection.close();
			}
			catch(SQLException sqe){
				logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
			}
		}
		///((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getList(fields, instruction, startRecord, recordCount, organization);
		
		return res;
	}
	
	

	*/
	public List<ApplicationPermissionType> searchApplicationPermissions(ProjectType project, long serviceId, String permissionName, String permissionDn, long startRecord, int recordCount){
		List<ApplicationPermissionType> res = new ArrayList<ApplicationPermissionType>();
		
		ProcessingInstructionType instruction = new ProcessingInstructionType();
		instruction.setPaginate(true);
		instruction.setStartIndex(startRecord);
		instruction.setRecordCount(recordCount);
		instruction.setOrderClause("servicegroupname ASC");
		
		Connection connection = ConnectionFactory.getInstance().getConnection();
		CONNECTION_TYPE connectionType = DBFactory.getConnectionType(connection);

		String selectString = "SELECT projectname, projectid, servicename, servicedn, serviceid, servicegroupname, servicegroupdn, servicegroupid FROM simservicegroups";
		QueryField[] qFields = getSearchFields(project, serviceId, permissionName, permissionDn);
		String sql = assembleQueryString(selectString, qFields, connectionType, instruction);
		logger.info(sql);
		try{
			logger.info(sql);
			PreparedStatement stat = connection.prepareStatement(sql);
			DBFactory.setStatementParameters(qFields, stat);
			ResultSet rset = stat.executeQuery();
			List<Long> matchIds = new ArrayList<>();
			while(rset.next()){
				matchIds.add(rset.getLong("servicegroupid"));
			}
			res = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).listByIds(ArrayUtils.toPrimitive(matchIds.toArray(new Long[0])), null, project.getOrganizationId());
			for(int i = 0; i < res.size();i++){
				Factories.getAttributeFactory().populateAttributes(res.get(i));
			}
			rset.close();
		}
		catch(SQLException sqe){
			logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
		} catch (FactoryException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		} catch (ArgumentException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		finally{
			try{
				connection.close();
			}
			catch(SQLException sqe){
				logger.error(FactoryException.LOGICAL_EXCEPTION,sqe);
			}
		}
		///((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getList(fields, instruction, startRecord, recordCount, organization);
		
		return res;
	}
	private QueryField[] getSearchFields(ProjectType project, long serviceId, String permissionName, String permissionDn){
		List<QueryField> fields = new ArrayList<>();
		QueryField q = new QueryField(SqlDataEnumType.BIGINT,"projectid",project.getId());
		q.setComparator(ComparatorEnumType.EQUALS);
		fields.add(q);
		if(serviceId > 0L){
			q = new QueryField(SqlDataEnumType.BIGINT,"serviceid",serviceId);
			q.setComparator(ComparatorEnumType.EQUALS);	
			fields.add(q);
		}
		if(permissionName != null){
			q = new QueryField(SqlDataEnumType.VARCHAR,"servicegroupname","%" + permissionName + "%");
			q.setComparator(ComparatorEnumType.LIKE);
			fields.add(q);
		}
		if(permissionDn != null){
			q = new QueryField(SqlDataEnumType.VARCHAR,"servicegroupdn","%" + permissionDn + "%");
			q.setComparator(ComparatorEnumType.LIKE);
			fields.add(q);
		}
		return fields.toArray(new QueryField[0]);

	}
	protected String assembleQueryString(String selectString, QueryField[] fields, CONNECTION_TYPE connectionType, ProcessingInstructionType instruction){
		String pagePrefix = DBFactory.getPaginationPrefix(instruction, connectionType);
		String pageSuffix = DBFactory.getPaginationSuffix(instruction, connectionType);
		String pageField = DBFactory.getPaginationField(instruction, connectionType);
		String paramToken = DBFactory.getParamToken(connectionType);
		String queryClause = FactoryBase.getQueryClause(instruction,fields, paramToken);
		
		selectString = selectString.replaceAll("#TOP#", (instruction != null && instruction.getTopCount() > 0 ? "TOP " + instruction.getTopCount() : ""));
		selectString = selectString.replaceAll("#PAGE#", pageField);
		
		String sqlQuery = pagePrefix + selectString + " WHERE " + queryClause
			+ (instruction != null && instruction.getGroupClause() != null ? " GROUP BY " + instruction.getGroupClause() : "")
			+ (instruction != null && instruction.getHavingClause() != null ? " HAVING " + instruction.getHavingClause() : "")
			+ pageSuffix
		;
		
		return sqlQuery;
	}
}
