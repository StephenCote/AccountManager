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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.util.StreamUtil;
import org.cote.beans.MessageBean;
import org.cote.rest.services.GroupService;
import org.junit.Before;
import org.junit.Test;

public class TestResources{
	public static final Logger logger = Logger.getLogger(TestResources.class.getName());
	
	@Before
	public void setUp() throws Exception {
		String log4jPropertiesPath = System.getProperty("log4j.configuration");
		if(log4jPropertiesPath != null){
			System.out.println("Properties=" + log4jPropertiesPath);
			PropertyConfigurator.configure(log4jPropertiesPath);
		}

	}
	
	/*
	@Test
	public void TestServiceConfig(){
		Document d = XmlUtil.GetDocumentFromBytes(getResourceBytes("/service.config.xml"));
		//logger.info("D=" + (d == null ? " null " : " doc "));
		assertNotNull("XML Resource is null",d);
		ServiceSchemaMethod[] m = ServiceSchemaBuilder.getMethods(TestResources.class, d);
		assertTrue("No methods found for " + this.getClass().getSimpleName(), m.length > 0);
	}
	*/
	@Test
	public void TestSchemaBean(){
		SchemaBean bean = ServiceSchemaBuilder.modelRESTService(GroupService.class, "/group");
		for(int i = 0; i < bean.getMethods().size(); i++){
			logger.info(bean.getMethods().get(i).getName());
		}
	}
	/*
	@Test
	public void TestReflectedServiceConfig(){
		
		Method[] ma = MessageService.class.getDeclaredMethods();
		assertTrue(ma.length > 0);
		for(int i = 0; i < ma.length;i++){
			if(ServiceSchemaBuilder.isAnnotated(ma[i], javax.ws.rs.POST.class) || ServiceSchemaBuilder.isAnnotated(ma[i], javax.ws.rs.GET.class)){
				ServiceSchemaMethod sm = ServiceSchemaBuilder.modelMethod(ma[i]);
				logger.info(sm.getName());
				
			}
			/ *
			logger.info(ma[i].getName());
			Method m = ma[i];
			try{
				Annotation an = ma[i].getAnnotation(javax.ws.rs.POST.class);
				logger.info("Found: " + an.getClass().getName());
			}
			catch(Exception e){
				logger.info("Sink error: " + e.getMessage());
			}
			* /
			/ *
			Annotation[] an = ma[i].getAnnotations();
			for(int a = 0; a < an.length; a++){
				logger.info("..." + an[a]);
			}
			* /
		}
	}
	*/
	public void exampleMethod(MessageBean bean){
		
	}

	private byte[] getResourceBytes(String path){
		InputStream in = getClass().getResourceAsStream(path);
		assertNotNull("Stream is null",in);
		byte[] b = new byte[0];
		try {
			b = StreamUtil.getStreamBytes(in);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return b;
	}

}