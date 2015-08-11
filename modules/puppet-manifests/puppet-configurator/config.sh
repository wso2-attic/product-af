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
declare -A APPFAC_EXTS=(["org.wso2.carbon.appfactory.build.stub"]=1 ["org.wso2.carbon.appfactory.multitenant.jenkins"]=1 ["org.wso2.carbon.appfactory.application.deployer.stub"]=1 ["org.wso2.carbon.appfactory.bps.ext"]=1 ["org.wso2.carbon.appfactory.jenkinsext"]=1 ["org.wso2.carbon.appfactory.ext"]=1 ["org.wso2.carbon.appfactory.stratos.listners"]=1)
declare -a BPS=("org.wso2.carbon.appfactory.common" "org.wso2.carbon.appfactory.bps.ext");
declare -a JPPSERVER=("org.wso2.carbon.appfactory.core" "org.wso2.carbon.appfactory.common" "org.wso2.carbon.appfactory.eventing" "org.wso2.carbon.appfactory.multitenant.jenkins" "org.wso2.carbon.appfactory.repository.mgt.service" "org.wso2.carbon.appfactory.s4.integration");
declare -a APPSERVER=("org.wso2.carbon.appfactory.common" "org.wso2.carbon.appfactory.eventing" "org.wso2.carbon.appfactory.ext" "org.wso2.carbon.appfactory.application.mgt.stub");
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
declare -a SM_COMMON=("signedjwt-authenticator" "nimbus-jose-jwt");

## patches
declare -a AM_PATCHES=();
declare -a AF_PATCHES=("patch0001" "patch0004" "patch0132" "patch0133" "patch1091");
declare -a AS_PATCHES=("patch0028" "patch0132" "patch0133" "patch9999");
declare -a ELB_PATCHES=("patch0001" "patch0027" "patch0236" "patch8000");
declare -a IS_PATCHES=("patch0031" "patch0137");
declare -a JPP_PATCHES=("patch0028");
declare -a PAAS_AS_PATCHES=("patch0004" "patch0276" "patch0277" "patch0318" "patch0400" "patch0495" "patch0506" "patch0844" "patch0889");
declare -a SS_PATCHES=("patch0209" "patch0260" "patch0274" "patch0298" "patch0320" "patch0350" "patch0351" "patch1085");
declare -a STRATOS_INSTALLER_PATCHES=("patch0003" "patch0006" "patch0007");
#declare -a STRATOS_INSTALLER_CONFIG_PATCHES=("patch0900");
declare -a TASK_SERVER=("patch0097" "patch0113");
declare -a BAM_PATCHES=("patch0401" "patch0623");

function _echo_red() {
    MSG=${1}
    echo -e "${RED}${MSG}${RESET_CLR}"
}

function _echo_green() {
    MSG=${1}
    echo -e "${GREEN}${MSG}${RESET_CLR}"
}
