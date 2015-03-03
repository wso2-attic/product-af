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

CURRENT_DIR=`pwd`
if [ -f `pwd`/config.sh ]; then
        source `pwd`/config.sh
else
        _echo_red "Unable to locate config.sh\n"
        exit 1
fi

function _clean_and_update() {
        PATH_TO_CLEAN=${1}
        cd ${PATH_TO_CLEAN}
        _echo_green "Cleaning untracked files and directories from ${PATH_TO_CLEAN}"
        git clean -df

        _echo_green "Stashing current changes..."
        git stash clear
        git stash
        git stash list

        _echo_green "Pulling changes from the main repo.."
        git pull origin

        read -p 'Do you want to apply the stashed changes?(y/n): ' response
        echo    # (optional) move to a new line
        if [[  $response =~ ^[Yy]$ ]]
        then
                git stash apply
                if [ $? -eq 0 ]; then
                    _echo_green "\nSuccessfully applied the stashed changes..."
                fi
        fi
        cd ${CURRENT_DIR}
}



cd $PUPPET_CONFIG_PATH
_clean_and_update $PUPPET_CONFIG_PATH

cd $CURRENT_DIR
if [ -f `pwd`/copy-jars.sh ]; then
        _echo_green "\nExecuting copy-jars.sh...\n"
        source `pwd`/copy-jars.sh
else
        _echo_red "Unable to locate copy-jars.sh\n"
        exit 1
fi

cd $CURRENT_DIR
if [ -f `pwd`/copy-patches.sh ]; then
        _echo_green "Executing copy-patches.sh..."
        source `pwd`/copy-patches.sh
else
        _echo_red "Unable to locate copy-patches.sh\n"
        exit 1
fi

#This is avoid manual changes to be done in puppet manifest files. We should be able to just modify single file and run the setup script.
cd $CURRENT_DIR
_echo_green "Changing puppet master IP in nodes.pp. Ignore sed: no input files error..."
grep -rl --exclude-dir="\.git" 192.168.18.237 "$PUPPET_CONFIG_PATH/manifests/nodes.pp" | xargs sed -i "s,192.168.18.237,${PUPPET_MASTER_IP},g"
grep -rl --exclude-dir="\.git" 192.168.18.250 "$PUPPET_CONFIG_PATH/manifests/nodes.pp" | xargs sed -i "s,192.168.18.250,${PUPPET_MASTER_IP},g"
   
