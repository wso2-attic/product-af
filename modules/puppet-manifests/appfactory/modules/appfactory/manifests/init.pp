class appfactory (
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
  $file_ignore_filter = undef,
  $s2gitblit_server   = undef

) inherits params {

  $deployment_code  = 'appfactory'
  $service_code     = 'appfactory'
  $carbon_home      = "${target}/wso2${service_code}-${version}"
  $esb_local_repo_zip_file = "esbcappslocalrepo.zip"

  $files_to_ignore  = $file_ignore_filter ? {
    "local" => ['*.jag','*.html','.svn'],
    default => ['.svn']
  }

  $service_templates  = [
                    "conf/appfactory/endpoints/ApplicationDeployer.epr", 
                    "conf/appfactory/endpoints/ApplicationManagementService.epr", 
                    "conf/appfactory/endpoints/ArtifactCreator.epr", 
                    "conf/appfactory/endpoints/CustomLifecyclesChecklistAdminService.epr", 
                    "conf/appfactory/endpoints/EmailSenderService.epr", 
                    "conf/appfactory/endpoints/EventNotificationService.epr", 
                    "conf/appfactory/endpoints/RepoManagementService.epr", 
                    "conf/appfactory/endpoints/TenantMgtService.epr", 
                    "conf/appfactory/endpoints/UserRegistrationService.epr", 
                    "conf/appfactory/endpoints/ContinousIntegrationService.epr", 
                    "conf/appfactory/endpoints/AppFactoryTenantMgtAdminService.epr", 
                    "conf/appfactory/endpoints/AppFactoryTenantInfraStructureInitializerService.epr", 
                    "conf/axis2/axis2.xml",
		                "conf/embedded-ldap.xml",
                    "conf/carbon.xml",
                    "conf/registry.xml",
                    "conf/user-mgt.xml",
                    "conf/jndi.properties",
                    "conf/issuetracker/issuetracker.xml",
                    "conf/security/authenticators.xml",
                    "conf/security/application-authenticators.xml",
                    "conf/datasources.properties",
                    "conf/email/invite-user-email-config.xml",
                    "conf/email/appfactory-confirmation-email-config.xml",
                    "conf/sso-idp-config.xml",
                    "deployment/server/jaggeryapps/dashboard/js/constants.js",
                    "deployment/server/jaggeryapps/cloudmgt/site/conf/cloud-mgt.xml",
                    "deployment/server/jaggeryapps/issuetracker/tracker.json",
                    "deployment/server/runtimes/iis-1.0.0.xml",
                    "deployment/server/runtimes/php-1.0.0.xml",
                    "deployment/server/runtimes/appserver-1.0.0.xml",
                    "deployment/server/runtimes/tomcat-1.0.0.xml",
                    "conf/datasources/appfactory-datasources.xml",
                    "conf/etc/cassandra.yaml",
          ]

  $common_templates = [
                    "conf/tenant-mgt.xml",
                    "conf/appfactory/appfactory.xml",
                    "conf/log4j.properties",
                    "conf/etc/cache.xml",
                    "conf/datasources/master-datasources.xml",
                    "conf/multitenancy/stratos.xml",
        ]

  tag ('appfactory')
#
#  # Export appfactory.xml to use for Stratos Manager and Appserver
#  
  @@file { "exported_appfactory.xml_${ec2_placement_availability_zone}":
##  @@file { "exported_appfactory.xml_${ipaddress}":
    ensure  => present,
    path    => "${carbon_home}/repository/conf/appfactory/appfactory.xml",
    mode    => '0755',
    content => template('wso2base/conf/appfactory/appfactory.xml.erb'),
    tag     => "appfactory_xml_${ec2_placement_availability_zone}",
  }
#
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
    security        => true,
    owner           => $owner,
    group           => $group,
    target          => $carbon_home,
    files_to_ignore => $files_to_ignore,
    require         => Initialize[$deployment_code],
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

  file { "/home/${owner}/.gitconfig":
      owner   => $owner,
      group   => $group,
      content => template("${service_code}/gitconfig.erb"),
      ensure  => present,
      require   => Deploy[$deployment_code]; 
  }

  file { "/home/${owner}/.netrc":
      owner   => $owner,
      group   => $group,
      content => template("${service_code}/netrc.erb"),
      ensure  => present,
      require   => Deploy[$deployment_code]; 
  }

  file { "${carbon_home}/maven.sh":
      owner   => $owner,
      group   => $group,
      content => template("${service_code}/maven.sh.erb"),
      mode    => '0755',
      ensure  => present,
      require => Deploy[$deployment_code];
  }

  start { $deployment_code:
      owner   => $owner,
      target  => $carbon_home,
      require => [  Initialize[$deployment_code],
                    Deploy[$deployment_code],
                    Push_templates[$service_templates],
                    Push_templates[$common_templates],
                    File["/home/${owner}/.netrc"],
                    File["/home/${owner}/.gitconfig"],
                    File["${carbon_home}/bin/wso2server.sh"]
                  ]
  }

  #check whether the server is started
  exec{
    "check_server_startup_status" :
      user      => $owner,
      path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      cwd       => "${carbon_home}/repository/logs",
      command   => "grep -r 'Mgt Console URL' ./",
      tries     => 20,
      try_sleep => 30,
      require   => Start[$deployment_code]
  }

  exec { "installing_maven_architypes":
    user    => $owner,
    path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    command => "${carbon_home}/maven.sh > /dev/null 2>&1 &",
    require => Exec["check_server_startup_status"]
  }

  exec { "copying_car_localrepo":
    user    => $owner,
    path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    cwd     => "${carbon_home}/resources",
    command => "unzip ${esb_local_repo_zip_file}",
    require => Deploy[$deployment_code];
  }

}
