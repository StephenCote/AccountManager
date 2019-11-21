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
package org.cote.accountmanager.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cote.accountmanager.exceptions.FactoryException;

public class BinaryUtil {
	public static final Logger logger = LogManager.getLogger(BinaryUtil.class);
	public static String fromBase64Str(String in_base64){
		return fromBase64Str(in_base64.getBytes());
	}
	public static String fromBase64Str(byte[] in_base64){
		String out_str = null;
		try {
			out_str = (new String(fromBase64(in_base64),"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		return out_str;

	}
	public static byte[] fromBase64(byte[] in_base64){
		return Base64.decodeBase64(in_base64);
	}
	public static String toBase64Str(String in_str){
		byte[] b = new byte[0];
		try {
			b = in_str.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		return toBase64Str(b);
	}
	public static String toBase64Str(byte[] in_str){
		String out_str = null;
		try {
			out_str = new String(toBase64(in_str),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage());
			logger.error(FactoryException.TRACE_EXCEPTION,e);
		}
		return out_str;
	}
	public static byte[] toBase64(byte[] in_str){
		return Base64.encodeBase64(in_str);
		
	}
}
