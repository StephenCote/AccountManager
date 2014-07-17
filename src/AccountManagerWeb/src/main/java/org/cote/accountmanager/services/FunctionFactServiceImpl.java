package org.cote.accountmanager.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cote.accountmanager.objects.FunctionFactType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public class FunctionFactServiceImpl  {
	
	public static final String defaultDirectory = "~/FunctionFacts";

	public static boolean delete(FunctionFactType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.FUNCTIONFACT, bean, request);
	}
	
	public static boolean add(FunctionFactType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.FUNCTIONFACT, bean, request);
	}
	public static boolean update(FunctionFactType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.FUNCTIONFACT, bean, request);
	}
	public static FunctionFactType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.FUNCTIONFACT, name, request);
	}
	public static FunctionFactType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.FUNCTIONFACT, groupId, name, request);
	}	
	public static FunctionFactType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.FUNCTIONFACT, id, request);
	}
	
	public static List<FunctionFactType> getGroupList(UserType user, String path, int startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.FUNCTIONFACT, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.FUNCTIONFACT, groupId, request);
	}	
}
