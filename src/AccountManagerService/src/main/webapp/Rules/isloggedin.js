this.getMod = function(){ return Module;};
function TestIsLoggedIn(oTest){
	window.uwm.getUser().then((o)=>{
		oTest.data = (o != null);
		this.logDebug("Testing if principal exists: " + oTest.data);
		EndTestIsLoggedIn(true);
	});
	
	return false;
}

