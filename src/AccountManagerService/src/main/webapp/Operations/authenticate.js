
Hemi.util.logger.addLogger(this, "Operation", "Operation Module", "622");

this.DoOperation = function(){
	/// TODO: The template contents should be provided by teh template service, not
	/// hard coded into the operation
	///
	var sName = this.getProperties().user_name;
	var sPass = this.getProperties().password;
	var sOrg = this.getProperties().organization;
	
	var bAuthN = uwm.login(sOrg,sName, sPass,{opener:this.getProperties().opener});
	if(bAuthN){
		if(typeof bAuthN == "boolean"){
			this.log("Logged in!");
			uwm.operation("ContinueWorkflow", {opener:this.getProperties().opener,session:oSession}, 0, this.ruleName);
		}
		else{
			this.log("Pending Async Login Request ...");
		}
	}
	else{
		this.logError("Failed to log in");
		uwm.operation("RequireAuthentication");
	}
}
this.SetRule = function(sRule){
	this.ruleName = sRule;
}