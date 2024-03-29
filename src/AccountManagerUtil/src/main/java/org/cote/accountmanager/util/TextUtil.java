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

import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TextUtil {
	public static final Logger logger = LogManager.getLogger(TextUtil.class);
	private static Pattern regLessThan = Pattern.compile("<");
	private static Pattern regGreaterThan = Pattern.compile(">");
	private static Pattern regAsciiCharactersOnly = Pattern.compile("[^\\x20-\\x7E]");
	
	private TextUtil() {
		
	}
	public static String toAsciiCharactersOnly(String inStr){
		if(inStr == null)
			return null;
		return regAsciiCharactersOnly.matcher(inStr).replaceAll("");
		
	}
	public static String toUTF8(String inStr){
		if(inStr == null)
			return null;
		return new String(inStr.getBytes(),StandardCharsets.UTF_8);
	}
	
	public static String encodeForHTML(String possibleHTML){
		if(possibleHTML == null)
			return null;
		String outStr = possibleHTML;
		outStr = regLessThan.matcher(outStr).replaceAll("&lt;");
		outStr = regGreaterThan.matcher(outStr).replaceAll("&gt;");
		return outStr;
	}
	
	public static String padString(String value, int padLength){
		StringBuilder outVal = new StringBuilder();
		int len = value.length();
		for(int i = len; i < padLength;i++){
			outVal.append("0");
		}
		outVal.append(value);
		return outVal.toString();
		
	}
}
