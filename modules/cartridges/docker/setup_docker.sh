#
# Copyright 2015  WSO2, Inc. (http://wso2.com)
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

CODE_PATH=/home/punnadi/wso2/git_code_base/git/fork_af/product-af
PUPPET_CONFIG_HOME=${CODE_PATH}/modules/puppet-manifests/puppet-configurator
DOCKER_HOME=${CODE_PATH}/modules/cartridges/docker
AF_ARTIFACTS_HOME=/home/punnadi/wso2/other/RND/AppFactory/af_artifacts
PATCHES_DIR=${AF_ARTIFACTS_HOME}/patches

if [ -f ${PUPPET_CONFIG_HOME}/config.sh ]; then
        source ${PUPPET_CONFIG_HOME}/config.sh
else
        _echo_red "Unable to locate config.sh\n"
        exit 1
fi

PACK_HOME=${CODE_PATH}/modules/distribution/product/target
PACKS_DIR=${PACK_HOME}/wso2appfactory-${NEW_VERSION}

if [ -f  ${PACK_HOME}/wso2appfactory-${NEW_VERSION}.zip ]; then
        cd ${PACK_HOME}
        if [ -d ${PACKS_DIR} ]; then
            rm -rf ${PACKS_DIR}
        fi
        echo -e "Extracting wso2appfactory-${NEW_VERSION} at `pwd`\n"
        unzip -q wso2appfactory-${NEW_VERSION}.zip
else
        echo "Couldn't find the source for migration :  wso2appfactory-${NEW_VERSION}.zip"
        exit 1
fi

function _clean_and_update() {
        PATH_TO_CLEAN=${1}
        cd ${PATH_TO_CLEAN}
        _echo_green "Cleaning untracked files and directories from ${PATH_TO_CLEAN}"
        sudo git clean -df
}

cd $DOCKER_HOME
_clean_and_update $DOCKER_HOME

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
                    if [ -f ${PACKS_DIR}/resources/extensions/${JAR}-${NEW_VERSION} ] ; then
                        echo -e "\n[Plugin] Copying ${PACKS_DIR}/resources/extensions/${JAR}-${NEW_VERSION}.jar to ${MODULE_DEST}"
                        rm -f ${DOCKER_HOME}/${MODULE_DEST}/${JAR}_${OLD_VERSION}
                        cp -f ${PACKS_DIR}/resources/extensions/${JAR}-${NEW_VERSION} ${DOCKER_HOME}/${MODULE_DEST}/
                   else
                        echo -e "\nCloudn't find ${JAR}-${NEW_VERSION}.jar in ${PACKS_DIR}/resources/extensions/\n"
                   fi
                else
                    if ls ${PACKS_DIR}/repository/components/plugins/${JAR}${NEW_VERSION} 1> /dev/null 2>&1 ; then
                        echo -e "\n[Dropin] Copying ${PACKS_DIR}/repository/components/plugins/${JAR}${NEW_VERSION}.jar to ${MODULE_DEST}"
                        rm -f ${DOCKER_HOME}/${MODULE_DEST}/${JAR}${OLD_VERSION}
                        cp -f ${PACKS_DIR}/repository/components/plugins/${JAR}${NEW_VERSION} ${DOCKER_HOME}/${MODULE_DEST}/
                   else
                        echo -e "\nCloudn't find ${JAR}${NEW_VERSION}.jar in ${PACKS_DIR}/repository/components/plugins/\n"
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
                    if [ -f ${PACKS_DIR}/resources/extensions/${JAR}-${NEW_VERSION}.jar ] ; then
                        echo -e "\n[Plugin] Copying ${AF_HOME}/resources/extensions/${JAR}-${NEW_VERSION}.jar to ${MODULE_DEST}"
                        rm -f ${DOCKER_HOME}/${MODULE_DEST}/${JAR}-${OLD_VERSION}.jar
 			            cp -f ${PACKS_DIR}/resources/extensions/${JAR}-${NEW_VERSION}.jar ${DOCKER_HOME}/${MODULE_DEST}/${JAR}-${NEW_VERSION}.jar
                   else
                        echo -e "\nCloudn't find ${JAR}-${NEW_VERSION}.jar in ${PACKS_DIR}/resources/extensions/\n"
                   fi
                else
                   NEW_VERSION2=${NEW_VERSION//[-]/.}
                   if [ -f ${PACKS_DIR}/repository/components/plugins/${JAR}_${NEW_VERSION2}.jar ] ; then
                        echo -e "\n[Dropin] Copying ${PACKS_DIR}/repository/components/plugins/${JAR}_${NEW_VERSION2}.jar to ${MODULE_DEST}"
     	                rm -f ${DOCKER_HOME}/${MODULE_DEST}/${JAR}_${OLD_VERSION}.jar
                        cp -f ${PACKS_DIR}/repository/components/plugins/${JAR}_${NEW_VERSION2}.jar ${DOCKER_HOME}/${MODULE_DEST}/${JAR}_${NEW_VERSION2}.jar
                   else
                        echo -e "\nCloudn't find ${JAR}_${NEW_VERSION}.jar in ${PACKS_DIR}/repository/components/plugins/\n"
                   fi
                fi
	done
}

