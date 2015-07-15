package org.cote.accountmanager.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.data.ConnectionFactory.CONNECTION_TYPE;
import org.cote.accountmanager.data.factory.ContactInformationFactory;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.services.AuthorizationService;
import org.cote.accountmanager.data.services.FactoryService;
import org.cote.accountmanager.data.factory.OrganizationFactory;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.AccountRoleType;
import org.cote.accountmanager.objects.ContactInformationType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.cote.accountmanager.objects.OrganizationType;
import org.cote.accountmanager.objects.UserRoleType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.AffectEnumType;
import org.cote.accountmanager.objects.types.SpoolNameEnumType;
import org.cote.accountmanager.objects.types.UserEnumType;
import org.cote.accountmanager.objects.types.UserStatusEnumType;
import org.cote.accountmanager.objects.types.ValueEnumType;
import org.cote.accountmanager.util.BinaryUtil;
import org.cote.accountmanager.util.CalendarUtil;
import org.cote.accountmanager.util.SecurityUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class TestSecuritySpool extends BaseDataAccessTest {
	public static final Logger logger = Logger.getLogger(TestSecuritySpool.class.getName());
	
	private static String referenceId = UUID.randomUUID().toString();
	private static String referenceId2 = UUID.randomUUID().toString();
	
	
	@Test
	public void testInsertToken(){
		assertNotNull("User is null", testUser);
		SecuritySpoolType token = null;
		boolean add_token = false;
		try{
			token = Factories.getSecurityTokenFactory().generateSecurityToken(referenceId, testUser.getOrganization());
			add_token = (token != null);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			fe.printStackTrace();
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertTrue("token was not added", add_token);
	}

	
	@Test
	public void testGetToken(){
		assertNotNull("User is null", testUser);
		SecuritySpoolType token = null;
		logger.info("Token: " + referenceId);
		try{
			token = Factories.getSecurityTokenFactory().getSecurityToken(referenceId, testUser.getOrganization());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull("Expected one token", token);
	}
	
	@Test
	public void testUpdateToken(){
		assertNotNull("User is null", testUser);
		SecuritySpoolType token = null;
		try{
			token = Factories.getSecurityTokenFactory().getSecurityToken(referenceId, testUser.getOrganization());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		assertNotNull("Expected one token", token);
		token.setData("Example data".getBytes());
		boolean updated = false;
		try{
			updated = Factories.getSecurityTokenFactory().update(token);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}
		assertTrue("token was not updated", updated);
	}
	
	@Test
	public void testDeleteToken(){
		assertNotNull("User is null", testUser);
		boolean deleted = false;
		try{
			deleted = Factories.getSecurityTokenFactory().deleteTokens(referenceId, testUser.getOrganization());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		}
		assertTrue("Failed to deleted", deleted);
	}
	/*
	@Test
	public void testInsertTokens(){
		List<SecuritySpoolType> tokenTypes = new ArrayList<SecuritySpoolType>();
		boolean error = false;
		assertFalse("Table cache is not cleaned up", Factories.getSecurityTokenFactory().getDataTable("spool").getRows().size() > 0);
		try{
		for(int i = 0; i < 10;i++){
			SecurityBean bean = new SecurityBean();
			SecurityFactory.getSecurityFactory().generateSecretKey(bean);
			SecuritySpoolType tokenType = Factories.getSecurityTokenFactory().newSecurityToken(referenceId2, Factories.getPublicOrganization());
			tokenType.setData(BinaryUtil.toBase64Str(bean.getCipherKey()) + "&&" + BinaryUtil.toBase64Str(bean.getCipherIV()));
			tokenTypes.add(tokenType);
		}
		if(Factories.getSecurityTokenFactory().addSecurityTokens(tokenTypes.toArray(new SecuritySpoolType[0])) == false){
			System.out.println("Failed to persist tokens");
		}
		}
		catch(FactoryException fe){
			error = true;
			logger.error(fe.getMessage());
			fe.printStackTrace();
		}
		assertFalse("An error occurred", error);
	}
	*/
	
}