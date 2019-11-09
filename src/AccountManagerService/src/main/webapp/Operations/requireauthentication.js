this.DoOperation = function(){
	/// TODO: The template contents should be provided by teh template service, not
	/// hard coded into the operation
	///
	var sForm = (window.uwm.altLogin ? window.uwm.altLogin.form : "Forms/Login.xml");
	Hemi.log("Require Auth / " + this.getProperties().opener + " / " + sForm);
	
	if(this.getProperties().opener){
		
		var o = Hemi.registry.service.getObject(this.getProperties().opener);
		o.loadTemplate(g_application_path + sForm);
	}
	else{
		window.uwm.createContent("oMain",g_application_path + sForm);
	}
}
this.SetRule = function(sRule){
	this.ruleName = sRule;
}
this.Initialize = function(){

};
this.Unload = function(){

};