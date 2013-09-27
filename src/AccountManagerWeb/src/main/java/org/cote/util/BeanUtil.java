package org.cote.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;


import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;

import org.cote.beans.SessionBean;


public class BeanUtil {
	public static <U,T> T getBean(Class<T> tClass, U map){
		 T bean = null;
		try{
			 JAXBContext contextA = JAXBContext.newInstance(map.getClass());
		      JAXBElement<U> jaxbElementA = new JAXBElement(new QName("flight"), map.getClass(), map);
		        JAXBSource sourceA = new JAXBSource(contextA, jaxbElementA);
	
		        JAXBContext contextB = JAXBContext.newInstance(tClass);
		        Unmarshaller unmarshallerB = contextB.createUnmarshaller();
		        JAXBElement<T> jaxbElementB = unmarshallerB.unmarshal(sourceA, tClass);
	        bean = jaxbElementB.getValue();
		}
		catch(JAXBException je){
			je.printStackTrace();
			System.out.println(je.getMessage());
		}
		return bean;
	}
	public static DirectoryGroupType[] getSanitizedGroups(DirectoryGroupType[] groups){
		List<DirectoryGroupType> beans = new ArrayList<DirectoryGroupType>();
		for(int i = 0; i < groups.length; i++) beans.add(getSanitizedGroup(groups[i],false));
		return beans.toArray(new DirectoryGroupType[0]);
	}
	public static DirectoryGroupType getSanitizedGroup(DirectoryGroupType group, boolean deepCopy){
		if(group == null) return null;
		group.getSubDirectories().clear();
		DirectoryGroupType bean = getBean(DirectoryGroupType.class, group);
		bean.setParentGroup(null);
		//bean.setOrganization(null);
		
		return bean;
	}
	/*
	public static UserBean getUserBean(UserType user){
		UserBean bean = new UserBean();
		bean.setAccountId(user.getAccountId());
		bean.setContactInformation(user.getContactInformation());
		bean.setDatabaseRecord(user.getDatabaseRecord());
		bean.setHomeDirectory(getSanitizedGroup(user.getHomeDirectory(),false));
		bean.setId(user.getId());
		bean.setName(user.getName());
		bean.setOrganization(user.getOrganization());
		bean.setOwnerId(user.getOwnerId());
		bean.setParentId(user.getParentId());
		bean.setPopulated(user.getPopulated());
		bean.setSession(user.getSession());
		bean.setSessionStatus(user.getSessionStatus());
		bean.setStatistics(user.getStatistics());
		bean.setUserId(user.getUserId());
		bean.setUserStatus(user.getUserStatus());
		bean.setUserType(user.getUserType());
		return bean;
	}
	*/
	public static SessionBean getSessionBean(UserSessionType session, String sessionId){

		SessionBean bean = new SessionBean();
		bean.setSessionId(sessionId);
		if(session == null){
			System.out.println("Invalid persistent session object");
			return bean;
		}
		bean.setDataSize(session.getDataSize());
		bean.setExpired(session.getExpired());
		bean.setOrganizationId(session.getOrganizationId());
		bean.setSecurityId(session.getSecurityId());
		bean.setSessionAccessed(session.getSessionAccessed());
		bean.setSessionCreated(session.getSessionCreated());
		bean.setSessionExpires(session.getSessionExpires());
		bean.setSessionStatus(session.getSessionStatus());
		bean.setUserId(session.getUserId());
		bean.getSessionData().addAll(session.getSessionData());
		
		return bean;
		
	}
}
