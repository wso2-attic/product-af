#!/bin/sh
#
#   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
#
#   WSO2 Inc. licenses this file to you under the Apache License,
#   Version 2.0 (the "License"); you may not use this file except
#   in compliance with the License.
#   You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
#

# Important: this script will not work in all environments, you need to install xdotool and sshpass
. ./config.properties
wso2asversion=5.2.1
WID=$(xprop -root | grep "_NET_ACTIVE_WINDOW(WINDOW)"| awk '{print $5}')
xdotool windowfocus $WID

#Set Script Name variable
SCRIPT=`basename ${BASH_SOURCE[0]}`

#Initialize variables to default values.
REMOTE_USER="afpuppet"
REMOTE_PW="afpuppet"
REMOTE_SERVER_IP=$MACHINE_IP
SERVERS="af api bps mb jenkins gitblit s2gitblit ss bam ues dev_pass dev_amq test_pass test_amq prod_pass prod_amq"

#Set fonts for Help.
NORM=`tput sgr0`
BOLD=`tput bold`
REV=`tput smso`

#Help function
function HELP {
  echo -e \\n"Help documentation for ${BOLD}${SCRIPT}.${NORM}"\\n
  echo -e "${REV}Basic usage:${NORM} ${BOLD}$SCRIPT -s SERVER_IP${NORM}"\\n
  echo "Command line switches are optional. The following switches are recognized."
  echo "${REV}-i${NORM}  --Sets the value for option ${BOLD}ip of the remote server${NORM}."
  echo "${REV}-u${NORM}  --Sets the value for option ${BOLD}username${NORM}. Default is ${BOLD}${REMOTE_USER}${NORM}."
  echo "${REV}-s${NORM}  --Sets the value for option ${BOLD}servers${NORM} to be tailed."
  echo "${NORM}    --Default is ${BOLD}\"${SERVERS}\"${NORM}."
  echo "${NORM}    --Use subset of default values to tail subset of servers."
  echo "${NORM}    --ex: ${BOLD}\"af jenkins\"${NORM}."
  echo -e "${REV}-h${NORM}  --Displays this help message. No further functions are performed."\\n
  echo -e "Example: to tail Remote servers"
  echo -e "       >> ${BOLD}$SCRIPT -s 192.168.16.2 -u afpuppet -s \"af jenkins\"${NORM}"\\n
  echo -e "       : to tail Local servers"
  echo -e "       >> ${BOLD}$SCRIPT"\\n
  exit 1
}

# tail remote servers
function _tail_remote {

    # $1 -> Remote IP
    # $2 -> Remote Username
    # $3 -> Remote Password
    # $4 -> Log file path

    local RMT_IP=${1}
    local RMT_UN=${2}
    local RMT_PW=${3}
    local LOG_FILE_PATH=${4}

    #connect
    xdotool type --delay 1 --clearmodifiers "sshpass -p ${RMT_PW} ssh ${RMT_UN}@${RMT_IP} -t \"tail -f ${LOG_FILE_PATH}\""; xdotool key Return;

    #Instead of above command you can use below three commands to connect and tail without appearing the password in
    #the termianl.
    #xdotool type --delay 1 --clearmodifiers "ssh ${RMT_UN}@${RMT_IP} -t \"tail -f ${LOG_FILE_PATH}\""; xdotool key Return;
    #sleep 2 # sleep until password promt is appeared
    #xdotool type --delay 1 --clearmodifiers "${RMT_PW}"; xdotool key Return;
}

# tail local servers
function _tail_local {
    # $1 -> Log file path

    local LOG_FILE_PATH=${1}
    local BASE_DIR_LOG=$(dirname "${LOG_FILE_PATH}")

    #tail
    xdotool type --delay 1 --clearmodifiers "cd ${BASE_DIR_LOG}"; xdotool key Return;
    xdotool type --delay 1 --clearmodifiers "tail -f ${LOG_FILE_PATH}"; xdotool key Return;
}


function _tail_server {
    # $1 -> Remote IP
    # $2 -> Remote Username
    # $3 -> Remote Password
    # $4 -> Log file path
    # $5 -> Name to be appeared on the tab

    local RMT_IP=${1}
    local RMT_UN=${2}
    local RMT_PW=${3}
    local LOG_FILE_PATH=${4}
    local SERVER_NAME=${5}

    #new tab
    xdotool key ctrl+shift+t

    #set tab name
    xdotool type --delay 1 --clearmodifiers 'ORIG=$PS1'; xdotool key Return;
    xdotool type --delay 1 --clearmodifiers "TITLE=\"\e]2;$SERVER_NAME - $RMT_IP\a\""; xdotool key Return;
    xdotool type --delay 1 --clearmodifiers 'PS1=${ORIG}${TITLE}'; xdotool key Return;


    if [ "${RMT_IP}" == "${MACHINE_IP}" ] ;then
        _tail_local ${LOG_FILE_PATH}    #tail local servers
    else
        _tail_remote ${RMT_IP} ${RMT_UN} ${RMT_PW} ${LOG_FILE_PATH}
    fi

}

