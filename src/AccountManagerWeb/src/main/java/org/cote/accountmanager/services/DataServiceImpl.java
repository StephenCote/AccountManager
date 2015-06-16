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
package org.cote.accountmanager.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.util.DataUtil;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.beans.SessionBean;
import org.cote.util.BeanUtil;





public class DataServiceImpl  {
	public static int MAX_FEEDBACK_PER_SESSION = 3;
	public static final String defaultDirectory = "~/Datas";
	public static final Logger logger = Logger.getLogger(DataServiceImpl.class.getName());
	public static boolean delete(DataType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.DATA, bean, request);
	}
	public static DataType getProfile(UserType user){
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, "getProfile",AuditEnumType.SESSION,"Anonymous");
		AuditService.targetAudit(audit, AuditEnumType.DATA, "Read profile information");
		return getProfile(user,audit);
	}
	protected static void addDefaultProfileAttributes(DataType data){
		data.getAttributes().add(Factories.getAttributeFactory().newAttribute(data, "blog.title", "My blog title"));
		data.getAttributes().add(Factories.getAttributeFactory().newAttribute(data, "blog.subtitle", "My blog subtitle"));
		data.getAttributes().add(Factories.getAttributeFactory().newAttribute(data, "blog.author", "My pen name"));
		data.getAttributes().add(Factories.getAttributeFactory().newAttribute(data, "blog.signature", "My signature"));
		Factories.getAttributeFactory().addAttributes(data);
	}
	public static DataType getProfile(UserType user, AuditType audit){
		DataType data = null;
		try{
			Factories.getUserFactory().populate(user);
			data = Factories.getDataFactory().getDataByName(".profile", false, user.getHomeDirectory());
			if(data == null){
				data = Factories.getDataFactory().newData(user, user.getHomeDirectory());
				data.setMimeType("text/plain");
				data.setName(".profile");
				if(Factories.getDataFactory().addData(data)){
					data = Factories.getDataFactory().getDataByName(".profile",false,user.getHomeDirectory());
					addDefaultProfileAttributes(data);
				}
			}
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			AuditService.denyResult(audit,fe.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			AuditService.denyResult(audit,e.getMessage());
		}
		
		if(data != null){
			Factories.getAttributeFactory().populateAttributes(data);
			AuditService.permitResult(audit,"Returning " + user.getName() + " profile data");
		}
		else{
			AuditService.denyResult(audit,"Unable to retrieve profile information");
		}
		return data;
	}
	public static boolean addFeedback(DataType bean,HttpServletRequest request){
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.ADD, "Feedback", AuditEnumType.DATA,bean.getName());
		
		OrganizationType org = ServiceUtil.getOrganizationFromRequest(request);
		String feedbackUserName = request.getServletContext().getInitParameter("feedback.user");
		boolean out_bool = false;
		try {
			SessionBean usess = BeanUtil.getSessionBean(SessionSecurity.getUserSession(request.getSession(true).getId(), org),request.getSession(true).getId());
			int subCount = 0;
			String subCountStr = usess.getValue("feedback.submitted");
			if(subCountStr != null) subCount = Integer.parseInt(subCountStr);
			if(subCount > MAX_FEEDBACK_PER_SESSION){
				AuditService.denyResult(audit, "Maximum number of feedback submissions made for session " + request.getSession(true).getId());
				return false;
			}
			usess.setValue("feedback.submitted", Integer.toString(subCount + 1));
			Factories.getSessionFactory().updateData(usess);
			UserType user = Factories.getUserFactory().getUserByName(feedbackUserName, org);
			if(user == null){
				AuditService.denyResult(audit,"Feedback user '" + feedbackUserName + "' doesn't exist in organization " + org.getName());
				return out_bool;
			}
			Factories.getUserFactory().populate(user);
			DirectoryGroupType feedbackGroup = Factories.getGroupFactory().getCreateDirectory(user, "Feedback", user.getHomeDirectory(), org);
			if(feedbackGroup == null){
				AuditService.denyResult(audit, "Feedback group is null");
				return out_bool;
			}
			
			DataType feedback = Factories.getDataFactory().newData(user, feedbackGroup);
			DataUtil.setValue(feedback, DataUtil.getValue(bean));
			feedback.setMimeType("text/plain");
			UserType subUser = ServiceUtil.getUserFromSession(request);
			String sDesc = "Feedback submitted by " + (subUser == null ? "anonymous user":subUser.getName()) + " on " + (new Date()).toString();
			AuditService.targetAudit(audit, AuditEnumType.USER, (subUser == null ? "Anonymous" : subUser.getName()));
			feedback.setDescription(sDesc);
			feedback.setName("Feedback - " + UUID.randomUUID().toString());
			if(Factories.getDataFactory().addData(feedback)){
				feedback = Factories.getDataFactory().getDataByName(feedback.getName(), true, feedback.getGroup());
				if(feedback == null){
					AuditService.denyResult(audit, "Failed to lookup feedback data");
					
					return out_bool;
				}
				
				feedback.getAttributes().add(Factories.getAttributeFactory().newAttribute(feedback, "name", bean.getName()));
				if(Factories.getAttributeFactory().addAttributes(feedback)){
					AuditService.permitResult(audit, "Created feedback " + feedback.getName());
					out_bool = true;
				}
			}
			else{
				AuditService.denyResult(audit, "Failed to add new feedback " + feedback.getName());
			}
			
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return out_bool;
	}
	
	public static boolean add(DataType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.DATA, bean, request);
	}
	public static boolean update(DataType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.DATA, bean, request);
	}
	public static DataType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.DATA, name, request);
	}
	public static DataType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.DATA, groupId, name, request);
	}	
	public static DataType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.DATA, id, request);
	}
	public static int count(String group, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.DATA, group, request);
	}
	
	public static List<DataType> getGroupList(UserType user, ProcessingInstructionType instruction, boolean detailsOnly,String path, long startRecord, int recordCount){
		///return BaseService.getGroupList(AuditEnumType.DATA, user, path, startRecord, recordCount);
		

		List<DataType> out_obj = new ArrayList<DataType>();

		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.USER,(user == null ? "Null" : user.getName()));
		AuditService.targetAudit(audit, AuditEnumType.DATA, path);
		
		if(SessionSecurity.isAuthenticated(user) == false){
			AuditService.denyResult(audit, "User is null or not authenticated");
			return null;
		}
			
		try {
			DirectoryGroupType dir = (DirectoryGroupType)Factories.getGroupFactory().findGroup(user, GroupEnumType.DATA,path, user.getOrganization());
			if(dir == null){
				AuditService.denyResult(audit, "Invalid path: '" + path + "'");
				return out_obj;
			}
			///AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			if(AuthorizationService.canViewGroup(user, dir) == true){
				AuditService.permitResult(audit, "Access authorized to group " + dir.getName());
				out_obj = getListByGroup(dir,instruction,detailsOnly,startRecord,recordCount);
				//out_Lifecycles = Factories.getLifecycleFactory().getListByGroup(dir, 0, 0, user.getOrganization());
			}
			else{
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return out_obj;
			}
		} catch (ArgumentException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 

		return out_obj;
		
	}
	public static  List<DataType> getListByGroup(DirectoryGroupType group,ProcessingInstructionType instruction, boolean detailsOnly, long startRecord, int recordCount) throws ArgumentException, FactoryException {

		List<DataType> out_obj = Factories.getDataFactory().getDataListByGroup(group, instruction,detailsOnly,startRecord, recordCount, group.getOrganization());
		for(int i = 0; i < out_obj.size();i++){
			DataType ngt = out_obj.get(i);
			if(ngt.getGroup().getPopulated() == true){
				ngt.getGroup().setParentGroup(null);
				ngt.getGroup().getSubDirectories().clear();
				ngt.setPopulated(false);
			}
		}
		return out_obj;			
	}
	
	
}
