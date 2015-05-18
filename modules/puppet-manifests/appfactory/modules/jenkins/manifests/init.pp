# Class: jenkins
#
# Install and configure jenkins
#

class jenkins (
  $base_dir     = '/mnt',
  $admin_uname  = 'root',
  $admin_passwd = 'root',
  $user         = 'root',
  $group        = 'root',
){

  $jenkins_home = "${base_dir}/jenkins"
  $jenkins_pack = "jenkins.war"
  $templates    = [
    'Configs/org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager.xml',
    'Configs/jenkins.model.JenkinsLocationConfiguration.xml'
  ]

  exec {
    "create_dirs_for_${name}":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      command => "mkdir -p ${base_dir} ${local_package_dir}";

    "create_dirs_for_${jenkins_home}/tmp":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      command => "mkdir -p ${jenkins_home}/tmp";


    'download_jenkins':
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd     => $local_package_dir,
      command => "wget -q ${package_repo}/${jenkins_pack}",
      require => Exec["create_dirs_for_${name}"];

    "extract_jenkins":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      command => "unzip ${local_package_dir}/${jenkins_pack} -d {jenkins_home}/tmp",
      require => Exec["download_jenkins"];

    "creating_proper_jenkins_war":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd     => $base_dir,
      command => "zip -rq jenkins.war /mnt/10.100.1.75/jenkins/tmp/*",
      require => Exec["extract_jenkins"];

    "create_dirs_for_${jenkins_home}/run":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      command => "mkdir -p ${jenkins_home}/run";

    "copying proper jenkins_war":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd     => $base_dir,
      command => "cp jenkins.war ${jenkins_home}/run/",
      require => Exec["creating_proper_jenkins_war"];

    "creating JenkinsHome":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      unless  => "test -d ${jenkins_home}",
      cwd     => $base_dir,
      command => "mkdir -p ${jenkins_home}/run/JenkinsHome",
      require => Exec["create_dirs_for_${jenkins_home}/run"];

    "copying_jenkins_configs":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      unless  => "test -d ${jenkins_home}",
      cwd     => $base_dir,
      command => "cp -r ${jenkins_home}/Configs/*  ${jenkins_home}/run/JenkinsHome/",
      require => Exec["creating JenkinsHome"];
  }

  file {
    "${base_dir}/tmp":
      ensure => directory,
      require => Exec["create_dirs_for_${name}"];

    $jenkins_home:
      owner   => $user,
      recurse => true,
      ignore  => '.svn',
      source  => 'puppet:///modules/jenkins',
      require => Exec["extract_jenkins"];
  }

  apply_templates {
    $templates:
      jenkins_home => $jenkins_home,
      require      => Exec["extract_jenkins"];
  }

  exec {
    'start jenkins':
      path        => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin'],
      environment => "jenkins_home=${jenkins_home}",
      cwd         => $jenkins_home,
      user        => $user,
      command     => "mkdir -p ${jenkins_home}/logs; ${jenkins_home}/run_jenkins.sh",
      require     => [ Apply_templates[$templates], File[$jenkins_home] ];
  }
}