# Uncomment follow 4 lines to check the number of arguments. If none are passed, print help and exit.
#NUMARGS=$#
#if [ $NUMARGS -eq 0 ]; then
#  HELP
#fi

### Start getopts code ###
#Flag to verify whether the REMOTE_SERVER_IP has provided as a option
iFlag=false
while getopts :i:u:s:h FLAG; do
  case $FLAG in
    i)  #set option "i"
      REMOTE_SERVER_IP=$OPTARG
      iFlag=true
      ;;
    u)  #set option "u"
      REMOTE_USER=$OPTARG
      echo "Remote user name = $REMOTE_USER"
      read -s -p 'Enter password for $REMOTE_USER : ' PW && echo
      REMOTE_PW=$PW
      ;;
    s)  #set option "s"
      SERVERS=$OPTARG
      ;;
    h)  #show help
      HELP
      ;;
    \?) #unrecognized option - show help
      echo -e "\033[33;31mOption -${BOLD}$OPTARG is not allowed.\033[00;00m"
      echo -e "\033[33;31mPlease see the below documentation.\033[00;00m"\\n
      HELP

      #If you just want to display a simple error message instead full help message
      #remove the 3 lines above and uncomment the 2 lines below.
      # echo -e "Use ${BOLD}$SCRIPT -h${NORM} to see the help documentation."\\n
      # exit 2
      ;;
  esac
done
shift $((OPTIND-1))  #This tells getopts to move on to the next argument.
### End getopts code ###

# Uncomment follow 4 lines to check whether REMOTE_SERVER_IP(option -i) has provided or not
#if ! ${iFlag} || [[ -z "$REMOTE_SERVER_IP" ]]
#then
#    echo -e "\033[33;31mRemote server IP should be specified!\033[00;00m"
#    HELP
#fi

# Initialize file path to log files
setupHome="/mnt/${REMOTE_SERVER_IP}"
declare -A af_dirs
af_dirs=(   ["af"]="${setupHome}/appfactory/wso2appfactory-$APPFACTORY_VERSION/repository/logs/wso2carbon.log" \
            ["api"]="${setupHome}/api-manager/wso2am-1.7.0/repository/logs/wso2carbon.log" \
            ["bps"]="${setupHome}/bps/wso2bps-3.2.0/repository/logs/wso2carbon.log" \
            ["mb"]="${setupHome}/mb/wso2mb-2.2.0/repository/logs/wso2carbon.log" \
            ["jenkins"]="${setupHome}/buildserver/wso2as-$wso2asversion/repository/logs/wso2carbon.log" \
            ["gitblit"]="${setupHome}/gitblit/logs/git.log" \
            ["s2gitblit"]="${setupHome}/s2gitblit/logs/s2gitblit.log" \
            ["ss"]="${setupHome}/ss/wso2ss-1.1.0/repository/logs/wso2carbon.log" \
            ["bam"]="${setupHome}/bam/wso2bam-2.4.1/repository/logs/wso2carbon.log" \
            ["ues"]="${setupHome}/ues/wso2ues-1.1.0/repository/logs/wso2carbon.log" \
            ["dev_pass"]="${setupHome}/dev_pass/privatepaas/install/apache-stratos-default/repository/logs/wso2carbon.log" \
            ["dev_amq"]="${setupHome}/dev_pass/privatepaas/install/apache-activemq-5.9.1/data/activemq.log" \
            ["test_pass"]="${setupHome}/test_pass/privatepaas/install/apache-stratos-default/repository/logs/wso2carbon.log" \
            ["test_amq"]="${setupHome}/test_pass/privatepaas/install/apache-activemq-5.9.1/data/activemq.log" \
            ["prod_pass"]="${setupHome}/prod_pass/privatepaas/install/apache-stratos-default/repository/logs/wso2carbon.log" \
            ["prod_amq"]="${setupHome}/prod_pass/privatepaas/install/apache-activemq-5.9.1/data/activemq.log")


WID=$(xprop -root | grep "_NET_ACTIVE_WINDOW(WINDOW)"| awk '{print $5}')
xdotool windowfocus $WID

echo "Remote Server IP: ${BOLD}$REMOTE_SERVER_IP${NORM}"
echo "Remote user name: ${BOLD}$REMOTE_USER${NORM}"
echo "Servers to be tailed: ${BOLD}\"$SERVERS\"${NORM}"

for i in ${SERVERS[@]}; do
  _tail_server ${REMOTE_SERVER_IP} ${REMOTE_USER} ${REMOTE_PW} ${af_dirs[${i,,}]} ${i^^}
done

exit 0


