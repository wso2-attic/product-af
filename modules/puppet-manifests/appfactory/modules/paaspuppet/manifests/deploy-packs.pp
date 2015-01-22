define paaspuppet::deploy-packs( $security, $target, $local_dir, $owner, $group, $deployment_files_target, $activemq_extracted_directory ,$activemq_client_jars) {

  #copying agent package
  exec {
      "Copy_${deployment_files_target['agent']}_pack_to_privatepaas_puppet":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "cp -r ${local_dir}/${deployment_files_target['agent']} ${target}/agent/files; chown -R ${owner}:${owner} ${target}/agent/files; chmod -R 755 ${target}/agent/files",
  }

  #copying java pack
  exec {
    "Copy_${deployment_files_target['java']}_pack_to_privatepaas_puppet":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "cp -r ${local_dir}/${deployment_files_target['java']} ${target}/java/files; chown -R ${owner}:${owner} ${target}/java/files; chmod -R 755 ${target}/agent/files",
  }

  #copying appserver pack
  exec {
    "Copy_${deployment_files_target['appserver']}_pack_to_privatepaas_puppet":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "cp -r ${local_dir}/${deployment_files_target['appserver']} ${target}/appserver/files; chown -R ${owner}:${owner} ${target}/appserver/files; chmod -R 755 ${target}/agent/files",
  }

  # Copying activemq client jars

  exec {
    "Copy_${deployment_files_target['activemq']}_pack_to_privatepaas_puppet":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "cp -r ${local_dir}/${deployment_files_target['activemq']} ${target}/agent/files/activemq; chown -R ${owner}:${owner} ${target}/agent/files/activemq; chmod -R 755 ${target}/agent/files/activemq",
  }

  #extracting activemq
  exec{ "Extracting activemq distribution":
    path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
    cwd       => "${target}/agent/files/activemq",
    unless    => "test -f ${target}/${activemq_extracted_directory}",
    command   => "mkdir -p tmp; mv ${deployment_files_target['activemq']} tmp/;tar -xf tmp/${deployment_files_target['activemq']} -C tmp/",
    require  => Exec["Copy_${deployment_files_target['activemq']}_pack_to_privatepaas_puppet"]
  }

  copyActivemqClientJarsToAgent{ $activemq_client_jars:
    target   => "$target/agent/files/activemq",
    activemq_extracted_directory => $activemq_extracted_directory,
    require  => Exec["Extracting activemq distribution"]
  }

  copyActivemqClientJarsToAppServer{ $activemq_client_jars:
    target   => "$target/appserver/files/configs/repository/components/lib",
    activemq_extracted_directory => $activemq_extracted_directory,
    activemqTarget =>  "$target/agent/files/activemq",
    require  => Exec["Extracting activemq distribution"]
  }

  # Removing the tempory stuff
  exec{ "Removing tempory dir":
    path      => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin',
    cwd       => "${target}/agent/files/activemq",
    command   => "rm -rf tmp",
    require  => [CopyActivemqClientJarsToAgent[$activemq_client_jars], CopyActivemqClientJarsToAppServer[$activemq_client_jars]]
  }
}

define copyActivemqClientJarsToAgent(
  $target,
  $activemq_extracted_directory
){

  exec {"Copy_${name}_to_${target}":
    path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    cwd     => $target,
    command => "cp -r ${target}/tmp/${activemq_extracted_directory}/lib/${name} ${target}; chown -R ${owner}:${owner} ${target}; chmod -R 755 ${target}"
  }

}

define copyActivemqClientJarsToAppServer(
  $target,
  $activemqTarget,
  $activemq_extracted_directory
){

  exec {"Copy_${name}_to_${target}":
    path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    cwd     => $target,
    command => "cp -r ${activemqTarget}/tmp/${activemq_extracted_directory}/lib/${name} ${target}; chown -R ${owner}:${owner} ${target}; chmod -R 755 ${target}"
  }

}
