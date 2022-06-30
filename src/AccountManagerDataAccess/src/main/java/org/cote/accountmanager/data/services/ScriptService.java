/*******************************************************************************
 * Copyright (C) 2002, 2020 Stephen Cote Enterprises, LLC. All rights reserved.
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
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.FunctionEnumType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.DataUtil;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Value;

public class ScriptService {
	public static final Logger logger = LogManager.getLogger(ScriptService.class);
	private static final String scriptEngineGraal = "graal.js";
	private static String scriptEngineName = scriptEngineGraal;
	//private static Context jsEngine = null;
	public static void setScriptEngineName(String s){
		scriptEngineName = s;
	}
	// private static Map<String,CompiledScript> jsCompiled = new HashMap<>();
	
	public static Context getJavaScriptEngine(){
		Context jsEngine = null;
		if(jsEngine == null){
			if(scriptEngineName.contentEquals(scriptEngineGraal)) {
			    Context context = Context.newBuilder("js")
			    	.allowIO(true)
			    	.allowPolyglotAccess(PolyglotAccess.ALL)
			    	.allowHostAccess(HostAccess.ALL)
			    	.allowHostClassLookup(className -> ScriptClassFilter.classFilter(className))
			    	.build();
			    context.getBindings("js").putMember("ScriptResolver", new ScriptResolver());
			    jsEngine = context;
			}
			else{
				logger.error("Not supported: " + scriptEngineName);
			}
		}
		return jsEngine;
	}
	public static <T> T run(Class<T> cls, Map<String,Object> params,FunctionType func) throws FactoryException,ArgumentException{

		if(func.getFunctionType() != FunctionEnumType.JAVASCRIPT) throw new ArgumentException("FunctionType '" + func.getFunctionType().toString() + "' is not applicable");
		DataType data = func.getFunctionData();
		if(data == null && func.getSourceUrn() != null){
			data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getByUrn(func.getSourceUrn());
			//throw new FactoryException("TODO: Add getByUrn");
		}
		if(data == null) throw new ArgumentException("Function '" + func.getName() + "' data is null");
		else if(data.getDetailsOnly()) throw new ArgumentException("Function data is not properly loaded");

		return run(cls, params,data);
	}
	public static <T> T run(Class<T> cls, Map<String,Object> params,DataType data) throws ArgumentException{

		String value = null;
		try {
			value = DataUtil.getValueString(data);
		} catch (DataException e) {
			
			logger.error(e.getMessage());
		}
		return run(cls, value,params);
	}

	public static Map<String, Object> getCommonParameterMap(UserType user){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("logger", logger);
		if(user != null) {
			params.put("user", user);
			params.put("organizationPath", user.getOrganizationPath());
		}
		
		return params;
	}
	public static String processTokens(UserType user, String scriptText){
		return scriptText.replaceAll("\\$\\{userUrn\\}", user.getUrn());
	}
	
	public static <T> T run(Class<T> cls, String script,Map<String,Object> params) throws ArgumentException{
		return run(script, params).as(cls);
	}
	public static Value run(String script,Map<String,Object> params) throws ArgumentException{
		Context context = getJavaScriptEngine();
		Value resp = null;
		if(context != null){
			if(params != null) {
				for(String key : params.keySet()){
					logger.info("Binding: " + key);
					context.getBindings("js").putMember(key, params.get(key));
				}
			}
			resp = context.eval("js",script);
		}
		else{
			throw new ArgumentException("Script context is null");
		}
		return resp;
	}

	public static class ScriptResolver {
		public String contextPath() {
			return System.getProperty("user.dir").replace("\\", "/");
		}
	}
}

class ScriptClassFilter {
    private static Pattern[] restrictedClasses = new Pattern[]{
    		Pattern.compile("^org\\.cote\\.accountmanager\\.data\\.factory"),
    		Pattern.compile("^org\\.cote\\.accountmanager\\.data\\.Factories"),
    		Pattern.compile("^org\\.cote\\.accountmanager\\.data\\.security"),
    		Pattern.compile("^org\\.cote\\.accountmanager\\.services\\.TypeSanitizer"),
    		Pattern.compile("^org\\.cote\\.accountmanager\\.services\\.SessionSecurity"),
    		Pattern.compile("^org\\.cote\\.accountmanager\\.services\\.VaultService"),
    		Pattern.compile("^org\\.cote\\.rocket\\.factory"),
    		Pattern.compile("^org\\.cote\\.rocket\\.Factories"),
    		Pattern.compile("^org\\.cote\\.rocket\\.Factories"),
    		Pattern.compile("^org\\.cote\\.rocket\\.services.TypeSanitizer")
	};
   
    public static boolean classFilter(String className){
    	boolean matched = false;
  	  for(int i = 0; i < restrictedClasses.length; i++){
  		  if(restrictedClasses[i].matcher(className).find()){
  			  matched = true;
  			  break;
  		  }
  	  }
      return (matched == false);
    }

  }
