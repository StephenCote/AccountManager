package org.cote.accountmanager.util;

import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JSONUtil {
	public static final Logger logger = Logger.getLogger(JSONUtil.class.getName());
	public static <T> Map<String,T> getMap(byte[] data, Class keyClass, Class mapClass){
		ObjectMapper mapper = new ObjectMapper();
		Map<String,T> map = null;
		try {
			TypeFactory t = TypeFactory.defaultInstance();
			map = mapper.readValue(data, t.constructMapType(Map.class, keyClass, mapClass));
		} catch (IOException e) {
			
			logger.error(e.getStackTrace());
		}
		return map;
		
	}
	public static <T> Map<String,T> getMap(String path, Class keyClass,Class mapClass){
		return getMap(FileUtil.getFile(path),keyClass,mapClass);
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
