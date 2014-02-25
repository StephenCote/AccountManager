package org.cote.accountmanager.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;


public class BlogServiceImpl{
	public static final Logger logger = Logger.getLogger(BlogServiceImpl.class.getName());
	private static String blogPath = "Blog";
	
	public static DataType read(long orgId, String userName, String name){
		DataType data = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, userName, AuditEnumType.USER, userName + " " + "/Home/" + userName + "/" + blogPath + " " + name);
		try{
			OrganizationType org = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(org == null){
				AuditService.denyResult(audit, "Organization #" + orgId + " does not exist");
				return data;
			}
			UserType user = Factories.getUserFactory().getUserByName(userName, org);
			if(user == null){
				AuditService.denyResult(audit, "User " + userName + " does not exist in Organization #" + orgId);
				return data;
				
			}
			UserType docUser = Factories.getDocumentControl(org);
			DirectoryGroupType dir = (DirectoryGroupType)Factories.getGroupFactory().findGroup(user, GroupEnumType.DATA, "/Home/" + userName + "/" + blogPath, org);
			if(dir != null){
				AuditService.permitResult(audit, "Proxy anonymous request through document control user");
				data = BaseService.readByName(audit,AuditEnumType.DATA,docUser,dir,name,null);
			}
		}
		catch(ArgumentException ae){
			AuditService.denyResult(audit, ae.getMessage());
			logger.error(ae.getMessage());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			AuditService.denyResult(audit, e.getMessage());
			logger.error(e.getMessage());
		}
		return data;
	}
	
	public static List<DataType> list(long orgId, String userName,boolean detailsOnly,int startIndex,int recordCount){
		List<DataType> data = new ArrayList<DataType>();
		
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, userName, AuditEnumType.USER, "/Home/" + userName + "/" + blogPath);
		try{
			OrganizationType org = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(org == null){
				AuditService.denyResult(audit, "Organization #" + orgId + " does not exist");
				return data;
			}
			UserType user = Factories.getUserFactory().getUserByName(userName, org);
			if(user == null){
				AuditService.denyResult(audit, "User " + userName + " does not exist in Organization #" + orgId);
				return data;
				
			}
			UserType docUser = Factories.getDocumentControl(org);
			//DirectoryGroupType dir = (DirectoryGroupType)Factories.getGroupFactory().findGroup(user, GroupEnumType.DATA, blogPath, org);
			//if(dir != null){
			//UserType user = ServiceUtil.getUserFromSession(request);
				ProcessingInstructionType instruction = new ProcessingInstructionType();
				/// TODO: SQL Injection point - don't ever directly take user input for the values of the instruction object
				/// this needs to be changed over to use the QueryField objects
				///
				instruction.setOrderClause("createddate DESC");
				AuditService.permitResult(audit, "Proxy anonymous request through document control user");
				data = DataServiceImpl.getGroupList(docUser, instruction,detailsOnly,"/Home/" + userName + "/" + blogPath, startIndex, recordCount );
			//}
		}
		catch(ArgumentException ae){
			AuditService.denyResult(audit, ae.getMessage());
			logger.error(ae.getMessage());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			AuditService.denyResult(audit, e.getMessage());
			logger.error(e.getMessage());
		}
		return data;
	}
}