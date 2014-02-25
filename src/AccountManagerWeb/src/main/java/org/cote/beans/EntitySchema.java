package org.cote.beans;

import javax.xml.bind.annotation.XmlRootElement;

import org.cote.accountmanager.objects.BaseGroupType;
import org.cote.accountmanager.objects.BaseRoleType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserGroupType;
import org.cote.accountmanager.objects.UserType;
import org.cote.beans.*;


@XmlRootElement(name="EntitySchema")
public class EntitySchema {
	private String defaultPackage = "org.cote.beans";
	private BaseRoleType baseRoleType = null;
	private MessageBean messageBeanSchema = null;
	private DataType dataTypeSchema = null;
	//private DirectoryBean directoryBeanSchema = null;
	private CryptoBean cryptoBeanSchema = null;
	private SessionBean sessionBean = null;
	private SessionDataBean sessionDataBean = null;
	private UserType userType = null;
	private DirectoryGroupType directoryGroupType = null;
	private BaseGroupType baseGroupType = null;
	private UserGroupType userGroupType = null;
	private ContactInformationType contactInformationType = null;
	private PersonType personType = null;
	private ContactType contactType = null;
	private OrganizationType organizationType = null;
	public EntitySchema(){
		userGroupType = new UserGroupType();
		baseGroupType = new BaseGroupType();
		contactType = new ContactType();
		personType = new PersonType();
		organizationType = new OrganizationType();
		baseRoleType = new BaseRoleType();
		directoryGroupType = new DirectoryGroupType();
		messageBeanSchema = new MessageBean();
		dataTypeSchema = new DataType();
		//directoryBeanSchema = new DirectoryBean();
		cryptoBeanSchema = new CryptoBean();
		sessionBean = new SessionBean();
		sessionDataBean = new SessionDataBean();
		userType = new UserType();
		contactInformationType = new ContactInformationType();
	}


	public BaseGroupType getBaseGroupType() {
		return baseGroupType;
	}


	public void setBaseGroupType(BaseGroupType baseGroupType) {
		this.baseGroupType = baseGroupType;
	}


	public UserGroupType getUserGroupType() {
		return userGroupType;
	}


	public void setUserGroupType(UserGroupType userGroupType) {
		this.userGroupType = userGroupType;
	}


	public PersonType getPersonType() {
		return personType;
	}


	public void setPersonType(PersonType personType) {
		this.personType = personType;
	}


	public ContactType getContactType() {
		return contactType;
	}


	public void setContactType(ContactType contactType) {
		this.contactType = contactType;
	}


	public OrganizationType getOrganizationType() {
		return organizationType;
	}


	public void setOrganizationType(OrganizationType organizationType) {
		this.organizationType = organizationType;
	}


	public BaseRoleType getBaseRoleType() {
		return baseRoleType;
	}


	public void setBaseRoleType(BaseRoleType baseRoleType) {
		this.baseRoleType = baseRoleType;
	}


	public String getDefaultPackage() {
		return defaultPackage;
	}

	public void setDefaultPackage(String defaultPackage) {
		this.defaultPackage = defaultPackage;
	}



	public DirectoryGroupType getDirectoryGroupType() {
		return directoryGroupType;
	}


	public void setDirectoryGroupType(DirectoryGroupType directoryGroupType) {
		this.directoryGroupType = directoryGroupType;
	}


	public SessionDataBean getSessionDataBean() {
		return sessionDataBean;
	}



	public void setSessionDataBean(SessionDataBean sessionDataBean) {
		this.sessionDataBean = sessionDataBean;
	}



	public ContactInformationType getContactInformationType() {
		return contactInformationType;
	}



	public void setContactInformationType(
			ContactInformationType contactInformationType) {
		this.contactInformationType = contactInformationType;
	}



	public UserType getUserType() {
		return userType;
	}

	public void setUserType(UserType userType) {
		this.userType = userType;
	}

	public CryptoBean getCryptoBeanSchema() {
		return cryptoBeanSchema;
	}

	public void setCryptoBeanSchema(CryptoBean cryptoBeanSchema) {
		this.cryptoBeanSchema = cryptoBeanSchema;
	}

	public MessageBean getMessageBeanSchema() {
		return messageBeanSchema;
	}

	public void setMessageBeanSchema(MessageBean messageBeanSchema) {
		this.messageBeanSchema = messageBeanSchema;
	}

	public DataType getDataTypeSchema() {
		return dataTypeSchema;
	}

	public void setDataTypeSchema(DataType dataTypeSchema) {
		this.dataTypeSchema = dataTypeSchema;
	}
/*
	public DirectoryBean getDirectoryBeanSchema() {
		return directoryBeanSchema;
	}

	public void setDirectoryBeanSchema(DirectoryBean directoryBeanSchema) {
		this.directoryBeanSchema = directoryBeanSchema;
	}
*/
	public SessionBean getSessionBean() {
		return sessionBean;
	}

	public void setSessionBean(SessionBean sessionBean) {
		this.sessionBean = sessionBean;
	}

}
