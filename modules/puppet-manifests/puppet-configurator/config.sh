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
PACKS_DIR=/var/www/software
PUPPET_MODULES_HOME=/home/ubuntu/product-af/modules/puppet-manifests/appfactory/modules
OLD_VERSION="2.1.0"
NEW_VERSION="2.1.0-SNAPSHOT"

# appfactory jars
declare -A APPFAC_EXTS=(["org.wso2.carbon.appfactory.build.stub"]=1 ["org.wso2.carbon.appfactory.multitenant.jenkins"]=1 ["org.wso2.carbon.appfactory.application.deployer.stub"]=1 ["org.wso2.carbon.appfactory.bps.ext"]=1 ["org.wso2.carbon.appfactory.jenkinsext"]=1 ["org.wso2.carbon.appfactory.ext"]=1 ["org.wso2.carbon.appfactory.stratos.listners"]=1)
declare -a BPS=("org.wso2.carbon.appfactory.common" "org.wso2.carbon.appfactory.bps.ext");
declare -a JPPSERVER=("org.wso2.carbon.appfactory.core" "org.wso2.carbon.appfactory.common" "org.wso2.carbon.appfactory.eventing" "org.wso2.carbon.appfactory.multitenant.jenkins" "org.wso2.carbon.appfactory.repository.mgt.service" );
declare -a APPSERVER=("org.wso2.carbon.appfactory.common" "org.wso2.carbon.appfactory.eventing" "org.wso2.carbon.appfactory.ext" "org.wso2.carbon.appfactory.application.mgt.stub");
declare -a STRATOS_MANAGER=("org.wso2.carbon.appfactory.stratos.listners" "org.wso2.carbon.appfactory.common");
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

