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
package org.cote.rocket.util;

import java.util.Calendar;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.ApplicationPermissionType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.PersonRoleType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.types.AccountEnumType;
import org.cote.accountmanager.objects.types.AccountStatusEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.propellant.objects.IdentityConnectionType;

public class IdentityEntityUtil {
	
	public static final Logger logger = LogManager.getLogger(IdentityEntityUtil.class);
	
	public static <T> T getNewType(FactoryEnumType factoryType){
		T out = null;
		/// these are parroting what the factories do, without setting the organization or owner or parent or group
		///
		switch(factoryType){
			case ACCOUNT:
				AccountType a = new AccountType();
				a.setNameType(NameEnumType.ACCOUNT);
				a.setAccountStatus(AccountStatusEnumType.NORMAL);
				a.setAccountType(AccountEnumType.NORMAL);
				a.setAccountId(UUID.randomUUID().toString());
				out = (T)a;
				break;
			case ROLE:
				PersonRoleType r = new PersonRoleType();
				r.setNameType(NameEnumType.ROLE);
				r.setRoleType(RoleEnumType.PERSON);
				out = (T)r;
				break;
			case PERSON:
				PersonType p = new PersonType();
				p.setNameType(NameEnumType.PERSON);
				Calendar now = Calendar.getInstance();
				now.setTimeInMillis(0);
				XMLGregorianCalendar cal = CalendarUtil.getXmlGregorianCalendar(now.getTime()); 
				p.setBirthDate(cal);

				out = (T)p;
				break;
			case GROUP:
				DirectoryGroupType d = new DirectoryGroupType();
				d.setNameType(NameEnumType.GROUP);
				d.setGroupType(GroupEnumType.DATA);
				out = (T)d;
				break;
			case PERMISSION:
				ApplicationPermissionType m = new ApplicationPermissionType();
				m.setPermissionType(PermissionEnumType.APPLICATION);
				m.setNameType(NameEnumType.PERMISSION);
				out = (T)m;
				break;
			default:
				logger.error("Unhandled type: " + factoryType);
				break;
		}
		return out;
	}

	public static void applyDefaultValues(IdentityConnectionType conn){
		/// Set default filters
		///
		
		logger.warn("Delegate default config to identity service provider.");
		
		/*
		 * 
		 * EXAMPLE ISIM 6 config
		 */
		 /*
		conn.setFilterPerson("(objectClass=erPersonItem)");
		conn.setFilterSystemUser("(objectClass=erSystemUser)");
		conn.setFilterSystemRole("(objectClass=erSystemRole)");
		conn.setFilterApplication("(objectClass=erServiceItem)");
		conn.setFilterApplicationGroup("(objectClass=erGroupItem)");
		conn.setFilterApplicationAccount("(objectClass=erAccountItem)");
		conn.setFilterRole("(objectClass=erRole)");

		/// Set default group maps
		MapType map = new MapType();
		map.setName("ersql2000databasename");
		map.setValue("erSQL2000DatabaseGroupAccount");
		conn.getApplicationPermissionMaps().add(map);
		
		map = new MapType(); 
		map.setName("eritamgroupname");
		map.setValue("eritamgroups");
		conn.getApplicationPermissionMaps().add(map);
		map = new MapType();
		map.setName("ersqldbrole");
		map.setValue("erSQLDatabaseRoleGroup");
		conn.getApplicationPermissionMaps().add(map);
		map = new MapType();
		map.setName("ersql2000serverrole");
		map.setValue("erSQLServerRoleGroup");
		conn.getApplicationPermissionMaps().add(map);
		map = new MapType();
		map.setName("ersqldbschema");
		map.setValue("erSQLDatabaseSchemaGroup");
		conn.getApplicationPermissionMaps().add(map);
		*/
	}
}
