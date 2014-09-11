
Hemi.util.logger.addLogger(this, "Operation", "Operation Module", "622");

this.DoOperation = function(){
	/// TODO: The template contents should be provided by teh template service, not
	/// hard coded into the operation
	///
	var sName = this.getProperties().user_name;
	var sPass = this.getProperties().password;
	var oOrg = this.getProperties().organization;
	this.log(sName + "/" + sPass);
	/*
	var oUser = uwm.login(sName, sPass, oOrg);
	if(oUser != null){
		this.log("Logged in!");
		uwm.operation("ContinueWorkflow", {user:oUser}, 0, this.ruleName);
	}
	*/
	var oSession = uwm.login(sName, sPass, oOrg,{opener:this.getProperties().opener});
	if(oSession && oSession != null){
		if(typeof oSession == "object"){
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