(function(){
	
	var cache = {};
	
	var sBase = "/AccountManagerService/rest";
	var sResource = sBase + "/resource";
	var sPrincipal = sBase + "/principal";
	var sSearch = sBase + "/search";
	var sMake = sBase + "/make";
	var sList = sBase + "/list";
	function getCache(){
		return cache;
	}
	function clearCache(sType){
		if(!sType) cache = {};
		else delete cache[sType];
	}
	function removeFromCache(sType, sObjId){
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
	function deleteObject(sType,sObjId, fH){
		   return Hemi.xml.deleteJSON(sResource + "/" + sType + "/" + sObjId,fH,(fH ? 1 : 0));
		}
	function update(sType,oObj, fH){
		removeFromCache(sType, oObj.objectId);
	   return Hemi.xml.postJSON(sResource + "/" + sType + "/",oObj,fH,(fH ? 1 : 0));
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
	function get(sType,sObjectId,fH){
		var o = getFromCache(sType, "GET", sObjectId);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache("USER","GET",sObjectId,v.json);} if(f) f(s,v);};

	   return Hemi.xml.getJSON(sResource + "/" + sType + "/" + sObjectId,fc,(fH ? 1 : 0));
	}
	function getByName(sType,sObjectId,sName,fH){
		   return Hemi.xml.getJSON(sResource + "/" + sType + "/" + sObjectId + "/" + sName,fH,(fH ? 1 : 0));
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
	function find(sType,sObjType,sPath,fH){
		return makeFind(sType,sObjType,sPath,0,fH);
	}
	function make(sType,sObjType,sPath,fH){
		return makeFind(sType,sObjType,sPath,1,fH);
	}

	function makeFind(sType,sObjType,sPath,bMake,fH){
		var sK = "FIND-" + sObjType;
		var o = getFromCache(sType, sK, sPath);
		if(o){
			if(fH) fH("",o);
			return o;
		}
		var f = fH;
		var fc = function(s,v){if(typeof v != "undefined" && v != null){addToCache(sType,sK,sPath,v.json);} if(f) f(s,v);};

		return Hemi.xml.getJSON((bMake ? sMake : sSearch) + "/" + sType + "/" + sObjType + "/" + sPath,fc,(fH ? 1 : 0));
	}
	
	function getDotPath(path){
		return path.replace(/^\//,"").replace(/\//,".");

	}
	
	window.AM6Client = {
		dotPath : getDotPath,
		find : find,
		make : make,
		list : list,
		count: count,
		get : get,
		getByName : getByName,
		update : update,
		delete : deleteObject,
		cache : getCache,
		principal : getPrincipal,
		clearCache : clearCache
	}
}());