package org.cote.accountmanager.util;

import org.cote.accountmanager.objects.BaseAuthorizationType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.NameEnumType;
public class MapUtil {

	public static void shallowCloneAznType(BaseAuthorizationType src, BaseAuthorizationType targ){
		//targ.setUrn(src.getUrn());
		targ.setScore(src.getScore());
		targ.setDescription(src.getDescription());
		targ.setLogicalOrder(src.getLogicalOrder());
		shallowCloneNameIdDirectoryType(src, targ);
	}
	public static void shallowCloneNameIdDirectoryType(NameIdType src, NameIdType targ){
		targ.setName(src.getName());
		targ.setParentId(src.getParentId());
		//if(src.getNameType() == null) targ.setNameType(NameEnumType.APPLICATION);
		if(src.getNameType() != NameEnumType.UNKNOWN) targ.setNameType(src.getNameType());
		if(targ.getOrganization() == null) targ.setOrganization(src.getOrganization());
		if(targ.getUrn() != null) targ.setUrn(src.getUrn());
	}
}
