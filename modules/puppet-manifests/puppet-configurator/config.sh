#
# Copyright 2005-2014 WSO2, Inc. (http://wso2.com)
#
#      Licensed under the Apache License, Version 2.0 (the "License");
#      you may not use this file except in compliance with the License.
#      You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#      Unless required by applicable law or agreed to in writing, software
#      distributed under the License is distributed on an "AS IS" BASIS,
#      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#      See the License for the specific language governing permissions and
#      limitations under the License.
#


#!/bin/bash
PUPPET_MASTER_IP=192.168.18.237
PACKS_DIR=/var/www/software
SOURCE_PATH=/home/ubuntu/product-af
PUPPET_MODULES_HOME=${SOURCE_PATH}/modules/puppet-manifests/appfactory/modules
PUPPET_CONFIG_PATH=${SOURCE_PATH}/modules/puppet-manifests/appfactory
OLD_VERSION="2.1.0"
NEW_VERSION="2.2.0-SNAPSHOT"
RESET_CLR='\033[00;00m'
RED="\033[33;31m"
GREEN="\033[33;32m"
MAGENTA="\033[33;35m"


# appfactory jars
declare -A APPFAC_EXTS=(["org.wso2.carbon.appfactory.build.stub"]=1 ["org.wso2.carbon.appfactory.multitenant.jenkins"]=1 ["org.wso2.carbon.appfactory.application.deployer.stub"]=1 ["org.wso2.carbon.appfactory.bps.ext"]=1 ["org.wso2.carbon.appfactory.jenkinsext"]=1 ["org.wso2.carbon.appfactory.ext"]=1 ["org.wso2.carbon.appfactory.stratos.listners"]=1 ["org.wso2.carbon.appfactory.resource.mgt"]=1)
declare -a BPS=("org.wso2.carbon.appfactory.common" "org.wso2.carbon.appfactory.bps.ext");
declare -a JPPSERVER=("org.wso2.carbon.appfactory.core" "org.wso2.carbon.appfactory.common" "org.wso2.carbon.appfactory.eventing" "org.wso2.carbon.appfactory.multitenant.jenkins" "org.wso2.carbon.appfactory.repository.mgt.service" "org.wso2.carbon.appfactory.s4.integration");
declare -a APPSERVER=("org.wso2.carbon.appfactory.common" "org.wso2.carbon.appfactory.eventing" "org.wso2.carbon.appfactory.ext" "org.wso2.carbon.appfactory.application.mgt.stub");
declare -a GREGSERVER=("org.wso2.carbon.appfactory.common" "org.wso2.carbon.appfactory.eventing" "org.wso2.carbon.appfactory.resource.mgt" "org.wso2.carbon.appfactory.custom.userstore");
declare -a STRATOS_MANAGER=("org.wso2.carbon.appfactory.custom.userstore" "org.wso2.carbon.appfactory.stratos.listners" "org.wso2.carbon.appfactory.common" "org.wso2.carbon.appfactory.eventing" "org.wso2.carbon.appfactory.s4.integration");
declare -a STORAGE=("org.wso2.carbon.appfactory.common");
declare -a JPPSERVER_LIBS=("org.wso2.carbon.appfactory.jenkinsext" "org.wso2.carbon.appfactory.s4.integration" "org.wso2.carbon.appfactory.build.stub" "org.wso2.carbon.appfactory.application.deployer.stub")

## common jars
declare -a MB_COMMON=("signedjwt-authenticator" "nimbus-jose-jwt");
declare -a SS_COMMON=("signedjwt-authenticator" "nimbus-jose-jwt");
declare -a AM_COMMON=("signedjwt-authenticator" "nimbus-jose-jwt");
declare -a JPP_COMMON=("signedjwt-authenticator" "nimbus-jose-jwt");
declare -a BPS_COMMON=("nimbus-jose-jwt");
declare -a GITBLIT_COMMON=("nimbus-jose-jwt");
declare -a APPSERVER_COMMON=("signedjwt-authenticator" "nimbus-jose-jwt");
declare -a GREGSERVER_COMMON=("signedjwt-authenticator" "nimbus-jose-jwt");
declare -a SM_COMMON=("signedjwt-authenticator" "nimbus-jose-jwt");

## patches
declare -a AM_PATCHES=();
declare -a AF_PATCHES=("patch0001" "patch0004" "patch0132" "patch0133" "patch1091");
declare -a AS_PATCHES=("patch0028" "patch0132" "patch0133" "patch9999");
declare -a GREG_PATCHES=("patch0008");
declare -a ELB_PATCHES=("patch0001" "patch0027" "patch0236" "patch8000");
declare -a IS_PATCHES=("patch0031" "patch0137");
declare -a JPP_PATCHES=("patch0028");
declare -a PAAS_AS_PATCHES=("patch0004" "patch0276" "patch0277" "patch0318" "patch0400" "patch0495" "patch0506" "patch0844" "patch0889");
declare -a SS_PATCHES=("patch0209" "patch0260" "patch0274" "patch0298" "patch0320" "patch0350" "patch0351" "patch1085");
declare -a STRATOS_INSTALLER_PATCHES=("patch0003" "patch0006" "patch0007");
#declare -a STRATOS_INSTALLER_CONFIG_PATCHES=("patch0900");
declare -a TASK_SERVER=("patch0097" "patch0113");
declare -a BAM_PATCHES=("patch0401" "patch0623");

