package org.cote.accountmanager.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.factory.NameIdGroupFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdDirectoryGroupType;
import org.cote.accountmanager.objects.ProcessingInstructionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.DataType;





public class DataServiceImpl  {
	
	public static final String defaultDirectory = "~/Datas";

	public static boolean delete(DataType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.DATA, bean, request);
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
	
	public static List<DataType> getGroupList(UserType user, ProcessingInstructionType instruction, boolean detailsOnly,String path, int startRecord, int recordCount){
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
	private static  List<DataType> getListByGroup(DirectoryGroupType group,ProcessingInstructionType instruction, boolean detailsOnly, int startRecord, int recordCount) throws ArgumentException, FactoryException {

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
