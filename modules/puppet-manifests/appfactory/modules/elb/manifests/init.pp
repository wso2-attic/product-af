class elb (
  $version            = undef,
  $services           = undef,
  $members            = undef,
  $cluster_domain     = 'elb',
  $elbtype            = 's2',
  $maintenance_mode   = 'refresh',
  $cloud              = false,
  $owner              = 'root',
  $group              = 'root',
  $target             = '/mnt',
  $localmember_host   = "localhost",
  $auto_scaler        = false,
  $auto_failover      = false,
) inherits params {

  $deployment_code  = 'elb'
  $service_code     = 'elb'
  $carbon_home      = "${target}/wso2${service_code}-${version}"

  $service_templates  = [
            "conf/axis2/axis2.xml",
            "conf/carbon.xml",
            "conf/loadbalancer.conf",
# Adding a private master datasource only to avoid h2 database issue when clustering. 
            "conf/datasources/master-datasources.xml",
          ]

    $common_templates = [
            "conf/log4j.properties",
            "conf/etc/cache.xml",
        ]

  tag ('elb')

  clean {
    $deployment_code:
      mode   => $maintenance_mode,
      target => $carbon_home,
  }

  initialize {
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

  deploy { $deployment_code:
    security => true,
    owner    => $owner,
    group    => $group,
    target   => $carbon_home,
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
