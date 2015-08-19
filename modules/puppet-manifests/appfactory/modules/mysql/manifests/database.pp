## Need to reorder this package

class mysql::database(
                  $configs,
                  $base_dir = '/mnt'){

  $password = "root"

#  file{"/etc/mysql":
#     ensure => directory
#  }
#
#  file{ "/etc/mysql/my.cnf":
#    ensure  => present,
#    source  => "puppet:///modules/mysql/server/my.cnf",
#    require => File["/etc/mysql"]
#  }
#
#  package { "mysql-client":
#    ensure  => installed,
#    require => File["/etc/mysql/my.cnf"]
#  }
#  package { "mysql-server":
#    ensure => installed,
#    require => File["/etc/mysql/my.cnf"]
#  }
#
#  exec { "Set MySQL server root password":
#    subscribe   => [ Package["mysql-server"], Package["mysql-client"]],
#    refreshonly => true,
#    unless      => "mysqladmin -uroot -p$password status",
#    path        => "/bin:/usr/bin",
#    command     => "mysqladmin -uroot password $password",
#    require     => Service["mysql"]
#  }
#
#  service{ "mysql":
#    ensure  => "running",
#    enable  => true,
#    require => File["/etc/mysql/my.cnf"]
#  }

  create_resources(database,$configs)

  define database($script_name,$config){
    $mysql_host = $config[0]
    $database_name = $config[1]
    $database_user = $config[2]
    $database_user_password = $config[3]

    file {
      "/tmp/$database_name":
      ensure => directory,
      require => Exec["create_dirs_for_$database_name"]
    }
    exec{
      "create_dirs_for_$database_name":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd => "/tmp",
      command => "mkdir -p /tmp/ ${database_name}",
    }
    mysqldb {$database_name:
      user => $database_user,
      password => $database_user_password,
      host => $mysql_host,
      sql => "/tmp/$database_name/$script_name.sql",
      script_name => $script_name,
      require  => File["/tmp/$database_name/$script_name.sql"],
    }

    file {
      "/tmp/$database_name/$script_name.sql":
        ensure => present,
        source => "puppet:///modules/mysql/sql/$script_name.sql",
        require => File["/tmp/$database_name"]
    }
  }

  define mysqldb( $user, $password, $script_name, $host, $sql ) {
    $root_user = "root"
    $root_password = "root"

    $sql_command = $script_name ? {
      undef   => "create database $name; grant all on $name.* to $user@'%' identified by '$password';",
      default => "create database $name; grant all on $name.* to $user@'%' identified by '$password'; use $name ;source $sql;"
    }
    
   if $operatingsystem == "Darwin"{
     exec { "create-$name-db":
      unless => "/usr/local/mysql/bin/mysql -u$root_user -p$root_password $name",
      command => "/usr/local/mysql/bin/mysql -u$root_user -p$root_password -e \"$sql_command\"",
    }
   }
    else{
    exec { "create-$name-db":
       unless => "/usr/bin/mysql -u$root_user -p$root_password $name",
       command => "/usr/bin/mysql -u$root_user -p$root_password -e \"$sql_command\"",
     }
   }

    #exec { "create-$name-db-tables":
    #  subscribe => Exec["create-$name-db"],
    #  command   => "/usr/bin/mysql -u$root_user -p$root_password $name < $sql",
    #}
  }
}


