#
# Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
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

now=$(date)
echo "==========================================================================================================================================="
echo "Today is: $now"

#Checking the latest available App Factory Build URL
MACHINE_IP=192.168.18.2
AF_VERSION=2.2.0-SNAPSHOT
SETUP_PATH=/mnt/$MACHINE_IP/appfactory/wso2appfactory-$AF_VERSION
CURRENT_PATH=/home/afpuppet/nightlyBuild
cd $CURRENT_PATH

# Getting the last build id
n=$(curl https://wso2.org/jenkins/job/product-af/lastBuild/buildNumber)

echo "Last build Id : $n"

# checking whether the pack is available
httpResponse=$(curl -I  https://wso2.org/jenkins/job/product-af/$n/org.wso2.appfactory\$wso2appfactory/artifact/org.wso2.appfactory/wso2appfactory/$AF_VERSION/wso2appfactory-$AF_VERSION.zip -s -f  | grep "HTTP/1.1")
echo "Http response : $httpResponse"

delim1="1.1 "
delim2=" "

var1=${httpResponse#*${delim1}}
httpStatusCode=${var1%${delim2}*}

#Received HTTP Status code from the above REST call
echo "Http response code :${httpStatusCode}"

#expectedStatusCode=200

# Checking whether the newly built pack is available
if [ "$httpStatusCode" = 200 ]
then
        #Removing the old AF packs
        rm -rfv wso2appfactory-$AF_VERSION.zip
        echo "Removed old AF pack"
        rm -rfv wso2appfactory-$AF_VERSION/
        echo "Removed old AF extracted directory"

        echo "Wgetting the pack from jenkins latest build...."
        wget "https://wso2.org/jenkins/job/product-af/$n/org.wso2.appfactory\$wso2appfactory/artifact/org.wso2.appfactory/wso2appfactory/$AF_VERSION/wso2appfactory-$AF_VERSION.zip"

        # Extract the pack to the location in 18.2 machine
        unzip wso2appfactory-$AF_VERSION.zip

        # Create patch9999 directory if not exists
        cd $SETUP_PATH/repository/components/patches/
        mkdir -p patch9999
        # Remove the content inside patch9999
        rm -rfv $SETUP_PATH/repository/components/patches/patch9999/*
        cd $CURRENT_PATH

        # Get the jars from the extracted pack and copy to the setup's patches directory
        cp -v $CURRENT_PATH/wso2appfactory-$AF_VERSION/repository/components/plugins/*.appfactory.* $SETUP_PATH/repository/components/patches/patch9999

else
   echo "obtained httpStatusCode is not equal to expectedStatusCode"
fi

# Get a git pull from the appmgt
cd $CURRENT_PATH/af_code/product-af/modules/jaggery-apps/appmgt/src
git config credential.helper 'cache --timeout=89400'
git pull origin master
#git checkout master

cd $CURRENT_PATH

# Copy the appmgt to the setup
cp -rv $CURRENT_PATH/af_code/product-af/modules/jaggery-apps/appmgt/src/* $SETUP_PATH/repository/deployment/server/jaggeryapps/appmgt

# Restart the setup
echo "re-starting App Factory"
$SETUP_PATH/bin/wso2server.sh restart
sleep 30s

# Put a new build job with mvn clean test
