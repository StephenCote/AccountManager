package org.cote.accountmanager.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.Security;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.PolicyType;
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
	/*
		Actor {Can/Can't} {Do} PermissionSuffix Type
		Rule
	*/	
	public String getImportType(){
		return "{"
			+ "\"name\":\"Test Name\""
			+ ",\"rules\":["
				+ "{"
					+ "\"name\":\"Test Sub Type\""
				+ "}"
			+ "]"
			+ "}"
		;
	}
	
	@Test
	public void testSimplifiedTypeImport(){
		PolicyType policy = JSONUtil.importObject(getImportType(), PolicyType.class);
		assertNotNull("Imported type is null",policy);
		assertTrue("Imported type missing child type",policy.getRules().size() > 0);
	
		policy = JSONUtil.importObject(FileUtil.getFileAsString("./testType.json"), PolicyType.class);
		assertNotNull("Imported type #2 is null",policy);
		
		//FactEnumType.
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
