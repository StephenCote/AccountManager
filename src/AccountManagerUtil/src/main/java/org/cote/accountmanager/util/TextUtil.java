package org.cote.accountmanager.util;

import java.io.UnsupportedEncodingException;
import java.util.regex.Pattern;

public class TextUtil {
	private static Pattern regLessThan = Pattern.compile("<");
	private static Pattern regGreaterThan = Pattern.compile(">");
	private static Pattern regAsciiCharactersOnly = Pattern.compile("[^\\x20-\\x7E]");
	public static String toAsciiCharactersOnly(String inStr){
		if(inStr == null) return null;
		return regAsciiCharactersOnly.matcher(inStr).replaceAll("");
		
	}
	public static String toUTF8(String inStr){
		if(inStr == null) return null;
		String outStr = null;
		try {
			outStr = new String(inStr.getBytes(),"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outStr;
	}
	public static String encodeForHTML(String possibleHTML){
		if(possibleHTML == null) return null;
		String outStr = possibleHTML;
		outStr = regLessThan.matcher(outStr).replaceAll("&lt;");
		outStr = regGreaterThan.matcher(outStr).replaceAll("&gt;");
		return outStr;
	}
}
