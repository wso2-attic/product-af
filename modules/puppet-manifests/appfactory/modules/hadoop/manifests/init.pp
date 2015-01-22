class hadoop ($namenode = true , $first_run = false) {
  $target = '/mnt'

  # root directory of hadoop_data and hadoop_tmp
  $hadoop_base     = '/mnt'
  $hadoop_package  = 'hadoop-1.2.1'
  $hadoop_home     = "${hadoop_base}/${hadoop_package}"
  $hadoop_user     = 'kurumba'
  $hadoop_group    = 'kurumba'
  $hadoop_heapsize = 1024
  $dfs_replication = 1

  $templates       = [ 'core-site.xml','hadoop-env.sh','hadoop-policy.xml','hdfs-site.xml','mapred-site.xml']

  define apply_templates ($directory) {
    file { "${directory}/conf/${name}":
      owner   => $hadoop_user,
      group   => $hadoop_group,
      content => template("hadoop/conf/${name}.erb");
    }
  }

  exec {
    "creating_target_for_${name}":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      command => "mkdir -p ${target}";

    "creating_local_package_repo_for_${name}":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      unless  => "test -d ${local_package_dir}",
      command => "mkdir -p ${local_package_dir}";

    "downloading_${hadoop_package}.tar.gz_for_${name}":
      path      => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd       => $local_package_dir,
      unless    => "test -f ${local_package_dir}/${hadoop_package}-bin.tar.gz",
      command   => "wget -q ${package_repo}/${hadoop_package}-bin.tar.gz",
      logoutput => 'on_failure',
      creates   => "${local_package_dir}/${hadoop_package}.tar.gz",
      timeout   => 0,
      require   => Exec["creating_local_package_repo_for_${name}", "creating_target_for_${name}"];

    "extracting_${hadoop_package}_for_${name}":
      path      => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd       => $target,
      unless    => "test -d ${target}/${hadoop_package}.tar.gz",
      command   => "tar xzf ${local_package_dir}/${hadoop_package}-bin.tar.gz",
      logoutput => 'on_failure',
      creates   => "${target}/${hadoop_package}/conf",
      timeout   => 0,
      require   => Exec["downloading_${hadoop_package}.tar.gz_for_${name}"];

    "changing_ownership_${hadoop_package}_${name}":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      command => "chown -R ${hadoop_user}:${hadoop_group} ${hadoop_home}",
      require => Exec["extracting_${hadoop_package}_for_${name}"];
  }

  file {
    "${target}/hadoop":
      ensure  => link,
      target  => "/mnt/${hadoop_package}",
      require => Exec["extracting_${hadoop_package}_for_${name}"];

    "/home/${hadoop_user}/.ssh/authorized_keys":
      ensure => present,
      source => 'puppet:///modules/hadoop/ssh/authorized_keys';
    
    "/home/${hadoop_user}/.ssh/config":
      ensure => present,
      source => 'puppet:///modules/hadoop/ssh/config';
  }

  file {
    ["${target}/hadoop_tmp","${target}/hadoop_data", "${target}/hadoop_data/dfs"]:
      ensure => directory,
      owner  => $hadoop_user,
      group  => $hadoop_group;
  }

  apply_templates { $templates:
      directory =>  "${hadoop_home}",
      require   => [ Exec["extracting_${hadoop_package}_for_${name}"],
                     Exec["changing_ownership_${hadoop_package}_${name}"]]
  }

  if $namenode == true {
    file {
      "${hadoop_home}/conf/masters":
        ensure  => present,
        content => template('hadoop/conf/masters.nn.erb'),
        require => Exec["extracting_${hadoop_package}_for_${name}"],
        owner   => $hadoop_user,
        group   => $hadoop_group;

      "${hadoop_home}/conf/slaves":
        ensure  => present,
        content => template('hadoop/conf/slaves.nn.erb'),
        require => Exec["extracting_${hadoop_package}_for_${name}"],
        owner   => $hadoop_user,
        group   => $hadoop_group;
    }
  } else {
    file { 
      "${hadoop_home}/conf/masters":
        ensure  => present,
        owner   => $hadoop_user,
        group   => $hadoop_group,
        content => template('hadoop/conf/masters.s.erb'),
        require => Exec["extracting_${hadoop_package}_for_${name}"];

      "${hadoop_home}/conf/slaves":
        ensure  => present,
        owner   => $hadoop_user,
        group   => $hadoop_group,
        content => template('hadoop/conf/slaves.s.erb'),
        require => Exec["extracting_${hadoop_package}_for_${name}"];
    }
  }

  #Starting Hadoop
  if $namenode == true {
    if $first_run == true {
      exec {
        "formating_${name}_namenode":
          user    => $hadoop_user,
          path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
          command => "${hadoop_home}/bin/hadoop namenode -format",
          require => [Apply_templates[$templates], File["${target}/hadoop_data/dfs"]];

         "strating_${name}_namenode":
            path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
            user    => $hadoop_user,
            command => "${hadoop_home}/bin/start-all.sh",
            require => [Apply_templates[$templates],Exec["formating_${name}_namenode"]];
      }
    } else {
        exec { "strating_${name}_namenode":
          path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
          user    => $hadoop_user,
          command => "${hadoop_home}/bin/start-all.sh",
          require => Apply_templates[$templates];
      }
    }
  }
  else {
    exec {
      "strating_${name}_datanode":
        path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
        user    => $hadoop_user,
        command => "${hadoop_home}/bin/hadoop-daemon.sh start datanode",
        require => Apply_templates[$templates];

      "strating_${name}_tasktracker":
        path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
        user    => $hadoop_user,
        command => "${hadoop_home}/bin/hadoop-daemon.sh start tasktracker",
        require => Apply_templates[$templates];
    }
  }
}
