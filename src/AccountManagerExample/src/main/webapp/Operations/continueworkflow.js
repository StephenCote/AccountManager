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
	if(uwm.rule("IsRegistered",{opener:this.getProperties().opener}, "ContinueRegistration")){
		this.log("Continue to registration");
		return;
	}
	if(!uwm.rule("IsLoggedIn",{opener:this.getProperties().opener}, 0,"RequireAuthentication")){
		this.log("Continue to authentication");
		return;
	}
	this.log("Continue to main / " + this.getProperties().opener);
	if(this.getProperties().opener){

		var o = Hemi.registry.service.getObject(this.getProperties().opener);
		o.loadTemplate(g_application_path + (window.uwm.altMain ? window.uwm.altMain.form : "Forms/Main.xml"));
	}
	else{
		window.uwm.createContent("oMain",g_application_path + (window.uwm.altMain ? window.uwm.altMain.form : "Forms/Main.xml"));
	}
}
this.SetRule = function(sRule){
	this.ruleName = sRule;
}
this.Initialize = function(){

};
this.Unload = function(){

};