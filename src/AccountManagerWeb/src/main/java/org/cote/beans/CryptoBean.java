package org.cote.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.SecurityType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://www.cote.org/beans", name="CryptoBean")
@XmlSeeAlso({
	SecurityType.class
})
public class CryptoBean extends SecurityType {
	private final String javaClass = "org.cote.beans.CryptoBean";
	private String spoolId = null;
	public CryptoBean(){

	}
	
	public String getSpoolId() {
		return spoolId;
	}

	public void setSpoolId(String spoolId) {
		this.spoolId = spoolId;
	}

	public String getJavaClass() {
		return javaClass;
	}
}