#
# Copyright 2015 WSO2, Inc. (http://wso2.com)
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
        _echo_red "\nUnable to locate config.sh"
        exit 1
fi

#AF_SETUP_PATCHES=af_setup_patches-${NEW_VERSION}
#AF_ARTIFACTS=af_artifacts
AF_ARTIFACTS_HOME=${PACKS_DIR}/af_artifacts
PATCHES_HOME=${AF_ARTIFACTS_HOME}/patches
#LIB_HOME=${AF_ARTIFACTS_HOME}/lib
#DROPINS_HOME=${AF_ARTIFACTS_HOME}/dropins
ERROR_OCCURED=false

#### Changes beyond this point, do ONLY if you know what you are doing ####
function _update_patches() {
        # $1 -> Array of patches
        # $2 -> source directory
        # $3 -> module destination directory

        declare -a LIST=("${!1}")
        SOURCE_DIR=${2}
        DEST_DIR=${3}

        for PATCH_FOLDER in "${LIST[@]}"
        do
                if [ -d "${PATCHES_HOME}/${SOURCE_DIR}/${PATCH_FOLDER}" ]; then
                    echo -e "\n${MAGENTA}[Patches][${SOURCE_DIR}]${RESET_CLR} Copying ${PATCH_FOLDER} to ${PUPPET_MODULES_HOME}/${DEST_DIR}/"
                    if [ -d "${PUPPET_MODULES_HOME}/${DEST_DIR}" ]; then
                        rm -rf ${PUPPET_MODULES_HOME}/${DEST_DIR}/${PATCH_FOLDER}
                        cp -f -r ${PATCHES_HOME}/${SOURCE_DIR}/${PATCH_FOLDER} ${PUPPET_MODULES_HOME}/${DEST_DIR}/
                    else
                        ERROR_OCCURED=true
                        _echo_red "\n[Patches][${SOURCE_DIR}] Destination folder ${PUPPET_MODULES_HOME}/${DEST_DIR} does not exists."
                    fi
                else
                    ERROR_OCCURED=true
                    _echo_red "\n[Patches][${SOURCE_DIR}] Cloudn't find ${PATCH_FOLDER} inside the extracted ${AF_SETUP_PATCHES}/${SOURCE_DIR}."
                fi
        done
}

function _update_dropins() {
        # $1 -> Array of dropins
        # $2 -> source directory
        # $3 -> module destination directory

        declare -a LIST=("${!1}")
        SOURCE_DIR=${2}
        DEST_DIR=${3}

        for DROPIN_JAR in "${LIST[@]}"
        do
                if [ -f "${AF_ARTIFACTS_HOME}/${SOURCE_DIR}/${DROPIN_JAR}" ]; then
                    echo -e "\n${MAGENTA}[Dropins][${SOURCE_DIR}]${RESET_CLR} Copying ${DROPIN_JAR} to ${PUPPET_MODULES_HOME}/${DEST_DIR}/"
                    if [ -d "${PUPPET_MODULES_HOME}/${DEST_DIR}" ]; then
#                        rm -rf ${PUPPET_MODULES_HOME}/${DEST_DIR}/${DROPIN_JAR}
                        cp -f ${AF_ARTIFACTS_HOME}/${SOURCE_DIR}/${DROPIN_JAR} ${PUPPET_MODULES_HOME}/${DEST_DIR}/
                    else
                        ERROR_OCCURED=true
                        _echo_red "\n[Dropins][${SOURCE_DIR}] Destination folder ${PUPPET_MODULES_HOME}/${DEST_DIR} does not exists."
                    fi
                else
                    ERROR_OCCURED=true
                    _echo_red "\n[Dropins][${SOURCE_DIR}] Cloudn't find ${DROPIN_JAR} inside the extracted ${AF_ARTIFACTS_HOME}/${SOURCE_DIR}."
                fi
        done
}

