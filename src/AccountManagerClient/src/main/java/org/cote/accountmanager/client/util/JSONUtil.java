package org.cote.accountmanager.client.util;

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JSONUtil {
	public static final Logger logger = LogManager.getLogger(JSONUtil.class);
	public static <T> Map<String,T> getMap(byte[] data, Class keyClass, Class mapClass){
		ObjectMapper mapper = new ObjectMapper();
		Map<String,T> map = null;
		try {
			TypeFactory t = TypeFactory.defaultInstance();
			map = mapper.readValue(data, t.constructMapType(Map.class, keyClass, mapClass));
		} catch (IOException e) {
			
			logger.error("Error",e);
		}
		return map;
		
	}

	
	public static <T> T importObject(String s,Class<T>  cls){
		ObjectMapper mapper = new ObjectMapper();
		T outObj = null;
		try {
			outObj = mapper.readValue(s, cls);
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.error("Trace",e);
		}
		return outObj;
		
	}
	public static <T> String exportObject(T obj){
		ObjectMapper mapper = new ObjectMapper();
		mapper.setSerializationInclusion(Include.NON_EMPTY);
		 String outStr = null;
		try {
			outStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.error("Trace",e);
		}
		return outStr;
	}
}
