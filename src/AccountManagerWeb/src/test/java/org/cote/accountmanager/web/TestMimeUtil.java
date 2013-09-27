package org.cote.accountmanager.web;

import static org.junit.Assert.assertNotNull;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.util.MimeUtil;


import org.junit.Before;
import org.junit.Test;


public class TestMimeUtil{
	public static final Logger logger = Logger.getLogger(TestMimeUtil.class.getName());
	
	@Before
	public void setUp() throws Exception {
		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}

	}

	@Test
	public void TestSchemaBean(){
		String mimeType = MimeUtil.getType("TestFile.pdf");
		assertNotNull("Mime is null",mimeType);
		logger.info("Mimetype = '" + mimeType + "'");
	}
	

}