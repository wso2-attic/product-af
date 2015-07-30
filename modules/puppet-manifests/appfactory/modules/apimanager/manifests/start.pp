define apimanager::start ( $target, $owner ) {
  exec { "starting_${name}":
    user    => $owner,
    path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
    unless  => "test -f ${target}/wso2carbon.lck",
    command => "touch ${target}/wso2carbon.lck; ${target}/bin/wso2server.sh > /dev/null 2>&1 &",
    creates => "${target}/repository/wso2carbon.log",
  }
}

