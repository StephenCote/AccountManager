package org.cote.accountmanager.data.query;

import org.cote.accountmanager.objects.types.ComparatorEnumType;

public class QueryBuilderUtil {
	public static String getComparatorExpression(ComparatorEnumType c)
	{
		switch (c)
		{
			case BETWEEN:
				return "BETWEEN";
			case EQUALS:
				return "=";
			case GREATER_THAN:
				return ">";
			case GREATER_THAN_OR_EQUALS:
				return ">=";
			case IN:
				return "IN";
			case IS_NULL:
			case IS_NULL_EQUALS:
			case IS_NULL_NOT_EQUALS:
				return "ISNULL";
			case LESS_THAN:
				return "<";
			case LESS_THAN_OR_EQUALS:
				return "<=";
			case NOT_EQUALS:
				return "<>";
			case NOT_IN:
				return "NOT IN";
			case NOT_NULL:
				return "IS NOT NULL";
			case LIKE:
				return "LIKE";
			case UNKNOWN:
			default:
				break;

		}
		return null;
	}
}
