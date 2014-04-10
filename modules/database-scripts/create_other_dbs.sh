#!/bin/bash


# Database Users
# ---------------------------------------------
API_MGT_USER_PWD="root"
S2_USER_PWD="root"
JPADB_USER_PWD="root"
RSS_USER_PWD="root"
USER_STORE_PWD="root"
CLOUDMGTAPP_PWD="root"
ISSUE_TRACKER_PWD="root"
HIVEUSER_PWD='root'
IDENTITY_PWD='root'
BPS_PWD='root'
# Other Databases
# ---------------------------------------------

# Database for mysql DB
USER="root"
# Password for mysql DB
PASS="root"

# Host name for mysql server
MYSQL_HOST="localhost"

ECHO=$(which echo)
MYSQL=$(which mysql)
MYSQL_BASE_CMD="${MYSQL} -h${MYSQL_HOST} -u${USER} -p${PASS}"

# List of Databases
DATABASES="apimgt cloud_mgt  rss_mgt userstore apim_userstore  jpadb s2_foundation_dev  s2_foundation_test s2_foundation_prod bps rss_1_0_2 issue_tracker identity af_stats devuserstore testuserstore produserstore"
# Do not create db for hiveuser - metastore_db.

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
${MYSQL_BASE_CMD} -Bse "grant all on apimgt.* to 'apimgt'@'%' identified by '$API_MGT_USER_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on rss_mgt.* to 'rss_mgt'@'%' identified by '$RSS_USER_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on rss_1_0_2.* to 'rss_mgt'@'%' identified by '$RSS_USER_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on userstore.* to 'userstore'@'%' identified by '$USER_STORE_PWD'"

${MYSQL_BASE_CMD} -Bse "grant all on devuserstore.* to 'userstore'@'%' identified by '$USER_STORE_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on testuserstore.* to 'userstore'@'%' identified by '$USER_STORE_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on produserstore.* to 'userstore'@'%' identified by '$USER_STORE_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on apim_userstore.* to 'userstore'@'%' identified by '$USER_STORE_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on s2_foundation_dev.* to 's2user'@'%' identified by '$S2_USER_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on s2_foundation_test.* to 's2user'@'%' identified by '$S2_USER_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on s2_foundation_prod.* to 's2user'@'%' identified by '$S2_USER_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on bpsds_dev.* to 's2user'@'%' identified by '$S2_USER_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on bpsds_prod.* to 's2user'@'%' identified by '$S2_USER_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on bpsds_test.* to 's2user'@'%' identified by '$S2_USER_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on jpadb.* to 'jpadb'@'%' identified by '$JPADB_USER_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on cloud_mgt.* to 'cloud'@'%' identified by '$CLOUDMGTAPP_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on issue_tracker.* to 'issuetracker'@'%' identified by '$ISSUE_TRACKER_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on metastore_db.* to 'hiveuser'@'%' identified by '$HIVEUSER_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on identity.* to 'identity'@'%' identified by '$IDENTITY_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on bps.* to 'bps'@'%' identified by '$BPS_PWD'"
${MYSQL_BASE_CMD} -Bse "grant all on af_stats.* to 'af_stats'@'%' identified by 'root'"

for db in userstore apim_userstore devuserstore testuserstore produserstore
do
	${ECHO} -en "Creating tables on ${db} database ... "
	${MYSQL_BASE_CMD} $db < dbscripts/userstore.sql
	[ $? -eq 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"
done

for db in apimgt jpadb 
do
	${ECHO} -en "Creating tables on ${db} database ... "
	${MYSQL_BASE_CMD} $db < dbscripts/$db.sql
	[ $? -eq 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"
done

#FOR Storage Server
${ECHO} -en "Creating tables on rss_mgt database ... "
${MYSQL_BASE_CMD} rss_mgt < dbscripts/wso2_rss_mysql.sql
[ $? -eq 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"

#FOR Storage Server_1.0.2
${ECHO} -en "Creating tables on rss_1.0.2 database ... "
${MYSQL_BASE_CMD} rss_1_0_2 < dbscripts/rss_1.0.2.sql
[ $? -eq 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"

#for db in dev test prod
#do
#	${ECHO} -en "Creating tables on ${db} database ... "
#	${MYSQL_BASE_CMD} bpsds_${db} < dbscripts/as-bpsds.sql
#	[ $? -eq 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"
#done

${MYSQL_BASE_CMD} identity < dbscripts/identity.sql
${MYSQL_BASE_CMD} bps < dbscripts/bps.sql

${ECHO} -en "Creating tables on s2_foundation_dev  database ... "
${MYSQL_BASE_CMD} s2_foundation_dev < dbscripts/s2foundation_schema.sql
[ $? -eq 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"

${ECHO} -en "Creating tables on s2_foundation_test database ... "
${MYSQL_BASE_CMD} s2_foundation_test < dbscripts/s2foundation_schema.sql
[ $? -eq 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"

${ECHO} -en "Creating tables on s2_foundation_prod database ... "
${MYSQL_BASE_CMD} s2_foundation_prod < dbscripts/s2foundation_schema.sql
[ $? -eq 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"

# For cloud management app database.
${ECHO} -en "Creating tables on cloud_mgt database ... "
${MYSQL_BASE_CMD} cloud_mgt < dbscripts/cloud_mgt_schema.sql
[ $? -eq 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"

# For cloud management app database.
${ECHO} -en "Creating tables on issue_tracker database ... "
${MYSQL_BASE_CMD} issue_tracker < dbscripts/issue_tracker.sql
[ $? -eq 0 ] && ${ECHO} "[DONE]" || ${ECHO} "[FAILED]"



${ECHO} -e "----------------------------------------"

${ECHO} -e "Done."

exit $?

