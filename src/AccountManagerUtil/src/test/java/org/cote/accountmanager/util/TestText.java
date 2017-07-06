package org.cote.accountmanager.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.Test;
;

public class TestText {
	public static final Logger logger = LogManager.getLogger(TestText.class);


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

