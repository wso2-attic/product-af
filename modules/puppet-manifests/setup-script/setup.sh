#!/bin/bash
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

#set -e

source ./config.properties

if [ $(whoami) != 'root' ]; then
        echo "Must be root to run $0"
        exit 1;
fi

DATE_COMMAND=$(which date)
TIME_STAMP=`${DATE_COMMAND} '+%Y-%m-%d.%H:%M:%S'`

echo -e "\n###########################################"
echo -e   "### Starting AppFactory Developer Setup ###"
echo -e   "###########################################\n"

echo -e  "Removing previous agent certificates...\n"
rm -rf /var/lib/puppet/ssl/*

#constructing the pattern to beused in uniqueid
numeric="0-9"
pattern=$DEV_ID$numeric

# set username and password for the puppet agent
DEFAULT_UN='afpuppet'	#default username
DEAFULT_PW='afpuppet'	#default password

echo 'Setting username and password for puppet agent...'
read -p 'Enter username (default username is "'$DEFAULT_UN'" and press enter to continue with the default): ' PUPPET_UN
if [ -z "$PUPPET_UN" ]
  then
    PUPPET_UN=$DEFAULT_UN
fi

while true
do
    read -s -p 'Enter password (default password is "'$DEAFULT_PW'" and press enter to continue with the default): ' PUPPET_PW && echo

    if [ -z "$PUPPET_PW" ]
     then
     PUPPET_PW=$DEAFULT_PW
     break
    fi    

    read -s -p "Confirm the Password for user "'$PUPPET_UN'" (again): " PUPPET_PW_2 && echo
    
    [ "$PUPPET_PW" = "$PUPPET_PW_2" ] && break
    echo "Password confirmation failed please try again ..." 
done

#PKG_OK=$(dpkg-query -W --showformat='${Status}\n' mkpasswd 2>/dev/null | grep -c "install ok installed")
#echo Checking for somelib: $PKG_OK
if [ $(dpkg-query -W --showformat='${Status}\n' whois 2>/dev/null | grep -c "install ok installed") -eq 0 ]; then
  echo "Installing mkpasswd..."
  sudo apt-get --force-yes --yes install whois
fi

# The user’s password, in encrypted format(sha-512) which is used by the latests
# ubuntu releases (10.04 LTS ,12.04LTS, 14.04 LTS, 14.10,15.04 ).
PUPPET_PW_SHA=`mkpasswd -m sha-512 $PUPPET_PW`

if [ "${#DEV_ID}" -gt "3" ]; then
 FILTERED_DEV_ID=${DEV_ID:0:3}
else 
 FILTERED_DEV_ID=${DEV_ID}
fi

# Setting up machine_ipfacter
if [ ! -d /etc/facter/facts.d/ ]; then
   mkdir -p /etc/facter/facts.d/
fi

echo "{\"kubernetes_host\" : \"$KUBERNETES_HOST\",\"kubernetes_port\" : \"$KUBERNETES_PORT\",\"appfac_ip\" : \"$MACHINE_IP\", \"dev_id\":\"$FILTERED_DEV_ID\",\"puppet_un\":\"$PUPPET_UN\",\"encrtpted_pw\":\"$PUPPET_PW_SHA\" }" > /etc/facter/facts.d/appfactory-facts.json

# generating a unique name for this node
NODE_UNIQUE_ID=$(cat /dev/urandom | tr -dc $pattern | fold -w 6 | head -n 1 | tr '[:upper:]' '[:lower:]' )
 
for i in ${nodes[@]}; do
  NODE_UNIQUE_CERTNAME="$NODE_UNIQUE_ID-${i}"
  echo "############# Starting ${i} setup ####################"
  puppet agent --enable ; puppet agent -vt --certname $NODE_UNIQUE_CERTNAME 2>&1 | tee -a setup-logs/puppet${TIME_STAMP}.log; puppet agent --disable;
done
puppet agent --disable
exit 0

