package org.cote.accountmanager.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cote.accountmanager.objects.RuleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public class RuleServiceImpl  {
	
	public static final String defaultDirectory = "~/Rules";

	public static boolean delete(RuleType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.RULE, bean, request);
	}
	
	public static boolean add(RuleType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.RULE, bean, request);
	}
	public static boolean update(RuleType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.RULE, bean, request);
	}
	public static RuleType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.RULE, name, request);
	}
	public static RuleType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.RULE, groupId, name, request);
	}	
	public static RuleType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.RULE, id, request);
	}
	
	public static List<RuleType> getGroupList(UserType user, String path, int startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.RULE, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.RULE, groupId, request);
	}	
}
