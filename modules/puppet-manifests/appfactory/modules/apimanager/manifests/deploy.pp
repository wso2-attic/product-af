define apimanager::deploy ( $security, $target, $owner, $group, $amtype, $files_to_ignore ) {

  file {
    "/tmp/${amtype}":
      ensure          => present,
      owner           => $owner,
      group           => $group,
      sourceselect    => all,
      ignore          => $files_to_ignore,
      recurse         => true,
      source          => [
                          'puppet:///modules/apimanager/configs/',
                          'puppet:///modules/apimanager/patches/',
                          'puppet:///modules/wso2base/configs/',
                          'puppet:///modules/wso2base/patches/',
                        ]
  }

  exec {
    "Copy_${name}_modules_to_carbon_home":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "cp -r /tmp/${amtype}/* ${target}/; chown -R ${owner}:${owner} ${target}/; chmod -R 755 ${target}/",
      require => File["/tmp/${amtype}"];

    "Remove_${name}_temporory_modules_directory":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "rm -rf /tmp/${amtype}",
      require => Exec["Copy_${name}_modules_to_carbon_home"];
  }
}
