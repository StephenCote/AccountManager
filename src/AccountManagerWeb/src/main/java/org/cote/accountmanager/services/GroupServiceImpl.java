package org.cote.accountmanager.services;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.util.ServiceUtil;
import org.cote.util.BeanUtil;





public class GroupServiceImpl  {
	
	public static final Logger logger = Logger.getLogger(GroupServiceImpl.class.getName());
	
	
	public static boolean delete(BaseGroupType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.GROUP, bean, request);
	}
	public static boolean add(BaseGroupType bean, HttpServletRequest request){
		return BaseService.add(AuditEnumType.GROUP, bean, request);
	}
	public static boolean update(BaseGroupType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.GROUP, bean, request);
	}
	public static int countInParent(long orgId, long parentId, HttpServletRequest request){
		OrganizationType org = null;
		BaseGroupType group = null;
		try{
			org = Factories.getOrganizationFactory().getOrganizationById(orgId);
			if(org != null) group = Factories.getGroupFactory().getById(parentId, org);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		if(group == null){
			System.out.println("Invalid parentId reference: " + parentId);
			return 0;
		}
		return BaseService.countInParent(AuditEnumType.GROUP, group, request);
	}
	public static BaseGroupType findGroup(GroupEnumType groupType, String path, HttpServletRequest request){
		BaseGroupType bean = null;
		if(path == null || path.length() == 0) path = "~";
		if(path.startsWith("~") == false && path.startsWith("/") == false) path = "/" + path;
		//logger.error("Path = '" + path + "'");
		AuditType audit = AuditService.beginAudit(ActionEnumType.READ, path,AuditEnumType.SESSION,request.getSession(true).getId());
		UserType user = ServiceUtil.getUserFromSession(audit, request);
		if(user == null) return bean;
		try {
			BaseGroupType dir = Factories.getGroupFactory().findGroup(user, groupType, path, user.getOrganization());
			if(dir == null){
				AuditService.denyResult(audit, "Invalid path: " + groupType.toString() + " " + path);
				return bean;
			}
			if(AuthorizationService.canViewGroup(user, dir) == false){
				AuditService.denyResult(audit, "User " + user.getName() + " (#" + user.getId() + ") not authorized to view group " + dir.getName() + " (#" + dir.getId() + ")");
				return bean;
			}
			Factories.getGroupFactory().populate(dir);	
			/// Work with a clone of the group because if it's cached, don't null out the cached copy's version
			dir = BeanUtil.getBean(DirectoryGroupType.class,dir);

			//Factories.getGroupFactory().get
			if(dir.getGroupType() == GroupEnumType.DATA){
				DirectoryGroupType ddir = (DirectoryGroupType)dir;
				/*
				Factories.getGroupFactory().populateSubDirectories(ddir);
				for(int i = 0; i < ddir.getSubDirectories().size();i++){
					Factories.getGroupFactory().populate(ddir.getSubDirectories().get(i));
				}
				*/
				bean = BeanUtil.getSanitizedGroup(ddir,false);
			}
			else{
				bean = dir;
			}
			AuditService.targetAudit(audit, AuditEnumType.GROUP, dir.getName() + " (#" + dir.getId() + ")");
			AuditService.permitResult(audit, "Access authorized to group " + dir.getName());
			
			
			
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return bean;
	}
	public static List<BaseGroupType> listInGroup(GroupEnumType groupType, String path, long startIndex, int recordCount, HttpServletRequest request){
		BaseGroupType dir = findGroup(groupType, path, request);
		List<BaseGroupType> dirs = new ArrayList<BaseGroupType>();
		if(dir == null) return dirs;
		try {
			dirs = Factories.getGroupFactory().getListByParent(groupType, dir,  startIndex, recordCount, dir.getOrganization());
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return dirs;
	}
	

	
}
