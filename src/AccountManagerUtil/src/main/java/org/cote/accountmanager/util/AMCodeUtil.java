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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class AMCodeUtil {
	private static final String LINEPATTERN = "(.|\\n|\\r)*?)";
	private static final String SPACEPATTERN = "(?:\\s*)";
	private static Map<Pattern,String> patterns = null;
	private static Map<Pattern, String> getPatterns(){
		if(patterns != null) return patterns;
		patterns = new HashMap<>();

		patterns.put(Pattern.compile("\\[ul" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/ul" + SPACEPATTERN + "\\]"), "<ul>$1</ul>");
		patterns.put(Pattern.compile("\\[ol" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/ol" + SPACEPATTERN + "\\]"), "<ol>$1</ol>");
		patterns.put(Pattern.compile("\\[li" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/li" + SPACEPATTERN + "\\]"), "<li>$1</li>");
		patterns.put(Pattern.compile("\\[b" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/b" + SPACEPATTERN + "\\]"), "<b>$1</b>");
		patterns.put(Pattern.compile("\\[p" + SPACEPATTERN + "\\]"), "<p>");
		patterns.put(Pattern.compile("\\[/p" + SPACEPATTERN + "\\]"), "</p>");
		patterns.put(Pattern.compile("\\[p class=(" +LINEPATTERN + SPACEPATTERN + "\\]"), "<p class=\"$1\">");
		patterns.put(Pattern.compile("\\[div class=(" +LINEPATTERN + SPACEPATTERN + "\\]"), "<div class=\"$1\">");
		patterns.put(Pattern.compile("\\[div style=(" +LINEPATTERN + SPACEPATTERN + "\\]"), "<div style=\"$1\">");
		patterns.put(Pattern.compile("\\[/div" + SPACEPATTERN + "\\]"), "</div>");
		patterns.put(Pattern.compile("\\[div" + SPACEPATTERN + "\\]"), "<div>");


		patterns.put(Pattern.compile("\\[blockquote" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/blockquote" + SPACEPATTERN + "\\]"), "<blockquote>$1</blockquote>");
		patterns.put(Pattern.compile("\\[h1" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/h1" + SPACEPATTERN + "\\]"), "<h1>$1</h1>");
		patterns.put(Pattern.compile("\\[h2" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/h2" + SPACEPATTERN + "\\]"), "<h2>$1</h2>");
		patterns.put(Pattern.compile("\\[h3" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/h3" + SPACEPATTERN + "\\]"), "<h3>$1</h3>");
		patterns.put(Pattern.compile("\\[h4" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/h4" + SPACEPATTERN + "\\]"), "<h4>$1</h4>");
		patterns.put(Pattern.compile("\\[h5" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/h5" + SPACEPATTERN + "\\]"), "<h5>$1</h5>");
		patterns.put(Pattern.compile("\\[i" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/i" + SPACEPATTERN + "\\]"), "<i>$1</i>");
		patterns.put(Pattern.compile("\\[s" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/s" + SPACEPATTERN + "\\]"), "<strike>$1</strike>");
		patterns.put(Pattern.compile("\\[url" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/url" + SPACEPATTERN + "\\]"),"<a href=\"$1\" target=\"_blank\" title=\"$1\">$1</a>");
		patterns.put(Pattern.compile("\\[url=\"\"(" +LINEPATTERN + SPACEPATTERN + "\"\"\\](" +LINEPATTERN + "\\[/url" + SPACEPATTERN + "\\]"), "<a href=\"$1\" target=\"_blank\" title=\"$1\">$3</a>");
		patterns.put(Pattern.compile("\\[url=(" +LINEPATTERN + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/url" + SPACEPATTERN + "\\]"), "<a href=\"$1\" target=\"_blank\" title=\"$1\">$3</a>");
		patterns.put(Pattern.compile("\\[img" + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/img" + SPACEPATTERN + "\\]"), "<img src=\"$1\" border=\"0\" alt=\"\" />");
		patterns.put(Pattern.compile("\\[img align=(" +LINEPATTERN + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/img" + SPACEPATTERN + "\\]"), "<img src=\"$3\" border=\"0\" align=\"$1\" alt=\"\" />");
		patterns.put(Pattern.compile("\\[img class=(" +LINEPATTERN + SPACEPATTERN + "\\](" +LINEPATTERN + "\\[/img" + SPACEPATTERN + "\\]"), "<img src=\"$3\" border=\"0\" class=\"$1\" alt=\"\" />");
		patterns.put(Pattern.compile("\\[img=(" +LINEPATTERN + "x(" +LINEPATTERN + SPACEPATTERN + "\\]((.|\n)*?)\\[/img" + SPACEPATTERN + "\\]"), "<img width=\"$1\" height=\"$3\" src=\"$5\" border=\"0\" alt=\"\" />");
		patterns.put(Pattern.compile("\\[hr" + SPACEPATTERN + "\\]"), "<hr />");
		
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
