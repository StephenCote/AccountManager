package org.cote.accountmanager.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cote.accountmanager.objects.OperationType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public class OperationServiceImpl  {
	
	public static final String defaultDirectory = "~/Operations";

	public static boolean delete(OperationType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.OPERATION, bean, request);
	}
	
	public static boolean add(OperationType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.OPERATION, bean, request);
	}
	public static boolean update(OperationType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.OPERATION, bean, request);
	}
	public static OperationType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.OPERATION, name, request);
	}
	public static OperationType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.OPERATION, groupId, name, request);
	}	
	public static OperationType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.OPERATION, id, request);
	}
	
	public static List<OperationType> getGroupList(UserType user, String path, int startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.OPERATION, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.OPERATION, groupId, request);
	}	
}
