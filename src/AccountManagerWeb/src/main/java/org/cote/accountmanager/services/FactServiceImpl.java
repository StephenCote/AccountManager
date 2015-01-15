package org.cote.accountmanager.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FactType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;




public class FactServiceImpl  {
	
	public static final String defaultDirectory = "~/Facts";

	public static boolean delete(FactType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.FACT, bean, request);
	}
	
	public static boolean add(FactType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.FACT, bean, request);
	}
	public static boolean update(FactType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.FACT, bean, request);
	}
	public static FactType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.FACT, name, request);
	}
	public static FactType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.FACT, groupId, name, request);
	}	
	public static FactType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.FACT, id, request);
	}
	
	public static List<FactType> getGroupList(UserType user, String path, long startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.FACT, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.FACT, groupId, request);
	}	
}
