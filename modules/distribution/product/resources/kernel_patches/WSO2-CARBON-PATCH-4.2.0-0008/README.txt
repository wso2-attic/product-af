Patch ID	    :	WSO2-CARBON-PATCH-4.2.0-0008
Applies To	    :	WSO2 CARBON-4.2.0
Associated JIRA	:   https://wso2.org/jira/browse/CARBON-14769
                    https://wso2.org/jira/browse/CARBON-14796
                    https://wso2.org/jira/browse/IDENTITY-2385
                    https://wso2.org/jira/browse/REGISTRY-2159
                    https://wso2.org/jira/browse/IDENTITY-2457
                    https://wso2.org/jira/browse/IDENTITY-2016
                    https://wso2.org/jira/browse/IDENTITY-2464
                    https://wso2.org/jira/browse/IDENTITY-2483
                    https://wso2.org/jira/browse/IDENTITY-2281
                    https://wso2.org/jira/browse/CARBON-14795
                    https://wso2.org/jira/browse/IDENTITY-2504

DESCRIPTION
-----------
This patch provide fixes for above mentioned associated JIRAs. 

INSTALLATION INSTRUCTIONS
-------------------------

(i)   Shutdown the server, if you have already started.

(ii)  Copy the wso2carbon-version.txt file to <CARBON_SERVER>/bin.

(iii) Copy the patch0008 to  <CARBON_SERVER>/repository/components/patches/

(vi)  Copy cipher-tool.properties and authenticators.xml files on WSO2-CARBON-PATCH-4.2.0-0008/repository/conf/security to <CARBON_SERVER>/repository/conf/security

(v)  Restart the server with :
      	Linux/Unix :  sh wso2server.sh
    	Windows    :  wso2server.bat

