package org.cote.accountmanager.data.security;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.cote.accountmanager.objects.ApiClientConfigurationType;
import org.cote.accountmanager.objects.NameIdType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://www.cote.org/beans", name="ApiClientConfigurationBean")
@XmlSeeAlso({
	ApiClientConfigurationType.class,
    NameIdType.class
})
public class ApiClientConfigurationBean extends ApiClientConfigurationType {
	private final String javaClass = "org.cote.accountmanager.data.security.ApiClientConfigurationBean";
	public ApiClientConfigurationBean(){
	
	}
	public String getJavaClass() {
		return javaClass;
	}
	
	public static ApiClientConfigurationBean newInstance(ApiClientConfigurationType apiCfg){
		ApiClientConfigurationBean api = new ApiClientConfigurationBean();
		api.setDataUrn(apiCfg.getDataUrn());
		api.setName(apiCfg.getName());
		api.setServiceType(apiCfg.getServiceType());
		api.setServiceUrl(apiCfg.getServiceUrl());
		api.getAttributes().addAll(apiCfg.getAttributes());
		return api;
	}
	
}

