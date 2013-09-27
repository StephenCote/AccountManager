package org.cote.accountmanager.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

public class JAXBUtil {

	public static <T> T importObject(Class<T> tClass, String input){

		T obj = null;
		 // setup object mapper using the AppConfig class
	    JAXBContext context;
		try {
			context = JAXBContext.newInstance(tClass);
		    ByteArrayInputStream bais =new ByteArrayInputStream(input.getBytes("UTF-8"));
		    obj = (T) context.createUnmarshaller().unmarshal(bais);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    // parse the XML and return an instance of the AppConfig class
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	   return obj;
	}
	public static <T> String exportObject(Class<T> tClass, T obj){

		String output = null;
		 // setup object mapper using the AppConfig class
	    JAXBContext context;
		try {
			context = JAXBContext.newInstance(tClass);
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    context.createMarshaller().marshal(obj, baos);
		    baos.flush();
		    output = new String(baos.toByteArray(),"UTF-8");
		    baos.close();
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    // parse the XML and return an instance of the AppConfig class
		catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    		//unmarshal(createInput("config.xml"));
	    return output;
	}
}
