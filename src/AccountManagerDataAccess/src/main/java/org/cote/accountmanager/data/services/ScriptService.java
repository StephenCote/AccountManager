package org.cote.accountmanager.data.services;

import java.util.HashMap;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptService {
	private static ScriptEngine jsEngine = null;
	private static Map<String,CompiledScript> jsCompiled = new HashMap<String,CompiledScript>();
	
	public static ScriptEngine getJavaScriptEngine(){
		if(jsEngine == null){
			jsEngine = new ScriptEngineManager().getEngineByName("javascript");
		}
		return jsEngine;
	}
	public static boolean compileScript(String name, String script){
		
		boolean out_bool = false;
	    if (jsEngine instanceof Compilable)
	    {
	        Compilable compEngine = (Compilable)jsEngine;
			try {
				jsCompiled.put(name, compEngine.compile(script));
				out_bool = true;
			} catch (ScriptException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    return out_bool;
	}
	
}
