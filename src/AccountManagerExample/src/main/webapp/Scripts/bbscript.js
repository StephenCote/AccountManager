///bbConvert(frames[1].document.documentElement.childNodes[1].firstChild)

window.bbConverter = {
	patterns : [
	    //{exp:/’/,replace:"'"},
	    //{exp:/-/,replace:"-"},
	    
	    //{exp:/\s{2,}/gi,replace:" "},
	    //{exp:/&nbsp;/gi,replace:""},
	    //{exp:/\sclass\s+=\s+"p1"/gi,replace:""},
		{exp:/\[ul(?:\s*)\]((.|\n|\r)*?)\[\/ul(?:\s*)\]/gi,replace:"<ul>$1</ul>"},
		{exp:/\[ol(?:\s*)\]((.|\n|\r)*?)\[\/ol(?:\s*)\]/gi,replace:"<ol>$1</ol>"},
		{exp:/\[li(?:\s*)\]((.|\n|\r)*?)\[\/li(?:\s*)\]/gi,replace:"<li>$1</li>"},
		{exp:/\[b(?:\s*)\]((.|\n|\r)*?)\[\/b(?:\s*)\]/gi,replace:"<b>$1</b>"},
		{exp:/\[p(?:\s*)\]((.|\n|\r)*?)\[\/p(?:\s*)\]/gi,replace:"<p>$1</p>"},
		{exp:/\[div(?:\s*)\]((.|\n|\r)*?)\[\/div(?:\s*)\]/gi,replace:"<div>$1</div>"},
		{exp:/\[p class=((.|\n|\r)*?)(?:\s*)\]((.|\n|\r)*?)\[\/p(?:\s*)\]/gi,replace:"<p class = \"$1\">$3</p>"},
		{exp:/\[div class=((.|\n|\r)*?)(?:\s*)\]((.|\n|\r)*?)\[\/div(?:\s*)\]/gi,replace:"<div class = \"$1\">$3</div>"},
		{exp:/\[blockquote(?:\s*)\]((.|\n|\r)*?)\[\/blockquote(?:\s*)\]/gi,replace:"<blockquote>$1</blockquote>"},
		{exp:/\[h1(?:\s*)\]((.|\n|\r)*?)\[\/h1(?:\s*)\]/gi,replace:"<h1>$1</h1>"},
		{exp:/\[h2(?:\s*)\]((.|\n|\r)*?)\[\/h2(?:\s*)\]/gi,replace:"<h2>$1</h2>"},
		{exp:/\[h3(?:\s*)\]((.|\n|\r)*?)\[\/h3(?:\s*)\]/gi,replace:"<h3>$1</h3>"},
		{exp:/\[h4(?:\s*)\]((.|\n|\r)*?)\[\/h4(?:\s*)\]/gi,replace:"<h4>$1</h4>"},
		{exp:/\[h5(?:\s*)\]((.|\n|\r)*?)\[\/h5(?:\s*)\]/gi,replace:"<h5>$1</h5>"},
		{exp:/\[i(?:\s*)\]((.|\n|\r)*?)\[\/i(?:\s*)\]/gi,replace:"<i>$1</i>"},
		{exp:/\[s(?:\s*)\]((.|\n|\r)*?)\[\/s(?:\s*)\]/gi,replace:"<strike>$1</strike>"},
	
		{exp:/\[url(?:\s*)\]((.|\n|\r)*?)\[\/url(?:\s*)\]/gi,replace:"<a href=\"$1\" target=\"_blank\" title=\"$1\">$1</a>"},
		{exp:/\[url=\"\"((.|\n|\r)*?)(?:\s*)\"\"\]((.|\n|\r)*?)\[\/url(?:\s*)\]/gi,replace:"<a href=\"$1\" target=\"_blank\" title=\"$1\">$3</a>"},
		{exp:/\[url=((.|\n|\r)*?)(?:\s*)\]((.|\n|\r)*?)\[\/url(?:\s*)\]/gi,replace:"<a href=\"$1\" target=\"_blank\" title=\"$1\">$3</a>"},
	
		{exp:/\[img(?:\s*)\]((.|\n|\r)*?)\[\/img(?:\s*)\]/gi,replace:"<img src=\"$1\" border=\"0\" alt=\"\" />"},
		{exp:/\[img align=((.|\n|\r)*?)(?:\s*)\]((.|\n|\r)*?)\[\/img(?:\s*)\]/gi,replace:"<img src=\"$3\" border=\"0\" align=\"$1\" alt=\"\" />"},
		{exp:/\[img class=((.|\n|\r)*?)(?:\s*)\]((.|\n|\r)*?)\[\/img(?:\s*)\]/gi,replace:"<img src=\"$3\" border=\"0\" class=\"$1\" alt=\"\" />"},
		{exp:/\[img=((.|\n|\r)*?)x((.|\n|\r)*?)(?:\s*)\]((.|\n|\r)*?)\[\/img(?:\s*)\]/gi,replace:"<img width=\"$1\" height=\"$3\" src=\"$5\" border=\"0\" alt=\"\" />"}
	],
	copyInto : function(s, o){
		var x = Hemi.xml.parseXmlDocument("<html-fragment>" + bbConverter.import(s) + "</html-fragment>");
		if(!x || x.documentElement == null){
			Hemi.logError("Failed to parse " + s);
			return 0;
		}
		Hemi.xml.setInnerXHTML(o, x.documentElement);
		return 1;
	},
	import : function(s){
		
		/// Currently isn't handling embedded cases, such as <div><div></div></div>
		
		var aP = bbConverter.patterns;
		for(var i = 0; i < aP.length;i++){
			while(s.match(aP[i].exp)){
				s = s.replace(aP[i].exp, aP[i].replace);
			}
		}
		
		return s;

	},
	convert : function(o){
	   var r = "";
	   if(typeof o == "string") return o;
	   if(typeof o.nodeType != "number") return "[error]";
	   switch(o.nodeType){
	   		case 1:
	   			r = bbConverter.node(o);
		   break;
	      case 3:
	           r = o.nodeValue;
	   	       r = r.replace(/\s{2,}/gi," "),
	   	       r = r.replace(/&nbsp;/gi," ");
	           break;
	   }
	   return r;
	
	},

	convertNodes : function(a){
	   var aa = [];
	   for(var i = 0; i < a.length; i++){
	       aa.push(bbConverter.convert(a[i]));
	   }
	   return aa.join("");
	},

	node : function(o){
		var s = "";
		if(typeof o != "object" || o == null || o.nodeType != 1) return s;
		var sStart = "";
		var sEnd = "";
		switch(o.nodeName.toLowerCase()){
			case "a":
				sStart = "[url=" + o.getAttribute("href") + "]";
				sEnd = "[/url]";
				break;
			case "ul":
			case "ol":
			case "li":
			case "blockquote":
			case "h1":
			case "h2":
			case "h3":
			case "h4":
			case "h5":
			case "b":
			case "i":
				var sA = o.getAttribute("align");
				sStart = "[" + o.nodeName.toLowerCase() + (sA ? " align=" + sA : "") + "]";
				sEnd = "[/" + o.nodeName.toLowerCase() + "]";
				break;
			case "img":
				var sA = o.getAttribute("align");
				var sC = o.getAttribute("class");
				return "[img" + (sC ? " class=" + sC :"") + (sA ? " align=" + sA : "") + "]" + o.getAttribute("src") + "[/img]";
				break;
			case "p":
			case "div":
				
				var sC = o.getAttribute("class");
				/// Trash the default html designer class
				///
				if(sC && sC.match(/^p1$/)) sC = 0;
				sStart = "[" + o.nodeName.toLowerCase() + (sC ? " class=" + sC :"") + "]";
				sEnd = "[/" + o.nodeName.toLowerCase() + "]";
				break;
			default:
				sStart = "";
				sEnd = "";
				break;
			
		}
	   
		return (sStart + bbConverter.convertNodes(o.childNodes) + sEnd);
	}
};
