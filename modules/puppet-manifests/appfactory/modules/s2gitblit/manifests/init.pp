# Class: s2gitblit
#
# Install and configure gitblit
#

class s2gitblit (
  $version      = '1.4.1',
  $base_dir     = '/mnt',
  $admin_uname  = 'root',
  $admin_passwd = 'root',
  $user         = 'root',
  $group        = 'root',
  $server_ip    = undef
){

  $gitblit_home = "${base_dir}/s2gitblit"
  $gitblit_pack = "gitblit-${version}.zip"
  $templates    = [
                    'start-gitblit.sh',
                    'data/groovy/notifys2.groovy',
                    'data/gitblit.properties',
                    'user.conf',
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

    'set_gitblit_permission':
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin'],
      command => "chown -R ${user}:${group} ${gitblit_home}",
      require => Exec['extract_gitblit'];
  }

  file {
    "${base_dir}/tmp":
      ensure => directory,
      require => Exec["create_dirs_for_${name}"];

    $gitblit_home:
      owner   => $user,
      group   => $group,
      recurse => true,
      ignore  => '.svn',
      source  => 'puppet:///modules/s2gitblit',
      require => Exec['extract_gitblit'];
  }

  apply_templates {
    $templates:
      gitblit_home => $gitblit_home,
      require      => Exec['set_gitblit_permission'];
  }

  exec {
    'start gitblit':
      path    => ['/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin'],
      cwd     => $gitblit_home,
      user    => $user,
      command => "mkdir -p ${gitblit_home}/logs; /bin/bash start-gitblit.sh",
      require => [ Apply_templates[$templates], File[$gitblit_home] ];
  }
}
