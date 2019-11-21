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
