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

set -e

. ./config.properties

echo -e "\n###########################################"
echo -e   "### Cleaning Developer Setup ###"
echo -e   "###########################################\n"

sudo /bin/bash stop.sh

echo -ne  "Removing Cartridge puppet manifest....."
sudo rm -rf /etc/puppet/manifests/*
sudo rm -rf /etc/puppet/modules/*
echo -ne "                           [OK]\n"

echo -ne "Removing the entire appfactory setup..."
sudo rm -rf /mnt/$MACHINE_IP
echo -ne "                           [OK]\n"

echo -ne "Removing previous agent certificates..."
sudo rm -rf /var/lib/puppet/ssl/*
echo -ne "                           [OK]\n"

sudo rm -rf /mnt/packs/wso2appfactory-${APPFACTORY_VERSION}.zip

echo -ne "Removing and cleaning nginx configurations."
sudo service nginx stop
sudo service dnsmasq stop
sudo apt-get remove nginx
sudo apt-get remove dnsmasq
sudo rm /etc/dnsmasq.conf
sudo rm -rf /etc/dhcp/dhclient.conf

sudo su <<'EOF'

. ./config.properties

#constructing the pattern to beused in uniqueid
numeric="0-9"
pattern=$DEV_ID$numeric

# generating a unique name for this node
NODE_UNIQUE_ID=$(cat /dev/urandom | tr -dc $pattern | fold -w 6 | head -n 1 | tr '[:upper:]' '[:lower:]' )
 
NODE_UNIQUE_CERTNAME="$NODE_UNIQUE_ID-clean.devsetup"
echo -ne "Cleaning Databases.....................\n"
puppet agent --enable ; puppet agent -vt --certname $NODE_UNIQUE_CERTNAME; puppet agent --disable

EOF
exit 0


