#!/bin/bash
# ----------------------------------------------------------------------------
#  Copyright 2005-2013 WSO2, Inc. http://www.wso2.org
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
# ----------------------------------------------------------------------------

# Pack files
export CONFIG_MB="true"
export ACTIVE_MQ_DISTRIBUTION=apache-activemq-5.9.1-bin.tar.gz # Relavent activemq distribution
export ACTIVE_MQ_EXTRACTED=apache-activemq-5.9.1 # Extracted activemq distribution folder name

export JAVA_FILE_DISTRUBUTION=jdk-7u51-linux-x64.tar.gz # Relevant JDK distribution
export JAVA_NAME_EXTRACTED=jdk1.7.0_51 # Extracted JDK folder name

export MYSQL_CONNECTOR=mysql-connector-java-5.1.29-bin.jar # Relevant MySQL connector

# General configuration
export JAVA_HOME=/opt/jdk1.7.0_51
export log_path=/var/log/apache-stratos
export stratos_domain="test.paas.wso2.com"
export machine_ip="10.11.12.8"
export host_user="ubuntu"
export SLEEPTIME=30
export PPAAS_PORT=9443
export BAM_PORT=9444
export IS_PORT=9445
export CEP_PORT=9446
export GITBLIT_PORT=9418

# Puppet master configuration
export skip_puppet=""
export puppet_external="n"
export puppet_external_ip=""
export puppet_external_host=""

# cep as a separate profile
export separate_cep="n"

# IaaS configuration
export iaas="os"
# Region Name
export region="RegionOne"
# Cartridge base image
export cartridge_base_img_id="e73ef74a-84c9-4bcc-8249-0f2ea45a8fd2"

# OpenStack
export os_identity="openstackDemo:admin"
export os_credentials="password"
export os_jclouds_endpoint="http://appfactorycloud.private.wso2.com:5000/v2.0"
export os_keypair_name="appfackey"
export os_security_groups="default"

# EC2
export ec2_vpc=""
export ec2_identity=""
export ec2_credentials=""
export ec2_owner_id=""
export ec2_keypair_name=""
export ec2_security_groups=""
export ec2_availability_zone=""
export ec2_security_group_ids=""
export ec2_subnet_id=""
export ec2_associate_public_ip_address="true"

# vCloud
export vcloud_identity=""
export vcloud_credentials=""
export vcloud_jclouds_endpoint=""

# MySQL configuration
export setup_mysql="n"
export mysql_host="10.11.12.8"
export mysql_port="3306"
export mysql_uname="root"
export mysql_password="root"

#/etc/hosts mapping
export using_etc_host_mapping="y"

# WSO2 PPaaS services
export as_enabled="y"
export bps_enabled="n"
export esb_enabled="n"
export is_enabled="n"
export apim_enabled="n"

# Worker Manager deployment
export as_worker_mgt_enabled="n"
export bps_worker_mgt_enabled="n"
export esb_worker_mgt_enabled="n"
export is_worker_mgt_enabled="n"

# Clustering of services
export as_clustering_enabled="n"
export is_clustering_enabled="n"
export esb_clustering_enabled="n"
export bps_clustering_enabled="n"
export keymanager_clustering_enabled="n"

# WSO2 PPaaS core services
export bam_enabled="n"
export config_sso_enabled="n"
export using_dns="n"
export wso2_ppaas_enabled="n"
