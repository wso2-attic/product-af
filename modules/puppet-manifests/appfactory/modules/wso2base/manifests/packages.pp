class wso2base::packages {

  $packages = ['lsof','unzip','sysstat','telnet', 'git', 'less', 'tree', 'zip', 'curl']

  package { $packages:
    ensure => installed,
  }

}
