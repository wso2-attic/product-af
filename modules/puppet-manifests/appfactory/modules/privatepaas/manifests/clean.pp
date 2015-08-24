define privatepaas::clean ( $mode, $target ) {
  if $mode == 'refresh' {
    exec{
      "Stop_process_${name}":
        path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
        command => "kill -9 `cat ${target}/install/apache-stratos-default/wso2carbon.pid` ; /bin/echo Killed",
    }
  }
  elsif $mode == 'new' {
    exec { "Stop_process_and_remove_CARBON_HOME_${name}":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "kill -9 `cat ${target}/install/apache-stratos-4.1.1/wso2carbon.pid` ; rm -rf ${target}";
    }
  }
}
