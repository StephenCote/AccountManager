function TestIsRegistered(oTest){
	
	oTest.data = (window.uwm.registration ? true : false);
	this.log("Testing if registration exists: " + oTest.data);
}