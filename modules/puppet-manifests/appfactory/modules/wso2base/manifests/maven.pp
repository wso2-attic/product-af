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
  }

  file { '/opt/mvn':
    ensure  => link,
    target  => "/opt/${maven_dir}",
    require => Exec["${name}_extract_mvn"];
  }
}
