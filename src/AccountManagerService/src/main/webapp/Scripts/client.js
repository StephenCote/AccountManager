(function(){
	
	if(!window.uwm){
		window.uwm = {
				
			debugMode : 1,
			operation : function(){
				
			},
			/// This is a static implementation while refactoring
			///
			rule : function(sRule){
				if(sRule == "IsLoggedIn"){
					return (getPrincipal() != null ? 1 : 0);
				}
			},
			getUser : function(){
				return getPrincipal();
			},
			logout : function(){
				if(doLogout) doLogout();
			},
			getApi : function(sType){
				return AM6Client;
			},
			getApiTypeView : function(sType){
				return "/AccountManagerService";
			},
			getPathForType : function(sType, sDef){
				if(uwm.pathProvider) return uwm.pathProvider(sType);
				return sDef;
			},
			getDefaultParentForType : function(sType, vDef){
				if(uwm.defaultParentProvider) return uwm.defaultParentProvider(sType,vDef);
				return vDef;
			},
			base64Encode : function(a){
				return Base64.encode(a);
			},
			base64Decode : function(a){
				return Base64.decode(a);
			},
			strToBin : function(s){
				var a = [];
				for(var i = 0; i < s.length;) a.push(s.charCodeAt(i++));
				return a;
			},
			binToStr : function(a){
				var s = [];
				if(!a) return s;
				for(var i = 0; i < a.length;) s.push(String.fromCharCode(a[i++]));
				return s.join("");
			},
			guid : function(){
				return Hemi.guid();
			},
			/// sKey and sIv in Base64 string format
			///
			encipher : function(sText, sKey, sIv){
			 var enc = slowAES.encrypt(uwm.strToBin(sText),
	        		slowAES.modeOfOperation.CBC,
	        		slowAES.padding.PKCS7,
	                uwm.strToBin(uwm.base64Decode(sKey)),
	                uwm.strToBin(uwm.base64Decode(sIv))
	          );
	          return enc.cipher;

			},
			decipher : function(sText, sKey, sIv){
	        
	        	return slowAES.decrypt(uwm.strToBin(uwm.base64Decode(sText)),
	        			slowAES.modeOfOperation.CBC,
	        			slowAES.padding.PKCS7,
	        			uwm.strToBin(uwm.base64Decode(sKey)),
	        			uwm.strToBin(uwm.base64Decode(sIv))
	        	);
			}
			
		};
	}
	var cache = {};
	var principal = 0;
	var sCurrentOrganization = 0;
	var sBase = "/AccountManagerService/rest";
	var sCache = sBase + "/cache";
	var sResource = sBase + "/resource";
	var sPrincipal = sBase + "/principal";
	var sSearch = sBase + "/search";
	var sMake = sBase + "/make";
	var sList = sBase + "/list";
	var sAuthZ = sBase + "/authorization"
	function getCache(){
		return cache;
	}
	function clearCache(vType,fH){
		var sType = vType, oObj;
		if(typeof vType == "object" && vType != null){
			sType = vType.nameType;
			oObj = vType;
			return removeFromCache(sType, vType.objectId);
		}
		if(!sType){
			cache = {};
			return Hemi.xml.getJSON(sCache + "/clearAll",fH,(fH ? 1 : 0));
		}
		else{
			delete cache[sType];
			return Hemi.xml.getJSON(sCache + "/clear/" + sType,fH,(fH ? 1 : 0));
		}
	}
	function removeFromCache(vType, sObjId){
		var sType = vType;
		if(typeof vType == "object" && vType != null){
			sType = vType.nameType;
			if(!sObjId) sObjId = vType.objectId;
		}
		if(!cache[sType]) return;
		for(var s in cache[sType]){
			delete cache[sType][s][sObjId];
		}
	}
	function getFromCache(sType, sAct, sObjId){
		if(!cache[sType]) return 0;
		if(!cache[sType][sAct]) return 0;
		if(typeof cache[sType][sAct][sObjId]=="undefined") return 0;
		return cache[sType][sAct][sObjId];
	}
	function addToCache(sType, sAct, sId, vObj){
		if(!cache[sType]) cache[sType] = {};
		if(!cache[sType][sAct]) cache[sType][sAct] = {};
		cache[sType][sAct][sId]=vObj;
		
	}
	function deleteObject(sType,sObjId, fH){
		delete cache[sType];
		   return Hemi.xml.deleteJSON(sResource + "/" + sType + "/" + sObjId,fH,(fH ? 1 : 0));
		}
	function update(sType,oObj, fH){
		//removeFromCache(sType, oObj.objectId);
		delete cache[sType];
	   return Hemi.xml.postJSON(sResource + "/" + sType + "/",oObj,fH,(fH ? 1 : 0));
	}
	function getDocumentControl(fH){
		var o = getFromCache("USER", "GET", "_documentcontrol_");
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache("USER","GET","_documentcontrol_",v.json);} if(f) f(s,v);};
	   return Hemi.xml.getJSON(sPrincipal + "/anonymous/",fc,(fH ? 1 : 0));
	}
	function getPrincipal(fH){
		var o = getFromCache("USER", "GET", "_principal_");
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache("USER","GET","_principal_",v.json);} if(f) f(s,v);};
	   return Hemi.xml.getJSON(sPrincipal + "/",fc,(fH ? 1 : 0));
	}
	function get(sType,sObjectId,fH){
		var o = getFromCache(sType, "GET", sObjectId);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,"GET",sObjectId,v.json);} if(f) f(s,v);};

	   return Hemi.xml.getJSON(sResource + "/" + sType + "/" + sObjectId,fc,(fH ? 1 : 0));
	}
	function getByName(sType,sObjectId,sName,fH){
		   return Hemi.xml.getJSON(sResource + "/" + sType + "/" + sObjectId + "/" + sName,fH,(fH ? 1 : 0));
		}
	function count(sType,sObjectId,fH){
		var o = getFromCache(sType, "COUNT", sObjectId);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,"COUNT",sObjectId,v.json);} if(f) f(s,v);};

	   return Hemi.xml.getJSON(sList + "/" + sType + "/" + sObjectId + "/count",fc,(fH ? 1 : 0));
	}
	function list(sType,sObjectId,iStart,iLength,fH){
		
		var sK = "LIST-" + iStart + "-" + iLength;
		var o = getFromCache(sType, sK, sObjectId);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,sK,sObjectId,v.json);} if(f) f(s,v);};
		return Hemi.xml.getJSON(sList + "/" + sType + "/" + sObjectId + "/" + iStart + "/" + iLength,fc,(fH ? 1 : 0));
	}
	function findByTag(sType, oSearch, fH){
		return Hemi.xml.postJSON(sSearch + "/" + sType + "/tags",oSearch,fH,(fH ? 1 : 0));
	}
	function countByTag(sType, oSearch, fH){
		return Hemi.xml.postJSON(sSearch + "/" + sType + "/tags/count",oSearch,fH,(fH ? 1 : 0));
	}
	function find(sType,sObjType,sPath,fH){
		return makeFind(sType,sObjType,sPath,0,fH);
	}
	function make(sType,sObjType,sPath,fH){
		return makeFind(sType,sObjType,sPath,1,fH);
	}

	function makeFind(sType,sObjType,sPath,bMake,fH){
		var sK = "FIND-" + sObjType;
		if(sPath.match(/^\\/)) sPath = getDotPath(sPath);
		var o = getFromCache(sType, sK, sPath);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,sK,sPath,v.json);} if(f) f(s,v);};

		return Hemi.xml.getJSON((bMake ? sMake : sSearch) + "/" + sType + "/" + sObjType + "/" + sPath,fc,(fH ? 1 : 0));
	}
	
	function getDotPath(path){
		return path.replace(/^\//,"").replace(/\//,".");

	}
	
	function logout(fH){
		AM6Client.currentOrganization = sCurrentOrganization = 0;
		Hemi.xml.getJSON("/AccountManagerService/rest/logout",fH,(fH ? 1 : 0));
	}

	function login(cred, fH){
		AM6Client.currentOrganization = sCurrentOrganization = cred.organizationPath;

		Hemi.xml.postJSON("/AccountManagerService/rest/login",cred,fH,(fH ? 1 : 0));
	}
	
	function listMembers(sType, sObjectId, sActorType, fH){
		return Hemi.xml.getJSON(sAuthZ + "/" + sType + "/" + sObjectId + "/" + sActorType,fH,(fH ? 1 : 0));
	}
	function setMember(sType, sObjectId, sActorType, sActorId, bSet, fH){
		return Hemi.xml.getJSON(sAuthZ + "/" + sType + "/" + sObjectId + "/member/" + sActorType + "/" + sActorId + "/" + bSet,fH,(fH ? 1 : 0));
	}
	function getUserObject(sType,sOType,fH){
		if(!sType && !sOType) return getPrincipal(fH);
		return Hemi.xml.getJSON(sAuthZ + "/" + sType + "/user/" + sOType,fH,(fH ? 1 : 0));
	}
	function listEntitlementsForType(sType, sObjId, fH){
		if(!sObjId) sObjId = null;
		 return Hemi.xml.getJSON(sAuthZ + "/" + sType + "/roles/" + sObjId,fH,(fH ? 1 : 0));
	}
	window.AM6Client = {
		dotPath : getDotPath,
		find : find,
		findByTag : findByTag,
		countByTag : countByTag,
		make : make,
		list : list,
		count: count,
		get : get,
		getByName : getByName,
		update : update,
		delete : deleteObject,
		cache : getCache,
		principal : getPrincipal,
		anonymous : getDocumentControl,
		entitlements : listEntitlementsForType,
		members : listMembers,
		member : setMember,
		user: getUserObject,
		currentOrganization : sCurrentOrganization,
		clearCache : clearCache,
		clearAuthorizationCache : function(fH){
			return Hemi.xml.getJSON(sCache + "/clearAuthorization",fH,(fH ? 1 : 0));
		},
		getAttribute : function(o,n){
			var v = 0;
			for(var i = 0; o.attributes && i < o.attributes.length;i++){
				if(o.attributes[i].name == n){
					v = o.attributes[i];
				}
			}
			return v;
		},
		getAttributeValue : function(o,n,d){
			var a = AM6Client.getAttribute(o,n);
			if(!a) return d;
			return a.values[0];
		},
		addAttribute : function(o,s,v){
			if(!o.attributes) o.attributes = [];
			o.attributes.push(AM6Client.newAttribute(s,v));
			return 1;
		},
		newAttribute : function(s,v){
			var a = new org.cote.objects.attributeType(),x=[];
			a.dataType = "VARCHAR";
			a.name = s;
			
			if(typeof v == "string") x.push(v);
			else if(typeof v == "object" && v instanceof Array) x = v;
			a.values = x;
			return a;
		},
		removeAttribute : function(o, n){
			if(!o.attributes || o.attributes == null) return 0;
			for(var i = 0; i < o.attributes.length; i++){
				if(o.attributes[i].name == n){
					o.attributes.splice(i,1);
					break;
				}
			}
		},
		login : login,
		logout : logout
	};
	
	window.addEventListener("load",clientLoad,false);
	function clientLoad(){
		Hemi.message.service.subscribe("onspaceconfigload", function (s, v){
			if(!v.is_primary) return;
			var oSpace = Hemi.app.space.service.getPrimarySpace();
	
			getPrincipal(function(s,v){
				principal = 0;
				AM6Client.currentOrganization = sCurrentOrganization = 0;
				if(v && v != null && v.json != null){
					principal = v.json;
					AM6Client.currentOrganization = sCurrentOrganization = principal.organizationPath;
				}
			});
			
		});
	}
	
}());

