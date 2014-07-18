package org.cote.accountmanager.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public class ContactServiceImpl  {
	
	public static final String defaultDirectory = "~/Contacts";

	public static boolean delete(ContactType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.CONTACT, bean, request);
	}
	
	public static boolean add(ContactType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.CONTACT, bean, request);
	}
	public static boolean update(ContactType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.CONTACT, bean, request);
	}
	public static ContactType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.CONTACT, name, request);
	}
	public static ContactType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.CONTACT, groupId, name, request);
	}	
	public static ContactType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.CONTACT, id, request);
	}
	
	public static List<ContactType> getGroupList(UserType user, String path, int startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.CONTACT, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.CONTACT, groupId, request);
	}	
}
