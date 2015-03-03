This README file contains steps to try out the Customer Portal Sample.
Customer Portal Sample will demonstrate following scenarios.
1. Consuming data from a database using data sources.
2. Consuming data from different databases when application lifecyle changes from Development to Testing environments.

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
	Name - Customer Portal
	Key - customerportal
	Application Type - Java Web Application
3. Add members into your newly created application using Team section.
4. Login as devUser@samples.com
5. Go to Repos and Build page and copy git URL(https://git.appfactory.private.wso2.com:8443/git/samples.com/customerportal.git) and clone it to your local directory(developer_workspace).
	git clone https://git.appfactory.private.wso2.com:8443/git/samples.com/customerportal.git
	You need to provide the username as devUser@samples.com and password of devUser.
6. Copy src directory from sample location to developer_workspace/customerportal/src.
	Copy WebContent/index.jsp to developer_workspace/customerportal/WebContent/index.jsp
7. Commit and push the changes we did.
	cd developer_workspace/customerportal
	git add *
	git commit -m "Sample code"
	git push
8. Go to Database section and add database in AppFactory portal.
	Name - CPDb
	Default User Password - Password
9. Populate data on above created database using a mysql client.
	mysql -uCPDb_0u0xsdLt -pPassword -hmysql-dev-01.appfactory.private.wso2.com CPDb_samples_com < CustomerPortal/dbscripts/dev-data.sql
10. Go to Runtime config section in Customer Portal App and add datasource.
	Datasource name - customer_ds
	Database URL - Select the database URL for CpDb database(jdbc:mysql://mysql-dev-01.appfactory.private.wso2.com:3306/CPDb_samples_com)
	Password - Password
11. Go to Repos and Build page. Trigger a build. Once build is completed, click on Deploy. You can check the sample application by clicking on Launch.
12. Next create a branch from trunk as 1.0.0. Go to application home page and try to invoke the Launch button for 1.0.0 version.
13. Next go to Life-cycle Management page and select check boxes and click on Promote. This will promote the 1.0.0 version of application to Testing stage.
14. Logout and login as the qaUser@samples.com
15. In application home page, click on 'Accept and Deploy' button. This will deploy the 1.0.0 version in Testing stage. If you access the link, you will notice the same application is deployed on testing server with data from CPDb database.
16. Create another database which contains QA dataset and update the datasource.
	Go to Databases section and add new Database.
		Name - CPQA
		Password - Password
	Import the data into CPQA database.
		mysql -uCPQA_0u0xsdLt -pPassword -hmysql-dev-01.appfactory.private.wso2.com CPQA_samples_com < CustomerPortal/dbscripts/prod-data.sql
	Update datasource with CPQA database details.
17. Launch the application again and you will notice data from CPQA database is rendered.
