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
package org.cote.accountmanager.service.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;

import org.apache.log4j.Logger;
import org.cote.accountmanager.util.XmlUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ServiceSchemaBuilder {
	public static final Logger logger = Logger.getLogger(ServiceSchemaBuilder.class.getName());
	public static SchemaBean modelRESTService(Class c, String servicePath){
		SchemaBean schemaBean = new SchemaBean();
		schemaBean.setServiceURL(servicePath);
		Method[] ma = c.getDeclaredMethods();
		for(int i = 0; i < ma.length;i++){
			logger.info("Declared method: " + ma[i].getName());
			if(
				ServiceSchemaBuilder.isAnnotated(ma[i], javax.ws.rs.POST.class)
				|| ServiceSchemaBuilder.isAnnotated(ma[i], javax.ws.rs.GET.class)
				|| ServiceSchemaBuilder.isAnnotated(ma[i], javax.ws.rs.PUT.class)
				|| ServiceSchemaBuilder.isAnnotated(ma[i], javax.ws.rs.DELETE.class)
			){
				String altName = ma[i].getName();
				if(ServiceSchemaBuilder.isAnnotated(ma[i], javax.ws.rs.Path.class)){
					Path an = ma[i].getAnnotation(Path.class);

					if(an != null && an.value() != null){
						altName = an.value().replaceAll("/","");
						if(altName.indexOf("{") > -1) altName = altName.substring(0,altName.indexOf("{"));
						//logger.info("Annotated for " + altName);
					}
					else{
						//logger.info("Null path for " + altName);
					}
				}
				else{
					logger.info("Not annotated for " + altName);
				}
				schemaBean.getMethods().add(ServiceSchemaBuilder.modelMethod(ma[i], altName));
			}
		}
		return schemaBean;
	}
	public static ServiceSchemaMethod modelMethod(Method m){
		return modelMethod(m, m.getName());
	}
	public static ServiceSchemaMethod modelMethod(Method m, String name){
		ServiceSchemaMethod meth = new ServiceSchemaMethod();
		meth.setName(name);
		if(m.getReturnType() != null){
			meth.setReturnValue(new ServiceSchemaMethodParameter("retVal",m.getReturnType().getName()));
		}
		for(int i = 0; i < m.getParameterTypes().length;i++){
			meth.getParameters().add(new ServiceSchemaMethodParameter("p" + i,m.getParameterTypes()[i].getName()));
		}

		if(ServiceSchemaBuilder.isAnnotated(m, javax.ws.rs.POST.class)) meth.setHttpMethod("POST");
		else if (ServiceSchemaBuilder.isAnnotated(m, javax.ws.rs.PUT.class)) meth.setHttpMethod("PUT");
		else if(ServiceSchemaBuilder.isAnnotated(m, javax.ws.rs.DELETE.class)) meth.setHttpMethod("DELETE");
		/*
		logger.info(m.getName() + " annotated for " + 
				ServiceSchemaBuilder.isAnnotated(m, javax.ws.rs.POST.class) 
				+ ServiceSchemaBuilder.isAnnotated(m, javax.ws.rs.GET.class)
				+ ServiceSchemaBuilder.isAnnotated(m, javax.ws.rs.PUT.class)
				+ ServiceSchemaBuilder.isAnnotated(m, javax.ws.rs.DELETE.class)
				);
		*/	
		return meth;
	}
	public static boolean isAnnotated(Method m, Class c){
		boolean out_bool = false;
		try{
			Annotation[] an = m.getDeclaredAnnotations();
			for(int i = 0; i < an.length;i++){
				if(an[i].annotationType().getName().equals(c.getName())){
					//logger.info(an[i].annotationType().getName() + " == " + c.getName());
					out_bool = true;
					break;
				}
			}
			//Annotation an = m.getAnnotation(c);
			//logger.info("Annotation " + c.getName() + " == " + (an == null? an.annotationType().getName() : false));
			//out_bool = true;
		}
		catch(Exception e){
			/// sink error
		}
		return out_bool;
			
	}
	public static ServiceSchemaMethod[] getMethods(Class c, Document d){
		String name = c.getSimpleName();
		Element svc = null;
		NodeList nl = d.getElementsByTagName("service");
		for(int i = 0; i < nl.getLength(); i++){
			Element m = (Element)nl.item(i);
			if(name.equals(m.getAttribute("name"))){
				svc = m;
				break;
			}
		}
		if(svc == null) return new ServiceSchemaMethod[0];
		return getMethods(svc);
	}
	public static ServiceSchemaMethod[] getMethods(Element svc){
		List<ServiceSchemaMethod> methods = new ArrayList<ServiceSchemaMethod>();
		NodeList nl = svc.getElementsByTagName("method");
		if(nl.getLength() == 0) return new ServiceSchemaMethod[0];
		for(int i = 0; i < nl.getLength(); i++){
			
			Element m = (Element)nl.item(i);
			String name = XmlUtil.GetElementText(m, "name");
			
			
			ServiceSchemaMethod newMethod = new ServiceSchemaMethod();
			newMethod.setName(name);
			NodeList pl = m.getElementsByTagName("parameter");
			for(int pt = 0; pt < pl.getLength(); pt++){
				Element p = (Element)pl.item(pt);
				String pName = XmlUtil.GetElementText(p,  "name");
				String pType = XmlUtil.GetElementText(p, "type");
				newMethod.getParameters().add(new ServiceSchemaMethodParameter(pName, pType));
			}
			methods.add(newMethod);
		}
		return methods.toArray(new ServiceSchemaMethod[0]);
	}
}
