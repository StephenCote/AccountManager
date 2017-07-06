package org.cote.accountmanager.util;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.security.Security;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.PolicyType;
import org.cote.accountmanager.util.SimpleGeography.CountryType;
import org.cote.accountmanager.util.SimpleGeography.RegionType;
import org.junit.Before;
import org.junit.Test;

public class TestJSON {
	public static final Logger logger = LogManager.getLogger(TestJSON.class);
	@Before
	public void setUp() throws Exception {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
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
	public void testImportMap(){
		//String fileStr = FileUtil.getFileAsString("/Users/Steve/Projects/workspace/Location/src/main/webapp/geo/countries.json");
		String fileStr2 = FileUtil.getFileAsString("/Users/Steve/Projects/workspace/Location/src/main/webapp/geo/US.json");
		Map<String,CountryType> countries = SimpleGeography.getCountries("/Users/Steve/Projects/workspace/Location/src/main/webapp/geo/countries.json");
		SimpleGeography.populateCountry(countries, "US","/Users/Steve/Projects/workspace/Location/src/main/webapp/geo/US.json");
		SimpleGeography.populateCountry(countries, "MX","/Users/Steve/Projects/workspace/Location/src/main/webapp/geo/MX.json");
		SimpleGeography.populateCountry(countries, "CA","/Users/Steve/Projects/workspace/Location/src/main/webapp/geo/CA.json");
		CountryType us = countries.get("US");
		assertNotNull(us);
		RegionType[] regions = us.getRegions().get("98275");
		assertTrue(regions.length > 0);
		logger.info(JSONUtil.exportObject(regions[0]));
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
