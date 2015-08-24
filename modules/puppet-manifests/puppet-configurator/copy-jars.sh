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

# !/bin/bash

#set -o nounset

if [ -f `pwd`/config.sh ]; then
        source `pwd`/config.sh
else
        echo "Unable to locate config.sh"
        exit 1
fi

AF_HOME=${PACKS_DIR}/wso2appfactory-${NEW_VERSION}

#### Changes beyond this point, do ONLY if you know what you are doing ####
function _update_common_jars() {
        # $1 -> Array of jars
        # $2 -> WSO2 Service
        # $3 -> Old version to be removed
        # $4 -> New version to be copied

        declare -a LIST=("${!1}")
        MODULE_DEST=${2}
        OLD_VERSION=${3}
        NEW_VERSION=${4}

        for JAR in "${LIST[@]}"
        do
                # check whether the jar is an extension or a dropin
                if [ ! -z ${APPFAC_EXTS["$JAR"]} ] ; then
                    if [ -f ${AF_HOME}/resources/extensions/${JAR}-${NEW_VERSION} ] ; then
                        echo -e "\n[Plugin] Copying ${AF_HOME}/resources/extensions/${JAR}-${NEW_VERSION}.jar to ${MODULE_DEST}"
                        rm -f ${PUPPET_MODULES_HOME}/${MODULE_DEST}/${JAR}_${OLD_VERSION}
                        cp -f ${AF_HOME}/resources/extensions/${JAR}-${NEW_VERSION} ${PUPPET_MODULES_HOME}/${MODULE_DEST}/
                   else
                        echo -e "\nCloudn't find ${JAR}-${NEW_VERSION}.jar in ${AF_HOME}/resources/extensions/\n"
                   fi
                else
                    if ls ${AF_HOME}/repository/components/plugins/${JAR}${NEW_VERSION} 1> /dev/null 2>&1 ; then
                        echo -e "\n[Dropin] Copying ${AF_HOME}/repository/components/plugins/${JAR}${NEW_VERSION}.jar to ${MODULE_DEST}"
                        rm -f ${PUPPET_MODULES_HOME}/${MODULE_DEST}/${JAR}${OLD_VERSION}
                        cp -f ${AF_HOME}/repository/components/plugins/${JAR}${NEW_VERSION} ${PUPPET_MODULES_HOME}/${MODULE_DEST}/
                   else
                        echo -e "\nCloudn't find ${JAR}${NEW_VERSION}.jar in ${AF_HOME}/repository/components/plugins/\n"
                   fi
                fi
        done
}

