define identity::deploy ( $security, $target, $owner, $group, $is_cloud_deployment ) {

  file {
    "/tmp/${identity::deployment_code}":
      ensure          => present,
      owner           => $owner,
      group           => $group,
      sourceselect    => all,
      ignore          => '.svn',
      recurse         => true,
      source          => [
                          'puppet:///modules/wso2base/configs/',
                          'puppet:///modules/wso2base/patches/',
                          'puppet:///modules/identity/configs/',
                          'puppet:///modules/identity/patches/']
  }
  # We modify the authenticationendpoint.war for non cloud deployments
  if $is_cloud_deployment == false{
    authenticationendpoint{"${identity::deployment_code}":
      security => true,
      owner    => $owner,
      group    => $group,
      target   => $target,
      require  => File["/tmp/${identity::deployment_code}"],
      notify   => Exec["Copy_${name}_modules_to_carbon_home"]
    }
  }

  exec {
    "Copy_${name}_modules_to_carbon_home":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "cp -r /tmp/${identity::deployment_code}/* ${target}/; chown -R ${owner}:${owner} ${target}/; chmod -R 755 ${target}/",
      require => File["/tmp/${identity::deployment_code}"];

    "Remove_${name}_temporory_modules_directory":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "rm -rf /tmp/${identity::deployment_code}",
      require => Exec["Copy_${name}_modules_to_carbon_home"];
  }
}
