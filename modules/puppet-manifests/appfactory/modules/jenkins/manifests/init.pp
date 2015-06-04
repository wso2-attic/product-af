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
  $jenkins_pack_location = "${jenkins_base_dir}/${jenkins_package_name}"
  $templates    = [
    'Configs/org.wso2.carbon.appfactory.jenkins.AppfactoryPluginManager.xml',
    'Configs/jenkins.model.JenkinsLocationConfiguration.xml',
    'Configs/org.wso2.carbon.appfactory.jenkins.extentions.AFLocalRepositoryLocator.xml',
    'Configs/hudson.plugins.git.GitSCM.xml',
    'Configs/config.xml',
    'Configs/user-config.xml',
    'jenkins.sh'
  ]

  exec {
    "download_jenkins":
      path => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd => $jenkins_base_dir,
      command => "wget -q ${package_repo}/${jenkins_package_name}";

    "create_dirs_for_${name}":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      command => "mkdir -p ${base_dir} ${local_package_dir}";

    "creating_jenkins_home":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd     => $base_dir,
      command => "mkdir -p ${jenkins_home}";

    "creating_jenkins_users_location":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd     => $base_dir,
      command => "mkdir -p ${jenkins_home}/users/${jenkins_admin_username}",
      require => Exec["copying_jenkins_configs"];

    "copying_jenkins_user_configs":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd     => $base_dir,
      command => "mv ${jenkins_base_dir}/Configs/user-config.xml  ${jenkins_home}/users/${jenkins_admin_username}/config.xml",
      require => Exec["creating_jenkins_users_location"];

    "copying_jenkins_configs":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd     => $base_dir,
      command => "cp -r ${jenkins_base_dir}/Configs/*  ${jenkins_home}/",
      require => Apply_templates[$templates],
  }

  file {
    $jenkins_base_dir:
      owner   => $user,
      group   => $user,
      mode   => '0755',
      recurse => true,
      ignore  => '.svn',
      source  => 'puppet:///modules/jenkins',
      require => Exec["creating_jenkins_home"];

    $jenkins_pack_location:
      owner   => $user,
      group   => $user,
      recurse => true,
      mode => '0755',
      require => Exec["download_jenkins"];
  }

  apply_templates {
    $templates:
      jenkins_base_dir => $jenkins_base_dir,
      require      => Exec["creating_jenkins_home"];
  }

  exec {
    'start jenkins':
      path        => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin'],
      environment => "jenkins_home=${jenkins_home}",
      cwd         => $jenkins_home,
      user        => $user,
      command     => "mkdir -p ${jenkins_home}/logs; /bin/bash ${jenkins_base_dir}/jenkins.sh",
      require     => [ Apply_templates[$templates], File[$jenkins_base_dir], File[$jenkins_pack_location], Exec["copying_jenkins_user_configs"], Exec["download_jenkins"]];
  }
}
