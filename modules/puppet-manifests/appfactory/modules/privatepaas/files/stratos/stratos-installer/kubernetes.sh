#!/bin/bash
# ----------------------------------------------------------------------------
#
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing,
#  software distributed under the License is distributed on an
#  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#  KIND, either express or implied.  See the License for the
#  specific language governing permissions and limitations
#  under the License.
#
# ----------------------------------------------------------------------------
#
#  This script is invoked by setup.sh for configuring KUBERNETES IaaS information.
# ----------------------------------------------------------------------------

# Die on any error:
set -e

SLEEP=60
dir=`dirname $0`
current_dir=`cd $dir;pwd`

source "$current_dir/conf/setup.conf"
export LOG=$log_path/stratos-kubernetes.log

stratos_extract_path=$1

if [[ ! -d $log_path ]]; then
    mkdir -p $log_path
fi

pushd $stratos_extract_path
echo "adoooo $stratos_extract_path" >> $LOG

echo "Set KUBERNETES provider specific info in repository/conf/cloud-controller.xml" >> $LOG

sed -i "s@EC2_PROVIDER_START@!--@g"  repository/conf/cloud-controller.xml
sed -i "s@EC2_PROVIDER_END@--@g"  repository/conf/cloud-controller.xml
sed -i "s@OPENSTACK_PROVIDER_START@!--@g" repository/conf/cloud-controller.xml
sed -i "s@OPENSTACK_PROVIDER_END@--@g" repository/conf/cloud-controller.xml
sed -i "s@VCLOUD_PROVIDER_START@!--@g" repository/conf/cloud-controller.xml
sed -i "s@VCLOUD_PROVIDER_END@--@g" repository/conf/cloud-controller.xml
sed -i "s@GCE_PROVIDER_START@!--@g" repository/conf/cloud-controller.xml
sed -i "s@GCE_PROVIDER_END@--@g" repository/conf/cloud-controller.xml
sed -i "s@KUBERNETES_PROVIDER_START@@g" repository/conf/cloud-controller.xml
sed -i "s@KUBERNETES_IDENTITY@$kubernetes_identity@g" repository/conf/cloud-controller.xml
sed -i "s@KUBERNETES_CREDENTIAL@$kubernetes_credential@g" repository/conf/cloud-controller.xml
sed -i "s@KUBERNETES_PROVIDER_END@@g" repository/conf/cloud-controller.xml
sed -i "s@MOCK_IAAS_PROVIDER_START@!--@g" repository/conf/cloud-controller.xml
sed -i "s@MOCK_IAAS_PROVIDER_END@--@g" repository/conf/cloud-controller.xml


popd

