<%@ page import="org.wso2.carbon.context.CarbonContext" %>
<%@ page import="org.wso2.carbon.context.RegistryType" %>
<%@ page import="org.wso2.carbon.registry.core.Registry" %>
<%@ page import="org.wso2.carbon.registry.core.Resource" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>
<%@ page import="org.apache.commons.httpclient.HttpClient" %>
<%@ page import="org.apache.commons.httpclient.HttpStatus" %>
<%@ page import="org.apache.commons.httpclient.methods.GetMethod" %>
<%@ page import="org.apache.commons.httpclient.methods.PostMethod" %>
<%@ page import="sun.misc.BASE64Encoder" %>
<%@page import="java.util.logging.Logger" %>
<%@ page import="static java.lang.System.setProperty" %>
<%@ page import="java.security.Security" %>
<%@ page import="static java.lang.System.clearProperty" %>
<%@ page import="static java.lang.System.getProperties" %>
<%@ page import="static java.lang.System.*" %>
<%@ page import="org.json.simple.parser.JSONParser" %>
<%@ page import="org.json.simple.JSONObject" %>

<%
    String endpoint = request.getParameter("endpoint");
    String consumerkey = request.getParameter("consumerkey");
    String secretkey = request.getParameter("seckey");
    String apiManagerUrl = request.getParameter("apiManagerUrl");
    String username = request.getParameter("username");
    String password = request.getParameter("password");


    if (endpoint == null) {
        endpoint = "";
        consumerkey = "";
        secretkey = "";
        apiManagerUrl = "";
        username = "";
        password = "";
    } else {
        String applicationName = pageContext.getServletContext().getInitParameter("applicationKey");
        CarbonContext cCtx = CarbonContext.getThreadLocalCarbonContext();
        Registry registry = (Registry) cCtx.getRegistry(RegistryType.SYSTEM_GOVERNANCE);

        Resource resource = registry.get("dependencies/" + applicationName + "/ConsumerKey");
        if (resource.getContent() instanceof String) {
            consumerkey = (String) resource.getContent();
        } else {
            consumerkey = new String((byte[]) resource.getContent());
        }

        resource = registry.get("dependencies/" + applicationName + "/ConsumerSecret");
        if (resource.getContent() instanceof String) {
            secretkey = (String) resource.getContent();
        } else {
            secretkey = new String((byte[]) resource.getContent());
        }
    }
%>

<html>
<body>
<% Logger logger = Logger.getLogger(this.getClass().getName());%>
<h2>Calling API</h2>

<form action="index.jsp" method="get">

    <p>API Manager Url : <input type="text" name="apiManagerUrl" value="<%=apiManagerUrl%>"></input></p>

    <p>Endpoint URL : <input type="text" name="endpoint" value="<%=endpoint%>"></input></p>

    <p>Username : <input type="text" name="username" value="<%=username%>"></input>

    <p>Password : <input type="password" name="password" value="<%=password%>"></input>

        <input type="submit" value="Invoke"/>
</form>
<%
    String applicationName = pageContext.getServletContext().getInitParameter("applicationKey");
    String accessToken = "";
    if (endpoint != null && endpoint.length() > 0) {

        //Get the token endpoint
        logger.info("endpoint OK");
        String submitUrl = apiManagerUrl.trim()+"/oauth2/token";
        String apiEndPoint = endpoint.trim();
        logger.info("Submit URL : " + submitUrl);
        logger.info("End point" + endpoint);
        String applicationToken = consumerkey + ":" + secretkey;
        logger.info("Consumer Secret : " + secretkey);
        logger.info("Consumer Key : " + consumerkey);
        logger.info("token : " + applicationToken);
        BASE64Encoder base64Encoder = new BASE64Encoder();
        applicationToken = "Basic " + base64Encoder.encode(applicationToken.getBytes()).trim();
        logger.info("Application Token : " + applicationToken);
        HttpClient client = new HttpClient();
        logger.info("Http client created");
        PostMethod method = new PostMethod(submitUrl);
        logger.info("Post method created");
        method.addRequestHeader("Authorization", applicationToken);
        logger.info("Adding request header authorization");
        method.addRequestHeader("Content-Type", "application/x-www-form-urlencoded");
        logger.info("Adding request header Content-Type");
        method.addParameter("grant_type", "password");
        logger.info("Adding prameters grant_type");
        method.addParameter("username", username);
        logger.info("Add parameters username : " + username);
        method.addParameter("password", password);
        logger.info("Add parameters password : " + password);
        String carbon_home = getProperty("carbon.home");
        logger.info("CARBON_HOME : " + carbon_home);
        System.setProperty("javax.net.ssl.trustStore", carbon_home + "/repository/resources/security/client-truststore.jks");

        String values = "";
        int httpStatusCode = 0;
        try {
            logger.info("Ready to execute http request..");
            httpStatusCode = client.executeMethod(method);
            logger.info("Http status code : " + httpStatusCode);
            String accessTokenJson = "";

            if (HttpStatus.SC_OK == httpStatusCode) {
                logger.info("http status ok - 1");
                accessTokenJson = method.getResponseBodyAsString();
                logger.info("Json : " + accessTokenJson);
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(accessTokenJson);
                JSONObject jsonObject = (JSONObject) obj;

                accessToken = (String) jsonObject.get("access_token");
                String refreshToken = (String) jsonObject.get("refresh_token");
                out.println("Access Token Received - " + accessToken);
                GetMethod apiMethod = new GetMethod(apiEndPoint);
                apiMethod.addRequestHeader("Authorization", "Bearer " + accessToken);

                logger.info("Api Endpoint : " + apiEndPoint);
                httpStatusCode = client.executeMethod(apiMethod);
                if (HttpStatus.SC_OK == httpStatusCode) {
                    logger.info("http status ok - 2");
                    values = apiMethod.getResponseBodyAsString();

                } else {
                    logger.info("http status bad - 2");
                    values = "Error occurred invoking the service " + httpStatusCode;
                }

            } else {
                logger.info("http status bad - 1");
                values = "Error occurred invoking the service \n Http status : " + httpStatusCode;

            }

        } catch (Exception ex) {
            logger.info("http status bad - 3 : " + httpStatusCode);
            values = "Error occurred invoking the service \n Http status : " + ex;
        }

%>
<p><%="API Output\n " + values%>
</p>
<%
    }

%>
</body>
</html>
