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
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.GenericType;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.NameIdType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.ResponseEnumType;
import org.cote.accountmanager.service.rest.SchemaBean;
import org.cote.accountmanager.service.rest.ServiceSchemaBuilder;
import org.cote.accountmanager.util.JSONUtil;
import org.cote.accountmanager.util.StreamUtil;
import org.cote.beans.EntitySchema;
import org.cote.beans.MessageBean;
import org.cote.rest.services.GroupService;
import org.junit.Before;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

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
	public void TestSerialization(){
		EntitySchema bean = new EntitySchema();
		DataType data = bean.getDataTypeSchema();
		List<DataType> dataList = new ArrayList<>();
		dataList.add(data);
		Class dc = DataType.class;
		//Type cc2 = List<DataType>.getClass().getGenericSuperclass();

		TestRequest<List<?>> req = new TestRequest<>(ActionEnumType.READ, AuditEnumType.DATA, ResponseEnumType.INFO);
		req.setRequest(dataList);
		String json = JSONUtil.exportObject(req);
		logger.info(json);
		/*
		TestRequest<?> req2 = null;

			ObjectMapper mapper = new ObjectMapper();
			try {
				TypeFactory t = TypeFactory.defaultInstance();
				req2 = mapper.readValue(json, t.constructType(TestRequest.class, NameIdType.class));
				//GenericType<List<DataType>> gt = new GenericType<List<DataType>>();
				List<DataType> duck = (List<DataType>)req2.getRequest();
				List<DataType> ducks = new ArrayList<>();
				for(DataType obj : duck){
					ducks.add(obj);
				}

				//NameIdType chk1 = duck.get(0);
				//DataType d1 = (DataType)chk1;
				//logger.info("Restored: " + events.size() + " events");
			} catch (IOException e) {
				
				logger.error(e.getStackTrace());
			}
			
		JSONUtil.importObject(json, TestRequest.class);
		*/
		
	}
	  public static Class<?> getClass(Type type) {
		    if (type instanceof Class) {
		      return (Class) type;
		    }
		    else if (type instanceof ParameterizedType) {
		      return getClass(((ParameterizedType) type).getRawType());
		    }
		    else if (type instanceof GenericArrayType) {
		      Type componentType = ((GenericArrayType) type).getGenericComponentType();
		      Class<?> componentClass = getClass(componentType);
		      if (componentClass != null ) {
		        return Array.newInstance(componentClass, 0).getClass();
		      }
		      else {
		    	  logger.error("Component class is null");
		        return null;
		      }
		    }
		    else {
		    	logger.error("Don't know type instance: " + type);
		      return null;
		    }
		  }
	  public static <T> List<Class<?>> getTypeArguments(
			    Class<T> baseClass, Class<? extends T> childClass) {
			    Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
			    Type type = childClass;
			    // start walking up the inheritance hierarchy until we hit baseClass
			    while (! getClass(type).equals(baseClass)) {
			      if (type instanceof Class) {
			        // there is no useful information for us in raw types, so just keep going.
			        type = ((Class) type).getGenericSuperclass();
			      }
			      else {
			        ParameterizedType parameterizedType = (ParameterizedType) type;
			        Class<?> rawType = (Class) parameterizedType.getRawType();
			  
			        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
			        TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
			        for (int i = 0; i < actualTypeArguments.length; i++) {
			          resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
			        }
			  
			        if (!rawType.equals(baseClass)) {
			          type = rawType.getGenericSuperclass();
			        }
			      }
			    }
			  
			    // finally, for each actual type argument provided to baseClass, determine (if possible)
			    // the raw class for that type argument.
			    Type[] actualTypeArguments;
			    if (type instanceof Class) {
			      actualTypeArguments = ((Class) type).getTypeParameters();
			    }
			    else {
			      actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
			    }
			    List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
			    // resolve types by chasing down type variables.
			    for (Type baseType: actualTypeArguments) {
			      while (resolvedTypes.containsKey(baseType)) {
			        baseType = resolvedTypes.get(baseType);
			      }
			      typeArgumentsAsClasses.add(getClass(baseType));
			    }
			    return typeArgumentsAsClasses;
			  }
	
	public static <T> GenericType<T> getType(Class<T> classZ){
        ParameterizedType genericType = new ParameterizedType() {            
            public Type[] getActualTypeArguments() {
                return new Type[] {classZ};
            }
            public Type getRawType() {
                return List.class;
            }
            public Type getOwnerType() {
                return List.class;
            }
        };
        
        return new GenericType<T>(genericType) {};
	}
	/*
	<T> List<T> getList(String path, List<String[]> params, final Class<T> clazz) {
	    GenericType<List<T>> type = getListType(clazz);
	    Response entity = getEntity(path, params);
	    return entity.readEntity(type);
	}

	<T> T getObject(String path, List<String[]> params, final Class<T> type) {
	    Response entity = getEntity(path, params);
	    return (T)entity.readEntity(new GenericType(type) {});
	}

	private <T> GenericType<List<T>> getListType(final Class<T> clazz) {
	    ParameterizedType genericType = new ParameterizedType() {
	        public Type[] getActualTypeArguments() {
	            return new Type[]{clazz};
	        }

	        public Type getRawType() {
	            return List.class;
	        }

	        public Type getOwnerType() {
	            return List.class;
	        }
	    };
	    return new GenericType<List<T>>(genericType) { };
	}
	*/
	public <T> TestRequest<T> getRequest(final Class<T> classZ){
		return new TestRequest<T>();
	}
	public static class TestRequest<T>{
		private Class classType = null;
		private GenericType<T> genType = null;
		private T request = null;
		private ActionEnumType requestType;
		private AuditEnumType objectType;
		private ResponseEnumType responseType;
		public TestRequest(ActionEnumType act, AuditEnumType obj, ResponseEnumType resp){
			requestType = act;
			objectType = obj;
			responseType = resp;
			//classType = (Class<T>)obj.getClass();
		}
		public TestRequest(){

		}
		
		public GenericType<T> getGenType() {
			return genType;
		}
		public void setGenType(GenericType<T> genType) {
			this.genType = genType;
		}
		public Class getClassType() {
			return classType;
		}
		public void setClassType(Class classType) {
			this.classType = classType;
		}
		public void setRequestType(ActionEnumType requestType) {
			this.requestType = requestType;
		}
		public void setObjectType(AuditEnumType objectType) {
			this.objectType = objectType;
		}
		public void setResponseType(ResponseEnumType responseType) {
			this.responseType = responseType;
		}
		public T getRequest() {
			return request;
		}
		public void setRequest(T req) {
			this.request = req;
			//this.genType = getType((Class<T>)request.getClass().getSuperclass());
			this.classType = getTypeArguments(TestRequest.class, getClass()).get(0);
		}
		public ActionEnumType getRequestType() {
			return requestType;
		}
		public AuditEnumType getObjectType() {
			return objectType;
		}
		public ResponseEnumType getResponseType() {
			return responseType;
		}
		
	}
	
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
			
			logger.error(e.getStackTrace());
		}
		try {
			in.close();
		} catch (IOException e) {
			
			logger.error(e.getStackTrace());
		}
		return b;
	}

}