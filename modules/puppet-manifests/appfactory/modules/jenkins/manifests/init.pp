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
    'Configs/jenkins.model.JenkinsLocationConfiguration.xml'
    'Configs/org.wso2.carbon.appfactory.jenkins.extentions.AFLocalRepositoryLocator.xml.erb'
    'Configs/hudson.plugins.git.GitSCM.xml.erb'
    'Configs/config.xml.erb'
  ]
  exec {
    "create_dirs_for_${name}":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      command => "mkdir -p ${base_dir} ${local_package_dir}";

    "create_dirs_for_${jenkins_base_dir}/tmp":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      command => "mkdir -p ${jenkins_base_dir}/tmp";


    'download_jenkins':
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd     => $local_package_dir,
      command => "wget -q ${package_repo}/${jenkins_pack}",
      require => Exec["create_dirs_for_${name}"];

    "extract_jenkins":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      command => "unzip ${local_package_dir}/${jenkins_pack} -d {jenkins_base_dir}/tmp",
      require => Exec["download_jenkins"];

    "creating_proper_jenkins_war":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd     => $base_dir,
      command => "zip -rq jenkins.war ${jenkins_base_dir}/tmp/*",
      require => Exec["extract_jenkins"];

    "copying proper jenkins_war":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      cwd     => $base_dir,
      command => "cp jenkins.war ${jenkins_base_dir}/",
      require => Exec["creating_proper_jenkins_war"];

    "creating JenkinsHome":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      unless  => "test -d ${jenkins_base_dir}",
      cwd     => $base_dir,
      command => "mkdir -p ${jenkins_home}",

    "copying_jenkins_configs":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      unless  => "test -d ${jenkins_base_dir}",
      cwd     => $base_dir,
      command => "cp -r ${jenkins_base_dir}/Configs/*  ${jenkins_home}/",
      require => Exec["creating JenkinsHome"];
  }

  file {
    "${base_dir}/tmp":
      ensure => directory,
      require => Exec["create_dirs_for_${name}"];

    $jenkins_base_dir:
      owner   => $user,
      recurse => true,
      ignore  => '.svn',
      source  => 'puppet:///modules/jenkins',
      require => Exec["extract_jenkins"];
  }

  apply_templates {
    $templates:
      jenkins_base_dir => $jenkins_base_dir,
      require      => Exec["extract_jenkins"];
  }

  exec {
    'start jenkins':
      path        => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin'],
      environment => "jenkins_base_dir=${jenkins_base_dir}",
      cwd         => $jenkins_base_dir,
      user        => $user,
      command     => "mkdir -p ${jenkins_base_dir}/logs; ${jenkins_base_dir}/run_jenkins.sh",
      require     => [ Apply_templates[$templates], File[$jenkins_base_dir] ];
  }
}

