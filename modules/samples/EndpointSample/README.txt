This README file contains steps to try out the Endpoint Sample.
Endpoint Sample will demonstrate following scenarios.
1. How to create an endpoint via Resource Properties.
2. How to access value of the endpoint.
3. How to change value of the endpoint in different lifecycle stages and access it via application.

Prerequisite
------------
1. Register a domain(samples.com) using https://apps.appfactory.private.wso2.com:9443/cloudmgt/site/pages/register.jag.
	organization - samples
	domain - samples.com
	admin username - admin
2. Login to https://apps.appfactory.private.wso2.com:9443/appmgt as registered user(admin@samples.com)
3. Import below members to your organization using link https://cloudmgt.appfactory.private.wso2.com:9443/cloudmgt/site/pages/user.jag
	devUser - Developer
	qaUser - QA
	devOpUser - DevOps 

Steps for Sample
----------------
1. Login into https://apps.appfactory.private.wso2.com:9443/appmgt as the registered user(admin@samples.com).
2. Create an application using link https://apps.appfactory.private.wso2.com:9443/appmgt/site/pages/createapplication.jag.
	Name - Endpoint 
	Key - endpoint
	Application Type - Java Web Application
3. Add members into your newly created application using Team section.
4. Login as devUser@samples.com
5. Go to Repos and Build page and copy git URL(https://git.appfactory.private.wso2.com:8443/git/samples.com/endpoint.git) and clone it to your local directory(developer_workspace).
	git clone https://git.appfactory.private.wso2.com:8443/git/samples.com/endpoint.git
	You need to provide the username as devUser@samples.com and password of devUser.
6. Copy EndpointSample//src/main/webapp/index.jsp file to developer_workspace/endpoint/src/main/webapp/.
7. Commit and push the changes we did.
	cd developer_workspace/endpoint
	git add *
	git commit -m "Sample code"
	git push
8. Go to Resources page and select Properties tab. Click on 'Add Property' and add below values.
Note: Only members with Developer role can create properties.
	Name - TimeZoneEndpoint
        Registry Environment - Development 
        Description - Example Endpoint created in Development environment
        Value - http://json-time.appspot.com/time.json?tz=US/Alaska
9. Go to Repos and Build page. Trigger a build. Once build is completed, click on Deploy. You can check the sample application by clicking on Launch. Enter Below values and Hit 'Submit'.
       Application Key - endpoint
       Property Name - TimeZoneEndpoint
    The value ''http://json-time.appspot.com/time.json?tz=US/Alaska' will be displayed in background.
10. Next create a branch from trunk as 1.0.0. Go to application home page and try to invoke the Launch button for 1.0.0 version.
11. Next go to Life-cycle Management page and select check boxes and click on Promote. This will promote the 1.0.0 version of application to Testing stage.
12. Logout and login as the qaUser@samples.com
13. In application home page, click on 'Accept and Deploy' button. This will deploy the 1.0.0 version in Testing stage and make a copy of TimeZoneEndpoint property to Testing stage. 
    If you access the link and follow step 9, you will notice the same results.
14. Go to Resources page and select Properties tab. Now you will be able to see the 'TimeZoneEndpoint' property created in both Development and Testing stages. 
    As a QA role, you can only view the value of 'TimeZoneEndpoint' property in Development stage and edit the value of 'TimeZoneEndpoint' porperty in Testing stage.
15. Edit the 'TimeZoneEndpoint' property value in Testing stage and set the below value.
       Value - http://json-time.appspot.com/time.json?tz=US/Pasific
16. Follow the step 9.
    The value ''http://json-time.appspot.com/time.json?tz=US/Pasific' will be displayed in background.
17. Simillarily you can try the Endpoint in Production stage with devOpUser user and check the results.

