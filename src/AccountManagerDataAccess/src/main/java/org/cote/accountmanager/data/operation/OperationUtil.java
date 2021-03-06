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
package org.cote.accountmanager.data.operation;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.exceptions.FactoryException;
;

public class OperationUtil {
	public static final Logger logger = LogManager.getLogger(OperationUtil.class);

	private static Map<String,Class<?>> operations = new HashMap<>();
	private static Map<String,IOperation> operationInst = new HashMap<>();
	
	public static IOperation getOperationInstance(String className){
		Class<?> cls = getOperation(className);
		IOperation oper = null;
		if(cls == null){
			logger.error(className + " is not defined");
			return null;
		}
		
		if(operationInst.containsKey(className)) return operationInst.get(className);
		try {
			oper = (IOperation)cls.getDeclaredConstructor().newInstance();
			operationInst.put(className, oper);

		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return oper;
	}
	public static Class<?> getOperation(String className){
		if(operations.containsKey(className)) return operations.get(className);
		Class<?> cls = null;
		try {
			cls = Class.forName(className);
			operations.put(className, cls);
		} catch (ClassNotFoundException e) {
			
			logger.error(FactoryException.LOGICAL_EXCEPTION,e);
		}
		return cls;
	}
	
}
