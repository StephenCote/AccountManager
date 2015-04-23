if (typeof window != "object") window = {};
(function () {
	
	window.uwm = uwm = {
		debugMode : 1,
		pathProvider : 0,
		apiTypes : {},
		apiTypeHash : {},
		addApi : function(sApi, sViewBase){
			uwm.apiTypes[sApi] = {
				api : sApi,
				viewBase : sViewBase,
				types : []
			};
		},
		getDefaultParentForType : function(sType, vDef){
			if(uwm.defaultParentProvider) return uwm.defaultParentProvider(sType,vDef);
			return vDef;
		},
		getPathForType : function(sType, sDef){
			if(uwm.pathProvider) return uwm.pathProvider(sType);
			return sDef;
		},
		getApi : function(sType){
			return uwm.apiTypeHash[sType];
		},
		getApiTypeView : function(sType){
			var sApi = uwm.apiTypeHash[sType];
			if(!sApi){
				Hemi.logError("Invalid type for '" + sType + "'");
				return "/ERROR";
			}
			return uwm.apiTypes[sApi].viewBase;
		},
		addApiTypes : function(sApi,aTypes){
			if(!uwm.apiTypes[sApi]) return 0;
			uwm.apiTypes[sApi].types = aTypes;
			for(var i = 0; i < aTypes.length;i++) uwm.apiTypeHash[aTypes[i]] = sApi;
			return 1;
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
		addEventListener : function(o, e, f){
			return Hemi.event.addEventListener(o, e, f);
		},
		removeEventListener : function(o, e, f){
			return Hemi.event.removeEventListener(o, e, f);
		},
		getJSON : function(u,fH, a, i, c){
			return Hemi.xml.getJSON(u,fH, a, i, c);
		},
		postJSON : function(u, d, fH, a, i, c){
			/// , (fH ? 1 : 0)
			return Hemi.xml.postJSON(u, d, fH, a, i, c);
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
		/// sKey and sIv are in Base64 string format
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
		
		readData : function(sGroup, sName, sId, fHandler, sMime){
			return uwm.data.queryData("Read", 0, sGroup, sName, sId, 1, 1, sMime, fHandler);
		},
		deleteData : function(sGroup, oD, sName, sId){
			return uwm.data.queryData("Delete", oD, sGroup, sName, sId);
		},
		getNameExists : function(sGroup, sName){
			var oD = uwm.data.getNewData(sName);
			return uwm.data.queryData("CheckName", oD, sGroup, sName);
		},
		editData : function(sGroup, oD, sName, sId){
			return uwm.data.queryData("Edit", oD, sGroup, sName, sId);
		},
		addData : function(sGroup, oD){
			return uwm.data.queryData("Add", oD, sGroup);
		},
		listData : function(sName, fHandler){
			return uwm.data.queryDataList("Directory", sName, fHandler);
		},
		
		namespace : function(s){
			return Hemi.namespace(s);
		},
		lookup : function(s){
			return Hemi.lookup(s);
		},
		openPopInImage : function(sUrl){
			var i1, i2, i3;
			if((i1 = sUrl.indexOf("/Media") )== -1 || (i2 = sUrl.indexOf("/Data/")) == -1){
				return;
			}
			var sOrgPath = sUrl.substring(i1 + 6,i2);
			var oOrg = accountManager.findOrganization(sOrgPath);
			var sPath = sUrl.substring(i2 + 5,sUrl.length);
			var sName = sPath.substring((i3 = sPath.lastIndexOf("/")) + 1, sPath.length);
			var iW = parseInt(.8 * document.documentElement.clientWidth);
			var iH = parseInt(.8 * document.documentElement.clientHeight);
			iW = Math.floor(iW/250) * 250;
			if(iW <= 0) iW = 250;
			iH = Math.floor(iH/250) * 250;
			if(iH <= 0) iH = 250;
			var sMediaUrl = "/AccountManager/Thumbnail" + sOrgPath + "/Data" + sPath + "/" + iW + "x" + iH;
			var vProps = {
				media_name : sName,
				media_id : "N/A",
				media_url: sMediaUrl
			};
			Hemi.app.createWindow(sName,"/Forms/ImageViewer.xml",sUrl,0,0,vProps,function(oW){
				oW.setIsModal(1);
				oW.setCanMinimize(0);
				oW.setCanMaximize(0);
				oW.center();
			});
		},
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
		createWindow : function(n, u){
			return Hemi.app.createWindow(n, g_application_path + "Forms/Login.xml", n);
		},
		createModal : function(n, u){
			var oW = Hemi.app.createWindow(n, g_application_path + "Forms/Login.xml", n);
			oW.setIsModal(true);
			return oW;
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
			/// alert(g_application_path + "Rules/" + sName + ":" + oTestResult + ":" + (oTestResult ? oTestResult.data : "NA"));
			if(oTestResult && oTestResult.data == true){
				/// Only set return val to op if all operations are updated to expect a return value
				/// There was a few hours well spent
				bOut = 1;
				if(sSuccessOp){
					// bOut = 
					window.uwm.operation(sSuccessOp, vParams, oNode, sName);
				}
				else{
					//bOut = 1;
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
		getSessionData : function(sName, oSess){
			if(!oSess) oSess = window.uwm.getSession();
			if(!oSess.sessionData) return;
			var sVal;
			for(var i = 0; i < oSess.sessionData.length;i++){
				if(oSess.sessionData[i].name == sName){
					sVal = oSess.sessionData[i].value;
					break;
				}
			}
			return sVal;
		},
		getSession : function(bRefresh){
			if(!bRefresh && window.uwm.session) return window.uwm.session;
			var sessionSvc = window.uwmServices.getService("Session");
			window.uwm.session = sessionSvc.getSafeSession(Hemi.guid());
			var oSess = Hemi.registry.service.getObject("session");
			if(oSess) oSess.Refresh(window.uwm.session);
			//rocket.flushSession();
			Hemi.log("Flush any session or cache references in related services");
			return window.uwm.session;
		},
		getUser : function(bRefresh){
			if(!bRefresh && window.uwm.user) return window.uwm.user;
			var userSvc = window.uwmServices.getService("User");
			window.uwm.user = userSvc.getSelf();
			return window.uwm.user;
		},
		login : function(u, p, o, v){
			var userSvc = window.uwmServices.getService("User");
			var user = new org.cote.beans.userType();
			user.name = u;
			user.password = p;
			user.organization = o;
			var vParms = (v ? v : {});
			/*
			window.uwm.user = userSvc.postLogin(user);
			window.uwm.session = user.session;
			//rocket.flushSession();
			Hemi.log("Flush any session or cache references in related services");
			return window.uwm.user;
			*/
			userSvc.postLogin(user,{
				hemiSvcCfg:1,
				async:1,
				handler:function(s, v){
					if(v && v.json && v.json.session){
						window.uwm.user = v.json;
						window.uwm.session = v.json.session;
						uwm.clearCache();
						var oSess = Hemi.registry.service.getObject("session");
						if(oSess) oSess.Refresh(window.uwm.session);
						if(uwm.altFlushSession) uwm.altFlushSession();
					}
					vParms.user = v.json;
					uwm.operation("ContinueWorkflow", vParms, 0, "Authenticate");
				}
			});

			return 1;
			
		},
		logout : function(){
			if(uwm.rule("IsLoggedIn")){
				window.uwm.session = window.uwmServices.getService("User").safeLogout(Hemi.guid());
				uwm.clearCache();
				var oSess = Hemi.registry.service.getObject("session");
				if(oSess) oSess.Refresh(window.uwm.session);
				Hemi.log("Flush any session or cache references in related services");
				if(uwm.altFlushSession) uwm.altFlushSession();
				//rocket.flushSession();
				return 1;
			}
			return 0;
		},
		clearCache : function(){
			window.uwmServiceCache.clearCache();
			Hemi.xml.clearCache();
		},
		register : function(u, p, e, o){
			var userSvc = window.uwmServices.getService("User");
			var user = new org.cote.beans.userType();
			var ci = new org.cote.beans.contactInformationType();
			var ct = new org.cote.beans.contactType();
			user.name = u;
			user.password = p;
			user.organization = o;
			//ci.email = e;
			ci.contacts = [];
			ci.contacts.push(ct);

			ct.group = accountManager.getGroup("/Contacts");
			ct.name = u + " Registration Email";
			ct.preferred = true;
			ct.contactType = "EMAIL";
			ct.locationType = "HOME";
			ct.contactValue = e;

			Hemi.log("Submit registration: " + u + " / " + p + " / " + e + " in org " + (o ? o.name : "public"));
			user.contactInformation = ci;
			window.uwm.registration = userSvc.postRegistration(user);
			return window.uwm.registration;
		}
    };
	
	
	
	window.uwm.data = {
			queryDataList : function(sCtx, sName, fHandler){
				var oRequest = Hemi.data.io.service.newIORequest(
			        3,
			        "HemiFramework",
					sCtx,
			        sName,
					"List", //action
					0, // id
					0, // name
					0, // details only
					(fHandler ? 1 : 0), // async
					0, // cache
					0 // instruction
				);
				Hemi.data.io.service.openRequest(Hemi.data.io.service.getSubject(), oRequest, fHandler);
				return Hemi.data.io.service.getResponseByName(oRequest.responseId);
			},
			queryData : function(sAct, oD, sCtx, sName, sId, bFull, bAsync, sMime, fHandler){
				if(!sCtx) sCtx = "Public";
				var oRequest = Hemi.data.io.service.newIORequest(
			        3,
			        "HemiFramework",
					sCtx,
			        0,
					sAct, //action
					(sId ? sId : 0), // id
					(sName ? sName : 0), // name
					(bFull ? 0 : 1), // details only
					(bAsync ? 1 : 0), // async
					0, // cache
					0 // instruction
				);
				if(sMime) oRequest.mimeType = sMime;
				if(oD) oRequest.requestData.push(oD);
				Hemi.data.io.service.openRequest(Hemi.data.io.service.getSubject(), oRequest, fHandler);
				var oResponse = Hemi.data.io.service.getResponseByName(oRequest.responseId);
				return oResponse;
			},

			getNewData : function(sName, sId, sData){
				var oData = Hemi.data.io.service.newData();
				oData.postData = 1;
				oData.name = sName;
				if(sId) oData.id = sId;
				oData.mimeType = "application/xml";
				oData.value = sData;
				return oData;
			}	
	};
	/// ANY ClientFramework SETUP
	///
    Hemi.include("hemi.app");
    Hemi.include("hemi.event");
    Hemi.include("hemi.css");
    Hemi.include("hemi.app.space");
    Hemi.include("hemi.app.comp");
    Hemi.include("hemi.app.module");
    Hemi.include("hemi.app.module.test");
    /// Hemi.include("uwm.io", g_application_path + "Scripts/");
    Hemi.include("hemi.data.validator");
    Hemi.include("hemi.json.rpc");
    
    /// EVENT INSTRUMENTATION
    ///
    
    ///uwm.addEventListener(window, "load", uwm.processLoadHandlers);
	Hemi.message.service.subscribe("onspaceconfigload", function (s, v){
		if(!v.is_primary) return;
		var oSpace = Hemi.app.space.service.getPrimarySpace();
		
		var oSession = Hemi.app.createApplicationComponent("/AccountManagerExample/Components/component.session.xml",0, oSpace,"session");
		oSession.Refresh(1);

		uwm.processLoadHandlers();
		
	});
	
	function checkForUpgrades(){
		if(typeof Upgradeable != "object") return;
		Hemi.app.createWindow(0, "/AccountManagerExample/Templates/UpgradeAvailable.xml","Upgradeable",0,0,{old_project_name:Upgradeable.old_project_name,old_project_version:Upgradeable.old_project_version,new_project_name:Upgradeable.new_project_name,new_project_version:Upgradeable.new_project_version,new_project_path:Upgradeable.new_project_path,upgrade_notes:Upgradeable.upgrade_notes});
	}
	
	window.uwm.addPageLoadHandler(checkForUpgrades);
    
    
})();
