class wso2base{
  include hosts
  include packages
  include java

  wso2base::users { 'user':
    puppet_username   => $puppet_username,
    puppet_password => $puppet_password,
  }

  #We ensure that the direcotry is there.
  #file { "/home/ubuntu/.ssh":
  #  ensure => "directory",
  #}
  #file { '/home/ubuntu/.ssh/authorized_keys':
  #  ensure => present,
  #  owner  => 'ubuntu',
  #  group  => 'ubuntu',
  #  source => 'puppet:///modules/wso2base/authorized_keys';
  #}

  file { '/etc/environment':
    ensure => present,
    source => 'puppet:///modules/wso2base/environment',
  }

  cron { 'ntpdate':
    command => "/usr/sbin/ntpdate pool.ntp.org",
    user    => root,
    minute  => '*/50'
  }
}