_update_jars APPSERVER[@] wso2as-5.2.1/templates-module/files/repository/components/dropins ${OLD_VERSION} ${NEW_VERSION}
_update_common_jars APPSERVER_COMMON[@] wso2as-5.2.1/templates-module/files/repository/components/dropins "*" "*"

function _update_patches() {
        # $1 -> Array of patches
        # $2 -> source directory
        # $3 -> module destination directory

        declare -a LIST=("${!1}")
        SOURCE_DIR=${2}
        DEST_DIR=${3}

        for PATCH_FOLDER in "${LIST[@]}"
        do
                if [ -d "${PATCHES_DIR}/${SOURCE_DIR}/${PATCH_FOLDER}" ]; then
                    echo -e "\n${MAGENTA}[Patches][${SOURCE_DIR}]${RESET_CLR} Copying ${PATCH_FOLDER} to ${DOCKER_HOME}/${DEST_DIR}/"
                    if [ -d "${DOCKER_HOME}/${DEST_DIR}" ]; then
                        rm -rf ${DOCKER_HOME}/${DEST_DIR}/${PATCH_FOLDER}
                        cp -f -r ${PATCHES_DIR}/${SOURCE_DIR}/${PATCH_FOLDER} ${DOCKER_HOME}/${DEST_DIR}/
                    else
#                        ERROR_OCCURED=true
                        _echo_red "\n[Patches][${SOURCE_DIR}] Destination folder ${DOCKER_HOME}/${DEST_DIR} does not exists."
                    fi
                else
#                    ERROR_OCCURED=true
                    _echo_red "\n[Patches][${SOURCE_DIR}] Cloudn't find ${PATCH_FOLDER} inside the extracted ${PATCHES_DIR}/${SOURCE_DIR}."
                fi
        done
}

_update_patches PAAS_AS_PATCHES[@] paas_as wso2as-5.2.1/templates-module/files/repository/components/patches

function _copy_files() {
        # $1 -> Array of dropins
        # $2 -> source directory
        # $3 -> module destination directory

        declare -a LIST=("${!1}")
        SOURCE_DIR=${2}
        DEST_DIR=${3}

        for FILE in "${LIST[@]}"
        do
                if [ -f "${AF_ARTIFACTS_HOME}/${SOURCE_DIR}/${FILE}" ]; then
                    echo -e "\n${MAGENTA}[Files][${SOURCE_DIR}]${RESET_CLR} Copying ${FILE} to ${DOCKER_HOME}/${DEST_DIR}/"
                    if [ -d "${DOCKER_HOME}/${DEST_DIR}" ]; then
                        cp -f ${AF_ARTIFACTS_HOME}/${SOURCE_DIR}/${FILE} ${DOCKER_HOME}/${DEST_DIR}/
                    else
#                        ERROR_OCCURED=true
                        _echo_red "\n[Files][${SOURCE_DIR}] Destination folder ${DOCKER_HOME}/${DEST_DIR} does not exists."
                    fi
                else
#                    ERROR_OCCURED=true
                    _echo_red "\n[Files][${SOURCE_DIR}] Cloudn't find ${DROPIN_JAR} inside the extracted ${AF_ARTIFACTS_HOME}/${SOURCE_DIR}."
                fi
        done
}


_copy_files AS_DROPINS[@] dropins wso2as-5.2.1/templates-module/files/repository/components/dropins
_copy_files AS_LIBS[@] lib wso2as-5.2.1/templates-module/files/repository/components/lib
_copy_files PAAS_AS_SPRING_RUNTIME[@] spring_4.1.5_release wso2as-5.2.1/templates-module/files/lib/runtimes/spring_4.1.5_release

declare -a BASE_IMAGE=("apache-stratos-python-cartridge-agent-4.1.1.zip" "jdk-7u60-linux-x64.tar.gz" "ppaas-configurator-4.1.0-SNAPSHOT.zip");
declare -a WSO2AS_IMAGE=("jdk-7u60-linux-x64.tar.gz" "mysql-connector-java-5.1.27-bin.jar" "wso2as-5.2.1.zip");

_copy_files BASE_IMAGE[@] docker base-image/packages
_copy_files WSO2AS_IMAGE[@] docker wso2as-5.2.1/packages


cd ${PACK_HOME}
if [ -d ${PACKS_DIR} ]; then
    rm -rf ${PACKS_DIR}
fi

_echo_green "DONE"


