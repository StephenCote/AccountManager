/*
 * Define who is providing the service layer
 */
if(!window.uwmServices){
	window.uwmServices = Hemi.json.rpc.service;
	window.uwmServiceCache = Hemi.json.rpc.cache.service;
}

/*
 * Register the available services
 * NOTE: The 3rd and 4th parameters of the service registration may be used to define static service and object models so they are not queried everytime
 */
(function(){
	/// Jersey JSON+REST w/ Basic AM SMD Support
	///
	

	
	/*
	 * Register the AccountManager core services
	 */
	
	uwmServices.addService(
			"AMSchema",
			"/AccountManager/rest/schema/smd"
			, true
			, true
	);
	uwmServices.addService(
			"Organization",
			"/AccountManager/rest/organization/smd",
			true,
			true
	);
	uwmServices.addService(
			"Blog",
			"/AccountManager/rest/blog/smd",
			true,
			true
	);
	uwmServices.addService(
			"Message",
			"/AccountManager/rest/message/smd"
			, true
			, true
	);	
	uwmServices.addService(
			"Crypto",
			"/AccountManager/rest/crypto/smd"
			, true
			, true
	);
	uwmServices.addService(
			"Session",
			"/AccountManager/rest/session/smd"
			, true
			, true
	);
	uwmServices.addService(
			"User",
			"/AccountManager/rest/user/smd"
			, true
			, true
	);
	uwmServices.addService(
			"Data",
			"/AccountManager/rest/data/smd"
			, true
			, true
			, true
	);
	uwmServices.addService(
			"Role",
			"/AccountManager/rest/role/smd"
			, true
			, true
	);
	uwmServices.addService(
			"Group",
			"/AccountManager/rest/group/smd"
			, true
			, true
			, true
	);
})();
