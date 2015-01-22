<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.context.RegistryType" %>
<%@ page import="org.wso2.carbon.registry.core.Registry" %>
<%@ page import="org.wso2.carbon.registry.core.Resource" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.commons.httpclient.HttpClient" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="org.apache.commons.httpclient.methods.GetMethod" %>
<%@ page import="org.wso2.carbon.registry.core.exceptions.RegistryException" %>
<%@ page import="org.wso2.carbon.registry.core.utils.RegistryUtils" %>
<%@ page import="org.wso2.carbon.registry.core.RegistryConstants" %>
<%@ page import="org.apache.commons.httpclient.HttpClient" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="org.apache.commons.httpclient.methods.GetMethod" %>

<%
    String appKey = request.getParameter("appKey");
    String propName = request.getParameter("propName");
    String propValue = "";

    if (appKey != null && appKey != "") {

        String resourcePath = "/dependencies/" + appKey + "/" + propName + "/url";

        CarbonContext cCtx = CarbonContext.getThreadLocalCarbonContext();
        Registry registry = (Registry) cCtx.getRegistry(RegistryType.SYSTEM_GOVERNANCE);

        try {

            if (registry.resourceExists(resourcePath)) {
                Resource resource = registry.get(resourcePath);
                if (resource.getContent() != null) {
                    if (resource.getContent() instanceof String) {
                        propValue = (String) resource.getContent();
                    } else if (resource.getContent() instanceof byte[]) {
                        propValue = new String((byte[]) resource.getContent());
                    }
                }
            } else {
                propValue = propName + " property doesn't exists";
            }
        } catch (RegistryException e) {
            propValue = "Unable to read the resource content";
        }
    } else {
        appKey = "";
        propName = "";
    }
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <title>Insert title here</title>
</head>
<body>
<h1>External API Sample</h1>
<br />
<form action="index.jsp" method="get">
    <p>
        Application key : <input type="text" name="appKey"
                                 value="<%=appKey%>" required></input>
    </p>
    <p>
        Property Name : <input type="text" name="propName"
                               value="<%=propName%>" required></input>
    </p>

    <input type="submit" value="Submit" />
    <p><%=propValue%></p>

</form>

<%

    if (propValue != null && propValue.length() > 0) {


        String values = "";
        HttpClient client = new HttpClient();
            GetMethod apiMethod = new GetMethod(propValue);

            int httpStatusCode = client.executeMethod(apiMethod);
            if (HttpStatus.SC_OK == httpStatusCode) {
                values = apiMethod.getResponseBodyAsString();
                values = StringUtils.replaceEach(values, new String[]{"&", "\"", "<", ">"}, new String[]{"&amp;", "&quot;", "&lt;", "&gt;"});
            } else {
                values = "Eror occurred invoking the service " + httpStatusCode;
            }


%>
<p><%="API Output\n " + values%></p>
<%
    }

%>

</body>
