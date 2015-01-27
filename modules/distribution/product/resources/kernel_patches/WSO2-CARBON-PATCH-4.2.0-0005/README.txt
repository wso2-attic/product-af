Patch ID         :  WSO2-CARBON-PATCH-4.2.0-0005
Applies To       :  WSO2 CARBON-4.2.0
Associated JIRA  :  https://wso2.org/jira/browse/DS-844
                    https://wso2.org/jira/browse/CARBON-14635
		            https://wso2.org/jira/browse/ESBJAVA-2906
		            https://wso2.org/jira/browse/ESBJAVA-2909
                    https://wso2.org/jira/browse/ESBJAVA-2824
                    https://wso2.org/jira/browse/ESBJAVA-2951
                    https://wso2.org/jira/browse/CARBON-14643
                    https://wso2.org/jira/browse/BPS-458

DESCRIPTION
-----------
This patch provide fixes for above mentioned associated JIRAs. 

INSTALLATION INSTRUCTIONS
-------------------------

(i)  Shutdown the server, if you have already started.

(ii) Copy the wso2carbon-version.txt file to <CARBON_SERVER>/bin.

(iii) Copy the patch0005 to  <CARBON_SERVER>/repository/components/patches/

(iv) Replace client-truststore.jks file on <CARBON_SERVER>/repository/resources/security with file provided inside WSO2-CARBON-PATCH-4.2.0-0005/repository/resources/security/

(v) Restart the server with :
       Linux/Unix :  sh wso2server.sh
       Windows    :  wso2server.bat

