#!/bin/bash

appCount=$1
defaultAppType=$2

doProcess()
{

if [ "$appCount" = "" ];then
   echo number of apps argument is missing. second argument apptype such as war,jaxrs,jaggery,dbs,bpel is optional. 
   return
fi

for (( c=1; c<="$appCount"; c++ ))
do
appType=war
  if [ "$defaultAppType" = "" ]; then
  modulo=$( echo "$c % 6 " |bc )
  if [ "$modulo" = 0 ]; then
	appType=war
  elif [ "$modulo" == 1 ]; then
        appType=jaxrs
  elif [ "$modulo" == 2 ]; then
        appType=jaxws
  elif [ "$modulo" == 3 ]; then
        appType=jaggery
  elif [ "$modulo" == 4 ]; then
        appType=dbs
  elif [ "$modulo" == 5 ]; then
        appType=bpel	
  fi
else
 appType=$defaultAppType
fi
   uuid=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 8 | head -n 1)$appType
   appText=APP$uuid,APP$uuid,AppDesc$uuid,$appType,git,Java Web Application
   echo "$appText" >> ./apps/af_apps.csv 
done 
}
doProcess
