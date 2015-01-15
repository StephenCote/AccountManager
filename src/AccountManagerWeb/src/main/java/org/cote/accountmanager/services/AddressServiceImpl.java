package org.cote.accountmanager.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cote.accountmanager.objects.AddressType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public class AddressServiceImpl  {
	
	public static final String defaultDirectory = "~/Addresss";

	public static boolean delete(AddressType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.ADDRESS, bean, request);
	}
	
	public static boolean add(AddressType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.ADDRESS, bean, request);
	}
	public static boolean update(AddressType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.ADDRESS, bean, request);
	}
	public static AddressType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.ADDRESS, name, request);
	}
	public static AddressType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.ADDRESS, groupId, name, request);
	}	
	public static AddressType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.ADDRESS, id, request);
	}
	
	public static List<AddressType> getGroupList(UserType user, String path, long startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.ADDRESS, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.ADDRESS, groupId, request);
	}	
}