## runtimes
declare -a PAAS_AS_SPRING_RUNTIME=("aopalliance-1.0.jar" "spring-beans-4.1.5.RELEASE.jar" "spring-expression-4.1.5.RELEASE.jar" "spring-jms-4.1.5.RELEASE.jar"
                                   "spring-test-4.1.5.RELEASE.jar" "spring-webmvc-portlet-4.1.5.RELEASE.jar" "commons-logging-1.1.1.jar" "spring-context-4.1.5.RELEASE.jar"
                                   "spring-instrument-4.1.5.RELEASE.jar" "spring-messaging-4.1.5.RELEASE.jar" "spring-tx-4.1.5.RELEASE.jar" "spring-websocket-4.1.5.RELEASE.jar"
                                   "spring-aop-4.1.5.RELEASE.jar" "spring-context-support-4.1.5.RELEASE.jar" "spring-instrument-tomcat-4.1.5.RELEASE.jar" "spring-orm-4.1.5.RELEASE.jar"
                                   "spring-web-4.1.5.RELEASE.jar" "spring-aspects-4.1.5.RELEASE.jar" "spring-core-4.1.5.RELEASE.jar" "spring-jdbc-4.1.5.RELEASE.jar" "spring-oxm-4.1.5.RELEASE.jar"
                                   "spring-webmvc-4.1.5.RELEASE.jar");

## dropins
declare -a AF_DROPINS=("org.apache.commons.lang3_3.1.0.jar" "org.apache.stratos.common_4.1.3.jar" "org.apache.stratos.messaging_4.1.3.jar" "org.wso2.carbon.andes.stub-4.2.1.jar" "org.wso2.carbon.cloudmgt.users_1.1.0.jar" "org.wso2.carbon.issue.tracker-2.0.0.jar" "org.wso2.carbon.registry.metadata-1.0.0-SNAPSHOT.jar");
declare -a BAM_DROPINS=("org.wso2.carbon.logging.summarizer_4.2.0.jar");
declare -a GREG_DROPINS=("andes-client-0.13.wso2v10.jar" "org.apache.commons.lang3_3.1.0.jar" "org.apache.stratos.common_4.1.3.jar" "org.apache.stratos.messaging_4.1.3.jar" "org.wso2.carbon.andes.stub-4.2.1.jar");
declare -a MB_DROPINS=("org.wso2.carbon.identity.authenticator.mutualssl-4.2.0.jar");
declare -a AS_DROPINS=("org.apache.stratos.common_4.1.3.jar" "org.apache.stratos.messaging_4.1.3.jar" "org.wso2.carbon.adc.repositoryinformation.service.stub_4.1.1.jar" "org.wso2.carbon.deployment.synchronizer.git_4.1.1.jar" "org.wso2.carbon.identity.authenticator.mutualssl_4.2.0.jar" "org.wso2.carbon.logging.propfile_1.0.0.jar" "org.wso2.carbon.social.core_1.1.0.jar" "org.wso2.carbon.registry.ws.api_4.2.0.jar" "org.wso2.carbon.registry.ws.client_4.2.0.jar" "org.wso2.carbon.registry.ws.stub_4.2.0.jar");
declare -a SM_DROPINS=("gitblit_1.2.0.wso2v1.jar" "org.eclipse.jgit_2.1.0.wso2v1.jar" "org.wso2.carbon.adc.reponotification.service.stub-4.1.1.jar" "org.wso2.carbon.andes.stub-4.2.1.jar" "org.wso2.carbon.identity.authenticator.mutualssl_4.2.0.jar" "org.wso2.carbon.registry.ws.api_4.2.0.jar" "org.wso2.carbon.registry.ws.client_4.2.0.jar" "org.wso2.carbon.registry.ws.stub_4.2.0.jar" "org.wso2.carbon.um.ws.api.stub_4.2.1.jar" "org.wso2.carbon.um.ws.api_4.2.1.jar" "org.wso2.carbon.um.ws.service_4.2.1.jar" "org.wso2.carbon.user.mgt.common_4.2.0.jar");

## libs
declare -a AF_LIBS=("activemq-broker-5.9.1.jar" "activemq-client-5.9.1.jar" "andes_0.13.0.wso2v8.jar" "geronimo-j2ee-management_1.1_spec-1.0.1.jar" "geronimo-jms_1.1_spec-1.1.1.jar" "hawtbuf-1.9.jar" "jsch-0.1.51.jar");
declare -a GREG_LIBS=("geronimo-j2ee-management_1.1_spec-1.0.1.jar" "jackson-core-asl_1.9.2.jar" "jackson-mapper-asl_1.9.2.jar");
declare -a AS_LIBS=("activemq-broker-5.9.1.jar" "activemq-client-5.9.1.jar" "commons-collections-3.2.1.jar" "commons-lang3-3.1.jar" "geronimo-j2ee-management_1.1_spec-1.0.1.jar" "geronimo-jms_1.1_spec-1.1.1.jar" "hawtbuf-1.9.jar" "jsch_0.1.49.wso2v1.jar" "mysql-connector-java-5.1.27-bin.jar" "org.eclipse.jgit_2.3.1-wso2v2.jar");

function _echo_red() {
    MSG=${1}
    echo -e "${RED}${MSG}${RESET_CLR}"
}

function _echo_green() {
    MSG=${1}
    echo -e "${GREEN}${MSG}${RESET_CLR}"
}
