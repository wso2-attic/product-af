# AppFacCLI
Google Summer of Code project to create a CLI tool for App Factory

# Build the tool from source
01. Checkout the tool at "https://github.com/wso2/product-af/modules/tools/AppFacCLI"
02. Run the 'build' script in 'bin' directory.
03. executable 'appfac' gets generated in a new 'out' directory.

# Available commands
-Login / Logout             - List Applications of user
- Create new application    - List application versions
- Delete application        - Get application info
- Create artifact           - Get build and deploy status for application
- Triiger Build             - Print build logs

#Sample commands

> appfac   ( Lists help for all the available commands )

help                                  ,h              ,Shows help for appfac CLI tool

printBuildLogs                        ,pl             ,Prints logs for a given build

logout                                ,lo             ,Logout from a user session

deleteApplication                     ,da             ,Deletes an application of user

login                                 ,l              ,Login to app factory

getApplicationsOfUser                 ,la             ,Lists applications of a user

getAppVersionsInStage                 ,lv             ,Lists versions of an application in a stage

getBuildAndDeployStatusForVersion     ,bs             ,Get last build success details of a particular version of an 
application

triggerBuild                          ,tb             ,Triggers a build for an app, waits until its success and 

displays build logs

createNewApplication                  ,cap            ,Creates a new application for user

getAppInfo                            ,ai             ,Get information of an application

createArtifact                        ,car            ,Creates an artifact of an application

setBaseUrl                            ,setBaseUrl     ,Sets base url for the tool

# Set base url
Before you start using the tool, the base url for the tool needs to be set.
e.g. : https://apps.cloud.wso2.com

> The built 'appfac' executable can be used even without installing 'GO'
