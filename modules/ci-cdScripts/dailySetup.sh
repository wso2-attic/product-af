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

#Checking the latest available App Factory Build URL
MACHINE_IP=192.168.19.241
AF_VERSION=2.2.0-SNAPSHOT
SETUP_PATH=/mnt/$MACHINE_IP/appfactory/wso2appfactory-$AF_VERSION
n=1
while read line;           
do           
    echo $line 
	n=$line         
done <buildNo.txt

httpResponse=$(curl -I  https://wso2.org/jenkins/job/product-af/$n/org.wso2.appfactory\$wso2appfactory/artifact/org.wso2.appfactory/wso2appfactory/$AF_VERSION/wso2appfactory-$AF_VERSION.zip -s -f  | grep "HTTP/1.1")
echo "$httpResponse"


delim1="1.1 "
delim2=" "

var1=${httpResponse#*${delim1}}
httpStatusCode=${var1%${delim2}*}

#Received HTTP Status code from the above REST call
echo "${httpStatusCode}"


expectedStatusCode=200

# WGET the pack from the latest build
if [ $httpStatusCode == $expectedStatusCode ]
then
   wget "https://wso2.org/jenkins/job/product-af/$n/org.wso2.appfactory\$wso2appfactory/artifact/org.wso2.appfactory/wso2appfactory/2.2.0-SNAPSHOT/wso2appfactory-2.2.0-SNAPSHOT.zip"
else
   echo "a is not equal to b"
fi

#Update the text file with the next available build number
var=$(($n+1))
echo "$var" > buildNo.txt


# Extract the pack to the location in 241 machine
rm -rf wso2appfactory-$AF_VERSION/
echo "Removed old AF extracted directory"
unzip wso2appfactory-$AF_VERSION.zip


# Create a patch directory with the latest available patch number in the 241 machine
a=1
while read patchNumber;           
do           
    echo $patchNumber 
	a=$patchNumber         
done <patchNumber.txt

cd $SETUP_PATH/repository/components/patches/
mkdir patch$patchNumber


# Update the text file with the next patch  number in 241 machine
var2=$(($a+1))
echo "$var2" > patchNumber.txt


# Get the jars from the extracted pack and copy to the setup's patches directory
cp *.appfactory.* $SETUP_PATH/repository/components/patches/patch$patchNumber

# Get a git pull from the appmgt
cd appmgt
rm -rf appmgt/
git clone https://git.cloud.wso2.com/git/wso2appfactory/appmgt
git checkout 2.1.1

# Copy the appmgt to the setup
cp appmgt/src/* $SETUP_PATH/repository/deployment/server/jaggeryapps/appmgt

# Restart the setup
bash /home/node1/setup-script/restart.sh


# Put a new build job with mvn clean test
