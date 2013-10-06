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
				this.log("Delegate " + oShape.action);
				if(oShape.matteId) o = oCanvas.getShapeById(oShape.matteId);
				else if (oShape.referenceType == "MATTE") o = oShape;
				oPanel.getObjects().view.getObjects().controller[oShape.action](oShape.referenceType, oShape.referenceId, o);
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
			/// Return on the "view" action because it's overlayed with a temp matte
			///
			if(!oShape || !oShape.hover
			//		|| oShape.action == "view"
			){
				this.logDebug("Don't clear because " + (!oShape ? " no shape" : (!oShape.hover ? " no hover " : oShape.action)));
				return;
			}

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
					oShape.hover = 1;
				}
				else{
					var img = new Image();
					img.onload = function(){
						var oShape = oCanvas.Image(img, oRefShape.x, oRefShape.y + oRefShape.image.height + 5);
						oShape.action = oM.action;
						oShape.panelId = oPanel.getObjectId();
						oShape.referenceId = -1;
						oShape.referenceType = oPanel.getProperties().itemType.toUpperCase();
						oShape.hover = 1;
						oM.image = img;
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
			
			if(oSrc.referenceType == "GROUP" && oTarg.referenceType == "GROUP"){
				var oPanel = Hemi.registry.service.getObject(oShape.panelId);
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
			}
			else if ((oSrc.referenceType == "GROUP" || oSrc.referenceType == "DATA") && oTarg.referenceType == "CTL"){
				var oPanel = Hemi.registry.service.getObject(oTarg.panelId);

				if(oTarg.action == "deleteObject" && oPanel.getObjects().view.getObjects().controller[oTarg.action]){
					oPanel.getObjects().view.getObjects().controller[oTarg.action](oSrc.referenceType,oSrc.referenceId);
				}
				if(oSrc.referenceType == "DATA" && oTarg.action == "controlPanel"){
					openObject(oPanel,oSrc.referenceType, oSrc.referenceId, oSrc);
				}
			}
			else if (oSrc.referenceType == "DATA" && oTarg.referenceType == "GROUP"){
				
				var oObj = accountManager.getDataById(oSrc.referenceId);
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
			Hemi.event.removeEventListener(window,"resize",this._prehandle_window_resize);
		},
		object_create : function(){
			var _s = this.getProperties();
			Hemi.util.logger.addLogger(this, "Gallery View", "Gallery View", 232);
			
			Hemi.event.addScopeBuffer(this);
			this.scopeHandler("window_resize",0,0,1);
			
			instrumentVC(this);
			var v = this.view("gallery",{scaleHeight:1,scaleWidth:1,basePath:"~/"});
			v.panel("nav",{
				width:100,
				height:"boxHeight-55",
				top:0,
				left:0,
				thumbWidth:50,
				thumbHeight:50,
				thumbTree:1,
				smallIcon:1,
				vslot:1,
				suggestedCountOffset:2,
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
				width:"boxWidth-105",
				height:"boxHeight-55",
				top:0,
				left:105,
				thumbWidth:128,
				thumbHeight:128,
				thumbScroll:0,
				itemType:"Data",
				itemPath:"GalleryHome/",
				//itemIcon:"48px-Crystal_Clear_action_filenew.png",
				//itemIcon:"Crystal_Clear_action_filenew.png",
				itemIcon : "Crystal_Clear_mimetype_misc.png",
				itemIconImg : "Crystal_Clear_mimetype_image.png"
			});
			v.panel("controlPanel",{
				width:"boxWidth-105",
				height:"boxHeight-55",
				top:0,
				left:105,
				thumbWidth:128,
				thumbHeight:128,
				thumbScroll:0,
				actions:[
		         {action: "newGroup",label: "New Group",icon : "Crystal_Clear_action_filenew.png"},
			     {action: "newImage",label: "New Image",icon : "Crystal_Clear_mimetype_image.png"},
			     {action: "openShare",label: "Sharing",icon : "Crystal_Clear_app_Login_Manager.png"},
			     {action: "openLog",label: "Log",icon : "Crystal_Clear_app_kexi.png"},
			     {action: "openDebug",label: "Debug",icon : g_application_path + "Media/Icons/Hemi_Logo_128x128.png"}
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
					{action:"deleteObject",small:1,slot:"slots - 2",icon:"48px-Crystal_Clear_filesystem_trashcan_empty.png"},
					{action:"logout",small:1,slot:"slots - 1",icon:"48px-Crystal_Clear_app_logout.png"}
				]

			});
			v.panel("matte",{
				width:"boxWidth",
				height:"boxHeight",
				top:0,
				left:0,
				thumbWidth:50,
				thumbHeight:50,
				actions:[]
			});			
			//this.view("tasks",{scaleHeight:1,scaleWidth:1}).panel("nav",{width:100}).panel("content",{left:100}).panel("control",{row:2,top:0,height:100}).panel("footer",{row:3,height:50,width:"33%"}).panel("footer2",{row:3,height:50,width:"33%"}).panel("footer3",{row:3,height:50,width:"33%"});
			//initializeVC(this);
			
			this.getCanvasController().getCanvasContainer().style.cssText = "position:absolute;top:0px;left:0px;";
			
			this.switchView("gallery",["nav","content","commands"]);
			Hemi.event.addEventListener(window,"resize",this._prehandle_window_resize);
			
		}, // end object create
		_handle_window_resize : function(){
			var v = this.getCurrentView(),aP;
			scaleView(v);
			aP = v.panels();
			for(var i = 0; i < aP.length;i++){
				if(aP[i].getProperties().visible){
					aP[i].repaint();
				}
			}
			
		},
		switchView : function(sName,aP){
			var v = this.view(sName),_s = this.getProperties();
			scaleView(v);
			this.logDebug("Panels: " + v.panels().length);
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
			this.getCanvasController().getCanvas().Rasterize();
		},
		getCanvas : function(){
			return this.getCanvasController().getCanvas();
		},
		getCanvasController : function(){
			return ctl;
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
		newGroup : function(){
			pickText(this,"New Group Name","createGroup");
		},
		cd : function(sType, sId, oShape){
			changeDirectory(sId, this.getCurrentViewPanel("nav"),["content"]);
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
		createStory : function(sName){

			var b = accountManager.addTask(sName, "", "UNKNOWN", 0,(new Date()),(new Date()),0,0, 0, 0, 0, 0, 0, 0, 0, this.getCurrentViewPanel("nav").getObjects().currentDirectory);
			if(b){
				this.getCurrentViewPanel("nav").repaint();
			}
			this.logDebug("Create story: " + sName + " " + (b ? true : false));
		},
		newImage : function(){
			var oP = this.getCurrentViewPanel("nav");
			openWindow(oP, "Data", 0, showDataForm);
		},
		view : function(sType, sId, iViewIndex){
			var oP = this.getCurrentViewPanel("nav");
			viewObject(oP, sType, sId, iViewIndex);
		}

	});
	

	function viewObject(p, sType, sId, iViewIndex){
		var o = accountManager.getDataById(sId),_o = p.getObjects(),_s = p.getProperties();
		if(typeof iViewIndex == "object") iViewIndex = iViewIndex.referenceIndex;
		_s.showImage = 1;
		_s.showImageId = sId;
		_s.showImageIndex = iViewIndex;
		//_s.showControlPanel = 0;
		this.clearView(1);
		var img = new Image();
		
		
		
		var oG = _o.canvas;

		var oR = oG.Rect(0, 0, _s.width, _s.height, "#000000","#000000");
		oR.referenceIndex = iViewIndex;
		var t = this;
		img.onload = function(){

			var iMaxWidth = t.getProperties().width;
			var iMaxHeight = t.getProperties().height;
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
			t.log("Image: " + img.width + "x" + img.height + " at " + iX + ", " + iY);
			
			var oShape = oG.Image(img, iX, iY);
			oShape.action = "gestureMatteImage";
			oShape.referenceName = o.name;
			oShape.referenceId = o.id;
			oShape.referenceType = "OBJECT";
			oShape.matteId = oR.id;
			oShape.noLabel = 1;
			oShape.groupId = o.group.id;
			oShape.referenceIndex = oR.referenceIndex;
					
			oR.noLabel = 1;
			oR.action = "gestureMatteImage";
			oR.referenceType = "MATTE";
			oR.referenceId = oShape.id;
		}
		
		img.src = "data:" + o.mimeType + ";base64," + o.dataBytesStore;


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
		var _o = o.getObjects(), _s = o.getProperties(), oM, _no = o.getObjects().view.panel("nav").getObjects();
		if(!_no.currentDirectory) _no.currentDirectory = accountManager.getCreatePath(sPath);
		if(!_no.baseGroup) _no.baseGroup = _no.currentDirectory;
		var iter = 0;
		if(sType == 'Group'){
			if(_no.currentDirectory.id != _no.baseGroup.id){
				if(_no.currentDirectory.parentId != _no.baseGroup.id){
					//paintGroup(_o.baseGroup,0);
					paintVerticalSlottedItem(o,_no.baseGroup,0,iter++);
				}
				//paintGroup(accountManager.getGroupById(_o[sProp].parentId),0,"48px-Crystal_Clear_action_1uparrow.png");
				paintVerticalSlottedItem(o,accountManager.getGroupById(_no.currentDirectory.parentId),0,iter++);
			}
			//paintGroup(_o[sProp],0,"48px-Crystal_Clear_filesystem_folder_grey_open.png");
			paintVerticalSlottedItem(o,_no.currentDirectory,0,iter++);
		}
		_s.totalCount = accountManager["count" + sType + "s"](_no.currentDirectory.path);
		var aSub = accountManager["list" + sType + "s"](_no.currentDirectory.path,_s.startIndex,_s.suggestedCount - (_s.suggestedCountOffset ? _s.suggestedCountOffset : 0));
		_o.currentList = [];
		Hemi.log("Painting " + aSub.length + " items for " + _no.currentDirectory.path);
		for(var i = 0; aSub && i < aSub.length;i++){
			if(sType == 'Group' && aSub[i].name.match(/^\.thumbnail$/gi)) continue;
			_o.currentList.push(aSub[i]);
			if(_s.vslot){
				paintVerticalSlottedItem(o,aSub[i],1,i);
			}
			else{
				paintItem(o,aSub[i],i);
			}
		}

	}
	
	function changeDirectory(sId,p, aF){
		var _o = p.getObjects(),_p = p.getProperties();
		var o = accountManager.getGroupById(sId);
		if(!o || o.id == _o.currentDirectory) return;
		_o.currentDirectory = o;
		Hemi.log("Changing directory to " + o.path);
		p.repaint();
		for(var i = 0; i < aF.length;i++){
			var oP = galleryView.getCurrentView().panel(aF[i]);
			if(!oP) Hemi.logError("Invalid panel: '" + aF[i] + "'");
			else{
				Hemi.log("Repaint '" + aF[i] + "'");
				oP.repaint();
			}
		}
	}
	
	function next(p){
		var _s = p.getProperties(), iCount;
		iCount = _s.suggestedCount  - (_s.suggestedCountOffset ? _s.suggestedCountOffset : 0);
		_s.startIndex += iCount;
		if(_s.totalCount > 0 && _s.startIndex >= _s.totalCount){
			_s.startIndex = _s.totalCount - iCount;
		}
		if(_s.startIndex < 0) _s.startIndex = 0;
		p.repaint();
	}
	function back(p){
		var _s = p.getProperties(),iCount;
		iCount = _s.suggestedCount  - (_s.suggestedCountOffset ? _s.suggestedCountOffset : 0);
		_s.startIndex -= iCount;
		if(_s.startIndex < 0) _s.startIndex = 0;
		p.repaint();
	}
	
	function repaintPanel(p){
		clearPanel(p);
		paintPanel(p);
	}
	
	function clearPanel(p){
		var _o = p.getObjects(), _s = p.getProperties(), oM,aPS=p.getShapes(),oCvs = p.getCanvas();
		_o.view.getObjects().controller.logDebug("Clear Panel " + _s.left + ", " + _s.top + " X " + _s.width + ", " + _s.height + " with " + aPS.length + " shapes");
		for(var i = 0; i < aPS.length;i++){
			var oS = oCvs.getShapeById(aPS[i]);
			if(oS){
				oCvs.removeShape(oS);
			}
		}
		aPS.length = 0;
		oCvs.setTemporaryContextConfig(p.getCanvas().getConfigByName("NoShadow"));
		oCvs.setContextConfig(p.getCanvas().getConfigByName("NoShadow"));
		//oM = oCvs.Rect(_s.left, _s.top, _s.width, _s.height, "#FFFFFF","#FF0000");
		oM = oCvs.Rect(p.left(), p.top(), p.width(), p.height(), "#FFFFFF","#FF0000");
		oM.selectable = 0;
		_s.visible = 0;
		
		p.getCanvas().Rasterize();
	}
	function paintPanel(p){
		var _o = p.getObjects(), _s = p.getProperties(),oA;
		_s.currentCount = 0;
		_o.view.getObjects().controller.logDebug("Paint Panel " + _s.left + ", " + _s.top + " X " + _s.width + ", " + _s.height);
		_s.visible = 1;
		p.getCanvas().setTemporaryContextConfig(p.getCanvas().getConfigByName("NoShadow"));
		p.getCanvas().setContextConfig(p.getCanvas().getConfigByName("NoShadow"));

		if(_o.actions){
			var iSlots = Math.floor(p.width() / _s.thumbWidth);
			var iMid = Math.floor(iSlots/2);
			var iVSlots = Math.floor(p.height() / _s.thumbHeight);
			var iVMid = Math.floor(iVSlots/2);		
			for(var i = 0; i < _o.actions.length;i++){
				oA = _o.actions[i];
				///Hemi.log("Paint Panel Action " + oA.action);
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
		}
		if(_s.itemType && _s.itemPath){
			_o.view.getProperties().basePath + _s.itemPath
			refreshGroupType(p,_s.itemType,_o.view.getProperties().basePath + _s.itemPath,0);
		}
		_s.visible = 1;
	}
	
	function paintItem(p,o,i){
		if(!o) return;
		
		var _s = p.getProperties(),_o=p.getObjects(),oP, g = (o.nameType == 'GROUP' ? o : o.group);
		
		Hemi.log("Paint item " + o.name + " in " + g.path);
		
		var sIcoSrc = _o.view.getProperties()["icon" + (_s.smallIcon ? "Small" : "Large")+ "Base"] + _s.itemIcon;
		if(o.mimeType && o.mimeType.match(/^image/gi)){
			sIcoSrc = _o.view.getProperties()["icon" + (_s.smallIcon ? "Small" : "Large")+ "Base"] + _s.itemIconImg;
			if(g && !g.populated) g = accountManager.getGroupById(g.id);
			if(g.path){
				sIcoSrc = "/AccountManager/Thumbnail/" + accountManager.getOrganizationDotPath() + "/Data" + g.path + "/" + o.name + "/" + _s.thumbWidth + "x" + _s.thumbHeight;
			}
		}

		// # of icons per row
		var iMaxX = parseInt(p.width() / (_s.thumbWidth + 10));
		var iMaxY = parseInt(p.height() / (_s.thumbHeight + 10));
		var slotX = p.left() + (_s.currentCount % iMaxX)*(_s.thumbWidth+10);
		var slotY = p.top() + parseInt(_s.currentCount / iMaxY)*(_s.thumbHeight+10);
		
		paintPanelObject(p,o,0,i,sIcoSrc, o.name, o.id, p.getProperties().itemType.toUpperCase(),"view",slotX, slotY, 1, 1,1)
		/*
		var img = new Image();
		var obj = o;
		var oG = p.getCanvas();
		var t = this;		
		var refIndex = i;
		oP = p;
		_o.view.getObjects().controller.logDebug(obj.name + " " + sIcoSrc + " to " + slotX + ", " + slotY + " from " + _s.currentCount + ", " + iMaxX );
		_s.currentCount++;
		img.onload = function(){
			var oShape = oG.Image(img, slotX, slotY);
			oShape.action = "view";
			oShape.panelId = oP.getObjectId();
			oShape.referenceName = obj.name;
			oShape.referenceId = obj.id;
			oShape.referenceType = p.getProperties().itemType.toUpperCase();
			oShape.referenceIndex = refIndex;
			//oShape.icoType = "IMG";
			oShape.groupId = obj.group.id;
			oShape.hover = 1;
			oShape.drag = 1;
			oG.Text(scaleText(obj.name), slotX, slotY + oP.getProperties().thumbHeight, "#000000","#000000","8pt","Arial");
			oG.Rasterize();
		}
		img.src = sIcoSrc;
		*/
	}
	
	
	function paintVerticalSlottedItem(p,o,a,i){
		if(!o) return;
		var _s = p.getProperties(),_o = p.getObjects(),s,sIcoSrc,oP;
		s = (o.icon ? o.icon : _s.itemIcon);
		if(!s) sIcoSrc = _o.view.getProperties()["icon" + (_s.smallIcon ? "Small" : "Large")+ "Base"] + "48px-Crystal_Clear_filesystem_folder_grey.png";
		else sIcoSrc = (s.match(/\//) ? "" : _o.view.getProperties()["icon" + (_s.smallIcon ? "Small" : "Large")+ "Base"]) + s;
		//Hemi.log("Paint vertical slotted item: " + o);
		//if(_s.itemType == 'Group' && o.id == _o.lifecycleHome.id){
		if(o.id == accountManager.getHome().id){
			sIcoSrc =  _o.view.getProperties()["icon" + (_s.smallIcon ? "Small" : "Large")+ "Base"] + "48px-Crystal_Clear_filesystem_folder_home2.png";
		}
		var slotX = (a * _s.thumbWidth);
		var slotY = _s.currentCount * (_s.thumbWidth + 10);

		if(typeof o.action == "string"){
			paintPanelObject(p,o,a,i,sIcoSrc, o.label, -1, 'CTL',o.action,slotX, slotY, 1, 1,0);
		}
		else{
			paintPanelObject(p,o,a,i,sIcoSrc, o.name, o.id, _s.itemType.toUpperCase(),"cd",slotX, slotY, 1, 1,1);
		}
		/*
		var img = new Image();
		var obj = o;
		var oP = p;
		var refIndex = i;		
		img.onload = function(){
			var oG = oP.getCanvas();
			var iL =  slotX;
			var iT = slotY;
			//if(img.width > _s.dirThumbWidth) 
			img.width = _s.thumbWidth;
			//if(img.height > _s.dirThumbHeight) img.height = _s.dirThumbHeight;
			var oShape = oG.Image(img, iL, iT);
			if(typeof obj.action == "string"){
				oShape.action = obj.action;
				oShape.referenceName = obj.label;
				oShape.referenceType = "CTL";
				oG.Text(scaleText(o.label), slotX, slotY + img.height, "#000000","#000000","8pt","Arial");
			}
			else{
				oP.getObjects().view.getObjects().controller.logDebug("Painting vertical slotted item " + obj.name + " " + oP.getProperties().itemType + " at " + slotX + "," + slotY);
				oShape.path = obj.path;
				oShape.referenceName = obj.name;
				oShape.referenceId = obj.id;
				oShape.action = "cd";
				oShape.referenceType = oP.getProperties().itemType.toUpperCase();
				oShape.drag = 1;
				oShape.referenceIndex = refIndex;
				oG.Text(scaleText(obj.name), slotX, slotY + img.height, "#000000","#000000","8pt","Arial");
			}
			
			oShape.panelId = oP.getObjectId();			
			//oShape.icoType = "DIR";
			oShape.hover = 1;
			oG.Rasterize();
			oP.getShapes().push(oShape.id);
		}
		img.src = sIcoSrc;
		*/
	}
	
	function paintAction(p, a){
		var _s = p.getProperties(),_o=p.getObjects();
		var sIcoSrc = (a.icon.match(/\//) ? "" : _o.view.getProperties()["icon" + (a.small ? "Small" : "Large") + "Base"]) + a.icon;
		var iMaxX = parseInt(p.width() / (_s.thumbWidth + 10));
		var iMaxY = parseInt(p.height() / (_s.thumbHeight + 10));
		var slotX = p.left() + (_s.currentCount % iMaxX)*(_s.thumbWidth+10);
		var slotY = parseInt(_s.currentCount / iMaxX)*(_s.thumbHeight+10);

		paintPanelObject(p,0,a,-1,sIcoSrc, a.label,-1,'CTL',a.action,slotX, slotY, 1,1,0);
		/*
		var img = new Image();
		var oG = p.getCanvas();
		_s.currentCount++;
		img.onload = function(){
			var oShape = oG.Image(img, slotX, slotY);
			oShape.action = a.action;
			oShape.panelId = p.getObjectId();
			oShape.referenceName = a.label;
			oShape.referenceType = "CTL";
			//oShape.icoType = "IMG";
			oShape.hover = 1;
			oG.Text(scaleText(a.label), slotX, slotY + _s.thumbHeight, "#000000","#000000","8pt","Arial");
			oG.Rasterize();
			p.getShapes().push(oShape.id);
		}
		img.src = sIcoSrc;
		*/
	}
	function paintSlottedAction(p,a,i){
		var _s = p.getProperties(),_o = p.getObjects();
		var slotX = i * _s.thumbWidth;
		var sIcoSrc = _o.view.getProperties()["icon" + (a.small ? "Small" : "Large") + "Base"] + a.icon;
		
		paintPanelObject(p, 0, a, i, sIcoSrc, 0,-1,"CTL",a.action,slotX,p.top() + 1, 0, 1,0);
		/*
		var img = new Image();
		var oG = p.getCanvas();
		img.onload = function(){
			var oShape = oG.Image(img, slotX, _s.top + 1);
			oShape.action = a.action;
			oShape.panelId = p.getObjectId();
			oShape.referenceType = "CTL";
			oShape.hover = 1;
			oG.Rasterize();
			p.getShapes().push(oShape.id);
		};
		img.src = sIcoSrc;
		*/
	}
	
	function paintPanelObject(oPanel,oObj,oAct,iIndex,sIco, sLbl, iRefId, sRef,sAct,iX, iY, bText, bHover,bDrag){
		var _s = oPanel.getProperties();
		var img = new Image();
		var oG = oPanel.getCanvas();
		_s.currentCount++;
		Hemi.log("Pre-Paint Object: " + sAct + " " + sLbl + " " + sRef + " "  + bText + ":" + bHover + ":" + bDrag);
		img.onload = function(){

			var oShape = oG.Image(img, iX, iY);
			oShape.action = sAct;
			oShape.panelId = oPanel.getObjectId();
			oShape.referenceType = sRef;
			if(iRefId >= 0) oShape.referenceId = iRefId;
			oShape.hover = bHover;
			oShape.drag = bDrag;
			if(sLbl) oShape.referenceName = sLbl;
			
			if(bText) oG.Text(scaleText(sLbl), iX, iY + _s.thumbHeight, "#000000","#000000","8pt","Arial");
			oG.Rasterize();
			oPanel.getShapes().push(oShape.id);
			Hemi.log("Paint Object: " + sAct + " " + sLbl + " " + sRef + " "  + bText + ":" + bHover + ":" + bDrag);
		};
		img.src = sIco;
	}
	
	function initializeCanvasController(o)
	{
		Hemi.util.logger.addLogger(o, "Example Interface Controller", "Example UIC", 231);
		o.logDebug("Example UIC Initializing");
		var _s = o.getProperties();
		var _o = o.getObjects();

		if(!_o.cvs_container){
			_o.cvs_container = document.createElement("div");
			document.body.appendChild(_o.cvs_container);
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
				_sp.suggestedCount = Math.floor(oP.width() / (_s.thumbWidth+10));
			}
			else{
				_sp.suggestedCount = Math.floor(oP.height() / (_sp.thumbHeight+10)) * Math.floor(oP.width() / (_s.thumbWidth+10));
			}
		}
		o.getCanvas().Resize(w + "px",h + "px");
	}

	function copyShapeProperties(s, t){
		t.id = s.id;
		t.panelId = s.panelId;
		t.referenceName = s.referenceName;
		t.referenceType = s.referenceType;
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
		if(!o) o = window;
		return (typeof window.innerWidth == "number" ? window.innerWidth : document.documentElement.clientWidth);
	}
	function boxHeight(o){
		if(!o) o = window;
		return (typeof window.innerHeight == "number" ? window.innerHeight : document.documentElement.clientHeight);
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
		Hemi.logDebug("Reparent " + s.name + " to " + t.name);
		s.parentId = t.id;
		accountManager.updateGroup(s);
		repaintPanel(oPanel);
	}
	function reparentObject(s, t){
		Hemi.logDebug("Reparent " + s.name + " to " + t.name);
		s.group = t;
		s.detailsOnly = true;
		delete s.dataBytesStore;
		accountManager.updateData(s);
		galleryView.getCurrentView().panel("nav").repaint();
		galleryView.getCurrentView().panel("content").repaint();
		//repaintPanel(oPanel);
	}
	function openObject(oPanel,sType, sId, oShape){
		var o = accountManager.getDataById(sId), _o = galleryView.getCurrentView().panel("content").getObjects();
		var oProps = {openerId:oPanel.getObjectId(),picker:0,viewType:o};
		if(_o.viewWindow && !_o.viewWindow.getIsClosed()) _o.viewWindow.Close();
		var oW = Hemi.app.createWindow(o.name, "/AccountManagerExample/Forms/Data.xml", "View-" + o.id, 0, 0, oProps);
	    if (oW) {
	    	oW.resizeTo(400, 400);
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
					_s.iconLargeBase = g_application_path + "Media/Icons/Crystal/128x128/";
					_s.iconSmallBase = g_application_path + "Media/Icons/Crystal/48x48/";
					/// Matte Config defines the control boundary regions
					///
					_s.matteConfig = {
						left: 50,
						right: 50,
						top: 50,
						bottom: 50
					};
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
							
							_s.currentCount = 0;
							_s.suggestedCount = 0;
							_s.totalCount = 0;
							_s.startIndex = 0;
							_s.viewIndex = 0;

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
		var oW = Hemi.app.createWindow("Picker",g_application_path + "Forms/TextPicker.xml","TextPicker-" + Hemi.guid(),0,0,{pickerLabel:sL,picker_handler:sH,openerId:o.getObjectId()},HandlePickerLoaded);
		if(!oW) return;
		oW.setHideOnClose(0);
		oW.resizeTo(400,100);
		oW.center();
		oW.setIsModal(true);
		oW.hideButtons();
	}
	function HandlePickerLoaded(oW){

	}
	function openWindow(oPanel,sType,oType, fHandler){
		var oProps = {openerId:oPanel.getObjectId(),listType:sType,picker:0,viewType:oType};
		var oW = Hemi.app.createWindow((oType && oType.id ? oType.name : "New" + sType), "/AccountManagerExample/Forms/" + sType + ".xml", "View-" + (oType && oType.id ? sType + "-" + oType.id : Hemi.guid()), 0, 0, oProps, fHandler);
        if (oW) {
        	oW.resizeTo(400, 400);
        	Hemi.app.getWindowManager().CenterWindow(oW);
        	// Destroy the window when closed
        	//
        	oW.setHideOnClose(0);
        }
	}
}());