class jppserver (
  $version            = '5.2.0',
  $offset             = 0,
  $tribes_port        = 4000,
  $group_mgt_port     = 4000,
  $config_db          = 'jppserver_config',
  $datasource         = 'WSO2JSConfigDB',
  $maintenance_mode   = true,
  $depsync            = false,
  $sub_cluster_domain = 'mgt',
  $owner              = 'root',
  $group              = 'root',
  $tenant_id          = 'base',
  $target             = '/mnt',
  $members            = {},
) {

  $deployment_code = 'jppserver'
  $service_code    = 'as'
  $carbon_home     = "${target}/wso2${service_code}-${version}"

  $service_templates = [
                          'conf/axis2/axis2.xml',
                          'conf/carbon.xml',
                          'conf/tomcat/catalina-server.xml',
                          'jenkins/configs/org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager.xml',
                          'jenkins/configs/config.xml',
                          'jenkins/configs/hudson.plugins.git.GitSCM.xml',
                        ]

  $common_templates = [
                        'conf/tenant-mgt.xml',
                        'conf/appfactory/appfactory.xml',
                        'conf/datasources/master-datasources.xml', 
                        'conf/user-mgt.xml',
                      ]

  tag($service_code)

  clean { $deployment_code:
    mode   => $maintenance_mode,
    target => $carbon_home,
  }

  initialize { $deployment_code:
    repo      => $package_repo,
    version   => $version,
    mode      => $maintenance_mode,
    service   => $service_code,
    local_dir => $local_package_dir,
    owner     => $owner,
    target    => $target,
    require   => Clean[$deployment_code],
  }

  deploy { $deployment_code:
    security => true,
    owner    => $owner,
    group    => $group,
    target   => $carbon_home,
    require  => Initialize[$deployment_code],
  }

  if $sub_cluster_domain == 'worker' {
    createworker { $deployment_code:
      target  => $carbon_home,
      require => Deploy[$deployment_code],
    }
  }

  push_templates {
    $service_templates:
      target    => $carbon_home,
      directory  => $deployment_code,
      require   => Deploy[$deployment_code];

    $common_templates:
      target    => $carbon_home,
      directory => 'wso2base',
      require   => Deploy[$deployment_code],
  }

  file { "/home/${owner}/.gitconfig":
    ensure  => present,
    owner   => $owner,
    group   => $group,
    content => template("${deployment_code}/gitconfig.erb");
  }

  file { "/home/${owner}/.netrc":
    ensure  => present,
    owner   => $owner,
    group   => $group,
    content => template("${deployment_code}/netrc.erb");
  }

  start { $deployment_code:
    owner   => $owner,
    target  => $carbon_home,
    require => [
      Initialize[$deployment_code],
      Deploy[$deployment_code],
      Push_templates[$service_templates],
      Push_templates[$common_templates],
      ],
  }
}
