/// Convert this to a JSP to aggregate all of the scripts together
///
(function(){
	if(!window.g_application_path) window.g_application_path = "/AccountManagerExample/";
	function WriteScript(s){
		document.write("<scr" + "ipt type = \"text/javascript\" src = \"" + s + "\"></scr" + "ipt>");		
	}
	
	if(!window.HemiConfig) window.HemiConfig = { hemi_base: "/HemiFramework/Hemi/" };
	WriteScript("/HemiFramework/Hemi/hemi.js");
	WriteScript(g_application_path + "Scripts/3rdParty/base64.js");
	WriteScript(g_application_path + "Scripts/3rdParty/Aes.complete.js");
	WriteScript(g_application_path + "Scripts/pagescript.js");
	WriteScript(g_application_path + "Scripts/services.registration.js");
	WriteScript(g_application_path + "Scripts/accountManager.api.js");
	WriteScript(g_application_path + "Scripts/uwm.proxy.provider.js");
}());