package org.cote.accountmanager.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cote.accountmanager.objects.types.SqlDataEnumType;


public class StatementParamMap {
	private Map<String,StatementParameter> parameterMap = null;

	public StatementParamMap(){
		parameterMap = Collections.synchronizedMap(new HashMap<String,StatementParameter>());
	}

	public StatementParameter addIntParameter(String name, int index){
		StatementParameter param = new StatementParameter(SqlDataEnumType.INTEGER, name, index);
		parameterMap.put(name, param);
		return param;
	}
	
	public StatementParameter addStringParameter(String name, int index){
		StatementParameter param = new StatementParameter(SqlDataEnumType.VARCHAR, name, index);
		parameterMap.put(name, param);
		return param;
	}

	public Map<String, StatementParameter> getParameterMap() {
		return parameterMap;
	}
	
}
