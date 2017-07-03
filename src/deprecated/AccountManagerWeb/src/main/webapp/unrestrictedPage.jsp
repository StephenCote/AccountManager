
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"
 	import = "java.util.Set,java.util.Iterator,java.security.Principal,javax.security.auth.Subject,org.cote.util.AccountManagerPrincipal"   
 %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Insert title here</title>
</head>
<body>
<%
Principal p = request.getUserPrincipal();
AccountManagerPrincipal ap = (AccountManagerPrincipal)p;
if(p == null){
	%>No Principal<%
}
else{
	%>Principal=<%=p.getName() %><%
}
%>
</body>
</html>