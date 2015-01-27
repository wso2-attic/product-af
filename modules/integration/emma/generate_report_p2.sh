#!/bin/bash

pwd

echo "Stopping AF..."
./bin/wso2server.sh stop
sleep 15s

echo "Generate code coverage report..."
java -cp emma*.jar emma report -r html -in coverage.em -in coverage.ec

echo "copy original jars to component/plugins"
rm repository/components/plugins/org.wso2.carbon.appfactory.*
cp temp_jars/* repository/components/plugins/

echo "Starting AF..."
./bin/wso2server.sh start -p default >> repository/logs/wso2carbon.log
sleep 30s

exit 0
