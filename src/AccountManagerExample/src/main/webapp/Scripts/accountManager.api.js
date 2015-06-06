(function () {
	
	/// The following script classifies Type to API assignment, used for discovery when multiple service APIs are intermixed in the same common View code
	///
	uwm.addApi("accountManager", "/AccountManagerExample");
	uwm.addApiTypes("accountManager", ["Account","Address","Contact","Group","Person","User","Data","Role","Permission","Tag"]);
	
	window.accountManager = accountManager = {

			organization_paths : {},
			permission_paths : {},
			role_paths : {},
			setRole : function(oObj, oRole, bSet){
				if(!oObj || !oRole) return;
				if(!oObj.nameType.match(/^GROUP|PERSON|USER|ACCOUNT$/)){
					Hemi.logError("Unsupported object type: " + oObj.nameType);
					return 0;
				}
				var sObj= oObj.nameType.substring(0,1) + oObj.nameType.substring(1,oObj.nameType.length).toLowerCase();
				return uwmServices.getService("Role")["setRoleFor" + sObj](oObj.id, oRole.id, (bSet ? true : false));
			},
			setPermission : function(oObj, oActor, oPerm, bEnable){
				if(!oObj) return 0;
				if(!oObj.nameType.match(/^GROUP|DATA|ROLE$/)){
					Hemi.logError("Unsupported object type: " + oObj.nameType);
					return 0;
				}
				if(!oActor.nameType.match(/^GROUP|PERSON|USER|ACCOUNT|ROLE$/)){
					Hemi.logError("Unsupported actor type: " + oActor.nameType);
					return 0;
				}
				var sObj= oObj.nameType.substring(0,1) + oObj.nameType.substring(1,oObj.nameType.length).toLowerCase();
				var sAct= oActor.nameType.substring(0,1) + oActor.nameType.substring(1,oActor.nameType.length).toLowerCase();
				return uwmServices.getService("Permission")["setPermissionOn" + sObj + "For" + sAct](oObj.id, oActor.id, oPerm.id,bEnable);
			},
			setGroup : function(oObj, oGroup, bSet){
				if(!oObj || !oGroup) return;
				if(!oObj.nameType.match(/^PERSON|USER|ACCOUNT$/)){
					Hemi.logError("Unsupported object type: " + oObj.nameType);
					return 0;
				}
				var sObj= oObj.nameType.substring(0,1) + oObj.nameType.substring(1,oObj.nameType.length).toLowerCase();
				return uwmServices.getService("Group")["setGroupFor" + sObj](oObj.id, oGroup.id, (bSet ? true : false));
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
				var a = accountManager.getAttribute(o,n);
				if(!a) return d;
				return a.values[0];
			},
			addAttribute : function(o,s,v){
				if(!o.attributes) o.attributes = [];
				o.attributes.push(accountManager.newAttribute(s,v));
				return 1;
			},
			newAttribute : function(s,v){
				var a = new org.cote.beans.attributeType(),x=[];
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
			/*
			getPermissionPath : function(o){
				if(!o) return null;
				if(accountManager.permission_paths[o.id]) return accountManager.permission_paths[o.id];
				var aB = [];
				var oOrg = o;
				// Don't count Global org, which is id = 1
				while(oOrg && oOrg.id > 1){
					aB.push(oOrg.name);
					oOrg = accountManager.getPermissionById(oOrg.parentId);
				}
				sOrgPath = "/" + aB.reverse().join("/");
				accountManager.permission_paths[o.id] = sOrgPath;
				return accountManager.permission_paths[o.id];
			},
			*/
			getPublicUser : function(){
				return uwmServices.getService("User").getPublicUser();
			},
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
			flushCache : function(){
				uwmServiceCache.clearCache();
				return uwmServices.getService("AccountManager").flushCache();
			},
			clearAuthorizationCache : function(){
				return uwmServices.getService("AccountManager").clearAuthorizationCache();
			},
			clearUserCache : function(){
				uwmServices.getService("User").clearCache();
				uwmServiceCache.clearServiceCache("User");
			},
			
			
			
			countAccounts : function(sPath){
				return uwmServices.getService("Account").count(sPath);
			},
			listAccounts : function(sPath, iStartIndex, iRecordCount){
				if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
				if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
				return accountManager.serviceListInGroup(uwmServices.getService("Account"),sPath, iStartIndex, iRecordCount);
			},
			addAccount : function(sName, sType, sStatus, oGroup){
				var o = new org.cote.beans.accountType();
				o.name = sName;
				o.accountType = sType;
				o.accountStatus = sStatus;
				if(oGroup){
					o.group = accountManager.getCleanGroup(oGroup);
				}
				return uwmServices.getService("Account").add(o);
			},
			populateAccount : function(o){
				return uwmServices.getService("Account").populate(o);
			},

			deleteAccount : function(oRec,vCfg){
				return uwmServices.getService("Account").delete(oRec,vCfg);
			},
			updateAccount : function(oRec){
				return uwmServices.getService("Account").update(oRec);
			},
			getAccount : function(sName, oGroup){
				if(oGroup) return uwmServices.getService("Account").readByGroupId(oGroup.id,sName);
				return uwmServices.getService("Account").read(sName);
			},
			getAccountById : function(iId){
				return uwmServices.getService("Account").readById(iId);
			},

			countPersons : function(sPath){
				return uwmServices.getService("Person").count(sPath);
			},
			listPersons : function(sPath, iStartIndex, iRecordCount){
				if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
				if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
				return accountManager.serviceListInGroup(uwmServices.getService("Person"),sPath, iStartIndex, iRecordCount);
			},
			addPerson : function(sName, oGroup){
				var o = new org.cote.beans.personType();
				o.name = sName;
				o.birthDate = new Date();
				if(oGroup){
					o.group = accountManager.getCleanGroup(oGroup);
				}
				o.description = "";
				o.suffix = "";
				o.prefix = "";
				o.title = "";
				o.alias = "";
				o.firstName = "";
				o.lastName = "";
				o.middleName = "";
				o.gender = "unknown";

				return uwmServices.getService("Person").add(o);
			},
			populatePerson : function(o){
				return uwmServices.getService("Person").populate(o);
			},

			deletePerson : function(oRec,vCfg){
				return uwmServices.getService("Person").delete(oRec,vCfg);
			},
			updatePerson : function(oRec){
				return uwmServices.getService("Person").update(oRec);
			},
			getPerson : function(sName, oGroup){
				if(oGroup) return uwmServices.getService("Person").readByGroupId(oGroup.id,sName);
				return uwmServices.getService("Person").read(sName);
			},
			getPersonById : function(iId){
				return uwmServices.getService("Person").readById(iId);
			},
			
			countTags : function(sPath){
				return uwmServices.getService("Tag").count(sPath);
			},
			listTagsFor : function(oObj){
				return uwmServices.getService("Tag").listTagsFor(oObj.nameType,oObj.id);
			},

			listTags : function(sPath, iStartIndex, iRecordCount){
				if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
				if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
				return accountManager.serviceListInGroup(uwmServices.getService("Tag"),sPath, iStartIndex, iRecordCount);
			},
			addTag : function(sName, sType, oGroup){
				var o = new org.cote.beans.personType();
				o.name = sName;
				o.tagType = sType;
				if(oGroup){
					o.group = accountManager.getCleanGroup(oGroup);
				}
				o.description = "";
				return uwmServices.getService("Tag").add(o);
			},
			populateTag : function(o){
				return uwmServices.getService("Tag").populate(o);
			},

			deleteTag : function(oRec,vCfg){
				return uwmServices.getService("Tag").delete(oRec,vCfg);
			},
			updateTag : function(oRec){
				return uwmServices.getService("Tag").update(oRec);
			},
			getTag : function(sName, oGroup){
				if(oGroup) return uwmServices.getService("Tag").readByGroupId(oGroup.id,sName);
				return uwmServices.getService("Tag").read(sName);
			},
			getTagById : function(iId){
				return uwmServices.getService("Tag").readById(iId);
			},
			

			countAddresss : function(sPath){
				return uwmServices.getService("Address").count(sPath);
			},
			listAddresss : function(sPath, iStartIndex, iRecordCount){
				if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
				if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
				return accountManager.serviceListInGroup(uwmServices.getService("Address"),sPath, iStartIndex, iRecordCount);
			},
			addAddress : function(sName, sLocType, oGroup){
				var o = new org.cote.beans.addressType();
				o.name = sName;
				o.locationType = sLocType;
				if(oGroup){
					o.group = accountManager.getCleanGroup(oGroup);
				}
				return uwmServices.getService("Address").add(o);
			},
			populateAddress : function(o){
				return uwmServices.getService("Address").populate(o);
			},

			deleteAddress : function(oRec,vCfg){
				return uwmServices.getService("Address").delete(oRec,vCfg);
			},
			updateAddress : function(oRec){
				return uwmServices.getService("Address").update(oRec);
			},
			getAddress : function(sName, oGroup){
				if(oGroup) return uwmServices.getService("Address").readByGroupId(oGroup.id,sName);
				return uwmServices.getService("Address").read(sName);
			},
			getAddressById : function(iId){
				return uwmServices.getService("Address").readById(iId);
			},

			countContacts : function(sPath){
				return uwmServices.getService("Contact").count(sPath);
			},
			listContacts : function(sPath, iStartIndex, iRecordCount){
				if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
				if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
				return accountManager.serviceListInGroup(uwmServices.getService("Contact"),sPath, iStartIndex, iRecordCount);
			},
			addContact : function(sName, sType, sLocType, oGroup){
				var o = new org.cote.beans.contactType();
				o.name = sName;
				o.locationType = sLocType;
				o.contactType = sType;
				if(oGroup){
					o.group = accountManager.getCleanGroup(oGroup);
				}
				return uwmServices.getService("Contact").add(o);
			},
			populateContact : function(o){
				return uwmServices.getService("Contact").populate(o);
			},

			deleteContact : function(oRec,vCfg){
				return uwmServices.getService("Contact").delete(oRec,vCfg);
			},
			updateContact : function(oRec){
				return uwmServices.getService("Contact").update(oRec);
			},
			getContact : function(sName, oGroup){
				if(oGroup) return uwmServices.getService("Contact").readByGroupId(oGroup.id,sName);
				return uwmServices.getService("Contact").read(sName);
			},
			getContactById : function(iId){
				return uwmServices.getService("Contact").readById(iId);
			},
			countUsers : function(oOrg){
				if(!oOrg) oOrg = window.uwm.getUser().organization;
				return uwmServices.getService("User").count(oOrg.id);
			},
			listUsers : function(oOrg, iStartIndex, iRecordCount){
				/// param difference when funneled through GridType.xml
				if(typeof oOrg == "string") oOrg = 0;
				if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
				if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
				return accountManager.serviceListInOrganization(uwmServices.getService("User"),oOrg, iStartIndex, iRecordCount);
			},
			addUser : function(sName, sPassword, sEmail, oOrg){
				if(!oOrg) oOrg = uwm.getUser().organization;
				var o = new org.cote.beans.userType();
				o.name = sName;
				o.password = sPassword;
				o.contactInformation = new org.cote.beans.contactInformationType();
				o.contactInformation.contacts = [];
				
				var ct = new org.cote.beans.contactType();
				ct.group = accountManager.getGroupByPath("/Contacts");
				ct.name = sName + " Registration Email";
				ct.preferred = true;
				ct.contactType = "EMAIL";
				ct.locationType = "HOME";
				ct.contactValue = sEmail;
				
				o.contactInformation.contacts.push(ct);
				
				//o.contactInformation.email = sEmail;
				o.organization = oOrg;

				return uwmServices.getService("User").add(o);
			},
			deleteUser : function(oRec, vCfg){
				return uwmServices.getService("User").delete(oRec,vCfg);
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
			listRolesForGroup : function(oGroup){
				var oOrg = oGroup.organization;
				return uwmServices.getService("Group").listAuthorizedRoles(oOrg.id,oGroup.id);
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
			clearRoleCache : function(){
				uwmServices.getService("Role").clearCache();
				uwmServiceCache.clearServiceCache("Role");
			},

			countRoles : function(oOrg,oPar){
				if(!oOrg) oOrg = uwm.getUser().organization;
				if(!oPar) return uwmServices.getService("Role").count(oOrg.id);
				return uwmServices.getService("Role").countInParent(oOrg.id, oPar.id);
			},
			listRoles : function(oOrg, oParent, sType,iStartIndex, iRecordCount){
				// param difference when funneled through GridType.xml
				if(typeof oOrg == "string") oOrg = 0;
				if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
				if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
				if(!sType) sType = "UNKNOWN";
				return uwmServices.getService("Role").listInParent(oOrg.id, oParent.id, sType, iStartIndex, iRecordCount);
				//if(!oParent) return accountManager.serviceListInOrganization(uwmServices.getService("Role"),oOrg, iStartIndex, iRecordCount);
				//return accountManager.serviceListInParent(uwmServices.getService("Role"),oOrg, oParent,iStartIndex, iRecordCount);
			},
			addRole : function(sName, sType, oPar, oOrg){
				if(!oOrg) oOrg = uwm.getUser().organization;
				var o = new org.cote.beans.baseRoleType();
				o.name = sName;
				o.roleType = sType;
				o.organization = oOrg;
				if(typeof oPar == "object" && oPar != null) o.parentId = oPar.id;
				else if(typeof oPar == "number") o.parentId = oPar;
				else o.parentId = 0;

				return uwmServices.getService("Role").add(o);
			},
			deleteRole : function(oRec, vCfg){
				return uwmServices.getService("Role").delete(oRec, vCfg);
			},
			updateRole : function(oRec, vCfg){
				return uwmServices.getService("Role").update(oRec, vCfg);
			},
			getRoleById : function(iId){
				return uwmServices.getService("Role").readById(iId);
			},
			getRolePath : function(iId){
				return uwmServices.getService("Role").getPath((typeof iId == "object" ? iId.id : iId));
			},
			getRole : function(sName, sType, oParent, oOrg){
				if(!oOrg) oOrg = uwm.getUser().organization;
				if(!sType) sType = "UNKNOWN";
				return uwmServices.getService("Role").readByParentId(oOrg.id, (oParent ? oParent.id : 0), sType,sName);
				//if(!oParent) return uwmServices.getService("Role").readByOrganizationId(oOrg.id, sName);
				//return uwmServices.getService("Role").readByParentId(oOrg.id, oParent.id, sName);
			},
			getRootRole : function(sType,oOrg){
				if(!oOrg) oOrg = uwm.getUser().organization;
				if(!sType) sType = "UNKNOWN";
				return uwmServices.getService("Role").getRootRole(oOrg.id,sType);
			},
			/// Note: requesting the user's own role dynamically allocates the role, and also will add the user to the role and user reader role
			/// This is a temporary setup in the RoleService
			///
			getUserRole : function(sType, oOrg){
				if(!oOrg) oOrg = uwm.getUser().organization;
				if(!sType) sType = "ACCOUNT";
				return uwmServices.getService("Role").getUserRole(oOrg.id,sType);
			},
			
			clearPermissionCache : function(){
				uwmServices.getService("Permission").clearCache();
				uwmServiceCache.clearServiceCache("Permission");
			},

			countPermissions : function(oOrg,oPar){
				if(!oOrg) oOrg = uwm.getUser().organization;
				if(!oPar) return uwmServices.getService("Permission").count(oOrg.id);
				return uwmServices.getService("Permission").countInParent(oOrg.id, oPar.id);
			},
			listPermissions : function(oOrg, oParent, sType, iStartIndex, iRecordCount){
				// param difference when funneled through GridType.xml
				if(typeof oOrg == "string" || !oOrg) oOrg = uwm.getUser().organization;
				if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
				if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
				if(!sType) sType = "UNKNOWN";
				return uwmServices.getService("Permission").listInParent(oOrg.id, oParent.id, sType, iStartIndex, iRecordCount);
				//if(!oParent) return accountManager.serviceListInOrganization(uwmServices.getService("Permission"),oOrg, iStartIndex, iRecordCount);
				//return accountManager.serviceListInParent(uwmServices.getService("Permission"),oOrg, oParent,iStartIndex, iRecordCount);
			},
			addPermission : function(sName, sType, oPar, oOrg){
				if(!oOrg) oOrg = uwm.getUser().organization;
				var o = new org.cote.beans.basePermissionType();
				o.name = sName;
				o.permissionType = sType;
				o.organization = oOrg;
				if(typeof oPar == "object" && oPar != null) o.parentId = oPar.id;
				else if(typeof oPar == "number") o.parentId = oPar;
				else o.parentId = 0;

				return uwmServices.getService("Permission").add(o);
			},
			deletePermission : function(oRec, vCfg){
				return uwmServices.getService("Permission").delete(oRec, vCfg);
			},
			updatePermission : function(oRec, vCfg){
				return uwmServices.getService("Permission").update(oRec, vCfg);
			},
			getPermissionById : function(iId){
				return uwmServices.getService("Permission").readById(iId);
			},
			getPermissionPath : function(iId){
				return uwmServices.getService("Permission").getPath((typeof iId == "object" ? iId.id : iId));
			},
			getPermission : function(sName, sType, oParent, oOrg){
				if(!oOrg) oOrg = uwm.getUser().organization;
				if(!sType) sType = "UNKNOWN";
				return uwmServices.getService("Permission").readByParentId(oOrg.id, (oParent ? oParent.id : 0), sType,sName);
			},
			listSystemPermissions : function(oOrg){
				if(!oOrg) oOrg = uwm.getUser().organization;
				return uwmServices.getService("Permission").listSystemPermissions(oOrg.id);
			},
			/// Note: requesting the user's own permission dynamically allocates a permission that the user owns.
			/// Similar to roles, this is because Roles and Permissions scope to the parent and not to a groupid.
			/// This is a temporary setup in the PermissionService
			///
			getUserPermission : function(sType){
				if(!sType) sType = "ACCOUNT";
				return uwmServices.getService("Permission").getUserPermission(sType);
			},
			
		authorizeGroupRole : function(oOrg, iRoleId, iGroupId, bView, bEdit, bDel, bCreate){
			if(!oOrg) oOrg = uwm.getUser().organization;
			return uwmServices.getService("Group").authorizeRole(oOrg.id, iRoleId, iGroupId, bView, bEdit, bDel, bCreate);
		},
		authorizeGroupUser : function(oOrg, iUserId, iRoleId, bView, bEdit, bDel, bCreate){
			if(!oOrg) oOrg = uwm.getUser().organization;
			return uwmServices.getService("Group").authorizeUser(oOrg.id, iUserId, iRoleId, bView, bEdit, bDel, bCreate);
		},
		listGroups : function(oOrg, oParent, sType,iStartIndex, iRecordCount){
			if(!oOrg) oOrg = uwm.getUser().organization;
			if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
			if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
			if(!sType) sType = "UNKNOWN";
			return uwmServices.getService("Group").listInParent(oOrg.id, oParent.id, sType, iStartIndex, iRecordCount);
		},
		listGroupUsers : function(oGroup){
			var oOrg, iGroupId = 0;
			if(!oGroup) return null;
			oOrg = oGroup.organization;
			iGroupId = oGroup.id;
			return uwmServices.getService("Group").listUsers(oOrg.id, iGroupId);
		},
		listGroupAccounts : function(oGroup){
			var oOrg, iGroupId = 0;
			if(!oGroup) return null;
			oOrg = oGroup.organization;
			iGroupId = oGroup.id;
			return uwmServices.getService("Group").listAccounts(oOrg.id, iGroupId);
		},
		listGroupPersons : function(oGroup){
			var oOrg, iGroupId = 0;
			if(!oGroup) return null;
			oOrg = oGroup.organization;
			iGroupId = oGroup.id;
			return uwmServices.getService("Group").listPersons(oOrg.id, iGroupId);
		},

		/*
		listGroups : function(sPath, iStartIndex, iRecordCount){
			if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
			if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
			///return accountManager.serviceListInGroup(uwmServices.getService("Group"),sPath, iStartIndex, iRecordCount);
			return uwmServices.getService("Group").listInDataGroup(sPath, iStartIndex, iRecordCount);
		},
		*/
		clearGroupCache : function(){
			uwmServices.getService("Group").clearCache();
			uwmServiceCache.clearServiceCache("Group");
		},
		countGroups : function(sPath){
			return uwmServices.getService("Group").count(sPath);
		},
		getCreatePath : function(sType,sPath){
			return uwmServices.getService("Group").getCreatePath(sType,sPath);
		},
		getHome : function(){
			return uwmServices.getService("Group").home();
		},
		deleteGroup : function(oRec, vCfg){
			return uwmServices.getService("Group")["delete" + (oRec.groupType == "DATA" ? "Directory" : "")](oRec, vCfg);
		},
		getGroup : function(sName, sType, oParent, oOrg){
			if(!oOrg) oOrg = uwm.getUser().organization;
			if(!sType) sType = "UNKNOWN";
			return uwmServices.getService("Group").readByParentId(oOrg.id, (oParent ? oParent.id : 0), sType,sName);
		},
		getGroupByPath : function(sType,sPath){
			return uwmServices.getService("Group").readByPath(sType,sPath);
		},
		getGroupById : function(iId){
			return uwmServices.getService("Group").readById(iId);
		},
		addGroup : function(sName, sType, iParentId, oOrg){
			var o = new org.cote.beans.baseGroupType();
			if(!oOrg) oOrg = uwm.getUser().organization;
			o.organization = oOrg;
			o.parentId = iParentId;
			o.name = sName;
			if(!sType) sType = 'DATA';
			o.groupType = sType;
			return uwmServices.getService("Group").add(o);
		},
		updateGroup : function(oGroup){
			return uwmServices.getService("Group")["update" + (oGroup.groupType == "DATA" ? "Directory" : "")](oGroup);
		},
		
		authorizeDataRole : function(oOrg, iRoleId, iDataId, bView, bEdit, bDel, bCreate){
			if(!oOrg) oOrg = uwm.getUser().organization;
			return uwmServices.getService("Data").authorizeRole(oOrg.id, iRoleId, iDataId, bView, bEdit, bDel, bCreate);
		},
		authorizeDataUser : function(oOrg, iUserId, iDataId, bView, bEdit, bDel, bCreate){
			if(!oOrg) oOrg = uwm.getUser().organization;
			return uwmServices.getService("Data").authorizeUser(oOrg.id, iUserId, iDataId, bView, bEdit, bDel, bCreate);
		},
		clearDataCache : function(){
			uwmServices.getService("Data").clearCache();
			uwmServiceCache.clearServiceCache("Data");
		},

		countDatas : function(sPath){
			return uwmServices.getService("Data").count(sPath);
		},
		listDatas : function(sPath, iStartIndex, iRecordCount){
			if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
			if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
			return accountManager.serviceListInGroup(uwmServices.getService("Data"),sPath, iStartIndex, iRecordCount);
		},
		deleteData : function(oRec, vCfg){
			return uwmServices.getService("Data").delete(oRec, vCfg);
		},
		updateData : function(oRec){
			return uwmServices.getService("Data").update(oRec);
		},
		getProfile : function(){
			return uwmServices.getService("Data").getProfile();
		},
		updateProfile : function(o){
			return uwmServices.getService("Data").updateProfile(o);
		},

		getData : function(sName, oGroup){
			if(oGroup) return uwmServices.getService("Data").readByGroupId(oGroup.id,sName);
			return uwmServices.getService("Data").read(sName);
		},
		getDataById : function(iId){
			return uwmServices.getService("Data").readById(iId);
		},
		sendFeedback : function(sName, vData){
			var o = new org.cote.beans.dataType();
			o.name = sName;
			o.dataBytesStore = uwm.base64Encode(vData);
			o.blob = true;
			o.group = null;
			o.mimeType = null;
			o.description = null;
			return uwmServices.getService("Data").addFeedback(o);
		},
		addData : function(sName, sDesc, sType, vData, oGroup, vCfg){
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
			return uwmServices.getService("Data").add(o, vCfg);
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
		/// There is currently a hard coded implication that the group is a DATA group
		///
		serviceListInGroup : function(oSvc, sPath, iStartIndex, iRecordCount){
			if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
			if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
			return oSvc.listInGroup(sPath, iStartIndex, iRecordCount);		
		},
		serviceListInOrganization : function(oSvc, oOrg, iStartIndex, iRecordCount){
			if(!oOrg) oOrg = uwm.getUser().organization;
			if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
			if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
			return oSvc.listInOrganization(oOrg.id, iStartIndex, iRecordCount);		
		},
		serviceListInParent : function(oSvc, oOrg, oPar, iStartIndex, iRecordCount){
			if(!oOrg) oOrg = uwm.getUser().organization;
			if(!iStartIndex || iStartIndex < 0) iStartIndex = 0;
			if(!iRecordCount || iRecordCount < 0) iRecordCount = 0;
			return oSvc.listInParent(oOrg.id, oPar.id, iStartIndex, iRecordCount);		
		}
	}
})();

