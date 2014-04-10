#!/bin/bash

userCount=$1
userRole=$2

doProcess()
{

if [ "$userCount" = "" ];then
   echo first argument number of users is missing. 
   return
fi

if [ "$userRole" = "" ];then
   echo second argument user role is missing. 
   return
fi

for (( c=1; c<="$userCount"; c++ ))
do
   uuid=$(cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 8 | head -n 1)$userRole
   userText=USER$uuid,password,$userRole
   echo "$userText" >> ./users/af_"$userRole"_users.csv 
done 
}
doProcess
