package org.cote.accountmanager.data.util;

import java.util.Comparator;

import org.cote.accountmanager.objects.LogicalNameIdType;
public class LogicalTypeComparator implements Comparator<LogicalNameIdType>{
	public int compare(LogicalNameIdType doc1, LogicalNameIdType doc2){
		return doc1.getLogicalOrder().compareTo(doc2.getLogicalOrder());
	}
}
