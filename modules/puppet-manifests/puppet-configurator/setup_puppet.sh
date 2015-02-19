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
PUPPET_CONFIG_PATH=/home/ubuntu/product-af/modules/puppet-manifests/appfactory/
cd $PUPPET_CONFIG_PATH
sudo git clean -df
cd $CURRENT_DIR

if [ -f `pwd`/config.sh ]; then
        source `pwd`/config.sh
else
        _echo_red "Unable to locate config.sh\n"
        exit 1
fi

cd $CURRENT_DIR
if [ -f `pwd`/copy-jars.sh ]; then
        _echo_green "Executing copy-jars.sh...\n"
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
   
