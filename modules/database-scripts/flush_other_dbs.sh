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
DBS="apimgt cloud_mgt rss_mgt userstore apim_userstore jpadb s2_foundation_dev s2_foundation_test s2_foundation_prod bpsds_dev bpsds_prod bpsds_test issue_tracker identity bps af_stats devuserstore testuserstore produserstore"

# Creating DBs
for D in ${DBS}; do 
	${ECHO} -en "Dropping ${D} .. "
	${MYSQL_BASE_CMD} "-e DROP DATABASE IF EXISTS ${D};"
	[ $? == 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"
done

${ECHO} -e "Done."

exit $?
