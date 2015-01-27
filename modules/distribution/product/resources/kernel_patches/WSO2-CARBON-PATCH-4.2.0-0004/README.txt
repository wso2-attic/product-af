Patch ID         :  WSO2-CARBON-PATCH-4.2.0-0004
Applies To       :  WSO2 CARBON-4.2.0
Associated JIRA  :  https://wso2.org/jira/browse/SS-269
		            https://wso2.org/jira/browse/CARBON-14578
		            https://wso2.org/jira/browse/CARBON-14543
		            https://wso2.org/jira/browse/IDENTITY-1843
                    https://wso2.org/jira/browse/IDENTITY-1844
                    https://wso2.org/jira/browse/IDENTITY-1884
                    https://wso2.org/jira/browse/CARBON-14600
                    https://wso2.org/jira/browse/CARBON-14602
                    https://wso2.org/jira/browse/CARBON-14611
                    https://wso2.org/jira/browse/IDENTITY-1927
                    https://wso2.org/jira/browse/IDENTITY-1961
                    https://wso2.org/jira/browse/IDENTITY-1966
                    https://wso2.org/jira/browse/CARBON-14579
		            https://wso2.org/jira/browse/IDENTITY-1970
                    https://wso2.org/jira/browse/CARBON-14620

DESCRIPTION
-----------
This patch provide fixes for above mentioned associated JIRAs. 

INSTALLATION INSTRUCTIONS
-------------------------

(i)  Shutdown the server, if you have already started.

(ii) Copy the wso2carbon-version.txt file to <CARBON_SERVER>/bin.

(iii) Copy the patch0004 to  <CARBON_SERVER>/repository/components/patches/

(iv)  Replace ciphertool.bat file on <CARBON_SERVER>/bin with file provided inside WSO2-CARBON-PATCH-4.2.0-0004/bin

(v)   Replace carbon.xml file on <CARBON_SERVER>/repository/conf with file provided inside WSO2-CARBON-PATCH-4.2.0-0004/repository/conf

(vi)  Copy cipher-tool.properties files on WSO2-CARBON-PATCH-4.2.0-0004/repository/resources/security to <CARBON_SERVER>/repository/resources/security

(vii) Replace org.wso2.carbon.server-4.2.0..jar file on <CARBON_SERVER>/lib with file provided inside WSO2-CARBON-PATCH-4.2.0-0004/lib

(viii) Restart the server with :
       Linux/Unix :  sh wso2server.sh
       Windows    :  wso2server.bat

