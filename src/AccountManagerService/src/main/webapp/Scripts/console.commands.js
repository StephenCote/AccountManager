(function(){
	uwm.addPageLoadHandler(function(){
		
			
			
		amc.addCommand("cd", function(s){
			var _s = this.getProperties(), _o = this.getObjects();
			if(!s || s.length == 0){
				this.addLine("Invalid argument");
				return;
			}
			s = s.replace(/^\.\//,"");
			if(!s.match(/^[~\/]/)) s = _s.currentPath + "/" + s;
			if(s.match(/\.\./)){
				//if(!s.match(/\/$/)) s = s;	
				s = s.replace(/\/[\w\s]+\/\.\./,"");
			}
			if(s.length > 1) s = s.replace(/\/$/,"");
			var oG = AM6Client.find("GROUP","UNKNOWN",s);
			if(!oG || oG == null){
				this.addLine("Invalid path: " + s);
			}
			else{
				_s.currentPath = oG.path;
				_o.currentGroup = oG;
				this.processCommand("ls -hide", 1);
				Hemi.message.service.publish("onchangedirectory", amc);
			}
			
		});
		amc.addCommand("login", function(){
			if(arguments.length < 2){
				this.addLine("Syntax: login [/Organization] username password");
				return;
			}
			var sOrg = "/Public";
			var i = 0;
			if(arguments.length > 2){
				i = 1;
				sOrg = arguments[0];
			}
			var oC = this;
			AM6Client.loginWithPassword(sOrg, arguments[i], arguments[i+1], function(s, v){
				var oU;
				if(v && v.json){
					AM6Client.clearCache(0,1);
					window.uwm.getUser().then((oU)=>{
						if(uwm.altFlushSession) uwm.altFlushSession();
						oC.addLine("Welcome!");
						Hemi.message.service.publish("onsessionrefresh", oU);
					});
				}
				
			});
		});
		amc.addCommand("logout", function(){
			// this.processCommand("clearHistory", 1);
			// this.processCommand("clear", 1);
			var b = uwm.logout();
			if(b) this.addLine("Good bye!");
			delete this.getObjects().currentGroup;
		});
		amc.addCommand("pwd", function(){
			this.addLine(this.getProperties().currentPath);
			
		});
		amc.addCommand("help", function(){
			for(var i in this.commands){
				this.addLine(i);
			}
		});
		amc.addCommand("clearUse", function(){
			this.getObjects().use.length = 0;
			this.getObjects().useMap = {};
		});
		amc.addCommand("clear", function(){
			this.getList().clearItems();
		});
		amc.addCommand("clearHistory", function(bClearStorage){
			this.getObjects().history.length = 0;
			this.getProperties().historyIndex = 0;
			if(bClearStorage) this.updateStorage();
			this.addLine("Cleared console history","_avoid");
		});
		amc.addCommand("history", function(){
			var aH = this.getObjects().history;
			for(var i = 0; i < aH.length; i++) this.addLine(aH[i]);
		});
		amc.addCommand("use",function(sOpt){
			
			if(!sOpt){
				this.addLine("Syntax: use pattern");
				return;
			}
			var _o = this.getObjects(), a, o, ix;
			a = _o.currentDataList.concat(_o.currentGroupList);
			ix = _o.use.length;
			for(var i = 0; i < a.length; i++){
				if(a[i].name == sOpt){
					o = a[i];
					break;
				}
			}
			if(o){
				if(typeof _o.useMap[o.objectId] != "undefined"){
					this.addLine("Alreading using " + o.objectId);
				}
				else{
					_o.use[ix] = o;
					_o.useMap[o.objectId] = ix;
					this.addLine("Using " + o.name + " (" + o.objectId + ") as $" + (ix + 1));
				}
			}
			else{
				this.addLine("Object not found");
			}
		});
		amc.addCommand("ls", function(bOpt){
			var sP = this.getProperties().currentPath, _o = this.getObjects(), bNoDisplay = (bOpt == "-hide");
			for(var i = 0; i < arguments.length; i++){
				if(!arguments[i].match(/^-/)){
					sP = arguments[i];
					break;
				}
			}
			if(!sP.match(/^[\.~\/]/)) sP = this.getProperties().currentPath + "/" + sP;
			var oG = AM6Client.find("GROUP","UNKNOWN", sP);
			if(!oG || oG == null){
				this.addLine("Unable to read path: " + sP);
				return;
			}
			var aL = AM6Client.list("GROUP", oG.objectId, 0,1000);
			_o.currentGroupList = aL;
			if(!bNoDisplay){
				var sT = "dir";
				if(oG.groupType == "BUCKET") sT = "bucket";
				var oPG = AM6Client.get("GROUP", oG.parentId);
				if(oPG && oPG != null) this.addLine("<" + sT + "> ..", oPG);
				for(var i = 0; i < aL.length; i++){
					sT = "dir";
					if(aL[i].groupType == "BUCKET") sT = "bucket";
					this.addLine("<" + sT + "> " + aL[i].name, aL[i]);
				}
			}
			aL = [];
			switch(oG.groupType){
				case "DATA":
					aL = AM6Client.list("DATA", oG.objectId, 0,1000);
					
					var sSubType = oG.name.replace(/s$/gi,"");
					if(this.subType.includes(sSubType)){
						aL = aL.concat(AM6Client.list(sSubType.toUpperCase(), oG.objectId, 0,0));
					}
					
					break;
				case "BUCKET":
					/// Need alternate way to handle other bucket types such as ROLE, ACCOUNT, etc
					///
					aL = AM6Client.members("GROUP", oG.objectId, "DATA");
					break;
			}
			_o.currentDataList = aL;
			if(!bNoDisplay){
				for(var i = 0; i < aL.length; i++){
					this.addLine(this.decorateItem(aL[i]), aL[i]);
				}
			}
		});
	
	});
}())