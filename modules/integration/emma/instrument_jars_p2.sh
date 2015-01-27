#!/bin/bash

pwd 

echo "Stopping AF..."
./bin/wso2server.sh stop
sleep 15s

echo "Clean up coverage files..."
rm -rf coverage/
rm coverage.em
rm coverage.ec

echo "copy jar files to temp location"
mkdir temp_jars
cp repository/components/plugins/org.wso2.carbon.appfactory.* temp_jars/

echo "Copy emma jar to repository/component/lib..."
cp emma*.jar repository/components/lib/

echo "Instrumenting jars..."
java -cp emma*.jar emma instr -m overwrite -cp @jarlist.txt

echo "Starting AF..."
./bin/wso2server.sh start -p default >> repository/logs/wso2carbon.log
sleep 30s

exit 0
