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
package org.cote.accountmanager.data.fact;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.DataAccessException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.ControlFactory;
import org.cote.accountmanager.data.factory.CredentialFactory;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.data.factory.NameIdFactory;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.factory.PermissionFactory;
import org.cote.accountmanager.data.factory.RoleFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.objects.BasePermissionType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.PermissionEnumType;
import org.cote.accountmanager.objects.types.RoleEnumType;

public class FactUtil {
	public static final Logger logger = Logger.getLogger(FactUtil.class.getName());
	public static final Pattern idPattern = Pattern.compile("^\\d+$");
	
	public static void setFactReference(FactType sourceFact, FactType matchFact){
		if(sourceFact.getFactReference() != null) return;
		
		NameIdType obj = factoryRead(sourceFact,matchFact);
		if(obj == null){
			logger.error("Failed to find object " + sourceFact.getSourceUrn() + " (" + sourceFact.getFactoryType().toString() + ") in organization " + matchFact.getOrganizationId());
			return;
		}
		logger.info("Found object " + sourceFact.getSourceUrn() + " (" + sourceFact.getFactoryType().toString() + ") in organization " + matchFact.getOrganizationId() + " having id " + obj.getId());
		sourceFact.setFactReference(obj);
	}
	public static String getFactAttributeValue(FactType sourceFact, FactType matchFact){
		setFactReference(sourceFact, matchFact);
		if(sourceFact.getFactReference() == null)
			return null;
		Factories.getAttributeFactory().populateAttributes(sourceFact.getFactReference());
		return Factories.getAttributeFactory().getAttributeValueByName(sourceFact.getFactReference(), matchFact.getSourceUrn());
	}
	
	public static String getFactValue(FactType sourceFact, FactType matchFact){
		String out_val = null;
		/// Fact value is driven by a combination of what the source fact has and what  the matchFact expects
		/// The source fact provides context, and the match fact provides specificity
		///
		switch(matchFact.getFactType()){
			case STATIC:
				out_val = sourceFact.getFactData();
				break;
			case ATTRIBUTE:
				out_val = getFactAttributeValue(sourceFact, matchFact);
				break;
			default:
				logger.error("Unhandled fact type: " + matchFact.getFactType());
				break;
		}
		return out_val;
	}
	public static String getMatchFactValue(FactType sourceFact, FactType matchFact){
		String out_val = null;
		switch(matchFact.getFactType()){
			/// Note: The match of an attribute fact is presently the static value
			/// This is because the source type got cross-purposed to parameter
			case ATTRIBUTE:
			case STATIC:
				out_val = matchFact.getFactData();
				break;
			default:
				logger.error("Unhandled fact type: " + matchFact.getFactType());
				break;
		}
		return out_val;
	}
	
	private static DirectoryGroupType getDirectoryFromFact(FactType sourceFact, FactType referenceFact) throws FactoryException, ArgumentException{
		if(sourceFact.getSourceUrl() == null){
			logger.error("Source URL is null");
			return null;
		}
		DirectoryGroupType dir = (DirectoryGroupType)Factories.getGroupFactory().findGroup(null, GroupEnumType.DATA, sourceFact.getSourceUrl(), referenceFact.getOrganizationId());
		if(dir == null)
			throw new ArgumentException("Invalid group path " + sourceFact.getSourceUrl());
		return dir;
	}
	private static BasePermissionType getPermissionFromFact(FactType sourceFact, FactType referenceFact) throws FactoryException, ArgumentException, DataAccessException{
		
		BasePermissionType permission = (BasePermissionType)Factories.getPermissionFactory().findPermission(PermissionEnumType.fromValue(sourceFact.getSourceType()), sourceFact.getSourceUrl(), referenceFact.getOrganizationId());
		if(permission == null) throw new ArgumentException("Invalid permission path " + sourceFact.getSourceUrl());
		return permission;
	}
	private static BaseRoleType getRoleFromFact(FactType sourceFact, FactType referenceFact) throws FactoryException, ArgumentException, DataAccessException{
		BaseRoleType role = (BaseRoleType)Factories.getRoleFactory().findRole(RoleEnumType.fromValue(sourceFact.getSourceType()), sourceFact.getSourceUrl(), referenceFact.getOrganizationId());
		if(role == null) throw new ArgumentException("Invalid role path " + sourceFact.getSourceUrl());
		return role;
	}
	/* 
	 * NOTE: Authorization factories intentionally not included in the lookup by name for rules
	 */

