WSO2-AS 5.2.1 Template for the Configurator
-------------------------------------------------------------------------------------

This template supports following configurations

1. Clustering AS
2. Fronting the AS cluster with WSO2 ELB

Following are the configuration parameters that is used by the template.
You can configure following in the ***module.ini*** file.

#### Read from environment variables :


    READ_FROM_ENVIRONMENT = false
 

-------------------------------------------------------------------------------------

#### Set the path of product directory :

    CARBON_HOME = <AS_HOME>

---

#### Enable clustering : 

    CONFIG_PARAM_CLUSTERING = true

* Used in - < AS_HOME >/repository/conf/axis2/axis2.xml

---

#### Set Membership Schema :

    CONFIG_PARAM_MEMBERSHIP_SCHEME = wka

* Used in - < AS_HOME >/repository/conf/axis2/axis2.xml

---
        
#### Set Domain :

    CONFIG_PARAM_DOMAIN = wso2.am.domain

* Used in - < AS_HOME >/repository/conf/axis2/axis2.xml

---

#### Well known members declaration :

    CONFIG_PARAM_WKA_MEMBERS = "127.0.0.1:4000,127.0.1.1:4001"

* Format - "ip_address1:port1,ip_address2:port2"
* Used in - < AS_HOME >/repository/conf/axis2/axis2.xml

---

#### Set Local Member Hostname and port :

    CONFIG_PARAM_LOCAL_MEMBER_HOST = 127.0.0.1
    CONFIG_PARAM_LOCAL_MEMBER_PORT = 4000

* Used in - < AS_HOME >/repository/conf/axis2/axis2.xml

---

### Set Port offset :

    CONFIG_PARAM_PORT_OFFSET = 0

* Used in - < AS_HOME >/repository/conf/carbon.xml

---
#### Set proxy ports when using a load balancer :

    CONFIG_PARAM_HTTP_PROXY_PORT = 80
    CONFIG_PARAM_HTTPS_PROXY_PORT = 443

* Used in - < AS_HOME >/repository/conf/tomcat/catalina-server.xml

---
#### Set worker/manger sub-domain in nodes  :

    CONFIG_PARAM_SUB_DOMAIN= worker

 * Used in - < AS_HOME >/repository/conf/axis2/axis2.xml
 * Used in - < AS_HOME >/repository/conf/carbon.xml
 * Used in - < AS_HOME >/repository/conf/registry.xml

---
#### Set worker and manager hostnames

    CONFIG_PARAM_WORKER_HOST_NAME = am.cloud-test.wso2.com
    CONFIG_PARAM_MGT_HOST_NAME = mgt.am.cloud-test.wso2.com

* Used in - < AS_HOME >/repository/conf/axis2/axis2.xml
* Used in - < AS_HOME >/repository/conf/carbon.xml

---

## Following are the config parameters used for setting up external database 
#### Set URL

    CONFIG_PARAM_URL= jdbc:mysql://localhost:3306/

#### Set Username

    CONFIG_PARAM_USER_NAME=root

#### Set Password
```
CONFIG_PARAM_PAMSWORD=root
```
#### Set Driver class name

    CONFIG_PARAM_DRIVER_CLAMS_NAME=com.mysql.jdbc.Driver

#### Set Max Active

    CONFIG_PARAM_MAX_ACTIVE=50

#### Set Max Wait

    CONFIG_PARAM_MAX_WAIT=60000

#### Set test on borrow

    CONFIG_PARAM_TEST_ON_BORROW=true

#### Set validation query
    CONFIG_PARAM_VALIDATION_QUERY=SELECT 1

#### Set validation interval

    CONFIG_PARAM_VALIDATION_INTERVAL=30000

#### Set Local Registry database

    CONFIG_PARAM_REGISTRY_LOCAL1="jdbc/WSO2CarbonDB:REGISTRY_LOCAL1"

#### Set Registry database

    CONFIG_PARAM_REGISTRY_DB="jdbc/WSO2RegistryDB:REGISTRY_DB"

#### Set datasource and shared user database

    CONFIG_PARAM_USER_DB="jdbc/WSO2UMDB:WSO2_USER_DB"

##### Used in 

