<!DOCTYPE html>
<!--[if lt IE 7]>      <html class="no-js lt-ie9 lt-ie8 lt-ie7"> <![endif]-->
<!--[if IE 7]>         <html class="no-js lt-ie9 lt-ie8"> <![endif]-->
<!--[if IE 8]>         <html class="no-js lt-ie9"> <![endif]-->
<!--[if gt IE 8]><!-->
<html class="no-js"> <!--<![endif]-->
<!--
~ Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
~
~ WSO2 Inc. licenses this file to you under the Apache License,
~ Version 2.0 (the "License"); you may not use this file except
~ in compliance with the License.
~ You may obtain a copy of the License at
~
~ http://www.apache.org/licenses/LICENSE-2.0
~
~ Unless required by applicable law or agreed to in writing,
~ software distributed under the License is distributed on an
~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~ KIND, either express or implied. See the License for the
~ specific language governing permissions and limitations
~ under the License.
-->

<%@ page import="org.wso2.carbon.appfactory.login.config.ServiceReferenceHolder" %>
<%@ page import="org.wso2.carbon.appfactory.common.AppFactoryConstants" %>
<%@ page import="org.wso2.carbon.appfactory.login.config.SSOConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIMessage" %>
<%@ page import="org.wso2.stratos.identity.saml2.sso.mgt.ui.Util" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar"
           prefix="carbon" %>
