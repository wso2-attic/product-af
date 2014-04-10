#!/bin/bash


# Cloud Registry Databases
# ---------------------------------------------

# Host name for mysql server
MYSQL_HOST="localhost"

# MYSQL Database Admin User
USER="root"
PASS="root"


# Database General Users
REGISTRY_USER_PWD="registry"
API_MGT_USER_PWD="apim" 


ECHO=$(which echo)
MYSQL=$(which mysql)
MYSQL_BASE_CMD="${MYSQL} -h${MYSQL_HOST} -u${USER} -p${PASS}"

# List of Databases
DATABASES="identity_config api_mgt_registry registry appfactory_config esb_dev_config esb_test_config esb_prod_config api_mgt_config appfactory_bps_config stratos_ctrl_config storage_server_config cloud_mgt_config appserver_dev_config appserver_prod_config appserver_test_config jenkins_config dev_registry test_registry prod_registry apim_store_registry apim_gateway_registry apim_keymanager_registry apim_publisher_registry bam_config ues_config"

# Drop Existing DBs
${ECHO} -en "Droping existing databases ... "
for db in ${DATABASES}
do 
	${MYSQL_BASE_CMD} -Bse "drop database if exists ${db}"
	[ $? -ne 0 ] && ${ECHO} "[FAILED]"
done
${ECHO} -e "[DONE]"

${ECHO} -e "----------------------------------------"

# Create DBs
for db in ${DATABASES}
do 
	${ECHO} -en "Creating ${db} database ... "
	${MYSQL_BASE_CMD} -Bse "create database ${db}"
	[ $? -eq 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"
done

${ECHO} -e "----------------------------------------"

# Grant permissions
for db in ${DATABASES}
do 
	${ECHO} -en "Granting permissions on ${db} database ... "
	if [ "api_mgt_registry" == "${db}" ];
	then 
		${MYSQL_BASE_CMD} -Bse "grant all on api_mgt_registry.* to 'apimgt'@'%' identified by '$API_MGT_USER_PWD'"
	else 
		${MYSQL_BASE_CMD} -Bse "grant all on ${db}.* to 'registry'@'%' identified by '$REGISTRY_USER_PWD'"
	fi
	[ $? -eq 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"
done

${ECHO} -e "----------------------------------------"

# Create database tables

for db in ${DATABASES}
do
	${ECHO} -en "Creating tables on ${db} database ... "
	${MYSQL_BASE_CMD} ${db} < dbscripts/registry.sql
	[ $? -eq 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"
done

${ECHO} -e "----------------------------------------"

${ECHO} -e "Done."

exit $?
