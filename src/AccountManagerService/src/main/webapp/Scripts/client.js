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
				var iX = parseInt(Math.min(document.documentElement.clientWidth,document.documentElement.clientHeight) * .8);
				var sMediaUrl = "/AccountManagerService/thumbnail" + sOrgPath + "/Data" + sPath + "/" + iX + "x" + iX;
				var vProps = {
					media_name : sName,
					media_id : "N/A",
					media_url: sMediaUrl,
					maxWidth : iX,
					maxHeight : iX
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
			},
			packServiceList : function(){
				var aBuff = [];
				for(var i = 0; i < uwmServices.getServices().length;i++){
				var oSvc = uwmServices.getServices()[i];
				aBuff.push("uwmServices.addService(\n");
				aBuff.push("\t\"" + oSvc.name + "\",\n");
				aBuff.push("\t\"" + oSvc.uri + "\",\n");
				aBuff.push("\t" + (oSvc.schema ? JSON.stringify(oSvc.schema) : true) + ",\n");
				aBuff.push("\t" + (oSvc.jsonSchema ? JSON.stringify(oSvc.jsonSchema) : false) + "\n");
				/// Note: Local cache directive needs to be set individually
				aBuff.push("\t,enableRecommendedCache\n");
				aBuff.push(");\n\n");
				}
				return aBuff.join("");
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
	function configureCommunity(fH){
		var f = fH;
		var fc = function(s,v){if(f) f(s,v);};
	   return Hemi.xml.getJSON(sComm + "/configure",fc,(fH ? 1 : 0));	
	}
	function enrollReader(sUid, sCid, sPid, fH){
		var aU = [sComm + "/enroll/reader/" + sUid];
		if(sCid){
			aU.push("/" + sCid);
			if(sPid){
				aU.push("/" + sPid);
			}
		}
		return Hemi.xml.getJSON(aU.join(""),fH,(fH ? 1 : 0));
	}
	function enrollAdmin(sUid, sCid, fH){
		var aU = [sComm + "/enroll/admin/" + sUid];
		if(sCid){
			aU.push("/" + sCid);
		}
		return Hemi.xml.getJSON(aU.join(""),fH,(fH ? 1 : 0));
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
	function addProjectArtifacts(sType, sObjId, fH){
		   return Hemi.xml.getJSON(sComm + "/artifacts/" + sType + "/" + sObjId,fH,(fH ? 1 : 0));
		}
	function addCommunity(sCommunityName, fH){
	   return Hemi.xml.getJSON(sComm + "/new/" + sCommunityName,fH,(fH ? 1 : 0));
	}
	function deleteCommunityProject(sObjId, fH){
		var sType = "PROJECT";
		delete cache[sType];
	   return Hemi.xml.deleteJSON(sComm + "/project/" + sObjId,fH,(fH ? 1 : 0));
	}
	function deleteCommunity(sObjId, fH){
		var sType = "COMMUNITY";
		delete cache[sType];
	   return Hemi.xml.deleteJSON(sComm + "/" + sObjId,fH,(fH ? 1 : 0));
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
	function getCommunityProjectFull(sObjectId,fH){
		var sType = "COMMUNITY.PROJECT";
		var o = getFromCache(sType, "GET", sObjectId);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,"GET",sObjectId,v.json);} if(f) f(s,v);};

	   return Hemi.xml.getJSON(sComm + "/project/" + sObjectId,fc,(fH ? 1 : 0));
	}
	function updateCommunityProject(oObj, sCommName, fH){
		delete cache["COMMUNITY.PROJECT"];
		if(sCommName) delete cache["COMMUNITY." + sCommName + ".PROJECT"];
	   return Hemi.xml.postJSON(sComm + "/project",oObj,fH,(fH ? 1 : 0));
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
	   return Hemi.xml.getJSON(sComm + "/new/" + sCommunityId + "/" + sProjectName,fH,(fH ? 1 : 0));
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
	function findBy(sType, sObjId, oSearch, fH){
		return Hemi.xml.postJSON(sSearch + "/" + sType + "/" + sObjId,oSearch,fH,(fH ? 1 : 0));
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
		if(sPath.match(/^\//) || sPath.match(/\./)){
			// sPath = getDotPath(sPath,"..");
			sPath = "B64-" + uwm.base64Encode(sPath).replace(/=/gi,"%3D");
		}
		
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
	function listSystemRoles(fH){
		var sK = sCurrentOrganization + " SystemRoles";
		var o = getFromCache("ROLE", "GET", sK);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache("ROLE","GET",sK,v.json);} if(f) f(s,v);};
		
		return Hemi.xml.getJSON(sAuthZ + "/ROLE/systemRoles",fc,(fH ? 1 : 0));
	}
	function permitSystem(sType, sObjectId, sActorType, sActorId, bView, bEdit, bDelete, bCreate, fH){
		return Hemi.xml.getJSON(sAuthZ + "/" + sType + "/" + sObjectId + "/permit/" + sActorType + "/" + sActorId + "/" + bView + "/" + bEdit + "/" + bDelete + "/" + bCreate,fH,(fH ? 1 : 0));
	}
	function permit(sType, sObjectId, sActorType, sActorId, sPermId, bEnable, fH){
		return Hemi.xml.getJSON(sAuthZ + "/" + sType + "/" + sObjectId + "/permit/" + sActorType + "/" + sActorId + "/" + sPermId + "/" + bEnable,fH,(fH ? 1 : 0));
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
		addProjectArtifacts : addProjectArtifacts,
		addCommunityProject : addCommunityProject,
		addCommunity : addCommunity,
		deleteCommunity : deleteCommunity,
		deleteCommunityProject : deleteCommunityProject,
		enrollReaderInCommunity : enrollReader,
		enrollAdminInCommunity : enrollAdmin,
		community : getCommunity,
		communityProjectFull : getCommunityProjectFull,
		updateCommunityProject : updateCommunityProject,
		configureCommunity : configureCommunity,
		communityProject : getCommunityProject,
		communityProjectPermissionBase : getCommunityProjectPermissionBase,
		communityProjectRoleBase : getCommunityProjectRoleBase,
		findByTag : findByTag,
		findBy : findBy,
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
		permit : permit,
		permitSystem : permitSystem,
		members : listMembers,
		member : setMember,
		systemRoles : listSystemRoles,
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


	(function(){
		if(!window.uwmServices){
			window.uwmServices = uwmServices = Hemi.json.rpc.service;
			window.uwmServiceCache = uwmServiceCache = Hemi.json.rpc.cache.service;
		}
		
		uwmServices.addService(
				"AMSchema",
				"/AccountManagerService/rest/schema/smd",
				{"serviceType":"JSON-REST","serviceURL":"/AccountManagerService/rest/schema","methods":[{"name":"get","httpMethod":"GET","parameters":[],"returnValue":{"name":"retVal","type":"org.cote.accountmanager.service.rest.SchemaBean"}},{"name":"smd","httpMethod":"GET","parameters":[{"name":"p0","type":"javax.ws.rs.core.UriInfo"}],"returnValue":{"name":"retVal","type":"org.cote.accountmanager.service.rest.SchemaBean"}},{"name":"entity","httpMethod":"GET","parameters":[],"returnValue":{"name":"retVal","type":"org.cote.beans.EntitySchema"}}]},
				{"defaultPackage":"org.cote.objects","authenticationRequest":{"credential":null,"checkCredential":null,"tokens":null,"subject":null,"organizationPath":null,"credentialType":null,"checkCredentialType":null,"subjectType":null},"noteType":{"text":null,"childNotes":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"createdDate":null,"modifiedDate":null},"lifecycleType":{"schedules":null,"budgets":null,"projects":null,"goals":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"description":null},"artifactType":{"previousTransition":null,"nextTransition":null,"referenceObject":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"artifactType":null,"description":null,"createdDate":null,"previousTransitionId":null,"nextTransitionId":null,"artifactDataId":null,"referenceUrn":null},"blueprintType":{"attributes":null,"artifacts":null,"cases":null,"requirements":null,"dependencies":null,"models":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"description":null,"modelType":null},"budgetType":{"time":null,"cost":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"budgetType":null,"description":null},"caseType":{"actors":null,"prerequisites":null,"sequence":null,"diagrams":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"caseType":null,"description":null},"costType":{"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"currencyType":null,"value":null},"estimateType":{"cost":null,"time":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"description":null,"estimateType":null},"expenseType":{"budget":null,"time":null,"cost":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"reason":null,"expenseType":null},"formType":{"elements":null,"description":null,"template":null,"childForms":null,"viewTemplate":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"isTemplate":null,"isGrid":null},"formElementType":{"elementValues":null,"validationRule":null,"elementTemplate":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"elementType":null,"description":null,"elementName":null,"elementLabel":null},"formElementValueType":{"textValue":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"isBinary":null,"formId":null,"formElementId":null,"binaryId":null},"goalType":{"schedule":null,"budget":null,"requirements":null,"dependencies":null,"cases":null,"assigned":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"goalType":null,"description":null,"priority":null},"methodologyType":{"processes":null,"budgets":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"description":null},"modelType":{"artifacts":null,"cases":null,"requirements":null,"dependencies":null,"models":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"description":null,"modelType":null},"moduleType":{"artifacts":null,"work":null,"actualTime":null,"actualCost":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"description":null,"moduleType":null},"processType":{"steps":null,"budgets":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"iterates":null,"description":null},"processStepType":{"goals":null,"budgets":null,"requirements":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"description":null},"projectType":{"blueprints":null,"requirements":null,"dependencies":null,"artifacts":null,"modules":null,"stages":null,"schedule":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"description":null},"requirementType":{"note":null,"form":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"requirementType":null,"description":null,"priority":null,"requirementId":null,"requirementStatus":null},"resourceType":{"resourceData":null,"estimate":null,"schedule":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"resourceType":null,"utilization":null,"resourceDataId":null,"description":null},"scheduleType":{"goals":null,"budgets":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"startTime":null,"endTime":null},"locationType":{"boundaries":null,"borders":null,"description":null,"childLocations":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"geographyType":null,"classification":null},"eventType":{"location":null,"entryTraits":null,"exitTraits":null,"things":null,"actors":null,"observers":null,"influencers":null,"orchestrators":null,"groups":null,"childEvents":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"eventType":null,"description":null,"startDate":null,"endDate":null},"traitType":{"description":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"traitType":null,"score":null,"alignmentType":null},"stageType":{"methodology":null,"work":null,"budget":null,"schedule":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"description":null},"taskType":{"requirements":null,"artifacts":null,"work":null,"notes":null,"estimate":null,"actualTime":null,"actualCost":null,"resources":null,"dependencies":null,"childTasks":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"taskStatus":null,"description":null,"createdDate":null,"modifiedDate":null,"completedDate":null,"dueDate":null,"startDate":null},"ticketType":{"assignedResource":null,"requiredResources":null,"estimate":null,"actualTime":null,"actualCost":null,"tickets":null,"dependencies":null,"artifacts":null,"notes":null,"forms":null,"audit":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"createdDate":null,"modifiedDate":null,"dueDate":null,"closedDate":null,"reopenedDate":null,"description":null,"ticketStatus":null,"priority":null,"severity":null},"timeType":{"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"basisType":null,"value":null},"validationRuleType":{"errorMessage":null,"replacementValue":null,"description":null,"rules":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"expression":null,"isRuleSet":null,"isReplacementRule":null,"validationType":null,"comparison":null,"allowNull":null},"workType":{"resources":null,"tasks":null,"artifacts":null,"dependencies":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"description":null},"applicationRequestType":{"imports":null,"sort":null,"startRecord":null,"recordCount":null,"paginate":null,"populateGroup":null,"organizationId":null,"fullRecord":null,"lifecycleId":null,"projectId":null,"applicationId":null},"identityDataImportType":{"header":null,"name":null,"type":null},"baseRoleType":{"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"roleType":null,"referenceId":null,"parentPath":null},"dataTypeSchema":{"securityType":null,"dataBytesStore":null,"cipherKey":null,"passKey":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"detailsOnly":null,"publicId":null,"description":null,"dimensions":null,"mimeType":null,"size":null,"createdDate":null,"modifiedDate":null,"expiryDate":null,"volatile":null,"blob":null,"compressed":null,"shortData":null,"passwordProtected":null,"passwordProtect":null,"enciphered":null,"encipher":null,"vaulted":null,"vaultId":null,"keyId":null,"readDataBytes":null,"wasDataBlob":null,"rating":null,"pointer":null,"dataHash":null,"compressionType":null},"cryptoBeanSchema":{"attributes":null,"publicKeyBytes":null,"privateKeyBytes":null,"cipherIV":null,"cipherKey":null,"encryptedCipherIV":null,"encryptedCipherKey":null,"javaClass":"org.cote.beans.CryptoBean","spoolId":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"cipherProvider":null,"symmetricCipherKeySpec":null,"asymmetricCipherKeySpec":null,"randomSeedLength":null,"encryptCipherKey":null,"reverseEncrypt":null,"hashProvider":null,"cipherKeySpec":null,"cipherKeySize":null,"keySize":null,"globalKey":null,"primaryKey":null,"organizationKey":null,"symmetricKeyId":null,"asymmetricKeyId":null,"previousKeyId":null},"userType":{"contactInformation":null,"session":null,"homeDirectory":null,"statistics":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"sessionStatus":null,"userStatus":null,"userType":null,"databaseRecord":null,"userId":null,"accountId":null},"directoryGroupType":{"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupType":null,"referenceId":null,"path":null},"baseGroupType":{"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupType":null,"referenceId":null,"path":null},"userGroupType":{"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupType":null,"referenceId":null,"path":null},"contactInformationType":{"contacts":null,"addresses":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"contactInformationType":null,"description":null,"referenceId":null},"personType":{"description":null,"firstName":null,"middleName":null,"lastName":null,"title":null,"suffix":null,"birthDate":null,"gender":null,"alias":null,"prefix":null,"users":null,"accounts":null,"contactInformation":null,"partners":null,"dependents":null,"notes":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null},"contactType":{"contactValue":null,"description":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"contactType":null,"locationType":null,"preferred":null},"addressType":{"addressLine1":null,"addressLine2":null,"city":null,"region":null,"state":null,"postalCode":null,"country":null,"description":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"locationType":null,"preferred":null},"organizationType":{"organizationParent":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"logicalId":null,"referenceId":null,"organizationType":null},"attributeType":{"values":[],"name":null,"dataType":null,"index":null,"referenceType":null,"referenceId":null,"organizationId":null},"credentialType":{"salt":null,"credential":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"referenceType":null,"referenceId":null,"createdDate":null,"modifiedDate":null,"expiryDate":null,"primary":null,"previousCredentialId":null,"nextCredentialId":null,"keyId":null,"vaultId":null,"enciphered":null,"vaulted":null,"credentialType":null,"hashProvider":null},"authenticationRequestType":{"credential":null,"checkCredential":null,"tokens":null,"subject":null,"organizationPath":null,"credentialType":null,"checkCredentialType":null,"subjectType":null},"authenticationResponseType":{"message":null,"user":null,"sessionId":null,"response":null,"organizationPath":null,"organizationId":null},"policyDefinitionType":{"parameters":null,"organizationPath":null,"urn":null,"expiresDate":null,"decisionAge":null,"enabled":null,"modifiedDate":null,"createdDate":null},"policyRequestType":{"facts":null,"contextUser":null,"urn":null,"subject":null,"credential":null,"requestType":null,"asyncRequest":null,"organizationPath":null,"subjectType":null},"policyResponseType":{"message":null,"patternChain":null,"response":null,"urn":null,"expiresDate":null,"score":null},"factType":{"factReference":null,"factData":null,"attributes":null,"description":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"score":null,"sourceUrn":null,"sourceDataType":null,"sourceUrl":null,"factType":null,"factoryType":null,"parameter":null,"sourceType":null},"functionType":{"facts":null,"functionData":null,"attributes":null,"description":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"score":null,"functionType":null,"sourceUrl":null,"sourceUrn":null},"functionFactType":{"attributes":null,"description":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"score":null,"functionUrn":null,"factUrn":null},"patternType":{"fact":null,"match":null,"operation":null,"attributes":null,"description":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"score":null,"factUrn":null,"comparator":null,"patternType":null,"matchUrn":null,"operationUrn":null},"policyType":{"rules":null,"attributes":null,"description":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"score":null,"enabled":null,"expiresDate":null,"decisionAge":null,"modifiedDate":null,"createdDate":null,"condition":null},"operationType":{"operation":null,"attributes":null,"description":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"score":null,"operationType":null},"ruleType":{"rules":null,"patterns":null,"attributes":null,"description":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":null,"score":null,"ruleType":null,"condition":null},"basePermissionType":{"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"permissionType":null,"referenceId":null,"parentPath":null},"authorizationPolicyType":{"roles":null,"permissions":null,"members":null,"contextType":null,"factoryType":null,"contextId":null,"contextName":null,"memberType":null,"systemAdministrator":null,"accountAdministrator":null,"dataAdministrator":null,"contextUrn":null,"roleReader":null,"accountReader":null,"groupReader":null,"authenticated":null,"authenticationId":null},"accountType":{"contactInformation":null,"statistics":null,"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"accountType":null,"accountStatus":null,"referenceId":null,"accountId":null,"databaseRecord":null},"baseTagType":{"attributes":null,"nameType":null,"parentId":null,"name":null,"id":null,"ownerId":null,"populated":null,"objectId":null,"attributesPopulated":null,"urn":null,"organizationId":null,"organizationPath":null,"groupId":null,"groupPath":null,"tagType":null},"dataTagSearchRequest":{"tags":null,"sort":null,"startRecord":null,"recordCount":null,"paginate":null,"populateGroup":null,"organizationId":null,"fullRecord":null},"baseSearchRequestType":{"sort":null,"startRecord":null,"recordCount":null,"paginate":null,"populateGroup":null,"organizationId":null,"fullRecord":null},"sortQueryType":{"sortOrder":null,"sortField":null}}
				,true
			);
	}());
	
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

