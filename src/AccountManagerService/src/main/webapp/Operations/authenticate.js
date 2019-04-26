
Hemi.util.logger.addLogger(this, "Operation", "Operation Module", "622");

this.DoOperation = function(){
	/// TODO: The template contents should be provided by teh template service, not
	/// hard coded into the operation
	///
	var sName = this.getProperties().user_name;
	var sPass = this.getProperties().password;
	var sOrg = this.getProperties().organization;
	var oP = this.getProperties().opener;
	uwm.login(sOrg,sName, sPass,{opener:this.getProperties().opener},function(s,v){
		if(v){
			if(typeof v == "boolean" || typeof v == "object"){
				console.debug("Logged in!");
				uwm.operation("ContinueWorkflow", {opener:oP,session:0}, 0, this.ruleName);
			}
			else{
				console.debug("Pending Async Login Request ...");
			}
		}
		else{
			console.error("Failed to log in");
			uwm.operation("RequireAuthentication");
		}
	});
}
this.SetRule = function(sRule){
	this.ruleName = sRule;
}