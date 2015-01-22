define elb::deploy ( $security, $target, $owner, $group ) {

  file {
    "/tmp/${elb::deployment_code}":
      ensure          => present,
      owner           => $owner,
      group           => $group,
      sourceselect    => all,
      ignore          => '.svn',
      recurse         => true,
      source          => [
                          'puppet:///modules/elb/configs/',
                          'puppet:///modules/elb/patches/',
                          'puppet:///modules/wso2base/configs/',
                          'puppet:///modules/wso2base/patches/',
                        ]
  }

  exec {
    "Copy_${name}_modules_to_carbon_home":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "cp -r /tmp/${elb::deployment_code}/* ${target}/; chown -R ${owner}:${owner} ${target}/; chmod -R 755 ${target}/",
      require => File["/tmp/${elb::deployment_code}"];

    "Remove_${name}_temporory_modules_directory":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "rm -rf /tmp/${elb::deployment_code}",
      require => Exec["Copy_${name}_modules_to_carbon_home"];
  }
}
