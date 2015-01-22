## Need to reorder this package

class mysql($user,$password){
  $root_user = "root"
  $root_password = "root"

#  file{"/etc/mysql":
#     ensure => directory
#  }

#  file{ "/etc/mysql/my.cnf":
#    ensure  => present,
#    source  => "puppet:///modules/mysql/server/my.cnf",
#    require => File["/etc/mysql"]
#  }

#  package { "MySQL-client":
#    ensure  => installed,
#    require => File["/etc/mysql/my.cnf"]
#  }
#  package { "MySQL-server":
#    ensure => installed,
#    require => File["/etc/mysql/my.cnf"]
#  }

 # exec { "Set MySQL server root password":
 #   subscribe   => [ Package["MySQL-server"], Package["MySQL-client"]],
 #   refreshonly => true,
 #   unless      => "mysqladmin -u$root_user -p$root_password status",
 #   path        => "/bin:/usr/bin",
 #   command     => "mysqladmin -u$root_user password $root_password",
 #   require     => Service["mysql"]
 # }

  #service{ "mysql":
  #  ensure  => "running",
  #  enable  => true,
  #  require => File["/etc/mysql/my.cnf"]
  #}

  exec { "create mysql users":
    unless  => "/usr/bin/mysql -u$user -p$password",
    command => "/usr/bin/mysql -u$root_user -p$root_password -e \"create user '$user'@'%' identified by '$password'; grant all privileges on *.* to $user@'%' identified by '$password'\"",
 }

}


