/*******************************************************************************
 * Copyright (C) 2002, 2015 Stephen Cote Enterprises, LLC. All rights reserved.
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
package org.cote.accountmanager.web;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;



import org.junit.Before;
import org.junit.Test;


public class TestPatterns{
	public static final Logger logger = Logger.getLogger(TestPatterns.class.getName());
	
	@Before
	public void setUp() throws Exception {
		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}

	}
	
	@Test
	public void TestDimensionPattern(){
		Pattern dimPattern = Pattern.compile("(\\/\\d+x\\d+)$", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		String test = "/Here/There/File.ext/100x100";
		Matcher d = dimPattern.matcher(test);
		logger.info("Match for digit sequence: " + test.matches("(\\d+)"));
		//logger.info("Match for digit-x-digit sequence: " + test.matches("([0-9]+x[0-9]+)"));
		boolean bit = false;
		boolean bFind = d.find();
		boolean bCount = (d.groupCount() == 1);
		if(bFind && bCount){
			bit = true;
		}
		else{
			bit = false;
		}
		logger.info("Find: " + bFind);
		logger.info("Count: " + bCount);
		assertTrue("Could not find pattern",bit);
	}

	@Test
	public void TestUriPattern(){
		String uri = "/Accelerant.Rocket/Data/Home/steve/Data/The Data.ext";
		String sp1 = "^\\/([A-Za-z0-9\\.]+)\\/([\\w]+)([%-_\\/\\s\\.A-Za-z0-9]+)$";
		String sp0 = "\\/([0-9A-Za-z]+)([%-_\\s\\.A-Za-z0-9\\/]+)";
		Pattern p = Pattern.compile(sp1);
		Matcher m = p.matcher(uri);
		assertTrue("Did not find pattern",m.find() && m.groupCount() == 3);
		String org = "/" + m.group(1).replace('.', '/');
		String type = m.group(2);
		String path = m.group(3);
		String name = null;
		int index = 0;
		if((index = path.lastIndexOf('/')) > -1){
			name = path.substring(index+1,path.length());
			path = path.substring(0,index);
		}
		logger.info("Org = " + org);
		logger.info("Type = " + type);
		logger.info("Path = " + path);
		logger.info("Name = " + name);
	}
	
	@Test
	public void TestNoUriPattern(){
		String uri = "";
		Pattern p = Pattern.compile("\\/([0-9A-Za-z]+)([%-_\\s\\.A-Za-z0-9\\/]+)", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		Matcher m = p.matcher(uri);
		assertFalse("Found a pattern",m.find() && m.groupCount() == 2);
	}

	
}