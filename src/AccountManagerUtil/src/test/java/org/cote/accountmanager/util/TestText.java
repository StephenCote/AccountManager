package org.cote.accountmanager.util;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
;

public class TestText {
	public static final Logger logger = Logger.getLogger(TestText.class.getName());
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
	public void TestText(){
		String code = "<html><head>ADw-script AD4-alert(202) ADw-/script AD4-</head><body><h1>Test</h1></body></html>";
		String out = TextUtil.encodeForHTML(TextUtil.toUTF8(code));
		//logger.info(out);
	}

	@Test
	public void TestASCIICharsOnly(){
		String code = "!@#$%^&*(){}+-= ABC abc '\" ;: 09 .,";
		String out = TextUtil.toAsciiCharactersOnly(TextUtil.toUTF8(code));
		logger.info(out);
	}
	
}

