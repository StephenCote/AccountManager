package org.cote.accountmanager.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cote.accountmanager.objects.BaseTagType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public class TagServiceImpl  {
	
	public static final String defaultDirectory = "~/Tags";

	public static boolean delete(BaseTagType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.TAG, bean, request);
	}
	
	public static boolean add(BaseTagType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.TAG, bean, request);
	}
	public static boolean update(BaseTagType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.TAG, bean, request);
	}
	public static BaseTagType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.TAG, name, request);
	}
	public static BaseTagType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.TAG, groupId, name, request);
	}	
	public static BaseTagType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.TAG, id, request);
	}
	
	public static List<BaseTagType> getGroupList(UserType user, String path, long startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.TAG, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.TAG, groupId, request);
	}	
}
