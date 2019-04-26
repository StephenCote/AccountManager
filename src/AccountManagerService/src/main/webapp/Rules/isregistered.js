function TestIsRegistered(oTest){
	
	oTest.data = (window.uwm.registration ? true : false);
	this.logDebug("Testing if registration requirements exists: " + oTest.data);
}