
Hemi.util.logger.addLogger(this, "Operation", "Operation Module", "622");

this.DoOperation = function(){
	/// TODO: The template contents should be provided by teh template service, not
	/// hard coded into the operation
	///
	var sName = this.getProperties().user_name;
	var sPass = this.getProperties().password;
	var sEmail = this.getProperties().email;
	var oOrg = this.getProperties().organization;
	this.log("Register " + sName + " / " + sPass + " / " + sEmail);
	var oReg = window.uwm.register(sName, sPass, sEmail,oOrg);
	if(oReg != null){
		this.log("Registered.");
		window.uwm.operation("ContinueWorkflow", {registration:oReg}, 0, this.ruleName);
	}
	else this.logError("Failed to log in");
}
this.SetRule = function(sRule){
	this.ruleName = sRule;
}