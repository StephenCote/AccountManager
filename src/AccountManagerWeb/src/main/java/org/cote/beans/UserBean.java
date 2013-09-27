package org.cote.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.UserType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://www.cote.org/beans", name="UserBean")
@XmlSeeAlso({
	UserType.class,
	ContactInformationType.class,
	ContactInformationBean.class
})
public class UserBean extends UserType {
	private final String javaClass = "org.cote.beans.UserBean";
	public UserBean(){

	}
	public String getJavaClass() {
		return javaClass;
	}
}