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

  $jenkins_base_dir = "${base_dir}/jenkins"
  $jenkins_pack = "jenkins.war"
  $templates    = [
    'Configs/org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager.xml',
    'Configs/jenkins.model.JenkinsLocationConfiguration.xml',
    'Configs/org.wso2.carbon.appfactory.jenkins.extentions.AFLocalRepositoryLocator.xml',
    'Configs/hudson.plugins.git.GitSCM.xml',
    'Configs/config.xml',
    'Configs/user-config.xml',
    'run_jenkins.sh'
  ]
  exec {
    "create_dirs_for_${name}":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      command => "mkdir -p ${base_dir} ${local_package_dir}";

    "creating JenkinsHome":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      unless  => "test -d ${jenkins_base_dir}",
      cwd     => $base_dir,
      command => "mkdir -p ${jenkins_home}";

    "creating JenkinsUsersLocation":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      unless  => "test -d ${jenkins_base_dir}",
      cwd     => $base_dir,
      command => "mkdir -p ${jenkins_home}/users/${jenkins_admin_username}",
      require => Exec["creating JenkinsHome"];

    "copying_jenkins_user_configs":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      unless  => "test -d ${jenkins_base_dir}",
      cwd     => $base_dir,
      command => "mv ${jenkins_base_dir}/Configs/user-config.xml  ${jenkins_home}/users/${jenkins_admin_username}/config.xml",
      require => Exec["creating JenkinsUsersLocation"];

    "copying_jenkins_configs":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      unless  => "test -d ${jenkins_base_dir}",
      cwd     => $base_dir,
      command => "cp -r ${jenkins_base_dir}/Configs/*  ${jenkins_home}/",
      require => Exec["creating JenkinsHome"],
  }

  file {
    "${base_dir}/tmp":
      ensure => directory,
      require => Exec["creating JenkinsHome"];

    $jenkins_base_dir:
      owner   => $user,
      recurse => true,
      ignore  => '.svn',
      source  => 'puppet:///modules/jenkins',
      require => Exec["creating JenkinsHome"];
  }

  apply_templates {
    $templates:
      jenkins_home => $jenkins_base_dir,
      require      => Exec["creating JenkinsHome"];
  }

  exec {
    'start jenkins':
      path        => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin'],
      environment => "jenkins_base_dir=${jenkins_base_dir}",
      cwd         => $jenkins_home,
      user        => $user,
      command     => "mkdir -p ${jenkins_base_dir}/logs; ${jenkins_base_dir}/run_jenkins.sh",
      require     => [ Apply_templates[$templates], File[$jenkins_base_dir] ];
  }
}
