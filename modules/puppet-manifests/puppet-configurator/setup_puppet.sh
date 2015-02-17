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
        echo "Unable to locate config.sh"
        exit 1
fi

cd $CURRENT_DIR
if [ -f `pwd`/copy-jars.sh ]; then
        echo "Executing copy-jars.sh..."
        source `pwd`/copy-jars.sh
else
        echo "Unable to locate copy-jars.sh"
        exit 1
fi

cd $CURRENT_DIR
if [ -f `pwd`/copy-patches.sh ]; then
        echo "Executing copy-patches.sh..."
        source `pwd`/copy-patches.sh
else
        echo "Unable to locate copy-patches.sh"
        exit 1
fi