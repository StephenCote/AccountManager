(function(){
	var sBase = "/AccountManagerService/rest";
	var sResource = sBase + "/resource";
	var sSearch = sBase + "/search";
	var sList = sBase + "/list";

	function update(sType,oObj, fH){
	   return Hemi.xml.postJSON(sResource + "/" + sType + "/",oObj,fH,(fH ? 1 : 0));
	}
	function get(sType,sObjectId,fH){
	   return Hemi.xml.getJSON(sResource + "/" + sType + "/" + sObjectId,fH,(fH ? 1 : 0));
	}
	function getByName(sType,sObjectId,sName,fH){
		   return Hemi.xml.getJSON(sResource + "/" + sType + "/" + sObjectId + "/" + sName,fH,(fH ? 1 : 0));
		}
	function count(sType,sObjectId,fH){
	   return Hemi.xml.getJSON(sList + "/" + sType + "/" + sObjectId + "/count",fH,(fH ? 1 : 0));
	}
	function list(sType,sObjectId,iStart,iLength,fH){
	   return Hemi.xml.getJSON(sList + "/" + sType + "/" + sObjectId + "/" + iStart + "/" + iLength,fH,(fH ? 1 : 0));
	}
	function find(sType,sObjType,sPath,fH){
	   return Hemi.xml.getJSON(sSearch + "/" + sType + "/" + sObjType + "/" + sPath,fH,(fH ? 1 : 0));
	}
	
	window.AM6Client = {
		find : find,
		list : list,
		count: count,
		get : get,
		getByName : getByName,
		update : update
	}
}());