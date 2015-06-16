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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.DataRow;
import org.cote.accountmanager.data.DataTable;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.query.QueryField;
import org.cote.accountmanager.data.query.QueryFields;
import org.cote.accountmanager.objects.DataRowType;
import org.cote.accountmanager.objects.MessageSpoolType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.SpoolBucketEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;
import org.cote.accountmanager.objects.BaseSpoolType;
import org.cote.accountmanager.util.CalendarUtil;

public class SecurityTokenFactory extends SpoolFactory {
	/// Expiry in hours
	///
	private int defaultTokenExpiry = 6;
	public SecurityTokenFactory()
	{
		super();
	}
	public void initialize(Connection connection) throws FactoryException{
		super.initialize(connection);
		DataTable table = this.getDataTable("spool");
		table.setBulkInsert(true);
		
	}
	public boolean deleteToken(SecuritySpoolType message) throws FactoryException
	{
		removeFromCache(message);
		int deleted = deleteByField(new QueryField[] { QueryFields.getFieldSpoolGuid(message) }, message.getOrganization().getId());
		return (deleted > 0);
	}
	public boolean deleteTokens(String referenceId, OrganizationType organization) throws FactoryException
	{

		clearCache();
		int deleted = deleteByField(new QueryField[] { QueryFields.getFieldSpoolName(referenceId), QueryFields.getFieldSpoolBucketType(SpoolBucketEnumType.SECURITY_TOKEN), QueryFields.getFieldSpoolBucketName(SpoolNameEnumType.GENERAL) }, organization.getId());
		return (deleted > 0);
	}
	
	public SecuritySpoolType getSecurityToken(String referenceId, OrganizationType organization) throws FactoryException, ArgumentException
	{
		SecuritySpoolType[] tokens = getSecurityTokens(referenceId, organization);
		if (tokens.length == 0) return null;
		return tokens[0];
	}

	public SecuritySpoolType[] getSecurityTokens(String spoolName, OrganizationType organization) throws FactoryException, ArgumentException
	{
		List<BaseSpoolType> tokens = getByField(new QueryField[] { QueryFields.getFieldSpoolName(spoolName), QueryFields.getFieldSpoolBucketType(SpoolBucketEnumType.SECURITY_TOKEN) }, organization.getId());
		if (tokens.size() == 0) return new SecuritySpoolType[0];
		return tokens.toArray(new SecuritySpoolType[0]);
	}
	public SecuritySpoolType popSecurityToken(String guid, OrganizationType organization) throws FactoryException, ArgumentException
	{
		SecuritySpoolType token = getSecurityToken(guid, organization);
		if (token == null || deleteToken(token) == false) return null;
		return token;
	}

	public SecuritySpoolType getSecurityTokenById(String guid, OrganizationType organization) throws FactoryException, ArgumentException
	{
		List<BaseSpoolType> tokens = getByField(new QueryField[] { QueryFields.getFieldSpoolGuid(guid),QueryFields.getFieldSpoolBucketType(SpoolBucketEnumType.SECURITY_TOKEN) }, organization.getId());
		if (tokens.size() == 0) return null;
		return (SecuritySpoolType)tokens.get(0);
	}


	public SecuritySpoolType generateSecurityToken(String referenceId, OrganizationType organization) throws FactoryException
	{
		return generateSecurityToken(referenceId, null, organization);
	}
	public SecuritySpoolType generateSecurityToken(String referenceId, String data, OrganizationType organization) throws FactoryException
	{
		SecuritySpoolType new_token = newSecurityToken(referenceId, organization);
		new_token.setData(data);
		if (new_token == null || addSecurityToken(new_token) == false) return null;
		return new_token;
		
	}
	public SecuritySpoolType newSecurityToken(String referenceId, OrganizationType organization) throws FactoryException
	{
		if (organization == null || organization.getId() <=0) throw new FactoryException("Invalid organization");

		SecuritySpoolType new_token = new SecuritySpoolType();
		
		new_token.setGuid(UUID.randomUUID().toString());
		new_token.setSpoolBucketName(SpoolNameEnumType.GENERAL);
		new_token.setOwnerId((long)0);
		new_token.setOrganization(organization);
		new_token.setName(referenceId);
		new_token.setSpoolBucketType(SpoolBucketEnumType.SECURITY_TOKEN);
		new_token.setValueType(ValueEnumType.STRING);
		new_token.setCreated(CalendarUtil.getXmlGregorianCalendar(Calendar.getInstance().getTime()));
		Date expDate = Calendar.getInstance().getTime();
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.HOUR, defaultTokenExpiry);
		new_token.setExpiration(CalendarUtil.getXmlGregorianCalendar(cal.getTime()));
		new_token.setExpires(true);
		new_token.setSpoolStatus(0);

		return new_token;
	}
	
	public boolean isValid(BaseSpoolType message)
	{
		if (
			message == null
			|| message.getOrganization() == null
			|| message.getOrganization().getId() <= 0
			|| message.getSpoolBucketName() == null
			|| message.getSpoolBucketType() == null
			|| message.getName() == null
			|| message.getGuid() == null
		) return false;
		return true;
	}

	protected BaseSpoolType read(ResultSet rset, ProcessingInstructionType instruction) throws SQLException, FactoryException, ArgumentException
	{
			SecuritySpoolType new_message = new SecuritySpoolType();
			return super.read(rset, new_message);
	}

	public boolean addSecurityToken(SecuritySpoolType new_token) throws FactoryException
	{
		if (!isValid(new_token)) throw new FactoryException("Token is invalid");
		/// Bulk insert note: prepareAdd and insertRow won't add the row to the local table row cache, so it must be added manually
		///
		DataRow row = prepareAdd(new_token, "spool");
		getDataTable("spool").getRows().add(row);
		boolean ins = insertRow(row);
		
		writeSpool("spool");
		return ins;
	}
	public boolean addSecurityTokens(SecuritySpoolType[] new_tokens) throws FactoryException
	{
		int error = 0;
		System.out.println("Inserting tokens: " + new_tokens.length + " into table with rows " + getDataTable("spool").getRows().size());
		for(int i = 0; i < new_tokens.length; i++){
			if (!isValid(new_tokens[i])) throw new FactoryException("Token is invalid");
			/// Bulk insert note: prepareAdd and insertRow won't add the row to the local table row cache, so it must be added manually
			///
			DataRow row = prepareAdd(new_tokens[i], "spool");
			getDataTable("spool").getRows().add(row);
			boolean ins = insertRow(row);

			//System.out.println("Adding: " + new_tokens[i].getGuid());
			if(!ins) error++;
		}

		writeSpool("spool");
		return (error == 0);
	}
	
}
