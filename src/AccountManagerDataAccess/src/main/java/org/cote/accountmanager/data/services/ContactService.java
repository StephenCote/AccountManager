package org.cote.accountmanager.data.services;

import java.util.List;

import org.apache.log4j.Logger;

import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.types.ContactEnumType;

public class ContactService {
public static final Logger logger = Logger.getLogger(ContactService.class.getName());
	
	public static ContactType getPreferredEmailContact(ContactInformationType cinfo){
		ContactType email = null;
		ContactType check = null;
		if(cinfo == null) return email;
		List<ContactType> contacts = cinfo.getContacts();
		for(int i = 0; i < contacts.size();i++){
			check = contacts.get(i);
			if(check.getContactType() == ContactEnumType.EMAIL){
				if(email == null || check.getPreferred()) email = check;
				if(check.getPreferred()) break;
			}
		}
		return email;
	}
	
}
