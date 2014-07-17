package org.cote.accountmanager.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public class FunctionServiceImpl  {
	
	public static final String defaultDirectory = "~/Functions";

	public static boolean delete(FunctionType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.FUNCTION, bean, request);
	}
	
	public static boolean add(FunctionType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.FUNCTION, bean, request);
	}
	public static boolean update(FunctionType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.FUNCTION, bean, request);
	}
	public static FunctionType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.FUNCTION, name, request);
	}
	public static FunctionType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.FUNCTION, groupId, name, request);
	}	
	public static FunctionType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.FUNCTION, id, request);
	}
	
	public static List<FunctionType> getGroupList(UserType user, String path, int startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.FUNCTION, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.FUNCTION, groupId, request);
	}	
}
