package org.cote.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.cote.accountmanager.objects.ContactInformationType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://www.cote.org/beans", name="ContactInformationBean")
@XmlSeeAlso({
	ContactInformationType.class
})
public class ContactInformationBean extends ContactInformationType {
	private final String javaClass = "org.cote.beans.ContactInformationBean";
	public ContactInformationBean(){

	}
	public String getJavaClass() {
		return javaClass;
	}
}