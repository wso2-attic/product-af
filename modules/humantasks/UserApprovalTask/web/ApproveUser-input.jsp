<%@ page import="org.apache.axiom.om.OMElement" %>
<%@ page import="javax.xml.namespace.QName" %>
<p>
        <%
        String userName = "";
        String firstName = "";
        String lastName = "";
        String email = "";

        OMElement requestElement = (OMElement) request.getAttribute("taskInput");
        String ns = "http://www.example.com/claims/schema";

        if (requestElement != null) {

            OMElement userNameElement = requestElement.getFirstChildWithName(new QName(ns, "userName"));

            if(userNameElement !=null){
                userName = userNameElement.getText();
            }

            OMElement firstNameElement = requestElement.getFirstChildWithName(new QName(ns, "firstName"));

            if(firstNameElement !=null){
                firstName = firstNameElement.getText();
            }

            OMElement lastNameElement = requestElement.getFirstChildWithName(new QName(ns, "lastName"));

            if(lastNameElement !=null){
                lastName = lastNameElement.getText();
            }

            OMElement emailElement = requestElement.getFirstChildWithName(new QName(ns, "email"));

            if(emailElement !=null){
                email = emailElement.getText();
            }
        }
    %>

<table border="0">
    <tr>
        <td>User Name</td>
        <td><%=userName%>
        </td>
    </tr>
    <tr>
        <td>First Name</td>
        <td><%=firstName%>
        </td>
    </tr>
    <tr>
        <td>Last Name</td>
        <td><%=lastName%>
        </td>
    </tr>
    <tr>
        <td>Email</td>
        <td><%=email%>
        </td>
    </tr>
</table>

</p>
