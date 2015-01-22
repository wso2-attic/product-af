define paaspuppet::deploy( $security, $target, $owner, $group) {

  file {
    "${target}":
      ensure          => present,
      owner           => $owner,
      group           => $group,
      sourceselect    => all,
      ignore          => '.svn',
      recurse         => true,
      replace         => "no",
      source          => ['puppet:///modules/paaspuppet/puppet/']
  }
}