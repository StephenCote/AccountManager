(function () {
	Hemi.include("hemi.event");
	Hemi.include("hemi.data.io");
	Hemi.namespace("uwm.io", Hemi, {
		service: null,

		serviceImpl: function () {
			var t = this;

			Hemi.prepareObject("uwm_io_provider", "%FILE_VERSION%", 1, t, 1);
			Hemi.util.logger.addLogger(t, "UWM IO Provider", "UWM IO Provider", "633");
			Hemi.event.addScopeBuffer(t);
			t.scopeHandler("loadxml", 0, 0, 1);

			t.handle_io_register = function (oService) {
				this.getProperties().useRegisteredApi = 1;
				this.implement("Catalog", "List");
				this.implement("Action", "CheckName");
				this.implement("Action", "Add");
				this.implement("Action", "Delete");
				this.implement("Action", "Edit");
				this.implement("Action", "Read");
			};
			t.handle_io_request = function (oService, oSubject, oRequest, oResponse) {
				if (oRequest.requestApplication != "UWM") {
					this.logDebug("Not applicable request: " + oRequest.requestApplication);
					return 1;
				}
				return 0;
			};
			t.handleRequestAction = function (oService, oSubject, oRequest, oResponse, bGet, sReq, sAction) {
				var sAcct, oR;

				if (!(sAcct = t.getAccountConstruct(oSubject)) || !oSubject.isAuthenticated) {
					this.log("Skip request for unspecified or unauthenticated subject");
					return 0;
				}

				var aPD = t.getPostData(oRequest), bP = 1;

				if (aPD.length == 0){
					bP = 0;
				}
				var ctx = oRequest.requestContext;
				//if (ctx && ctx.match(/^\d+$/)) ctx = "ID-" + ctx;

				t.log("handleRequestAction: " + sAction + ": " + g_application_path + oRequest.requestApplication + "/" + oSubject.name + "/" + ctx + "/" + (sReq ? sReq + "/" : "") + sAction + "?is-xml=1");
				/*
				if (bGet) oR = Hemi.xml[(oRequest.mimeType && oRequest.mimeType.match(/^text/) ? "getText" : "getXml")](g_application_path + oRequest.requestApplication + "/" + oSubject.name + "/" + ctx + "/" + (sReq ? sReq + "/" : "") + sAction + "?is-xml=1&ts=" + (new Date()).getTime(), (oRequest.async ? this._prehandle_loadxml : 0), oRequest.async, oRequest.id);
				else if (bP) oR = Hemi.xml.postXml(g_application_path + oRequest.requestApplication + "/" + oSubject.name + "/" + ctx + "/" + (sReq ? sReq + "/" : "") + sAction + "?is-xml=1", oPD.value, (oRequest.async ? this._prehandle_loadxml : 0), oRequest.async, oRequest.id);
				else oR = Hemi.xml.postXml(g_application_path + oRequest.requestApplication + "/" + oSubject.name + "/" + ctx + "/" + (sReq ? sReq + "/" : "") + sAction + "?is-xml=1&ts=" + (new Date()).getTime(), 0, (oRequest.async ? this._prehandle_loadxml : 0), oRequest.async, oRequest.id);
				*/
				return oR;

			};
			t.requestActionAdd = function (oService, oSubject, oRequest, oResponse) {
				var b = 1;

				var oR = t.handleRequestAction(oService, oSubject, oRequest, oResponse, 0, 0, "Add");
				/*
				t.checkBoolResponse(oResponse, oR);

				if (oResponse.status) {
					t.checkIdResponse(oResponse, oR);
					if (oResponse.responseId) {
						oResponse.responsePath = g_application_path + oRequest.requestApplication + "/" + oSubject.name + "/" + oRequest.requestContext + "/ID-" + oResponse.responseId;
					}
				}
				*/
				t.log("Response: " + oResponse.message);
				return b;
			};
			/*
			t.requestActionCheckName = function (oService, oSubject, oRequest, oResponse) {
				var b = 1;
				var oR = t.handleRequestAction(oService, oSubject, oRequest, oResponse, 0, 0, "CheckName");
				t.checkBoolResponse(oResponse, oR);
				t.log("Response: " + oResponse.message);
				return b;
			};
			t.requestActionDelete = function (oService, oSubject, oRequest, oResponse) {
				var b = 1;
				var sReq = 0;
				if (oRequest.requestId && oRequest.requestId.match(/^\d+$/)) sReq = "ID-" + oRequest.requestId;
				else sReq = oRequest.requestName;
				var oR = t.handleRequestAction(oService, oSubject, oRequest, oResponse, 0, sReq, "Delete");
				t.checkBoolResponse(oResponse, oR);
				t.log("Response: " + oResponse.message);
				return b;
			};
			t.requestActionEdit = function (oService, oSubject, oRequest, oResponse) {
				var b = 1;
				var oR = t.handleRequestAction(oService, oSubject, oRequest, oResponse, 0, 0, "Edit");
				t.checkBoolResponse(oResponse, oR);
				if (oResponse.status) {
					t.checkIdResponse(oResponse, oR);
					if (oResponse.responseId) {
						oResponse.responsePath = g_application_path + oRequest.requestApplication + "/" + oSubject.name + "/" + oRequest.requestContext + "/ID-" + oResponse.responseId;
					}
				}
				t.log("Response: " + oResponse.message);
				return b;
			};
			t.requestActionRead = function (oService, oSubject, oRequest, oResponse) {
				var sReq = 0;
				if (oRequest.requestId && oRequest.requestId.match(/^\d+$/)) sReq = "ID-" + oRequest.requestId;
				else sReq = oRequest.requestName;
				var oR = t.handleRequestAction(oService, oSubject, oRequest, oResponse, 1, sReq, "Data");

				if (oRequest.async) {
					return 0;
				}

				t.writeDataList(oRequest, oResponse, (typeof oR == "string" ? oR : oR.documentElement));
				return 1;

			};
			*/
			/*
			t.requireSubject = function (oSubject) {
			var o = t.getAccountConstruct(oSubject);
			if (!o || !oSubject.isAuthenticated)
			return 0;
			return o;
			};
			*/
			t.getPostData = function (oRequest) {
				var aPD = [];
				for (var i = 0; i < oRequest.requestData.length; i++) {
					if (oRequest.requestData[i]) {
						aPD.push(oRequest.requestData[i].value);
						break;
					}
				}
				return oPD;
			};
			t.checkIdResponse = function (oResponse, oD) {
				oResponse.responseId = 0;
				if (oD == null || typeof oD.id == "undefined") {
					return;
				}
				oResponse.responseId = oD.id;

			};

			t.requestCatalogList = function (oService, oSubject, oRequest, oResponse) {
				var b = 1;
				if (oRequest.requestApplication != "UWM") {
					this.log("Skip Request: " + oRequest.requestApplication);
					return b;
				}
				var oAcct = t.getAccountConstruct(oSubject);
				if (!oAcct || !oSubject.isAuthenticated) {
					this.log("Skip request for unspecified or unauthenticated subject");
					return b;
				}

				switch (oRequest.requestContext) {
					case "Directory":
					case "Data":
						var sMod = "";
						if (oRequest.instruction.paginate) {
							sMod = "&start-record=" + oRequest.instruction.startRecord + "&record-count=" + oRequest.instruction.recordCount;
						}
						if (oRequest.requestApplication == "Explorer") {
							sReq = oRequest.requestCatalog;
							if (oRequest.requestId && oRequest.requestId.match(/^\d+$/i)) sReq = "ID-" + oRequest.requestId;
							t.log("Catalog: " + g_application_path + oRequest.requestApplication + "/" + oRequest.requestContext + "/" + sReq);
							Hemi.xml.getXml(g_application_path + oRequest.requestApplication + "/" + oRequest.requestContext + "/" + sReq + "?is-xml=1" + sMod, this._prehandle_loadxml, 1, oRequest.id, oRequest.cache);
							b = 0;
						}
						else if (oRequest.requestApplication == "DWAC") {
							sReq = oRequest.requestCatalog;

							if (oRequest.requestId && oRequest.requestId.match(/^\d+$/i)) sReq = "ID-" + oRequest.requestId;
							t.log("Catalog: " + g_application_path + oRequest.requestApplication + "/" + oSubject.name + "/" + sReq + "/" + oRequest.requestAction);
							Hemi.xml.getXml(g_application_path + oRequest.requestApplication + "/" + oSubject.name + "/" + sReq + "/" + oRequest.requestAction + "?is-xml=1" + sMod, this._prehandle_loadxml, oRequest.async, oRequest.id, oRequest.cache);

							b = 0;
						}
						break;
					default:
						this.logWarning("Unhandled request context: " + oRequest.requestContext);
						b = 1;
						break;
				}
				return b;

			};
			/*
			t.getRequestConstruct = function (oRequest) {
			var r = 0;
			if (oRequest.requestId) r = "ID-" + oRequest.requestId;
			else if (oRequest.requestName && oRequest.requestName.length) r = oRequest.requestName;
			return r;
			};
			*/
			t.getAccountConstruct = function (oSubject) {
				var r = 0;
				if (oSubject.name && oSubject.name.length > 0) r = oSubject.name;
				else if (oSubject.id && oSubject.id.length > 0) r = "ID-" + oSubject.id;
				return r;
			};

			t._handle_loadxml = function (s, v) {
				try {
					//(v.id != sIOID)
					var oRequest = Hemi.data.io.service.getRequestByName(v.id);
					if (!oRequest) {
						this.logError("Invalid request for id " + v.id);
						return;
					}
					var oResponse = Hemi.data.io.service.getResponseByName(oRequest.responseId);
					if (!oResponse) {
						this.logError("Invalid response for id " + v.id);
						return;
					}
					if (v.xdom) {
						if (v.xdom.getElementsByTagName("Login-Required").length > 0) {
							this.logError("Authentication required for id " + v.id);
							oRequest.authenticationRequired = true;
							Hemi.data.io.service.continueRequest(oRequest, this, true);
						}
						var oDL = v.xdom.getElementsByTagName("DataList");
						if (oDL.length > 0) {
							t.writeDataList(oRequest, oResponse, oDL[0]);
						}
						else {
							oDL = v.xdom.getElementsByTagName("Data");
							for (var i = 0; i < oDL.length; i++) {
								t.writeData(oRequest, oResponse, oDL[i]);
							}
						}

						var oGroups = v.xdom.getElementsByTagName("Groups");
						if (oGroups.length > 0) {
							t.writeGroups(oRequest, oResponse, oGroups[0]);
						}
					}
					else if (oRequest.mimeType && oRequest.mimeType.match(/^text/) && v.text) {
						t.writeData(oRequest, oResponse, v.text);
					}
					this.log("Handled response for id " + v.id + ": Group Count=" + oResponse.responseGroups.length + " / Data Count = " + oResponse.responseData.length);
					/// Hemi.log(oResponse.responseGroups.length + ":" + oResponse.responseGroups[0].groups.length);
					Hemi.data.io.service.continueRequest(oRequest, this, true);



				}
				catch (e) {
					alert(e.name + "\n" + e.number + "\n" + e.description + "\n" + e.message);
				}
			};
			t.writeDataList = function (oRequest, oResponse, oParent) {
				var oChild = 0;
				oResponse.instruction.totalCount = parseInt(t.GetElementText(oParent, "TotalCount"));
				//alert(oResponse.totalCount);
				for (var i = 0; i < oParent.childNodes.length; i++) {
					oChild = oParent.childNodes[i];
					if (oChild.nodeType != 1 || oChild.nodeName != "Data") continue;
					t.writeData(oRequest, oResponse, oChild, oResponse.responseData);
				}
			};
			t.writeData = function (oRequest, oResponse, oNode, aParentList) {
				if (oNode.nodeName != "Data") return 0;
				var o = Hemi.data.io.service.newData(), p = t.ImportPolicy(oNode);
				if (typeof oNode == "object") {
					o.name = t.GetElementText(oNode, "Name");
					o.path = t.GetElementText(oNode, "Path");
					o.id = t.GetElementText(oNode, "Id");
					o.group = t.GetElementText(oNode, "GroupId");
					o.size = parseInt(t.GetElementText(oNode, "Size"));
					o.description = t.GetElementText(oNode, "Description");
					o.createdDate = t.GetElementText(oNode, "Created");
					o.modifiedDate = t.GetElementText(oNode, "Modified");
					o.detailsOnly = t.GetBoolElementText(oNode, "DetailsOnly");
					o.hash = t.GetElementText(oNode, "Hash");
					o.mimeType = t.GetElementText(oNode, "MimeType");
					if (!o.detailsOnly) {
						o.value = t.GetElementText(oNode, "Value");
					}
				}
				else if (typeof oNode == "string") {
					o.name = (oRequest.requestName ? oRequest.requestName : oRequest.requestId);
					o.id = oRequest.requestId;
					o.group = oRequest.requestContext;
					o.size = oNode.length;
					o.description = 0;
					o.createdDate = 0;
					o.modifiedDate = 0;
					o.detailsOnly = 0;
					o.hash = 0;
					o.mimeType = oRequest.mimeType;
					o.value = oNode;
				}
				if (oRequest.requestApplication && oRequest.requestApplication.match(/^dwac$/i))
					o.path = g_application_path + oRequest.requestApplication + "/" + Hemi.data.io.service.getSubject().name + "/" + oRequest.requestContext + "/ID-" + o.id;


				if (p) o.policies.push(p);

				oResponse.writeData(o, t, aParentList, Hemi.data.io.service.getBusType().ONLINE);

				return o;
			};

			t.writeGroups = function (oRequest, oResponse, oParent, aSubGroups) {
				var oChild = 0;
				if (!aSubGroups) aSubGroups = oResponse.responseGroups;
				/// var aCGroups = aSubGroups;
				for (var i = 0; i < oParent.childNodes.length; i++) {
					oChild = oParent.childNodes[i];
					if (oChild.nodeType != 1 || oChild.nodeName != "Group") continue;
					t.writeGroup(oRequest, oResponse, oChild, aSubGroups);
					///aCGroups = (t.writeGroup(oResponse, oChild, aSubGroups)).groups;
				}
			};
			t.writeGroup = function (oRequest, oResponse, oNode, aParentGroups) {
				if (oNode.nodeName != "Group") return 0;
				var o = Hemi.data.io.service.newGroup(), p = t.ImportPolicy(oNode);
				o.name = t.GetElementText(oNode, "Name");
				o.path = t.GetElementText(oNode, "Path");
				o.id = t.GetElementText(oNode, "Id");
				/// Hemi.log("id - " + o.id);
				o.type = t.GetElementText(oNode, "Type");
				o.parentId = t.GetElementText(oNode, "ParentId");
				o.populated = t.GetBoolElementText(oNode, "IsPopulated");
				if (p) o.policies.push(p);
				oResponse.writeGroup(o, t, aParentGroups, Hemi.data.io.service.getBusType().ONLINE);

				for (var c = 0; c < oNode.childNodes.length; c++) {
					var oChild = oNode.childNodes[c];
					if (oChild.nodeName == "SubDirectories") {
						t.writeGroups(oRequest, oResponse, oChild, o.groups);
						continue;
					}
				}
				return o;
			};

			t.GetBoolElementText = function (oParent, sName) {
				var b = 0;
				var sText = this.GetElementText(oParent, sName);
				if (sText && sText.match(/^true$/i) || sText.match(/^1$/)) b = 1;
				return b;
			};

			t.GetElementText = function (oParent, sName) {
				var aN = oParent.getElementsByTagName(sName);
				if (aN.length == 0) return ""; // was 0;
				return Hemi.xml.getInnerText(aN[0]);
			};

			t.ImportPolicy = function (oXmlNode) {
				var oP = this.GetChildNode(oXmlNode, "Policy"), p;
				if (!oP) return 0;
				p = Hemi.data.io.service.newPolicy();
				p.read = this.GetBoolElementText(oP, "Read");
				p.write = this.GetBoolElementText(oP, "Write");
				p.change = this.GetBoolElementText(oP, "Change");
				p.del = this.GetBoolElementText(oP, "Delete");
				p.statement = this.GetElementText(oP, "Statement");
				return p;
			};
			t.GetChildNode = function (oXmlNode, sName) {
				var oC = 0;
				for (var i = 0; i < oXmlNode.childNodes.length; i++) {
					if (oXmlNode.childNodes[i].nodeType == 1 && oXmlNode.childNodes[i].nodeName == sName) {
						oC = oXmlNode.childNodes[i];
						break;
					}
				}
				return oC;
			};
			t.ready_state = 4;
			Hemi.data.io.service.register(this, Hemi.data.io.service.getBusType().ONLINE);

		}
	}, 1);
} ());