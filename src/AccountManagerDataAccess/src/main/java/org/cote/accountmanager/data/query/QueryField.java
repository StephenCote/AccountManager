package org.cote.accountmanager.data.query;

import org.cote.accountmanager.objects.FieldMatch;
import org.cote.accountmanager.objects.types.SqlDataEnumType;

public class QueryField extends FieldMatch {
	private Object value = null;

	public <T> QueryField(SqlDataEnumType inType, String name, T value){
		this.name = name;
		this.dataType = inType;
		this.value = value;
		
	}
	
	public <T> T getValue() {
		return (T)value;
	}

	public <T> void setValue(T value) {
		this.value = value;
	}
	
	public void addField(QueryField field){
		this.getFields().add(field);
	}
	
}
