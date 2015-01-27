#!/bin/bash

. ./emma_config.properties

sshpass -p "$password" ssh -o StrictHostKeyChecking=no $user@$IP "cd /mnt/$IP/appfactory/wso2appfactory-2.1.0/ && sh ./generate_report_p2.sh"


