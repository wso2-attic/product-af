<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.user.api.UserRealm" %>
<%@ page import="org.wso2.carbon.user.api.AuthorizationManager" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>User Realm Sample</title>
</head>
<body>

<h2>Authorization Manager Sample</h2>

<%
    // get user realm from thread local carbon context
    UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
    // get authorization manager
    AuthorizationManager authorizationManager = userRealm.getAuthorizationManager();

    // below sample use few methods of authorization manager API. You can use all methods to build your application.

    String username = request.getParameter("username");
    String role = request.getParameter("role");
    String action = request.getParameter("action");
    String resource = request.getParameter("resource");
    boolean isUserAuthorized = false;

    if (action != null) {
        if (action.equals("authorizeRole")) {
            authorizationManager.authorizeRole(role, resource, "ui.execute");
            out.println(role + " role is authorized for resource:"+resource);
        } else if (action.equals("denyRole")) {
            authorizationManager.denyRole(role, resource, "ui.execute");
            out.println(role + " is denied for resource:"+resource);
        } else if (action.equals("isUserAuthorized")) {
            isUserAuthorized = authorizationManager.isUserAuthorized(username, resource, "ui.execute");
        }
    }


%>

<form action="index.jsp">
    <div>
        <h3>Authorize/Deny Role</h3>
        role: <input type="text" name="role" id="role"/> <br>
        resource: <input type="text" name="resource" id="resource"/>
        <input type="button"
               onclick="window.location.href='authorization-manager.jsp?action=authorizeRole&role='+document.getElementById('role').value+'&resource='+document.getElementById('resource').value;"
               value="Authorize Role">
        <input type="button"
               onclick="window.location.href='authorization-manager.jsp?action=denyRole&role='+document.getElementById('role').value+'&resource='+document.getElementById('resource').value;"
               value="Deny Role">
    </div>
    <div>
        <h3>Check is user authorized</h3>
        username: <input type="text" name="username" id="username"/>
        resource: <input type="text" name="resource" id="resource2"/>
        <input type="button"
               onclick="window.location.href='authorization-manager.jsp?action=isUserAuthorized&username='+document.getElementById('username').value+'&resource='+document.getElementById('resource2').value;"
               value="Is Authorized">
        <%
            if("isUserAuthorized".equals(action)){
               %>
        <br>
        Is user:<%=username%> authorized to resource:<%=resource%> ? <%=isUserAuthorized%>
        <%
            }
        %>
    </div>

</form>

</body>
</html>