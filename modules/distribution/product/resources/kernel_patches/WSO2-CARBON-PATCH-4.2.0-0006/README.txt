Patch ID	:	WSO2-CARBON-PATCH-4.2.0-0006
Applies To	:	WSO2 CARBON-4.2.0
Associated JIRA	:	https://wso2.org/jira/browse/IDENTITY-2019
			https://wso2.org/jira/browse/CARBON-14651
			https://wso2.org/jira/browse/CARBON-14654
			https://wso2.org/jira/browse/ESBJAVA-2051
			https://wso2.org/jira/browse/CARBON-14677
			https://wso2.org/jira/browse/CARBON-14565
			https://wso2.org/jira/browse/ESBJAVA-3010
			https://wso2.org/jira/browse/ESBJAVA-1614
			https://wso2.org/jira/browse/IDENTITY-2044
			https://wso2.org/jira/browse/CARBON-14704
			https://wso2.org/jira/browse/CARBON-14522
			https://wso2.org/jira/browse/CARBON-14728
			https://wso2.org/jira/browse/CARBON-14729
			https://wso2.org/jira/browse/CARBON-14726
			https://wso2.org/jira/browse/CARBON-14730
			https://wso2.org/jira/browse/IDENTITY-2056
			https://wso2.org/jira/browse/CARBON-14738
			https://wso2.org/jira/browse/CARBON-14747
			https://wso2.org/jira/browse/CARBON-14745
			https://wso2.org/jira/browse/BPS-497
		        https://wso2.org/jira/browse/CARBON-14743
	           	https://wso2.org/jira/browse/IDENTITY-2053
			https://wso2.org/jira/browse/DS-870
			https://wso2.org/jira/browse/CARBON-14750
			https://wso2.org/jira/browse/CARBON-14751
			https://wso2.org/jira/browse/CARBON-14752
            https://wso2.org/jira/browse/CARBON-14753
            https://wso2.org/jira/browse/CARBON-14748
            https://wso2.org/jira/browse/CARBON-14754
            https://wso2.org/jira/browse/DS-875
            https://wso2.org/jira/browse/DS-876

DESCRIPTION
-----------
This patch provide fixes for above mentioned associated JIRAs. 

INSTALLATION INSTRUCTIONS
-------------------------

(i)  Shutdown the server, if you have already started.

(ii) Copy the wso2carbon-version.txt file to <CARBON_SERVER>/bin.

(iii) Copy the patch0006 to  <CARBON_SERVER>/repository/components/patches/

(iv) Replace axis2.xml, axis2_client.xml and tenant-axis2.xml files on <CARBON_SERVER>/repository/conf/axis2 with file provided inside WSO2-CARBON-PATCH-4.2.0-0006/repository/conf/axis2

(v) Replace carbon.xml file on <CARBON_SERVER>/repository/conf with file provided inside WSO2-CARBON-PATCH-4.2.0-0006/repository/conf

(vi)  Replace org.wso2.carbon.server-4.2.0.jar file on <CARBON_SERVER>/lib with file provided inside WSO2-CARBON-PATCH-4.2.0-0006/lib

(vii) Replace org.wso2.ciphertool-1.0.0-wso2v2.jar file on <CARBON_SERVER>/lib with file provided inside WSO2-CARBON-PATCH-4.2.0-0006/lib

(viii) Restart the server with :
       Linux/Unix :  sh wso2server.sh
       Windows    :  wso2server.bat

