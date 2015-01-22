class paaspuppet (
    $maintenance_mode=true,
    $owner=root,
    $group=root,
    $target="/etc/puppet",
    $ppaas_mb_ip        = "puppet",
    $ppaas_cep_ip       = "puppet",
    $ppaas_mysql_host   = "puppet",
    $server_ip  = undef,
    $local_dir  = "/mnt/packs",
    $appserver_subdomain = undef
  ) inherits params {


  $deployment_code	= "paaspuppet"

  $deployment_files = [
                        "apache-stratos-cartridge-agent-4.0.0-wso2v1-bin.zip",
                        "jdk-7u51-linux-x64.tar.gz",
                        "wso2as-5.2.1.zip",
                        "apache-activemq-5.9.1-bin.tar.gz"
                      ]

  $activemq_extracted_directory = "apache-activemq-5.9.1"
  $activemq_client_jars = ["activemq-broker-5.9.1.jar" , "activemq-client-5.9.1.jar" , "geronimo-j2ee-management_1.1_spec-1.0.1.jar" , "geronimo-jms_1.1_spec-1.1.1.jar" , "hawtbuf-1.9.jar" ]

  $deployment_files_target = {
                                agent     => $deployment_files[0],
                                java      => $deployment_files[1],
                                appserver => $deployment_files[2],
                                activemq  => $deployment_files[3]
                              }

  $service_templates 	= [
                  "manifests/nodes/base.pp",
                  "puppet.conf",
                  "manifests/nodes/appserver.pp"
                 ]

  tag ('privatepaas')

#  clean { $deployment_code:
#          mode   => $maintenance_mode,
#          target => $private_paas_home,
#  }

  initialize { $deployment_code:
    repo              => $package_repo,
    mode              => $maintenance_mode,
    local_dir         => $local_package_dir,
    owner             => $owner,
    deployment_files  => $deployment_files,
    #require           => Clean[$deployment_code],
  }

  deploy { $deployment_code:
      security            => true,
      owner               => $owner,
      group               => $group,
      target              => "/etc/puppet"
  }

  deploy-packs {
    "Copying packs":
    security     => true,
    owner        => $owner,
    group        => $group,
    local_dir    => $local_dir,
    target       => "/etc/puppet/modules",
    deployment_files_target => $deployment_files_target,
    activemq_extracted_directory => $activemq_extracted_directory,
    activemq_client_jars => $activemq_client_jars,
    require      => Deploy[$deployment_code]
  }

  push_templates {
    $service_templates:
      target      => "/etc/puppet",
      require     => Deploy[$deployment_code]
  }

  file { "appfactory.xml_for_appserver":
    ensure  => present,
    owner   => $owner,
    group   => $group,
    mode    => '0755',
    path  => "/etc/puppet/modules/appserver/files/configs/repository/conf/appfactory/appfactory.xml",
    content => template("wso2base/conf/appfactory/appfactory.xml.erb")
  }

  # Starting puppet-master
  service{ "puppetmaster":
    ensure  => "running",
    enable  => true,
    require => [Push_templates[$service_templates],File["appfactory.xml_for_appserver"]]
  }

}
