package org.cote.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://www.cote.org/beans", name="DirectoryBean")
@XmlSeeAlso({
	/*
	DirectoryGroupType.class,
    BaseGroupType.class,
    NameIdType.class
    */
})
public class DirectoryBean extends DirectoryGroupType {
	private final String javaClass = "org.cote.beans.DirectoryBean";
	public DirectoryBean(){

	}
	public String getJavaClass() {
		return javaClass;
	}
	

}