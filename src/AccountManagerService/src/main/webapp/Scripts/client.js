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
			createContent : function(i, u, f){
				var o = document.getElementById(i);
				var ai = Hemi.GetSpecifiedAttribute(o, "acrid");
				var c = 0;
				if(ai){
					c = Hemi.registry.service.getObject(ai);
				}
				else{
					c = Hemi.app.createApplicationComponent(0, o, Hemi.app.space.service.getPrimarySpace(), i);
					c.setTemplateIsSpace(1);
					c.setAsync(1);
				}
				
				c.local_template_init = f;
				c.loadTemplate(u);
				
				return c;
			},


			getRule : function(sName){
				return Hemi.app.module.service.getModuleByName(sName.toLowerCase());
			},
			
			rule : function(sName, vParams, sSuccessOp, sFailOp, oNode, fStatus, fSuite){
				var oTest = Hemi.app.module.test.service.NewTest(sName.toLowerCase(), oNode, fStatus, fSuite, g_application_path + "Rules/");
				var bOut = 0;
				Hemi.log("START RULE: " + sName);
				if(!vParams && uwm.altPane) vParams = {opener:uwm.altPane.opener};
				if(vParams){
					for(var i in vParams){
						oTest.getProperties()[i]=vParams[i];
					}
				}
				oTest.RunTests();
				var oTestResult = oTest.getTestByName("Test" + sName);
				if(oTestResult && oTestResult.data == true){
					/// Only set return val to op if all operations are updated to expect a return value
					/// There was a few hours well spent
					bOut = 1;
					if(sSuccessOp){
						window.uwm.operation(sSuccessOp, vParams, oNode, sName);
					}
				}
				else if(sFailOp){
					window.uwm.operation(sFailOp, vParams, oNode, sName);
				}
				oTest.destroy();
				Hemi.log("EXIT RULE: " + sName + " (" + (bOut ? true : false) + ")");
				return bOut;
			},
			operation : function(sName, vParams, oNode, sRule){
				var oMod = Hemi.app.module.service.NewModule(sName.toLowerCase(), oNode, g_application_path + "Operations/");
				if(!vParams && uwm.altPane) vParams = {opener:uwm.altPane.opener};
				if(vParams){
					for(var i in vParams){
						oMod.getProperties()[i]=vParams[i];
					}
				}
				if(oMod == null) return 0;
				if(oMod.SetRule) oMod.SetRule(sRule);
				if(oMod.DoOperation) return oMod.DoOperation();
				else return 1;

			},

			
			getUser : function(){
				return getPrincipal();
			},
			
			login : function(o, u, p, v){
				var vParms = (v ? v : {});
				AM6Client.loginWithPassword(o, u, p, function(s, v){
					var oU;
					if(v && v.json){
						AM6Client.clearCache(0,1);
						oU = window.uwm.getUser();
						/// window.uwm.session =window.uwm.user.session;
						if(uwm.altFlushSession) uwm.altFlushSession();
					}
					Hemi.message.service.publish("onsessionrefresh", oU);
					vParms.user = oU;
					uwm.operation("ContinueWorkflow", vParms, 0, "Authenticate");
					
				});

				return 1;
				
			},
			
			logout : function(){
				var b = AM6Client.logout();
				Hemi.message.service.publish("onsessionrefresh", null);
				if(doLogout) doLogout();
				return b;
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
	var sToken = sBase + "/token";
	function getCache(){
		return cache;
	}
	function clearCache(vType,bLocalOnly, fH){
		var sType = vType, oObj;
		if(typeof vType == "object" && vType != null){
			sType = vType.nameType;
			oObj = vType;
			return removeFromCache(sType, vType.objectId);
		}
		if(!sType){
			cache = {};
			return (bLocalOnly ? 1 : Hemi.xml.getJSON(sCache + "/clearAll",fH,(fH ? 1 : 0)));
		}
		else{
			delete cache[sType];
			return (bLocalOnly ? 1 : Hemi.xml.getJSON(sCache + "/clear/" + sType,fH,(fH ? 1 : 0)));
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
	function getApplicationProfile(fH){
		var o = getFromCache("APPLICATION", "GET", "_principal_");
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache("APPLICATION","GET","_principal_",v.json);} if(f) f(s,v);};
	   return Hemi.xml.getJSON(sPrincipal + "/application",fc,(fH ? 1 : 0));
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
	
	function validateMaterialToken(sType,sT, fH){
	   return Hemi.xml.getJSON(sToken + "/material/validate/" + sType + "/" + sT,fH,(fH ? 1 : 0));
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
		AM6Client.clearCache(0,1);
		return Hemi.xml.getJSON("/AccountManagerService/rest/logout",fH,(fH ? 1 : 0));
	}
	function loginWithPassword(sOrg, sName, sCred, fH){
		var cred = new org.cote.objects.credentialType();
		cred.name = sName;
		cred.credential = Base64.encode(sCred);
		cred.organizationPath = sOrg;
		cred.credentialType = "HASHED_PASSWORD";
		return AM6Client.login(cred,fH);
	}
	function login(cred, fH){
		AM6Client.currentOrganization = sCurrentOrganization = cred.organizationPath;
		return Hemi.xml.postJSON("/AccountManagerService/rest/login",cred,fH,(fH ? 1 : 0));
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
		application : getApplicationProfile,
		userPerson : getUserPersonObject,
		currentOrganization : sCurrentOrganization,
		newPrimaryCredential : newPrimaryCredential,
		validateMaterialToken : validateMaterialToken,
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
		loginWithPassword : loginWithPassword,
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
				{"serviceType":"JSON-REST","serviceURL":"/AccountManagerService/rest/schema","methods":[{"name":"get","httpMethod":"GET","parameters":[],"returnValue":{"name":"retVal","type":"org.cote.accountmanager.service.rest.SchemaBean"}},{"name":"entity","httpMethod":"GET","parameters":[],"returnValue":{"name":"retVal","type":"org.cote.beans.EntitySchema"}},{"name":"smd","httpMethod":"GET","parameters":[{"name":"p0","type":"javax.ws.rs.core.UriInfo"}],"returnValue":{"name":"retVal","type":"org.cote.accountmanager.service.rest.SchemaBean"}}]},
				{"defaultPackage":"org.cote.objects","authenticationRequest":{"credential":null,"checkCredential":null,"tokens":[],"subject":null,"organizationPath":null,"credentialType":"UNKNOWN","checkCredentialType":null,"subjectType":"UNKNOWN"},"noteType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"text":null,"childNotes":[],"createdDate":null,"modifiedDate":null},"lifecycleType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"schedules":[],"budgets":[],"projects":[],"goals":[],"description":null},"artifactType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"previousTransition":null,"nextTransition":null,"referenceObject":null,"artifactType":"UNKNOWN","description":null,"createdDate":null,"previousTransitionId":0,"nextTransitionId":0,"artifactDataId":0,"referenceUrn":null},"blueprintType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"artifacts":[],"cases":[],"requirements":[],"dependencies":[],"models":[],"description":null,"modelType":null},"budgetType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"time":null,"cost":null,"budgetType":null,"description":null},"caseType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"actors":[],"prerequisites":[],"sequence":[],"diagrams":[],"caseType":null,"description":null},"costType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"currencyType":null,"value":null},"estimateType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"cost":null,"time":null,"description":null,"estimateType":null},"expenseType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"budget":null,"time":null,"cost":null,"reason":null,"expenseType":null},"formType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"elements":[],"description":null,"template":null,"childForms":[],"viewTemplate":null,"isTemplate":false,"isGrid":false},"formElementType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"elementValues":[],"validationRule":null,"elementTemplate":null,"elementType":null,"description":null,"elementName":null,"elementLabel":null},"formElementValueType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"textValue":null,"isBinary":false,"formId":0,"formElementId":0,"binaryId":0},"goalType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"schedule":null,"budget":null,"requirements":[],"dependencies":[],"cases":[],"assigned":null,"goalType":"UNKNOWN","description":null,"priority":null},"methodologyType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"processes":[],"budgets":[],"description":null},"modelType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"artifacts":[],"cases":[],"requirements":[],"dependencies":[],"models":[],"description":null,"modelType":null},"moduleType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"artifacts":[],"work":[],"actualTime":null,"actualCost":null,"description":null,"moduleType":null},"processType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"steps":[],"budgets":[],"iterates":false,"description":null},"processStepType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"goals":[],"budgets":[],"requirements":[],"description":null},"projectType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"blueprints":[],"requirements":[],"dependencies":[],"artifacts":[],"modules":[],"stages":[],"schedule":null,"description":null},"requirementType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"note":null,"form":null,"requirementType":null,"description":null,"priority":"UNKNOWN","requirementId":null,"requirementStatus":null},"resourceType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"resourceData":null,"estimate":null,"schedule":null,"resourceType":null,"utilization":0,"resourceDataId":0,"description":null},"scheduleType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"goals":[],"budgets":[],"startTime":null,"endTime":null},"locationType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"boundaries":[],"borders":[],"description":null,"childLocations":[],"geographyType":null,"classification":null},"eventType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"location":null,"entryTraits":[],"exitTraits":[],"things":[],"actors":[],"observers":[],"influencers":[],"orchestrators":[],"groups":[],"childEvents":[],"eventType":null,"description":null,"startDate":null,"endDate":null},"traitType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"description":null,"traitType":null,"score":null,"alignmentType":null},"applicationProfileType":{"userRoles":[],"systemRoles":[],"user":null,"person":null,"organizationPath":null},"stageType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"methodology":null,"work":null,"budget":null,"schedule":null,"description":null},"taskType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"requirements":[],"artifacts":[],"work":[],"notes":[],"estimate":null,"actualTime":[],"actualCost":[],"resources":[],"dependencies":[],"childTasks":[],"taskStatus":null,"description":null,"createdDate":null,"modifiedDate":null,"completedDate":null,"dueDate":null,"startDate":null},"ticketType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"assignedResource":null,"requiredResources":[],"estimate":null,"actualTime":null,"actualCost":null,"tickets":[],"dependencies":[],"artifacts":[],"notes":[],"forms":[],"audit":[],"createdDate":null,"modifiedDate":null,"dueDate":null,"closedDate":null,"reopenedDate":null,"description":null,"ticketStatus":null,"priority":null,"severity":null},"timeType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"basisType":null,"value":null},"validationRuleType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"errorMessage":null,"replacementValue":null,"description":null,"rules":[],"expression":null,"isRuleSet":false,"isReplacementRule":false,"validationType":null,"comparison":false,"allowNull":false},"workType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"resources":[],"tasks":[],"artifacts":[],"dependencies":[],"description":null},"applicationRequestType":{"sort":null,"startRecord":0,"recordCount":0,"paginate":false,"populateGroup":false,"organizationId":0,"fullRecord":false,"lifecycleId":0,"projectId":0,"imports":[],"applicationId":0},"identityDataImportType":{"header":[],"name":null,"type":null},"baseRoleType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"roleType":null,"referenceId":0,"parentPath":null},"dataTypeSchema":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"securityType":null,"dataBytesStore":null,"cipherKey":null,"passKey":null,"detailsOnly":false,"publicId":"0","description":null,"dimensions":null,"mimeType":null,"size":0,"createdDate":null,"modifiedDate":null,"expiryDate":null,"blob":false,"compressed":false,"shortData":null,"passwordProtected":false,"passwordProtect":false,"enciphered":false,"encipher":false,"vaulted":false,"vaultId":null,"keyId":null,"readDataBytes":false,"wasDataBlob":false,"rating":0,"pointer":false,"dataHash":null,"compressionType":"NONE","volatile":true},"cryptoBeanSchema":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"publicKeyBytes":null,"privateKeyBytes":null,"cipherIV":null,"cipherKey":null,"encryptedCipherIV":null,"encryptedCipherKey":null,"cipherProvider":"BC","symmetricCipherKeySpec":"AES/CBC/PKCS7Padding","asymmetricCipherKeySpec":"RSA","randomSeedLength":576,"encryptCipherKey":false,"reverseEncrypt":false,"hashProvider":"SHA-512","cipherKeySpec":"AES","cipherKeySize":256,"keySize":1024,"globalKey":false,"primaryKey":false,"organizationKey":false,"symmetricKeyId":0,"asymmetricKeyId":0,"previousKeyId":0,"javaClass":"org.cote.beans.CryptoBean","spoolId":null},"userType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"contactInformation":null,"session":null,"homeDirectory":null,"statistics":null,"sessionStatus":"UNKNOWN","userStatus":"UNKNOWN","userType":"UNKNOWN","databaseRecord":false,"userId":null,"accountId":0},"directoryGroupType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupType":"UNKNOWN","referenceId":0,"path":null},"baseGroupType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupType":"UNKNOWN","referenceId":0,"path":null},"userGroupType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupType":"UNKNOWN","referenceId":0,"path":null},"contactInformationType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"contacts":[],"addresses":[],"contactInformationType":null,"description":null,"referenceId":0},"personType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"description":null,"firstName":null,"middleName":null,"lastName":null,"title":null,"suffix":null,"birthDate":null,"gender":null,"alias":null,"prefix":null,"users":[],"accounts":[],"contactInformation":null,"partners":[],"dependents":[],"notes":[]},"contactType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"contactValue":null,"description":null,"contactType":null,"locationType":null,"preferred":false},"addressType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"addressLine1":null,"addressLine2":null,"city":null,"region":null,"state":null,"postalCode":null,"country":null,"description":null,"locationType":null,"preferred":false},"organizationType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"organizationParent":null,"logicalId":0,"referenceId":0,"organizationType":"UNKNOWN"},"attributeType":{"values":[],"name":null,"dataType":null,"index":null,"referenceType":null,"referenceId":null,"organizationId":null},"credentialType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"salt":null,"credential":null,"referenceType":"UNKNOWN","referenceId":0,"createdDate":null,"modifiedDate":null,"expiryDate":null,"primary":false,"previousCredentialId":0,"nextCredentialId":0,"keyId":null,"vaultId":null,"enciphered":false,"vaulted":false,"credentialType":"UNKNOWN","hashProvider":"SHA-512"},"authenticationRequestType":{"credential":null,"checkCredential":null,"tokens":[],"subject":null,"organizationPath":null,"credentialType":"UNKNOWN","checkCredentialType":null,"subjectType":"UNKNOWN"},"authenticationResponseType":{"message":null,"user":null,"sessionId":null,"response":null,"organizationPath":null,"organizationId":null},"policyDefinitionType":{"parameters":[],"organizationPath":null,"urn":null,"expiresDate":null,"decisionAge":null,"enabled":null,"modifiedDate":null,"createdDate":null},"policyRequestType":{"facts":[],"contextUser":null,"urn":null,"subject":null,"credential":null,"requestType":null,"asyncRequest":false,"organizationPath":null,"subjectType":null},"policyResponseType":{"message":null,"patternChain":[],"response":null,"urn":null,"expiresDate":null,"score":0},"factType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"description":null,"score":0,"factReference":null,"factData":null,"sourceUrn":null,"sourceDataType":"UNKNOWN","sourceUrl":null,"factType":"UNKNOWN","factoryType":"UNKNOWN","parameter":false,"sourceType":null},"functionType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"description":null,"score":0,"facts":[],"functionData":null,"functionType":"UNKNOWN","sourceUrl":null,"sourceUrn":null},"functionFactType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"description":null,"score":0,"functionUrn":null,"factUrn":null},"patternType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"description":null,"score":0,"fact":null,"match":null,"operation":null,"factUrn":null,"comparator":null,"patternType":null,"matchUrn":null,"operationUrn":null},"policyType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"description":null,"score":0,"rules":[],"enabled":false,"expiresDate":null,"decisionAge":0,"modifiedDate":null,"createdDate":null,"condition":null},"operationType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"description":null,"score":0,"operation":null,"operationType":null},"ruleType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"logicalOrder":0,"description":null,"score":0,"rules":[],"patterns":[],"ruleType":"UNKNOWN","condition":"UNKNOWN"},"basePermissionType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"permissionType":null,"referenceId":0,"parentPath":null},"authorizationPolicyType":{"roles":[],"permissions":[],"members":[],"contextType":null,"factoryType":null,"contextId":0,"contextName":null,"memberType":null,"systemAdministrator":false,"accountAdministrator":false,"dataAdministrator":false,"contextUrn":null,"roleReader":false,"accountReader":false,"groupReader":false,"authenticated":false,"authenticationId":null},"accountType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"contactInformation":null,"statistics":null,"accountType":null,"accountStatus":null,"referenceId":0,"accountId":null,"databaseRecord":false},"baseTagType":{"attributes":[],"nameType":"UNKNOWN","parentId":0,"name":null,"id":0,"ownerId":0,"populated":false,"objectId":null,"attributesPopulated":false,"urn":null,"organizationId":0,"organizationPath":null,"groupId":null,"groupPath":null,"tagType":null},"dataTagSearchRequest":{"sort":null,"startRecord":0,"recordCount":0,"paginate":false,"populateGroup":false,"organizationId":0,"fullRecord":false,"tags":[]},"baseSearchRequestType":{"sort":null,"startRecord":0,"recordCount":0,"paginate":false,"populateGroup":false,"organizationId":0,"fullRecord":false},"sortQueryType":{"sortOrder":"UNKNOWN","sortField":"UNKNOWN"}}
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

