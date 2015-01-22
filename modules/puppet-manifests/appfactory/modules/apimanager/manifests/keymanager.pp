class apimanager::keymanager (
  $version            = undef,
  $offset             = 0,
  $services           = undef,
  $members            = undef,
  $clustering         = false,
  $sub_cluster_domain = "mgt",
  $maintenance_mode   = 'refresh',
  $localmember_port   = '4000',
  $config_db          = governance,
  $depsync            = false,
  $cloud              = false,
  $owner              = 'root',
  $group              = 'root',
  $target             = '/mnt',
  $auto_scaler        = false,
  $auto_failover      = false,
) inherits params {

  $deployment_code  = 'apimanager'
  $service_code     = 'am'
  $amtype           = 'keymanager'
  $carbon_home      = "${target}/wso2${service_code}-${version}"

  $service_templates  = [
                    "conf/api-manager.xml",
                    "conf/carbon.xml",
                    "conf/registry.xml",
                    "conf/log4j.properties",
                    "conf/axis2/axis2.xml",
                    "conf/datasources/am-datasources.xml",
          ]

  $common_templates = [
                    "conf/user-mgt.xml",
                    "conf/tenant-mgt.xml",
                    "conf/datasources/master-datasources.xml",
        ]

  tag ('apimanager')

  apimanager::clean {
    $deployment_code:
      mode   => $maintenance_mode,
      target => $carbon_home,
  }

  apimanager::initialize {
    $deployment_code:
      repo      => $package_repo,
      version   => $version,
      mode      => $maintenance_mode,
      service   => $service_code,
      local_dir => $local_package_dir,
      owner     => $owner,
      target    => $target,
      require   => Clean[$deployment_code],
  }

  apimanager::deploy { $deployment_code:
    security => true,
    owner    => $owner,
    group    => $group,
    target   => $carbon_home,
    amtype   => $amtype,
    require  => Initialize[$deployment_code],
  }

  push_templates {
    $service_templates:
      target     => $carbon_home,
      directory  => $deployment_code,
      require    => Deploy[$deployment_code];

    $common_templates:
      target     => $carbon_home,
      directory  => "wso2base",
      require    => Deploy[$deployment_code];
  }

  file { "${carbon_home}/bin/wso2server.sh":
      ensure    => present,
      owner     => $owner,
      group     => $group,
      mode      => '0755',
      content   => template("${deployment_code}/wso2server.sh.erb"),
      require   => Deploy[$deployment_code];
  }

  start {
    $deployment_code:
      owner   => $owner,
      target  => $carbon_home,
      require => [  Initialize[$deployment_code],
                    Deploy[$deployment_code],
                    Push_templates[$service_templates],],
        }

}
