(function () {
	
	window.uwm = {
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
		 return slowAES.encrypt(uwm.strToBin(dec),
        		slowAES.modeOfOperation.CBC,
        		slowAES.padding.PKCS7,
                uwm.strToBin(uwm.base64Decode(key)),
                uwm.strToBin(uwm.base64Decode(iv))
		 );
		},
		decipher : function(sText, sKey, sIv){
        
        	return slowAES.decrypt(uwm.strToBin(uwm.base64Decode(encSvr)),
        			slowAES.modeOfOperation.CBC,
        			slowAES.padding.PKCS7,
        			uwm.strToBin(uwm.base64Decode(key)),
        			uwm.strToBin(uwm.base64Decode(iv))
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
		rule : function(sName, vParams, sSuccessOp, sFailOp, oNode, fStatus, fSuite){
			var oTest = Hemi.app.module.test.service.NewTest(sName.toLowerCase(), oNode, fStatus, fSuite, g_application_path + "Rules/");
			var bOut = 0;
			Hemi.log("START RULE: " + sName);
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
			window.uwm.session = sessionSvc.getSession();
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
		login : function(u, p, o){
			var userSvc = window.uwmServices.getService("User");
			var user = new org.cote.beans.userType();
			user.name = u;
			user.password = p;
			user.organization = o;
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
					window.uwm.user = v.json;
					window.uwm.session = v.json.session;
					uwm.operation("ContinueWorkflow", {user:v.json}, 0, "Authenticate");
				}
			});

			return 1;
			
		},
		logout : function(){
			if(uwm.rule("IsLoggedIn")){
				window.uwm.session = window.uwmServices.getService("User").getLogout();
				var oSess = Hemi.registry.service.getObject("session");
				if(oSess) oSess.Refresh(window.uwm.session);
				Hemi.log("Flush any session or cache references in related services");
				window.uwmServiceCache.clearCache();
				//rocket.flushSession();
				return 1;
			}
			return 0;
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
    Hemi.include("uwm.io", g_application_path + "/Scripts/");
    Hemi.include("hemi.data.validator");
    Hemi.include("hemi.json.rpc");
    
    /// EVENT INSTRUMENTATION
    ///
    
    ///uwm.addEventListener(window, "load", uwm.processLoadHandlers);
	Hemi.message.service.subscribe("onspaceconfigload", function (s, v){
		if(!v.is_primary) return;
		var oSpace = Hemi.app.space.service.getPrimarySpace();
		
		var oSession = Hemi.app.createApplicationComponent(g_application_path + "Components/component.session.xml",0, oSpace,"session");
		oSession.Refresh(1);

		uwm.processLoadHandlers();
		
	});
    
    
} ());
