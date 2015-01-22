define privatepaas::deploy ( $security, $target, $owner, $group, $local_dir) {

  file {
    "${target}":
      ensure          => present,
      owner           => $owner,
      group           => $group,
      sourceselect    => all,
      ignore          => '.svn',
      recurse         => true,
      source          => ['puppet:///modules/privatepaas/stratos/']
  }

#  We are using /mnt/packs as the packs directory
  exec {
    "Copy_${name}_modules_to_privatepaas_home":
      path    => '/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin:/opt/java/bin/',
      command => "chown -R ${owner}:${owner} ${local_dir}/; chmod -R 755 ${local_dir}/",
      require => File["${target}"];
  }
}
