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
			},
			profile : function(){
				var oType = uwm.getUser();

				/// A person object will exist for users created via registration or the console
				///
				var oP = AM6Client.userPerson(oType.objectId);
				var sType = "User";
				if(oP){
					sType = 'Person';
					oType = oP;
				}
				var sViewType = "Profile";
				var oProps = {listType:sType,viewType:oType};
				var oW = Hemi.app.createWindow(oType.name, uwm.getApiTypeView(sType) + "/Forms/" + sViewType + ".xml", "View-" + sType + "-" + oType.id , 0, 0, oProps, 0);
	            if (oW) {
	            	oW.resizeTo(475, 400);
	            	Hemi.app.getWindowManager().CenterWindow(oW);
	            	// Destroy the window when closed
	            	//
	            	oW.setHideOnClose(0);
	            }
			},
			openPopInImage : function(sUrl){
				var i1, i2, i3;
				if((i1 = sUrl.indexOf("/media/") )== -1 || (i2 = sUrl.indexOf("/Data/")) == -1){
					Hemi.logError("Invalid URL: " + sUrl);
					return;
				}
				var sOrgPath = sUrl.substring(i1 + 6,i2);
				var oOrg = AM6Client.find("ORGANIZATION","UNKNOWN",sOrgPath);
				var sPath = sUrl.substring(i2 + 5,sUrl.length);
				var sName = sPath.substring((i3 = sPath.lastIndexOf("/")) + 1, sPath.length);
				var iW = parseInt(.8 * document.documentElement.clientWidth);
				var iH = parseInt(.8 * document.documentElement.clientHeight);
				Hemi.log("Pop image " + iW + " x " + iH);
				iW = Math.floor(iW/250) * 250;
				if(iW <= 0) iW = 250;
				iH = Math.floor(iH/250) * 250;
				if(iH <= 0) iH = 250;
				var sMediaUrl = "/AccountManagerService/thumbnail" + sOrgPath + "/Data" + sPath + "/" + iW + "x" + iH;
				var vProps = {
					media_name : sName,
					media_id : "N/A",
					media_url: sMediaUrl
				};
				Hemi.app.createWindow(sName,"/AccountManagerService/Forms/ImageViewer.xml",sUrl,0,0,vProps,function(oW){
					oW.setIsModal(1);
					oW.setCanMinimize(0);
					oW.setCanMaximize(0);
					oW.center();
				});
			},
			
			decorateLinkToPopIn : function(oParent){
				var aL = oParent.getElementsByTagName("a");
				for(var i = 0; i < aL.length;i++){
					if(!aL[i].href || !aL[i].href.match(/\/AccountManagerService\/media/)) continue;
					aL[i].onclick = function(){
						uwm.openPopInImage(this.href);
						return false;
					}
				}
			},
			
			handlers : {
				load : [],
				unload : []
			},
			addPageLoadHandler : function(f){
				uwm.handlers.load.push(f);
			},
			processLoadHandlers : function(){
				var aH = uwm.handlers.load, i = 0;
				for(; i < aH.length;) aH[i++]();
				aH.length = 0;
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
	var sAuthZ = sBase + "/authorization";
	var sComm = sBase + "/community";
	var sCred = sBase + "/credential";
	var sPol = sBase + "/policy";
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
	function newPrimaryCredential(sType, sObjId, oAuthN, fH){
		return Hemi.xml.postJSON(sCred + "/" + sType + "/" + sObjId,oAuthN,fH,(fH ? 1 : 0));
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
	function evaluate(oObj, fH){
		   return Hemi.xml.postJSON(sPol + "/evaluate",oObj,fH,(fH ? 1 : 0));
		}
	function define(sObjectId, fH){
	   return Hemi.xml.getJSON(sPol + "/define/" + sObjectId,fH,(fH ? 1 : 0));
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
	function configureCommunityProjectGroupEntitlements(sLid, sPid, sGid,fH){
		var f = fH;
		var fc = function(s,v){if(f) f(s,v);};

	   return Hemi.xml.getJSON(sComm + "/configure/" + sLid + "/" + sPid + "/" + sGid,fc,(fH ? 1 : 0));	
	}
	function isCommunityConfigured(fH){
		var f = fH;
		var fc = function(s,v){if(f) f(s,v);};
	   return Hemi.xml.getJSON(sComm + "/isconfigured",fc,(fH ? 1 : 0));	
	}
	function getCommunityProjectRoleBase(oP, fH){
		var sType = "ROLE";
		var o = getFromCache(sType, "GET", oP.objectId);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,"GET",oP.objectId,v.json);} if(f) f(s,v);};

	   return Hemi.xml.getJSON(sComm + "/role/base/" + oP.objectId,fc,(fH ? 1 : 0));
	}
	function getCommunityProjectPermissionBase(oP, fH){
		var sType = "PERMISSION";
		var o = getFromCache(sType, "GET", oP.objectId);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,"GET",oP.objectId,v.json);} if(f) f(s,v);};

	   return Hemi.xml.getJSON(sComm + "/permission/base/" + oP.objectId,fc,(fH ? 1 : 0));
	}
	function addCommunity(sCommunityName, fH){
		var sType = "COMMUNITY";
		var o = getFromCache(sType, "GET", sCommunityName);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,"GET",sCommunityName,v.json);} if(f) f(s,v);};

	   return Hemi.xml.getJSON(sComm + "/new/" + sCommunityName,fc,(fH ? 1 : 0));
	}
	function getCommunity(sCommunityName, fH){
		var sType = "COMMUNITY";
		var o = getFromCache(sType, "GET", sCommunityName);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,"GET",sCommunityName,v.json);} if(f) f(s,v);};

	   return Hemi.xml.getJSON(sComm + "/find/" + sCommunityName,fc,(fH ? 1 : 0));
	}
	function getCommunityProject(sCommunityName, sProjectName,fH){
		var sType = "COMMUNITY." + sCommunityName + ".PROJECT";
		var o = getFromCache(sType, "GET", sProjectName);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,"GET",sProjectName,v.json);} if(f) f(s,v);};

	   return Hemi.xml.getJSON(sComm + "/find/" + sCommunityName + "/" + sProjectName,fc,(fH ? 1 : 0));
	}
	function addCommunityProject(sCommunityId, sProjectName,fH){
		var sType = "COMMUNITY." + sCommunityId + ".PROJECT";
		var o = getFromCache(sType, "GET", sProjectName);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,"GET",sProjectName,v.json);} if(f) f(s,v);};

	   return Hemi.xml.getJSON(sComm + "/new/" + sCommunityId + "/" + sProjectName,fc,(fH ? 1 : 0));
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
	function getByName(sType,sObjectId,sName,fH, bParent){
		var sKey = sObjectId + "-" + sName;
		var o = getFromCache(sType, "GET", sKey);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,"GET",sKey,v.json);} if(f) f(s,v);};
		
		   return Hemi.xml.getJSON(sResource + "/" + sType + "/" + (bParent ? "parent/" : "") + sObjectId + "/" + sName,fc,(fH ? 1 : 0));
		}
	function getByNameInGroupParent(sType,sObjectId,sName,fH){
		return getByName(sType, sObjectId, sName, fH, 1);
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
	function findTags(sType, sObjId, fH){
		return Hemi.xml.getJSON(sSearch + "/" + sType + "/tags/" + sObjId,fH,(fH ? 1 : 0));
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
		/// Band-aid - need to better encode these
		///
		if(sPath.match(/^\//) || sPath.match(/\./)) sPath = getDotPath(sPath,"..");
		
		var o = getFromCache(sType, sK, sPath);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,sK,sPath,v.json);} if(f) f(s,v);};

		return Hemi.xml.getJSON((bMake ? sMake : sSearch) + "/" + sType + "/" + sObjType + "/" + sPath,fc,(fH ? 1 : 0));
	}
	
	function getDotPath(path, sAlt){
		return path.replace(/^\//,"").replace(/\//gi,(sAlt ? sAlt : "."));
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
	function getUserPersonObject(sId,fH){
		if(!sId){
			var o = getPrincipal();
			if(o) sId = o.objectId;
		}
		return Hemi.xml.getJSON(sPrincipal + "/person" + (sId ? "/" + sId : ""),fH,(fH ? 1 : 0)); 
	}
	function getUserObject(sType,sOType,fH){
		if(!sType && !sOType) return getPrincipal(fH);
		return Hemi.xml.getJSON(sAuthZ + "/" + sType + "/user/" + sOType,fH,(fH ? 1 : 0));
	}
	function listEntitlementsForType(sType, sObjId, fH){
		if(!sObjId) sObjId = null;
		 return Hemi.xml.getJSON(sAuthZ + "/" + sType + "/roles/" + sObjId,fH,(fH ? 1 : 0));
	}
	function mediaDataPath(oObj, bThumb){
		return "/AccountManagerService/" + (bThumb ? "thumbnail" : "media") + "/" + AM6Client.dotPath(o.organizationPath) + "/Data" + o.groupPath + "/" + o.name + (bThumb ? "/100x100" : "");
	}
	window.AM6Client = {
		dotPath : getDotPath,
		define : define,
		evaluate : evaluate,
		find : find,
		addCommunityProject : addCommunityProject,
		addCommunity : addCommunity,
		community : getCommunity,
		communityProject : getCommunityProject,
		communityProjectPermissionBase : getCommunityProjectPermissionBase,
		communityProjectRoleBase : getCommunityProjectRoleBase,
		findByTag : findByTag,
		countByTag : countByTag,
		findTags : findTags,
		make : make,
		mediaDataPath : mediaDataPath,
		list : list,
		count: count,
		get : get,
		getByName : getByName,
		getByNameInGroupParent : getByNameInGroupParent,
		update : update,
		delete : deleteObject,
		cache : getCache,
		principal : getPrincipal,
		anonymous : getDocumentControl,
		entitlements : listEntitlementsForType,
		isCommunityConfigured : isCommunityConfigured,
		configureCommunityProjectGroupEntitlements : configureCommunityProjectGroupEntitlements,
		members : listMembers,
		member : setMember,
		user: getUserObject,
		userPerson : getUserPersonObject,
		currentOrganization : sCurrentOrganization,
		newPrimaryCredential : newPrimaryCredential,
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
	
//	window.addEventListener("load",clientLoad,false);
//	function clientLoad(){

	if(!window.uwmServices){
		window.uwmServices = uwmServices = Hemi.json.rpc.service;
		window.uwmServiceCache = uwmServiceCache = Hemi.json.rpc.cache.service;
	}
	
	uwmServices.addService(
		"AMSchema",
		"/AccountManagerService/rest/schema/smd",
		true,
		true,
		false
	);
	
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
				uwm.processLoadHandlers();
			});
		});
//	}
	
}());

