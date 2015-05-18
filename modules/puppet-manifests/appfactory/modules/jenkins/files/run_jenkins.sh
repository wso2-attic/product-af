# !/bin/bash

MY_PATH="`dirname \"$0\"`"
MY_PATH="`( cd \"$MY_PATH\" && pwd )`"
cd $MY_PATH/run/
mkdir -p $MY_PATH/run/JenkinsHome
export JENKINS_HOME=$MY_PATH/run/JenkinsHome
echo "Jenkins home "$JENKINS_HOME
echo "Running jenkins.war..."
#java -jar jenkins.war
java -jar jenkins.war
#For debug
#java    -jar -Xdebug -Xrunjdwp:transport=dt_socket,address=9000,server=y,suspend=n  jenkins.war