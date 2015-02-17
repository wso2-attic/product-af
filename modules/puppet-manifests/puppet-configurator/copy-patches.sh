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
        echo "Unable to locate config.sh"
        exit 1
fi

AF_SETUP_PATCHES=af_setup_patches-${NEW_VERSION}
PATCHES_HOME=${PACKS_DIR}/${AF_SETUP_PATCHES}

#### Changes beyond this point, do ONLY if you know what you are doing ####
function _update_patches() {
        # $1 -> Array of patches
        # $2 -> source directory
        # $3 -> module destination directory

        declare -a LIST=("${!1}")
        PATCH_MODULE_DIR=${2}
        MODULE_DEST=${3}

        for PATCH_FOLDER in "${LIST[@]}"
        do
                if [ -d "${PATCHES_HOME}/${PATCH_MODULE_DIR}/${PATCH_FOLDER}" ]; then
                    echo -e "\n[Patches][${PATCH_MODULE_DIR}] Copying ${PATCH_FOLDER} to ${PUPPET_MODULES_HOME}/${MODULE_DEST}/"
                    rm -rf ${PUPPET_MODULES_HOME}/${MODULE_DEST}/${PATCH_FOLDER}
                    cp -f -r ${PATCHES_HOME}/${PATCH_MODULE_DIR}/${PATCH_FOLDER} ${PUPPET_MODULES_HOME}/${MODULE_DEST}/
                else
                    echo -e "\nCloudn't find ${PATCH_FOLDER} inside the extracted ${AF_SETUP_PATCHES}/${PATCH_MODULE_DIR}\n"
                fi
        done
}

if [ -f  ${PACKS_DIR}/${AF_SETUP_PATCHES}.zip ]; then
        cd ${PACKS_DIR}
        echo -e "Extracting ${AF_SETUP_PATCHES}.zip at `pwd`\n"
        unzip -q ${AF_SETUP_PATCHES}.zip
else
        echo "Couldn't find the source for patch migration :  ${AF_SETUP_PATCHES}.zip"
        exit 1
fi

_update_patches AF_PATCHES[@] appfactory appfactory/files/patches/repository/components/patches
_update_patches AM_PATCHES[@] apimanager apimanager/files/patches/repository/components/patches
_update_patches AS_PATCHES[@] as as/files/patches/repository/components/patches
_update_patches ELB_PATCHES[@] elb elb/files/patches/repository/components/patches
_update_patches IS_PATCHES[@] identity identity/files/patches/repository/components/patches
_update_patches JPP_PATCHES[@] jppserver jppserver/files/patches/repository/components/patches
_update_patches PAAS_AS_PATCHES[@] paas_as paaspuppet/files/puppet/modules/appserver/files/patches/repository/components/patches
_update_patches SS_PATCHES[@] storage storage/files/patches/repository/components/patches
_update_patches STRATOS_INSTALLER_PATCHES[@] stratos-installer privatepaas/files/appfactory_deployment/repository/components/patches
_update_patches STRATOS_INSTALLER_CONFIG_PATCHES[@] stratos-installer-config privatepaas/files/stratos/stratos-installer/config/all/repository/components/patches
_update_patches TASK_SERVER[@] taskserver taskserver/files/patches/repository/components/patches

#cleaning the AF extracted folder
rm -rf ${PATCHES_HOME}

echo "Done!!"

