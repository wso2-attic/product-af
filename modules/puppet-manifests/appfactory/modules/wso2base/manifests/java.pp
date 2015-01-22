class wso2base::java {

  $java_package = 'jdk-7u51-linux-x64.tar.gz'
  $java_dir     = 'jdk1.7.0_51'

  exec {
    "${name}_download_java":
      path    => ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
      cwd     => "/opt/",
      unless  => "test -f /opt/${java_package}",
      command => "wget -q ${package_repo}/${java_package}";

    "${name}_extract_java":
      path    => ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin"],
      unless  => "test -d /opt/${java_dir}",
      cwd     => "/opt/",
      command => "tar xvfz ${java_package}",
      require => Exec["${name}_download_java"];
  }

  file { '/opt/java':
    ensure  => link,
    target  => "/opt/${java_dir}",
    require => Exec["${name}_extract_java"];
  }
}
