This README file contains steps to try out the User Realm Sample.
User Realm Sample will demonstrate following scenarios how to create and launch your own application that calls WSO2 Carbon User Management APIs and saves/deletes users to/from the LDAP user stores of App Factory.

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
	Name - UserRealmSample 
	Key - userrealmsample
	Application Type - Java Web Application
3. Add members into your newly created application using Team section.
4. Login as devUser@samples.com
5. Go to Repos and Build page and copy git URL(https://git.appfactory.private.wso2.com:8443/git/samples.com/userrealmsample.git) and clone it to your local directory(developer_workspace).
	git clone https://git.appfactory.private.wso2.com:8443/git/samples.com/userrealmsample.git
	You need to provide the username as devUser@samples.com and password of devUser.
6. Copy src directory from sample location to $developer_workspace/userrealmsample/src.
	Copy WebContent/index.jsp to developer_workspace/customerportal/WebContent/index.jsp
	Open the pom.xml file in the folder and copy the values of the following elements:
		<groupId>
		<artifactId>
		<version>
		<packaging>
	Copy pom.xml of the sample to $developer_workspace/userrealmsample/pom.xml. Replace the values of above copied elements in the $developer_workspace/userrealmsample/pom.xml
7. Commit and push the changes we did.
	cd developer_workspace/userrealmsample
	git add *
	git commit -m "Sample code"
	git push
8. Go to Repos and Build page. Trigger a build. Once build is completed, click on Deploy. You can check the sample application by clicking on Launch. 
	You will see two links for 
		UserStore Manager sample 
		Authorization Manager sample
9. Click on UserStore Manager sample. This will allow you to add new users and new roles to your tenant domain. 
10. Click on Authorization Manager sample. This will allow you to add resources or deny resources to particular roles you added in UserStore Manager sample. 


