#!/bin/bash
starttime=`date +%s`
working_dir=$1
organization=$2
app=$3
user=$4
user_password=$5
git_host_name=$6
counter=$7

echo $user"================================================="$user_password

organization_dir=$working_dir/$organization/$(uuidgen)
app_dir=$organization_dir/$app/
#echo app directory $organization_dir

#create organization directory if not exists
if [ ! -d $organization_dir ]; then
   mkdir -p $organization_dir
fi

#check if already cloned by checking existence of app_dir
if [ ! -d $app_dir ] ; 
then
  #clone the repo
  cd $organization_dir
  git_clone_url=https://$user%40$organization:$user_password@$git_host_name/git/$organization/$app.git   
 # echo cloning repo...
  git clone $git_clone_url

  #commit code
  cd $app_dir
#  git fetch origin
  git pull
#  grep -rl "Include" --include *.jsp . | xargs sed -i "s|Include|Include$starttime|g"
  touch $(uuidgen).java
  git add *
  git commit -a -m "commit"
 # echo pushing app..
  git push $GIT_URL #master:master
fi
endtime=`date +%s`
echo $(($endtime - $starttime))