function _update_jars() {
        # $1 -> Array of jars
        # $2 -> WSO2 Service
        # $3 -> Old version to be removed
        # $4 -> New version to be copied
        
        declare -a LIST=("${!1}")
        MODULE_DEST=${2}
        OLD_VERSION=${3}
        NEW_VERSION=${4}

        for JAR in "${LIST[@]}"
        do
                # check whether the jar is an extension or a dropin
                if [ ! -z ${APPFAC_EXTS["$JAR"]} ] ; then
                    if [ -f ${AF_HOME}/resources/extensions/${JAR}-${NEW_VERSION}.jar ] ; then
                        echo -e "\n[Plugin] Copying ${AF_HOME}/resources/extensions/${JAR}-${NEW_VERSION}.jar to ${MODULE_DEST}"
                        rm -f ${PUPPET_MODULES_HOME}/${MODULE_DEST}/${JAR}-${OLD_VERSION}.jar
 			cp -f ${AF_HOME}/resources/extensions/${JAR}-${NEW_VERSION}.jar ${PUPPET_MODULES_HOME}/${MODULE_DEST}/${JAR}-${NEW_VERSION}.jar
                   else
                        echo -e "\nCloudn't find ${JAR}-${NEW_VERSION}.jar in ${AF_HOME}/resources/extensions/\n"
                   fi
                else
                   NEW_VERSION2=${NEW_VERSION//[-]/.}
                   if [ -f ${AF_HOME}/repository/components/plugins/${JAR}_${NEW_VERSION2}.jar ] ; then
                        echo -e "\n[Dropin] Copying ${AF_HOME}/repository/components/plugins/${JAR}_${NEW_VERSION2}.jar to ${MODULE_DEST}"
     	                rm -f ${PUPPET_MODULES_HOME}/${MODULE_DEST}/${JAR}_${OLD_VERSION}.jar
                        cp -f ${AF_HOME}/repository/components/plugins/${JAR}_${NEW_VERSION2}.jar ${PUPPET_MODULES_HOME}/${MODULE_DEST}/${JAR}_${NEW_VERSION2}.jar
                   else
                        echo -e "\nCloudn't find ${JAR}_${NEW_VERSION}.jar in ${AF_HOME}/repository/components/plugins/\n"
                   fi
                fi
	done
}


if [ -f  ${PACKS_DIR}/wso2appfactory-${NEW_VERSION}.zip ]; then
        cd ${PACKS_DIR}
        echo -e "Extracting wso2appfactory-${NEW_VERSION} at `pwd`\n"
        unzip -q wso2appfactory-${NEW_VERSION}.zip
else
        echo "Couldn't find the source for migration :  wso2appfactory-${NEW_VERSION}.zip"
        exit 1
fi

echo "########### Copying appfac dropin jars ###########"

_update_jars BPS[@] bps/files/configs/repository/components/dropins ${OLD_VERSION} ${NEW_VERSION} 
_update_jars APPSERVER[@] paaspuppet/files/puppet/modules/appserver/files/configs/repository/components/dropins ${OLD_VERSION} ${NEW_VERSION} 
_update_jars GREGSERVER[@] greg/files/configs/repository/components/dropins ${OLD_VERSION} ${NEW_VERSION}
_update_jars STRATOS_MANAGER[@] privatepaas/files/appfactory_deployment/repository/components/dropins ${OLD_VERSION} ${NEW_VERSION}
#_update_jars JPPSERVER[@] jppserver/files/configs/repository/components/dropins ${OLD_VERSION} ${NEW_VERSION}
_update_jars STORAGE[@] storage/files/configs/repository/components/dropins ${OLD_VERSION} ${NEW_VERSION} 
#rm  ${PUPPET_MODULES_HOME}/jppserver/files/configs/lib/runtimes/jenkins/*
#_update_jars JPPSERVER_LIBS[@] jppserver/files/configs/lib/runtimes/jenkins ${OLD_VERSION} ${NEW_VERSION}

#updating commn jars

echo -e "\n########### Copying common jars ###########"

_update_common_jars BPS_COMMON[@] bps/files/configs/repository/components/dropins "*" "*" 
_update_common_jars MB_COMMON[@] messagebroker/files/configs/repository/components/dropins "*" "*" 
_update_common_jars SS_COMMON[@] storage/files/configs/repository/components/dropins "*" "*"
_update_common_jars AM_COMMON[@] apimanager/files/configs/repository/components/dropins "*" "*"
#_update_common_jars JPP_COMMON[@] jppserver/files/configs/repository/components/dropins "*" "*"
_update_common_jars GITBLIT_COMMON[@] gitblit/files/ext "*" "*"
_update_common_jars APPSERVER_COMMON[@] paaspuppet/files/puppet/modules/appserver/files/configs/repository/components/dropins "*" "*"
_update_common_jars GREGSERVER_COMMON[@] greg/files/configs/repository/components/dropins "*" "*"
_update_common_jars SM_COMMON[@] privatepaas/files/appfactory_deployment/repository/components/dropins "*" "*"


echo "[Jenkins] Creating jenkins.war"
mkdir -p ${PACKS_DIR}/tmp
cd ${PACKS_DIR}
unzip -q ${PACKS_DIR}/jenkins.war -d tmp

#pack jenkins plugins placed in resources folder to minimal jenkins.war
cp -rf ${PUPPET_MODULES_HOME}/jenkins/files/plugins/* tmp/WEB-INF/plugins

#removing existing appfactory-plugin
rm -rf tmp/WEB-INF/plugins/appfactory-plugin*.hpi
cp -rf ${AF_HOME}/resources/plugins/jenkins/appfactory-plugin*.hpi tmp/WEB-INF/plugins
cd tmp
zip -rq jenkins.war *
cd ..
cp tmp/jenkins.war ${PACKS_DIR}/
rm -rf tmp

#copying BPEL to BPS
echo " Coppying BPELS to bps "
rm -rf ${PUPPET_MODULES_HOME}/bps/files/configs/repository/deployment/server/bpel/*
cp -rf ${AF_HOME}/resources/bpels/* ${PUPPET_MODULES_HOME}/bps/files/configs/repository/deployment/server/bpel/

#copying BAM toolbox
echo " Coppying AF_Analytics toolbox to bam "
rm -rf ${PUPPET_MODULES_HOME}/bam/files/configs/repository/deployment/server/bam-toolbox/AF_Analytics.tbox
cp -rf ${AF_HOME}/resources/toolboxes/AF_Analytics.tbox ${PUPPET_MODULES_HOME}/bam/files/configs/repository/deployment/server/bam-toolbox/

#Copying CXODashboard to UES
rm -rf ${PUPPET_MODULES_HOME}/ues/files/configs/repository/deployment/server/jaggeryapps/CXODashboard
cp -rf ${AF_HOME}/resources/dashboards/CXODashboard ${PUPPET_MODULES_HOME}/ues/files/configs/repository/deployment/server/jaggeryapps/

#cleaning the AF extracted folder
rm -rf ${AF_HOME}

echo "Done!!"
