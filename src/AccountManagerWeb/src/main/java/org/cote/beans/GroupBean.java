package org.cote.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DirectoryGroupType;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://www.cote.org/beans", name="GroupBean")
@XmlSeeAlso({
	DirectoryGroupType.class
})
public class GroupBean extends DirectoryGroupType {
	private final String javaClass = "org.cote.beans.GroupBean";
	public GroupBean(){

	}
	public String getJavaClass() {
		return javaClass;
	}
}