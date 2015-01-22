class mysql::clean($databases){

  $password = "root"

  service{ "mysql":
    ensure  => "running",
    enable  => true
  }

  drop_database{$databases:}

  define drop_database() {
    $root_user = "root"
    $root_password = "root"

    $sql_command = "DROP DATABASE IF EXISTS ${title};"

    exec { "drop-${title}-db":
      command => "/usr/bin/mysql -u$root_user -p$root_password -e \"$sql_command\"",
    }
  }
}