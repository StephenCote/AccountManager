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
package org.cote.accountmanager.data.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.cote.accountmanager.data.ArgumentException;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FunctionEnumType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.util.DataUtil;
import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
/*
 * TODO: 2016/08/18
 * There is still a big security gap  in that the package structure doesn't limit some of the base factory access
 * At the moment, a filter class is defined to limit scripted access to the factories.
 */

public class ScriptService {
	public static final Logger logger = Logger.getLogger(ScriptService.class.getName());
	public static String SCRIPT_ENGINE_JAVASCRIPT = "javascript";
	public static String SCRIPT_ENGINE_NASHORN = "nashorn";
	public static String SCRIPT_ENGINE_NAME = SCRIPT_ENGINE_NASHORN;
	private static ScriptEngine jsEngine = null;
	private static Map<String,CompiledScript> jsCompiled = new HashMap<String,CompiledScript>();
	
	public static ScriptEngine getJavaScriptEngine(){
		if(jsEngine == null){
			if(SCRIPT_ENGINE_NAME.equals(SCRIPT_ENGINE_NASHORN)){
			    NashornScriptEngineFactory factory = new NashornScriptEngineFactory();

			    jsEngine = factory.getScriptEngine(
			      new ScriptServiceFilter());
			}
			else{
				jsEngine = new ScriptEngineManager().getEngineByName(SCRIPT_ENGINE_NAME);
		
			}
		}
		return jsEngine;
	}
	public static Object run(UserType user,Map<String,Object> params,FunctionType func) throws ArgumentException{
		//byte[] value = null;
		if(func.getFunctionType() != FunctionEnumType.JAVASCRIPT) throw new ArgumentException("FunctionType '" + func.getFunctionType().toString() + "' is not applicable");
		DataType data = func.getFunctionData();
		if(data == null && func.getSourceUrn() != null){
			data = Factories.getDataFactory().getByUrn(func.getSourceUrn());
		}
		if(data == null) throw new ArgumentException("Function '" + func.getName() + "' data is null");
		else if(data.getDetailsOnly() == true) throw new ArgumentException("Function data is not properly loaded");

		return run(user,params,data);
	}
	public static Object run(UserType user,Map<String,Object> params,DataType data) throws ArgumentException{
		String value = null;
		try {
			value = DataUtil.getValueString(data);
		} catch (DataException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
		}
		return run(user,data.getUrn(),value,params);
	}
	public static Object run(UserType user,String name, String script,Map<String,Object> params) throws ArgumentException{
		CompiledScript compScr = compileScript(name, script);

		if(compScr == null) throw new ArgumentException("Compiled script for '" + name + "' is null");

		return run(user, name, params);

	}
	public static Object run(UserType user,String name,Map<String,Object> params) throws ArgumentException{
		CompiledScript compScr = jsCompiled.get(name);
		Object resp = null;
		if(compScr != null){
			Bindings bd = compScr.getEngine().createBindings();
			Iterator<String> keys = params.keySet().iterator();
			bd.putAll(params);
			bd.put("user", user);
			//DirectoryGroupType homeDir = Factories.getGroupFactory().getUserDirectory(user);
			//bd.put("organizationId", user.getOrganizationId());
			try{
				//logger.info("Evaluating: " + name);
				resp = compScr.eval(bd);
			} catch (ScriptException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			}
		}
		else{
			throw new ArgumentException("Compiled script for '" + name + "' is null");
			//logger.error("Compiled script is null for '" + name + "'");
		}
		return resp;
	}
	public static CompiledScript compileScript(String name, String script){
		ScriptEngine jse = getJavaScriptEngine();
		CompiledScript out_scr = null;
	  if (jse instanceof Compilable)
	    {
	        Compilable compEngine = (Compilable)jse;
			try {
				//logger.debug("Compiling: " + name);
				out_scr = compEngine.compile(script);
				jsCompiled.put(name, out_scr);
			} catch (ScriptException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
			}

	    }
	    else{
	    	
	    	logger.error("Script engine is not compilable");
	    }

	    return out_scr;
	}
}
class ScriptServiceFilter implements ClassFilter {
	private static Pattern am5Factory = Pattern.compile("^org\\.cote.accountmanager\\.data\\.factory");
    private static Pattern[] restrictedClasses = new Pattern[]{
    	Pattern.compile("^org\\.cote.accountmanager\\.data\\.factory"),
    	Pattern.compile("^org\\.cote.accountmanager\\.data\\.Factories"),
    	Pattern.compile("^org\\.cote.accountmanager\\.data\\.security"),
    	Pattern.compile("^org\\.cote.rocket\\.factory"),
    	Pattern.compile("^org\\.cote.rocket\\.Factories"),
    	Pattern.compile("^org\\.cote.rocket\\.Factories")
    };
	//private List<Pattern> restrictedClasses = new ArrayList<>(Arrays.asList(new Pattern[]{}));

	@Override
    public boolean exposeToScripts(String s) {
	  boolean out_bool = true;
      //if (am5Factory.matcher(s).find()) return false;
	  for(int i = 0; i < restrictedClasses.length; i++){
		  if(restrictedClasses[i].matcher(s).find()){
			  out_bool = false;
			  break;
		  }
	  }
      return out_bool;
    }
  }
