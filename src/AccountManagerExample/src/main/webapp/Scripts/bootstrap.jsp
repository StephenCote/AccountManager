<%@ page language="java" contentType="application/javascript; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
    import = "java.util.Date"
    %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%
long defCacheSeconds = 7200;
long expiry = new Date().getTime() + (defCacheSeconds*1000);

response.setHeader("Cache-Control", "public,max-age="+ defCacheSeconds);
response.setDateHeader("Expires", expiry);
response.setCharacterEncoding("UTF-8");

%>
(function(){
	if(!window.g_application_path) window.g_application_path = "/AccountManagerExample/";
	if(!window.HemiConfig) window.HemiConfig = { hemi_base: "/HemiFramework/Hemi/" };
})();

<c:import url="/Hemi/hemi.comp.js" context="/HemiFramework" />

<%@include file="3rdParty/base64.js" %>
<%@include file="3rdParty/Aes.complete.js" %>
<%@include file="pagescript.js" %>
<%@include file="services.registration.js" %>
<%@include file="accountManager.api.js" %>
<%@include file="bbscript.js" %>
