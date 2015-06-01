class wso2base::maven {

  $maven_package = 'apache-maven-3.0.5-bin.tar.gz'
  $maven_dir     = 'apache-maven-3.0.5' 

  exec {
    "${name}_download_mvn":
      path    => ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
      cwd     => "/opt/",
      unless  => "test -f /opt/${maven_package}",
      command => "wget -q ${package_repo}/${maven_package}";

    "${name}_extract_mvn":
      path    => ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
      unless  => "test -d /opt/${maven_dir}",
      cwd     => "/opt/",
      command => "tar xvfz ${maven_package}",
      require => Exec["${name}_download_mvn"];

    "${name}_create_mvn_plugins_dir":
      path    => ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
      unless  => "test -d /opt/${maven_dir}",
      cwd     => "/tmp/",
      command => "mkdir tmp_mvn_plugins",
      require => Exec["${name}_extract_mvn"];

    "${name}_execute_mvn_command":
      path    => ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
      cwd     => "/tmp/tmp_mvn_plugins",
      command => "mvn clean install";


  }

  file { '/opt/mvn':
    ensure  => link,
    target  => "/opt/${maven_dir}",
    require => Exec["${name}_extract_mvn"];
  }

   file { '/tmp/tmp_mvn_plugins':
    ensure  => present,
    recurse => remote,
    source  => [
                'puppet:///modules/wso2base/files/tmp_mvn_plugins/'
               ],
    require => Exec["${name}_create_mvn_plugins_dir"],
    notify => Exec["${name}_execute_mvn_command"];

  }
}
