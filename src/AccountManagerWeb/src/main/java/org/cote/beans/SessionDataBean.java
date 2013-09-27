package org.cote.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.cote.accountmanager.objects.UserSessionDataType;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://www.cote.org/beans", name="SessionDataBean")
@XmlSeeAlso({
	UserSessionDataType.class
})
public class SessionDataBean extends UserSessionDataType {
	private final String javaClass = "org.cote.beans.SessionDataBean";
	public SessionDataBean(){

	}
	public String getJavaClass() {
		return javaClass;
	}
}