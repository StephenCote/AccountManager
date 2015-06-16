/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
