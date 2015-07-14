#!/bin/bash
# source the properties

. ./config.properties

sudo su $APPFACTORY_USER <<'EOF'

. ./config.properties

cd /mnt/$MACHINE_IP/

CURRENT_DIR=/mnt/$MACHINE_IP

afpath=appfactory/wso2appfactory-$APPFACTORY_VERSION
bampath=bam/wso2bam-$BAM_VERSION
mbpath=mb/wso2mb-$MB_VERSION
jenkinsPath=jenkins
bpspath=bps/wso2bps-$BPS_VERSION
uespath=ues/wso2ues-$UES_VERSION
apimpath=api-manager/wso2am-$APIM_VERSION
storagepath=ss/wso2ss-$SS_VERSOIN

echo "STOPPING SERVERS..."

ps -ef | grep /mnt/$MACHINE_IP | mawk '{print($2)}' | xargs kill -9
ps -ef | grep 'gitblit' | mawk '{print($2)}' | xargs kill -9

sleep 30s 

echo "starting App Factory"
./$afpath/bin/wso2server.sh start

sleep 30s

echo "starting BAM"
./$bampath/bin/wso2server.sh start

echo "starting MB"
./$mbpath/bin/wso2server.sh start

echo "starting jenkins"
./$jenkinsPath/jenkins.sh start

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

echo "Starting Active MQs"
./dev_pass/privatepaas/install/apache-activemq-$ACTIVEMQ_VERSION/bin/activemq start
./prod_pass/privatepaas/install/apache-activemq-$ACTIVEMQ_VERSION/bin/activemq start
./test_pass/privatepaas/install/apache-activemq-$ACTIVEMQ_VERSION/bin/activemq start

sleep 5s

source "$CURRENT_DIR/dev_pass/privatepaas/conf.sh"
export LOG=$log_path/stratos-setup.log
setup_path="$CURRENT_DIR/dev_pass/privatepaas/stratos-installer"
source $setup_path/conf/setup.conf;$setup_path/start-servers.sh -p default >> $LOG
sleep 10s

source "$CURRENT_DIR/test_pass/privatepaas/conf.sh"
export LOG=$log_path/stratos-setup.log
setup_path="$CURRENT_DIR/test_pass/privatepaas/stratos-installer"
source $setup_path/conf/setup.conf;$setup_path/start-servers.sh -p default >> $LOG
sleep 10s

source "$CURRENT_DIR/prod_pass/privatepaas/conf.sh"
export LOG=$log_path/stratos-setup.log
setup_path="$CURRENT_DIR/prod_pass/privatepaas/stratos-installer"
source $setup_path/conf/setup.conf;$setup_path/start-servers.sh -p default >> $LOG
sleep 10s


EOF