* < AS_HOME >/repository/conf/user-mgt.xml
* < AS_HOME >/repository/conf/datasources/master-datasources.xml
* < AS_HOME >/repository/conf/registry.xml

###App Factory
└── repository
    └── components
        ├── dropins
        │   ├── activemq_broker_5.9.1_1.0.0.jar
        │   ├── activemq_client_5.9.1_1.0.0.jar
        │   ├── commons_collections_3.2.1_1.0.0.jar
        │   ├── commons_lang3_3.1_1.0.0.jar
        │   ├── geronimo_j2ee_management_1.1_spec_1.0.1_1.0.0.jar
        │   ├── geronimo_jms_1.1_spec_1.1.1_1.0.0.jar
        │   ├── hawtbuf_1.9_1.0.0.jar
        │   ├── jsch_0.1.49.wso2v1_1.0.0.jar
        │   ├── mysql_connector_java_5.1.27_bin_1.0.0.jar
        │   ├── nimbus-jose-jwt_2.26.1.wso2v2.jar
        │   ├── org.apache.stratos.common_4.1.1.jar
        │   ├── org.apache.stratos.messaging_4.1.1.jar
        │   ├── org.eclipse.jgit_2.3.1_wso2v2_1.0.0.jar
        │   ├── org.wso2.carbon.adc.repositoryinformation.service.stub_4.1.1.jar
        │   ├── org.wso2.carbon.appfactory.application.mgt.stub-2.2.0-SNAPSHOT.jar
        │   ├── org.wso2.carbon.appfactory.common-2.2.0-SNAPSHOT.jar
        │   ├── org.wso2.carbon.appfactory.eventing-2.2.0-SNAPSHOT.jar
        │   ├── org.wso2.carbon.appfactory.ext-2.2.0-SNAPSHOT.jar
        │   ├── org.wso2.carbon.deployment.synchronizer.git_4.1.1.jar
        │   ├── org.wso2.carbon.identity.authenticator.mutualssl_4.2.0.jar
        │   ├── org.wso2.carbon.logging.propfile_1.0.0.jar
        │   ├── org.wso2.carbon.social.core_1.1.0.jar
        │   └── signedjwt-authenticator_4.3.3.jar
        ├── lib
        │   ├── activemq-broker-5.9.1.jar
        │   ├── activemq-client-5.9.1.jar
        │   ├── commons-collections-3.2.1.jar
        │   ├── commons-lang3-3.1.jar
        │   ├── geronimo-j2ee-management_1.1_spec-1.0.1.jar
        │   ├── geronimo-jms_1.1_spec-1.1.1.jar
        │   ├── hawtbuf-1.9.jar
        │   ├── jsch_0.1.49.wso2v1.jar
        │   ├── mysql-connector-java-5.1.27-bin.jar
        │   └── org.eclipse.jgit_2.3.1-wso2v2.jar
        └── patches
            ├── patch0004
            │   ├── org.wso2.carbon.application.deployer_4.2.0.jar
            │   ├── org.wso2.carbon.core_4.2.0.jar
            │   ├── org.wso2.carbon.core.services_4.2.0.jar
            │   ├── org.wso2.carbon.ui_4.2.0.jar
            │   ├── org.wso2.carbon.user.core_4.2.0.jar
            │   └── org.wso2.carbon.utils_4.2.0.jar
            ├── patch0276
            │   └── org.wso2.carbon.server.admin_4.2.0.jar
            ├── patch0277
            │   └── org.wso2.carbon.usage.agent_2.2.0.jar
            ├── patch0318
            │   ├── org.wso2.carbon.service.mgt_4.2.1.jar
            │   └── org.wso2.carbon.webapp.mgt_4.2.2.jar
            ├── patch0400
            │   ├── org.wso2.carbon.dataservices.core-4.2.1.jar
            │   └── org.wso2.carbon.identity.authenticator.saml2.sso-4.2.0.jar
            ├── patch0495
            │   └── org.wso2.carbon.logging.service_4.2.1.jar
            ├── patch0506
            │   └── org.wso2.carbon.dataservices.core_4.2.1.jar
            ├── patch0844
            │   └── org.wso2.carbon.logging.service_4.2.1.jar
            └── patch0889
                └── org.wso2.carbon.webapp.mgt_4.2.2.jar
