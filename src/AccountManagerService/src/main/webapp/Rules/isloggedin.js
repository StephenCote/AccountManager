function TestIsLoggedIn(oTest){
	this.log("Testing if principal exists");
	oTest.data = (window.uwm.getUser() != null);
}
