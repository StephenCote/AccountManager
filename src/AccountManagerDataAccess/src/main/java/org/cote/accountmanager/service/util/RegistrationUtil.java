/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.service.util;

import java.util.Calendar;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.data.factory.SessionDataFactory;
import org.cote.accountmanager.data.factory.UserFactory;
import org.cote.accountmanager.data.services.AuditService;
import org.cote.accountmanager.data.services.PersonService;
import org.cote.accountmanager.data.services.SessionSecurity;
import org.cote.accountmanager.data.services.UserService;
import org.cote.accountmanager.data.util.BeanUtil;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.ContactType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserSessionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.service.rest.SessionBean;
import org.cote.accountmanager.util.CalendarUtil;


public class RegistrationUtil {
	private static int registrationExpiry = 1;
	public static final Logger logger = LogManager.getLogger(RegistrationUtil.class);
	public static boolean confirmUserRegistration(String regSessId, String regId, String cred,String remoteAddr, String sessionId){
		boolean outBool = false;

		SessionBean session = null;
		SessionBean regSession = null;
		AuditType audit = AuditService.beginAudit(ActionEnumType.RESPONSE, "confirmUserRegistration",AuditEnumType.INFO, remoteAddr);
		AuditService.targetAudit(audit, AuditEnumType.REGISTRATION, regSessId);
		AuditType[]  audits = new AuditType[0];
		try{
			OrganizationType regOrg = null;
			session = BeanUtil.getSessionBean(SessionSecurity.getUserSession(regSessId, Factories.getPublicOrganization().getId()),regSessId);
			logger.info("Registration Session Data for " + regSessId);
			for(int i = 0; i < session.getSessionData().size();i++){
				logger.info(session.getSessionData().get(i).getName() + "=" + session.getSessionData().get(i).getValue());
			}
			long regOrgId = 0;
			try{
				String orgStr = session.getValue("organization-id");
				if(orgStr != null) regOrgId = Long.parseLong(orgStr);
			}
			catch(NumberFormatException nfe){
				logger.error(nfe.getMessage());
			}
			if(regOrgId > 0) regOrg = ((OrganizationFactory)Factories.getFactory(FactoryEnumType.ORGANIZATION)).getOrganizationById( regOrgId);
			if(regOrg == null){
				AuditService.denyResult(audit,"Invalid registration organization: " + regOrgId);
				return false;
			}
			
			regSession = BeanUtil.getSessionBean(SessionSecurity.getUserSession(regSessId, regOrg.getId()),regSessId);
			if(regSession == null){
				AuditService.denyResult(audit, "Registration id not found");
				return false;
			}
			String regRemoteAddr = regSession.getValue("remote-address");
			if(regRemoteAddr == null || regRemoteAddr.equals(remoteAddr) == false){
				AuditService.denyResult(audit, "Registration IP '" + regRemoteAddr + "' does not match '" + remoteAddr + "' in org " + regSession.getOrganizationId());
				return false;
			}

			
			String userName = regSession.getValue("userName");
			audits = Factories.getAuditFactory().getAuditBySourceAndTarget(AuditEnumType.REGISTRATION, remoteAddr, AuditEnumType.USER, userName);
			if(audits.length == 0){
				AuditService.denyResult(audit, "Audit trail missing for address '" + remoteAddr + "' requesting new user '" + userName + "'");
				return false;
			}

			String email = regSession.getValue("email");
			outBool = PersonService.createRegisteredUserAsPerson(audit, userName, cred,email,regOrg.getId());

		}
		catch(FactoryException | ArgumentException e) {
			logger.error(e.getMessage());
		} 
		/// Regardless of the outcome, remove the request audit (to unlock the IP check), and the registration session
		///
		try {
			if(audits.length > 0) Factories.getAuditFactory().deleteAudit(audits[0]);
			if(regSession != null) Factories.getSessionFactory().clearSession(regSession.getSessionId());
		} catch (FactoryException e) {
			logger.error("Error",e);
		}
		return outBool;
	}
	
