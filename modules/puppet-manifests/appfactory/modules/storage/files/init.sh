#!/bin/bash

export JAVA_HOME=/opt/java
export PATH=$PATH:$JAVA_HOME/bin/

OPTS=$@
IFACE=eth0
STORE=/mnt/packs
LOG=/var/log/stratos_init.log
SERVICES="as bam bps brs cep dss esb gs ms mb governance lb"
URL="http://dist.wso2.org/products/stratos/"
URLLB="http://dist.wso2.org/products/lb"
VERSION="1.5.2"
LBVERSION="1.0.2"

MKDIR=`which mkdir`
UNZIP=`which unzip`
TOUCH=`which touch`
WGET=`which wget`
FIND=`which find`
SLEEP=`which sleep`
TEST=`which test`

err_log() {
	local DATE=`date +%Y%m%d-%H%M`
	echo "${DATE} - ${1}" >> ${LOG}
}

getmyip() {
	local INETFACES=`ifconfig -a | grep -o -e "[a-z][a-z]*[0-9]*[ ]*Link" | perl -pe "s|^([a-z]*[0-9]*)[ ]*Link|\1|"`

	for INETFACE in $INETFACES
	do
        	local INETADDR=`ifconfig $INETFACE | grep -o -e "inet addr:[^ ]*" | grep -o -e "[^:]*$"`
        	local PUBIP=`wget -q -O - checkip.dyndns.org | sed -e 's/[^:]*: //' -e 's/<.*$//'`

        	[ -z $INETADDR ] && continue || true
        	[ $INETFACE == "lo" ] && continue || true

        	if [ ${INETADDR} == ${PUBIP} ]; then
                	echo "${PUBIP}"
                	exit 0
        	else
                	echo "${INETADDR}"
        	fi
	done
}

# Validating inputs and other requirements
# Must have atleast one argument
[ $# -lt 1 ] && (echo -e "Error: Insuffitient number of arguments\n$0 as|bam|bps|brs|cep|dss|esb|gs|ms|mb|governance|lb")
# Store directory
[ -d ${STORE} ] && true || (err_log "Could not find ${STORE} .. Creating it for you." ; ${MKDIR} ${STORE})
# Service names
for S in ${SERVICES}; do for D in $OPTS; do [ ${S} == ${D} ] && true || (err_log "${D} is not a proper servive name") ; done ; done
# Log file
[ -f ${LOG} ] && true || (${TOUCH} ${LOG})
# Directory structure for the deployment if not exist
INETADDR=$(getmyip)
[ -d /mnt/${INETADDR} ] && (err_log "Directory structure already exists!" ; exit 0) || (err_log "Could not find /mnt/${INETADDR} .. Creating it for you." ; ${MKDIR} -p /mnt/${INETADDR}/)

# Download packs
for D in ${OPTS}
do
#	if [ ${D} == "as" ]; then DIR='appserver'; ${WGET} -O ${STORE}/wso2stratos-${D}.zip ${URL}/${VERSION}/${DIR}/wso2stratos-as-${VERSION}.zip ; fi
 	if [ ${D} == "bam" ]; then DIR='bam'; ${WGET} -O ${STORE}/wso2stratos-${D}-${VERSION}.zip ${URL}/${VERSION}/${DIR}/wso2stratos-bam-${VERSION}.zip ; fi
	if [ ${D} == "bps" ]; then DIR='bps'; ${WGET} -O ${STORE}/wso2stratos-${D}-${VERSION}.zip ${URL}/${VERSION}/${DIR}/wso2stratos-bps-${VERSION}.zip ; fi
	if [ ${D} == "brs" ]; then DIR='brs'; ${WGET} -O ${STORE}/wso2stratos-${D}-${VERSION}.zip ${URL}/${VERSION}/${DIR}/wso2stratos-brs-${VERSION}.zip ; fi
	if [ ${D} == "cep" ]; then DIR='cep'; ${WGET} -O ${STORE}/wso2stratos-${D}-${VERSION}.zip ${URL}/${VERSION}/${DIR}/wso2stratos-cep-${VERSION}.zip ; fi
	if [ ${D} == "dss" ]; then DIR='data-services'; ${WGET} -O ${STORE}/wso2stratos-${D}-${VERSION}.zip ${URL}/${VERSION}/${DIR}/wso2stratos-dss-${VERSION}.zip ; fi
	if [ ${D} == "esb" ]; then DIR='esb'; ${WGET} -O ${STORE}/wso2stratos-${D}-${VERSION}.zip ${URL}/${VERSION}/${DIR}/wso2stratos-esb-${VERSION}.zip ; fi
	if [ ${D} == "gs" ]; then DIR='gadget-server'; ${WGET} -O ${STORE}/wso2stratos-${D}-${VERSION}.zip ${URL}/${VERSION}/${DIR}/wso2stratos-gs-${VERSION}.zip ; fi
	if [ ${D} == "ms" ]; then DIR='mashup'; ${WGET} -O ${STORE}/wso2stratos-${D}-${VERSION}.zip ${URL}/${VERSION}/${DIR}/wso2stratos-ms-${VERSION}.zip ; fi
	if [ ${D} == "mb" ]; then DIR='mb'; ${WGET} -O ${STORE}/wso2stratos-${D}-${VERSION}.zip ${URL}/${VERSION}/${DIR}/wso2stratos-mb-${VERSION}.zip ; fi
	if [ ${D} == "governance" ]; then DIR='registry'; ${WGET} -O ${STORE}/wso2stratos-${D}-${VERSION}.zip ${URL}/${VERSION}/${DIR}/wso2stratos-governance-${VERSION}.zip ; fi
#	if [ ${D} == "lb" ]; then ${WGET} -O ${STORE}/wso2stratos-${D}-${LBVERSION}.zip ${URLLB}/wso2lb-${LBVERSION}.zip ; fi
done

# Wait till wget to commit downloaded content to the output file
${SLEEP} 10

## Find and extracting Stratos services to /mnt/${INETADDR}/
${FIND} ${STORE} -iname *.zip -exec ${UNZIP} -d /mnt/${INETADDR}/ {} \;

