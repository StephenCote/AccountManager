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
package org.cote.accountmanager.exceptions;

public class FactoryException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final String TRACE_EXCEPTION = "Trace Exception";
	public static final String OBJECT_UNKNOWN_TYPE = "Object cannot be of an unknown type";
	public static final String OBJECT_NULL_REF = "Object is null for reference %s";
	public static final String ARGUMENT_NULL = "Method argument is null";
	public static final String OBJECT_NULL_TYPE = "Object is null for type %s";
	public static final String TYPE_NOT_REGISTERED = "Type %s is not registered";
	public static final String TYPE_ALREADY_REGISTERED = "Type %s is already registered";
	public static final String UNHANDLED_ACTOR_TYPE = "Unhandled actor type: %s";
	public static final String UNHANDLED_TYPE = "Unhandled type: %s";
	public static final String LOGICAL_EXCEPTION = "Logical Exception";
	public static final String LOGICAL_EXCEPTION_MSG = "Logical Exception: %s";
	public static final String PARTICIPATION_FACTORY_REGISTRATION_EXCEPTION = "Participation factory for %s is not registered for authorization";
	public FactoryException(String msg){
		super(msg);
	}
}