	public static <T> T factoryRead(FactType sourceFact,final FactType referenceFact){
		T out_obj = null;
		boolean lookupRef = false;
		FactType useRef = (lookupRef ? referenceFact : sourceFact);
		if(sourceFact.getFactoryType() == FactoryEnumType.UNKNOWN || referenceFact.getFactoryType() == FactoryEnumType.UNKNOWN){
			logger.error("Source fact (" + sourceFact.getFactoryType() + ") or reference fact (" + referenceFact.getFactoryType() + ") is not configured for a factory read operation");
			return null;
		}
		if(sourceFact.getSourceUrn() == null){
			logger.error("Source URN is null for " + sourceFact.getUrn());
			return out_obj;
		}
		try {
			NameIdFactory fact = Factories.getFactory(useRef.getFactoryType());
			DirectoryGroupType dir = null;
			if(idPattern.matcher(sourceFact.getSourceUrn()).matches()){
				out_obj = fact.getById(Long.parseLong(sourceFact.getSourceUrn()), referenceFact.getOrganizationId());
			}
			else{
				switch(useRef.getFactoryType()){
					/// User is one of the only organization level schemas with a unique constraint on just the name
					///
					case USER:
						out_obj = (T)((UserFactory)fact).getByName(sourceFact.getSourceUrn(), referenceFact.getOrganizationId());
						break;
					case CREDENTIAL:
						out_obj = (T)((CredentialFactory)fact).getByObjectId(sourceFact.getSourceUrn(), referenceFact.getOrganizationId());
						break;
					case CONTROL:
						out_obj = (T)((ControlFactory)fact).getControlByObjectId(sourceFact.getSourceUrn(), referenceFact.getOrganizationId());
						break;		
					/// NameIdGroupFactory types
					case ACCOUNT:
					case CONTACT:
					case PERSON:
					case ADDRESS:
						if((sourceFact.getSourceUrl() == null || sourceFact.getSourceUrl().length() == 0) && sourceFact.getSourceUrn() != null){
							out_obj = (T)((NameIdGroupFactory)fact).getByUrn(sourceFact.getSourceUrn());
						}
						else{
							dir =  getDirectoryFromFact(sourceFact,referenceFact);
							out_obj = (T)((NameIdGroupFactory)fact).getByNameInGroup(sourceFact.getSourceUrn(), dir);
							logger.debug("Looking for " + useRef.getFactoryType() + " " + sourceFact.getSourceUrn() + " in " + (dir != null ? dir.getPath() : "Null Dir") + " - Result is " + (out_obj == null ? "Null":"Found"));
						}
						break;
					/// Data is a predecessor to the NameIdGroupFactory type, but it doesn't inherity from that base class
					case DATA:
						if((sourceFact.getSourceUrl() == null || sourceFact.getSourceUrl().length() == 0) && sourceFact.getSourceUrn() != null){
							out_obj = (T)((DataFactory)fact).getByUrn(sourceFact.getSourceUrn());
						}
						else{
							out_obj = (T)((DataFactory)fact).getDataByName(sourceFact.getSourceUrn(), getDirectoryFromFact(sourceFact,referenceFact));
						}
						break;
					case GROUP:
						if((sourceFact.getSourceUrl() == null || sourceFact.getSourceUrl().length() == 0) && sourceFact.getSourceUrn() != null){
							out_obj = (T)((GroupFactory)fact).getByUrn(sourceFact.getSourceUrn());
						}
						else{
							dir =  getDirectoryFromFact(sourceFact,referenceFact);
							out_obj = (T)((GroupFactory)fact).getDirectoryByName(sourceFact.getSourceUrn(), dir,referenceFact.getOrganizationId());
							logger.debug("Looking for " + useRef.getFactoryType() + " " + sourceFact.getSourceUrn() + " in " + (dir != null ? dir.getPath() : "Null Dir") + " - Result is " + (out_obj == null ? "Null":"Found"));
						}
						break;
					case ROLE:
						if((sourceFact.getSourceUrl() == null || sourceFact.getSourceUrl().length() == 0) && sourceFact.getSourceUrn() != null){
							out_obj = (T)((RoleFactory)fact).getByUrn(sourceFact.getSourceUrn());
						}
						else{
							BaseRoleType parent = getRoleFromFact(sourceFact, referenceFact);
							out_obj = (T)((RoleFactory)fact).getRoleByName(sourceFact.getSourceUrn(), parent, RoleEnumType.fromValue(sourceFact.getSourceType()), referenceFact.getOrganizationId());
							logger.debug("Looking for " + useRef.getFactoryType() + " " + sourceFact.getSourceUrn() + " in " + (parent != null ? sourceFact.getSourceUrl() : "Null Role") + " - Result is " + (out_obj == null ? "Null":"Found"));
						}
						break;
					case PERMISSION:
						if((sourceFact.getSourceUrl() == null || sourceFact.getSourceUrl().length() == 0) && sourceFact.getSourceUrn() != null){
							out_obj = (T)((PermissionFactory)fact).getByUrn(sourceFact.getSourceUrn());
						}
						else{
							BasePermissionType perparent = getPermissionFromFact(sourceFact, referenceFact);
							out_obj = (T)((PermissionFactory)fact).getPermissionByName(sourceFact.getSourceUrn(),PermissionEnumType.fromValue(sourceFact.getSourceType()), perparent, referenceFact.getOrganizationId());
							logger.debug("Looking for " + useRef.getFactoryType() + " " + sourceFact.getSourceUrn() + " in " + (perparent != null ? sourceFact.getSourceUrl() : "Null Permission") + " - Result is " + (out_obj == null ? "Null":"Found"));
						}
						break;

					default:
						throw new ArgumentException("Unhandled factory type " + useRef.getFactoryType());
				}
			}
		} catch (FactoryException | ArgumentException | DataAccessException e) {

			logger.error(e.getMessage());
			logger.error("Trace",e);
		}
		return out_obj;
	}

}
