package org.cote.accountmanager.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cote.accountmanager.objects.PatternType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public class PatternServiceImpl  {
	
	public static final String defaultDirectory = "~/Patterns";

	public static boolean delete(PatternType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.PATTERN, bean, request);
	}
	
	public static boolean add(PatternType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.PATTERN, bean, request);
	}
	public static boolean update(PatternType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.PATTERN, bean, request);
	}
	public static PatternType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.PATTERN, name, request);
	}
	public static PatternType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.PATTERN, groupId, name, request);
	}	
	public static PatternType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.PATTERN, id, request);
	}
	
	public static List<PatternType> getGroupList(UserType user, String path, long startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.PATTERN, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.PATTERN, groupId, request);
	}	
}
