function TestIsLoggedIn(oTest){
	this.log("Testing if session is logged in");
	var session = window.uwm.getSession();
	this.Assert(session != null, "Session is null");
	oTest.data = (session.sessionStatus == "AUTHENTICATED");
}
