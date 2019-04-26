
this.DoOperation = function(){
	/// TODO: The template contents should be provided by teh template service, not
	/// hard coded into the operation
	///
	
	uwm.rule("IsRegistered",0, 0, "ContinueWorkflow").then((b)=>{
		if(!b) return;
		window.uwm.createContent("oMain",g_application_path + "Forms/CompleteRegistration.xml");
	});
}
this.SetRule = function(sRule){
	this.ruleName = sRule;
}
