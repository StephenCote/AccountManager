	(function(){
		Hemi.include("hemi.event");
		Hemi.include("hemi.util.logger");
		Hemi.include("hemi.graphics.canvas");
		
		if(window.galleryView){
			window.galleryView.destroy();
			window.galleryView  = null;
		}
		
		var ctl = Hemi.newObject("CanvasController","1.0",true,true,{
			object_destroy : function(){
				this.getCanvas().destroy();
				this.getObjects().cvs_container.parentNode.removeChild(this.getObjects().cvs_container);
			},
			object_create : function(){
				initializeCanvasController(this);
				this.getCanvas().AddShapeDecorator(this);
			},
			getCanvasContainer : function(){
				return this.getObjects().cvs_container;
			},
	
			getCanvas : function(){
				return this.getObjects().canvas;
			},
			clearStyle : function(){
				getCanvas().setTemporaryContextConfig(getCanvas().getConfigByName("NoShadow"));
				getCanvas().setContextConfig(getCanvas().getConfigByName("NoShadow"));
			},

			handle_canvas_click : function(oCanvas, e)
			{
				//this.log("Consider Click - " + oCanvas.getObjects().MouseClickShape);
				var oShape = oCanvas.getObjects().MouseClickShape;
				if(!oShape){
					this.logWarning("No click shape at " + oCanvas.getProperties().MouseTrackLeft + "x" + oCanvas.getProperties().MouseTrackTop);
					return;
				}
				
				if(!oShape.action){
					this.logWarning("No click action for " + oShape.id);
					return;
				}
				var oPanel = Hemi.registry.service.getObject(oShape.panelId);
	
				this.logDebug("Action Click - " + oShape.action);
	
				if(oPanel && oPanel.getObjects().view.getObjects().controller[oShape.action]){
					var o = oShape;
					this.logDebug("Delegate " + oShape.action);
					if(oShape.matteId) o = oCanvas.getShapeById(oShape.matteId);
					else if (oShape.referenceType == "MATTE") o = oShape;
					oPanel.getObjects().view.getObjects().controller[oShape.action](oPanel,oShape.referenceType, oShape.referenceId, o);
				}
				else{
					this.logWarning("Undefined delegate for " + oShape.action + " on " + (oPanel ? oPanel.getObjectId() : " null panel"));
				}
			},
			handle_canvas_mouseover : function(oCanvas, e){
				var oShape = oCanvas.getObjects().MouseOverShape;
				if(!oShape || !oShape.hover) return;
				/// Paint over the current position
				///
				if(oShape.type == "Image"){
					this.logDebug("Panel #:" + oShape.panelId);
					var oPanel = Hemi.registry.service.getObject(oShape.panelId);
					if(isRendering()){
						this.logDebug("Disable effect while content is rendering");
						return;
					}
					var oLast = oCanvas.getTemporaryContextConfig();
					var oCfg = oCanvas.getConfigByName("DropGreenShadow");
	
					oCanvas.setTemporaryContextConfig(oCfg);
					
	
					var oI = oCanvas.Image(oShape.image,oShape.x, oShape.y, "#00FF00", "#000000");
					copyShapeProperties(oShape, oI);
	
					var icoHeight = oPanel.getProperties().thumbHeight;//(oShape.icoType == "IMG" ?  o.imgThumbHeight : properties.dirThumbHeight);
					this.logDebug("Shape: " + oShape.x + "," + oShape.y + " " + oShape.referenceName + " " + oShape.panelId + " " + (oPanel ? 'Panel' : 'No Panel') + " " + icoHeight);
	
					if(oShape.referenceName && !oShape.noLabel) oCanvas.Text(scaleText(oShape.referenceName), oShape.x, oShape.y + icoHeight, "#000000","#000000","8pt","Arial");
					oCanvas.setTemporaryContextConfig(oLast);
				}
			},
			handle_canvas_mouseout : function(oCanvas, e){
				var oShape = oCanvas.getObjects().MouseOverShape;
				/// Return on the "viewObject" action because it's overlayed with a temp matte
				///
				if(!oShape || !oShape.hover || oShape.action == "gestureMatteImage"
				){
					this.logDebug("Don't clear because " + (!oShape ? " no shape" : (!oShape.hover ? " no hover " : oShape.action)));
					return;
				}
				var oPanel = Hemi.registry.service.getObject(oShape.panelId);
				if(isRendering()){
					this.logDebug("Disable effect while content rendering");
					return;
				}

				this.logDebug("Clear because " + (!oShape ? " no shape" : (!oShape.hover ? " no hover " : oShape.action)));
				oCanvas.ClearTempCanvas();
	
			},
			paintMenu : function(oPanel, oCanvas, oRefShape, aMenu){
				
				for(var i = 0; i < aMenu.length;i++){
	
					var oM = aMenu[i];
					if(oM.image){
						var oShape = oCanvas.Image(oM.image, oRefShape.x, oRefShape.y + oRefShape.image.height + 5);
						oShape.action = oM.action;
						oShape.panelId = oPanel.getObjectId();
						oShape.referenceId = -1;
						oShape.referenceType = oPanel.getProperties().itemType.toUpperCase();
						//oShape.objectType = oPanel.getProperties().objectType;
						oShape.hover = 1;
						oPanel.getShapes().push(oShape.id);
					}
					else{
						var img = new Image();
						img.onload = function(){
							var oShape = oCanvas.Image(img, oRefShape.x, oRefShape.y + oRefShape.image.height + 5);
							oShape.action = oM.action;
							oShape.panelId = oPanel.getObjectId();
							oShape.referenceId = -1;
							oShape.referenceType = oPanel.getProperties().itemType.toUpperCase();
							//oShape.objectType = oPanel.getProperties().objectType;
							oShape.hover = 1;
							oM.image = img;
							oPanel.getShapes().push(oShape.id);
						}
						img.src =  oPanel.getObjects().view.getProperties().iconSmallBase +oM.icon;
					}
				}
				
			},
			handle_canvas_mousedown : function(oCanvas, e)
			{
				if(!oCanvas.getProperties().MouseTrackChoose) return;
	
				var oShape = oCanvas.getObjects().MouseDownShape;
				if(!oShape || !oShape.drag){
					/// No shape drag
					return;
				}
				oCanvas.ClearTempCanvas();
				/// Paint over the current position
				///
				if(oShape.type == "Image"){
					///this.log("Paint decorated image");
					var oPanel = Hemi.registry.service.getObject(oShape.panelId);
					
					var oLast = oCanvas.getTemporaryContextConfig();
					var oCfg = oCanvas.getConfigByName("DropBlueShadow");
					oCanvas.setTemporaryContextConfig(oCfg);
					var oX = oCanvas.Image(oShape.image,oShape.x, oShape.y, "#00FF00", "#000000");
					copyShapeProperties(oShape, oX);
	
					var icoHeight = oPanel.getProperties().thumbHeight;//(oShape.icoType == "IMG" ?  properties.imgThumbHeight : properties.dirThumbHeight);
					if(oShape.referenceName && !oShape.noLabel) oCanvas.Text(scaleText(oShape.referenceName), oShape.x, oShape.y + icoHeight, "#000000","#000000","8pt","Arial");
					oCanvas.setTemporaryContextConfig(oLast);
					
					//if(oPanel.getObjects().menu) this.paintMenu(oPanel, oCanvas, oShape, oPanel.getObjects().menu);
				}

			},
			handleDropShape : function(oSrc, oTarg){
				
				//if((oSrc.referenceType == "GROUP" || oSrc.referenceType == "OBJECT") && (oTarg.referenceType == "GROUP" || oTarg.referenceType == "OBJECT")){
				if(oSrc.referenceType == "GROUP" && oTarg.referenceType == "GROUP"){
					var oPanel = Hemi.registry.service.getObject(oSrc.panelId);
					var oSGroup = accountManager.getGroupById(oSrc.referenceId);
					var oTGroup = accountManager.getGroupById(oTarg.referenceId);
					if(!oSGroup || !oTGroup){
						this.logWarning("Invalid source or target group from " + oSrc.referenceId + " and " + oTarg.referenceId);
						return;
					}
					if(oTGroup.id == oSGroup.parentId || oTGroup.parentId == oSGroup.id || oTGroup.id == oSGroup.id){
						this.logWarning("Parent/Group relationship already exists, or parent cannot be moved into its own child");
						return;
					}
					reparentGroup(oPanel,oSGroup, oTGroup);
					//reparentObject(oObj, oTGroup);
				}
				else if ((oSrc.referenceType == "GROUP" || oSrc.referenceType == "OBJECT") && oTarg.referenceType == "CTL"){
					var oPanel = Hemi.registry.service.getObject(oTarg.panelId);
	
					if(oTarg.action == "deleteObject" && oPanel.getObjects().view.getObjects().controller[oTarg.action]){
						oPanel.getObjects().view.getObjects().controller[oTarg.action](oPanel,oSrc.referenceType,oSrc.referenceId,oSrc);
					}
					if((oSrc.referenceType == "GROUP" || oSrc.referenceType == "OBJECT") && oTarg.action == "controlPanel"){
						openObject(oPanel,oSrc.referenceType, oSrc.referenceId, oSrc);
					}
				}
				else if (oSrc.referenceType == "OBJECT" && oTarg.referenceType == "GROUP"){
					Hemi.logError("DEAD CODE WARNING");
					var oSPanel = Hemi.registry.service.getObject(oSrc.panelId);
					var oObj = getObjectById(oSPanel,oSrc.referenceType,oSrc.referenceId,oSrc);
					var oTGroup = accountManager.getGroupById(oTarg.referenceId);
					if(!oObj){
						this.logError("Invalid object reference: " + oSrc.referenceName + " (#" + oSrc.referenceId + ")");
						return;
					}
					if(!oTGroup){
						this.logError("Invalid group reference: " + oTarg.referenceName + " (#" + oTarg.referenceId + ")");
						return;
					}
					reparentObject(oObj, oTGroup);
	
				}
				else if (oSrc.referenceType == "OBJECT" && oTarg.referenceType == "OBJECT" && oSrc.action){
					var oPanel = Hemi.registry.service.getObject(oSrc.panelId);
	
					if(oPanel && oPanel.getObjects().view.getObjects().controller[oTarg.action]){
						oPanel.getObjects().view.getObjects().controller[oTarg.action](oSrc.referenceType, oSrc.referenceId, oSrc);
					}
				}
				else{
					this.logWarning("Unknown referenceTypes.  Source=" + oSrc.referenceType + " and Target = " + oTarg.referenceType);
				}
			},
			handle_canvas_mouseup : function(oCanvas, e)
			{
				var _s = oCanvas.getProperties(), _p = oCanvas.getObjects();
				var oDShape = oCanvas.getObjects().MouseDownShape;
				var oCShape = oCanvas.getObjects().MouseOverShape;
				this.logDebug((oDShape ? oDShape.id + " " + oDShape.type : "No Shape") + " over " + (oCShape ? oCShape.id + " " + oCShape.type : "No Shape"));
				if(!oDShape || (oDShape && !oDShape.drag && !oDShape.hover )) return;
				this.logDebug("Mouseup: Clear Temp Canvas");
				oCanvas.ClearTempCanvas();
				
				if(oDShape && oCShape){
					if(oDShape.id != oCShape.id){
						this.handleDropShape(oDShape, oCShape);
						this.logDebug("Handle drop: " + oDShape.id + " to " + oCShape.id);
						Hemi.event.cancelEvent(e);
						/// Returning false from the mouseup event will set the internal 'blockClick' event on the canvas service
						///
						return false;
					}
					else{
						// this.log("Down shape equals click shape");
					}
				}
				else{
					this.logDebug("DShape = " + oDShape + " / CShape = " + oCShape);
				}
			},
			handle_canvas_mousemove : function(oCanvas, e)
			{
			   if(!oCanvas.getProperties().MouseTrackDown) return;
	
				     
				
				var sDropColor = "#FFFF00";
				var oDropShape = oCanvas.getObjects().MouseDropShape;
				var oDropNode, oCurrentNode = 0;
	
				var oCurrent = oCanvas.getObjects().MouseDownShape;
				if(oCurrent && !oCurrent.drag) return;
				oCanvas.ClearTempCanvas();
				
				if(oCanvas.getProperties().MouseTrackChoose && oCurrent ){
					if(oCurrent.type == "Image"){
						var oM = oCanvas.Rect(oCurrent.x, oCurrent.y, oCurrent.image.width, oCurrent.image.height, "#FFFFFF", "#FFFFFF");
						oM.selectable = 0;
						//this.log("Panel #:" + oCurrent.panelId);
						var oPanel = Hemi.registry.service.getObject(oCurrent.panelId);
						var fLast = oCanvas.getTemporaryContext().globalAlpha;
						oCanvas.getTemporaryContext().globalAlpha = 0.5;
						oM = oCanvas.Image(oCurrent.image,oCurrent.x, oCurrent.y, "#00FF00","#000000");			
						oCanvas.getTemporaryContext().globalAlpha = fLast;
						oM.selectable = 0;
						oM = oCanvas.Image(
							oCurrent.image,
							oCanvas.getProperties().MouseTrackLeft - oCanvas.getProperties().MouseOffsetX,
							oCanvas.getProperties().MouseTrackTop - oCanvas.getProperties().MouseOffsetY,
							"#00FF00", "#000000"
						);
						oM.selectable = 0;
						var icoHeight = oPanel.getProperties().thumbHeight;//(oCurrent.icoType == "IMG" ?  properties.imgThumbHeight : properties.dirThumbHeight);
						if(oCurrent.referenceName && !oCurrent.noLabel) oCanvas.Text(scaleText(oCurrent.referenceName), oCanvas.getProperties().MouseTrackLeft - oCanvas.getProperties().MouseOffsetX, oCanvas.getProperties().MouseTrackTop - oCanvas.getProperties().MouseOffsetY + icoHeight, "#000000","#000000","8pt","Arial");
						
						//if(oPanel.getObjects().menu) this.paintMenu(oPanel, oCanvas, oCurrent, oPanel.getObjects().menu);
						
					}
				} 
	
				if(oDropShape && oDropShape.type == "Image" && (oDropShape.referenceType == "CTL" || oDropShape.referenceType == "GROUP")){
					var oM = oCanvas.Image(oDropShape.image, oDropShape.x, oDropShape.y, sDropColor, "#000000");
					if(oDropShape.referenceName && !oDropShape.noLabel){
						var oPanel = Hemi.registry.service.getObject(oDropShape.panelId);
						var icoHeight = oPanel.getProperties().thumbHeight;//(oDropShape.icoType == "IMG" ?  properties.imgThumbHeight : properties.dirThumbHeight);
						oCanvas.Text(scaleText(oDropShape.referenceName), oDropShape.x, oDropShape.y + icoHeight, "#000000","#000000","8pt","Arial");
					}
					oM.selectable = 0;
				}
			}
		});
		
		window.galleryView = Hemi.newObject("GalleryView","1.0",true,true,{
			object_destroy : function(){
				this.getCanvasController().destroy();
				Hemi.event.removeEventListener(window,"keydown",this._prehandle_keydown);
				Hemi.event.removeEventListener(window,"hashchange",this._prehandle_hash_change);
				Hemi.event.removeEventListener(window,"resize",this._prehandle_window_resize);
				Hemi.event.removeEventListener(window,"hashchange",this._prehandle_window_resize);
				Hemi.event.removeEventListener(window,"orientationchange",this._prehandle_window_resize);
			},
			object_create : function(){
				var _s = this.getProperties();
				Hemi.util.logger.addLogger(this, "Gallery View", "Gallery View", 232);
				if(!uwm.rule("IsLoggedIn")){
					this.log("User is not authenticated");
					this.destroy();
					return;
				}
				Hemi.event.addScopeBuffer(this);
				this.scopeHandler("window_resize",0,0,1);
				this.scopeHandler("hash_change",0,0,1);
				this.scopeHandler("keydown",0,0,1);
								
				instrumentVC(this);
				var v = this.view("gallery",{scaleHeight:1,scaleWidth:1,basePath:"~/"});
				v.panel("nav",{
					width:100,
					height:"boxHeight-50",
					top:0,
					left:0,
					thumbWidth:50,
					thumbHeight:50,
					thumbTree:1,
					smallIcon:1,
					vslot:1,
					suggestedCountOffset:3,
					itemType:"Group",
					itemPath:"GalleryHome/",
					//itemIcon:"48px-Crystal_Clear_mimetype_kmultiple.png",
					itemIcon:"48px-Crystal_Clear_filesystem_folder_grey.png",
					actions:[
					   //{action: "newGroup",vslot:0,label: "New Group",icon : "48px-Crystal_Clear_mimetype_misc.png"}
					   //,{action: "viewUnassigned",vslot:1,label: "Tasks",icon : "48px-Crystal_Clear_filesystem_folder_grey.png"}
					],
					
					menu:[
					   {action: "demote",icon : "48px-Crystal_Clear_action_forward.png"}
					]
				});
				v.panel("content",{
					width:"boxWidth-100",
					height:"boxHeight-50",
					top:0,
					left:100,
					thumbWidth:128,
					thumbHeight:128,
					thumbScroll:0,
					objectType:"Data",
					itemType:"Object",
					itemPath:"GalleryHome/",
					//itemIcon:"48px-Crystal_Clear_action_filenew.png",
					//itemIcon:"Crystal_Clear_action_filenew.png",
					itemIcon : "Crystal_Clear_mimetype_misc.png",
					itemIconImg : "Crystal_Clear_mimetype_image.png"
				});
				v.panel("controlPanel",{
					width:"boxWidth-100",
					height:"boxHeight-50",
					top:0,
					left:100,
					thumbWidth:128,
					thumbHeight:128,
					thumbScroll:0,
					actions:[
					 {action: "tagSearch",label:"Tag Search",icon:"Crystal_Clear_app_kfind.png"},
			         {action: "newGroup",label: "New Group",icon : "Crystal_Clear_action_filenew.png"},
			         {action: "dndUpload",label:"Drag/Drop Upload",icon:"Crystal_Clear_action_2uparrow.png"},
				     {action: "newImage",label: "New Image",icon : "Crystal_Clear_mimetype_image.png"},
				     {action: "openShare",label: "Sharing",icon : "Crystal_Clear_app_Login_Manager.png"},
				     {action: "openCache",label: "Cache",icon : "Crystal_Clear_app_database.png"},
				     {action: "openLog",label: "Log",icon : "Crystal_Clear_app_kexi.png"},
				     {action: "openDebug",label: "Debug",icon : "/AccountManagerExample/Media/Icons/Hemi_Logo_128x128.png"}
					]
				});
				v.panel("commands",{
					width:"boxWidth",
					height:50,
					top:"boxHeight-50",
					left:0,
					thumbWidth:50,
					thumbHeight:50,
					actions:[
					    {action:"navBack",small:1,slot:0,icon:"48px-Crystal_Clear_action_back.png"},
						{action:"navNext",small:1,slot:1,icon:"48px-Crystal_Clear_action_forward.png"},
						{action:"itemBack",small:1,slot:"mid-2",icon:"48px-Crystal_Clear_action_back.png"},
						{action:"controlPanel",small:1,slot:"mid",icon:"48px-Crystal_Clear_app_ksysguard.png"},
						{action:"itemNext",small:1,slot:"mid+2",icon:"48px-Crystal_Clear_action_forward.png"},
						{action:"deleteObject",small:1,slot:"slots - 3",icon:"48px-Crystal_Clear_filesystem_trashcan_empty.png"},
						{action:"logout",small:1,slot:"slots - 2",icon:"48px-Crystal_Clear_app_logout.png"},
						{action:"exit",small:1,slot:"slots - 1",icon:"48px-Crystal_Clear_app_shutdown.png"}
					]
	
				});
				v.panel("matte",{
					width:"boxWidth",
					height:"boxHeight",
					backgroundColor:"#000000",
					strokeColor:"#000000",
					top:0,
					left:0,
					thumbWidth:50,
					thumbHeight:50,
					/// Matte Config defines the control boundary regions
					///
					matteConfig : {
						left: 50,
						right: 50,
						top: 50,
						bottom: 50
					},
					actions:[]
				});			
				//this.view("tasks",{scaleHeight:1,scaleWidth:1}).panel("nav",{width:100}).panel("content",{left:100}).panel("control",{row:2,top:0,height:100}).panel("footer",{row:3,height:50,width:"33%"}).panel("footer2",{row:3,height:50,width:"33%"}).panel("footer3",{row:3,height:50,width:"33%"});
				//initializeVC(this);
				var gc = ctl.getObjects().galleryContainer;
				this.getCanvasController().getCanvasContainer().style.cssText = "position:absolute;top:" + (gc == document.body ? "0" : Hemi.css.getAbsoluteTop(gc)) + "px;left:" + (gc == document.body ? "0" : Hemi.css.getAbsoluteLeft(gc)) + "px;";
				
				this.switchView("gallery",["nav","content","commands"]);
				Hemi.event.addEventListener(window,"hashchange",this._prehandle_hash_change);
				Hemi.event.addEventListener(window,"resize",this._prehandle_window_resize);
				Hemi.event.addEventListener(window,"hashchange",this._prehandle_window_resize);
				Hemi.event.addEventListener(window,"orientationchange",this._prehandle_window_resize);
				Hemi.event.addEventListener(window,"keydown",this._prehandle_keydown);
				
			}, // end object create
			_handle_keydown : function(e){
				e = Hemi.event.getEvent(e);
				var bN = 0,bA = 0,bC = 0,bU = 0;
				
				switch(e.keyCode){
					case 39:
						bN = 1;
						bA = 1;
						//next(this.getCurrentViewPanel("content"),1);
						break;
					case 38:
						bU = 1;
						break;
					case 37:
					
						bN = 1;
						bA = 0;
						//back(this.getCurrentViewPanel("content"),1);
						break;
					case 27:
						closeImage();
						bC = 1;
						break;
					case 84:
						this.getProperties().tagMode = (!this.getProperties().tagMode);
						break;
					default:
						Hemi.log("Unhandled key code: " + e.keyCode);
						break;
				}
				
				if((!bN && !bU) || bC){
					Hemi.log("Unhandled combination");
					return 0;
				}
				
				var oPanel = galleryView.views()[0].panel("matte"),oShape;

				/// there should only be 2 shapes on the matte
				///
				if(oPanel.getObjects().shapes.length == 2 && (oShape = galleryView.getCanvas().getShapeById(oPanel.getObjects().shapes[0]))){
					/*
					Hemi.logError("Unexpected matte shape array");
					return 0;
				};
				*/
					/*
					var oShape = galleryView.getCanvas().getShapeById(oPanel.getObjects().shapes[0]);
					if(!oShape){
						Hemi.logError("Matte shape not found");
						return 0;
					}
					*/
					gestureMatteImage(oPanel, oShape.referenceType, oShape.referenceId, oShape,bN,bA);
				}
				else{
					if(bN){
						if(e.shiftKey){
							if(bA) this.navNext();
							else this.navBack();

						}
						else{
							if(bA) this.itemNext();
							else this.itemBack();
						}
					}
					else if(bU){
						this.log("cdup");
						this.cdup(this.views()[0].panel("nav"),0,this.views()[0].panel("nav").getObjects().currentDirectory.parentId,0);
					}
					else{
						this.log("otherwise");
					}
				}
				
			},
			_handle_hash_change : function(){
				this.log("New Hash: " + location.hash);
				//this.switchView("gallery",["nav","content","commands"]);
			},
			_handle_window_resize : function(){
				/*
				var v = this.getCurrentView(),aP;
				scaleView(v);
				aP = v.panels();
				for(var i = 0; i < aP.length;i++){
					if(aP[i].getProperties().visible){
						aP[i].repaint();
					}
				}
				*/
				var _s = this.getProperties();
				if(_s.resizing) window.clearTimeout(_s.resizing);
				_s.resizing = window.setTimeout(resetGalleryDimensions, 250);
		
			},
			clearViewPanels : function(v){
				for(var i = 0; i < v.getPanels().length;i++) clearPanel(v.getPanels()[i]);
			},
			switchView : function(sName,aP){
				var v = this.view(sName),_s = this.getProperties();
				ctl.getCanvas().Clear();
				///this.clearViewPanels(v);
				scaleView(v);
				//this.logDebug("Panels: " + v.panels().length);
				_s.currentView = sName;
				for(var i = 0; i < aP.length;i++){
					v.panel(aP[i]).repaint();
				}
				/*
				v.panels()[0].repaint();
				v.panels()[1].repaint();
				v.panels()[3].repaint();
				*/
			},
			getCurrentView : function(){
				return this.view(this.getProperties().currentView);
			},
			getCurrentViewPanel : function(n){
				var o = this.getCurrentView();
				if(!o) return;
				return o.panel(n);
			},
			rasterize : function(){
				ctl.getCanvas().Rasterize();
			},
			getCanvas : function(){
				return ctl.getCanvas();
			},
			getCanvasController : function(){
				return ctl;
			},
			isShowingControlPanel : function(){
				return this.getCurrentViewPanel("controlPanel").getProperties().visible;
			},
			controlPanel : function(sType, sId){
				var oP =  this.getCurrentViewPanel("controlPanel");
				var oP2 = this.getCurrentViewPanel("content");
				if(oP.getProperties().visible){
					clearPanel(oP);
					paintPanel(oP2);
				}
				else{
					clearPanel(oP2);
					paintPanel(oP);
				}
					
			},
			openLog : function(){
				Hemi.app.createWindow('Log Viewer','Templates/LogViewer.xml', 'LogViewer');
			},
			openShare : function(oTargetPanel, sType, sId, oShape){
				var d = this.getCurrentViewPanel("nav").getObjects().currentDirectory;
				var oProps = {viewType:d};
				var oW = Hemi.app.createWindow('Sharing','/AccountManagerExample/Forms/Sharing.xml','Sharing-' + d.id,0,0,oProps);
				if(oW){
					oW.setCanMinimize(0);
					oW.setCanMaximize(0);
			    	oW.resizeTo(450, 400);
			    	oW.setHideOnClose(0);
			    	Hemi.app.getWindowManager().CenterWindow(oW);
				}
			},
			openCache : function(oTargetPanel, sType, sId, oShape){
				var oW = Hemi.app.createWindow('Cache','/AccountManagerExample/Forms/CacheUtility.xml','Cache');
				if(oW){
					oW.setCanMinimize(0);
					oW.setCanMaximize(0);
			    	oW.resizeTo(450, 400);
			    	oW.setHideOnClose(0);
			    	Hemi.app.getWindowManager().CenterWindow(oW);
				}
			},
			tagSearch: function(){
				var oProps = {altSearch:1,openerId:this.getObjectId(),searchHandler:"doTagSearch"};
				var oW = Hemi.app.createWindow('Tag Search','/AccountManagerExample/Forms/TagSearch.xml','TagSearch',0,0,oProps);
				if(!oW) return;
				oW.resizeTo(450,400);
				oW.setHideOnClose(0);
				Hemi.app.getWindowManager().CenterWindow(oW);
			},
			doTagSearch : function(aT){
				var oP = this.getCurrentViewPanel("content"),oP2 = this.getCurrentViewPanel("nav");
				oP.getObjects().searchTags = aT;
				oP.getProperties().showTagSearch = 1;
				oP.getProperties().tagSearchCount = 0;
				
				oP2.getProperties().startIndex = 0;
				oP2.getProperties().totalCount = 0;
				oP2.getProperties().currentCount = 0;
				if(galleryView.isShowingControlPanel()){
					galleryView.controlPanel();
				}
				this.getCurrentView().panel("content").repaint();
			},
			openDebug : function(){
				var oW = Hemi.app.createWindow('Framework Profiler','Templates/FrameworkProfiler.xml','Profiler');
				if(!oW) return;
				oW.setCanMinimize(0);
				oW.setCanMaximize(0);
				oW.moveTo(0,0);
			},
			navNext : function(){
				next(this.getCurrentViewPanel("nav"));
			},
			navBack : function(){
				back(this.getCurrentViewPanel("nav"));
			},
			itemNext : function(b){
				next(this.getCurrentViewPanel("content"));
			},
			itemBack : function(b){
				back(this.getCurrentViewPanel("content"));
			},
			newStory : function(){
				//openWindow(this.getCurrentViewPanel("nav"),"Story");
				pickText(this,"New Story Name","createStory");
			},
			deleteObject : function(oTargetPanel, sType, sId, oShape){
				var oP = Hemi.registry.service.getObject(oShape.panelId);
				deleteObject(oP, sType, sId, oShape);
			},
			newGroup : function(){
				pickText(this,"New Group Name","createGroup");
			},
			cd : function(oPanel,sType, sId, oShape){
				changeDirectory(sId, oPanel,["content"]);
			},
			cdup : function(oPanel,sType, sId, oShape){
				changeDirectory(sId, oPanel,["content"],1);
			},
			getCurrentGroup : function(){
				return this.getCurrentView().panel("nav").getObjects().currentDirectory;
			},
			createGroup : function(s){
				var d = this.getCurrentViewPanel("nav").getObjects().currentDirectory;
				var b = accountManager.getCreatePath(d.path + "/" + s);
				if(b){
					window.uwmServiceCache.clearServiceCache("Group");
					this.getCurrentViewPanel("nav").repaint();
				}
				this.logDebug("Create group: " + s + " " + (b ? true : false));
			},
			/*
			createStory : function(sName){
	
				var b = accountManager.addTask(sName, "", "UNKNOWN", 0,(new Date()),(new Date()),0,0, 0, 0, 0, 0, 0, 0, 0, this.getCurrentViewPanel("nav").getObjects().currentDirectory);
				if(b){
					this.getCurrentViewPanel("nav").repaint();
				}
				this.logDebug("Create story: " + sName + " " + (b ? true : false));
			},
			*/
			newImage : function(){
				var oP = this.getCurrentViewPanel("nav");
				openWindow(oP, "Data", 0, showDataForm);
			},
			dndUpload : function(){
				var oP = this.getCurrentViewPanel("nav");
				//openWindow(oP, "DataDnd", 0, showDNDForm);
				var vProps = {openerId:this.getObjectId()};
				Hemi.app.createWindow("DataDnD", "/AccountManagerExample/Forms/DataDnd.xml", "DataDnD", 0, 0, vProps, showDNDForm);
			},
			viewObject : function(oTargetPanel,sType, sId, oShape){
				var oP = Hemi.registry.service.getObject(oShape.panelId);
				this.log("Viewing " + sId);
				viewObject(oP, sType, sId, oShape);
			},
			gestureMatteImage : function(oTargetPanel, sType, sId, oShape){
				var oP = Hemi.registry.service.getObject(oShape.panelId);
				gestureMatteImage(oP, sType, sId, oShape);
			},
			logout : function(){
				window.uwm.logout();
				window.uwm.operation("ContinueWorkflow");
				this.destroy();
			},
			exit : function(){
				this.destroy();
			},
			alignViews : function(){
				resetGalleryDimensions();
			}

	
		});
		
		function resetGalleryDimensions(){
			
			var v = galleryView.getCurrentView(),_s = galleryView.getProperties(),aP;
			
			var gc = ctl.getObjects().galleryContainer;
			var sCss = "position:absolute;top:" + (gc == document.body ? "0" : Hemi.css.getAbsoluteTop(gc)) + "px;left:" + (gc == document.body ? "0" : Hemi.css.getAbsoluteLeft(gc)) + "px;";
			Hemi.log('CSS=' + sCss);
			galleryView.getCanvasController().getCanvasContainer().style.cssText = sCss;

			scaleView(v);
			aP = v.panels();
			for(var i = 0; i < aP.length;i++){
				if(aP[i].getProperties().visible){
					aP[i].repaint();
				}
			}
			_s.resizing = 0;
			
		}

		
		function deleteObject(oPanel, sType, sId, oShape){
			var o = getObjectById(oPanel, sType, sId,0),ot = getObjectType(oPanel);
			if(!o){
				ctl.logError("Invalid object for " + sId + " type " + ot);
				return;
			}
			var oObj = o;
			window[uwm.getApi(ot)]["delete" + ot](o,{
				hemiSvcCfg:1,
				async:1,
				handler:function(s, v){
					if(typeof v.json == "boolean" && v.json){
						ctl.log("Deleted " + oObj.name);
						oPanel.repaint();
					}
					else{
						ctl.logWarning("Unable to delete " + oObj.name);
					}
				}
			});
			
		}
		/// TODO: key-based advance overloaded with the gesture. This should be broken up 
		/// 
		function gestureMatteImage(oMattePanel, sType, sId, oMatte, bKeyAdvance, bNext){
			if(!oMatte) return;
			var _s = oMattePanel.getProperties(),
				_p = oMattePanel.getObjects(), 
				bP =0,
				iML = ctl.getCanvas().getProperties().MouseTrackLeft,
				iMT = ctl.getCanvas().getProperties().MouseTrackTop,
				vM,
				oContentPanel = Hemi.registry.service.getObject(oMatte.contentPanelId),
				_cs,
				_cp,
				iTI
			;
			if(!oContentPanel){
				ctl.log("Invalid content panel id: " + oMatte.contentPanelId);
				return;
			}
			_cs = oContentPanel.getProperties();
			_cp = oContentPanel.getObjects();
			vM = _s.matteConfig;
			/// Left Pane
			ctl.logDebug("Gesture idx " + oMatte.referenceIndex + " at " + iML + ", " + iMT + " within " + oMattePanel.width() + "x" + oMattePanel.height());
			//ctl.log("Panel: " + oMattePanel.getObjectId() + " <> " + oMatte.panelId);
			iTI = oMatte.referenceIndex;
			
			//_s.iconStartIndex >= _s.totalIconCount
			if((bKeyAdvance && !bNext) || (!bKeyAdvance && iML <= vM.left)){
	
				if((iTI-1) < 0 && _cs.startIndex > 0){
					//_s.showImage = 0;
					//this.imageBack(true);
					ctl.log("Paginate back");
					back(oContentPanel, 1);
					iTI = _cp.currentList.length - 1;
					bP = 1;
				}
				else if(iTI > 0){
					iTI--;
					bP = 1;
				}
				ctl.logDebug("Gesture Left " + bP + " to " + iTI);
			}
			/// Right Pane
			else if( (bKeyAdvance && bNext) || (!bKeyAdvance && iML >= (oMattePanel.width() - vM.right))){
				
				if((iTI+1) >= _cp.currentList.length && (iTI+1) < _cs.totalCount){
					//_s.showImage = 0;
					ctl.log("Paginate Forward");
					// because " + (iTI+1) + " >= " + _cp.currentList.length + " && " + (iTI+1) + " < " + _cs.totalCount);
					next(oContentPanel,1);
					iTI = 0;
					bP = 1;
				}
				else if((iTI+1) < _cp.currentList.length){
					iTI++;
					bP = 1;
				}
				ctl.logDebug("Gesture Right " + bP + " to " + iTI);
			}
			/// Top Pane
			else if(iMT <= vM.top){
				ctl.logDebug("Gesture Top");
			}
			/// Bottom Pane
			else if(iMT >= (oMattePanel.height() - vM.bottom)){
				ctl.logDebug("Gesture Bottom");
			}
			/// Center Pane
			else{
				ctl.logDebug("Gesture Center");
				closeImage();
			}
			if(bP){
				viewObject(oContentPanel,oMatte.referenceType,_cp.currentList[iTI].id,iTI);
			}
		}
		function closeImage(){
			galleryView.switchView("gallery",["nav","content","commands"]);
		}
		
		/// At the moment, viewObject assumes it's a matte object (full view)
		/// Need to fix this - it was written originally as an image gallery
		///
		function viewObject(oContentPanel, sType, sId, iViewIndex){
			var o = getObjectById(oContentPanel, sType, sId,0),_o = oContentPanel.getObjects(),_s = oContentPanel.getProperties(), ctl = oContentPanel.getObjects().view.getObjects().controller, mP;
			if(!o || o==null){
				ctl.logError("Invalid object for id " + sId);
				return;
			}
			if(typeof iViewIndex == "object") iViewIndex = iViewIndex.referenceIndex;
			if(ctl.getProperties().viewName != "matte") ctl.switchView("gallery",["matte"]);
			mP = ctl.getCurrentView().panel("matte");
			_s.showImage = 1;
			_s.showImageId = sId;
			_s.showImageIndex = iViewIndex;
			//_s.showControlPanel = 0;
			//this.clearView(1);
			var img = new Image();
			
			
			
			var oG = ctl.getCanvas();
			var oPanel = oContentPanel;
			
			//oG.Rect(0, 0, mP.width(), mP.height(), "#000000","#000000");
	
			img.onload = function(){
	
				var iMaxWidth = mP.width();
				var iMaxHeight = mP.height();
				if(img.width > iMaxWidth || img.height > iMaxHeight){
					var iS1 = (iMaxWidth / img.width);
					var iS2 = (iMaxHeight / img.height);
					var iW = (iS1 * img.width);
					var iH = (iS1 * img.height);
					if(iMaxHeight < iH){
						iW = (iS2 * img.width);
						iH = (iS2 * img.height);
					}
					img.width = iW;
					img.height = iH;			
				}
				var iX = (iMaxWidth - img.width) / 2;
				var iY = (iMaxHeight - img.height) / 2; 
				galleryView.logDebug("View image: " + o.name + " with dimensions " + img.width + "x" + img.height + " at " + iX + ", " + iY);
				
				clearPanel(mP);
				
				var oR = oG.Rect(0, 0, mP.width(), mP.height(), "#000000","#000000");
				oR.referenceIndex = iViewIndex;

				
				var oShape = oG.Image(img, iX, iY);
				oShape.action = "gestureMatteImage";
				oShape.referenceName = o.name;
				oShape.referenceId = o.id;
				oShape.referenceType = "OBJECT";
				oShape.matteId = oR.id;
				oShape.noLabel = 1;
				oShape.groupId = o.group.id;
				oShape.referenceIndex = oR.referenceIndex;
				oShape.panelId = mP.getObjectId();
				oShape.contentPanelId = oPanel.getObjectId();
				mP.getShapes().push(oShape.id);
						
				oR.noLabel = 1;
				oR.action = "gestureMatteImage";
				oR.referenceType = "MATTE";
				oR.referenceId = oShape.id;
				oR.panelId = mP.getObjectId();
				oR.contentPanelId = oPanel.getObjectId();
				mP.getShapes().push(oR.id);
				
				ctl.getCanvas().Rasterize();
				
				if(galleryView.getProperties().tagMode){
					var aT = accountManager.listTagsFor(o);
					for(var i = 0; i < aT.length;i++){
						oG.Text(aT[i].name, 5, 5 + (25*i),"#FFFFFF","#FFFFFF","12pt","Arial");
					}
				}
			}
			
			img.src = "data:" + o.mimeType + ";base64," + o.dataBytesStore;
	
	
		}
		function showDNDForm(oW){
			if(!oW.GetElementByRID) return;
			oW.setHideOnClose(0);
			oW.setTitle("Drag-n-Drop");
			oW.resizeTo(290,350);
			var oP = galleryView.getCurrentViewPanel("nav");
			oW.GetElementByRID("path").value = oP.getObjects().currentDirectory.path;
		}
		function showDataForm(oW){
			if(!oW.GetElementByRID) return;
			var oP = galleryView.getCurrentViewPanel("nav");
			oW.GetElementByRID("dataTypeContainer").style.display = "none";
			oW.GetElementByRID("mimeType").value = "binary";
			oW.GetElementByRID("path").value = oP.getObjects().currentDirectory.path;
			oW.changeDataType();
		}
		
		
		function refreshGroupType(o,sType,sPath, bSkipDraw){
			var _o = o.getObjects(), _s = o.getProperties(), oM, _no = o.getObjects().view.panel("nav").getObjects(),iter = 0;
			if(!_no.currentDirectory) _no.currentDirectory = accountManager.getCreatePath(sPath);
			if(!_no.baseGroup) _no.baseGroup = _no.currentDirectory;

			if(!bSkipDraw && sType == 'Group'){
				/// If not in the base group
				if(_no.currentDirectory.id != _no.baseGroup.id){
					/// And the parent group is not the base group
					if(_no.currentDirectory.parentId != _no.baseGroup.id){
						paintVerticalSlottedItem(o,_no.baseGroup,"cd",0,iter++);
					}
					paintVerticalSlottedItem(o,accountManager.getGroupById(_no.currentDirectory.parentId),"cdup",0,iter++);
				}
				//paintGroup(_o[sProp],0,"48px-Crystal_Clear_filesystem_folder_grey_open.png");
				paintVerticalSlottedItem(o,_no.currentDirectory,"cd",0,iter++);
			}
			var sObjType = (_s.objectType ? _s.objectType : sType);
			var aSub = [];
			var oCP;
			if(sType == 'Object' && (oCP = o.getObjects().view.panel("content")).getProperties().showTagSearch){
				var oR = new org.cote.beans.dataTagSearchRequest();
				oR.tags = oCP.getObjects().searchTags;
				oR.startRecord = _s.startIndex;
				oR.recordCount = _s.suggestedCount - (_s.suggestedCountOffset ? _s.suggestedCountOffset : 0);
				oR.paginate = true;
				oR.populateGroup = true;
				if(!oCP.getProperties().tagSearchCount) oCP.getProperties().tagSearchCount = uwmServices.getService("Tag").countTags(oR);
				_s.totalCount = oCP.getProperties().tagSearchCount;
				aSub =  uwmServices.getService("Tag").listByTags(oR);
			}
			else{
				_s.totalCount = accountManager["count" + sObjType + "s"](_no.currentDirectory.path);
				aSub = accountManager["list" + sObjType + "s"](_no.currentDirectory.path,_s.startIndex,_s.suggestedCount - (_s.suggestedCountOffset ? _s.suggestedCountOffset : 0));
			}
			
			_o.currentList = [];
			///ctl.logDebug("Painting " + aSub.length + " items of " + _s.totalCount + " for " + _no.currentDirectory.path + " from " + _s.startIndex + " to " + _s.suggestedCount);
			for(var i = 0; aSub && i < aSub.length;i++){
				if(sType == 'Group' && aSub[i].name.match(/^\.thumbnail$/gi)) continue;
				_o.currentList.push(aSub[i]);
				if(bSkipDraw) continue;
				if(_s.vslot){
					paintVerticalSlottedItem(o,aSub[i],0,1,i);
					iter++;
				}
				else{
					paintItem(o,aSub[i],i);
					iter++;
				}
			}
			
			_s.rasterTotal += iter;
		}
		function isRendering(oPanel){
			var _s = (oPanel ? oPanel : galleryView.getCurrentView().panel("content")).getProperties();
			return (_s.rasterCount != _s.rasterTotal);
		}
		function checkRaster(oPanel){
			var _s = oPanel.getProperties();
			if(_s.rasterCount == _s.rasterTotal){
				ctl.log("Raster " + oPanel.access_name + " with " + _s.rasterCount + ":" + _s.rasterTotal);
				ctl.getCanvas().Rasterize();
			}
		}
		
		function changeDirectory(sId,p, aF, bSkipDirReset){
			var _o = p.getObjects(),_p = p.getProperties();
			var o = accountManager.getGroupById(sId);
			if(!o || o.id == _o.currentDirectory) return;
			_o.currentDirectory = o;
			ctl.log("Changing directory to " + o.path);

			galleryView.getCurrentViewPanel("content").getProperties().showTagSearch = 0;
			delete galleryView.getObjects().searchTags;
			
			if(galleryView.isShowingControlPanel()){
				galleryView.controlPanel();
			}
			
			//if(!bSkipDirReset){
				//ctl.log("Resetting primary pagination");
				/// For primary panel, move pagination markers back to beginning
				///
				_p.startIndex = (_o.groupState[o.path] ? _o.groupState[o.path] : 0);
				_p.totalCount = 0;
				_p.currentCount = 0;
			//}

			ctl.getCanvas().clearEventTrack();
			p.repaint();
			
			for(var i = 0; i < aF.length;i++){
				var oP = galleryView.getCurrentView().panel(aF[i]);
				if(!oP) ctl.logError("Invalid panel: '" + aF[i] + "'");
				else{
					//ctl.log("Repaint '" + aF[i] + "'");
					ctl.logDebug("Resetting secondary pagination");
					/// For reference panels, move pagination markers back to beginning
					/// Note: the skip doesn't apply here
					///
					oP.getProperties().startIndex = (oP.getObjects().groupState[o.path] ? oP.getObjects().groupState[o.path] : 0);;
					oP.getProperties().totalCount = 0;
					oP.getProperties().currentCount = 0;
					oP.repaint();
				}
			}
			Hemi.message.service.publish("onchangedirectory", galleryView);
		}
		
		function next(p, b){
			var _s = p.getProperties(), iCount;
			iCount = _s.suggestedCount  - (_s.suggestedCountOffset ? _s.suggestedCountOffset : 0);
			/*
			if(_s.totalCount > 0 && (_s.startIndex + iCount) < _s.totalCount){
				_s.startIndex += iCount;
				repaintPanel(p,b);
			}
			*/
			
			_s.startIndex += iCount;
			if(_s.totalCount > 0 && _s.startIndex >= _s.totalCount){
				_s.startIndex = _s.totalCount - iCount;
			}
			if(_s.startIndex < 0) _s.startIndex = 0;
			p.getObjects().groupState[galleryView.getCurrentGroup().path] = _s.startIndex;
			repaintPanel(p, b);
			
		}
		function back(p, b){
			var _s = p.getProperties(),iCount;
			iCount = _s.suggestedCount  - (_s.suggestedCountOffset ? _s.suggestedCountOffset : 0);
			_s.startIndex -= iCount;
			if(_s.startIndex < 0) _s.startIndex = 0;
			p.getObjects().groupState[galleryView.getCurrentGroup().path] = _s.startIndex;
			repaintPanel(p, b);
		}
		
		function repaintPanel(p,b){
			if(!b) clearPanel(p);
			paintPanel(p,b);
		}
		
		function clearPanel(p){
			var _o = p.getObjects(), _s = p.getProperties(), oM,aPS=p.getShapes(),oCvs = ctl.getCanvas();
			_o.view.getObjects().controller.logDebug("Clear Panel " + _s.left + ", " + _s.top + " X " + _s.width + ", " + _s.height + " with " + aPS.length + " shapes");
			for(var i = 0; i < aPS.length;i++){
				var oS = oCvs.getShapeById(aPS[i]);
				if(oS){
					oCvs.removeShape(oS);
				}
			}
			aPS.length = 0;
			
			ctl.getCanvas().ClearTempCanvas();
			
			oCvs.setTemporaryContextConfig(ctl.getCanvas().getConfigByName("NoShadow"));
			oCvs.setContextConfig(ctl.getCanvas().getConfigByName("NoShadow"));
			//oM = oCvs.Rect(_s.left, _s.top, _s.width, _s.height, "#FFFFFF","#FF0000");
			var bC = (_s.backgroundColor ? _s.backgroundColor : "#FFFFFF");
			var sC = (_s.strokeColor ? _s.strokeColor : "#FFFFFF");
			oM = oCvs.Rect(p.left(), p.top(), p.width(), p.height(), bC,sC);
			oM.selectable = 0;
			_s.visible = 0;
			_s.rasterCount = 0;
			_s.rasterTotal = 0;
			ctl.getCanvas().Rasterize();
		}
		function paintPanel(p, b){
			var _o = p.getObjects(), _s = p.getProperties(),oA;
			_s.currentCount = 0;
			_o.view.getObjects().controller.logDebug("Paint Panel " + _s.left + ", " + _s.top + " X " + _s.width + ", " + _s.height);
			_s.visible = 1;

			_s.rasterTotal = 0;
			_s.rasterCount = 0;
			

			if(!b){
				ctl.getCanvas().setTemporaryContextConfig(p.getCanvas().getConfigByName("NoShadow"));
				ctl.getCanvas().setContextConfig(ctl.getCanvas().getConfigByName("NoShadow"));
			}
			if(!b && _o.actions){
				var iSlots = Math.floor(p.width() / _s.thumbWidth);
				var iMid = Math.floor(iSlots/2);
				var iVSlots = Math.floor(p.height() / _s.thumbHeight);
				var iVMid = Math.floor(iVSlots/2);		

				for(var i = 0; i < _o.actions.length;i++){
					oA = _o.actions[i];
					///ctl.log("Paint Panel Action " + oA.action);
					if(typeof oA.slot != "undefined"){
						var iSlot = 0;
						if(typeof oA.slot == "number"){
							iSlot = oA.slot;
						}
						else{
							iSlot = eval(oA.slot.replace(/slots/,iSlots).replace(/mid/,iMid));
						}
						paintSlottedAction(p,oA,iSlot);
					}
					else if(typeof oA.vslot != "undefined"){
						var iVSlot = 0;
						if(typeof oA.vslot == "number"){
							iVSlot = oA.vslot;
						}
						else{
							iVSlot = eval(oA.vslot.replace(/slots/,iVSlots).replace(/mid/,iVMid));
						}
						paintVerticalSlottedItem(p,oA,iVSlot,-1);
					}
					else if(oA.label){
						paintAction(p,oA);
					}
				}
				_s.rasterTotal += _o.actions.length;
			}
			if(_s.itemType && _s.itemPath){
				//_o.view.getProperties().basePath + _s.itemPath
				refreshGroupType(p,_s.itemType,_o.view.getProperties().basePath + _s.itemPath,b);
			}
			checkRaster(p);
			if(!b) _s.visible = 1;
		}
		
		function paintItem(p,o,i,b){
			if(!o) return;
			
			var _s = p.getProperties(),_o=p.getObjects(),oP, g = (o.nameType == 'GROUP' ? o : o.group), _no = p.getObjects().view.panel("nav").getObjects();
			
			//ctl.log("Paint item " + o.name + " in " + g.path);
			
			var sIcoSrc = _o.view.getProperties()["icon" + (_s.smallIcon ? "Small" : "Large")+ "Base"] + _s.itemIcon;
			if(!b && o.mimeType && o.mimeType.match(/^image/gi)){
				sIcoSrc = _o.view.getProperties()["icon" + (_s.smallIcon ? "Small" : "Large")+ "Base"] + _s.itemIconImg;
				if(g.id == _no.currentDirectory.id) g = _no.currentDirectory;
				else if(g && !g.populated) g = accountManager.getGroupById(g.id);
				if(g.path){
					sIcoSrc = "/AccountManager/Thumbnail/" + accountManager.getOrganizationDotPath() + "/Data" + g.path + "/" + o.name + "/" + _s.thumbWidth + "x" + _s.thumbHeight;
				}
			}
	
			// # of icons per row
			var iMaxX = parseInt(p.width() / (_s.thumbWidth + 10));
			var iMaxY = parseInt(p.height() / (_s.thumbHeight + 10));
			var slotX = p.left() + (_s.currentCount % iMaxX)*(_s.thumbWidth+10);
			var slotY = p.top() + parseInt(_s.currentCount / iMaxX)*(_s.thumbHeight+10);
			
			paintPanelObject(p,o,0,i,sIcoSrc, o.name, o.id, p.getProperties().itemType.toUpperCase(),"viewObject",slotX, slotY, 1, 1,1)
	
		}
		
		
		function paintVerticalSlottedItem(p,o,a,z,i){
			if(!o) return;
			var _s = p.getProperties(),_o = p.getObjects(),s,sIcoSrc,oP,sB,_no = p.getObjects().view.panel("nav").getObjects();
			sB =  _o.view.getProperties()["icon" + (_s.smallIcon ? "Small" : "Large")+ "Base"];
			s = (o.icon ? o.icon : _s.itemIcon);
			if(_no.baseGroup && o.id == _no.baseGroup.id){
				sIcoSrc =  sB + "48px-Crystal_Clear_filesystem_folder_home2.png";
			}
			else if(a && a == "cdup") sIcoSrc = sB + "48px-Crystal_Clear_action_1uparrow.png";
			else if(!z) sIcoSrc = sB + "48px-Crystal_Clear_filesystem_folder_grey_open.png";
			else if(!s) sIcoSrc = sB + "48px-Crystal_Clear_filesystem_folder_grey.png";
			else sIcoSrc = (s.match(/\//) ? "" : sB) + s;
			//ctl.log("Paint vertical slotted item: " + o);
			//if(_s.itemType == 'Group' && o.id == _o.lifecycleHome.id){

			var slotX = (z * _s.thumbWidth);
			var slotY = _s.currentCount * (_s.thumbWidth + 10);
			if(!a) a = "cd";
			if(typeof o.action == "string"){
				paintPanelObject(p,o,z,i,sIcoSrc, o.label, -1, 'CTL',o.action,slotX, slotY, 1, 1,0);
			}
			else{
				paintPanelObject(p,o,z,i,sIcoSrc, o.name, o.id, _s.itemType.toUpperCase(),a,slotX, slotY, 1, 1,1);
			}
			
		}
		
		function paintAction(p, a){
			var _s = p.getProperties(),_o=p.getObjects();
			var sIcoSrc = (a.icon.match(/\//) ? "" : _o.view.getProperties()["icon" + (a.small ? "Small" : "Large") + "Base"]) + a.icon;
			var iMaxX = parseInt(p.width() / (_s.thumbWidth + 10));
			var iMaxY = parseInt(p.height() / (_s.thumbHeight + 10));
			var slotX = p.left() + (_s.currentCount % iMaxX)*(_s.thumbWidth+10);
			var slotY = parseInt(_s.currentCount / iMaxX)*(_s.thumbHeight+10);
	
			paintPanelObject(p,0,a,-1,sIcoSrc, a.label,-1,'CTL',a.action,slotX, slotY, 1,1,0);
		}
		function paintSlottedAction(p,a,i){
			var _s = p.getProperties(),_o = p.getObjects();
			var slotX = i * _s.thumbWidth;
			var sIcoSrc = _o.view.getProperties()["icon" + (a.small ? "Small" : "Large") + "Base"] + a.icon;
			
			paintPanelObject(p, 0, a, i, sIcoSrc, 0,-1,"CTL",a.action,slotX,p.top() + 1, 0, 1,0);
		}
		
		function paintPanelObject(oPanel,oObj,oAct,iIndex,sIco, sLbl, iRefId, sRef,sAct,iX, iY, bText, bHover,bDrag,bRepaint){
			var _s = oPanel.getProperties();
			var img = new Image();
			var oG = ctl.getCanvas();
			var idx = iIndex;
			if(!bRepaint) _s.currentCount++;
			//ctl.log("Pre-Paint Object: " + sAct + " " + sLbl + " " + sRef + " "  + bText + ":" + bHover + ":" + bDrag);
			img.onload = function(){
				if(img.width == 0 || img.heigth == 0){
					if(!bRepaint){
						sIco = oPanel.getObjects().view.getProperties()["icon" + (_s.smallIcon ? "Small" : "Large")+ "Base"];
						paintPanelObject(oPanel, oObj, oAct, iIndex, sIco, sLbl, iRefId, sRef, sAct, iX, iY, bText, bHover, bDrag, 1);
					}
					return;
				}
				var oShape = oG.Image(img, iX, iY);
				oShape.action = sAct;
				oShape.panelId = oPanel.getObjectId();
				oShape.referenceType = sRef;
				oShape.referenceIndex = idx;
				//oShape.objectType = _s.objectType;
				if(iRefId >= 0) oShape.referenceId = iRefId;
				oShape.hover = bHover;
				oShape.drag = bDrag;
				if(sLbl) oShape.referenceName = sLbl;
				
				if(bText) oG.Text(scaleText(sLbl), iX, iY + _s.thumbHeight, "#000000","#000000","8pt","Arial");
				//oG.Rasterize();
				oPanel.getShapes().push(oShape.id);
				oPanel.getProperties().rasterCount++;
				checkRaster(oPanel);
				//ctl.log("Paint Object: " + sAct + " " + sLbl + " " + sRef + " "  + bText + ":" + bHover + ":" + bDrag);
			};
			img.onerror = function(){
				if(!bRepaint){
					sIco = oPanel.getObjects().view.getProperties()["icon" + (_s.smallIcon ? "Small" : "Large")+ "Base"];
					paintPanelObject(oPanel, oObj, oAct, iIndex, sIco, sLbl, iRefId, sRef, sAct, iX, iY, bText, bHover, bDrag, 1);
					return;
				}
				oPanel.getProperties().rasterCount++;
				checkRaster(oPanel);
			};
			img.src = sIco;
		}
		
		function initializeCanvasController(o)
		{
			Hemi.util.logger.addLogger(o, "Example Interface Controller", "Example UIC", 231);
			o.logDebug("Example UIC Initializing");
			var _s = o.getProperties();
			var _o = o.getObjects();
			_o.galleryContainer = (typeof g_gallery_container == "object" ? g_gallery_container : document.body);
			if(!_o.cvs_container){
				_o.cvs_container = document.createElement("div");
				_o.galleryContainer.appendChild(_o.cvs_container);
			}
			_o.cvs_container.style.position = "relative";
	
			_o.canvas = Hemi.graphics.canvas.newInstance(_o.cvs_container);
	
			var oCfg = _o.canvas.newContextConfig("NoShadow");
			oCfg.fillStyle = "#FFFFFF";
		    oCfg.strokeStyle = "#FFFFFF";
		    oCfg.shadowColor = "#FFFFFF";
		    oCfg.shadowOffsetX = 0;
		    oCfg.shadowOffsetY = 0;
		    oCfg.shadowBlur = 0;
		    _o.canvas.setTemporaryContextConfig(oCfg);
		    
		    oCfg = _o.canvas.newContextConfig("DropBlueShadow");
		    oCfg.fillStyle = "#FFFFFF";
		    oCfg.strokeStyle = "#FFFFFF";
		    oCfg.shadowColor = "#AAAAFF";
		    oCfg.shadowOffsetX = 5;
		    oCfg.shadowOffsetY = 5;
		    oCfg.shadowBlur = 1;
		    
		    oCfg = _o.canvas.newContextConfig("DropGreenShadow");
		    oCfg.fillStyle = "#FFFFFF";
		    oCfg.strokeStyle = "#FFFFFF";
		    oCfg.shadowColor = "#AAFFAA";
		    oCfg.shadowOffsetX = 5;
		    oCfg.shadowOffsetY = 5;
		    oCfg.shadowBlur = 1;
		    
		}
		
		function scaleView(o, w, h){
			var _s = o.getProperties(),_o = o.getObjects(),aP=o.getPanels(),oP,_sp;
			_s.boxWidth = boxWidth();
			_s.boxHeight = boxHeight();
			if(!w) w = _s.boxWidth * _s.scaleWidth;
			if(!h) h = _s.boxHeight * _s.scaleHeight;
			_s.width = w;
			_s.height = h;
			for(var i = 0; i < aP.length;i++){
				oP = aP[i];
				_sp = oP.getProperties();
				if(_sp.thumbTree){
					_sp.suggestedCount = Math.floor(oP.height() /( _sp.thumbHeight + 10));
				}
				else if (_sp.thumbScroll){
					_sp.suggestedCount = Math.floor(oP.width() / (_sp.thumbWidth+10));
				}
				else{
					
					_sp.suggestedCount = Math.floor(oP.height() / (_sp.thumbHeight+10)) * Math.floor(oP.width() / (_sp.thumbWidth+10));
					//ctl.log(_sp.suggestedCount + " from " + oP.width() + " x " + oP.height() + " / " + _sp.thumbWidth + "x" + _sp.thumbHeight);
				}
			}
			ctl.getCanvas().Resize(w + "px",h + "px");
		}
	
		function copyShapeProperties(s, t){
			t.id = s.id;
			t.panelId = s.panelId;
			t.contentPanelId = s.panelId;
			t.referenceName = s.referenceName;
			t.referenceType = s.referenceType;
			//t.objectType = s.objectType;
			t.referenceId = s.referenceId;
			t.referenceIndex = s.referenceIndex;
			t.icoType = s.icoType;
			t.action = s.action;
			t.path = s.path;
			t.drag = s.drag;
			t.hover = s.hover;
			t.noLabel = s.noLabel;
			t.matteId = s.matteId;
			t.selectable = s.selectable;
			
		}
		function boxWidth(o){
			if(!o) o = ctl.getObjects().galleryContainer;
			return (o == document.body ? (typeof window.innerWidth == "number" ? window.innerWidth : document.documentElement.clientWidth) : o.clientWidth);
		}
		function boxHeight(o){
			if(!o) o = ctl.getObjects().galleryContainer;
			return (o == document.body ? (typeof window.innerHeight == "number" ? window.innerHeight : document.documentElement.clientHeight) : o.clientHeight);
		}
		function scaleText(s){
			var iLabelWidth = 12,b,i,x="";
			
			if(s.length <= iLabelWidth) return s;
		
			if((i = s.lastIndexOf(".")) >= (s.length - iLabelWidth)){
				x = s.substring(i,s.length);
				s=s.substring(0,i);
			}
			return s.substring(0,iLabelWidth - x.length - 3) + "..." + x;
		}
		function reparentGroup(oPanel,s, t){
			ctl.logDebug("Reparent " + s.name + " to " + t.name);
			s.parentId = t.id;
			accountManager.updateGroup(s);
			accountManager.clearGroupCache();
			repaintPanel(oPanel);
		}
		function reparentObject(s, t){
			ctl.log("Reparent " + s.name + " (" + s.nameType + ") to " + t.name + " (" + t.nameType + ")");
			if(s.nameType.match(/^DATA/)){
				s.group = t;
				s.detailsOnly = true;
				delete s.dataBytesStore;
			}
			else if(s.nameType.match(/^GROUP$/)){
				s.parentId = t.id;
			}
			else{
				s.groupId = t.id;
			}
			accountManager["update" + s.nameType.substring(0,1) + s.nameType.substring(1,s.nameType.length).toLowerCase()](s);
			uwmServiceCache.clearCache(s.nameType);
			uwmServiceCache.clearCache(t.nameType);
			galleryView.getCurrentView().panel("nav").repaint();
			galleryView.getCurrentView().panel("content").repaint();
			//repaintPanel(oPanel);
		}
		function getObjectType(oPanel){
			var _s = oPanel.getProperties();
			return (_s.objectType ? _s.objectType : _s.itemType);
		}
		function getObjectById(oPanel, sType, sId, oShape){
			var ot = getObjectType(oPanel),f;
			f = window[uwm.getApi(ot)]["get" + ot + "ById"];
			if(!f){
				ctl.logError("Invalid reference type '" + sType + "' to retrieve object: '" + ot + "'");
				return 0;
			}
			return f(sId);
		}
		
		function openObject(oTargPanel,sType, sId, oShape){
			var oPanel = Hemi.registry.service.getObject(oShape.panelId);
			var o = getObjectById(oPanel,sType, sId, oShape), _o = galleryView.getCurrentView().panel("content").getObjects();
			var oProps = {openerId:oPanel.getObjectId(),picker:0,viewType:o};
			if(_o.viewWindow && !_o.viewWindow.getIsClosed()) _o.viewWindow.Close();
			var sType = o.nameType.substring(0,1) + o.nameType.substring(1,o.nameType.length).toLowerCase() ;
			var oW = Hemi.app.createWindow(o.name, uwm.getApiTypeView(sType) + "/Forms/" + o.nameType.substring(0,1) + o.nameType.substring(1,o.nameType.length).toLowerCase() + ".xml", "View-" + o.id, 0, 0, oProps);
		    if (oW) {
		    	oW.resizeTo(450, 400);
		    	Hemi.app.getWindowManager().CenterWindow(oW);
		    	// Destroy the window when closed
		    	//
		    	oW.setHideOnClose(0);
		    	oW.setCanMinimize(0);
		    	oW.setCanMaximize(0);
		    	oW.setCanResize(0);
		    	_o.viewWindow = oW;
		    }
	
		}
		
		function instrumentVC(o){
			Hemi.object.addObjectAccessor(o,"view");
			o.views = function(){return this.getViews();};
			o.view = function(s,cfg){
				var v;
				if((v = this.getViewByName(s))) return v;
				if(!cfg) cfg = {};
				v = Hemi.newObject("View " + s,"1.0",true,true,{
					object_create : function(){
						var _s = this.getProperties(),_o = this.getObjects();
						Hemi.object.addObjectAccessor(this,"panel");
						for(var c in cfg){
							_s[c] = cfg[c];
						}
						if(!_s.scaleWidth) _s.scaleWidth = 1;
						if(!_s.scaleHeight) _s.scaleHeight = 1;
						if(_s.basePath){
							_o.baseDir = accountManager.getCreatePath(_s.basePath);
						}
						_s.iconLargeBase = "/AccountManagerExample/Media/Icons/Crystal/128x128/";
						_s.iconSmallBase = "/AccountManagerExample/Media/Icons/Crystal/48x48/";
					},
					getCanvas : function(){
						return this.getObjects().controller.getCanvas();
					},
					scale : function(w,h){
						scaleView(this);
					},
					draw : function(){
						//layout(this);
					},
					panels : function(){
						return this.getPanels();
					},
					panel : function(s,cfg){
						var v;
						if((v = this.getPanelByName(s))) return v;
						if(!cfg) cfg = {};
						v = Hemi.newObject("Panel " + s,"1.0",true,true,{
							object_create : function(){
								var _s = this.getProperties(), _o = this.getObjects();
								
								//_o.config = cfg;
								_o.shapes = [];
								_o.grid = {};
								_o.groupState = {};
								for(var c in cfg){
									if(c.match(/^actions$/gi)) continue;
									if(c.match(/^menu/gi)) continue;
									_s[c] = cfg[c];
								}
								if(!_s.thumbWidth) _s.thumbWidth = 48;
								if(!_s.thumbHeight) _s.thumbHeight = 48;
								if(typeof _s.width == "undefined") _s.width = 'auto';
								if(typeof _s.height == "undefined") _s.height = 'auto';
								_o.actions = cfg.actions;
								_o.menu = cfg.menu;
								
								_s.suggestedCount = 0;
								_s.currentCount = 0;
								_s.totalCount = 0;
								_s.startIndex = 0;
								_s.viewIndex = 0;
								
								_s.rasterCount = 0;
								_s.rasterTotal = 0;
	
							},
							getShapes : function(){
								return this.getObjects().shapes;
							},
							getCanvas : function(){
								return this.getObjects().view.getCanvas();
							},
							panel : function(s,v){
								return this.getObjects().view.panel(s,v);
							},
							clear : function(){
								clearPanel(this);
							},
							paint : function(){
								paintPanel(this);
							},
							repaint : function(){
								repaintPanel(this);
							},
							/// API hook for the HTML forms
							///
							refreshList : function(){
								this.repaint();
							},
							top : function(){
								return expressPosition(this.getProperties().top,this.getObjects().view);
							},
							left : function(){
								return expressPosition(this.getProperties().left,this.getObjects().view);
							},
							width : function(){
								return expressPosition(this.getProperties().width,this.getObjects().view);
							},
							height : function(){
								return expressPosition(this.getProperties().height,this.getObjects().view);
							}
							
						});
						v.getObjects().view = this;
						this.addNewPanel(v,s);
						return v;
					}
				});
				v.getObjects().controller = this;
				this.addNewView(v,s);
				return v;
			}
		}
		function expressPosition(v, oV){
			var vs = oV.getProperties();
			if(typeof v == "string"){
				v = eval(v.replace(/boxHeight/,vs.boxHeight).replace(/boxWidth/,vs.boxWidth));
			}
			return v;
		}
		function pickText(o,sL,sH){
			var oW = Hemi.app.createWindow("Picker","/AccountManagerExample/Forms/TextPicker.xml","TextPicker-" + Hemi.guid(),0,0,{pickerLabel:sL,picker_handler:sH,openerId:o.getObjectId()},HandlePickerLoaded);
			if(!oW) return;
			oW.setHideOnClose(0);
			oW.resizeTo(450,100);
			oW.center();
			oW.setIsModal(true);
			oW.hideButtons();
		}
		function HandlePickerLoaded(oW){
	
		}
		function openWindow(oPanel,sType,oType, fHandler){
			var oProps = {openerId:oPanel.getObjectId(),listType:sType,picker:0,viewType:oType};
			var oW = Hemi.app.createWindow((oType && oType.id ? oType.name : "New" + sType), uwm.getApiTypeView(sType) + "/Forms/" + sType + ".xml", "View-" + (oType && oType.id ? sType + "-" + oType.id : Hemi.guid()), 0, 0, oProps, fHandler);
	        if (oW) {
	        	oW.resizeTo(450, 400);
	        	Hemi.app.getWindowManager().CenterWindow(oW);
	        	// Destroy the window when closed
	        	//
	        	oW.setHideOnClose(0);
	        }
		}
	}());