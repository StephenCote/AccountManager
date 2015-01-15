package org.cote.accountmanager.services;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AuditEnumType;

public class AccountServiceImpl  {
	
	public static final String defaultDirectory = "~/Accounts";

	public static boolean delete(AccountType bean,HttpServletRequest request){
		
		return BaseService.delete(AuditEnumType.ACCOUNT, bean, request);
	}
	
	public static boolean add(AccountType bean,HttpServletRequest request){
		
		return BaseService.add(AuditEnumType.ACCOUNT, bean, request);
	}
	public static boolean update(AccountType bean,HttpServletRequest request){
		return BaseService.update(AuditEnumType.ACCOUNT, bean, request);
	}
	public static AccountType read(String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.ACCOUNT, name, request);
	}
	public static AccountType readByGroupId(long groupId, String name,HttpServletRequest request){
		return BaseService.readByName(AuditEnumType.ACCOUNT, groupId, name, request);
	}	
	public static AccountType readById(long id,HttpServletRequest request){
		return BaseService.readById(AuditEnumType.ACCOUNT, id, request);
	}
	
	public static List<AccountType> getGroupList(UserType user, String path, long startRecord, int recordCount){
		return BaseService.getGroupList(AuditEnumType.ACCOUNT, user, path, startRecord, recordCount);
	}
	public static int count(String groupId, HttpServletRequest request){
		return BaseService.countByGroup(AuditEnumType.ACCOUNT, groupId, request);
	}	
}
