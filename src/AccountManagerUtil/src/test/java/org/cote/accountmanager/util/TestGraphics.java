package org.cote.accountmanager.util;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestGraphics {
	public static final Logger logger = Logger.getLogger(TestGraphics.class.getName());
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
	public void TestThumbnail(){
		byte[] src = StreamUtil.fileToBytes("/Users/Steve/Downloads/356381.jpg");
		assertTrue("Source length is empty",src.length > 0);
		logger.info("Source Length = " + src.length);
		try{
			byte[] dest = GraphicsUtil.createThumbnail(src, 100, 100);
			assertTrue("Dest length is empty",dest.length > 0);
			File f = new File("/Users/Steve/Downloads/356381_thumbnail.jpg");
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(dest);
			fos.flush();
			fos.close();
		}
		catch(IOException e){
			e.printStackTrace();
			logger.error(e.getMessage());
		}
		
	}
}