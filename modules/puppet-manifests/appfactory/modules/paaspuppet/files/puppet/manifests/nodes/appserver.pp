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

# appserver cartridge node
node /[0-9]{1,12}.(default|manager|worker).asdev/ inherits base {
  $docroot = "/mnt/${server_ip}/wso2as-5.2.1"
  require java	
  class {'agent':}
  class {'appserver':

        version            => '5.2.1',
        sub_cluster_domain => 'dev',
	      members            => undef,
	      offset		         => 0,
        mb_ip              => "puppet",
        mb_port            => 61616,
        hazelcast_port     => 4000,
	      config_db          => 'as_config',
        config_target_path => 'config',
        maintenance_mode   => 'zero',
        depsync            => false,
        clustering         => false,
	      cloud		           => true,
        owner              => 'root',
        group              => 'root',
        target             => "/mnt/${server_ip}",
	      as_stage	         => "Development",
        server_key	       => "DEV-AS",
        as_stage_prefix    => "dev",
	      mysql_server	     => "192.168.18.176",
  }

  Class['stratos_base'] -> Class['java'] -> Class['appserver'] ~> Class['agent']
}

node /[0-9]{1,12}.(default|manager|worker).astest/ inherits base {
  $docroot = "/mnt/${server_ip}/wso2as-5.2.1"
  require java	
  class {'agent':}
  class {'appserver':

        version            => '5.2.1',
        sub_cluster_domain => 'test',
	      members            => undef,
	      offset		         => 0,
        mb_ip              => "puppet",
        mb_port            => 61616,
        hazelcast_port     => 4000,
	      config_db          => 'as_config',
        config_target_path => 'config',
        maintenance_mode   => 'zero',
        depsync            => false,
        clustering         => false,
	      cloud		           => true,
        owner              => 'root',
        group              => 'root',
        target             => "/mnt/${server_ip}",
	      as_stage	         => "Testing",
        server_key	       => "TEST-AS",
        as_stage_prefix    => "test",
        mysql_server	     => "192.168.18.167",
  }

  Class['stratos_base'] -> Class['java'] -> Class['appserver'] ~> Class['agent']
}


node /[0-9]{1,12}.(default|manager|worker).asprod/ inherits base {
  $docroot = "/mnt/${server_ip}/wso2as-5.2.1"
  require java	
  class {'agent':}
  class {'appserver':

        version            => '5.2.1',
        sub_cluster_domain => 'prod',
	      members            => undef,
	      offset		         => 0,
        mb_ip              => "puppet",
        mb_port            => 61616,
        hazelcast_port     => 4000,
	      config_db          => 'as_config',
        config_target_path => 'config',
        maintenance_mode   => 'zero',
        depsync            => false,
        clustering         => false,
	      cloud		           => true,
        owner              => 'root',
        group              => 'root',
        target             => "/mnt/${server_ip}",
	      as_stage	         => "Production",
        server_key	       => "PROD-AS",
        as_stage_prefix    => "prod",
        mysql_server	     => "192.168.18.169",
  }

  Class['stratos_base'] -> Class['java'] -> Class['appserver'] ~> Class['agent']
}


