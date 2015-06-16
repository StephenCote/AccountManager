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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ConnectionFactory;
import org.cote.accountmanager.data.DBFactory;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.ParticipantEnumType;

/// TODO : Remove this class - it's now dead code with EffectiveAuthorizationService
///
public class ParticipationUtil {
	public static final Logger logger = Logger.getLogger(ParticipationUtil.class.getName());
	/*
	public static long[] getDataFromGroupForAccount(AccountType account)
	{
		return getMapIdFromParticipation(
			"groupparticipation",
			0,
			ParticipantEnumType.DATA,
			"data",
			"id",
			account.getId(),
			"ownerid",
			null,
			AffectEnumType.AGGREGATE,
			account.getOrganization().getId()
		);
	}
	public static long[] getDataFromGroupForAccount(BaseGroupType group, AccountType account)
	{
		return getMapIdFromParticipation(
			"groupparticipation",
			group.getId(),
			ParticipantEnumType.DATA,
			"data",
			"id",
			account.getId(),
			"ownerid",
			null,
			AffectEnumType.AGGREGATE,
			group.getOrganization().getId()
		);
	}
	*/
	public static long[] getMapIdFromParticipation(
		String participation_table,
		long participation_id,
		ParticipantEnumType participant_type,
		String map_table,
		String map_join_column,
		long map_id,
		String map_column,
		BasePermissionType permission,
		AffectEnumType affect_type,
		long organization_id
)
	{
		List<Long> out_ids = new ArrayList<Long>();

		Connection connection = ConnectionFactory.getInstance().getConnection();
		String lock_hint = DBFactory.getNoLockHint(DBFactory.getConnectionType(connection));
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));
		
		try
		{
			String sql = "SELECT " + participation_table + ".participantid FROM " + participation_table + lock_hint
				+ " INNER JOIN " + map_table + lock_hint
				+ " ON " + map_table + "." + map_join_column + " = " + participation_table + ".participantid"
				+ " AND " + participation_table + ".organizationid = " + map_table + ".organizationid"
				+ " AND " + map_table + "." + map_column + "=" + token
				+ " WHERE " + participation_table + ".participanttype = " + token
				+ (participation_id > 0 ? " AND " + participation_table + ".participationid = " + token : "")
				+ " AND " + participation_table + ".affecttype = " + token
				+ " AND " + participation_table + ".affectid = " + token
				+ " AND " + participation_table + ".organizationid = " + token
			;
			
			logger.warn("REFACTOR TO EFFECTIVE PERMISSIONS");
			logger.info(sql);
			logger.info("Participant Type: " + participant_type);
			logger.info("Participation Id: " + participation_id);
			logger.info("Affect Type: " + affect_type);
			logger.info("Affect Id: " + (permission == null ? 0 : permission.getId()));
			logger.info("Organization Id: " + organization_id);
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			for(int i = 0; i < stack.length;i++){
				logger.info(stack[i].toString());
			}
			
			// params: map_id, participanttype, (opt:participationid), affecttype, affectid, orgid
			PreparedStatement statement = connection.prepareStatement(sql);
			int pcount = 1;
			statement.setLong(pcount++, map_id);
			statement.setString(pcount++, participant_type.toString());
			if(participation_id > 0) statement.setLong(pcount++, participation_id);
			statement.setString(pcount++, affect_type.toString());
			statement.setLong(pcount++, (permission == null ? 0 : permission.getId()));
			statement.setLong(pcount++, organization_id);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				out_ids.add(rset.getLong(0));
			}
			rset.close();

		}
		catch (SQLException sqe)
		{
			System.out.println(sqe.getMessage());
			sqe.printStackTrace();
			
		}
		finally
		{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ArrayUtils.toPrimitive(out_ids.toArray(new Long[out_ids.size()]));
	}

	/// <summary>
	/// Given an account and data object, find a corresponding roleparticipation id where the account participates in the role and the role participates with data with affect.
	/// </summary>
	/// <param name="data"></param>
	/// <param name="account"></param>
	/// <param name="permission"></param>
	/// <param name="affect_type"></param>
	/// <returns>RoleParticipation id for account</returns>
	public static long getParticipationForMapFromDataRole(NameIdType map, DataType data, BasePermissionType permission, AffectEnumType affect_type)
	{
		long out_id = 0;
		ParticipantEnumType part_type = ParticipantEnumType.valueOf(map.getNameType().toString());
		long[] ids = getParticipationMap("dataparticipation", data.getId(), ParticipantEnumType.ROLE, "roleparticipation", map.getId(), part_type, permission, affect_type, data.getOrganization().getId());
		if (ids.length > 0) out_id = ids[0];
		return out_id;
	}
	public static long getParticipationForMapFromGroupRole(NameIdType map, BaseGroupType group, BasePermissionType permission, AffectEnumType affect_type)
	{
		long out_id = 0;
		ParticipantEnumType part_type = ParticipantEnumType.valueOf(map.getNameType().toString());
		long[] ids = getParticipationMap("groupparticipation", group.getId(), ParticipantEnumType.ROLE, "roleparticipation", map.getId(), part_type, permission, affect_type, group.getOrganization().getId());
		if (ids.length > 0) out_id = ids[0];
		return out_id;
	}
	public static long[] getParticipationMap(
		String participation_table,
		long participation_id,
		ParticipantEnumType participation_type,
		String participant_table,
		long participant_id,
		ParticipantEnumType participant_type,
		BasePermissionType permission, 
		AffectEnumType affect_type,
		long organization_id
	)
	{
		List<Long> out_ids = new ArrayList<Long>();

		Connection connection = ConnectionFactory.getInstance().getConnection();
		String lock_hint = DBFactory.getNoLockHint(DBFactory.getConnectionType(connection));
		String token = DBFactory.getParamToken(DBFactory.getConnectionType(connection));

		try
		{
			
			String sql = "SELECT " + participant_table + ".id FROM " + participant_table + lock_hint
								+ " INNER JOIN " + participation_table + lock_hint
								+ " ON " + participation_table + ".participantid = " + participant_table + ".participationid"
								+ " AND " + participation_table + ".organizationid = " + participant_table + ".organizationid"
								+ " WHERE " + participant_table + ".participanttype = " + token
								+ " AND " + participant_table + ".participantid = " + token
								+ " AND " + participation_table + ".participationid = " + token
								+ " AND " + participation_table + ".participanttype = " + token
								+ " AND " + participation_table + ".affecttype = " + token
								+ " AND " + participation_table + ".affectid = " + token
								+ " AND " + participation_table + ".organizationid = " + token
			;
			logger.warn("REFACTOR TO EFFECTIVE PERMISSIONS");
			logger.info(sql);
			logger.info("Participant Type: " + participant_type);
			logger.info("Participant Id: " + participant_id);
			logger.info("Participation Id: " + participation_id);
			logger.info("Participation Type: " + participation_type);
			logger.info("Affect Type: " + affect_type);
			logger.info("Affect Id: " + (permission == null ? 0 : permission.getId()));
			logger.info("Organization Id: " + organization_id);
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			for(int i = 0; i < stack.length;i++){
				logger.info(stack[i].toString());
			}
			//System.out.println(sql);
			// participanttype, participant_id, participation_id, participation_type, affecttype, affectid, organizationid
			PreparedStatement statement = connection.prepareStatement(sql);
			int pcount = 1;
			statement.setString(pcount++, participant_type.toString());
			statement.setLong(pcount++, participant_id);
			statement.setLong(pcount++, participation_id);
			statement.setString(pcount++, participation_type.toString());
			statement.setString(pcount++, affect_type.toString());
			statement.setLong(pcount++, (permission == null ? 0 : permission.getId()));
			statement.setLong(pcount++, organization_id);
			ResultSet rset = statement.executeQuery();
			while (rset.next())
			{
				out_ids.add(rset.getLong(1));
			}
			rset.close();

		}
		catch (SQLException sqe)
		{
			System.out.println(sqe.getMessage());
			sqe.printStackTrace();
			
		}
		finally
		{
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ArrayUtils.toPrimitive(out_ids.toArray(new Long[out_ids.size()]));
	}
}
