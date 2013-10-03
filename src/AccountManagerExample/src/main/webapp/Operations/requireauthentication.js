this.DoOperation = function(){
	/// TODO: The template contents should be provided by teh template service, not
	/// hard coded into the operation
	///
	//window.uwm.createModal("Login",g_application_path + "Forms/Login.xml");
	window.uwm.createContent("oMain",g_application_path + "Forms/Login.xml");
}
this.SetRule = function(sRule){
	this.ruleName = sRule;
}
this.Initialize = function(){

};
this.Unload = function(){

};