	/// createUserRegistration will generate a session that includes registration information
	/// 
	///
	public static UserSessionType createUserRegistration(UserType user, String remoteAddr, boolean limitIP){
		UserSessionType regSession = null;
		String sessionId = null;
		String registrationId = null;
		SessionDataFactory df = Factories.getSessionDataFactory();

		AuditType audit = AuditService.beginAudit(ActionEnumType.REQUEST, "createUserRegistration", AuditEnumType.INFO, remoteAddr);
		System.out.println("Creating user registration for '" + user.getName() + "'");
		AuditService.targetAudit(audit, AuditEnumType.USER, user.getName());
		try{
			AuditType[] nameAudits = Factories.getAuditFactory().getAuditByTarget(AuditEnumType.USER, user.getName());
			if(limitIP){
				logger.info("IP Limiter Enabled.  Checking for other registrations by IP " + remoteAddr);
				AuditType[] ipAudits = Factories.getAuditFactory().getAuditBySource(AuditEnumType.REGISTRATION, remoteAddr);
				if(ipAudits.length > 0){
					AuditService.denyResult(audit, "IP already registered");
					return null;
				}
			}
			else{
				logger.info("IP Limiter Disabled.  The same IP can submit multiple registrations");
			}
			if(nameAudits.length > 0){
				AuditService.denyResult(audit, "User name '" + user.getName() + "' is pending registeration in " + user.getOrganizationId() + " organization");
				return null;				
			}
			if(((UserFactory)Factories.getNameIdFactory(FactoryEnumType.USER)).getUserNameExists(user.getName(), user.getOrganizationId())){
				AuditService.denyResult(audit, "User name '" + user.getName() + "' is already registered in " + user.getOrganizationId() + " organization");
				return null;
			}			
			sessionId = UUID.randomUUID().toString();
			registrationId = UUID.randomUUID().toString();
			regSession = Factories.getSessionFactory().newUserSession(sessionId);
			//regSession.setOrganizationId(user.getOrganization().getId());
			
			
			/// Reg Session has to be in the Public organization because the org is not being included in any registration communication
			/// And session lookup is not being permitted across organizations
			///
			regSession.setOrganizationId(Factories.getPublicOrganization().getId());
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, registrationExpiry);
			regSession.setSessionExpires(CalendarUtil.getXmlGregorianCalendar(cal.getTime()));
			audit.setAuditExpiresDate(regSession.getSessionExpires());
			
			Factories.getSessionFactory().addSession(regSession);

			//regSession = Factories.getSessionFactory().getSession(sessionId, Factories.getPublicOrganization());
			//String enc_password = OrganizationSecurityUtil.encipherString(UUID.randomUUID().toString(), user.getOrganization());
			//String enc_password = OrganizationSecurityUtil.encipherString(user.getPassword(), Factories.getPublicOrganization());
			df.setValue(regSession, "remote-address", remoteAddr);
			df.setValue(regSession, "registration-id", registrationId);
			df.setValue(regSession, "userName", user.getName());
			//df.setValue(regSession, "password", enc_password);
			df.setValue(regSession, "organization-id",Long.toString(user.getOrganizationId()));
			
			/*
			if(true){
				System.out.println("REFACTOR REFACTOR EMAIL");
				return null;
			}
			*/
			ContactType emailContact = UserService.getPreferredEmailContact(user);
			if(emailContact != null){
				
				System.out.println("Received contact information supplemental.  Email is '" + emailContact.getContactValue() + "'");
				df.setValue(regSession, "email", emailContact.getContactValue());
			}
			else{
				AuditService.denyResult(audit, "Email address not specified");
				System.out.println("Did not receive contact information supplemental");
				regSession = null;
				throw new FactoryException("Required data was not provided: Contact Information object was null");
			}
			
			
			Factories.getSessionFactory().updateData(regSession);
			
			/// Strip the password value out of the reg session before returning it
			/// And set the original password to null
			/// There's no reason to keep it
			///
			//df.setValue(regSession, "password", null);
			df.setValue(regSession, "userName", null);
			regSession.getChangeSessionData().clear();

			audit.setAuditSourceType(AuditEnumType.REGISTRATION);
			AuditService.pendResult(audit, "Registration pending user acceptance");
			System.out.println("Test: Audit target data = " + audit.getAuditTargetData());
			Factories.getAuditFactory().flushSpool();
		}
		catch(FactoryException fe){
			System.out.println(fe.getMessage());
			logger.error("Error",fe);
		}
		return regSession;
	}
}
