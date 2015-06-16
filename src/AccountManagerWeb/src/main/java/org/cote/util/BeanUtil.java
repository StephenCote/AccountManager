/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
 * Redistribution without modification is permitted provided the following conditions are met:
 *
 *    1. Redistribution may not deviate from the original distribution,
 *        and must reproduce the above copyright notice, this list of conditions
 *        and the following disclaimer in the documentation and/or other materials
 *        provided with the distribution.
 *    2. Products may be derived from this software.
 *    3. Redistributions of any form whatsoever must retain the following acknowledgment:
 *        "This product includes software developed by Stephen Cote Enterprises, LLC"
 *
 * THIS SOFTWARE IS PROVIDED BY STEPHEN COTE ENTERPRISES, LLC ``AS IS''
 * AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THIS PROJECT OR ITS CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, 
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY 
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
