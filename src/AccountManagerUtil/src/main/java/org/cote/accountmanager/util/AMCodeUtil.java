/*******************************************************************************
 * Copyright (C) 2002, 2017 Stephen Cote Enterprises, LLC. All rights reserved.
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class AMCodeUtil {
	
	private static Map<Pattern,String> patterns = null;
	private static Map<Pattern, String> getPatterns(){
		if(patterns != null) return patterns;
		patterns = new HashMap<>();

		patterns.put(Pattern.compile("\\[ul(?:\\s*)\\]((.|\\n|\\r)*?)\\[/ul(?:\\s*)\\]"), "<ul>$1</ul>");
		patterns.put(Pattern.compile("\\[ol(?:\\s*)\\]((.|\\n|\\r)*?)\\[/ol(?:\\s*)\\]"), "<ol>$1</ol>");
		patterns.put(Pattern.compile("\\[li(?:\\s*)\\]((.|\\n|\\r)*?)\\[/li(?:\\s*)\\]"), "<li>$1</li>");
		patterns.put(Pattern.compile("\\[b(?:\\s*)\\]((.|\\n|\\r)*?)\\[/b(?:\\s*)\\]"), "<b>$1</b>");
		patterns.put(Pattern.compile("\\[p(?:\\s*)\\]"), "<p>");
		patterns.put(Pattern.compile("\\[/p(?:\\s*)\\]"), "</p>");
		patterns.put(Pattern.compile("\\[p class=((.|\\n|\\r)*?)(?:\\s*)\\]"), "<p class=\"$1\">");
		patterns.put(Pattern.compile("\\[div class=((.|\\n|\\r)*?)(?:\\s*)\\]"), "<div class=\"$1\">");
		patterns.put(Pattern.compile("\\[/div(?:\\s*)\\]"), "</div>");
		patterns.put(Pattern.compile("\\[div(?:\\s*)\\]"), "<div>");


		patterns.put(Pattern.compile("\\[blockquote(?:\\s*)\\]((.|\\n|\\r)*?)\\[/blockquote(?:\\s*)\\]"), "<blockquote>$1</blockquote>");
		patterns.put(Pattern.compile("\\[h1(?:\\s*)\\]((.|\\n|\\r)*?)\\[/h1(?:\\s*)\\]"), "<h1>$1</h1>");
		patterns.put(Pattern.compile("\\[h2(?:\\s*)\\]((.|\\n|\\r)*?)\\[/h2(?:\\s*)\\]"), "<h2>$1</h2>");
		patterns.put(Pattern.compile("\\[h3(?:\\s*)\\]((.|\\n|\\r)*?)\\[/h3(?:\\s*)\\]"), "<h3>$1</h3>");
		patterns.put(Pattern.compile("\\[h4(?:\\s*)\\]((.|\\n|\\r)*?)\\[/h4(?:\\s*)\\]"), "<h4>$1</h4>");
		patterns.put(Pattern.compile("\\[h5(?:\\s*)\\]((.|\\n|\\r)*?)\\[/h5(?:\\s*)\\]"), "<h5>$1</h5>");
		patterns.put(Pattern.compile("\\[i(?:\\s*)\\]((.|\\n|\\r)*?)\\[/i(?:\\s*)\\]"), "<i>$1</i>");
		patterns.put(Pattern.compile("\\[s(?:\\s*)\\]((.|\\n|\\r)*?)\\[/s(?:\\s*)\\]"), "<strike>$1</strike>");
		patterns.put(Pattern.compile("\\[url(?:\\s*)\\]((.|\\n|\\r)*?)\\[/url(?:\\s*)\\]"),"<a href=\"$1\" target=\"_blank\" title=\"$1\">$1</a>");
		patterns.put(Pattern.compile("\\[url=\"\"((.|\\n|\\r)*?)(?:\\s*)\"\"\\]((.|\\n|\\r)*?)\\[/url(?:\\s*)\\]"), "<a href=\"$1\" target=\"_blank\" title=\"$1\">$3</a>");
		patterns.put(Pattern.compile("\\[url=((.|\\n|\\r)*?)(?:\\s*)\\]((.|\\n|\\r)*?)\\[/url(?:\\s*)\\]"), "<a href=\"$1\" target=\"_blank\" title=\"$1\">$3</a>");
		patterns.put(Pattern.compile("\\[img(?:\\s*)\\]((.|\\n|\\r)*?)\\[/img(?:\\s*)\\]"), "<img src=\"$1\" border=\"0\" alt=\"\" />");
		patterns.put(Pattern.compile("\\[img align=((.|\\n|\\r)*?)(?:\\s*)\\]((.|\\n|\\r)*?)\\[/img(?:\\s*)\\]"), "<img src=\"$3\" border=\"0\" align=\"$1\" alt=\"\" />");
		patterns.put(Pattern.compile("\\[img class=((.|\\n|\\r)*?)(?:\\s*)\\]((.|\\n|\\r)*?)\\[/img(?:\\s*)\\]"), "<img src=\"$3\" border=\"0\" class=\"$1\" alt=\"\" />");
		patterns.put(Pattern.compile("\\[img=((.|\\n|\\r)*?)x((.|\\n|\\r)*?)(?:\\s*)\\]((.|\n)*?)\\[/img(?:\\s*)\\]"), "<img width=\"$1\" height=\"$3\" src=\"$5\" border=\"0\" alt=\"\" />");
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
