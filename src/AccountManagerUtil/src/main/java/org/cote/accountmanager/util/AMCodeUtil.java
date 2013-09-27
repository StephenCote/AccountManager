package org.cote.accountmanager.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class AMCodeUtil {
	
	private static Map<Pattern,String> patterns = null;
	private static Map<Pattern, String> getPatterns(){
		if(patterns != null) return patterns;
		patterns = new HashMap<Pattern,String>();
		
		patterns.put(Pattern.compile("\\[ul(?:\\s*)\\]((.|\\n)*?)\\[/ul(?:\\s*)\\]"), "<ul>$1</ul>");
		patterns.put(Pattern.compile("\\[ol(?:\\s*)\\]((.|\\n)*?)\\[/ol(?:\\s*)\\]"), "<ol>$1</ol>");
		patterns.put(Pattern.compile("\\[li(?:\\s*)\\]((.|\\n)*?)\\[/li(?:\\s*)\\]"), "<li>$1</li>");
		patterns.put(Pattern.compile("\\[b(?:\\s*)\\]((.|\\n)*?)\\[/b(?:\\s*)\\]"), "<b>$1</b>");
		patterns.put(Pattern.compile("\\[i(?:\\s*)\\]((.|\\n)*?)\\[/i(?:\\s*)\\]"), "<i>$1</i>");
		patterns.put(Pattern.compile("\\[s(?:\\s*)\\]((.|\\n)*?)\\[/s(?:\\s*)\\]"), "<strike>$1</strike>");
	
		patterns.put(Pattern.compile("\\[url(?:\\s*)\\]((.|\\n)*?)\\[/url(?:\\s*)\\]"),"<a href=\"$1\" target=\"_blank\" title=\"$1\">$1</a>");
		patterns.put(Pattern.compile("\\[url=\"\"((.|\\n)*?)(?:\\s*)\"\"\\]((.|\\n)*?)\\[/url(?:\\s*)\\]"), "<a href=\"$1\" target=\"_blank\" title=\"$1\">$3</a>");
		patterns.put(Pattern.compile("\\[url=((.|\\n)*?)(?:\\s*)\\]((.|\\n)*?)\\[/url(?:\\s*)\\]"), "<a href=\"$1\" target=\"_blank\" title=\"$1\">$3</a>");
	
		patterns.put(Pattern.compile("\\[img(?:\\s*)\\]((.|\\n)*?)\\[/img(?:\\s*)\\]"), "<img src=\"$1\" border=\"0\" alt=\"\" />");
		patterns.put(Pattern.compile("\\[img align=((.|\\n)*?)(?:\\s*)\\]((.|\\n)*?)\\[/img(?:\\s*)\\]"), "<img src=\"$3\" border=\"0\" align=\"$1\" alt=\"\" />");
		patterns.put(Pattern.compile("\\[img=((.|\\n)*?)x((.|\\n)*?)(?:\\s*)\\]((.|\n)*?)\\[/img(?:\\s*)\\]"), "<img width=\"$1\" height=\"$3\" src=\"$5\" border=\"0\" alt=\"\" />");
	
		patterns.put(Pattern.compile("\\[hr(?:\\s*)\\]"), "<hr />");

		return patterns;
	
	}
	public static String decodeAMCodeToHtml(String input){
		String output = input;
		Map<Pattern, String> patterns = getPatterns();
		Iterator<Pattern> i = patterns.keySet().iterator();
		while(i.hasNext()){
			Pattern p = i.next();
			String r = patterns.get(p);
			output = p.matcher(output).replaceAll(r);
		}
		return output;
	}
	
}
