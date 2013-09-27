package org.cote.accountmanager.data.services;

import java.util.List;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.FactoryException;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.PersonType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ContactEnumType;

public class UserService {
public static final Logger logger = Logger.getLogger(UserService.class.getName());
	
	public static ContactType getPreferredEmailContact(UserType user){
		return ContactService.getPreferredEmailContact(user.getContactInformation());
	}
	
}
