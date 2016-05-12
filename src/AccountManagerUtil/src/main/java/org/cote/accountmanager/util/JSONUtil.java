package org.cote.accountmanager.util;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JSONUtil {
	public static final Logger logger = Logger.getLogger(JSONUtil.class.getName());
	
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
