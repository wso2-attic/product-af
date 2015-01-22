define appfactory::deploy ( $security, $target, $owner, $group, $files_to_ignore ) {

  file {
    "/tmp/${appfactory::deployment_code}":
      ensure          => present,
      owner           => $owner,
      group           => $group,
      sourceselect    => all,
      ignore          => $files_to_ignore,
      recurse         => true,
      source          => [
                          'puppet:///modules/appfactory/configs/',
                          'puppet:///modules/appfactory/patches/',
                          'puppet:///modules/wso2base/configs/',
                          'puppet:///modules/wso2base/patches/',
                        ]
  }

  exec {
    "Copy_${name}_modules_to_carbon_home":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "cp -r /tmp/${appfactory::deployment_code}/* ${target}/; chown -R ${owner}:${owner} ${target}/; chmod -R 755 ${target}/",
      require => File["/tmp/${appfactory::deployment_code}"];

    "Remove_${name}_temporory_modules_directory":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "rm -rf /tmp/${appfactory::deployment_code}",
      require => Exec["Copy_${name}_modules_to_carbon_home"];
  }
}
