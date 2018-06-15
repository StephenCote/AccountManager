this.DoOperation = function(){
	/// TODO: The template contents should be provided by teh template service, not
	/// hard coded into the operation
	///
	Hemi.log("Require Auth / " + this.getProperties().opener);
	if(this.getProperties().opener){

		var o = Hemi.registry.service.getObject(this.getProperties().opener);
		o.loadTemplate(g_application_path + (window.uwm.altLogin ? window.uwm.altLogin.form : "Forms/Login.xml"));
	}
	else{
		window.uwm.createContent("oMain",g_application_path + (window.uwm.altLogin ? window.uwm.altLogin.form : "Forms/Login.xml"));
	}
	//window.uwm.createContent("oMain",g_application_path + (window.uwm.altLogin ? window.uwm.altLogin.form : "Forms/LoginIn.xml"));
}
this.SetRule = function(sRule){
	this.ruleName = sRule;
}
this.Initialize = function(){

};
this.Unload = function(){

};