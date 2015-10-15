#!/bin/bash
# source the properties

. ./config.properties

service dnsmasq restart
service nginx stop

sudo su $APPFACTORY_USER <<'EOF'

. ./config.properties

cd /mnt/$MACHINE_IP/

CURRENT_DIR=/mnt/$MACHINE_IP

afpath=appfactory/wso2appfactory-$APPFACTORY_VERSION
bampath=bam/wso2bam-$BAM_VERSION
mbpath=mb/wso2mb-$MB_VERSION
jenkinspath=jenkins
gregpath=wso2greg-$GREG_VERSION
bpspath=bps/wso2bps-$BPS_VERSION
uespath=ues/wso2ues-$UES_VERSION
apimpath=api-manager/wso2am-$APIM_VERSION
storagepath=ss/wso2ss-$STORAGE_VERSOIN

echo "STOPPING SERVERS..."

ps -ef | grep /mnt/$MACHINE_IP | mawk '{print($2)}' | xargs kill -9
ps -ef | grep 'gitblit' | mawk '{print($2)}' | xargs kill -9

sleep 30s 

echo "starting App Factory"
./$afpath/bin/wso2server.sh start

while ! nc -z localhost 9443; do sleep 5; done; echo '------ AF is up ----------'

echo "starting MB"
./$mbpath/bin/wso2server.sh start

while ! nc -z localhost 9743; do sleep 5; done; echo '------ MB is up ----------'

echo "starting BAM"
./$bampath/bin/wso2server.sh start

echo "starting jenkins"
./$jenkinspath/jenkins.sh start

echo "starting BPS"
./$bpspath/bin/wso2server.sh start

echo "starting UES"
./$uespath/bin/wso2server.sh start

echo "starting s2-gitblit"
cd s2gitblit/
/bin/bash start-gitblit.sh

echo "starting gitblit"
cd ../gitblit/
/bin/bash start-gitblit.sh
cd ..

echo "starting APIM"
./$apimpath/bin/wso2server.sh start

echo "starting stogare server"
./$storagepath/bin/wso2server.sh start

echo "starting greg servers"
./dev_greg/$gregpath/bin/wso2server.sh start
./test_greg/$gregpath/bin/wso2server.sh start
./prod_greg/$gregpath/bin/wso2server.sh start

echo "starting stratos"
./ppaas/privatepaas/install/apache-stratos-default/bin/stratos.sh start
./ppaas/privatepaas/install/apache-activemq-5.9.1/bin/activemq start

sleep 10s

EOF

sudo su << 'EOF'
. ./config.properties

cd /mnt/$MACHINE_IP/
bash nginx/apache-stratos-nginx-extension-4.1.2/bin/nginx-extension.sh > extension.log  2>&1 &

EOF