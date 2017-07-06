package org.cote.accountmanager.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.Security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.beans.SecurityBean;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.factory.SecurityFactory;
import org.cote.accountmanager.objects.DataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestDataUtil {
	public static final Logger logger = LogManager.getLogger(TestDataUtil.class);
	@Before
	public void setUp() throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testDataType(){
		logger.info("Testing default DataType values");
		DataType data = new DataType();
		assertNotNull("Blob boolean is null", data.getBlob());
	}
	@Test
	public void testDataTypeSetShortData(){
		logger.info("Setting short data");
		DataType data = new DataType();
		boolean error = false;
		try{
			DataUtil.setValueString(data, "Example text");
		}
		catch(DataException de){
			error = true;
		}
		
		assertFalse("An error was encountered", error);
		assertTrue("Short data not set", data.getShortData() != null && data.getShortData().length() > 0);
	}
	@Test
	public void testDataTypeGetShortData(){
		logger.info("Getting short data");
		DataType data = new DataType();
		boolean error = false;
		String comp_text = null;
		try{
			DataUtil.setValueString(data, "Example text");
			comp_text = DataUtil.getValueString(data);
		}
		catch(DataException de){
			error = true;
			logger.error(de.getMessage());
		}
		assertFalse("An error was encountered", error);
		assertTrue("Short data not set", comp_text != null && comp_text.length() > 0);
		assertTrue("Output not equal to input", comp_text.equals("Example text"));
	}
	
	@Test
	public void testSetData(){
		logger.info("Setting data");
		DataType data = new DataType();
		boolean error = false;
		String  value = "Example text";
		try{
			DataUtil.setValue(data, value.getBytes());
		}
		catch(DataException de){
			error = true;
		}
	}
	
	@Test
	public void testGetData(){
		logger.info("Getting data");
		DataType data = new DataType();
		boolean error = false;
		String  value = "Example text";
		byte[] out_data = new byte[0];
		try{
			DataUtil.setValue(data, value.getBytes());
			out_data = DataUtil.getValue(data);
		}
		catch(DataException de){
			error = true;
		}
		
		assertTrue("Data does not match",(new String(out_data).equals(value)));
	}
	@Test
	public void testSetEncipheredData(){
		logger.info("Setting enciphered data");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean ciphBean = new SecurityBean();
		ciphBean.setEncryptCipherKey(true);
		sf.generateKeyPair(ciphBean);
		sf.generateSecretKey(ciphBean);


		DataType data = new DataType();
		//data.setCipherKey(SecurityUtil.serializeToXml(ciphBean, false, false, true).getBytes());
		DataUtil.setCipher(data, ciphBean);
		data.setEncipher(true);
		boolean error = false;
		String  value = "Example text";
		
		try{
			DataUtil.setValue(data, value.getBytes());
		}
		catch(DataException de){
			error = true;
			logger.error(de.getMessage());
			logger.error("Error",de);
			
		}
		logger.info("Completing test ...");
		assertFalse("Error occurred", error);
		assertTrue("Data was not enciphered",data.getEnciphered());
	}
	
	@Test
	public void testGetEncipheredData(){
		logger.info("Getting enciphered data");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean ciphBean = new SecurityBean();
		ciphBean.setEncryptCipherKey(true);
		sf.generateKeyPair(ciphBean);
		sf.generateSecretKey(ciphBean);
		DataType data = new DataType();
		assertNotNull("Cipher key is null", ciphBean.getCipherKey());
		assertNotNull("Cipher iv is null", ciphBean.getCipherIV());
		DataUtil.setCipher(data, ciphBean);
		//data.setCipherKey(SecurityUtil.serializeToXml(ciphBean, false, false, true).getBytes());
		data.setEncipher(true);
		boolean error = false;
		String  value = "Example text";
		byte[] outBytes = new byte[0];
		try{
			DataUtil.setValue(data, value.getBytes());
			logger.info("Input: " + BinaryUtil.toBase64Str(value));
			logger.info("Output: " + BinaryUtil.toBase64Str(data.getDataBytesStore()));
			/// NOTE: invoking setValue with a key set will strip the key off the object.  This is by design.  That means the key must be RESET before reading the value
			DataUtil.setCipher(data,ciphBean);
			outBytes = DataUtil.getValue(data);
			
		}
		catch(DataException de){
			error = true;
			logger.error(de.getMessage());
		}
		assertFalse("Error occurred", error);
		String comp_text = new String(outBytes);
		assertTrue("Data does not match", comp_text.equals(value));
		//assertTrue("Data was not enciphered",data.getEnciphered());
	}
	@Test
	public void testSetProtectedData(){
		logger.info("Setting protected data");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean ciphBean = new SecurityBean();
		sf.generateSecretKey(ciphBean);

		DataType data = new DataType();
		data.setPassKey(SecurityUtil.serializeToXml(ciphBean, false, false, true).getBytes());
		data.setPasswordProtect(true);
		boolean error = false;
		String  value = "Example text";
		try{
			DataUtil.setValue(data, value.getBytes());
		}
		catch(DataException de){
			error = true;
		}
		assertTrue("Data was not protected",data.getPasswordProtected());
	}
	
	@Test
	public void testGetProtectedData(){
		logger.info("Getting protected data");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean ciphBean = new SecurityBean();
		sf.generateSecretKey(ciphBean);

		DataType data = new DataType();
		//DataUtil.setPassword(data, password);
		data.setPassKey(SecurityUtil.serializeToXml(ciphBean, false, false, true).getBytes());
		//DataUtil.setPassword(data, password);
		data.setPasswordProtect(true);
		boolean error = false;
		String  value = "Example text";
		byte[] outBytes = new byte[0];
		try{
			DataUtil.setValue(data, value.getBytes());
			logger.info("Input: " + BinaryUtil.toBase64Str(value));
			logger.info("Output: " + BinaryUtil.toBase64Str(data.getDataBytesStore()));
			data.setPassKey(SecurityUtil.serializeToXml(ciphBean, false, false, true).getBytes());
			outBytes = DataUtil.getValue(data);
			
		}
		catch(DataException de){
			error = true;
		}
		String comp_text = new String(outBytes);
		logger.info("Check: '" + value + "'");
		logger.info("Compare: '" + comp_text + "'");
		assertTrue("Data does not match", comp_text.equals(value));
		//assertTrue("Data was not enciphered",data.getEnciphered());
	}

	@Test
	public void testSetEncipheredProtectedData(){
		logger.info("Setting enciphered protected data");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean ciphBean = new SecurityBean();
		sf.generateSecretKey(ciphBean);

		DataType data = new DataType();
		DataUtil.setPassword(data, "password");
		DataUtil.setCipher(data, ciphBean);
		boolean error = false;
		String  value = "Example text";
		try{
			DataUtil.setValue(data, value.getBytes());
		}
		catch(DataException de){
			error = true;
		}
		assertTrue("Data was not protected",data.getPasswordProtected());
		assertTrue("Data was not enciphered",data.getEnciphered());
	}
	
	@Test
	public void testGetEncipheredProtectedData(){
		logger.info("Getting enciphered protected data");
		SecurityFactory sf = SecurityFactory.getSecurityFactory();
		SecurityBean ciphBean = new SecurityBean();
		sf.generateSecretKey(ciphBean);

		DataType data = new DataType();
		DataUtil.setPassword(data, "password");
		DataUtil.setCipher(data, ciphBean);
		boolean error = false;
		String  value = "Example text";
		byte[] outBytes = new byte[0];
		try{
			DataUtil.setValue(data, value.getBytes());
			logger.info("Input: " + BinaryUtil.toBase64Str(value));
			logger.info("Output: " + BinaryUtil.toBase64Str(data.getDataBytesStore()));
			/// By design - must reset any ciphers after setting or reading the value, as DataUtil will null these out for safety
			///
			DataUtil.setPassword(data, "password");
			DataUtil.setCipher(data, ciphBean);
			outBytes = DataUtil.getValue(data);
			
		}
		catch(DataException de){
			error = true;
		}

		String comp_text = new String(outBytes);
		logger.info("Check: '" + value + "'");
		logger.info("Compare: '" + comp_text + "'");

		assertTrue("Data does not match", comp_text.equals(value));
		//assertTrue("Data was not enciphered",data.getEnciphered());
	}


	


}
