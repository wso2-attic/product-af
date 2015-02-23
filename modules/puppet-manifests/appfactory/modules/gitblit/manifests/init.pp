# Class: gitblit
#
# Install and configure gitblit
#

class gitblit (
  $version      = '1.4.1',
  $base_dir     = '/mnt',
  $admin_uname  = 'root',
  $admin_passwd = 'root',
  $user         = 'root',
  $group        = 'root',
){

  $gitblit_home = "${base_dir}/gitblit"
  $gitblit_pack = "gitblit-${version}.zip"
  $templates    = [
                    'start-gitblit.sh',
                    'data/gitblit.properties',
                    'data/groovy/jenkins.groovy',
                    'data/groovy/notifycommits.groovy',
                    'data/groovy/pre-commit-validator.groovy'
                ]

  exec {
    "create_dirs_for_${name}":
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      command => "mkdir -p ${base_dir} ${local_package_dir}";

    'download_gitblit':
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      unless  => "test -f ${local_package_dir}/${gitblit_pack}",
      cwd     => $local_package_dir,
      command => "wget -q ${package_repo}/${gitblit_pack}",
      require => Exec["create_dirs_for_${name}"];

    'extract_gitblit':
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      unless  => "test -d ${gitblit_home}",
      cwd     => $base_dir,
      command => "unzip ${local_package_dir}/${gitblit_pack} -d ${gitblit_home}",
      require => Exec['download_gitblit'];
  }

  file {
    "${gitblit_home}/tmp":
      ensure  => directory,
      owner   => $user,
      group   => $group,
      require => Exec["create_dirs_for_${name}"];

    $gitblit_home:
      owner   => $user,
      recurse => true,
      ignore  => '.svn',
      source  => 'puppet:///modules/gitblit',
      require => Exec['extract_gitblit'];
  }

  apply_templates {
    $templates:
      gitblit_home => $gitblit_home,
      require      => Exec['extract_gitblit'];
  }

  exec {
    'start gitblit':
      path        => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin'],
      environment => "GITBLIT_HOME=${gitblit_home}",
      cwd         => $gitblit_home,
      user        => $user,
      command     => "mkdir -p ${gitblit_home}/logs; /bin/bash start-gitblit.sh",
      require     => [ Apply_templates[$templates], File[$gitblit_home] ];
  }
}
