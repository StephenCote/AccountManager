package org.cote.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.cote.accountmanager.objects.BaseType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.MessageType;


@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace="http://www.cote.org/beans", name="MessageBean")
@XmlSeeAlso({
    MessageType.class,
    BaseType.class
})

//@Mapped(namespaceMap = { @XmlNsMap(namespace = "http://www.cote.org/accountmanager/objects", jsonName = "schema") })

public class MessageBean extends MessageType {
	private final String javaClass = "org.cote.beans.MessageBean";
	public MessageBean(){

	}
	public String getJavaClass() {
		return javaClass;
	}
	
	
	
	
}
