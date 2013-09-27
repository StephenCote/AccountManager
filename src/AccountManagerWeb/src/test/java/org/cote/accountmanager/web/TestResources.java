package org.cote.accountmanager.web;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.util.StreamUtil;
import org.cote.accountmanager.util.XmlUtil;
import org.cote.beans.MessageBean;
import org.cote.beans.SchemaBean;
import org.cote.rest.schema.ServiceSchemaBuilder;
import org.cote.rest.schema.ServiceSchemaMethod;
import org.cote.rest.services.GroupService;
import org.cote.rest.services.MessageService;
import org.cote.rest.services.SchemaService;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

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