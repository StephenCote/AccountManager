package org.cote.rest.schema;

import java.util.ArrayList;
import java.util.List;

public class ServiceSchemaMethod {
	private String name = null;
	private String httpMethod = "GET";
	private List<ServiceSchemaMethodParameter> parameters = null;
	private ServiceSchemaMethodParameter returnValue = null;
	public ServiceSchemaMethod(){
		parameters = new ArrayList<ServiceSchemaMethodParameter>();
	}
	public ServiceSchemaMethod(String name, List<ServiceSchemaMethodParameter> parameters){
		this.name = name;
		this.parameters = parameters;
	}
	
	public String getHttpMethod() {
		return httpMethod;
	}
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<ServiceSchemaMethodParameter> getParameters() {
		return parameters;
	}
	public void setParameters(List<ServiceSchemaMethodParameter> parameters) {
		this.parameters = parameters;
	}
	public ServiceSchemaMethodParameter getReturnValue() {
		return returnValue;
	}
	public void setReturnValue(ServiceSchemaMethodParameter returnValue) {
		this.returnValue = returnValue;
	}
	
	
	
}
