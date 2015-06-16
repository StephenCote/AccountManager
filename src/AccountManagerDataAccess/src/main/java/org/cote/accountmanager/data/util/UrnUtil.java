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
package org.cote.accountmanager.data.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.NameIdFactory;
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
	public static final Logger logger = Logger.getLogger(UrnUtil.class.getName());
	
	/// AM 5 URN Syntax
	/// am:type:org.path:parent.path:name
	
	public static String urnPrefix = "am";
	public static String urnSeparator = ":";
	
	public static Pattern factoryPattern = Pattern.compile("^am:(\\S[^:]+):(\\S[^:]+)");
	/*
	public static Pattern bucketPattern = Pattern.compile("^am:(\\S[^:]+):(\\S[^:]+):(\\S[^:]+):(\\S[^:]+)");
	public static Pattern groupedObjectPattern = Pattern.compile("^am:(\\S[^:]+):(\\S[^:]+):(\\S[^:]+):(\\S[^:]+):(\\S[^:]+)");
	*/
	private static String getEncodedGroupPath(BaseGroupType group) throws FactoryException, ArgumentException{
		return group.getGroupType().toString().toLowerCase() + urnSeparator + getDotGroupPath(group);
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
					key = getEncodedGroupPath(gobj.getGroup()) + urnSeparator + gobj.getName();
					break;
				case TAG:
					BaseTagType tobj = (BaseTagType)object;
					key = getEncodedGroupPath(tobj.getGroup()) + urnSeparator + tobj.getTagType().toString() + urnSeparator + tobj.getName();
					break;
				case DATA:
					key = getEncodedGroupPath(((DataType)object).getGroup()) + urnSeparator + BinaryUtil.toBase64Str(object.getName());
					break;
				case NOTE:
				case TASK:
					NameIdDirectoryGroupType gobj2 = (NameIdDirectoryGroupType)object;
					key = getEncodedGroupPath(gobj2.getGroup()) + urnSeparator + gobj2.getParentId() + urnSeparator + gobj2.getName();
					break;
				default:
					logger.error("Unhandled object type: " + object.getNameType());
					break;
			}
			if(key != null){
				urn = urnPrefix + urnSeparator + object.getNameType().toString()
					 + urnSeparator + getDotOrganizationPath(object.getOrganization())
					 + urnSeparator + key;
				urn = getNormalizedString(urn);
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		return urn;
	}
	public static String getNormalizedString(String in){
		return in.toLowerCase().replaceAll("[^A-Za-z0-9\\.\\:]+","");
	}
	private static String getDotGroupPath(BaseGroupType group) throws FactoryException, ArgumentException{
		Factories.getGroupFactory().populate(group);

		String path = group.getPath();
		if(path == null){
			throw new ArgumentException("Null group path");
		}
		/// allow for an empty path, which represents the root
		if(path.length() == 0) return path;
		///
		return path.substring(1,path.length()).replace('/', '.');
	}
	private static String getDotPermissionPath(BasePermissionType per) throws FactoryException, ArgumentException{
		String path = Factories.getPermissionFactory().getPermissionPath(per);
		if(path == null){
			throw new ArgumentException("Null permission path");
		}
		/// allow for an empty path, which represents the root
		if(path.length() == 0) return path;
		///
		return path.substring(1,path.length()).replace('/', '.');
	}
	private static String getDotRolePath(BaseRoleType role) throws FactoryException, ArgumentException{
		String path = Factories.getRoleFactory().getRolePath(role);
		if(path == null){
			throw new ArgumentException("Null role path");
		}
		/// allow for an empty path, which represents the root
		if(path.length() == 0) return path;
		///
		return path.substring(1,path.length()).replace('/', '.');
	}

	public static String getDotOrganizationPath(OrganizationType org) throws FactoryException, ArgumentException{
		String path = Factories.getOrganizationFactory().getOrganizationPath(org);
		if(path == null || path.length() < 2){
			throw new ArgumentException("Unexpected organization path: " + path);
		}
		return path.substring(1,path.length()).replace('/', '.');
	}
	/*
	public static OrganizationType getOrganization(String urn) throws FactoryException, ArgumentException{
		String orgPath = extractOrganizationPath(urn);
		if(orgPath == null){
			logger.error("Null organization path in urn " + urn);
			return null;
		}
		orgPath = "/" + orgPath.replace('.', '/');
		return Factories.getOrganizationFactory().findOrganization(orgPath);
	}
	public static String extractOrganizationPath(String urn){
		Matcher m = organizationPattern.matcher(urn);

		if(m.find()){
			if(m.groupCount() > 1){
		
				return m.group(2);
			}
			else{
				logger.error("Unexpceted group count: " + m.groupCount());
			}
		}
		else{
			logger.error("No match from urn " + urn);
		}
		return null;
	}
	*/
	public static <T> T getByUrn(String urn) {

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
	/*
	private static <T> T getObject(Pattern pattern, String urn) throws FactoryException, ArgumentException { 
		T obj = null;
		String bucketPath = null;
		String bucketType = null;
		//String orgPath = null;
		String objType = null;
		Matcher m = bucketPattern.matcher(urn);
		if(m.find() && m.groupCount() > 3){
			objType = m.group(1);
			//orgPath = m.group(2);
			bucketType = m.group(3);
			bucketPath = m.group(4);
		}
		if( bucketPath == null || bucketType == null){
			logger.error("Null bucket path or type with " + bucketPath + " as type " + bucketType);
			return null;
		}
		//orgPath = "/" + orgPath.replace('.', '/');
		bucketPath = "/" + bucketPath.replace('.', '/');
		OrganizationType org = getOrganization(urn);
		///Factories.getOrganizationFactory().findOrganization(orgPath);
		NameEnumType nameType = NameEnumType.fromValue(objType.toUpperCase());
		try{
			switch(nameType){
				case PERMISSION:
					obj = (T)Factories.getPermissionFactory().findPermission(PermissionEnumType.fromValue(bucketType.toUpperCase()), bucketPath, org);
					break;
				case ROLE:
					obj = (T)Factories.getRoleFactory().findRole(RoleEnumType.fromValue(bucketType.toUpperCase()), bucketPath, org);
					break;
			}
		}
		catch(DataAccessException e){
			logger.error(e.getMessage());
		}
		return obj;
	}
	public static <T> T getGroupObject(String urn) throws FactoryException, ArgumentException{
		T obj = null;

		String groupPath = null;
		String groupType = null;
		String itemName = null;
		//String orgPath = null;
		String objType = null;
		Matcher m = bucketPattern.matcher(urn);
		if(m.find() && m.groupCount() > 3){
			objType = m.group(1);
			//orgPath = m.group(2);
			groupType = m.group(3);
			groupPath = m.group(4);
			//if(m.groupCount() > 4) itemName = m.group(5);
		}
		m = groupedObjectPattern.matcher(urn);
		if(m.find() && m.groupCount() > 4){
			itemName = m.group(5);
		}
		if( groupPath == null || groupType == null){
			logger.error("Null group path or type from " + urn + " with " + groupPath + " as type " + groupType);
			return null;
		}
		
		//orgPath = "/" + orgPath.replace('.', '/');
		groupPath = "/" + groupPath.replace('.', '/');
		OrganizationType org = getOrganization(urn);
		///Factories.getOrganizationFactory().findOrganization(orgPath);
		NameEnumType nameType = NameEnumType.fromValue(objType.toUpperCase());

		BaseGroupType group = Factories.getGroupFactory().findGroup(null,GroupEnumType.fromValue(groupType.toUpperCase()),groupPath,getOrganization(urn));

		switch(nameType){

			default:
				NameIdGroupFactory fact = Factories.getFactory(FactoryEnumType.fromValue(objType.toUpperCase()));
				obj = fact.getByName(itemName, (DirectoryGroupType)group);
				break;
		}

		return obj;
	}
	
	public static String extractGroupPath(String urn){
		Matcher m = bucketPattern.matcher(urn);

		if(m.matches() && m.groupCount() > 3){
			return m.group(4);
		}
		return null;
	}
	*/
}
