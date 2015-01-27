#!/bin/bash

. ./emma_config.properties

echo "copying required files..."
sshpass -p "$password" scp jarlist.txt $user@$IP:$default_location
sshpass -p "$password" scp emma*.jar $user@$IP:$default_location
sshpass -p "$password" scp instrument_jars_p2.sh $user@$IP:$default_location
sshpass -p "$password" scp generate_report_p2.sh $user@$IP:$default_location

echo "loging to server and run instrument_jars_p2.sh"
sshpass -p "$password" ssh -o StrictHostKeyChecking=no $user@$IP "cd /mnt/$IP/appfactory/wso2appfactory-2.1.0/ && sh ./instrument_jars_p2.sh"


