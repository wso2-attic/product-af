This README file contains steps to try out the API Invoker sample.
API Invoker sample demonstrates how to invoke yahoo weather API and displays results in a Web application.

Prerequisite
------------
1. Register a domain(samples.com) using https://apps.appfactory.private.wso2.com:9443/cloudmgt/site/pages/register.jag.
        organization - samples
        domain - samples.com
        admin username - admin
2. Login to https://apps.appfactory.private.wso2.com:9443/appmgt as registered user(admin@samples.com)
3  Login to API Publisher via https://apimanager.appfactory.private.wso2.com:9449/publisher/ as tenant admin and go to the add section.
   Add API with the following information:

        Name - yahooweather
        Context - /yahooweather
        Version - 1.0.0

        Select GET and POST in the resource section and click Implement. Hit Save.
        Under the Endpoint section in the next window provide the Production and Sandbox URLs and click Manage.

        Production URL - http://weather.yahooapis.com/forecastrss?w=2442047&u=c
        Sandbox URL - http://weather.yahooapis.com/forecastrss?w=2502265
        
        Select Tier Availability as Unlimited and hit Save & Publish.

  After creating the API, go to the Life Cycle tab in the API Publisher. Then, select the State to PUBLISHED and update. 
  This publishes the API to App Factory's storefront so that application developers can find and subscribe to it.



Steps for Sample
----------------
1. Login into https://apps.appfactory.private.wso2.com:9443/appmgt as the registered user(admin@samples.com).
2. Create an application using link https://apps.appfactory.private.wso2.com:9443/appmgt/site/pages/createapplication.jag.
        Name - APIInvoker
        Key - apiinvoker
        Application Type - Java Web Application
3. Go to Repos and Build page and copy git URL(https://git.appfactory.private.wso2.com:8443/git/samples.com/apiinvoker.git) and clone it to your local directory(developer_workspace).
        git clone https://git.appfactory.private.wso2.com:8443/git/samples.com/apiinvoker.git
        You need to provide the username as admin@samples.com and password of admin user.
4. Copy WebContent directory to developer_workspace/apiinvoker. Copy src directory from sample location to developer_workspace/apiinvoker/src.
5. Commit and push the changes we did.
        cd developer_workspace/apiinvoker
        git add *
        git commit -m "Sample code"
        git push
6. Go to Runtime Configs --> APIs and click Go to API Manager.
7. The API Manager's storefront opens in a separate window: https://apimanager.appfactory.private.wso2.com/store. It lists all APIs published to the store, 
   including  yahooweather 1.0.0 API you created and published earlier.
8. Click the yahooweather-1.0.0 API to open its details. Select apiinvoker from Applications dropdown and Subscribe. 
9. After subscribing, go to 'My Subscriptions'. You will get options to generate access keys. Generate keys for sandbox and production separately.    
10. Refresh App Factory Runtime Configs --> APIs and hit 'Sync Keys' button. The generated keys will be appeared in the UI based on user's permissions.
11. Select the Repos and Builds tab. Build and Deploy APIInvoker application in the development environment by simply clicking the relevant buttons in the UI screen.

    API Manager Url must be HTTP: http://apimanager.appfactory.private.wso2.com:9769
    End point Url of the Yahoo Weather API: http://gateway.apimanager.appfactory.private.wso2.com:8286/t/samples.com/yahooweather/1.0.0
    Username & Password of the logged user

12. Submit the details to receive response with weather information printed as an XML file.
13. Promote the application to Production environment and view results.

Note: The results you get in development environment differ from the same in production environment, because the keys used by the two environments are different. 
The programmer is generally not aware of the sandbox and production keys. This is handled under the hood.


