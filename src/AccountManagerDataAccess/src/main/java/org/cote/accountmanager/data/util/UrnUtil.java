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
package org.cote.accountmanager.data.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.BinaryUtil;
public class UrnUtil {
	public static final Logger logger = LogManager.getLogger(UrnUtil.class);
	
	/// AM 5 URN Syntax
	/// am:type:org.path:parent.path:name
	
	public static final String urnPrefix = "am";
	public static final String urnSeparator = ":";
	public static final Pattern factoryPattern = Pattern.compile("^am:(\\S[^:]+):(\\S[^:]+)");

	private static String getEncodedGroupPath(BaseGroupType group) throws FactoryException, ArgumentException{
		return group.getGroupType().toString().toLowerCase() + urnSeparator + getDotGroupPath(group);
	}
	private static String getEncodedGroupPath(long groupId,long organizationId) throws FactoryException, ArgumentException{
		if(groupId == 0L) throw new ArgumentException("Invalid groupId: " + groupId);
		if(organizationId == 0L) throw new ArgumentException("Invalid organizationId: " + organizationId);
		BaseGroupType dir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getGroupById(groupId,organizationId);
		if(dir == null){
			throw new ArgumentException("Null group for " + groupId + " in " + organizationId);
		}
		return dir.getGroupType().toString().toLowerCase() + urnSeparator + getDotGroupPath(dir);
	}
	private static String getEncodedPermissionPath(BasePermissionType per) throws FactoryException, ArgumentException{
		return per.getPermissionType().toString().toLowerCase() + urnSeparator + getDotPermissionPath(per);
	}
	private static String getEncodedRolePath(BaseRoleType role) throws FactoryException, ArgumentException{
		return role.getRoleType().toString().toLowerCase() + urnSeparator + getDotRolePath(role);
	}
	
	public static String getUrn(NameIdType object){
		String urn = null;
		String key = null;
		try{
			switch(object.getNameType()){
				case USER:
					key = object.getName(); 
					break;
				case ORGANIZATION:
					urn = urnPrefix + urnSeparator + object.getNameType().toString() + urnSeparator + getDotOrganizationPath((OrganizationType)object);
					break;
				case PERMISSION:
					key = getEncodedPermissionPath((BasePermissionType)object);
					break;
				case ROLE:
					key = getEncodedRolePath((BaseRoleType)object);
					break;
				case GROUP:
					key = getEncodedGroupPath((BaseGroupType)object);
					break;
				case TRAIT:
				case ARTIFACT:
				case BUDGET:
				case CASE:
				case COST:
				case ESTIMATE:
				case FORM:
				case FORMELEMENT:
				case VALIDATIONRULE:
				case GOAL:
				case LIFECYCLE:
				case METHODOLOGY:
				case MODEL:
				case MODULE:
				case PROCESS:
				case PROCESSSTEP:
				case PROJECT:
				case REQUIREMENT:
				case RESOURCE:
				case SCHEDULE:
				case STAGE:
				case TICKET:
				case TIME:
				case WORK:
				case PERSON:
				case ACCOUNT:
				case CONTACT:
				case ADDRESS:
				case FACT:
				case FUNCTION:
				case OPERATION:
				case PATTERN:
				case POLICY:
				case RULE:
					NameIdDirectoryGroupType gobj = (NameIdDirectoryGroupType)object;
					key = getEncodedGroupPath(gobj.getGroupId(),gobj.getOrganizationId()) + urnSeparator + gobj.getName();
					break;
				case TAG:
					BaseTagType tobj = (BaseTagType)object;
					key = getEncodedGroupPath(tobj.getGroupId(),tobj.getOrganizationId()) + urnSeparator + tobj.getTagType().toString() + urnSeparator + tobj.getName();
					break;
				case DATA:
					/// 2015/06/29 - the data URN is intentionally encoded to accomodate mixed case names that are otherwise conflicting when cast to lower case.
					DataType data = (DataType)object;
					key = getEncodedGroupPath(data.getGroupId(),data.getOrganizationId()) + urnSeparator + BinaryUtil.toBase64Str(object.getName());
					break;
				case EVENT:
				case LOCATION:
				case NOTE:
				case TASK:
					NameIdDirectoryGroupType gobj2 = (NameIdDirectoryGroupType)object;
					key = getEncodedGroupPath(gobj2.getGroupId(),gobj2.getOrganizationId()) + urnSeparator + gobj2.getParentId() + urnSeparator + gobj2.getName();
					break;
				default:
					/// some objects, like ControlType, don't use URNs.
					logger.debug("Unhandled object type: " + object.getNameType());
					break;
			}
			if(key != null){
				urn = urnPrefix + urnSeparator + object.getNameType().toString()
					 + urnSeparator + getDotOrganizationPath(object.getOrganizationId())
					 + urnSeparator + key;
			}
			if(urn != null){
				urn = getNormalizedString(urn);
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getMessage());
		}
		return urn;
	}
	public static String getNormalizedString(String in){
		String outStr = in.toLowerCase().replaceAll("[\\s\\-]",".");
		return outStr.toLowerCase().replaceAll("[^A-Za-z0-9\\.\\:]+","");
	}
	private static String getDotGroupPath(BaseGroupType group) throws FactoryException, ArgumentException{
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).populate(group);
		((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).denormalize(group);
		//Factories
		return getDotPath(group.getPath());
	}
	private static String getDotPath(String path) throws ArgumentException{
		if(path == null){
			throw new ArgumentException("Null group path");
		}
		/// allow for an empty path, which represents the root
		if(path.length() == 0) return path;
		///
		return path.substring(1,path.length()).replace('/', '.');
	}
	private static String getDotPermissionPath(BasePermissionType per) throws FactoryException, ArgumentException{
		String path = ((PermissionFactory)Factories.getFactory(FactoryEnumType.PERMISSION)).getPermissionPath(per);
		if(path == null){
			throw new ArgumentException("Null permission path");
		}
		/// allow for an empty path, which represents the root
		if(path.length() == 0) return path;
		///
		return path.substring(1,path.length()).replace('/', '.');
	}
	private static String getDotRolePath(BaseRoleType role) throws FactoryException, ArgumentException{
		String path = ((RoleFactory)Factories.getFactory(FactoryEnumType.ROLE)).getRolePath(role);
		if(path == null){
			throw new ArgumentException("Null role path");
		}
		/// allow for an empty path, which represents the root
		if(path.length() == 0) return path;
		///
		return path.substring(1,path.length()).replace('/', '.');
	}

	public static String getDotOrganizationPath(long org) throws FactoryException, ArgumentException{
		return getDotOrganizationPath(((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationById(org));
	}
	public static String getDotOrganizationPath(OrganizationType org) throws FactoryException, ArgumentException{
		String path = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationPath(org);
		if(path == null || path.length() < 2){
			throw new ArgumentException("Unexpected organization path: " + path);
		}
		return path.substring(1,path.length()).replace('/', '.');
	}

	public static <T> T getByUrn(String urn) throws FactoryException {

		Matcher m = factoryPattern.matcher(urn);
		FactoryEnumType ftype = FactoryEnumType.UNKNOWN;
		if(m.find() && m.groupCount() >= 2){
			ftype = FactoryEnumType.fromValue(m.group(1).toUpperCase());
		}
		NameIdFactory fact = Factories.getFactory(ftype);
		T obj = null;
		if(fact != null) obj = fact.getByUrn(urn);
		else{
			logger.error("Null factory for type " + ftype.toString());
		}
		return obj;
		
	}
	public static boolean isUrn(String possibleUrn){
		return factoryPattern.matcher(possibleUrn).find();
	}
	
}
