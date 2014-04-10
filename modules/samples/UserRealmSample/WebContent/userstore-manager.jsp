<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.user.api.UserRealm" %>
<%@ page import="org.wso2.carbon.user.api.UserStoreManager" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>User Realm Sample</title>
</head>
<body>

<h2>UserStore Manager Sample</h2>

<%
    // get user realm from thread local carbon context
    UserRealm userRealm = CarbonContext.getThreadLocalCarbonContext().getUserRealm();
    // get user store manager
    UserStoreManager userStoreManager = userRealm.getUserStoreManager();

    // below sample use few methods of user store manager API. You can use all methods to build your application.

    String username = request.getParameter("username");
    String role = request.getParameter("role");
    String action = request.getParameter("action");

    if (action != null) {
        if (action.equals("deleteUser")) {
            userStoreManager.deleteUser(username);
            out.println(username + " user is successfully deleted.");
        } else if (action.equals("deleteRole")) {
            userStoreManager.deleteRole(role);
            out.println(role + " is role successfully deleted.");
        } else if (action.equals("addUser")) {
            userStoreManager.addUser(username, "password", null, null, null);
            out.println(username + " user is successfully added.");
        } else if (action.equals("addRole")) {
            userStoreManager.addRole(role, new String[]{username}, null);
            out.println(role + " role is successfully added.");
        }
    }


%>

<form action="index.jsp">
    <div>
        <h3>Add/Delete users</h3>
        username: <input type="text" name="username" id="username"/>
        <input type="button"
               onclick="window.location.href='userstore-manager.jsp?action=addUser&username='+document.getElementById('username').value;"
               value="Add User">
        <input type="button"
               onclick="window.location.href='userstore-manager.jsp?action=deleteUser&username='+document.getElementById('username').value;"
               value="Delete User">
    </div>
    <div>
        <h3>Add/Delete Roles</h3>
        roleName: <input type="text" name="role" id="role"/>
        existingUser: <input type="text" name="username" id="username2"/>
        <input type="button"
               onclick="window.location.href='userstore-manager.jsp?action=addRole&role='+document.getElementById('role').value+'&username='+document.getElementById('username2').value;"
               value="Add Role">
        <input type="button"
               onclick="window.location.href='userstore-manager.jsp?action=deleteRole&role='+document.getElementById('role').value;"
               value="Delete Role">
    </div>

</form>

</body>
</html>