function _update_libs() {
        # $1 -> Array of libs
        # $2 -> source directory
        # $3 -> module destination directory

        declare -a LIST=("${!1}")
        SOURCE_DIR=${2}
        DEST_DIR=${3}

        for LIB_JAR in "${LIST[@]}"
        do
                if [ -f "${AF_ARTIFACTS_HOME}/${SOURCE_DIR}/${LIB_JAR}" ]; then
                    echo -e "\n${MAGENTA}[Lib][${SOURCE_DIR}]${RESET_CLR} Copying ${LIB_JAR} to ${PUPPET_MODULES_HOME}/${DEST_DIR}/"
                    if [ -d "${PUPPET_MODULES_HOME}/${DEST_DIR}" ]; then
#                        rm -rf ${PUPPET_MODULES_HOME}/${DEST_DIR}/${LIB_JAR}
                        cp -f ${AF_ARTIFACTS_HOME}/${SOURCE_DIR}/${LIB_JAR} ${PUPPET_MODULES_HOME}/${DEST_DIR}/
                    else
                        ERROR_OCCURED=true
                        _echo_red "\n[Lib][${SOURCE_DIR}] Destination folder ${PUPPET_MODULES_HOME}/${DEST_DIR} does not exists."
                    fi
                else
                    ERROR_OCCURED=true
                    _echo_red "\n[Lib][${SOURCE_DIR}] Cloudn't find ${LIB_JAR} inside the extracted ${AF_ARTIFACTS_HOME}/${SOURCE_DIR}."
                fi
        done
}

#if [ -f  ${PACKS_DIR}/${AF_SETUP_PATCHES}.zip ]; then
#        cd ${PACKS_DIR}
#        _echo_green "Extracting ${AF_SETUP_PATCHES}.zip at `pwd`"
#        unzip -q ${AF_SETUP_PATCHES}.zip
#else
#        ERROR_OCCURED=true
#        _echo_red "\nCouldn't find the source for patch migration :  ${AF_SETUP_PATCHES}.zip"
#        exit 1
#fi

_update_patches AF_PATCHES[@] appfactory appfactory/files/patches/repository/components/patches
_update_patches AM_PATCHES[@] apimanager apimanager/files/patches/repository/components/patches
_update_patches AS_PATCHES[@] as as/files/patches/repository/components/patches
_update_patches GREG_PATCHES[@] greg greg/files/patches/repository/components/patches
_update_patches ELB_PATCHES[@] elb elb/files/patches/repository/components/patches
_update_patches IS_PATCHES[@] identity identity/files/patches/repository/components/patches
_update_patches SS_PATCHES[@] storage storage/files/patches/repository/components/patches
_update_patches STRATOS_INSTALLER_PATCHES[@] stratos-installer privatepaas/files/appfactory_deployment/repository/components/patches
#_update_patches STRATOS_INSTALLER_CONFIG_PATCHES[@] stratos-installer-config privatepaas/files/stratos/stratos-installer/config/all/repository/components/patches
_update_patches TASK_SERVER[@] taskserver taskserver/files/patches/repository/components/patches
_update_patches BAM_PATCHES[@] bam bam/files/patches/repository/components/patches

_update_dropins AF_DROPINS[@] dropins appfactory/files/configs/repository/components/dropins
_update_dropins BAM_DROPINS[@] dropins bam/files/configs/repository/components/dropins
_update_dropins GREG_DROPINS[@] dropins greg/files/configs/repository/components/dropins
_update_dropins MB_DROPINS[@] dropins messagebroker/files/configs/repository/components/dropins
_update_dropins AS_DROPINS[@] dropins paaspuppet/files/puppet/modules/appserver/files/configs/repository/components/dropins
_update_dropins SM_DROPINS[@] dropins privatepaas/files/appfactory_deployment/repository/components/dropins

_update_libs AF_LIBS[@] lib appfactory/files/configs/repository/components/lib
_update_libs GREG_LIBS[@] lib greg/files/configs/repository/components/lib
_update_libs SM_LIBS[@] lib privatepaas/files/appfactory_deployment/repository/components/lib

#cleaning the extracted patches folder
#rm -rf ${PATCHES_HOME}

if [ ${ERROR_OCCURED} = true ]; then
    _echo_red "\nError occurred while copying some patches. Please check the logs!"
else
    _echo_green "\nSuccessfully copied all the patches!"
fi

