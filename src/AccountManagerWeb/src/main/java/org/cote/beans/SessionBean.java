package org.cote.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.SecurityType;
import org.cote.accountmanager.objects.UserSessionType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://www.cote.org/beans", name="SessionBean")
@XmlSeeAlso({
	UserSessionType.class
})
public class SessionBean extends UserSessionType {
	private final String javaClass = "org.cote.beans.SessionBean";
	public SessionBean(){

	}
	public String getJavaClass() {
		return javaClass;
	}
	public void setValue(String name, String value){
		Factories.getSessionDataFactory().setValue(this, name, value); 
	}
	public String getValue(String name){
		return Factories.getSessionDataFactory().getValue(this, name); 
	}
}