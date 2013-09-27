package org.cote.beans;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.cote.accountmanager.objects.BaseType;
import org.cote.accountmanager.objects.MessageType;

public class MessageTest extends MessageType {
	public MessageTest(){
		
	}
	
	private String grok = null;

	public String getGrok() {
		return grok;
	}

	public void setGrok(String grok) {
		this.grok = grok;
	}
	
}
