package org.cote.accountmanager.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.apache.log4j.Logger;
import org.cote.accountmanager.objects.SecuritySpoolType;
import org.junit.Test;

public class TestSecuritySpool extends BaseDataAccessTest {
	public static final Logger logger = Logger.getLogger(TestSecuritySpool.class.getName());
	
	private static String referenceId = UUID.randomUUID().toString();
	private static String referenceId2 = UUID.randomUUID().toString();
	
	
	@Test
	public void testTokens(){
		testInsertToken();
		testGetToken();
		testUpdateToken();
		testDeleteToken();
	}
	public void testInsertToken(){
		assertNotNull("User is null", testUser);
		SecuritySpoolType token = null;
		boolean add_token = false;
		try{
			token = Factories.getSecurityTokenFactory().generateSecurityToken(referenceId, testUser.getOrganizationId());
			add_token = (token != null);
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
			logger.error(fe.getStackTrace());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}
		assertTrue("token was not added", add_token);
	}


	public void testGetToken(){
		assertNotNull("User is null", testUser);
		SecuritySpoolType token = null;
		logger.info("Token: " + referenceId);
		try{
			token = Factories.getSecurityTokenFactory().getSecurityToken(referenceId, testUser.getOrganizationId());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
		}
		assertNotNull("Expected one token", token);
	}
	

	public void testUpdateToken(){
		assertNotNull("User is null", testUser);
		SecuritySpoolType token = null;
		try{
			token = Factories.getSecurityTokenFactory().getSecurityToken(referenceId, testUser.getOrganizationId());
		}
		catch(FactoryException fe){
			logger.error(fe.getMessage());
		} catch (ArgumentException e) {
			
			logger.error(e.getStackTrace());
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
	
	public void testDeleteToken(){
		assertNotNull("User is null", testUser);
		boolean deleted = false;
		try{
			deleted = Factories.getSecurityTokenFactory().deleteTokens(referenceId, testUser.getOrganizationId());
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
			logger.error(fe.getStackTrace());
		}
		assertFalse("An error occurred", error);
	}
	*/
	
}