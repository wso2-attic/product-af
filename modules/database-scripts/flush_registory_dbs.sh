#!/bin/bash

# Cloud Registry Databases
# ---------------------------------------------

# Host name for mysql server
MYSQL_HOST="localhost"

# MYSQL Database Admin User
USER="root"
PASS="root"

ECHO=`which echo`
MYSQL=`which mysql`
MYSQL_BASE_CMD="${MYSQL} -u${USER} -p${PASS} -h${MYSQL_HOST}"

# For all DBs
DBS="api_mgt_registry registry appfactory_config esb_dev_config esb_test_config esb_staging_config esb_prod_config api_mgt_config appfactory_bps_config stratos_ctrl_config storage_server_config cloud_mgt_config appserver_dev_config appserver_prod_config appserver_test_config appserver_staging_config jenkins_config dev_registry test_registry prod_registry apim_store_registry apim_gateway_registry apim_keymanager_registry apim_publisher_registry ues_config bam_config identity_config"

# Creating DBs
for D in ${DBS}; do 
	${ECHO} -en "Dropping ${D} .. "
	${MYSQL_BASE_CMD} "-e DROP DATABASE IF EXISTS ${D};"
	[ $? == 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"
done

${ECHO} -e "Done."

exit $?
