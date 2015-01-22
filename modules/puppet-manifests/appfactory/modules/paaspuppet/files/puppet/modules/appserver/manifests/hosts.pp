define  appserver::hosts{

    file { "/etc/hosts":
        owner   => root,
        group   => root,
        mode    => 775,
        content => template("appserver/hosts.erb"),
    } 
}
