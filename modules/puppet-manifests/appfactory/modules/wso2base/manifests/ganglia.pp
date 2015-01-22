class ganglia {

  package { 'ganglia-monitor':
    ensure  => latest,
  }

  file { '/etc/ganglia/gmond.conf':
     ensure  => present,
     notify  => Service['ganglia-monitor'],
     content => template("gmond.conf.erb"),
     require => Package['ganglia-monitor'];
  }

  service { 'ganglia-monitor':
    ensure  => running,
    enable  => true,
    require => Package['ganglia-monitor'];
  }

#  exec { 'Restart ganglia-monitor':
#    command => '/etc/init.d/ganglia-monitor restart',
#    require => File['/etc/ganglia/gmond.conf'];
#  }
}
