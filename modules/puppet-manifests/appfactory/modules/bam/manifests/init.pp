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
#
# Class: bam
#
# This class installs WSO2 BAM
#
# Parameters:
#
# Actions:
#   - Install WSO2 BAM
#
# Requires:
#
# Sample Usage:
#

class bam (
  $version            = undef,
  $offset             = 0,
  $hazelcast_port     = 4000,
  $config_db          = governance,
  $maintenance_mode   = true,
  $depsync            = false,
  $sub_cluster_domain = mgt,
  $clustering         = false,
  $cloud              = false,
  $members            = {},
  $owner              = root,
  $group              = root,
  $target             = '/mnt',
  $monitoring         = false,
  $dep_sync_enabled   = undef,
  $membership_scheme  = 'wka'
) inherits params {

  $deployment_code = 'monitor'
  $carbon_version  = $version
  $service_code    = 'bam'
  $carbon_home     = "${target}/wso2${service_code}-${carbon_version}"

  $service_templates = [
                        'conf/axis2/axis2.xml',
                        'conf/carbon.xml',
                        'conf/tomcat/catalina-server.xml',
                        'conf/datasources/bam-datasources.xml',
                        'conf/etc/tasks-config.xml',
                        'conf/advanced/hive-site.xml',
                        'conf/etc/hector-config.xml',
                        'conf/etc/summarizer-config.xml',
                        'conf/etc/cassandra-auth.xml',
                        'conf/etc/cassandra-component.xml',
                        'conf/etc/cassandra.yaml'
                       ]

  $common_templates = [
                        "conf/tenant-mgt.xml",
                        "conf/user-mgt.xml",
                        "conf/registry.xml",
#                        "conf/log4j.properties",
                        "conf/datasources/master-datasources.xml"
                      ] 

  tag($service_code)

  bam::clean { $deployment_code:
    mode   => $maintenance_mode,
    target => $carbon_home;
  }

  bam::initialize { $deployment_code:
    repo      => $package_repo,
    version   => $carbon_version,
    mode      => $maintenance_mode,
    service   => $service_code,
    local_dir => $local_package_dir,
    owner     => $owner,
    target    => $target,
    require   => Bam::Clean[$deployment_code];
  }

  bam::deploy { $deployment_code:
    service       => $service_code,
    security      => true,
    owner         => $owner,
    group         => $group,
    target        => $carbon_home,
    require       => Bam::Initialize[$deployment_code];
  }

  bam::push_templates {
    $service_templates:
      target    => $carbon_home,
      directory => $service_code,
      require   => Bam::Deploy[$deployment_code];

    $common_templates:
      target    => $carbon_home,
      directory => 'wso2base',
      require   => Bam::Deploy[$deployment_code];
  }

  bam::start { $deployment_code:
    owner   => $owner,
    target  => $carbon_home,
    require => [
                Bam::Initialize[$deployment_code],
                Bam::Deploy[$deployment_code],
                Push_templates[$service_templates],
                Push_templates[$common_templates],
      ],
  }
}

