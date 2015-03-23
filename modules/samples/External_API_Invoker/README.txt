This README file contains steps to try out the External API Invoker Sample.
External API Invoker Sample will demonstrate following scenarios.
1. How to create an External API via Runtime Configs.
2. How to access the values of the external API.
3. How to change the values of the external APIs and other related attributes in different lifecycle stages and access it via application.

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
	Name - ExternalAPIInvoker 
	Key - externalapiinvoker
	Application Type - Java Web Application
3. Add members into your newly created application using Team section.
4. Login as devUser@samples.com
5. Go to Repos and Build page and copy git URL(https://git.appfactory.private.wso2.com:8443/git/samples.com/externalapiinvoker.git) and clone it to your local directory(developer_workspace).
	git clone https://git.appfactory.private.wso2.com:8443/git/samples.com/externalapiinvoker.git
	You need to provide the username as devUser@samples.com and password of devUser.
6. Copy src directory from sample location to developer_workspace/externalapiinvoker/src.
	Copy WebContent directory to developer_workspace/externalapiinvoker.
7. Commit and push the changes we did.
	cd developer_workspace/externalapiinvoker
	git add *
	git commit -m "Sample code"
	git push
8. Go to 'Runtimes Configs' page and select APIs tab. Click on 'Add External API' and add below values.
Note: Only members with Developer role can create properties.
	Name - TimezoneAPI
        Environment - Development 
        Authentication - None
        API URL - http://json-time.appspot.com/time.json?tz=US/Alaska
9. Go to Repos and Build page. Trigger a build. Once build is completed, click on Deploy. You can check the sample application by clicking on Launch. Enter Below values and Hit 'Submit'.
       Application Key - externalapiinvoker
       Property Name - TimezoneAPI
    The value 'http://json-time.appspot.com/time.json?tz=US/Alaska' will be displayed in background.
10. Next create a branch from trunk as 1.0.0. Go to application home page and try to invoke the Launch button for 1.0.0 version.
11. Next go to Life-cycle Management page and select check boxes and click on Promote. This will promote the 1.0.0 version of application to Testing stage.
12. Logout and login as the qaUser@samples.com
13. In application home page, click on 'Accept and Deploy' button. This will deploy the 1.0.0 version in Testing stage and make a copy of TimezoneAPI property to Testing stage. 
    If you access the link and follow step 9, you will notice the same results.
14. Go to Runtime Configs page and select APIs tab. Now you will be able to see the 'TimezoneAPI' created in both Development and Testing stages. 
    As a QA role, you can only view the value of 'TimezoneAPI' in Development stage and edit the value of 'TimezoneAPI' in Testing stage.
15. Edit the 'TimezoneAPI'  value in Testing stage and set the below value.
       API URL - http://json-time.appspot.com/time.json?tz=US/Pasific
16. Follow the step 9.
    The value 'http://json-time.appspot.com/time.json?tz=US/Pasific' will be displayed in background.
17. Similarly you can try the External API in Production stage with devOpUser user and check the results.