<html lang="en">
  <head>

      <meta charset="utf-8">
      <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
      <meta name="description" content="">
      <meta name="viewport" content="width=device-width">

      <link rel="stylesheet" href="../appfactory/assets/css/font-awesome.min.css">
      <!--[if IE 7]>
      <link rel="stylesheet" href="../appfactory/assets/css/font-awesome-ie7.min.css">
      <![endif]-->
      <link rel="stylesheet" href="../appfactory/assets/css/normalize.min.css">
      <link type="text/css" rel="stylesheet" href="../appfactory/assets/css/jquery.qtip.min.css" />
      <link rel="stylesheet" href="../appfactory/assets/css/start.css">
      <link rel="stylesheet" href="../appfactory/assets/css/development.css">

      <script src="../appfactory/assets/js/vendor/modernizr-2.6.2-respond-1.1.0.min.js"></script>





      <title>WSO2 App Factory</title>
      <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
      <!--[if lt IE 9]>
      <script src="../appfactory/assets/js/vendor/html5/html5.js"></script>
      <![endif]-->
      <script type="text/javascript"  src="../appfactory/assets/js/vendor/jquery-1.7.1.min.js"></script>

      
      <script type="text/javascript">
  	var _gaq = _gaq || [];
  	_gaq.push(['_setAccount', 'UA-XXXXXXXX']);
  	_gaq.push(['_trackPageview']);

  	(function() {
    	var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;
    	ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';
    	var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);
  	})();

     function loginError(msg){
        $('#loginError').html(msg).show();
        //setInterval(function() {$('#messageBlock').html("").hide();},7000);
     }
      </script>

      <!-- Le fav and touch icons -->
      <link rel="shortcut icon" href="../appfactory/images/favicon.png">
      <style>
	.signUpLink{
	    color:black;
	    text-decoration:none;
	}
	label.error, .alert-error{
	    color:#FF0000;
	    font-size:12px;
	    padding-top:5px;
	}
	#loginError{
	    padding-bottom:20px;
	    margin-top:-20px;
	    font-weight:bold;
	}
      </style>
  </head>
  
  

  <body>
      
    <fmt:bundle basename="org.wso2.carbon.appfactory.login.ui.i18n.Resources">
    <%
        String regLink =  ServiceReferenceHolder.getInstance().getAppFactoryConfiguration().getFirstProperty(AppFactoryConstants.REGISTRATION_LINK);
        String signUpText =  ServiceReferenceHolder.getInstance().getAppFactoryConfiguration().getFirstProperty("SignUpText");
        String signInText =  ServiceReferenceHolder.getInstance().getAppFactoryConfiguration().getFirstProperty("SignInText");
        String errorMessage = "error.message";
        String tenantRegistrationPageURL = Util.getTenantRegistrationPageURL();

      
        
        if (request.getParameter(SSOConstants.AUTH_FAILURE) != null &&
            Boolean.parseBoolean(request.getParameter(SSOConstants.AUTH_FAILURE))) {
            if(request.getParameter(SSOConstants.AUTH_FAILURE_MSG) != null){
                errorMessage = (String) request.getParameter(SSOConstants.AUTH_FAILURE_MSG);
            }        
    %>
    <script type="text/javascript">
        $(document).ready(function() {
            loginError('<fmt:message key="<%=errorMessage%>"/>');
        });
    </script>
    <%
        } else if(request.getAttribute("urn:oasis:names:tc:SAML:2.0:status:Requester") !=null){
            session.invalidate();
            errorMessage = "error.message.session.timeout";
    %>
       <script type="text/javascript">
           $(document).ready(function() {
               loginError('<fmt:message key="<%=errorMessage%>"/>');
           });
       </script>
       <%
        }  else if (request.getSession().getAttribute(CarbonUIMessage.ID) !=null) {
            CarbonUIMessage carbonMsg = (CarbonUIMessage)request.getSession().getAttribute(CarbonUIMessage.ID);
            %>

                <script type="text/javascript">
                    $(document).ready(function() {
                        loginError("<%=carbonMsg.getMessage()%>");
                    });
                </script>
      <%}
    %>
    <script type="text/javascript">
        function doLogin() {
            var loginForm = document.getElementById('loginForm');
            loginForm.submit();
        }
        function doRegister() {
            document.getElementById('registrationForm').submit();
        }
    </script>
    <!--
    START Header back ground
    No real content is here just to display the head
    -->




        <!--[if lt IE 7]>
        <p class="chromeframe">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade your browser</a> or <a href="http://www.google.com/chromeframe/?redirect=true">activate Google Chrome Frame</a> to improve your experience.</p>
        <![endif]-->


        <div class="wrapper">
            <div class="branding">
                <h1><img src="../appfactory/assets/img/appfactory.png" alt="App Factory" /></h1>
                <p>Revolutionizing App Delivery</p>
            </div>
            <article class="start">
                <header class="start_header">
		        <a class="signUpLink" href="<%=regLink%>" target="_blank"><h2 class="account"><%=signUpText%>.</h2></a>
			
	     	    <!-- TODO make the right logic to handle the condition above -->
                </header>
                <section class="start_content">
                    <div class="alert alert-error error" style="display: none" id="loginError"></div>
                    <form  action="../../commonauth" method="post"   id="loginForm">
                        <div class="input_row">
                            <label for="username"><fmt:message key='username'/></label>
                            <input type="text" id="username" name="username" class="required email" />


                            <input type="hidden" name="<%=SSOConstants.SESSION_DATA_KEY%>"
                                   value="<%=request.getParameter(SSOConstants.SESSION_DATA_KEY)%>"/>

                        </div>
                        <div class="input_row">
                            <label for="password"><fmt:message key='password'/></label>
                            <input type="password" id="password" name="password" class="required"  />
                        </div>
                        <div class="input_row btn_row">
                            <button class="btn" type="submit">Sign In</button> <a href="<%=regLink%>" class="link">Register</a>
                        </div>

                    </form>
                </section>
            </article>
        </div><!-- /wrapper -->
        <footer></footer>
        
        <script type="text/javascript"  src="../appfactory/assets/js/start.js"></script>







    <script src="../appfactory/assets/js/vendor/jquery-validation/jquery.validate.min.js"></script>
    <script type="text/javascript">
        $(document).ready(function(){
           $("#loginForm").validate({
			invalidHandler: function(event, validator) {
				$('#loginError').hide();
			}
		   });

           $("#loginForm #username").rules('add', {
					messages:{
							email:'<fmt:message key="error.message.invalid.username"/>'
                    }
            }); 		   
         });
    </script>

    </fmt:bundle>

  </body>
</html>
