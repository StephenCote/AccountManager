package org.cote.accountmanager.util;


import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TestGraphics {
	public static final Logger logger = LogManager.getLogger(TestGraphics.class);


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
			logger.error("Error",e);
			logger.error(e.getMessage());
		}
		
	}
}