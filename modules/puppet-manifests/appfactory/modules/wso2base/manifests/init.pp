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

 file { '/tmp/pom.xml':
    ensure => present,
    source => 'puppet:///modules/wso2base/pom.xml',
  }

 file { '/tmp/bin.xml':
    ensure => present,
    source => 'puppet:///modules/wso2base/bin.xml',
    require=>File["/tmp/pom.xml"];
  }

file { '/opt/apache-maven-3.0.5/':
    ensure => present,
    notify => Exec["execute_mvn_command"],
    require=>File["/tmp/bin.xml"];
  }

  exec { "execute_mvn_command":
      path    => ["/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/mvn/bin:/opt/java/bin"],
      cwd     => "/tmp/",
      command => "mvn clean install";
  }

  cron { 'ntpdate':
    command => "/usr/sbin/ntpdate pool.ntp.org",
    user    => root,
    minute  => '*/50'
  }
}
