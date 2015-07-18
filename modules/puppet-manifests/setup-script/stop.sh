#!/bin/bash
# source the properties

#set -e

. ./config.properties

#sudo su ${APPFACTORY_USER} <<'EOF'

#. ./config.properties

#if [ "$(whoami)" != "${APPFACTORY_USER}" ]; then
#        echo "Must be ${APPFACTORY_USER} to run $0"
#        exit 1;
#fi

echo -ne  "Stopping all servers..................."
if [ ! -d /mnt/$MACHINE_IP/ ]; then
 echo -ne "                           [FAILED]\n"  
 exit 0
fi

cd /mnt/$MACHINE_IP/

afpath=appfactory/wso2appfactory-$APPFACTORY_VERSION
bampath=bam/wso2bam-$BAM_VERSION
mbpath=mb/wso2mb-$MB_VERSION
aspath=buildserver/wso2as-$AS_VERSION
bpspath=bps/wso2bps-$BPS_VERSION
uespath=ues/wso2ues-$UES_VERSION
apimpath=api-manager/wso2am-$APIM_VERSION

ps -ef | grep /mnt/$MACHINE_IP | mawk '{print($2)}' | xargs kill -9
ps -ef | grep 'gitblit' | mawk '{print($2)}' | xargs kill -9
ps -ef | grep 'jenkins' | mawk '{print($2)}' | xargs kill -9

echo -ne "                           [OK]\n"
#EOF

