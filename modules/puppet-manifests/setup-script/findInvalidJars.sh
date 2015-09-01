#!/bin/bash
#Run this with ./findInvalidJars.sh directoryPath once you complete the setup.

SEARCH_DIR=$1
AF_JAR_VERSION=2.2.0-SNAPSHOT
AF_JAR_VERSION_MODIFIED=2.2.0.SNAPSHOT
RED="\033[33;31m"
RESET_CLR='\033[00;00m'

if ! ( [[ $SEARCH_DIR == *"appfactory"* ]] )
    then
        for i in $(find $SEARCH_DIR -iregex '.*appfactory.*.jar' -printf "%f\n"); do
            if ! ([[ $i == *"$AF_JAR_VERSION"* ]] || [[ $i == *"$AF_JAR_VERSION_MODIFIED"* ]])
            then
                echo -e "${RED}Invalid JAR : $i FOUND!!! FIX NOW.${RESET_CLR}";
            fi
        done
    else
       for i in $(find $SEARCH_DIR -iname '*.jar' -printf "%f\n"); do
            if [[ $i == *appfactory* ]]
            then
                if ! ([[ $i == *"$AF_JAR_VERSION"* ]] || [[ $i == *"$AF_JAR_VERSION_MODIFIED"* ]])
                then
                    echo -e "${RED}Invalid JAR : $i FOUND!!! FIX NOW.${RESET_CLR}";
                fi
            fi
       done
fi
