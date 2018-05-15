/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.data.services;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.types.ContactEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.objects.types.LocationEnumType;

public class ContactService {
public static final Logger logger = LogManager.getLogger(ContactService.class);
	public static ContactType getPreferredMobileContact(ContactInformationType cinfo){
		return getPreferredContact(cinfo,ContactEnumType.PHONE,LocationEnumType.MOBILE);
	}	
	public static ContactType getPreferredEmailContact(ContactInformationType cinfo){
		return getPreferredContact(cinfo,ContactEnumType.EMAIL,LocationEnumType.UNKNOWN);
	}	
	public static ContactType getPreferredPhoneContact(ContactInformationType cinfo){
		return getPreferredContact(cinfo,ContactEnumType.PHONE,LocationEnumType.UNKNOWN);
	}
	public static ContactType getPreferredContact(ContactInformationType cinfo,ContactEnumType preferredType,LocationEnumType preferredLocation){
		if(cinfo == null) return null;
		if(cinfo.getPopulated() == false){
			try {
				((ContactInformationFactory)Factories.getFactory(FactoryEnumType.CONTACTINFORMATION)).populate(cinfo);
			} catch (FactoryException | ArgumentException e) {
				
				logger.error(FactoryException.LOGICAL_EXCEPTION,e);
			}
		}
		ContactType contact = null;
		ContactType check = null;

		List<ContactType> contacts = cinfo.getContacts();
		for(int i = 0; i < contacts.size();i++){
			check = contacts.get(i);
			if(check.getContactType() == preferredType && (preferredLocation == LocationEnumType.UNKNOWN || check.getLocationType() == preferredLocation)){
				if(contact == null || check.getPreferred()) contact = check;
				if(check.getPreferred()) break;
			}
		}
		return contact;
	}
	
}
