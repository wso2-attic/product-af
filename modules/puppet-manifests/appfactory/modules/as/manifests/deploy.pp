define as::deploy ( $security, $target, $owner, $group ) {

  file {
    "/tmp/${as::deployment_code}":
      ensure          => present,
      owner           => $owner,
      group           => $group,
      sourceselect    => all,
      ignore          => '.svn',
      recurse         => true,
      source          => [
                          'puppet:///modules/as/configs/',
                          'puppet:///modules/as/patches/',
                          'puppet:///modules/wso2base/configs/',
                          'puppet:///modules/wso2base/patches/',
                        ]
  }

  exec {
    "Copy_${name}_modules_to_carbon_home":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "cp -r /tmp/${as::deployment_code}/* ${target}/; chown -R ${owner}:${owner} ${target}/; chmod -R 755 ${target}/",
      require => File["/tmp/${as::deployment_code}"];

    "Remove_${name}_temporory_modules_directory":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "rm -rf /tmp/${as::deployment_code}",
      require => Exec["Copy_${name}_modules_to_carbon_home"];
  }
}
