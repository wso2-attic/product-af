define privatepaas::deploy-sm ( $security, $target, $owner, $group, $local_dir) {

  file {
    "${target}/install/apache-stratos-default":
      ensure          => present,
      owner           => $owner,
      group           => $group,
      sourceselect    => all,
      ignore          => '.svn',
      recurse         => true,
      source          => ['puppet:///modules/privatepaas/appfactory_deployment/']
  }
}
