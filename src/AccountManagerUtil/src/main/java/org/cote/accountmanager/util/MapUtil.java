package org.cote.accountmanager.util;

import org.cote.accountmanager.objects.AccountType;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.ListDataType;
import org.cote.accountmanager.objects.ListItemType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.GroupEnumType;
import org.cote.accountmanager.objects.types.NameEnumType;
public class MapUtil {
	public static ListItemType convertToListItemXXXXX(NameIdType map){
		return null;
		/*
		ListItemType new_item = new ListItemType();
		new_item.setName(map.getName());
		new_item.setId(map.getId());
		new_item.setOwnerId(map.getOwnerId());
		if(map.getOrganization() != null) new_item.setOrganizationId(map.getOrganization().getId());
		
		switch(map.getNameType()){
			case ACCOUNT:
				AccountType account = (AccountType)map;
				new_item.getData().add(NewListItemData("Status", account.getAccountStatus().toString()));
				//new_item.setDate(account.getCreatedDate());

				if(account.getContactInformation() != null){
					new_item.getData().add(NewListItemData("FirstName", account.getContactInformation().getFirstName()));
					new_item.getData().add(NewListItemData("LastName", account.getContactInformation().getLastName()));
					new_item.getData().add(NewListItemData("Email", account.getContactInformation().getEmail()));
					new_item.getData().add(NewListItemData("Website", account.getContactInformation().getWebsite()));
				}
			case USER:
				UserType user = (UserType)map;
				if(user.getHomeDirectory() != null){
					new_item.setGroupId(user.getHomeDirectory().getId());
					new_item.setGroupName(user.getHomeDirectory().getName());
				}
				break;
			case DATA:
				DataType data = (DataType)map;
				new_item.setDate(data.getCreatedDate());
				new_item.setDescription(data.getDescription());
				new_item.getData().add(NewListItemData("MimeType",data.getMimeType()));
				new_item.getData().add(NewListItemData("IsCompressed",data.getMimeType()));
				new_item.getData().add(NewListItemData("IsPasswordProtected",data.getPasswordProtected().toString()));
				new_item.getData().add(NewListItemData("IsEnciphered",data.getEnciphered().toString()));
				new_item.getData().add(NewListItemData("IsVaulted",data.getVaulted().toString()));
				new_item.getData().add(NewListItemData("Size",data.getSize().toString()));
				new_item.setGroupId(data.getGroup().getId());
				new_item.setGroupName(data.getGroup().getName());
				if(data.getMimeType().startsWith("text/")){
					/// new_item.setValue(data.getValueString());
				}
				break;
			case GROUP:
				BaseGroupType base_group = (BaseGroupType)map;
				new_item.setGroupName(base_group.getName());
				new_item.setGroupId(base_group.getId());
				if(base_group.getGroupType() == GroupEnumType.DATA){
					new_item.getData().add(NewListItemData("Path",((DirectoryGroupType)base_group).getPath()));
				}
				break;
		}
		
		return new_item;
		*/
	}
	public static ListDataType NewListItemData(String name, String value){
		ListDataType data = new ListDataType();
		data.setName(name);
		data.setValue(value);
		return data;
	}
}
