package org.cote.accountmanager.data.util;

import java.util.Comparator;

import org.cote.accountmanager.objects.BaseAuthorizationType;
public class AuthorizationTypeComparator implements Comparator<BaseAuthorizationType>{
	public int compare(BaseAuthorizationType doc1, BaseAuthorizationType doc2){
		return doc1.getLogicalOrder().compareTo(doc2.getLogicalOrder());
	}
}
