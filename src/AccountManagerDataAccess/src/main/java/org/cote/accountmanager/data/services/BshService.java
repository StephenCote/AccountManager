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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.data.Factories;
import org.cote.accountmanager.data.factory.DataFactory;
import org.cote.accountmanager.data.factory.GroupFactory;
import org.cote.accountmanager.exceptions.ArgumentException;
import org.cote.accountmanager.exceptions.DataException;
import org.cote.accountmanager.exceptions.FactoryException;
import org.cote.accountmanager.objects.AuditType;
import org.cote.accountmanager.objects.DataType;
import org.cote.accountmanager.objects.DirectoryGroupType;
import org.cote.accountmanager.objects.FunctionEnumType;
import org.cote.accountmanager.objects.FunctionType;
import org.cote.accountmanager.objects.UserType;
import org.cote.accountmanager.objects.types.ActionEnumType;
import org.cote.accountmanager.objects.types.AuditEnumType;
import org.cote.accountmanager.objects.types.FactoryEnumType;
import org.cote.accountmanager.util.DataUtil;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.Parser;


public class BshService {

	public static final Logger logger = LogManager.getLogger(BshService.class);

	public static Object run(UserType user,Map<String,Object> params,FunctionType func) throws ArgumentException, FactoryException{
		if(func.getFunctionType() != FunctionEnumType.JAVA) throw new ArgumentException("FunctionType '" + func.getFunctionType().toString() + "' is not applicable");
		DataType data = func.getFunctionData();
		if(data == null && func.getSourceUrn() != null){
			data = ((DataFactory)Factories.getFactory(FactoryEnumType.DATA)).getByUrn(func.getSourceUrn());
		}
		if(data == null) throw new ArgumentException("Function '" + func.getName() + "' data is null");
		else if(data.getDetailsOnly()) throw new ArgumentException("Function data is not properly loaded");

		return run(user,params,data);
	}
	
	public static Object run(UserType user,Map<String,Object> params,DataType data){
		byte[] value = null;
		try {
			value = DataUtil.getValue(data);
		} catch (DataException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return runWithParams(user,params,value,false);
	}
	public static Object run(UserType user,Map<String,Object> params,String script,boolean parseOnly){
		return runWithParams(user,params,script.getBytes(StandardCharsets.UTF_8),parseOnly);
	}
	public static Object runWithParams(UserType contextUser,Map<String,Object> params,byte[] script,boolean parseOnly){
		Object out_obj = null;
		AuditType audit = null;
		if(contextUser != null) audit = AuditService.beginAudit(ActionEnumType.EXECUTE, "BeanShell Script", AuditEnumType.USER, contextUser.getName() + "(#" + contextUser.getId() + ")");
		else audit = AuditService.beginAudit(ActionEnumType.EXECUTE, "BeanShell Script", AuditEnumType.USER, "Anonymous user");
		boolean bComp = false;
		try {
			if(contextUser != null){
				Factories.getNameIdFactory(FactoryEnumType.USER).populate(contextUser);
				Factories.getAttributeFactory().populateAttributes(contextUser);
			}

			AuditService.targetAudit(audit, AuditEnumType.DATA, "BeanShell Script");
			
			byte[] header = getAM5Import();
			byte[] mergeScript =ArrayUtils.addAll(header,script);
			BufferedReader buff = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(mergeScript)));
			if(parseOnly){

				Parser parser = new Parser(buff);
				
				while( !parser.Line()) {
				    parser.popNode();

				}
			}
			else{
				Interpreter intr = getInterpreter(contextUser,params);
				intr.set("audit",audit);
				out_obj = intr.eval(buff);

			}
			buff.close();
			bComp = true;
			
			
		} catch (EvalError | IOException | FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		if(bComp) AuditService.permitResult(audit, "Completed execution");
		else AuditService.denyResult(audit, "Failed to complete execution");
		return out_obj;
	}
	
	public static Interpreter getInterpreter(UserType contextUser,Map<String,Object> params){
		Interpreter intr = new Interpreter();
		try{
			if(contextUser != null){
				DirectoryGroupType homeDir = ((GroupFactory)Factories.getFactory(FactoryEnumType.GROUP)).getUserDirectory(contextUser);
				intr.set("user",contextUser);
				intr.set("organizationId", contextUser.getOrganizationId());
				intr.set("home",homeDir);
			}
			if(params != null){
				Iterator<String> keys = params.keySet().iterator();
				while(keys.hasNext()){
					String key = keys.next();
					intr.set(key,params.get(key));
				}
			}
		}
		catch (EvalError | FactoryException | ArgumentException e) {
			
			logger.error(e.getMessage());
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return intr;
	}
	
	private static byte[] getAM5Import(){

		StringBuilder buff = new StringBuilder();
		buff.append("import org.cote.accountmanager.objects.*;\n");
		buff.append("import org.cote.accountmanager.objects.types.*;\n");
		buff.append("import org.cote.accountmanager.data.*;\n");
		buff.append("import org.apache.logging.log4j.LogManager;\nimport org.apache.logging.log4j.Logger;\n");
		buff.append("Logger logger = LogManager.getLogger(\"BeanShell\");\n");

		return buff.toString().getBytes(StandardCharsets.UTF_8);
	}
}
