(function(){
	
	var ctl;
	uwm.addPageLoadHandler(function(){

		window.amc = ctl = Hemi.newObject("Console","1.0",true,true,{
			object_create : function(){
				/// window.amc = ctl = this;
				var _s = this.getProperties(), _o = this.getObjects();
				_s.iconView = 1;
				_s.maximize = 1;
				_o.history = [];
				_s.historyIndex = 0;
				_o.use = [];
				_o.useMap = {};
				Hemi.event.addScopeBuffer(this);
				this.scopeHandler("keydown",0,0,1);
				this.scopeHandler("session_refresh",0,0,1);
				this.scopeHandler("window_resize",0,0,1);
				Hemi.event.addEventListener(window,"resize",this._prehandle_window_resize);
				Hemi.message.service.subscribe(this, "onsessionrefresh", "_prehandle_session_refresh");
				Hemi.message.service.subscribe(this, "onbuffercommitted", "handle_buffer_commit", this.getList());
				document.getElementById("commandLine").onkeydown = this._prehandle_keydown;
				this.getComponentByRID("typelist").setLoadHandler(this.scopeHandler("configList",0,1,1));
				this.focus();
			},
			
			object_destroy : function(){
				Hemi.message.service.unsubscribe(this, "onsessionrefresh", "handle_session_refresh");
				Hemi.message.service.unsubscribe(this, "onbuffercommitted", "handle_buffer_commit", this.getList());
				Hemi.event.removeEventListener(window,"resize",this._prehandle_window_resize);
			},
			_handle_window_resize : function(){
				this.resizeFrame();
			},
			resizeFrame : function(w, h){
				if(!h && this.getProperties().maximize){
					/// Drop the list size down temporarily
					this.getList().getContainer().style.height = "50px";
					h = (document.getElementById("contentContainer").parentNode.clientHeight - document.getElementById("contentContainer").offsetTop - document.getElementById("commandContainer").offsetHeight) + "px";
					//alert(h + "/" + (document.body.clientHeight - document.getElementById("contentContainer").offsetTop) );
				}
				if(w) document.getElementById("typelist").style.width = w;;
				if(h){
					
					document.getElementById("typelist").style.height = h;
				}
				this.getList().getContainer().style.height = document.getElementById("typelist").clientHeight + "px";
				
			},
			configList : function(s, v){
				this.addLine("Account Manager Service 5.6","_avoid");
				this.addLine("Simple Shell 1.0","_avoid");
				this.refreshDisplay();
				var oL = this.getList();
				oL.setAutoScroll(1);
				var oC = this;
				oL.setResultHandler(function(s,v){
					oC.getList().deselectAllItems();
					if(v && v.data){
						oC.viewObject(v.data);
					}
				});
				setTimeout("Hemi.registry.service.getObject('" + this.getObjectId() + "').resizeFrame()",50);

				/// oL.setAutoSelect(1);
			},
			viewObject : function(o){
				var oWp;
				if(o.nameType == "DATA"){
					if(o.mimeType.match(/video/gi) || o.mimeType.match(/^image/gi)){
						var bVid = o.mimeType.match(/^video\//gi);
						var sUrl = location.protocol + "//" + location.hostname + (location.port ? ":" + location.port : "") + g_application_path + "media/" + AM6Client.dotPath(AM6Client.currentOrganization) + "/Data" +  o.groupPath + "/" + o.name
						uwm.openPopInImage(sUrl, o.mimeType, bVid, 1);
					}
					else if(o.mimeType.match(/^text/gi) || o.mimeType.match(/xml/gi) || o.mimeType.match(/javascript/gi)){
						var oProps = {openerId:this.getObjectId(),picker:0,viewType:o,autoDisplay:1};
						oWp = Hemi.app.createWindow(o.name, uwm.getApiTypeView("DATA") + "/Forms/Data.xml", "View-" + o.id, 0, 0, oProps);

					}
				}
				else if(o.nameType == "GROUP"){
					this.processCommand("cd \"" + o.path + "\"");
					this.processCommand("ls", 1);
				}
				else if(o.nameType == "USER" || o.nameType == "PERSON"){
					if(!o.populated) o = AM6Client.get(o.nameType, o.objectId);
					var oProps = {openerId:this.getObjectId(),picker:0,viewType:o,autoDisplay:1,listType:(o.nameType == "USER" ? "User" : "Person")};
					oWp = Hemi.app.createWindow(o.name, uwm.getApiTypeView(o.nameType) + "/Forms/Profile.xml", "View-" + o.id, 0, 0, oProps);

				}
			    if (oWp) {
			    	oWp.then((oW)=>{
				    	oW.resizeTo(475, 400);
				    	Hemi.app.getWindowManager().then((oM)=>{oM.CenterWindow(oW);});;
				    	oW.setHideOnClose(0);
				    	oW.setCanMinimize(0);
				    	oW.setCanMaximize(0);
				    	oW.setCanResize(0);
			    	});
			    }
			},
			addLine : function(s, v){
				var oL = this.getList();
				oL.addItem(s, (v ? v : "_avoid"));
				
			},
			handle_buffer_commit : function(s, v){
				//var oL = this.getList();
				//oL.getContainer().scrollTop = oL.getContainer().scrollHeight;
			},
			getList : function(){
				return this.getComponentByRID("typelist").GetWideSelect();
			},
			getCurrentGroup : function(){
				return this.getObjects().currentGroup;
			},
			getComponentByRID : function(n){
				var o = Hemi.app.space.service.getPrimarySpace().getSpaceObjectByName(n);
				if(!o || !o.object) return 0;
				return o.object.getApplicationComponent();
			},
			peek : function(c, s){
				var _o = this.getObjects(), a = [],p,r,sP;
				if(c.match(/^(ls|cd)$/)){
					sP = s.substring(0,s.lastIndexOf("/"));
					this.processCommand("ls -hide " + sP, 1);
					s = s.substring(s.lastIndexOf("/") + 1, s.length);
				}
				p = new RegExp("^" + s);
				a = _o.currentGroupList;
				if(!c.match(/^(ls|cd)$/)) a = a.concat(_o.currentDataList);
				for(i = 0; i < a.length; i++){
					if(a[i].name.match(p)){
						r = a[i].name;
						break;
					}
				}
				if(r && sP) r = sP + "/" + r;
				return r;
			},
			_handle_keydown : function(e){
				e = Hemi.event.getEvent(e);
				var oS = Hemi.event.getEventSource(e), _s = this.getProperties(); _o = this.getObjects();
				if(!oS || !oS.nodeName || !oS.nodeName.match(/^(input|textarea|select)/gi)) return;
				switch(e.keyCode){
					/// TAB
					case 9:
						var aC = this.parseCommand(document.getElementById("commandLine").value.trim());
						if(this.peekCommands.includes(aC[0])){
							Hemi.event.cancelEvent(e);
							var sPeek = this.peek(aC[0], aC[aC.length-1]);
							if(sPeek){
								var sCmd = document.getElementById("commandLine").value;
								sCmd = sCmd.substring(0,sCmd.lastIndexOf(" ") + 1) + sPeek;
								document.getElementById("commandLine").value = sCmd;
							}
							return false;
						}
						break;
					/// ESC
					case 27:
						document.getElementById("commandLine").value = "";
						break;
					/// Enter
					case 13:
						var cmdL = document.getElementById("commandLine").value.trim()
						var cmd = document.getElementById("prompt").innerText + " " + cmdL;
						this.processCommand(cmdL);
						document.getElementById("commandLine").value = "";
						break;
					/// Up arrow
					case 38:
						if(_s.historyIndex > 0){
							document.getElementById("commandLine").value = _o.history[_s.historyIndex - 1];
							_s.historyIndex--;
						}
						Hemi.event.cancelEvent(e);
						return false;
						break;
					/// Down arrow
					case 40:
						if(_s.historyIndex < _o.history.length){
							_s.historyIndex++;
							document.getElementById("commandLine").value = _o.history[_s.historyIndex - 1];

						}
						Hemi.event.cancelEvent(e);
						return false;
						break;
					default:
						// Hemi.log(e.keyCode);
						break;
				}
				
			},
			addCommand : function(s, f){
				if(this.commands[s]) return 0;
				this.commands[s] = f;
				return 1;
			},
			parseCommand : function(s){
				var aS;
				if(!s || (aS = s.match(/[\(\)\$@\w\.\-\~\/]+|"[^"]+"/g)) == null) return [];
				return aS;
			},
			isCommand : function(s){
				var aS = this.parseCommand(s);
				return (this.commands[aS[0]] ? 1 : 0);
			},
			useCommand : function(aS, ix){
				var _o = this.getObjects(),o,ues = /^\$(\d+)\./,sC;
				if(!(o = _o.use[ix])){
					this.addLine("Unknown use index '" + ix + "'");
					return;
				}
				if(aS[0].match(ues)){
					sC = aS[0].replace(ues,"");
					/// Method
					if(sC.match(/\(/)){
						if(sC.match(/^view/)){
							this.viewObject(o);
						}
					}
					else{
						this.addLine(o[sC]);	
					}
				}
				else{
					
					for(var i in o){
						if(o[i]) this.addLine("(" + (typeof o[i]) + ") " +  i + " = " + o[i]);
					}
				}
				
				
			},
			processCommand : function(s, bHide){
				var aS = this.parseCommand(s), bAuth = (this.getObjects().user ? 1 : 0), ue = /^\$(\d+)/, m;
				
				if(!aS.length){
					return;
				}
				for(var a = 1; a < aS.length; a++){
					aS[a] = aS[a].replace(/"/gi,"");
				}
				if(!bHide && !this.privateCommands.includes(aS[0])){
					var aH = this.getObjects().history;
					if(!aH.length || aH[aH.length-1] != s){
						aH.push(s);
						this.getProperties().historyIndex = aH.length;
						this.addLine(document.getElementById("prompt").innerText + " " + s);
						this.updateStorage();
					}
				}
				if(!bAuth && !this.anonymousCommands.includes(aS[0])){
					this.addLine("Not authenticated");
					return;
				}
				if(!this.commands[aS[0]] || this.commands[aS[0]] == null){
					if( (m = s.match(ue)) && m.length >= 2 ){
						this.useCommand(aS, parseInt(m[1])-1);
					}
					else{
						this.addLine("Unknown command: " + aS[0]);
					}
					return;
				}
				this.commands[aS[0]].apply(this,aS.slice(1));
				
			
			},
			_handle_session_refresh : function(s,v){
				if(!v || v == null){
					this.processCommand("clearHistory", 1);
					this.processCommand("clear", 1);
				}
				this.refreshDisplay();
			},
			refreshDisplay : function(){
				
				var _o = this.getObjects(), _s = this.getProperties(), sP = "Anonymous:$", sPa = "/";
				uwm.getUser().then((oU)=>{
					_o.user = (oU && oU != null ? oU : null);
					if(_o.user != null){
						sP = oU.name + ":$";
						sPa = "/Home/" + oU.name;
						_s.currentPath = sPa;
						this.loadStorage();
						/// this.processCommand("ls -hide", 1);
						this.processCommand("cd \"" + _s.currentPath + "\"", 1);
					}
					else{
						_s.currentPath = "/";
					}
					
					Hemi.xml.setInnerXHTML(document.getElementById("prompt"), sP);
				});
			},
			loadStorage : function(){
				var _o = this.getObjects();
				if(!Hemi.storage.testStorageSupported() || !_o.user) return;
				var hS = Hemi.storage.getStorageProvider();
				var sH = hS.getItem("history-" + _o.user.objectId);
				var sP = hS.getItem("currentPath-" + _o.user.objectId);
				if(sH){
					this.getObjects().history = JSON.parse(sH);
					this.getProperties().historyIndex = this.getObjects().history.length;
				}
				if(sP){
					this.addLine("Restoring path: " + sP);
					this.getProperties().currentPath = sP;
				}
			},
			updateStorage : function(){
				var _o = this.getObjects();
				if(!Hemi.storage.testStorageSupported() || !_o.user) return;
				var hS = Hemi.storage.getStorageProvider();
				hS.setItem("history-" + _o.user.objectId, JSON.stringify(this.getObjects().history));
				hS.setItem("currentPath-" + _o.user.objectId, this.getProperties().currentPath);
			},
			focus : function(){
				document.getElementById("commandLine").focus();
			},
			commands : {
				


			},
			
			subType : [
				"Address","Contact","Application","Group","Ticket","Story","Requirement","Note","Module","Model","Goal","ValidationRule","Form","FormElement","Case","Artifact","ProcessStep","Process","Method","Stage","Data","Work","Task","Cost","Time","Estimate","Budget","Person","Account","Resource","Lifecycle","Project","Schedule","Methodology","Policy","Pattern","Rule","Fact","Operation","Function","Permission","Role","Tag","Event","Location","Trait"
			],
			decorateItem : function(o){
				if(typeof o == "string" || !o.nameType) return o;
				var r = o;
				switch(o.nameType){
					case "DATA":
						r = this.decorateDataItemName(o);
						break;
					case "USER":
					case "PERSON":
						r = this.decorateProfileIcon(o);
						break;
					default:
						r = o.name;
						break;
				};
				return r;
			},
			decorateProfileIcon : function(o){
				var _p = this.getProperties();
				if(!_p.iconView) return o.name;
				var w = (_p.iconWidth ? _p.iconWidth : 48);
				var h = (_p.iconHeight ? _p.iconHeight : 48);
				var oL = document.createElement("div");
				var oP = document.createElement("p");
				
				oP.setAttribute("style","text-indent:0px !important;");
				var oP2 = document.createElement("p");
				var oI = document.createElement("img");
				oI.setAttribute("class","alignright");
				var sIco = "/AccountManagerService/Media/Icons/user_48x48.png";
				var sPid = AM6Client.getAttributeValue(o,"v1-profile",0);
				
				oP.appendChild(oI);
				oP.appendChild(document.createTextNode(o.name));
				oP.appendChild(document.createElement("br"));
				oP.appendChild(document.createTextNode((o.description ? o.description : "")));
				oP.appendChild(document.createElement("br"));
				oP.appendChild(document.createTextNode((o.alias ? o.alias : "")));
				oL.appendChild(oP);
				oP2.setAttribute("class","clearalign");
				oL.appendChild(oP2);
				if(sPid){
					AM6Client.get("DATA", sPid, function(s, v){
						if(v && v.json) v = v.json;
						if(v && v != null){;
							var sOrg = AM6Client.dotPath(v.organizationPath);
							oI.setAttribute("src","/AccountManagerService/thumbnail/" + sOrg + "/Data" + v.groupPath + "/" + v.name + "/" + w + "x" + h);
						}
						else{
							oI.setAttribute("src",sIco);
						}
					});
				}
				else{
					oI.setAttribute("src",sIco);
				}
				return oL;
			},
			decorateDataItemName : function(o){
				var _p = this.getProperties();
				if(!_p.iconView) return o.name;
				var w = (_p.iconWidth ? _p.iconWidth : 48);
				var h = (_p.iconHeight ? _p.iconHeight : 48);
				var oL = document.createElement("div");
				var oP = document.createElement("p");
				oP.setAttribute("style","text-indent:0px !important;");
				var oP2 = document.createElement("p");
				var oI = document.createElement("img");
				oI.setAttribute("class","alignright");
				var sIco = "/AccountManagerService/Media/Icons/Crystal/48x48/48px-Crystal_Clear_action_filenew.png";
				if(o.mimeType.match(/^image/)){
					sIco = "/AccountManagerService/thumbnail/" + AM6Client.dotPath(AM6Client.currentOrganization) + "/Data" + o.groupPath + "/" + o.name + "/" + w + "x" + h
				}
				oI.setAttribute("src",sIco);
				oP.appendChild(oI);
				oP.appendChild(document.createTextNode(o.name));
				oP.appendChild(document.createElement("br"));
				if(o.description != null) oP.appendChild(document.createTextNode(o.description));
				oP.appendChild(document.createElement("br"));
				oP.appendChild(document.createTextNode(o.createdDate.toString()));
				oL.appendChild(oP);
				oP2.setAttribute("class","clearalign");
				oL.appendChild(oP2);
				return oL;
							
			},
			privateCommands : ["login","logout","history","clearHistory"],
			anonymousCommands : ["help","login"],
			peekCommands : ["ls","cd","use"]
				
		});
		
	});
	
	
}())