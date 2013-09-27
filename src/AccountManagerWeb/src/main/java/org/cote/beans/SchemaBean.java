package org.cote.beans;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.cote.rest.schema.ServiceSchemaMethod;


@XmlRootElement(name="SchemaBean")
public class SchemaBean {
	private String serviceType = "JSON-REST";
	private String serviceURL = null;
	private List<ServiceSchemaMethod> methods = new ArrayList<ServiceSchemaMethod>();

	public SchemaBean(){
		//this.methods = new ArrayList<String>();
	}
	


	public String getServiceURL() {
		return serviceURL;
	}



	public void setServiceURL(String serviceURL) {
		this.serviceURL = serviceURL;
	}



	public List<ServiceSchemaMethod> getMethods() {
		return methods;
	}


	public void setMethods(List<ServiceSchemaMethod> methods) {
		this.methods = methods;
	}


	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}


	public String getServiceType() {
		return serviceType;
	}
	

}