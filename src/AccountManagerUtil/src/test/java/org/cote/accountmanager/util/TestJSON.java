package org.cote.accountmanager.util;

import static org.junit.Assert.assertNotNull;

import java.security.Security;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.objects.DataType;
import org.junit.Before;
import org.junit.Test;

public class TestJSON {
	public static final Logger logger = Logger.getLogger(TestJSON.class.getName());
	@Before
	public void setUp() throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}
	}
	
	@Test
	public void testJSONSerialization(){
		DataType data = new DataType();
		String str = JSONUtil.exportObject(data);
		assertNotNull("Failed to export",str);
		
		DataType data2 = JSONUtil.importObject(str, DataType.class);
		assertNotNull("Failed to import",data2);

		
	}
}
