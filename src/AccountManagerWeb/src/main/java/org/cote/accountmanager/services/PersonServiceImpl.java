package org.cote.accountmanager.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public class PersonServiceImpl  {
	
	public static final String defaultDirectory = "~/Persons";

	public static boolean delete(PersonType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.PERSON, bean, request);
	}
	
	public static boolean add(PersonType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.PERSON, bean, request);
	}
	public static boolean update(PersonType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.PERSON, bean, request);
	}
	public static PersonType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.PERSON, name, request);
	}
	public static PersonType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.PERSON, groupId, name, request);
	}	
	public static PersonType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.PERSON, id, request);
	}
	
	public static List<PersonType> getGroupList(UserType user, String path, int startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.PERSON, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.PERSON, groupId, request);
	}	
}
