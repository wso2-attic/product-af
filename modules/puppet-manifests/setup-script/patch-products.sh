#!/bin/bash
# This script can be used to patch the developer setup
# Needs to be improved to use the copyjars script methods
# source the properties

. ./config.properties



BASE_DIR=/mnt/$MACHINE_IP

afpath=$BASE_DIR/appfactory/wso2appfactory-$APPFACTORY_VERSION
bampath=$BASE_DIR/bam/wso2bam-$BAM_VERSION
mbpath=$BASE_DIR/mb/wso2mb-$MB_VERSION
jenkinspath=$BASE_DIR/jenkins
bpspath=$BASE_DIR/bps/wso2bps-$BPS_VERSION
apimpath=$BASE_DIR/api-manager/wso2am-$APIM_VERSION

## patch appfactory
AF_PATCH_DIR=$afpath/repository/components/patches/patch9999/
mkdir $AF_PATCH_DIR

cd ../../components

cp org.wso2.carbon.appfactory.repository/target/org.wso2.carbon.appfactory.repository.mgt.service-$APPFACTORY_VERSION.jar $AF_PATCH_DIR
cp org.wso2.carbon.appfactory.common/target/org.wso2.carbon.appfactory.common-$APPFACTORY_VERSION.jar $AF_PATCH_DIR
cp org.wso2.carbon.appfactory.s4.integration/target/org.wso2.carbon.appfactory.s4.integration-$APPFACTORY_VERSION.jar $AF_PATCH_DIR
cp org.wso2.carbon.appfactory.application.mgt/target/org.wso2.carbon.appfactory.application.mgt-$APPFACTORY_VERSION.jar $AF_PATCH_DIR
cp org.wso2.carbon.appfactory.common/target/org.wso2.carbon.appfactory.common-$APPFACTORY_VERSION.jar $AF_PATCH_DIR
cp org.wso2.carbon.appfactory.deployers/target/org.wso2.carbon.appfactory.deployers-$APPFACTORY_VERSION.jar $AF_PATCH_DIR
cp org.wso2.carbon.appfactory.build/target/org.wso2.carbon.appfactory.build-$APPFACTORY_VERSION.jar $AF_PATCH_DIR

## patch jenkins
JENKINS_PATCH_DIR=$jenkinspath/jenkins_home/plugins/appfactory-plugin-$APPFACTORY_VERSION/WEB-INF/lib/
mkdir JENKINS_PATCH_DIR

cp org.wso2.carbon.appfactory.repository/target/org.wso2.carbon.appfactory.repository.mgt.service-$APPFACTORY_VERSION.jar $JENKINS_PATCH_DIR
cp org.wso2.carbon.appfactory.common/target/org.wso2.carbon.appfactory.common-$APPFACTORY_VERSION.jar $JENKINS_PATCH_DIR
cp org.wso2.carbon.appfactory.s4.integration/target/org.wso2.carbon.appfactory.s4.integration-$APPFACTORY_VERSION.jar $JENKINS_PATCH_DIR
cp org.wso2.carbon.appfactory.application.mgt/target/org.wso2.carbon.appfactory.application.mgt-$APPFACTORY_VERSION.jar $JENKINS_PATCH_DIR
cp org.wso2.carbon.appfactory.common/target/org.wso2.carbon.appfactory.common-$APPFACTORY_VERSION.jar $JENKINS_PATCH_DIR
cp org.wso2.carbon.appfactory.deployers/target/org.wso2.carbon.appfactory.deployers-$APPFACTORY_VERSION.jar $JENKINS_PATCH_DIR
