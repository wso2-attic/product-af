#bin/bash
# Important: this script will not work in all environments, you need to install xdotool
. ./config.properties
setupHome="/mnt/$MACHINE_IP"
wso2asversion=5.2.1
WID=$(xprop -root | grep "_NET_ACTIVE_WINDOW(WINDOW)"| awk '{print $5}')
xdotool windowfocus $WID
xdotool key ctrl+shift+t
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/appfactory/wso2appfactory-$APPFACTORY_VERSION/bin"; xdotool key Return; 
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/appfactory/wso2appfactory-$APPFACTORY_VERSION/repository/logs/wso2carbon.log"; xdotool key Return;

xdotool key ctrl+shift+t 
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/api-manager/wso2am-1.9.0/bin"; xdotool key Return;
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/api-manager/wso2am-1.9.0/repository/logs/wso2carbon.log"; xdotool key Return;

xdotool key ctrl+shift+t
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/bps/wso2bps-3.2.0/bin"; xdotool key Return;
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/bps/wso2bps-3.2.0/repository/logs/wso2carbon.log"; xdotool key Return;

xdotool key ctrl+shift+t 
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/mb/wso2mb-2.2.0/bin"; xdotool key Return;
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/mb/wso2mb-2.2.0/repository/logs/wso2carbon.log"; xdotool key Return;

xdotool key ctrl+shift+t
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/jenkins"; xdotool key Return;
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/jenkins/logs/jenkins.log"; xdotool key Return;

xdotool key ctrl+shift+t
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/gitblit/"; xdotool key Return; 
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/gitblit/logs/git.log"; xdotool key Return;

xdotool key ctrl+shift+t
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/s2gitblit/"; xdotool key Return; 
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/s2gitblit/logs/s2gitblit.log"; xdotool key Return;

xdotool key ctrl+shift+t 
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/ss/wso2ss-1.1.0/bin"; xdotool key Return;
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/ss/wso2ss-1.1.0/repository/logs/wso2carbon.log"; xdotool key Return;

xdotool key ctrl+shift+t
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/bam/wso2bam-2.4.1/bin"; xdotool key Return;
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/bam/wso2bam-2.4.1/repository/logs/wso2carbon.log"; xdotool key Return;

xdotool key ctrl+shift+t
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/ues/wso2ues-1.1.0/bin"; xdotool key Return;
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/ues/wso2ues-1.1.0/repository/logs/wso2carbon.log"; xdotool key Return;

xdotool key ctrl+shift+t 
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/dev_pass/privatepaas/install/apache-stratos-default/bin"; xdotool key Return;
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/dev_pass/privatepaas/install/apache-stratos-default/repository/logs/wso2carbon.log"; xdotool key Return;

xdotool key ctrl+shift+t 
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/dev_pass/privatepaas/install/apache-activemq-5.9.1/bin"; xdotool key Return;
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/dev_pass/privatepaas/install/apache-activemq-5.9.1/data/activemq.log"; xdotool key Return;

xdotool key ctrl+shift+t 
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/test_pass/privatepaas/install/apache-stratos-default/bin"; xdotool key Return;
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/test_pass/privatepaas/install/apache-stratos-default/repository/logs/wso2carbon.log"; xdotool key Return;

xdotool key ctrl+shift+t 
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/test_pass/privatepaas/install/apache-activemq-5.9.1/bin"; xdotool key Return;
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/test_pass/privatepaas/install/apache-activemq-5.9.1/data/activemq.log"; xdotool key Return;

xdotool key ctrl+shift+t 
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/prod_pass/privatepaas/install/apache-stratos-default/bin"; xdotool key Return;
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/prod_pass/privatepaas/install/apache-stratos-default/repository/logs/wso2carbon.log"; xdotool key Return;

xdotool key ctrl+shift+t 
xdotool type --delay 1 --clearmodifiers "cd ${setupHome}/prod_pass/privatepaas/install/apache-activemq-5.9.1/bin"; xdotool key Return;
xdotool type --delay 1 --clearmodifiers "tailf ${setupHome}/prod_pass/privatepaas/install/apache-activemq-5.9.1/data/activemq.log"; xdotool key Return;
