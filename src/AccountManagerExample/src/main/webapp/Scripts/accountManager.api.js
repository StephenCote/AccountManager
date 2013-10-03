(function () {
	
	window.accountManager = {
			organization_paths : {},
			getOrganizationPath : function(o){
				if(!o && uwm.getUser()) o = uwm.getUser().organization;
				if(!o) return null;
				if(accountManager.organization_paths[o.id]) return accountManager.organization_paths[o.id];
				var aB = [];
				var oOrg = o;
				// Don't count Global org, which is id = 1
				while(oOrg && oOrg.id > 1){
					aB.push(oOrg.name);
					oOrg = accountManager.getOrganizationById(oOrg.parentId);
				}
				sOrgPath = "/" + aB.reverse().join("/");
				accountManager.organization_paths[o.id] = sOrgPath;
				return accountManager.organization_paths[o.id];
			},

			getOrganizationDotPath : function(o){
				if(!o && uwm.getUser()) o = uwm.getUser().organization;
				if(!o) return null;
				var sKey = o.id + ".alt";
				if(accountManager.organization_paths[sKey]) return accountManager.organization_paths[sKey];

				sDotPath = accountManager.getOrganizationPath(o);
				if(typeof sDotPath != "string") return null;
				sDotPath = sDotPath.replace(/^\//,"");
				sDotPath = sDotPath.replace(/\//,".");
				accountManager.organization_paths[sKey] = sDotPath;
				return accountManager.organization_paths[sKey];
			},
			getPublicOrganization : function(){
				return uwmServices.getService("Organization").getPublic();
			},
			getRootOrganization : function(){
				return uwmServices.getService("Organization").getRoot();
			},
			getOrganization : function(sName, oParent){
				return uwmServices.getService("Organization").read(oParent.id,sName);
			},
			getOrganizationById : function(iId){
				return uwmServices.getService("Organization").readById(iId);
			},
			findOrganization : function(sPath){
				return uwmServices.getService("Organization").find(sPath);
			},
			countUsers : function(oOrg){
				return uwmServices.getService("User").count(oOrg.id);
			},
			listUsers : function(oOrg, iStartIndex, iRecordCount){
				/// param difference when funneled through GridType.xml
				if(typeof oOrg == "string") oOrg = 0;
				return accountManager.serviceListInOrganization(uwmServices.getService("User"),oOrg, iStartIndex, iRecordCount);
			},
			addUser : function(sName, sPassword, sEmail, oOrg){
				if(!oOrg) oOrg = uwm.getUser().organization;
				var o = new org.cote.beans.userType();
				o.name = sName;
				o.password = sPassword;
				o.contactInformation = new org.cote.beans.contactInformationType();
				o.contactInformation.email = sEmail;
				o.organization = oOrg;

				return uwmServices.getService("User").add(o);
			},
			deleteUser : function(oRec){
				return uwmServices.getService("User").delete(oRec);
			},
			updateUser : function(oRec){
				return uwmServices.getService("User").update(oRec);
			},
			getUser : function(sName){
				return uwmServices.getService("User").read(sName);
			},
			getUserById : function(iId){
				return uwmServices.getService("User").readById(iId);
			},
			authorizeRoleUser : function(oOrg, iUserId, iRoleId, bView, bEdit, bDel, bCreate){
				if(!oOrg) oOrg = uwm.getUser().organization;
				return uwmServices.getService("Role").authorizeUser(oOrg.id, iUserId, iRoleId, bView, bEdit, bDel, bCreate);
			},
			listRoleUsers : function(oRole){
				var oOrg, iRoleId = 0;
				if(!oRole) return null;
				oOrg = oRole.organization;
				iRoleId = oRole.id;
				return uwmServices.getService("Role").listUsers(oOrg.id, iRoleId);
			},
			listRolesForUser : function(oUser){
				var oOrg, iUserId = 0;
				if(!oUser){
					oOrg = uwm.getUser().organization;
					iUserId = uwm.getUser().id;
				}
				else{
					oOrg = oUser.organization;
					iUserId = oUser.id;
				}
				
				return uwmServices.getService("Role").listForUser(oOrg.id, iUserId);
			},
			countRoles : function(oOrg,oPar){
				if(!oOrg) oOrg = uwm.getUser().organization;
				if(!oPar) return uwmServices.getService("Role").count(oOrg.id);
				return uwmServices.getService("Role").countInParent(oOrg.id, oPar.id);
			},
			listRoles : function(oOrg, oParent, iStartIndex, iRecordCount){
				// param difference when funneled through GridType.xml
				if(typeof oOrg == "string") oOrg = 0;
				if(!oParent) return accountManager.serviceListInOrganization(uwmServices.getService("Role"),oOrg, iStartIndex, iRecordCount);
				return accountManager.serviceListInParent(uwmServices.getService("Role"),oOrg, oParent,iStartIndex, iRecordCount);
			},
			addRole : function(sName, oPar, oOrg){
				if(!oOrg) oOrg = uwm.getUser().organization;
				var o = new org.cote.beans.baseRoleType();
				o.name = sName;
				o.roleType = "USER";
				o.organization = oOrg;
				if(oPar) o.parentId = oPar.id;
				else o.parentId = 0;

				return uwmServices.getService("Role").add(o);
			},
			deleteRole : function(oRec){
				return uwmServices.getService("Role").delete(oRec);
			},
			updateRole : function(oRec){
				return uwmServices.getService("Role").update(oRec);
			},
			getRole : function(sName, oParent, oOrg){
				if(!oOrg) oOrg = uwm.getUser().organization;
				if(!oParent) return uwmServices.getService("Role").readByOrganizationId(oOrg.id, sName);
				return uwmServices.getService("Role").readByParentId(oOrg.id, oParent.id, sName);
			},
		authorizeGroupRole : function(oOrg, iRoleId, iRoleId, bView, bEdit, bDel, bCreate){
			if(!oOrg) oOrg = uwm.getUser().organization;
			return uwmServices.getService("Group").authorizeRole(oOrg.id, iRoleId, iRoleId, bView, bEdit, bDel, bCreate);
		},
		authorizeGroupUser : function(oOrg, iUserId, iRoleId, bView, bEdit, bDel, bCreate){
			if(!oOrg) oOrg = uwm.getUser().organization;
			return uwmServices.getService("Group").authorizeUser(oOrg.id, iUserId, iRoleId, bView, bEdit, bDel, bCreate);
		},
		listGroups : function(sPath, iStartIndex, iRecordCount){
			return accountManager.serviceListInGroup(uwmServices.getService("Group"),sPath, iStartIndex, iRecordCount);
		
			/*
			return uwmServices.getService("Group").dir(sPath);
			*/
		},
		
		countGroups : function(sPath){
			return uwmServices.getService("Group").count(sPath);
		},
		getCreatePath : function(sPath){
			return uwmServices.getService("Group").getCreatePath(sPath);
		},
		getHome : function(){
			return uwmServices.getService("Group").home();
		},
		deleteGroup : function(oRec){
			return uwmServices.getService("Group").delete(oRec);
		},
		getGroup : function(sPath){
			return uwmServices.getService("Group").cd(sPath);
		},
		getGroupById : function(iId){
			return uwmServices.getService("Group").find(iId);
		},
		addGroup : function(sName, iParentId){
			var o = new org.cote.beans.groupType();
			o.parentId = iParentId;
			o.name = sName;
			return uwmServices.getService("Group").add(o);
		},
		updateGroup : function(oGroup){
			return uwmServices.getService("Group").update(oGroup);
		},
		
		
		countDatas : function(sPath){
			return uwmServices.getService("Data").count(sPath);
		},
		listDatas : function(sPath, iStartIndex, iRecordCount){
			return accountManager.serviceListInGroup(uwmServices.getService("Data"),sPath, iStartIndex, iRecordCount);
		},
		deleteData : function(oRec){
			return uwmServices.getService("Data").delete(oRec);
		},
		updateData : function(oRec){
			return uwmServices.getService("Data").update(oRec);
		},
		getData : function(sName, oGroup){
			if(oGroup) return uwmServices.getService("Data").readByGroupId(oGroup.id,sName);
			return uwmServices.getService("Data").read(sName);
		},
		getDataById : function(iId){
			return uwmServices.getService("Data").readById(iId);
		},
		addData : function(sName, sDesc, sType, vData, oGroup){
			var o = new org.cote.beans.dataType();
			o.name = sName;
			o.description = sDesc;
			o.mimeType = sType;
			o.dataBytesStore = uwm.base64Encode(vData);
			o.blob = true;
			if(oGroup){
				//delete oGroup.javaClass;
				o.group = accountManager.getCleanGroup(oGroup);
			}
			return uwmServices.getService("Data").add(o);
		},
		
		/// TODO: GroupService is choking on subDirectories due to type mismatch between 'bean' and 'type'
		/// The 'bean' was for SCA compat, but presently i'm switching back to pure rest compat and skipping the extra work
		///
		getCleanGroup : function(o){
			var n = {};
			n.id = o.id;
			n.name = o.name;
			n.parentId = o.parentId;
			n.organization = o.organization;
			n.nameType = o.nameType;
			n.populated = false;
			n.ownerId = o.ownerId;
			n.groupType = o.groupType;
			return n;
		
		},
		serviceListInGroup : function(oSvc, sPath, iStartIndex, iRecordCount){
			if(!iStartIndex) iStartIndex = 0;
			if(!iRecordCount) iRecordCount = 0;
			return oSvc.listInGroup(sPath, iStartIndex, iRecordCount);		
		},
		serviceListInOrganization : function(oSvc, oOrg, iStartIndex, iRecordCount){
			if(!oOrg) oOrg = uwm.getUser().organization;
			if(!iStartIndex) iStartIndex = 0;
			if(!iRecordCount) iRecordCount = 0;
			return oSvc.listInOrganization(oOrg.id, iStartIndex, iRecordCount);		
		},
		serviceListInParent : function(oSvc, oOrg, oPar, iStartIndex, iRecordCount){
			if(!oOrg) oOrg = uwm.getUser().organization;
			if(!iStartIndex) iStartIndex = 0;
			if(!iRecordCount) iRecordCount = 0;
			return oSvc.listInParent(oOrg.id, oPar.id, iStartIndex, iRecordCount);		
		}
	}
}());

