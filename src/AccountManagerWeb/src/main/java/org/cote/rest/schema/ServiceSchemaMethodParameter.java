package org.cote.rest.schema;

public class ServiceSchemaMethodParameter {
	private String name = null;
	private String type = null;
	public ServiceSchemaMethodParameter(){
		
	}
	public ServiceSchemaMethodParameter(String name, String type){
		this.name = name;
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	

}
