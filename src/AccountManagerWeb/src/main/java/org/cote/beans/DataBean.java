package org.cote.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.CompressionEnumType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://www.cote.org/beans", name="DataBean")
@XmlSeeAlso({
	DataType.class,
    NameIdType.class
})
public class DataBean extends DataType {
	private final String javaClass = "org.cote.beans.DataBean";
	public DataBean(){
	
	}
	public String getJavaClass() {
		return javaClass;
	}
	
}
