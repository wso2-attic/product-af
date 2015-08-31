class nginx($owner,$group,$target,$ext_version){
  $root_user = "root"
  $root_password = "root"
  $deployment_code = "nginx"

  $packages = [
    'nginx',
    'dnsmasq'
  ]

  package { $packages:
    ensure => installed,
  }

  initialize { $deployment_code:
    repo      => $package_repo,
    version   => $ext_version,
    local_dir => $local_package_dir,
    owner     => $owner,
    target    => $target
  }

  service { 'nginx':
    ensure    => stopped,
    name      => 'nginx',
    hasstatus => true,
    pattern   => 'nginx',
    require   => Package['nginx'];
  }

  file { "/etc/dhcp/dhclient.conf":
    ensure  => present,
    owner   => $owner,
    group   => $group,
    content => template("nginx/dhcpclient.conf.erb"),
    require => Package["dnsmasq"];
  }

  file { "/etc/dnsmasq.conf":
    ensure  => present,
    owner   => $owner,
    group   => $group,
    content => template("nginx/dnsmasq.conf.erb"),
    require => Package["dnsmasq"];
  }

  service{ "dnsmasq" :
    ensure => running,
    subscribe => [File["/etc/dhcp/dhclient.conf"],File["/etc/dnsmasq.conf"]],
    require => Package["dnsmasq"];
  }

  exec { "strating_nginx":
    user    => $owner,
    environment => "JAVA_HOME=/opt/java",
    cwd     => "${target}/apache-stratos-nginx-extension-${ext_version}/bin",
    path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    command => "bash nginx-extension.sh > extension.log  2>&1 &",
    require => Initialize[$deployment_code]
  }
}


