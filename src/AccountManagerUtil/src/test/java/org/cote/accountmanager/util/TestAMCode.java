package org.cote.accountmanager.util;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
;

public class TestAMCode {
	public static final Logger logger = Logger.getLogger(TestAMCode.class.getName());
	@Before
	public void setUp() throws Exception {

		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}
	}

	@After
	public void tearDown() throws Exception {
	}


	
	@Test
	public void TestAMCode(){
		String code = "[b]Test[/b]\n[url=http://www.whitefrost.com]whitefrost[/url]\n[i]test[/i]\n[ul][li]Test[/li][/ul]";
		String out = AMCodeUtil.decodeAMCodeToHtml(code);
		logger.info(out);
	}

}

