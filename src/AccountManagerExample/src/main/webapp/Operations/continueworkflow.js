/// Catch-all continuation operation:
/// 1) check base rules
/// 2) continue flow as needed - eg: post-login to special, or post-login to main
///
Hemi.util.logger.addLogger(this, "Operation", "Operation Module", "622");
this.DoOperation = function(){
	/// TODO: The template contents should be provided by teh template service, not
	/// hard coded into the operation
	///
	this.log("Registered: " + (uwm.registration ? 1 : 0));
	if(uwm.rule("IsRegistered",0, "ContinueRegistration")){
		this.log("Continue to registration");
		return;
	}
	if(!uwm.rule("IsLoggedIn",0, 0,"RequireAuthentication")){
		this.log("Continue to authentication");
		return;
	}
	this.log("Continue to main");
	if(window.uwm.altMain) window.uwm.createContent("oMain",g_application_path + window.uwm.altMain.form);
	else window.uwm.createContent("oMain",g_application_path + "Forms/Main.xml");
}
this.SetRule = function(sRule){
	this.ruleName = sRule;
}
this.Initialize = function(){

};
this.Unload = function(){

};