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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseSpoolType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.SpoolBucketEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;
import org.cote.accountmanager.util.CalendarUtil;

public class SecurityTokenFactory extends SpoolFactory {

	private int defaultTokenExpiry = 6;
	public static final int TOKEN_EXPIRY_10_MINUTES = 600;
	public static final int TOKEN_EXPIRY_1_HOUR = 3600;
	public static final int TOKEN_EXPIRY_6_HOURS = 21600;

	public SecurityTokenFactory()
	{
		super();
		this.factoryType = FactoryEnumType.SECURITYTOKEN;
	}
	public void initialize(Connection connection) throws FactoryException{
		super.initialize(connection);
		DataTable table = this.getDataTable(this.primaryTableName);
		table.setBulkInsert(true);
		
	}
	public boolean deleteToken(SecuritySpoolType message) throws FactoryException
	{
		removeFromCache(message);
		int deleted = deleteByField(new QueryField[] { QueryFields.getFieldGuid(message.getGuid()) }, message.getOrganizationId());
		return (deleted > 0);
	}
	public boolean deleteTokens(String referenceId, long organizationId) throws FactoryException
	{

		clearCache();
		int deleted = deleteByField(new QueryField[] { QueryFields.getFieldName(referenceId), QueryFields.getFieldSpoolBucketType(SpoolBucketEnumType.SECURITY_TOKEN), QueryFields.getFieldSpoolBucketName(SpoolNameEnumType.GENERAL) }, organizationId);
		return (deleted > 0);
	}
	
	public SecuritySpoolType getSecurityToken(String referenceId, long organizationId) throws FactoryException, ArgumentException
	{
		SecuritySpoolType[] tokens = getSecurityTokens(referenceId, organizationId);
		if (tokens.length == 0) return null;
		return tokens[0];
	}

	public SecuritySpoolType[] getSecurityTokens(String spoolName, long organizationId) throws FactoryException, ArgumentException
	{
		List<BaseSpoolType> tokens = getByField(new QueryField[] { QueryFields.getFieldName(spoolName), QueryFields.getFieldSpoolBucketType(SpoolBucketEnumType.SECURITY_TOKEN) }, organizationId);
		if (tokens.isEmpty()) return new SecuritySpoolType[0];
		return tokens.toArray(new SecuritySpoolType[0]);
	}
	public SecuritySpoolType popSecurityToken(String guid, long organizationId) throws FactoryException, ArgumentException
	{
		SecuritySpoolType token = getSecurityToken(guid, organizationId);
		if (token == null || !deleteToken(token)) return null;
		return token;
	}

	public SecuritySpoolType getSecurityTokenById(String guid, long organizationId) throws FactoryException, ArgumentException
	{
		List<BaseSpoolType> tokens = getByField(new QueryField[] { QueryFields.getFieldGuid(guid),QueryFields.getFieldSpoolBucketType(SpoolBucketEnumType.SECURITY_TOKEN) }, organizationId);
		if (tokens.isEmpty()) return null;
		return (SecuritySpoolType)tokens.get(0);
	}

	
	public List<SecuritySpoolType> getSecurityTokenByNameInGroup(String name, long groupid, long organizationId) throws FactoryException, ArgumentException
	{
		return convertList(getByField(new QueryField[] { QueryFields.getFieldName(name),QueryFields.getFieldGroup(groupid),QueryFields.getFieldSpoolBucketType(SpoolBucketEnumType.SECURITY_TOKEN) }, organizationId));
	}
	

	public SecuritySpoolType generateSecurityToken(String referenceId, long organizationId) throws FactoryException, ArgumentException
	{
		return generateSecurityToken(referenceId, new byte[0], organizationId);
	}
	public SecuritySpoolType generateSecurityToken(String referenceId, byte[] data, long organizationId) throws FactoryException, ArgumentException
	{
		SecuritySpoolType newToken = newSecurityToken(referenceId, organizationId);
		newToken.setData(data);
		if (addSecurityToken(newToken) == false) return null;
		return newToken;
		
	}
	public SecuritySpoolType newSecurityToken(String referenceId, long organizationId) throws FactoryException, ArgumentException
	{
		if ( organizationId <=0L) throw new FactoryException("Invalid organization");

		SecuritySpoolType newToken = newSpoolEntry(SpoolBucketEnumType.SECURITY_TOKEN);
		newToken.setSpoolBucketName(SpoolNameEnumType.GENERAL);
		newToken.setOrganizationId(organizationId);
		newToken.setName(referenceId);

		newToken.setValueType(ValueEnumType.STRING);

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, defaultTokenExpiry);
		newToken.setExpiration(CalendarUtil.getXmlGregorianCalendar(cal.getTime()));
		newToken.setExpires(true);

		return newToken;
	}
	
	public boolean isValid(BaseSpoolType message)
	{
		if (
			message == null

			|| message.getOrganizationId() <= 0L
			|| message.getSpoolBucketName() == null
			|| message.getSpoolBucketType() == null
			|| message.getName() == null
			|| message.getGuid() == null
		) return false;
		return true;
	}

	protected BaseSpoolType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
			SecuritySpoolType newMessage = new SecuritySpoolType();
			return super.read(rset, newMessage);
	}

	public boolean addSecurityToken(SecuritySpoolType newToken) throws FactoryException
	{
		if (!isValid(newToken)) throw new FactoryException("Token is invalid");
		/// Bulk insert note: prepareAdd and insertRow won't add the row to the local table row cache, so it must be added manually
		///
		DataRow row = prepareAdd(newToken, "spool");
		getDataTable("spool").getRows().add(row);
		boolean ins = insertRow(row);
		
		writeSpool(this.primaryTableName);
		return ins;
	}
	public boolean addSecurityTokens(SecuritySpoolType[] newTokens) throws FactoryException
	{
		int error = 0;
		logger.info("Inserting tokens: " + newTokens.length + " into table with rows " + getDataTable(this.primaryTableName).getRows().size());
		for(int i = 0; i < newTokens.length; i++){
			if (!isValid(newTokens[i])) throw new FactoryException("Token is invalid");
			/// Bulk insert note: prepareAdd and insertRow won't add the row to the local table row cache, so it must be added manually
			///
			DataRow row = prepareAdd(newTokens[i], "spool");
			getDataTable("spool").getRows().add(row);
			boolean ins = insertRow(row);

			if(!ins) error++;
		}

		writeSpool("spool");
		return (error == 0);
	}
	
}
