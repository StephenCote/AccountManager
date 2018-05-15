/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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

import java.io.IOException;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.exceptions.FactoryException;

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
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
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
			logger.error(FactoryException.TRACE_EXCEPTION,e);
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
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		return outStr;
	}
}
