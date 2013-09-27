package org.cote.accountmanager.data;

import org.cote.accountmanager.objects.StatementParameterType;
import org.cote.accountmanager.objects.types.SqlDataEnumType;




public class StatementParameter extends StatementParameterType {

	public StatementParameter(SqlDataEnumType paramType, String name, int index){
		this.parameterName = name;
		this.parameterType = paramType;
		this.parameterIndex = index;
	}


	
	
}
