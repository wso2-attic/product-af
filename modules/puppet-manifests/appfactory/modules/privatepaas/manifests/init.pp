class privatepaas (
  $iaas_provider = undef,
  $maintenance_mode=true,
  $owner=root,
  $group=root,
  $target="/mnt",
  $ppaas_mb_ip        = "puppet",
  $ppaas_cep_ip       = "puppet",
  $ppaas_mysql_host   = "puppet",
  $server_ip  = undef,
  $appserver_subdomain = undef,
  $offset = 0,
  $cartridge_alias = undef,
  $cartridge_type = undef,
  $cert_name = undef,
  $user_store = "userstore" ,
  $registry_db_schema  = "REGISTRY_DB",
  $config_db_schema    = "CONFIG_DB",
  $paas_stage = "Development"
  ) inherits params {


  $deployment_code	= "privatepaas"

  $deployment_files =[ "apache-activemq-5.9.1-bin.tar.gz",
                       "apache-stratos-4.1.0-SNAPSHOT.zip"
#                      "apache-stratos-cartridge-agent-4.0.0-wso2v1-bin.zip",
#                      "apache-stratos-load-balancer-4.0.0-wso2v1.zip",
#                      "jdk-7u51-linux-x64.tar.gz",
#                      "mysql-connector-java-5.1.29-bin.jar",
#                      "wso2as-5.2.1.zip",
#                      "wso2-private-paas-4.0.0-installer.zip"
                      ]
  $service_templates 	= [
                  "conf.sh",
 	              "boot.sh",
                  "clean.sh"
      ]

  $stratos_manager_templates = [
                  "repository/conf/carbon.xml",
                  "repository/conf/cartridge-config.properties",
                  "repository/conf/security/authenticators.xml",
                  "repository/conf/tenant-mgt.xml",
                  "repository/conf/user-mgt.xml",
                  "repository/conf/datasources/master-datasources.xml",
                  "repository/conf/registry.xml",
                  "bin/stratos.sh"
  ]


  $openstack_templates = [
                  "repository/conf/json/partition.json",
                  "repository/conf/json/autoscale-policy.json",
                  "repository/conf/json/cartridge-definition.json",
                  "repository/conf/json/nodejs-cart.json",
                  "repository/conf/json/deployment-policy.json",
                  "repository/conf/json/service-deployment.json",
                  "repository/conf/json/executor.sh"
  ]


  $docker_templates = [
                  "repository/conf/kubernetes/kubernetes-cluster.json",
                  "repository/conf/kubernetes/autoscaling-policy.json",
                  "repository/conf/kubernetes/network-partition.json",
                  "repository/conf/kubernetes/deployment-policy.json",
                  "repository/conf/kubernetes/wso2as_dev.json",
                  "repository/conf/kubernetes/wso2as_test.json",
                  "repository/conf/kubernetes/wso2as_prod.json",
                  "repository/conf/kubernetes/application-policy_dev.json",
                  "repository/conf/kubernetes/application-policy_test.json",
                  "repository/conf/kubernetes/application-policy_prod.json",
                  "repository/conf/kubernetes/application_dev.json",
                  "repository/conf/kubernetes/application_test.json",
                  "repository/conf/kubernetes/application_prod.json",
                  "repository/conf/kubernetes/executor.sh",
                  "repository/conf/kubernetes/deployment.log"
  ]


  $common_templates = [
                        "conf/appfactory/appfactory.xml"
                      ]

  tag ('privatepaas')

  $private_paas_home = "${target}/${deployment_code}"

  clean { $deployment_code:
          mode   => $maintenance_mode,
          target => $private_paas_home,
  }

  initialize { $deployment_code:
      repo              => $package_repo,
      mode              => $maintenance_mode,
      local_dir         => $local_package_dir,
      owner             => $owner,
      target            => $target,
      deployment_files  => $deployment_files,
      require           => Clean[$deployment_code],
  }

  deploy { $deployment_code:
      security            => true,
      owner               => $owner,
      group               => $group,
      local_dir           => $local_package_dir,
      target              => $private_paas_home,
      require             => Initialize[$deployment_code],
  }

  file { "/home/${owner}/.netrc":
    owner   => $owner,
    group   => $group,
    content => template("${deployment_code}/netrc.erb"),
    ensure  => present,
    require   => Deploy[$deployment_code];
  }

  push_templates { 
    $service_templates:
      target      => $private_paas_home,
      directory   => "${deployment_code}/stratos",
      require     => Deploy[$deployment_code],
      notify      => Exec["strating_stratos_setup"];
  }

  exec { "strating_stratos_setup":
    user    => "root",
    cwd     => "${private_paas_home}",
    path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    command => "/bin/bash boot.sh",
    refresh => "echo starting"
  }

  deploy-sm{ $deployment_code:
    security            => true,
    owner               => $owner,
    group               => $group,
    local_dir           => $local_package_dir,
    target              => $private_paas_home,
    require             => Exec["strating_stratos_setup"]
  }

  push_templates {
    $common_templates:
      target      => "${private_paas_home}/install/apache-stratos-default/repository",
      directory   => "wso2base",
      require     => Deploy[$deployment_code]
  }

  if $iaas_provider == "os" {

	  push_templates { $openstack_templates:
		  target      => "${private_paas_home}/install/apache-stratos-default",
			      directory   => "${deployment_code}/appfactory_deployment",
			      require     => Deploy[$deployment_code]
	  }

	  push_templates { $stratos_manager_templates:
		  target      => "${private_paas_home}/install/apache-stratos-default",
			      directory   => "${deployment_code}/appfactory_deployment",
			      require     => [Deploy-sm [$deployment_code], Push_templates[$common_templates],
			      Push_templates[$service_templates], Push_templates[$openstack_templates] ],
			      notify      => Exec["start_stratos_servers"]
	  }

  }else{

	  push_templates { $docker_templates:
		  target      => "${private_paas_home}/install/apache-stratos-default",
			      directory   => "${deployment_code}/appfactory_deployment",
			      require     => Deploy[$deployment_code]
	  }


	  push_templates { $stratos_manager_templates:
		  target      => "${private_paas_home}/install/apache-stratos-default",
			      directory   => "${deployment_code}/appfactory_deployment",
			      require     => [Deploy-sm [$deployment_code], Push_templates[$common_templates],
			      Push_templates[$service_templates], Push_templates[$docker_templates] ],
			      notify      => Exec["start_stratos_servers"]
	  }

  }

  exec{
    "start_stratos_servers" :
    user    => $owner,
    path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    cwd     => "${private_paas_home}/stratos-installer",
    creates => "${private_paas_home}/install/apache-stratos-default/repository/logs/wso2carbon.log",
    command => "/bin/bash start-servers.sh",
    require => Push_templates[$stratos_manager_templates],
    refreshonly => true
  }

  #check whether the server is started
  exec{
    "check_server_startup_status" :
    user      => $owner,
    path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    cwd       => "${private_paas_home}/install/apache-stratos-default/repository/logs",
    command   => "grep -r 'Mgt Console URL' ./",
    tries     => 20,
    try_sleep => 30,
    require   => Exec["start_stratos_servers"],
  }

  
  if $iaas_provider == "os" {
 #Run the curl command to deploy the cartridges
  exec {
   "deploy_os_stratos_cartridges" :
    user    => $owner,
    path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    cwd     => "${private_paas_home}/install/apache-stratos-default/repository/conf/json",
    command => "/bin/bash executor.sh",
    refresh => "echo deploying cartridges",
    require => Exec["check_server_startup_status"]
  }

}else{
  #Run the curl command to deploy the cartridges
  exec {
   "deploy_dokcer_stratos_cartridges" :
    user    => $owner,
    path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    cwd     => "${private_paas_home}/install/apache-stratos-default/repository/conf/kubernetes",
    command => "/bin/bash executor.sh >> deployment.log",
    refresh => "echo deploying docker containers",
    require => Exec["check_server_startup_status"]
  }
}
}
