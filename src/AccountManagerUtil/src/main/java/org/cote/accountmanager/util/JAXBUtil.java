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
package org.cote.accountmanager.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.HashMap;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.namespace.QName;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class JAXBUtil {
	public static final Logger logger = LogManager.getLogger(JAXBUtil.class);
	public static <U,T> T clone(Class<T> tClass, U map){
		return clone(tClass,map,new QName("http://www.cote.org/accountmanager/objects"));
	}
	
	/// 2017/06/30 - Switched the clone operation away from JAXB since there's a really bad GC issue when put under load
	///
	public static <U,T> T clone(Class<T> tClass, U map, QName qName){
		/// Trying bouncing through JSON to avoid JAXB GC problems
		///
		return (T)JSONUtil.importObject(JSONUtil.exportObject(map), map.getClass());
		//return jaxbClone(tClass, map, qName);
	}
	public static <U,T> T jaxbClone(Class<T> tClass, U map, QName qName){
		 T bean = null;
		try{
			 JAXBContext contextA  = JAXBContext.newInstance(map.getClass());

		      JAXBElement<U> jaxbElementA = new JAXBElement(qName, map.getClass(), map);
		        JAXBSource sourceA = new JAXBSource(contextA, jaxbElementA);
	
		        JAXBContext contextB = JAXBContext.newInstance(tClass);
		        Unmarshaller unmarshallerB = contextB.createUnmarshaller();
		        JAXBElement<T> jaxbElementB = unmarshallerB.unmarshal(sourceA, tClass);
	        bean = jaxbElementB.getValue();
		}
		catch(JAXBException je){
			logger.error(je.getMessage());
			logger.error("Trace",je);
		}
		return bean;
	}
	public static <T> T importObject(Class<T> tClass, String input){

		T obj = null;
	    JAXBContext context;
		try {
			context = JAXBContext.newInstance(tClass);
		    ByteArrayInputStream bais =new ByteArrayInputStream(input.getBytes("UTF-8"));
		    obj = (T) context.createUnmarshaller().unmarshal(bais);
		} catch (JAXBException e) {
			logger.error(e.getMessage());
			logger.error("Trace",e);
		}
		catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			logger.error("Trace",e);
		}

	   return obj;
	}
	public static <T> String exportObject(Class<T> tClass, T obj){

		String output = null;
	    JAXBContext context;
		try {
			context = JAXBContext.newInstance(tClass);
		    ByteArrayOutputStream baos = new ByteArrayOutputStream();
		    context.createMarshaller().marshal(obj, baos);
		    baos.flush();
		    output = new String(baos.toByteArray(),"UTF-8");
		    baos.close();
		} catch (JAXBException e) {
			logger.error(e.getMessage());
			logger.error("Trace",e);
		}
		catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			logger.error("Trace",e);
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.error("Trace",e);
		}
	    return output;
	}
}
